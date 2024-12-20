package com.community.api.dto;

import com.community.api.entity.Advertisement;
import com.community.api.entity.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.rest.api.wrapper.APIWrapper;
import org.broadleafcommerce.common.rest.api.wrapper.BaseWrapper;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryImpl;
import org.broadleafcommerce.core.catalog.domain.Product;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
public class AdvertisementWrapper extends BaseWrapper implements APIWrapper<Advertisement> {

    @JsonProperty("advertisement_id")
    private Long advertisementId;

    @JsonProperty("number")
    private String number;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("notifying_authority")
    private String notifyingAuthority;

    @JsonProperty("created_date")
    private Date createdDate;

    @JsonProperty("creator_user_id")
    private Long userId;

    @JsonProperty("creator_role_id")
    private Role creatorRole;

    @JsonProperty("modified_date")
    private Date modifiedDate;

    @JsonProperty("modifier_user_id")
    private Long modifierId;

    @JsonProperty("modifier_role_id")
    private Role modifierRole;

    @JsonProperty("active_start_date")
    private Date activeStartDate;

    @JsonProperty("active_end_date")
    private Date activeEndDate;

    @JsonProperty("url")
    private String url;

    @JsonProperty("category")
    private CustomCategoryWrapper category;


    @Override
    public void wrapDetails(Advertisement advertisement, HttpServletRequest httpServletRequest) {
        this.advertisementId = advertisement.getAdvertisementId();
        this.title = advertisement.getTitle();
        this.number = advertisement.getNumber();
        this.description = advertisement.getDescription();
        this.url = advertisement.getUrl();
        this.createdDate = advertisement.getCreatedDate();
        this.activeStartDate = advertisement.getActiveStartDate();
        this.activeEndDate = advertisement.getActiveEndDate();
        this.notifyingAuthority = advertisement.getNotifyingAuthority();
        this.userId = advertisement.getUserId();
        this.creatorRole = advertisement.getCreatorRole();
        this.modifierId = advertisement.getModifierId();
        this.modifierRole = advertisement.getModifierRole();
        this.modifiedDate = advertisement.getModifiedDate();
        CustomCategoryWrapper categoryWrapper = new CustomCategoryWrapper();
        categoryWrapper.wrapSummary((Category) advertisement.getCategory(), null);
        this.category = categoryWrapper;
    }

    @Override
    public void wrapSummary(Advertisement advertisement, HttpServletRequest httpServletRequest) {

    }
}
