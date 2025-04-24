package com.community.api.dto;


import lombok.*;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Category;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CategoryDto {

    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private String longDescription;
    private String displayTemplate;
    private Date activeStartDate;
    private Date activeEndDate;
    private Character archived;

    public CategoryDto(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.active = category.isActive();
        this.longDescription = category.getLongDescription();
        this.displayTemplate = category.getDisplayTemplate();
        this.activeStartDate = category.getActiveStartDate();
        this.activeEndDate = category.getActiveEndDate();
        this.archived = ((Status) category).getArchived();
    }
}
