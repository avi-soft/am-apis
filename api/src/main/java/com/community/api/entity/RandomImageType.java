package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micrometer.core.lang.Nullable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "random_image_type")
@Getter
@Setter
public class RandomImageType
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "random_image_type_id")
    private Integer randomImageTypeId;

    @Column(name = "random_image_type_name")
    private  String randomImageTypeName;

    @JsonIgnore
    @OneToMany(mappedBy = "randomImageType")
    private List<Image> images;
}
