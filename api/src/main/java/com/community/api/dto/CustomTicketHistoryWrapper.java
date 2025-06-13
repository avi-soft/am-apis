package com.community.api.dto;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketHistory;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketType;
import com.community.api.entity.Role;
import com.community.api.services.exception.ExceptionHandlingService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.broadleafcommerce.common.rest.api.wrapper.APIWrapper;
import org.broadleafcommerce.common.rest.api.wrapper.BaseWrapper;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

public class CustomTicketHistoryWrapper extends BaseWrapper implements APIWrapper<CustomServiceProviderTicket> {

    @JsonProperty("ticket_history_id")
    protected Long ticketHistoryId;

    @JsonProperty("ticket")
    protected CustomServiceProviderTicket ticket;

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

    @JsonProperty("modifier_name")
    protected String modifierName;

    @JsonProperty("target_completion_time")
    protected Date targetCompletionDate;

    @JsonProperty("assigned_date")
    protected Date assignedDate;

    @JsonProperty("comment")
    protected String comment;

    @JsonProperty("ticket_state")
    protected CustomTicketState customTicketState;

    @JsonProperty("ticket_type")
    protected CustomTicketType customTicketType;

    @JsonProperty("ticket_status")
    protected CustomTicketStatus customTicketStatus;

    @JsonProperty("ticket_documents")
    protected List<TicketDocumentWrapper> ticketDocumentWrapperList;


    public void customWrapDetails(CustomTicketHistory customTicketHistory, List<TicketDocumentWrapper> ticketDocumentWrapperList, EntityManager entityManager) {
//        this.ticket = customTicketHistory.getTicket();
        this.ticketHistoryId = customTicketHistory.getTicketHistoryId();
        this.assigneeUserId = customTicketHistory.getAssignee();
        this.assigneeRole = customTicketHistory.getAssigneeRole();
        this.modifiedDate = customTicketHistory.getModifiedDate();
        this.targetCompletionDate = customTicketHistory.getTargetCompletionDate();
        this.modifierUserId = customTicketHistory.getModifierId();
        this.modifierRole = customTicketHistory.getModifierRole();
        this.customTicketState = customTicketHistory.getTicketState();
        this.customTicketType = customTicketHistory.getTicketType();
        this.customTicketStatus = customTicketHistory.getTicketStatus();
        this.assignedDate = customTicketHistory.getTicketAssignDate();
        this.ticketDocumentWrapperList = ticketDocumentWrapperList;
        this.comment = customTicketHistory.getComment();

        ServiceProviderEntity modifier = null;
        try {
            if(customTicketHistory.getModifierId() != null) {
                modifier = entityManager.find(ServiceProviderEntity.class, customTicketHistory.getModifierId());
                this.modifierName = modifier.getFirst_name();
                if (modifier.getLast_name() != null) {
                    this.modifierName += " " + modifier.getLast_name();
                }
            } else {
                this.modifierName = "-";
            }
        }
        catch (Exception e)
        {
            ExceptionHandlingService exceptionHandlingService = new ExceptionHandlingService();
            exceptionHandlingService.handleException(e);
            this.modifierName = "-";
        }

    }

    @Override
    public void wrapDetails(CustomServiceProviderTicket customServiceProviderTicket, HttpServletRequest httpServletRequest) {

    }

    @Override
    public void wrapSummary(CustomServiceProviderTicket customServiceProviderTicket, HttpServletRequest httpServletRequest) {

    }


}
