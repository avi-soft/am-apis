package com.community.api.endpoint.avisoft.controller.Customer;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.PhysicalRequirementDto;
import com.community.api.dto.ReserveCategoryAgeDto;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.endpoint.avisoft.controller.otpmodule.OtpEndpoint;
import com.community.api.endpoint.customer.AddressDTO;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomProductReserveCategoryBornBeforeAfterRef;
import com.community.api.entity.CustomerReferrer;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.DocumentValidity;
import com.community.api.entity.QualificationDetails;
import com.community.api.entity.Post;
import com.community.api.services.FileDownloadService;
import com.community.api.services.PostExecutionService;
import com.community.api.services.ProductReserveCategoryBornBeforeAfterRefService;
import com.community.api.services.ReserveCategoryAgeService;
import com.community.api.services.ResponseService;
import com.community.api.services.SanitizerService;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.DocumentStorageService;
import com.community.api.services.ReserveCategoryService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.ReserveCategoryDtoService;
import com.community.api.services.PhysicalRequirementDtoService;
import com.community.api.services.RoleService;
import com.community.api.services.FileService;
import com.community.api.services.ProductReserveCategoryFeePostRefService;
import com.community.api.services.DistrictService;
import com.community.api.services.ApiConstants;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.Document;
import com.community.api.utils.DocumentType;
import com.community.api.utils.ServiceProviderDocument;
import io.micrometer.core.lang.Nullable;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.profile.core.domain.Address;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerAddress;
import org.broadleafcommerce.profile.core.domain.CustomerImpl;
import org.broadleafcommerce.profile.core.service.AddressService;
import org.broadleafcommerce.profile.core.service.CustomerAddressService;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.File;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import static com.community.api.component.Constant.request;
import static org.apache.hc.core5.util.Deadline.DATE_FORMAT;

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
    private DocumentStorageService fileUploadService;

    @Autowired
    private static SharedUtilityService sharedUtilityServiceApi;

    @Autowired
    private ReserveCategoryAgeService reserveCategoryAgeService;

    @Autowired
    private ExceptionHandlingService exceptionHandlingService;
    @Autowired
    private ReserveCategoryDtoService reserveCategoryDtoService;
    @Autowired
    private PhysicalRequirementDtoService physicalRequirementDtoService;
    @Autowired
    private  ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;



    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private ProductReserveCategoryFeePostRefService reserveCategoryFeePostRefService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private static ResponseService responseService;
    @Autowired
    private FileDownloadService fileDownloadService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DocumentStorageService documentStorageService;
    @Autowired
    private  ReserveCategoryService reserveCategoryService;

    @Autowired
    private  SanitizerService sanitizerService;

    @Autowired
    private CatalogService catalogService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PostExecutionService postExecutionService;

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
    private SharedUtilityService sharedUtilityService;

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
    @Authorize(value = {Constant.roleUser,Constant.roleSuperAdmin,Constant.roleAdmin,Constant.roleServiceProvider})
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
    @Authorize(value = {Constant.roleUser,Constant.roleServiceProvider})
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomer(@RequestBody Map<String, Object> details, @RequestParam Long customerId,@RequestHeader(value = "Authorization") String authHeader) {
        try {
            String jwtToken = authHeader.substring(7);
            List<String>deleteLogs=new ArrayList<>();
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);

            List<String> errorMessages = new ArrayList<>();

            details=sanitizerService.sanitizeInputMap(details);

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
            if((roleId==4&&customCustomer.getCreatedByRole()==4&&customCustomer.getCreatedById()!=tokenUserId)||(roleId==4&&customCustomer.getRegisteredBySp().equals(false))||(roleId==5&&!tokenUserId.equals(customerId)))
                return ResponseService.generateErrorResponse("Forbidden Access",HttpStatus.UNAUTHORIZED);
            if (customCustomer == null) {
                return ResponseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);
            }
            String secondaryMobileNumber = (String) details.get("secondaryMobileNumber");
            String mobileNumber = (String) details.get("mobileNumber");
            if (secondaryMobileNumber != null && mobileNumber==null && secondaryMobileNumber.equalsIgnoreCase(customCustomer.getMobileNumber())) {
                return ResponseService.generateErrorResponse("Primary and Secondary Mobile Numbers cannot be the same", HttpStatus.BAD_REQUEST);
            }

            if(details.containsKey("hidePhoneNumber"))
            {
                customCustomer.setHidePhoneNumber((Boolean)details.get("hidePhoneNumber"));
                if((Boolean)details.get("hidePhoneNumber").equals(true))
                {
                errorMessages.addAll(validateHidePhoneNumber(details, customCustomer));
                }
                if(secondaryMobileNumber!=null &&!customCustomerService.isValidMobileNumber(secondaryMobileNumber))
                    errorMessages.add("Secondary mobile is invalid");
                if(details.containsKey("whatsappNumber")&& !customCustomerService.isValidMobileNumber((String)details.get("whatsappNumber")))
                    errorMessages.add("Invalid Whatsapp NUmber");
                if(errorMessages.isEmpty()) {
                    customCustomer.setSecondaryMobileNumber(secondaryMobileNumber);
                    customCustomer.setWhatsappNumber((String)details.get("whatsappNumber"));
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

            if (mobileNumber != null && secondaryMobileNumber==null && mobileNumber.equalsIgnoreCase(customCustomer.getSecondaryMobileNumber())) {
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


            if (details.containsKey("firstName")&&!details.get("firstName").toString().isEmpty()) {
                customCustomer.setFirstName((String) details.get("firstName"));
            } else if (details.containsKey("firstName")&&details.get("firstName").toString().isEmpty())
            {
                errorMessages.add("First name cannot be null");
            } else if (details.containsKey("firstName")&&!sharedUtilityService.isAlphabetic((String)details.get("firstName"))) {
                errorMessages.add("Invalid First name");
            }
            if (details.containsKey("lastName")&&!details.get("lastName").toString().isEmpty())
                customCustomer.setLastName((String) details.get("lastName"));
            else if (details.containsKey("lastName")&&details.get("lastName").toString().isEmpty())
            {
                errorMessages.add("Last name cannot be null");
            }
            else if (details.containsKey("lastName")&&!sharedUtilityService.isAlphabetic((String)details.get("lastName"))) {
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
            if (state != null && district != null && pincode != null) {
                boolean updated=false;
                for (CustomerAddress customerAddress : customCustomer.getCustomerAddresses()) {
                    if (customerAddress.getAddressName().equals("CURRENT_ADDRESS")) {
                        customerAddress.getAddress().setAddressLine1((String) details.get("currentAddress"));
                        customerAddress.getAddress().setStateProvinceRegion(districtService.findStateById(Integer.parseInt(state)));
                        customerAddress.getAddress().setCounty(districtService.findDistrictById(Integer.parseInt(district)));
                        customerAddress.getAddress().setPostalCode(pincode);
                        customerAddress.getAddress().setCity((String) details.get("currentCity"));
                        updated = true;
                        break;
                    }
                }
                if(!updated) {
                    Map<String, Object> addressMap = new HashMap<>();
                    addressMap.put("address", details.get("currentAddress"));
                    addressMap.put("state", districtService.findStateById(Integer.parseInt(state)));
                    addressMap.put("city", details.get("currentCity"));
                    addressMap.put("district", districtService.findDistrictById(Integer.parseInt(district)));
                    addressMap.put("pinCode", pincode);
                    addressMap.put("addressName", "CURRENT_ADDRESS");
                    addAddress(customerId, addressMap);
                }
            }
            details.remove("currentState");
            details.remove("currentDistrict");
            details.remove("currentAddress");
            details.remove("currentPincode");
            details.remove("currentCity");
            state = (String) details.get("permanentState");
            district = (String) details.get("permanentDistrict");
            pincode = (String) details.get("permanentPincode");
            if (state != null && district != null && pincode != null) {
                boolean updated = false;
                for (CustomerAddress customerAddress : customCustomer.getCustomerAddresses()) {

                    if (customerAddress.getAddressName().equals("PERMANENT_ADDRESS")) {
                        customerAddress.getAddress().setAddressLine1((String) details.get("permanentAddress"));
                        customerAddress.getAddress().setStateProvinceRegion(districtService.findStateById(Integer.parseInt(state)));
                        customerAddress.getAddress().setCounty(districtService.findDistrictById(Integer.parseInt(district)));
                        customerAddress.getAddress().setPostalCode(pincode);
                        customerAddress.getAddress().setCity((String) details.get("permanentCity"));
                        updated = true;
                        break;
                    }
                }
                if (!updated) {
                    Map<String, Object> addressMap = new HashMap<>();
                    addressMap.put("address", details.get("permanentAddress"));
                    addressMap.put("state", districtService.findStateById(Integer.parseInt(state)));
                    addressMap.put("city", details.get("permanentCity"));
                    addressMap.put("district", districtService.findDistrictById(Integer.parseInt(district)));
                    addressMap.put("pinCode", pincode);
                    addressMap.put("addressName", "PERMANENT_ADDRESS");
                    addAddress(customerId, addressMap);
                }
            }

            details.remove("permanentState");
            details.remove("permanentDistrict");
            details.remove("permanentAddress");
            details.remove("permanentPincode");
            details.remove("permanentCity");

            if(details.containsKey("ncc_certificate"))
            {
                String nccCertificateValue = (String) details.get("ncc_certificate");

                if(!nccCertificateValue.equalsIgnoreCase("NCC Certificate A") && !nccCertificateValue.equalsIgnoreCase("NCC Certificate B") && !nccCertificateValue.equalsIgnoreCase("NCC Certificate C"))
                {
                    return ResponseService.generateErrorResponse("You can add value for ncc certificate either NCC Certificate A or NCC Certificate B or  NCC Certificate C",HttpStatus.BAD_REQUEST);
                }
                customCustomer.setNcc_certificate(nccCertificateValue);
                customCustomer.setIs_ncc_certificate(true);

            }
            if(details.containsKey("dob"))
            {
               if(sharedUtilityService.isFutureDate((String)details.get("dob")))
                   errorMessages.add("DOB cannot be in future");
            }
                if(details.containsKey("is_ncc_certificate"))
                {
                Boolean isNccCertificate = Boolean.parseBoolean((String)  details.get("is_ncc_certificate"));
                if(isNccCertificate.equals(true))
                {
                    if(!details.containsKey("ncc_certificate"))
                    {
                        return ResponseService.generateErrorResponse("You have to select ncc certificate type",HttpStatus.BAD_REQUEST);
                    }
                    customCustomer.setNcc_certificate((String) details.get("ncc_certificate"));
                }

                if(isNccCertificate.equals(false))
                {
                    customCustomer.setNcc_certificate(null);
                    List<Document> customerDocuments=customCustomer.getDocuments();
                    for(Document document: customerDocuments)
                    {
                        if(document.getIsArchived().equals(false))
                        {
                            if(document.getCustom_customer().getId().equals(customerId) )
                            {
                                if(document.getDocumentType().getDocument_type_id().equals(18) || document.getDocumentType().getDocument_type_id().equals(19) ||document.getDocumentType().getDocument_type_id().equals(20))
                                {
                                    document.setIsArchived(true);
                                    entityManager.merge(document);
                                }
                            }
                        }
                    }

                }
                customCustomer.setIs_ncc_certificate(isNccCertificate);

            }
            if(details.containsKey("nss_certificate"))
            {
                String nssCertificateValue = (String) details.get("nss_certificate");
                if(!nssCertificateValue.equalsIgnoreCase("NSS Certificate A") && !nssCertificateValue.equalsIgnoreCase("NSS Certificate B") && !nssCertificateValue.equalsIgnoreCase("NSS Certificate C"))
                {
                    return ResponseService.generateErrorResponse("You can add value for ncc certificate either NSS Certificate A or NSS Certificate B or  NSS Certificate C",HttpStatus.BAD_REQUEST);
                }
                customCustomer.setNss_certificate(nssCertificateValue);
                customCustomer.setIs_nss_certificate(true);
            }
            if(details.containsKey("is_nss_certificate"))
            {
                Boolean isNssCertificate = Boolean.parseBoolean((String)  details.get("is_nss_certificate"));
                System.out.println("430");
                System.out.println(isNssCertificate);
                if(isNssCertificate.equals(true))
                {
                    if(!details.containsKey("nss_certificate"))
                    {
                        return ResponseService.generateErrorResponse("You have to select nss certificate type",HttpStatus.BAD_REQUEST);
                    }
                    customCustomer.setNss_certificate((String) details.get("nss_certificate"));
                }
                else if(isNssCertificate.equals(false))
                {
                    customCustomer.setNss_certificate(null);
                    List<Document> customerDocuments=customCustomer.getDocuments();
                    for(Document document: customerDocuments)
                    {
                        if(document.getIsArchived().equals(false))
                        {
                            if(document.getCustom_customer().getId().equals(customerId) )
                            {
                                if(document.getDocumentType().getDocument_type_id().equals(21) || document.getDocumentType().getDocument_type_id().equals(28) ||document.getDocumentType().getDocument_type_id().equals(29))
                                {
                                    document.setIsArchived(true);
                                    entityManager.merge(document);
                                }
                            }
                        }
                    }
                }
                customCustomer.setIs_nss_certificate(isNssCertificate);
            }

            details.remove("is_ncc_certificate");
            details.remove("ncc_certificate");
            details.remove("is_nss_certificate");
            details.remove("nss_certificate");

            if(customCustomer.getGender()!=null&&customCustomer.getGender().equals("Female")&&details.containsKey("chestSizeCms"))
                return ResponseService.generateErrorResponse("Cannot add chest size for gender : Female",HttpStatus.BAD_REQUEST);
            if(customCustomer.getGender()==null&&details.containsKey("chestSizeCms"))
                return ResponseService.generateErrorResponse("Cannot add chest size without specifying gender",HttpStatus.BAD_REQUEST);

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
                if (newValue.toString().isEmpty() && !isNullable) {
                    errorMessages.add(fieldName + " cannot be null");
                    continue;
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

                // Set value if type is compatible
                if (newValue != null && field.getType().isAssignableFrom(newValue.getClass())) {
                    field.set(customCustomer, newValue);
                }
            }

            // Update address if needed


            if (!errorMessages.isEmpty()) {
                return ResponseService.generateErrorResponse("List of Failed validations: " + errorMessages.toString(), HttpStatus.BAD_REQUEST);
            }
            customCustomer.setModifiedById(tokenUserId);
            customCustomer.setModifiedByRole(roleId);
            em.merge(customCustomer);
            return ResponseService.generateSuccessResponse("User details updated successfully", sharedUtilityService.breakReferenceForCustomer(customCustomer,authHeader), HttpStatus.OK);

        }catch (DataIntegrityViolationException ex) {
            exceptionHandling.handleException(ex);
            return ResponseService.generateErrorResponse("Error updating " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ConstraintViolationException ex) {
            exceptionHandling.handleException(ex);
            return ResponseService.generateErrorResponse("Error updating " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch(NoSuchFieldException e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("No such field present :" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch(Exception e){
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error updating " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public List<String> validateHidePhoneNumber(Map<String,Object>details,CustomCustomer customer)
    {
        List<String>errorMessages=new ArrayList<>();
        details=sanitizerService.sanitizeInputMap(details);

        if(((Boolean)details.get("hidePhoneNumber")).equals(true))
        {

            if(details.containsKey("secondaryMobileNumber")&&((String)details.get("secondaryMobileNumber")).isEmpty())
            {
                errorMessages.add("Need to provide Secondary Mobile Number when hiding primary Mobile Number");
            }

            if(details.containsKey("whatsappNumber")&&((String)details.get("whatsappNumber")).isEmpty())
            {
                errorMessages.add("Whatsapp number cannot be null");
            }
            if(details.containsKey("whatsappNumber")&&((String)details.get("whatsappNumber")).equals(customer.getMobileNumber()))
            {
                errorMessages.add("Cannot set primary number as whatsapp number when hidden");
            }
        }
        return errorMessages;
    }
    public boolean isFieldPresent (Class < ? > clazz, String fieldName){
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return field != null; // Field exists
        } catch (NoSuchFieldException e) {
            return false; // Field does not exist
        }
    }

    @Transactional
    @Authorize(value = {Constant.roleUser,Constant.roleSuperAdmin,Constant.roleAdmin,Constant.roleServiceProvider,Constant.roleServiceProviderAdmin})
    @RequestMapping(value = "/get-customer-details/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<?> getUserDetails(@PathVariable Long customerId ,@RequestHeader(value = "Authorization") String authHeader) {
        try {
            String jwtToken = authHeader.substring(7);
            List<String>deleteLogs=new ArrayList<>();
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            if(roleService.getRoleByRoleId(roleId).getRole_name().equals(Constant.roleUser)&&!customerId.equals(tokenUserId))
            {
                return ResponseService.generateErrorResponse("Forbidden access",HttpStatus.FORBIDDEN);
            }
            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
            if (customCustomer == null) {
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            }
            CustomerImpl customer = em.find(CustomerImpl.class, customerId);  // Assuming you retrieve the base Customer entity
            Map<String, Object> customerDetails = sharedUtilityService.breakReferenceForCustomer(customer,authHeader);

            return responseService.generateSuccessResponse("User details retrieved successfully", customerDetails, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving user details", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @Authorize(value = {Constant.roleUser,Constant.roleServiceProvider,Constant.roleSuperAdmin,Constant.roleAdmin,Constant.roleAdminServiceProvider})
    @PostMapping("/upload-documents")
    public ResponseEntity<?> uploadDocuments(
            @RequestParam Long customerId,
            @RequestParam(value = "files",required = false) List<MultipartFile> files,
            @RequestParam("fileTypes") List<Integer> fileTypes,
            @RequestParam(value = "qualificationDetailId",required = false) Long qualificationDetailId,
            @RequestParam(value = "dateOfIssue", required = false) String dateOfIssue,
            @RequestParam(value = "validUpto", required = false)  String validUpto,
            @RequestParam(value = "removeFileTypes", required = false) Boolean removeFileTypes,
            @RequestHeader(value = "Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseService.generateErrorResponse("Authorization header is missing or invalid.", HttpStatus.UNAUTHORIZED);
            }
            String jwtToken = authHeader.substring(7);
            List<String>deleteLogs=new ArrayList<>();
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();
            String queryStringArchive=null;
            String queryStringArchiveId=null;

            //**********DELETE DOCUMENT :START*********
            if(removeFileTypes!=null && removeFileTypes.equals(true))
            {
                if(role.equals(Constant.roleUser)) {
                    queryStringArchive = String.format(Constant.FETCH_DOCUMENT_TO_ARCHIVE, "document", "custom_customer_id");
                    queryStringArchiveId=String.format(Constant.FETCH_DOCUMENT_TO_ARCHIVE_ID, "document","custom_customer_id");
                }
                else if (role.equals(Constant.roleServiceProvider)) {
                    queryStringArchive = String.format(Constant.FETCH_DOCUMENT_TO_ARCHIVE, "service_provider_documents", "service_provider_id");
                    queryStringArchiveId=String.format(Constant.FETCH_DOCUMENT_TO_ARCHIVE_ID, "service_provider_documents","service_provider_id");
                }
                for(Integer fileType:fileTypes) {
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
                    try {
                        Query query = entityManager.createNativeQuery(queryStringArchiveId);
                        query.setParameter("userId", customerId);
                        query.setParameter("documentTypeId", fileType);
                        BigInteger id = (BigInteger) query.getSingleResult();
                        query = entityManager.createNativeQuery(queryStringArchive);
                        query.setParameter("userId", customerId);
                        query.setParameter("documentTypeId", fileType);
                        int result = query.executeUpdate();
                        System.out.println("rows affected :"+result);
                        if (result == 1) {
                            switch (role) {
                                case Constant.roleUser:
                                    System.out.println("CID:" + id.longValue());
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
                                    System.out.println("SID:" + id.longValue());
                                    ServiceProviderDocument serviceProviderDocument = entityManager.find(ServiceProviderDocument.class, id.longValue());
                                    ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, customerId);
                                    if (serviceProvider.getDocuments() != null) {
                                        Iterator<ServiceProviderDocument> iterator = serviceProvider.getDocuments().iterator();
                                        while (iterator.hasNext()) {
                                            ServiceProviderDocument documentToDelete = iterator.next();
                                            if (documentToDelete.getDocumentId().equals(serviceProviderDocument.getDocumentId())) {
                                                System.out.println("hiSp");
                                                iterator.remove();  // safely remove the document
                                                entityManager.merge(serviceProvider);  // merge after modification
                                                break;
                                            }
                                        }
                                        deleteLogs.add(documentTypeObj.getDocument_type_name() + " Deleted");
                                    }
                            }
                        }
                        else
                            return ResponseService.generateErrorResponse("No documents found",HttpStatus.NOT_FOUND);
                        return ResponseService.generateSuccessResponse("Document deleted successfully",deleteLogs,HttpStatus.OK);
                    }catch (NoResultException noResultException)
                    {
                        return ResponseService.generateErrorResponse("No record found",HttpStatus.NOT_FOUND);
                    }
                    catch (PersistenceException persistenceException)
                    {
                        return ResponseService.generateErrorResponse("No operation to perform",HttpStatus.NOT_FOUND);
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

            if (!customerId.equals(tokenUserId)) {
                return ResponseService.generateErrorResponse("Unauthorized request.", HttpStatus.UNAUTHORIZED);
            }

            Map<Integer, List<MultipartFile>> groupedFiles = new HashMap<>();
            for (int i = 0; i < files.size(); i++) {
                Integer fileTypeId = fileTypes.get(i);
                MultipartFile file = files.get(i);
                groupedFiles.computeIfAbsent(fileTypeId, k -> new ArrayList<>()).add(file);
            }

            if (roleService.findRoleName(roleId).equals(Constant.roleUser)) {
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

                    if(documentTypeObj.getIs_qualification_document().equals(true))
                    {
                        if(qualificationDetailId==null)
                        {
                            throw new IllegalArgumentException("QualificationDetail id cannot be null for uploading Qualfication Documents");
                        }
                    }

                    if(documentTypeObj.getIs_issue_date_required().equals(true))
                    {
                        if(dateOfIssue==null)
                        {
                            throw new IllegalArgumentException("Date of issue cannot be null");
                        }
                        if(documentTypeObj.getIs_expiration_date_required().equals(true))
                        {
                            if(validUpto==null)
                            {
                                throw new IllegalArgumentException("Valid upto (expiration date of document) cannot be null");
                            }
                        }
                    }
                    for (MultipartFile file : fileList) {

                        // Validate document
                        documentStorageService.validateDocument(file, documentTypeObj);

                        Document existingDocument = em.createQuery(
                                        "SELECT d FROM Document d WHERE d.custom_customer = :customCustomer " +
                                                "AND d.documentType = :documentType AND d.name IS NOT NULL ", Document.class)
                                .setParameter("customCustomer", customCustomer)
                                .setParameter("documentType", documentTypeObj)
                                .getResultStream()
                                .findFirst()
                                .orElse(null);


                        fileUploadService.uploadFileOnFileServer(file, documentTypeObj.getDocument_type_name(), customerId.toString(), role);

                        if (removeFileTypes != null && removeFileTypes) {

                            if (existingDocument != null && fileNameId != 13) {
                                if (existingDocument != null) {
                                    String filePath = existingDocument.getFilePath();

                                    if (filePath != null) {
                                        fileUploadService.deleteFile( customerId,  documentTypeObj.getDocument_type_name(),  existingDocument.getName(),  role);
                                    }

                                    existingDocument.setDocumentType(null);
                                    existingDocument.setFilePath(null);
                                    existingDocument.setName(null);
                                    em.persist(existingDocument);

                                    deletedDocumentMessages.add( documentTypeObj.getDocument_type_name() + "' has been deleted.");
                                }
                                continue;
                            }
                        }


                        if (fileNameId == 13 && (!file.isEmpty() || file != null)) {
                            String newFileName = file.getOriginalFilename();
                            // Check for existing document with the same name
                            Document existingDocument13 = em.createQuery(
                                            "SELECT d FROM Document d WHERE d.custom_customer = :customCustomer AND d.documentType = :documentType AND d.name = :documentName AND (d.name IS NOT NULL)", Document.class)
                                    .setParameter("customCustomer", customCustomer)
                                    .setParameter("documentType", documentTypeObj)
                                    .setParameter("documentName", newFileName)
                                    .getResultStream()
                                    .findFirst()
                                    .orElse(null);

                            if (existingDocument13 == null) {
                                documentStorageService.createDocument(file, documentTypeObj, customCustomer, customerId, role);
                            } else if (existingDocument13 != null) {
                                String filePath = existingDocument13.getFilePath();
                                if (removeFileTypes != null && removeFileTypes && newFileName!=null ) {
                                    fileUploadService.deleteFile( customerId,  documentTypeObj.getDocument_type_name(),  existingDocument.getName(),  role);
                                }
                                existingDocument13.setFilePath(null);
                                existingDocument13.setName(null);
                                existingDocument13.setCustom_customer(null);
                                em.merge(existingDocument);
                                deletedDocumentMessages.add( documentTypeObj.getDocument_type_name() + "' has been deleted.");
                            }
                        }
                        // If the file is not empty and a document already exists, update the document
                        else if (existingDocument != null && (!file.isEmpty() || file != null) && fileNameId != 13) {
                            String filePath = existingDocument.getFilePath();
                            if(qualificationDetailId!=null && documentTypeObj.getIs_qualification_document().equals(true))
                            {
                                QualificationDetails qualificationDetails=findQualificationDetailForCustomer(qualificationDetailId,customCustomer);
                                existingDocument.setIs_qualification_document(true);
                                existingDocument.setQualificationDetails(qualificationDetails);
                            }

                            if(dateOfIssue!=null && documentTypeObj.getIs_issue_date_required().equals(true))
                            {
                                DocumentValidity documentValidity=null;
                                if(existingDocument.getDocumentValidity()==null)
                                {
                                     documentValidity= new DocumentValidity();
                                    validateDate(dateOfIssue,validUpto);
                                    documentValidity.setDate_of_issue( convertStringToDate(dateOfIssue));
                                    if(validUpto==null)
                                    {
                                        documentValidity.setIs_valid_upto_na(true);
                                        documentValidity.setValid_upto(null);
                                    }
                                    else {
                                        documentValidity.setIs_valid_upto_na(false);
                                        documentValidity.setValid_upto( convertStringToDate(validUpto));
                                    }
                                    documentValidity.setDocument(existingDocument);
                                    existingDocument.setDocumentValidity(documentValidity);
                                    entityManager.persist(documentValidity);

                                }
                                else if(existingDocument.getDocumentValidity()!=null)
                                {
                                    documentValidity= existingDocument.getDocumentValidity();
                                    validateDate(dateOfIssue,validUpto);
                                    documentValidity.setDate_of_issue( convertStringToDate(dateOfIssue));
                                    if(validUpto==null)
                                    {
                                        documentValidity.setIs_valid_upto_na(true);
                                        documentValidity.setValid_upto(null);

                                    }
                                    else {
                                        documentValidity.setIs_valid_upto_na(false);
                                        documentValidity.setValid_upto(convertStringToDate(validUpto));
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
                                String newFileName = file.getOriginalFilename();
                                existingDocument.setIsArchived(false);
                                if (!newFileName.equals(oldFileName)) {
                                    fileUploadService.deleteFile( customerId,  documentTypeObj.getDocument_type_name(),  existingDocument.getName(),  role);
                                    documentStorageService.updateOrCreateDocument(existingDocument, file, documentTypeObj, customerId, role);
                                }
                            }
                        } else {
                            // If the file is not empty create the document
                            if (!file.isEmpty() || file != null && (fileNameId != 13)) {
                                Document document=documentStorageService.createDocument(file, documentTypeObj, customCustomer, customerId, role);
                                if(qualificationDetailId!=null && documentTypeObj.getIs_qualification_document().equals(true))
                                {
                                    QualificationDetails qualificationDetails=findQualificationDetailForCustomer(qualificationDetailId,customCustomer);
                                    document.setIs_qualification_document(true);
                                    document.setQualificationDetails(qualificationDetails);
                                    entityManager.merge(document);
                                }
                                if(dateOfIssue!=null && documentTypeObj.getIs_issue_date_required().equals(true))
                                {
                                    DocumentValidity documentValidity= new DocumentValidity();
                                    validateDate(dateOfIssue,validUpto);
                                    documentValidity.setDate_of_issue( convertStringToDate(dateOfIssue));
                                    if(validUpto==null)
                                    {
                                        documentValidity.setIs_valid_upto_na(true);
                                        documentValidity.setValid_upto(null);
                                    }
                                    else {
                                        documentValidity.setIs_valid_upto_na(false);
                                        documentValidity.setValid_upto(convertStringToDate(validUpto));
                                    }
                                    documentValidity.setDocument(document);
                                    entityManager.persist(documentValidity);
                                    document.setDocumentValidity(documentValidity);
                                    entityManager.merge(document);
                                }
                            }
                        }
                    }
                }
                CustomCustomer updatedCustomer = entityManager.find(CustomCustomer.class,customerId);
                CompletableFuture<List<Map<String, Object>>> futureDocuments = postExecutionService.returnCustomerDocuments(updatedCustomer.getDocuments());
                List<Map<String, Object>> filteredDocuments = futureDocuments.get(); // Blocks until the result is available

                // Construct the response with the updated data
                responseData.put("uploadedDocuments", filteredDocuments);
                return ResponseService.generateSuccessResponse("Documents updated successfully", responseData, HttpStatus.OK);
            } else {
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

                    if(documentTypeObj.getIs_qualification_document().equals(true))
                    {
                        if(qualificationDetailId==null)
                        {
                            throw new IllegalArgumentException("QualificationDetail id cannot be null for uploading Qualfication Documents");
                        }
                    }
                    if(documentTypeObj.getIs_issue_date_required().equals(true))
                    {
                        if(dateOfIssue==null)
                        {
                            throw new IllegalArgumentException("Date of issue cannot be null");
                        }
                        if(documentTypeObj.getIs_expiration_date_required().equals(true))
                        {
                            if(validUpto==null)
                            {
                                throw new IllegalArgumentException("Valid upto (expiration date of document) cannot be null");
                            }
                        }
                    }
                    for (MultipartFile file : fileList) {

                        documentStorageService.validateDocument(file, documentTypeObj);
                        ServiceProviderDocument existingDocument = em.createQuery(
                                        "SELECT d FROM ServiceProviderDocument d WHERE d.serviceProviderEntity = :serviceProviderEntity AND d.documentType = :documentType AND d.name IS NOT NULL", ServiceProviderDocument.class)
                                .setParameter("serviceProviderEntity", serviceProviderEntity)
                                .setParameter("documentType", documentTypeObj)

                                .getResultStream()
                                .findFirst()
                                .orElse(null);

                        fileUploadService.uploadFileOnFileServer(file, documentTypeObj.getDocument_type_name(), customerId.toString(), role);

                        if (removeFileTypes != null && removeFileTypes) {
                            if (existingDocument != null && fileNameId != 13) {
                                if (existingDocument != null) {

                                    String filePath = existingDocument.getFilePath();
                                    if (filePath != null) {
                                        fileUploadService.deleteFile( customerId,  documentTypeObj.getDocument_type_name(),  existingDocument.getName(),  role);
                                    }
                                    existingDocument.setDocumentType(null);
                                    existingDocument.setName(null);
                                    existingDocument.setFilePath(null);
                                    existingDocument.setServiceProviderEntity(null);
                                    em.persist(existingDocument);

                                    deletedDocumentMessages.add(documentTypeObj.getDocument_type_name() + " has been deleted.");
                                }
                                continue;
                            }
                        }

                        if (fileNameId == 13 && (!file.isEmpty() || file != null)) {
                            String newFileName = file.getOriginalFilename();

                            // Check for existing document with the same name
                            ServiceProviderDocument existingDocument13 = em.createQuery(
                                            "SELECT d FROM ServiceProviderDocument d WHERE d.serviceProviderEntity = :serviceProviderEntity AND d.documentType = :documentType AND d.name = :documentName AND (d.name IS NOT NULL)", ServiceProviderDocument.class)
                                    .setParameter("serviceProviderEntity", serviceProviderEntity)
                                    .setParameter("documentType", documentTypeObj)
                                    .setParameter("documentName", newFileName)
                                    .getResultStream()
                                    .findFirst()
                                    .orElse(null);

                            if (existingDocument13 == null) {
                                documentStorageService.createDocumentServiceProvider(file, documentTypeObj, serviceProviderEntity, customerId, role);
                            }

                            else if (existingDocument13 != null) {
                                if (removeFileTypes != null && removeFileTypes && newFileName!=null ) {
                                    fileUploadService.deleteFile( customerId,  documentTypeObj.getDocument_type_name(),  existingDocument.getName(),  role);

                                }
                                existingDocument13.setFilePath(null);
                                existingDocument13.setName(null);
                                existingDocument13.setServiceProviderEntity(null);

                                em.merge(existingDocument13);
                                deletedDocumentMessages.add( documentTypeObj.getDocument_type_name() + "' has been deleted.");
                            }


                        }
                        // If the file is not empty and a document already exists, update the document
                        else if (existingDocument != null && (!file.isEmpty() || file != null) && fileNameId != 13) {
                            String filePath = existingDocument.getFilePath();
                            if(qualificationDetailId!=null && documentTypeObj.getIs_qualification_document().equals(true))
                            {
                                QualificationDetails qualificationDetails=findQualificationDetailForServiceProvider(qualificationDetailId,serviceProviderEntity);
                                existingDocument.setIs_qualification_document(true);
                                existingDocument.setQualificationDetails(qualificationDetails);
                            }

                            if(dateOfIssue!=null && documentTypeObj.getIs_issue_date_required().equals(true)) {
                                DocumentValidity documentValidity = null;
                                if (existingDocument.getDocumentValidity() == null) {
                                    documentValidity = new DocumentValidity();
                                    validateDate(dateOfIssue, validUpto);
                                    documentValidity.setDate_of_issue( convertStringToDate(dateOfIssue));
                                    if (validUpto == null) {
                                        documentValidity.setIs_valid_upto_na(true);
                                        documentValidity.setValid_upto(null);
                                    } else {
                                        documentValidity.setIs_valid_upto_na(false);
                                        documentValidity.setValid_upto( convertStringToDate(validUpto));
                                    }
                                    documentValidity.setServiceProviderDocument(existingDocument);
                                    existingDocument.setDocumentValidity(documentValidity);
                                    entityManager.persist(documentValidity);

                                } else if (existingDocument.getDocumentValidity() != null) {
                                    documentValidity = existingDocument.getDocumentValidity();
                                    validateDate(dateOfIssue, validUpto);
                                    documentValidity.setDate_of_issue( convertStringToDate(dateOfIssue));
                                    if (validUpto == null) {
                                        documentValidity.setIs_valid_upto_na(true);
                                        documentValidity.setValid_upto(null);
                                    } else {
                                        documentValidity.setIs_valid_upto_na(false);
                                        documentValidity.setValid_upto( convertStringToDate(validUpto));
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
                                    fileUploadService.deleteFile( customerId,  documentTypeObj.getDocument_type_name(),  existingDocument.getName(),  role);

                                    documentStorageService.updateOrCreateServiceProvider(existingDocument, file, documentTypeObj, customerId, role);
                                }
                            }
                        } else {
                            // If the file is not empty create the document
                            if (!file.isEmpty() || file != null && (fileNameId != 13)) {
                                ServiceProviderDocument serviceProviderDocument=documentStorageService.createDocumentServiceProvider(file, documentTypeObj, serviceProviderEntity, customerId, role);
                                if(qualificationDetailId!=null && documentTypeObj.getIs_qualification_document().equals(true))
                                {
                                    QualificationDetails qualificationDetails=findQualificationDetailForServiceProvider(qualificationDetailId,serviceProviderEntity);
                                    serviceProviderDocument.setIs_qualification_document(true);
                                    serviceProviderDocument.setQualificationDetails(qualificationDetails);
                                    entityManager.merge(serviceProviderDocument);
                                }
                                if(dateOfIssue!=null && documentTypeObj.getIs_issue_date_required().equals(true))
                                {
                                    DocumentValidity documentValidity= new DocumentValidity();
                                    validateDate(dateOfIssue,validUpto);
                                    documentValidity.setDate_of_issue( convertStringToDate(dateOfIssue));
                                    if(validUpto==null)
                                    {
                                        documentValidity.setIs_valid_upto_na(true);
                                        documentValidity.setValid_upto(null);
                                    }
                                    else {
                                        documentValidity.setIs_valid_upto_na(false);
                                        documentValidity.setValid_upto( convertStringToDate(validUpto));
                                    }
                                    documentValidity.setServiceProviderDocument(serviceProviderDocument);
                                    entityManager.persist(documentValidity);
                                    serviceProviderDocument.setDocumentValidity(documentValidity);
                                    entityManager.merge(serviceProviderDocument);
                                }
                            }
                        }
                    }

                }
                ServiceProviderEntity updatedServiceProviderEntity = entityManager.find(ServiceProviderEntity.class,customerId);
                CompletableFuture<List<Map<String, Object>>> futureDocuments = postExecutionService.returnServiceProvider(updatedServiceProviderEntity.getDocuments());
                List<Map<String, Object>> filteredDocuments = futureDocuments.get();

                responseData.put("uploadedDocuments", filteredDocuments);
                return ResponseService.generateSuccessResponse("Documents uploaded successfully", responseData, HttpStatus.OK);
            }

        }
        catch (DataIntegrityViolationException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Document with the same name and file path already exists." + e.getMessage(), HttpStatus.BAD_REQUEST);

        }
        catch (IllegalArgumentException e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error updating documents: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    @Authorize(value = {Constant.roleUser})
    @RequestMapping(value = "update-username", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerUsername(@RequestBody Map<String, Object> updates, @RequestParam Long customerId,@RequestHeader(value = "Authorization") String authHeader) {
        try {

            updates=sanitizerService.sanitizeInputMap(updates);

            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            String username = (String) updates.get("username");
            if(username!=null)
                username=username.trim();

            if (username.isEmpty()||username.contains(" ")) {
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
                if(customer.getUsername()!=null && customer.getUsername().equals(username))
                    return ResponseService.generateErrorResponse("Old and new username cannot be same", HttpStatus.BAD_REQUEST);
                customer.setUsername(username);
                em.merge(customer);
                return ResponseService.generateSuccessResponse("User name  updated successfully : ", sharedUtilityService.breakReferenceForCustomer(customer,authHeader), HttpStatus.OK);

            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Error updating username", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @Authorize(value = {Constant.roleUser})
    @RequestMapping(value = "create-or-update-password", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerPassword(@RequestBody Map<String, Object> details, @RequestParam Long customerId,@RequestHeader(value = "Authorization") String authHeader) {
        try {
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
                    return ResponseService.generateSuccessResponse("Password Created", sharedUtilityService.breakReferenceForCustomer(customer,authHeader), HttpStatus.OK);
                }
                System.out.println(password+","+customer.getPassword());
                if (!passwordEncoder.matches(password, customer.getPassword())) {

                    customer.setPassword(passwordEncoder.encode(password));
                    em.merge(customer);
                    return ResponseService.generateSuccessResponse("Password Updated", sharedUtilityService.breakReferenceForCustomer(customer,authHeader), HttpStatus.OK);
                }
                else
                {
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
    @Authorize(value = {Constant.roleUser,Constant.roleSuperAdmin,Constant.roleAdminServiceProvider})
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
                newAddress.setAddressName((String) addressDetails.get("addressName"));
                List<CustomerAddress> addressLists = customer.getCustomerAddresses();
                addressLists.add(newAddress);
                customer.setCustomerAddresses(addressLists);
                if(!addressDetails.containsKey("inFunctionCall"))
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
        }catch (NumberFormatException e) {
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


        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error in retreiving Address", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @RequestMapping(value = "address-details", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveAddressList(@RequestParam Long customerId, @RequestParam Long addressId,@RequestHeader(value = "Authorization") String authHeader) {
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
        }catch (NumberFormatException e) {
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

    public ResponseEntity<?> createAuthResponse(String token, Customer customer,String authHeader) {
        OtpEndpoint.ApiResponse authResponse = new OtpEndpoint.ApiResponse(token, sharedUtilityService.breakReferenceForCustomer(customer,authHeader), HttpStatus.OK.value(), HttpStatus.OK.name(), "User has been logged in");
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
    public ResponseEntity<?>saveForm(@PathVariable long customer_id,@RequestParam long product_id)
    {
        try{
            Long id = Long.valueOf(customer_id);

            CustomCustomer customer=entityManager.find(CustomCustomer.class,id);
            if(customer==null)
            {
                return ResponseService.generateErrorResponse("Customer not found",HttpStatus.NOT_FOUND);
            }
            CustomProduct product=entityManager.find(CustomProduct.class,product_id);
            if(product==null)
            {
                return ResponseService.generateErrorResponse(Constant.PRODUCTNOTFOUND,HttpStatus.NOT_FOUND);
            }
            List<CustomProduct>savedForms=customer.getSavedForms();
            if ((((Status) product).getArchived() == 'Y' || !product.getDefaultSku().getActiveEndDate().after(new Date()))) {
                return ResponseService.generateErrorResponse("Cannot save an archived product",HttpStatus.BAD_REQUEST);
            }
            if(savedForms.contains(product))
                return ResponseService.generateErrorResponse("You can save a form only once",HttpStatus.UNPROCESSABLE_ENTITY);
            savedForms.add(product);
            customer.setSavedForms(savedForms);
            entityManager.merge(customer);
            Map<String,Object>responseBody=new HashMap<>();
           /* Map<String,Object>formBody=sharedUtilityService.createProductResponseMap(product,null,customer);*/
            CustomProductWrapper customProductWrapper = new CustomProductWrapper();
            List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(product_id);
            List<PhysicalRequirementDto> physicalRequirementDtoList = physicalRequirementDtoService.getPhysicalRequirementDto(product_id);
            List< ReserveCategoryAgeDto> ageRequirement = reserveCategoryAgeService.getReserveCategoryDto(product.getId());
            customProductWrapper.wrapDetails(product, reserveCategoryDtoList, physicalRequirementDtoList,ageRequirement,null);
            return ResponseService.generateSuccessResponse("Form Saved",customProductWrapper,HttpStatus.OK);
        }
        catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return ResponseService.generateErrorResponse("Error saving Form : "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @Authorize(value = {Constant.roleUser})
    @DeleteMapping("/unsave-form/{customer_id}")
    public ResponseEntity<?>unSaveForm(@PathVariable long customer_id,@RequestParam long product_id)
    {
        try{
            CustomCustomer customer=entityManager.find(CustomCustomer.class,customer_id);
            if(customer==null)
            {
                return ResponseService.generateErrorResponse("Customer not found",HttpStatus.NOT_FOUND);
            }
            CustomProduct product=entityManager.find(CustomProduct.class,product_id);
            if(product==null)
            {
                return ResponseService.generateErrorResponse(Constant.PRODUCTNOTFOUND,HttpStatus.NOT_FOUND);
            }
            List<CustomProduct>savedForms=customer.getSavedForms();
            if(savedForms.contains(product))
                savedForms.remove(product);
            else
                return ResponseService.generateErrorResponse("Form not present in saved Form list",HttpStatus.UNPROCESSABLE_ENTITY);
            customer.setSavedForms(savedForms);
            entityManager.merge(customer);
            CustomProductWrapper customProductWrapper = new CustomProductWrapper();
            List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(product_id);
            List<PhysicalRequirementDto> physicalRequirementDtoList = physicalRequirementDtoService.getPhysicalRequirementDto(product_id);
            List< ReserveCategoryAgeDto> ageRequirement = reserveCategoryAgeService.getReserveCategoryDto(product.getId());
            customProductWrapper.wrapDetails(product, reserveCategoryDtoList, physicalRequirementDtoList,ageRequirement,null);
            return ResponseService.generateSuccessResponse("Form Removed",customProductWrapper,HttpStatus.OK);
        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error removing Form : "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(value = "/forms/show-saved-forms")
    @Authorize(value = {Constant.roleUser})
    public ResponseEntity<?> getSavedForms(HttpServletRequest request, @RequestParam long customer_id) throws Exception {
        try {
            CustomCustomer customer = entityManager.find(CustomCustomer.class, customer_id);
            if (customer == null)
                ResponseService.generateErrorResponse("Customer with this id not found", HttpStatus.NOT_FOUND);
            if (customer.getSavedForms().isEmpty())
                ResponseService.generateErrorResponse("Saved form list is empty", HttpStatus.NOT_FOUND);
            List<CustomProductWrapper> listOfSavedProducts = new ArrayList<>();
            for (Product product : customer.getSavedForms()) {
                CustomProduct customProduct=entityManager.find(CustomProduct.class,product.getId());
                if ((((Status) customProduct).getArchived() == 'Y')) {
                    continue;
                }
                CustomProductWrapper customProductWrapper = new CustomProductWrapper();
                List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(product.getId());
                List<PhysicalRequirementDto> physicalRequirementDtoList = physicalRequirementDtoService.getPhysicalRequirementDto(product.getId());
                List< ReserveCategoryAgeDto> ageRequirement = reserveCategoryAgeService.getReserveCategoryDto(product.getId());
                customProductWrapper.wrapDetails(customProduct, reserveCategoryDtoList, physicalRequirementDtoList,ageRequirement,null);
                listOfSavedProducts.add(customProductWrapper);
            }
            return ResponseService.generateSuccessResponse("Forms saved : ", listOfSavedProducts, HttpStatus.OK);
        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>("SOMEEXCEPTIONOCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/forms/show-filled-forms")
    public ResponseEntity<?> getFilledFormsByUserId(HttpServletRequest request, @RequestParam long customer_id) throws Exception {
        try {
            CustomCustomer customer = entityManager.find(CustomCustomer.class, customer_id);
            if (customer == null)
                ResponseService.generateErrorResponse("Customer with this id not found", HttpStatus.NOT_FOUND);
            if (customer.getSavedForms().isEmpty())
                ResponseService.generateErrorResponse("Saved form list is empty", HttpStatus.NOT_FOUND);
            List<CustomProductWrapper> listOfSavedProducts = new ArrayList<>();
            for (Product product : customer.getSavedForms()) {
                CustomProduct customProduct=entityManager.find(CustomProduct.class,product.getId());
                if ((((Status) customProduct).getArchived() == 'Y')) {
                    continue;
                }
                CustomProductWrapper customProductWrapper = new CustomProductWrapper();
                List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(product.getId());
                List<PhysicalRequirementDto> physicalRequirementDtoList = physicalRequirementDtoService.getPhysicalRequirementDto(product.getId());
                List< ReserveCategoryAgeDto> ageRequirement = reserveCategoryAgeService.getReserveCategoryDto(product.getId());
                customProductWrapper.wrapDetails(customProduct, reserveCategoryDtoList, physicalRequirementDtoList,ageRequirement,null);
                listOfSavedProducts.add(customProductWrapper);
            }
            return ResponseService.generateSuccessResponse("Forms saved : ", listOfSavedProducts, HttpStatus.OK);
        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>("SOMEEXCEPTIONOCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/forms/show-recommended-forms")
    public ResponseEntity<?> getRecommendedFormsByUserId(HttpServletRequest request, @RequestParam long customer_id) throws Exception {
        try {
            CustomCustomer customer = entityManager.find(CustomCustomer.class, customer_id);
            if (customer == null)
                ResponseService.generateErrorResponse("Customer with this id not found", HttpStatus.NOT_FOUND);
            if (customer.getSavedForms().isEmpty())
                ResponseService.generateErrorResponse("Saved form list is empty", HttpStatus.NOT_FOUND);
            List<CustomProductWrapper> listOfSavedProducts = new ArrayList<>();
            for (Product product : customer.getSavedForms()) {
                CustomProduct customProduct=entityManager.find(CustomProduct.class,product.getId());
                if ((((Status) customProduct).getArchived() == 'Y')) {
                    continue;
                }
                CustomProductWrapper customProductWrapper = new CustomProductWrapper();
                List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(product.getId());
                List<PhysicalRequirementDto> physicalRequirementDtoList = physicalRequirementDtoService.getPhysicalRequirementDto(product.getId());
                List<Post>postList= customProduct.getPosts();
                List< ReserveCategoryAgeDto> ageRequirement = reserveCategoryAgeService.getReserveCategoryDto(product.getId());
                customProductWrapper.wrapDetails(customProduct, reserveCategoryDtoList, physicalRequirementDtoList,ageRequirement,postList);
                listOfSavedProducts.add(customProductWrapper);
            }
            return ResponseService.generateSuccessResponse("Forms saved : ", listOfSavedProducts, HttpStatus.OK);
        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>("SOMEEXCEPTIONOCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-customers")
    @Authorize(value = {Constant.roleServiceProvider,Constant.roleAdmin,Constant.roleSuperAdmin,Constant.roleServiceProviderAdmin})
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader(value = "Authorization") String authHeader) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();
            int startPosition = offset * limit;
            TypedQuery<CustomCustomer> query = entityManager.createQuery(Constant.GET_ALL_CUSTOMERS, CustomCustomer.class);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);
            List<Map> results = new ArrayList<>();
            for (CustomCustomer customer : query.getResultList()) {
                Customer customerToadd = customerService.readCustomerById(customer.getId());
                results.add(sharedUtilityService.breakReferenceForCustomer(customerToadd,authHeader));
            }
            return ResponseService.generateSuccessResponse("List of customers : ", results, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in customers: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @Authorize(value = {Constant.roleUser})
    @PostMapping("/set-referrer/{customer_id}/{service_provider_id}")

    public ResponseEntity<?> setReferrerForCustomer(@PathVariable Long customer_id, @PathVariable Long service_provider_id) {
        try {
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer_id);
            if (customCustomer == null)
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, service_provider_id);
            if (serviceProvider == null)
                return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.NOT_FOUND);
            List<CustomerReferrer>referrerSp=customCustomer.getMyReferrer();
            CustomerReferrer primaryRef = null;
            for(CustomerReferrer customerReferrer:referrerSp)
            {
                if(customerReferrer.getPrimaryRef() != null && customerReferrer.getPrimaryRef()) {
                    primaryRef = customerReferrer;
                }
                if(customerReferrer.getServiceProvider().getService_provider_id().equals(service_provider_id))
                    return ResponseService.generateErrorResponse("Selected Service Provider already set as Referrer", HttpStatus.BAD_REQUEST);
            }
            if(!referrerSp.isEmpty() && primaryRef != null) {
                primaryRef.setPrimaryRef(false);
                entityManager.merge(primaryRef);
            }

            CustomerReferrer customerReferrer=new CustomerReferrer();
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
        }  catch (Exception e) {
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
    public QualificationDetails findQualificationDetailForServiceProvider(Long qualificationDetailId,ServiceProviderEntity serviceProviderEntity) throws IllegalArgumentException {
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

    public Boolean validateDate(String dateOfIssueStr, String validUptoStr) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);

        try {
            // Validate format
            if (!isValidDateFormat(dateOfIssueStr, dateFormat)) {
                throw new IllegalArgumentException("Date of Issue must be in yyyy-MM-dd format");
            }

            Date dateOfIssue = dateFormat.parse(dateOfIssueStr);

            Date validUpto = null;
            if (validUptoStr != null) {
                if (!isValidDateFormat(validUptoStr, dateFormat)) {
                    throw new IllegalArgumentException("Valid Upto Date must be in yyyy-MM-dd format");
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
    public ResponseEntity<?> createUser()
    {
        CustomCustomer customCustomer=new CustomCustomer();
        customCustomer.setId(customerService.findNextCustomerId());
        entityManager.persist(customCustomer);
        Long id=customCustomer.getId();
        return ResponseService.generateSuccessResponse("User created successfully",customCustomer,HttpStatus.CREATED);
    }

    public static Date convertStringToDate(String dateStr) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        return dateFormat.parse(dateStr);
    }

}