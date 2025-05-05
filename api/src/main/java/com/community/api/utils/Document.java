package com.community.api.utils;
import com.community.api.entity.CustomCustomer;
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
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    private String name;
    private String filePath;

    @Lob
    private byte[] data;

    @Column(name = "is_qualification_document",columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean is_qualification_document=false;

    @JsonBackReference("documents-customer")
    @ManyToOne
    @JoinColumn(name = "custom_customer_id")
    private CustomCustomer custom_customer;

    @ManyToOne
    @JoinColumn(name = "document_type_Id")
    private DocumentType documentType;

    @OneToOne
    @JoinColumn(name = "qualification_detail_id", referencedColumnName = "qualification_detail_id")
    private QualificationDetails qualificationDetails;

    @Column(name = "archived",columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isArchived;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonManagedReference("document-validity-customer")
    @JoinColumn(name = "document_validity_id", referencedColumnName = "id")
    private DocumentValidity documentValidity;

    @JsonIgnore
    private String otherDocument;

}