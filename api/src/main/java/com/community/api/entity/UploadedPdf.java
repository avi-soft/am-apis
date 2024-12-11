package com.community.api.entity;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "uploaded_pdf")
public class UploadedPdf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "pdf_data")
    @JsonIgnore
    private byte[] pdf_data;

    @Column(name = "file_name")
    private String file_name;

    @Column(name = "file_type")
    private String file_type;

    @Column(name = "file_path")
    private String file_path;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_provider_id")
    private ServiceProviderEntity serviceProvider;
}
