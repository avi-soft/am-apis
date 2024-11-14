package com.community.api.utils;

import com.community.api.entity.FileType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "document_file_types",
            joinColumns = @JoinColumn(name = "document_type_id"),
            inverseJoinColumns = @JoinColumn(name = "file_type_id")
    )
    @JsonManagedReference
    private List<FileType> required_document_types;

    @Column(name = "max_document_size")
    private String max_document_size;
    @Column(name = "min_document_size")
    private String min_document_size;
//    @Column(name="max_document_size")
//    private Integer max_document_size;
//    @Column(name= "min_document_size")
//    private Integer min_document_size;

    public DocumentType(String documentTypeName, String description) {
        this.document_type_name = documentTypeName;
        this.description = description;
    }
}