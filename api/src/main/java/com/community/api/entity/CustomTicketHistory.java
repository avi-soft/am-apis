package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.core.order.domain.OrderImpl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "custom_ticket_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomTicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_history_id")
    protected Long ticketHistoryId;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    @JsonBackReference
    @JsonProperty("ticket")
    protected CustomServiceProviderTicket ticketId;

    @ManyToOne
    @JoinColumn(name = "ticket_state_id")
    @JsonProperty("ticket_state")
    protected CustomTicketState ticketState;

    @ManyToOne
    @JoinColumn(name = "ticket_status_id")
    @JsonProperty("ticket_status")
    protected CustomTicketStatus ticketStatus;

    @ManyToOne
    @JoinColumn(name = "ticket_type_id")
    @JsonProperty("ticket_type")
    protected CustomTicketType ticketType;

    @Column(name = "modified_date")
    @JsonProperty("modified_date")
    private Date modifiedDate;

    @Column(name = "modifier_user_id")
    @JsonProperty("modifier_user_id")
    private Long modifierId;

    @ManyToOne
    @JoinColumn(name = "modifier_role_id")
    @JsonProperty("modifier_role_id")
    private Role modifierRole;

    @Column(name = "assignee_user_id")
    @JsonProperty("assignee_user_id")
    private Long assignee;

    @ManyToOne
    @JoinColumn(name = "assignee_role_id")
    @JsonProperty("assignee_role_id")
    private Role assigneeRole;

    @Column(name = "target_completion_time")
    @JsonProperty("target_completion_time")
    private Date targetCompletionDate;

    @Column(name = "created_date")
    @JsonProperty("created_date")
    private Date createdDate;

    @Column(name = "ticket_assign_time")
    @JsonProperty("ticket_assign_time")
    private Date ticketAssignDate;

    @OneToOne
    @JsonBackReference
    @JoinColumn(name = "ORDER_ID")
    @JsonProperty("order")
    private OrderImpl order;

    @Column
    @JsonProperty("comment")
    private String comment;
}
