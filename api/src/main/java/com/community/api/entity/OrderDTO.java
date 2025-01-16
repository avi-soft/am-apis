package com.community.api.entity;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.PostDetailsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.order.service.type.OrderStatus;

import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO
{
    private Long orderId;
    private List<PostDetailsDTO> postPreferenceOrder;
    private String orderName;
    private Money total;
    private Date orderPlacedDate;
    private String orderNumber;
    private String customerEmail;
    private Long customerId;
    private Money subTotal;
    private Integer orderStateId;
    private Long spAssigneeId;
}

