package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "custom_stream")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomStream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stream_id")
    protected Long streamId;

    @NotNull
    @Column(name = "archived")
    protected Character archived = 'N';

    @NotNull
    @Column(name = "stream_name")
    protected String streamName;

    @Column(name = "stream_description")
    protected String streamDescription;

    @Column(name = "created_at")
    protected Date createdDate;

    @Column(name = "created_by")
    protected Long creatorUserId;

    @Column(name = "creator_role")
    protected Role creatorRole;

}
