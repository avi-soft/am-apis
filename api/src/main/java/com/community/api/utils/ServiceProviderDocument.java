package com.community.api.utils;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketHistory;
import com.community.api.entity.DocumentValidity;
import com.community.api.entity.QualificationDetails;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "service_provider_documents")
public class ServiceProviderDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    private String name;
    private String filePath;

    @Lob
    @JsonIgnore
    private byte[] data;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "service_provider_id")
    private ServiceProviderEntity serviceProviderEntity;

    @ManyToOne
    @JoinColumn(name = "document_type_Id")
    private DocumentType documentType;

    @Column(name = "archived",columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isArchived;

    @Column(name = "is_qualification_document",columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean is_qualification_document=false;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "qualification_detail_id", referencedColumnName = "qualification_detail_id")
    @JsonIgnore
    private QualificationDetails qualificationDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonManagedReference("document-validity-service-provider")
    @JoinColumn(name = "document_validity_id", referencedColumnName = "id")
    private DocumentValidity documentValidity;

    @JsonIgnore
    private String otherDocument;

    @Column(name = "uploaded_date")
    @JsonProperty("uploaded_date")
    protected Date uploadedDate;

    @Column(name = "modified_date")
    @JsonProperty("modified_date")
    protected Date modifiedDate;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    @JsonIgnore
    private CustomServiceProviderTicket serviceProviderTicket;

    @ManyToOne
    @JoinColumn(name = "ticket_history_id")
    @JsonIgnore
    private CustomTicketHistory ticketHistory;

}
