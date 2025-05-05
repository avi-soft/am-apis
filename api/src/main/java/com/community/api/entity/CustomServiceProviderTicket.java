package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.core.order.domain.OrderImpl;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "custom_service_provider_ticket")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomServiceProviderTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    @JsonProperty("ticket_id")
    private Long ticketId;

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

    @Column(name = "creator_user_id")
    @JsonProperty("creator_user_id")
    private Long userId;

    @Column(name = "created_date")
    @JsonProperty("creator_date")
    private Date createdDate;

    @ManyToOne
    @JoinColumn(name = "creator_role_id")
    @JsonProperty("creator_role_id")
    private Role creatorRole;

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

    @Column(name = "ticket_assign_time")
    @JsonProperty("ticket_assign_time")
    private Date ticketAssignDate;

    @OneToOne
    @JsonBackReference
    @JoinColumn(name = "ORDER_ID")
    @JsonProperty("order")
    private OrderImpl order;

    @Column(name = "comment")
    @JsonProperty("comment")
    private String comment;

    @Column(name = "task_desc")
    @JsonProperty("task_desc")
    private String desc;

    @ElementCollection
    @CollectionTable(name = "ticket_rejected_by",
            joinColumns = @JoinColumn(name = "ticket_id"))
    @Column(name = "rejected_by_id")
    private List<Long>rejectedBy = new ArrayList<>();
}