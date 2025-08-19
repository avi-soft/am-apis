package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name="email_queue")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailQueue {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @Column(name="user_id")
    protected Long userId;

    @ManyToOne
    @JoinColumn(name="role_id")
    protected Role role;

    @Column(name="created_date")
    protected Date createdDate;

    @Column(name = "archived", columnDefinition = "BOOLEAN DEFAULT FALSE")
    protected Boolean archived = false;

    @ManyToOne
    @JoinColumn(name="ticket_id")
    protected CustomServiceProviderTicket ticket;

}
