package com.community.api.entity;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "service_provider_re_ranking_eligibility")
public class ServiceProviderReRankingEligibility {

    @Id
    @Column(name = "service_provider_id")
    private Long serviceProviderId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "service_provider_id")
    private ServiceProviderEntity serviceProvider;

    @Column(name = "is_admin_overridden", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean adminOverridden = false;

    @Column(name = "is_eligible_for_re_ranking")
    private Boolean eligibleForReRanking;

}
