
package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.CustomerBasicDetailsDto;
import com.community.api.dto.PostDetailsDTO;
import com.community.api.dto.ReferrerDTO;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.*;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.utils.Document;
import com.community.api.utils.DocumentType;
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
import javax.persistence.Query;
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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
    DocumentStorageService documentStorageService;
    @Autowired
    HttpServletRequest request;
    @Autowired
    DeviceDetectorService deviceDetectorService;
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
    public Map<String, Object> breakReferenceForCustomer(Customer customer,String authHeader,HttpServletRequest httpServletRequest) throws Exception {
        String jwtToken = authHeader.substring(7);
        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
        Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
        String role = roleService.getRoleByRoleId(roleId).getRole_name();
        if (deviceDetectorService.isRequestFromMobileApp(request)) {
            Map<String, Object> customerDetailsForMobile = new HashMap<>();
            customerDetailsForMobile.put("id", customer.getId());
            customerDetailsForMobile.put("dateCreated", customer.getAuditable().getDateCreated());
            customerDetailsForMobile.put("createdBy", customer.getAuditable().getCreatedBy());
            customerDetailsForMobile.put("dateUpdated", customer.getAuditable().getDateUpdated());
            customerDetailsForMobile.put("updatedBy", customer.getAuditable().getUpdatedBy());
            customerDetailsForMobile.put("username", customer.getUsername());
            customerDetailsForMobile.put("password", customer.getPassword());
            customerDetailsForMobile.put("emailAddress", customer.getEmailAddress());
            customerDetailsForMobile.put("firstName", customer.getFirstName());
            customerDetailsForMobile.put("lastName", customer.getLastName());
            customerDetailsForMobile.put("fullName", customer.getFirstName() + " " + customer.getLastName());
            customerDetailsForMobile.put("externalId", customer.getExternalId());
            customerDetailsForMobile.put("challengeQuestion", customer.getChallengeQuestion());
            customerDetailsForMobile.put("challengeAnswer", customer.getChallengeAnswer());
            customerDetailsForMobile.put("isPasswordChangeRequired", customer.isPasswordChangeRequired());
            customerDetailsForMobile.put("isReceiveEmail", customer.isReceiveEmail());
            customerDetailsForMobile.put("isRegistered", customer.isRegistered());
            customerDetailsForMobile.put("isDeactivated", customer.isDeactivated());
            customerDetailsForMobile.put("customerPayments", customer.getCustomerPayments());
            customerDetailsForMobile.put("taxExemptionCode", customer.getTaxExemptionCode());
            customerDetailsForMobile.put("unencodedPassword", customer.getUnencodedPassword());
            customerDetailsForMobile.put("unencodedChallengeAnswer", customer.getUnencodedChallengeAnswer());
            customerDetailsForMobile.put("isAnonymous", customer.isAnonymous());
            customerDetailsForMobile.put("isCookied", customer.isCookied());
            customerDetailsForMobile.put("isLoggedIn", customer.isLoggedIn());
            customerDetailsForMobile.put("transientProperties", customer.getTransientProperties());

            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
            Order cart = orderService.findCartForCustomer(customer);
            if (cart != null)
                customerDetailsForMobile.put("cartId", cart.getId());
            else
                customerDetailsForMobile.put("cartId", null);
            if(role.equals(Constant.roleServiceProvider)) {
                if (customCustomer.getHidePhoneNumber().equals(false)) {
                    customerDetailsForMobile.put("mobileNumber", customCustomer.getMobileNumber());
                }
            }
            else
            {
                customerDetailsForMobile.put("mobileNumber", customCustomer.getMobileNumber());
            }
            customerDetailsForMobile.put("hideMobileNumber", customCustomer.getHidePhoneNumber());
            customerDetailsForMobile.put("secondaryMobileNumber", customCustomer.getSecondaryMobileNumber());
            customerDetailsForMobile.put("whatsappNumber", customCustomer.getWhatsappNumber());
            customerDetailsForMobile.put("countryCode", customCustomer.getCountryCode());
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
            customerDetailsForMobile.put("primaryRef",primaryRef);
            customerDetailsForMobile.put("referrers",ref);
            customerDetailsForMobile.put("otp", customCustomer.getOtp());
            customerDetailsForMobile.put("fathersName", customCustomer.getFathersName());
            customerDetailsForMobile.put("mothersName", customCustomer.getMothersName());
            customerDetailsForMobile.put("panNumber", customCustomer.getPanNumber());
            customerDetailsForMobile.put("nationality", customCustomer.getNationality());
            customerDetailsForMobile.put("dob", customCustomer.getDob());
            customerDetailsForMobile.put("gender", customCustomer.getGender());
            customerDetailsForMobile.put("adharNumber", customCustomer.getAdharNumber());
            customerDetailsForMobile.put("category", customCustomer.getCategory());
            customerDetailsForMobile.put("subcategory", customCustomer.getSubcategory());
            customerDetailsForMobile.put("domicile", customCustomer.getDomicile());
            customerDetailsForMobile.put("domicileState", customCustomer.getDomicileState());
            customerDetailsForMobile.put("secondaryEmail", customCustomer.getSecondaryEmail());
            customerDetailsForMobile.put("categoryIssueDate", customCustomer.getCategoryIssueDate());

            if(customCustomer.getHeightCms() != null) {
                customerDetailsForMobile.put("heightCms", customCustomer.getHeightCms().toString());
            }else {
                customerDetailsForMobile.put("heightCms", customCustomer.getHeightCms());
            }

            if(customCustomer.getWeightKgs() != null) {
                customerDetailsForMobile.put("weightKgs", customCustomer.getWeightKgs().toString());
            }else {
                customerDetailsForMobile.put("weightKgs", customCustomer.getWeightKgs());
            }

            if(customCustomer.getChestSizeCms() != null) {
                customerDetailsForMobile.put("chestSizeCms", customCustomer.getChestSizeCms().toString());
            }else {
                customerDetailsForMobile.put("chestSizeCms", customCustomer.getChestSizeCms());
            }

            if(customCustomer.getShoeSizeInches() != null) {
                customerDetailsForMobile.put("shoeSizeInches", customCustomer.getShoeSizeInches().toString());
            }else {
                customerDetailsForMobile.put("shoeSizeInches", customCustomer.getShoeSizeInches());
            }

            if(customCustomer.getWaistSizeCms() != null) {
                customerDetailsForMobile.put("waistSizeCms", customCustomer.getWaistSizeCms().toString());
            }else {
                customerDetailsForMobile.put("waistSizeCms", customCustomer.getWaistSizeCms());
            }

            customerDetailsForMobile.put("canSwim", customCustomer.getCanSwim());
            customerDetailsForMobile.put("proficiencyInSportsNationalLevel", customCustomer.getProficiencyInSportsNationalLevel());
            customerDetailsForMobile.put("firstChoiceExamCity", customCustomer.getFirstChoiceExamCity());
            customerDetailsForMobile.put("secondChoiceExamCity", customCustomer.getSecondChoiceExamCity());
            customerDetailsForMobile.put("thirdChoiceExamCity", customCustomer.getThirdChoiceExamCity());
            customerDetailsForMobile.put("mphilPassed", customCustomer.getMphilPassed());
            customerDetailsForMobile.put("phdPassed", customCustomer.getPhdPassed());
            customerDetailsForMobile.put("numberOfAttempts", customCustomer.getNumberOfAttempts());
            customerDetailsForMobile.put("categoryValidUpto", customCustomer.getCategoryValidUpto());
            customerDetailsForMobile.put("religion", customCustomer.getReligion());
            customerDetailsForMobile.put("belongsToMinority", customCustomer.getBelongsToMinority());
            customerDetailsForMobile.put("secondaryMobileNumber", customCustomer.getSecondaryMobileNumber());
            customerDetailsForMobile.put("whatsappNumber", customCustomer.getWhatsappNumber());
            customerDetailsForMobile.put("secondaryEmail", customCustomer.getSecondaryEmail());
            customerDetailsForMobile.put("disability", customCustomer.getDisability());
            customerDetailsForMobile.put("disabilityType", customCustomer.getDisabilityType());
            customerDetailsForMobile.put("disabilityPercentage", customCustomer.getDisabilityPercentage());
            customerDetailsForMobile.put("exService", customCustomer.getExService());
            customerDetailsForMobile.put("isMarried", customCustomer.getIsMarried());
            customerDetailsForMobile.put("identificationMark1", customCustomer.getIdentificationMark1());
            customerDetailsForMobile.put("identificationMark2", customCustomer.getIdentificationMark2());
            customerDetailsForMobile.put("isNccCertificate",customCustomer.getIsNccCertificate());
            customerDetailsForMobile.put("isNssCertificate",customCustomer.getIsNssCertificate());
            customerDetailsForMobile.put("nccCertificate",customCustomer.getNccCertificate());
            customerDetailsForMobile.put("nssCertificate",customCustomer.getNssCertificate());
            customerDetailsForMobile.put("createdByRole",customCustomer.getCreatedByRole());
            customerDetailsForMobile.put("createdById",customCustomer.getCreatedById());
            customerDetailsForMobile.put("modifiedByRole",customCustomer.getModifiedByRole());
            customerDetailsForMobile.put("modifiedById",customCustomer.getModifiedById());
            customerDetailsForMobile.put("registeredBySp",customCustomer.getRegisteredBySp());
            customerDetailsForMobile.put("interestedInDefence", customCustomer.getInterestedInDefence());
            customerDetailsForMobile.put("workExperienceScopeId", customCustomer.getWorkExperienceScopeId());
            customerDetailsForMobile.put("workExperience", customCustomer.getWorkExperience());
            customerDetailsForMobile.put("sportCertificateId", customCustomer.getSportCertificateId());
            customerDetailsForMobile.put("isOtherOrStateCategory", customCustomer.getIsOtherOrStateCategory());
            customerDetailsForMobile.put("otherOrStateCategory",customCustomer.getOtherOrStateCategory());
            customerDetailsForMobile.put("otherCategoryDateOfIssue",customCustomer.getOtherCategoryDateOfIssue());
            customerDetailsForMobile.put("otherCategoryValidUpto",customCustomer.getOtherCategoryValidUpto());
            customerDetailsForMobile.put("isSportsCertificate",customCustomer.getIsSportsCertificate());
            customerDetailsForMobile.put("domicileIssueDate",customCustomer.getDomicileIssueDate());
            customerDetailsForMobile.put("domicileValidUpto",customCustomer.getDomicileValidUpto());
            customerDetailsForMobile.put("isLivePhotoNa",customCustomer.getIsLivePhotoNa());
            customerDetailsForMobile.put("archived",customCustomer.getArchived());
            customerDetailsForMobile.put("archivedByRole",customCustomer.getArchivedByRole());
            customerDetailsForMobile.put("archivedById",customCustomer.getArchivedById());
            customerDetailsForMobile.put("profileComplete",customCustomer.getProfileComplete());
            for (CustomerAddress customerAddress : customer.getCustomerAddresses()) {
                if (customerAddress.getAddressName().equals("CURRENT_ADDRESS")) {
                    customerDetailsForMobile.put("addressName",customerAddress.getAddressName());
                    customerDetailsForMobile.put("currentState", customerAddress.getAddress().getStateProvinceRegion());
                    customerDetailsForMobile.put("currentCity", customerAddress.getAddress().getCity());
                    customerDetailsForMobile.put("currentDistrict", customerAddress.getAddress().getCounty());
                    customerDetailsForMobile.put("currentPincode", customerAddress.getAddress().getPostalCode());
                    customerDetailsForMobile.put("currentAddress", customerAddress.getAddress().getAddressLine1());
                    customerDetailsForMobile.put("stateId", String.valueOf(districtService.getStateByStateName(customerAddress.getAddress().getStateProvinceRegion()).getState_id()));
                    customerDetailsForMobile.put("districtId", String.valueOf(districtService.findDistrictByName(customerAddress.getAddress().getCounty()).getDistrict_id()));
                }
                if (customerAddress.getAddressName().equals("PERMANENT_ADDRESS")) {
                    customerDetailsForMobile.put("addressName",customerAddress.getAddressName());
                    customerDetailsForMobile.put("permanentState", customerAddress.getAddress().getStateProvinceRegion());
                    customerDetailsForMobile.put("permanentCity", customerAddress.getAddress().getCity());
                    customerDetailsForMobile.put("permanentDistrict", customerAddress.getAddress().getCounty());
                    customerDetailsForMobile.put("permanentPincode", customerAddress.getAddress().getPostalCode());
                    customerDetailsForMobile.put("permanentAddress", customerAddress.getAddress().getAddressLine1());
                    customerDetailsForMobile.put("stateId", String.valueOf(districtService.getStateByStateName(customerAddress.getAddress().getStateProvinceRegion()).getState_id()));
                    customerDetailsForMobile.put("districtId", String.valueOf(districtService.findDistrictByName(customerAddress.getAddress().getCounty()).getDistrict_id()));
                }

            }

            Map<String, String> currentAddress = new HashMap<>();
            Map<String, String> permanentAddress = new HashMap<>();
            for (CustomerAddress customerAddress : customer.getCustomerAddresses()) {
                if (customerAddress.getAddressName().equals("CURRENT_ADDRESS")) {
                    currentAddress.put("addressName",customerAddress.getAddressName());
                    currentAddress.put("state", customerAddress.getAddress().getStateProvinceRegion());
                    currentAddress.put("city", customerAddress.getAddress().getCity());
                    currentAddress.put("district", customerAddress.getAddress().getCounty());
                    currentAddress.put("pincode", customerAddress.getAddress().getPostalCode());
                    currentAddress.put("addressLine", customerAddress.getAddress().getAddressLine1());
                    currentAddress.put("stateId", String.valueOf(districtService.getStateByStateName(customerAddress.getAddress().getStateProvinceRegion()).getState_id()));
                    currentAddress.put("districtId", String.valueOf(districtService.findDistrictByName(customerAddress.getAddress().getCounty()).getDistrict_id()));
                }
                if (customerAddress.getAddressName().equals("PERMANENT_ADDRESS")) {
                    currentAddress.put("addressName",customerAddress.getAddressName());
                    permanentAddress.put("state", customerAddress.getAddress().getStateProvinceRegion());
                    permanentAddress.put("city", customerAddress.getAddress().getCity());
                    permanentAddress.put("district", customerAddress.getAddress().getCounty());
                    permanentAddress.put("pincode", customerAddress.getAddress().getPostalCode());
                    permanentAddress.put("addressLine", customerAddress.getAddress().getAddressLine1());
                    permanentAddress.put("stateId", String.valueOf(districtService.getStateByStateName(customerAddress.getAddress().getStateProvinceRegion()).getState_id()));
                    permanentAddress.put("districtId", String.valueOf(districtService.findDistrictByName(customerAddress.getAddress().getCounty()).getDistrict_id()));
                }

            }
//            customerDetailsForMobile.put("currentAddress", currentAddress);
//            customerDetailsForMobile.put("permanentAddress", permanentAddress);
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
            customerDetailsForMobile.put("addresses", addresses);

            List<QualificationDetails> qualificationDetails= customCustomer.getQualificationDetailsList();
            List<Map<String, Object>> qualificationsWithNames = mapQualificationsForCustomer(qualificationDetails);
            customerDetailsForMobile.put("qualificationDetails", qualificationsWithNames);

            List<Map<String, Object>> filteredDocuments = new ArrayList<>();

            for (Document document : customCustomer.getDocuments()) {
                if(document.getIsArchived().equals(false))
                {
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
                        String fileUrl = fileService.getFileUrl(documentStorageService.encrypt(document.getFilePath()), request);
                        documentDetails.put("fileUrl", fileUrl);

                        // Get the document type name dynamically without modifying the actual entity
                        String documentTypeName = document.getDocumentType().getDocument_type_name();
                        if ("Others".equalsIgnoreCase(documentTypeName) && document.getOtherDocument() != null) {
                            documentTypeName = document.getOtherDocument(); // Override for response only
                        }

                        // Create a response map for documentType to avoid modifying the original entity
                        Map<String, Object> documentTypeResponse = new HashMap<>();
                        documentTypeResponse.put("document_type_id", document.getDocumentType().getDocument_type_id());
                        documentTypeResponse.put("document_type_name", documentTypeName);
                        documentTypeResponse.put("description", document.getDocumentType().getDescription());
                        documentTypeResponse.put("is_qualification_document", document.getDocumentType().getIs_qualification_document());
                        documentTypeResponse.put("is_issue_date_required", document.getDocumentType().getIs_issue_date_required());
                        documentTypeResponse.put("is_expiration_date_required", document.getDocumentType().getIs_expiration_date_required());
                        documentTypeResponse.put("required_document_types", document.getDocumentType().getRequired_document_types());
                        documentTypeResponse.put("max_document_size", document.getDocumentType().getMax_document_size());
                        documentTypeResponse.put("min_document_size", document.getDocumentType().getMin_document_size());
                        documentTypeResponse.put("sort_order", document.getDocumentType().getSort_order());

                        documentDetails.put("documentType", documentTypeResponse);
                        filteredDocuments.add(documentDetails);
                    }
                }
            }

            if (!filteredDocuments.isEmpty()) {
                customerDetailsForMobile.put("documents", filteredDocuments);
            }

            return customerDetailsForMobile;
        }
        else {
            Map<String, Object> customerDetailsForDesktop = new HashMap<>();
            customerDetailsForDesktop.put("id", customer.getId());
            customerDetailsForDesktop.put("dateCreated", customer.getAuditable().getDateCreated());
            customerDetailsForDesktop.put("createdBy", customer.getAuditable().getCreatedBy());
            customerDetailsForDesktop.put("dateUpdated", customer.getAuditable().getDateUpdated());
            customerDetailsForDesktop.put("updatedBy", customer.getAuditable().getUpdatedBy());
            customerDetailsForDesktop.put("username", customer.getUsername());
            customerDetailsForDesktop.put("password", customer.getPassword());
            customerDetailsForDesktop.put("emailAddress", customer.getEmailAddress());
            customerDetailsForDesktop.put("firstName", customer.getFirstName());
            customerDetailsForDesktop.put("lastName", customer.getLastName());
            customerDetailsForDesktop.put("fullName", customer.getFirstName() + " " + customer.getLastName());
            customerDetailsForDesktop.put("externalId", customer.getExternalId());
            customerDetailsForDesktop.put("challengeQuestion", customer.getChallengeQuestion());
            customerDetailsForDesktop.put("challengeAnswer", customer.getChallengeAnswer());
            customerDetailsForDesktop.put("passwordChangeRequired", customer.isPasswordChangeRequired());
            customerDetailsForDesktop.put("receiveEmail", customer.isReceiveEmail());
            customerDetailsForDesktop.put("registered", customer.isRegistered());
            customerDetailsForDesktop.put("deactivated", customer.isDeactivated());
            customerDetailsForDesktop.put("customerPayments", customer.getCustomerPayments());
            customerDetailsForDesktop.put("taxExemptionCode", customer.getTaxExemptionCode());
            customerDetailsForDesktop.put("unencodedPassword", customer.getUnencodedPassword());
            customerDetailsForDesktop.put("unencodedChallengeAnswer", customer.getUnencodedChallengeAnswer());
            customerDetailsForDesktop.put("anonymous", customer.isAnonymous());
            customerDetailsForDesktop.put("cookied", customer.isCookied());
            customerDetailsForDesktop.put("loggedIn", customer.isLoggedIn());
            customerDetailsForDesktop.put("transientProperties", customer.getTransientProperties());

            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
            Order cart = orderService.findCartForCustomer(customer);
            if (cart != null)
                customerDetailsForDesktop.put("cartId", cart.getId());
            else
                customerDetailsForDesktop.put("cartId", null);
            if(role.equals(Constant.roleServiceProvider)) {
                if (customCustomer.getHidePhoneNumber().equals(false)) {
                    customerDetailsForDesktop.put("mobileNumber", customCustomer.getMobileNumber());
                }
            }
            else
            {
                customerDetailsForDesktop.put("mobileNumber", customCustomer.getMobileNumber());
            }
            customerDetailsForDesktop.put("hideMobileNumber", customCustomer.getHidePhoneNumber());
            customerDetailsForDesktop.put("secondaryMobileNumber", customCustomer.getSecondaryMobileNumber());
            customerDetailsForDesktop.put("whatsappNumber", customCustomer.getWhatsappNumber());
            // List<ServiceProviderEntity>refSp=new ArrayList<>();
            // for(CustomerReferrer customerReferrer:customCustomer.getMyReferrer())
            // {
            //     refSp.add(customerReferrer.getServiceProvider());
            // }
            // customerDetailsForDesktop.put("referres",refSp);
            customerDetailsForDesktop.put("countryCode", customCustomer.getCountryCode());
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
            customerDetailsForDesktop.put("primary_referrer",primaryRef);
            customerDetailsForDesktop.put("referrers",ref);
            customerDetailsForDesktop.put("otp", customCustomer.getOtp());
            customerDetailsForDesktop.put("fathersName", customCustomer.getFathersName());
            customerDetailsForDesktop.put("mothersName", customCustomer.getMothersName());
            customerDetailsForDesktop.put("panNumber", customCustomer.getPanNumber());
            customerDetailsForDesktop.put("nationality", customCustomer.getNationality());
            customerDetailsForDesktop.put("dob", customCustomer.getDob());
            customerDetailsForDesktop.put("gender", customCustomer.getGender());
            customerDetailsForDesktop.put("adharNumber", customCustomer.getAdharNumber());
            customerDetailsForDesktop.put("category", customCustomer.getCategory());
            customerDetailsForDesktop.put("subcategory", customCustomer.getSubcategory());
            customerDetailsForDesktop.put("domicile", customCustomer.getDomicile());
            customerDetailsForDesktop.put("domicileState", customCustomer.getDomicileState());
            customerDetailsForDesktop.put("secondaryEmail", customCustomer.getSecondaryEmail());
            customerDetailsForDesktop.put("category_issue_date", customCustomer.getCategoryIssueDate());

            if(customCustomer.getHeightCms() != null) {
                customerDetailsForDesktop.put("height_cms", customCustomer.getHeightCms().toString());
            }else {
                customerDetailsForDesktop.put("height_cms", customCustomer.getHeightCms());
            }

            if(customCustomer.getWeightKgs() != null) {
                customerDetailsForDesktop.put("weight_kgs", customCustomer.getWeightKgs().toString());
            }else {
                customerDetailsForDesktop.put("weight_kgs", customCustomer.getWeightKgs());
            }

            if(customCustomer.getChestSizeCms() != null) {
                customerDetailsForDesktop.put("chest_size_cms", customCustomer.getChestSizeCms().toString());
            }else {
                customerDetailsForDesktop.put("chest_size_cms", customCustomer.getChestSizeCms());
            }

            if(customCustomer.getShoeSizeInches() != null) {
                customerDetailsForDesktop.put("shoe_size_inches", customCustomer.getShoeSizeInches().toString());
            }else {
                customerDetailsForDesktop.put("shoe_size_inches", customCustomer.getShoeSizeInches());
            }

            if(customCustomer.getWaistSizeCms() != null) {
                customerDetailsForDesktop.put("waist_size_cms", customCustomer.getWaistSizeCms().toString());
            }else {
                customerDetailsForDesktop.put("waist_size_cms", customCustomer.getWaistSizeCms());
            }

            customerDetailsForDesktop.put("can_swim", customCustomer.getCanSwim());
            customerDetailsForDesktop.put("proficiency_in_sports_national_level", customCustomer.getProficiencyInSportsNationalLevel());
            customerDetailsForDesktop.put("first_choice_exam_city", customCustomer.getFirstChoiceExamCity());
            customerDetailsForDesktop.put("second_choice_exam_city", customCustomer.getSecondChoiceExamCity());
            customerDetailsForDesktop.put("third_choice_exam_city", customCustomer.getThirdChoiceExamCity());
            customerDetailsForDesktop.put("mphil_passed", customCustomer.getMphilPassed());
            customerDetailsForDesktop.put("phd_passed", customCustomer.getPhdPassed());
            customerDetailsForDesktop.put("number_of_attempts", customCustomer.getNumberOfAttempts());
            customerDetailsForDesktop.put("category_valid_upto", customCustomer.getCategoryValidUpto());
            customerDetailsForDesktop.put("religion", customCustomer.getReligion());
            customerDetailsForDesktop.put("belongs_to_minority", customCustomer.getBelongsToMinority());
            customerDetailsForDesktop.put("secondary_mobile_number", customCustomer.getSecondaryMobileNumber());
            customerDetailsForDesktop.put("whatsapp_number", customCustomer.getWhatsappNumber());
            customerDetailsForDesktop.put("secondary_email", customCustomer.getSecondaryEmail());
            customerDetailsForDesktop.put("disability_handicapped", customCustomer.getDisability());
            customerDetailsForDesktop.put("disability_type", customCustomer.getDisabilityType());
            customerDetailsForDesktop.put("disability_percentage", customCustomer.getDisabilityPercentage());
            customerDetailsForDesktop.put("is_ex_service_man", customCustomer.getExService());
            customerDetailsForDesktop.put("is_married", customCustomer.getIsMarried());
            customerDetailsForDesktop.put("visible_identification_mark_1", customCustomer.getIdentificationMark1());
            customerDetailsForDesktop.put("visible_identification_mark_2", customCustomer.getIdentificationMark2());
            customerDetailsForDesktop.put("is_ncc_certificate",customCustomer.getIsNccCertificate());
            customerDetailsForDesktop.put("is_nss_certificate",customCustomer.getIsNssCertificate());
            customerDetailsForDesktop.put("ncc_certificate",customCustomer.getNccCertificate());
            customerDetailsForDesktop.put("nss_certificate",customCustomer.getNssCertificate());
            customerDetailsForDesktop.put("created_by_role",customCustomer.getCreatedByRole());
            customerDetailsForDesktop.put("created_by_id",customCustomer.getCreatedById());
            customerDetailsForDesktop.put("modified_by_role",customCustomer.getModifiedByRole());
            customerDetailsForDesktop.put("modified_by_id",customCustomer.getModifiedById());
            customerDetailsForDesktop.put("registered_by_sp",customCustomer.getRegisteredBySp());
            customerDetailsForDesktop.put("interested_in_defence", customCustomer.getInterestedInDefence());
            customerDetailsForDesktop.put("workExperienceScope", customCustomer.getWorkExperienceScopeId());
            customerDetailsForDesktop.put("work_experience", customCustomer.getWorkExperience());
            customerDetailsForDesktop.put("sport_certificate", customCustomer.getSportCertificateId());
            customerDetailsForDesktop.put("isOtherOrStateCategory", customCustomer.getIsOtherOrStateCategory());
            customerDetailsForDesktop.put("otherOrStateCategory",customCustomer.getOtherOrStateCategory());
            customerDetailsForDesktop.put("otherCategoryDateOfIssue",customCustomer.getOtherCategoryDateOfIssue());
            customerDetailsForDesktop.put("otherCategoryValidUpto",customCustomer.getOtherCategoryValidUpto());
            customerDetailsForDesktop.put("isSportsCertificate",customCustomer.getIsSportsCertificate());
            customerDetailsForDesktop.put("domicileIssueDate",customCustomer.getDomicileIssueDate());
            customerDetailsForDesktop.put("domicileValidUpto",customCustomer.getDomicileValidUpto());
            customerDetailsForDesktop.put("isLivePhotoNa",customCustomer.getIsLivePhotoNa());
            customerDetailsForDesktop.put("archived",customCustomer.getArchived());
            customerDetailsForDesktop.put("suspended_or_activated_by_role",customCustomer.getArchivedByRole());
            customerDetailsForDesktop.put("suspended_or_activated_by_id",customCustomer.getArchivedById());
            customerDetailsForDesktop.put("profileComplete",customCustomer.getProfileComplete());

            Map<String, String> currentAddress = new HashMap<>();
            Map<String, String> permanentAddress = new HashMap<>();
            for (CustomerAddress customerAddress : customer.getCustomerAddresses()) {
                if (customerAddress.getAddressName().equals("CURRENT_ADDRESS")) {
                    currentAddress.put("addressName",customerAddress.getAddressName());
                    currentAddress.put("state", customerAddress.getAddress().getStateProvinceRegion());
                    currentAddress.put("city", customerAddress.getAddress().getCity());
                    currentAddress.put("district", customerAddress.getAddress().getCounty());
                    currentAddress.put("pincode", customerAddress.getAddress().getPostalCode());
                    currentAddress.put("addressLine", customerAddress.getAddress().getAddressLine1());
                    currentAddress.put("stateId", String.valueOf(districtService.getStateByStateName(customerAddress.getAddress().getStateProvinceRegion()).getState_id()));
                    currentAddress.put("districtId", String.valueOf(districtService.findDistrictByName(customerAddress.getAddress().getCounty()).getDistrict_id()));
                }
                if (customerAddress.getAddressName().equals("PERMANENT_ADDRESS")) {
                    currentAddress.put("addressName",customerAddress.getAddressName());
                    permanentAddress.put("state", customerAddress.getAddress().getStateProvinceRegion());
                    permanentAddress.put("city", customerAddress.getAddress().getCity());
                    permanentAddress.put("district", customerAddress.getAddress().getCounty());
                    permanentAddress.put("pincode", customerAddress.getAddress().getPostalCode());
                    permanentAddress.put("addressLine", customerAddress.getAddress().getAddressLine1());
                    permanentAddress.put("stateId", String.valueOf(districtService.getStateByStateName(customerAddress.getAddress().getStateProvinceRegion()).getState_id()));
                    permanentAddress.put("districtId", String.valueOf(districtService.findDistrictByName(customerAddress.getAddress().getCounty()).getDistrict_id()));
                }

            }
            customerDetailsForDesktop.put("currentAddress", currentAddress);
            customerDetailsForDesktop.put("permanentAddress", permanentAddress);

        /*customerDetailsForDesktop.put("current_district_id",
                districtService.findDistrictByName(currentAddress.get("district")) != null
                        ? districtService.findDistrictByName(currentAddress.get("district"))
                        : null);

        customerDetailsForDesktop.put("current_state_id",
                districtService.getStateByStateName(currentAddress.get("state")) != null
                        ? districtService.getStateByStateName(currentAddress.get("state"))
                        : null);

        customerDetailsForDesktop.put("permanent_district_id",
                districtService.findDistrictByName(permanentAddress.get("district")) != null
                        ? districtService.findDistrictByName(permanentAddress.get("district"))
                        : null);

        customerDetailsForDesktop.put("permanent_state_id",
                districtService.getStateByStateName(permanentAddress.get("state")) != null
                        ? districtService.getStateByStateName(permanentAddress.get("state"))
                        : null);*/

      /*  customerDetailsForDesktop.put("qualificationDetails",customCustomer.getQualificationDetailsList());
        customerDetailsForDesktop.put("documentList",customCustomer.getDocumentList());
        List<Map<String,Object>>listOfSavedProducts=new ArrayList<>();*/
    /*    if(!customCustomer.getSavedForms().isEmpty()) {
            for (Product product : customCustomer.getSavedForms()) {
                listOfSavedProducts.add(createProductResponseMap(product, null,customCustomer));
            }
        }

        customerDetailsForDesktop.put("savedForms",listOfSavedProducts);*/
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
            customerDetailsForDesktop.put("addresses", addresses);

            List<QualificationDetails> qualificationDetails= customCustomer.getQualificationDetailsList();
            List<Map<String, Object>> qualificationsWithNames = mapQualificationsForCustomer(qualificationDetails);
            customerDetailsForDesktop.put("qualificationDetails", qualificationsWithNames);

            List<Map<String, Object>> filteredDocuments = new ArrayList<>();

            for (Document document : customCustomer.getDocuments()) {
                if (document.getIsArchived().equals(false)) {
                    if (document.getFilePath() != null && document.getDocumentType() != null) {
                        Map<String, Object> documentDetails = new HashMap<>();
                        documentDetails.put("documentId", document.getDocumentId());
                        documentDetails.put("name", document.getName());
                        documentDetails.put("filePath", document.getFilePath());

                        if (document.getIs_qualification_document().equals(true) && document.getQualificationDetails() != null) {
                            documentDetails.put("qualification_detail_id", document.getQualificationDetails().getQualification_detail_id());
                        }

                        if (document.getDocumentValidity() != null) {
                            documentDetails.put("documentValidity", document.getDocumentValidity());
                        }

                        String fileUrl = fileService.getFileUrl(documentStorageService.encrypt(document.getFilePath()), request);
                        System.out.println("heloooooooooooooooooooooooooooooooooooooooooo");
                        documentDetails.put("fileUrl", fileUrl);

                        // Get the document type name dynamically without modifying the actual entity
                        String documentTypeName = document.getDocumentType().getDocument_type_name();
                        if ("Others".equalsIgnoreCase(documentTypeName) && document.getOtherDocument() != null) {
                            documentTypeName = document.getOtherDocument(); // Override for response only
                        }

                        // Create a response map for documentType to avoid modifying the original entity
                        Map<String, Object> documentTypeResponse = new HashMap<>();
                        documentTypeResponse.put("document_type_id", document.getDocumentType().getDocument_type_id());
                        documentTypeResponse.put("document_type_name", documentTypeName);
                        documentTypeResponse.put("description", document.getDocumentType().getDescription());
                        documentTypeResponse.put("is_qualification_document", document.getDocumentType().getIs_qualification_document());
                        documentTypeResponse.put("is_issue_date_required", document.getDocumentType().getIs_issue_date_required());
                        documentTypeResponse.put("is_expiration_date_required", document.getDocumentType().getIs_expiration_date_required());
                        documentTypeResponse.put("required_document_types", document.getDocumentType().getRequired_document_types());
                        documentTypeResponse.put("max_document_size", document.getDocumentType().getMax_document_size());
                        documentTypeResponse.put("min_document_size", document.getDocumentType().getMin_document_size());
                        documentTypeResponse.put("sort_order", document.getDocumentType().getSort_order());

                        documentDetails.put("documentType", documentTypeResponse);
                        filteredDocuments.add(documentDetails);
                    }
                }
            }

            if (!filteredDocuments.isEmpty()) {
                customerDetailsForDesktop.put("documents", filteredDocuments);
            }

            return customerDetailsForDesktop;
        }

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
    public Map<String, Object> serviceProviderDetailsMap(ServiceProviderEntity serviceProvider) throws Exception {
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
        serviceProviderDetails.put("registration_number",serviceProvider.getRegistration_number());
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
            if (document.getIsArchived().equals(false)) {
                if (document.getFilePath() != null && document.getDocumentType() != null) {
                    Map<String, Object> documentDetails = new HashMap<>();
                    documentDetails.put("documentId", document.getDocumentId());
                    documentDetails.put("name", document.getName());
                    documentDetails.put("filePath", document.getFilePath());

                    if (document.getIs_qualification_document().equals(true) && document.getQualificationDetails() != null) {
                        documentDetails.put("qualification_detail_id", document.getQualificationDetails().getQualification_detail_id());
                    }

                    if (document.getDocumentValidity() != null) {
                        documentDetails.put("documentValidity", document.getDocumentValidity());
                    }

                    String fileUrl = fileService.getFileUrl(documentStorageService.encrypt(document.getFilePath()), request);
                    documentDetails.put("fileUrl", fileUrl);

                    // Get the document type name dynamically without modifying the actual entity
                    String documentTypeName = document.getDocumentType().getDocument_type_name();
                    if ("Others".equalsIgnoreCase(documentTypeName) && document.getOtherDocument() != null) {
                        documentTypeName = document.getOtherDocument(); // Override for response only
                    }

                    // Create a response map for documentType to avoid modifying the original entity
                    Map<String, Object> documentTypeResponse = new HashMap<>();
                    documentTypeResponse.put("document_type_id", document.getDocumentType().getDocument_type_id());
                    documentTypeResponse.put("document_type_name", documentTypeName);
                    documentTypeResponse.put("description", document.getDocumentType().getDescription());
                    documentTypeResponse.put("is_qualification_document", document.getDocumentType().getIs_qualification_document());
                    documentTypeResponse.put("is_issue_date_required", document.getDocumentType().getIs_issue_date_required());
                    documentTypeResponse.put("is_expiration_date_required", document.getDocumentType().getIs_expiration_date_required());
                    documentTypeResponse.put("required_document_types", document.getDocumentType().getRequired_document_types());
                    documentTypeResponse.put("max_document_size", document.getDocumentType().getMax_document_size());
                    documentTypeResponse.put("min_document_size", document.getDocumentType().getMin_document_size());
                    documentTypeResponse.put("sort_order", document.getDocumentType().getSort_order());

                    documentDetails.put("documentType", documentTypeResponse);
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
                    Qualification qualification = entityManager.find(Qualification.class, qualificationDetail.getQualification_id());
                    Institution institution =  qualificationDetail.getInstitution();
                    CustomStream customStream = entityManager.find(CustomStream.class, qualificationDetail.getStream_id());
                    BoardUniversity boardUniversity = entityManager.find(BoardUniversity.class, qualificationDetail.getBoard_university_id());

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
                    qualificationInfo.put("highest_qualification_subject_names",qualificationDetail.getHighest_qualification_subject_names());
                    qualificationInfo.put("course_duration_in_months",qualificationDetail.getCourse_duration_in_months());
                    qualificationInfo.put("other_qualification",qualificationDetail.getOther_qualification());
                    qualificationInfo.put("other_stream",qualificationDetail.getOther_stream());
                    qualificationInfo.put("other_board_university",qualificationDetail.getOther_board_university());
                    qualificationInfo.put("other_institution",qualificationDetail.getOther_institution());

                    if (qualification != null) {
                        qualificationInfo.put("qualification_name", qualification.getQualification_name());
                    } else {
                        qualificationInfo.put("qualification_name", "Unknown Qualification");
                    }

                    if (boardUniversity != null) {
                        qualificationInfo.put("board_university_name", boardUniversity.getBoard_university_name());
                    } else {
                        qualificationInfo.put("board_university_name", "Unknown Board University");
                    }

                    // Add stream_name
                    if (customStream != null) {
                        qualificationInfo.put("stream_name", customStream.getStreamName());
                    } else {
                        qualificationInfo.put("stream_name", "Unknown Stream");
                    }
//

                    // Add institution_name
                    if (institution != null) {
                        qualificationInfo.put("institution_name", institution.getInstitution_name());
                    } else {
                        qualificationInfo.put("institution_name", "Unknown Institution");
                    }

                    // Add subjects
                    List<Map<String, Object>> subjects = new ArrayList<>();

                    int otherSubjectIndex = 0; // Track index for other subjects
                    for (Long subjectId : qualificationDetail.getSubject_ids()) {
                        CustomSubject subject = entityManager.find(CustomSubject.class, subjectId);

                        if (subject != null) {
                            if ("others".equalsIgnoreCase(subject.getSubjectName()) && !qualificationDetail.getOtherSubjects().isEmpty()) {
                                // Iterate through all other subjects and add them separately
                                Map<String, Object> subjectInfo = new HashMap<>();
                                subjectInfo.put("subject_id", subjectId);
                                subjectInfo.put("subject_name", qualificationDetail.getOtherSubjects().get(otherSubjectIndex));
                                subjects.add(subjectInfo);
                                otherSubjectIndex++;
                            } else {
                                Map<String, Object> subjectInfo = new HashMap<>();
                                subjectInfo.put("subject_id", subject.getSubjectId());
                                subjectInfo.put("subject_name", subject.getSubjectName());
                                subjects.add(subjectInfo);
                            }
                        } else {
                            Map<String, Object> subjectInfo = new HashMap<>();
                            subjectInfo.put("subject_id", subjectId);
                            subjectInfo.put("subject_name", "Unknown Subject");
                            subjects.add(subjectInfo);
                        }
                    }

                    qualificationInfo.put("subjects", subjects);
                    List<SubjectDetail> subjectDetails = new ArrayList<>();
                    int otherIndex = 0;

                    List<SubjectDetail> sortedSubjectDetails = new ArrayList<>(qualificationDetail.getSubject_details());
                    sortedSubjectDetails.sort(Comparator.comparing(SubjectDetail::getSubject_detail_id));

                    for (SubjectDetail detail : sortedSubjectDetails) {
                        // Create a new instance of CustomSubject and copy fields manually
                        CustomSubject tempSubject = new CustomSubject();
                        tempSubject.setSubjectId(detail.getCustomSubject().getSubjectId()); // Copy ID
                        tempSubject.setSubjectName(detail.getCustomSubject().getSubjectName()); // Copy name

                        if (tempSubject.getSubjectName().equalsIgnoreCase("Others")) {
                            tempSubject.setSubjectName(qualificationDetail.getOtherSubjects().get(otherIndex));
                            otherIndex++;
                        }

                        tempSubject.setArchived(detail.getCustomSubject().getArchived());
                        tempSubject.setCreatedDate(detail.getCustomSubject().getCreatedDate());
                        tempSubject.setCreatorRole(detail.getCustomSubject().getCreatorRole());
                        tempSubject.setCreatorUserId(detail.getCustomSubject().getCreatorUserId());
                        tempSubject.setSubjectDescription(detail.getCustomSubject().getSubjectDescription());

                        // Create a new instance of SubjectDetail and copy fields manually
                        SubjectDetail tempDetail = new SubjectDetail();
                        tempDetail.setSubject_detail_id(detail.getSubject_detail_id());
                        tempDetail.setCustomSubject(tempSubject);
                        tempDetail.setSubject_marks_obtained(detail.getSubject_marks_obtained());
                        tempDetail.setSubject_total_marks(detail.getSubject_total_marks());
                        tempDetail.setSubject_grade(detail.getSubject_grade());
                        tempDetail.setSubject_equivalent_percentage(detail.getSubject_equivalent_percentage());
                        tempDetail.setSubject_marks_type(detail.getSubject_marks_type());

                        subjectDetails.add(tempDetail);
                    }

                    qualificationInfo.put("subject_details", subjectDetails);
                    qualificationInfo.put("otherSubjects",qualificationDetail.getOtherSubjects());

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
                    Institution institution =  qualificationDetail.getInstitution();
                    CustomStream customStream = entityManager.find(CustomStream.class, qualificationDetail.getStream_id());
                    BoardUniversity boardUniversity = entityManager.find(BoardUniversity.class, qualificationDetail.getBoard_university_id());
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
                    qualificationInfo.put("other_qualification",qualificationDetail.getOther_qualification());
                    qualificationInfo.put("other_stream",qualificationDetail.getOther_stream());
                    qualificationInfo.put("other_board_university",qualificationDetail.getOther_board_university());
                    qualificationInfo.put("other_institution",qualificationDetail.getOther_institution());

                    if (qualification != null) {
                        qualificationInfo.put("qualification_name", qualification.getQualification_name());
                    } else {
                        qualificationInfo.put("qualification_name", "Unknown Qualification");
                    }

                    if (boardUniversity != null) {
                        qualificationInfo.put("board_university_name", boardUniversity.getBoard_university_name());
                    } else {
                        qualificationInfo.put("board_university_name", "Unknown Board University");
                    }

                    // Add stream_name
                    if (customStream != null) {
                        qualificationInfo.put("stream_name", customStream.getStreamName());
                    } else {
                        qualificationInfo.put("stream_name", "Unknown Stream");
                    }

                    if (institution != null) {
                        qualificationInfo.put("institution_name", institution.getInstitution_name());
                    }else {
                        qualificationInfo.put("institution_name", "Unknown Institution");
                    }
                    qualificationInfo.put("otherSubjects",qualificationDetail.getOtherSubjects());
                    qualificationInfo.put("highest_qualification_subject_names",qualificationDetail.getHighest_qualification_subject_names());
                    qualificationInfo.put("course_duration_in_months",qualificationDetail.getCourse_duration_in_months());

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
    public  List<CustomerBasicDetailsDto> getPaginatedList(List<CustomerBasicDetailsDto> fullList, int page, int pageSize) {
        int fromIndex = (page) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize,fullList.size());

        if (fromIndex >= fullList.size()) {
            return List.of(); // Return empty list if page is out of bounds
        }

        return fullList.subList(fromIndex, toIndex);
    }
    public int calculateAge(String birthDateString) {
        if (birthDateString == null || birthDateString.isEmpty()) {
            return -1;  // Handle null/empty case
        }

        try {
            // Attempt to parse the date using the given format
            LocalDate birthDate = LocalDate.parse(birthDateString, DateTimeFormatter.ofPattern("dd-MM-yyyy"));

            // Return age in years
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (DateTimeParseException e) {
            // If the format is incorrect or parsing fails, return -1
            return -1;
        }
    }
    @Transactional
    public void blackListToken(String token,Integer roleId,Long userId)
    {

        BlackListedTokens blackListedTokens=new BlackListedTokens();
        blackListedTokens.setBlackListToken(token);
        blackListedTokens.setBlackListDate(LocalDate.now());
        blackListedTokens.setRoleId(roleId);
        blackListedTokens.setUserId(userId);
        entityManager.persist(blackListedTokens);
    }
    public boolean isBlackListed(String token)
    {
        Query query=entityManager.createNativeQuery("SELECT count(*) from black_listed_tokens where blacklisttoken = :token");
        query.setParameter("token",token);
        if(((BigInteger)query.getSingleResult()).intValue()!=0)
            return true;
        return false;
    }
    public void removeToken(String token) {
        Query query = entityManager.createNativeQuery(
                "DELETE FROM black_listed_tokens WHERE blacklisttoken = CAST(:token AS TEXT)"
        );
        query.setParameter("token", token);
        query.executeUpdate();
    }

    public boolean validateCustomerContactDetails(CustomCustomer customCustomer)
    {
        List<CustomerAddress> addresses=customCustomer.getCustomerAddresses();
        if(addresses==null || addresses.isEmpty())
        {
            customCustomer.setProfileComplete(false);
            throw new IllegalArgumentException("In Contact Details, Address cannot be null or empty");
        }
        if(addresses.size()<2)
        {
            customCustomer.setProfileComplete(false);
            throw new IllegalArgumentException("Both current as well as Permanent address should be provided");
        }
       for(CustomerAddress customerAddress: addresses)
       {
           String addressName=null;
           if(customerAddress.getAddressName().equalsIgnoreCase("CURRENT_ADDRESS"))
           {
               addressName="Present Address";
           }
           else if(customerAddress.getAddressName().equalsIgnoreCase("PERMANENT_ADDRESS"))
           {
               addressName="Permanent Address";
           }
               if(customerAddress.getAddressName().equalsIgnoreCase("CURRENT_ADDRESS"))
               {
               if(customerAddress.getAddress().getAddressLine1()==null || (customerAddress.getAddress().getAddressLine1()!=null && customerAddress.getAddress().getAddressLine1().trim().isEmpty()))
               {
                   customCustomer.setProfileComplete(false);
                   throw new IllegalArgumentException("In Contact Details, "+ addressName+ " cannot be null or empty");
               }
               if(customerAddress.getAddress().getCity()==null || (customerAddress.getAddress().getCity()!=null && customerAddress.getAddress().getCity().trim().isEmpty()))
               {
                   customCustomer.setProfileComplete(false);
                   throw new IllegalArgumentException("In Contact Details, City cannot be null or empty in "+ addressName);
               }
               if(customerAddress.getAddress().getCounty()==null || (customerAddress.getAddress().getCounty()!=null && customerAddress.getAddress().getCounty().trim().isEmpty()))
               {
                   customCustomer.setProfileComplete(false);
                   throw new IllegalArgumentException("In Contact Details, District cannot be null or empty in "+ addressName);
               }
               if(customerAddress.getAddress().getStateProvinceRegion()==null || (customerAddress.getAddress().getStateProvinceRegion()!=null && customerAddress.getAddress().getStateProvinceRegion().trim().isEmpty()))
               {
                   customCustomer.setProfileComplete(false);
                   throw new IllegalArgumentException("In Contact Details, State cannot be null or empty in "+ addressName);
               }
               if(customerAddress.getAddress().getPostalCode()==null || (customerAddress.getAddress().getPostalCode()!=null && customerAddress.getAddress().getPostalCode().trim().isEmpty()))
               {
                   customCustomer.setProfileComplete(false);
                   throw new IllegalArgumentException("In Contact Details, Pin code cannot be null or empty in "+ addressName);
               }
           }

       }
        if(customCustomer.getMobileNumber()==null || (customCustomer.getMobileNumber()!=null &&customCustomer.getMobileNumber().trim().isEmpty()))
        {
            customCustomer.setProfileComplete(false);
            throw new IllegalArgumentException("In Contact Details, Primary mobile number cannot be null or empty");
        }
        if(customCustomer.getSecondaryMobileNumber()==null || (customCustomer.getSecondaryMobileNumber()!=null &&customCustomer.getSecondaryMobileNumber().trim().isEmpty()))
        {
            customCustomer.setProfileComplete(false);
            throw new IllegalArgumentException("In Contact Details, Secondary mobile number cannot be null or empty");
        }
        if(customCustomer.getWhatsappNumber()==null || (customCustomer.getWhatsappNumber()!=null &&customCustomer.getWhatsappNumber().trim().isEmpty()))
        {
            customCustomer.setProfileComplete(false);
            throw new IllegalArgumentException("In Contact Details, Whatsapp number cannot be null or empty");
        }
        if(customCustomer.getEmailAddress()==null || (customCustomer.getEmailAddress()!=null &&customCustomer.getEmailAddress().trim().isEmpty()))
        {
            customCustomer.setProfileComplete(false);
            throw new IllegalArgumentException("In Contact Details, Primary Email address cannot be null or empty");
        }
        return true;
    }

    public boolean validateCustomerPersonalDetails(CustomCustomer customCustomer)
    {
        if(customCustomer.getFirstName()==null || (customCustomer.getFirstName()!=null &&customCustomer.getFirstName().trim().isEmpty()))
        {
            throw new IllegalArgumentException("In Personal Details, First name cannot be null or empty");
        }
        if(customCustomer.getLastName()==null || (customCustomer.getLastName()!=null &&customCustomer.getLastName().trim().isEmpty()))
        {
            throw new IllegalArgumentException("In Personal Details, Last name cannot be null or empty");
        }
        if(customCustomer.getFathersName()==null || (customCustomer.getFathersName()!=null &&customCustomer.getFathersName().trim().isEmpty()))
        {
            throw new IllegalArgumentException("In Personal Details, Father's name cannot be null or empty");
        }
        if(customCustomer.getMothersName()==null || (customCustomer.getMothersName()!=null &&customCustomer.getMothersName().trim().isEmpty()))
        {
            throw new IllegalArgumentException("In Personal Details, Mother's name cannot be null or empty");
        }
        if(customCustomer.getAdharNumber()==null || (customCustomer.getAdharNumber()!=null &&customCustomer.getAdharNumber().trim().isEmpty()))
        {
            throw new IllegalArgumentException("In Personal Details, Aadhaar number cannot be null or empty");
        }
        if(customCustomer.getDob()==null || (customCustomer.getDob()!=null &&customCustomer.getDob().trim().isEmpty()))
        {
            throw new IllegalArgumentException("In Personal Details, Date of birth cannot be null or empty");
        }
        if(customCustomer.getReligion()==null || (customCustomer.getReligion()!=null &&customCustomer.getReligion().trim().isEmpty()))
        {
            throw new IllegalArgumentException("In Personal Details, Religion cannot be null or empty");
        }
        if(customCustomer.getGender()==null || (customCustomer.getGender()!=null &&customCustomer.getGender().trim().isEmpty()))
        {
            throw new IllegalArgumentException("In Personal Details, Gender cannot be null or empty");
        }
        if(customCustomer.getCategory()==null || (customCustomer.getCategory()!=null &&customCustomer.getCategory().trim().isEmpty()))
        {
            throw new IllegalArgumentException("In Personal Details, Category cannot be null or empty");
        }
        if(customCustomer.getIsLivePhotoNa()==null)
        {
            throw new IllegalArgumentException("You have to select whether you will upload live photo or not ");
        }
        if(!customCustomer.getCategory().equalsIgnoreCase("GEN") && (customCustomer.getCategoryIssueDate()==null || (customCustomer.getCategoryIssueDate()!=null &&customCustomer.getCategoryIssueDate().trim().isEmpty())))
        {
            throw new IllegalArgumentException("In Personal Details, Category issue date cannot be null or empty");
        }
        if(customCustomer.getIsOtherOrStateCategory()==null)
        {
            throw new IllegalArgumentException("In Personal Details,You have to select whether isOtherOrStateCategory is true or false ");
        }
        if(customCustomer.getIsOtherOrStateCategory().equals(true))
        {
            if(customCustomer.getOtherCategoryDateOfIssue()==null)
            {
                throw new IllegalArgumentException("In Personal Details, Other category's issue date cannot be null or empty if getIsOtherOrStateCategory is true");
            } if(customCustomer.getOtherOrStateCategory()==null || (customCustomer.getOtherOrStateCategory()!=null &&customCustomer.getOtherOrStateCategory().trim().isEmpty()))
        {
            throw new IllegalArgumentException("In Personal Details, Other or State category cannot be null or empty if getIsOtherOrStateCategory is true");
        }
        }

        if(customCustomer.getBelongsToMinority()==null)
        {
            throw new IllegalArgumentException("In Personal Details,You have to select whether isMinority is true or false ");
        }

        if(customCustomer.getDomicile()==null)
        {
            throw new IllegalArgumentException("In Personal Details,You have to select whether state domicile is true or false ");
        }

        if(customCustomer.getDomicile().equals(true))
        {
            if(customCustomer.getDomicileState()==null)
            {
                throw new IllegalArgumentException("In Personal Details, state cannot be null or empty if state domicile is true");
            }
            if(customCustomer.getDomicileIssueDate()==null )
            {
                throw new IllegalArgumentException("In Personal Details, state cannot be null or empty if domicile date of issue is true");
            }
        }
        if(customCustomer.getDisability()==null)
        {
            throw new IllegalArgumentException("In Personal Details,You have to select whether isDisability is true or false ");
        }
        if(customCustomer.getExService()==null)
        {
            throw new IllegalArgumentException("In Personal Details,You have to select whether ex- service men is true or false ");
        }
        if(customCustomer.getIsMarried()==null)
        {
            throw new IllegalArgumentException("In Personal Details,You have to select whether you are married or not ");
        }
        if(customCustomer.getIdentificationMark1()==null || (customCustomer.getIdentificationMark1()!=null &&customCustomer.getIdentificationMark1().trim().isEmpty()))
        {
            throw new IllegalArgumentException("In Personal Details, Identification mark 1 cannot be null or empty");
        }
        if(customCustomer.getIdentificationMark2()==null || (customCustomer.getIdentificationMark2()!=null &&customCustomer.getIdentificationMark2().trim().isEmpty()))
        {
            throw new IllegalArgumentException("In Personal Details, Identification mark 2 cannot be null or empty");
        }
        return true;
    }

    public boolean validatePhysicalDetails(CustomCustomer customCustomer)
    {

        if(customCustomer.getInterestedInDefence()==null)
        {
            throw new IllegalArgumentException("In Physical Details, You have to select whether you are interested in defence or not");
        }
        if(customCustomer.getInterestedInDefence().equals(true))
        {
            if(customCustomer.getHeightCms()==null)
            {
                throw new IllegalArgumentException("In Physical Details, Height cannot be null or empty");
            }
            if(customCustomer.getWeightKgs()==null)
            {
                throw new IllegalArgumentException("In Physical Details, Weight cannot be null or empty");
            }
            if(customCustomer.getShoeSizeInches()==null)
            {
                throw new IllegalArgumentException("In Physical Details, Shoe size cannot be null or empty");
            }
            if(customCustomer.getWaistSizeCms()==null)
            {
                throw new IllegalArgumentException("In Physical Details, Waist size cannot be null or empty");
            }
            if(customCustomer.getGender().equalsIgnoreCase("male"))
            {
                if(customCustomer.getChestSizeCms()==null)
                {
                    throw new IllegalArgumentException("In Physical Details, Chest size cannot be null or empty");
                }
            }
        }
        if(customCustomer.getProficiencyInSportsNationalLevel()==null )
        {
            throw new IllegalArgumentException("In Physical Details, you have to select whether proficiency in sports at national level or not");
        }
        if(customCustomer.getCanSwim()==null )
        {
            throw new IllegalArgumentException("In Physical Details, you have to select whether you can swim or not");
        }
        if(customCustomer.getIsNccCertificate()==null)
        {
            throw new IllegalArgumentException("In Physical Details, you have to select whether you have ncc certificate or not");
        }
        if(customCustomer.getIsNssCertificate()==null)
        {
            throw new IllegalArgumentException("In Physical Details, you have to select whether you have nss certificate or not");
        }
        if(customCustomer.getIsSportsCertificate()==null)
        {
            throw new IllegalArgumentException("In Physical Details, you have to select whether you have sports certificate or not");
        }
        return true;
    }
    public boolean validateMiscellaniousDetails(CustomCustomer customCustomer)
    {
        if(customCustomer.getMphilPassed()==null)
        {
            throw new IllegalArgumentException("In Miscellaneous Details, you have to select whether you are MPhil passed or not");
        }
        if(customCustomer.getPhdPassed()==null)
        {
            throw new IllegalArgumentException("In Miscellaneous Details, you have to select whether you are Phd passed or not");
        }
        if(customCustomer.getWorkExperience()!=null)
        {
            if(customCustomer.getWorkExperienceScopeId()==null)
            {
                throw new IllegalArgumentException("In Miscellaneous Details, you have to select work experience scope");
            }
        }
        return true;
    }

    public boolean validateDocumentsDetails(CustomCustomer customCustomer)
    {
        List<Document>documents= customCustomer.getDocuments();
        List<String> documentsNotUploaded= new ArrayList<>();
        boolean isLivePhotoCaptured=false;
        boolean isAadharCardFrontUploaded=false;
        boolean isAadharCardBackUploaded=false;
        boolean isMinority=false;
        boolean isOtherCategory=false;
        boolean isDomicile=false;
        boolean isDisability=false;
        boolean isExService=false;
        boolean isCategoryCertificate=false;
        boolean isPersonalPhoto=false;
        boolean isSignature=false;
        boolean isRightThumb=false;
        boolean isLeftThumb=false;
        boolean isNcc=false;
        boolean isNss=false;
        boolean isSports=false;
        boolean isQualification=false;
        if(documents==null)
        {
            throw new IllegalArgumentException("Aadhaar card- Front and Back , Personal Photograph, Signature,Live Passport size photograph, left thumb and right thumb impressions is necessary to upload");
        }
        int countQualificationDocuments=0;
        for(Document document: documents)
        {
            if(document.getDocumentType().getDocument_type_id().equals(3)&& !document.getIsArchived())
            {
                isLivePhotoCaptured=true;
            }
            if(document.getDocumentType().getDocument_type_id().equals(1)&& !document.getIsArchived())
            {
                isAadharCardFrontUploaded=true;
            }
            if(document.getDocumentType().getDocument_type_id().equals(24)&& !document.getIsArchived())
            {
                isAadharCardBackUploaded=true;
            }
            if(document.getDocumentType().getDocument_type_id().equals(6)&& !document.getIsArchived())
            {
                isCategoryCertificate=true;
            }
            if(document.getDocumentType().getDocument_type_id().equals(17)&& !document.getIsArchived())
            {
                isPersonalPhoto=true;
            }
            if(document.getDocumentType().getDocument_type_id().equals(4)&& !document.getIsArchived())
            {
                isSignature=true;
            }
            if(document.getDocumentType().getDocument_type_id().equals(25)&& !document.getIsArchived())
            {
                isLeftThumb=true;
            } if(document.getDocumentType().getDocument_type_id().equals(26)&& !document.getIsArchived())
            {
                isRightThumb=true;
            }
            if(customCustomer.getBelongsToMinority().equals(true))
            {
                if(document.getDocumentType().getDocument_type_id().equals(31)&& !document.getIsArchived())
                {
                    isMinority=true;
                }
            }
            if(customCustomer.getIsOtherOrStateCategory().equals(true))
            {
                if(document.getDocumentType().getDocument_type_id().equals(30)&& !document.getIsArchived())
                {
                    isOtherCategory=true;
                }
            }
            if(customCustomer.getDomicile().equals(true))
            {
                if(document.getDocumentType().getDocument_type_id().equals(10)&& !document.getIsArchived())
                {
                    isDomicile=true;
                }
            }
            if(customCustomer.getDisability().equals(true))
            {
                if(document.getDocumentType().getDocument_type_id().equals(11)&& !document.getIsArchived())
                {
                    isDisability=true;
                }
            }
            if(customCustomer.getExService().equals(true))
            {
                if(document.getDocumentType().getDocument_type_id().equals(15)&& !document.getIsArchived())
                {
                    isExService=true;
                }
            }
            if(customCustomer.getIsNccCertificate().equals(true))
            {
                if((document.getDocumentType().getDocument_type_id().equals(18) ||document.getDocumentType().getDocument_type_id().equals(19)||document.getDocumentType().getDocument_type_id().equals(20))&& !document.getIsArchived())
                {
                    isNcc=true;
                }
            }
            if(customCustomer.getIsNssCertificate().equals(true))
            {
                if((document.getDocumentType().getDocument_type_id().equals(21) ||document.getDocumentType().getDocument_type_id().equals(28)||document.getDocumentType().getDocument_type_id().equals(29)) && !document.getIsArchived())
                {
                    isNss=true;
                }
            }
            if(customCustomer.getIsNssCertificate().equals(true))
            {
                if((document.getDocumentType().getDocument_type_id().equals(22) ||document.getDocumentType().getDocument_type_id().equals(23)) && !document.getIsArchived())
                {
                    isSports=true;
                }
            }

            //get all qualifications of customer
            List<QualificationDetails> qualificationDetails= customCustomer.getQualificationDetailsList();
            if(qualificationDetails!=null && !qualificationDetails.isEmpty())
            {
                if(customCustomer.getDocuments()!=null)
                {
                    if(document.getDocumentType().getDocument_type_id().equals(12) && document.getIsArchived().equals(false))
                    {
                        countQualificationDocuments++;
                    }
                    if(countQualificationDocuments==customCustomer.getQualificationDetailsList().size())
                    {
                        isQualification=true;
                    }
                }
            }

        }

        //Validation for personal Photo
        if(!isPersonalPhoto)
        {
            documentsNotUploaded.add("Personal Photo");
        }

        if(customCustomer.getIsLivePhotoNa().equals(false)) {
            if (!isLivePhotoCaptured) {
                documentsNotUploaded.add("Live Photograph");
            }
        }

        if(!isSignature)
        {
            documentsNotUploaded.add("Signature");
        }

        if(!isAadharCardFrontUploaded)
        {
            documentsNotUploaded.add("Front Aadhaar card");
        }
        if(!isAadharCardBackUploaded)
        {
            documentsNotUploaded.add("Back Aadhaar card");
        }

        if(customCustomer.getCategory()!=null && !customCustomer.getCategory().equalsIgnoreCase("GEN"))
        {
            if(!isCategoryCertificate)
            {
                documentsNotUploaded.add("Category Certificate");
            }
        }
        if(customCustomer.getBelongsToMinority().equals(true))
        {
            if(!isMinority)
            {
                documentsNotUploaded.add("Minority certificate");
            }
        }
        if(customCustomer.getIsOtherOrStateCategory().equals(true))
        {
            if(!isOtherCategory)
            {
                documentsNotUploaded.add("Other or State category certificate");
            }
        }
        if(customCustomer.getDomicile().equals(true))
        {
            if(!isDomicile)
            {
                documentsNotUploaded.add("Domicile certificate");
            }
        }
        if(customCustomer.getDisability().equals(true))
        {
            if(!isDisability)
            {
                documentsNotUploaded.add("Disability certificate");
            }
        }
        if(customCustomer.getExService().equals(true))
        {
            if(!isExService)
            {
                documentsNotUploaded.add("Ex service certificate");
            }
        }
        if(customCustomer.getIsNccCertificate().equals(true))
        {
            if(!isNcc)
            {
                documentsNotUploaded.add("NCC certificate");
            }
        }
        if(customCustomer.getIsNssCertificate().equals(true))
        {
            if(!isNss)
            {
                documentsNotUploaded.add("NSS certificate");
            }
        }
        if(customCustomer.getIsSportsCertificate().equals(true))
        {
            if(!isSports)
            {
                documentsNotUploaded.add("Sports certificate");
            }
        }
        if(customCustomer.getQualificationDetailsList()!=null && !customCustomer.getQualificationDetailsList().isEmpty())
        {
            if(!isQualification)
            {
                documentsNotUploaded.add("Qualification certificates");
            }
        }

        if(!documentsNotUploaded.isEmpty())
        {
            String ans="";
            for (int i=0;i<documentsNotUploaded.size();i++)
            {
                ans=ans+documentsNotUploaded.get(i)+", ";
            }
            throw new IllegalArgumentException("In document upload section, "+ans+ " is not uploaded");
        }
        return true;
    }

}


