package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "service_provider_rank")
public class ServiceProviderRank
{

    @Id
    @Column(name = "rank_id")
    private Long rank_id;

    @Column(name = "rank_name", columnDefinition = "TEXT")
    private String  rank_name;

    @Column(name = "rank_description", columnDefinition = "TEXT")
    private String rank_description;

    private String created_at,updated_at,created_by;

    @NotNull
    @Column(name="maximum_ticket_size")
    private Integer maximumTicketSize;

    @NotNull
    @Column(name="maximum_binding_size")
    private Integer maximumBindingSize;

}

