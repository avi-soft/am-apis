package com.community.api.endpoint.avisoft.controller.ServiceProvider;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CommunicationRequest;
import com.community.api.dto.CreateTicketDto;
import com.community.api.endpoint.avisoft.controller.Acknowledgement.AcknowledgementWebhook;
import com.community.api.endpoint.avisoft.controller.Customer.CustomerEndpoint;
import com.community.api.endpoint.avisoft.controller.ServiceProviderActionController;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.*;
import com.community.api.services.*;
import com.community.api.services.ServiceProvider.ServiceProviderRankService;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.ServiceProviderDocument;
import com.mchange.rmi.NotAuthorizedException;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.broadleafcommerce.core.order.service.OrderService;

import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.community.api.services.ServiceProvider.ServiceProviderServiceImpl.getLongList;

@Slf4j
@RestController
@RequestMapping("/service-providers")
public class ServiceProviderController {

    @Autowired
    CustomerEndpoint customerEndpoint;
    @Autowired
    ExceptionHandlingService exceptionHandlingService;
    @Autowired
    QualificationService qualificationService;
    @Autowired
    private ServiceProviderServiceImpl serviceProviderService;
    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private OrderStatusByStateService orderStatusByStateService;
    @Value("${twilio.accountSid}")
    private String accountSid;
    @Value("${twilio.authToken}")
    private String authToken;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TwilioServiceForServiceProvider twilioService;
    @Autowired
    private DocumentStorageService documentStorageService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    @Autowired
    private SanitizerService sanitizerService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ReserveCategoryDtoService reserveCategoryDtoService;
    @Autowired
    private PhysicalRequirementDtoService physicalRequirementDtoService;
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PrivilegeService privilegeService;
    private StatusChangeEmailService statusChangeEmailService;
    @Autowired
    private ServiceProviderRankService serviceProviderRankService;

    @Autowired
    public void setStatusChangeEmailService(StatusChangeEmailService statusChangeEmailService) {
        this.statusChangeEmailService = statusChangeEmailService;
    }
    /*@Autowired
    private DummyAssignerService dummyAssignerService;*/

    @Transactional
    @PostMapping("/assign-skill")
    public ResponseEntity<?> addSkill(@RequestParam Long serviceProviderId, @RequestParam int skillId) {
        try {
            Skill skill = entityManager.find(Skill.class, skillId);
            ServiceProviderEntity serviceProviderEntity = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (serviceProviderEntity.getIsArchived().equals(true))
                return ResponseService.generateErrorResponse("SP is archived", HttpStatus.NOT_FOUND);
            List<Skill> listOfSkills = serviceProviderEntity.getSkills();
            listOfSkills.add(skill);
            serviceProviderEntity.setSkills(listOfSkills);
            entityManager.merge(serviceProviderEntity);
            return responseService.generateSuccessResponse("Skill assigned to service provider id : " + serviceProviderEntity.getService_provider_id(), serviceProviderEntity, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error assigning skill: " + e.getMessage());
        }
    }

    @Transactional
    @PutMapping("save-service-provider")
    public ResponseEntity<?> updateServiceProvider(@RequestParam Long userId, @RequestBody Map<String, Object> serviceProviderDetails, @RequestHeader(value = "Authorization") String authHeader) throws Exception {
        try {
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, userId);
            if (serviceProvider == null)
                return ResponseService.generateErrorResponse("Service Provider with provided Id not found", HttpStatus.NOT_FOUND);
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);

            if (role.getRole_name().equals(Constant.SERVICE_PROVIDER) && (serviceProvider.getRole() != roleId || !Objects.equals(tokenUserId, userId)))
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
            if (serviceProvider.getIsArchived().equals(true))
                return ResponseService.generateErrorResponse("SP is archived", HttpStatus.NOT_FOUND);
            return serviceProviderService.updateServiceProvider(userId, serviceProviderDetails, authHeader);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error updating: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Autowired
    AcknowledgementWebhook webhook;
    @Authorize(value = {Constant.roleAdmin, Constant.roleServiceProvider, Constant.roleAdminServiceProvider, Constant.roleSuperAdmin})
    @Transactional
    @PutMapping("{spId}/submit-profile")
    public ResponseEntity<?> submitProfie(@PathVariable Long spId, @RequestHeader(value = "Authorization") String authHeader) throws Exception {
        String jwtToken = authHeader.substring(7);
        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
        Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, spId);
        if (serviceProvider.getRole() != roleId && !Objects.equals(tokenUserId, spId))
            return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
        if (serviceProvider == null)
            return ResponseService.generateErrorResponse("Need to provide service provider id", HttpStatus.BAD_REQUEST);


        // List of required fields
        List<String> REQUIRED_FIELDS = Arrays.asList(
                "first_name",                   // @NotEmpty + @Pattern
                "last_name",                    // @NotEmpty + @Pattern
                "father_name",                 // @NotEmpty + @Pattern
                "mother_name",                 // @NotEmpty + @Pattern
                "date_of_birth",              // @NotEmpty
                "aadhaar_number",             // @NotEmpty + @Size + @Pattern
                "pan_number",                 // @NotEmpty + @Size + @Pattern (despite @Nullable)
                "mobileNumber",               // @Size(min=9, max=13)
                "whatsapp_number",           // @NotEmpty + @Size + @Pattern
                "primary_email",             // @NotEmpty + @Email
//                "password",                  // @NotEmpty + @JsonIgnore
                "is_running_business_unit"// @NotEmpty (despite @Nullable)
        );
        //validating all required fields
        for (String fieldName : REQUIRED_FIELDS) {
            try {
                Field field = ServiceProviderEntity.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                if (field.get(serviceProvider) == null) {
                    return ResponseService.generateErrorResponse(fieldName + " is not filled", HttpStatus.BAD_REQUEST);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
/*        if(serviceProvider.getQualificationDetailsList().isEmpty())
            return ResponseService.generateErrorResponse("Highest qualification not filled",HttpStatus.BAD_REQUEST);*/
        boolean hasCurrent = false;
        boolean hasPermanent = false;
        boolean hasBusinessAddress= false;

        for (ServiceProviderAddress addr : serviceProvider.getSpAddresses()) {
            if (addr.getAddress_type_id() == Constant.CURRENT_ADDRESS_ID) {
                hasCurrent = true;
            } else if (addr.getAddress_type_id() == Constant.PERMANENT_ADDRESS_ID) {
                hasPermanent = true;
            }
            else if(addr.getAddress_type_id()== Constant.OFFICE_ADDRESS_ID)
            {
                hasBusinessAddress=true;
            }
        }

        if(!hasBusinessAddress)
            if(serviceProvider.getIs_running_business_unit().equals(true))
            {
                return ResponseService.generateErrorResponse("Business address is not filled",HttpStatus.BAD_REQUEST);
            }
        if (!hasCurrent && !hasPermanent)
            return ResponseService.generateErrorResponse("Both current and permanent address are not filled", HttpStatus.BAD_REQUEST);
        else if (!hasPermanent)
            return ResponseService.generateErrorResponse("Permanent address is not filled", HttpStatus.BAD_REQUEST);
        else if (!hasCurrent)
            return ResponseService.generateErrorResponse("Current address is not filled", HttpStatus.BAD_REQUEST);

        if (serviceProvider.getIs_running_business_unit().equals(true)) {
            REQUIRED_FIELDS = Arrays.asList(
                    "business_name",
                    "business_email",
                    "number_of_employees",
                    "isCFormAvailable",
                    "has_technical_knowledge",
                    "work_experience_in_months"
            );
            for (String fieldName : REQUIRED_FIELDS) {
                try {
                    Field field = ServiceProviderEntity.class.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    if (field.get(serviceProvider) == null) {
                        return ResponseService.generateErrorResponse(fieldName + " is not filled", HttpStatus.BAD_REQUEST);
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
                }
            }
            if (serviceProvider.getIsCFormAvailable() && serviceProvider.getRegistration_number() == null)
                return ResponseService.generateErrorResponse("Registeration number is not filled", HttpStatus.BAD_REQUEST);
            if (serviceProvider.getInfra().isEmpty())
                return ResponseService.generateErrorResponse("Infra list cannot be empty", HttpStatus.BAD_REQUEST);
            if (serviceProvider.getHas_technical_knowledge() && serviceProvider.getSkills().isEmpty())
                return ResponseService.generateErrorResponse("Skill list cannot be empty", HttpStatus.BAD_REQUEST);
            if (bankAccountService.getBankAccountsByCustomerId(serviceProvider.getService_provider_id(), 4).isEmpty())
                return ResponseService.generateErrorResponse("Bank account Not added", HttpStatus.BAD_REQUEST);
            Map<String, Integer> docMap = new HashMap<>();

            docMap.put("Aadhaar_Card_Front", 1);
            docMap.put("Aadhaar_Card_Backside", 1);
            docMap.put("Signature", 1);
            docMap.put("Pan_Card", 1);
            if (!serviceProvider.getPfpNa()) {
                docMap.put("Personal_Photo", 1);
            }
            for (ServiceProviderDocument document : serviceProvider.getDocuments()) {
                System.out.println(document.getDocumentType().getDocument_type_name());
                if (docMap.containsKey(document.getDocumentType().getDocument_type_name())) {
                    docMap.put(document.getDocumentType().getDocument_type_name(), 0);
                }
            }
            for (Map.Entry<String, Integer> entry : docMap.entrySet()) {
                if (entry.getValue() == 1)
                    return ResponseService.generateErrorResponse(entry.getKey() + " is not uploaded", HttpStatus.BAD_REQUEST);
            }
        }
        serviceProvider.setCompleted(true);
        serviceProvider.setRejected(false);
     /*   if(!webhook.checkRef(spId,4))
        {
            return ResponseService.generateErrorResponse("User has not acknowledged the policy",HttpStatus.BAD_REQUEST);
        }*/
        /*SPAcknowledgement userAcknowledgement=new SPAcknowledgement();
        userAcknowledgement.setAcknowledgedAt(new Date());
        userAcknowledgement.setUserId(spId);
        userAcknowledgement.setAcknowledgementVersion("v.1");
        entityManager.persist(userAcknowledgement);*/
        entityManager.merge(serviceProvider);
        return ResponseService.generateSuccessResponse("Details validated Successfully", sharedUtilityService.serviceProviderDetailsMap(serviceProvider,false), HttpStatus.OK);
    }

    @Transactional
    @DeleteMapping("delete")
    public ResponseEntity<?> deleteServiceProvider(@RequestParam Long userId) {
        try {
            ServiceProviderEntity serviceProviderToBeDeleted = entityManager.find(ServiceProviderEntity.class, userId);
            if (serviceProviderToBeDeleted == null)
                return responseService.generateErrorResponse("No record found", HttpStatus.NOT_FOUND);
            else
                serviceProviderToBeDeleted.setIsArchived(true);
            entityManager.merge(serviceProviderToBeDeleted);
            return responseService.generateSuccessResponse("Service Provider Archived", null, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting: " + e.getMessage());
        }
    }

    @Transactional
    @PostMapping("create-or-update-password")
    public ResponseEntity<?> deleteServiceProvider(@RequestBody Map<String, Object> passwordDetails, @RequestParam long userId) {
        try {
            if (!sharedUtilityService.validateInputMap(passwordDetails).equals(SharedUtilityService.ValidationResult.SUCCESS)) {
                return ResponseService.generateErrorResponse("Invalid Request Body", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            String password = (String) passwordDetails.get("password");
            passwordDetails = sanitizerService.sanitizeInputMap(passwordDetails);
            // String newPassword = (String) passwordDetails.get("newPassword");
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, userId);
            if (serviceProvider.getIsArchived().equals(true))
                return ResponseService.generateErrorResponse("SP is archived", HttpStatus.NOT_FOUND);
            if (serviceProvider == null)
                return responseService.generateErrorResponse("No records found", HttpStatus.NOT_FOUND);
            if (serviceProvider.getPassword() == null) {
                serviceProvider.setPassword(passwordEncoder.encode(password));
                serviceProvider.setIsPasswordCreated(true);
                entityManager.merge(serviceProvider);
                return responseService.generateSuccessResponse("Password created", serviceProvider, HttpStatus.OK);
            } else {
                if (password == null /*|| newPassword == null*/)
                    return responseService.generateErrorResponse("Empty password entered", HttpStatus.BAD_REQUEST);
                /*if (passwordEncoder.matches(password, serviceProvider.getPassword())) {
                    serviceProvider.setPassword(passwordEncoder.encode(newPassword));*/
                if (!passwordEncoder.matches(password, serviceProvider.getPassword())) {
                    serviceProvider.setPassword(passwordEncoder.encode(password));
                    entityManager.merge(serviceProvider);
                    return responseService.generateSuccessResponse("New Password Set", serviceProvider, HttpStatus.OK);
                }
                return responseService.generateErrorResponse("Old Password and new Password cannot be same", HttpStatus.BAD_REQUEST);
            }/*else
                    return new ResponseEntity<>("Password do not match", HttpStatus.BAD_REQUEST);*/
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error changing/updating password: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("get-service-provider")
    public ResponseEntity<?> getServiceProviderById(@RequestParam Long userId) throws Exception {
        try {
            ServiceProviderEntity serviceProviderEntity = serviceProviderService.getServiceProviderById(userId);
            if (serviceProviderEntity.getIsArchived().equals(true))
                return ResponseService.generateErrorResponse("SP is archived", HttpStatus.NOT_FOUND);
            if (serviceProviderEntity == null) {
                throw new Exception("ServiceProvider with ID " + userId + " not found");
            }
            return ResponseEntity.ok(serviceProviderEntity);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some fetching account " + e.getMessage());
        }
    }

    @Transactional
    @PostMapping("/add-address")
    public ResponseEntity<?> addAddress(@RequestParam long serviceProviderId, @RequestBody ServiceProviderAddress serviceProviderAddress) throws Exception {
        try {
            if (serviceProviderAddress == null) {
                return responseService.generateErrorResponse("Incomplete Details", HttpStatus.BAD_REQUEST);
            }
            ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (existingServiceProvider == null) {
                return responseService.generateErrorResponse("Service Provider Not found", HttpStatus.BAD_REQUEST);
            }
            List<ServiceProviderAddress> addresses = existingServiceProvider.getSpAddresses();
            serviceProviderAddress.setState(districtService.findStateById(Integer.parseInt(serviceProviderAddress.getState())));
            serviceProviderAddress.setDistrict(districtService.findDistrictById(Integer.parseInt(serviceProviderAddress.getDistrict())));
            addresses.add(serviceProviderAddress);
            existingServiceProvider.setSpAddresses(addresses);
            serviceProviderAddress.setServiceProviderEntity(existingServiceProvider);
            entityManager.persist(serviceProviderAddress);
            entityManager.merge(existingServiceProvider);
            return responseService.generateSuccessResponse("Address added successfully", serviceProviderAddress, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error adding address " + e.getMessage());
        }
    }

    @GetMapping("/get-address-names")
    public ResponseEntity<?> getAddressTypes() {
        try {
            TypedQuery<ServiceProviderAddressRef> query = entityManager.createQuery(Constant.jpql, ServiceProviderAddressRef.class);
            return responseService.generateSuccessResponse("List of addresses : ", query.getResultList(), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some issue in fetching addressNames " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @Transactional
    @Authorize(value = {Constant.roleSuperAdmin, Constant.roleAdmin, Constant.roleAdminServiceProvider})
    @GetMapping("/get-all-service-providers")
    public ResponseEntity<?> getAllServiceProviders(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(required = false) String superAdmin,
            @RequestParam(required = false) String admin,
            @RequestParam(required = false) String spAdmin,
            @RequestParam(required = false) String sp,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "30") int limit,
            HttpServletRequest request) {
        try {
            if (offset < 0) {
                return ResponseService.generateErrorResponse("Offset for pagination cannot be a negative number", HttpStatus.BAD_REQUEST);
            }
            if (limit <= 0) {
                return ResponseService.generateErrorResponse("Limit for pagination cannot be a negative number or 0", HttpStatus.BAD_REQUEST);
            }

            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);

            Map<String, String[]> params = request.getParameterMap();

            // Validate params
            for (String paramKey : Arrays.asList("admin", "spAdmin", "superAdmin", "sp")) {
                if (params.containsKey(paramKey)) {
                    String val = request.getParameter(paramKey);
                    if (!"true".equalsIgnoreCase(val) && !"false".equalsIgnoreCase(val)) {
                        return ResponseService.generateErrorResponse(
                                "Invalid value '" + val + "' for parameter '" + paramKey + "'. Must be 'true' or 'false'",
                                HttpStatus.BAD_REQUEST);
                    }
                }
            }

            int startPosition = offset * limit;
            List<Integer> rolesToFetch = new ArrayList<>();
            List<String> roleLabels = new ArrayList<>();

            if ("true".equalsIgnoreCase(superAdmin)) rolesToFetch.add(1);
            if ("true".equalsIgnoreCase(admin)) rolesToFetch.add(2);
            if ("true".equalsIgnoreCase(spAdmin)) rolesToFetch.add(3);
            if ("true".equalsIgnoreCase(sp)) rolesToFetch.add(4);

            // If no specific filter provided, default by roleId
            if (rolesToFetch.isEmpty()) {
                if (roleId == 1) {
                    rolesToFetch.addAll(Arrays.asList(1, 2, 3, 4));
                } else if (roleId == 2) {
                    rolesToFetch.addAll(Arrays.asList(2, 3, 4));
                } else if (roleId == 3) {
                    rolesToFetch.addAll(Arrays.asList(3, 4));
                }
            }

            // Permission check based on roleId
            if ((rolesToFetch.contains(1) || rolesToFetch.contains(2) || rolesToFetch.contains(3)) && roleId != 1) {
                if (rolesToFetch.contains(1)) {
                    return ResponseService.generateErrorResponse("Only super admin can access super admin list", HttpStatus.FORBIDDEN);
                }
                if (rolesToFetch.contains(2) && roleId > 2) {
                    return ResponseService.generateErrorResponse("Only admin and super admin can access admin list", HttpStatus.FORBIDDEN);
                }
                if (rolesToFetch.contains(3) && roleId > 3) {
                    return ResponseService.generateErrorResponse("Only spAdmin, admin, or super admin can access sp-admin list", HttpStatus.FORBIDDEN);
                }
            }

            // Compose query
            String baseQuery = "FROM ServiceProviderEntity s WHERE s.role IN :roles";
            if (rolesToFetch.contains(4)) {
                baseQuery += " AND (s.role != 4 OR s.isArchived = false)";
            }

            Query countQuery = entityManager.createQuery("SELECT COUNT(s) " + baseQuery);
            countQuery.setParameter("roles", rolesToFetch);
            long totalItems = (long) countQuery.getSingleResult();
            long totalPages = (int) Math.ceil((double) totalItems / limit);

            if (offset >= totalPages && offset != 0) {
                return ResponseService.generateErrorResponse("No more service providers available", HttpStatus.BAD_REQUEST);
            }

            Query dataQuery = entityManager.createQuery("SELECT s " + baseQuery, ServiceProviderEntity.class);
            dataQuery.setParameter("roles", rolesToFetch);
            dataQuery.setFirstResult(startPosition);
            dataQuery.setMaxResults(limit);

            List<ServiceProviderEntity> results = dataQuery.getResultList();
            List<Map<String, Object>> resultOfSp = new ArrayList<>();
            for (ServiceProviderEntity serviceProvider : results) {
                resultOfSp.add(sharedUtilityService.serviceProviderDetailsMap(serviceProvider,false));
            }

            // Create success message
            for (Integer role : rolesToFetch) {
                switch (role) {
                    case 1:
                        roleLabels.add("SuperAdmin");
                        break;
                    case 2:
                        roleLabels.add("Admin");
                        break;
                    case 3:
                        roleLabels.add("SP-Admin");
                        break;
                    case 4:
                        roleLabels.add("Service Providers");
                        break;
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("serviceProviders", resultOfSp);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            String successMessage = "List of " + String.join(", ", roleLabels);
            return ResponseService.generateSuccessResponse(successMessage, response, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service providers: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @GetMapping("/get-all-details/{serviceProviderId}")
    public ResponseEntity<?> getAllDetails(@PathVariable Long serviceProviderId) {
        try {
            ServiceProviderEntity serviceProviderEntity = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (serviceProviderEntity == null) {
                return ResponseService.generateErrorResponse("Service provider does not found", HttpStatus.NOT_FOUND);
            }

            Map<String, Object> serviceProviderMap = sharedUtilityService.serviceProviderDetailsMap(serviceProviderEntity,false);
            return ResponseService.generateSuccessResponse("Service Provider details retrieved successfully", serviceProviderMap, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service provider details " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @GetMapping("/get-all-service-providers-with-completed-test")
    public ResponseEntity<?> getAllServiceProvidersWithCompletedTest(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int limit) {
        try {
            int startPosition = page * limit;

            TypedQuery<ServiceProviderEntity> query = entityManager.createQuery(
                    "SELECT s FROM ServiceProviderEntity s WHERE s.testStatus.test_status_id = :testStatusId",
                    ServiceProviderEntity.class);

            query.setParameter("testStatusId", Constant.TEST_COMPLETED_STATUS);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);

            List<ServiceProviderEntity> results = query.getResultList();
            if (results.isEmpty()) {
                return ResponseService.generateSuccessResponse("There is no any service Provider who has completed the test", results, HttpStatus.OK);
            }

            return ResponseService.generateSuccessResponse("List of service providers with completed test status: ", results, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service providers: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin, Constant.roleAdmin, Constant.roleServiceProvider, Constant.roleAdminServiceProvider, Constant.roleUser})
    @Transactional
    @GetMapping("/filter-service-provider")
    public ResponseEntity<?> filterServiceProvider(
            @RequestParam(required = false) List<String> state,
            @RequestParam(required = false) List<String> district,
            @RequestParam(required = false) String full_name,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) Long test_status_id,
            @RequestParam(required = false) String user_name,
            @RequestParam(required = false) List<Integer> qualificationType,
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Boolean suspended,
            @RequestParam(required = false) Boolean approved,
            @RequestParam(required = false) Boolean rejected,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) List<Long> rank,
            @RequestParam(required = false) String type,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "30") int limit,
            @RequestParam(value = "a4dh",defaultValue = "false")Boolean ext,
            @RequestParam(required = false) Long ticketId,
            @RequestParam(required = false, defaultValue = "DESC") String sortOrder,
            HttpServletRequest request) {

        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role roleName = roleService.getRoleByRoleId(roleId);
            System.out.println("ticketId" + ticketId);
            Map<String, String[]> uri = request.getParameterMap();
            if (role != null && (role <= roleId && roleId != 5)&&Boolean.TRUE.equals(!ext))
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);

            // Validate input
            if ((uri.containsKey("state") && (state == null || state.isEmpty())) ||
                    (uri.containsKey("district") && (district == null || district.isEmpty())) ||
                    (uri.containsKey("test_status_id") && test_status_id == null) ||
                    (uri.containsKey("district") && district == null) ||
                    (uri.containsKey("userName") && user_name == null) ||
                    (uri.containsKey("qualificationType") && qualificationType == null) ||
                    (uri.containsKey("mobileNumber") && mobileNumber == null) ||
                    (uri.containsKey("type") && type == null) ) {
                return ResponseService.generateErrorResponse("Empty fields are not accepted", HttpStatus.BAD_REQUEST);
            }
            if (role != null && role == 2 && (!roleName.getRole_name().equals(Constant.roleSuperAdmin))) {
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
            }
            if (role != null && role == 1 && (!roleName.getRole_name().equals(Constant.roleSuperAdmin))) {
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
            }
            // Validate full_name (only alphabets and spaces allowed)
            if (full_name != null && !full_name.matches("^[a-zA-Z ]+$")) {
                return ResponseService.generateErrorResponse("Full name cannot contain digits or special characters", HttpStatus.BAD_REQUEST);
            }

            if (user_name != null && !user_name.matches("^[a-zA-Z0-9]+$")) {
                return ResponseService.generateErrorResponse("Username can only contain letters and numbers", HttpStatus.BAD_REQUEST);
            }

            if (mobileNumber != null && !mobileNumber.matches("^[0-9]+$")) {
                return ResponseService.generateErrorResponse("Mobile number must contain digits only", HttpStatus.BAD_REQUEST);
            }

            if (type != null && !type.matches("[A-Z]+$")) {
                return ResponseService.generateErrorResponse("Service Provider Type must contains Uppercase letter only and nothing else.", HttpStatus.BAD_REQUEST);
            }

            String first_name = null;
            String last_name = null;

            List<Long> qualificationNames = new ArrayList<>();
            List<String> qualificationStrings = new ArrayList<>();

            // Handle search by mobile number
            if (mobileNumber != null && !mobileNumber.isEmpty() && serviceProviderService.isValidMobileNumber(mobileNumber)) {
                ResponseEntity<SuccessResponse> response = (ResponseEntity<SuccessResponse>) serviceProviderService.searchServiceProviderBasedOnGivenFields(state, district, first_name, last_name, mobileNumber, test_status_id, ticketId, role, completed, suspended, approved, rejected, user_name, qualificationType, rank, type);
                List<Map<String, Object>> resultList = new ArrayList<>();
                if (response.getBody() != null && response.getBody().getData() != null) {
                    resultList = (List<Map<String, Object>>) response.getBody().getData();
                }

                int totalItems = resultList.size();
                int totalPages = (int) Math.ceil((double) totalItems / limit);
                int currentPage = offset;

                int fromIndex = Math.min(offset * limit, totalItems);
                int toIndex = Math.min(fromIndex + limit, totalItems);
                List<Map<String, Object>> paginatedList = resultList.subList(fromIndex, toIndex);

                Map<String, Object> finalResponse = new HashMap<>();
                finalResponse.put("response", paginatedList);
                finalResponse.put("totalItems", totalItems);
                finalResponse.put("totalPages", totalPages);
                finalResponse.put("currentPage", currentPage);
                String message = resultList.isEmpty() ? "No Details Found" : "Details found";
                return ResponseService.generateSuccessResponse(message, finalResponse, HttpStatus.OK);
            }

            if (user_name != null && !user_name.isEmpty()) {
                ResponseEntity<SuccessResponse> response = (ResponseEntity<SuccessResponse>) serviceProviderService.searchServiceProviderBasedOnGivenFields(state, district, first_name, last_name, mobileNumber, test_status_id, ticketId, role, completed, suspended, approved, rejected, user_name, qualificationType, rank, type);
                List<Map<String, Object>> resultList = new ArrayList<>();
                if (response.getBody() != null && response.getBody().getData() != null) {
                    resultList = (List<Map<String, Object>>) response.getBody().getData();
                }

                int totalItems = resultList.size();
                int totalPages = (int) Math.ceil((double) totalItems / limit);
                int currentPage = offset;

                int fromIndex = Math.min(offset * limit, totalItems);
                int toIndex = Math.min(fromIndex + limit, totalItems);
                List<Map<String, Object>> paginatedList = resultList.subList(fromIndex, toIndex);

                Map<String, Object> finalResponse = new HashMap<>();
                finalResponse.put("response", paginatedList);
                finalResponse.put("totalItems", totalItems);
                finalResponse.put("totalPages", totalPages);
                finalResponse.put("currentPage", currentPage);
                String message = resultList.isEmpty() ? "No Details Found" : "Details found";
                return ResponseService.generateSuccessResponse(message, finalResponse, HttpStatus.OK);
            }

            // Handle search by full name (split into first and last names)
            if (full_name != null) {
                String[] name = sharedUtilityService.separateName(full_name.trim());
                if (!name[0].equals("")) first_name = name[0];
                if (!name[1].equals("")) last_name = name[1];
            }

            if (qualificationType != null) {
                for (Integer id : qualificationType) {
                    if (qualificationService.getQualificationByQualificationId(id) == null)
                        return ResponseService.generateErrorResponse("Invalid qualification Id", HttpStatus.BAD_REQUEST);
                    qualificationStrings.add(qualificationService.getQualificationByQualificationId(id).getQualification_name());
                    qualificationNames.add(qualificationService.getQualificationByQualificationId(id).getOverlap());

                }
            }


            if (rank != null) {
                for (Long id : rank) {
                    if (serviceProviderRankService.getServiceProviderRankByRankId(id) == null)
                        return ResponseService.generateErrorResponse("Invalid rank Id", HttpStatus.BAD_REQUEST);
                }
            }


            // First call with the provided order of first_name and last_name
            ResponseEntity<SuccessResponse> response1 = (ResponseEntity<SuccessResponse>)
                    serviceProviderService.searchServiceProviderBasedOnGivenFields(state, district, first_name, last_name, mobileNumber, test_status_id, ticketId, role, completed, suspended, approved, rejected, user_name, qualificationType, rank, type);

            // Second call with swapped order of first_name and last_name
            ResponseEntity<SuccessResponse> response2 = (ResponseEntity<SuccessResponse>)
                    serviceProviderService.searchServiceProviderBasedOnGivenFields(state, district, last_name, first_name, mobileNumber, test_status_id, ticketId, role, completed, suspended, approved, rejected, user_name, qualificationType, rank, type);

            // CHANGE: Use LinkedHashSet to maintain insertion order and remove duplicates
            Set<Map<String, Object>> mergedResults = new LinkedHashSet<>();
            if (response1.getBody() != null && response1.getBody().getData() != null) {
                mergedResults.addAll((List<Map<String, Object>>) response1.getBody().getData());
            }
            if (response2.getBody() != null && response2.getBody().getData() != null) {
                mergedResults.addAll((List<Map<String, Object>>) response2.getBody().getData());
            }

            // Pagination logic
            List<Map<String, Object>> finalList = new ArrayList<>(mergedResults);

            finalList.sort((a, b) -> {
                Date dateA = (Date) a.get("updated_date");
                Date dateB = (Date) b.get("updated_date");

                if (dateA == null && dateB == null) return 0;

                if ("ASC".equalsIgnoreCase(sortOrder)) {
                    if (dateA == null) return -1; // nulls first
                    if (dateB == null) return 1;
                    return dateA.compareTo(dateB);
                } else {
                    if (dateA == null) return 1; // nulls last
                    if (dateB == null) return -1;
                    return dateB.compareTo(dateA);
                }
            });

            int totalItems = finalList.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int currentPage = offset;

            int fromIndex = Math.min(offset * limit, totalItems);
            int toIndex = Math.min(fromIndex + limit, totalItems);

            List<Map<String, Object>> paginatedList = finalList.subList(fromIndex, toIndex);

            // Construct response
            Map<String, Object> response = new HashMap<>();
            Map<Integer, String> resp = new HashMap<>();
            resp.put(1, "Super Admins");
            resp.put(0, "All users");
            resp.put(2, "Admins");
            resp.put(3, "Service Provider Admins");
            resp.put(4, "Service Providers");
            if (role == null)
                role = 0;
            response.put("response", paginatedList);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", currentPage);
            if (fromIndex >= totalItems) {
                return ResponseService.generateSuccessResponse("No " + resp.get(role) + " Found", response, HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse(resp.get(role), response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (
                Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service provider details " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @GetMapping("/show-referred-candidates/{service_provider_id}")
    public ResponseEntity<?> showReferredCandidates(
            @PathVariable Long service_provider_id,
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(required = false) Boolean registeredByMe,
            HttpServletRequest httpServletRequest,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "30") int limit) {
        try {
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, service_provider_id);
            if (serviceProvider == null) {
                return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.NOT_FOUND);
            }

            List<Map<String, Object>> customers = new ArrayList<>();
            for (CustomerReferrer customerReferrer : serviceProvider.getMyReferrals()) {
                if (registeredByMe != null && registeredByMe.equals(true)) {
                    if (customerReferrer.getCustomer().getRegisteredBySp().equals(true)) {
                        customers.add(sharedUtilityService.breakReferenceForCustomer(customerReferrer.getCustomer(), authHeader, httpServletRequest));
                    }
                } else {
                    customers.add(sharedUtilityService.breakReferenceForCustomer(customerReferrer.getCustomer(), authHeader, httpServletRequest));
                }
            }

            // Pagination details
            int totalItems = customers.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int currentPage = offset;

            int fromIndex = Math.min(offset * limit, totalItems);
            int toIndex = Math.min(fromIndex + limit, totalItems);

            if (fromIndex >= totalItems) {
                return ResponseService.generateErrorResponse("No more referred candidates available", HttpStatus.NOT_FOUND);
            }

            List<Map<String, Object>> paginatedCustomers = customers.subList(fromIndex, toIndex);

            // Response with pagination metadata
            Map<String, Object> response = new HashMap<>();
            response.put("candidates", paginatedCustomers);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", currentPage);

            return ResponseService.generateSuccessResponse("List of referred candidates:", response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching candidates: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @Transactional
    @GetMapping("/{serviceProviderId}/order-requests")
    public ResponseEntity<?> allOrderRequestsBySPId(@PathVariable Long serviceProviderId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "30") int limit, @RequestParam(defaultValue = "all") String requestStatus) {
        try {
            int startPosition = page * limit;
            Query query = null;
            requestStatus = requestStatus.toLowerCase();
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (serviceProvider == null)
                return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.NOT_FOUND);
            if (requestStatus.equals("all")) {
                query = entityManager.createNativeQuery(Constant.GET_ONE_SP_ALL_ORDER_REQUEST);
            } else {
                query = entityManager.createNativeQuery(Constant.GET_ONE_SP_ORDER_REQUEST);
                switch (requestStatus) {
                    case "accepted":
                        query.setParameter("requestStatus", "ACCEPTED");
                        break;
                    case "returned":
                        query.setParameter("requestStatus", "RETURNED");
                        break;
                    case "new":
                        query.setParameter("requestStatus", "GENERATED");
                        break;
                    default:
                        return ResponseService.generateErrorResponse("Invalid Order request Status", HttpStatus.BAD_REQUEST);
                }
            }
            query.setParameter("serviceProviderId", serviceProviderId);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);
            List<BigInteger> orderRequestIds = query.getResultList();
            List<OrderRequest> spOrderRequests = new ArrayList<>();
            for (BigInteger orderRequestId : orderRequestIds) {
                OrderRequest orderRequest = entityManager.find(OrderRequest.class, orderRequestId.longValue());
                if (orderRequest != null)
                    spOrderRequests.add(orderRequest);
            }
            return ResponseService.generateSuccessResponse("Order Requests :", spOrderRequests, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching candidates: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @PostMapping("/{serviceProviderId}/return-ticket/{ticketId}")
    public ResponseEntity<?> orderRequestAction(@PathVariable Long serviceProviderId, @PathVariable Long ticketId, @RequestBody CreateTicketDto createTicketDto, @RequestHeader(value = "Authorization") String authHeader) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);
            if (createTicketDto == null || createTicketDto.getTicketStatus() == null)
                return ResponseService.generateErrorResponse("Return status is required", HttpStatus.BAD_REQUEST);
            if (role.getRole_name().equals(Constant.roleUser) || ((role.getRole_name().equals(Constant.roleServiceProvider) && !Objects.equals(tokenUserId, serviceProviderId))))
                return ResponseService.generateErrorResponse("FORBIDDEN", HttpStatus.FORBIDDEN);
            CustomServiceProviderTicket ticket = entityManager.find(CustomServiceProviderTicket.class, ticketId);
            if (ticket == null)
                return ResponseService.generateErrorResponse("Ticket not found", HttpStatus.NOT_FOUND);
            if (ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_RETURNED))
                return ResponseService.generateErrorResponse("Ticket already returned", HttpStatus.BAD_REQUEST);
            if (!ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_TO_DO))
                return ResponseService.generateErrorResponse("Cannot return ticket after accepting", HttpStatus.BAD_REQUEST);
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (serviceProvider == null)
                return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.NOT_FOUND);
            if (!ticket.getAssignee().equals(serviceProvider.getService_provider_id()))
                return ResponseService.generateErrorResponse("Ticket does not belong to the specified SP,Check again", HttpStatus.BAD_REQUEST);
            if (ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_RETURNED))
                return ResponseService.generateErrorResponse("Ticket already Returned ", HttpStatus.UNPROCESSABLE_ENTITY);
            CustomTicketStatus customTicketStatus = entityManager.find(CustomTicketStatus.class, createTicketDto.getTicketStatus());
            if (!Arrays.asList(Constant.TICKET_STATUS_BDWL, Constant.TICKET_STATUS_OTHER).contains(createTicketDto.getTicketStatus()) || customTicketStatus == null)
                return ResponseService.generateErrorResponse("Invalid status selected", HttpStatus.BAD_REQUEST);
            if (createTicketDto.getTicketStatus().equals(Constant.TICKET_STATUS_OTHER) && (createTicketDto.getComment() == null || createTicketDto.getComment().isEmpty()))
                return ResponseService.generateErrorResponse("Comment is required", HttpStatus.BAD_REQUEST);
            if (createTicketDto.getComment() == null)
                createTicketDto.setComment("Returned by SP with ID :" + serviceProviderId);
            ticket.setAssignee(null);
            ticket.setAssigneeRole(null);
            ticket.setTicketState(entityManager.find(CustomTicketState.class, Constant.TICKET_STATE_RETURNED));
            ticket.setTicketStatus(entityManager.find(CustomTicketStatus.class, createTicketDto.getTicketStatus()));
            ticket.getRejectedBy().add(serviceProviderId);
            ticket.setComment(createTicketDto.getComment());
            entityManager.merge(ticket);
            return ResponseService.generateSuccessResponse("Ticket Returned", ticket, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in returning ticket: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @RequestMapping(value = "/{serviceProviderId}/completeOrder/{orderRequestId}", method = RequestMethod.PUT)
    public ResponseEntity<?> completeOrder(@PathVariable Long serviceProviderId, @PathVariable Long orderRequestId, @RequestParam Integer statusId) {
        try {
            OrderRequest orderRequest = entityManager.find(OrderRequest.class, orderRequestId);
            if (orderRequest == null)
                return ResponseService.generateErrorResponse("Order Request not found", HttpStatus.NOT_FOUND);
            CustomOrderState customOrderState = entityManager.find(CustomOrderState.class, orderRequest.getOrderId());
            if (Constant.ORDER_STATE_COMPLETED.getOrderStateId().equals(customOrderState.getOrderStateId())) {
                return ResponseService.generateErrorResponse("Order Already Completed", HttpStatus.BAD_REQUEST);
            }
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (serviceProvider == null) {
                return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.NOT_FOUND);
            }
            if (!orderRequest.getServiceProvider().equals(serviceProvider))
                return ResponseService.generateErrorResponse("Order Request does not belong to the specified SP,Check again", HttpStatus.BAD_REQUEST);
            if (!customOrderState.getOrderStateId().equals(Constant.ORDER_STATE_IN_PROGRESS.getOrderStateId()))
                return ResponseService.generateErrorResponse("Cannot complete this order manually as its status is : " + orderStatusByStateService.getOrderStateById(customOrderState.getOrderStateId()).getOrderStateName(), HttpStatus.UNPROCESSABLE_ENTITY);
            if (statusId != null) {
                CustomOrderStatus customOrderStatus = entityManager.find(CustomOrderStatus.class, statusId);
                if (customOrderStatus == null) {
                    return ResponseService.generateErrorResponse("Invalid Order Status selected", HttpStatus.BAD_REQUEST);
                }
                if (!orderStatusByStateService.getOrderStatusByOrderStateId(Constant.ORDER_STATE_COMPLETED.getOrderStateId()).contains(customOrderStatus)) {
                    return ResponseService.generateErrorResponse("Selected order Status does not belong to this action", HttpStatus.BAD_REQUEST);
                }
                customOrderState.setOrderStateId(Constant.ORDER_STATE_COMPLETED.getOrderStateId());
                customOrderState.setOrderStatusId(statusId);
                entityManager.merge(customOrderState);
                Map<String, Object> response = new HashMap<>();
                response.put("order_id", orderRequest.getOrderId());
                response.put("order_request_id", orderRequestId);
                return ResponseService.generateSuccessResponse("Order Completed", response, HttpStatus.OK);
            } else
                return ResponseService.generateErrorResponse("Select an order completion status", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error assigning Request to Service Provider", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @Authorize(value = {Constant.roleSuperAdmin, Constant.roleAdmin, Constant.roleAdminServiceProvider})
    @PutMapping("manage-sp")
    public ResponseEntity<?> activateOrSuspendSp(@RequestBody Map<String, Object> map, @RequestParam String action, @RequestHeader(name = "Authorization") String authHeader) throws Exception {
        //extracting info from jwt token
        int actionCount = 0, successCount = 0;
        System.out.println("hii");
        String jwtToken = authHeader.substring(7);
        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
        Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
        List<Long> ids = getLongList(map, "spIds");
        Map<Long, String> skippedIds = new HashMap<>();
        List<Long> actionedIds = new ArrayList<>();
        List<ServiceProviderEntity> processedServiceProviders = new ArrayList<>();
        action = action.toLowerCase();
        String actionReq = null;

        if (!action.equals(Constant.ACTION_SUSPEND) && !action.equals(Constant.ACTION_ACTIVATE) && (!action.equals(Constant.ACTION_APPROVE)) && (!action.equals(Constant.ACTION_REJECT))) {
            return ResponseService.generateErrorResponse("Invalid action", HttpStatus.BAD_REQUEST);
        }
        // Check if the spIds list is empty and return an error response
        if (ids.isEmpty()) {
            return ResponseService.generateErrorResponse("No Service Provider IDs provided", HttpStatus.BAD_REQUEST);
        }

        if (action.equals("suspend") || action.equals("reject"))
            actionReq = action + "ed";
        else
            actionReq = action + "d";
        for (Long serviceProviderId : ids) {
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);

            if (serviceProvider == null) {
                skippedIds.put(serviceProviderId, "SP Not Found");
                continue;
            }
            if (serviceProvider.getRole() == 1) {
                skippedIds.put(serviceProviderId, "Action not Authorized");
                continue;
            }
            if (action.equals(Constant.ACTION_APPROVE)) {
                Role role = roleService.getRoleByRoleId(roleId);
                if (!Constant.roleSuperAdmin.equals(role.getRole_name()) && !Constant.roleAdmin.equals(role.getRole_name())) {
                    return ResponseService.generateErrorResponse("Action Forbidden", HttpStatus.FORBIDDEN);
                }
                if (serviceProvider.getApproved().equals(true)) {
                    skippedIds.put(serviceProviderId, "User Already approved");
                    ++actionCount;
                    continue;
                }
                if (!serviceProvider.getCompleted()) {
                    skippedIds.put(serviceProviderId, "Profile not completed");
                    continue;
                }
                serviceProvider.setApproved(true);
                serviceProvider.setLoginMessage("Profile has been approved by the Administrator");
                ServiceProviderTestStatus serviceProviderTestStatus = entityManager.find(ServiceProviderTestStatus.class, Constant.APPROVED_SP);
                if (serviceProviderTestStatus == null) {
                    return ResponseService.generateErrorResponse("Test Status id " + Constant.APPROVED_SP + " Not found", HttpStatus.NOT_FOUND);
                }
                serviceProvider.setServiceProviderStatus(serviceProviderTestStatus);

                Long id = serviceProvider.getService_provider_id();
                int role_id = serviceProvider.getRole();

                // Assign ADD_PRODUCT, UPDATE_PRODUCT, DELETE_PRODUCT privileges
                List<String> requiredPrivileges = Arrays.asList(
                        Constant.PRIVILEGE_ADD_PRODUCT,
                        Constant.PRIVILEGE_UPDATE_PRODUCT,
                        Constant.PRIVILEGE_DELETE_PRODUCT
                );

                for (String privilegeName : requiredPrivileges) {
                    Privileges privilege = privilegeService.getPrivilegeByName(privilegeName);
                    if (privilege != null) {
                        privilegeService.assignPrivilege(privilege.getPrivilege_id(), id, role_id);
                    }
                }
                serviceProvider.setRejected(false);

            } else if (Constant.ACTION_REJECT.equals(action)) {
                Role role = roleService.getRoleByRoleId(roleId);
                if (!Constant.roleSuperAdmin.equals(role.getRole_name()) && !Constant.roleAdmin.equals(role.getRole_name())) {
                    return ResponseService.generateErrorResponse("Action Forbidden", HttpStatus.FORBIDDEN);
                }
                if (serviceProvider.getRejected() != null && serviceProvider.getRejected().equals(true)) {
                    skippedIds.put(serviceProviderId, "User Already rejected");
                    ++actionCount;
                    continue;
                }
                if (serviceProvider.getRejected() != null && serviceProvider.getApproved()) {
                    skippedIds.put(serviceProviderId, "Cannot reject,User is approved");
                    ++actionCount;
                    continue;
                }
                serviceProvider.setRejected(true);
                ServiceProviderTestStatus serviceProviderTestStatus = entityManager.find(ServiceProviderTestStatus.class, Constant.REJECTED_SP);
                if (serviceProviderTestStatus == null) {
                    return ResponseService.generateErrorResponse("Test Status id " + Constant.REJECTED_SP + " Not found", HttpStatus.NOT_FOUND);
                }
                serviceProvider.setServiceProviderStatus(serviceProviderTestStatus);
                serviceProvider.setApproved(false);
                serviceProvider.setLoginMessage("Profile has been Rejected by the Administrator");
            }
            //checking valid permissions
            else if (action.equals(Constant.ACTION_SUSPEND)) {
                if (serviceProvider.getIsArchived().equals(true)) {
                    skippedIds.put(serviceProviderId, "User Already Suspended");
                    ++actionCount;
                    continue;
                }
                serviceProvider.setLastStatusId(serviceProvider.getServiceProviderStatus().getTest_status_id());
                serviceProvider.setIsArchived(true);
                ServiceProviderTestStatus serviceProviderTestStatus = entityManager.find(ServiceProviderTestStatus.class, Constant.SUSPENDED_SP);
                if (serviceProviderTestStatus == null) {
                    return ResponseService.generateErrorResponse("Test Status id " + Constant.SUSPENDED_SP + " Not found", HttpStatus.NOT_FOUND);
                }
                serviceProvider.setServiceProviderStatus(serviceProviderTestStatus);
            } else {
                if (serviceProvider.getIsArchived().equals(false)) {
                    skippedIds.put(serviceProviderId, "User Already Activate");
                    ++actionCount;
                    continue;
                }
                serviceProvider.setIsArchived(false);
                //set the last status
                ServiceProviderTestStatus serviceProviderTestStatus = entityManager.find(ServiceProviderTestStatus.class, serviceProvider.getLastStatusId());
                if (serviceProviderTestStatus == null) {
                    return ResponseService.generateErrorResponse("Test Status id " + serviceProvider.getLastStatusId() + " Not found", HttpStatus.NOT_FOUND);
                }
                serviceProvider.setServiceProviderStatus(serviceProviderTestStatus);
            }

            if (action.equals(Constant.ACTION_SUSPEND)) {
                sharedUtilityService.blackListToken(serviceProvider.getToken(), 4, serviceProvider.getService_provider_id());
                customerEndpoint.logout(serviceProvider.getToken());
            } else {
                sharedUtilityService.removeToken(serviceProvider.getToken());
            }

            actionedIds.add(serviceProviderId);
            ++successCount;
            entityManager.merge(serviceProvider);

            processedServiceProviders.add(serviceProvider);
        }

        if (!processedServiceProviders.isEmpty()) {
            statusChangeEmailService.sendStatusChangeEmails(processedServiceProviders, action, authHeader);
        }

        Map<String, Object> response = new HashMap<>();
        if (skippedIds.isEmpty()) {
            response.put(actionReq + "Ids", actionedIds);
            return ResponseService.generateSuccessResponse("Selected Accounts " + actionReq + " successfully", response, HttpStatus.OK);
        } else if (actionedIds.isEmpty()) {
            response.put(actionReq + " Ids:", actionedIds);
            response.put("Skipped Ids:", skippedIds);
            return ResponseService.generateSuccessResponse("Unable to " + action, response, HttpStatus.BAD_REQUEST);
        } else {
            response.put(actionReq + " Ids:", actionedIds);
            response.put("Skipped Ids:", skippedIds);
            return ResponseService.generateSuccessResponse("Action Partially Fulfilled", response, HttpStatus.OK);
        }
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @Transactional
    @PutMapping("{spId}/force-complete")
    public ResponseEntity<?> completeSp(@PathVariable Long spId, @RequestHeader(value = "Authorization") String authHeader) throws Exception {
        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, spId);
        if (serviceProvider == null)
            return ResponseService.generateErrorResponse("User not found", HttpStatus.NOT_FOUND);
        if (serviceProvider.getCompleted())
            return ResponseService.generateErrorResponse("Profile already completed", HttpStatus.BAD_REQUEST);
        serviceProvider.setCompleted(true);
        try {
            return ResponseService.generateSuccessResponse("Profile moved to completed", sharedUtilityService.serviceProviderDetailsMap(serviceProvider,false), HttpStatus.OK);
        } catch (Exception exception) {
            return ResponseService.generateErrorResponse("Could not complete profile due to an error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @Authorize(value = {Constant.roleSuperAdmin})
    @GetMapping("get-admins")
    public ResponseEntity<?> returnAdmins(@RequestParam(defaultValue = "30", required = false) int limit, @RequestParam(defaultValue = "0", required = false) int page) throws Exception {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ServiceProviderEntity> cq = cb.createQuery(ServiceProviderEntity.class);
        Root<ServiceProviderEntity> root = cq.from(ServiceProviderEntity.class);

        // Predicate for role = 1
        Predicate rolePredicate = cb.equal(root.get("role"), 2);
        cq.where(rolePredicate);

        TypedQuery<ServiceProviderEntity> query = entityManager.createQuery(cq);
        List<Map<String, Object>> res = new ArrayList<>();
        for (ServiceProviderEntity serviceProvider : query.getResultList()) {
            res.add(sharedUtilityService.serviceProviderDetailsMap(serviceProvider,false));
        }
        int totalItems = query.getResultList().size();
        int totalPages = (int) Math.ceil((double) totalItems / limit);
        int fromIndex = page * limit;
        int toIndex = Math.min(fromIndex + limit, totalItems);
        Map<String, Object> result = new HashMap<>();
        result.put("totalItems", totalItems);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("Admins", res.subList(fromIndex, toIndex));
        return ResponseService.generateSuccessResponse("Admins fetched successfully", res.subList(fromIndex, toIndex), HttpStatus.OK);
    }

    @Authorize(value = {Constant.roleSuperAdmin})
    @GetMapping("re-ranking")
    @Transactional
    public ResponseEntity<?> reRankingOfServiceProvider() throws Exception {
        try {
            // Re-ranking of Service-Providers.

            // Fetch all the service provider whom we have to re-rank.
            List<ServiceProviderEntity> subsequentReRanking = new ArrayList<>();
            List<ServiceProviderEntity> firstTimeReRanking = new ArrayList<>();

            // FETCH ALL THE NEW SERVICE PROVIDERS WHOSE ADMIN OVERRIDDEN IS FALSE AND IS ELIGIBLE FOR RANKING IS NULL OR 0.
            serviceProviderService.updateServiceProviderEligibilityForReRanking(subsequentReRanking, firstTimeReRanking);

            log.info("Subsequent service provider for re-ranking: {}", subsequentReRanking.size());
            log.info("First time re-ranking: {}", firstTimeReRanking.size());


            return ResponseService.generateSuccessResponse("Re-ranking run successfully.", null, HttpStatus.OK);
        } catch (NotFoundException notAuthorizedException) {
            exceptionHandlingService.handleException(notAuthorizedException);
            return ResponseService.generateErrorResponse(notAuthorizedException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (NotAuthorizedException notAuthorizedException) {
            exceptionHandlingService.handleException(notAuthorizedException);
            return ResponseService.generateErrorResponse(notAuthorizedException.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse("Some Exception Occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}