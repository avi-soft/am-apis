package com.community.api.entity;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "action_log")
public class ActionLog {
    @Id
    @Column(name = "action_log_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long actionLogId;

    @ManyToOne
    @JoinColumn(name = "service_provider_id", referencedColumnName = "service_provider_id", nullable = false)
    private ServiceProviderEntity serviceProvider;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", nullable = false)
    private CustomCustomer customCustomer;

    @ManyToMany
    @JoinTable(
            name = "action_log_modes",
            joinColumns = @JoinColumn(name = "action_log_id"),
            inverseJoinColumns = @JoinColumn(name = "mode_id")
    )
    private List<CustomMode> customModes;

    @ManyToOne
    @JoinColumn(name = "content_id", referencedColumnName = "content_id", nullable = false)
    private CommunicationContent content;

    @Column(name = "delivery_status")
    private String deliveryStatus;

}
