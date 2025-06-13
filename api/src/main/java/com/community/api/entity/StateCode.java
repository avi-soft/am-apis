package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "custom_state_codes")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StateCode implements Serializable {
    @Id
    private Integer state_id;
    private String state_name;
    private String state_code;
    @Column(columnDefinition ="BOOLEAN DEFAULT FALSE")
    private Boolean archived;
    @Column(columnDefinition ="BOOLEAN DEFAULT TRUE")
    private Boolean isState;
}
