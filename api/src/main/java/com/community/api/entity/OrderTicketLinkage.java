package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@AllArgsConstructor
@Table(name = "order_ticket_linkage")
@NoArgsConstructor
@Getter
@Setter
public class OrderTicketLinkage {
    @Id
    @Column(name = "linkage_id")
    private Integer orderStateLinkageId;
    @Column(name = "order_state_id")
    private Integer orderStateId;
    @Column(name = "ticket_state_id")
    private Long ticketStateId;
    @Column(name = "ticket_status_id")
    private Long ticketStatusId;
}
