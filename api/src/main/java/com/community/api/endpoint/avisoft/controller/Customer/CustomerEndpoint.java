
package com.community.api.endpoint.avisoft.controller.Customer;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CommunicationRequest;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.CustomerBasicDetailsDto;
import com.community.api.endpoint.avisoft.controller.ServiceProvider.ServiceProviderController;
import com.community.api.endpoint.avisoft.controller.otpmodule.OtpEndpoint;
import com.community.api.endpoint.customer.AddressDTO;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomerReferrer;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.DocumentValidity;
import com.community.api.entity.Qualification;
import com.community.api.entity.QualificationDetails;
import com.community.api.entity.Post;
import com.community.api.entity.Role;
import com.community.api.entity.StateCode;
import com.community.api.services.*;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.Document;
import com.community.api.utils.DocumentType;
import com.community.api.utils.ServiceProviderDocument;
import io.micrometer.core.lang.Nullable;
import org.apache.tomcat.util.bcel.Const;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.profile.core.domain.*;
import org.broadleafcommerce.profile.core.service.AddressService;
import org.broadleafcommerce.profile.core.service.CountryService;
import org.broadleafcommerce.profile.core.service.CustomerAddressService;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.persistence.Query;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Column;
import javax.persistence.TypedQuery;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.File;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.community.api.component.Constant.*;
import static com.community.api.services.ServiceProvider.ServiceProviderServiceImpl.getLongList;

@RestController
@RequestMapping(value = "/customer",
        produces = {
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_XML_VALUE
        }
)

public class CustomerEndpoint {
    private PasswordEncoder passwordEncoder;
    private CustomerService customerService;  //@TODO- do this task asap
    private ExceptionHandlingImplement exceptionHandling;
    private EntityManager em;
    private CustomCustomerService customCustomerService;
    private AddressService addressService;
    private CustomerAddressService customerAddressService;
    private JwtUtil jwtUtil;

    @Autowired
    private ResponseService responseService;
    @Autowired
    QualificationService qualificationService;
    @Autowired
    private DocumentStorageService fileUploadService;
    @Autowired
    private SharedUtilityService sharedUtilityServiceApi;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ReserveCategoryAgeService reserveCategoryAgeService;
    @Autowired
    private ExceptionHandlingService exceptionHandlingService;
    @Autowired
    private ReserveCategoryDtoService reserveCategoryDtoService;
    @Autowired
    private PhysicalRequirementDtoService physicalRequirementDtoService;
    @Autowired
    private ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    private ProductReserveCategoryFeePostRefService reserveCategoryFeePostRefService;

    @Autowired
    private CountryService countryService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private FileDownloadService fileDownloadService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DocumentStorageService documentStorageService;
    @Autowired
    private ReserveCategoryService reserveCategoryService;
    @Autowired
    private ApplicationScopeService applicationScopeService;
    @Autowired
    private SanitizerService sanitizerService;
    @Autowired
    private CatalogService catalogService;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private PostExecutionService postExecutionService;
    @Autowired
    private SharedUtilityService sharedUtilityService;

    public static Date convertStringToDate(String dateStr, String s) throws ParseException {
        if (dateStr == null || dateStr.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat.setLenient(false);
        return dateFormat.parse(dateStr);
    }

    public static java.sql.Date convertStringToSQLDate(String dateStr, String dateFormatInString) throws ParseException {
        if (dateStr == null || dateStr.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatInString);
        dateFormat.setLenient(false);
        return new java.sql.Date(dateFormat.parse(dateStr).getTime());
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setEm(EntityManager em) {
        this.em = em;
    }

    @Autowired
    public void setCustomCustomerService(CustomCustomerService customCustomerService) {
        this.customCustomerService = customCustomerService;
    }

    @Autowired
    public void setAddressService(AddressService addressService) {
        this.addressService = addressService;
    }

    @Autowired
    public void setCustomerAddressService(CustomerAddressService customerAddressService) {
        this.customerAddressService = customerAddressService;
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @RequestMapping(value = "get-customer", method = RequestMethod.GET)
    @Authorize(value = {Constant.roleUser, Constant.roleSuperAdmin, Constant.roleAdmin, Constant.roleServiceProvider})
    public ResponseEntity<?> retrieveCustomerById(@RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer Service Not Initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer with this ID does not exist", HttpStatus.NOT_FOUND);

            } else {
                return ResponseService.generateSuccessResponse("Customer with this ID is found " + customerId, customer, HttpStatus.OK);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving Customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomer(@RequestBody Map<String, Object> details, @RequestParam Long customerId, @RequestHeader(value = "extAuthToken", required = false) String authToken, @RequestHeader(value = "Authorization") String authHeader,HttpServletRequest httpServletRequest) {
        try {
            Boolean externalUpdate = false;
            Boolean isValidDate = null;
            Boolean isValidDateDomicile = null;
            String jwtToken = authHeader.substring(7);
            List<String> deleteLogs = new ArrayList<>();
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            List<String> errorMessages = new ArrayList<>();

            details = sanitizerService.sanitizeInputMap(details);

            /*Iterator<String> iterator = details.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (details.get(key).toString().isEmpty()) {
                    iterator.remove(); // Safely remove using the iterator
                    errorMessages.add(key + " cannot be null");
                }
            }*/
            if (!errorMessages.isEmpty()) {
                return ResponseService.generateErrorResponse("List of Failed validations: " + errorMessages.toString(), HttpStatus.BAD_REQUEST);
            }
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);

            if ((roleId == 3||roleId==4)||(roleId == 5 && !tokenUserId.equals(customerId))/*(roleId == 4 && customCustomer.getCreatedByRole() == 4 && customCustomer.getCreatedById() != tokenUserId) || (roleId == 4 && customCustomer.getRegisteredBySp().equals(false)) || (roleId == 5 && !tokenUserId.equals(customerId))||roleId==1||roleId==2||roleId==3*/) {
                if (authToken != null && !authToken.isEmpty()) {
                    Integer roleUpdating = jwtTokenUtil.extractRoleId(authToken);
                    Long userId = jwtTokenUtil.extractId(authToken);
                    if (roleUpdating != 5 || !userId.equals(customerId))
                        return ResponseService.generateErrorResponse("Forbidden Access", HttpStatus.UNAUTHORIZED);
                } else {
                    return ResponseService.generateErrorResponse("Forbidden Access", HttpStatus.UNAUTHORIZED);
                }
            }
            if (customCustomer == null) {
                return ResponseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);
            }
            if (customCustomer.getArchived().equals(true)) {
                return ResponseService.generateErrorResponse("Your account is suspended. Please contact support.", HttpStatus.FORBIDDEN);
            }
            String secondaryMobileNumber = (String) details.get("secondaryMobileNumber");
            String mobileNumber = (String) details.get("mobileNumber");
            if (secondaryMobileNumber != null && mobileNumber == null && secondaryMobileNumber.equalsIgnoreCase(customCustomer.getMobileNumber())) {
                return ResponseService.generateErrorResponse("Primary and Secondary Mobile Numbers cannot be the same", HttpStatus.BAD_REQUEST);
            }
            if (details.containsKey("interestedInDefence")) {
                Boolean value = (Boolean) details.get("interestedInDefence");
                customCustomer.setInterestedInDefence(value);
            }
            // physical attributes locale variables.
            double minHeight = 50.0, maxHeight = 250.0,minWeight = 10.0, maxWeight = 300.0,minShoeSize = 4.0, maxShoeSize = 15.0,minWaistSize = 20.0, maxWaistSize = 150.0,minChestSize = 20.0, maxChestSize = 125.0;

            if ((customCustomer.getInterestedInDefence() != null && details.containsKey("interestedInDefence"))) {
                if (customCustomer.getInterestedInDefence()) {
                    // List of required fields
                    final List<String> requiredFields = Arrays.asList("heightCms", "weightKgs", "shoeSizeInches", "waistSizeCms");

                    // Check if all required fields are present and not empty
                    Map<String, Object> finalDetails = details;
                    boolean conditionExists = requiredFields.stream()
                            .allMatch(field -> finalDetails.containsKey(field) && finalDetails.get(field) != null && !finalDetails.get(field).toString().isEmpty());
                    if (!conditionExists) {
                        errorMessages.add("All relevant fields : height, weight, shoe size, waist size must be present ");
                    } else {
                        try {
                            String heightStr = (String) finalDetails.get("heightCms");
                            if (heightStr != null && !heightStr.isEmpty()) {
                                Double heightValue = Double.parseDouble(heightStr);
                                if (heightValue < minHeight || heightValue > maxHeight) {
                                    errorMessages.add("Height should be between " + minHeight + " and " + maxHeight + " cms.");
                                } else {
                                    customCustomer.setHeightCms(heightValue);
                                }
                            } else {
                                errorMessages.add("Height is required and must be a valid value.");
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.add("Height must be valid");
                        }

                        try {
                            String weightStr = (String) finalDetails.get("weightKgs");
                            if (weightStr != null && !weightStr.isEmpty()) {
                                Double weightValue = Double.parseDouble(weightStr);
                                if (weightValue < minWeight || weightValue > maxWeight) {
                                    errorMessages.add("Weight should be between " + minWeight + " and " + maxWeight + " kgs.");
                                } else {
                                    customCustomer.setWeightKgs(weightValue);
                                }
                            } else {
                                errorMessages.add("Weight is required and must be a valid value.");
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.add("Weight must be valid.");
                        }

                        try {
                            String shoeSizeStr = (String) finalDetails.get("shoeSizeInches");
                            if (shoeSizeStr != null && !shoeSizeStr.isEmpty()) {
                                Double shoeSizeValue = Double.parseDouble(shoeSizeStr);
                                if (shoeSizeValue < minShoeSize || shoeSizeValue > maxShoeSize) {
                                    errorMessages.add("Shoe size should be between " + minShoeSize + " and " + maxShoeSize + " inches.");
                                } else {
                                    customCustomer.setShoeSizeInches(shoeSizeValue);
                                }
                            } else {
                                errorMessages.add("Shoe size is required and must be a valid value.");
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.add("Shoe size must be valid.");
                        }

                        try {
                            String waistSizeStr = (String) finalDetails.get("waistSizeCms");
                            if (waistSizeStr != null && !waistSizeStr.isEmpty()) {
                                Double waistSizeValue = Double.parseDouble(waistSizeStr);
                                if (waistSizeValue < minWaistSize || waistSizeValue > maxWaistSize) {
                                    errorMessages.add("Waist size should be between " + minWaistSize + " and " + maxWaistSize + " cms.");
                                } else {
                                    customCustomer.setWaistSizeCms(waistSizeValue);
                                }
                            } else {
                                errorMessages.add("Waist size is required and must be a valid value.");
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.add("Waist size must be valid.");
                        }
                    }
                } else {
                    String height = (String) details.get("heightCms");
                    String weightKgs = (String) details.get("weightKgs");
                    String shoeSizeInches = (String) details.get("shoeSizeInches");
                    String waistSizeCms = (String) details.get("waistSizeCms");

                    if (height != null && !height.isEmpty()) {
                        try {
                           Double heightValue = Double.parseDouble(height);
                            if (heightValue < minHeight || heightValue > maxHeight) {
                                errorMessages.add("Height should be between " + minHeight + " and " + maxHeight + " cms.");
                            } else {
                                customCustomer.setHeightCms(heightValue);
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.add("Height must be valid.");
                        }
                    }

                    if (weightKgs != null && !weightKgs.isEmpty()) {
                        try {
                            Double weightValue = Double.parseDouble(weightKgs);
                            if (weightValue < minWeight || weightValue > maxWeight) {
                                errorMessages.add("Weight should be between " + minWeight + " and " + maxWeight + " kgs.");
                            } else {
                                customCustomer.setWeightKgs(weightValue);
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.add("Weight must be valid.");
                        }
                    }

                    if (shoeSizeInches != null && !shoeSizeInches.isEmpty()) {
                        try {
                            Double shoeSizeValue = Double.parseDouble(shoeSizeInches);
                            if (shoeSizeValue < minShoeSize || shoeSizeValue > maxShoeSize) {
                                errorMessages.add("Shoe size should be between " + minShoeSize + " and " + maxShoeSize + " inches.");
                            } else {
                                customCustomer.setShoeSizeInches(shoeSizeValue);
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.add("Shoe size must be valid.");
                        }
                    }

                    if (waistSizeCms != null && !waistSizeCms.isEmpty()) {
                        try {
                            Double waistSizeValue = Double.parseDouble(waistSizeCms);
                            if (waistSizeValue < minWaistSize || waistSizeValue > maxWaistSize) {
                                errorMessages.add("Waist size should be between " + minWaistSize + " and " + maxWaistSize + " cms.");
                            } else {
                                customCustomer.setWaistSizeCms(waistSizeValue);
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.add("Waist size must be valid.");
                        }
                    }
                }}


            if (details.containsKey("workExperienceScopeId")) {
                CustomApplicationScope customApplicationScope = applicationScopeService.getApplicationScopeById(Long.parseLong(details.get("workExperienceScopeId").toString()));
                customCustomer.setWorkExperienceScopeId(customApplicationScope);
                if (details.containsKey("workExperience")) {
                    Integer workExperience = Integer.parseInt(details.get("workExperience").toString());
                    customCustomer.setWorkExperience(workExperience);
                }
            } else if (details.containsKey("workExperience")) {
                errorMessages.add("Give scope of work before adding work experience");
            }

            if (details.containsKey("sportCertificateId")) {
                CustomApplicationScope customApplicationScope = applicationScopeService.getApplicationScopeById(Long.parseLong((String) details.get("sportCertificateId")));
                customCustomer.setSportCertificateId(customApplicationScope);
            }
            if(details.containsKey("exService"))
            {
                Boolean exService = (Boolean) details.get("exService");
                if(exService)
                {
                    customCustomer.setExService(true);
                }
                else {
                    List<Document> customerDocuments = customCustomer.getDocuments();
                    for (Document document : customerDocuments) {
                        if (document.getIsArchived().equals(false)) {
                            if (document.getCustom_customer().getId().equals(customerId)) {
                                if (document.getDocumentType().getDocument_type_id().equals(15)) {
                                    document.setIsArchived(true);
                                    entityManager.merge(document);
                                }
                            }
                        }
                    }
                    customCustomer.setExService(false);
                }
            }


            if (details.containsKey("domicile")) {
                Boolean domicile = (Boolean) details.get("domicile");
                if (domicile) {
                    if (details.containsKey("domicileState")) {
                        StateCode state = districtService.getStateByStateId(Integer.parseInt(details.get("domicileState").toString()));
                        customCustomer.setDomicile(true);
                        customCustomer.setDomicileState(state);
                    } else {
                        errorMessages.add("cannot leave domicile state as null by opting for the domicile.");
                    }
                } else {
                    if ((details.containsKey("domicileState"))) {
                        errorMessages.add("cannot give domicile state w/o opting for the domicile.");
                    }
                    customCustomer.setDomicile(false);
                    customCustomer.setDomicileState(null);
                }
            } else if (details.containsKey("domicileState")) {
                errorMessages.add("cannot give domicile state w/o opting for the domicile.");
            }

            if (details.containsKey("hidePhoneNumber")) {
                customCustomer.setHidePhoneNumber((Boolean) details.get("hidePhoneNumber"));
                if ((Boolean) details.get("hidePhoneNumber").equals(true)) {
                    errorMessages.addAll(validateHidePhoneNumber(details, customCustomer));
                }
                if (secondaryMobileNumber != null && !customCustomerService.isValidMobileNumber(secondaryMobileNumber))
                    errorMessages.add("Secondary mobile is invalid");
                if (details.containsKey("whatsappNumber") && !customCustomerService.isValidMobileNumber((String) details.get("whatsappNumber")))
                    errorMessages.add("Invalid Whatsapp NUmber");
                if (errorMessages.isEmpty()) {
                    customCustomer.setSecondaryMobileNumber(secondaryMobileNumber);
                    customCustomer.setWhatsappNumber((String) details.get("whatsappNumber"));
                    customCustomer.setHidePhoneNumber((Boolean) details.get("hidePhoneNumber"));
                }
                details.remove("secondaryMobileNumber");
                details.remove("whatsappNumber");
                details.remove("hidePhoneNumber");
            }
            // Validate mobile number
            if (mobileNumber != null && secondaryMobileNumber != null) {
                if (mobileNumber.equalsIgnoreCase(secondaryMobileNumber)) {
                    errorMessages.add("Primary and Secondary Mobile Numbers cannot be the same");
                }
            }
            if (mobileNumber != null && !customCustomerService.isValidMobileNumber(mobileNumber)) {
                return ResponseService.generateErrorResponse("Cannot update phoneNumber", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (mobileNumber != null && secondaryMobileNumber == null && mobileNumber.equalsIgnoreCase(customCustomer.getSecondaryMobileNumber())) {
                return ResponseService.generateErrorResponse("Primary and Secondary Mobile Numbers cannot be the same", HttpStatus.BAD_REQUEST);
            }

            // Check for existing username and email
            String username = (String) details.get("username");
            String emailAddress = (String) details.get("emailAddress");
            Customer existingCustomerByUsername = (username != null) ? customerService.readCustomerByUsername(username) : null;
            Customer existingCustomerByEmail = (emailAddress != null) ? customerService.readCustomerByEmail(emailAddress) : null;

            if ((existingCustomerByUsername != null && !existingCustomerByUsername.getId().equals(customerId)) ||
                    (existingCustomerByEmail != null && !existingCustomerByEmail.getId().equals(customerId))) {
                return ResponseService.generateErrorResponse("Email or Username already in use", HttpStatus.BAD_REQUEST);
            }

            // Update customer fields
            customCustomer.setId(customerId);
            customCustomer.setMobileNumber(customCustomer.getMobileNumber());
            customCustomer.setQualificationDetailsList(customCustomer.getQualificationDetailsList());
            customCustomer.setCountryCode(customCustomer.getCountryCode());

            if (details.containsKey("firstName") && !details.get("firstName").toString().isEmpty()) {
                customCustomer.setFirstName((String) details.get("firstName"));
            } else if (details.containsKey("firstName") && details.get("firstName").toString().isEmpty()) {
                errorMessages.add("First name cannot be null");
            } else if (details.containsKey("firstName") && !sharedUtilityService.isAlphabetic((String) details.get("firstName"))) {
                errorMessages.add("Invalid First name");
            }
            if (details.containsKey("lastName") && !details.get("lastName").toString().isEmpty())
                customCustomer.setLastName((String) details.get("lastName"));
            else if (details.containsKey("lastName") && details.get("lastName").toString().isEmpty()) {
                errorMessages.add("Last name cannot be null");
            } else if (details.containsKey("lastName") && !sharedUtilityService.isAlphabetic((String) details.get("lastName"))) {
                errorMessages.add("Invalid Last name");
            }
            if (details.containsKey("emailAddress") && ((String) details.get("emailAddress")).isEmpty())
                errorMessages.add("email Address cannot be null");
            if (details.containsKey("emailAddress") && !((String) details.get("emailAddress")).isEmpty())
                customCustomer.setEmailAddress(emailAddress);
            // Handle dynamic fields
            details.remove("firstName");
            details.remove("lastName");
            details.remove("emailAddress");

            String state = (String) details.get("currentState");
            String district = (String) details.get("currentDistrict");
            String pincode = (String) details.get("currentPincode");
            String addressLine = (String) details.get("currentAddress");
            String city = (String) details.get("currentCity");
            boolean flag = true;
            String[] keys = {"currentState", "currentDistrict", "currentPincode", "currentAddress", "currentCity"};
            int containsCount = 0;
            for (String key : keys) {
                if (details.containsKey(key) && details.get(key) != null)
                    containsCount++;
                else {
                    flag = false;
                    break;  // Exit the loop as we found a missing or null value
                }
            }
            if (flag && containsCount == 5)
                errorMessages.addAll(customCustomerService.validateAddress(addressLine, city, pincode));
            if (flag && containsCount == 5) {
                boolean updated = false;
                for (CustomerAddress customerAddress : customCustomer.getCustomerAddresses()) {
                    if (customerAddress.getAddressName().equals("CURRENT_ADDRESS")) {
                        customerAddress.getAddress().setAddressLine1(addressLine);
                        String stateName = districtService.findStateById(Integer.parseInt(state));
                        if (stateName == null)
                            return ResponseService.generateErrorResponse("Invalid State", HttpStatus.BAD_REQUEST);
                        customerAddress.getAddress().setStateProvinceRegion(stateName);
                        String districtName = districtService.findDistrictById(Integer.parseInt(district));
                        if (districtName == null)
                            return ResponseService.generateErrorResponse("Invalid district", HttpStatus.BAD_REQUEST);
                        customerAddress.getAddress().setCounty(districtName);
                        customerAddress.getAddress().setPostalCode(pincode);
                        customerAddress.getAddress().setCity(city);
                        CountryImpl country = (CountryImpl) countryService.findCountryByAbbreviation("ADD-C");
                        customerAddress.getAddress().setCountry(country);
                        updated = true;
                        break;
                    }
                }
                if (!updated) {
                    Map<String, Object> addressMap = new HashMap<>();
                    addressMap.put("address", details.get("currentAddress"));
                    String stateName = districtService.findStateById(Integer.parseInt(state));
                    if (stateName == null)
                        return ResponseService.generateErrorResponse("Invalid State", HttpStatus.BAD_REQUEST);
                    addressMap.put("state", stateName);
                    addressMap.put("city", details.get("currentCity"));
                    String districtName = districtService.findDistrictById(Integer.parseInt(district));
                    if (districtName == null)
                        return ResponseService.generateErrorResponse("Invalid district", HttpStatus.BAD_REQUEST);
                    addressMap.put("district", districtName);
                    addressMap.put("pinCode", pincode);
                    addressMap.put("addressName", "CURRENT_ADDRESS");
                    addAddress(customerId, addressMap);
                }
            } else if (!flag && containsCount != 0)
                errorMessages.add("All fields : Address line,state,city,district,pincode should be provided to add Current Address");
            details.remove("currentState");
            details.remove("currentDistrict");
            details.remove("currentAddress");
            details.remove("currentPincode");
            details.remove("currentCity");
            Boolean flagP = true;
            containsCount = 0;
            String[] keysP = {"permanentState", "permanentDistrict", "permanentPincode", "permanentAddress", "permanentCity"};
            for (String key : keysP) {

                if (details.containsKey(key) && details.get(key) != null)
                    containsCount++;
                else {
                    flagP = false;
                    break;  // Exit the loop as we found a missing or null value
                }
            }
            state = (String) details.get("permanentState");
            district = (String) details.get("permanentDistrict");
            pincode = (String) details.get("permanentPincode");
            addressLine = (String) details.get("permanentAddress");
            city = (String) details.get("permanentCity");

            if (flagP && containsCount == 5)
                errorMessages.addAll(customCustomerService.validateAddress(addressLine, city, pincode));
            if (flagP && containsCount == 5) {
                boolean updated = false;
                for (CustomerAddress customerAddress : customCustomer.getCustomerAddresses()) {

                    if (customerAddress.getAddressName().equals("PERMANENT_ADDRESS")) {
                        customerAddress.getAddress().setAddressLine1(addressLine);
                        String stateName = districtService.findStateById(Integer.parseInt(state));
                        if (stateName == null)
                            return ResponseService.generateErrorResponse("Invalid State", HttpStatus.BAD_REQUEST);
                        customerAddress.getAddress().setStateProvinceRegion(stateName);
                        String districtName = districtService.findDistrictById(Integer.parseInt(district));
                        if (districtName == null)
                            return ResponseService.generateErrorResponse("Invalid district", HttpStatus.BAD_REQUEST);
                        customerAddress.getAddress().setCounty(districtName);
                        customerAddress.getAddress().setPostalCode(pincode);
                        customerAddress.getAddress().setCity(city);
                        CountryImpl country = (CountryImpl) countryService.findCountryByAbbreviation("ADD-P");
                        customerAddress.getAddress().setCountry(country);
                        updated = true;
                        break;
                    }
                }
                if (!updated) {
                    Map<String, Object> addressMap = new HashMap<>();
                    addressMap.put("address", details.get("permanentAddress"));
                    String stateName = districtService.findStateById(Integer.parseInt(state));
                    if (stateName == null)
                        return ResponseService.generateErrorResponse("Invalid State", HttpStatus.BAD_REQUEST);
                    addressMap.put("state", stateName);
                    addressMap.put("city", details.get("permanentCity"));
                    String districtName = districtService.findDistrictById(Integer.parseInt(district));
                    if (districtName == null)
                        return ResponseService.generateErrorResponse("Invalid district", HttpStatus.BAD_REQUEST);
                    addressMap.put("district", districtName);
                    addressMap.put("pinCode", pincode);
                    addressMap.put("addressName", "PERMANENT_ADDRESS");
                    addAddress(customerId, addressMap);
                }
            } else if (!flagP && containsCount != 0)
                errorMessages.add("All fields : Address line,state,city,district,pincode should be provided to add Permanent address");
            if (details.containsKey("adharNumber")) {
                String adharNumber = (String) details.get("adharNumber");
                if (customCustomer.getAdharNumber() != null) {
                    if (!customCustomer.getAdharNumber().equals(adharNumber)) {
                        Query query = entityManager.createNativeQuery("SELECT COUNT(*) FROM custom_customer WHERE adhar_number = :adharNumber");
                        query.setParameter("adharNumber", adharNumber);
                        Integer result = ((Number) query.getSingleResult()).intValue();
                        if (result > 0) {
                            errorMessages.add("Aadhar number already in use.");
                            details.remove("adharNumber");
                        }
                    }
                } else if (customCustomer.getAdharNumber() == null) {
                    Query query = entityManager.createNativeQuery("SELECT COUNT(*) FROM custom_customer WHERE adhar_number = :adharNumber");
                    query.setParameter("adharNumber", adharNumber);
                    Integer result = ((Number) query.getSingleResult()).intValue();
                    System.out.println("result" + result);
                    if (result > 0) {
                        errorMessages.add("Aadhar number already in use!!");
                        details.remove("adharNumber");
                    }
                }
            }

            details.remove("permanentState");
            details.remove("permanentDistrict");
            details.remove("permanentAddress");
            details.remove("permanentPincode");
            details.remove("permanentCity");

            String dateFormat = "dd-MM-yyyy";
            if (details.containsKey("nccCertificate")) {
                String nccCertificateValue = (String) details.get("nccCertificate");

                if (!nccCertificateValue.equalsIgnoreCase("NCC Certificate A") && !nccCertificateValue.equalsIgnoreCase("NCC Certificate B") && !nccCertificateValue.equalsIgnoreCase("NCC Certificate C")) {
                    return ResponseService.generateErrorResponse("You can add value for ncc certificate either NCC Certificate A or NCC Certificate B or  NCC Certificate C", HttpStatus.BAD_REQUEST);
                }
                customCustomer.setNccCertificate(nccCertificateValue);
                customCustomer.setIsNccCertificate(true);

            }
            if (details.containsKey("dob")) {
                //SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

                // Parse the string to a Date object
                String dob = (String) details.get("dob").toString();
                //if (!dob.before(new Date())) {
                //errorMessages.add("DOB must be of past.");
                //}
                int age= sharedUtilityServiceApi.calculateAge(dob);
                if(age==-1)
                    errorMessages.add("Invalid date of birth");
                else if(age<8)
                    errorMessages.add("Your age should be greater than equal to 8");
                else
                customCustomer.setDob(dob);
            }
            if (details.containsKey("isLivePhotoNa")) {
               Boolean isLivePhotoNa= (Boolean) details.get("isLivePhotoNa");
               if(isLivePhotoNa.equals(true))
               {
                   assert customCustomer.getDocuments() != null;
                   for(Document document: customCustomer.getDocuments())
                   {
                       if(document.getDocumentType().getDocument_type_id().equals(3) && document.getIsArchived().equals(false))
                       {
                           throw new IllegalArgumentException("You cannot select NA as true if live photo is already uploaded");
                       }

                   }
               }
                customCustomer.setIsLivePhotoNa(isLivePhotoNa);
            }
            if (details.containsKey("isNccCertificate")) {
                Boolean isNccCertificate = (Boolean) details.get("isNccCertificate");
                if (isNccCertificate.equals(true)) {
                    if (!details.containsKey("nccCertificate")) {
                        return ResponseService.generateErrorResponse("You have to select ncc certificate type", HttpStatus.BAD_REQUEST);
                    }
                    customCustomer.setNccCertificate((String) details.get("nccCertificate"));
                }

                if (isNccCertificate.equals(false)) {
                    customCustomer.setNccCertificate(null);
                    List<Document> customerDocuments = customCustomer.getDocuments();
                    for (Document document : customerDocuments) {
                        if (document.getIsArchived().equals(false)) {
                            if (document.getCustom_customer().getId().equals(customerId)) {
                                if (document.getDocumentType().getDocument_type_id().equals(18) || document.getDocumentType().getDocument_type_id().equals(19) || document.getDocumentType().getDocument_type_id().equals(20)) {
                                    document.setIsArchived(true);
                                    entityManager.merge(document);
                                }
                            }
                        }
                    }

                }
                customCustomer.setIsNccCertificate(isNccCertificate);

            }
            if (details.containsKey("nssCertificate")) {
                String nssCertificateValue = (String) details.get("nssCertificate");
                if (!nssCertificateValue.equalsIgnoreCase("NSS Certificate A") && !nssCertificateValue.equalsIgnoreCase("NSS Certificate B") && !nssCertificateValue.equalsIgnoreCase("NSS Certificate C")) {
                    return ResponseService.generateErrorResponse("You can add value for ncc certificate either NSS Certificate A or NSS Certificate B or  NSS Certificate C", HttpStatus.BAD_REQUEST);
                }
                customCustomer.setNssCertificate(nssCertificateValue);
                customCustomer.setIsNssCertificate(true);
            }
            if (details.containsKey("isNssCertificate")) {
                Boolean isNssCertificate = (Boolean) details.get("isNssCertificate");
                if (isNssCertificate.equals(true)) {
                    if (!details.containsKey("nssCertificate")) {
                        return ResponseService.generateErrorResponse("You have to select nss certificate type", HttpStatus.BAD_REQUEST);
                    }
                    customCustomer.setNssCertificate((String) details.get("nssCertificate"));
                } else if (isNssCertificate.equals(false)) {
                    customCustomer.setNssCertificate(null);
                    List<Document> customerDocuments = customCustomer.getDocuments();
                    for (Document document : customerDocuments) {
                        if (document.getIsArchived().equals(false)) {
                            if (document.getCustom_customer().getId().equals(customerId)) {
                                if (document.getDocumentType().getDocument_type_id().equals(21) || document.getDocumentType().getDocument_type_id().equals(28) || document.getDocumentType().getDocument_type_id().equals(29)) {
                                    document.setIsArchived(true);
                                    entityManager.merge(document);
                                }
                            }
                        }
                    }
                }
                customCustomer.setIsNssCertificate(isNssCertificate);
            }

            details.remove("isNccCertificate");
            details.remove("nccCertificate");
            details.remove("isNssCertificate");
            details.remove("nssCertificate");

            if (details.containsKey("isOtherOrStateCategory")) {
                Boolean isOtherCategory = (Boolean) details.get("isOtherOrStateCategory");
                if (isOtherCategory.equals(true)) {
                    if (!details.containsKey("otherOrStateCategory")) {
                        return ResponseService.generateErrorResponse("You have to enter other or State Category", HttpStatus.BAD_REQUEST);
                    }
                    if (!details.containsKey("otherCategoryDateOfIssue")) {
                        return ResponseService.generateErrorResponse("You have to enter date of issue for other or State Category", HttpStatus.BAD_REQUEST);
                    }
                    if (details.containsKey("otherOrStateCategory") && details.get("otherOrStateCategory").toString().trim().isEmpty()) {
                        return ResponseService.generateErrorResponse("other or state Category value cannot be empty ", HttpStatus.BAD_REQUEST);
                    }
                    if (details.containsKey("otherCategoryDateOfIssue") && details.get("otherCategoryDateOfIssue").toString().trim().isEmpty()) {
                        return ResponseService.generateErrorResponse("otherCategory DateOfIssue cannot be empty ", HttpStatus.BAD_REQUEST);
                    }
                    if (details.containsKey("otherCategoryValidUpto")) {
                        String validUpto = (String) details.get("otherCategoryValidUpto");
                        if (validUpto.isEmpty()) {
                            customCustomer.setOtherCategoryValidUpto(null);
                            isValidDate = validateDate((String) details.get("otherCategoryDateOfIssue"), null, dateFormat);
                        } else if (validUpto.trim().isEmpty()) {
                            customCustomer.setOtherCategoryValidUpto(null);
                            validateDate((String) details.get("otherCategoryDateOfIssue"), null, dateFormat);
                        } else {
                            validateDate((String) details.get("otherCategoryDateOfIssue"), (String) details.get("otherCategoryValidUpto"), dateFormat);
                            customCustomer.setOtherCategoryValidUpto(convertStringToSQLDate((String) details.get("otherCategoryValidUpto"), dateFormat));
                        }
                    } else {
                        validateDate((String) details.get("otherCategoryDateOfIssue"), (String) details.get("otherCategoryValidUpto"), dateFormat);
                    }
                    customCustomer.setOtherOrStateCategory((String) details.get("otherOrStateCategory"));
                    customCustomer.setOtherCategoryDateOfIssue(convertStringToSQLDate((String) details.get("otherCategoryDateOfIssue"), dateFormat));
                } else if (isOtherCategory.equals(false)) {
                    if (details.containsKey("otherOrStateCategory")) {
                        return ResponseService.generateErrorResponse("otherOrStateCategory cannot be given if isOtherCategory is false", HttpStatus.BAD_REQUEST);
                    }
                    if (details.containsKey("otherCategoryDateOfIssue")) {
                        return ResponseService.generateErrorResponse("otherCategoryDateOfIssue key cannot be given if isOtherCategory is false", HttpStatus.BAD_REQUEST);
                    }
                    if (details.containsKey("otherCategoryValidUpto")) {
                        return ResponseService.generateErrorResponse("otherCategoryValidUpto key cannot be given if isOtherCategory is false", HttpStatus.BAD_REQUEST);
                    }
                    customCustomer.setOtherOrStateCategory(null);
                    List<Document> customerDocuments = customCustomer.getDocuments();
                    for (Document document : customerDocuments) {
                        if (document.getIsArchived().equals(false)) {
                            if (document.getCustom_customer().getId().equals(customerId)) {
                                if (document.getDocumentType().getDocument_type_id().equals(30)) {
                                    document.setIsArchived(true);
                                    entityManager.merge(document);
                                }
                            }
                        }
                    }
                    customCustomer.setOtherCategoryDateOfIssue(null);
                    customCustomer.setOtherCategoryValidUpto(null);
                }
                customCustomer.setIsOtherOrStateCategory(isOtherCategory);
            }
            details.remove("isOtherOrStateCategory");

            if (details.containsKey("domicile")) {
                Boolean domicile = (Boolean) details.get("domicile");
                if (domicile.equals(true)) {
                    if (!details.containsKey("domicileIssueDate")) {
                        return ResponseService.generateErrorResponse("You have to enter date of issue for domicile", HttpStatus.BAD_REQUEST);
                    }
                    if (details.containsKey("domicileIssueDate") && details.get("domicileIssueDate").toString().trim().isEmpty()) {
                        return ResponseService.generateErrorResponse("domicile DateOfIssue cannot be empty ", HttpStatus.BAD_REQUEST);
                    }
                    if (details.containsKey("domicileValidUpto")) {
                        String validUpto = (String) details.get("domicileValidUpto");
                        if (validUpto.isEmpty()) {
                            customCustomer.setDomicileValidUpto(null);
                            isValidDateDomicile = validateDate((String) details.get("domicileIssueDate"), null, dateFormat);
                        } else if (validUpto.trim().isEmpty()) {
                            customCustomer.setDomicileValidUpto(null);
                            validateDate((String) details.get("domicileIssueDate"), null, dateFormat);
                        } else {
                            validateDate((String) details.get("domicileIssueDate"), (String) details.get("domicileValidUpto"), dateFormat);
                            customCustomer.setDomicileValidUpto(convertStringToSQLDate((String) details.get("domicileValidUpto"), dateFormat));
                        }
                    } else {
                        validateDate((String) details.get("domicileIssueDate"), (String) details.get("domicileValidUpto"), dateFormat);
                    }
                    customCustomer.setDomicileIssueDate(convertStringToSQLDate((String) details.get("domicileIssueDate"), dateFormat));
                } else if (domicile.equals(false)) {
                    if (details.containsKey("domicileIssueDate")) {
                        return ResponseService.generateErrorResponse("domicileIssueDate key cannot be given if domicile is false", HttpStatus.BAD_REQUEST);
                    }
                    if (details.containsKey("domicileState")) {
                        return ResponseService.generateErrorResponse("domicileState key cannot be given if domicile is false", HttpStatus.BAD_REQUEST);
                    }
                    if (details.containsKey("domicileValidUpto")) {
                        return ResponseService.generateErrorResponse("domicileValidUpto key cannot be given if domicile is false", HttpStatus.BAD_REQUEST);
                    }
                    customCustomer.setDomicileState(null);
                    List<Document> customerDocuments = customCustomer.getDocuments();
                    for (Document document : customerDocuments) {
                        if (document.getIsArchived().equals(false)) {
                            if (document.getCustom_customer().getId().equals(customerId)) {
                                if (document.getDocumentType().getDocument_type_id().equals(10)) {
                                    document.setIsArchived(true);
                                    entityManager.merge(document);
                                }
                            }
                        }
                    }
                    customCustomer.setDomicileIssueDate(null);
                    customCustomer.setDomicileValidUpto(null);
                }
                customCustomer.setDomicile(domicile);
            }
            details.remove("domicile");

            if (details.containsKey("belongsToMinority")) {
                Boolean isMinority = (Boolean) details.get("belongsToMinority");
                if (isMinority.equals(false)) {
                    List<Document> customerDocuments = customCustomer.getDocuments();
                    for (Document document : customerDocuments) {
                        if (document.getIsArchived().equals(false)) {
                            if (document.getCustom_customer().getId().equals(customerId)) {
                                if (document.getDocumentType().getDocument_type_id().equals(31)) {
                                    document.setIsArchived(true);
                                    entityManager.merge(document);
                                }
                            }
                        }
                    }
                }
                customCustomer.setBelongsToMinority(isMinority);
            }
            details.remove("belongsToMinority");

            if (details.containsKey("isSportsCertificate")) {
                Boolean isSportsCertificate = (Boolean) details.get("isSportsCertificate");
                if (isSportsCertificate.equals(false)) {
                    List<Document> customerDocuments = customCustomer.getDocuments();
                    for (Document document : customerDocuments) {
                        if (document.getIsArchived().equals(false)) {
                            if (document.getCustom_customer().getId().equals(customerId)) {
                                if (document.getDocumentType().getDocument_type_id().equals(22) || document.getDocumentType().getDocument_type_id().equals(23)) {
                                    document.setIsArchived(true);
                                    entityManager.merge(document);
                                }
                            }
                        }
                    }
                    customCustomer.setSportCertificateId(null);
                }
                customCustomer.setIsSportsCertificate(isSportsCertificate);
            }

            details.remove("sportsCertificate");
            details.remove("isSportsCertificate");

            if ((customCustomer.getGender() != null && customCustomer.getGender().toLowerCase().equals("female")
                    || (customCustomer.getGender() == null && details.containsKey("gender") && ((String) details.get("gender")).toLowerCase().equals("female"))
                    || (customCustomer.getGender() != null && customCustomer.getGender().toLowerCase().equals("male") && details.containsKey("gender") && ((String) details.get("gender")).toLowerCase().equals("female")))
                    && details.containsKey("chestSizeCms")) {
                return ResponseService.generateErrorResponse("Cannot add chest size for gender : Female", HttpStatus.BAD_REQUEST);
            }

            if (customCustomer.getGender() == null && details.containsKey("chestSizeCms"))
                return ResponseService.generateErrorResponse("Cannot add chest size without specifying gender", HttpStatus.BAD_REQUEST);

            if (details.containsKey("chestSizeCms")) {
                if (customCustomer.getGender().equals("Female")) {
                    return ResponseService.generateErrorResponse("Cannot add chest size with female", HttpStatus.BAD_REQUEST);
                } else {
                    String chestSizeCms = (String) details.get("chestSizeCms");
                    if (chestSizeCms != null && !chestSizeCms.isEmpty()) {
                        try {
                            Double chestSizeValue = Double.parseDouble(chestSizeCms);
                            if (chestSizeValue < minChestSize || chestSizeValue > maxChestSize) {
                                errorMessages.add("Chest size should be between " + minChestSize + " and " + maxChestSize + " cms.");
                            } else {
                                customCustomer.setChestSizeCms(chestSizeValue);
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.add("Chest size must be valid.");
                        }
                    }
                    customCustomer.setChestSizeCms(Double.parseDouble(chestSizeCms));
                }
            }

            for (Map.Entry<String, Object> entry : details.entrySet()) {
                String fieldName = entry.getKey();
                Object newValue = entry.getValue();
                Field field = CustomCustomer.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                Column columnAnnotation = field.getAnnotation(Column.class);
                boolean isColumnNotNull = (columnAnnotation != null && !columnAnnotation.nullable());
                // Check if the field has the @Nullable annotation
                boolean isNullable = field.isAnnotationPresent(Nullable.class);
                field.setAccessible(true);
                if (newValue != null) {
                    if (newValue.toString().isEmpty() && !isNullable) {
                        errorMessages.add(fieldName + " cannot be null");
                        continue;
                    }
                }
                if (field.isAnnotationPresent(Pattern.class)) {
                    Pattern patternAnnotation = field.getAnnotation(Pattern.class);
                    String regex = patternAnnotation.regexp();
                    String message = patternAnnotation.message(); // Get custom message
                    if (!newValue.toString().matches(regex)) {
                        errorMessages.add(patternAnnotation.message()); // Use a placeholder
                        continue;
                    }
                }
                // Validate not null

                // Validate size if applicable
                if (field.isAnnotationPresent(Size.class)) {
                    Size sizeAnnotation = field.getAnnotation(Size.class);
                    int min = sizeAnnotation.min();
                    int max = sizeAnnotation.max();
                    if (newValue.toString().length() > max || newValue.toString().length() < min) {
                        errorMessages.add(fieldName + " size should be between " + min + " and " + max);
                        continue;
                    }
                }
                if (field.isAnnotationPresent(Min.class)) {
                    Min minAnnotation = field.getAnnotation(Min.class);
                    long minValue = minAnnotation.value();

                    // Check if newValue can be parsed to a number
                    if (newValue != null) {
                        long parsedValue = sharedUtilityService.parseToLong(newValue);
                        if (parsedValue < minValue) {
                            errorMessages.add(minAnnotation.message());
                        }
                    }
                }

                // Check for @Max annotation
                if (field.isAnnotationPresent(Max.class)) {
                    Max maxAnnotation = field.getAnnotation(Max.class);
                    long maxValue = maxAnnotation.value();

                    // Check if newValue can be parsed to a number
                    if (newValue != null) {
                        long parsedValue = sharedUtilityService.parseToLong(newValue);
                        if (parsedValue > maxValue) {
                            errorMessages.add(maxAnnotation.message());
                        }
                    }
                }

                // Set value if type is compatible
                if (newValue != null && field.getType().isAssignableFrom(newValue.getClass())) {
                    field.set(customCustomer, newValue);
                }
            }
            if (details.containsKey("category")) {
                if (((String) details.get("category")).equalsIgnoreCase("GEN")) {
                    customCustomer.setCategoryIssueDate(null);
                    customCustomer.setCategoryValidUpto(null);
                }
            }else if(!details.containsKey("category")) {
                if (customCustomer.getCategory().equalsIgnoreCase("GEN")) {
                    customCustomer.setCategoryIssueDate(null);
                    customCustomer.setCategoryValidUpto(null);
                }
            }
            // Update address if needed
            if (details.containsKey("categoryIssueDate") && details.containsKey("categoryValidUpto")) {

                if (sharedUtilityService.validateCategoryIssueAndValidUptoDates((String) details.get("categoryIssueDate"), (String) details.get("categoryValidUpto"), errorMessages)) {
                    if (details.containsKey("category")) {
                        if(!((String)details.get("category")).equalsIgnoreCase("GEN"))
                        {
                            customCustomer.setCategoryIssueDate((String) details.get("categoryIssueDate"));
                            customCustomer.setCategoryValidUpto((String) details.get("categoryValidUpto"));
                        }
                        else {
                            customCustomer.setCategoryIssueDate(null);
                            customCustomer.setCategoryValidUpto(null);
                        }
                    }
                    else if(!details.containsKey("category")) {
                        if(!customCustomer.getCategory().equalsIgnoreCase("GEN"))
                        {
                            customCustomer.setCategoryIssueDate((String) details.get("categoryIssueDate"));
                            customCustomer.setCategoryValidUpto((String) details.get("categoryValidUpto"));
                        }
                        else {
                            customCustomer.setCategoryIssueDate(null);
                            customCustomer.setCategoryValidUpto(null);
                        }
                    }
                }

            } else if (details.containsKey("categoryIssueDate")) {

                if (sharedUtilityService.validateCategoryIssueDate((String) details.get("categoryIssueDate"), customCustomer, errorMessages)) {
                    if (details.containsKey("category")) {
                        if(!((String)details.get("category")).equalsIgnoreCase("GEN"))
                        {
                            customCustomer.setCategoryIssueDate((String) details.get("categoryIssueDate"));
                        }
                        else {
                            customCustomer.setCategoryIssueDate(null);
                            customCustomer.setCategoryValidUpto(null);
                        }
                    }
                    else if(!details.containsKey("category")) {
                        if(!customCustomer.getCategory().equalsIgnoreCase("GEN"))
                        {
                            customCustomer.setCategoryIssueDate((String) details.get("categoryIssueDate"));
                        }
                        else {
                            customCustomer.setCategoryIssueDate(null);
                            customCustomer.setCategoryValidUpto(null);
                        }
                    }

                }
            } else if (details.containsKey("categoryValidUpto")) {

                if (sharedUtilityService.validateCategoryUptoDate((String) details.get("categoryValidUpto"), customCustomer, errorMessages)) {
                    if (details.containsKey("category")) {
                        if(!((String)details.get("category")).equalsIgnoreCase("GEN"))
                        {
                            customCustomer.setCategoryValidUpto((String) details.get("categoryValidUpto"));
                        }
                        else {
                            customCustomer.setCategoryIssueDate(null);
                            customCustomer.setCategoryValidUpto(null);
                        }
                    }
                    else if(!details.containsKey("category")) {
                        if(!customCustomer.getCategory().equalsIgnoreCase("GEN"))
                        {
                            customCustomer.setCategoryValidUpto((String) details.get("categoryValidUpto"));
                        }
                        else {
                            customCustomer.setCategoryIssueDate(null);
                            customCustomer.setCategoryValidUpto(null);
                        }
                    }
                }
            }
            if (details.containsKey("disability")) {
                Boolean cond = (Boolean) details.get("disability");
                customCustomer.setDisability(cond);
                if (cond) {
                    if (details.containsKey("disabilityType")) {
                        String disabilityType = (String) details.get("disabilityType");
                        customCustomer.setDisabilityType(disabilityType);
                        if (details.containsKey("disabilityPercentage")) {
                            Object disabilityPercentageObj = details.get("disabilityPercentage");

                            Double disabilityPercentage = null;

                            // Check if the value is already a Double or Integer and handle accordingly
                            if (disabilityPercentageObj instanceof Double) {
                                disabilityPercentage = (Double) disabilityPercentageObj;
                            } else {
                                disabilityPercentage = ((Integer) disabilityPercentageObj).doubleValue();
                            }
                            if (disabilityPercentage < 0.0 || disabilityPercentage > 100.0) {
                                errorMessages.add("disability percentage must be in range 1-100");
                            }
                            customCustomer.setDisabilityPercentage(disabilityPercentage);
                        }
                    } else {
                        errorMessages.add("disability type is mandatory when disability is given");
                    }
                } else {
                    List<Document> customerDocuments = customCustomer.getDocuments();
                    for (Document document : customerDocuments) {
                        if (document.getIsArchived().equals(false)) {
                            if (document.getCustom_customer().getId().equals(customerId)) {
                                if (document.getDocumentType().getDocument_type_id().equals(11) ) {
                                    document.setIsArchived(true);
                                    entityManager.merge(document);
                                }
                            }
                        }
                    }
                    customCustomer.setDisabilityType(null);
                    customCustomer.setDisabilityPercentage(0.0);
                }
            } else if (details.containsKey("disabilityType")) {
                errorMessages.add("disability must be given in order to give disability Type");
            } else if (details.containsKey("disabilityPercentage")) {
                errorMessages.add("disability must be given in order to give disability Type");
            }

            if (details.containsKey("workExperienceScopeId")) {
                Long scopeId = Long.parseLong(details.get("workExperienceScopeId").toString());
                CustomApplicationScope customApplicationScope = applicationScopeService.getApplicationScopeById(scopeId);
                if (customApplicationScope == null) {
                    errorMessages.add("No Application scope found with this id");
                }
                customCustomer.setWorkExperienceScopeId(customApplicationScope);
            }
            if (isValidDate != null && isValidDate.equals(true)) {
                errorMessages.remove(errorMessages.size() - 1);
            }
            if (isValidDateDomicile != null && isValidDateDomicile.equals(true)) {
                errorMessages.remove(errorMessages.size() - 1);
            }
            if (!errorMessages.isEmpty()) {
                return ResponseService.generateErrorResponse("List of Failed validations: " + errorMessages.toString(), HttpStatus.BAD_REQUEST);
            }
            customCustomer.setModifiedById(tokenUserId);
            customCustomer.setModifiedByRole(roleId);

            if(!customCustomer.getEmailActive()&&customCustomer.getEmailAddress()!=null)
            {
                customCustomer.setEmailActive(true);
            em.merge(customCustomer);
            List<String>email=new ArrayList<>();
            email.add(customCustomer.getEmailAddress());
                Customer customer=customerService.readCustomerById(customCustomer.getId());
                String welcomeMessage = String.format(WELCOME_BODY_TEMPLATE,customer.getFirstName()+" "+customer.getLastName());
                CompletableFuture.runAsync(() -> {
                    try {
                        emailService.sendEmailWithAttachments(email, Constant.WELCOME_SUBJECT, welcomeMessage, null);
                    } catch (MessagingException e) {
                         throw new RuntimeException(e);
                    }
                });
            }
            else {
                em.merge(customCustomer);
            }
            return ResponseService.generateSuccessResponse("User details updated successfully", sharedUtilityService.breakReferenceForCustomer(customCustomer, authHeader,httpServletRequest), HttpStatus.OK);

        } catch (ClassCastException classCastException) {
            exceptionHandling.handleException(classCastException);
            return ResponseService.generateErrorResponse("Invalid Casting: " + classCastException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ParseException parseException) {
            exceptionHandling.handleException(parseException);
            return ResponseService.generateErrorResponse("Unparsable Exception: " + parseException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NumberFormatException exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Invalid Format: " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            exceptionHandling.handleException(dataIntegrityViolationException);
            return ResponseService.generateErrorResponse("Error updating " + dataIntegrityViolationException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ConstraintViolationException constraintViolationException) {
            exceptionHandling.handleException(constraintViolationException);
            return ResponseService.generateErrorResponse("Error updating " + constraintViolationException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NoSuchFieldException noSuchFieldException) {
            exceptionHandling.handleException(noSuchFieldException);
            return ResponseService.generateErrorResponse("No such field present :" + noSuchFieldException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Error updating " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<String> validateHidePhoneNumber(Map<String, Object> details, CustomCustomer customer) {
        List<String> errorMessages = new ArrayList<>();
        details = sanitizerService.sanitizeInputMap(details);

        if (((Boolean) details.get("hidePhoneNumber")).equals(true)) {

            if (details.containsKey("secondaryMobileNumber") && ((String) details.get("secondaryMobileNumber")).isEmpty()) {
                errorMessages.add("Need to provide Secondary Mobile Number when hiding primary Mobile Number");
            }

            if (details.containsKey("whatsappNumber") && ((String) details.get("whatsappNumber")).isEmpty()) {
                errorMessages.add("Whatsapp number cannot be null");
            }
            if (details.containsKey("whatsappNumber") && ((String) details.get("whatsappNumber")).equals(customer.getMobileNumber())) {
                errorMessages.add("Cannot set primary number as whatsapp number when hidden");
            }
        }
        return errorMessages;
    }

    public boolean isFieldPresent(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return field != null; // Field exists
        } catch (NoSuchFieldException e) {
            return false; // Field does not exist
        }
    }

    @Transactional
    @Authorize(value = {Constant.roleUser, Constant.roleSuperAdmin, Constant.roleAdmin, Constant.roleServiceProvider, Constant.roleServiceProviderAdmin})
    @RequestMapping(value = "/get-customer-details/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<?> getUserDetails(@PathVariable Long customerId, @RequestHeader(value = "Authorization") String authHeader,HttpServletRequest httpServletRequest) {
        try {
            String jwtToken = authHeader.substring(7);
            List<String> deleteLogs = new ArrayList<>();
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role=roleService.getRoleByRoleId(roleId);

            //checking for super admin and admin
            if((role.getRole_name().equals(roleUser)&& !Objects.equals(tokenUserId, customerId))/*||role.getRole_name().equals(roleServiceProvider)*/)
                return ResponseService.generateErrorResponse("Forbidden",HttpStatus.FORBIDDEN);
            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
            if (customCustomer == null) {
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            }
            if(customCustomer.getArchived()!=null)
            {
                if (customCustomer.getArchived().equals(true)) {
                    return ResponseService.generateErrorResponse("Your account is suspended. Please contact support.", HttpStatus.FORBIDDEN);
                }
            }
            CustomerImpl customer = em.find(CustomerImpl.class, customerId);  // Assuming you retrieve the base Customer entity
            Map<String, Object> customerDetails = sharedUtilityService.breakReferenceForCustomer(customer, authHeader,httpServletRequest);

            return responseService.generateSuccessResponse("User details retrieved successfully", customerDetails, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving user details", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @Authorize(value = {Constant.roleUser, Constant.roleServiceProvider, Constant.roleSuperAdmin, Constant.roleAdmin, Constant.roleAdminServiceProvider})
    @PostMapping("/upload-documents")
    public ResponseEntity<?> uploadDocuments(
            @RequestParam Long customerId,
            @RequestParam(value = "exUpdate",defaultValue = "false",required = false) Boolean extUpdate,
            @RequestHeader(value = "extAuth",required = false) String extAuth,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam("fileTypes") List<Integer> fileTypes,
            @RequestParam(value = "qualificationDetailId", required = false) Long qualificationDetailId,
            @RequestParam(value = "dateOfIssue", required = false) String dateOfIssue,
            @RequestParam(value = "validUpto", required = false) String validUpto,
            @RequestParam(value = "otherDocument", required = false) String otherDocument,
            @RequestParam(value = "removeFileTypes", required = false) Boolean removeFileTypes,
            @RequestHeader(value = "Authorization") String authHeader)
    {
        try {
            String dateFormat = "yyyy-MM-dd";
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseService.generateErrorResponse("Authorization header is missing or invalid.", HttpStatus.UNAUTHORIZED);
            }
            String jwtToken = authHeader.substring(7);
            List<String> deleteLogs = new ArrayList<>();
            Integer roleId;
            Long tokenUserId;
            if(extAuth==null||extAuth.isEmpty()) {
                roleId = jwtTokenUtil.extractRoleId(jwtToken);
                tokenUserId = jwtTokenUtil.extractId(jwtToken);
            }
            else
            {
                roleId = jwtTokenUtil.extractRoleId(extAuth);
                tokenUserId = jwtTokenUtil.extractId(extAuth);
            }
            if(extUpdate&&(roleId!=1&&roleId!=2&&roleId!=5)&&(extAuth==null||extAuth.isEmpty()))
                return ResponseService.generateErrorResponse("Forbidden Access",HttpStatus.UNAUTHORIZED);
            String role=null;
            if(extUpdate)
                role= roleUser;
            else
                role = roleService.getRoleByRoleId(roleId).getRole_name();
            if(!role.equals(roleUser))
            {
                role=roleServiceProvider;
            }
            String queryStringArchive = null;
            String queryStringArchiveId = null;

            //**********DELETE DOCUMENT :START*********
            if (removeFileTypes != null && removeFileTypes.equals(true)) {
                if (role.equals(Constant.roleUser)) {
                    queryStringArchive = String.format(Constant.FETCH_DOCUMENT_TO_ARCHIVE, "document", "custom_customer_id");
                    queryStringArchiveId = String.format(Constant.FETCH_DOCUMENT_TO_ARCHIVE_ID, "document", "custom_customer_id");
                } else if (role.equals(Constant.roleServiceProvider)) {
                    queryStringArchive = String.format(Constant.FETCH_DOCUMENT_TO_ARCHIVE, "service_provider_documents", "service_provider_id");
                    queryStringArchiveId = String.format(Constant.FETCH_DOCUMENT_TO_ARCHIVE_ID, "service_provider_documents", "service_provider_id");
                }
                for (Integer fileType : fileTypes) {
                    DocumentType documentTypeObj = em.createQuery(
                                    "SELECT dt FROM DocumentType dt WHERE dt.document_type_id = :documentTypeId", DocumentType.class)
                            .setParameter("documentTypeId", fileType)
                            .getResultStream()
                            .findFirst()
                            .orElse(null);

                    if (documentTypeObj == null) {
                        return ResponseService.generateErrorResponse(
                                "Unknown document type for file: " + fileType,
                                HttpStatus.BAD_REQUEST);
                    }
                    if(documentTypeObj.getDocument_type_id().equals(12))
                    {
                        if(qualificationDetailId==null)
                        {
                            throw new IllegalArgumentException("Qualification detail id is required to delete a qualification document");
                        }
                    }
                    if(documentTypeObj.getDocument_type_id().equals(13))
                    {
                        if(otherDocument==null)
                        {
                            throw new IllegalArgumentException("other document is required to delete a other document");
                        }
                    }
                    boolean isQualificationDocumentToDelete=false;
                    boolean isOtherDocumentToDelete=false;
                    try {
                        String archiveIdQuery = queryStringArchiveId;
                        String archiveQuery = queryStringArchive;

                        // Append qualification_detail_id condition if provided
                        if(documentTypeObj.getDocument_type_id().equals(12) && qualificationDetailId != null)
                        {
                            isQualificationDocumentToDelete=true;
                            archiveIdQuery += " AND qualification_detail_id = :qualificationDetailId";
                            archiveQuery += " AND qualification_detail_id = :qualificationDetailId";
                        }
                        if(documentTypeObj.getDocument_type_id().equals(13) && qualificationDetailId != null)
                        {
                            isOtherDocumentToDelete=true;
                            archiveIdQuery += " AND otherdocument = :otherDocument";
                            archiveQuery += " AND otherdocument = :otherDocument";
                        }

                        Query query = entityManager.createNativeQuery(archiveIdQuery);
                        query.setParameter("userId", customerId);
                        query.setParameter("documentTypeId", fileType);
                        if (isQualificationDocumentToDelete) {
                            query.setParameter("qualificationDetailId", qualificationDetailId);
                        }
                        if(isOtherDocumentToDelete)
                        {
                            query.setParameter("otherDocument", otherDocument);
                        }

                        BigInteger id = (BigInteger) query.getSingleResult();

                        query = entityManager.createNativeQuery(archiveQuery);
                        query.setParameter("userId", customerId);
                        query.setParameter("documentTypeId", fileType);
                        if (isQualificationDocumentToDelete) {
                            query.setParameter("qualificationDetailId", qualificationDetailId);
                        }
                        if(isOtherDocumentToDelete)
                        {
                            query.setParameter("otherDocument", otherDocument);
                        }

                        int result = query.executeUpdate();

                        if (result == 1) {
                            switch (role) {
                                case Constant.roleUser:
                                    Document document = entityManager.find(Document.class, id.longValue());
                                    CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customerId);
                                    if (customCustomer.getDocuments() != null) {
                                        Iterator<Document> iterator = customCustomer.getDocuments().iterator();
                                        while (iterator.hasNext()) {
                                            Document documentToDeleteC = iterator.next();
                                            if (documentToDeleteC.getDocumentId().equals(document.getDocumentId())) {
                                                iterator.remove();  // safely remove the document
                                                entityManager.merge(customCustomer);  // merge after modification
                                                break;
                                            }
                                        }
                                    }
                                    break;
                                case Constant.roleServiceProvider:
                                    ServiceProviderDocument serviceProviderDocument = entityManager.find(ServiceProviderDocument.class, id.longValue());
                                    ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, customerId);
                                    if (serviceProvider.getDocuments() != null) {
                                        Iterator<ServiceProviderDocument> iterator = serviceProvider.getDocuments().iterator();
                                        while (iterator.hasNext()) {
                                            ServiceProviderDocument documentToDelete = iterator.next();
                                            if (documentToDelete.getDocumentId().equals(serviceProviderDocument.getDocumentId())) {
                                                iterator.remove();  // safely remove the document
                                                entityManager.merge(serviceProvider);  // merge after modification
                                                break;
                                            }
                                        }
                                        deleteLogs.add(documentTypeObj.getDocument_type_name() + " Deleted");
                                    }
                            }
                        } else {
                            return ResponseService.generateErrorResponse("No documents found", HttpStatus.NOT_FOUND);
                        }
                        return ResponseService.generateSuccessResponse("Document deleted successfully", deleteLogs, HttpStatus.OK);
                    } catch (NoResultException noResultException) {
                        return ResponseService.generateErrorResponse("No record found", HttpStatus.NOT_FOUND);
                    } catch (PersistenceException persistenceException) {
                        return ResponseService.generateErrorResponse("No operation to perform", HttpStatus.NOT_FOUND);
                    }
                }
            }
            //*******DELETE DOCUMENT :END**********
            if (customerId == null || files == null || fileTypes == null) {
                return ResponseService.generateErrorResponse("Invalid request parameters.", HttpStatus.BAD_REQUEST);
            }


            if (role == null) {
                return ResponseService.generateErrorResponse("Role not found for this user.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (!customerId.equals(tokenUserId)&&(roleId!=1&&roleId!=2)) {
                return ResponseService.generateErrorResponse("Unauthorized request.", HttpStatus.UNAUTHORIZED);
            }

            Map<Integer, List<MultipartFile>> groupedFiles = new HashMap<>();
            for (int i = 0; i < files.size(); i++) {
                Integer fileTypeId = fileTypes.get(i);
                MultipartFile file = files.get(i);
                groupedFiles.computeIfAbsent(fileTypeId, k -> new ArrayList<>()).add(file);
            }
            MultipartFile processedFile=null;

            if (roleService.findRoleName(roleId).equals(roleUser)||((roleService.findRoleName(roleId).equals(roleSuperAdmin)||roleService.findRoleName(roleId).equals(roleAdmin))&&extUpdate)) {
                HashSet<Document> documentsToSave = new HashSet<>();
                CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
                if (customCustomer == null) {
                    return ResponseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);
                }

                Map<String, Object> responseData = new HashMap<>();
                List<String> deletedDocumentMessages = new ArrayList<>();

                for (Map.Entry<Integer, List<MultipartFile>> entry : groupedFiles.entrySet()) {
                    Integer fileNameId = entry.getKey();
                    List<MultipartFile> fileList = entry.getValue();

                    DocumentType documentTypeObj = em.createQuery(
                                    "SELECT dt FROM DocumentType dt WHERE dt.document_type_id = :documentTypeId", DocumentType.class)
                            .setParameter("documentTypeId", fileNameId)
                            .getResultStream()
                            .findFirst()
                            .orElse(null);

                    if (documentTypeObj == null) {
                        return ResponseService.generateErrorResponse(
                                "Unknown document type for file: " + fileNameId,
                                HttpStatus.BAD_REQUEST);
                    }
                    if (documentTypeObj.getDocument_type_id().equals(13)) {
                        if (otherDocument == null) {
                            throw new IllegalArgumentException("otherDocument name cannot be null for uploading other Documents");
                        }
                        if (otherDocument.trim().isEmpty()) {
                            throw new IllegalArgumentException("otherDocument name cannot be empty");
                        }
                    }

                    if (documentTypeObj.getIs_qualification_document().equals(true)) {
                        if (qualificationDetailId == null) {
                            throw new IllegalArgumentException("QualificationDetail id cannot be null for uploading Qualfication Documents");
                        }
                    }

                    if (documentTypeObj.getIs_issue_date_required().equals(true)) {
                        if (dateOfIssue == null) {
                            throw new IllegalArgumentException("Date of issue cannot be null");
                        }
                        if (documentTypeObj.getIs_expiration_date_required().equals(true)) {
                            if (validUpto == null) {
                                throw new IllegalArgumentException("Valid upto (expiration date of document) cannot be null");
                            }
                        }
                    }
                    for (MultipartFile file : fileList) {

                        // Validate document
                        if (documentTypeObj.getDocument_type_id().equals(3)) {  // If it's a Live Photo
                            processedFile = documentStorageService.convertToJpg(file);
                            customCustomer.setIsLivePhotoNa(false);
                        } else {
                            documentStorageService.validateDocument(file, documentTypeObj);
                        }
                        Document existingDocument = null;

                        if (qualificationDetailId != null && documentTypeObj.getIs_qualification_document().equals(true)) {
                            existingDocument = em.createQuery(
                                            "SELECT d FROM Document d WHERE d.custom_customer = :customCustomer " +
                                                    "AND d.documentType = :documentType " +
                                                    "AND (d.qualificationDetails.qualification_detail_id = :qualificationDetailId ) " +
                                                    "AND d.name IS NOT NULL", Document.class)
                                    .setParameter("customCustomer", customCustomer)
                                    .setParameter("documentType", documentTypeObj)
                                    .setParameter("qualificationDetailId", qualificationDetailId)
                                    .getResultStream()
                                    .findFirst()
                                    .orElse(null);
                        } else {
                            existingDocument = em.createQuery(
                                            "SELECT d FROM Document d WHERE d.custom_customer = :customCustomer " +
                                                    "AND d.documentType = :documentType AND d.name IS NOT NULL ", Document.class)
                                    .setParameter("customCustomer", customCustomer)
                                    .setParameter("documentType", documentTypeObj)
                                    .getResultStream()
                                    .findFirst()
                                    .orElse(null);
                        }

                        if (documentTypeObj.getDocument_type_id().equals(3)) {
                            fileUploadService.uploadFileOnFileServer(processedFile, documentTypeObj.getDocument_type_name(), customerId.toString(), role);
                        } else {
                            fileUploadService.uploadFileOnFileServer(file, documentTypeObj.getDocument_type_name(), customerId.toString(), role);
                        }

                        if (removeFileTypes != null && removeFileTypes) {

                            if (existingDocument != null && fileNameId != 13) {
                                if (existingDocument != null) {
                                    String filePath = existingDocument.getFilePath();

                                    if (filePath != null) {
                                        fileUploadService.deleteFile(customerId, documentTypeObj.getDocument_type_name(), existingDocument.getName(), role);
                                    }

                                    existingDocument.setDocumentType(null);
                                    existingDocument.setFilePath(null);
                                    existingDocument.setName(null);
                                    em.persist(existingDocument);
                                    documentsToSave.add(existingDocument);

                                    deletedDocumentMessages.add(documentTypeObj.getDocument_type_name() + "' has been deleted.");
                                }
                                continue;
                            }
                        }


                        if (fileNameId == 13 && (!file.isEmpty() || file != null)) {
                            String newFileName = file.getOriginalFilename();
                            // Check for existing document with the same name
                            Document existingDocument13 = em.createQuery(
                                            "SELECT d FROM Document d WHERE d.custom_customer = :customCustomer " +
                                                    "AND d.documentType = :documentType " +
                                                    "AND LOWER(d.otherDocument) = LOWER(:otherDocument) " +  // Case-insensitive check
                                                    "AND d.name IS NOT NULL", Document.class)
                                    .setParameter("customCustomer", customCustomer)
                                    .setParameter("documentType", documentTypeObj)
                                    .setParameter("otherDocument", otherDocument.toLowerCase())  // Ensure consistency
                                    .getResultStream()
                                    .findFirst()
                                    .orElse(null);

                            if (existingDocument13 == null) {
                                Document createdDocument = documentStorageService.createDocument(file, documentTypeObj, customCustomer, customerId, role);
                                if (documentTypeObj.getDocument_type_id().equals(13)) {
                                    createdDocument.setOtherDocument(otherDocument);
                                    entityManager.merge(createdDocument);
                                }
                                documentsToSave.add(createdDocument);
                            } else if (existingDocument13 != null) {

                                String filePath = existingDocument13.getFilePath();
                                if (filePath != null) {
                                    String absolutePath = System.getProperty("user.dir") + "/../test/" + filePath;
                                    File oldFile = new File(absolutePath);
                                    String oldFileName = oldFile.getName();
                                    existingDocument13.setIsArchived(false);
                                    if (!newFileName.equals(oldFileName)) {
                                        fileUploadService.deleteFile(customerId, documentTypeObj.getDocument_type_name(), existingDocument13.getName(), role);
                                        documentStorageService.updateOrCreateDocument(existingDocument13, file, documentTypeObj, customerId, role);
                                    }
                                }
                                entityManager.merge(existingDocument13);
                                documentsToSave.add(existingDocument13);
                            }
                        }
                        // If the file is not empty and a document already exists, update the document
                        else if (existingDocument != null && (!file.isEmpty() || file != null) && fileNameId != 13) {
                            String filePath = existingDocument.getFilePath();
                            if (documentTypeObj.getDocument_type_id().equals(3)) {
                                customCustomer.setIsLivePhotoNa(false);
                            }
                            if (qualificationDetailId != null && documentTypeObj.getIs_qualification_document().equals(true)) {
                                QualificationDetails qualificationDetails = findQualificationDetailForCustomer(qualificationDetailId, customCustomer);
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
                                    documentValidity.setDocument(existingDocument);
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
                                    documentValidity.setDocument(existingDocument);
                                    existingDocument.setDocumentValidity(documentValidity);
                                    entityManager.merge(documentValidity);
                                }

                            }
                            if (filePath != null) {
                                String absolutePath = System.getProperty("user.dir") + "/../test/" + filePath;
                                File oldFile = new File(absolutePath);
                                String oldFileName = oldFile.getName();

                                // Get the expected filename after any conversion
                                boolean isLivePhoto = documentTypeObj.getDocument_type_id().equals(3);
                                String newFileName = documentStorageService.getConvertedFilename(file, isLivePhoto);

                                existingDocument.setIsArchived(false);

                                if (!newFileName.equals(oldFileName)) {
                                    fileUploadService.deleteFile(customerId, documentTypeObj.getDocument_type_name(), existingDocument.getName(), role);
                                    if (isLivePhoto) {
                                        documentStorageService.updateOrCreateDocument(existingDocument, processedFile, documentTypeObj, customerId, role);
                                    } else {
                                        documentStorageService.updateOrCreateDocument(existingDocument, file, documentTypeObj, customerId, role);
                                    }
                                }
                            }
                            entityManager.merge(existingDocument);
                            documentsToSave.add(existingDocument);
                        } else {
                            // If the file is not empty create the document
                            if (!file.isEmpty() || file != null && (fileNameId != 13)) {
                                Document document = null;
                                if (documentTypeObj.getDocument_type_id().equals(3)) {
                                    customCustomer.setIsLivePhotoNa(false);
                                    document = documentStorageService.createDocument(processedFile, documentTypeObj, customCustomer, customerId, role);
                                } else {
                                    document = documentStorageService.createDocument(file, documentTypeObj, customCustomer, customerId, role);
                                }
                                documentsToSave.add(document);
                                if (documentTypeObj.getDocument_type_id().equals(3)) {
                                    customCustomer.setIsLivePhotoNa(false);
                                }
                                if (qualificationDetailId != null && documentTypeObj.getIs_qualification_document().equals(true)) {
                                    QualificationDetails qualificationDetails = findQualificationDetailForCustomer(qualificationDetailId, customCustomer);
                                    document.setIs_qualification_document(true);
                                    document.setQualificationDetails(qualificationDetails);
                                    entityManager.merge(document);
                                    documentsToSave.add(document);
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
                                    documentValidity.setDocument(document);
                                    entityManager.persist(documentValidity);
                                    document.setDocumentValidity(documentValidity);
                                    entityManager.merge(document);
                                    documentsToSave.add(document);
                                }
                            }
                        }
                    }
                }

                entityManager.merge(customCustomer);
                List<Map<String, Object>> filteredDocuments = new ArrayList<>();
                for (Document document : documentsToSave) {
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

                                documentDetails.put("documentValidity", validityDetails); // Include as nested map
                            }

                            // Generate a file URL for the document
                            String fileUrl = fileService.getFileUrl(document.getFilePath(), request);
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
                            documentDetails.put("fileUrl", fileUrl);
                            filteredDocuments.add(documentDetails);
                        }
                    }
                }
                responseData.put("uploadedDocuments", filteredDocuments);
                return ResponseService.generateSuccessResponse("Documents updated successfully", responseData, HttpStatus.OK);
            } else {
                Set<ServiceProviderDocument> serviceProviderDocumentToSave = new HashSet<>();
                // Service Provider logic
                ServiceProviderEntity serviceProviderEntity = em.find(ServiceProviderEntity.class, customerId);
                if (serviceProviderEntity == null) {
                    return ResponseService.generateErrorResponse("No data found for this serviceProvider", HttpStatus.NOT_FOUND);
                }

                Map<String, Object> responseData = new HashMap<>();
                List<String> deletedDocumentMessages = new ArrayList<>();

                // Handle file uploads and deletions

                for (Map.Entry<Integer, List<MultipartFile>> entry : groupedFiles.entrySet()) {
                    Integer fileNameId = entry.getKey();
                    List<MultipartFile> fileList = entry.getValue();


                    DocumentType documentTypeObj = em.createQuery(
                                    "SELECT dt FROM DocumentType dt WHERE dt.document_type_id = :documentTypeId", DocumentType.class)
                            .setParameter("documentTypeId", fileNameId)
                            .getResultStream()
                            .findFirst()
                            .orElse(null);

                    if (documentTypeObj == null) {
                        return ResponseService.generateErrorResponse("Unknown document type for file: " + fileNameId, HttpStatus.BAD_REQUEST);
                    }

                    if(documentTypeObj.getDocument_type_id().equals(13))
                    {
                        if(otherDocument==null)
                        {
                            throw new IllegalArgumentException("otherDocument name cannot be null for uploading other Documents");
                        }
                        if(otherDocument.trim().isEmpty())
                        {
                            throw new IllegalArgumentException("otherDocument name cannot be empty");
                        }
                    }

                    if (documentTypeObj.getIs_qualification_document().equals(true)) {
                        if (qualificationDetailId == null) {
                            throw new IllegalArgumentException("QualificationDetail id cannot be null for uploading Qualfication Documents");
                        }
                    }
                    if (documentTypeObj.getIs_issue_date_required().equals(true)) {
                        if (dateOfIssue == null) {
                            throw new IllegalArgumentException("Date of issue cannot be null");
                        }
                        if (documentTypeObj.getIs_expiration_date_required().equals(true)) {
                            if (validUpto == null) {
                                throw new IllegalArgumentException("Valid upto (expiration date of document) cannot be null");
                            }
                        }
                    }
                    for (MultipartFile file : fileList) {
                        if (documentTypeObj.getDocument_type_id().equals(3)) {  // If it's a Live Photo
                            processedFile = documentStorageService.convertToJpg(file);
                        }
                        else {
                            documentStorageService.validateDocument(file, documentTypeObj);
                        }
                        ServiceProviderDocument existingDocument = em.createQuery(
                                        "SELECT d FROM ServiceProviderDocument d WHERE d.serviceProviderEntity = :serviceProviderEntity AND d.documentType = :documentType AND d.name IS NOT NULL", ServiceProviderDocument.class)
                                .setParameter("serviceProviderEntity", serviceProviderEntity)
                                .setParameter("documentType", documentTypeObj)

                                .getResultStream()
                                .findFirst()
                                .orElse(null);

                        if(documentTypeObj.getDocument_type_id().equals(3))
                        {
                            fileUploadService.uploadFileOnFileServer(processedFile, documentTypeObj.getDocument_type_name(), customerId.toString(), role);
                        }
                        else {
                            fileUploadService.uploadFileOnFileServer(file, documentTypeObj.getDocument_type_name(), customerId.toString(), role);
                        }

                        if (removeFileTypes != null && removeFileTypes) {
                            if (existingDocument != null && fileNameId != 13) {
                                if (existingDocument != null) {

                                    String filePath = existingDocument.getFilePath();
                                    if (filePath != null) {
                                        fileUploadService.deleteFile(customerId, documentTypeObj.getDocument_type_name(), existingDocument.getName(), role);
                                    }
                                    existingDocument.setDocumentType(null);
                                    existingDocument.setName(null);
                                    existingDocument.setFilePath(null);
                                    existingDocument.setServiceProviderEntity(null);
                                    em.persist(existingDocument);
                                    serviceProviderDocumentToSave.add(existingDocument);
                                    deletedDocumentMessages.add(documentTypeObj.getDocument_type_name() + " has been deleted.");
                                }
                                continue;
                            }
                        }

                        if (fileNameId == 13 && (!file.isEmpty() || file != null)) {
                            String newFileName = file.getOriginalFilename();
                            // Check for existing document with the same name
                            ServiceProviderDocument existingDocument13 = em.createQuery(
                                            "SELECT d FROM ServiceProviderDocument d WHERE d.serviceProviderEntity = :serviceProviderEntity " +
                                                    "AND d.documentType = :documentType " +
                                                    "AND (:otherDocument IS NULL OR LOWER(d.otherDocument) = LOWER(:otherDocument)) " +  // Handle null safely
                                                    "AND d.name = :documentName " +  // Include document name check
                                                    "AND d.name IS NOT NULL",
                                            ServiceProviderDocument.class
                                    )
                                    .setParameter("serviceProviderEntity", serviceProviderEntity)
                                    .setParameter("documentType", documentTypeObj)
                                    .setParameter("otherDocument", otherDocument != null ? otherDocument.toLowerCase() : null)  // Avoid NullPointerException
                                    .setParameter("documentName", newFileName)  // Ensure document name is included
                                    .getResultStream()
                                    .findFirst()
                                    .orElse(null);

                            if (existingDocument13 == null) {
                                ServiceProviderDocument serviceProviderDocument = documentStorageService.createDocumentServiceProvider(file, documentTypeObj, serviceProviderEntity, customerId, role);
                                if(documentTypeObj.getDocument_type_id().equals(13))
                                {
                                    serviceProviderDocument.setOtherDocument(otherDocument);
                                    entityManager.merge(serviceProviderDocument);
                                }
                                serviceProviderDocumentToSave.add(serviceProviderDocument);
                            } else if (existingDocument13 != null) {
                                String filePath = existingDocument13.getFilePath();
                                if (filePath != null) {
                                    String absolutePath = System.getProperty("user.dir") + "/../test/" + filePath;
                                    File oldFile = new File(absolutePath);
                                    String oldFileName = oldFile.getName();
                                    existingDocument13.setIsArchived(false);
                                    if (!newFileName.equals(oldFileName)) {
                                        fileUploadService.deleteFile(customerId, documentTypeObj.getDocument_type_name(), existingDocument13.getName(), role);
                                        documentStorageService.updateOrCreateServiceProvider(existingDocument13, file, documentTypeObj, customerId, role);
                                    }
                                }
                                entityManager.merge(existingDocument13);
                                serviceProviderDocumentToSave.add(existingDocument13);
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
                            if (filePath != null) {

                                String absolutePath = System.getProperty("user.dir") + "/../test/" + filePath;
                                File oldFile = new File(absolutePath);
                                String oldFileName = oldFile.getName();
                                String newFileName = file.getOriginalFilename();
                                existingDocument.setIsArchived(false);
                                if (!newFileName.equals(oldFileName)) {
//                                    oldFile.delete();
                                    fileUploadService.deleteFile(customerId, documentTypeObj.getDocument_type_name(), existingDocument.getName(), role);

                                    if(documentTypeObj.getDocument_type_id().equals(3))
                                    {
                                        documentStorageService.updateOrCreateServiceProvider(existingDocument, processedFile, documentTypeObj, customerId, role);
                                    }
                                    else {
                                        documentStorageService.updateOrCreateServiceProvider(existingDocument, file, documentTypeObj, customerId, role);
                                    }
                                }
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

                            // Generate a file URL for the document
                            String fileUrl = fileService.getFileUrl(document.getFilePath(), request);
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
                responseData.put("uploadedDocuments", filteredDocuments);
                return ResponseService.generateSuccessResponse("Documents uploaded successfully", responseData, HttpStatus.OK);
            }

        } catch (DataIntegrityViolationException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Document with the same name and file path already exists." + e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error updating documents: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    /*@Authorize(value = {Constant.roleUser})*/
    @RequestMapping(value = "update-username", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerUsername(@RequestBody Map<String, Object> updates, @RequestParam Long customerId, @RequestHeader(value = "Authorization") String authHeader,HttpServletRequest httpServletRequest) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role=roleService.getRoleByRoleId(roleId);

            //checking for super admin and admin
            if((role.getRole_name().equals(roleUser)&& !Objects.equals(tokenUserId, customerId))||role.getRole_name().equals(roleServiceProvider))

                return ResponseService.generateErrorResponse("Forbidden",HttpStatus.FORBIDDEN);
            updates = sanitizerService.sanitizeInputMap(updates);

            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            String username = (String) updates.get("username");
            if (username != null)
                username = username.trim();

            if (username.isEmpty() || username.contains(" ")) {
                return ResponseService.generateErrorResponse("Invalid username", HttpStatus.NOT_FOUND);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);

            }
            Customer existingCustomerByUsername = null;
            existingCustomerByUsername = customerService.readCustomerByUsername(username);

            if ((existingCustomerByUsername != null) && !existingCustomerByUsername.getId().equals(customerId)) {
                return ResponseService.generateErrorResponse("Username is not available", HttpStatus.BAD_REQUEST);

            } else {
                if (customer.getUsername() != null && customer.getUsername().equals(username))
                    return ResponseService.generateErrorResponse("Old and new username cannot be same", HttpStatus.BAD_REQUEST);
                customer.setUsername(username);
                em.merge(customer);
                return ResponseService.generateSuccessResponse("User name  updated successfully : ", sharedUtilityService.breakReferenceForCustomer(customer, authHeader,httpServletRequest), HttpStatus.OK);

            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Error updating username", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @Authorize(value = {Constant.roleUser, roleAdmin, roleSuperAdmin})
    @RequestMapping(value = "create-or-update-password", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerPassword(@RequestBody Map<String, Object> details, @RequestParam Long customerId,@RequestHeader(value = "Authorization") String authHeader,HttpServletRequest httpServletRequest) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role=roleService.getRoleByRoleId(roleId);

            //checking for super admin and admin
            if((role.getRole_name().equals(roleUser)&& !Objects.equals(tokenUserId, customerId))||role.getRole_name().equals(roleServiceProvider))
                return ResponseService.generateErrorResponse("Forbidden",HttpStatus.FORBIDDEN);
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            //details=sanitizerService.sanitizeInputMap(details);

            String password = (String) details.get("password");
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);
            }
            if (password != null && !(password.isEmpty())) {
                if (customer.getPassword() == null || customer.getPassword().isEmpty()) {
                    customer.setPassword(passwordEncoder.encode(password));
                    em.merge(customer);
                    return ResponseService.generateSuccessResponse("Password Created", sharedUtilityService.breakReferenceForCustomer(customer, authHeader,httpServletRequest), HttpStatus.OK);
                }
                if (!passwordEncoder.matches(password, customer.getPassword())) {

                    customer.setPassword(passwordEncoder.encode(password));
                    em.merge(customer);
                    return ResponseService.generateSuccessResponse("Password Updated", sharedUtilityService.breakReferenceForCustomer(customer, authHeader,httpServletRequest), HttpStatus.OK);
                } else {
                    return ResponseService.generateErrorResponse("Old Password and new Password cannot be same", HttpStatus.BAD_REQUEST);
                }
            } else {
                return ResponseService.generateErrorResponse("Empty Password", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Error updating password", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @Authorize(value = {Constant.roleUser, Constant.roleSuperAdmin, Constant.roleAdminServiceProvider})
    @RequestMapping(value = "delete", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteCustomer(@RequestParam String customerId) {
        try {
            Long id = Long.valueOf(customerId);
            if (customerService == null) {
                return ResponseService.generateErrorResponse(ApiConstants.CUSTOMER_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Customer customer = customerService.readCustomerById(id);
            if (customer != null) {
                customerService.deleteCustomer(customer);
                return ResponseService.generateSuccessResponse("Record Deleted Successfully", "", HttpStatus.OK);
            } else {
                return ResponseService.generateErrorResponse("No Records found for this ID " + id, HttpStatus.NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in deleting customer: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @Authorize(value = {Constant.roleUser})
    @RequestMapping(value = "add-address", method = RequestMethod.POST)
    public ResponseEntity<?> addAddress(@RequestParam Long customerId, @RequestBody Map<String, Object> addressDetails) {
        try {
            Long id = Long.valueOf(customerId);
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Customer customer = customerService.readCustomerById(id);
            if (customer != null) {
                CustomerAddress newAddress = customerAddressService.create();
                Address address = addressService.create();
                address.setAddressLine1((String) addressDetails.get("address"));
                address.setCity((String) addressDetails.get("city"));
                address.setStateProvinceRegion((String) addressDetails.get("state"));
                address.setCounty((String) addressDetails.get("district"));
                address.setPostalCode((String) addressDetails.get("pinCode"));
                newAddress.setAddress(address);
                newAddress.setCustomer(customer);
                String addressName = (String) addressDetails.get("addressName");
                newAddress.setAddressName(addressName);
                CountryImpl country = null;
                if (addressName.equals("CURRENT_ADDRESS"))
                    country = (CountryImpl) countryService.findCountryByAbbreviation("ADD-C");
                else if (addressName.equals("PERMANENT_ADDRESS"))
                    country = (CountryImpl) countryService.findCountryByAbbreviation("ADD-P");
                newAddress.getAddress().setCountry(country);
                List<CustomerAddress> addressLists = customer.getCustomerAddresses();
                addressLists.add(newAddress);
                customer.setCustomerAddresses(addressLists);
                if (!addressDetails.containsKey("inFunctionCall"))
                    em.merge(customer);
                addressDetails.remove("inFunctionCall");
                //using reflections
                AddressDTO addressDTO = new AddressDTO();
                for (Map.Entry<String, Object> entry : addressDetails.entrySet()) {
                    try {
                        Field field = AddressDTO.class.getDeclaredField(entry.getKey());
                        field.setAccessible(true);
                        field.set(addressDTO, entry.getValue());
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        exceptionHandling.handleException(e);
                    }
                }
                addressDTO.setDistrict(address.getCounty());
                addressDTO.setCustomerId(newAddress.getCustomer().getId());
                CustomCustomer customCustomer = em.find(CustomCustomer.class, newAddress.getCustomer().getId());
                if (customCustomer == null) {
                    return ResponseService.generateErrorResponse("Error saving address", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                addressDTO.setPhoneNumber(customCustomer.getMobileNumber());
                return ResponseService.generateSuccessResponse("Address added successfully : ", addressDTO, HttpStatus.OK);


            } else {
                return ResponseService.generateErrorResponse("No Records found for this ID", HttpStatus.NOT_FOUND);

            }
        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error saving Address", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @RequestMapping(value = "retrieve-address", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveAddressList(@RequestParam Long customerId) {
        try {
            Long customerID = Long.valueOf(customerId);
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            Customer customer = customerService.readCustomerById(customerID);
            if (customer != null) {
                List<CustomerAddress> addressList = customer.getCustomerAddresses();
                List<AddressDTO> listOfAddresses = new ArrayList<>();
                for (CustomerAddress customerAddress : addressList) {
                    AddressDTO addressDTO = makeAddressDTO(customerAddress);
                    listOfAddresses.add(addressDTO);
                }
                return ResponseService.generateSuccessResponse("Addresses details : ", listOfAddresses, HttpStatus.OK);
            } else {
                return ResponseService.generateErrorResponse("No data found for this customerId", HttpStatus.INTERNAL_SERVER_ERROR);

            }


        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error in retreiving Address", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @RequestMapping(value = "address-details", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveAddressList(@RequestParam Long customerId, @RequestParam Long addressId, @RequestHeader(value = "Authorization") String authHeader) {
        try {
            Long customerID = Long.valueOf(customerId);
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            Customer customer = customerService.readCustomerById(customerID);
            CustomerAddress customerAddress = customerAddressService.readCustomerAddressById(addressId);
            if (customerAddress == null) {
                return ResponseService.generateErrorResponse("Address not found", HttpStatus.NOT_FOUND);
            } else {
                return ResponseService.generateSuccessResponse("Address details : ", makeAddressDTO(customerAddress), HttpStatus.OK);

            }
        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error saving Address", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    public AddressDTO makeAddressDTO(CustomerAddress customerAddress) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressId(customerAddress.getAddress().getId());
        addressDTO.setAddress(customerAddress.getAddress().getAddressLine1());
        addressDTO.setPinCode(customerAddress.getAddress().getPostalCode());
        addressDTO.setState(customerAddress.getAddress().getStateProvinceRegion());
        addressDTO.setCity(customerAddress.getAddress().getCity());
        addressDTO.setCustomerId(customerAddress.getCustomer().getId());
        addressDTO.setAddressName(customerAddress.getAddressName());
        CustomCustomer customCustomer = em.find(CustomCustomer.class, customerAddress.getCustomer().getId());
        addressDTO.setPhoneNumber(customCustomer.getMobileNumber());
        return addressDTO;
    }

    public ResponseEntity<?> createAuthResponse(String token, Customer customer, String authHeader,HttpServletRequest httpServletRequest) throws Exception {
        OtpEndpoint.ApiResponse authResponse = new OtpEndpoint.ApiResponse(token, sharedUtilityService.breakReferenceForCustomer(customer, authHeader,httpServletRequest), HttpStatus.OK.value(), HttpStatus.OK.name(), "User has been logged in");
        return ResponseService.generateSuccessResponse("Token details : ", authResponse, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(Constant.BEARER_CONST)) {
            return ResponseEntity.badRequest().body("Token is required");
        }

        String token = authorizationHeader.substring(7);
        try {
            jwtUtil.logoutUser(token);
            return responseService.generateSuccessResponse("Logged out successfully", null, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during logout");
        }
    }

    @Transactional
    @Authorize(value = {Constant.roleUser})
    @PostMapping("/save-form/{customer_id}")
    public ResponseEntity<?> saveForm(@PathVariable long customer_id, @RequestParam long product_id) {
        try {
            Long id = Long.valueOf(customer_id);

            CustomCustomer customer = entityManager.find(CustomCustomer.class, id);
            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            }
            CustomProduct product = entityManager.find(CustomProduct.class, product_id);
            if (product == null) {
                return ResponseService.generateErrorResponse(Constant.PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }
            List<CustomProduct> savedForms = customer.getSavedForms();
            if ((((Status) product).getArchived() == 'Y' || !product.getDefaultSku().getActiveEndDate().after(new Date()))) {
                return ResponseService.generateErrorResponse("Cannot save an archived product", HttpStatus.BAD_REQUEST);
            }
            if (savedForms.contains(product))
                return ResponseService.generateErrorResponse("You can save a form only once", HttpStatus.UNPROCESSABLE_ENTITY);
            savedForms.add(product);
            customer.setSavedForms(savedForms);
            entityManager.merge(customer);
            Map<String, Object> responseBody = new HashMap<>();
            /* Map<String,Object>formBody=sharedUtilityService.createProductResponseMap(product,null,customer);*/
            CustomProductWrapper customProductWrapper = new CustomProductWrapper();
            /*List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(product_id);
            List<PhysicalRequirementDto> physicalRequirementDtoList = physicalRequirementDtoService.getPhysicalRequirementDto(product_id);
            List< ReserveCategoryAgeDto> ageRequirement = reserveCategoryAgeService.getReserveCategoryDto(product.getId());*/
            customProductWrapper.wrapDetails(product, null, null, reserveCategoryFeePostRefService);
            return ResponseService.generateSuccessResponse("Form Saved", customProductWrapper, HttpStatus.OK);
        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error saving Form : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @Authorize(value = {Constant.roleUser})
    @DeleteMapping("/unsave-form/{customer_id}")
    public ResponseEntity<?> unSaveForm(@PathVariable long customer_id, @RequestParam long product_id) {
        try {
            CustomCustomer customer = entityManager.find(CustomCustomer.class, customer_id);
            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            }
            CustomProduct product = entityManager.find(CustomProduct.class, product_id);
            if (product == null) {
                return ResponseService.generateErrorResponse(Constant.PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }
            List<CustomProduct> savedForms = customer.getSavedForms();
            if (savedForms.contains(product))
                savedForms.remove(product);
            else
                return ResponseService.generateErrorResponse("Form not present in saved Form list", HttpStatus.UNPROCESSABLE_ENTITY);
            customer.setSavedForms(savedForms);
            entityManager.merge(customer);
            CustomProductWrapper customProductWrapper = new CustomProductWrapper();
           /* List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(product_id);
            List<PhysicalRequirementDto> physicalRequirementDtoList = physicalRequirementDtoService.getPhysicalRequirementDto(product_id);
            List< ReserveCategoryAgeDto> ageRequirement = reserveCategoryAgeService.getReserveCategoryDto(product.getId());*/
            customProductWrapper.wrapDetails(product, null, null, reserveCategoryFeePostRefService);
            return ResponseService.generateSuccessResponse("Form Removed", customProductWrapper, HttpStatus.OK);
        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error removing Form : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/forms/show-saved-forms")
    public ResponseEntity<?> getSavedForms(HttpServletRequest request,
                                           @RequestParam long customer_id,
                                           @RequestParam(value = "offset", defaultValue = "0") int offset,
                                           @RequestParam(value = "limit", defaultValue = "10") int limit,@RequestHeader(value = "Authorization")String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role=roleService.getRoleByRoleId(roleId);

            //checking for super admin and admin
            if((role.getRole_name().equals(roleUser)&& !Objects.equals(tokenUserId, customer_id))||role.getRole_name().equals(roleServiceProvider))
                return ResponseService.generateErrorResponse("Forbidden",HttpStatus.FORBIDDEN);
            if (offset < 0) {
                throw new IllegalArgumentException("Offset for pagination cannot be a negative number");
            }
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit for pagination cannot be a negative number or 0");
            }

            CustomCustomer customer = entityManager.find(CustomCustomer.class, customer_id);
            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer with this ID not found", HttpStatus.NOT_FOUND);
            }

            List<CustomProductWrapper> listOfSavedProducts = new ArrayList<>();

            for (Product product : customer.getSavedForms()) {
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());
                if (customProduct != null && ((Status) customProduct).getArchived() == 'Y') {
                    continue;
                }

                CustomProductWrapper customProductWrapper = new CustomProductWrapper();
                customProductWrapper.wrapDetails(customProduct, null, null, reserveCategoryFeePostRefService);
                listOfSavedProducts.add(customProductWrapper);
            }

            // Calculate pagination details
            int totalItems = listOfSavedProducts.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int currentPage = offset;

            if (offset >= totalPages && offset != 0) {
                return ResponseService.generateErrorResponse("No more saved forms available", HttpStatus.BAD_REQUEST);
            }

            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);

            List<CustomProductWrapper> paginatedList = (fromIndex < totalItems)
                    ? listOfSavedProducts.subList(fromIndex, toIndex)
                    : Collections.emptyList(); // Return empty list if offset exceeds total items

            // Response Map
            Map<String, Object> response = new HashMap<>();
            response.put("forms", paginatedList);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", currentPage);

            return ResponseService.generateSuccessResponse(
                    paginatedList.isEmpty() ? "Saved forms list is empty" : "Forms retrieved successfully",
                    response,
                    HttpStatus.OK
            );

        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>("SOME EXCEPTION OCCURRED: " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/forms/show-filled-forms")
    public ResponseEntity<?> getFilledFormsByUserId(HttpServletRequest request,
                                                    @RequestParam long customer_id,
                                                    @RequestParam(value = "offset", defaultValue = "0") int offset,
                                                    @RequestParam(value = "limit", defaultValue = "10") int limit,@RequestHeader(value = "Authorization")String authHeader) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role=roleService.getRoleByRoleId(roleId);

            //checking for super admin and admin
            if((role.getRole_name().equals(roleUser)&& !Objects.equals(tokenUserId, customer_id))||role.getRole_name().equals(roleServiceProvider))
                return ResponseService.generateErrorResponse("Forbidden",HttpStatus.FORBIDDEN);
            // Validate pagination parameters
            if (offset < 0) throw new IllegalArgumentException("Offset for pagination cannot be a negative number");
            if (limit <= 0) throw new IllegalArgumentException("Limit for pagination cannot be zero or negative");

            // Find customer
            CustomCustomer customer = entityManager.find(CustomCustomer.class, customer_id);
            if (customer == null)
                return ResponseService.generateErrorResponse("Customer with this id not found", HttpStatus.NOT_FOUND);

            // Prepare list of saved products
            List<CustomProductWrapper> listOfSavedProducts = customer.getSavedForms().stream()
                    .map(product -> entityManager.find(CustomProduct.class, product.getId()))
                    .filter(customProduct -> customProduct != null && ((Status) customProduct).getArchived() != 'Y')
                    .map(customProduct -> {
                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct, null, null, reserveCategoryFeePostRefService);
                        return wrapper;
                    })
                    .collect(Collectors.toList());

            // Pagination details
            int totalItems = listOfSavedProducts.size();
            int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / limit);

            if (offset >= totalPages && offset != 0)
                return ResponseService.generateErrorResponse("No more filled forms available", HttpStatus.BAD_REQUEST);

            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);

            List<CustomProductWrapper> paginatedList = totalItems == 0 ? Collections.emptyList()
                    : listOfSavedProducts.subList(fromIndex, toIndex);

            // Prepare response
            Map<String, Object> response = Map.of(
                    "forms", paginatedList,
                    "totalItems", totalItems,
                    "totalPages", totalPages,
                    "currentPage", offset
            );

            return ResponseService.generateSuccessResponse("Forms filled", response, HttpStatus.OK);

        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            exceptionHandlingService.handleException(e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            return new ResponseEntity<>("SOME EXCEPTION OCCURRED: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "/forms/show-recommended-forms")
    public ResponseEntity<?> getRecommendedFormsByUserId(HttpServletRequest request,
                                                         @RequestParam long customer_id,
                                                         @RequestParam(value = "offset", defaultValue = "0") int offset,
                                                         @RequestParam(value = "limit", defaultValue = "10") int limit,@RequestHeader(value = "Authorization")String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role=roleService.getRoleByRoleId(roleId);

            //checking for super admin and admin
            if((role.getRole_name().equals(roleUser)&& !Objects.equals(tokenUserId, customer_id))||role.getRole_name().equals(roleServiceProvider))
                return ResponseService.generateErrorResponse("Forbidden",HttpStatus.FORBIDDEN);

            if (offset < 0) {
                throw new IllegalArgumentException("Offset for pagination cannot be a negative number");
            }
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit for pagination cannot be a negative number or 0");
            }

            CustomCustomer customer = entityManager.find(CustomCustomer.class, customer_id);
            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer with this id not found", HttpStatus.NOT_FOUND);
            }

            List<CustomProductWrapper> listOfSavedProducts = new ArrayList<>();

            for (Product product : customer.getSavedForms()) {
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());
                if (customProduct != null && ((Status) customProduct).getArchived() == 'Y') {
                    continue;
                }

                CustomProductWrapper customProductWrapper = new CustomProductWrapper();
                List<Post> postList = customProduct.getPosts();
                customProductWrapper.wrapDetails(customProduct, postList, null, reserveCategoryFeePostRefService);
                listOfSavedProducts.add(customProductWrapper);
            }

            // Calculate pagination details
            int totalItems = listOfSavedProducts.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int currentPage = offset;

            if (totalItems == 0 && offset==0) {
                // Return 200 with an empty list if there are no forms
                Map<String, Object> response = new HashMap<>();
                response.put("forms", Collections.emptyList());
                response.put("totalItems", 0);
                response.put("totalPages", 0);
                response.put("currentPage", currentPage);
                return ResponseService.generateSuccessResponse("No recommended forms available", response, HttpStatus.OK);
            }

            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);

            if (offset >= totalPages&& offset != 0) {
                return ResponseService.generateErrorResponse("No more recommended forms available", HttpStatus.BAD_REQUEST);
            }

            List<CustomProductWrapper> paginatedList = listOfSavedProducts.subList(fromIndex, toIndex);

            // Create response with pagination info
            Map<String, Object> response = new HashMap<>();
            response.put("forms", paginatedList);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", currentPage);

            return ResponseService.generateSuccessResponse("Recommended Forms", response, HttpStatus.OK);

        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>("SOME EXCEPTION OCCURRED: " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/submit-customer-details/{customerId}")
    public ResponseEntity<?> submitCustomerDetails( @PathVariable Long customerId, @RequestHeader(value = "Authorization") String authHeader,HttpServletRequest httpServletRequest)
    {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role=roleService.getRoleByRoleId(roleId);

            //checking for super admin and admin
            if((role.getRole_name().equals(roleUser)&& !Objects.equals(tokenUserId, customerId))||role.getRole_name().equals(roleServiceProvider))
                return ResponseService.generateErrorResponse("Forbidden",HttpStatus.FORBIDDEN);
            CustomCustomer customCustomer= entityManager.find(CustomCustomer.class,customerId);
            if(customCustomer==null)
            {
                throw new IllegalArgumentException("Customer with id "+ customerId+ " not found");
            }
            if(!sharedUtilityService.validateCustomerPersonalDetails(customCustomer));
            {
                customCustomer.setProfileComplete(false);
            }
            if(!sharedUtilityService.validateCustomerContactDetails(customCustomer));
            {
                customCustomer.setProfileComplete(false);
            }
            if(!sharedUtilityService.validatePhysicalDetails(customCustomer));
            {
                customCustomer.setProfileComplete(false);
            }
            if(!sharedUtilityService.validateMiscellaniousDetails(customCustomer));
            {
                customCustomer.setProfileComplete(false);
            }
            if(!sharedUtilityService.validateDocumentsDetails(customCustomer));
            {
                customCustomer.setProfileComplete(false);
            }
            customCustomer.setProfileComplete(true);
            return ResponseService.generateSuccessResponse("User details submitted successfully", sharedUtilityService.breakReferenceForCustomer(customCustomer, authHeader, httpServletRequest), HttpStatus.OK);
        }
        catch (NumberFormatException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        }catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in deleting customer: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-customers")
    @Authorize(value = {Constant.roleServiceProvider, Constant.roleAdmin, Constant.roleSuperAdmin, Constant.roleServiceProviderAdmin})
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader(value = "Authorization") String authHeader,HttpServletRequest httpServletRequest) {
        try {
            if(offset<0)
            {
                throw new IllegalArgumentException("Offset for pagination cannot be a negative number");
            }
            if(limit<=0)
            {
                throw new IllegalArgumentException("Limit for pagination cannot be a negative number or 0");
            }
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();
            int startPosition = offset * limit;

            // **Get total number of customers (without pagination)**
            TypedQuery<Long> countQuery = entityManager.createQuery(
                    "SELECT COUNT(c) FROM CustomCustomer c WHERE c.archived = false", Long.class);
            Long totalItems = countQuery.getSingleResult();  // Total count of active customers

            // **Fetch paginated customers**
            TypedQuery<CustomCustomer> query = entityManager.createQuery(
                    "SELECT c FROM CustomCustomer c WHERE c.archived = false", CustomCustomer.class);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);
            List<CustomCustomer> customers = query.getResultList();

            // Convert customers to response format
            List<Map> results = customers.stream()
                    .map(customer -> {
                        try {
                            return sharedUtilityService.breakReferenceForCustomer(
                                    customerService.readCustomerById(customer.getId()), authHeader, httpServletRequest);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            // **Calculate total pages correctly**
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            if (offset >= totalPages&& offset != 0) {
                throw new IllegalArgumentException("No more customers available");
            }

            // **Prepare the response map**
            Map<String, Object> response = new HashMap<>();
            response.put("customers", results);
            response.put("totalItems", totalItems); // Total number of customers (entire dataset)
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            // **Return success response**
            return ResponseService.generateSuccessResponse("CUSTOMERS RETRIEVED SUCCESSFULLY", response, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @PostMapping("/set-referrer/{customer_id}/{service_provider_id}")
    public ResponseEntity<?> setReferrerForCustomer(@PathVariable Long customer_id, @PathVariable Long service_provider_id,@RequestHeader(value = "Authorization")String authHeader) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role=roleService.getRoleByRoleId(roleId);

            //checking for super admin and admin
            if((role.getRole_name().equals(roleUser)&& !Objects.equals(tokenUserId, customer_id))||role.getRole_name().equals(roleServiceProvider))
                return ResponseService.generateErrorResponse("Forbidden",HttpStatus.FORBIDDEN);

            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer_id);
            if (customCustomer == null)
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            if (customCustomer.getArchived().equals(true)) {
                return ResponseService.generateErrorResponse("Your account is suspended. Please contact support.", HttpStatus.FORBIDDEN);
            }
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, service_provider_id);
            if (serviceProvider == null)
                return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.NOT_FOUND);
            List<CustomerReferrer> referrerSp = customCustomer.getMyReferrer();
            CustomerReferrer primaryRef = null;
            for (CustomerReferrer customerReferrer : referrerSp) {
                if (customerReferrer.getPrimaryRef() != null && customerReferrer.getPrimaryRef()) {
                    primaryRef = customerReferrer;
                }
                if (customerReferrer.getServiceProvider().getService_provider_id().equals(service_provider_id))
                    return ResponseService.generateErrorResponse("Selected Service Provider already set as Referrer", HttpStatus.BAD_REQUEST);
            }
            if (!referrerSp.isEmpty() && primaryRef != null) {
                primaryRef.setPrimaryRef(false);
                entityManager.merge(primaryRef);
            }
            if (customCustomer.getPrimaryRef() == 0 || (customCustomer.getRegisteredBySp() && customCustomer.getCreatedByRole() != 4) || (customCustomer.getCreatedByRole()) == 5) {
                customCustomer.setPrimaryRef(service_provider_id);
            }
            CustomerReferrer customerReferrer = new CustomerReferrer();
            customerReferrer.setPrimaryRef(true); // by raman and Kshitij will solve the complete issue of last referrer as primary referee.;
            customerReferrer.setCustomer(customCustomer);
            customerReferrer.setServiceProvider(serviceProvider);
            customCustomer.getMyReferrer().add(customerReferrer);
            customerReferrer.setCreatedAt(LocalDateTime.now());
            entityManager.persist(customerReferrer);

            entityManager.merge(customCustomer);
            return ResponseService.generateSuccessResponse("Referrer Set", sharedUtilityService.serviceProviderDetailsMap(serviceProvider), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error setting customer's referrer " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public QualificationDetails findQualificationDetailForCustomer(Long qualificationDetailId, CustomCustomer customCustomer) {
        List<QualificationDetails> qualificationDetails = customCustomer.getQualificationDetailsList();
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

    private boolean isValidDateFormat(String dateStr, SimpleDateFormat dateFormat) {
        try {
            dateFormat.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    @Transactional
    @PostMapping("/create-user")
    public ResponseEntity<?> createUser() {
        CustomCustomer customCustomer = new CustomCustomer();
        customCustomer.setId(customerService.findNextCustomerId());
        entityManager.persist(customCustomer);
        Long id = customCustomer.getId();
        return ResponseService.generateSuccessResponse("User created successfully", customCustomer, HttpStatus.CREATED);
    }

    @Authorize(value = {Constant.roleAdmin, Constant.roleAdminServiceProvider, Constant.roleSuperAdmin, Constant.roleServiceProvider})
    @GetMapping("/filter")
    @Transactional
    public ResponseEntity<?> filterCustomer(@RequestParam(required = false) List<String> name, @RequestParam(required = false) List<Long> ref, @RequestParam(required = false) List<Integer> stateId, @RequestParam(required = false) List<Integer> districtId, @RequestParam(required = false) List<Integer> qualificationType, @RequestParam(required = false) String username, @RequestParam(required = false) Boolean completed,@RequestParam(required = false,defaultValue = "false")Boolean suspended, @RequestHeader(value = "Authorization") String authHeader, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit, @RequestParam(required = false, defaultValue = "ASC") String sort) throws Exception {
        /* try {*/
        if (!sort.equals("DESC") && !sort.equals("ASC"))
            return ResponseService.generateErrorResponse("Invalid sort filter", HttpStatus.BAD_REQUEST);
        List<Long> refereeId=null;
        if(ref!=null)
            refereeId= ref;
        else
            refereeId=new ArrayList<>();
        String jwtToken = authHeader.substring(7);
        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
        Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
        if (roleService.getRoleByRoleId(roleId).getRole_name().equals(Constant.roleServiceProvider)) {
            if (refereeId.isEmpty())
                refereeId.add(tokenUserId);
            else if (ref != null)
                return ResponseService.generateErrorResponse("Invalid search filter selected", HttpStatus.BAD_REQUEST);
        }
/*            if(name!=null&&!sharedUtilityService.isAlphabetic(name))
                return ResponseService.generateErrorResponse("Invalid name",HttpStatus.BAD_REQUEST);*/
        String stateName = null, districtName = null, qualificationName = null, firstName = null, lastName = null;
        String[] names = null;
        List<String>stateNames=new ArrayList<>();
        List<String>districtNames=new ArrayList<>();
        List<Long>qualificationNames=new ArrayList<>();
        List<String >qualificationStrings=new ArrayList<>();
        if (stateId != null) {
            for(Integer stateCode:stateId) {
                stateName = districtService.findStateById(stateCode);
                if (stateName == null)
                    return ResponseService.generateErrorResponse("Invalid state Id", HttpStatus.BAD_REQUEST);
                stateNames.add(stateName);
            }
        }
        else
            stateNames=null;
        if (districtId != null) {
            for (Integer district : districtId) {
                districtName = districtService.findDistrictById(district);
                if (districtName == null)
                    return ResponseService.generateErrorResponse("Invalid district Id", HttpStatus.BAD_REQUEST);
                districtNames.add(districtName);
            }
        }
        else
            districtNames=null;
        if (qualificationType != null) {
            for (Integer id : qualificationType) {
                if (qualificationService.getQualificationByQualificationId(id) == null)
                    return ResponseService.generateErrorResponse("Invalid qualification Id", HttpStatus.BAD_REQUEST);
                qualificationStrings.add(qualificationService.getQualificationByQualificationId(id).getQualification_name());
                qualificationNames.add(qualificationService.getQualificationByQualificationId(id).getOverlap());
            }
        }
        else
            qualificationNames=null;

        List<String> firstNames = new ArrayList<>();
        List<String> lastNames = new ArrayList<>();
        if (name != null && !name.isEmpty()) {
            for (String singleName : name) {
                String[] FilterNames = sharedUtilityService.separateName(singleName.trim());
                if (FilterNames[0] != null)
                    firstNames.add(FilterNames[0]);
                if (FilterNames[1] != null)
                    lastNames.add(FilterNames[1]);
            }

        }


        List<Long> refids=new ArrayList<>();
        if(refereeId!=null&&!refereeId.isEmpty()) {
            // Convert the list of Long to a list of String using Java Streams
            refids =refereeId.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }
        if(refids.isEmpty())
            refids=null;

        List<BigInteger> resultSet1 = customCustomerService.filterCustomer(refids, firstNames, lastNames, stateNames, districtNames, qualificationNames, username, completed, authHeader, offset, limit, sort);
        List<BigInteger> resultSet2 = customCustomerService.filterCustomer(refids, lastNames, firstNames, stateNames, districtNames, qualificationNames, username, completed, authHeader, offset, limit, sort);
        Set<BigInteger> uniqueResults = new HashSet<>();

// Add all elements from both result sets
        uniqueResults.addAll(resultSet1);
        uniqueResults.addAll(resultSet2);
        List<BigInteger> uniqueResultList = new ArrayList<>(uniqueResults);
        System.out.println("count:"+uniqueResultList.size());
// Convert the Set back to a List
        List<CustomerBasicDetailsDto> customerList = new ArrayList<>();
        Map<Integer, Integer> Qualificationorder = new HashMap<>();
        Qualificationorder.put(1, 1);
        Qualificationorder.put(2, 2);
        Qualificationorder.put(6, 3);
        Qualificationorder.put(7, 4);
        Qualificationorder.put(3, 5);
        Qualificationorder.put(4, 6);
        Qualificationorder.put(5, 7);
        Qualificationorder.put(8, 8);
        for (BigInteger id : uniqueResultList) {
            System.out.println("fetchedId: by the querry "+id);
            Customer customer = null;
            try {
                customer = customerService.readCustomerById(id.longValue());
            } catch (Exception e) {
                System.out.println("ID skipped"+id+"due to"+e);
                continue;
            }
            if (customer != null) {
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, id.longValue());
                CustomerBasicDetailsDto customerBasicDetailsDto = new CustomerBasicDetailsDto();
                String state = null;
                String primaryRefName = "N/A";
                Long primaryRefId = null;
                if (customCustomer != null) {
                    if(!customCustomer.getArchived().equals(suspended))
                        continue;

                    if (stateNames != null) {
//                            for (CustomerAddress customerAddress : customer.getCustomerAddresses())
//                                state = customerAddress.getAddress().getStateProvinceRegion();
//                        customerBasicDetailsDto.setState(state);
//                    }
//                        else {
//
//                            for (CustomerAddress customerAddress : customer.getCustomerAddresses()) {
//                                if (customerAddress.getAddressName().equals("PERMANENT_ADDRESS") ||customerAddress.getAddressName().equals("CURRENT_ADDRESS"))
//                                    state = customerAddress.getAddress().getStateProvinceRegion();
//                            }
//                            customerBasicDetailsDto.setState(state);
//                        }

                        String matchingState = null;

                        // 1. Get all valid customer states (PERMANENT/CURRENT addresses)
                        List<String> customerStates = new ArrayList<>();
                        if (customer.getCustomerAddresses() != null) {
                            for (CustomerAddress address : customer.getCustomerAddresses()) {
                                if (address == null || address.getAddress() == null) continue;

                                if ("PERMANENT_ADDRESS".equals(address.getAddressName()) ||
                                        "CURRENT_ADDRESS".equals(address.getAddressName())) {
                                    state = address.getAddress().getStateProvinceRegion();
                                    if (state != null) {
                                        customerStates.add(state);
                                    }
                                }
                            }
                        }

                        // 2. Find first match with stateNames (if stateId was provided)
                        if (!stateNames.isEmpty()) {
                            for (String customerState : customerStates) {
                                if (stateNames.contains(customerState)) {
                                    matchingState = customerState;
                                    break;
                                }
                            }

                            customerBasicDetailsDto.setState(matchingState);

                        }
                    }
                    else {

                            for (CustomerAddress customerAddress : customer.getCustomerAddresses()) {
                                if (customerAddress.getAddressName().equals("PERMANENT_ADDRESS") ||customerAddress.getAddressName().equals("CURRENT_ADDRESS"))
                                    state = customerAddress.getAddress().getStateProvinceRegion();
                            }
                            customerBasicDetailsDto.setState(state);
                        }
                    customerBasicDetailsDto.setCustomerId(id.longValue());
                    customerBasicDetailsDto.setEmail(customer.getEmailAddress());
                    customerBasicDetailsDto.setFullName(customer.getFirstName() + " " + customer.getLastName());
                    customerBasicDetailsDto.setGender(customCustomer.getGender());
                    customerBasicDetailsDto.setUsername(customer.getUsername());

                        /*if (ref != null) {
                            System.out.println(customCustomer.getId()+","+customCustomer.getPrimaryRef());
                            if (customCustomer.getPrimaryRef() != 0 && ref.contains(customCustomer.getPrimaryRef())) {
                                System.out.println("true"+customCustomer.getId());
                                ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, customCustomer.getPrimaryRef());
                                if (serviceProvider != null) {
                                    primaryRefName = serviceProvider.getFirst_name() + " " + serviceProvider.getLast_name();
                                    primaryRefId = serviceProvider.getService_provider_id();
                                }
                            } else
                                continue;
                        }*/
                    if (customCustomer.getPrimaryRef() != 0) {
                        System.out.println("true"+customCustomer.getId());
                        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, customCustomer.getPrimaryRef());
                        if (serviceProvider != null) {
                            primaryRefName = serviceProvider.getFirst_name() + " " + serviceProvider.getLast_name();
                            primaryRefId = serviceProvider.getService_provider_id();
                        }
                    }



                    Integer age = sharedUtilityServiceApi.calculateAge(customCustomer.getDob());
                    if (age != -1)
                        customerBasicDetailsDto.setAge(age);
                    List<QualificationDetails> qualifications = customCustomer.getQualificationDetailsList();
                    int max = 0;
                    if (!qualifications.isEmpty()) {
                        for (QualificationDetails qualificationDetails : qualifications) {
                            System.out.println("kk"+qualificationDetails.getQualification_id());
                            Qualification qualificationFound=entityManager.find(Qualification.class,qualificationDetails.getQualification_id());
                            if (Qualificationorder.get(qualificationFound.getOverlap().intValue()) > max) {
                                customerBasicDetailsDto.setHighestQualification(qualificationService.getQualificationByQualificationId(qualificationDetails.getQualification_id()).getQualification_name());
                                max = Qualificationorder.get(qualificationFound.getOverlap().intValue());
                            }
                        }
                        if (qualificationType != null && max != 0 && !qualificationStrings.contains(customerBasicDetailsDto.getHighestQualification())) {
                            continue;
                        }
                        if (max == 0)
                            customerBasicDetailsDto.setHighestQualification(null);
                    }
                    customerBasicDetailsDto.setPrimaryRef(primaryRefName);
                    customerBasicDetailsDto.setPrimaryRefId(primaryRefId);
                    if (!customCustomer.getHidePhoneNumber())
                        customerBasicDetailsDto.setPhone(customCustomer.getMobileNumber());
                    else
                        customerBasicDetailsDto.setPhone(null);
                    customerList.add(customerBasicDetailsDto);
                }
            }
        }
        if(sort.equals("ASC"))
            customerList.sort(Comparator.comparingLong(CustomerBasicDetailsDto::getCustomerId));
        else
            customerList.sort(Comparator.comparingLong(CustomerBasicDetailsDto::getCustomerId).reversed());
        int totalItems = customerList.size();
        int totalPages = (int) Math.ceil((double) totalItems / limit);

        List<CustomerBasicDetailsDto> paginatedList = sharedUtilityService.getPaginatedList(customerList, offset, limit);

        System.out.println("dkfjalksdjflkasdjflkajds"+offset);
        System.out.println(offset);

        Map<String, Object> response = new HashMap<>();
        response.put("customers", paginatedList);       // Your paginated data
        response.put("totalItems", totalItems);      // Total number of items
        response.put("totalPages", totalPages);      // Total number of pages
        response.put("currentPage", offset);           // Current offset number

        return ResponseService.generateSuccessResponse("Fetched Customers", response, HttpStatus.OK);
    }/* catch (MethodArgumentTypeMismatchException | NumberFormatException exception) {
            return ResponseService.generateErrorResponse("Invalid value provided in search filter", HttpStatus.BAD_REQUEST);
        }*/


    @Transactional
    @PutMapping("manage-user")
    public ResponseEntity<?> activateOrSuspendUser(@RequestBody Map<String, Object> map, @RequestParam String action, @RequestHeader(name = "Authorization") String authHeader) throws Exception {
        //extracting info from jwt token
        int actionCount = 0, successCount = 0;
        String jwtToken = authHeader.substring(7);
        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
        Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
        List<Long> ids = getLongList(map, "customerIds");
        Map<Long, String> skippedIds = new HashMap<>();
        List<Long> actionedIds = new ArrayList<>();
        String actionReq = null;
        if (!action.equals(Constant.ACTION_SUSPEND) && !action.equals(Constant.ACTION_ACTIVATE)) {
            return ResponseService.generateErrorResponse("Invalid action", HttpStatus.BAD_REQUEST);
        }
        if (action.equals("suspend"))
            actionReq = action + "ed";
        else
            actionReq = action + "d";
        for (Long customerId : ids) {
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customerId);
            //checking permissions
            if (roleService.getRoleByRoleId(roleId).getRole_name().equals(Constant.roleUser)) {
                skippedIds.put(customerId, "Action not Authorized");
                continue;
            }
            if (customCustomer == null) {
                skippedIds.put(customerId, "Customer Not Found");
                continue;
            }
            if (roleService.getRoleByRoleId(roleId).getRole_name().equals(Constant.roleServiceProvider) || (roleService.getRoleByRoleId(roleId).getRole_name().equals(Constant.roleAdminServiceProvider))) {
                //query to check mapping
                ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, tokenUserId);
                Query query = entityManager.createNativeQuery("Select count(*) from customer_referrer where service_provider_id =:spId and customer_id = :customerId");
                query.setParameter("spId", tokenUserId);
                query.setParameter("customerId", customerId);
                BigInteger count = (BigInteger) query.getSingleResult();
                if (count.intValue() == 0) {
                    skippedIds.put(customerId, "Unauthorized to suspend users not referred by you");
                    continue;
                }
            }
            //checking valid permissions
            if (action.equals(Constant.ACTION_SUSPEND)) {
                if (customCustomer.getArchived().equals(true)) {
                    skippedIds.put(customerId, "User Already Suspended");
                    ++actionCount;
                    continue;
                }
                customCustomer.setArchived(true);
            } else {
                if (customCustomer.getArchived().equals(false)) {
                    skippedIds.put(customerId, "User Already Activate");
                    ++actionCount;
                    continue;
                }
                customCustomer.setArchived(false);
            }
            customCustomer.setArchivedByRole(roleId);
            customCustomer.setArchivedById(tokenUserId);
            if(action.equals(Constant.ACTION_SUSPEND)) {
                sharedUtilityService.blackListToken(customCustomer.getToken(),5,customCustomer.getId());
                logout(customCustomer.getToken());
            }
            else
            {
                sharedUtilityService.removeToken(customCustomer.getToken());
            }
            actionedIds.add(customerId);
            ++successCount;
            entityManager.merge(customCustomer);
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
            return ResponseService.generateSuccessResponse("Action Partially Fulfilled", response, HttpStatus.BAD_REQUEST);
        }
    }
}
