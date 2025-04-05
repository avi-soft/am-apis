package com.community.api.services.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.entity.CustomServiceProviderTicket;
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
import com.community.api.services.RateLimiterService;
import com.community.api.services.ResponseService;
import com.community.api.services.ServiceProviderInfraService;
import com.community.api.services.ServiceProviderLanguageService;
import com.community.api.services.ServiceProviderTestService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.SkillService;
import com.community.api.services.TwilioServiceForServiceProvider;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import io.github.bucket4j.Bucket;
import io.micrometer.core.lang.Nullable;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.validation.constraints.Pattern;

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
    private ServiceProviderInfraService serviceProviderInfraService;
    @Autowired
    private ServiceProviderLanguageService serviceProviderLanguageService;
    @Autowired
    private RateLimiterService rateLimiterService;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    @Autowired
    private ServiceProviderTestService serviceProviderTestService;

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
    public ResponseEntity<?> updateServiceProvider(Long userId, Map<String, Object> updates) {
        try {
            updates = sharedUtilityService.trimStringValues(updates);
            List<String> errorMessages = new ArrayList<>();


            // Find existing ServiceProviderEntity
            ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, userId);
            if (existingServiceProvider == null) {
                errorMessages.add("ServiceProvider with ID " + userId + " not found");
            }

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
                updates.remove("type");
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
                    return responseService.generateErrorResponse("Email not available", HttpStatus.BAD_REQUEST);
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
            if (updates.containsKey("pan_number") && ((String) updates.get("pan_number")).isEmpty())
                errorMessages.add("pan number cannot be empty");
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
                    if(updates.containsKey("latitude")) {
                        Double latitude=(Double) (updates.get("latitude"));
                        if(latitude.intValue()>90||latitude.intValue()<-90)
                            errorMessages.add("Invalid latitude");
                        else
                            existingServiceProvider.setLatitude(latitude);
                    }
                    if(updates.containsKey("longitude")) {
                        Double longitude=(Double) (updates.get("longitude"));
                        if(longitude.intValue()>180||longitude.intValue()<-180)
                            errorMessages.add("Invalid longitude");
                        else
                            existingServiceProvider.setLongitude((Double) (updates.get("longitude")));
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

                if (updates.containsKey("number_of_employees")) {
                    if (existingServiceProvider.getNumber_of_employees() != null && existingServiceProvider.getNumber_of_employees() < 2 || updates.get("is_running_business_unit").equals(false)) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(12L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            return ResponseService.generateErrorResponse("Scoring Criteria is not found for scoring Staff Score", HttpStatus.BAD_REQUEST);
                        } else {
                            existingServiceProvider.setStaffScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    } else if (existingServiceProvider.getNumber_of_employees() != null && existingServiceProvider.getNumber_of_employees() >= 2
                            && existingServiceProvider.getNumber_of_employees() <= 4 && updates.get("is_running_business_unit").equals(true)) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(11L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            return ResponseService.generateErrorResponse("Scoring Criteria is not found for scoring Staff Score", HttpStatus.BAD_REQUEST);
                        } else {
                            existingServiceProvider.setStaffScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    } else if (existingServiceProvider.getNumber_of_employees() != null && existingServiceProvider.getNumber_of_employees() > 4 && updates.get("is_running_business_unit").equals(true)) {
                        scoringCriteriaToMap = traverseListOfScoringCriteria(10L, scoringCriteriaList, existingServiceProvider);
                        if (scoringCriteriaToMap == null) {
                            return ResponseService.generateErrorResponse("Scoring Criteria is not found for scoring Staff Score", HttpStatus.BAD_REQUEST);
                        } else {
                            existingServiceProvider.setStaffScore(scoringCriteriaToMap.getScore());
                            scoringCriteriaToMap = null;
                        }
                    }
                }
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
        String pattern = Constant.PINCODE_REGEXP;
        if (!java.util.regex.Pattern.matches(pattern, pincode))
            errorMessages.add("Pincode should contain only numbers and should be of length 6");
        pattern = Constant.CITY_REGEXP;
        if (!java.util.regex.Pattern.matches(pattern, city))
            errorMessages.add("Field city should only contain letters");
        String stateName = districtService.findStateById(Integer.parseInt(state));
        if (stateName == null)
            errorMessages.add("Invalid State");
        String districtName = districtService.findDistrictById(Integer.parseInt(district));
        if (districtName == null)
            errorMessages.add("Invalid district");
        return errorMessages;
    }
    public List<String> validatePAddressFields(Map<String, Object> updates) {
        List<String> errorMessages = new ArrayList<>();
        String state = (String) updates.get("permanent_state");
        String district = (String) updates.get("permanent_district");
        String pincode = (String) updates.get("permanent_pincode");
        String city = (String) updates.get("permanent_city");
        String residentialAddress = (String) updates.get("permanent_residential_address");
        String[] fieldNames = {"permanent_state", "permanent_district", "permanent_pincode", "permanent_residential_address", "permanent_city"};
        String[] fieldValues = {state, district, pincode, residentialAddress, city};
        for (int i = 0; i < fieldValues.length; i++) {
            if (fieldValues[i] == null || fieldValues[i].trim().isEmpty()) {
                errorMessages.add(fieldNames[i] + " cannot be empty");
            }
        }
        String pattern = Constant.PINCODE_REGEXP;
        if (!java.util.regex.Pattern.matches(pattern, pincode))
            errorMessages.add("Pincode should contain only numbers and should be of length 6");
        pattern = Constant.CITY_REGEXP;
        if (!java.util.regex.Pattern.matches(pattern, city))
            errorMessages.add("Field city should only contain letters");
        String stateName = districtService.findStateById(Integer.parseInt(state));
        if (stateName == null)
            errorMessages.add("Invalid State");
        String districtName = districtService.findDistrictById(Integer.parseInt(district));
        if (districtName == null)
            errorMessages.add("Invalid district");

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
    public ResponseEntity<?> searchServiceProviderBasedOnGivenFields(String state, String district, String first_name, String last_name, String mobileNumber, Long test_status_id,Long ticketId) {
        try {
            CustomServiceProviderTicket customServiceProviderTicket=null;
            if(ticketId!=null)
            {
                customServiceProviderTicket=entityManager.find(CustomServiceProviderTicket.class,ticketId);
            }
            if (first_name == null && last_name == null && state == null && district == null && mobileNumber == null && test_status_id == null) {
                Query query = entityManager.createQuery("SELECT s FROM ServiceProviderEntity s JOIN ServiceProviderAddress a ON s = a.serviceProviderEntity", ServiceProviderEntity.class);
                List<ServiceProviderEntity> serviceProviderEntityList = query.getResultList();
                List<Map<String, Object>> response = new ArrayList<>();
                for (ServiceProviderEntity serviceProvider : serviceProviderEntityList) {
                    response.add(sharedUtilityService.serviceProviderDetailsMap(serviceProvider));
                }
                return ResponseService.generateSuccessResponse("Service Providers", response, HttpStatus.OK);
            }

            if (mobileNumber != null && !isNumeric(mobileNumber)) {
                throw new IllegalArgumentException("Mobile number is not in correct format.");
            }
            if ((state != null && !isAlphabetOnly(state)) || (district != null && !isAlphabetOnly(district)) || (first_name != null && !isAlphabetOnly(first_name)) || (last_name != null && !isAlphabetOnly(last_name))) {
                throw new IllegalArgumentException("String values are not in right format.");
            }

            Map<String, Character> alias = new HashMap<>();
            if (first_name != null) {
                first_name = first_name.trim();
                first_name = first_name.toLowerCase();
            }
            if (last_name != null) {
                last_name = last_name.trim();
                last_name = last_name.toLowerCase();
            }
            alias.put("state", 'a');
            alias.put("district", 'a');
            alias.put("first_name", 's');
            alias.put("last_name", 's');
            alias.put("test_status_id", 's');
            String generalizedQuery = "SELECT s.*\n" +
                    "FROM service_provider s\n" +
                    "JOIN custom_service_provider_address a ON s.service_provider_id = a.service_provider_id\n" +
                    "WHERE ";
            if (mobileNumber != null) {
                ServiceProviderEntity serviceProviderEntity = entityManager.createQuery(Constant.PHONE_QUERY_SERVICE_PROVIDER, ServiceProviderEntity.class)
                        .setParameter("mobileNumber", mobileNumber)
                        .setParameter("country_code", "+91")
                        .getResultStream()
                        .findFirst()
                        .orElse(null);
                if (serviceProviderEntity != null)
                    return ResponseService.generateSuccessResponse("Service Providers", sharedUtilityService.serviceProviderDetailsMap(serviceProviderEntity), HttpStatus.OK);
                else
                    throw new PersistenceException("No results found for your input");
            }
            if (test_status_id != null) {
                Query query = entityManager.createQuery("SELECT s FROM ServiceProviderTestStatus s WHERE s.test_status_id = :test_status_id", ServiceProviderTestStatus.class);
                query.setParameter("test_status_id", test_status_id);
                List<ServiceProviderTestStatus> serviceProviderTestStatus = query.getResultList();
                if (serviceProviderTestStatus.size() == 0) {

                    throw new IllegalArgumentException("No Test Status is found with this id");
                }
            }
            String[] fieldsNames = {"state", "district", "first_name", "last_name", "test_status_id"};
            Object[] fields = {state, district, first_name, last_name, test_status_id};
            for (int i = 0; i < fields.length; i++) {
                if (fields[i] != null) {
                    if (fieldsNames[i].equals("first_name") || fieldsNames[i].equals("last_name")) {
                        String fieldValue = fields[i].toString().toLowerCase(); // Convert input to lower case
                        // Check if the field value is longer than 2 characters (to avoid unnecessary wildcard matching)
                        if (fieldValue.length() > 2) {
                            generalizedQuery += "LOWER(" + alias.get(fieldsNames[i]) + "." + fieldsNames[i] + ") LIKE LOWER(:" + fieldsNames[i] + ") || '%' AND ";
                        } else {
                            generalizedQuery += "LOWER(" + alias.get(fieldsNames[i]) + "." + fieldsNames[i] + ") LIKE LOWER(:" + fieldsNames[i] + ") || '%' AND ";
                        }
                    } else {
                        generalizedQuery += alias.get(fieldsNames[i]) + "." + fieldsNames[i] + " = :" + fieldsNames[i] + " AND ";
                    }
                }
            }
            generalizedQuery = generalizedQuery.trim();
            int lastSpaceIndex = generalizedQuery.lastIndexOf(" ");
            generalizedQuery = generalizedQuery.substring(0, lastSpaceIndex);
            Query query;
            query = entityManager.createNativeQuery(generalizedQuery, ServiceProviderEntity.class);
            for (int i = 0; i < fields.length; i++) {
                if (fields[i] != null)
                    query.setParameter(fieldsNames[i], fields[i]);
            }
            System.out.println("Am here");
            List<ServiceProviderEntity> listOfSp = query.getResultList();
            if (customServiceProviderTicket != null) {
                Iterator<ServiceProviderEntity> iterator = listOfSp.iterator();
                while (iterator.hasNext()) {
                    ServiceProviderEntity serviceProvider = iterator.next();
                    System.out.println("Am hereeeeeeeeee");
                    if (customServiceProviderTicket.getRejectedBy().contains(serviceProvider.getService_provider_id())) {
                        iterator.remove();
                    }
                }
            }
            List<Map<String, Object>> response = new ArrayList<>();
            for (ServiceProviderEntity serviceProvider : listOfSp) {
                response.add(sharedUtilityService.serviceProviderDetailsMap(serviceProvider));
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
            Query query2 = entityManager.createQuery("SELECT s FROM ServiceProviderTestStatus s WHERE s.test_status_id = :test_status_id", ServiceProviderTestStatus.class);
            query2.setParameter("test_status_id", 3L);
            List<ServiceProviderTestStatus> serviceProviderTestStatus = query2.getResultList();
            if (serviceProviderTestStatus.size() == 0) {
                throw new IllegalArgumentException("No Test Status is found with this id");
            }

            Query query = entityManager.createQuery("SELECT s FROM ServiceProviderEntity s JOIN ServiceProviderAddress a ON s = a.serviceProviderEntity WHERE s.testStatus = :testStatusId AND s.isActive = :isActive", ServiceProviderEntity.class);
            query.setParameter("testStatusId", serviceProviderTestStatus.get(0));
            query.setParameter("isActive", true);

            List<ServiceProviderEntity> serviceProviderEntityList = query.getResultList();
            return serviceProviderEntityList;

        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("Exception caught while incrementing ticketAssigned of SP: " + exception.getMessage());
        }
    }
}