package com.community.api.services;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.ServiceProviderTestStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.community.api.services.ServiceProvider.ServiceProviderServiceImpl.isAlphabetOnly;

@Service
public class CustomCustomerService {
    private EntityManager em;
    @Autowired
    SharedUtilityService sharedUtilityService;
    @Autowired
    JwtUtil jwtTokenUtil;
    public CustomCustomerService(EntityManager em)
    {
        this.em= em;
    }

    public Boolean validateInput(CustomCustomer customer) {
        if (customer.getUsername().isEmpty() || customer.getUsername() == null || customer.getMobileNumber().isEmpty() || customer.getMobileNumber() == null || customer.getPassword() == null || customer.getPassword().isEmpty())
            return false;
        if (!isValidMobileNumber(customer.getMobileNumber()))
            return false;

        return true;
    }

    public boolean isValidMobileNumber(String mobileNumber) {

        // If the mobile number is empty, return true (valid).
        if (mobileNumber == null || mobileNumber.isEmpty()) {
            return true;
        }

        // Remove leading "0" if present.
        if (mobileNumber.startsWith("0")) {
            mobileNumber = mobileNumber.substring(1);
        }

        String mobileNumberPattern = "^\\d{9,13}$";
        return Pattern.compile(mobileNumberPattern).matcher(mobileNumber).matches();
    }

    public CustomCustomer findCustomCustomerByPhone(String mobileNumber,String countryCode) {

        if (countryCode == null) {
            countryCode = Constant.COUNTRY_CODE;
        }

        return em.createQuery(Constant.PHONE_QUERY, CustomCustomer.class)
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("countryCode", countryCode)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public CustomCustomer findCustomCustomerById(Long customerId) {
        // Check if customerId is valid
        if (customerId == null) {
            return null;
        }

        return em.createQuery("SELECT c FROM CustomCustomer c WHERE c.id = :customerId", CustomCustomer.class)
                .setParameter("customerId", customerId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }



    public CustomCustomer findCustomCustomerByPhoneWithOtp(String mobileNumber,String countryCode) {

        if (countryCode == null) {
            countryCode = Constant.COUNTRY_CODE;
        }

        return em.createQuery(Constant.PHONE_QUERY_OTP, CustomCustomer.class)
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("countryCode", countryCode)
                .setParameter("otp", null)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public  List<String> validateAddress(String addressLine, String city, String pincode) {
        List<String> errorMessages = new ArrayList<>();

        // Validate Address Line: It should not be empty or null
        if (addressLine == null || addressLine.trim().isEmpty()) {
            errorMessages.add("Address Line cannot be empty.");
        }

        // Validate City: It should only contain alphabets and possibly spaces
        if (city == null || !Pattern.matches("^[a-zA-Z\\s]+$", city)) {
            errorMessages.add("City name should only contain alphabets and spaces.");
        }

        // Validate Pincode: It should be a 6-digit number where the first digit is not zero
        if (pincode == null || !Pattern.matches("^[1-9][0-9]{5}$", pincode)) {
            errorMessages.add("Pincode should be a 6-digit number starting with a digit from 1 to 9.");
        }

        // Return the list of error messages (if any)
        return errorMessages;
    }

    public List<BigInteger> filterCustomer(List<Long> service_provider_id,String first_name,String last_name,List<String>sub_state_prov_reg,List<String> county,List<Long> qualification_name,String username,Boolean completed,String authHeader,int page,int limit,String sort)  {
            List<Map<String, Object>> response = new ArrayList<>();
        int startPosition = page * limit;
        String jwtToken = authHeader.substring(7);
        /*Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
        Long tokenUserId = jwtTokenUtil.extractId(jwtToken);*/
        if(username!=null&&!username.isEmpty())
        {
            Query query=em.createNativeQuery("SELECT customer_id FROM blc_customer WHERE user_name = :username");
            query.setParameter("username",username);
            return query.getResultList();
        }
        Map<String, String> alias = new HashMap<>();
        Map<String,String>aliasQuery=new HashMap<>();
        if (first_name != null) {
            first_name = first_name.trim();
            first_name = first_name.toLowerCase();
        }
        if (last_name != null) {
            last_name = last_name.trim();
            last_name = last_name.toLowerCase();
        }

        aliasQuery.put("sub_state_prov_reg","JOIN blc_customer_address cust_addr ON cust.customer_id = cust_addr.customer_id JOIN blc_address addr ON cust_addr.address_id = addr.address_id ");
        aliasQuery.put("overlapping","JOIN qualification_details qual_details ON qual_details.custom_customer_id = cust.customer_id JOIN qualification qual ON qual_details.qualification_id = qual.qualification_id ");
        aliasQuery.put("service_provider_id","JOIN customer_referrer referrer ON cust.customer_id = referrer.customer_id ");
        aliasQuery.put("profile_completed","JOIN custom_customer cc ON cust.customer_id = cc.customer_id ");
        alias.put("sub_state_prov_reg", "addr");
        alias.put("county", "addr");
        alias.put("first_name", "cust");
        alias.put("last_name", "cust");
        alias.put("service_provider_id", "referrer");
        alias.put("overlapping","qual");
        alias.put("profile_completed","cc");
        String generalizedQuery=null;
            generalizedQuery = Constant.CUSTOMER_FILTER;
            if ((county != null &&!county.isEmpty()) || (sub_state_prov_reg != null && !sub_state_prov_reg.isEmpty())) {
            generalizedQuery=generalizedQuery+aliasQuery.get("sub_state_prov_reg");
            }
            if(qualification_name!=null&&!qualification_name.isEmpty())
            {
                generalizedQuery=generalizedQuery+aliasQuery.get("overlapping");
            }
            if(service_provider_id!=null)
            {
                generalizedQuery=generalizedQuery+aliasQuery.get("service_provider_id");
            }
        if(completed!=null)
        {
            generalizedQuery=generalizedQuery+aliasQuery.get("profile_completed");
        }
        generalizedQuery=generalizedQuery+" WHERE ";
        String[] fieldsNames = {"sub_state_prov_reg", "county", "first_name", "last_name", "service_provider_id","overlapping","profile_completed"};
        Object[] fields = {sub_state_prov_reg, county, first_name, last_name, service_provider_id,qualification_name,completed};
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
                    generalizedQuery += alias.get(fieldsNames[i]) + "." + fieldsNames[i] + " IN (:" + fieldsNames[i]+")" + " AND ";
                }
            }

        }


        System.out.println(generalizedQuery);
        generalizedQuery = generalizedQuery.trim();
        int lastSpaceIndex = generalizedQuery.lastIndexOf(" ");
        generalizedQuery = generalizedQuery.substring(0, lastSpaceIndex)+" ORDER by cust.customer_id "+sort;
        System.out.println(generalizedQuery);
        Query query;
        query = em.createNativeQuery(generalizedQuery);
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] != null) {
                System.out.println(fieldsNames[i]);
                System.out.println(fields[i]);
                query.setParameter(fieldsNames[i], fields[i]);
            }
        }


     /*   query.setFirstResult(startPosition);
        query.setMaxResults(limit);*/
        List<BigInteger>resultList=query.getResultList();
        return resultList;
    }


}
