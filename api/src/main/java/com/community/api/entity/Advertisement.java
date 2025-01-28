package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.core.catalog.domain.CategoryImpl;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "advertisement")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Advertisement {

    /*  NEED TO REVIEW THIS AS WE COULD ALSO USE THE BROADLEAF SEQUENCE FOR PRIMARY KEY.

    @Id
    @GeneratedValue(generator= "Advertisement")
    @GenericGenerator(
            name="Advertisement",
            strategy="com.community.api.entity.Advertisement",
            parameters = {
                    @Parameter(name="segment_value", value="Advertisement"),
                    @Parameter(name="entity_name", value="com.community.api.entity.Advertisement")
            }
    )*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="advertisement_id")
    @JsonProperty("advertisement_id")
    private Long advertisementId;

    @Column(name = "number", nullable = false, unique = true)
    @JsonProperty("number")
    private String number;

    @NotNull
    @NotEmpty
    @Column(name = "title")
    @JsonProperty("title")
    private String title;

    @NotNull
    @Column(name = "archived")
    @JsonProperty("archived")
    private Character archived = 'N';

    @Column(name = "description",columnDefinition = "text")
    @JsonProperty("description")
    private String description;

    @Column(name = "created_date")
    @JsonProperty("created_date")
    private Date createdDate;

    @Column(name = "creator_user_id")
    @JsonProperty("creator_user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "creator_role_id")
    @JsonProperty("creator_role_id")
    private Role creatorRole;

    @Column(name = "modified_date")
    @JsonProperty("modified_date")
    private Date modifiedDate;

    @Column(name = "modifier_user_id")
    @JsonProperty("modifier_user_id")
    private Long modifierId;

    @ManyToOne
    @JoinColumn(name = "modifier_role_id")
    @JsonProperty("modifier_role_id")
    private Role modifierRole;

    @Column(name = "notification_start_date")
    @JsonProperty("notification_start_date")
    private Date notificationStartDate;

    @Column(name = "active_end_date")
    @JsonProperty("active_end_date")
    private Date notificationEndDate;

    @NotNull
    @NotEmpty
    @Column(name = "url", unique = true)
    @JsonProperty("url")
    private String url;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonProperty("category_id")
    private CategoryImpl category;

    @Column(name = "notifying_authority")
    @JsonProperty("notifying_authority")
    private String notifyingAuthority;

}