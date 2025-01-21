package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "other_item")
public class OtherItem
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer other_item_id;

    @Column(name = "user_id")
    Long user_id;

    @Column(name = "role_id")
    Integer role_id;

    @Column(name = "typed_text", columnDefinition = "text")
    String typed_text;

    @Column(name = "source_name")
    String source_name;

    @Column(name = "field_name")
    String field_name;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true)
    private CustomProduct customProduct;
}
