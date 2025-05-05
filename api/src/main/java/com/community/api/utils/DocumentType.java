package com.community.api.utils;

import com.community.api.entity.FileType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.Column;
import java.util.List;

@Entity
@Table(name = "custom_document")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DocumentType {
    @Id
    @Column(name = "document_type_id")
    private Integer document_type_id;
    @Column(name = "document_type_name")
    private String document_type_name;
    @Column(name = "description")
    private String description;
    @Column(name = "is_qualification_document",nullable = false,columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean is_qualification_document;

    @Column(name = "is_issue_date_required",nullable = false,columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean is_issue_date_required;

    @Column(name = "is_expiration_date_required",nullable = false,columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean is_expiration_date_required;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "document_file_types",
            joinColumns = @JoinColumn(name = "document_type_id"),
            inverseJoinColumns = @JoinColumn(name = "file_type_id")
    )
    private List<FileType> required_document_types;

    @Column(name = "max_document_size")
    private String max_document_size;
    @Column(name = "min_document_size")
    private String min_document_size;

    @Column(name = "sort_order", nullable = false)  // New column for sorting order
    private Integer sort_order = Integer.MAX_VALUE;

    public DocumentType(Integer document_type_id, String document_type_name, String description, String max_document_size, String min_document_size, Boolean is_qualification_document, Boolean is_issue_date_required, Boolean is_expiration_date_required) {
        this.document_type_id = document_type_id;
        this.document_type_name = document_type_name;
        this.description = description;
        this.max_document_size = max_document_size;
        this.min_document_size = min_document_size;
        this.is_qualification_document=is_qualification_document;
        this.is_issue_date_required = is_issue_date_required;
        this.is_expiration_date_required=is_expiration_date_required;
    }
    public DocumentType(String documentTypeName, String description) {
        this.document_type_name = documentTypeName;
        this.description = description;
    }
}