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
@Table(name = "ticket_state_linkage")
@NoArgsConstructor
@Getter
@Setter
public class TicketStateLinkage {

    @Id
    @Column(name = "ticket_state_linkage_id")
    private Long ticketStateLinkageId;

    @Column(name = "ticket_state_id_from")
    private Long ticketStateIdFrom;

    @Column(name = "ticket_state_id_to")
    private Long ticketStateIdTo;

    @Column(name = "role_id")
    private Integer roleId;

}