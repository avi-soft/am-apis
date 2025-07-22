package com.community.api.entity;

import com.community.api.utils.Document;
import com.community.api.utils.ServiceProviderDocument;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "document_validity")
public class DocumentValidity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_of_issue", nullable = false)
    private Date date_of_issue;

    @Column(name = "valid_upto", nullable = true)
    private Date valid_upto;

    @Column(name = "is_valid_upto_na", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean is_valid_upto_na = false;

    @OneToOne(mappedBy = "documentValidity")
    @JsonBackReference("document-validity-customer")
    private Document document;

    @OneToOne(mappedBy = "documentValidity")
    @JsonBackReference("document-validity-service-provider")
    private ServiceProviderDocument serviceProviderDocument;
}
