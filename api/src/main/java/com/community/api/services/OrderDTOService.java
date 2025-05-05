package com.community.api.services;

import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.PhysicalRequirementDto;
import com.community.api.dto.PostDetailsDTO;
import com.community.api.dto.ReserveCategoryAgeDto;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.entity.CombinedOrderDTO;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductReserveCategoryBornBeforeAfterRef;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.OrderCustomerDetailsDTO;
import com.community.api.entity.OrderDTO;
import com.community.api.entity.*;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderAttribute;
import org.broadleafcommerce.core.order.domain.OrderAttributeImpl;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderDTOService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ReserveCategoryDtoService reserveCategoryDtoService;
    @Autowired
    private PhysicalRequirementDtoService physicalRequirementDtoService;
    @Autowired
    private ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;
    @Autowired
    private ReserveCategoryAgeService reserveCategoryAgeService;
    @Autowired
    private ProductReserveCategoryFeePostRefService feePostRefService;
    @Transactional
    public CombinedOrderDTO wrapOrder(Order order, CustomOrderState orderState, CustomServiceProviderTicket ticket, OrderCustomerDetailsDTO customerDetails)
    {
        OrderDTO orderDTO=null;
        Long assigneeId=null;
        if(ticket!=null)
            assigneeId=ticket.getAssignee();
        List<Long>preferenceOrder=null;
        List<PostDetailsDTO>postPreferenceOrder=new ArrayList<>();
        OrderAttribute orderAttribute =(OrderAttribute)order.getOrderAttributes().get("sorted");
        if(orderAttribute!=null)
        {
        String retrievedPostPreferenceString=orderAttribute.getValue();
        if(!retrievedPostPreferenceString.equals("NO_AVAILABLE_POSTS"))
        {
        if (retrievedPostPreferenceString != null && !retrievedPostPreferenceString.isEmpty()) {
            preferenceOrder = Arrays.stream(retrievedPostPreferenceString.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }
        for(Long id :preferenceOrder)
        {
            Post post=entityManager.find(Post.class,id);
            if(post!=null) {
                PostDetailsDTO detailsDTO=new PostDetailsDTO();
                detailsDTO.setPostId(post.getPostId());
                detailsDTO.setPostName(post.getPostName());
                detailsDTO.setPostCode(post.getPostCode());
                postPreferenceOrder.add(detailsDTO);
            }
        }}}
        //if(order.getOrderItems().get(0).getOrderItemAttributes().containsKey("assigneeSPId"))
        orderDTO = new OrderDTO(
                order.getId(),
                postPreferenceOrder,
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
        /*List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(productId);
        List<PhysicalRequirementDto> physicalRequirementDtoList = physicalRequirementDtoService.getPhysicalRequirementDto(productId);*/
                    List<Post> postList= customProduct.getPosts();
                    //List<ReserveCategoryAgeDto> ageRequirement = reserveCategoryAgeService.getReserveCategoryDto(productId);
        customProductWrapper.wrapDetails(customProduct, postList,null,feePostRefService);
    }
    CombinedOrderDTO combinedOrderDTO=new CombinedOrderDTO();
                combinedOrderDTO.setOrderDetails(orderDTO);
                combinedOrderDTO.setProductDetails(customProductWrapper);
                combinedOrderDTO.setTicket(ticket);
                combinedOrderDTO.setCustomerDetails(customerDetails);
                return combinedOrderDTO;
    }
}
