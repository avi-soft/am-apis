package com.community.api.services.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.DocumentValidity;
import com.community.api.entity.QualificationDetails;
import com.community.api.entity.ScoringCriteria;
import com.community.api.entity.ServiceProviderAddress;
import com.community.api.entity.ServiceProviderAddressRef;
import com.community.api.entity.ServiceProviderInfra;
import com.community.api.entity.ServiceProviderLanguage;
import com.community.api.entity.ServiceProviderRank;
import com.community.api.entity.ServiceProviderTestStatus;
import com.community.api.entity.Skill;
import com.community.api.entity.StateCode;
import com.community.api.services.ApiConstants;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.DistrictService;
import com.community.api.services.DocumentStorageService;
import com.community.api.services.FileService;
import com.community.api.services.RateLimiterService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.ServiceProviderInfraService;
import com.community.api.services.ServiceProviderLanguageService;
import com.community.api.services.ServiceProviderTestService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.SkillService;
import com.community.api.services.TwilioServiceForServiceProvider;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.DocumentType;
import com.community.api.utils.ServiceProviderDocument;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import io.github.bucket4j.Bucket;
import io.micrometer.core.lang.Nullable;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Lob;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import javax.validation.constraints.Pattern;

import static com.community.api.component.Constant.PHONE_QUERY_SERVICE_PROVIDER_FILTER;
import static com.community.api.component.Constant.request;

@Slf4j
@Service
public class ServiceProviderServiceImpl implements ServiceProviderService {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private CustomCustomerService customCustomerService;
    @Autowired
    private CustomerService customerService;
    @Value("${twilio.accountSid}")
    private String accountSid;
    @Value("${twilio.authToken}")
    private String authToken;
    @Autowired
    private TwilioServiceForServiceProvider twilioService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    private ServiceProviderInfraService serviceProviderInfraService;
    @Autowired
    private ServiceProviderLanguageService serviceProviderLanguageService;
    @Autowired
    private RateLimiterService rateLimiterService;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    @Autowired
    private ExceptionHandlingService exceptionHandlingService;
    @Autowired
    private ServiceProviderTestService serviceProviderTestService;

    @Autowired
    private DocumentStorageService fileUploadService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DocumentStorageService documentStorageService;

    @Value("${twilio.phoneNumber}")
    private String twilioPhoneNumber;

    public static List<Integer> getIntegerList(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;

            if (!list.isEmpty() && list.get(0) instanceof Integer) {
                return (List<Integer>) list;
            }
        }

        return Collections.emptyList();
    }

    public static List<Long> getLongList(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;

            List<Long> longList = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Long) {
                    longList.add((Long) item);
                } else if (item instanceof Integer) {
                    longList.add(((Integer) item).longValue());
                }
            }

            return longList;
        }

        return Collections.emptyList();
    }

    public static boolean isOnlyDigits(String str) {
        // Check if the string is not null and matches the regex for only digits
        return str != null && str.matches("^[0-9]+$");
    }

    public static boolean isNumeric(String str) {
        return str != null && str.matches("\\d{10}");
    }

    public static boolean isAlphabetOnly(String str) {
        return str != null && str.matches("^[A-Za-z]+( [A-Za-z]+)*$");
    }

    @Override
    @Transactional
    public ServiceProviderEntity saveServiceProvider(ServiceProviderEntity serviceProviderEntity) {
        try {
            entityManager.persist(serviceProviderEntity);
            ServiceProviderStatus serviceProviderStatus = entityManager.find(ServiceProviderStatus.class, 1);
            serviceProviderEntity.setStatus(serviceProviderStatus);
            return serviceProviderEntity;
        } catch (Exception e) {
            throw new RuntimeException("Error saving service provider entity", e);
        }
    }

    @Transactional
    public ResponseEntity<?> updateServiceProvider(Long userId, Map<String, Object> updates,String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseService.generateErrorResponse("Authorization header is missing or invalid.", HttpStatus.UNAUTHORIZED);
            }
            String jwtToken = authHeader.substring(7);
            List<String> deleteLogs = new ArrayList<>();
            Integer roleId;
            Long tokenUserId;
            roleId = jwtTokenUtil.extractRoleId(jwtToken);
            tokenUserId = jwtTokenUtil.extractId(jwtToken);
            String role= roleService.findRoleName(roleId);

            updates = sharedUtilityService.trimStringValues(updates);
            List<String> errorMessages = new ArrayList<>();


            // Find existing ServiceProviderEntity
            ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, userId);
            if (existingServiceProvider == null) {
                errorMessages.add("ServiceProvider with ID " + userId + " not found");
            }
            String type= null;
            if (updates.containsKey("type")) {
                String typeStr = (String) updates.get("type");

                // Validate that the type value is either "Professional" or "Individual"
                if (typeStr == null || typeStr.trim().isEmpty()) {
                    return ResponseService.generateErrorResponse("Service Provider type cannot be null or empty", HttpStatus.BAD_REQUEST);
                }
                if (!typeStr.equalsIgnoreCase("PROFESSIONAL") && !typeStr.equalsIgnoreCase("INDIVIDUAL")) {
                    return ResponseService.generateErrorResponse("Invalid value for 'type'. Allowed values are 'PROFESSIONAL' or 'INDIVIDUAL'.", HttpStatus.BAD_REQUEST);
                }
                existingServiceProvider.setType(typeStr.toUpperCase());
                type= typeStr.toUpperCase();
                updates.remove("type");
            }
            else
            {
                type= existingServiceProvider.getType();
            }
            if(role.equalsIgnoreCase(Constant.ADMIN) || role.equalsIgnoreCase(Constant.SUPER_ADMIN))
            {
                if(updates.containsKey("rankId"))
                {
                    Object rankIdObj = updates.get("rankId");
                    Long rankId = rankIdObj instanceof Number ? ((Number) rankIdObj).longValue() : null;
                    ServiceProviderRank serviceProviderRank= entityManager.find(ServiceProviderRank.class,rankId);
                    if(serviceProviderRank==null)
                    {
                        return ResponseService.generateErrorResponse("Rank with id " + rankId + " does not exist",HttpStatus.BAD_REQUEST);
                    }
                    if(type.equalsIgnoreCase("PROFESSIONAL") && rankId >4)
                    {
                        return ResponseService.generateErrorResponse("The service Provider is Professional so only Professional Ranking can be given i.e. from 1a to 1d",HttpStatus.BAD_REQUEST);
                    }
                    else  if (type.equalsIgnoreCase("INDIVIDUAL" )&& rankId<5)
                    {
                        return ResponseService.generateErrorResponse("The service Provider is Individual so only Individual Ranking can be given i.e. from 2a to 2d",HttpStatus.BAD_REQUEST);
                    }
                    existingServiceProvider.setAdminOverridden(true);
                    existingServiceProvider.setEligibleForReRanking(null);
                    existingServiceProvider.setRanking(serviceProviderRank);
//                    existingServiceProvider.setAutoScoring(false);
                }
                updates.remove("rankId");
            }
            else
            {
                if(updates.containsKey("rankId") && updates.get("rankId")!=null)
                {
                   return ResponseService.generateErrorResponse("Not authorized to update the rank of Service Provier. Only Admin or Super Admin can update the Rank",HttpStatus.BAD_REQUEST);
                }
            }
            if (updates.containsKey("partTimeOrFullTime")) {

                String partTimeOrFullTimeStr = (String) updates.get("partTimeOrFullTime");

                // Validate that the type value is either "Professional" or "Individual"
                if (partTimeOrFullTimeStr == null || partTimeOrFullTimeStr.trim().isEmpty()) {
                    return ResponseService.generateErrorResponse("Service Provider partTime or FullTime field cannot be null or empty", HttpStatus.BAD_REQUEST);
                }
                if (!partTimeOrFullTimeStr.equalsIgnoreCase("PART TIME") && !partTimeOrFullTimeStr.equalsIgnoreCase("FULL TIME")) {
                    return ResponseService.generateErrorResponse("Invalid value for 'partTime or FullTime'. Allowed values are 'PART TIME' or 'FULL TIME'.", HttpStatus.BAD_REQUEST);
                }
                existingServiceProvider.setPartTimeOrFullTime(partTimeOrFullTimeStr.toUpperCase());
            }

            String mobileNumber = (String) updates.get("mobileNumber");
            String secondaryMobileNumber = (String) updates.get("secondary_mobile_number");

            if (mobileNumber != null && secondaryMobileNumber != null) {
                if (mobileNumber.equalsIgnoreCase(secondaryMobileNumber)) {
                    errorMessages.add("Primary and Secondary Mobile Numbers cannot be the same");
                }
            }
            if (mobileNumber != null && secondaryMobileNumber == null && mobileNumber.equalsIgnoreCase(existingServiceProvider.getSecondary_mobile_number())) {
                return ResponseService.generateErrorResponse("Primary and Secondary Mobile Numbers cannot be the same", HttpStatus.BAD_REQUEST);
            }
            if (secondaryMobileNumber != null && mobileNumber == null && secondaryMobileNumber.equalsIgnoreCase(existingServiceProvider.getMobileNumber())) {
                return ResponseService.generateErrorResponse("Primary and Secondary Mobile Numbers cannot be the same", HttpStatus.BAD_REQUEST);
            }
            List<String> addresskeys = new ArrayList<>();
            addresskeys.add("district");
            addresskeys.add("city");
            addresskeys.add("pincode");
            addresskeys.add("state");
            addresskeys.add("residential_address");
            int count = 0;
            for (String key : updates.keySet()) {
                if (addresskeys.contains(key))
                    count++;
            }
            if (count > 0 && count < addresskeys.size())
                return ResponseService.generateErrorResponse("Need all address fields to add or update address", HttpStatus.BAD_REQUEST);

            if (updates.containsKey("district") && updates.containsKey("state") && updates.containsKey("city") && updates.containsKey("pincode") && updates.containsKey("residential_address")) {
                if (validateAddressFields(updates).isEmpty()) {
                    boolean flag=false;
                    Long addId=0L;
                    for(ServiceProviderAddress serviceProviderAddress:existingServiceProvider.getSpAddresses())
                    {
                        if(serviceProviderAddress.getAddress_type_id()==2) {
                            flag = true;
                            addId=serviceProviderAddress.getAddress_id();
                            break;
                        }
                    }
                    if (!flag) {
                        ServiceProviderAddress serviceProviderAddress = new ServiceProviderAddress();
                        serviceProviderAddress.setAddress_type_id(findAddressName("CURRENT_ADDRESS").getAddress_type_Id());
                        serviceProviderAddress.setAddress_name("CURRENT_ADDRESS");
                        serviceProviderAddress.setPincode((String) updates.get("pincode"));
                        serviceProviderAddress.setDistrict((String) updates.get("district"));
                        serviceProviderAddress.setState((String) updates.get("state"));
                        serviceProviderAddress.setCity((String) updates.get("city"));
                        serviceProviderAddress.setAddress_line((String) updates.get("residential_address"));
                        if (serviceProviderAddress.getAddress_line() != null || serviceProviderAddress.getCity() != null || serviceProviderAddress.getDistrict() != null || serviceProviderAddress.getState() != null || serviceProviderAddress.getPincode() != null) {
                            addAddress(existingServiceProvider.getService_provider_id(), serviceProviderAddress);
                        }
                    } else {
                        ServiceProviderAddress serviceProviderAddress = entityManager.find(ServiceProviderAddress.class,addId);
                        ServiceProviderAddress serviceProviderAddressDTO = new ServiceProviderAddress();
                        serviceProviderAddressDTO.setAddress_type_id(serviceProviderAddress.getAddress_type_id());
                        serviceProviderAddressDTO.setAddress_id(serviceProviderAddress.getAddress_id());
                        serviceProviderAddressDTO.setState((String) updates.get("state"));
                        serviceProviderAddressDTO.setDistrict((String) updates.get("district"));
                        serviceProviderAddressDTO.setAddress_line((String) updates.get("residential_address"));
                        serviceProviderAddressDTO.setPincode((String) updates.get("pincode"));
                        serviceProviderAddressDTO.setServiceProviderEntity(existingServiceProvider);
                        serviceProviderAddressDTO.setCity((String) updates.get("city"));
                        for (String error : updateAddress(existingServiceProvider.getService_provider_id(), serviceProviderAddress, serviceProviderAddressDTO)) {
                            errorMessages.add(error);
                        }
                    }
                } else {
                    errorMessages.addAll(validateAddressFields(updates));
                }
            }

            //removing key for address
            updates.remove("residential_address");
            updates.remove("city");
            updates.remove("state");
            updates.remove("district");
            updates.remove("pincode");
            // Validate and check for unique constraints
            ServiceProviderEntity existingSPByUsername = null;
            ServiceProviderEntity existingSPByEmail = null;



            List<String> PermanentAddressKeys = new ArrayList<>();
            PermanentAddressKeys.add("permanent_district");
            PermanentAddressKeys.add("permanent_city");
            PermanentAddressKeys.add("permanent_pincode");
            PermanentAddressKeys.add("permanent_state");
            PermanentAddressKeys.add("permanent_residential_address");
            int KeysCount = 0;
            for (String key : updates.keySet()) {
                if (PermanentAddressKeys.contains(key))
                    KeysCount++;
            }
            if (KeysCount > 0 && KeysCount < addresskeys.size())
                return ResponseService.generateErrorResponse("Need all address fields to add or update address", HttpStatus.BAD_REQUEST);
            if (updates.containsKey("permanent_district") && updates.containsKey("permanent_state") && updates.containsKey("permanent_city") && updates.containsKey("permanent_pincode") && updates.containsKey("permanent_residential_address")) {
                if (validatePAddressFields(updates).isEmpty()) {
                    boolean flag=false;
                    Long addId=0L;
                    for(ServiceProviderAddress serviceProviderAddress:existingServiceProvider.getSpAddresses())
                    {
                        if(serviceProviderAddress.getAddress_type_id()==5) {
                            flag = true;
                            addId=serviceProviderAddress.getAddress_id();
                            break;
                        }
                    }
                    if (!flag) {
                        ServiceProviderAddress serviceProviderAddress = new ServiceProviderAddress();
                        serviceProviderAddress.setAddress_type_id(findAddressName("PERMANENT_ADDRESS").getAddress_type_Id());
                        serviceProviderAddress.setAddress_name("PERMANENT_ADDRESS");
                        serviceProviderAddress.setPincode((String) updates.get("permanent_pincode"));
                        serviceProviderAddress.setDistrict((String) updates.get("permanent_district"));
                        serviceProviderAddress.setState((String) updates.get("permanent_state"));
                        serviceProviderAddress.setCity((String) updates.get("permanent_city"));
                        serviceProviderAddress.setAddress_line((String) updates.get("permanent_residential_address"));
                        if (serviceProviderAddress.getAddress_line() != null || serviceProviderAddress.getCity() != null || serviceProviderAddress.getDistrict() != null || serviceProviderAddress.getState() != null || serviceProviderAddress.getPincode() != null) {
                            addAddress(existingServiceProvider.getService_provider_id(), serviceProviderAddress);
                        }
                    } else {
                        ServiceProviderAddress serviceProviderAddress = entityManager.find(ServiceProviderAddress.class,addId);
                        ServiceProviderAddress serviceProviderAddressDTO = new ServiceProviderAddress();
                        serviceProviderAddress.setAddress_name("PERMANENT_ADDRESS");
                        serviceProviderAddressDTO.setAddress_type_id(serviceProviderAddress.getAddress_type_id());
                        serviceProviderAddressDTO.setAddress_id(serviceProviderAddress.getAddress_id());
                        serviceProviderAddressDTO.setState((String) updates.get("permanent_state"));
                        serviceProviderAddressDTO.setDistrict((String) updates.get("permanent_district"));
                        serviceProviderAddressDTO.setAddress_line((String) updates.get("permanent_residential_address"));
                        serviceProviderAddressDTO.setPincode((String) updates.get("permanent_pincode"));
                        serviceProviderAddressDTO.setServiceProviderEntity(existingServiceProvider);
                        serviceProviderAddressDTO.setCity((String) updates.get("permanent_city"));
                        for (String error : updateAddress(existingServiceProvider.getService_provider_id(), serviceProviderAddress, serviceProviderAddressDTO)) {
                            errorMessages.add(error);
                        }
                    }
                } else {
                    errorMessages.addAll(validatePAddressFields(updates));
                }
            }

            updates.remove("permanent_state");
            updates.remove("permanent_district");
            updates.remove("permanent_pincode");
            updates.remove("permanent_residential_address");
            updates.remove("permanent_city");

            ServiceProviderAddress currentAddress = null;
            ServiceProviderAddress permanentAddress = null;

            for (ServiceProviderAddress address : existingServiceProvider.getSpAddresses()) {
                if (address.getAddress_type_id() == 2) { // current
                    currentAddress = address;
                } else if (address.getAddress_type_id() == 5) { // permanent
                    permanentAddress = address;
                }
            }

            if (currentAddress != null && permanentAddress != null) {
                existingServiceProvider.setIsSameAsCurrentAddress(areAddressesSame(currentAddress, permanentAddress));
            } else {
                existingServiceProvider.setIsSameAsCurrentAddress(false);
            }

            entityManager.merge(existingServiceProvider);


            // running business unit section
            List<String> businessKeys = new ArrayList<>();
            businessKeys.add("business_name");


            businessKeys.add("business_location");
            businessKeys.add("business_email");
            businessKeys.add("number_of_employees");
            businessKeys.add("latitude");
            businessKeys.add("longitude");
            businessKeys.add("business_geo_location");
            businessKeys.add("isCFormAvailable");
            businessKeys.add("registration_number");

            if (updates.containsKey("is_running_business_unit")) {
                Object isRunningObj = updates.get("is_running_business_unit");

                int keysPresent = 0;
                for (String key : updates.keySet()) {
                    if (businessKeys.contains(key))
                        keysPresent++;
                }

                boolean isRunning = Boolean.parseBoolean(isRunningObj.toString());

                if (isRunning) {
                    if (keysPresent > 0 && keysPresent < businessKeys.size()) {
                        return ResponseService.generateErrorResponse(
                                "Need all business fields to add or update business profile", HttpStatus.BAD_REQUEST
                        );
                    }

                    // Null or empty check for each business field
                    for (String key : businessKeys) {
                        Object value = updates.get(key);
                        if(key.equals("registration_number"))
                            continue;
                        if (value == null || value.toString().trim().isEmpty()) {
                            return ResponseService.generateErrorResponse(
                                    "Field '" + key + "' cannot be null or empty when is_running_business_unit is true",
                                    HttpStatus.BAD_REQUEST
                            );
                        }
                    }



                        existingServiceProvider.setIsCFormAvailable((Boolean) updates.get("isCFormAvailable"));
                        if(((Boolean) updates.get("isCFormAvailable")))
                        {
                            if (updates.get("registration_number") != null
                                    && !((String) updates.get("registration_number")).trim().isEmpty()){
                                existingServiceProvider.setRegistration_number((String) updates.get("registration_number"));
                            }
                            else
                                return ResponseService.generateErrorResponse("Registration Number can not be empty or null", HttpStatus.BAD_REQUEST);


                        }
                        else
                            existingServiceProvider.setRegistration_number(null);

                        updates.remove("isCFormAvailable");
                        updates.remove("registration_number");


                        Object latObj = updates.get("latitude");
                        Double latitude = null;

                        if (latObj instanceof Double) {
                            latitude = (Double) latObj;
                        } else if (latObj instanceof String) {
                            try {
                                latitude = Double.parseDouble((String) latObj);
                            } catch (NumberFormatException e) {
                                errorMessages.add("Latitude must be a valid number");
                            }
                        } else {
                            errorMessages.add("Latitude must be a valid number");
                        }

                        if (latitude != null) {
                            if (latitude > 90 || latitude < -90) {
                                errorMessages.add("Invalid latitude: must be between -90 and 90");
                            } else {
                                existingServiceProvider.setLatitude(latitude);
                            }
                        }



                        Object longObj = updates.get("longitude");
                        Double longitude = null;

                        if (longObj instanceof Double) {
                            longitude = (Double) longObj;
                        } else if (longObj instanceof String) {
                            try {
                                longitude = Double.parseDouble((String) longObj);
                            } catch (NumberFormatException e) {
                                errorMessages.add("Longitude must be a valid number");
                            }
                        } else {
                            errorMessages.add("Longitude must be a valid number");
                        }

                        if (longitude != null) {
                            if (longitude > 180 || longitude < -180) {
                                errorMessages.add("Invalid longitude: must be between -180 and 180");
                            } else {
                                existingServiceProvider.setLongitude(longitude);
                            }
                        }


                    updates.remove("latitude");
                    updates.remove("longitude");
                    existingServiceProvider.setNumber_of_employees((Integer) updates.get("number_of_employees"));
                    updates.remove("number_of_employees");

                }  else  {
                    existingServiceProvider.setIs_running_business_unit(false);
                    existingServiceProvider.setBusiness_name(null);
                    existingServiceProvider.setBusiness_location(null);
                    existingServiceProvider.setBusiness_email(null);
                    existingServiceProvider.setNumber_of_employees(null);
                    existingServiceProvider.setRegistration_number(null);
                    existingServiceProvider.setLatitude(null);
                    existingServiceProvider.setLongitude(null);
                    existingServiceProvider.setBusiness_geo_location(null);
                    updates.remove("latitude");
                    updates.remove("longitude");
                    updates.remove("number_of_employees");
                    updates.remove("business_email");
                    updates.remove("business_location");
                    updates.remove("business_name");
                    updates.remove("business_geo_location");
                    updates.remove("registration_number");
                    updates.remove("isCFormAvailable");

                }
            }
            if (updates.containsKey("work_experience_in_months")) {
                Object workExpMonths = updates.get("work_experience_in_months");
                int months = 0;


                if (workExpMonths != null && !workExpMonths.toString().trim().isEmpty()) {
                    try {
                        months = Integer.parseInt(workExpMonths.toString().trim());
                    } catch (NumberFormatException e) {
                        return ResponseService.generateErrorResponse("Invalid value for work_experience_in_months", HttpStatus.BAD_REQUEST);
                    }
                }

                Object workExp = updates.get("work_experience_in");

                if (months != 0) {

                    if (workExp == null || workExp.toString().trim().isEmpty()) {
                        return ResponseService.generateErrorResponse("Work Experience description is required ", HttpStatus.BAD_REQUEST);
                    }
                } else {

                    if (workExp != null && workExp.toString().trim().isEmpty()) {
                        existingServiceProvider.setWork_experience_in(null);
                        updates.remove("work_experience_in");
                    }
                }
            }


            if (updates.containsKey("user_name")) {
                updates.remove("user_name");
            }
            if (updates.containsKey("primary_mobile_number")) {
                String userName = (String) updates.get("user_name");
                existingSPByUsername = findServiceProviderByUserName(userName);
            }

            if (updates.containsKey("primary_email")) {
                String primaryEmail = (String) updates.get("primary_email");
                existingSPByEmail = findSPbyEmail(primaryEmail);
            }

            if ((existingSPByUsername != null) || existingSPByEmail != null) {
                if (existingSPByUsername != null && !existingSPByUsername.getService_provider_id().equals(userId)) {
                    return responseService.generateErrorResponse("Username is not available", HttpStatus.BAD_REQUEST);
                }
                if (existingSPByEmail != null && !existingSPByEmail.getService_provider_id().equals(userId)) {
                    return responseService.generateErrorResponse("Email already in use", HttpStatus.BAD_REQUEST);
                }
            }
            List<Skill> serviceProviderSkills = new ArrayList<>();
            List<ServiceProviderInfra> serviceProviderInfras = new ArrayList<>();
            List<ServiceProviderLanguage> serviceProviderLanguages = new ArrayList<>();
            List<Integer> infraList = getIntegerList(updates, "infra_list");
            List<Integer> skillList = getIntegerList(updates, "skill_list");
            List<Integer> languageList = getIntegerList(updates, "language_list");
            if (updates.containsKey("has_technical_knowledge")) {
                if ((boolean) updates.get("has_technical_knowledge").equals(true)) {
                    if (!skillList.isEmpty()) {
                        for (int skill_id : skillList) {
                            Skill skill = entityManager.find(Skill.class, skill_id);

                            if(skill.getSkill_id()==6 && skill.getSkill_name().equalsIgnoreCase("Any Other Expertise"))
                            {
                                if(!updates.containsKey("other_skill"))
                                {
                                    return ResponseService.generateErrorResponse("You have to enter the other skill",HttpStatus.BAD_REQUEST);
                                }
                                else {
                                    String otherSkill = (String)updates.get("other_skill");
                                    if(otherSkill==null || otherSkill.trim().isEmpty())
                                    {
                                        return ResponseService.generateErrorResponse("other skill text field cannot be null or empty",HttpStatus.BAD_REQUEST);
                                    }
                                    assert existingServiceProvider != null;
                                    existingServiceProvider.setOtherSkill(otherSkill);

                                }
                            }
                            else {
                                 existingServiceProvider.setOtherSkill(null);
                            }
                            if (skill != null) {
                                if (!serviceProviderSkills.contains(skill))
                                    serviceProviderSkills.add(skill);
                            }
                        }
                    }
                }
            } else {
                if (!existingServiceProvider.getSkills().isEmpty()) {
                    serviceProviderSkills = existingServiceProvider.getSkills();

                } else
                    serviceProviderSkills = null;
            }
            updates.remove("other_skill");
            TypedQuery<ScoringCriteria> typedQuery = entityManager.createQuery(Constant.GET_ALL_SCORING_CRITERIA, ScoringCriteria.class);
            List<ScoringCriteria> scoringCriteriaList = typedQuery.getResultList();

            Integer totalScore = 0;
            ScoringCriteria scoringCriteriaToMap = null;

            if (updates.containsKey("has_technical_knowledge")) {
                if (updates.containsKey("skill_list") && updates.get("has_technical_knowledge").equals(true)) {
                    List<Integer> skillListToGet = getIntegerList(updates, "skill_list");
                    int totalSkills = skillListToGet.size();
                    if (totalSkills <= 4) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(8L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            return ResponseService.generateErrorResponse("Scoring Criteria is not found for Technical Expertise Score", HttpStatus.BAD_REQUEST);
                        } else {
                            Integer totalTechnicalScores = totalSkills * scoringCriteriaToMap.getScore();
                            existingServiceProvider.setTechnicalExpertiseScore(totalTechnicalScores);
                            scoringCriteriaToMap = null;
                        }
                    }
                    if (totalSkills >= 5) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(9L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            return ResponseService.generateErrorResponse("Scoring Criteria is not found for Technical Expertise Score", HttpStatus.BAD_REQUEST);
                        } else {
                            existingServiceProvider.setTechnicalExpertiseScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    }
                } else if (updates.containsKey("skill_list") && updates.get("has_technical_knowledge").equals(false)) {
                    existingServiceProvider.setTechnicalExpertiseScore(0);
                }
            }

            if (!infraList.isEmpty()) {
                for (int infra_id : infraList) {
                    ServiceProviderInfra serviceProviderInfrastructure = entityManager.find(ServiceProviderInfra.class, infra_id);
                    if (serviceProviderInfrastructure != null) {
                        if (!serviceProviderInfras.contains(serviceProviderInfrastructure))
                            serviceProviderInfras.add(serviceProviderInfrastructure);
                    }
                }
            } else {
                serviceProviderInfras = existingServiceProvider.getInfra();
            }
            if(updates.containsKey("pfpNa")&& (Boolean) updates.get("pfpNa"))
            {
                System.out.println("hii");
                    Iterator<ServiceProviderDocument> iterator = existingServiceProvider.getDocuments().iterator();
                    while (iterator.hasNext()) {
                        ServiceProviderDocument document = iterator.next();
                        if (document.getDocumentType().getDocument_type_id() == 17) {
                            iterator.remove();
                        }
                    }
            }
            if (!languageList.isEmpty()) {
                for (int language_id : languageList) {
                    ServiceProviderLanguage serviceProviderLanguage = entityManager.find(ServiceProviderLanguage.class, language_id);
                    if (serviceProviderLanguage != null) {
                        if (!serviceProviderLanguages.contains(serviceProviderLanguage))
                            serviceProviderLanguages.add(serviceProviderLanguage);
                    }
                }
            } else {
                serviceProviderLanguages = existingServiceProvider.getLanguages();
            }
            existingServiceProvider.setInfra(serviceProviderInfras);
            existingServiceProvider.setSkills(serviceProviderSkills);
            existingServiceProvider.setLanguages(serviceProviderLanguages);

            if (existingServiceProvider.getType().equalsIgnoreCase("INDIVIDUAL")) {
                if (updates.containsKey("infra_list") && (updates.get("infra_list") instanceof List) && !((List<?>) updates.get("infra_list")).isEmpty()) {
                    List<ServiceProviderInfra> infrastructures = existingServiceProvider.getInfra();
                    int totalInfras = infrastructures.size();
                    if (totalInfras >= 5) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(13L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            return ResponseService.generateErrorResponse("Scoring Criteria is not found for Infra Score", HttpStatus.BAD_REQUEST);
                        } else {
                            existingServiceProvider.setInfraScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    } else if (totalInfras >= 2 && totalInfras <= 4) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(14L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            return ResponseService.generateErrorResponse("Scoring Criteria is not found for Infra Score", HttpStatus.BAD_REQUEST);
                        } else {
                            existingServiceProvider.setInfraScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    } else if (totalInfras == 1) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(15L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            return ResponseService.generateErrorResponse("Scoring Criteria is not found for Infra Score", HttpStatus.BAD_REQUEST);
                        } else {
                            existingServiceProvider.setInfraScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    }
                } else if (updates.containsKey("infra_list") && (updates.get("infra_list") instanceof List) && ((List<?>) updates.get("infra_list")).isEmpty()) {
                    scoringCriteriaToMap = traverseListOfScoringCriteria(16L, scoringCriteriaList, existingServiceProvider);
                    if (scoringCriteriaToMap == null) {
                        return ResponseService.generateErrorResponse("Scoring Criteria is not found for Infra Score", HttpStatus.BAD_REQUEST);
                    } else {
                        existingServiceProvider.setInfraScore(scoringCriteriaToMap.getScore());
                        scoringCriteriaToMap = null;
                    }
                }

                if (updates.containsKey("partTimeOrFullTime")) {
                    if (existingServiceProvider.getPartTimeOrFullTime().equalsIgnoreCase("PART TIME")) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(18L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            return ResponseService.generateErrorResponse("Scoring Criteria is not found for Part time or Full time Score", HttpStatus.BAD_REQUEST);
                        } else {
                            existingServiceProvider.setPartTimeOrFullTimeScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    }
                    if (existingServiceProvider.getPartTimeOrFullTime().equalsIgnoreCase("FULL TIME")) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(17L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            return ResponseService.generateErrorResponse("Scoring Criteria is not found for Part time or Full time Score", HttpStatus.BAD_REQUEST);
                        } else {
                            existingServiceProvider.setPartTimeOrFullTimeScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    }
                }
            }
            updates.remove("skill_list");
            updates.remove("infra_list");
            updates.remove("language_list");


            if (updates.containsKey("date_of_birth")) {
                String dob = (String) updates.get("date_of_birth");
                if (sharedUtilityService.isFutureDate(dob))
                    errorMessages.add("DOB cannot be in future");
            }

            if (updates.containsKey("secondary_email") && "".equals(updates.get("secondary_email"))) {
                existingServiceProvider.setSecondary_email(null);
                updates.remove("secondary_email");
            }


            if (updates.containsKey("aadhaar_number")) {
                String newAadhaarNumber = (String) updates.get("aadhaar_number");

                if (newAadhaarNumber == null || newAadhaarNumber.trim().isEmpty()) {
                    errorMessages.add("Aadhaar number cannot be empty");
                } else {
                    String existingAadhaarNumber = (String) entityManager.createQuery(
                                        "SELECT sp.aadhaar_number FROM ServiceProviderEntity sp WHERE sp.service_provider_id = :id", String.class)
                                .setParameter("id", userId)
                                .getSingleResult();

                    if (!newAadhaarNumber.equals(existingAadhaarNumber)) {
                            Long aadhaarCount = entityManager.createQuery(
                                            "SELECT COUNT(sp) FROM ServiceProviderEntity sp WHERE sp.aadhaar_number = :aadhaar_number AND sp.service_provider_id != :id", Long.class)
                                    .setParameter("aadhaar_number", newAadhaarNumber)
                                    .setParameter("id", userId)
                                    .getSingleResult();

                            if (aadhaarCount > 0) {
                                return ResponseService.generateErrorResponse("Aadhaar number already exists", HttpStatus.BAD_REQUEST);
                            }
                        }

                }
            }

            if (updates.containsKey("pan_number")) {
                String newPanNumber = (String) updates.get("pan_number");

                if (newPanNumber == null || newPanNumber.trim().isEmpty()) {
                    errorMessages.add("PAN number cannot be empty");
                } else {
                    // Fetch existing PAN number from DB for current record
                    String existingPanNumber = (String) entityManager.createQuery(
                                    "SELECT sp.pan_number FROM ServiceProviderEntity sp WHERE sp.service_provider_id = :id", String.class)
                            .setParameter("id", userId)
                            .getSingleResult();

                    // UNIQUENESS CHECK
                    if (!newPanNumber.equals(existingPanNumber)) {
                        Long panCount = entityManager.createQuery(
                                        "SELECT COUNT(sp) FROM ServiceProviderEntity sp WHERE sp.pan_number = :pan_number AND sp.service_provider_id != :id", Long.class)
                                .setParameter("pan_number", newPanNumber)
                                .setParameter("id", userId)
                                .getSingleResult();

                        if (panCount > 0) {
                            return ResponseService.generateErrorResponse("PAN number already exists", HttpStatus.BAD_REQUEST);
                        }
                    }
                }
            }

            // Update only the fields that are present in the map using reflections
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String fieldName = entry.getKey();
                Object newValue = entry.getValue();

                Field field = ServiceProviderEntity.class.getDeclaredField(fieldName);
                System.out.println(field);
                Column columnAnnotation = field.getAnnotation(Column.class);
                boolean isColumnNotNull = (columnAnnotation != null && !columnAnnotation.nullable());
                // Check if the field has the @Nullable annotation
                boolean isNullable = field.isAnnotationPresent(Nullable.class);
                field.setAccessible(true);
                if (newValue.toString().isEmpty() && !isNullable)
                    errorMessages.add(fieldName + " cannot be null");
                if (newValue.toString().isEmpty() && isNullable)
                    continue;
                if (newValue != null) {
                    if (field.isAnnotationPresent(Size.class)) {
                        Size sizeAnnotation = field.getAnnotation(Size.class);
                        int min = sizeAnnotation.min();
                        int max = sizeAnnotation.max();
                        if (newValue.toString().length() > max || newValue.toString().length() < min) {
                            if (max == min)
                                errorMessages.add(fieldName + " size should be of size " + max);
                            else
                                errorMessages.add(fieldName + " size should be in between " + min + " " + max);
                            continue;
                        }
                    }
                    if (field.isAnnotationPresent(Email.class)) {
                        Email emailAnnotation = field.getAnnotation(Email.class);
                        String message = emailAnnotation.message();
                        if (fieldName.equals("primary_email")) {
                            if (newValue.equals((String) updates.get("secondary_email")) || (existingServiceProvider.getSecondary_email() != null && newValue.equals(existingServiceProvider.getSecondary_email())))
                                errorMessages.add("primary and secondary email cannot be same");
                        } else if (fieldName.equals("secondary_email")) {
                            if (newValue.equals((String) updates.get("primary_email")) || (existingServiceProvider.getPrimary_email() != null && newValue.equals(existingServiceProvider.getPrimary_email())))
                                errorMessages.add("primary and secondary email cannot be same");
                        }
                        if (!sharedUtilityService.isValidEmail((String) newValue)) {
                            errorMessages.add(message.replace("{field}", fieldName));
                            continue;
                        }
                    }
                    if (field.isAnnotationPresent(Pattern.class)) {
                        Pattern patternAnnotation = field.getAnnotation(Pattern.class);
                        String regex = patternAnnotation.regexp();
                        String message = patternAnnotation.message(); // Get custom message
                        if (!newValue.toString().matches(regex)) {
                            errorMessages.add(fieldName + "is invalid"); // Use a placeholder
                            continue;
                        }
                    }

                    if (fieldName.equals("date_of_birth")) {
                        String dobString = (String) newValue;
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                        try {
                            LocalDate dob = LocalDate.parse(dobString, formatter);
                            if (dob.isAfter(LocalDate.now())) {
                                errorMessages.add("Date of birth cannot be in the future");
                            }
                        } catch (DateTimeParseException e) {
                            errorMessages.add("Invalid date format for " + fieldName + ". Expected format is DD-MM-YYYY.");
                        }
                    }
                }
                field.setAccessible(true);
                // Optionally, check for type compatibility before setting the value
                if (newValue != null && field.getType().isAssignableFrom(newValue.getClass())) {
                    field.set(existingServiceProvider, newValue);
                }
            }
            if (!errorMessages.isEmpty()) {
                StringBuilder response= new StringBuilder();
                for(String error:errorMessages)
                {
                    response.append(error).append(",");
                }
                response = new StringBuilder(response.substring(0, response.length() - 1));
                return ResponseService.generateErrorResponse(response.toString(), HttpStatus.BAD_REQUEST);
            }
            // Merge the updated entity
            entityManager.merge(existingServiceProvider);
            if (existingServiceProvider.getUser_name() == null && !existingServiceProvider.getSpAddresses().isEmpty()) {
                String username = generateUsernameForServiceProvider(existingServiceProvider);
                existingServiceProvider.setUser_name(username);
            }
            entityManager.merge(existingServiceProvider);


            if (updates.containsKey("work_experience_in_months")) {
                if (existingServiceProvider.getWorkExperienceScore() != null && existingServiceProvider.getWork_experience_in_months() < 12) {
                    existingServiceProvider.setWorkExperienceScore(0);
                } else if (existingServiceProvider.getWork_experience_in_months() != null && existingServiceProvider.getWork_experience_in_months() >= 12
                        && existingServiceProvider.getWork_experience_in_months() <= 23) {
                    scoringCriteriaToMap = traverseListOfScoringCriteria(2L, scoringCriteriaList, existingServiceProvider);
                    if (scoringCriteriaToMap == null) {
                        return ResponseService.generateErrorResponse("Scoring Criteria is not found for scoring Work Experience Score", HttpStatus.BAD_REQUEST);
                    } else {
                        existingServiceProvider.setWorkExperienceScore(scoringCriteriaToMap.getScore());
                        scoringCriteriaToMap = null;
                    }
                } else if (existingServiceProvider.getWork_experience_in_months() != null && existingServiceProvider.getWork_experience_in_months() >= 24
                        && existingServiceProvider.getWork_experience_in_months() <= 35) {
                    scoringCriteriaToMap = traverseListOfScoringCriteria(3L, scoringCriteriaList, existingServiceProvider);
                    if (scoringCriteriaToMap == null) {
                        return ResponseService.generateErrorResponse("Scoring Criteria is not found for scoring Work Experience Score", HttpStatus.BAD_REQUEST);
                    } else {
                        existingServiceProvider.setWorkExperienceScore(scoringCriteriaToMap.getScore());
                        scoringCriteriaToMap = null;
                    }
                } else if (existingServiceProvider.getWork_experience_in_months() != null && existingServiceProvider.getWork_experience_in_months() >= 36
                        && existingServiceProvider.getWork_experience_in_months() <= 59) {
                    scoringCriteriaToMap = traverseListOfScoringCriteria(4L, scoringCriteriaList, existingServiceProvider);
                    if (scoringCriteriaToMap == null) {
                        return ResponseService.generateErrorResponse("Scoring Criteria is not found for scoring Work Experience Score", HttpStatus.BAD_REQUEST);
                    } else {
                        existingServiceProvider.setWorkExperienceScore(scoringCriteriaToMap.getScore());
                        scoringCriteriaToMap = null;
                    }
                } else if (existingServiceProvider.getWork_experience_in_months() != null && existingServiceProvider.getWork_experience_in_months() >= 60) {
                    scoringCriteriaToMap = traverseListOfScoringCriteria(5L, scoringCriteriaList, existingServiceProvider);
                    if (scoringCriteriaToMap == null) {
                        return ResponseService.generateErrorResponse("Scoring Criteria is not found for scoring Work Experience Score", HttpStatus.BAD_REQUEST);
                    } else {
                        existingServiceProvider.setWorkExperienceScore(scoringCriteriaToMap.getScore());
                        scoringCriteriaToMap = null;
                    }
                }
            }

            if (existingServiceProvider.getType().equalsIgnoreCase("PROFESSIONAL")) {
                if (updates.containsKey("is_running_business_unit")) {
                    if (Boolean.TRUE.equals(existingServiceProvider.getIs_running_business_unit())) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(1L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            return ResponseService.generateErrorResponse("Scoring Criteria is not found for scoring businessScore", HttpStatus.BAD_REQUEST);
                        } else {
                            existingServiceProvider.setBusinessUnitInfraScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    } else {
                        existingServiceProvider.setBusinessUnitInfraScore(0);
                    }
                }
                Integer numberOfEmployees=0;
                Object numEmpObj = updates.get("number_of_employees");
                if (numEmpObj != null) {
                    if (numEmpObj instanceof Number) {
                        numberOfEmployees = ((Number) numEmpObj).intValue();
                    } else if (numEmpObj instanceof String) {
                        try {
                            numberOfEmployees = Integer.parseInt((String) numEmpObj);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid number_of_employees format: must be an integer string", e);
                        }
                    } else {
                        throw new IllegalArgumentException("Unsupported type for number_of_employees: " + numEmpObj.getClass());
                    }
                    if(Boolean.TRUE.equals(existingServiceProvider.getIs_running_business_unit()))
                    {
                        existingServiceProvider.setNumber_of_employees(Integer.parseInt((String) numEmpObj));
                    }
                    else{
                        numberOfEmployees=0;
                    }
                }
                else {
                    if(Boolean.TRUE.equals(existingServiceProvider.getIs_running_business_unit()))
                    {
                        numberOfEmployees=existingServiceProvider.getNumber_of_employees();
                    }
                    else{
                         numberOfEmployees=0;
                     }
                }
                Boolean isRunning = existingServiceProvider.getIs_running_business_unit();
                System.out.println(isRunning);
                    if (numberOfEmployees != null && numberOfEmployees < 2 || !isRunning) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(12L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            return ResponseService.generateErrorResponse("Scoring Criteria is not found for scoring Staff Score", HttpStatus.BAD_REQUEST);
                        } else {
                            existingServiceProvider.setStaffScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    } else if (numberOfEmployees != null && numberOfEmployees >= 2
                            && numberOfEmployees <= 4 && isRunning) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(11L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            return ResponseService.generateErrorResponse("Scoring Criteria is not found for scoring Staff Score", HttpStatus.BAD_REQUEST);
                        } else {
                            existingServiceProvider.setStaffScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    } else if (numberOfEmployees != null && numberOfEmployees > 4 && isRunning) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(10L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            return ResponseService.generateErrorResponse("Scoring Criteria is not found for scoring Staff Score", HttpStatus.BAD_REQUEST);
                        } else {
                            existingServiceProvider.setStaffScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    }
            }
            else {
                totalScore=totalScore- existingServiceProvider.getStaffScore();
                existingServiceProvider.setStaffScore(0);
                existingServiceProvider.setBusinessUnitInfraScore(0);
            }

            if (existingServiceProvider.getType().equalsIgnoreCase("PROFESSIONAL")) {

                totalScore = existingServiceProvider.getBusinessUnitInfraScore() + existingServiceProvider.getWorkExperienceScore() + existingServiceProvider.getTechnicalExpertiseScore() + existingServiceProvider.getQualificationScore() + existingServiceProvider.getStaffScore();
            } else {
                existingServiceProvider.setBusinessUnitInfraScore(0);
                totalScore = existingServiceProvider.getInfraScore() + existingServiceProvider.getWorkExperienceScore() + existingServiceProvider.getTechnicalExpertiseScore() + existingServiceProvider.getQualificationScore() + existingServiceProvider.getPartTimeOrFullTimeScore();
            }
            if (existingServiceProvider.getWrittenTestScore() != null) {
                totalScore = totalScore + existingServiceProvider.getWrittenTestScore();
            }
            if (existingServiceProvider.getImageUploadScore() != null) {
                totalScore = totalScore + existingServiceProvider.getImageUploadScore();
            }

            existingServiceProvider.setTotalScore(0);
            existingServiceProvider.setTotalScore(totalScore);
            assignRank(existingServiceProvider, totalScore);

            Map<String, Object> serviceProviderMap = sharedUtilityService.serviceProviderDetailsMap(existingServiceProvider);

            return responseService.generateSuccessResponse("Service Provider Updated Successfully", serviceProviderMap, HttpStatus.OK);
        } catch (NoSuchFieldException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("No such field present :" + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error updating Service Provider : ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ScoringCriteria traverseListOfScoringCriteria(Long scoringCriteriaId, List<ScoringCriteria> scoringCriteriaList, ServiceProviderEntity existingServiceProvider) {
        for (ScoringCriteria scoringCriteria : scoringCriteriaList) {
            if (scoringCriteria.getId().equals(scoringCriteriaId)) {
                return scoringCriteria;
            }
        }
        return null;
    }

    public void assignRank(ServiceProviderEntity existingServiceProvider, Integer totalScore) {
        if(existingServiceProvider.getAutoScoring().equals(true))
        {
            if (existingServiceProvider.getType().equalsIgnoreCase("PROFESSIONAL")) {
                ServiceProviderRank serviceProviderRank = serviceProviderTestService.assignRankingForProfessional(totalScore);
                if (serviceProviderRank == null) {
                    throw new IllegalArgumentException("Service Provider Rank is not found for assigning a rank to the Professional ServiceProvider");
                }
                existingServiceProvider.setRanking(serviceProviderRank);
            } else {
                ServiceProviderRank serviceProviderRank = serviceProviderTestService.assignRankingForIndividual(totalScore);
                if (serviceProviderRank == null) {
                    throw new IllegalArgumentException("Service Provider Rank is not found for assigning a rank to the Individual ServiceProvider");
                }
                existingServiceProvider.setRanking(serviceProviderRank);
            }
        }
    }

    public List<String> validateAddressFields(Map<String, Object> updates) {
        List<String> errorMessages = new ArrayList<>();
        String state = (String) updates.get("state");
        String district = (String) updates.get("district");
        String pincode = (String) updates.get("pincode");
        String city = (String) updates.get("city");
        String residentialAddress = (String) updates.get("residential_address");

        String[] fieldNames = {"state", "district", "pincode", "residential_address", "city"};
        String[] fieldValues = {state, district, pincode, residentialAddress, city};

        for (int i = 0; i < fieldValues.length; i++) {
            if (fieldValues[i] == null || fieldValues[i].trim().isEmpty()) {
                errorMessages.add(fieldNames[i] + " cannot be empty");
            }
        }

        // Validate pincode format
        String pattern = Constant.PINCODE_REGEXP;
        if (pincode != null && !pincode.trim().isEmpty() && !java.util.regex.Pattern.matches(pattern, pincode)) {
            errorMessages.add("Pincode should contain only numbers and should be of length 6");
        }

        // Validate city format
        pattern = Constant.CITY_REGEXP;
        if (city != null && !city.trim().isEmpty() && !java.util.regex.Pattern.matches(pattern, city)) {
            errorMessages.add("Field city should only contain letters");
        }

        // Only parse and validate state/district if they're non-empty
        if (state != null && !state.trim().isEmpty()) {
            try {
                String stateName = districtService.findStateById(Integer.parseInt(state));
                if (stateName == null) {
                    errorMessages.add("Invalid State");
                }
            } catch (NumberFormatException e) {
                errorMessages.add("Invalid Current State ID format");
            }
        }

        if (district != null && !district.trim().isEmpty()) {
            try {
                String districtName = districtService.findDistrictById(Integer.parseInt(district));
                if (districtName == null) {
                    errorMessages.add("Invalid District");
                }
            } catch (NumberFormatException e) {
                errorMessages.add("Invalid Current District ID format");
            }
        }

        return errorMessages;
    }

    public List<String> validatePAddressFields(Map<String, Object> updates) {
        List<String> errorMessages = new ArrayList<>();
        String state = (String) updates.get("permanent_state");
        String district = (String) updates.get("permanent_district");
        String pincode = (String) updates.get("permanent_pincode");
        String city = (String) updates.get("permanent_city");
        String residentialAddress = (String) updates.get("permanent_residential_address");

        String[] fieldNames = {
                "permanent_state", "permanent_district",
                "permanent_pincode", "permanent_residential_address",
                "permanent_city"
        };
        String[] fieldValues = {
                state, district, pincode, residentialAddress, city
        };

        for (int i = 0; i < fieldValues.length; i++) {
            if (fieldValues[i] == null || fieldValues[i].trim().isEmpty()) {
                errorMessages.add(fieldNames[i] + " cannot be empty");
            }
        }

        // Validate pincode format
        String pattern = Constant.PINCODE_REGEXP;
        if (pincode != null && !pincode.trim().isEmpty() && !java.util.regex.Pattern.matches(pattern, pincode)) {
            errorMessages.add("Pincode should contain only numbers and should be of length 6");
        }

        // Validate city format
        pattern = Constant.CITY_REGEXP;
        if (city != null && !city.trim().isEmpty() && !java.util.regex.Pattern.matches(pattern, city)) {
            errorMessages.add("Field city should only contain letters");
        }

        // Validate permanent_state
        if (state != null && !state.trim().isEmpty()) {
            try {
                String stateName = districtService.findStateById(Integer.parseInt(state));
                if (stateName == null) {
                    errorMessages.add("Invalid State");
                }
            } catch (NumberFormatException e) {
                errorMessages.add("Invalid Permanent State ID format");
            }
        }

        // Validate permanent_district
        if (district != null && !district.trim().isEmpty()) {
            try {
                String districtName = districtService.findDistrictById(Integer.parseInt(district));
                if (districtName == null) {
                    errorMessages.add("Invalid District");
                }
            } catch (NumberFormatException e) {
                errorMessages.add("Invalid Permanent District ID format");
            }
        }

        return errorMessages;
    }


    @Override
    public ServiceProviderEntity getServiceProviderById(Long userId) {
        return entityManager.find(ServiceProviderEntity.class, userId);
    }

    @Transactional
    public ResponseEntity<?> sendOtpToMobile(String mobileNumber, String countryCode) {

        if (mobileNumber == null || mobileNumber.isEmpty()) {
            throw new IllegalArgumentException("Mobile number cannot be null or empty");
        }

        try {
            Twilio.init(accountSid, authToken);
            String completeMobileNumber = Constant.COUNTRY_CODE + mobileNumber;
            String otp = generateOTP();

/*            Message message = Message.creator(

                            new PhoneNumber(completeMobileNumber),
                            new PhoneNumber(twilioPhoneNumber),
                            otp)


                    .create();
*/

            ServiceProviderEntity existingServiceProvider = findServiceProviderByPhone(mobileNumber, countryCode);
            if (existingServiceProvider == null) {
                ServiceProviderEntity serviceProviderEntity = new ServiceProviderEntity();
                serviceProviderEntity.setService_provider_id(customerService.findNextCustomerId());
                serviceProviderEntity.setCountry_code(Constant.COUNTRY_CODE);
                serviceProviderEntity.setMobileNumber(mobileNumber);
                serviceProviderEntity.setOtp(otp);
                entityManager.persist(serviceProviderEntity);

            } else {
                existingServiceProvider.setOtp(otp);
                entityManager.merge(existingServiceProvider);
            }


            return responseService.generateSuccessResponse("OTP has been sent successfully !!!", otp, HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return responseService.generateErrorResponse("Unauthorized access: Please check your API key", HttpStatus.UNAUTHORIZED);
            } else {
                exceptionHandling.handleHttpClientErrorException(e);
                return responseService.generateErrorResponse("Internal server error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return responseService.generateErrorResponse("Error sending OTP: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error sending OTP: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public synchronized String generateOTP() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(8999);
        return String.valueOf(otp);
    }

    @Transactional
    public boolean setotp(String mobileNumber, String countryCode) {
        ServiceProviderEntity exisitingServiceProvider = findServiceProviderByPhone(mobileNumber, countryCode);

        if (exisitingServiceProvider != null) {
            String storedOtp = exisitingServiceProvider.getOtp();
            if (storedOtp != null) {
                exisitingServiceProvider.setOtp(null);
                entityManager.merge(exisitingServiceProvider);
                return true;
            }
        }
        return false;
    }

    public boolean isValidMobileNumber(String mobileNumber) {

        if (mobileNumber.startsWith("0")) {
            mobileNumber = mobileNumber.substring(1);
        }
        String mobileNumberPattern = "^\\d{9,13}$";
        return java.util.regex.Pattern.compile(mobileNumberPattern).matcher(mobileNumber).matches();
    }

    public ServiceProviderEntity findServiceProviderByPhone(String mobileNumber, String countryCode) {

        return entityManager.createQuery(Constant.PHONE_QUERY_SERVICE_PROVIDER, ServiceProviderEntity.class)
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("country_code", countryCode)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public ServiceProviderEntity findServiceProviderByUserName(String username) {

        return entityManager.createQuery(Constant.USERNAME_QUERY_SERVICE_PROVIDER, ServiceProviderEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    //find service provider by phone and validate the password.
    public ResponseEntity<?> authenticateByPhone(String mobileNumber, String countryCode, String password, HttpServletRequest request, HttpSession session) throws Exception {
        ServiceProviderEntity existingServiceProvider = findServiceProviderByPhone(mobileNumber, countryCode);
            if(existingServiceProvider.getIsArchived())
                return ResponseService.generateErrorResponse("Your account is supsended ,please contact support.",HttpStatus.UNAUTHORIZED);
        return validateServiceProvider(existingServiceProvider, password, request, session);
    }

    //find service provider by username and validate the password.
    public ResponseEntity<?> authenticateByUsername(String username, String password, HttpServletRequest request, HttpSession session) throws Exception {
        ServiceProviderEntity existingServiceProvider = findServiceProviderByUserName(username);
        if(existingServiceProvider.getIsArchived())
            return ResponseService.generateErrorResponse("Your account is supsended ,please contact support.",HttpStatus.UNAUTHORIZED);
        return validateServiceProvider(existingServiceProvider, password, request, session);
    }

    //mechanism to check password
    @Transactional
    public ResponseEntity<?> validateServiceProvider(ServiceProviderEntity serviceProvider, String password, HttpServletRequest request, HttpSession session) throws Exception {
        if (serviceProvider == null) {
            return responseService.generateErrorResponse("No Records Found", HttpStatus.NOT_FOUND);
        }
        if(serviceProvider.getIsArchived())
            return ResponseService.generateErrorResponse("Your account is supsended ,please contact support.",HttpStatus.UNAUTHORIZED);
        if (passwordEncoder.matches(password, serviceProvider.getPassword())) {
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String tokenKey = "authTokenServiceProvider_" + serviceProvider.getMobileNumber();


            String existingToken = serviceProvider.getToken();

            Map<String, Object> serviceProviderResponse = sharedUtilityService.serviceProviderDetailsMap(serviceProvider);


            if (existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {

                Map<String, Object> responseBody = createAuthResponse(existingToken, serviceProviderResponse).getBody();

                return ResponseEntity.ok(responseBody);
            } else {
                String newToken = jwtUtil.generateToken(serviceProvider.getService_provider_id(), serviceProvider.getRole(), ipAddress, userAgent);
                session.setAttribute(tokenKey, newToken);

                serviceProvider.setToken(newToken);
                entityManager.persist(serviceProvider);

                Map<String, Object> responseBody = createAuthResponse(newToken, serviceProviderResponse).getBody();


                return ResponseEntity.ok(responseBody);


            }
        } else {
            return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public ResponseEntity<?> loginWithPassword(@RequestBody Map<String, Object> serviceProviderDetails, HttpServletRequest request, HttpSession session) {
        try {
            String mobileNumber = (String) serviceProviderDetails.get("mobileNumber");
            if (mobileNumber != null) {
                if (mobileNumber.startsWith("0"))
                    mobileNumber = mobileNumber.substring(1);
            }

            String username = (String) serviceProviderDetails.get("username");
            String password = (String) serviceProviderDetails.get("password");
            String countryCode = (String) serviceProviderDetails.getOrDefault("countryCode", Constant.COUNTRY_CODE);
            // Check for empty password
            if (password == null || password.isEmpty()) {
                return responseService.generateErrorResponse("Password cannot be empty", HttpStatus.BAD_REQUEST);

            }
            if (mobileNumber != null && !mobileNumber.isEmpty()) {
                return authenticateByPhone(mobileNumber, countryCode, password, request, session);
            } else if (username != null && !username.isEmpty()) {
                return authenticateByUsername(username, password, request, session);
            } else {
                return responseService.generateErrorResponse("Empty Phone Number or username", HttpStatus.BAD_REQUEST);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public ResponseEntity<?> loginWithUsernameAndOTP(String username, HttpSession session) {
        try {
            if (username == null) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);

            }
            ServiceProviderEntity existingServiceProivder = findServiceProviderByUserName(username);
            if (existingServiceProivder == null) {
                return responseService.generateErrorResponse("No records found", HttpStatus.NOT_FOUND);


            }
            if (existingServiceProivder.getMobileNumber() == null) {
                return responseService.generateErrorResponse("No mobile Number registerd for this account", HttpStatus.NOT_FOUND);

            }
            String countryCode = existingServiceProivder.getCountry_code();
            if (countryCode == null)
                countryCode = Constant.COUNTRY_CODE;
            return (sendOtp(existingServiceProivder.getMobileNumber(), countryCode, session));
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> sendOtp(String mobileNumber, String countryCode, HttpSession session) throws UnsupportedEncodingException {
        try {
            mobileNumber = mobileNumber.startsWith("0")
                    ? mobileNumber.substring(1)
                    : mobileNumber;
            if (countryCode == null)
                countryCode = Constant.COUNTRY_CODE;
            Bucket bucket = rateLimiterService.resolveBucket(mobileNumber, "/service-provider/otp/send-otp");
            if (bucket.tryConsume(1)) {
                if (!isValidMobileNumber(mobileNumber)) {
                    return responseService.generateErrorResponse("Invalid mobile number", HttpStatus.BAD_REQUEST);

                }
                ResponseEntity<?> otpResponse = twilioService.sendOtpToMobile(mobileNumber, countryCode);
                return otpResponse;
            } else {
                return responseService.generateErrorResponse("You can send OTP only once in 1 minute", HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);

            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public String generateUsernameForServiceProvider(ServiceProviderEntity serviceProviderDetails) {
        String firstName = serviceProviderDetails.getFirst_name();
        String lastName = serviceProviderDetails.getLast_name();
        String state = serviceProviderDetails.getSpAddresses().get(0).getState();
        String username = null;
        StateCode stateDetails;
        if (firstName != null && lastName != null && state != null) {
            stateDetails = findStateCode(state);
            username = stateDetails.getState_code() + firstName + lastName;
            //suffix check
            //if a user already exist with username like PBRajSharma
            if (!findServiceProviderListByUsername(username).isEmpty()) {
                List<ServiceProviderEntity> listOfSp = findServiceProviderListByUsername(username);
                ServiceProviderEntity serviceProvider = listOfSp.get(listOfSp.size() - 1);
                String suffix = serviceProvider.getUser_name().substring(serviceProvider.getUser_name().length() - 2);
                int suffixValue = Integer.parseInt(suffix);
                if (suffixValue < 9)
                    username = username + "0" + Integer.toString(suffixValue + 1);
                else
                    username = username + Integer.toString(suffixValue + 1);
            }
            //simply adding 01 if there are no users for the given username
            else
                username = username + "01";
        }
        return username;
    }

    @Transactional
    public ResponseEntity<?> verifyOtp(Map<String, Object> serviceProviderDetails, HttpSession session, HttpServletRequest request) {
        try {
            String username = (String) serviceProviderDetails.get("username");
            String otpEntered = (String) serviceProviderDetails.get("otpEntered");
            String mobileNumber = (String) serviceProviderDetails.get("mobileNumber");
            String countryCode = (String) serviceProviderDetails.get("countryCode");
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }

            if (username != null) {
                ServiceProviderEntity serviceProvider = findServiceProviderByUserName(username);
                if (serviceProvider == null) {
                    return responseService.generateErrorResponse("No records found ", HttpStatus.NOT_FOUND);

                }
                mobileNumber = serviceProvider.getMobileNumber();
            } else if (mobileNumber == null || mobileNumber.isEmpty()) {
                return responseService.generateErrorResponse("mobile number can not be null ", HttpStatus.BAD_REQUEST);

            }

            if (!isValidMobileNumber(mobileNumber)) {
                return responseService.generateErrorResponse("Invalid mobile number ", HttpStatus.BAD_REQUEST);

            }
            if (mobileNumber.startsWith("0"))
                mobileNumber = mobileNumber.substring(1);
            ServiceProviderEntity existingServiceProvider = findServiceProviderByPhone(mobileNumber, countryCode);

            if (existingServiceProvider == null) {
                return responseService.generateErrorResponse("Invalid Data Provided ", HttpStatus.UNAUTHORIZED);

            }
            Integer role = existingServiceProvider.getRole();
            String storedOtp = existingServiceProvider.getOtp();
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String tokenKey = "authTokenServiceProvider_" + mobileNumber;


            if (otpEntered == null || otpEntered.trim().isEmpty()) {
                return responseService.generateErrorResponse("OTP cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (otpEntered.equals(storedOtp)) {
                existingServiceProvider.setOtp(null);
                entityManager.merge(existingServiceProvider);


                String existingToken = existingServiceProvider.getToken();


                Map<String, Object> serviceProviderResponse = sharedUtilityService.serviceProviderDetailsMap(existingServiceProvider);
                if (existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {


                    Map<String, Object> responseBody = createAuthResponse(existingToken, serviceProviderResponse).getBody();


                    return ResponseEntity.ok(responseBody);
                } else {
                    String newToken = jwtUtil.generateToken(existingServiceProvider.getService_provider_id(), role, ipAddress, userAgent);
                    session.setAttribute(tokenKey, newToken);

                    existingServiceProvider.setToken(newToken);
                    entityManager.persist(existingServiceProvider);
                    Map<String, Object> responseBody = createAuthResponse(newToken, serviceProviderResponse).getBody();
                    if (existingServiceProvider.getSignedUp() == 0) {
                        existingServiceProvider.setSignedUp(1);
                        entityManager.merge(existingServiceProvider);
                        responseBody.put("message", "User has been signed up");
                    }

                    return ResponseEntity.ok(responseBody);
                }
            } else {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.UNAUTHORIZED);

            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Otp verification error" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, Object>> createAuthResponse(String token, Map<String, Object> serviceProviderEntity) {
        Map<String, Object> responseBody = new HashMap<>();

        Map<String, Object> data = new HashMap<>();
        data.put("serviceproviderDetails", serviceProviderEntity);
        responseBody.put("status_code", HttpStatus.OK.value());
        responseBody.put("data", data);
        responseBody.put("token", token);
        responseBody.put("message", "User has been logged in");
        responseBody.put("status", "OK");

        return ResponseEntity.ok(responseBody);
    }

    public StateCode findStateCode(String state_name) {

        return entityManager.createQuery(Constant.STATE_CODE_QUERY, StateCode.class)
                .setParameter("state_name", state_name)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public ServiceProviderEntity findSPbyEmail(String email) {

        return entityManager.createQuery(Constant.SP_EMAIL_QUERY, ServiceProviderEntity.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public ServiceProviderAddressRef findAddressName(String address_name) {

        return entityManager.createQuery(Constant.GET_SERVICE_PROVIDER_DEFAULT_ADDRESS, ServiceProviderAddressRef.class)
                .setParameter("address_name", address_name)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<ServiceProviderEntity> findServiceProviderListByUsername(String username) {
        username = username + "%";
        return entityManager.createQuery(Constant.SP_USERNAME_QUERY, ServiceProviderEntity.class)
                .setParameter("username", username)
                .getResultList();
    }

    @Transactional
    public ResponseEntity<?> addAddress(long serviceProviderId, ServiceProviderAddress serviceProviderAddress) throws Exception {
        try {
            if (serviceProviderAddress == null) {
                return responseService.generateErrorResponse("Incomplete Details", HttpStatus.BAD_REQUEST);
            }
            ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (existingServiceProvider == null) {
                return responseService.generateErrorResponse("Service Provider Not found", HttpStatus.BAD_REQUEST);
            }
            List<ServiceProviderAddress> addresses = existingServiceProvider.getSpAddresses();
            for (ServiceProviderAddress serviceProviderAddressToAdd : addresses) {
                if (serviceProviderAddressToAdd.getAddress_type_id() == serviceProviderAddress.getAddress_type_id())
                    return ResponseService.generateErrorResponse("Cannot add another address of this type", HttpStatus.BAD_REQUEST);
            }
            if (!isOnlyDigits(serviceProviderAddress.getState()) || !isOnlyDigits(serviceProviderAddress.getDistrict()))
                return ResponseService.generateErrorResponse("Invalid state or district", HttpStatus.BAD_REQUEST);
            serviceProviderAddress.setState(districtService.findStateById(Integer.parseInt(serviceProviderAddress.getState())));
            serviceProviderAddress.setDistrict(districtService.findDistrictById(Integer.parseInt(serviceProviderAddress.getDistrict())));
            addresses.add(serviceProviderAddress);
            existingServiceProvider.setSpAddresses(addresses);
            serviceProviderAddress.setServiceProviderEntity(existingServiceProvider);

            entityManager.persist(serviceProviderAddress);

            entityManager.merge(existingServiceProvider);
            return responseService.generateSuccessResponse("Address added successfully", serviceProviderAddress, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error adding address", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public List<String> updateAddress(long serviceProviderId, ServiceProviderAddress serviceProviderAddress, ServiceProviderAddress dto) throws Exception {
        List<String> errorList = new ArrayList<>();
        if (serviceProviderAddress == null) {
            errorList.add("Incomplete Details");
        }
        ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
        if (existingServiceProvider == null) {
            errorList.add("Incomplete Details");
        }
        ServiceProviderAddress addressToupdate = null;
        List<ServiceProviderAddress> addresses = existingServiceProvider.getSpAddresses();
        if (addresses.contains(serviceProviderAddress)) {
            for (ServiceProviderAddress iteratedAddress : addresses) {
                if (iteratedAddress.getAddress_id() == serviceProviderAddress.getAddress_id()) {
                    addressToupdate = iteratedAddress;
                    break;
                }
            }
        }
        for (Field field : ServiceProviderAddress.class.getDeclaredFields()) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            field.setAccessible(true);
            Object newValue = field.get(dto);
            if (newValue == null || (newValue.toString().isEmpty())) {
//                errorList.add(field.getName() + "cannot be empty");
            }
        }
        if (!errorList.isEmpty())
            return errorList;
        if (addressToupdate != null) {
            if (dto.getState() != null && !dto.getState().isEmpty())
                addressToupdate.setState(districtService.findStateById(Integer.parseInt(dto.getState())));
            if (dto.getDistrict() != null && !dto.getDistrict().isEmpty())
                addressToupdate.setDistrict(districtService.findDistrictById(Integer.parseInt(dto.getDistrict())));
            if (dto.getAddress_line() != null && !dto.getAddress_line().isEmpty())
                addressToupdate.setAddress_line(dto.getAddress_line());
            if (dto.getCity() != null && !dto.getCity().isEmpty())
                addressToupdate.setCity(dto.getCity());
            if (dto.getPincode() != null && !dto.getPincode().isEmpty())
                addressToupdate.setPincode(dto.getPincode());
            existingServiceProvider.setSpAddresses(addresses);
            serviceProviderAddress.setServiceProviderEntity(existingServiceProvider);
        }
        return errorList;
    }


    @Transactional
    public ResponseEntity<?> searchServiceProviderBasedOnGivenFields(
            List<String> state,
            List<String> district,
            String first_name,
            String last_name,
            String mobileNumber,
            Long test_status_id,
            Long ticketId,
            Integer role,
            Boolean completed,
            Boolean archived,
            Boolean approved,
            Boolean rejected,
            String userName,
            List<Integer> qualificationType) {

        try {
            CustomServiceProviderTicket customServiceProviderTicket = null;
            if (ticketId != null) {
                customServiceProviderTicket = entityManager.find(CustomServiceProviderTicket.class, ticketId);
            }

            // If all filter values are null/empty, return all service providers
            if (first_name == null && last_name == null &&
                    (state == null || state.isEmpty()) &&
                    (district == null || district.isEmpty()) &&
                    mobileNumber == null &&
                    userName == null &&
                    test_status_id == null &&
                    role == null && completed == null &&
                    archived == null && approved == null && rejected == null &&
                    (qualificationType == null || qualificationType.isEmpty())) {

                Query query = entityManager.createQuery(
                        "SELECT s FROM ServiceProviderEntity s JOIN ServiceProviderAddress a ON s = a.serviceProviderEntity",
                        ServiceProviderEntity.class);
                List<ServiceProviderEntity> serviceProviderEntityList = query.getResultList();
                List<Map<String, Object>> response = new ArrayList<>();
                for (ServiceProviderEntity serviceProvider : serviceProviderEntityList) {
                    response.add(sharedUtilityService.serviceProviderDetailsMap(serviceProvider));
                }
                return ResponseService.generateSuccessResponse("Service Providers", response, HttpStatus.OK);
            }

            // Validate mobile number format
            if (mobileNumber != null && !isNumeric(mobileNumber)) {
                throw new IllegalArgumentException("Mobile number is not in correct format.");
            }

            // Validate string fields for correct format
            if ((state != null && containsNonAlphabet(state)) ||
                    (district != null && containsNonAlphabet(district)) ||
                    (first_name != null && !isAlphabetOnly(first_name)) ||
                    (last_name != null && !isAlphabetOnly(last_name))) {
                throw new IllegalArgumentException("String values are not in the right format.");
            }

            Map<String, Character> alias = new HashMap<>();
            alias.put("state", 'a');
            alias.put("district", 'a');
            alias.put("first_name", 's');
            alias.put("last_name", 's');
            alias.put("role", 's');
            alias.put("completed", 's');
            alias.put("archived", 's');
            alias.put("approved", 's');
            alias.put("rejected", 's');


            // Trim and lowercase
            if (first_name != null) first_name = first_name.trim().toLowerCase();
            if (last_name != null) last_name = last_name.trim().toLowerCase();

            // Start building query
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT s.* FROM service_provider s ");
                    if(state!=null||district!=null) {
                        queryBuilder.append("JOIN custom_service_provider_address a ON s.service_provider_id = a.service_provider_id ")
                                .append("LEFT JOIN qualification_details qd ON s.service_provider_id = qd.service_provider_id ");
                    }
                    if(qualificationType!=null) {
                        queryBuilder.append("LEFT JOIN qualification_details qd ON s.service_provider_id = qd.service_provider_id ");
                    }
                   queryBuilder.append("WHERE ");

            // Add qualificationType filter
            if (qualificationType != null && !qualificationType.isEmpty()) {
                queryBuilder.append("qd.qualification_id IN :qualificationType AND ");
            }

            if (mobileNumber != null) {
                ServiceProviderEntity serviceProviderEntity = entityManager.createQuery(PHONE_QUERY_SERVICE_PROVIDER_FILTER, ServiceProviderEntity.class)
                        .setParameter("mobileNumber", mobileNumber)
                        .setParameter("country_code", "+91")
                        .getResultStream()
                        .findFirst()
                        .orElse(null);
                if (serviceProviderEntity != null) {
                    List<Map<String, Object>> response = new ArrayList<>();
                    response.add(sharedUtilityService.serviceProviderDetailsMap(serviceProviderEntity));
                        return ResponseService.generateSuccessResponse("Service Providers", response, HttpStatus.OK);
                }else {
                    // Return empty response
                    List<Map<String, Object>> response = new ArrayList<>();
                    return ResponseService.generateSuccessResponse("No Details found for the given mobile number", response, HttpStatus.OK);
                }
            }

            if (userName != null && !userName.trim().isEmpty()) {
                ServiceProviderEntity serviceProviderEntity = entityManager.createQuery(
                                "SELECT s FROM ServiceProviderEntity s WHERE LOWER(s.user_name) = LOWER(:user_name)", ServiceProviderEntity.class)
                        .setParameter("user_name", userName.trim().toLowerCase())
                        .getResultStream()
                        .findFirst()
                        .orElse(null);
                if (serviceProviderEntity != null) {
                    List<Map<String, Object>> response = new ArrayList<>();
                    response.add(sharedUtilityService.serviceProviderDetailsMap(serviceProviderEntity));
                    return ResponseService.generateSuccessResponse("Service Providers", response, HttpStatus.OK);
                } else {
                    // Return empty response
                    List<Map<String, Object>> response = new ArrayList<>();
                    return ResponseService.generateSuccessResponse("No Details found for the given UserName", response, HttpStatus.OK);
                }
            }

            if (test_status_id != null) {
                Query query = entityManager.createQuery("SELECT s FROM ServiceProviderTestStatus s WHERE s.test_status_id = :test_status_id", ServiceProviderTestStatus.class);
                query.setParameter("test_status_id", test_status_id);
                if (query.getResultList().isEmpty()) {
                    throw new IllegalArgumentException("No Test Status is found with this id");
                }
            }

            String[] fieldNames = {"first_name", "last_name",  "role", "completed", "archived", "approved", "rejected"};
            Object[] fieldValues = {first_name, last_name,  role, completed, archived, approved, rejected, };

            // Add fields dynamically
            for (int i = 0; i < fieldValues.length; i++) {
                if (fieldValues[i] != null) {
                    if (fieldNames[i].equals("first_name") || fieldNames[i].equals("last_name") || fieldNames[i].equals("user_name")) {
                        queryBuilder.append("LOWER(")
                                .append(alias.get(fieldNames[i])).append(".").append(fieldNames[i])
                                .append(") LIKE :").append(fieldNames[i]).append(" AND ");
                    } else {
                        queryBuilder.append(alias.get(fieldNames[i])).append(".").append(fieldNames[i])
                                .append(" = :").append(fieldNames[i]).append(" AND ");
                    }
                }
            }

            // Add state and district filters
            if (state != null && !state.isEmpty()) {
                queryBuilder.append("a.state IN :states AND ");
            }
            if (district != null && !district.isEmpty()) {
                queryBuilder.append("a.district IN :districts AND ");
            }

            // Remove last AND
            String queryString = queryBuilder.toString();
            if (queryString.endsWith(" AND ")) {
                queryString = queryString.substring(0, queryString.length() - 5);
            }

            // Final query
            Query finalQuery = entityManager.createNativeQuery(queryString, ServiceProviderEntity.class);

            // Bind parameters
            for (int i = 0; i < fieldValues.length; i++) {
                if (fieldValues[i] != null) {
                    if (fieldNames[i].equals("first_name") || fieldNames[i].equals("last_name") || fieldNames[i].equals("user_name")) {
                        finalQuery.setParameter(fieldNames[i], fieldValues[i] + "%"); // LIKE search
                    } else {
                        finalQuery.setParameter(fieldNames[i], fieldValues[i]);
                    }
                }
            }

            if (state != null && !state.isEmpty()) {
                finalQuery.setParameter("states", state);
            }
            if (district != null && !district.isEmpty()) {
                finalQuery.setParameter("districts", district);
            }
            if (qualificationType != null && !qualificationType.isEmpty()) {
                finalQuery.setParameter("qualificationType", qualificationType);
            }

            // Execute query
            List<ServiceProviderEntity> listOfSp = finalQuery.getResultList();

            // Ticket rejection filter
            if (customServiceProviderTicket != null) {
                Iterator<ServiceProviderEntity> iterator = listOfSp.iterator();
                while (iterator.hasNext()) {
                    ServiceProviderEntity serviceProvider = iterator.next();
                    if (customServiceProviderTicket.getRejectedBy().contains(serviceProvider.getService_provider_id())) {
                        iterator.remove();
                    }
                }
            }

            // Prepare response
            List<Map<String, Object>> response = new ArrayList<>();
            for (ServiceProviderEntity sp : listOfSp) {
                response.add(sharedUtilityService.serviceProviderDetailsMap(sp));
            }

            return ResponseService.generateSuccessResponse("Service Providers", response, HttpStatus.OK);

        } catch (PersistenceException e) {
            return ResponseService.generateErrorResponse("Error finding SP : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse("Error finding SP : " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error finding SP : " + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }







    private boolean containsNonAlphabet(List<String> list) {
        for (String s : list) {
            if (!isAlphabetOnly(s)) {
                return true;
            }
        }
        return false;
    }

    public List<ServiceProviderEntity> getAllSp(int page, int limit) {
        int startPosition = page * limit;
        // Create the query
        TypedQuery<ServiceProviderEntity> query = entityManager.createQuery(Constant.GET_ALL_SERVICE_PROVIDERS, ServiceProviderEntity.class);
        // Apply pagination
        query.setFirstResult(startPosition);
        query.setMaxResults(limit);
        List<ServiceProviderEntity> results = query.getResultList();
        return results;
    }

    public void serviceProviderTicketAssignedIncrement(ServiceProviderEntity serviceProvider) throws Exception {
        try {
            serviceProvider.setTicketAssigned(serviceProvider.getTicketAssigned() + 1);
            entityManager.merge(serviceProvider);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Exception caught while incrementing ticketAssigned of SP: " + exception.getMessage());
        }
    }

    public List<ServiceProviderEntity> getActiveAndApprovedServiceProviders() throws Exception {
        try {
            Query query = entityManager.createQuery("SELECT s FROM ServiceProviderTestStatus s WHERE s.test_status_id = :test_status_id", ServiceProviderTestStatus.class);
            // test status for 3L is APPROVED.
            query.setParameter("test_status_id", 3L);

            List<ServiceProviderTestStatus> serviceProviderTestStatus = query.getResultList();
            if (serviceProviderTestStatus.isEmpty()) {
                throw new IllegalArgumentException("No Test Status is found with this id");
            }

            ArrayList<Integer> roleIds = new ArrayList<>();
            roleIds.add(4);
            roleIds.add(2);

            query = entityManager.createQuery("SELECT s FROM ServiceProviderEntity s JOIN ServiceProviderAddress a ON s = a.serviceProviderEntity WHERE s.serviceProviderStatus = :testStatusId AND s.isActive = :isActive AND s.approved = :isApproved AND s.role IN :roleIds", ServiceProviderEntity.class);
            query.setParameter("testStatusId", serviceProviderTestStatus.get(0));
            query.setParameter("isActive", true);
            query.setParameter("isApproved", true);
            query.setParameter("roleIds", roleIds);

            return query.getResultList();

        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Exception caught fetching approve and active SP: " + exception.getMessage());
        }
    }

    private boolean areAddressesSame(ServiceProviderAddress a, ServiceProviderAddress b) {
        if (a == null || b == null) return false;

        return Objects.equals(a.getPincode(), b.getPincode()) &&
                Objects.equals(a.getDistrict(), b.getDistrict()) &&
                Objects.equals(a.getState(), b.getState()) &&
                Objects.equals(a.getCity(), b.getCity()) &&
                Objects.equals(a.getAddress_line(), b.getAddress_line());
    }

    @Transactional
    public Map<String, Object> updateServiceProviderDocument(Map<Integer, List<MultipartFile>> groupedFiles, Long customerId, String otherDocument, Long qualificationDetailId, String dateOfIssue, String validUpto, String role, Boolean removeFileTypes, Set<ServiceProviderDocument> serviceProviderDocumentToSave) throws Exception {

            String dateFormat = "yyyy-MM-dd";
            MultipartFile processedFile=null;

            // Service Provider logic
            ServiceProviderEntity serviceProviderEntity = entityManager.find(ServiceProviderEntity.class, customerId);
            if (serviceProviderEntity == null) {
                throw new NotFoundException("No data found for this serviceProvider");
            }

            Map<String, Object> responseData = new HashMap<>();
            List<String> deletedDocumentMessages = new ArrayList<>();

            // Handle file uploads and deletions
            for (Map.Entry<Integer, List<MultipartFile>> entry : groupedFiles.entrySet()) {
                Integer fileNameId = entry.getKey();
                List<MultipartFile> fileList = entry.getValue();

                DocumentType documentTypeObj = entityManager.createQuery(Constant.GET_DOCUMENT_TYPE_BY_DOCUMENT_TYPE_ID, DocumentType.class)
                        .setParameter("documentTypeId", fileNameId)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

                if (documentTypeObj == null) {
                    throw new IllegalArgumentException("Unknown document type for file: " + fileNameId);
                }

                if(documentTypeObj.getDocument_type_id().equals(Constant.DOCUMENT_TYPE_OTHER_ID))
                {
                    if(otherDocument==null)
                    {
                        throw new IllegalArgumentException("other Document name cannot be null for uploading other Documents");
                    }
                    if(otherDocument.trim().isEmpty())
                    {
                        throw new IllegalArgumentException("other Document name cannot be empty");
                    }
                }

                if (documentTypeObj.getIs_qualification_document().equals(true) && qualificationDetailId == null) {
                        throw new IllegalArgumentException("Qualification Detail id cannot be null for uploading Qualification Documents");
                    }

                if (documentTypeObj.getIs_issue_date_required().equals(true)) {
                    if (dateOfIssue == null) {
                        throw new IllegalArgumentException("Date of issue cannot be null");
                    }
                    if (documentTypeObj.getIs_expiration_date_required().equals(true) && validUpto == null) {
                        throw new IllegalArgumentException("Valid up to (expiration date of document) cannot be null");
                    }
                }

                for (MultipartFile file : fileList) {
                    if (documentTypeObj.getDocument_type_id().equals(Constant.DOCUMENT_TYPE_LIVE_PHOTOGRAPH_ID)) {  // If it's a Live Photo
                        processedFile = documentStorageService.convertToJpg(file);
                    }
                    else {
                        documentStorageService.validateDocument(file, documentTypeObj);
                    }

                    ServiceProviderDocument existingDocument = entityManager.createQuery(
                                    Constant.GET_DOCUMENT_DATA_OF_SERVICE_PROVIDER_BY_DOCUMENT_TYPE_ID, ServiceProviderDocument.class)
                            .setParameter("serviceProviderEntity", serviceProviderEntity)
                            .setParameter("documentType", documentTypeObj)

                            .getResultStream()
                            .findFirst()
                            .orElse(null);

                    // If live photograph first processed to jpg then upload.
                    if(documentTypeObj.getDocument_type_id().equals(Constant.DOCUMENT_TYPE_LIVE_PHOTOGRAPH_ID))
                    {
                        fileUploadService.uploadFileOnFileServer(processedFile, documentTypeObj.getDocument_type_name(), customerId.toString(), role);
                    }
                    else {
                        fileUploadService.uploadFileOnFileServer(file, documentTypeObj.getDocument_type_name(), customerId.toString(), role);
                    }

                    // Deletes file from file server.
                    if (removeFileTypes != null && removeFileTypes) {
                        if (existingDocument != null && !Objects.equals(fileNameId, Constant.DOCUMENT_TYPE_OTHER_ID)) {

                            String filePath = existingDocument.getFilePath();
                            if (filePath != null) {
                                fileUploadService.deleteFile(customerId, documentTypeObj.getDocument_type_name(), existingDocument.getName(), role);
                            }
                            existingDocument.setDocumentType(null);
                            existingDocument.setName(null);
                            existingDocument.setFilePath(null);
                            existingDocument.setServiceProviderEntity(null);
                            entityManager.persist(existingDocument);
                            serviceProviderDocumentToSave.add(existingDocument);
                            deletedDocumentMessages.add(documentTypeObj.getDocument_type_name() + " has been deleted.");
                            continue;
                        }
                    }

                    // For document type others.
                    if (Objects.equals(fileNameId, Constant.DOCUMENT_TYPE_OTHER_ID) && (!file.isEmpty() || file != null)) {
                        String newFileName = file.getOriginalFilename();
                        // Check for existing document with the same name
                        ServiceProviderDocument existingOtherDocument = entityManager.createQuery(Constant.GET_OTHER_DOCUMENT_DATA_OF_SERVICE_PROVIDER_BY_DOCUMENT_TYPE_ID, ServiceProviderDocument.class
                                )
                                .setParameter("serviceProviderEntity", serviceProviderEntity)
                                .setParameter("documentType", documentTypeObj)
                                .setParameter("otherDocument", otherDocument != null ? otherDocument.toLowerCase() : null)  // Avoid NullPointerException
                                .setParameter("documentName", newFileName)  // Ensure document name is included
                                .getResultStream()
                                .findFirst()
                                .orElse(null);

                        if (existingOtherDocument == null) {
                            ServiceProviderDocument serviceProviderDocument = documentStorageService.createDocumentServiceProvider(file, documentTypeObj, serviceProviderEntity, customerId, role);
                            if(documentTypeObj.getDocument_type_id().equals(Constant.DOCUMENT_TYPE_OTHER_ID))
                            {
                                serviceProviderDocument.setOtherDocument(otherDocument);
                                entityManager.merge(serviceProviderDocument);
                            }
                            serviceProviderDocumentToSave.add(serviceProviderDocument);
                        } // If document type other with same name already exists then.
                        else if (existingOtherDocument != null) {
                            String filePath = existingOtherDocument.getFilePath();
                            if (filePath != null) {
                                String absolutePath = System.getProperty("user.dir") + "/../test/" + filePath;
                                File oldFile = new File(absolutePath);
                                String oldFileName = oldFile.getName();
                                existingOtherDocument.setIsArchived(false);
                                if (!newFileName.equals(oldFileName)) {
                                    fileUploadService.deleteFile(customerId, documentTypeObj.getDocument_type_name(), existingOtherDocument.getName(), role);
                                    documentStorageService.updateOrCreateServiceProvider(existingOtherDocument, file, documentTypeObj, customerId, role);
                                }
                            }
                            entityManager.merge(existingOtherDocument);
                            serviceProviderDocumentToSave.add(existingOtherDocument);
                        }
                    }

                    // If the file is not empty and a document already exists, update the document
                    else if (existingDocument != null && (!file.isEmpty() || file != null) && fileNameId != 13) {
                        String filePath = existingDocument.getFilePath();
                        if (qualificationDetailId != null && documentTypeObj.getIs_qualification_document().equals(true)) {
                            QualificationDetails qualificationDetails = findQualificationDetailForServiceProvider(qualificationDetailId, serviceProviderEntity);
                            existingDocument.setIs_qualification_document(true);
                            existingDocument.setQualificationDetails(qualificationDetails);
                        }

                        if (dateOfIssue != null && documentTypeObj.getIs_issue_date_required().equals(true)) {
                            DocumentValidity documentValidity = null;
                            if (existingDocument.getDocumentValidity() == null) {
                                documentValidity = new DocumentValidity();
                                validateDate(dateOfIssue, validUpto, dateFormat);
                                documentValidity.setDate_of_issue(convertStringToDate(dateOfIssue, "yyyy-MM-dd"));
                                if (validUpto == null) {
                                    documentValidity.setIs_valid_upto_na(true);
                                    documentValidity.setValid_upto(null);
                                } else {
                                    documentValidity.setIs_valid_upto_na(false);
                                    documentValidity.setValid_upto(convertStringToDate(validUpto, "yyyy-MM-dd"));
                                }
                                documentValidity.setServiceProviderDocument(existingDocument);
                                existingDocument.setDocumentValidity(documentValidity);
                                entityManager.persist(documentValidity);

                            } else if (existingDocument.getDocumentValidity() != null) {
                                documentValidity = existingDocument.getDocumentValidity();
                                validateDate(dateOfIssue, validUpto, dateFormat);
                                documentValidity.setDate_of_issue(convertStringToDate(dateOfIssue, "yyyy-MM-dd"));
                                if (validUpto == null) {
                                    documentValidity.setIs_valid_upto_na(true);
                                    documentValidity.setValid_upto(null);
                                } else {
                                    documentValidity.setIs_valid_upto_na(false);
                                    documentValidity.setValid_upto(convertStringToDate(validUpto, "yyyy-MM-dd"));
                                }
                                documentValidity.setServiceProviderDocument(existingDocument);
                                existingDocument.setDocumentValidity(documentValidity);
                                entityManager.merge(documentValidity);
                            }
                        }

                        if (existingDocument != null && (!file.isEmpty() || file != null) && fileNameId != 13) {
//                                String filePath = existingDocument.getFilePath();
                            String fileName = existingDocument.getName();
                            boolean isLivePhoto = documentTypeObj.getDocument_type_id().equals(3);

                            // Additional validation before attempting to delete
                            if (filePath != null && fileName != null && !fileName.isEmpty()) {
                                try {
                                    // For live photos, ensure consistent naming across both systems
                                    if (isLivePhoto) {
                                        // Extract file extension from the name if possible
                                        String extension = "";
                                        int lastDotIndex = fileName.lastIndexOf('.');
                                        if (lastDotIndex > 0) {
                                            extension = fileName.substring(lastDotIndex);
                                        }

                                        // Ensure we're using consistent naming format for live photos
                                        // This assumes the same naming convention as used in documentStorageService.convertToJpg()
                                        String expectedFileName = "live_photo" + extension;

                                        if (!fileName.equals(expectedFileName)) {
                                            log.info("Warning: Live photo name mismatch. Expected: " + expectedFileName + ", Actual: " + fileName);
                                            // Use the expected name if they differ
                                            fileName = expectedFileName;
                                        }
                                    }

                                    // Call the delete method with properly validated parameters
                                    fileUploadService.deleteFile(customerId, documentTypeObj.getDocument_type_name(), fileName, role);
                                    log.info("File successfully deleted");

                                } catch (Exception e) {
                                    log.error("Error deleting file: {}", e.getMessage());
                                }
                            } else {
                                log.info("Skipping file deletion - missing path or filename information");
                            }

                            // Continue with updating the document
                            existingDocument.setIsArchived(false);

                            /*try {
                                // Always proceed with the document update regardless of delete success
                                if (isLivePhoto) {
                                    documentStorageService.updateOrCreateServiceProvider(existingDocument, processedFile, documentTypeObj, customerId, role);
                                }
                                else {
                                    documentStorageService.updateOrCreateServiceProvider(existingDocument, file, documentTypeObj, customerId, role);
                                }

                                entityManager.merge(existingDocument);
                                serviceProviderDocumentToSave.add(existingDocument);

                            } catch (Exception exception) {
                                System.err.println("Error updating document: " + e.getMessage());
                                throw e; // Rethrow this exception as it's a critical failure
                            }*/

                            // Always proceed with the document update regardless of delete success
                            if (isLivePhoto) {
                                documentStorageService.updateOrCreateServiceProvider(existingDocument, processedFile, documentTypeObj, customerId, role);
                            }
                            else {
                                documentStorageService.updateOrCreateServiceProvider(existingDocument, file, documentTypeObj, customerId, role);
                            }

                            entityManager.merge(existingDocument);
                            serviceProviderDocumentToSave.add(existingDocument);
                        }
                        entityManager.merge(existingDocument);
                        serviceProviderDocumentToSave.add(existingDocument);
                    } else {
                        // If the file is not empty create the document
                        if (!file.isEmpty() || file != null && (fileNameId != 13)) {
                            ServiceProviderDocument serviceProviderDocument = null;
                            if(documentTypeObj.getDocument_type_id().equals(3))
                            {
                                serviceProviderDocument = documentStorageService.createDocumentServiceProvider(processedFile, documentTypeObj, serviceProviderEntity, customerId, role);
                            }
                            else {
                                serviceProviderDocument = documentStorageService.createDocumentServiceProvider(file, documentTypeObj, serviceProviderEntity, customerId, role);
                            }
                            serviceProviderDocumentToSave.add(serviceProviderDocument);
                            if (qualificationDetailId != null && documentTypeObj.getIs_qualification_document().equals(true)) {
                                QualificationDetails qualificationDetails = findQualificationDetailForServiceProvider(qualificationDetailId, serviceProviderEntity);
                                serviceProviderDocument.setIs_qualification_document(true);
                                serviceProviderDocument.setQualificationDetails(qualificationDetails);
                                entityManager.merge(serviceProviderDocument);
                                serviceProviderDocumentToSave.add(serviceProviderDocument);
                            }
                            if (dateOfIssue != null && documentTypeObj.getIs_issue_date_required().equals(true)) {
                                DocumentValidity documentValidity = new DocumentValidity();
                                validateDate(dateOfIssue, validUpto, dateFormat);
                                documentValidity.setDate_of_issue(convertStringToDate(dateOfIssue, "yyyy-MM-dd"));
                                if (validUpto == null) {
                                    documentValidity.setIs_valid_upto_na(true);
                                    documentValidity.setValid_upto(null);
                                } else {
                                    documentValidity.setIs_valid_upto_na(false);
                                    documentValidity.setValid_upto(convertStringToDate(validUpto, "yyyy-MM-dd"));
                                }
                                documentValidity.setServiceProviderDocument(serviceProviderDocument);
                                entityManager.persist(documentValidity);
                                serviceProviderDocument.setDocumentValidity(documentValidity);
                                entityManager.merge(serviceProviderDocument);
                                serviceProviderDocumentToSave.add(serviceProviderDocument);
                            }
                        }
                    }
                }

            }
            List<Map<String, Object>> filteredDocuments = new ArrayList<>();

            for (ServiceProviderDocument document : serviceProviderDocumentToSave) {
                if (document.getIsArchived() != null && !document.getIsArchived()) { // Exclude archived documents
                    if (document.getFilePath() != null && document.getDocumentType() != null) {
                        Map<String, Object> documentDetails = new HashMap<>();
                        documentDetails.put("documentId", document.getDocumentId());
                        documentDetails.put("name", document.getName());
                        documentDetails.put("filePath", document.getFilePath());

                        // Add qualification details if applicable
                        if (Boolean.TRUE.equals(document.getIs_qualification_document()) && document.getQualificationDetails() != null) {
                            documentDetails.put("qualification_detail_id", qualificationDetailId);
                        }

                        // Add document validity details if applicable
                        if (document.getDocumentValidity() != null) {
                            Map<String, String> validityDetails = new HashMap<>();
                            validityDetails.put("dateOfIssue", dateOfIssue);
                            validityDetails.put("validUpto", validUpto);

                            documentDetails.put("documentValidity", validityDetails);
                        }
                        String filePath;
                        /*try {
                            filePath = documentStorageService.encrypt(document.getFilePath());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }*/
                        filePath = documentStorageService.encrypt(document.getFilePath());

                        String fileUrl = fileService.getFileUrl(filePath, request);
                        // Generate a file URL for the document
                        documentDetails.put("fileUrl", fileUrl);

                        Map<String, Object> documentTypeResponse = new HashMap<>();
                        documentTypeResponse.put("document_type_id", document.getDocumentType().getDocument_type_id());
                        if(otherDocument!=null && !otherDocument.trim().isEmpty())
                        {
                            documentTypeResponse.put("document_type_name", otherDocument);
                        }
                        else {
                            documentTypeResponse.put("document_type_name", document.getDocumentType().getDocument_type_name());
                        }
                        documentTypeResponse.put("description", document.getDocumentType().getDescription());
                        documentTypeResponse.put("is_qualification_document", document.getDocumentType().getIs_qualification_document());
                        documentTypeResponse.put("is_issue_date_required", document.getDocumentType().getIs_issue_date_required());
                        documentTypeResponse.put("is_expiration_date_required", document.getDocumentType().getIs_expiration_date_required());
                        documentTypeResponse.put("required_document_types", document.getDocumentType().getRequired_document_types());
                        documentTypeResponse.put("max_document_size", document.getDocumentType().getMax_document_size());
                        documentTypeResponse.put("min_document_size", document.getDocumentType().getMin_document_size());
                        documentTypeResponse.put("sort_order", document.getDocumentType().getSort_order());

                        documentDetails.put("documentType", documentTypeResponse);
                        filteredDocuments.add(documentDetails);
                    }
                }
            }


            log.info("Deleted Documents logs: {}", deletedDocumentMessages);
            responseData.put("uploadedDocuments", filteredDocuments);
            return responseData;
    }

    @Transactional
    public Map<String, Object> updateServiceProviderTicketDocument(Map<Integer, List<MultipartFile>> groupedFiles, Long serviceProviderId, String otherDocument, Long qualificationDetailId, String dateOfIssue, String validUpto, String role, Boolean removeFileTypes, CustomServiceProviderTicket ticket, Set<ServiceProviderDocument> serviceProviderDocumentToSave) throws Exception {
        try {

            MultipartFile processedFile=null;

            // Service Provider logic
            ServiceProviderEntity serviceProviderEntity = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (serviceProviderEntity == null) {
                throw new NotFoundException("No data found for this serviceProvider");
            }

            Map<String, Object> responseData = new HashMap<>();
            List<String> deletedDocumentMessages = new ArrayList<>();

            // Handle file uploads and deletions
            for (Map.Entry<Integer, List<MultipartFile>> entry : groupedFiles.entrySet()) {
                Integer fileNameId = entry.getKey();
                List<MultipartFile> fileList = entry.getValue();
                log.info("here the list of file is of size is: {}", fileList.size());

                DocumentType documentTypeObj = entityManager.createQuery(Constant.GET_DOCUMENT_TYPE_BY_DOCUMENT_TYPE_ID, DocumentType.class)
                        .setParameter("documentTypeId", fileNameId)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

                List<ServiceProviderDocument> existingDocuments = entityManager.createQuery(
                                Constant.GET_DOCUMENT_DATA_OF_SERVICE_PROVIDER_BY_DOCUMENT_TYPE_ID_AND_TICKET, ServiceProviderDocument.class)
                        .setParameter("serviceProviderEntity", serviceProviderEntity)
                        .setParameter("documentType", documentTypeObj)
                        .setParameter("serviceProviderTicket", ticket)
                        .getResultList();

                if (documentTypeObj == null) {
                    throw new IllegalArgumentException("Unknown document type for file: " + fileNameId);
                }

                for (MultipartFile file : fileList) {
                    documentStorageService.validateDocument(file, documentTypeObj);

                    fileUploadService.uploadFileOnFileServer(file, documentTypeObj.getDocument_type_name(), serviceProviderId.toString(), role);

                    if (removeFileTypes != null && removeFileTypes) {
                        for(ServiceProviderDocument existingDocument: existingDocuments) {
                            if (existingDocument != null && !Objects.equals(fileNameId, Constant.DOCUMENT_TYPE_OTHER_ID)) {

                                if (existingDocument.getFilePath() != null) {
                                    fileUploadService.deleteFile(serviceProviderId, documentTypeObj.getDocument_type_name(), existingDocument.getName(), role);
                                }
                                existingDocument.setDocumentType(null);
                                existingDocument.setName(null);
                                existingDocument.setFilePath(null);
                                existingDocument.setServiceProviderEntity(null);
                                existingDocument.setIsArchived(true);
                                entityManager.persist(existingDocument);
                                serviceProviderDocumentToSave.add(existingDocument);
                                deletedDocumentMessages.add(documentTypeObj.getDocument_type_name() + " has been deleted.");
                            }
                        }
                        continue;
                    }

                    // If the file is not empty and a document already exists, update the document
                    if (existingDocuments != null && !existingDocuments.isEmpty() && fileNameId != 13) {

                        for(ServiceProviderDocument existingDocument: existingDocuments) {

                            String filePath = existingDocument.getFilePath();

                            if (existingDocument != null) {
                                String fileName = existingDocument.getName();

                                // Additional validation before attempting to delete
                                if (filePath != null && fileName != null && !fileName.isEmpty()) {

                                    // Call the delete method with properly validated parameters
                                    log.info("File successfully Archived");
                                    // Continue with updating the document
                                    existingDocument.setIsArchived(true);
                                    existingDocument.setModifiedDate(new Date());

                                } else {
                                    log.info("Skipping file deletion - missing path or filename information");
                                }

                                entityManager.merge(existingDocument);
                            }
                        }
                    }

                    // If the file is not empty create the document
                    if (!file.isEmpty()) {
                        ServiceProviderDocument serviceProviderDocument = null;
                        serviceProviderDocument = documentStorageService.createTicketDocumentServiceProvider(file, documentTypeObj, serviceProviderEntity, serviceProviderId, role, ticket);

                        serviceProviderDocumentToSave.add(serviceProviderDocument);
                    }

                }
            }

            List<Map<String, Object>> filteredDocuments = new ArrayList<>();

            for (ServiceProviderDocument document : serviceProviderDocumentToSave) {
                if (document.getIsArchived() != null && !document.getIsArchived()) { // Exclude archived documents
                    if (document.getFilePath() != null && document.getDocumentType() != null) {
                        Map<String, Object> documentDetails = new HashMap<>();
                        documentDetails.put("documentId", document.getDocumentId());
                        documentDetails.put("name", document.getName());
                        documentDetails.put("filePath", document.getFilePath());

                        // Add qualification details if applicable
                        if (Boolean.TRUE.equals(document.getIs_qualification_document()) && document.getQualificationDetails() != null) {
                            documentDetails.put("qualification_detail_id", qualificationDetailId);
                        }

                        // Add document validity details if applicable
                        if (document.getDocumentValidity() != null) {
                            Map<String, String> validityDetails = new HashMap<>();
                            validityDetails.put("dateOfIssue", dateOfIssue);
                            validityDetails.put("validUpto", validUpto);

                            documentDetails.put("documentValidity", validityDetails);
                        }

                        String filePath;
                        filePath = documentStorageService.encrypt(document.getFilePath());

                        String fileUrl = fileService.getFileUrl(filePath, request);

                        // Generate a file URL for the document
                        documentDetails.put("fileUrl", fileUrl);

                        Map<String, Object> documentTypeResponse = new HashMap<>();
                        documentTypeResponse.put("document_type_id", document.getDocumentType().getDocument_type_id());
                        if(otherDocument!=null && !otherDocument.trim().isEmpty())
                        {
                            documentTypeResponse.put("document_type_name", otherDocument);
                        }
                        else {
                            documentTypeResponse.put("document_type_name", document.getDocumentType().getDocument_type_name());
                        }
                        documentTypeResponse.put("description", document.getDocumentType().getDescription());
                        documentTypeResponse.put("is_qualification_document", document.getDocumentType().getIs_qualification_document());
                        documentTypeResponse.put("is_issue_date_required", document.getDocumentType().getIs_issue_date_required());
                        documentTypeResponse.put("is_expiration_date_required", document.getDocumentType().getIs_expiration_date_required());
                        documentTypeResponse.put("required_document_types", document.getDocumentType().getRequired_document_types());
                        documentTypeResponse.put("max_document_size", document.getDocumentType().getMax_document_size());
                        documentTypeResponse.put("min_document_size", document.getDocumentType().getMin_document_size());
                        documentTypeResponse.put("sort_order", document.getDocumentType().getSort_order());

                        documentDetails.put("documentType", documentTypeResponse);
                        filteredDocuments.add(documentDetails);
                    }
                }
            }

            log.info("Deleted Documents logs: {}", deletedDocumentMessages);
            responseData.put("uploadedDocuments", filteredDocuments);
            return responseData;

        } catch (NoResultException noResultException) {
            exceptionHandlingService.handleException(noResultException);
            throw new NoResultException("No record found: " + noResultException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public QualificationDetails findQualificationDetailForServiceProvider(Long qualificationDetailId, ServiceProviderEntity serviceProviderEntity) throws IllegalArgumentException {
        List<QualificationDetails> qualificationDetails = serviceProviderEntity.getQualificationDetailsList();
        QualificationDetails qualificationToFind = null;
        for (QualificationDetails qualificationDetails1 : qualificationDetails) {
            if (qualificationDetails1.getQualification_detail_id().equals(qualificationDetailId)) {
                qualificationToFind = qualificationDetails1;
                break;
            }
        }
        if (qualificationToFind == null) {
            throw new IllegalArgumentException("Qualification details with id " + qualificationDetailId + " does not exists");
        }
        return qualificationToFind;
    }

    public static Date convertStringToDate(String dateStr, String s) throws ParseException {
        if (dateStr == null || dateStr.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat.setLenient(false);
        return dateFormat.parse(dateStr);
    }

    private boolean isValidDateFormat(String dateStr, SimpleDateFormat dateFormat) {
        try {
            dateFormat.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public Boolean validateDate(String dateOfIssueStr, String validUptoStr, String dateFormatInString) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatInString);
        dateFormat.setLenient(false);

        try {
            // Validate format
            if (!isValidDateFormat(dateOfIssueStr, dateFormat)) {
                throw new IllegalArgumentException("Date of Issue must be in " + dateFormatInString + " format");
            }

            Date dateOfIssue = dateFormat.parse(dateOfIssueStr);
            Date validUpto = null;
            if (validUptoStr != null) {

                if (!isValidDateFormat(validUptoStr, dateFormat)) {
                    throw new IllegalArgumentException("Valid Upto Date must be in " + dateFormatInString + " format");
                }
                validUpto = dateFormat.parse(validUptoStr);

                // Check if validUpto is before dateOfIssue
                if (validUpto.before(dateOfIssue)) {
                    throw new IllegalArgumentException("Valid Upto Date cannot be before Date of Issue");
                }
            }
            return true;
        } catch (IllegalArgumentException ex) {
            exceptionHandlingService.handleException(ex);
            throw ex; // Rethrow with meaningful context
        } catch (ParseException ex) {
            exceptionHandlingService.handleException(ex);
            throw new IllegalArgumentException("Invalid date format", ex);
        }
    }

    @Transactional
    public void updateServiceProviderEligibilityForReRanking(List<ServiceProviderEntity> subsequentReRankingSP, List<ServiceProviderEntity> firstTimeReRankingSP) throws Exception {
        try {
            List<ServiceProviderEntity> serviceProviderEntityNotAdminOverriddenList = entityManager.createQuery(Constant.GET_SERVICE_PROVIDER_CONDITION_ADMIN_OVERRIDDEN, ServiceProviderEntity.class)
                    .setParameter("adminOverridden", false)
                    .getResultList();

            // Iterate each Service Provider
            for(ServiceProviderEntity serviceProvider: serviceProviderEntityNotAdminOverriddenList) {

                log.info("service provider id is: {}", serviceProvider.getService_provider_id());
                // If service provider eligibility is null then will update depending on logic (if Professional and completed more than or equal to 10 then make it eligible else not and for individual if his ticket completion number is more than or equal to 3 then make him eligible else not.
                if(serviceProvider.getEligibleForReRanking() == null) {
                    serviceProvider = updateServiceProviderEligibility(serviceProvider);
                    if(serviceProvider.getEligibleForReRanking()) {
                        subsequentReRankingSP.add(serviceProvider);
                    } else {
                        firstTimeReRankingSP.add(serviceProvider);
                    }
                }
                else if(!serviceProvider.getEligibleForReRanking()) {
                    serviceProvider = updateServiceProviderEligibility(serviceProvider);
                    if(serviceProvider.getEligibleForReRanking()) {
                        subsequentReRankingSP.add(serviceProvider);
                    } else {
                        firstTimeReRankingSP.add(serviceProvider);
                    }
                }
                else {
                    subsequentReRankingSP.add(serviceProvider);
                }

            }

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    @Transactional
    public ServiceProviderEntity updateServiceProviderEligibility(ServiceProviderEntity serviceProvider) throws Exception {
        try {
            if(serviceProvider.getType().equals(Constant.SERVICE_PROVIDER_PROFESSIONAL)) {
                if(serviceProvider.getTicketCompleted() <= Constant.PROFESSIONAL_SERVICE_PROVIDER_NEW_LIMIT) {
                    serviceProvider.setEligibleForReRanking(false);
                } else {
                    serviceProvider.setEligibleForReRanking(true);
                }
            } else if(serviceProvider.getType().equals(Constant.SERVICE_PROVIDER_INDIVIDUAL)) {
                if(serviceProvider.getTicketCompleted() <= Constant.INDIVIDUAL_SERVICE_PROVIDER_NEW_LIMIT) {
                    serviceProvider.setEligibleForReRanking(false);
                } else {
                    serviceProvider.setEligibleForReRanking(true);
                }
            } else {
                throw new IllegalArgumentException("Service Provider w/o recognised Type found");
            }

            return entityManager.merge(serviceProvider);

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

//    public List<ServiceProviderEntity> findServiceProviderNotAdminOverridden() throws Exception {
//
//        try {
//            return entityManager.createQuery(Constant.GET_SERVICE_PROVIDER_NOT_ADMIN_OVERRIDDEN, ServiceProviderEntity.class)
//                    .setParameter("adminOverridden", false)
//                    .getResultList();
//        } catch (Exception exception) {
//            exceptionHandlingService.handleException(exception);
//            throw new Exception(exception.getMessage());
//        }
//    }

}