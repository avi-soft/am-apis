package com.community.api.utils;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.DocumentValidity;
import com.community.api.entity.QualificationDetails;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import javax.persistence.*;

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
    private byte[] data;

    @JsonBackReference
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

    @OneToOne(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "qualification_detail_id", referencedColumnName = "qualification_detail_id")
    private QualificationDetails qualificationDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonManagedReference("document-validity-service-provider")
    @JoinColumn(name = "document_validity_id", referencedColumnName = "id")
    private DocumentValidity documentValidity;

    @JsonIgnore
    private String otherDocument;
}
