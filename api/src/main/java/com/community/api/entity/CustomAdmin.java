package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Entity;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name ="custom_admin")
public class CustomAdmin
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long admin_id;

    @Min(0)
    private int role;
    private String password;
    private String user_name;
    private String otp;
    @Size(min = 9, max = 13)
    private String mobileNumber;
    private String country_code;
    @Column(columnDefinition = "TEXT")
    private String token;
    private LocalDateTime otpExpirationTime;
    private int signedUp=0;
    private Date created_at,updated_at;
    private String created_by, modified_by;


    public CustomAdmin(Long admin_id, int role, String password,String user_name, String mobileNumber,String country_code,int signedUp, Date created_at, String created_by) {
        this.admin_id = admin_id;
        this.role = role;
        this.password = password;
        this.user_name=user_name;
        this.mobileNumber = mobileNumber;
        this.country_code = country_code;
        this.signedUp = signedUp;
        this.created_at = created_at;
        this.created_by = created_by;
    }
}
