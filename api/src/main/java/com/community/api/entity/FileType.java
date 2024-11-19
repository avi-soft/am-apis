package com.community.api.entity;

import com.community.api.utils.DocumentType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Column;
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

    @ManyToMany(mappedBy = "required_document_types")
    @JsonIgnore
    private List<DocumentType> documentTypes;
}