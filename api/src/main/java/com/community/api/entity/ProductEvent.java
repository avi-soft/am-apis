package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ProductEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
}
