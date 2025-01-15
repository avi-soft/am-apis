package com.community.api.dto;

import com.community.api.entity.Advertisement;
import com.community.api.entity.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.rest.api.wrapper.APIWrapper;
import org.broadleafcommerce.common.rest.api.wrapper.BaseWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class AdvertisementProductWrapper extends BaseWrapper implements APIWrapper<Advertisement> {

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

    @JsonProperty("notification_start_date")
    private Date notificationStartDate;

    @JsonProperty("notification_end_date")
    private Date notificationEndDate;

    @JsonProperty("url")
    private String url;

    @JsonProperty("products")
    private List<CustomAdvertisementProductWrapper> productWrapperList = null;

    @Override
    public void wrapDetails(Advertisement advertisement, HttpServletRequest httpServletRequest) {
        this.advertisementId = advertisement.getAdvertisementId();
        this.title = advertisement.getTitle();
        this.number = advertisement.getNumber();
        this.description = advertisement.getDescription();
        this.url = advertisement.getUrl();
        this.createdDate = advertisement.getCreatedDate();
        this.notificationStartDate = advertisement.getNotificationStartDate();
        this.notificationEndDate = advertisement.getNotificationEndDate();
        this.notifyingAuthority = advertisement.getNotifyingAuthority();
        this.userId = advertisement.getUserId();
        this.creatorRole = advertisement.getCreatorRole();
        this.modifierId = advertisement.getModifierId();
        this.modifierRole = advertisement.getModifierRole();
        this.modifiedDate = advertisement.getModifiedDate();
    }

    public void wrapDetails(Advertisement advertisement, List<CustomAdvertisementProductWrapper> wrapper, HttpServletRequest httpServletRequest) {
        this.advertisementId = advertisement.getAdvertisementId();
        this.title = advertisement.getTitle();
        this.number = advertisement.getNumber();
        this.description = advertisement.getDescription();
        this.url = advertisement.getUrl();
        this.createdDate = advertisement.getCreatedDate();
        this.notificationStartDate = advertisement.getNotificationStartDate();
        this.notificationEndDate = advertisement.getNotificationEndDate();
        this.notifyingAuthority = advertisement.getNotifyingAuthority();
        this.userId = advertisement.getUserId();
        this.creatorRole = advertisement.getCreatorRole();
        this.modifierId = advertisement.getModifierId();
        this.modifierRole = advertisement.getModifierRole();
        this.modifiedDate = advertisement.getModifiedDate();
        this.productWrapperList = wrapper;
    }

    @Override
    public void wrapSummary(Advertisement advertisement, HttpServletRequest httpServletRequest) {

    }
}
