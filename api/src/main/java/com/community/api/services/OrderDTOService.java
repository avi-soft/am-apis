package com.community.api.services;

import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.PhysicalRequirementDto;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.entity.CombinedOrderDTO;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.OrderCustomerDetailsDTO;
import com.community.api.entity.OrderDTO;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class OrderDTOService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ReserveCategoryDtoService reserveCategoryDtoService;
    @Autowired
    private PhysicalRequirementDtoService physicalRequirementDtoService;
    @Transactional
    public CombinedOrderDTO wrapOrder(Order order, CustomOrderState orderState, CustomServiceProviderTicket ticket, OrderCustomerDetailsDTO customerDetails)
    {
        OrderDTO orderDTO=null;
        Long assigneeId=null;
        if(ticket!=null)
            assigneeId=ticket.getAssignee();
        if(order.getOrderItems().get(0).getOrderItemAttributes().containsKey("assigneeSPId"))
        orderDTO = new OrderDTO(
                order.getId(),
                order.getName(),
                order.getTotal(),
                order.getSubmitDate(),
                order.getOrderNumber(),
                order.getEmailAddress(),
                order.getCustomer().getId(),
                order.getSubTotal(),
                orderState.getOrderStateId(),
                assigneeId
        );
    OrderItem orderItem=order.getOrderItems().get(0);
    Long productId=Long.parseLong(orderItem.getOrderItemAttributes().get("productId").getValue());
    CustomProduct customProduct=entityManager.find(CustomProduct.class,productId);
    CustomProductWrapper customProductWrapper=null;
                if(customProduct!=null) {
        customProductWrapper = new CustomProductWrapper();
        List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(productId);
        List<PhysicalRequirementDto> physicalRequirementDtoList = physicalRequirementDtoService.getPhysicalRequirementDto(productId);
        customProductWrapper.wrapDetails(customProduct, reserveCategoryDtoList, physicalRequirementDtoList);
    }
    CombinedOrderDTO combinedOrderDTO=new CombinedOrderDTO();
                combinedOrderDTO.setOrderDetails(orderDTO);
                combinedOrderDTO.setProductDetails(customProductWrapper);
                combinedOrderDTO.setTicket(ticket);
                combinedOrderDTO.setCustomerDetails(customerDetails);
                return combinedOrderDTO;
    }
}
