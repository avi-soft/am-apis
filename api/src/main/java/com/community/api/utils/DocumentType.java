package com.community.api.utils;

import com.community.api.entity.FileType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @Column(name = "is_qualification_document")
    private Boolean is_qualification_document;

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

    public DocumentType(Integer document_type_id, String document_type_name, String description, String max_document_size, String min_document_size) {
        this.document_type_id = document_type_id;
        this.document_type_name = document_type_name;
        this.description = description;
        this.max_document_size = max_document_size;
        this.min_document_size = min_document_size;
    }
    public DocumentType(String documentTypeName, String description) {
        this.document_type_name = documentTypeName;
        this.description = description;
    }
}