package com.community.api.services;

import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerAddress;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerAddressFetcher {
    public Map<String,Map<String,String>> fetch(Customer customer)
    {
        Map<String,Map<String,String>> addresses=new HashMap<>();
    Map<String,String> currentAddress=new HashMap<>();
    Map<String,String>permanentAddress=new HashMap<>();
    for(CustomerAddress customerAddress:customer.getCustomerAddresses()) {
        if (customerAddress.getAddressName().equals("CURRENT_ADDRESS")) {
            currentAddress.put("state", customerAddress.getAddress().getStateProvinceRegion());
            currentAddress.put("city", customerAddress.getAddress().getCity());
            currentAddress.put("district", customerAddress.getAddress().getCounty());
            currentAddress.put("pincode", customerAddress.getAddress().getPostalCode());
            currentAddress.put("Address line", customerAddress.getAddress().getAddressLine1());
        }
        if (customerAddress.getAddressName().equals("PERMANENT_ADDRESS")) {
            permanentAddress.put("state", customerAddress.getAddress().getStateProvinceRegion());
            permanentAddress.put("city", customerAddress.getAddress().getCity());
            permanentAddress.put("district", customerAddress.getAddress().getCounty());
            permanentAddress.put("pincode", customerAddress.getAddress().getPostalCode());
            permanentAddress.put("Address line", customerAddress.getAddress().getAddressLine1());
        }
    }
    addresses.put("current_address",currentAddress);
    addresses.put("permanent_address",permanentAddress);
    return addresses;
    }
}
