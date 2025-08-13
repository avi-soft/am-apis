package com.community.api.endpoint.avisoft.controller.Customer;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.CustomerBasicDetailsDto;
import com.community.api.dto.ProductDetailsDTO;
import com.community.api.endpoint.avisoft.controller.Acknowledgement.AcknowledgementWebhook;
import com.community.api.endpoint.avisoft.controller.otpmodule.OtpEndpoint;
import com.community.api.endpoint.customer.AddressDTO;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.*;
import com.community.api.entity.Role;
import com.community.api.services.*;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.Document;
import com.community.api.utils.ServiceProviderDocument;
import io.micrometer.core.lang.Nullable;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderAttribute;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.domain.OrderItemAttribute;
import org.broadleafcommerce.core.order.service.OrderService;
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
import org.springframework.transaction.interceptor.TransactionAspectSupport;
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
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.persistence.Query;
import javax.persistence.EntityManager;
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
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.community.api.component.Constant.*;
import static com.community.api.services.ServiceProvider.ServiceProviderServiceImpl.getLongList;

@Slf4j
@RestController
@RequestMapping(value = "/customer",
        produces = {
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_XML_VALUE
        }
)

public class CustomerEndpoint {
    @Autowired
    QualificationService qualificationService;
    @Autowired
    GenderService genderService;
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
    private DocumentStorageService fileUploadService;
    @Autowired
    private SharedUtilityService sharedUtilityServiceApi;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ReserveCategoryAgeService reserveCategoryAgeService;
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
    private ExceptionHandlingService exceptionHandlingService;
    @Autowired
    private OrderService orderService;
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
    @Autowired
    private ServiceProviderServiceImpl serviceProviderService;
    @Autowired
    private StatusChangeEmailService statusChangeEmailService;

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
    public ResponseEntity<?> updateCustomer(@RequestBody Map<String, Object> details, @RequestParam Long customerId, @RequestHeader(value = "Authorization") String authHeader, HttpServletRequest httpServletRequest) {
        try {
            Boolean externalUpdate = false;
            Boolean isValidDate = null;
            Boolean isValidDateDomicile = null;
            String jwtToken = authHeader.substring(7);
            List<String> deleteLogs = new ArrayList<>();
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String roleName = roleService.findRoleName(roleId);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Map<String, String> errorMessages = new LinkedHashMap<>();
            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
            if(roleId==4)
            {
                if(customerId==null)
                    return ResponseService.generateErrorResponse("Id not provided",HttpStatus.NOT_FOUND);
                ExternalUseToken externalUseToken=entityManager.find(ExternalUseToken.class,tokenUserId);
                if(externalUseToken==null||externalUseToken.getToken()==null||externalUseToken.getToken().isEmpty())
                    return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.UNAUTHORIZED);
                if(!jwtTokenUtil.extractId(externalUseToken.getToken()).equals(customerId))
                    return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.UNAUTHORIZED);
            } else if((roleId == 5 && !tokenUserId.equals(customerId))) {
                return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.UNAUTHORIZED);
            }
            details = sanitizerService.sanitizeInputMap(details);

            String dobStr = (String) details.get("dob");

            // Define the expected date format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            if (dobStr != null) {
                try {
                    LocalDate dob = LocalDate.parse(dobStr, formatter);
                    LocalDate today = LocalDate.now();

                    // Check if DOB is not in the future
                    if (dob.isAfter(today))
                        errorMessages.put("dob", "DOB cannot be of future");

                } catch (DateTimeParseException e) {
                    // Invalid date format
                    errorMessages.put("dob", "Invalid DOB");

                }
            }

            /*Iterator<String> iterator = details.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (details.get(key).toString().isEmpty()) {
                    iterator.remove(); // Safely remove using the iterator
                    errorMessages.add(key + " cannot be null");
                }
            }*/
           /* if (!errorMessages.isEmpty()) {
                String message = String.join(", ", errorMessages.values());
                return ResponseService.generateSuccessResponse(message, errorMessages.keySet(), HttpStatus.BAD_REQUEST);
            }*/
            if (customerService == null) {
                return ResponseService.generateSuccessResponse("Customer service is not initialized.", "customerService", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (customCustomer == null) {
                return ResponseService.generateSuccessResponse("No data found for this customerId", "customerId", HttpStatus.NOT_FOUND);
            }
            if (customCustomer.getArchived().equals(true)) {
                return ResponseService.generateSuccessResponse("Your account is suspended. Please contact support.", "archived", HttpStatus.FORBIDDEN);
            }
            List<OtherItem> existingItems = customCustomer.getOtherItems();
            String secondaryMobileNumber = (String) details.get("secondaryMobileNumber");
            String mobileNumber = (String) details.get("mobileNumber");
            if (secondaryMobileNumber != null && mobileNumber == null && secondaryMobileNumber.equalsIgnoreCase(customCustomer.getMobileNumber())) {
                errorMessages.put("mobileNumber", "Primary and Secondary Mobile Numbers cannot be the same");
            }
            if (details.containsKey("interestedInDefence")) {
                Boolean value = (Boolean) details.get("interestedInDefence");
                customCustomer.setInterestedInDefence(value);
            }
            if (details.containsKey("has_state_category")) {
                String categoryStateName = (String) details.get("category_state_name");
                Boolean hasStateCategory = (Boolean) details.get("has_state_category");
                String stateCategoryName = (String) details.get("state_category");
                if (hasStateCategory) {
                    if (!details.containsKey("category_state_name") || categoryStateName == null || categoryStateName.trim().isEmpty())
                        errorMessages.put("category_state_name", "Need to provide state for state level category");
                    if (!details.containsKey("state_category") || stateCategoryName == null || stateCategoryName.trim().isEmpty())
                        errorMessages.put("state_category", "State level category name required");
                    customCustomer.setHasStateCategory(true);
                    customCustomer.setStateCategory(stateCategoryName);
                    customCustomer.setCategoryStateName(categoryStateName);
                    customCustomer.setCategory(null);
                } else {
                    if ((stateCategoryName != null && !stateCategoryName.trim().isEmpty()) || (categoryStateName != null && categoryStateName.trim().isEmpty()))
                        errorMessages.put("state_category", "State level category cannot be provided");
                    customCustomer.setHasStateCategory(false);
                }
            }
          /*  else
            {
                return ResponseService.generateErrorResponse("Need to provide whether state level category or not",HttpStatus.BAD_REQUEST);
            }*/

            if (details.containsKey("familyIncome")) {
                Object incomeObj = details.get("familyIncome");
                if (incomeObj == null || incomeObj.toString().trim().isEmpty()) {
                    customCustomer.setFamilyIncome(null); // Clear the value
                } else {
                    try {
                        Long familyIncomeValue = Long.parseLong(incomeObj.toString().trim());
                        if (familyIncomeValue <= 0) {
                            errorMessages.put("familyIncome", "FamilyIncome must be greater than 0");
                        }
                        customCustomer.setFamilyIncome(familyIncomeValue);
                    } catch (NumberFormatException e) {
                        errorMessages.put("familyIncome", "Invalid value for familyIncome");
                    }
                }
            }
            if (customCustomer.getFamilyIncome() == null) {
                List<Document> customerDocuments = customCustomer.getDocuments();
                for (Document document : customerDocuments) {
                    if (document.getIsArchived().equals(false)) {
                        if (document.getCustom_customer().getId().equals(customerId)) {
                            if (document.getDocumentType().getDocument_type_id().equals(8)) {
                                document.setIsArchived(true);
                                entityManager.merge(document);
                            }
                        }
                    }
                }
            }


            details.remove("familyIncome");

            // physical attributes locale variables.
            double minHeight = 50.0, maxHeight = 250.0, minWeight = 10.0, maxWeight = 300.0, minShoeSize = 4.0, maxShoeSize = 15.0, minWaistSize = 20.0, maxWaistSize = 150.0, minChestSize = 20.0, maxChestSize = 125.0;

            if ((customCustomer.getInterestedInDefence() != null && details.containsKey("interestedInDefence"))) {
                if (customCustomer.getInterestedInDefence()) {
                    // List of required fields
                    final List<String> requiredFields = Arrays.asList("heightCms", "weightKgs", "shoeSizeInches", "waistSizeCms");

                    // Check if all required fields are present and not empty
                    Map<String, Object> finalDetails = details;
                    boolean allFieldsPresent = true;

                    for (String field : requiredFields) {
                        if (!finalDetails.containsKey(field) || finalDetails.get(field) == null || finalDetails.get(field).toString().trim().isEmpty()) {
                            errorMessages.put(field, field + " is required when interestedInDefence is true");
                            allFieldsPresent = false;
                        }
                    }

                    if (allFieldsPresent) {
                        try {
                            String heightStr = (String) finalDetails.get("heightCms");
                            if (heightStr != null && !heightStr.isEmpty()) {
                                Double heightValue = Double.parseDouble(heightStr);
                                if (heightValue < minHeight || heightValue > maxHeight) {
                                    errorMessages.put("heightCms", "Height should be between " + minHeight + " and " + maxHeight + " cms.");
                                } else {
                                    customCustomer.setHeightCms(heightValue);
                                }
                            } else {
                                errorMessages.put("heightCms", "Height is required and must be a valid value.");
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.put("heightCms", "Height must be valid");
                        }

                        try {
                            String weightStr = (String) finalDetails.get("weightKgs");
                            if (weightStr != null && !weightStr.isEmpty()) {
                                Double weightValue = Double.parseDouble(weightStr);
                                if (weightValue < minWeight || weightValue > maxWeight) {
                                    errorMessages.put("weightKgs", "Weight should be between " + minWeight + " and " + maxWeight + " kgs.");
                                } else {
                                    customCustomer.setWeightKgs(weightValue);
                                }
                            } else {
                                errorMessages.put("weightKgs", "Weight is required and must be a valid value.");
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.put("weightKgs", "Weight must be valid.");
                        }

                        try {
                            String shoeSizeStr = (String) finalDetails.get("shoeSizeInches");
                            if (shoeSizeStr != null && !shoeSizeStr.isEmpty()) {
                                Double shoeSizeValue = Double.parseDouble(shoeSizeStr);
                                if (shoeSizeValue < minShoeSize || shoeSizeValue > maxShoeSize) {
                                    errorMessages.put("shoeSizeInches", "Shoe size should be between " + minShoeSize + " and " + maxShoeSize + " inches.");
                                } else {
                                    customCustomer.setShoeSizeInches(shoeSizeValue);
                                }
                            } else {
                                errorMessages.put("shoeSizeInches", "Shoe size is required and must be a valid value.");
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.put("shoeSizeInches", "Shoe size must be valid.");
                        }

                        try {
                            String waistSizeStr = (String) finalDetails.get("waistSizeCms");
                            if (waistSizeStr != null && !waistSizeStr.isEmpty()) {
                                Double waistSizeValue = Double.parseDouble(waistSizeStr);
                                if (waistSizeValue < minWaistSize || waistSizeValue > maxWaistSize) {
                                    errorMessages.put("waistSizeCms", "Waist size should be between " + minWaistSize + " and " + maxWaistSize + " cms.");
                                } else {
                                    customCustomer.setWaistSizeCms(waistSizeValue);
                                }
                            } else {
                                errorMessages.put("waistSizeCms", "Waist size is required and must be a valid value.");
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.put("waistSizeCms", "Waist size must be valid.");
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
                                errorMessages.put("heightCms", "Height should be between " + minHeight + " and " + maxHeight + " cms.");
                            } else {
                                customCustomer.setHeightCms(heightValue);
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.put("heightCms", "Height must be valid.");
                        }
                    } else if (height != null && height.isEmpty()) {
                        customCustomer.setHeightCms(null);
                    }

                    if (weightKgs != null && !weightKgs.isEmpty()) {
                        try {
                            Double weightValue = Double.parseDouble(weightKgs);
                            if (weightValue < minWeight || weightValue > maxWeight) {
                                errorMessages.put("weightKgs", "Weight should be between " + minWeight + " and " + maxWeight + " kgs.");
                            } else {
                                customCustomer.setWeightKgs(weightValue);
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.put("weightKgs", "Weight must be valid.");
                        }
                    } else if (weightKgs != null && weightKgs.isEmpty()) {
                        customCustomer.setWeightKgs(null);
                    }

                    if (shoeSizeInches != null && !shoeSizeInches.isEmpty()) {
                        try {
                            Double shoeSizeValue = Double.parseDouble(shoeSizeInches);
                            if (shoeSizeValue < minShoeSize || shoeSizeValue > maxShoeSize) {
                                errorMessages.put("shoeSizeInches", "Shoe size should be between " + minShoeSize + " and " + maxShoeSize + " inches.");
                            } else {
                                customCustomer.setShoeSizeInches(shoeSizeValue);
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.put("shoeSizeInches", "Shoe size must be valid.");
                        }
                    } else if (shoeSizeInches != null && shoeSizeInches.isEmpty()) {
                        customCustomer.setShoeSizeInches(null);
                    }

                    if (waistSizeCms != null && !waistSizeCms.isEmpty()) {
                        try {
                            Double waistSizeValue = Double.parseDouble(waistSizeCms);
                            if (waistSizeValue < minWaistSize || waistSizeValue > maxWaistSize) {
                                errorMessages.put("waistSizeCms", "Waist size should be between " + minWaistSize + " and " + maxWaistSize + " cms.");
                            } else {
                                customCustomer.setWaistSizeCms(waistSizeValue);
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.put("waistSizeCms", "Waist size must be valid.");
                        }
                    } else if (waistSizeCms != null && waistSizeCms.isEmpty()) {
                        customCustomer.setWaistSizeCms(null);
                    }
                }
            }

            if (details.containsKey("workExperienceScopeId")) {
                CustomApplicationScope customApplicationScope = applicationScopeService.getApplicationScopeById(Long.parseLong(details.get("workExperienceScopeId").toString()));
                customCustomer.setWorkExperienceScopeId(customApplicationScope);
                if (details.containsKey("workExperience")) {
                    Integer workExperience = Integer.parseInt(details.get("workExperience").toString());
                    if (workExperience < 1) {
                        customCustomer.setWorkExperienceScopeId(null);
                    }
                    customCustomer.setWorkExperience(workExperience);
                }
            }
            if (details.containsKey("workExperience")) {
                Integer workExperience = Integer.parseInt(details.get("workExperience").toString());
                if (workExperience < 1) {
                    customCustomer.setWorkExperienceScopeId(null);
                } else {
                    if (!details.containsKey("workExperienceScopeId") && customCustomer.getWorkExperienceScopeId() == null) {
                        {
                            throw new IllegalArgumentException("Work experience scope id cannot be null if work experience > 0");
                        }
                    }
                }
                customCustomer.setWorkExperience(workExperience);
            }

            if (details.containsKey("sportCertificateId")) {
                CustomApplicationScope customApplicationScope = applicationScopeService.getApplicationScopeById(Long.parseLong((String) details.get("sportCertificateId")));
                customCustomer.setSportCertificateId(customApplicationScope);
            }
            if (details.containsKey("exService")) {
                Boolean exService = (Boolean) details.get("exService");
                if (exService) {
                    customCustomer.setExService(true);
                } else {
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
                        errorMessages.put("domicileState", "cannot leave domicile state as null by opting for the domicile.");
                    }
                } else {
                    if ((details.containsKey("domicileState"))) {
                        errorMessages.put("domicileState", "cannot give domicile state w/o opting for the domicile.");
                    }
                    customCustomer.setDomicile(false);
                    customCustomer.setDomicileState(null);
                }
            } else if (details.containsKey("domicileState")) {
                errorMessages.put("domicileState", "cannot give domicile state w/o opting for the domicile.");
            }

            if (details.containsKey("hidePhoneNumber")) {
                customCustomer.setHidePhoneNumber((Boolean) details.get("hidePhoneNumber"));
                if ((Boolean) details.get("hidePhoneNumber").equals(true)) {
                    errorMessages.putAll(validateHidePhoneNumber(details, customCustomer));
                }
                if (secondaryMobileNumber != null && !customCustomerService.isValidMobileNumber(secondaryMobileNumber))
                    errorMessages.put("secondaryMobileNumber", "Secondary mobile is invalid");
                if (details.containsKey("whatsappNumber") && !customCustomerService.isValidMobileNumber((String) details.get("whatsappNumber")))
                    errorMessages.put("whatsappNumber", "Invalid Whatsapp NUmber");
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
                    errorMessages.put("mobileNumber", "Primary and Secondary Mobile Numbers cannot be the same");
                }
            }
            if (mobileNumber != null && !customCustomerService.isValidMobileNumber(mobileNumber)) {
                errorMessages.put("mobileNumber", "Cannot update phoneNumber");
            }

            if (mobileNumber != null && secondaryMobileNumber == null && mobileNumber.equalsIgnoreCase(customCustomer.getSecondaryMobileNumber())) {
                errorMessages.put("mobileNumber", "Primary and Secondary Mobile Numbers cannot be the same");
            }

            // Check for existing username and email
            String username = (String) details.get("username");
            String emailAddress = (String) details.get("emailAddress");
            Customer existingCustomerByUsername = (username != null) ? customerService.readCustomerByUsername(username) : null;
            Customer existingCustomerByEmail = (emailAddress != null) ? customerService.readCustomerByEmail(emailAddress) : null;

            if ((existingCustomerByUsername != null && !existingCustomerByUsername.getId().equals(customerId)) ||
                    (existingCustomerByEmail != null && !existingCustomerByEmail.getId().equals(customerId))) {
                errorMessages.put("emailAddress", "Email or Username already in use");
            }

            // Update customer fields
            customCustomer.setId(customerId);
            customCustomer.setMobileNumber(customCustomer.getMobileNumber());
            customCustomer.setIsAcknowledged(false);
            customCustomer.setQualificationDetailsList(customCustomer.getQualificationDetailsList());
            customCustomer.setCountryCode(customCustomer.getCountryCode());

            if (details.containsKey("firstName") && !details.get("firstName").toString().isEmpty()) {
                customCustomer.setFirstName((String) details.get("firstName"));
                customCustomer.setIsAcknowledged(false);
            } else if (details.containsKey("firstName") && details.get("firstName").toString().isEmpty()) {
                errorMessages.put("firstName", "First name cannot be null");
            } else if (details.containsKey("firstName") && !sharedUtilityService.isAlphabetic((String) details.get("firstName"))) {
                errorMessages.put("firstName", "Invalid First name");
            }
            if (details.containsKey("lastName") && !details.get("lastName").toString().isEmpty()) {
                customCustomer.setLastName((String) details.get("lastName"));
                customCustomer.setIsAcknowledged(false);
            } else if (details.containsKey("lastName") && details.get("lastName").toString().isEmpty()) {
                errorMessages.put("lastName", "Last name cannot be null");
            } else if (details.containsKey("lastName") && !sharedUtilityService.isAlphabetic((String) details.get("lastName"))) {
                errorMessages.put("lastName", "Invalid Last name");
            }
            if (details.containsKey("emailAddress") && ((String) details.get("emailAddress")).isEmpty())
                errorMessages.put("emailAddress", "email Address cannot be null");
            if (details.containsKey("emailAddress") && !((String) details.get("emailAddress")).isEmpty()) {
                customCustomer.setIsAcknowledged(false);
                customCustomer.setEmailAddress(emailAddress);
            }

            if (details.containsKey("fathersName") && !details.get("fathersName").toString().isEmpty()) {
                customCustomer.setFathersName((String) details.get("fathersName"));
                customCustomer.setIsAcknowledged(false);
            } else if (details.containsKey("fathersName") && details.get("fathersName").toString().isEmpty()) {
                errorMessages.put("fathersName", "Father's name cannot be null");
            } else if (details.containsKey("fathersName") && !sharedUtilityService.isAlphabetic((String) details.get("fathersName"))) {
                errorMessages.put("fathersName", "You entered invalid Father's name");
            }

            if (details.containsKey("mothersName") && !details.get("mothersName").toString().isEmpty()) {
                customCustomer.setMothersName((String) details.get("mothersName"));
                customCustomer.setIsAcknowledged(false);
            } else if (details.containsKey("mothersName") && details.get("mothersName").toString().isEmpty()) {
                errorMessages.put("mothersName", "Mother's name cannot be null");
            } else if (details.containsKey("mothersName") && !sharedUtilityService.isAlphabetic((String) details.get("mothersName"))) {
                errorMessages.put("mothersName", "You entered invalid Mother's name");
            }
            // Handle dynamic fields
            details.remove("firstName");
            details.remove("lastName");
            details.remove("fathersName");
            details.remove("mothersName");
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
                errorMessages.putAll(customCustomerService.validateAddress(addressLine, city, pincode));
            if (flag && containsCount == 5) {
                boolean updated = false;
                for (CustomerAddress customerAddress : customCustomer.getCustomerAddresses()) {
                    if (customerAddress.getAddressName().equals("CURRENT_ADDRESS")) {
                        customCustomer.setIsAcknowledged(false);
                        customerAddress.getAddress().setAddressLine1(addressLine);
                        String stateName = districtService.findStateById(Integer.parseInt(state));
                        if (stateName == null) {
                            errorMessages.put("currentState", "Invalid State");
                        }

                        customerAddress.getAddress().setStateProvinceRegion(stateName);
                        String districtName = districtService.findDistrictById(Integer.parseInt(district));
                        if (districtName == null) {
                            errorMessages.put("currentDistrict", "Invalid district");

                        }
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
                    if (stateName == null) {
                        errorMessages.put("currentState", "Invalid State");
                    }

                    addressMap.put("state", stateName);
                    addressMap.put("city", details.get("currentCity"));
                    String districtName = districtService.findDistrictById(Integer.parseInt(district));
                    if (districtName == null) {
                        errorMessages.put("currentDistrict", "Invalid district");
                    }
                    addressMap.put("district", districtName);
                    addressMap.put("pinCode", pincode);
                    addressMap.put("addressName", "CURRENT_ADDRESS");
                    addAddress(customerId, addressMap);
                }
            } else if (!flag && containsCount != 0) {
                for (String key : keys) {
                    if (!details.containsKey(key) || details.get(key) == null || details.get(key).toString().trim().isEmpty()) {
                        errorMessages.put(key, key + " is required to add Current Address");
                    }
                }
            }

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
                errorMessages.putAll(customCustomerService.validateAddress(addressLine, city, pincode));
            if (flagP && containsCount == 5) {
                boolean updated = false;
                for (CustomerAddress customerAddress : customCustomer.getCustomerAddresses()) {

                    if (customerAddress.getAddressName().equals("PERMANENT_ADDRESS")) {
                        customerAddress.getAddress().setAddressLine1(addressLine);
                        customCustomer.setIsAcknowledged(false);
                        String stateName = districtService.findStateById(Integer.parseInt(state));
                        if (stateName == null) {
                            errorMessages.put("currentState", "Invalid State");
                        }

                        customerAddress.getAddress().setStateProvinceRegion(stateName);
                        String districtName = districtService.findDistrictById(Integer.parseInt(district));
                        if (districtName == null) {
                            errorMessages.put("currentDistrict", "Invalid currentDistrict");
                        }
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
                    if (stateName == null) {
                        errorMessages.put("currentState", "Invalid State");
                    }

                    addressMap.put("state", stateName);
                    addressMap.put("city", details.get("permanentCity"));
                    String districtName = districtService.findDistrictById(Integer.parseInt(district));
                    if (districtName == null) {
                        errorMessages.put("currentDistrict", "Invalid district");
                    }
                    addressMap.put("district", districtName);
                    addressMap.put("pinCode", pincode);
                    addressMap.put("addressName", "PERMANENT_ADDRESS");
                    addAddress(customerId, addressMap);
                }
            } else if (!flagP && containsCount != 0) {
                for (String key : keysP) {
                    if (!details.containsKey(key) || details.get(key) == null || details.get(key).toString().trim().isEmpty()) {
                        errorMessages.put(key, key + " is required to add Permanent Address");
                    }
                }
            }

            // validate that both the addresses are same or not
            CustomerAddress currentAddress = null;
            CustomerAddress permanentAddress = null;

            for (CustomerAddress addr : customCustomer.getCustomerAddresses()) {
                if ("CURRENT_ADDRESS".equalsIgnoreCase(addr.getAddressName())) {
                    currentAddress = addr;
                } else if ("PERMANENT_ADDRESS".equalsIgnoreCase(addr.getAddressName())) {
                    permanentAddress = addr;
                }
            }

            if (currentAddress != null && permanentAddress != null) {
                Address curr = currentAddress.getAddress();
                Address perm = permanentAddress.getAddress();

                boolean addressesMatch =
                        Objects.equals(curr.getAddressLine1(), perm.getAddressLine1()) &&
                                Objects.equals(curr.getCity(), perm.getCity()) &&
                                Objects.equals(curr.getPostalCode(), perm.getPostalCode()) &&
                                Objects.equals(curr.getCounty(), perm.getCounty()) &&
                                Objects.equals(curr.getStateProvinceRegion(), perm.getStateProvinceRegion());

                customCustomer.setIsSameAsCurrentAddress(addressesMatch);
//                em.merge(customCustomer);
            }

            if (details.containsKey("adharNumber")) {
                String adharNumber = (String) details.get("adharNumber");
                if (customCustomer.getAdharNumber() != null) {
                    if (!customCustomer.getAdharNumber().equals(adharNumber)) {
                        Query query = entityManager.createNativeQuery("SELECT COUNT(*) FROM custom_customer WHERE adhar_number = :adharNumber");
                        query.setParameter("adharNumber", adharNumber);
                        Integer result = ((Number) query.getSingleResult()).intValue();
                        if (result > 0) {
                            errorMessages.put("adharNumber", "Aadhaar number already in use.");
                            details.remove("adharNumber");
                        }
                    }
                } else if (customCustomer.getAdharNumber() == null) {
                    Query query = entityManager.createNativeQuery("SELECT COUNT(*) FROM custom_customer WHERE adhar_number = :adharNumber");
                    query.setParameter("adharNumber", adharNumber);
                    Integer result = ((Number) query.getSingleResult()).intValue();
                    System.out.println("result" + result);
                    if (result > 0) {
                        errorMessages.put("adharNumber", "Aadhaar number already in use!!");
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
                    errorMessages.put("nccCertificate", "You can add value for ncc certificate either NCC Certificate A or NCC Certificate B or  NCC Certificate C");
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
                int age = sharedUtilityServiceApi.calculateAge(dob);
                if (age < 0)
                    errorMessages.put("dob", "Invalid date of birth");
//                else if (age < 8)
//                    errorMessages.add("Your age should be greater than equal to 8");
                else {
                    customCustomer.setDob(dob);
                    customCustomer.setIsAcknowledged(false);
                }
            }
            if (details.containsKey("isLivePhotoNa")) {
                Boolean isLivePhotoNa = (Boolean) details.get("isLivePhotoNa");
                if (isLivePhotoNa.equals(true)) {
                    assert customCustomer.getDocuments() != null;
                    for (Document document : customCustomer.getDocuments()) {
                        if (document.getDocumentType().getDocument_type_id().equals(3) && document.getIsArchived().equals(false)) {
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
                        errorMessages.put("nccCertificate", "You have to select ncc certificate type");
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
                    errorMessages.put("nssCertificate", "You can add value for ncc certificate either NSS Certificate A or NSS Certificate B or  NSS Certificate C");
                }
                customCustomer.setNssCertificate(nssCertificateValue);
                customCustomer.setIsNssCertificate(true);
            }
            if (details.containsKey("isNssCertificate")) {
                Boolean isNssCertificate = (Boolean) details.get("isNssCertificate");
                if (isNssCertificate.equals(true)) {
                    if (!details.containsKey("nssCertificate")) {
                        errorMessages.put("nssCertificate", "You have to select nss certificate type");
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
                        errorMessages.put("otherOrStateCategory", "You have to enter other or State Category");
                    }
                    if (!details.containsKey("otherCategoryDateOfIssue")) {
                        errorMessages.put("otherCategoryDateOfIssue", "You have to enter date of issue for other or State Category ");
                    }
                    if (details.containsKey("otherOrStateCategory") && details.get("otherOrStateCategory").toString().trim().isEmpty()) {
                        errorMessages.put("otherOrStateCategory", "Other or state Category value cannot be empty  ");
                    }
                    if (!details.get("otherOrStateCategory").toString().matches("^[a-zA-Z0-9 ]*$")) {
                        errorMessages.put("otherOrStateCategory", "Only alphanumeric characters are allowed in otherOrStateCategory");
                    }
                    if (details.containsKey("otherCategoryDateOfIssue") && details.get("otherCategoryDateOfIssue").toString().trim().isEmpty()) {
                        errorMessages.put("otherCategoryDateOfIssue", "OtherCategory DateOfIssue cannot be empty ");
                    }
                    if (details.containsKey("otherCategoryValidUpto")) {
                        String validUpto = (String) details.get("otherCategoryValidUpto");
                        if (validUpto.isEmpty()) {
                            customCustomer.setOtherCategoryValidUpto(null);
                            isValidDate = validateDate((String) details.get("otherCategoryDateOfIssue"), null, dateFormat, errorMessages, "otherCategoryDateOfIssue", "otherCategoryValidUpto");
                        } else if (validUpto.trim().isEmpty()) {
                            customCustomer.setOtherCategoryValidUpto(null);
                            validateDate((String) details.get("otherCategoryDateOfIssue"), null, dateFormat, errorMessages, "otherCategoryDateOfIssue", "otherCategoryValidUpto");
                        } else {
                            validateDate((String) details.get("otherCategoryDateOfIssue"), (String) details.get("otherCategoryValidUpto"), dateFormat, errorMessages, "otherCategoryDateOfIssue", "otherCategoryValidUpto");
                            customCustomer.setOtherCategoryValidUpto(convertStringToSQLDate((String) details.get("otherCategoryValidUpto"), dateFormat));
                        }
                    } else {
                        validateDate((String) details.get("otherCategoryDateOfIssue"), (String) details.get("otherCategoryValidUpto"), dateFormat, errorMessages, "otherCategoryDateOfIssue", "otherCategoryValidUpto");
                    }
                    customCustomer.setOtherOrStateCategory((String) details.get("otherOrStateCategory"));
                    customCustomer.setOtherCategoryDateOfIssue(convertStringToSQLDate((String) details.get("otherCategoryDateOfIssue"), dateFormat));
                } else if (isOtherCategory.equals(false)) {
                    if (details.containsKey("otherOrStateCategory")) {
                        errorMessages.put("otherOrStateCategory", "OtherOrStateCategory cannot be given if isOtherCategory is false");
                    }
                    if (details.containsKey("otherCategoryDateOfIssue")) {
                        errorMessages.put("otherCategoryDateOfIssue", "otherCategoryDateOfIssue key cannot be given if isOtherCategory is false");
                    }
                    if (details.containsKey("otherCategoryValidUpto")) {
                        errorMessages.put("otherCategoryValidUpto", "otherCategoryValidUpto key cannot be given if isOtherCategory is false");
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
                        errorMessages.put("domicileIssueDate", "You have to enter date of issue for domicile");
                    }
                    if (details.containsKey("domicileIssueDate") && details.get("domicileIssueDate").toString().trim().isEmpty()) {
                        errorMessages.put("domicileIssueDate", "domicile DateOfIssue cannot be empty ");
                    }
                    if (details.containsKey("domicileValidUpto")) {
                        String validUpto = (String) details.get("domicileValidUpto");
                        if (validUpto.isEmpty()) {
                            customCustomer.setDomicileValidUpto(null);
                            isValidDateDomicile = validateDate((String) details.get("domicileIssueDate"), null, dateFormat, errorMessages, "domicileIssueDate", "domicileValidUpto");
                        } else if (validUpto.trim().isEmpty()) {
                            customCustomer.setDomicileValidUpto(null);
                            validateDate((String) details.get("domicileIssueDate"), null, dateFormat, errorMessages, "domicileIssueDate", "domicileValidUpto");
                        } else {
                            validateDate((String) details.get("domicileIssueDate"), (String) details.get("domicileValidUpto"), dateFormat, errorMessages, "domicileIssueDate", "domicileValidUpto");
                            customCustomer.setDomicileValidUpto(convertStringToSQLDate((String) details.get("domicileValidUpto"), dateFormat));
                        }
                    } else {
                        validateDate((String) details.get("domicileIssueDate"), (String) details.get("domicileValidUpto"), dateFormat, errorMessages, "domicileIssueDate", "domicileValidUpto");
                    }
                    customCustomer.setDomicileIssueDate(convertStringToSQLDate((String) details.get("domicileIssueDate"), dateFormat));
                } else if (domicile.equals(false)) {
                    if (details.containsKey("domicileIssueDate")) {
                        errorMessages.put("domicileIssueDate", "domicileIssueDate key cannot be given if domicile is false");
                    }
                    if (details.containsKey("domicileState")) {
                        errorMessages.put("domicileState", "domicileState key cannot be given if domicile is false");
                    }
                    if (details.containsKey("domicileValidUpto")) {
                        errorMessages.put("domicileValidUpto", "domicileValidUpto key cannot be given if domicile is false");
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
                    && details.containsKey("chestSizeCms") && !details.get("chestSizeCms").toString().trim().isEmpty()) {
                errorMessages.put("chestSizeCms", "Cannot add chest size for gender : Female");
            }

            if (customCustomer.getGender() == null && details.containsKey("chestSizeCms")) {
                errorMessages.put("chestSizeCms", "Cannot add chest size without specifying gender");
            }

            if (details.containsKey("chestSizeCms") && !details.get("chestSizeCms").toString().trim().isEmpty()) {
                if (customCustomer.getGender().equals("Female")) {
                    errorMessages.put("chestSizeCms", "Cannot add chest size with female");
                } else {
                    String chestSizeCms = (String) details.get("chestSizeCms");
                    if (chestSizeCms != null && !chestSizeCms.isEmpty()) {
                        try {
                            Double chestSizeValue = Double.parseDouble(chestSizeCms);
                            if (chestSizeValue < minChestSize || chestSizeValue > maxChestSize) {
                                errorMessages.put("chestSizeCms", "Chest size should be between " + minChestSize + " and " + maxChestSize + " cms.");
                            } else {
                                customCustomer.setChestSizeCms(chestSizeValue);
                            }
                        } catch (NumberFormatException e) {
                            errorMessages.put("chestSizeCms", "Chest size must be valid.");
                        }
                    }
                    customCustomer.setChestSizeCms(Double.parseDouble(chestSizeCms));
                }
            } else if (details.containsKey("chestSizeCms") && details.get("chestSizeCms").toString().trim().isEmpty()) {
                {
                    customCustomer.setChestSizeCms(null);
                }
            }

            details.remove("has_state_category");
            details.remove("state_category");
            details.remove("category_state_name");
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
                        errorMessages.put(fieldName, fieldName + " cannot be null");
                        continue;
                    }
                }
                if (field.isAnnotationPresent(Pattern.class)) {
                    Pattern patternAnnotation = field.getAnnotation(Pattern.class);
                    String regex = patternAnnotation.regexp();
                    String message = patternAnnotation.message(); // Get custom message
                    if (!newValue.toString().matches(regex)) {
                        errorMessages.put(fieldName, patternAnnotation.message()); // Use a placeholder
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
                        errorMessages.put(fieldName, fieldName + " size should be between " + min + " and " + max);
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
                            errorMessages.put(fieldName, minAnnotation.message());
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
                            errorMessages.put(fieldName, maxAnnotation.message());
                        }
                    }
                }

                // Set value if type is compatible
                if (newValue != null && field.getType().isAssignableFrom(newValue.getClass())) {
                    field.set(customCustomer, newValue);
                }
            }

            if (details.containsKey("religion")) {
                Boolean isOtherReligion = false;
                OtherItem religionOtherItemToAdd = null;
                customCustomer.setReligion(details.get("religion").toString());

                if (details.get("religion").toString().equalsIgnoreCase("Others")) {
                    isOtherReligion = true;
                }

                Boolean userExists = false;
                if (isOtherReligion.equals(false)) {
                    customCustomer.setOtherReligion(null);
                    List<OtherItem> currentOtherItems = customCustomer.getOtherItems();
                    if (!currentOtherItems.isEmpty()) {
                        Iterator<OtherItem> iterator = currentOtherItems.iterator();
                        while (iterator.hasNext()) {
                            OtherItem otherItem = iterator.next();
                            if (customCustomer.getId().equals(otherItem.getUser_id())) {
                                userExists = true;
                            }
                            if ((otherItem.getSource_name().equalsIgnoreCase("customer profile update page")) &&
                                    otherItem.getField_name().equalsIgnoreCase("religion") && userExists) {
                                iterator.remove();
                            }
                        }
                        customCustomer.setOtherReligion(null);
                        customCustomer.setOtherItems(currentOtherItems);
                    }
                } else if (isOtherReligion.equals(true)) {
                    existingItems = customCustomer.getOtherItems();
                    if (existingItems != null && !existingItems.isEmpty()) {
                        boolean itemUpdated = false;
                        Iterator<OtherItem> iterator = existingItems.iterator();

                        while (iterator.hasNext()) {
                            OtherItem otherItem = iterator.next();
                            if ((otherItem.getSource_name().equalsIgnoreCase("customer profile update page")) &&
                                    otherItem.getField_name().equalsIgnoreCase("religion")) {
                                if (!details.containsKey("otherReligion")) {
                                    throw new IllegalArgumentException("You have to enter text for other religion");
                                }
                                otherItem.setTyped_text(details.get("otherReligion").toString());
                                otherItem.setSource_name("customer profile update page");
                                entityManager.merge(otherItem);
                                itemUpdated = true;
                            }
                        }

                        if (!itemUpdated) {
                            religionOtherItemToAdd = sharedUtilityService.handleOtherCaseForReligion(
                                    details.get("religion").toString(), (String) details.get("otherReligion"), roleId, customerId, "customer profile update page");
                            existingItems.add(religionOtherItemToAdd);
                        }
                    } else {
                        if (existingItems == null) {
                            existingItems = new ArrayList<>();
                        }
                        religionOtherItemToAdd = sharedUtilityService.handleOtherCaseForReligion(
                                details.get("religion").toString(), (String) details.get("otherReligion"), roleId, customerId, "customer profile update page");
                        existingItems.add(religionOtherItemToAdd);
                    }

                    customCustomer.setOtherItems(existingItems);
                    customCustomer.setOtherReligion((String) details.get("otherReligion"));
//                    entityManager.merge(customCustomer);
                }
//
            }
            if (details.containsKey("otherReligion")) {
                details.remove("otherReligion");
            }

            if (details.containsKey("category")) {
                if (((String) details.get("category")).equalsIgnoreCase("GEN")) {
                    customCustomer.setCategoryIssueDate(null);
                    customCustomer.setCategoryValidUpto(null);
                }
                Boolean isOtherCategory = false;
                List<CustomReserveCategory> reserveCategories = reserveCategoryService.getAllReserveCategory(null);
                Long reserveCategoryToAddId = null;
                if (reserveCategories != null && !reserveCategories.isEmpty()) {
                    for (CustomReserveCategory customReserveCategory : reserveCategories) {
                        if (customReserveCategory.getReserveCategoryName().equalsIgnoreCase((String) details.get("category"))) {
                            reserveCategoryToAddId = customReserveCategory.getReserveCategoryId();
                        }
                    }
                    if (reserveCategoryToAddId == null) {
                        throw new IllegalArgumentException("Reserve category with name " + details.get("category").toString() + " does not exist");
                    }
                }
                OtherItem categoryOtherItemToAdd = null;
                customCustomer.setCategory(details.get("category").toString());
                customCustomer.setIsAcknowledged(false);

                if (details.get("category").toString().equalsIgnoreCase("Others")) {
                    isOtherCategory = true;
                }

                Boolean userExists = false;
                if (isOtherCategory.equals(false)) {
                    customCustomer.setOtherCategory(null);
                    List<OtherItem> currentOtherItems = customCustomer.getOtherItems();
                    if (!currentOtherItems.isEmpty()) {
                        Iterator<OtherItem> iterator = currentOtherItems.iterator();
                        while (iterator.hasNext()) {
                            OtherItem otherItem = iterator.next();
                            if (customCustomer.getId().equals(otherItem.getUser_id())) {
                                userExists = true;
                            }
                            if ((otherItem.getSource_name().equalsIgnoreCase("customer profile update page")) &&
                                    otherItem.getField_name().equalsIgnoreCase("reserve_category") && userExists) {
                                iterator.remove();
                            }
                        }
                        customCustomer.setOtherCategory(null);
                        customCustomer.setOtherItems(currentOtherItems);
                    }
                } else if (isOtherCategory.equals(true)) {
                    existingItems = customCustomer.getOtherItems();
                    if (existingItems != null && !existingItems.isEmpty()) {
                        boolean itemUpdated = false;
                        Iterator<OtherItem> iterator = existingItems.iterator();

                        while (iterator.hasNext()) {
                            OtherItem otherItem = iterator.next();
                            if ((otherItem.getSource_name().equalsIgnoreCase("customer profile update page")) &&
                                    otherItem.getField_name().equalsIgnoreCase("reserve_category")) {
                                if (!details.containsKey("otherCategory")) {
                                    throw new IllegalArgumentException("You have to enter text for other reserved category");
                                }
                                otherItem.setTyped_text(details.get("otherCategory").toString());
                                otherItem.setSource_name("customer profile update page");
                                entityManager.merge(otherItem);
                                itemUpdated = true;
                            }
                        }

                        if (!itemUpdated) {
                            categoryOtherItemToAdd = sharedUtilityService.handleOtherCaseForReserveCategory(
                                    details.get("category").toString(), (String) details.get("otherCategory"), roleId, customerId, "customer profile update page");
                            existingItems.add(categoryOtherItemToAdd);
                        }
                    } else {
                        if (existingItems == null) {
                            existingItems = new ArrayList<>();
                        }
                        categoryOtherItemToAdd = sharedUtilityService.handleOtherCaseForReserveCategory(
                                details.get("category").toString(), (String) details.get("otherCategory"), roleId, customerId, "customer profile update page");
                        existingItems.add(categoryOtherItemToAdd);
                    }

                    customCustomer.setOtherItems(existingItems);
                    customCustomer.setOtherCategory((String) details.get("otherCategory"));
//                    entityManager.merge(customCustomer);
                }
//
            } else if (!details.containsKey("category")) {
                if (customCustomer.getCategory() != null) {
                    if (customCustomer.getCategory().equalsIgnoreCase("GEN")) {
                        customCustomer.setCategoryIssueDate(null);
                        customCustomer.setCategoryValidUpto(null);
                    }
                }
            }
            if (details.containsKey("otherCategory")) {
                details.remove("otherCategory");
            }
            // Update address if needed
            if (details.containsKey("categoryIssueDate") && details.containsKey("categoryValidUpto")) {

                if (sharedUtilityService.validateCategoryIssueAndValidUptoDates((String) details.get("categoryIssueDate"), (String) details.get("categoryValidUpto"), errorMessages)) {
                    if (details.containsKey("category")) {
                        if (!((String) details.get("category")).equalsIgnoreCase("GEN")) {
                            customCustomer.setCategoryIssueDate((String) details.get("categoryIssueDate"));
                            customCustomer.setCategoryValidUpto((String) details.get("categoryValidUpto"));
                        } else {
                            customCustomer.setCategoryIssueDate(null);
                            customCustomer.setCategoryValidUpto(null);
                        }
                    } else if (!details.containsKey("category")) {
                        if (!customCustomer.getCategory().equalsIgnoreCase("GEN")) {
                            customCustomer.setCategoryIssueDate((String) details.get("categoryIssueDate"));
                            customCustomer.setCategoryValidUpto((String) details.get("categoryValidUpto"));
                        } else {
                            customCustomer.setCategoryIssueDate(null);
                            customCustomer.setCategoryValidUpto(null);
                        }
                    }
                }

            } else if (details.containsKey("categoryIssueDate")) {

                if (sharedUtilityService.validateCategoryIssueDate((String) details.get("categoryIssueDate"), customCustomer, errorMessages)) {
                    if (details.containsKey("category")) {
                        if (!((String) details.get("category")).equalsIgnoreCase("GEN")) {
                            customCustomer.setCategoryIssueDate((String) details.get("categoryIssueDate"));
                        } else {
                            customCustomer.setCategoryIssueDate(null);
                            customCustomer.setCategoryValidUpto(null);
                        }
                    } else if (!details.containsKey("category")) {
                        if (!customCustomer.getCategory().equalsIgnoreCase("GEN")) {
                            customCustomer.setCategoryIssueDate((String) details.get("categoryIssueDate"));
                        } else {
                            customCustomer.setCategoryIssueDate(null);
                            customCustomer.setCategoryValidUpto(null);
                        }
                    }

                }
            } else if (details.containsKey("categoryValidUpto")) {

                if (sharedUtilityService.validateCategoryUptoDate((String) details.get("categoryValidUpto"), customCustomer, errorMessages)) {
                    if (details.containsKey("category")) {
                        if (!((String) details.get("category")).equalsIgnoreCase("GEN")) {
                            customCustomer.setCategoryValidUpto((String) details.get("categoryValidUpto"));
                        } else {
                            customCustomer.setCategoryIssueDate(null);
                            customCustomer.setCategoryValidUpto(null);
                        }
                    } else if (!details.containsKey("category")) {
                        if (!customCustomer.getCategory().equalsIgnoreCase("GEN")) {
                            customCustomer.setCategoryValidUpto((String) details.get("categoryValidUpto"));
                        } else {
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
                                errorMessages.put("disabilityPercentage", "disability percentage must be in range 1-100");
                            }
                            customCustomer.setDisabilityPercentage(disabilityPercentage);
                        }
                    } else {
                        errorMessages.put("disabilityType", "disability type is mandatory when disability is given");
                    }
                } else {
                    List<Document> customerDocuments = customCustomer.getDocuments();
                    for (Document document : customerDocuments) {
                        if (document.getIsArchived().equals(false)) {
                            if (document.getCustom_customer().getId().equals(customerId)) {
                                if (document.getDocumentType().getDocument_type_id().equals(11)) {
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
                errorMessages.put("disability", "disability must be given in order to give disability Type");
            } else if (details.containsKey("disabilityPercentage")) {
                errorMessages.put("disability", "disability must be given in order to give disability percentage");
            }

            if (details.containsKey("workExperienceScopeId")) {
                Long scopeId = Long.parseLong(details.get("workExperienceScopeId").toString());
                CustomApplicationScope customApplicationScope = applicationScopeService.getApplicationScopeById(scopeId);
                if (customApplicationScope == null) {
                    errorMessages.put("workExperienceScopeId", "No Application scope found with this id");
                }
                customCustomer.setWorkExperienceScopeId(customApplicationScope);
                if (details.containsKey("workExperience")) {
                    Integer workExperience = Integer.parseInt(details.get("workExperience").toString());
                    if (workExperience == 0) {
                        customCustomer.setWorkExperienceScopeId(null);
                    }
                    customCustomer.setWorkExperience(workExperience);
                } else if (customCustomer.getWorkExperience() < 1) {
                    customCustomer.setWorkExperienceScopeId(null);
                }
            }

            if (isValidDate != null && isValidDate.equals(true)) {
                if (!errorMessages.isEmpty()) {
                    String lastKey = null;
                    for (String key : errorMessages.keySet()) {
                        lastKey = key; // the last one in iteration will be the most recently added
                    }
                    if (lastKey != null) {
                        errorMessages.remove(lastKey);
                    }
                }
            }
            if (isValidDateDomicile != null && isValidDateDomicile.equals(true)) {
                if (!errorMessages.isEmpty()) {
                    String lastKey = null;
                    for (String key : errorMessages.keySet()) {
                        lastKey = key;
                    }
                    if (lastKey != null) {
                        errorMessages.remove(lastKey);
                    }
                }
            }
         /*   if (!errorMessages.isEmpty()) {
                Map.Entry<String, String> firstError = errorMessages.entrySet().iterator().next();
                String message = firstError.getValue();
                String field = firstError.getKey();
                return ResponseService.generateSuccessResponse(message, field, HttpStatus.BAD_REQUEST);
            }*/
          /*  if (!errorMessages.isEmpty()) {
                return ResponseService.generateErrorResponse("List of Failed validations: " + errorMessages.toString(), HttpStatus.BAD_REQUEST);
            }*/

            if (!errorMessages.isEmpty()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                String message = String.join(", ", errorMessages.values());
                return ResponseService.generateSuccessResponse(message, errorMessages.keySet(), HttpStatus.BAD_REQUEST);
            }
            customCustomer.setModifiedById(tokenUserId);
            customCustomer.setModifiedByRole(roleId);
            customCustomer.getAuditable().setDateUpdated(new Date());
            if (details.containsKey("isAcknowledged")) {
                Boolean value = (Boolean) details.get("isAcknowledged");
                customCustomer.setIsAcknowledged(value);
            }
            if (!customCustomer.getEmailActive() && customCustomer.getEmailAddress() != null && errorMessages.isEmpty()) {
                customCustomer.setEmailActive(true);
                em.merge(customCustomer);
                List<String> email = new ArrayList<>();
                email.add(customCustomer.getEmailAddress());
                Customer customer = customerService.readCustomerById(customCustomer.getId());
                String welcomeMessage = String.format(WELCOME_BODY_TEMPLATE, customer.getFirstName() + " " + customer.getLastName());
                CompletableFuture.runAsync(() -> {
                    try {
                        emailService.sendEmailWithAttachments(email, Constant.WELCOME_SUBJECT, welcomeMessage, null);
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                em.merge(customCustomer);
            }
            customCustomer.setModifiedByRole(roleId);
            customCustomer.setModifiedById(tokenUserId);
            entityManager.merge(customCustomer);
            return ResponseService.generateSuccessResponse("User details updated successfully", sharedUtilityService.breakReferenceForCustomer(customCustomer, authHeader, httpServletRequest), HttpStatus.OK);

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            return ResponseService.generateSuccessResponse(illegalArgumentException.getMessage(), "illegalArgumentException", HttpStatus.BAD_REQUEST);
        } catch (ClassCastException classCastException) {
            exceptionHandling.handleException(classCastException);
            return ResponseService.generateSuccessResponse("Invalid Casting: " + classCastException.getMessage(), "classCastException", HttpStatus.BAD_REQUEST);
        } catch (ParseException parseException) {
            exceptionHandling.handleException(parseException);
            return ResponseService.generateSuccessResponse("Unparsable Exception: " + parseException.getMessage(), "parseException", HttpStatus.BAD_REQUEST);
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            exceptionHandling.handleException(dataIntegrityViolationException);
            return ResponseService.generateSuccessResponse("Error updating " + dataIntegrityViolationException.getMessage(), "dataIntegrityViolation", HttpStatus.BAD_REQUEST);
        } catch (ConstraintViolationException constraintViolationException) {
            exceptionHandling.handleException(constraintViolationException);
            return ResponseService.generateSuccessResponse("Error updating " + constraintViolationException.getMessage(), "constraintViolation", HttpStatus.BAD_REQUEST);
        } catch (NoSuchFieldException noSuchFieldException) {
            exceptionHandling.handleException(noSuchFieldException);
            return ResponseService.generateSuccessResponse("No such field present :" + noSuchFieldException.getMessage(), "invalidField", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateSuccessResponse("Error updating " + exception.getMessage(), "generalException", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Map<String, String> validateHidePhoneNumber(Map<String, Object> details, CustomCustomer customer) {
        Map<String, String> errorMessages = new HashMap<>();
        details = sanitizerService.sanitizeInputMap(details);

        if (((Boolean) details.get("hidePhoneNumber")).equals(true)) {

            if (details.containsKey("secondaryMobileNumber") && ((String) details.get("secondaryMobileNumber")).isEmpty()) {
                errorMessages.put("secondaryMobileNumber", "Need to provide Secondary Mobile Number when hiding primary Mobile Number");
            }

            if (details.containsKey("whatsappNumber") && ((String) details.get("whatsappNumber")).isEmpty()) {
                errorMessages.put("whatsappNumber", "Whatsapp number cannot be null");
            }
            if (details.containsKey("whatsappNumber") && ((String) details.get("whatsappNumber")).equals(customer.getMobileNumber())) {
                errorMessages.put("whatsappNumber", "Cannot set primary number as whatsapp number when hidden");
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
    public ResponseEntity<?> getUserDetails(@PathVariable Long customerId, @RequestHeader(value = "Authorization") String authHeader, HttpServletRequest httpServletRequest, @RequestParam(required = false) Long ticketId) {
        try {
            String jwtToken = authHeader.substring(7);
            List<String> deleteLogs = new ArrayList<>();
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);
            //checking for super admin and admin
            if ((role.getRole_name().equals(roleUser) && !Objects.equals(tokenUserId, customerId))/*||role.getRole_name().equals(roleServiceProvider)*/)
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
            if (role.getRole_name().equals(roleServiceProvider) && ticketId != null) {
                CustomServiceProviderTicket ticket = em.find(CustomServiceProviderTicket.class, ticketId);
                if (ticket == null)
                    return ResponseService.generateErrorResponse("Invalid ticket", HttpStatus.BAD_REQUEST);
                Order order = orderService.findOrderById(ticket.getOrder().getId());
                if (!ticket.getAssignee().equals(tokenUserId) || !order.getCustomer().getId().equals(customerId) || (ticket.getTicketState().getTicketStateId().equals(TICKET_STATE_IN_REVIEW) || ticket.getTicketState().getTicketStateId().equals(TICKET_STATE_CLOSE)))
                    return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
            } else if (role.getRole_name().equals(roleServiceProvider)) {
                ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, tokenUserId);
                if (serviceProvider == null)
                    return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
                System.out.println("here");
                Query query = entityManager.createNativeQuery("Select count(customer_id) from customer_referrer where customer_id = :cid and service_provider_id =:sid");
                query.setParameter("cid", customCustomer.getId());
                query.setParameter("sid", serviceProvider.getService_provider_id());
                if (((BigInteger) query.getSingleResult()).intValue() == 0)
                    return ResponseService.generateErrorResponse("Forbidden Access", HttpStatus.FORBIDDEN);
                System.out.println(((BigInteger) query.getSingleResult()).intValue());
            }
            if (customCustomer == null) {
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            }
            if (customCustomer.getArchived() != null) {
                if (customCustomer.getArchived().equals(true) && !role.getRole_name().equals(SUPER_ADMIN) && !role.getRole_name().equals(ADMIN) && !role.getRole_name().equals(SERVICE_PROVIDER)) {
                    return ResponseService.generateErrorResponse("Your account is suspended. Please contact support.", HttpStatus.FORBIDDEN);
                }
            }
            CustomerImpl customer = em.find(CustomerImpl.class, customerId);  // Assuming you retrieve the base Customer entity
            Map<String, Object> customerDetails = sharedUtilityService.breakReferenceForCustomer(customer, authHeader, httpServletRequest);

            return responseService.generateSuccessResponse("User details retrieved successfully", customerDetails, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving user details", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Authorize(value = {Constant.roleUser, Constant.roleServiceProvider, Constant.roleSuperAdmin, Constant.roleAdmin, Constant.roleAdminServiceProvider})
    @PostMapping("/upload-documents")
    public ResponseEntity<?> uploadDocuments(
            @RequestParam Long customerId,
            @RequestParam(value = "extUpdate", defaultValue = "false", required = false) Boolean extUpdate,
            @RequestHeader(value = "extAuth", required = false) String extAuth,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam("fileTypes") List<Integer> fileTypes,
            @RequestParam(value = "qualificationDetailId", required = false) Long qualificationDetailId,
            @RequestParam(value = "dateOfIssue", required = false) String dateOfIssue,
            @RequestParam(value = "validUpto", required = false) String validUpto,
            @RequestParam(value = "otherDocument", required = false) String otherDocument,
            @RequestParam(value = "removeFileTypes", required = false) Boolean removeFileTypes,
            @RequestHeader(value = "Authorization") String authHeader) {
        try {

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseService.generateErrorResponse("Authorization header is missing or invalid.", HttpStatus.UNAUTHORIZED);
            }

            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long userId=jwtTokenUtil.extractId(jwtToken);
            if(roleId==5&&!userId.equals(customerId))
            {
                return ResponseService.generateErrorResponse("Forbidden",HttpStatus.FORBIDDEN);
            }

            if((extUpdate!=null&&extUpdate)&&roleId==4)
            {
                if(customerId==null)
                    return ResponseService.generateErrorResponse("Id not provided",HttpStatus.NOT_FOUND);
                CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customerId);
                if(customCustomer==null)
                    return ResponseService.generateErrorResponse("Customer not found",HttpStatus.NOT_FOUND);
                ExternalUseToken externalUseToken=entityManager.find(ExternalUseToken.class,userId);
                if(externalUseToken==null||externalUseToken.getToken()==null||externalUseToken.getToken().isEmpty())
                    return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.FORBIDDEN);
                if(jwtTokenUtil.extractId(externalUseToken.getToken()).equals(customerId))
                    roleId=5;
                else
                    return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.FORBIDDEN);
            }

            String role = null;
            if (extUpdate) {
                role = roleUser;
            } else {
                role = roleService.getRoleByRoleId(roleId).getRole_name();
            }
            if (!role.equals(roleUser)) {
                role = roleServiceProvider;
            }

            //**********DELETE DOCUMENT :START*********
            if (removeFileTypes != null && removeFileTypes.equals(true)) {
                List<String> deleteLogs = documentStorageService.deleteDocument(role, fileTypes, customerId, otherDocument, qualificationDetailId);
                return ResponseService.generateSuccessResponse("Document deleted successfully", deleteLogs, HttpStatus.OK);
            }
            //*******DELETE DOCUMENT :END**********

            //*******UPLOAD DOCUMENT START*******
            if (customerId == null || files == null || fileTypes == null) {
                return ResponseService.generateErrorResponse("Invalid request parameters.", HttpStatus.BAD_REQUEST);
            }

            if (role == null) {
                return ResponseService.generateErrorResponse("Role not found for this user.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (!customerId.equals(userId) && (roleId != 1 && roleId != 2)&&!extUpdate) {
                return ResponseService.generateErrorResponse("Forbidden Access.", HttpStatus.FORBIDDEN);
            }

            // Grouping of list of files w.r.t document type here (document_type is file_type which is naming convention issue).
            Map<Integer, List<MultipartFile>> groupedFiles = new HashMap<>();
            for (int i = 0; i < files.size(); i++) {
                Integer fileTypeId = fileTypes.get(0); // here fileType id meaning documentTypeId
                MultipartFile file = files.get(i);
                groupedFiles.computeIfAbsent(fileTypeId, k -> new ArrayList<>()).add(file);
            }

            // Will run for customer OR admin and super admin with extUpdate set to true only
            if (roleService.findRoleName(roleId).equals(roleUser) || ((roleService.findRoleName(roleId).equals(roleSuperAdmin) || roleService.findRoleName(roleId).equals(roleAdmin)) && extUpdate)) {
                // Keep track of documents to be saved
                HashSet<Document> documentsToSave = new HashSet<>();
                Map<String, Object> responseData = customCustomerService.updateCustomerDocument(groupedFiles, customerId, otherDocument, qualificationDetailId, dateOfIssue, validUpto, role, removeFileTypes, documentsToSave);
                return ResponseService.generateSuccessResponse("Documents updated successfully", responseData, HttpStatus.OK);
            } else {
                // Keep track of documents to be saved
                Set<ServiceProviderDocument> serviceProviderDocumentToSave = new HashSet<>();
                Map<String, Object> responseData = serviceProviderService.updateServiceProviderDocument(groupedFiles, customerId, otherDocument, qualificationDetailId, dateOfIssue, validUpto, role, removeFileTypes, serviceProviderDocumentToSave);
                return ResponseService.generateSuccessResponse("Documents uploaded successfully", responseData, HttpStatus.OK);
            }
            //*******UPLOAD DOCUMENT END

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
    public ResponseEntity<?> updateCustomerUsername(@RequestBody Map<String, Object> updates, @RequestParam Long customerId, @RequestHeader(value = "Authorization") String authHeader, HttpServletRequest httpServletRequest) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);

            //checking for super admin and admin
            if ((role.getRole_name().equals(roleUser) && !Objects.equals(tokenUserId, customerId)) || role.getRole_name().equals(roleServiceProvider))

                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
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
            if (customer.getUsername() != null) {
                return ResponseService.generateErrorResponse("Username cannot be changed once created", HttpStatus.BAD_REQUEST);
            }
            Customer existingCustomerByUsername = null;
            Query query = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM blc_customer WHERE LOWER(username) = :username"
            );
            query.setParameter("username", username.toLowerCase());

            Number count = (Number) query.getSingleResult();
            if (count.intValue() > 0) {
                return ResponseService.generateErrorResponse(
                        "Username already exists",
                        HttpStatus.BAD_REQUEST
                );
            }
            existingCustomerByUsername = customerService.readCustomerByUsername(username);

            if ((existingCustomerByUsername != null) && !existingCustomerByUsername.getId().equals(customerId)&&(username).equals(existingCustomerByUsername.getUsername())) {
                return ResponseService.generateErrorResponse("Username is not available", HttpStatus.BAD_REQUEST);

            } else {
                if (customer.getUsername() != null && customer.getUsername().equals(username))
                    return ResponseService.generateErrorResponse("Old and new username cannot be same", HttpStatus.BAD_REQUEST);
                customer.setUsername(username);
                em.merge(customer);
                return ResponseService.generateSuccessResponse("User name  updated successfully : ", sharedUtilityService.breakReferenceForCustomer(customer, authHeader, httpServletRequest), HttpStatus.OK);

            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Error updating username", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @Authorize(value = {Constant.roleUser, roleAdmin, roleSuperAdmin})
    @RequestMapping(value = "create-or-update-password", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerPassword(@RequestBody Map<String, Object> details, @RequestParam Long customerId, @RequestHeader(value = "Authorization") String authHeader, HttpServletRequest httpServletRequest) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);

            //checking for super admin and admin
            if ((role.getRole_name().equals(roleUser) && !Objects.equals(tokenUserId, customerId)) || role.getRole_name().equals(roleServiceProvider))
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
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

                    CustomCustomer customCustomer = customCustomerService.findCustomCustomerById(customer.getId());
                    customCustomer.setIsPasswordCreated(true);
                    em.merge(customCustomer);
                    return ResponseService.generateSuccessResponse("Password Created", sharedUtilityService.breakReferenceForCustomer(customer, authHeader, httpServletRequest), HttpStatus.OK);
                }
                if (!passwordEncoder.matches(password, customer.getPassword())) {
                    customer.setPassword(passwordEncoder.encode(password));
                    em.merge(customer);


                    return ResponseService.generateSuccessResponse("Password Updated", sharedUtilityService.breakReferenceForCustomer(customer, authHeader, httpServletRequest), HttpStatus.OK);
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

            addressDetails.remove("has_state_category");
            addressDetails.remove("state_category");
            addressDetails.remove("category_state_name");
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
                customCustomer.setIsAcknowledged(false);
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

    public ResponseEntity<?> createAuthResponse(String token, Customer customer, String authHeader, HttpServletRequest httpServletRequest) throws Exception {
        OtpEndpoint.ApiResponse authResponse = new OtpEndpoint.ApiResponse(token, sharedUtilityService.breakReferenceForCustomer(customer, authHeader, httpServletRequest), HttpStatus.OK.value(), HttpStatus.OK.name(), "User has been logged in");
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
                                           @RequestParam(value = "limit", defaultValue = "30") int limit, @RequestHeader(value = "Authorization") String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);

            //checking for super admin and admin
            if ((role.getRole_name().equals(roleUser) && !Objects.equals(tokenUserId, customer_id)) || role.getRole_name().equals(roleServiceProvider))
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
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

            List<CustomProduct> listOfSavedProducts = new ArrayList<>();

            for (Product product : customer.getSavedForms()) {
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());
                if (customProduct != null
                        && ((Status) customProduct).getArchived() == 'Y'
                        || customProduct.getActiveEndDate().before(new Date())) {
                    continue;
                }
               /* CustomProductWrapper customProductWrapper = new CustomProductWrapper();
                customProductWrapper.wrapDetails(customProduct, request, reserveCategoryService, reserveCategoryAgeService, genderService, customer, sharedUtilityService);*/
                listOfSavedProducts.add(customProduct);
            }

            return getSavedFormsWrapper(customer_id, listOfSavedProducts, offset, limit);

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

    @GetMapping(value = "/forms/show-applied-forms")
    @Transactional
    public ResponseEntity<?> getFilledFormsByUserId(HttpServletRequest request,
                                                    @RequestParam long customer_id,
                                                    @RequestParam(value = "offset", defaultValue = "0") int offset,
                                                    @RequestParam(value = "limit", defaultValue = "10") int limit,
                                                    @RequestHeader(value = "Authorization") String authHeader,
                                                    @RequestParam(value = "unique_products", required = false, defaultValue = "true") boolean uniqueProducts) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);

            // Authorization check
            if ((role.getRole_name().equals(roleUser) && !Objects.equals(tokenUserId, customer_id)) || role.getRole_name().equals(roleServiceProvider))
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);

            // Validate pagination
            if (offset < 0) throw new IllegalArgumentException("Offset cannot be negative");
            if (limit <= 0) throw new IllegalArgumentException("Limit must be positive");

            // Validate customer
            CustomCustomer customer = entityManager.find(CustomCustomer.class, customer_id);
            if (customer == null)
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);

            // Broadleaf-compliant query
            String queryString;
            if (uniqueProducts) {
                queryString = "SELECT t.order_id FROM (" +
                        "SELECT o.order_id, oi.order_item_id, o.submit_date, " +
                        "attr.value as product_id, " +
                        "ROW_NUMBER() OVER (PARTITION BY attr.value ORDER BY o.submit_date DESC) as rn " +
                        "FROM BLC_ORDER o " +
                        "JOIN BLC_ORDER_ITEM oi ON o.order_id = oi.order_id " +
                        "JOIN BLC_ORDER_ITEM_ATTRIBUTE attr ON oi.order_item_id = attr.order_item_id " +
                        "WHERE o.customer_id = :customerId " +
                        "AND o.order_status != 'FAILED' " +
                        "AND attr.name = 'productId'" +
                        ") t WHERE t.rn = 1";
            } else {
                queryString = "SELECT DISTINCT o.order_id, o.submit_date FROM BLC_ORDER o " + // Added submit_date to SELECT
                        "JOIN BLC_ORDER_ITEM oi ON o.order_id = oi.order_id " +
                        "JOIN BLC_ORDER_ITEM_ATTRIBUTE attr ON oi.order_item_id = attr.order_item_id " +
                        "WHERE o.customer_id = :customerId " +
                        "AND o.order_status != 'FAILED' " +
                        "AND attr.name = 'productId' " +
                        "ORDER BY o.submit_date DESC";
            }

            Query query = entityManager.createNativeQuery(queryString);
            query.setParameter("customerId", customer_id);

            List<Object[]> resultList = query.getResultList();

            List<BigInteger> orderIds = new ArrayList<>();

            if (uniqueProducts) {
                for (Object row : resultList) {
                    BigInteger orderId = (BigInteger) row;
                    orderIds.add(orderId);
                }
            } else {
                for (Object row : resultList) {
                    Object[] columns = (Object[]) row;
                    BigInteger orderId = (BigInteger) columns[0];
                    orderIds.add(orderId);
                }
            }
            List<CustomProductWrapper> appliedForms = new ArrayList<>();
            Set<Long> processedProductIds = new HashSet<>();

            for (BigInteger id : orderIds) {
                Order order = orderService.findOrderById(id.longValue());
                if (order == null || order.getOrderItems().isEmpty()) continue;

                // Get productId from OrderItemAttributes (Map<String, OrderItemAttribute>)
                Long productId = null;
                OrderItem firstItem = order.getOrderItems().get(0);
                Map<String, OrderItemAttribute> attributes = firstItem.getOrderItemAttributes();

                if (attributes != null) {
                    OrderItemAttribute productIdAttr = attributes.get("productId");
                    if (productIdAttr != null) {
                        productId = Long.parseLong(productIdAttr.getValue());
                    }
                }

                if (productId == null) continue;

                // Skip duplicates when unique_products=true
                if (uniqueProducts && processedProductIds.contains(productId)) {
                    continue;
                }

                CustomProduct product = entityManager.find(CustomProduct.class, productId);
                if (product != null && product.getArchived() != 'Y') {
                    CustomProductWrapper wrapper = new CustomProductWrapper();
                    wrapper.wrapDetails(order.getId(), product, request, reserveCategoryService,
                            reserveCategoryAgeService, genderService,
                            customer, sharedUtilityService);
                    appliedForms.add(wrapper);
                    processedProductIds.add(productId);
                }
            }

            // Pagination
            int totalItems = appliedForms.size();
            int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / limit);
            if (offset >= totalPages && offset != 0)
                return ResponseService.generateErrorResponse("No more forms available", HttpStatus.BAD_REQUEST);

            List<CustomProductWrapper> paginatedList = appliedForms.stream()
                    .skip(offset * limit)
                    .limit(limit)
                    .collect(Collectors.toList());

            // Response
            Map<String, Object> response = new HashMap<>();
            response.put("forms", paginatedList);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);
            response.put("uniqueProducts", uniqueProducts);

            return ResponseService.generateSuccessResponse("Forms retrieved successfully", response, HttpStatus.OK);

        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customer ID format", HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving forms", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping(value = "/forms/show-recommended-forms")
    public ResponseEntity<?> getRecommendedFormsByUserId(HttpServletRequest request,
                                                         @RequestParam long customer_id,
                                                         @RequestParam(value = "offset", defaultValue = "0") int offset,
                                                         @RequestParam(value = "limit", defaultValue = "30") int limit, @RequestHeader(value = "Authorization") String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);

            //checking for super admin and admin
            if ((role.getRole_name().equals(roleUser) && !Objects.equals(tokenUserId, customer_id)) || role.getRole_name().equals(roleServiceProvider))
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);

            if (offset < 0) {
                throw new IllegalArgumentException("Offset for pagination cannot be a negative number");
            }
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit for pagination cannot be a negative number or 0");
            }

            CustomCustomer customer = entityManager.find(CustomCustomer.class, customer_id);
            if (customer.getCategory() == null || customer.getCategory().isEmpty() || customer.getGender() == null || customer.getGender().isEmpty())
                return ResponseService.generateErrorResponse("Need to provide Category and Gender to enable Recommendations", HttpStatus.OK);
            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer with this id not found", HttpStatus.NOT_FOUND);
            }

            return getRecos(customer_id, offset, limit);
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
@Autowired
    AcknowledgementWebhook webhook;
    @PostMapping("/submit-customer-details/{customerId}")
    public ResponseEntity<?> submitCustomerDetails(@PathVariable Long customerId, @RequestHeader(value = "Authorization") String authHeader, HttpServletRequest httpServletRequest) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);

            if(roleId==4)
            {
                if(customerId==null)
                    return ResponseService.generateErrorResponse("Id not provided",HttpStatus.NOT_FOUND);
                ExternalUseToken externalUseToken=entityManager.find(ExternalUseToken.class,tokenUserId);
                if(externalUseToken==null||externalUseToken.getToken()==null||externalUseToken.getToken().isEmpty())
                    return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.UNAUTHORIZED);
                if(!jwtTokenUtil.extractId(externalUseToken.getToken()).equals(customerId))
                    return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.UNAUTHORIZED);
            } else if((roleId == 5 && !tokenUserId.equals(customerId))) {
                return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.UNAUTHORIZED);
            }
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customerId);
            if (customCustomer == null) {
                throw new IllegalArgumentException("Customer with id " + customerId + " not found");
            }
            if (!sharedUtilityService.validateCustomerPersonalDetails(customCustomer)) ;
            {
                customCustomer.setProfileComplete(false);
            }
            if (!sharedUtilityService.validateCustomerContactDetails(customCustomer)) ;
            {
                customCustomer.setProfileComplete(false);
            }
            if (!sharedUtilityService.validatePhysicalDetails(customCustomer)) ;
            {
                customCustomer.setProfileComplete(false);
            }
            if (!sharedUtilityService.validateMiscellaniousDetails(customCustomer)) ;
            {
                customCustomer.setProfileComplete(false);
            }
            if (!sharedUtilityService.validateDocumentsDetails(customCustomer)) ;
            {
                customCustomer.setProfileComplete(false);
            }
            customCustomer.setProfileComplete(true);
            /*if(!webhook.checkRef(customerId,5))
            {
              return ResponseService.generateErrorResponse("User has not acknowledged the policy",HttpStatus.BAD_REQUEST);
            }
            UserAcknowledgement userAcknowledgement=new UserAcknowledgement();
            userAcknowledgement.setAcknowledgedAt(new Date());
            userAcknowledgement.setUserId(customerId);
            userAcknowledgement.setAcknowledgementVersion("v.1");
            entityManager.persist(userAcknowledgement);*/
            return ResponseService.generateSuccessResponse("User details submitted successfully", sharedUtilityService.breakReferenceForCustomer(customCustomer, authHeader, httpServletRequest), HttpStatus.OK);
        } catch (NumberFormatException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
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
            @RequestParam(defaultValue = "30") int limit,
            @RequestHeader(value = "Authorization") String authHeader, HttpServletRequest httpServletRequest) {
        try {
            if (offset < 0) {
                throw new IllegalArgumentException("Offset for pagination cannot be a negative number");
            }
            if (limit <= 0) {
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
            if (offset >= totalPages && offset != 0) {
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
    public ResponseEntity<?> setReferrerForCustomer(@PathVariable Long customer_id, @PathVariable Long service_provider_id, @RequestParam(value = "primary_referee", required = false, defaultValue = "true") Boolean primaryReferee, @RequestHeader(value = "Authorization") String authHeader) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);

            //checking for super admin and admin
            if ((role.getRole_name().equals(roleUser) && !Objects.equals(tokenUserId, customer_id)))
                return ResponseService.generateErrorResponse("Forbidden", HttpStatus.FORBIDDEN);
            if(role.getRole_name().equals(roleServiceProvider)) {
                ExternalUseToken externalUseToken = entityManager.find(ExternalUseToken.class, tokenUserId);
                if (externalUseToken == null || externalUseToken.getToken() == null || externalUseToken.getToken().isEmpty())
                    return ResponseService.generateSuccessResponse("Forbidden Access", "role", HttpStatus.UNAUTHORIZED);
                }

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
            CustomerReferrer existingRef = null;
            for (CustomerReferrer customerReferrer : referrerSp) {
                if (customerReferrer.getPrimaryRef() != null && customerReferrer.getPrimaryRef()) {
                    primaryRef = customerReferrer;
                }
                if (customerReferrer.getServiceProvider().getService_provider_id().equals(service_provider_id) && !primaryReferee) {
                    return ResponseService.generateErrorResponse("Selected Service Provider already set as Referrer", HttpStatus.BAD_REQUEST);
                }
                if (service_provider_id.equals(customerReferrer.getServiceProvider().getService_provider_id())) {
                    existingRef = customerReferrer;
                }
            }
            if (customCustomer.getPrimaryRef() == 0 || (customCustomer.getRegisteredBySp() && customCustomer.getCreatedByRole() != 4) || (customCustomer.getCreatedByRole()) == 5) {
                if (primaryRef != null && primaryRef.getServiceProvider().getService_provider_id().equals(service_provider_id)) {
                    throw new IllegalArgumentException("Selected Service Provider already set as Primary Referrer");
                }
                customCustomer.setPrimaryRef(service_provider_id);
            }
            if (!referrerSp.isEmpty() && primaryRef != null) {
                primaryRef.setPrimaryRef(false);
                entityManager.merge(primaryRef);
            }
            if (existingRef != null && primaryReferee) {
                existingRef.setPrimaryRef(true);
                entityManager.merge(existingRef);
            } else {
                CustomerReferrer customerReferrer = new CustomerReferrer();
                customerReferrer.setPrimaryRef(true); // by raman and Kshitij will solve the complete issue of last referrer as primary referee.;
                customerReferrer.setCustomer(customCustomer);
                customerReferrer.setServiceProvider(serviceProvider);
                customCustomer.getMyReferrer().add(customerReferrer);
                customerReferrer.setCreatedAt(LocalDateTime.now());
                entityManager.persist(customerReferrer);
            }

            entityManager.merge(customCustomer);
            return ResponseService.generateSuccessResponse("Referrer Set", sharedUtilityService.serviceProviderDetailsMap(serviceProvider, false), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
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

    public Boolean validateDate(String dateOfIssueStr, String validUptoStr, String dateFormatInString, Map<String, String> errorMessages, String dateOfIssueFieldName, String dateOfExpireFieldName) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatInString);
        dateFormat.setLenient(false);

        try {
            // Validate format
            if (!isValidDateFormat(dateOfIssueStr, dateFormat)) {
                errorMessages.put(dateOfIssueFieldName, "Date of Issue must be in " + dateFormatInString + " format");
            }

            Date dateOfIssue = dateFormat.parse(dateOfIssueStr);
            Date validUpto = null;
            if (validUptoStr != null) {

                if (!isValidDateFormat(validUptoStr, dateFormat)) {
                    errorMessages.put(dateOfExpireFieldName, "Valid Upto Date must be in " + dateFormatInString + " format");
                }
                validUpto = dateFormat.parse(validUptoStr);

                // Check if validUpto is before dateOfIssue
                if (validUpto.before(dateOfIssue)) {
                    errorMessages.put(dateOfExpireFieldName, "Valid Upto Date cannot be before Date of Issue " + dateFormatInString + " format");
                }
            }
            return true;
        } catch (IllegalArgumentException ex) {
            exceptionHandlingService.handleException(ex);
            throw new IllegalArgumentException(ex.getMessage());
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
    public ResponseEntity<?> filterCustomer(@RequestParam(required = false) List<String> name, @RequestParam(required = false) List<Long> ref, @RequestParam(required = false) List<Integer> stateId, @RequestParam(required = false) List<Integer> districtId, @RequestParam(required = false) List<Integer> qualificationType, @RequestParam(required = false) String username, @RequestParam(required = false) Boolean completed, @RequestParam(required = false, defaultValue = "false") Boolean suspended, @RequestHeader(value = "Authorization") String authHeader, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "30") int limit, @RequestParam(required = false, defaultValue = "DESC") String sortOrder) throws Exception {
        /* try {*/
        if (!sortOrder.equals("DESC") && !sortOrder.equals("ASC"))
            return ResponseService.generateErrorResponse("Invalid sortOrder filter", HttpStatus.BAD_REQUEST);
        List<Long> refereeId = null;
        if (ref != null)
            refereeId = ref;
        else
            refereeId = new ArrayList<>();
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
        List<String> stateNames = new ArrayList<>();
        List<String> districtNames = new ArrayList<>();
        List<Long> qualificationNames = new ArrayList<>();
        List<String> qualificationStrings = new ArrayList<>();
        if (stateId != null) {
            for (Integer stateCode : stateId) {
                stateName = districtService.findStateById(stateCode);
                if (stateName == null)
                    return ResponseService.generateErrorResponse("Invalid state Id", HttpStatus.BAD_REQUEST);
                stateNames.add(stateName);
            }
        } else
            stateNames = null;
        if (districtId != null) {
            for (Integer district : districtId) {
                districtName = districtService.findDistrictById(district);
                if (districtName == null)
                    return ResponseService.generateErrorResponse("Invalid district Id", HttpStatus.BAD_REQUEST);
                districtNames.add(districtName);
            }
        } else
            districtNames = null;
        if (qualificationType != null) {
            for (Integer id : qualificationType) {
                if (qualificationService.getQualificationByQualificationId(Math.toIntExact(id)) == null)
                    return ResponseService.generateErrorResponse("Invalid qualification Id", HttpStatus.BAD_REQUEST);
                qualificationStrings.add(qualificationService.getQualificationByQualificationId(Math.toIntExact(id)).getQualification_name());
                qualificationNames.add(qualificationService.getQualificationByQualificationId(Math.toIntExact(id)).getOverlap());
            }
        } else
            qualificationNames = null;

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

        List<Long> refids = new ArrayList<>();
        if (refereeId != null && !refereeId.isEmpty()) {
            // Convert the list of Long to a list of String using Java Streams
            refids = refereeId.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }
        if (refids.isEmpty())
            refids = null;

        List<BigInteger> resultSet1 = customCustomerService.filterCustomer(refids, firstNames, lastNames, stateNames, districtNames, qualificationType, username, completed, authHeader, offset, limit, sortOrder);
        List<BigInteger> resultSet2 = customCustomerService.filterCustomer(refids, lastNames, firstNames, stateNames, districtNames, qualificationType, username, completed, authHeader, offset, limit, sortOrder);
        Set<BigInteger> uniqueResults = new HashSet<>();

        // Add all elements from both result sets
        uniqueResults.addAll(resultSet1);
        uniqueResults.addAll(resultSet2);
        List<BigInteger> uniqueResultList = new ArrayList<>(uniqueResults);
        System.out.println("count:" + uniqueResultList.size());

        //  Pre-load all state and district mappings
        Map<String, Integer> stateNameToIdCache = new HashMap<>();
        Map<String, Integer> districtNameToIdCache = new HashMap<>();

        // Collect all unique state and district names first
        Set<String> allStateNames = new HashSet<>();
        Set<String> allDistrictNames = new HashSet<>();

        for (BigInteger id : uniqueResultList) {
            try {
                Customer customer = customerService.readCustomerById(id.longValue());
                if (customer != null && customer.getCustomerAddresses() != null) {
                    for (CustomerAddress address : customer.getCustomerAddresses()) {
                        if (address == null || address.getAddress() == null) continue;

                        if ("PERMANENT_ADDRESS".equals(address.getAddressName()) ||
                                "CURRENT_ADDRESS".equals(address.getAddressName())) {

                            String state = address.getAddress().getStateProvinceRegion();
                            String district = address.getAddress().getCounty();

                            if (state != null) allStateNames.add(state);
                            if (district != null) allDistrictNames.add(district);
                        }
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }

        // Batch load all state and district IDs
        for (String stateNameKey : allStateNames) {
            try {
                Integer stateIdValue = districtService.findStateIdByName(stateNameKey);
                if (stateIdValue != null) {
                    stateNameToIdCache.put(stateNameKey, stateIdValue);
                }
            } catch (Exception e) {
                System.out.println("Error loading state: " + stateNameKey + " - " + e.getMessage());
            }
        }

        for (String districtNameKey : allDistrictNames) {
            try {
                Integer districtIdValue = districtService.findDistrictIdByName(districtNameKey);
                if (districtIdValue != null) {
                    districtNameToIdCache.put(districtNameKey, districtIdValue);
                }
            } catch (Exception e) {
                System.out.println("Error loading district: " + districtNameKey + " - " + e.getMessage());
            }
        }

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
            Customer customer = null;
            try {
                customer = customerService.readCustomerById(id.longValue());
            } catch (Exception exception) {
                log.error("Customer ID skipped: {} due to- {}", id, exception.getMessage());
                continue;
            }
            if (customer != null) {
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, id.longValue());
                CustomerBasicDetailsDto customerBasicDetailsDto = new CustomerBasicDetailsDto();
                String state = null;
                String primaryRefName = "N/A";
                Long primaryRefId = null;
                if (customCustomer != null) {
                    if (!customCustomer.getArchived().equals(suspended))
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
                    } else {

                        for (CustomerAddress customerAddress : customer.getCustomerAddresses()) {
                            if (customerAddress.getAddressName().equals("PERMANENT_ADDRESS") || customerAddress.getAddressName().equals("CURRENT_ADDRESS"))
                                state = customerAddress.getAddress().getStateProvinceRegion();
                        }
                        customerBasicDetailsDto.setState(state);
                    }

                    log.info("created Date: {}", customer.getAuditable().getDateCreated());
                    log.info("updated Date: {}", customer.getAuditable().getDateUpdated());

                    customerBasicDetailsDto.setCustomerId(id.longValue());
                    customerBasicDetailsDto.setEmail(customer.getEmailAddress());
                    customerBasicDetailsDto.setFullName(customer.getFirstName() + " " + customer.getLastName());
                    customerBasicDetailsDto.setGender(customCustomer.getGender());
                    customerBasicDetailsDto.setUsername(customer.getUsername());
                    customerBasicDetailsDto.setProfileComplete(customCustomer.getProfileComplete());
                    customerBasicDetailsDto.setSuspended(customCustomer.getArchived());

                    List<Long> qualificationDetailIds = new ArrayList<>();
                    List<Integer> qualificationIds = new ArrayList<>();

                    if (customCustomer.getQualificationDetailsList() != null && !customCustomer.getQualificationDetailsList().isEmpty()) {
                        for (QualificationDetails qualificationDetails : customCustomer.getQualificationDetailsList()) {
                            qualificationDetailIds.add(qualificationDetails.getQualification_detail_id());
                            qualificationIds.add(qualificationDetails.getQualification_id());
                        }
                    }

                    if (customer.getCustomerAddresses() != null) {
                        for (CustomerAddress address : customer.getCustomerAddresses()) {
                            if (address == null || address.getAddress() == null) continue;

                            if ("PERMANENT_ADDRESS".equals(address.getAddressName())) {
                                String permanentState = address.getAddress().getStateProvinceRegion();
                                String permanentDistrict = address.getAddress().getCounty();

                                // Use cache instead of database calls
                                if (permanentState != null) {
                                    Integer stateIdValue = stateNameToIdCache.get(permanentState);
                                    if (stateIdValue != null) {
                                        customerBasicDetailsDto.setPermanent_state_id(stateIdValue);
                                    }
                                }
                                if (permanentDistrict != null) {
                                    Integer districtIdValue = districtNameToIdCache.get(permanentDistrict);
                                    if (districtIdValue != null) {
                                        customerBasicDetailsDto.setPermanent_district_id(districtIdValue);
                                    }
                                }
                            } else if ("CURRENT_ADDRESS".equals(address.getAddressName())) {
                                String currentState = address.getAddress().getStateProvinceRegion();
                                String currentDistrict = address.getAddress().getCounty();

                                // Use cache instead of database calls
                                if (currentState != null) {
                                    Integer stateIdValue = stateNameToIdCache.get(currentState);
                                    if (stateIdValue != null) {
                                        customerBasicDetailsDto.setCurrent_state_id(stateIdValue);
                                    }
                                }
                                if (currentDistrict != null) {
                                    Integer districtIdValue = districtNameToIdCache.get(currentDistrict);
                                    if (districtIdValue != null) {
                                        customerBasicDetailsDto.setCurrent_district_id(districtIdValue);
                                    }
                                }
                            }
                        }
                    }

                    customerBasicDetailsDto.setQualification_detail_ids(qualificationDetailIds);
                    customerBasicDetailsDto.setQualification_ids(qualificationIds);

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
                        System.out.println("true" + customCustomer.getId());
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

//                    CODE TO IMPLEMENT THE HIGHEST QUALIFICATION FILTER
//                    int max = 0;
//                    if (!qualifications.isEmpty()) {
//                        for (QualificationDetails qualificationDetails : qualifications) {
//                            System.out.println("kk"+qualificationDetails.getQualification_id());
//                            Qualification qualificationFound=entityManager.find(Qualification.class,qualificationDetails.getQualification_id());
//                            if (Qualificationorder.get(qualificationFound.getOverlap().intValue()) > max) {
//                                customerBasicDetailsDto.setHighestQualification(qualificationService.getQualificationByQualificationId(qualificationDetails.getQualification_id()).getQualification_name());
//                                max = Qualificationorder.get(qualificationFound.getOverlap().intValue());
//                            }
//                        }
//                        if (qualificationType != null && max != 0 && !qualificationStrings.contains(customerBasicDetailsDto.getHighestQualification())) {
//                            continue;
//                        }
//                        if (max == 0)
//                            customerBasicDetailsDto.setHighestQualification(null);
//                    }

//                    if (!qualifications.isEmpty() && qualificationType != null) {
//                        for (QualificationDetails qualificationDetails : qualifications) {
//                            int qualificationId = (qualificationDetails.getQualification_id());
//
//                            if (qualificationType.contains(qualificationId)) {
//                                String qualificationNameToSet = qualificationService.getQualificationByQualificationId(qualificationId).getQualification_name();
//                                customerBasicDetailsDto.setHighestQualification(qualificationNameToSet);
//                                break; // Stop checking more qualifications for this customer
//                            }
//                        }
//                    }

                    if (qualificationType != null) {
                        for (QualificationDetails qualificationDetails : qualifications) {
                            int qualificationId = qualificationDetails.getQualification_id();

                            if (qualificationType.contains(qualificationId)) {
                                String qualificationNameToSet = qualificationService
                                        .getQualificationByQualificationId(qualificationId)
                                        .getQualification_name();
                                customerBasicDetailsDto.setHighestQualification(qualificationNameToSet);
                                break; // Stop checking more qualifications for this customer
                            }
                        }
                    } else {
                        int max = 0;
                        for (QualificationDetails qualificationDetails : qualifications) {
                            int qualificationId = qualificationDetails.getQualification_id();
                            Qualification qualificationFound = entityManager.find(Qualification.class, qualificationId);

                            if (qualificationFound != null) {
                                Integer order = Qualificationorder.get(qualificationFound.getOverlap().intValue());
                                if (order != null && order > max) {
                                    customerBasicDetailsDto.setHighestQualification(qualificationFound.getQualification_name());
                                    max = order;
                                }
                            }
                        }
                    }

                    customerBasicDetailsDto.setPrimaryRef(primaryRefName);
                    customerBasicDetailsDto.setPrimaryRefId(primaryRefId);
                    if (roleId == 1 || roleId == 2)
                        customerBasicDetailsDto.setPhone(customCustomer.getMobileNumber());
                    else if (!customCustomer.getHidePhoneNumber())
                        customerBasicDetailsDto.setPhone(customCustomer.getMobileNumber());
                    else
                        customerBasicDetailsDto.setPhone(null);
                    customerList.add(customerBasicDetailsDto);

                    customerBasicDetailsDto.setSecondaryMobileNumber(customCustomer.getSecondaryMobileNumber());
                    customerBasicDetailsDto.setCreatedDate(customCustomer.getAuditable().getDateCreated());
                    customerBasicDetailsDto.setUpdatedDate(customCustomer.getAuditable().getDateUpdated());
                }
            }
        }
        if ("ASC".equalsIgnoreCase(sortOrder)) {
            customerList.sort(
                    Comparator.comparing(
                            CustomerBasicDetailsDto::getUpdatedDate,
                            Comparator.nullsFirst(Comparator.naturalOrder())
                    )
            );
        } else {
            customerList.sort(
                    Comparator.comparing(
                            CustomerBasicDetailsDto::getUpdatedDate,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    )
            );
        }

        int totalItems = customerList.size();
        int totalPages = (int) Math.ceil((double) totalItems / limit);

        List<CustomerBasicDetailsDto> paginatedList = sharedUtilityService.getPaginatedList(customerList, offset, limit);

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
        List<Long> ids = getLongList(map, "userIds");
        Map<Long, String> skippedIds = new HashMap<>();
        List<Long> actionedIds = new ArrayList<>();
        List<CustomCustomer> processedCustomers = new ArrayList<>(); // Add this line
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
            if (action.equals(Constant.ACTION_SUSPEND)) {
                sharedUtilityService.blackListToken(customCustomer.getToken(), Constant.CUSTOMER_ROLE_ID, customCustomer.getId());
                logout(customCustomer.getToken());
            } else {
                sharedUtilityService.removeToken(customCustomer.getToken());
            }
            actionedIds.add(customerId);
            ++successCount;
            entityManager.merge(customCustomer);
            processedCustomers.add(customCustomer);
        }
        if (!processedCustomers.isEmpty()) {
            statusChangeEmailService.sendCustomerStatusChangeEmails(processedCustomers, action, authHeader);
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


    public ResponseEntity<?> getRecos(Long customerId, Integer offset, Integer limit) {
        Customer customer = customerService.readCustomerById(customerId);
        if (customer == null)
            return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
        CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customerId);
        List<QualificationDetails> qualificationDetails = customCustomer.getQualificationDetailsList();
        List<Integer> qualificationIds = new ArrayList<>();
        for (QualificationDetails qualificationDetail : qualificationDetails) {
            qualificationIds.add(qualificationDetail.getQualification_id());
        }
        int age = sharedUtilityService.calculateAge(customCustomer.getDob());
        Long reservedCategory = reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId();
        Long genderId = genderService.getGenderByName(customCustomer.getGender()).getGenderId();
        Double fee = null;
        String ageLimit = null;

        System.out.println("Limit is " + limit + "offset is" + offset);
        List<BigInteger> res = entityManager.createNativeQuery(recosQuery)
                .setParameter("customerId", customCustomer.getId())
                .setParameter("qualificationIds", qualificationIds)
                .setParameter("genderId", genderId)
                .setParameter("reserveCategoryId", reservedCategory)
                .setParameter("age", age)
                .setParameter("limit", limit)
                .setParameter("offset", limit * offset)
                .getResultList();

        System.out.println(res.size());
        System.out.println("Products");
        for (BigInteger resl : res) {
            System.out.println(resl.intValue());
        }

        BigInteger resultCount = (BigInteger) entityManager.createNativeQuery(recosCount)
                .setParameter("customerId", customCustomer.getId())
                .setParameter("qualificationIds", qualificationIds)
                .setParameter("genderId", genderId)
                .setParameter("reserveCategoryId", reservedCategory)
                .setParameter("age", age)
                .getSingleResult();

        List<ProductDetailsDTO> wrappers = new ArrayList<>();
        for (BigInteger id : res) {
            CustomProduct customProduct = entityManager.find(CustomProduct.class, id.longValue());
            Product blcProduct = catalogService.findProductById(id.longValue());
            genderId = 1L;  // Default to 1 (MALE)
            Long categoryId = 1L; // Default to 1 (GEN)
            int flag = 0;


            System.out.println("\n=== FEE CALCULATION DEBUG ===");
            System.out.println("Initial customer state: " + (customCustomer != null ? "Logged in" : "Not logged in"));

            // === PRIORITIZED FEE CALCULATION ===
            if (customCustomer != null) {
                try {
                    System.out.println("\nCustomer details:");
                    System.out.println("Raw category: " + customCustomer.getCategory());
                    System.out.println("Raw gender: " + customCustomer.getGender());

                    categoryId = reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId();
                    genderId = genderService.getGenderByName(customCustomer.getGender()).getGenderId();

                    System.out.println("Resolved categoryId: " + categoryId);
                    System.out.println("Resolved genderId: " + genderId);

                    // 1. Most specific: Exact category + gender (e.g., SC + MALE = 50)
                    System.out.println("\nChecking exact match (categoryId=" + categoryId + ", genderId=" + genderId + ")");
                    fee = reserveCategoryService.getReserveCategoryFee(customProduct.getId(), categoryId, genderId);
                    System.out.println("Exact match fee result: " + fee);

                    if (fee != null) {
                        flag++;
                        System.out.println("Found exact match fee: " + fee);
                    } else {
                        // 2. Customer's category + ALL genders
                        System.out.println("\nChecking category match (categoryId=" + categoryId + ", GENDER_ALL)");
                        fee = reserveCategoryService.getReserveCategoryFee(customProduct.getId(), categoryId, Constant.GENDER_ALL);
                        System.out.println("Category match fee result: " + fee);

                        if (fee != null) {
                            flag++;
                            System.out.println("Found category match fee: " + fee);
                        } else {
                            // 3. ALL categories + Customer's gender
                            System.out.println("\nChecking gender match (RESERVED_CATEGORY_ALL, genderId=" + genderId + ")");
                            fee = reserveCategoryService.getReserveCategoryFee(customProduct.getId(), Constant.RESERVED_CATEGORY_ALL, genderId);
                            System.out.println("Gender match fee result: " + fee);

                            if (fee != null) {
                                flag++;
                                System.out.println("Found gender match fee: " + fee);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("\nERROR in customer-specific fee lookup:");
                    e.printStackTrace();
                }
            }

            // 4. Final fallbacks
            if (fee == null) {
                System.out.println("\nNo customer-specific fee found, checking fallbacks:");

                System.out.println("Checking GEN+MALE (1L, 1L)");
                fee = reserveCategoryService.getReserveCategoryFee(customProduct.getId(), 1L, 1L);
                System.out.println("GEN+MALE fee result: " + fee);

                if (fee == null) {
                    System.out.println("Checking ALL+ALL");
                    fee = reserveCategoryService.getReserveCategoryFee(
                            customProduct.getId(), Constant.RESERVED_CATEGORY_ALL, Constant.GENDER_ALL);
                    System.out.println("ALL+ALL fee result: " + fee);
                }

                if (fee != null) {
                    flag++;
                } else {
                    fee = 0.0;
                    System.out.println("Using absolute fallback fee: 0.0");
                }
            }

            // === AGE LIMIT CALCULATION ===
            System.out.println("\n=== AGE LIMIT CALCULATION DEBUG ===");
            CustomProductReserveCategoryBornBeforeAfterRef ageLimitResult = null;

            if (customCustomer != null) {
                try {
                    ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(customProduct, Constant.RESERVED_CATEGORY_ALL, Constant.GENDER_ALL);
                    if (ageLimitResult == null) {
                        System.out.println("\nChecking exact age limit (categoryId=" + categoryId + ", genderId=" + genderId + ")");
                        ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(customProduct, categoryId, genderId);
                        System.out.println("Exact age limit result: " + ageLimitResult);

                        if (ageLimitResult == null) {
                            System.out.println("\nChecking category age limit (categoryId=" + categoryId + ", GENDER_ALL)");
                            ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(customProduct, categoryId, Constant.GENDER_ALL);
                            System.out.println("Category age limit result: " + ageLimitResult);

                            if (ageLimitResult == null) {
                                System.out.println("\nChecking gender age limit (RESERVED_CATEGORY_ALL, genderId=" + genderId + ")");
                                ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(
                                        customProduct, Constant.RESERVED_CATEGORY_ALL, genderId);
                                System.out.println("Gender age limit result: " + ageLimitResult);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("\nERROR in customer-specific age lookup:");
                    e.printStackTrace();
                }
            }

            // Final fallback for age
            if (ageLimitResult == null) {
                System.out.println("\nNo customer-specific age limit found, checking ALL+ALL");
                ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(
                        customProduct, Constant.RESERVED_CATEGORY_ALL, Constant.GENDER_ALL);
                System.out.println("ALL+ALL age limit result: " + ageLimitResult);
            }

            // Set age limit if found
            if (ageLimitResult != null) {
                System.out.println("\nSetting age limit with result: " + ageLimitResult);
                setAgeLimit(ageLimitResult, sharedUtilityService);
                flag++;
            }

            System.out.println("\n=== FINAL CHECKS ===");
            System.out.println("Flag value: " + flag);
            System.out.println("Current fee: " + fee);
            System.out.println("Current age limit: " + ageLimit);

            // === FALLBACK FOR BOTH FEE AND AGE (if no matches) ===
            if (flag < 2) {
                System.out.println("\nInsufficient matches (flag < 2), applying final fallbacks");

                if (fee == null) {
                    System.out.println("Rechecking GEN+MALE fee");
                    fee = reserveCategoryService.getReserveCategoryFee(customProduct.getId(), 1L, 1L);
                    if (fee == null) {
                        fee = 0.0;
                        System.out.println("Setting fee to 0.0");
                    }
                }

                if (ageLimit == null) {
                    System.out.println("Rechecking GEN+MALE age limit");
                    ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(customProduct, 1L, 1L);
                    if (ageLimitResult != null) {
                        setAgeLimit(ageLimitResult, sharedUtilityService);
                    }
                }
            }

            System.out.println("\n=== FINAL VALUES ===");
            System.out.println("Final fee: " + fee);
            System.out.println("Final age limit: " + ageLimit);
            System.out.println("=== PROCESS COMPLETE ===");

            ProductDetailsDTO dto = new ProductDetailsDTO();
            dto.setFee(fee);
            dto.setAgeLimit(getAgeLimits(ageLimitResult, sharedUtilityService));
            dto.setId(id.longValue());
            dto.setTotalVacanicies(customProduct.getTotalVacanciesInProduct());
            dto.setMetaTitle(customProduct.getMetaTitle());
            dto.setDisplayTemplate(customProduct.getDisplayTemplate());
            dto.setActiveEndDate(customProduct.getActiveEndDate());
            dto.setActiveStartDate(customProduct.getActiveStartDate());
            wrappers.add(dto);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("forms", wrappers);
        response.put("totalItems", resultCount);
        long totalPages = (resultCount.longValue() + limit - 1) / limit;
        response.put("totalPages", totalPages);
        response.put("currentPage", offset + 1);
        return ResponseService.generateSuccessResponse("Found products", response, HttpStatus.OK);
    }

    private void setAgeLimit(CustomProductReserveCategoryBornBeforeAfterRef ageLimitResult, SharedUtilityService sharedUtilityService) {
        String ageLimit;
        if (ageLimitResult == null) {
            ageLimit = "N/A";
            return;
        }
        int[] ageLimits = null;
        if (ageLimitResult.getBornAfter() != null && ageLimitResult.getBornBefore() != null) {
            ageLimits = sharedUtilityService.calculateAgeRange(
                    ageLimitResult.getBornBefore(),
                    ageLimitResult.getBornAfter(),
                    null);
        }


        ageLimit = (ageLimitResult.getMaximumAge() != null && ageLimitResult.getMinimumAge() != null &&
                ageLimitResult.getMaximumAge() != 0 && ageLimitResult.getMinimumAge() != 0)
                ? ageLimitResult.getMinimumAge() + "-" + ageLimitResult.getMaximumAge()
                : (ageLimits != null && ageLimits.length >= 2)
                ? ageLimits[0] + "-" + ageLimits[1]
                : "N/A";
    }

    private String getAgeLimits(CustomProductReserveCategoryBornBeforeAfterRef ageLimitResult, SharedUtilityService sharedUtilityService) {
        String ageLimit;
        if (ageLimitResult == null) {
            ageLimit = "N/A";
            return ageLimit;
        }
        int[] ageLimits = null;
        if (ageLimitResult.getBornAfter() != null && ageLimitResult.getBornBefore() != null) {
            ageLimits = sharedUtilityService.calculateAgeRange(
                    ageLimitResult.getBornBefore(),
                    ageLimitResult.getBornAfter(),
                    null);
        }


        ageLimit = (ageLimitResult.getMaximumAge() != null && ageLimitResult.getMinimumAge() != null &&
                ageLimitResult.getMaximumAge() != 0 && ageLimitResult.getMinimumAge() != 0)
                ? ageLimitResult.getMinimumAge() + "-" + ageLimitResult.getMaximumAge()
                : (ageLimits != null && ageLimits.length >= 2)
                ? ageLimits[0] + "-" + ageLimits[1]
                : "N/A";
        return ageLimit;
    }


    public ResponseEntity<?> getSavedFormsWrapper(Long customerId, List<CustomProduct> customProducts, Integer offset, Integer limit) throws Exception {
        try {
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null)
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customerId);
            List<QualificationDetails> qualificationDetails = customCustomer.getQualificationDetailsList();
            List<Integer> qualificationIds = new ArrayList<>();
            for (QualificationDetails qualificationDetail : qualificationDetails) {
                qualificationIds.add(qualificationDetail.getQualification_id());
            }
            int age = sharedUtilityService.calculateAge(customCustomer.getDob());
//            Long reservedCategory = reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId();
            Long genderId = null;
            Double fee = null;
            String ageLimit = null;

            List<ProductDetailsDTO> wrappers = new ArrayList<>();
            for (CustomProduct customProduct : customProducts) {
                genderId = 1L;  // Default to 1 (MALE)
                Long categoryId = 1L; // Default to 1 (GEN)
                int flag = 0;

                if (customCustomer != null) {
                    try {
                        if (customCustomer.getCategory() != null) {
                            categoryId = reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId();
                        } else {
                            categoryId = RESERVED_CATEGORY_ALL;
                        }
                        if (customCustomer.getGender() != null) {
                            genderId = genderService.getGenderByName(customCustomer.getGender()).getGenderId();
                        } else {
                            genderId = GENDER_ALL;
                        }

                        // 1. Most specific: Exact category + gender (e.g., SC + MALE = 50)
                        fee = reserveCategoryService.getReserveCategoryFee(customProduct.getId(), categoryId, genderId);

                        if (fee != null) {
                            flag++;
                        } else {
                            fee = reserveCategoryService.getReserveCategoryFee(customProduct.getId(), categoryId, Constant.GENDER_ALL);
                            if (fee != null) {
                                flag++;
                            } else {
                                // 3. ALL categories + Customer's gender
                                fee = reserveCategoryService.getReserveCategoryFee(customProduct.getId(), Constant.RESERVED_CATEGORY_ALL, genderId);
                                if (fee != null) {
                                    flag++;
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("\nERROR in customer-specific fee lookup:");
                        e.printStackTrace();
                    }
                }

                // 4. Final fallbacks
                if (fee == null) {
                    fee = reserveCategoryService.getReserveCategoryFee(customProduct.getId(), 1L, 1L);
                    if (fee == null) {
                        fee = reserveCategoryService.getReserveCategoryFee(
                                customProduct.getId(), Constant.RESERVED_CATEGORY_ALL, Constant.GENDER_ALL);
                    }

                    if (fee != null) {
                        flag++;
                    } else {
                        fee = 0.0;
                    }
                }
                CustomProductReserveCategoryBornBeforeAfterRef ageLimitResult = null;

                if (customCustomer != null) {
                    try {
                        ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(customProduct, Constant.RESERVED_CATEGORY_ALL, Constant.GENDER_ALL);
                        if (ageLimitResult == null) {
                            ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(customProduct, categoryId, genderId);

                            if (ageLimitResult == null) {
                                ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(customProduct, categoryId, Constant.GENDER_ALL);

                                if (ageLimitResult == null) {
                                    ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(
                                            customProduct, Constant.RESERVED_CATEGORY_ALL, genderId);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("\nERROR in customer-specific age lookup:");
                        e.printStackTrace();
                    }
                }

                // Final fallback for age
                if (ageLimitResult == null) {
                    ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(
                            customProduct, Constant.RESERVED_CATEGORY_ALL, Constant.GENDER_ALL);
                }

                // Set age limit if found
                if (ageLimitResult != null) {
                    setAgeLimit(ageLimitResult, sharedUtilityService);
                    flag++;
                }
                if (flag < 2) {
                    if (fee == null) {
                        fee = reserveCategoryService.getReserveCategoryFee(customProduct.getId(), 1L, 1L);
                        if (fee == null) {
                            fee = 0.0;
                        }
                    }

                    if (ageLimit == null) {
                        ageLimitResult = reserveCategoryAgeService.fetchAgeLimitByCategory(customProduct, 1L, 1L);
                        if (ageLimitResult != null) {
                            setAgeLimit(ageLimitResult, sharedUtilityService);
                        }
                    }
                }
                ProductDetailsDTO dto = new ProductDetailsDTO();
                dto.setFee(fee);
                dto.setAgeLimit(getAgeLimits(ageLimitResult, sharedUtilityService));
                dto.setId(customProduct.getId());
                dto.setTotalVacanicies(customProduct.getTotalVacanciesInProduct());
                dto.setMetaTitle(customProduct.getMetaTitle());
                dto.setDisplayTemplate(customProduct.getDisplayTemplate());
                dto.setActiveEndDate(customProduct.getActiveEndDate());
                dto.setActiveStartDate(customProduct.getActiveStartDate());
                wrappers.add(dto);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("forms", wrappers);
            response.put("totalItems", customProducts.size());
            long totalPages = (customProducts.size() + limit - 1) / limit;
            response.put("totalPages", totalPages);
            response.put("currentPage", offset + 1);
            return ResponseService.generateSuccessResponse("Found products", response, HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandling.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new Exception(e);
        }
    }
}
