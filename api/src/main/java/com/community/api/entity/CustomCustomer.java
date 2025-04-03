
package com.community.api.entity;

import com.community.api.utils.Document;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.micrometer.core.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.broadleafcommerce.profile.core.domain.CustomerImpl;

import javax.persistence.*;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "CUSTOM_CUSTOMER")
@Inheritance(strategy = InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomCustomer extends CustomerImpl {

    @Nullable
    @Column(name = "country_code")
    private String countryCode;

    @Nullable
    @Column(name = "mobile_number", unique = true)
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be a valid 10-digit number.")
    private String mobileNumber;

    @Nullable
    @Column(name = "otp", unique = false)
    private String otp;

    @Nullable
    @Column(name = "pan_number")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN number must be a valid 10-character alphanumeric string.")
    private String panNumber;

    @Nullable
    @Column(name = "father_name")
    @Pattern(regexp = "^[a-zA-Z]+( [a-zA-Z]+)*$", message = "Father's name must contain only alphabets")
    private String fathersName;

    @Nullable
    @Column(name = "nationality")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Nationality must only contain alphabetic characters.")
    private String nationality;

    @Nullable
    @Column(name = "date_of_birth")
//    @Temporal(TemporalType.DATE)
//    @Pattern(regexp = "^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-([12][0-9]{3})$", message = "Date of Birth must be in DD-MM-YYYY format.")
    private String dob;

    @Nullable
    @Column(name = "gender")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female, or Other.")
    private String gender;

    @Nullable
    @Column(name = "adhar_number", unique = true)
    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhar number must be a valid 12-digit numeric value.")
    private String adharNumber;

    @Nullable
    @Column(name = "category")
    private String category; //@TODO -make it int for using in cart

    @Column(name = "hide_phone_number")
    private Boolean hidePhoneNumber = false;

    @Nullable
    @Column(name = "category_issue_date")
    private String categoryIssueDate;
    @Nullable
    @Column(name = "height_cms")
    private Double heightCms; // Integer type for numeric validation

    @Nullable
    @Column(name = "weight_kgs")
    private Double weightKgs; // Integer type for numeric validation

    @Nullable
    @Column(name = "chest_size_cms")
    private Double chestSizeCms; // Integer type for numeric validation

    @Nullable
    @Column(name = "shoe_size_inches")
    private Double shoeSizeInches; // Integer type for numeric validation

    @Nullable
    @Column(name = "waist_size_cms")
    private Double waistSizeCms; // Integer type for numeric validation
    @Nullable
    @Column(name = "can_swim")
    private Boolean canSwim; // Yes/No
    @Nullable
    @Column(name = "proficiency_in_sports_national_level")
    private Boolean proficiencyInSportsNationalLevel; // Yes/No
    @Nullable
    @Column(name = "first_choice_exam_city")
    private String firstChoiceExamCity;

    @Column(name = "second_choice_exam_city")
    private String secondChoiceExamCity;

    @Column(name = "third_choice_exam_city")
    private String thirdChoiceExamCity;

    @Column(name = "mphil_passed")
    private Boolean mphilPassed;

    @Column(name = "phd_passed")
    private Boolean phdPassed;

    @Column(name = "number_of_attempts")
    private Integer numberOfAttempts;

    @Column(name = "interested_in_defence",columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean interestedInDefence;

    @Nullable
    @Column(name = "work_experience")
    private Integer workExperience; // work experience in months.

    @Nullable
    @Column(name = "category_valid_upto_date")
    private String categoryValidUpto;

    @Pattern(regexp = "^[a-zA-Z]+( [a-zA-Z]+)*$", message = "Mother's name must contain only alphabets")
    @Column(name = "mother_name")
    private String mothersName;

    @Column(name = "religion")
    private String religion;

    @Column(name = "belongs_to_minority")
    private Boolean belongsToMinority = false;

    @Nullable
    @Column(name = "sub_category")
    private String subcategory;

    @Nullable
    @Column(name = "domicile")
    private Boolean domicile = false;

    @Column(name = "domicile_issue_date")
    private java.sql.Date domicileIssueDate;

    @Column(name = "domicile_valid_upto")
    private java.sql.Date domicileValidUpto;

    @Nullable
    @Pattern(regexp = "^[0-9]{10}$|^$", message = "Secondary number must be a valid 10-digit number.")
    @Column(name = "secondary_mobile_number")
    private String secondaryMobileNumber;

    @Nullable
    @Column(name = "whatsapp_number")
    @Pattern(regexp = "^[0-9]{10}$", message = "WhatsApp number must be a valid 10-digit number.")
    private String whatsappNumber;

    @Nullable
    @Column(name = "secondary_email")
    @Pattern(regexp = "^(|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$", message = "Secondary email must be in a valid email format.")
    private String secondaryEmail;

    @Nullable
    @Column(name = "residential_address")
    private String residentialAddress;

    @Nullable
    @Column(name = "state")
    private String state;

    @Nullable
    @Column(name = "district")
    private String district;


    @Nullable
    @Column(name = "city")
    private String city;

    @Nullable
    @Column(name = "pincode")
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be a 6-digit numeric value.")
    private String pincode;

    @Nullable
    @ManyToMany
    @JoinTable(
            name = "customer_saved_forms",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private List<CustomProduct> savedForms;

    @Nullable
    @JsonManagedReference("qualificationDetailsList-customer")
    @OneToMany(mappedBy = "custom_customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QualificationDetails> qualificationDetailsList = new ArrayList<>();

    @Nullable
    @JsonManagedReference("documents-customer")
    @OneToMany(mappedBy = "custom_customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Document> documents;

    @Nullable
    @ManyToMany
    @JoinTable(
            name = "cart_recovery_log", // The name of the join table
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private List<CustomProduct> cartRecoveryLog;

    @Nullable
    @Column(length = 512)
    private String token;

    @Column(name = "disability_handicapped")
    private Boolean disability = false;

    @Column(name = "disability_type")
    private String disabilityType;

    @Column(name = "percentage_of_disability")
    private Double disabilityPercentage = 0.0;

    @Column(name = "is_ex_service_man")
    private Boolean exService = false;

    @Column(name = "is_ncc_certificate",columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isNccCertificate;

    @Column(name = "is_nss_certificate", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isNssCertificate;

    @Column(name = "ncc_certificate")
    private String nccCertificate;

    @Column(name = "nss_certificate")
    private String nssCertificate;

    @Column(name = "is_sports_certificate",columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isSportsCertificate;

    @Column(name = "is_other_or_state_category")
    private Boolean isOtherOrStateCategory;

    @Column(name = "other_or_state_category", columnDefinition = "text")
    private String otherOrStateCategory;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @Column(name = "other_category_date_of_issue", columnDefinition = "DATE")
    private java.sql.Date otherCategoryDateOfIssue;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @Column(name = "other_category_valid_upto", columnDefinition = "DATE")
    private java.sql.Date  otherCategoryValidUpto;

    @Column(name = "is_married")
    private Boolean isMarried = false;

    @Column(name = "visible_identification_mark_1")
    private String identificationMark1;

    @Column(name = "visible_identification_mark_2")
    private String identificationMark2;

    @JsonBackReference("referrer-customer")
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerReferrer> myReferrer = new ArrayList<>();



    @Column(name = "order_count",columnDefinition = "BIGINT DEFAULT 0")
    private Integer numberOfOrders;

    @Column(name = "registered_by_sp", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean registeredBySp = false;

    @Column(name = "profile_completed", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean profileComplete = false;

    @Column(name = "created_by_role", columnDefinition = "INTEGER DEFAULT 5")
    private Integer createdByRole;
    @Column(name = "created_by_id", columnDefinition = "BIGINT DEFAULT 0")
    private Long createdById;
    @Column(name = "modified_by_role", columnDefinition = "INTEGER DEFAULT 5")
    private Integer modifiedByRole;
    @Column(name = "modified_by_id", columnDefinition = "BIGINT DEFAULT 0")
    private Long modifiedById;

    public List<CustomerReferrer> getMyReferrer() {
        // Get the list of referrers
        List<CustomerReferrer> referrers = this.myReferrer;

        // Sort the referrers based on their bandwidth
        referrers.sort((r1, r2) -> {
            // Get the max ticket size from rank if max_ticket_size is not available
            Integer maxTicketSize1 = r1.getServiceProvider().getMaximumTicketSize() != null ? r1.getServiceProvider().getMaximumTicketSize() : r1.getServiceProvider().getRanking().getMaximumTicketSize();
            Integer maxTicketSize2 = r2.getServiceProvider().getMaximumTicketSize() != null ? r2.getServiceProvider().getMaximumTicketSize() : r2.getServiceProvider().getRanking().getMaximumTicketSize();

            // Avoid division by zero by ensuring maxTicketSize is not 0
            if (maxTicketSize1 == 0) maxTicketSize1 = 1;
            if (maxTicketSize2 == 0) maxTicketSize2 = 1;

            // Calculate bandwidth for both referrers
            double bandwidth1 = (double) (r1.getServiceProvider().getTicketAssigned() + r1.getServiceProvider().getTicketPending()) / maxTicketSize1 * 100;
            double bandwidth2 = (double) (r2.getServiceProvider().getTicketAssigned() + r2.getServiceProvider().getTicketPending()) / maxTicketSize2 * 100;

            // Sort by bandwidth (descending order)
            return Double.compare(bandwidth2, bandwidth1); // for descending order
        });

        // Return the sorted list
        Collections.reverse(referrers);
        return referrers;
    }

    @ManyToOne
    @JoinColumn(name = "work_experience_scope_id")
    protected CustomApplicationScope workExperienceScopeId;

    @ManyToOne
    @JoinColumn(name = "sport_certificate_id")
    protected CustomApplicationScope sportCertificateId;

    @ManyToOne
    @JoinColumn(name = "domicile_state")
    protected StateCode domicileState;

    @Column(name = "archived",columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean archived =false;
    @Column(name = "archived_by_role_id",columnDefinition = "BIGINT DEFAULT 0")
    private Integer archivedByRole;
    @Column(name = "archived_by_id",columnDefinition = "BIGINT DEFAULT 0")
    private Long archivedById;
    @Column(name = "is_live_photo_na",columnDefinition ="BOOLEAN DEFAULT FALSE")
    private Boolean isLivePhotoNa=false;
    @Column(name = "primary_referrer_id",columnDefinition = "BIGINT DEFAULT 0")
    private Long primaryRef=0L;
    @Column(name = "email_active",columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean emailActive=false;
}