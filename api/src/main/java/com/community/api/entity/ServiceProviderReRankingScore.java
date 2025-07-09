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
@Table(name = "service_provider_re_ranking_score")
public class ServiceProviderReRankingScore {

    @Id
    @Column(name = "service_provider_id")
    private Long serviceProviderId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "service_provider_id")
    private ServiceProviderEntity serviceProvider;

    @Column(name = "review_ticket_status_score")
    private Long reviewTicketStatusScore = 0L;

    @Column(name = "review_ticket_feedback_score")
    private Long reviewTicketFeedbackScore = 0L;

    @Column(name = "time_completion_score")
    private Long timeCompletionScore = 0L;

}
