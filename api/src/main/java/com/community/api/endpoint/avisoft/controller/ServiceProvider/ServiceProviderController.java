package com.community.api.endpoint.avisoft.controller.ServiceProvider;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CreateTicketDto;
import com.community.api.endpoint.avisoft.controller.Customer.CustomerEndpoint;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomOrderStatus;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomerReferrer;
import com.community.api.entity.OrderRequest;
import com.community.api.entity.Role;
import com.community.api.entity.ServiceProviderAddress;
import com.community.api.entity.ServiceProviderAddressRef;
import com.community.api.entity.Skill;
import com.community.api.entity.SuccessResponse;
import com.community.api.services.DistrictService;
import com.community.api.services.OrderStatusByStateService;
import com.community.api.services.PhysicalRequirementDtoService;
import com.community.api.services.ReserveCategoryDtoService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.SanitizerService;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.TicketStateService;
import com.community.api.services.TwilioServiceForServiceProvider;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.broadleafcommerce.core.order.service.OrderService;

import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.community.api.services.ServiceProvider.ServiceProviderServiceImpl.getLongList;

@RestController
@RequestMapping("/service-providers")
public class ServiceProviderController {

    @Autowired
    private ServiceProviderServiceImpl serviceProviderService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private OrderStatusByStateService orderStatusByStateService;
    @Value("${twilio.accountSid}")
    private String accountSid;
    @Value("${twilio.authToken}")
    private String authToken;
    @Autowired
    CustomerEndpoint customerEndpoint;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TwilioServiceForServiceProvider twilioService;
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
    public ResponseEntity<?> updateServiceProvider(@RequestParam Long userId, @RequestBody Map<String, Object> serviceProviderDetails) throws Exception {
        try {
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, userId);
            if (serviceProvider.getIsArchived().equals(true))
                return ResponseService.generateErrorResponse("SP is archived", HttpStatus.NOT_FOUND);
            if (serviceProvider == null)
                return ResponseService.generateErrorResponse("Service Provider with provided Id not found", HttpStatus.NOT_FOUND);
            return serviceProviderService.updateServiceProvider(userId, serviceProviderDetails);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error updating: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    @Transactional // Set readOnly for performance improvement
    @GetMapping("/get-all-service-providers")
    public ResponseEntity<?> getAllServiceProviders(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            if (offset < 0) {
                throw new IllegalArgumentException("Offset for pagination cannot be a negative number");
            }
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit for pagination cannot be a negative number or 0");
            }
            int startPosition = offset * limit;

            // Count total service providers (excluding archived ones)
            Query countQuery = entityManager.createQuery("SELECT COUNT(sp) FROM ServiceProviderEntity sp WHERE sp.isArchived = false");
            long totalItems = (long) countQuery.getSingleResult();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            if (offset >= totalPages && offset != 0) {
                throw new IllegalArgumentException("No more service providers available");
            }
            // Create the query with pagination
            Query query = entityManager.createQuery("SELECT s FROM ServiceProviderEntity s WHERE s.isArchived = false", ServiceProviderEntity.class);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);

            // Fetch results
            List<ServiceProviderEntity> results = query.getResultList();

            List<Map<String, Object>> resultOfSp = new ArrayList<>();
            for (ServiceProviderEntity serviceProvider : results) {
                if (!serviceProvider.getIsArchived()) {
                    resultOfSp.add(sharedUtilityService.serviceProviderDetailsMap(serviceProvider));
                }
            }

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("serviceProviders", resultOfSp);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            return ResponseService.generateSuccessResponse("List of service providers: ", response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service providers: " + e.getMessage(), HttpStatus.BAD_REQUEST);
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

            Map<String, Object> serviceProviderMap = sharedUtilityService.serviceProviderDetailsMap(serviceProviderEntity);
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
            @RequestParam(defaultValue = "10") int limit) {
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
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String full_name,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) Long test_status_id,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(required = false)Long ticketId,
            HttpServletRequest request) {

        try {
            System.out.println("ticketId"+ticketId);
            Map<String, String[]> uri = request.getParameterMap();

            // Validate input
            if ((uri.containsKey("state") && state == null) ||
                    (uri.containsKey("full_name") && (full_name == null || full_name.trim().isEmpty())) ||
                    (uri.containsKey("test_status_id") && test_status_id == null) ||
                    (uri.containsKey("district") && district == null) ||
                    (uri.containsKey("mobileNumber") && mobileNumber == null)) {
                return ResponseService.generateErrorResponse("Empty fields are not accepted", HttpStatus.BAD_REQUEST);
            }

            // Validate full_name (only alphabets and spaces allowed)
            if (full_name != null && !full_name.matches("^[a-zA-Z ]+$")) {
                return ResponseService.generateErrorResponse("Full name cannot contain digits or special characters", HttpStatus.BAD_REQUEST);
            }


            String first_name = null;
            String last_name = null;

            // Handle search by mobile number
            if (mobileNumber != null && !mobileNumber.isEmpty() && serviceProviderService.isValidMobileNumber(mobileNumber)) {
                return serviceProviderService.searchServiceProviderBasedOnGivenFields(state, district, first_name, last_name, mobileNumber, test_status_id,ticketId);
            }

            // Handle search by full name (split into first and last names)
            if (full_name != null) {
                String[] name = sharedUtilityService.separateName(full_name.trim());
                if (!name[0].equals("")) first_name = name[0];
                if (!name[1].equals("")) last_name = name[1];
            }

            // First call with the provided order of first_name and last_name
            ResponseEntity<SuccessResponse> response1 = (ResponseEntity<SuccessResponse>)
                    serviceProviderService.searchServiceProviderBasedOnGivenFields(state, district, first_name, last_name, mobileNumber, test_status_id,ticketId);

            // Second call with swapped order of first_name and last_name
            ResponseEntity<SuccessResponse> response2 = (ResponseEntity<SuccessResponse>)
                    serviceProviderService.searchServiceProviderBasedOnGivenFields(state, district, last_name, first_name, mobileNumber, test_status_id,ticketId);

            // Merge results and remove duplicates
            Set<Map<String, Object>> mergedResults = new HashSet<>();
            if (response1.getBody() != null && response1.getBody().getData() != null) {
                mergedResults.addAll((List<Map<String, Object>>) response1.getBody().getData());
            }
            if (response2.getBody() != null && response2.getBody().getData() != null) {
                mergedResults.addAll((List<Map<String, Object>>) response2.getBody().getData());
            }

            // Pagination logic
            List<Map<String, Object>> finalList = new ArrayList<>(mergedResults);
            int totalItems = finalList.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int currentPage = offset;

            int fromIndex = Math.min(offset * limit, totalItems);
            int toIndex = Math.min(fromIndex + limit, totalItems);


            List<Map<String, Object>> paginatedList = finalList.subList(fromIndex, toIndex);

            // Construct response
            Map<String, Object> response = new HashMap<>();
            response.put("serviceProviders", paginatedList);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", currentPage);

            if (fromIndex >= totalItems) {
                return ResponseService.generateSuccessResponse("No Service Providers Found", response, HttpStatus.OK);
            }

            return ResponseService.generateSuccessResponse("Service Providers", response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
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
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
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
    public ResponseEntity<?> allOrderRequestsBySPId(@PathVariable Long serviceProviderId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "all") String requestStatus) {
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
    public ResponseEntity<?> orderRequestAction(@PathVariable Long serviceProviderId, @PathVariable Long ticketId, @RequestBody CreateTicketDto createTicketDto,@RequestHeader(value = "Authorization")String authHeader) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId=jwtTokenUtil.extractId(jwtToken);
            Role role=roleService.getRoleByRoleId(roleId);
            if(createTicketDto==null||createTicketDto.getTicketStatus()==null)
                return ResponseService.generateErrorResponse("Return status is required",HttpStatus.BAD_REQUEST);
            if(role.getRole_name().equals(Constant.roleUser)||((role.getRole_name().equals(Constant.roleServiceProvider)&& !Objects.equals(tokenUserId, serviceProviderId))))
                return ResponseService.generateErrorResponse("FORBIDDEN",HttpStatus.FORBIDDEN);
            CustomServiceProviderTicket ticket=entityManager.find(CustomServiceProviderTicket.class,ticketId);
            if (ticket == null)
                return ResponseService.generateErrorResponse("Ticket not found", HttpStatus.NOT_FOUND);
            if(ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_RETURNED))
                return ResponseService.generateErrorResponse("Ticket already returned",HttpStatus.BAD_REQUEST);
            if(!ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_TO_DO))
                return ResponseService.generateErrorResponse("Cannot return ticket after accepting",HttpStatus.BAD_REQUEST);
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (serviceProvider == null)
                return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.NOT_FOUND);
            if (!ticket.getAssignee().equals(serviceProvider.getService_provider_id()))
                return ResponseService.generateErrorResponse("Ticket does not belong to the specified SP,Check again", HttpStatus.BAD_REQUEST);
            if (ticket.getTicketState().getTicketStateId().equals(Constant.TICKET_STATE_RETURNED))
                    return ResponseService.generateErrorResponse("Ticket already Returned ", HttpStatus.UNPROCESSABLE_ENTITY);
            CustomTicketStatus customTicketStatus=entityManager.find(CustomTicketStatus.class,createTicketDto.getTicketStatus());
            if(!Arrays.asList(Constant.TICKET_STATUS_BDWL,Constant.TICKET_STATUS_OTHER).contains(createTicketDto.getTicketStatus())||customTicketStatus==null)
                return ResponseService.generateErrorResponse("Invalid status selected",HttpStatus.BAD_REQUEST);
            if(createTicketDto.getTicketStatus().equals(Constant.TICKET_STATUS_OTHER)&&(createTicketDto.getComment()==null||createTicketDto.getComment().isEmpty()))
                return ResponseService.generateErrorResponse("Comment is required",HttpStatus.BAD_REQUEST);
            if(createTicketDto.getComment()==null)
                createTicketDto.setComment("Returned by SP with ID :"+serviceProviderId);
            ticket.setAssignee(null);
            ticket.setAssigneeRole(null);
            ticket.setTicketState(entityManager.find(CustomTicketState.class,Constant.TICKET_STATE_RETURNED));
            ticket.setTicketStatus(entityManager.find(CustomTicketStatus.class,createTicketDto.getTicketStatus()));
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
        String actionReq = null;


        if (!action.equals(Constant.ACTION_SUSPEND) && !action.equals(Constant.ACTION_ACTIVATE)) {
            return ResponseService.generateErrorResponse("Invalid action", HttpStatus.BAD_REQUEST);
        }
        // Check if the spIds list is empty and return an error response
        if (ids.isEmpty()) {
            return ResponseService.generateErrorResponse("No Service Provider IDs provided", HttpStatus.BAD_REQUEST);
        }


        if (action.equals("suspend"))
            actionReq = action + "ed";
        else
            actionReq = action + "d";
        for (Long customerId : ids) {
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, customerId);

            if (serviceProvider == null) {
                skippedIds.put(customerId, "SP Not Found");
                continue;
            }
            if (serviceProvider.getRole() != 4) {
                skippedIds.put(customerId, "Action not Authorized");
                continue;
            }

            //checking valid permissions
            if (action.equals(Constant.ACTION_SUSPEND)) {
                if (serviceProvider.getIsArchived().equals(true)) {
                    skippedIds.put(customerId, "User Already Suspended");
                    ++actionCount;
                    continue;
                }
                serviceProvider.setIsArchived(true);
            } else {
                if (serviceProvider.getIsArchived().equals(false)) {
                    skippedIds.put(customerId, "User Already Activate");
                    ++actionCount;
                    continue;
                }
                serviceProvider.setIsArchived(false);
            }
            if (action.equals(Constant.ACTION_SUSPEND)) {
                sharedUtilityService.blackListToken(serviceProvider.getToken(), 4, serviceProvider.getService_provider_id());
                customerEndpoint.logout(serviceProvider.getToken());
            }
            else
            {
                sharedUtilityService.removeToken(serviceProvider.getToken());
            }
            actionedIds.add(customerId);
            ++successCount;
            entityManager.merge(serviceProvider);
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
}