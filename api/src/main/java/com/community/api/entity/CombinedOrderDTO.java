package com.community.api.entity;

import com.community.api.dto.CustomProductWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CombinedOrderDTO {
   private OrderDTO orderDetails;
   private CustomProductWrapper productDetails;
   private CustomServiceProviderTicket ticket;
   private OrderCustomerDetailsDTO customerDetails;
}
