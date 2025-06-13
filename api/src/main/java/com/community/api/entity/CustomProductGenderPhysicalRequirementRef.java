package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "custom_product_gender_physical_requirement_reference")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomProductGenderPhysicalRequirementRef  implements Serializable {

    @Id
    @Column(name = "physical_requirement_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long physicalRequirementId;

    @ManyToOne
    @JoinColumn(name = "gender_id")
    protected CustomGender customGender;


    private String genderRunningfield;

    @Column(name = "height")
    Double height;


    @Column(name = "weight")
    Double weight;

    @Column(name = "shoe_size")
    Double shoeSize;

    @Column(name = "waist_size")
    Double waistSize;

    @Column(name = "chest_size")
    Double chestSize;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @JsonIgnore
    @Column(name = "additional_comments")
    @JsonProperty("additional_comments")
    private String additionalComments;
}
