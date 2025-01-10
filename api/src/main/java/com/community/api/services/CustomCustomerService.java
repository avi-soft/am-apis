package com.community.api.services;
import com.community.api.component.Constant;
import com.community.api.entity.CustomCustomer;
import org.apache.commons.collections4.CollectionUtils;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class CustomCustomerService {
    private EntityManager em;
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

}
