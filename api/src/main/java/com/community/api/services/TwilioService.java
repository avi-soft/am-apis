package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomerReferrer;
import com.community.api.entity.ServiceProviderAddress;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.bcel.Const;
import org.broadleafcommerce.common.audit.Auditable;
import org.broadleafcommerce.common.audit.AuditableListener;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.HttpClientErrorException;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class TwilioService {

    private ExceptionHandlingImplement exceptionHandling;

    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.phoneNumber}")
    private String twilioPhoneNumber;




    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    private RoleService roleService;

    private CustomCustomerService customCustomerService;
    private EntityManager entityManager;
    private HttpSession httpSession;
    @Autowired
    private ServiceProviderServiceImpl serviceProviderService;
    @Autowired

    private CustomerService customerService;

    public TwilioService(ExceptionHandlingImplement exceptionHandlingImplement, CustomCustomerService customCustomerService, EntityManager entityManager, HttpSession httpSession, CustomerService customerService) {
        this.exceptionHandling = exceptionHandlingImplement;
        this.customCustomerService = customCustomerService;
        this.entityManager = entityManager;
        this.httpSession = httpSession;
        this.customerService = customerService;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> sendOtpToMobile(String mobileNumber, String countryCode,String authHeader) {
        String role=null;
        Long tokenUserId=null;
        Integer roleId=0;
        if(authHeader!=null) {
            String jwtToken = authHeader.substring(7);
            roleId = jwtTokenUtil.extractRoleId(jwtToken);
            tokenUserId = jwtTokenUtil.extractId(jwtToken);
            role = roleService.getRoleByRoleId(roleId).getRole_name();
        }
        if (mobileNumber == null || mobileNumber.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", ApiConstants.STATUS_ERROR,
                    "status_code", HttpStatus.BAD_REQUEST,
                    "message", ApiConstants.MOBILE_NUMBER_NULL_OR_EMPTY
            ));
        }

        try {
            Twilio.init(accountSid, authToken);
            String completeMobileNumber = countryCode + mobileNumber;
            String otp = generateOTP();

            // Uncomment the code to send OTP via SMS
            /*
            Message message = Message.creator(
                new PhoneNumber(completeMobileNumber),
                new PhoneNumber(twilioPhoneNumber),
                otp
            ).create();
            */

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);
            ServiceProviderEntity serviceProvider = serviceProviderService.findServiceProviderByPhone(mobileNumber, countryCode);
            String maskedNumber = this.genereateMaskednumber(mobileNumber);
            if (existingCustomer == null && serviceProvider == null) {
                CustomCustomer customerDetails = new CustomCustomer();
                customerDetails.setId(customerService.findNextCustomerId());
                customerDetails.setCountryCode(countryCode);
                customerDetails.setMobileNumber(mobileNumber);
                customerDetails.setOtp(otp);
                entityManager.persist(customerDetails);
                Customer customer=customerService.readCustomerById(customerDetails.getId());
                if(role!=null&&(role.equals(Constant.roleServiceProvider)||role.equals(Constant.roleSuperAdmin)||role.equals(Constant.roleAdmin))) {
                    ServiceProviderEntity serviceProviderEntity = entityManager.find(ServiceProviderEntity.class, tokenUserId);
                    CustomerReferrer customerReferrer = new CustomerReferrer();
                    customerReferrer.setCreatedAt(LocalDateTime.now());
                    customerReferrer.setCustomer(customerDetails);
                    customerReferrer.setServiceProvider(serviceProviderEntity);
                    customerReferrer.setPrimaryRef(true);
                    customerDetails.getMyReferrer().add(customerReferrer);
//                    System.out.println("User id is "+tokenUserId);
//                    System.out.println("Role id is "+roleId);
                customerDetails.setRegisteredBySp(true);
                customerDetails.setCreatedById(tokenUserId);
                customerDetails.setCreatedByRole(roleId);
                if(roleId==4 || roleId==3 || roleId==2 || roleId ==1)
                    customerDetails.setPrimaryRef(tokenUserId);
                entityManager.merge(customer);
                }
                else
                {
                    customerDetails.setCreatedByRole(5);
                    customerDetails.setCreatedById(customer.getId());
                    entityManager.merge(customerDetails);
                }
                return ResponseEntity.ok(Map.of(
                        "otp", otp,
                        "message", "Otp has been sent successfully on " + maskedNumber
                ));
            } else if (serviceProvider != null) {
                if(serviceProvider.getIsArchived())
                    return ResponseEntity.ok(Map.of(

                        "message","Your account has been suspended ,please contact support.",
                        "status", HttpStatus.UNAUTHORIZED,
                        "status_code", HttpStatus.UNAUTHORIZED.value()
                ));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "status_code", HttpStatus.BAD_REQUEST,
                        "message", ApiConstants.NUMBER_ALREADY_REGISTERED_SERVICE_PROVIDER
                ));
            } else {
                if(existingCustomer.getArchived().equals(true)) {
                    return ResponseEntity.ok(Map.of(

                            "message","Your account has been suspended ,please contact support.",
                            "status", HttpStatus.UNAUTHORIZED,
                            "status_code", HttpStatus.UNAUTHORIZED.value()
                    ));
                }
                existingCustomer.setOtp(otp);
                entityManager.merge(existingCustomer);
                return ResponseEntity.ok(Map.of(

                        "otp", otp,
                        "message", "Otp has been sent successfully on " + maskedNumber
                ));
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "message", ApiConstants.UNAUTHORIZED_ACCESS,
                        "status_code", HttpStatus.UNAUTHORIZED
                ));
            } else {
                exceptionHandling.handleHttpClientErrorException(e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "message", ApiConstants.INTERNAL_SERVER_ERROR,
                        "status_code", HttpStatus.INTERNAL_SERVER_ERROR
                ));
            }
        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", ApiConstants.STATUS_ERROR,
                    "message", ApiConstants.ERROR_SENDING_OTP + e.getMessage(),
                    "status_code", HttpStatus.INTERNAL_SERVER_ERROR
            ));
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", ApiConstants.STATUS_ERROR,
                    "message", ApiConstants.ERROR_SENDING_OTP + e.getMessage(),
                    "status_code", HttpStatus.INTERNAL_SERVER_ERROR
            ));
        }
    }

    public synchronized String genereateMaskednumber(String mobileNumber) {
        String lastFourDigits = mobileNumber.substring(mobileNumber.length() - 4);

        int numXs = mobileNumber.length() - 4;

        StringBuilder maskBuilder = new StringBuilder();
        for (int i = 0; i < numXs; i++) {
            maskBuilder.append('x');
        }
        String mask = maskBuilder.toString();

        String maskedNumber = mask + lastFourDigits;
        return maskedNumber;
    }


    private synchronized String generateOTP() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(8999);
        return String.valueOf(otp);
    }


    @Transactional
    public boolean setotp(String mobileNumber, String countryCode) {
        CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);

        if (existingCustomer != null) {
            String storedOtp = existingCustomer.getOtp();
            if (storedOtp != null) {
                existingCustomer.setOtp(null);
                entityManager.merge(existingCustomer);
                return true;
            }
        }
        return false;
    }
}

