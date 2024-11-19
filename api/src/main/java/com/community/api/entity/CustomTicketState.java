package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "custom_ticket_state")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomTicketState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_state_id")
    @JsonProperty("ticket_state_id")
    protected Long ticketStateId;

    @Column(name = "ticket_state")
    @JsonProperty("ticket_state")
    protected String ticketState;

    @Column(name = "ticket_state_description")
    @JsonProperty("ticket_state_description")
    protected String ticketStateDescription;

}