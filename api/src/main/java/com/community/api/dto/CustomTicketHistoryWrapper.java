package com.community.api.dto;

import com.community.api.entity.CombinedOrderDTO;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketHistory;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketType;
import com.community.api.entity.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.broadleafcommerce.common.rest.api.wrapper.APIWrapper;
import org.broadleafcommerce.common.rest.api.wrapper.BaseWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class CustomTicketHistoryWrapper extends BaseWrapper implements APIWrapper<CustomServiceProviderTicket> {

    @JsonProperty("ticket_history_id")
    protected Long ticketHistoryId;

    @JsonProperty("ticket_id")
    protected Long ticketId;

    @JsonProperty("created_date")
    protected Date createdDate;

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


    public void customWrapDetails(CustomTicketHistory customTicketHistory) {
        this.ticketId = customTicketHistory.getTicketId().getTicketId();
        this.ticketHistoryId = customTicketHistory.getTicketHistoryId();
        this.assigneeUserId = customTicketHistory.getAssignee();
        this.assigneeRole = customTicketHistory.getAssigneeRole();
        this.createdDate = customTicketHistory.getCreatedDate();
        this.modifiedDate = customTicketHistory.getModifiedDate();
        this.targetCompletionDate = customTicketHistory.getTargetCompletionDate();
        this.modifierUserId = customTicketHistory.getModifierId();
        this.modifierRole = customTicketHistory.getModifierRole();
        this.customTicketState = customTicketHistory.getTicketState();
        this.customTicketType = customTicketHistory.getTicketType();
        this.customTicketStatus = customTicketHistory.getTicketStatus();
        this.assignedDate = customTicketHistory.getTicketAssignDate();
        this.createdDate = customTicketHistory.getCreatedDate();
    }

    @Override
    public void wrapDetails(CustomServiceProviderTicket customServiceProviderTicket, HttpServletRequest httpServletRequest) {

    }

    @Override
    public void wrapSummary(CustomServiceProviderTicket customServiceProviderTicket, HttpServletRequest httpServletRequest) {

    }


}
