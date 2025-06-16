package com.community.api.dto;

import com.community.api.entity.FileType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.util.List;
@Getter
@Setter
public class DocumentTypeDto
{
    private Integer document_type_id;
    private String document_type_name;
    private String description;
    private Boolean is_qualification_document;
    private List<Integer> required_document_type_ids;
    private String max_document_size;
    private String min_document_size;
    protected Double min_width_dimension_in_mm;
    protected Double max_width_dimension_in_mm;
    protected Double min_height_dimension_in_mm;
    protected Double max_height_dimension_in_mm;
    private Double dpi;
}
