package com.community.api.endpoint.avisoft.controller.otpmodule;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.entity.CustomAdmin;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.ExternalUseToken;
import com.community.api.entity.ServiceProviderReRankingEligibility;
import com.community.api.entity.ServiceProviderReRankingScore;
import com.community.api.entity.ServiceProviderTestStatus;
import com.community.api.entity.UserAcknowledgement;
import com.community.api.services.Admin.AdminService;
import com.community.api.services.ApiConstants;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.PdfEditService;
import com.community.api.services.RateLimiterService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.SanitizerService;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.TwilioService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.mchange.rmi.NotAuthorizedException;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import io.github.bucket4j.Bucket;
import io.swagger.annotations.ApiResponse;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/otp")
public class OtpEndpoint {

    private static final Logger log = LoggerFactory.getLogger(OtpEndpoint.class);

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private SharedUtilityService sharedUtilityService;

    @Autowired
    private TwilioService twilioService;

    @Autowired
    private CustomCustomerService customCustomerService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private EntityManager em;

    @Autowired
    private CustomerService customerService;
    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private ServiceProviderServiceImpl serviceProviderService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private SanitizerService sanitizerService;

    @Autowired
    private ResponseService responseService;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.accountSid}")
    private String accountSid;
    @Autowired
    private AdminService adminService;

    @PostMapping(value = "/send-otp", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendOtp(@RequestBody CustomCustomer customerDetails, HttpServletRequest request, HttpSession session, @RequestHeader(value = "Authorization", required = false) String authHeader) throws UnsupportedEncodingException {
        try {

            // TODO- @RAMAN NEED TO SEE THIS ON MONDAY AND TEST WHOLE FUNCTIONALITY.
            if (authHeader != null) {
                String jwtToken = authHeader.substring(7);
                String ipAddress = request.getRemoteAddr();
                String userAgent = request.getHeader("User-Agent");
                jwtTokenUtil.validateArchived(jwtToken, ipAddress, userAgent);

                int role = jwtTokenUtil.extractRoleId(jwtToken);
                if (roleService.findRoleName(role).equals(Constant.roleUser)) {
                    return responseService.generateErrorResponse("Forbidden Access", HttpStatus.FORBIDDEN);
                }
            }

            if (customerDetails.getMobileNumber() == null || customerDetails.getMobileNumber().isEmpty()) {
                return responseService.generateErrorResponse(ApiConstants.MOBILE_NUMBER_NULL_OR_EMPTY, HttpStatus.NOT_ACCEPTABLE);
            }

            String mobileNumber = customerDetails.getMobileNumber().startsWith("0")
                    ? customerDetails.getMobileNumber().substring(1)
                    : customerDetails.getMobileNumber();
            String countryCode = customerDetails.getCountryCode() == null || customerDetails.getCountryCode().isEmpty()
                    ? Constant.COUNTRY_CODE
                    : customerDetails.getCountryCode();


            CustomAdmin customAdmin = adminService.findAdminByPhone(mobileNumber, countryCode);
            if (customAdmin != null) {
                if (customAdmin.getRole() == 1) {
                    return ResponseService.generateErrorResponse("Number already registered as " + "SuperAdmin", HttpStatus.BAD_REQUEST);
                } else if (customAdmin.getRole() == 2) {
                    return ResponseService.generateErrorResponse("Number already registered as " + "Admin", HttpStatus.BAD_REQUEST);
                } else if (customAdmin.getRole() == 3) {
                    return ResponseService.generateErrorResponse("Number already registered as " + "Service Provider Admin", HttpStatus.BAD_REQUEST);
                }
            }

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhoneWithOtp(customerDetails.getMobileNumber(), countryCode);
            if (existingCustomer != null) {
                return responseService.generateErrorResponse(ApiConstants.CUSTOMER_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
            }

            Bucket bucket = rateLimiterService.resolveBucket(customerDetails.getMobileNumber(), "/otp/send-otp");
            if (bucket.tryConsume(1)) {
                if (!customCustomerService.isValidMobileNumber(mobileNumber)) {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_MOBILE_NUMBER, HttpStatus.BAD_REQUEST);

                }

                ResponseEntity<Map<String, Object>> otpResponse = twilioService.sendOtpToMobile(mobileNumber, countryCode, authHeader,null);
                Map<String, Object> responseBody = otpResponse.getBody();

                if (responseBody.get("otp") != null) {
                    return responseService.generateSuccessResponse((String) responseBody.get("message"), responseBody.get("otp"), HttpStatus.OK);
                } else {
                    return responseService.generateErrorResponse((String) responseBody.get("message"), HttpStatus.BAD_REQUEST);
                }
            } else {
                return responseService.generateErrorResponse(ApiConstants.RATE_LIMIT_EXCEEDED, HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
            }
        } catch (NotAuthorizedException notAuthorizedException) {
            exceptionHandling.handleException(notAuthorizedException);
            return ResponseService.generateErrorResponse("Your account is suspended ,please contact support.", HttpStatus.UNAUTHORIZED);
        } catch (PersistenceException persistenceException) {
            exceptionHandling.handleException(persistenceException);
            return ResponseService.generateErrorResponse("Error sending otp: " + persistenceException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Some error occurred: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Autowired
    PdfEditService pdfEditService;
    @Transactional
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, Object> loginDetails, HttpSession session, @RequestParam(name = "tempAuth",required = false,defaultValue ="false")Boolean tempAuth, HttpServletRequest request, @RequestHeader(name = "Authorization",required = false)String authHeadReq) {
        try {

            if (authHeadReq != null) {
                String jwtToken = authHeadReq.substring(7);
                String ipAddress = request.getRemoteAddr();
                String userAgent = request.getHeader("User-Agent");
                jwtTokenUtil.validateArchived(jwtToken, ipAddress, userAgent);

                int role = jwtTokenUtil.extractRoleId(jwtToken);
                if (roleService.findRoleName(role).equals(Constant.roleUser)) {
                    return responseService.generateErrorResponse("Forbidden Access", HttpStatus.FORBIDDEN);
                }
            }

            String authHeader = Constant.BEARER_CONST;
            loginDetails = sanitizerService.sanitizeInputMap(loginDetails);

            if (loginDetails == null) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            String otpEntered = (String) loginDetails.get("otpEntered");
            Integer role = (Integer) loginDetails.get("role");
            String countryCode = (String) loginDetails.get("countryCode");
            String username = (String) loginDetails.get("username");
            String mobileNumber = (String) loginDetails.get("mobileNumber");
            String ackId=(String) loginDetails.get("ack");
            if (role == null) {
                return responseService.generateErrorResponse(ApiConstants.ROLE_EMPTY, HttpStatus.BAD_REQUEST);
            }

            if (roleService.findRoleName(role).equals(Constant.roleUser)) {
                if (username != null) {
                    if (customerService == null) {
                        return responseService.generateErrorResponse(ApiConstants.CUSTOMER_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    Customer customer = customerService.readCustomerByUsername(username);

                    if (customer == null) {
                        return responseService.generateErrorResponse(ApiConstants.NO_RECORDS_FOUND, HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                    CustomCustomer customCustomer = em.find(CustomCustomer.class, customer.getId());
                    if (customCustomer != null) {
                        mobileNumber = customCustomer.getMobileNumber();
                    } else {
                        return responseService.generateErrorResponse(ApiConstants.NO_RECORDS_FOUND, HttpStatus.NOT_FOUND);
                    }
                } else if (mobileNumber == null) {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.INTERNAL_SERVER_ERROR);
                }

                if (otpEntered == null || otpEntered.trim().isEmpty()) {
                    return responseService.generateErrorResponse("otp is null ", HttpStatus.BAD_REQUEST);
                }

                CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);

                if (existingCustomer == null) {
                    return responseService.generateErrorResponse(ApiConstants.NO_RECORDS_FOUND, HttpStatus.NOT_FOUND);
                }

                if (existingCustomer.getArchived()) {
                    throw new NotAuthorizedException();
                }

                String storedOtp = existingCustomer.getOtp();
                String ipAddress = request.getRemoteAddr();
                String userAgent = request.getHeader("User-Agent");
                String tokenKey = "authToken_" + mobileNumber;
                Customer customer = customerService.readCustomerById(existingCustomer.getId());

                if (otpEntered.equals(storedOtp)) {
                    if(!existingCustomer.getIsAcknowledged()&&(ackId==null||ackId.isEmpty()))
                        return ResponseService.generateErrorResponse("Need acknowledgement for user",HttpStatus.BAD_REQUEST);
                    else if (existingCustomer.getIsAcknowledged()&&ackId!=null) {
                        return ResponseService.generateErrorResponse("User already acknowledged", HttpStatus.BAD_REQUEST);
                    }
                    else if(!existingCustomer.getIsAcknowledged()&&(ackId!=null||!ackId.isEmpty()))
                        {
                            try {
                                UserAcknowledgement userAcknowledgement = new UserAcknowledgement();
                                userAcknowledgement.setUserId(existingCustomer.getId());
                                userAcknowledgement.setAcknowledgementVersion("v1");
                                userAcknowledgement.setAcknowledgementId(ackId);
                                userAcknowledgement.setAcknowledgedAt(new Date());
                                em.merge(userAcknowledgement);
                                existingCustomer.setIsAcknowledged(true);
                        }catch (Exception exception)
                            {
                                exception.printStackTrace();
                                return ResponseService.generateErrorResponse("Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                    }
                    System.out.println("ack set");
                    existingCustomer.setOtp(null);
                    em.persist(existingCustomer);

                    if (tempAuth.equals(true)) {
                        String newToken = jwtUtil.generateToken(existingCustomer.getId(), role, ipAddress, userAgent);
                        session.setAttribute(tokenKey, newToken);
                        authHeader = authHeadReq;
                        String jwtToken = authHeader.substring(7);
                        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
                        Long userId = jwtTokenUtil.extractId(jwtToken);
                        if (roleId == 4) {
                            ExternalUseToken externalUseToken = em.find(ExternalUseToken.class, userId);
                            if (externalUseToken == null) {
                                externalUseToken = new ExternalUseToken();
                                externalUseToken.setToken(newToken);
                                externalUseToken.setSpId(userId);
                                em.persist(externalUseToken);
                            } else {
                                externalUseToken.setToken(newToken);
                                em.merge(externalUseToken);
                            }
                        }
                        ApiResponse response = new ApiResponse(newToken, sharedUtilityService.breakReferenceForCustomer(customer, authHeader, request), HttpStatus.OK.value(), HttpStatus.OK.name(), "User has been logged in");
                        pdfEditService.createPdfInMemory(ackId, 5, existingCustomer.getId(), mobileNumber);
                        return ResponseEntity.ok(response);
                    } else {
                        String existingToken = existingCustomer.getToken();
                        if (existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {
                            authHeader = authHeader + existingToken;
                            ApiResponse response = new ApiResponse(existingToken, sharedUtilityService.breakReferenceForCustomer(customer, authHeader, request), HttpStatus.OK.value(), HttpStatus.OK.name(), "User has been logged in");
                            pdfEditService.createPdfInMemory(ackId, 5, existingCustomer.getId(), mobileNumber);
                            return ResponseEntity.ok(response);

                        } else {
                            String newToken = jwtUtil.generateToken(existingCustomer.getId(), role, ipAddress, userAgent);
                            session.setAttribute(tokenKey, newToken);
                            existingCustomer.setToken(newToken);
                            authHeader = authHeader + newToken;
                            em.persist(existingCustomer);
                            ApiResponse response = new ApiResponse(newToken, sharedUtilityService.breakReferenceForCustomer(customer, authHeader, request), HttpStatus.OK.value(), HttpStatus.OK.name(), "User has been logged in");
                            pdfEditService.createPdfInMemory(ackId, 5, existingCustomer.getId(), mobileNumber);
                            return ResponseEntity.ok(response);
                        }
                    }
                } else {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
                }
            } else if (!roleService.findRoleName(role).equals(Constant.roleUser)) {
                return serviceProviderService.verifyOtp(loginDetails, session, request);
            }

    /*    else if(roleService.findRoleName(role).equals(Constant.ADMIN) ||roleService.findRoleName(role).equals(Constant.SUPER_ADMIN) ||roleService.findRoleName(role).equals(Constant.roleAdminServiceProvider)) {
            return adminService.verifyOtpForAdmin(loginDetails,session,request);
        }*/

            else {
                return responseService.generateErrorResponse(ApiConstants.INVALID_ROLE, HttpStatus.BAD_REQUEST);
            }
        }catch (Exception exception) {
            exception.printStackTrace();
           /* exceptionHandling.handleException(exception);*/
            return responseService.generateErrorResponse("Otp verification error:" + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }/* catch (NotAuthorizedException notAuthorizedException) {
            notAuthorizedException.printStackTrace();
            exceptionHandling.handleException(notAuthorizedException);
            return ResponseService.generateErrorResponse("Your account is suspended ,please contact support.", HttpStatus.UNAUTHORIZED);
        } catch (PersistenceException persistenceException) {
            persistenceException.printStackTrace();
            exceptionHandling.handleException(persistenceException);
            return ResponseService.generateErrorResponse("Error verifying otp:" + persistenceException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } */
    }

    @Transactional
    @PostMapping("/service-provider-signup")
    public ResponseEntity<?> sendOtpToMobile(@RequestBody Map<String, Object> signupDetails) {
        try {
            signupDetails = sanitizerService.sanitizeInputMap(signupDetails);
            String mobileNumber = (String) signupDetails.get("mobileNumber");
            String countryCode = (String) signupDetails.get("countryCode");

            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }
            mobileNumber = mobileNumber.startsWith("0") ? mobileNumber.substring(1) : mobileNumber;
            if (customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode) != null) {
                return responseService.generateErrorResponse(ApiConstants.NUMBER_REGISTERED_AS_CUSTOMER, HttpStatus.BAD_REQUEST);
            }

            CustomAdmin customAdmin = adminService.findAdminByPhone(mobileNumber, countryCode);
            if (customAdmin != null) {
                if (customAdmin.getRole() == 1) {
                    return ResponseService.generateErrorResponse("Number already registered as " + "SuperAdmin", HttpStatus.BAD_REQUEST);
                } else if (customAdmin.getRole() == 2) {
                    return ResponseService.generateErrorResponse("Number already registered as " + "Admin", HttpStatus.BAD_REQUEST);
                } else if (customAdmin.getRole() == 3) {
                    return ResponseService.generateErrorResponse("Number already registered as " + "Service Provider Admin", HttpStatus.BAD_REQUEST);
                }
            }

            if (!serviceProviderService.isValidMobileNumber(mobileNumber)) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_MOBILE_NUMBER, HttpStatus.BAD_REQUEST);
            }

            Twilio.init(accountSid, authToken);
            String otp = serviceProviderService.generateOTP();

            ServiceProviderEntity existingServiceProvider = serviceProviderService.findServiceProviderByPhone(mobileNumber, countryCode);

            if (existingServiceProvider == null) {
                ServiceProviderEntity serviceProviderEntity = new ServiceProviderEntity();
                serviceProviderEntity.setCountry_code(countryCode);
                serviceProviderEntity.setMobileNumber(mobileNumber);
                serviceProviderEntity.setOtp(otp);
                serviceProviderEntity.setApproved(false);
                // Change it to Date carefully as it's been used in re-ranking logic. (FIND)
                serviceProviderEntity.setDateJoined(LocalDate.now());
                serviceProviderEntity.setUpdatedDate(new Date());
                ServiceProviderStatus serviceProviderStatus = em.find(ServiceProviderStatus.class, Constant.INITIAL_STATUS);
                serviceProviderEntity.setStatus(serviceProviderStatus);
                ServiceProviderTestStatus serviceProviderTestStatus = em.find(ServiceProviderTestStatus.class, Constant.INITIAL_TEST_STATUS);
                serviceProviderEntity.setServiceProviderStatus(serviceProviderTestStatus);
                serviceProviderEntity.setRole(4);

                em.persist(serviceProviderEntity);
                em.flush();

                ServiceProviderReRankingEligibility serviceProviderReRankingEligibility = new ServiceProviderReRankingEligibility();
                ServiceProviderReRankingScore serviceProviderReRankingScore = new ServiceProviderReRankingScore();
                serviceProviderReRankingEligibility.setServiceProvider(serviceProviderEntity);
                serviceProviderReRankingScore.setServiceProvider(serviceProviderEntity);
                em.persist(serviceProviderReRankingEligibility);
                em.persist(serviceProviderReRankingScore);
            } else if (existingServiceProvider.getOtp() != null) {
                existingServiceProvider.setOtp(otp);
                em.merge(existingServiceProvider);
            } else {
                return responseService.generateErrorResponse(ApiConstants.MOBILE_NUMBER_REGISTERED, HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> details = new HashMap<>();
            String maskedNumber = twilioService.genereateMaskednumber(mobileNumber);
            details.put("otp", otp);
            return responseService.generateSuccessResponse(ApiConstants.OTP_SENT_SUCCESSFULLY + " on " + maskedNumber, otp, HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return responseService.generateErrorResponse(ApiConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            } else {
                exceptionHandling.handleHttpClientErrorException(e);
                return responseService.generateErrorResponse(ApiConstants.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return responseService.generateErrorResponse(ApiConstants.ERROR_SENDING_OTP + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.ERROR_SENDING_OTP + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-service-provider")
    public ResponseEntity<?> getServiceProviderById(@RequestParam Long userId) {
        try {
            ServiceProviderEntity serviceProviderEntity = serviceProviderService.getServiceProviderById(userId);
            if (serviceProviderEntity == null) {
                return responseService.generateErrorResponse("Service provider not found " + userId, HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> details = new HashMap<>();
            // details.put("message", "Service provider details are");
            details.put("status", ApiConstants.STATUS_SUCCESS);
            details.put("status_code", HttpStatus.OK);
            details.put("data", serviceProviderEntity);
            return responseService.generateSuccessResponse("Service provider details are", details, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.INTERNAL_SERVER_ERROR + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    public static class ApiResponse {
        private Data data;
        private int status_code;
        private String status;
        private String message;
        private String token;


        public ApiResponse(String token, Map<String, Object> customerDetails, int statusCodeValue, String statusCode, String message) {
            this.data = new Data(customerDetails);
            this.status_code = statusCodeValue;
            this.status = statusCode;
            this.message = message;
            this.token = token;
        }

        public Data getData() {
            return data;
        }

        public int getStatus_code() {
            return status_code;
        }

        public String getStatus() {
            return status;
        }

        public String getToken() {
            return token;
        }

        public String getMessage() {
            return message;
        }

        public class Data {
            private Map<String, Object> userDetails;

            public Data(Map<String, Object> customerDetails) {
                this.userDetails = customerDetails;
            }

            public Map<String, Object> getUserDetails() {
                return userDetails;
            }
        }
    }


}
