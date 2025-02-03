package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.PostDetailsDTO;
import com.community.api.dto.ReferrerDTO;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.BoardUniversity;
import com.community.api.entity.CustomAdmin;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomStream;
import com.community.api.entity.CustomSubject;
import com.community.api.entity.CustomerAddressDTO;
import com.community.api.entity.CustomerReferrer;
import com.community.api.entity.Institution;
import com.community.api.entity.OtherItem;
import com.community.api.entity.Post;
import com.community.api.entity.Qualification;
import com.community.api.entity.QualificationDetails;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.utils.Document;
import com.community.api.utils.ServiceProviderDocument;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SharedUtilityService {
    public ReserveCategoryService reserveCategoryService;
    @Autowired
    public OrderService orderService;
    @Autowired
    public ExceptionHandlingImplement exceptionHandling;
    @Autowired
    FileService fileService;
    @Autowired
    JwtUtil jwtTokenUtil;
    @Autowired
    RoleService roleService;
    @Autowired
    DistrictService districtService;
    @Autowired
    HttpServletRequest request;
    private EntityManager entityManager;
    private ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;

    public static String getCurrentTimestamp() {
        // Get the current date and time with timezone
        ZonedDateTime zonedDateTime = ZonedDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSXXX");
        return zonedDateTime.format(formatter);
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setReserveCategoryService(ReserveCategoryService reserveCategoryService) {
        this.reserveCategoryService = reserveCategoryService;
    }

    @Autowired
    public void setProductReserveCategoryFeePostRefService(ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService) {
        this.productReserveCategoryFeePostRefService = productReserveCategoryFeePostRefService;
    }

    public long findCount(String queryString) {
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult();
    }

    public Map<String, Object> createProductResponseMap(Product product, OrderItem orderItem, CustomCustomer customer,Long genderId) {
        Map<String, Object> productDetails = new HashMap<>();
        CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());
        if (orderItem != null)
            productDetails.put("order_item_id", orderItem.getId());
        List<PostDetailsDTO>postPreferenceOrder=new ArrayList<>();
        productDetails.put("product_id", product.getId());
        productDetails.put("url", product.getUrl());
        productDetails.put("meta_title", product.getName());
        productDetails.put("url_key", product.getUrlKey());
        productDetails.put("platform_fee", customProduct.getPlatformFee());
        productDetails.put("display_template", product.getDisplayTemplate());
        productDetails.put("default_sku_id", product.getDefaultSku().getId());
        productDetails.put("default_sku_name", product.getDefaultSku().getName());
        productDetails.put("sku_description", product.getDefaultSku().getDescription());
        productDetails.put("long_description", product.getDefaultSku().getLongDescription());
        productDetails.put("active_start_date", product.getDefaultSku().getActiveStartDate());
        List<Long>preferenceOrder=null;
        List<PostDetailsDTO>availablePosts=new ArrayList<>();
        if(customProduct.getPosts().size()>=1) {
            String retrievedPostPreferenceString = (String) (orderItem.getOrderItemAttributes().get("postPreference").getValue());
            if (retrievedPostPreferenceString != null) {
                if (retrievedPostPreferenceString != null && !retrievedPostPreferenceString.isEmpty()) {
                    preferenceOrder = Arrays.stream(retrievedPostPreferenceString.split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toList());
                }
                for (Long id : preferenceOrder) {
                    Post post = entityManager.find(Post.class, id);
                    if (post != null) {
                        PostDetailsDTO detailsDTO = new PostDetailsDTO();
                        detailsDTO.setPostId(post.getPostId());
                        detailsDTO.setPostName(post.getPostName());
                        detailsDTO.setPostCode(post.getPostCode());
                        postPreferenceOrder.add(detailsDTO);
                    }
                }
                for (Post post : customProduct.getPosts()) {
                    if (!preferenceOrder.contains(post.getPostId())) {
                        PostDetailsDTO detailsDTO = new PostDetailsDTO();
                        detailsDTO.setPostId(post.getPostId());
                        detailsDTO.setPostName(post.getPostName());
                        detailsDTO.setPostCode(post.getPostCode());
                        availablePosts.add(detailsDTO);
                    }
                }
            }
        }
        productDetails.put("available_posts",availablePosts);
        productDetails.put("preference_order",postPreferenceOrder);
        Double fee = reserveCategoryService.getReserveCategoryFee(product.getId(), reserveCategoryService.getCategoryByName(customer.getCategory()).getReserveCategoryId(),genderId);
        if (fee == null) {
            fee =  reserveCategoryService.getReserveCategoryFee(product.getId(), 1L,genderId);
            if(fee==null)
                fee=0.0;
        }
        //@TODO-Fee is dependent on category
        productDetails.put("fee", fee);//this is dummy data
        productDetails.put("category_id", product.getDefaultCategory().getId());
        productDetails.put("active_end_date", product.getDefaultSku().getActiveEndDate());
        return productDetails;
    }

    @Transactional
    public Map<String, Object> breakReferenceForCustomer(Customer customer,String authHeader) throws Exception {
        String jwtToken = authHeader.substring(7);
        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
        Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
        String role = roleService.getRoleByRoleId(roleId).getRole_name();
        Map<String, Object> customerDetails = new HashMap<>();
        customerDetails.put("id", customer.getId());
        customerDetails.put("date_created", customer.getAuditable().getDateCreated());
        customerDetails.put("created_by", customer.getAuditable().getCreatedBy());
        customerDetails.put("date_updated", customer.getAuditable().getDateUpdated());
        customerDetails.put("updated_by", customer.getAuditable().getUpdatedBy());
        customerDetails.put("username", customer.getUsername());
        customerDetails.put("password", customer.getPassword());
        customerDetails.put("email_address", customer.getEmailAddress());
        customerDetails.put("first_name", customer.getFirstName());
        customerDetails.put("last_name", customer.getLastName());
        customerDetails.put("full_name", customer.getFirstName() + " " + customer.getLastName());
        customerDetails.put("external_id", customer.getExternalId());
        customerDetails.put("challenge_question", customer.getChallengeQuestion());
        customerDetails.put("challenge_answer", customer.getChallengeAnswer());
        customerDetails.put("password_change_required", customer.isPasswordChangeRequired());
        customerDetails.put("receive_email", customer.isReceiveEmail());
        customerDetails.put("registered", customer.isRegistered());


        CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
        Order cart = orderService.findCartForCustomer(customer);
        if (cart != null)
            customerDetails.put("cart_id", cart.getId());
        else
            customerDetails.put("cart_id", null);
        if(role.equals(Constant.roleServiceProvider)) {
            if (customCustomer.getHidePhoneNumber().equals(false)) {
                customerDetails.put("mobile_number", customCustomer.getMobileNumber());
            }
        }
        else
        {
            customerDetails.put("mobile_number", customCustomer.getMobileNumber());
        }
        customerDetails.put("hide_mobile_number", customCustomer.getHidePhoneNumber());
        customerDetails.put("secondary_mobile_number", customCustomer.getSecondaryMobileNumber());
        customerDetails.put("whatsapp_number", customCustomer.getWhatsappNumber());
        // List<ServiceProviderEntity>refSp=new ArrayList<>();
        // for(CustomerReferrer customerReferrer:customCustomer.getMyReferrer())
        // {
        //     refSp.add(customerReferrer.getServiceProvider());
        // }
        // customerDetails.put("referres",refSp);
        customerDetails.put("country_code", customCustomer.getCountryCode());
        List<ReferrerDTO>ref=new ArrayList<>();
        ReferrerDTO primaryRef=new ReferrerDTO();
        for(CustomerReferrer customerReferrer:customCustomer.getMyReferrer())
        {
            if(customerReferrer.getPrimaryRef() != null && customerReferrer.getPrimaryRef()==true) {
                primaryRef.setServiceProvider(serviceProviderDetailsMap(customerReferrer.getServiceProvider()));
                primaryRef.setCreatedAt(customerReferrer.getCreatedAt());
            }
            ReferrerDTO referrerDTO=new ReferrerDTO();
            referrerDTO.setServiceProvider(serviceProviderDetailsMap(customerReferrer.getServiceProvider()));
            referrerDTO.setCreatedAt(customerReferrer.getCreatedAt());
            ref.add(referrerDTO);
        }
        customerDetails.put("primary_referrer",primaryRef);
        customerDetails.put("referrers",ref);
        customerDetails.put("otp", customCustomer.getOtp());
        customerDetails.put("fathers_name", customCustomer.getFathersName());
        customerDetails.put("mothers_name", customCustomer.getMothersName());
        customerDetails.put("pan_number", customCustomer.getPanNumber());
        customerDetails.put("nationality", customCustomer.getNationality());
        customerDetails.put("dob", customCustomer.getDob());
        customerDetails.put("gender", customCustomer.getGender());
        customerDetails.put("adhar_number", customCustomer.getAdharNumber());
        customerDetails.put("category", customCustomer.getCategory());
        customerDetails.put("sub_category", customCustomer.getSubcategory());
        customerDetails.put("domicile", customCustomer.getDomicile());
        customerDetails.put("domicile_state", customCustomer.getDomicileState());
        customerDetails.put("secondary_email", customCustomer.getSecondaryEmail());
        customerDetails.put("category_issue_date", customCustomer.getCategoryIssueDate());

        if(customCustomer.getHeightCms() != null) {
            customerDetails.put("height_cms", customCustomer.getHeightCms().toString());
        }else {
            customerDetails.put("height_cms", customCustomer.getHeightCms());
        }

        if(customCustomer.getWeightKgs() != null) {
            customerDetails.put("weight_kgs", customCustomer.getWeightKgs().toString());
        }else {
            customerDetails.put("weight_kgs", customCustomer.getWeightKgs());
        }

        if(customCustomer.getChestSizeCms() != null) {
            customerDetails.put("chest_size_cms", customCustomer.getChestSizeCms().toString());
        }else {
            customerDetails.put("chest_size_cms", customCustomer.getChestSizeCms());
        }

        if(customCustomer.getShoeSizeInches() != null) {
            customerDetails.put("shoe_size_inches", customCustomer.getShoeSizeInches().toString());
        }else {
            customerDetails.put("shoe_size_inches", customCustomer.getShoeSizeInches());
        }

        if(customCustomer.getWaistSizeCms() != null) {
            customerDetails.put("waist_size_cms", customCustomer.getWaistSizeCms().toString());
        }else {
            customerDetails.put("waist_size_cms", customCustomer.getWaistSizeCms());
        }

        customerDetails.put("can_swim", customCustomer.getCanSwim());
        customerDetails.put("proficiency_in_sports_national_level", customCustomer.getProficiencyInSportsNationalLevel());
        customerDetails.put("first_choice_exam_city", customCustomer.getFirstChoiceExamCity());
        customerDetails.put("second_choice_exam_city", customCustomer.getSecondChoiceExamCity());
        customerDetails.put("third_choice_exam_city", customCustomer.getThirdChoiceExamCity());
        customerDetails.put("mphil_passed", customCustomer.getMphilPassed());
        customerDetails.put("phd_passed", customCustomer.getPhdPassed());
        customerDetails.put("number_of_attempts", customCustomer.getNumberOfAttempts());
        customerDetails.put("category_valid_upto", customCustomer.getCategoryValidUpto());
        customerDetails.put("religion", customCustomer.getReligion());
        customerDetails.put("belongs_to_minority", customCustomer.getBelongsToMinority());
        customerDetails.put("secondary_mobile_number", customCustomer.getSecondaryMobileNumber());
        customerDetails.put("whatsapp_number", customCustomer.getWhatsappNumber());
        customerDetails.put("secondary_email", customCustomer.getSecondaryEmail());
        customerDetails.put("disability_handicapped", customCustomer.getDisability());
        customerDetails.put("disability_type", customCustomer.getDisabilityType());
        customerDetails.put("disability_percentage", customCustomer.getDisabilityPercentage());
        customerDetails.put("is_ex_service_man", customCustomer.getExService());
        customerDetails.put("is_married", customCustomer.getIsMarried());
        customerDetails.put("visible_identification_mark_1", customCustomer.getIdentificationMark1());
        customerDetails.put("visible_identification_mark_2", customCustomer.getIdentificationMark2());
        customerDetails.put("is_ncc_certificate",customCustomer.getIs_ncc_certificate());
        customerDetails.put("is_nss_certificate",customCustomer.getIs_nss_certificate());
        customerDetails.put("ncc_certificate",customCustomer.getNcc_certificate());
        customerDetails.put("nss_certificate",customCustomer.getNss_certificate());
        customerDetails.put("created_by_role",customCustomer.getCreatedByRole());
        customerDetails.put("created_by_id",customCustomer.getCreatedById());
        customerDetails.put("modified_by_role",customCustomer.getModifiedByRole());
        customerDetails.put("modified_by_id",customCustomer.getModifiedById());
        customerDetails.put("registered_by_sp",customCustomer.getRegisteredBySp());
        customerDetails.put("interested_in_defence", customCustomer.getInterestedInDefence());
        customerDetails.put("workExperienceScope", customCustomer.getWorkExperienceScopeId());
        customerDetails.put("work_experience", customCustomer.getWorkExperience());
        customerDetails.put("sport_certificate", customCustomer.getSportCertificateId());
        customerDetails.put("is_other_or_stateCategory", customCustomer.getIsOtherOrStateCategory());
        customerDetails.put("other_or_state_category",customCustomer.getOtherOrStateCategory());
        customerDetails.put("other_category_date_of_issue",customCustomer.getOtherCategoryDateOfIssue());
        customerDetails.put("other_category_valid_upto",customCustomer.getOtherCategoryValidUpto());
        customerDetails.put("is_minority",customCustomer.getIsMinority());
        customerDetails.put("is_sports_certificate",customCustomer.getIsSportsCertificate());
        customerDetails.put("domicile_issueDate",customCustomer.getDomicileIssueDate());
        customerDetails.put("domicile_valid_upto",customCustomer.getDomicileValidUpto());
        customerDetails.put("archived",customCustomer.getArchived());
        customerDetails.put("suspended_or_activated_by_role",customCustomer.getArchivedByRole());
        customerDetails.put("suspended_or_activated_by_id",customCustomer.getArchivedById());
        customerDetails.put("profile_completed",customCustomer.getComplete());

        Map<String, String> currentAddress = new HashMap<>();
        Map<String, String> permanentAddress = new HashMap<>();
        for (CustomerAddress customerAddress : customer.getCustomerAddresses()) {
            if (customerAddress.getAddressName().equals("CURRENT_ADDRESS")) {
                currentAddress.put("address_name",customerAddress.getAddressName());
                currentAddress.put("state", customerAddress.getAddress().getStateProvinceRegion());
                currentAddress.put("city", customerAddress.getAddress().getCity());
                currentAddress.put("district", customerAddress.getAddress().getCounty());
                currentAddress.put("pincode", customerAddress.getAddress().getPostalCode());
                currentAddress.put("address_line", customerAddress.getAddress().getAddressLine1());
                currentAddress.put("stateId", String.valueOf(districtService.getStateByStateName(customerAddress.getAddress().getStateProvinceRegion()).getState_id()));
                currentAddress.put("district_id", String.valueOf(districtService.findDistrictByName(customerAddress.getAddress().getCounty()).getDistrict_id()));
            }
            if (customerAddress.getAddressName().equals("PERMANENT_ADDRESS")) {
                currentAddress.put("address_name",customerAddress.getAddressName());
                permanentAddress.put("state", customerAddress.getAddress().getStateProvinceRegion());
                permanentAddress.put("city", customerAddress.getAddress().getCity());
                permanentAddress.put("district", customerAddress.getAddress().getCounty());
                permanentAddress.put("pincode", customerAddress.getAddress().getPostalCode());
                permanentAddress.put("address_line", customerAddress.getAddress().getAddressLine1());
                permanentAddress.put("stateId", String.valueOf(districtService.getStateByStateName(customerAddress.getAddress().getStateProvinceRegion()).getState_id()));
                permanentAddress.put("district_id", String.valueOf(districtService.findDistrictByName(customerAddress.getAddress().getCounty()).getDistrict_id()));
            }

        }
        customerDetails.put("current_address", currentAddress);
        customerDetails.put("permanent_address", permanentAddress);

        /*customerDetails.put("current_district_id",
                districtService.findDistrictByName(currentAddress.get("district")) != null
                        ? districtService.findDistrictByName(currentAddress.get("district"))
                        : null);

        customerDetails.put("current_state_id",
                districtService.getStateByStateName(currentAddress.get("state")) != null
                        ? districtService.getStateByStateName(currentAddress.get("state"))
                        : null);

        customerDetails.put("permanent_district_id",
                districtService.findDistrictByName(permanentAddress.get("district")) != null
                        ? districtService.findDistrictByName(permanentAddress.get("district"))
                        : null);

        customerDetails.put("permanent_state_id",
                districtService.getStateByStateName(permanentAddress.get("state")) != null
                        ? districtService.getStateByStateName(permanentAddress.get("state"))
                        : null);*/

      /*  customerDetails.put("qualificationDetails",customCustomer.getQualificationDetailsList());
        customerDetails.put("documentList",customCustomer.getDocumentList());
        List<Map<String,Object>>listOfSavedProducts=new ArrayList<>();*/
    /*    if(!customCustomer.getSavedForms().isEmpty()) {
            for (Product product : customCustomer.getSavedForms()) {
                listOfSavedProducts.add(createProductResponseMap(product, null,customCustomer));
            }
        }

        customerDetails.put("savedForms",listOfSavedProducts);*/
        List<CustomerAddressDTO> addresses = new ArrayList<>();
        for (CustomerAddress customerAddress : customer.getCustomerAddresses()) {
            CustomerAddressDTO addressDTO = new CustomerAddressDTO();
            addressDTO.setAddressId(customerAddress.getId());
            addressDTO.setAddressName(customerAddress.getAddressName());
            addressDTO.setAddressLine1(customerAddress.getAddress().getAddressLine1());
            addressDTO.setState(customerAddress.getAddress().getStateProvinceRegion());
            addressDTO.setPincode(customerAddress.getAddress().getPostalCode());
            addressDTO.setDistrict(customerAddress.getAddress().getCounty());
            addressDTO.setCity(customerAddress.getAddress().getCity());
            addresses.add(addressDTO);
        }
        customerDetails.put("addresses", addresses);

        List<QualificationDetails> qualificationDetails= customCustomer.getQualificationDetailsList();
        List<Map<String, Object>> qualificationsWithNames = mapQualificationsForCustomer(qualificationDetails);
        customerDetails.put("qualification_details", qualificationsWithNames);

        List<Map<String, Object>> filteredDocuments = new ArrayList<>();

        for (Document document : customCustomer.getDocuments()) {
            if(document.getIsArchived().equals(false))
            {
                if (document.getFilePath() != null && document.getDocumentType() != null) {
                    Map<String, Object> documentDetails = new HashMap<>();
                    documentDetails.put("document_id", document.getDocumentId());
                    documentDetails.put("name", document.getName());
                    documentDetails.put("file_path", document.getFilePath());
                    if(document.getIs_qualification_document().equals(true) && document.getQualificationDetails()!=null)
                    {
                        documentDetails.put("qualification_detail_id",document.getQualificationDetails().getQualification_detail_id());
                    }
                    if(document.getDocumentValidity()!=null)
                    {
                        documentDetails.put("document_validity",document.getDocumentValidity());
                    }
                    String fileUrl = fileService.getFileUrl(document.getFilePath(), request);
                    documentDetails.put("file_url", fileUrl);

                    documentDetails.put("document_type", document.getDocumentType());
                    filteredDocuments.add(documentDetails);
                }
            }
        }

        if (!filteredDocuments.isEmpty()) {
            customerDetails.put("documents", filteredDocuments);
        }

        return customerDetails;
    }

    public ValidationResult validateInputMap(Map<String, Object> inputMap) {
        if (inputMap.keySet().size() > Constant.MAX_REQUEST_SIZE)
            return ValidationResult.EXCEEDS_MAX_SIZE;

        // Iterate through the map entries to check for nested maps
        for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
            Object value = entry.getValue();

            // Check if the value is a nested map
            if (value instanceof Map) {
                Map<?, ?> nestedMap = (Map<?, ?>) value;

                // Check the size of the nested map's key set
                if (nestedMap.keySet().size() > Constant.MAX_NESTED_KEY_SIZE) {
                    return ValidationResult.EXCEEDS_NESTED_SIZE;
                }
            }
        }
        return ValidationResult.SUCCESS;

    }

    @Transactional
    public Map<String, Object> serviceProviderDetailsMap(ServiceProviderEntity serviceProvider) {
        Map<String, Object> serviceProviderDetails = new HashMap<>();
        serviceProviderDetails.put("type", serviceProvider.getType());
        serviceProviderDetails.put("service_provider_id", serviceProvider.getService_provider_id());
        serviceProviderDetails.put("user_name", serviceProvider.getUser_name());
        serviceProviderDetails.put("first_name", serviceProvider.getFirst_name());
        serviceProviderDetails.put("last_name", serviceProvider.getLast_name());
        serviceProviderDetails.put("full_name", serviceProvider.getFirst_name() + " " + serviceProvider.getLast_name());
        serviceProviderDetails.put("country_code", serviceProvider.getCountry_code());
        serviceProviderDetails.put("father_name", serviceProvider.getFather_name());
        serviceProviderDetails.put("date_of_birth", serviceProvider.getDate_of_birth());
        serviceProviderDetails.put("aadhaar_number", serviceProvider.getAadhaar_number());
        serviceProviderDetails.put("pan_number", serviceProvider.getPan_number());
        serviceProviderDetails.put("mobileNumber", serviceProvider.getMobileNumber());
        serviceProviderDetails.put("secondary_mobile_number", serviceProvider.getSecondary_mobile_number());
        serviceProviderDetails.put("role", serviceProvider.getRole());
        serviceProviderDetails.put("whatsapp_number", serviceProvider.getWhatsapp_number());
        serviceProviderDetails.put("primary_email", serviceProvider.getPrimary_email());
        serviceProviderDetails.put("secondary_email", serviceProvider.getSecondary_email());
        serviceProviderDetails.put("password", serviceProvider.getPassword());
        serviceProviderDetails.put("is_running_business_unit", serviceProvider.getIs_running_business_unit());
        serviceProviderDetails.put("business_name", serviceProvider.getBusiness_name());
        serviceProviderDetails.put("business_location", serviceProvider.getBusiness_location());
        serviceProviderDetails.put("business_email", serviceProvider.getBusiness_email());
        serviceProviderDetails.put("number_of_employees", serviceProvider.getNumber_of_employees());
        serviceProviderDetails.put("has_technical_knowledge", serviceProvider.getHas_technical_knowledge());
        serviceProviderDetails.put("work_experience_in_months", serviceProvider.getWork_experience_in_months());
        serviceProviderDetails.put("latitude", serviceProvider.getLatitude());
        serviceProviderDetails.put("longitude", serviceProvider.getLongitude());
        serviceProviderDetails.put("service_provider_status", serviceProvider.getTestStatus());
        serviceProviderDetails.put("rank", serviceProvider.getRanking());
        serviceProviderDetails.put("signedUp", serviceProvider.getSignedUp());
        serviceProviderDetails.put("skills", serviceProvider.getSkills());
        serviceProviderDetails.put("infra", serviceProvider.getInfra());
        serviceProviderDetails.put("languages", serviceProvider.getLanguages());
        serviceProviderDetails.put("privileges", serviceProvider.getPrivileges());
        serviceProviderDetails.put("spAddresses", serviceProvider.getSpAddresses());
        serviceProviderDetails.put("mothers_name", serviceProvider.getMother_name());
        serviceProviderDetails.put("business_unit_infra_score", serviceProvider.getBusinessUnitInfraScore());
        serviceProviderDetails.put("qualification_score", serviceProvider.getQualificationScore());
        serviceProviderDetails.put("technical_expertise_score", serviceProvider.getTechnicalExpertiseScore());
        serviceProviderDetails.put("work_experience_score", serviceProvider.getWorkExperienceScore());
        serviceProviderDetails.put("written_test_score", serviceProvider.getWrittenTestScore());
        serviceProviderDetails.put("image_upload_score", serviceProvider.getImageUploadScore());
        serviceProviderDetails.put("total_score", serviceProvider.getTotalScore());
        if (serviceProvider.getType() != null) {
            if (serviceProvider.getType().equalsIgnoreCase("PROFESSIONAL")) {
                serviceProviderDetails.put("number_of_employees", serviceProvider.getNumber_of_employees());
                serviceProviderDetails.put("staff_score", serviceProvider.getStaffScore());
            } else {
                serviceProviderDetails.put("part_time_or_full_time", serviceProvider.getPartTimeOrFullTime());
                serviceProviderDetails.put("part_time_or_full_time_score", serviceProvider.getPartTimeOrFullTimeScore());
                serviceProviderDetails.put("infra_scores", serviceProvider.getInfraScore());
            }
        }

        serviceProviderDetails.put("skills", serviceProvider.getSkills());
        serviceProviderDetails.put("infra", serviceProvider.getInfra());
        serviceProviderDetails.put("languages", serviceProvider.getLanguages());
        serviceProviderDetails.put("privileges", serviceProvider.getPrivileges());
        serviceProviderDetails.put("spAddresses", serviceProvider.getSpAddresses());
        List<QualificationDetails> qualificationDetails = serviceProvider.getQualificationDetailsList();
        List<Map<String, Object>> qualificationsWithNames = mapQualificationsForServiceProvider(qualificationDetails);
        serviceProviderDetails.put("qualificationDetails", qualificationsWithNames);

        List<Map<String, Object>> filteredDocuments = new ArrayList<>();

        for (ServiceProviderDocument document : serviceProvider.getDocuments()) {
            if (document.getIsArchived() != null && !document.getIsArchived()) {
                if (document.getFilePath() != null && document.getDocumentType() != null) {
                    Map<String, Object> documentDetails = new HashMap<>();
                    documentDetails.put("documentId", document.getDocumentId());
                    documentDetails.put("name", document.getName());
                    documentDetails.put("filePath", document.getFilePath());
                    if(document.getIs_qualification_document().equals(true) && document.getQualificationDetails()!=null)
                    {
                        documentDetails.put("qualification_detail_id",document.getQualificationDetails().getQualification_detail_id());
                    }
                    if(document.getDocumentValidity()!=null)
                    {
                        documentDetails.put("documentValidity",document.getDocumentValidity());
                    }

                    String fileUrl = fileService.getFileUrl(document.getFilePath(), request);
                    documentDetails.put("fileUrl", fileUrl);

                    documentDetails.put("documentType", document.getDocumentType());
                    filteredDocuments.add(documentDetails);
                }
            }

        }

        if (!filteredDocuments.isEmpty()) {
            serviceProviderDetails.put("documents", filteredDocuments);
        }
        return serviceProviderDetails;
    }

    public Map<String, Object> trimStringValues(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof String) {
                // Trim the string and update the map
                String trimmedValue = ((String) entry.getValue()).trim();
                entry.setValue(trimmedValue);
            }
        }
        return map;
    }

    public boolean isValidEmail(String email) {
        return email != null && email.matches(Constant.EMAIL_REGEXP);
    }

    public List<Map<String, Object>> mapQualificationsForCustomer(List<QualificationDetails> qualificationDetails) {
        return qualificationDetails.stream()
                .map(qualificationDetail -> {
                    Map<String, Object> qualificationInfo = new HashMap<>();
                    // Fetch the qualification by qualification_id
                    Qualification qualification = entityManager.find(Qualification.class, qualificationDetail.getQualification_id());
                    Institution institution =  qualificationDetail.getInstitution();
                    CustomStream customStream = entityManager.find(CustomStream.class, qualificationDetail.getStream_id());

                    // Fetch the BoardUniversity
                    BoardUniversity boardUniversity = entityManager.find(BoardUniversity.class, qualificationDetail.getBoard_university_id());

                    // Determine the board_university_name
                    String boardUniversityName = null;
                    if (qualificationDetail.getBoard_university_id() .equals(1L)) {
                        // Check the `otherItems` for a matching "Board or University" field
                        Optional<OtherItem> otherItemOpt = qualificationDetail.getOtherItems().stream()
                                .filter(otherItem ->
                                        otherItem.getField_name().equalsIgnoreCase("board_or_university") &&
                                                Objects.equals(otherItem.getUser_id(), qualificationDetail.getCustom_customer().getId()))
                                .findFirst();
                        if (otherItemOpt.isPresent()) {
                            boardUniversityName = otherItemOpt.get().getTyped_text();
                        }
                    }

                    if (boardUniversityName == null) {
                        // Use the BoardUniversity name if no valid entry in `otherItems` is found
                        boardUniversityName = boardUniversity != null ? boardUniversity.getBoard_university_name() : "Unknown BoardUniversity";
                    }

                    // Populate the map
                    qualificationInfo.put("qualification_detail_id", qualificationDetail.getQualification_detail_id());
                    qualificationInfo.put("institution_id", qualificationDetail.getInstitution().getInstitution_id());
                    qualificationInfo.put("date_of_passing", qualificationDetail.getDate_of_passing());
                    qualificationInfo.put("examination_role_number", qualificationDetail.getExamination_role_number());
                    qualificationInfo.put("examination_registration_number", qualificationDetail.getExamination_registration_number());
                    qualificationInfo.put("board_university_id", qualificationDetail.getBoard_university_id());
                    qualificationInfo.put("stream_id", qualificationDetail.getStream_id());
                    qualificationInfo.put("total_marks_type", qualificationDetail.getTotal_marks_type());
                    qualificationInfo.put("total_marks", qualificationDetail.getTotal_marks());
                    qualificationInfo.put("marks_obtained", qualificationDetail.getMarks_obtained());
                    qualificationInfo.put("cumulative_percentage_value", qualificationDetail.getCumulative_percentage_value());
                    qualificationInfo.put("qualification_id", qualificationDetail.getQualification_id());
                    qualificationInfo.put("is_grade",qualificationDetail.getIs_grade());
                    qualificationInfo.put("grade_value",qualificationDetail.getGrade_value());
                    qualificationInfo.put("is_division",qualificationDetail.getIs_division());
                    qualificationInfo.put("division_value",qualificationDetail.getDivision_value());

                    // Add qualification_name
                    if (qualification != null) {
                        qualificationInfo.put("qualification_name", qualification.getQualification_name());
                    } else {
                        qualificationInfo.put("qualification_name", "Unknown Qualification");
                    }

                    // Add board_university_name
                    qualificationInfo.put("board_university_name", boardUniversityName);

                    // Add institution_name
                    if (institution != null) {
                        qualificationInfo.put("institution_name", institution.getInstitution_name());
                    } else {
                        qualificationInfo.put("institution_name", "Unknown Institution");
                    }

                    // Add stream_name
                    if (customStream != null) {
                        qualificationInfo.put("stream_name", customStream.getStreamName());
                    } else {
                        qualificationInfo.put("stream_name", "Unknown Stream");
                    }

                    // Add subjects
                    List<Map<String, Object>> subjects = qualificationDetail.getSubject_ids().stream()
                            .map(subjectId -> {
                                Map<String, Object> subjectInfo = new HashMap<>();
                                CustomSubject subject = entityManager.find(CustomSubject.class, subjectId);
                                if (subject != null) {
                                    subjectInfo.put("subject_id", subject.getSubjectId());
                                    subjectInfo.put("subject_name", subject.getSubjectName());
                                } else {
                                    subjectInfo.put("subject_id", subjectId);
                                    subjectInfo.put("subject_name", "Unknown Subject");
                                }
                                return subjectInfo;
                            })
                            .collect(Collectors.toList());

                    qualificationInfo.put("subjects", subjects);
                    qualificationInfo.put("subject_details", qualificationDetail.getSubject_details());

                    Map<String, Object> filteredDocument = null;
                    Document document= qualificationDetail.getQualificationDocument();
                    if(document==null)
                    {
                        qualificationInfo.put("qualification_document",null);
                    }
                    else {
                        if(document.getIsArchived().equals(false))
                        {
                            if (document.getFilePath() != null && document.getDocumentType() != null) {
                                Map<String, Object> documentDetails = new HashMap<>();
                                documentDetails.put("documentId", document.getDocumentId());
                                documentDetails.put("name", document.getName());
                                documentDetails.put("filePath", document.getFilePath());
                                String fileUrl = fileService.getFileUrl(document.getFilePath(), request);
                                documentDetails.put("fileUrl", fileUrl);
                                filteredDocument=documentDetails;
                            }
                        }
                        qualificationInfo.put("qualification_document", filteredDocument);
                    }

                    return qualificationInfo;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> mapQualificationsForServiceProvider(List<QualificationDetails> qualificationDetails) {
        return qualificationDetails.stream()
                .map(qualificationDetail -> {
                    Map<String, Object> qualificationInfo = new HashMap<>();

                    // Fetch the qualification by qualification_id
                    Qualification qualification = entityManager.find(Qualification.class, qualificationDetail.getQualification_id());
                    Institution institution = entityManager.find(Institution.class, qualificationDetail.getInstitution().getInstitution_id());
                    CustomStream customStream = entityManager.find(CustomStream.class, qualificationDetail.getStream_id());

                    // Fetch the BoardUniversity
                    BoardUniversity boardUniversity = entityManager.find(BoardUniversity.class, qualificationDetail.getBoard_university_id());

                    // Determine the board_university_name
                    String boardUniversityName = null;
                    if (qualificationDetail.getBoard_university_id() .equals(1L)) {
                        // Check the `otherItems` for a matching "Board or University" field
                        Optional<OtherItem> otherItemOpt = qualificationDetail.getOtherItems().stream()
                                .filter(otherItem ->
                                        otherItem.getField_name().equalsIgnoreCase("board_or_university") &&
                                                Objects.equals(otherItem.getUser_id(), qualificationDetail.getService_provider().getService_provider_id()))
                                .findFirst();
                        if (otherItemOpt.isPresent()) {
                            boardUniversityName = otherItemOpt.get().getTyped_text();
                        }
                    }

                    if (boardUniversityName == null) {
                        // Use the BoardUniversity name if no valid entry in `otherItems` is found
                        boardUniversityName = boardUniversity != null ? boardUniversity.getBoard_university_name() : "Unknown BoardUniversity";
                    }

                    // Populate the map with necessary fields from qualificationDetail
                    qualificationInfo.put("qualification_detail_id",qualificationDetail.getQualification_detail_id());
                    qualificationInfo.put("date_of_passing", qualificationDetail.getDate_of_passing());
                    qualificationInfo.put("examination_role_number", qualificationDetail.getExamination_role_number());
                    qualificationInfo.put("examination_registration_number", qualificationDetail.getExamination_registration_number());
                    qualificationInfo.put("board_university_id", qualificationDetail.getBoard_university_id());
                    qualificationInfo.put("institution_id", qualificationDetail.getInstitution().getInstitution_id());
                    qualificationInfo.put("stream_id",qualificationDetail.getStream_id());
                    qualificationInfo.put("total_marks_type",qualificationDetail.getTotal_marks_type());
                    qualificationInfo.put("cumulative_percentage_value", qualificationDetail.getCumulative_percentage_value());
                    qualificationInfo.put("subject_name", qualificationDetail.getSubject_name());
                    qualificationInfo.put("total_marks", qualificationDetail.getTotal_marks());
                    qualificationInfo.put("marks_obtained", qualificationDetail.getMarks_obtained());
                    qualificationInfo.put("qualification_id",qualificationDetail.getQualification_id());
                    qualificationInfo.put("qualification_document",qualificationDetail.getQualificationDocument());
                    qualificationInfo.put("is_grade",qualificationDetail.getIs_grade());
                    qualificationInfo.put("grade_value",qualificationDetail.getGrade_value());
                    qualificationInfo.put("is_division",qualificationDetail.getIs_division());
                    qualificationInfo.put("division_value",qualificationDetail.getDivision_value());

                    // Replace the qualification_id with qualification_name
                    if (qualification != null) {
                        qualificationInfo.put("qualification_name", qualification.getQualification_name());
                    } else {
                        qualificationInfo.put("qualification_name", "Unknown Qualification");
                    }
                    // Add board_university_name
                    qualificationInfo.put("board_university_name", boardUniversityName);

                    if (institution != null) {
                        qualificationInfo.put("institution_name", institution.getInstitution_name());
                    }else {
                        qualificationInfo.put("institution_name", "Unknown Institution");
                    }
                    if (customStream != null) {
                        qualificationInfo.put("stream_name", customStream.getStreamName());
                    }else {
                        qualificationInfo.put("stream_name", "Unknown Stream");
                    }

                    Map<String, Object> filteredDocument = null;
                    ServiceProviderDocument serviceProviderDocument= qualificationDetail.getServiceProviderDocument();
                    if(serviceProviderDocument==null)
                    {
                        qualificationInfo.put("qualification_document",null);
                    }
                    else {
                        if(serviceProviderDocument.getIsArchived().equals(false))
                        {
                            if (serviceProviderDocument.getFilePath() != null && serviceProviderDocument.getDocumentType() != null) {
                                Map<String, Object> documentDetails = new HashMap<>();
                                documentDetails.put("documentId", serviceProviderDocument.getDocumentId());
                                documentDetails.put("name", serviceProviderDocument.getName());
                                documentDetails.put("filePath", serviceProviderDocument.getFilePath());
                                String fileUrl = fileService.getFileUrl(serviceProviderDocument.getFilePath(), request);
                                documentDetails.put("fileUrl", fileUrl);
                                filteredDocument=documentDetails;
                            }
                        }
                        qualificationInfo.put("qualification_document", filteredDocument);
                    }
                    return qualificationInfo;
                }).collect(Collectors.toList());
    }

    public boolean isFutureDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setLenient(false);
        try {
            Date inputDate = sdf.parse(dateStr);
            Date currentDate = new Date();
            return inputDate.after(currentDate);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return false;
        }
    }

    public boolean validateCategoryIssueAndValidUptoDates(String categoryIssueDate, String categoryUptoDate, List<String> errorMessages) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setLenient(false);
        try {
            boolean cond = true;
            Date issueDate = sdf.parse(categoryIssueDate);
            Date uptoDate = sdf.parse(categoryUptoDate);

            if(issueDate.after(uptoDate)) {
                cond = false;
                errorMessages.add("category Issue date cannot be future of category valid upto date.");
            }

            if(issueDate.after(new Date())) {
                cond = false;
                errorMessages.add("category Issue date cannot be future of current date");
            }
            return cond;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return false;
        }
    }

    public boolean validateCategoryIssueDate(String categoryIssueDate, CustomCustomer customer, List<String> errorMessages) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setLenient(false);
        try {
            boolean cond = true;
            Date issueDate = sdf.parse(categoryIssueDate);

            if(issueDate.after(new Date())) {
                cond = false;
                errorMessages.add("Category issue date has to past or current date");
            }
            if(customer.getCategoryValidUpto() != null) {
                Date uptoDate = sdf.parse(customer.getCategoryValidUpto());
                if(issueDate.after(uptoDate)) {
                    cond = false;
                    errorMessages.add("category Issue date cannot be future of category valid upto date.");
                }
            }

            return cond;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return false;
        }
    }

    public boolean validateCategoryUptoDate(String categoryUptoDate, CustomCustomer customer, List<String> errorMessages) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setLenient(false);
        try {
            boolean cond = true;
            Date uptoDate = sdf.parse(categoryUptoDate);

            if(!uptoDate.after(new Date())) {
                cond = false;
                errorMessages.add("Category upto date has to future date");
            }
            if(customer.getCategoryIssueDate() == null) {
                cond = false;
                errorMessages.add("There is no entry of categoryIssueDate cannot");
            }else {
                Date issueDate = sdf.parse(customer.getCategoryIssueDate());
                if(issueDate.after(uptoDate)) {
                    cond = false;
                    errorMessages.add("category Issue date cannot be future of category valid upto date.");
                }
            }

            return cond;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return false;
        }
    }

    public Map<String, Object> adminDetailsMap(CustomAdmin customAdmin) {
        Map<String, Object> customAdminDetails = new HashMap<>();
        if (customAdmin.getRole() == 2) {
            customAdminDetails.put("admin_id", customAdmin.getAdmin_id());
        } else if (customAdmin.getRole() == 1) {
            customAdminDetails.put("super_admin_id", customAdmin.getAdmin_id());
        } else if (customAdmin.getRole() == 3) {
            customAdminDetails.put("admin_service_provider_id", customAdmin.getAdmin_id());
        }

        customAdminDetails.put("role_id", customAdmin.getRole());
        customAdminDetails.put("user_name", customAdmin.getUser_name());
        customAdminDetails.put("password", customAdmin.getPassword());
        customAdminDetails.put("otp", customAdmin.getOtp());
        customAdminDetails.put("mobile_number", customAdmin.getMobileNumber());
        customAdminDetails.put("country_code", customAdmin.getCountry_code());
        return customAdminDetails;
    }

    public enum ValidationResult {
        SUCCESS,
        EXCEEDS_MAX_SIZE,
        EXCEEDS_NESTED_SIZE,
        INVALID_TYPE
    }
    public  int isInValidOrInPast(Date targetCompletionDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(new Date());
            Date currentDate = dateFormat.parse(formattedDate);
            // Convert the Date object to ZonedDateTime in the system's default time zone
            ZonedDateTime inputDateTime = targetCompletionDate.toInstant()
                    .atZone(ZoneId.of("Asia/Kolkata"));

            // Get the current date and time in IST
            ZonedDateTime currentDateTimeInIST = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            // Check if the parsed date in IST is before the current date and time in IST
            if (inputDateTime.isBefore(currentDateTimeInIST)) {
                return 1; // Date is in the past
            } else {
                return 0; // Date is valid but not in the past
            }

        }catch (NumberFormatException numberFormatException)
        {
            return -1;
        }
        catch (Exception e) {
            // Handle errors like conversion errors
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();  // Print the exception details for debugging
            return -1; // Return -1 if there is any error
        }
    }
    public  boolean isAlphabetic(String input) {
        // Check if the string contains only alphabetic characters
        if (input == null || input.isEmpty()) {
            return false;  // Return false for null or empty strings
        }

        // Use regular expression to check if the string contains only alphabets
        return input.matches("[a-zA-Z]+");
    }public long parseToLong(Object value) {
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value); // Parse string to long
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format");
            }
        } else if (value instanceof Number) {
            return ((Number) value).longValue(); // Cast directly to long if it's already a number
        } else {
            throw new IllegalArgumentException("Value is neither a valid String nor a Number");
        }
    }
    public int[] calculateAgeRange(Date bornBeforeDate, Date bornAfterDate) {
        // Convert Date to ZonedDateTime in the IST (India Standard Time) time zone
        ZoneId indiaZone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime bornBeforeZoned = bornBeforeDate.toInstant().atZone(indiaZone);
        ZonedDateTime bornAfterZoned = bornAfterDate.toInstant().atZone(indiaZone);

        // Get today's date in the same time zone (IST)
        ZonedDateTime today = ZonedDateTime.now(indiaZone);

        // Calculate max age (from bornBeforeDate)
        int maxAge = calculateAge(bornBeforeZoned, today);

        // Calculate min age (from bornAfterDate)
        int minAge = calculateAge(bornAfterZoned, today);

        // Return the result as an array [minAge, maxAge]
        return new int[] { minAge, maxAge };
    }

    public  int calculateAge(ZonedDateTime birthDate, ZonedDateTime currentDate) {
        // Calculate the years difference between birthDate and currentDate
        Period period = Period.between(birthDate.toLocalDate(), currentDate.toLocalDate());
        return period.getYears();
    }
    public String[] separateName(String fullName) {
        // Find the last space in the full name
        int lastSpaceIndex = fullName.lastIndexOf(" ");
        // If there's no space, it means there's only one name
        if (lastSpaceIndex == -1) {
            return new String[]{fullName, ""}; // Only a first name
        }
        // First name is everything before the last space
        String firstName = fullName.substring(0, lastSpaceIndex);
        // Last name is everything after the last space
        String lastName = fullName.substring(lastSpaceIndex + 1);
        return new String[]{firstName, lastName};
    }
    public  List<BigInteger> getPaginatedList(List<BigInteger> fullList, int page, int pageSize) {
        int fromIndex = (page) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, fullList.size());

        if (fromIndex >= fullList.size()) {
            return List.of(); // Return empty list if page is out of bounds
        }

        return fullList.subList(fromIndex, toIndex);
    }
}



