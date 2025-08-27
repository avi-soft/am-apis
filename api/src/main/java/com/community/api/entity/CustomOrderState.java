package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "ORDER_STATE") // extended version of order.
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomOrderState {

    @Column(name = "order_state_id")
    private Integer orderStateId;

    @Column(name = "order_status_id")
    private Integer orderStatusId;

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "modifier_user_id")
    private Long modifierUserId;

    @ManyToOne
    @JoinColumn(name = "modifier_role_id")
    private Role modifierRole;

    @Column(name = "modified_date")
    private Date modifiedDate;

    @Column(name = "refund_amount")
    private Double refundAmount;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "last_transition_state")
    private String lastState;

    public CustomOrderState(Integer orderStateId) {
        this.orderStateId=orderStateId;
    }
}
