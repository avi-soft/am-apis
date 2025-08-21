package com.community.api.services.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.ServiceProviderReRankingEligibilityDto;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.DocumentValidity;
import com.community.api.entity.QualificationDetails;
import com.community.api.entity.SPAcknowledgement;
import com.community.api.entity.ScoringCriteria;
import com.community.api.entity.ServiceProviderAddress;
import com.community.api.entity.ServiceProviderAddressRef;
import com.community.api.entity.ServiceProviderInfra;
import com.community.api.entity.ServiceProviderLanguage;
import com.community.api.entity.ServiceProviderRank;
import com.community.api.entity.ServiceProviderReRankingEligibility;
import com.community.api.entity.ServiceProviderTestStatus;
import com.community.api.entity.Skill;
import com.community.api.entity.StateCode;
import com.community.api.services.ApiConstants;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.DistrictService;
import com.community.api.services.DocumentStorageService;
import com.community.api.services.FileService;
import com.community.api.services.PdfEditService;
import com.community.api.services.RateLimiterService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.ServiceProviderInfraService;
import com.community.api.services.ServiceProviderLanguageService;
import com.community.api.services.ServiceProviderReRankingEligibilityService;
import com.community.api.services.ServiceProviderTestService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.SkillService;
import com.community.api.services.TwilioServiceForServiceProvider;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.DocumentType;
import com.community.api.utils.ServiceProviderDocument;
import com.mchange.rmi.NotAuthorizedException;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
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
    @Autowired
    ServiceProviderReRankingEligibilityService serviceProviderReRankingEligibilityService;

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

    public static Date convertStringToDate(String dateStr, String s) throws ParseException {
        if (dateStr == null || dateStr.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat.setLenient(false);
        return dateFormat.parse(dateStr);
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
    public ResponseEntity<?> updateServiceProvider(Long userId, Map<String, Object> updates, String authHeader) {
        ServiceProviderEntity existingServiceProvider = null;
        ServiceProviderEntity originalCopy = null;
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseService.generateSuccessResponse("Authorization header is missing or invalid.", "authorizationHeader", HttpStatus.UNAUTHORIZED);
            }

            String jwtToken = authHeader.substring(7);
            List<String> deleteLogs = new ArrayList<>();
            Integer roleId;
            Long tokenUserId;
            roleId = jwtTokenUtil.extractRoleId(jwtToken);
            tokenUserId = jwtTokenUtil.extractId(jwtToken);
            String role = roleService.findRoleName(roleId);

            updates = sharedUtilityService.trimStringValues(updates);
            Map<String, String> errorMessages = new HashMap<>();

            // Find existing ServiceProviderEntity
            existingServiceProvider = entityManager.find(ServiceProviderEntity.class, userId);
            if (existingServiceProvider == null) {
                errorMessages.put("service_provider_id", "ServiceProvider with ID " + userId + " not found");
            }

            // Define the expected date format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            if (existingServiceProvider != null) {
                originalCopy = cloneServiceProvider(existingServiceProvider);
            }
            String type = null;
            if (updates.containsKey("type")) {
                String typeStr = (String) updates.get("type");

                // Validate that the type value is either "Professional" or "Individual"
                if (typeStr == null || typeStr.trim().isEmpty()) {
                    errorMessages.put("type", "Service Provider type cannot be null or empty");
                }
                if (typeStr != null) {
                    if (!typeStr.equalsIgnoreCase("PROFESSIONAL") && !typeStr.equalsIgnoreCase("INDIVIDUAL")) {
                        errorMessages.put("type", "Invalid value for 'type'. Allowed values are 'PROFESSIONAL' or 'INDIVIDUAL'.");
                    }
                }
                existingServiceProvider.setType(typeStr.toUpperCase());
                type = typeStr.toUpperCase();
                updates.remove("type");
            } else {
                type = existingServiceProvider.getType();
            }

            ServiceProviderReRankingEligibilityDto serviceProviderReRankingEligibilityDto = null;

            if (role.equalsIgnoreCase(Constant.ADMIN) || role.equalsIgnoreCase(Constant.SUPER_ADMIN)) {
                if (updates.containsKey("rankId")) {
                    Object rankIdObj = updates.get("rankId");
                    Long rankId = rankIdObj instanceof Number ? ((Number) rankIdObj).longValue() : null;

                    ServiceProviderRank serviceProviderRank = entityManager.find(ServiceProviderRank.class, rankId);

                    if (serviceProviderRank == null) {
                        errorMessages.put("rankId", "Rank with id " + rankId + " does not exist");
                    }
                    if (rankId != null) {
                        if (type.equalsIgnoreCase("PROFESSIONAL") && rankId > 4) {
                            errorMessages.put("rankId", "The service Provider is Professional so only Professional Ranking can be given i.e. from 1a to 1d");
                        } else if (type.equalsIgnoreCase("INDIVIDUAL") && rankId < 5) {
                            errorMessages.put("rankId", "The service Provider is Individual so only Individual Ranking can be given i.e. from 2a to 2d");
                        }
                    }

                    serviceProviderReRankingEligibilityDto = new ServiceProviderReRankingEligibilityDto();

                    serviceProviderReRankingEligibilityDto.setAdminOverridden(true);
                    serviceProviderReRankingEligibilityDto.setEligibleForReRanking(null);
//                    existingServiceProvider.setAdminOverridden(true);
//                    existingServiceProvider.setEligibleForReRanking(null);
                    existingServiceProvider.setAutoScoring(false);
                    existingServiceProvider.setRanking(serviceProviderRank);
//                    existingServiceProvider.setAutoScoring(false);

                    serviceProviderReRankingEligibilityService.updateServiceProviderReRankingEligibility(existingServiceProvider, serviceProviderReRankingEligibilityDto);
                }
                updates.remove("rankId");

                if (updates.containsKey("maximum_ticket_size")) {
                    Object maximumTicketSizeObj = updates.get("maximum_ticket_size");
                    Integer maximumTicketSize = maximumTicketSizeObj instanceof Number ? ((Number) maximumTicketSizeObj).intValue() : null;

                    if (maximumTicketSize != null && maximumTicketSize < 0) {
                        errorMessages.put("maximum_ticket_size", "The maximum ticket size cannot be a negative number.");
                    }

                    serviceProviderReRankingEligibilityDto = new ServiceProviderReRankingEligibilityDto();

                    serviceProviderReRankingEligibilityDto.setAdminOverridden(true);
                    serviceProviderReRankingEligibilityDto.setEligibleForReRanking(null);

                    serviceProviderReRankingEligibilityDto = new ServiceProviderReRankingEligibilityDto();

                    serviceProviderReRankingEligibilityDto.setAdminOverridden(true);
                    serviceProviderReRankingEligibilityDto.setEligibleForReRanking(null);

                    existingServiceProvider.setMaximumTicketSize(maximumTicketSize);
//                    existingServiceProvider.setAdminOverridden(true);
//                    existingServiceProvider.setEligibleForReRanking(null);
                    existingServiceProvider.setAutoScoring(false);

                    serviceProviderReRankingEligibilityService.updateServiceProviderReRankingEligibility(existingServiceProvider, serviceProviderReRankingEligibilityDto);
                }
                updates.remove("maximum_ticket_size");

                if (updates.containsKey("maximum_binding_size")) {
                    Object maximumBindingSizeObj = updates.get("maximum_binding_size");
                    Integer maximumBindingSize = maximumBindingSizeObj instanceof Number ? ((Number) maximumBindingSizeObj).intValue() : null;

                    if (maximumBindingSize != null && maximumBindingSize < 0) {
                        errorMessages.put("maximum_binding_size", "The maximum binding size cannot be a negative number.");
                    }

                    serviceProviderReRankingEligibilityDto = new ServiceProviderReRankingEligibilityDto();

                    serviceProviderReRankingEligibilityDto.setAdminOverridden(true);
                    serviceProviderReRankingEligibilityDto.setEligibleForReRanking(null);

                    existingServiceProvider.setMaximumBindingSize(maximumBindingSize);
//                    existingServiceProvider.setAdminOverridden(true);
//                    existingServiceProvider.setEligibleForReRanking(null);
                    existingServiceProvider.setAutoScoring(false);

                    serviceProviderReRankingEligibilityService.updateServiceProviderReRankingEligibility(existingServiceProvider, serviceProviderReRankingEligibilityDto);
                }
                updates.remove("maximum_binding_size");

            } else {
                if (updates.containsKey("rankId") && updates.get("rankId") != null) {
                    errorMessages.put("rankId", "Not authorized to update the rank of Service Provider. Only Admin or Super Admin can update the Rank");
                }
                if (updates.containsKey("maximum_ticket_size")) {
                    errorMessages.put("maximum_ticket_size", "Not authorized to update the maximum ticket size of Service Provider. Only Admin or Super Admin can update it.");
                }
                if (updates.containsKey("maximum_binding_value")) {
                    errorMessages.put("maximum_binding_value", "Not authorized to update the maximum binding size of Service Provider. Only Admin or Super Admin can update it.");
                }
            }
            if (updates.containsKey("partTimeOrFullTime")) {

                String partTimeOrFullTimeStr = (String) updates.get("partTimeOrFullTime");

                // Validate that the type value is either "Professional" or "Individual"
                if (partTimeOrFullTimeStr == null || partTimeOrFullTimeStr.trim().isEmpty()) {
                    errorMessages.put("partTimeOrFullTime", "Service Provider partTime or FullTime field cannot be null or empty");
                }
                if (!partTimeOrFullTimeStr.equalsIgnoreCase("PART TIME") && !partTimeOrFullTimeStr.equalsIgnoreCase("FULL TIME")) {
                    errorMessages.put("partTimeOrFullTime", "Invalid value for 'partTime or FullTime'. Allowed values are 'PART TIME' or 'FULL TIME'.");
                }
                existingServiceProvider.setPartTimeOrFullTime(partTimeOrFullTimeStr.toUpperCase());
            }

            String mobileNumber = (String) updates.get("mobileNumber");
            String secondaryMobileNumber = (String) updates.get("secondary_mobile_number");

            if (mobileNumber != null && secondaryMobileNumber != null) {
                if (mobileNumber.equalsIgnoreCase(secondaryMobileNumber)) {
                    errorMessages.put("mobileNumber", "Primary and Secondary Mobile Numbers cannot be the same");
                }
            }
            if (mobileNumber != null && secondaryMobileNumber == null && mobileNumber.equalsIgnoreCase(existingServiceProvider.getSecondary_mobile_number())) {
                errorMessages.put("mobileNumber", "Primary and Secondary Mobile Numbers cannot be the same");
            }
            if (secondaryMobileNumber != null && mobileNumber == null && secondaryMobileNumber.equalsIgnoreCase(existingServiceProvider.getMobileNumber())) {
                errorMessages.put("mobileNumber", "Primary and Secondary Mobile Numbers cannot be the same");
            }
            updates.remove("mobileNumber");
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
            if (count > 0 && count < addresskeys.size()) {
                for (String key : addresskeys) {
                    if (!updates.containsKey(key) || updates.get(key) == null || updates.get(key).toString().trim().isEmpty()) {
                        errorMessages.put(key, key + " is required to add or update address");
                    }
                }
            }

            if (updates.containsKey("district") && updates.containsKey("state") && updates.containsKey("city") && updates.containsKey("pincode") && updates.containsKey("residential_address")) {
                existingServiceProvider.setIsAcknowledged(false);
                if (validateAddressFields(updates).isEmpty()) {
                    boolean flag = false;
                    Long addId = 0L;
                    for (ServiceProviderAddress serviceProviderAddress : existingServiceProvider.getSpAddresses()) {
                        if (serviceProviderAddress.getAddress_type_id() == 2) {
                            flag = true;
                            addId = serviceProviderAddress.getAddress_id();
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
                        ServiceProviderAddress serviceProviderAddress = entityManager.find(ServiceProviderAddress.class, addId);
                        ServiceProviderAddress serviceProviderAddressDTO = new ServiceProviderAddress();
                        serviceProviderAddressDTO.setAddress_type_id(serviceProviderAddress.getAddress_type_id());
                        serviceProviderAddressDTO.setAddress_id(serviceProviderAddress.getAddress_id());
                        serviceProviderAddressDTO.setState((String) updates.get("state"));
                        serviceProviderAddressDTO.setDistrict((String) updates.get("district"));
                        serviceProviderAddressDTO.setAddress_line((String) updates.get("residential_address"));
                        serviceProviderAddressDTO.setPincode((String) updates.get("pincode"));
                        serviceProviderAddressDTO.setServiceProviderEntity(existingServiceProvider);
                        serviceProviderAddressDTO.setCity((String) updates.get("city"));
                        Map<String, String> addressErrors = updateAddress(
                                existingServiceProvider.getService_provider_id(),
                                serviceProviderAddress,
                                serviceProviderAddressDTO
                        );
                        errorMessages.putAll(addressErrors);

                    }
                } else {
                    existingServiceProvider.setIsAcknowledged(false);
                    errorMessages.putAll(validateAddressFields(updates));
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
            if (KeysCount > 0 && KeysCount < PermanentAddressKeys.size()) {
                for (String key : PermanentAddressKeys) {
                    if (!updates.containsKey(key) || updates.get(key) == null || updates.get(key).toString().trim().isEmpty()) {
                        errorMessages.put(key, key + " is required to add or update permanent address");
                    }
                }
            }
            if (updates.containsKey("permanent_district") && updates.containsKey("permanent_state") && updates.containsKey("permanent_city") && updates.containsKey("permanent_pincode") && updates.containsKey("permanent_residential_address")) {
                existingServiceProvider.setIsAcknowledged(false);
                if (validatePAddressFields(updates).isEmpty()) {
                    boolean flag = false;
                    Long addId = 0L;
                    for (ServiceProviderAddress serviceProviderAddress : existingServiceProvider.getSpAddresses()) {
                        if (serviceProviderAddress.getAddress_type_id() == 5) {
                            flag = true;
                            addId = serviceProviderAddress.getAddress_id();
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
                        ServiceProviderAddress serviceProviderAddress = entityManager.find(ServiceProviderAddress.class, addId);
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
                        Map<String, String> addressErrors = updateAddress(
                                existingServiceProvider.getService_provider_id(),
                                serviceProviderAddress,
                                serviceProviderAddressDTO
                        );

                        errorMessages.putAll(addressErrors);

                    }
                } else {
                    errorMessages.putAll(validatePAddressFields(updates));
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

//            businessKeys.add("business_location");
            businessKeys.add("business_email");
            businessKeys.add("number_of_employees");
            businessKeys.add("business_latitude");
            businessKeys.add("business_longitude");
            businessKeys.add("business_geo_location");
            businessKeys.add("isCFormAvailable");
            businessKeys.add("registration_number");
            businessKeys.add("business_district");
            businessKeys.add("business_city");
            businessKeys.add("business_pincode");
            businessKeys.add("business_state");
            businessKeys.add("business_address");

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
                        for (String key : businessKeys) {
                            if (!updates.containsKey(key) || updates.get(key) == null || updates.get(key).toString().trim().isEmpty()) {
                                errorMessages.put(key, key + " is required to add or update business profile");
                            }
                        }
                    }

                    // Null or empty check for each business field
                    for (String key : businessKeys) {
                        Object value = updates.get(key);
                        if (key.equals("registration_number"))
                            continue;
                        if (value == null || value.toString().trim().isEmpty()) {
                            errorMessages.put(key, "Field '" + key + "' cannot be null or empty when is_running_business_unit is true");
                        }
                    }
                    existingServiceProvider.setBusiness_email((String)updates.get("business_email"));
                    existingServiceProvider.setBusiness_name((String)updates.get("business_name"));
                    //Adding business_address for professional SP
                    List<String> businessAddresssKeys = new ArrayList<>();
                    businessAddresssKeys.add("business_district");
                    businessAddresssKeys.add("business_city");
                    businessAddresssKeys.add("business_pincode");
                    businessAddresssKeys.add("business_state");
                    businessAddresssKeys.add("business_address");
                    businessAddresssKeys.add("business_longitude");
                    businessAddresssKeys.add("business_latitude");
                    businessAddresssKeys.add("business_geo_location");
                    /*int businessKeysCount = 0;
                    for (String key : updates.keySet()) {
                        if (businessAddresssKeys.contains(key))
                            businessKeysCount++;
                    }
                    if (businessKeysCount > 0 && businessKeysCount < businessAddresssKeys.size())
                    {
                        for (String key : businessAddresssKeys) {
                            if (!updates.containsKey(key) || updates.get(key) == null || updates.get(key).toString().trim().isEmpty()) {
                                errorMessages.put(key, key + " is required to add or update business address");
                            }
                        }
                    }*/
                    if (updates.containsKey("business_district") && updates.containsKey("business_state") && updates.containsKey("business_city") && updates.containsKey("business_pincode") && updates.containsKey("business_address") && updates.containsKey("business_longitude") && updates.containsKey("business_latitude") && updates.containsKey("business_geo_location")) {
                        existingServiceProvider.setIsAcknowledged(false);
                        if (validateBusinessAddressFields(updates).isEmpty()) {
                            boolean flag = false;
                            Long addId = 0L;
                            for (ServiceProviderAddress serviceProviderAddress : existingServiceProvider.getSpAddresses()) {
                                if (serviceProviderAddress.getAddress_type_id() == 1) {
                                    flag = true;
                                    addId = serviceProviderAddress.getAddress_id();
                                    break;
                                }
                            }
                            if (!flag) {
                                ServiceProviderAddress serviceProviderAddress = new ServiceProviderAddress();
                                serviceProviderAddress.setAddress_type_id(findAddressName("OFFICE_ADDRESS").getAddress_type_Id());
                                serviceProviderAddress.setAddress_name("OFFICE_ADDRESS");
                                serviceProviderAddress.setPincode((String) updates.get("business_pincode"));
                                serviceProviderAddress.setDistrict((String) updates.get("business_district"));
                                serviceProviderAddress.setState((String) updates.get("business_state"));
                                serviceProviderAddress.setCity((String) updates.get("business_city"));
                                serviceProviderAddress.setAddress_line((String) updates.get("business_address"));
                                serviceProviderAddress.setLongitude((Double) updates.get("business_longitude"));
                                serviceProviderAddress.setLatitude((Double) updates.get("business_latitude"));
                                serviceProviderAddress.setGeoLocation((String) updates.get("business_geo_location"));
                                if (serviceProviderAddress.getAddress_line() != null || serviceProviderAddress.getCity() != null || serviceProviderAddress.getDistrict() != null || serviceProviderAddress.getState() != null || serviceProviderAddress.getPincode() != null || serviceProviderAddress.getLongitude() != null || serviceProviderAddress.getLatitude() != null || serviceProviderAddress.getGeoLocation() != null) {
                                    addAddress(existingServiceProvider.getService_provider_id(), serviceProviderAddress);
                                }
                            } else {
                                ServiceProviderAddress serviceProviderAddress = entityManager.find(ServiceProviderAddress.class, addId);
                                ServiceProviderAddress serviceProviderAddressDTO = new ServiceProviderAddress();
                                serviceProviderAddress.setAddress_name("OFFICE_ADDRESS");
                                serviceProviderAddressDTO.setAddress_type_id(serviceProviderAddress.getAddress_type_id());
                                serviceProviderAddressDTO.setAddress_id(serviceProviderAddress.getAddress_id());
                                serviceProviderAddressDTO.setState((String) updates.get("business_state"));
                                serviceProviderAddressDTO.setDistrict((String) updates.get("business_district"));
                                serviceProviderAddressDTO.setAddress_line((String) updates.get("business_address"));
                                serviceProviderAddressDTO.setPincode((String) updates.get("business_pincode"));

                                serviceProviderAddressDTO.setCity((String) updates.get("business_city"));
                                Object longitudeObj = updates.get("business_longitude");
                                Object latitudeObj = updates.get("business_latitude");

                                if (longitudeObj instanceof Number) {
                                    serviceProviderAddressDTO.setLongitude(((Number) longitudeObj).doubleValue());
                                }
                                if (latitudeObj instanceof Number) {
                                    serviceProviderAddressDTO.setLatitude(((Number) latitudeObj).doubleValue());
                                }
                                Object geoObj = updates.get("business_geo_location");
                                if (geoObj != null) {
                                    serviceProviderAddressDTO.setGeoLocation(geoObj.toString());
                                }

                                serviceProviderAddressDTO.setServiceProviderEntity(existingServiceProvider);
                                Map<String, String> addressErrors = updateAddress(
                                        existingServiceProvider.getService_provider_id(),
                                        serviceProviderAddress,
                                        serviceProviderAddressDTO
                                );

                                errorMessages.putAll(addressErrors);
                            }
                        } else {
                            errorMessages.putAll(validateBusinessAddressFields(updates));
                        }
                    }

                    existingServiceProvider.setIsCFormAvailable((Boolean) updates.get("isCFormAvailable"));
                    if (((Boolean) updates.get("isCFormAvailable"))) {
                        if (updates.get("registration_number") != null
                                && !((String) updates.get("registration_number")).trim().isEmpty()) {
                            existingServiceProvider.setRegistration_number((String) updates.get("registration_number"));
                        } else
                            errorMessages.put("registration_number", "Registration Number can not be empty or null");

                    } else
                        existingServiceProvider.setRegistration_number(null);

                    updates.remove("isCFormAvailable");
                    updates.remove("registration_number");

                    existingServiceProvider.setNumber_of_employees((Integer) updates.get("number_of_employees"));
                    updates.remove("number_of_employees");

                } else {
                    existingServiceProvider.setIs_running_business_unit(false);
                    existingServiceProvider.setBusiness_name(null);

                    // Collect addresses to remove
                    List<ServiceProviderAddress> toRemove = new ArrayList<>();
                    for (ServiceProviderAddress serviceProviderAddress : existingServiceProvider.getSpAddresses()) {
                        if (serviceProviderAddress.getAddress_type_id() == 1) {
                            toRemove.add(serviceProviderAddress);
                        }
                    }
                    existingServiceProvider.getSpAddresses().removeAll(toRemove);

                    existingServiceProvider.setBusiness_location(null);
                    existingServiceProvider.setBusiness_email(null);
                    existingServiceProvider.setNumber_of_employees(null);
                    existingServiceProvider.setRegistration_number(null);
                    existingServiceProvider.setLatitude(null);
                    existingServiceProvider.setLongitude(null);
                    existingServiceProvider.setBusiness_geo_location(null);
                    existingServiceProvider.setIsCFormAvailable(false);
                    List<ServiceProviderDocument> serviceProviderDocuments = existingServiceProvider.getDocuments();
                    for (ServiceProviderDocument serviceProviderDocument : serviceProviderDocuments) {
                        if (serviceProviderDocument.getDocumentType().getDocument_type_id().equals(Constant.DOCUMENT_TYPE_C_FORM)) {
                            serviceProviderDocument.setIsArchived(true);
                        }
                    }

                }
                updates.remove("number_of_employees");
                updates.remove("business_email");
                updates.remove("business_location");
                updates.remove("business_name");
                updates.remove("registration_number");
                updates.remove("isCFormAvailable");
                updates.remove("business_state");
                updates.remove("business_district");
                updates.remove("business_pincode");
                updates.remove("business_address");
                updates.remove("business_city");
                updates.remove("business_longitude");
                updates.remove("business_latitude");
                updates.remove("business_geo_location");
            }
            if (updates.containsKey("work_experience_in_months")) {
                Object workExpMonths = updates.get("work_experience_in_months");
                int months = 0;

                if (workExpMonths != null && !workExpMonths.toString().trim().isEmpty()) {
                    try {
                        months = Integer.parseInt(workExpMonths.toString().trim());
                    } catch (NumberFormatException e) {
                        errorMessages.put("work_experience_in_months", "Invalid value for work_experience_in_months");
                    }
                }

                Object workExp = updates.get("work_experience_in");

                if (months != 0) {

                    if (workExp == null || workExp.toString().trim().isEmpty()) {
                        errorMessages.put("work_experience_in", "Work Experience description is required ");
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
                    errorMessages.put("user_name", "Username is not available");
                }
                if (existingSPByEmail != null && !existingSPByEmail.getService_provider_id().equals(userId)) {
                    errorMessages.put("primary_email", "Email already in use");
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

                            if (skill.getSkill_id() == 6 && skill.getSkill_name().equalsIgnoreCase("Any Other Expertise")) {
                                if (!updates.containsKey("other_skill")) {
                                    errorMessages.put("other_skill", "You have to enter the other skill");
                                } else {
                                    String otherSkill = (String) updates.get("other_skill");
                                    if (otherSkill == null || otherSkill.trim().isEmpty()) {
                                        errorMessages.put("other_skill", "other skill text field cannot be null or empty");
                                    }
                                    assert existingServiceProvider != null;
                                    existingServiceProvider.setOtherSkill(otherSkill);

                                }
                            } else {
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
                            errorMessages.put("scoringCriteria", "Scoring Criteria is not found for Technical Expertise Score");
                        } else {
                            Integer totalTechnicalScores = totalSkills * scoringCriteriaToMap.getScore();
                            existingServiceProvider.setTechnicalExpertiseScore(totalTechnicalScores);
                            scoringCriteriaToMap = null;
                        }
                    }
                    if (totalSkills >= 5) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(9L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            errorMessages.put("scoringCriteria", "Scoring Criteria is not found for Technical Expertise Score");
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
            if (updates.containsKey("pfpNa") && (Boolean) updates.get("pfpNa")) {
                Iterator<ServiceProviderDocument> iterator = existingServiceProvider.getDocuments().iterator();
                while (iterator.hasNext()) {
                    ServiceProviderDocument document = iterator.next();
                    if (document.getDocumentType().getDocument_type_id() == 3) {
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
                            errorMessages.put("scoringCriteria", "Scoring Criteria is not found for Infra Score");
                        } else {
                            existingServiceProvider.setInfraScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    } else if (totalInfras >= 2 && totalInfras <= 4) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(14L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            errorMessages.put("scoringCriteria", "Scoring Criteria is not found for Infra Score");
                        } else {
                            existingServiceProvider.setInfraScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    } else if (totalInfras == 1) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(15L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            errorMessages.put("scoringCriteria", "Scoring Criteria is not found for Infra Score");
                        } else {
                            existingServiceProvider.setInfraScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    }
                } else if (updates.containsKey("infra_list") && (updates.get("infra_list") instanceof List) && ((List<?>) updates.get("infra_list")).isEmpty()) {
                    scoringCriteriaToMap = traverseListOfScoringCriteria(16L, scoringCriteriaList, existingServiceProvider);
                    if (scoringCriteriaToMap == null) {
                        errorMessages.put("scoringCriteria", "Scoring Criteria is not found for Infra Score");
                    } else {
                        existingServiceProvider.setInfraScore(scoringCriteriaToMap.getScore());
                        scoringCriteriaToMap = null;
                    }
                }

                if (updates.containsKey("partTimeOrFullTime")) {
                    if (existingServiceProvider.getPartTimeOrFullTime().equalsIgnoreCase("PART TIME")) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(18L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            errorMessages.put("scoringCriteria", "Scoring Criteria is not found for Part time or Full time Score");
                        } else {
                            existingServiceProvider.setPartTimeOrFullTimeScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    }
                    if (existingServiceProvider.getPartTimeOrFullTime().equalsIgnoreCase("FULL TIME")) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(17L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            errorMessages.put("scoringCriteria", "Scoring Criteria is not found for Part time or Full time Score");
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
                    errorMessages.put("date_of_birth", "DOB cannot be in future");
            }

            if (updates.containsKey("secondary_email") && "".equals(updates.get("secondary_email"))) {
                existingServiceProvider.setSecondary_email(null);
                updates.remove("secondary_email");
            }

            if (updates.containsKey("aadhaar_number")) {
                String newAadhaarNumber = (String) updates.get("aadhaar_number");

                if (newAadhaarNumber == null || newAadhaarNumber.trim().isEmpty()) {
                    errorMessages.put("aadhaar_number", "Aadhaar number cannot be empty");
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
                            errorMessages.put("aadhaar_number", "Aadhaar number already exists");
                        }
                    }
                }
            }

            if (updates.containsKey("pan_number")) {
                String newPanNumber = (String) updates.get("pan_number");

                if (newPanNumber == null || newPanNumber.trim().isEmpty()) {
                    errorMessages.put("pan_number", "PAN number cannot be empty");
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
                            errorMessages.put("pan_number", "PAN number already exists");
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
                    errorMessages.put(fieldName, fieldName + " cannot be null");
                if (newValue.toString().isEmpty() && isNullable)
                    continue;
                if (newValue != null) {
                    if (field.isAnnotationPresent(Size.class)) {
                        Size sizeAnnotation = field.getAnnotation(Size.class);
                        int min = sizeAnnotation.min();
                        int max = sizeAnnotation.max();
                        if (newValue.toString().length() > max || newValue.toString().length() < min) {
                            if (max == min)
                                errorMessages.put(fieldName, fieldName + " size should be of size " + max);
                            else
                                errorMessages.put(fieldName, fieldName + " size should be in between " + min + " " + max);
                            continue;
                        }
                    }
                    if (field.isAnnotationPresent(Email.class)) {
                        Email emailAnnotation = field.getAnnotation(Email.class);
                        String message = emailAnnotation.message();
                        if (fieldName.equals("primary_email")) {
                            if (newValue.equals((String) updates.get("secondary_email")) || (existingServiceProvider.getSecondary_email() != null && newValue.equals(existingServiceProvider.getSecondary_email())))
                                errorMessages.put("primary_email", "primary and secondary email cannot be same");
                        } else if (fieldName.equals("secondary_email")) {
                            if (newValue.equals((String) updates.get("primary_email")) || (existingServiceProvider.getPrimary_email() != null && newValue.equals(existingServiceProvider.getPrimary_email())))
                                errorMessages.put("secondary_email", "primary and secondary email cannot be same");
                        }
                        if (!sharedUtilityService.isValidEmail((String) newValue)) {
                            errorMessages.put(fieldName, message.replace("{field}", fieldName));
                            continue;
                        }
                    }
                    if (field.isAnnotationPresent(Pattern.class)) {
                        Pattern patternAnnotation = field.getAnnotation(Pattern.class);
                        String regex = patternAnnotation.regexp();
                        String message = patternAnnotation.message(); // Get custom message
                        if (!newValue.toString().matches(regex)) {
                            errorMessages.put(fieldName, fieldName + " is invalid"); // Use a placeholder
                            continue;
                        }
                    }

                    if (fieldName.equalsIgnoreCase("first_name") || fieldName.equalsIgnoreCase("last_name") || fieldName.equalsIgnoreCase("father_name") || fieldName.equalsIgnoreCase("mother_name") || fieldName.equalsIgnoreCase("mobileNumber") || fieldName.equalsIgnoreCase("primary_email")) {
                        existingServiceProvider.setIsAcknowledged(false);
                    }

                    if (fieldName.equals("date_of_birth")) {
                        String dobString = (String) newValue;
                        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                        existingServiceProvider.setIsAcknowledged(false);
                        try {
                            LocalDate dob = LocalDate.parse(dobString, formatter);
                            if (dob.isAfter(LocalDate.now())) {
                                errorMessages.put("date_of_birth", "Date of birth cannot be in the future");
                            }
                        } catch (DateTimeParseException e) {
                            errorMessages.put("date_of_birth", "Invalid date format for " + fieldName + ". Expected format is DD-MM-YYYY.");
                        }
                    }
                }
                field.setAccessible(true);
                // Optionally, check for type compatibility before setting the value
                if (newValue != null && field.getType().isAssignableFrom(newValue.getClass())) {
                    field.set(existingServiceProvider, newValue);
                }
            }

            // Merge the updated entity

            existingServiceProvider.setUpdatedDate(new Date());
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
                        errorMessages.put("scoringCriteria", "Scoring Criteria is not found for scoring Work Experience Score");
                    } else {
                        existingServiceProvider.setWorkExperienceScore(scoringCriteriaToMap.getScore());
                        scoringCriteriaToMap = null;
                    }
                } else if (existingServiceProvider.getWork_experience_in_months() != null && existingServiceProvider.getWork_experience_in_months() >= 24
                        && existingServiceProvider.getWork_experience_in_months() <= 35) {
                    scoringCriteriaToMap = traverseListOfScoringCriteria(3L, scoringCriteriaList, existingServiceProvider);
                    if (scoringCriteriaToMap == null) {
                        errorMessages.put("scoringCriteria", "Scoring Criteria is not found for scoring Work Experience Score");
                    } else {
                        existingServiceProvider.setWorkExperienceScore(scoringCriteriaToMap.getScore());
                        scoringCriteriaToMap = null;
                    }
                } else if (existingServiceProvider.getWork_experience_in_months() != null && existingServiceProvider.getWork_experience_in_months() >= 36
                        && existingServiceProvider.getWork_experience_in_months() <= 59) {
                    scoringCriteriaToMap = traverseListOfScoringCriteria(4L, scoringCriteriaList, existingServiceProvider);
                    if (scoringCriteriaToMap == null) {
                        errorMessages.put("scoringCriteria", "Scoring Criteria is not found for scoring Work Experience Score");
                    } else {
                        existingServiceProvider.setWorkExperienceScore(scoringCriteriaToMap.getScore());
                        scoringCriteriaToMap = null;
                    }
                } else if (existingServiceProvider.getWork_experience_in_months() != null && existingServiceProvider.getWork_experience_in_months() >= 60) {
                    scoringCriteriaToMap = traverseListOfScoringCriteria(5L, scoringCriteriaList, existingServiceProvider);
                    if (scoringCriteriaToMap == null) {
                        errorMessages.put("scoringCriteria", "Scoring Criteria is not found for scoring Work Experience Score");
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
                            errorMessages.put("scoringCriteria", "Scoring Criteria is not found for scoring businessScore");
                        } else {
                            existingServiceProvider.setBusinessUnitInfraScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    } else {
                        existingServiceProvider.setBusinessUnitInfraScore(0);
                    }
                }
                Integer numberOfEmployees = 0;
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
                    if (Boolean.TRUE.equals(existingServiceProvider.getIs_running_business_unit())) {
                        existingServiceProvider.setNumber_of_employees(Integer.parseInt((String) numEmpObj));
                    } else {
                        numberOfEmployees = 0;
                    }
                } else {
                    if (Boolean.TRUE.equals(existingServiceProvider.getIs_running_business_unit())) {
                        numberOfEmployees = existingServiceProvider.getNumber_of_employees();
                    } else {
                        numberOfEmployees = 0;
                    }
                }
                Boolean isRunning = existingServiceProvider.getIs_running_business_unit();
                System.out.println(isRunning);
                if (numberOfEmployees != null && numberOfEmployees < 2 || !isRunning) {
                    scoringCriteriaToMap = traverseListOfScoringCriteria(12L, scoringCriteriaList, existingServiceProvider);
                    if (scoringCriteriaToMap == null) {
                        errorMessages.put("scoringCriteria", "Scoring Criteria is not found for scoring Staff Score");
                    } else {
                        existingServiceProvider.setStaffScore(scoringCriteriaToMap.getScore());
                        scoringCriteriaToMap = null;
                    }
                } else if (numberOfEmployees != null && numberOfEmployees >= 2
                        && numberOfEmployees <= 4 && isRunning) {
                    scoringCriteriaToMap = traverseListOfScoringCriteria(11L, scoringCriteriaList, existingServiceProvider);
                    if (scoringCriteriaToMap == null) {
                        errorMessages.put("scoringCriteria", "Scoring Criteria is not found for scoring Staff Score");
                    } else {
                        existingServiceProvider.setStaffScore(scoringCriteriaToMap.getScore());
                        scoringCriteriaToMap = null;
                    }
                } else if (numberOfEmployees != null && numberOfEmployees > 4 && isRunning) {
                    scoringCriteriaToMap = traverseListOfScoringCriteria(10L, scoringCriteriaList, existingServiceProvider);
                    if (scoringCriteriaToMap == null) {
                        errorMessages.put("scoringCriteria", "Scoring Criteria is not found for scoring Staff Score");
                    } else {
                        existingServiceProvider.setStaffScore(scoringCriteriaToMap.getScore());
                        scoringCriteriaToMap = null;
                    }
                }
            } else {
                totalScore = totalScore - existingServiceProvider.getStaffScore();
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
            if (existingServiceProvider.getAutoScoring() && !existingServiceProvider.getApproved()) {
                assignRank(existingServiceProvider, totalScore);
            }
            if (updates.containsKey("isAcknowledged")) {
                Boolean value = (Boolean) updates.get("isAcknowledged");
                existingServiceProvider.setIsAcknowledged(value);
            }
            if (!errorMessages.isEmpty()) {
                restoreServiceProvider(existingServiceProvider, originalCopy);
                String message = String.join(", ", errorMessages.values());
                return ResponseService.generateSuccessResponse(message, errorMessages.keySet(), HttpStatus.BAD_REQUEST);
            }
            entityManager.merge(existingServiceProvider);

            Map<String, Object> serviceProviderMap = sharedUtilityService.serviceProviderDetailsMap(existingServiceProvider, false);

            return responseService.generateSuccessResponse("Service Provider Updated Successfully", serviceProviderMap, HttpStatus.OK);
        } catch (NoSuchFieldException e) {
            restoreServiceProvider(existingServiceProvider, originalCopy);
            exceptionHandling.handleException(e);
            return ResponseService.generateSuccessResponse("No such field present: " + e.getMessage(), "noFieldPresent", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            restoreServiceProvider(existingServiceProvider, originalCopy);
            exceptionHandling.handleException(e);
            return ResponseService.generateSuccessResponse("Error updating Service Provider", "generalException", HttpStatus.INTERNAL_SERVER_ERROR);
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
        if (existingServiceProvider.getAutoScoring().equals(true)) {
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

    public Map<String, String> validateAddressFields(Map<String, Object> updates) {
        Map<String, String> errorMessages = new HashMap<>();
        String state = (String) updates.get("state");
        String district = (String) updates.get("district");
        String pincode = (String) updates.get("pincode");
        String city = (String) updates.get("city");
        String residentialAddress = (String) updates.get("residential_address");

        String[] fieldNames = {"state", "district", "pincode", "residential_address", "city"};
        String[] fieldValues = {state, district, pincode, residentialAddress, city};

        for (int i = 0; i < fieldValues.length; i++) {
            if (fieldValues[i] == null || fieldValues[i].trim().isEmpty()) {
                errorMessages.put(fieldNames[i], fieldNames[i] + " cannot be empty");
            }
        }

        // Validate pincode format
        String pattern = Constant.PINCODE_REGEXP;
        if (pincode != null && !pincode.trim().isEmpty() && !java.util.regex.Pattern.matches(pattern, pincode)) {
            errorMessages.put("pincode", "Pincode should contain only numbers and should be of length 6");
        }

        // Validate city format
        pattern = Constant.CITY_REGEXP;
        if (city != null && !city.trim().isEmpty() && !java.util.regex.Pattern.matches(pattern, city)) {
            errorMessages.put("city", "Field city should only contain letters");
        }

        // Only parse and validate state/district if they're non-empty
        if (state != null && !state.trim().isEmpty()) {
            try {
                String stateName = districtService.findStateById(Integer.parseInt(state));
                if (stateName == null) {
                    errorMessages.put("state", "Invalid State");
                }
            } catch (NumberFormatException e) {
                errorMessages.put("state", "Invalid Current State ID format");
            }
        }

        if (district != null && !district.trim().isEmpty()) {
            try {
                String districtName = districtService.findDistrictById(Integer.parseInt(district));
                if (districtName == null) {
                    errorMessages.put("district", "Invalid District");
                }
            } catch (NumberFormatException e) {
                errorMessages.put("district", "Invalid Current District ID format");
            }
        }

        return errorMessages;
    }

    public Map<String, String> validatePAddressFields(Map<String, Object> updates) {
        Map<String, String> errorMessages = new HashMap<>();
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
                errorMessages.put(fieldNames[i], fieldNames[i] + " cannot be empty");
            }
        }

        // Validate pincode format
        String pattern = Constant.PINCODE_REGEXP;
        if (pincode != null && !pincode.trim().isEmpty() && !java.util.regex.Pattern.matches(pattern, pincode)) {
            errorMessages.put("permanent_pincode", "Pincode should contain only numbers and should be of length 6");
        }

        // Validate city format
        pattern = Constant.CITY_REGEXP;
        if (city != null && !city.trim().isEmpty() && !java.util.regex.Pattern.matches(pattern, city)) {
            errorMessages.put("permanent_city", "Field city should only contain letters");
        }

        // Validate permanent_state
        if (state != null && !state.trim().isEmpty()) {
            try {
                String stateName = districtService.findStateById(Integer.parseInt(state));
                if (stateName == null) {
                    errorMessages.put("permanent_state", "Invalid State");
                }
            } catch (NumberFormatException e) {
                errorMessages.put("permanent_state", "Invalid Permanent State ID format");
            }
        }

        // Validate permanent_district
        if (district != null && !district.trim().isEmpty()) {
            try {
                String districtName = districtService.findDistrictById(Integer.parseInt(district));
                if (districtName == null) {
                    errorMessages.put("permanent_district", "Invalid District");
                }
            } catch (NumberFormatException e) {
                errorMessages.put("permanent_district", "Invalid Permanent District ID format");
            }
        }

        return errorMessages;
    }

    public Map<String, String> validateBusinessAddressFields(Map<String, Object> updates) {
        Map<String, String> errorMessages = new HashMap<>();
        String state = (String) updates.get("business_state");
        String district = (String) updates.get("business_district");
        String pincode = (String) updates.get("business_pincode");
        String city = (String) updates.get("business_city");
        String businessAddress = (String) updates.get("business_address");

        String[] fieldNames = {
                "business_state", "business_district",
                "business_pincode", "business_address",
                "business_city"
        };
        String[] fieldValues = {
                state, district, pincode, businessAddress, city
        };

        for (int i = 0; i < fieldValues.length; i++) {
            if (fieldValues[i] == null || fieldValues[i].trim().isEmpty()) {
                errorMessages.put(fieldNames[i], fieldNames[i] + " cannot be empty");
            }
        }

        // Validate pincode format
        String pattern = Constant.PINCODE_REGEXP;
        if (pincode != null && !pincode.trim().isEmpty() && !java.util.regex.Pattern.matches(pattern, pincode)) {
            errorMessages.put("business_pincode", "Pincode should contain only numbers and should be of length 6");
        }

        // Validate city format
        pattern = Constant.CITY_REGEXP;
        if (city != null && !city.trim().isEmpty() && !java.util.regex.Pattern.matches(pattern, city)) {
            errorMessages.put("business_city", "Field city should only contain letters");
        }

        // Validate permanent_state
        if (state != null && !state.trim().isEmpty()) {
            try {
                String stateName = districtService.findStateById(Integer.parseInt(state));
                if (stateName == null) {
                    errorMessages.put("business_state", "Invalid State");
                }
            } catch (NumberFormatException e) {
                errorMessages.put("business_state", "Invalid business State ID format");
            }
        }

        // Validate permanent_district
        if (district != null && !district.trim().isEmpty()) {
            try {
                String districtName = districtService.findDistrictById(Integer.parseInt(district));
                if (districtName == null) {
                    errorMessages.put("business_district", "Invalid District");
                }
            } catch (NumberFormatException e) {
                errorMessages.put("business_district", "Invalid business District ID format");
            }
        }

        Object latObj = updates.get("business_latitude");
        Double latitude = null;

        if (latObj instanceof Double) {
            latitude = (Double) latObj;
        } else if (latObj instanceof String) {
            try {
                latitude = Double.parseDouble((String) latObj);
            } catch (NumberFormatException e) {
                errorMessages.put("business_latitude", "Latitude must be a valid number");
            }
        } else {
            errorMessages.put("business_latitude", "Latitude must be a valid number");
        }

        if (latitude != null) {
            if (latitude > 90 || latitude < -90) {
                errorMessages.put("business_latitude", "Invalid latitude: must be between -90 and 90");
            }
        }

        Object longObj = updates.get("business_longitude");
        Double longitude = null;

        if (longObj instanceof Double) {
            longitude = (Double) longObj;
        } else if (longObj instanceof String) {
            try {
                longitude = Double.parseDouble((String) longObj);
            } catch (NumberFormatException e) {
                errorMessages.put("business_longitude", "Longitude must be a valid number");
            }
        } else {
            errorMessages.put("business_longitude", "Longitude must be a valid number");
        }

        if (longitude != null) {
            if (longitude > 180 || longitude < -180) {
                errorMessages.put("business_longitude", "Invalid longitude: must be between -180 and 180");
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
        if (existingServiceProvider.getIsArchived())
            return ResponseService.generateErrorResponse("Your account is suspended ,please contact support.", HttpStatus.UNAUTHORIZED);
        return validateServiceProvider(existingServiceProvider, password, request, session);
    }

    //find service provider by username and validate the password.
    public ResponseEntity<?> authenticateByUsername(String username, String password, HttpServletRequest request, HttpSession session) throws Exception {
        ServiceProviderEntity existingServiceProvider = findServiceProviderByUserName(username);
        if (existingServiceProvider.getIsArchived())
            return ResponseService.generateErrorResponse("Your account is suspended ,please contact support.", HttpStatus.UNAUTHORIZED);
        return validateServiceProvider(existingServiceProvider, password, request, session);
    }

    //mechanism to check password
    @Transactional
    public ResponseEntity<?> validateServiceProvider(ServiceProviderEntity serviceProvider, String password, HttpServletRequest request, HttpSession session) throws Exception {
        if (serviceProvider == null) {
            return responseService.generateErrorResponse("No Records Found", HttpStatus.NOT_FOUND);
        }
        if (serviceProvider.getIsArchived())
            return ResponseService.generateErrorResponse("Your account is suspended ,please contact support.", HttpStatus.UNAUTHORIZED);
        if (passwordEncoder.matches(password, serviceProvider.getPassword())) {
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String tokenKey = "authTokenServiceProvider_" + serviceProvider.getMobileNumber();

            String existingToken = serviceProvider.getToken();

            Map<String, Object> serviceProviderResponse = sharedUtilityService.serviceProviderDetailsMap(serviceProvider, false);


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
            return responseService.generateErrorResponse("Invalid Password.", HttpStatus.BAD_REQUEST);
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
    @Autowired
    PdfEditService pdfEditService;
    @Transactional
    public ResponseEntity<?> verifyOtp(Map<String, Object> serviceProviderDetails, HttpSession session, HttpServletRequest request) throws NotAuthorizedException {
        try {
            String username = (String) serviceProviderDetails.get("username");
            String otpEntered = (String) serviceProviderDetails.get("otpEntered");
            String mobileNumber = (String) serviceProviderDetails.get("mobileNumber");
            String countryCode = (String) serviceProviderDetails.get("countryCode");
            String ackId = (String) serviceProviderDetails.get("ack");
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
                return responseService.generateErrorResponse("Invalid Data Provided ", HttpStatus.BAD_REQUEST);
            }

            if(existingServiceProvider.getIsArchived()) {
                throw new NotAuthorizedException("Your account is suspended please contact support");
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
                if (!existingServiceProvider.getPolicyAcknowledgement() && (ackId == null || ackId.isEmpty()))
                    return ResponseService.generateErrorResponse("Need acknowledgement for user", HttpStatus.BAD_REQUEST);
                else if (existingServiceProvider.getPolicyAcknowledgement() && ackId != null) {
                    return ResponseService.generateErrorResponse("User already acknowledged", HttpStatus.BAD_REQUEST);
                } else if (!existingServiceProvider.getPolicyAcknowledgement() && (ackId != null || !ackId.isEmpty())) {
                    try {
                        SPAcknowledgement userAcknowledgement= entityManager.find(SPAcknowledgement.class,ackId);
                        if(userAcknowledgement!=null)
                            return ResponseService.generateErrorResponse("Acknowledgement ID has already been registered with other user",HttpStatus.BAD_REQUEST);
                        userAcknowledgement = new SPAcknowledgement();
                        userAcknowledgement.setUserId(existingServiceProvider.getService_provider_id());
                        userAcknowledgement.setAcknowledgementVersion("v1");
                        userAcknowledgement.setAcknowledgementId(ackId);
                        userAcknowledgement.setAcknowledgedAt(new Date());
                        entityManager.merge(userAcknowledgement);
                        existingServiceProvider.setPolicyAcknowledgement(true);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        return ResponseService.generateErrorResponse("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
                System.out.println("ack set");
                existingServiceProvider.setOtp(null);
                entityManager.merge(existingServiceProvider);


                String existingToken = existingServiceProvider.getToken();


                Map<String, Object> serviceProviderResponse = sharedUtilityService.serviceProviderDetailsMap(existingServiceProvider, true);
                if (existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {


                    Map<String, Object> responseBody = createAuthResponse(existingToken, serviceProviderResponse).getBody();

                    if(ackId!=null)
                        pdfEditService.sendPdfToApi(pdfEditService.createPdfInMemory(ackId, 4, existingServiceProvider.getService_provider_id(), mobileNumber), existingServiceProvider.getService_provider_id(),request,4);
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
                    if(ackId!=null)
                        pdfEditService.sendPdfToApi(pdfEditService.createPdfInMemory(ackId, 4, existingServiceProvider.getService_provider_id(), mobileNumber), existingServiceProvider.getService_provider_id(),request,4);
                    return ResponseEntity.ok(responseBody);
                }
            } else {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.UNAUTHORIZED);

            }

        } catch (NotAuthorizedException notAuthorizedException) {
            exceptionHandling.handleException(notAuthorizedException);
            throw new NotAuthorizedException(notAuthorizedException.getMessage());
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
    public Map<String, String> updateAddress(long serviceProviderId, ServiceProviderAddress serviceProviderAddress, ServiceProviderAddress dto) throws Exception {
        Map<String, String> errorList = new HashMap<>();
        if (serviceProviderAddress == null) {
            errorList.put("residential_address", "Incomplete Details");
        }
        ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
        if (existingServiceProvider == null) {
            errorList.put("service_provider_id", "Service Provider not found");
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
            if (dto.getLongitude() != null) {
                addressToupdate.setLongitude(dto.getLongitude());
            }
            if (dto.getLatitude() != null) {
                addressToupdate.setLatitude(dto.getLatitude());
            }
            if (dto.getGeoLocation() != null) {
                addressToupdate.setGeoLocation(dto.getGeoLocation());
            }
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
            List<Integer> qualificationType,
            List<Long> rank_id,
            String type) {

        try {
            log.info("inside search");

            CustomServiceProviderTicket customServiceProviderTicket = null;
            if (ticketId != null) {
                customServiceProviderTicket = entityManager.find(CustomServiceProviderTicket.class, ticketId);
            }

            // If all filter values are null/empty, return all service providers
            if (first_name == null && last_name == null &&
                    (state == null || state.isEmpty()) &&
                    (type == null || type.isEmpty()) &&
                    (district == null || district.isEmpty()) &&
                    mobileNumber == null &&
                    userName == null &&
                    test_status_id == null &&
                    role == null && completed == null &&
                    archived == null && approved == null && rejected == null &&
                    (rank_id == null || rank_id.isEmpty()) &&
                    (qualificationType == null || qualificationType.isEmpty())) {

                Query query = entityManager.createQuery(
                        "SELECT s FROM ServiceProviderEntity s JOIN ServiceProviderAddress a ON s = a.serviceProviderEntity",
                        ServiceProviderEntity.class);
                List<ServiceProviderEntity> serviceProviderEntityList = query.getResultList();
                List<Map<String, Object>> response = new ArrayList<>();
                for (ServiceProviderEntity serviceProvider : serviceProviderEntityList) {
                    response.add(createFilteredServiceProviderMap(serviceProvider, null, null));
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

     // filter for mobile number
            if (mobileNumber != null) {
                ServiceProviderEntity serviceProviderEntity = entityManager.createQuery(PHONE_QUERY_SERVICE_PROVIDER_FILTER, ServiceProviderEntity.class)
                        .setParameter("mobileNumber", mobileNumber)
                        .setParameter("country_code", "+91")
                        .getResultStream()
                        .findFirst()
                        .orElse(null);
                if (serviceProviderEntity != null) {
                    List<Map<String, Object>> response = new ArrayList<>();
                    response.add(createFilteredServiceProviderMap(serviceProviderEntity, state, district));
                    return ResponseService.generateSuccessResponse("Service Providers", response, HttpStatus.OK);
                } else {
                    // Return empty response
                    List<Map<String, Object>> response = new ArrayList<>();
                    return ResponseService.generateSuccessResponse("No Details found for the given mobile number", response, HttpStatus.OK);
                }
            }

            if (userName != null && !userName.trim().isEmpty()) {
                List<ServiceProviderEntity> serviceProviderEntities = entityManager.createQuery(
                                "SELECT s FROM ServiceProviderEntity s WHERE LOWER(s.user_name) LIKE LOWER(:user_name)", ServiceProviderEntity.class)
                        .setParameter("user_name", "%" + userName.trim().toLowerCase() + "%")
                        .getResultList();

                List<Map<String, Object>> response = new ArrayList<>();
                for (ServiceProviderEntity serviceProviderEntity : serviceProviderEntities) {
                    response.add(createFilteredServiceProviderMap(serviceProviderEntity, state, district));
                }

                if (!response.isEmpty()) {
                    return ResponseService.generateSuccessResponse("Service Providers", response, HttpStatus.OK);
                } else {
                    return ResponseService.generateSuccessResponse("No Details found for the given UserName", response, HttpStatus.OK);
                }
            }

            // Build dynamic query based on address filtering logic
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT DISTINCT s FROM ServiceProviderEntity s ");

            // Join with address table based on filtering requirements
            if (state != null || district != null) {
                queryBuilder.append("JOIN s.spAddresses a ");
            }

            if (qualificationType != null && !qualificationType.isEmpty()) {
                queryBuilder.append("LEFT JOIN s.qualificationDetailsList qd ");
            }

            queryBuilder.append("WHERE 1=1 ");

            // Add state and district filtering with address type logic
            if (state != null && !state.isEmpty()) {
                queryBuilder.append("AND (");
                queryBuilder.append("(s.type = 'PROFESSIONAL' AND s.is_running_business_unit = true AND a.address_type_id = 1 AND a.state IN :states) ");
                queryBuilder.append("OR (s.type = 'PROFESSIONAL' AND s.is_running_business_unit = true AND NOT EXISTS (SELECT 1 FROM s.spAddresses ba WHERE ba.address_type_id = 1) AND a.address_type_id = 2 AND a.state IN :states) ");
                queryBuilder.append("OR (s.type = 'PROFESSIONAL' AND (s.is_running_business_unit = false OR s.is_running_business_unit IS NULL) AND a.address_type_id = 2 AND a.state IN :states) ");
                queryBuilder.append("OR (s.type = 'INDIVIDUAL' AND a.address_type_id = 2 AND a.state IN :states)");
                queryBuilder.append(") ");
            }

            if (district != null && !district.isEmpty()) {
                queryBuilder.append("AND (");
                queryBuilder.append("(s.type = 'PROFESSIONAL' AND s.is_running_business_unit = true AND a.address_type_id = 1 AND a.district IN :districts) ");
                queryBuilder.append("OR (s.type = 'PROFESSIONAL' AND s.is_running_business_unit = true AND NOT EXISTS (SELECT 1 FROM s.spAddresses ba WHERE ba.address_type_id = 1) AND a.address_type_id = 2 AND a.district IN :districts) ");
                queryBuilder.append("OR (s.type = 'PROFESSIONAL' AND (s.is_running_business_unit = false OR s.is_running_business_unit IS NULL) AND a.address_type_id = 2 AND a.district IN :districts) ");
                queryBuilder.append("OR (s.type = 'INDIVIDUAL' AND a.address_type_id = 2 AND a.district IN :districts)");
                queryBuilder.append(") ");
            }

            // Add other filters
            if (first_name != null) {
                queryBuilder.append("AND LOWER(s.first_name) LIKE LOWER(:first_name) ");
            }
            if (last_name != null) {
                queryBuilder.append("AND LOWER(s.last_name) LIKE LOWER(:last_name) ");
            }
            if (role != null) {
                queryBuilder.append("AND s.role = :role ");
            }
            if (test_status_id != null) {
                    ServiceProviderTestStatus serviceProviderTestStatus= entityManager.find(ServiceProviderTestStatus.class,test_status_id);
                    if (serviceProviderTestStatus==null) {
                        throw new IllegalArgumentException("No Test Status is found with this id");
                    }
                queryBuilder.append(" AND s.serviceProviderStatus.test_status_id = :testStatusId ");
            }
            if (completed != null) {
                queryBuilder.append("AND s.completed = :completed ");
            }
            if (archived != null) {
                queryBuilder.append("AND s.isArchived = :archived ");
            }
            if (approved != null) {
                queryBuilder.append("AND s.approved = :approved ");
            }
            if (rejected != null) {
                queryBuilder.append("AND s.rejected = :rejected ");
            }
            if (type != null) {
                queryBuilder.append("AND s.type = :type ");
            }
            if (rank_id != null && !rank_id.isEmpty()) {
                queryBuilder.append("AND s.ranking.rank_id IN :rankIds ");
            }
            if (qualificationType != null && !qualificationType.isEmpty()) {
                queryBuilder.append("AND qd.qualification_id IN :qualificationType ");
            }

            // Execute query
            Query finalQuery = entityManager.createQuery(queryBuilder.toString(), ServiceProviderEntity.class);

            // Set parameters
            if (state != null && !state.isEmpty()) {
                finalQuery.setParameter("states", state);
            }
            if (district != null && !district.isEmpty()) {
                finalQuery.setParameter("districts", district);
            }
            if (first_name != null) {
                finalQuery.setParameter("first_name", first_name + "%");
            }
            if (last_name != null) {
                finalQuery.setParameter("last_name", last_name + "%");
            }
            if (role != null) {
                finalQuery.setParameter("role", role);
            } if (test_status_id != null) {
                finalQuery.setParameter("testStatusId", test_status_id);
            }
            if (completed != null) {
                finalQuery.setParameter("completed", completed);
            }
            if (archived != null) {
                finalQuery.setParameter("archived", archived);
            }
            if (approved != null) {
                finalQuery.setParameter("approved", approved);
            }
            if (rejected != null) {
                finalQuery.setParameter("rejected", rejected);
            }
            if (type != null) {
                finalQuery.setParameter("type", type);
            }

            if (rank_id != null && !rank_id.isEmpty()) {
                finalQuery.setParameter("rankIds", rank_id);
            }

            if (qualificationType != null && !qualificationType.isEmpty()) {
                finalQuery.setParameter("qualificationType", qualificationType);
            }

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
                response.add(createFilteredServiceProviderMap(sp, state, district));
            }

            log.info("end search");
            return ResponseService.generateSuccessResponse("Service Providers", response, HttpStatus.OK);

        } catch (PersistenceException persistenceException) {
            exceptionHandlingService.handleException(persistenceException);
            return ResponseService.generateErrorResponse("Error finding SP : " + persistenceException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse("Error finding SP : " + illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse("Error finding SP : " + exception.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    private Map<String, Object> createFilteredServiceProviderMap(ServiceProviderEntity serviceProvider, List<String> stateFilter, List<String> districtFilter) {
        Map<String, Object> serviceProviderDetails = new HashMap<>();

        // Basic SP details
        serviceProviderDetails.put("service_provider_id", serviceProvider.getService_provider_id());
        serviceProviderDetails.put("type", serviceProvider.getType());
        serviceProviderDetails.put("user_name", serviceProvider.getUser_name());
        serviceProviderDetails.put("first_name", serviceProvider.getFirst_name());
        serviceProviderDetails.put("last_name", serviceProvider.getLast_name());
        serviceProviderDetails.put("full_name", serviceProvider.getFirst_name() + " " + serviceProvider.getLast_name());
        serviceProviderDetails.put("country_code", serviceProvider.getCountry_code());
        serviceProviderDetails.put("mobileNumber", serviceProvider.getMobileNumber());
        serviceProviderDetails.put("primary_email", serviceProvider.getPrimary_email());
        serviceProviderDetails.put("role", serviceProvider.getRole());
        serviceProviderDetails.put("approved", serviceProvider.getApproved());
        serviceProviderDetails.put("completed", serviceProvider.getCompleted());
        serviceProviderDetails.put("is_active", serviceProvider.getIsActive());
        serviceProviderDetails.put("suspended", serviceProvider.getIsArchived());
        serviceProviderDetails.put("is_running_business_unit", serviceProvider.getIs_running_business_unit());
        serviceProviderDetails.put("business_name", serviceProvider.getBusiness_name());
        serviceProviderDetails.put("total_score", serviceProvider.getTotalScore());
        serviceProviderDetails.put("rank", serviceProvider.getRanking());
        serviceProviderDetails.put("service_provider_status", serviceProvider.getServiceProviderStatus());
        serviceProviderDetails.put("skills", serviceProvider.getSkills());
        serviceProviderDetails.put("created_date", serviceProvider.getDateJoined());
        serviceProviderDetails.put("updated_date", serviceProvider.getUpdatedDate());

        if (serviceProvider.getType() != null && serviceProvider.getType().equalsIgnoreCase("INDIVIDUAL")) {
            serviceProviderDetails.put("part_time_or_full_time", serviceProvider.getPartTimeOrFullTime());
        }

        Map<String, Object> addressToShow = determineAddressToShow(serviceProvider, stateFilter, districtFilter);
        if (addressToShow != null) {
            serviceProviderDetails.put("address", addressToShow);
        }

        return serviceProviderDetails;
    }

    private Map<String, Object> determineAddressToShow(ServiceProviderEntity serviceProvider, List<String> stateFilter, List<String> districtFilter) {
        if (serviceProvider.getSpAddresses() == null || serviceProvider.getSpAddresses().isEmpty()) {
            return null;
        }

        ServiceProviderAddress targetAddress = null;

        // Business logic for address selection
        if ("PROFESSIONAL".equalsIgnoreCase(serviceProvider.getType())) {
            if (Boolean.TRUE.equals(serviceProvider.getIs_running_business_unit())) {
                // For professional with business unit, prioritize OFFICE_ADDRESS
                targetAddress = serviceProvider.getSpAddresses().stream()
                        .filter(addr -> addr.getAddress_type_id() == 1) // OFFICE_ADDRESS
                        .findFirst()
                        .orElse(null);

                // If no office address found, use current address (NEW CONDITION)
                if (targetAddress == null) {
                    targetAddress = serviceProvider.getSpAddresses().stream()
                            .filter(addr -> addr.getAddress_type_id() == 2) // CURRENT_ADDRESS
                            .findFirst()
                            .orElse(null);
                }
            } else {
                // For professional without business unit, use CURRENT_ADDRESS
                targetAddress = serviceProvider.getSpAddresses().stream()
                        .filter(addr -> addr.getAddress_type_id() == 2) // CURRENT_ADDRESS
                        .findFirst()
                        .orElse(null);
            }
        } else if ("INDIVIDUAL".equalsIgnoreCase(serviceProvider.getType())) {
            // For individual, use CURRENT_ADDRESS
            targetAddress = serviceProvider.getSpAddresses().stream()
                    .filter(addr -> addr.getAddress_type_id() == 2) // CURRENT_ADDRESS
                    .findFirst()
                    .orElse(null);
        }

        // If no target address found, use any available address
        if (targetAddress == null && !serviceProvider.getSpAddresses().isEmpty()) {
            targetAddress = serviceProvider.getSpAddresses().get(0);
        }

        // Convert address to map
        if (targetAddress != null) {
            Map<String, Object> addressMap = new HashMap<>();
            addressMap.put("address_id", targetAddress.getAddress_id());
            addressMap.put("address_type_id", targetAddress.getAddress_type_id());
            addressMap.put("address_name", targetAddress.getAddress_name());
            addressMap.put("district", targetAddress.getDistrict());
            addressMap.put("address_line", targetAddress.getAddress_line());
            addressMap.put("state", targetAddress.getState());
            addressMap.put("city", targetAddress.getCity());
            addressMap.put("pincode", targetAddress.getPincode());
            addressMap.put("latitude", targetAddress.getLatitude());
            addressMap.put("longitude", targetAddress.getLongitude());
            addressMap.put("geoLocation", targetAddress.getGeoLocation());
            return addressMap;
        }

        return null;
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

    @Transactional
    public void serviceProviderTicketAssignedIncrement(ServiceProviderEntity serviceProvider) throws Exception {
        try {
            if (serviceProvider == null) {
                throw new IllegalArgumentException("Service Provider Not Found.");
            }

            serviceProvider.setTicketAssigned(serviceProvider.getTicketAssigned() + 1);
            entityManager.merge(serviceProvider);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
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

    public boolean areAddressesSame(ServiceProviderAddress a, ServiceProviderAddress b) {
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
        MultipartFile processedFile = null;

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

            if (documentTypeObj.getDocument_type_id().equals(Constant.DOCUMENT_TYPE_OTHER_ID)) {
                if (otherDocument == null) {
                    throw new IllegalArgumentException("other Document name cannot be null for uploading other Documents");
                }
                if (otherDocument.trim().isEmpty()) {
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
                } else {
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
                if (documentTypeObj.getDocument_type_id().equals(Constant.DOCUMENT_TYPE_LIVE_PHOTOGRAPH_ID)) {
                    fileUploadService.uploadFileOnFileServer(processedFile, documentTypeObj.getDocument_type_name(), customerId.toString(), role);
                } else {
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
                        if (documentTypeObj.getDocument_type_id().equals(Constant.DOCUMENT_TYPE_OTHER_ID)) {
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
                        } else {
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
                        if (documentTypeObj.getDocument_type_id().equals(3)) {
                            serviceProviderDocument = documentStorageService.createDocumentServiceProvider(processedFile, documentTypeObj, serviceProviderEntity, customerId, role);
                        } else {
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
                    documentDetails.put("created_date", document.getUploadedDate());

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
                    if (otherDocument != null && !otherDocument.trim().isEmpty()) {
                        documentTypeResponse.put("document_type_name", otherDocument);
                    } else {
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

            MultipartFile processedFile = null;

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
                        for (ServiceProviderDocument existingDocument : existingDocuments) {
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

                        for (ServiceProviderDocument existingDocument : existingDocuments) {

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
                        if (otherDocument != null && !otherDocument.trim().isEmpty()) {
                            documentTypeResponse.put("document_type_name", otherDocument);
                        } else {
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
            for (ServiceProviderEntity serviceProvider : serviceProviderEntityNotAdminOverriddenList) {

                log.info("service provider id is: {}", serviceProvider.getService_provider_id());
                // If service provider eligibility is null then will update depending on logic (if Professional and completed more than or equal to 10 then make it eligible else not and for individual if his ticket completion number is more than or equal to 3 then make him eligible else not.

                ServiceProviderReRankingEligibility serviceProviderReRankingEligibility = serviceProviderReRankingEligibilityService.getServiceProvideReRankingEligibilityByServiceProviderId(serviceProvider.getService_provider_id());
                if (serviceProviderReRankingEligibility == null) {
                    serviceProviderReRankingEligibility = updateServiceProviderEligibility(serviceProvider);
                    if (serviceProviderReRankingEligibility.getEligibleForReRanking()) {
                        subsequentReRankingSP.add(serviceProvider);
                    } else {
                        firstTimeReRankingSP.add(serviceProvider);
                    }
                } else if (!serviceProviderReRankingEligibility.getEligibleForReRanking()) {
                    serviceProviderReRankingEligibility = updateServiceProviderEligibility(serviceProvider);
                    if (serviceProviderReRankingEligibility.getEligibleForReRanking()) {
                        subsequentReRankingSP.add(serviceProvider);
                    } else {
                        firstTimeReRankingSP.add(serviceProvider);
                    }
                } else {
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
    public ServiceProviderReRankingEligibility updateServiceProviderEligibility(ServiceProviderEntity serviceProvider) throws Exception {
        try {

            if (serviceProvider == null) {
                throw new IllegalArgumentException("Service Provider Not Found.");
            }

            ServiceProviderReRankingEligibilityDto serviceProviderReRankingEligibilityDto = new ServiceProviderReRankingEligibilityDto();
            if (serviceProvider.getType().equals(Constant.SERVICE_PROVIDER_PROFESSIONAL)) {
                if (serviceProvider.getTicketCompleted() <= Constant.PROFESSIONAL_SERVICE_PROVIDER_NEW_LIMIT) {
                    serviceProviderReRankingEligibilityDto.setEligibleForReRanking(false);
//                    serviceProvider.setEligibleForReRanking(false);
                } else {
                    serviceProviderReRankingEligibilityDto.setEligibleForReRanking(true);
//                    serviceProvider.setEligibleForReRanking(true);
                }
            } else if (serviceProvider.getType().equals(Constant.SERVICE_PROVIDER_INDIVIDUAL)) {
                if (serviceProvider.getTicketCompleted() <= Constant.INDIVIDUAL_SERVICE_PROVIDER_NEW_LIMIT) {
                    serviceProviderReRankingEligibilityDto.setEligibleForReRanking(false);
//                    serviceProvider.setEligibleForReRanking(false);
                } else {
                    serviceProviderReRankingEligibilityDto.setEligibleForReRanking(true);
//                    serviceProvider.setEligibleForReRanking(true);
                }
            } else {
                throw new IllegalArgumentException("Service Provider w/o recognised Type found");
            }

            return serviceProviderReRankingEligibilityService.updateServiceProviderReRankingEligibility(serviceProvider, serviceProviderReRankingEligibilityDto);

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

    private ServiceProviderEntity cloneServiceProvider(ServiceProviderEntity original) {
        ServiceProviderEntity clone = new ServiceProviderEntity();

        clone.setService_provider_id(original.getService_provider_id());
        clone.setType(original.getType());
        clone.setTotalScore(original.getTotalScore());
        clone.setUser_name(original.getUser_name());
        clone.setPfpNa(original.getPfpNa());
        clone.setFirst_name(original.getFirst_name());
        clone.setLast_name(original.getLast_name());
        clone.setCountry_code(original.getCountry_code());
        clone.setFather_name(original.getFather_name());
        clone.setMother_name(original.getMother_name());
        clone.setDate_of_birth(original.getDate_of_birth());
        clone.setAadhaar_number(original.getAadhaar_number());
        clone.setPan_number(original.getPan_number());
        clone.setCompleted(original.getCompleted());
        clone.setApproved(original.getApproved());
        clone.setIsArchived(original.getIsArchived());
        clone.setMobileNumber(original.getMobileNumber());
        clone.setOtp(original.getOtp());
        clone.setSecondary_mobile_number(original.getSecondary_mobile_number());
        clone.setRole(original.getRole());
        clone.setWhatsapp_number(original.getWhatsapp_number());
        clone.setPrimary_email(original.getPrimary_email());
        clone.setSecondary_email(original.getSecondary_email());
        clone.setPassword(original.getPassword());
        clone.setIs_running_business_unit(original.getIs_running_business_unit());
        clone.setIsPasswordCreated(original.getIsPasswordCreated());
        clone.setBusiness_name(original.getBusiness_name());
        clone.setBusiness_location(original.getBusiness_location());
        clone.setBusiness_email(original.getBusiness_email());
        clone.setNumber_of_employees(original.getNumber_of_employees());
        clone.setIsCFormAvailable(original.getIsCFormAvailable());
        clone.setRegistration_number(original.getRegistration_number());
        clone.setPartTimeOrFullTime(original.getPartTimeOrFullTime());
        clone.setBusinessUnitInfraScore(original.getBusinessUnitInfraScore());
        clone.setWorkExperienceScore(original.getWorkExperienceScore());
        clone.setQualificationScore(original.getQualificationScore());
        clone.setTechnicalExpertiseScore(original.getTechnicalExpertiseScore());
        clone.setStaffScore(original.getStaffScore());
        clone.setWrittenTestScore(original.getWrittenTestScore());
        clone.setImageUploadScore(original.getImageUploadScore());
        clone.setPartTimeOrFullTimeScore(original.getPartTimeOrFullTimeScore());
        clone.setInfraScore(original.getInfraScore());
        clone.setOtherSkill(original.getOtherSkill());
        clone.setSkills(original.getSkills() != null ? new ArrayList<>(original.getSkills()) : null);
        clone.setHas_technical_knowledge(original.getHas_technical_knowledge());
        clone.setWork_experience_in(original.getWork_experience_in());
        clone.setWork_experience_in_months(original.getWork_experience_in_months());
        clone.setRejected(original.getRejected());
        clone.setHighest_qualification(original.getHighest_qualification());
        clone.setName_of_institute(original.getName_of_institute());
        clone.setYear_of_passing(original.getYear_of_passing());
        clone.setBoard_or_university(original.getBoard_or_university());
        clone.setTotal_marks(original.getTotal_marks());
        clone.setMarks_obtained(original.getMarks_obtained());
        clone.setCgpa(original.getCgpa());
        clone.setLatitude(original.getLatitude());
        clone.setLongitude(original.getLongitude());
        clone.setRank(original.getRank());
        clone.setSignedUp(original.getSignedUp());
        clone.setBusiness_geo_location(original.getBusiness_geo_location());
        clone.setIsSameAsCurrentAddress(original.getIsSameAsCurrentAddress());
        clone.setStatus(original.getStatus());
        clone.setServiceProviderStatus(original.getServiceProviderStatus());
        clone.setLastStatusId(original.getLastStatusId());
        clone.setRanking(original.getRanking());
        clone.setPrivileges(original.getPrivileges() != null ? new ArrayList<>(original.getPrivileges()) : null);
        clone.setInfra(original.getInfra() != null ? new ArrayList<>(original.getInfra()) : null);
        clone.setLanguages(original.getLanguages() != null ? new ArrayList<>(original.getLanguages()) : null);
        clone.setToken(original.getToken());
        clone.setTotalSkillTestPoints(original.getTotalSkillTestPoints());
        clone.setIsActive(original.getIsActive());
        clone.setMaximumTicketSize(original.getMaximumTicketSize());
        clone.setMaximumBindingSize(original.getMaximumBindingSize());
        clone.setTicketCompleted(original.getTicketCompleted());
        clone.setTicketPending(original.getTicketPending());
        clone.setTicketAssigned(original.getTicketAssigned());
//        clone.setAutoScoring(original.getAutoScoring());
//        clone.setAdminOverridden(original.getAdminOverridden());
//        clone.setEligibleForReRanking(original.getEligibleForReRanking());
//        clone.setReviewTicketStatusScore(original.getReviewTicketStatusScore());
//        clone.setReviewTicketFeedbackScore(original.getReviewTicketFeedbackScore());
//        clone.setTimeCompletionScore(original.getTimeCompletionScore());
        clone.setIsAcknowledged(original.getIsAcknowledged());

        return clone;
    }

    private void restoreServiceProvider(ServiceProviderEntity target, ServiceProviderEntity source) {
        target.setService_provider_id(source.getService_provider_id());
        target.setType(source.getType());
        target.setTotalScore(source.getTotalScore());
        target.setUser_name(source.getUser_name());
        target.setPfpNa(source.getPfpNa());
        target.setFirst_name(source.getFirst_name());
        target.setLast_name(source.getLast_name());
        target.setCountry_code(source.getCountry_code());
        target.setFather_name(source.getFather_name());
        target.setMother_name(source.getMother_name());
        target.setDate_of_birth(source.getDate_of_birth());
        target.setAadhaar_number(source.getAadhaar_number());
        target.setPan_number(source.getPan_number());
        target.setCompleted(source.getCompleted());
        target.setApproved(source.getApproved());
        target.setIsArchived(source.getIsArchived());
        target.setMobileNumber(source.getMobileNumber());
        target.setOtp(source.getOtp());
        target.setSecondary_mobile_number(source.getSecondary_mobile_number());
        target.setRole(source.getRole());
        target.setWhatsapp_number(source.getWhatsapp_number());
        target.setPrimary_email(source.getPrimary_email());
        target.setSecondary_email(source.getSecondary_email());
        target.setPassword(source.getPassword());
        target.setIs_running_business_unit(source.getIs_running_business_unit());
        target.setIsPasswordCreated(source.getIsPasswordCreated());
        target.setBusiness_name(source.getBusiness_name());
        target.setBusiness_location(source.getBusiness_location());
        target.setBusiness_email(source.getBusiness_email());
        target.setNumber_of_employees(source.getNumber_of_employees());
        target.setIsCFormAvailable(source.getIsCFormAvailable());
        target.setRegistration_number(source.getRegistration_number());
        target.setPartTimeOrFullTime(source.getPartTimeOrFullTime());
        target.setBusinessUnitInfraScore(source.getBusinessUnitInfraScore());
        target.setWorkExperienceScore(source.getWorkExperienceScore());
        target.setQualificationScore(source.getQualificationScore());
        target.setTechnicalExpertiseScore(source.getTechnicalExpertiseScore());
        target.setStaffScore(source.getStaffScore());
        target.setWrittenTestScore(source.getWrittenTestScore());
        target.setImageUploadScore(source.getImageUploadScore());
        target.setPartTimeOrFullTimeScore(source.getPartTimeOrFullTimeScore());
        target.setInfraScore(source.getInfraScore());
        target.setOtherSkill(source.getOtherSkill());
        target.setSkills(source.getSkills() != null ? new ArrayList<>(source.getSkills()) : null);
        target.setHas_technical_knowledge(source.getHas_technical_knowledge());
        target.setWork_experience_in(source.getWork_experience_in());
        target.setWork_experience_in_months(source.getWork_experience_in_months());
        target.setRejected(source.getRejected());
        target.setHighest_qualification(source.getHighest_qualification());
        target.setName_of_institute(source.getName_of_institute());
        target.setYear_of_passing(source.getYear_of_passing());
        target.setBoard_or_university(source.getBoard_or_university());
        target.setTotal_marks(source.getTotal_marks());
        target.setMarks_obtained(source.getMarks_obtained());
        target.setCgpa(source.getCgpa());
        target.setLatitude(source.getLatitude());
        target.setLongitude(source.getLongitude());
        target.setRank(source.getRank());
        target.setSignedUp(source.getSignedUp());
        target.setBusiness_geo_location(source.getBusiness_geo_location());
        target.setIsSameAsCurrentAddress(source.getIsSameAsCurrentAddress());
        target.setStatus(source.getStatus());
        target.setServiceProviderStatus(source.getServiceProviderStatus());
        target.setLastStatusId(source.getLastStatusId());
        target.setRanking(source.getRanking());
        target.setPrivileges(source.getPrivileges() != null ? new ArrayList<>(source.getPrivileges()) : null);
        target.setInfra(source.getInfra() != null ? new ArrayList<>(source.getInfra()) : null);
        target.setLanguages(source.getLanguages() != null ? new ArrayList<>(source.getLanguages()) : null);
        target.setToken(source.getToken());
        target.setTotalSkillTestPoints(source.getTotalSkillTestPoints());
        target.setIsActive(source.getIsActive());
        target.setMaximumTicketSize(source.getMaximumTicketSize());
        target.setMaximumBindingSize(source.getMaximumBindingSize());
        target.setTicketCompleted(source.getTicketCompleted());
        target.setTicketPending(source.getTicketPending());
        target.setTicketAssigned(source.getTicketAssigned());
//        target.setAutoScoring(source.getAutoScoring());
//        target.setAdminOverridden(source.getAdminOverridden());
//        target.setEligibleForReRanking(source.getEligibleForReRanking());
//        target.setReviewTicketStatusScore(source.getReviewTicketStatusScore());
//        target.setReviewTicketFeedbackScore(source.getReviewTicketFeedbackScore());
//        target.setTimeCompletionScore(source.getTimeCompletionScore());
        target.setIsAcknowledged(source.getIsAcknowledged());
    }


}