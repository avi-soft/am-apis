package com.community.api.entity;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micrometer.core.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ehcache.spi.service.ServiceProvider;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.File;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "communication_content")
public class CommunicationContent {
    @Id
    @Column(name = "content_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "service_provider_id", referencedColumnName = "service_provider_id", nullable = true)
    private ServiceProviderEntity serviceProvider;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "admin_id", referencedColumnName = "admin_id", nullable = true)
    private CustomAdmin admin;

    @Column(name = "content_text", columnDefinition = "text", nullable = true)
    private String contentText;

    @Column(name = "subject", columnDefinition = "text", nullable = true)
    private String subject;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private List<ContentFile> contentFiles;

}
