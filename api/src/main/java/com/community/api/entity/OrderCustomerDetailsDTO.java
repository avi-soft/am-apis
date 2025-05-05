package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.broadleafcommerce.profile.core.domain.CustomerAddress;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderCustomerDetailsDTO {
   Long customerId;
   String fullName;
   String emailAddress;
   String mobileNumber;
   Map<String,Map<String,String>> customerAddress;
   String username;
}
