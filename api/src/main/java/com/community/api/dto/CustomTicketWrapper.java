package com.community.api.dto;

import com.community.api.entity.CombinedOrderDTO;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketType;
import com.community.api.entity.Role;
import com.community.api.services.OrderDTOService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.broadleafcommerce.common.rest.api.wrapper.APIWrapper;
import org.broadleafcommerce.common.rest.api.wrapper.BaseWrapper;
import org.broadleafcommerce.core.order.domain.OrderImpl;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class CustomTicketWrapper extends BaseWrapper implements APIWrapper<CustomServiceProviderTicket> {
    @JsonProperty("ticket_id")
    protected Long id;

    @JsonProperty("created_date")
    protected Date createdDate;

    @JsonProperty("assignee_name")
    protected String assigneeName;

    @JsonProperty("modified_date")
    protected Date modifiedDate;

    @JsonProperty("assignee_user_id")
    protected Long assigneeUserId;

    @JsonProperty("assignee_role")
    protected Role assigneeRole;

    @JsonProperty("modifier_user_id")
    protected Long modifierUserId;

    @JsonProperty("modifier_role")
    protected Role modifierRole;

    @JsonProperty("target_completion_time")
    protected Date targetCompletionDate;

    @JsonProperty("assigned_date")
    protected Date assignedDate;

    @JsonProperty("ticket_state")
    protected CustomTicketState customTicketState;

    @JsonProperty("ticket_type")
    protected CustomTicketType customTicketType;

    @JsonProperty("ticket_status")
    protected CustomTicketStatus customTicketStatus;

    @JsonProperty("order")
    protected CombinedOrderDTO order;
    @JsonProperty("task_description")
    protected String task_desc;

    public void customWrapDetails(CustomServiceProviderTicket customServiceProviderTicket, CombinedOrderDTO combinedOrderDTO) {
        this.id = customServiceProviderTicket.getTicketId();
        this.assigneeUserId = customServiceProviderTicket.getAssignee();
        this.assigneeRole = customServiceProviderTicket.getAssigneeRole();
        combinedOrderDTO.setTicket(null);
        this.order = combinedOrderDTO;
        this.createdDate = customServiceProviderTicket.getCreatedDate();
        this.modifiedDate = customServiceProviderTicket.getModifiedDate();
        this.targetCompletionDate = customServiceProviderTicket.getTargetCompletionDate();
        this.modifierUserId = customServiceProviderTicket.getModifierId();
        this.modifierRole = customServiceProviderTicket.getModifierRole();
        this.customTicketState = customServiceProviderTicket.getTicketState();
        if(customServiceProviderTicket.getTicketType().getTicketTypeId()==3)
        {
            this.task_desc=customServiceProviderTicket.getDesc();
        }
        this.customTicketType = customServiceProviderTicket.getTicketType();
        this.customTicketStatus = customServiceProviderTicket.getTicketStatus();
        this.assignedDate = customServiceProviderTicket.getTicketAssignDate();
        this.assigneeName = combinedOrderDTO.getCustomerDetails().getFullName();
    }

    public void customWrapDetailsGetAll(CustomServiceProviderTicket customServiceProviderTicket, CombinedOrderDTO combinedOrderDTO) {
        this.id = customServiceProviderTicket.getTicketId();
        this.assigneeUserId = customServiceProviderTicket.getAssignee();
        this.assigneeRole = customServiceProviderTicket.getAssigneeRole();
        combinedOrderDTO.setCustomerDetails(null);
        combinedOrderDTO.setOrderDetails(null);
        combinedOrderDTO.setTicket(null);
        this.order = combinedOrderDTO;
        this.createdDate = customServiceProviderTicket.getCreatedDate();
        this.modifiedDate = customServiceProviderTicket.getModifiedDate();
        this.targetCompletionDate = customServiceProviderTicket.getTargetCompletionDate();
        this.modifierUserId = customServiceProviderTicket.getModifierId();
        this.modifierRole = customServiceProviderTicket.getModifierRole();
        this.customTicketState = customServiceProviderTicket.getTicketState();
        this.customTicketType = customServiceProviderTicket.getTicketType();
        this.customTicketStatus = customServiceProviderTicket.getTicketStatus();
        this.assignedDate = customServiceProviderTicket.getTicketAssignDate();
        this.assigneeName = combinedOrderDTO.getCustomerDetails().getFullName();
    }

    @Override
    public void wrapDetails(CustomServiceProviderTicket customServiceProviderTicket, HttpServletRequest httpServletRequest) {

    }

    @Override
    public void wrapSummary(CustomServiceProviderTicket customServiceProviderTicket, HttpServletRequest httpServletRequest) {

    }

}
