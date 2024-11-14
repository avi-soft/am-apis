package com.community.api.entity;

import com.community.api.utils.DocumentType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class FileType
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer file_type_id;

    @Column(name = "file_type_name")
    private String file_type_name;

    public FileType(Integer file_type_id, String file_type_name) {
        this.file_type_id = file_type_id;
        this.file_type_name = file_type_name;
    }

    @JsonBackReference
    @ManyToMany(mappedBy = "required_document_types")
    private List<DocumentType> documentTypes;
}