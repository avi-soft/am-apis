package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.Earnings;
import com.community.api.services.exception.ExceptionHandlingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class EarningService {
    private final CatalogService catalogService;
    private final ExceptionHandlingService exceptionHandlingService;
    private final EntityManager entityManager;

    public void createEarning(CustomServiceProviderTicket ticket) {
        try {
            if(ticket == null) {
                throw new IllegalArgumentException("ticket is null");
            }
            if(!ticket.getTicketType().getTicketTypeId().equals(Constant.TICKET_TYPE_ID_OF_PRIMARY_TICKET)) {
                throw new IllegalArgumentException("Earning only applicable for primary tickets.");
            }

            Order order = ticket.getOrder();
            OrderItem orderItem = order.getOrderItems().get(0);

            Product product = findProductFromItemAttribute(orderItem);
            CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());
            Double platformFee = customProduct.getPlatformFee();

            if(platformFee == null || platformFee.equals(0d) || ticket.getAssignee() == null) {
                log.info("Either platform fee is null or zero or assignee is null");
            } else {

                Earnings earnings = new Earnings();
                earnings.setDate(new Date());
                earnings.setOrderId(order.getId());
                earnings.setPlatformFee(platformFee);

                platformFee = (2 * platformFee )/ 5;
                earnings.setCommission(platformFee);
                earnings.setPending(platformFee);

                earnings.setPaid(0d);
                earnings.setPaymentDone(false);
                earnings.setTicketId(ticket.getTicketId());
                earnings.setProviderId(ticket.getAssignee());
                earnings.setOrderAmount(order.getSubTotal().getAmount().longValue());
                earnings.setSettled(false);

                entityManager.merge(earnings);
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw illegalArgumentException;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw exception;
        }
    }

    public Product findProductFromItemAttribute(OrderItem orderItem) {
        Long productId = Long.parseLong(orderItem.getOrderItemAttributes().get("productId").getValue());
        Product product = catalogService.findProductById(productId);
        return product;
    }
}
