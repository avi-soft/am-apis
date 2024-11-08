package com.community.api.entity;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.core.order.domain.Order;
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
@Table(name = "custom_service_provider_ticket")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomServiceProviderTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JoinColumn(name = "ticket_id")
    private Long ticketId;

    @ManyToOne
    @JoinColumn(name = "ticket_state_id")
    protected CustomTicketState ticketState;

    @ManyToOne
    @JoinColumn(name = "ticket_status_id")
    protected CustomTicketStatus ticketStatus;

    @ManyToOne
    @JoinColumn(name = "ticket_type_id")
    protected CustomTicketType ticketType;

    @Column(name = "creator_user_id")
    private Long userId;

    @Column(name = "created_date")
    private Date createdDate;

    @ManyToOne
    @JoinColumn(name = "creator_role_id")
    private Role creatorRole;

    @Column(name = "modified_date")
    private Date modifiedDate;

    @Column(name = "modifier_user_id")
    private Long modifierId;

    @ManyToOne
    @JoinColumn(name = "modifier_role_id")
    private Role modifierRole;

    @Column(name = "assignee_user_id")
    private Long assignee;

    @ManyToOne
    @JoinColumn(name = "assignee_role_id")
    private Role assigneeRole;

    @Column(name = "target_completion_time")
    private Date targetCompletionDate;

    @Column(name = "ticket_assign_time")
    private Date ticketAssignDate;

    @OneToOne
    @JoinColumn(name = "ORDER_ID")
    private OrderImpl order;
}