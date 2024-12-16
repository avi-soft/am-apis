package com.community.api.entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "institution")
public class Institution
{
    @Id
    private Long institution_id;

    @Column(name="institution_name",nullable = false)
    private String institution_name;

    @Column(name = "institution_location", nullable = false)
    private String institution_address;

    @Column(name = "institution_code",nullable = false)
    private String institution_code;

    @Column(name = "created_date", updatable = false)
    private String created_date;

    @Column(name = "modified_date")
    private String modified_date;

    @Column(name = "created_by")
    private String created_by;

    @Column(name = "modified_by")
    private String modified_by;
}
