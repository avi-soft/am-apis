package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.DocumentValidity;
import com.community.api.entity.QualificationDetails;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.Document;
import com.community.api.utils.DocumentType;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.File;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import static com.community.api.component.Constant.DOCUMENT_TYPE_LIVE_PHOTOGRAPH_ID;
import static com.community.api.component.Constant.request;

@Slf4j
@Service
public class CustomCustomerService {
    @Autowired
    SharedUtilityService sharedUtilityService;
    @Autowired
    JwtUtil jwtTokenUtil;
    private EntityManager em;
    @Autowired
    private DocumentStorageService fileUploadService;
    @Autowired
    private ExceptionHandlingService exceptionHandlingService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private FileService fileService;
    @Autowired
    private DocumentStorageService documentStorageService;

    public CustomCustomerService(EntityManager em) {
        this.em = em;
    }

    public static Date convertStringToDate(String dateStr, String s) throws ParseException {
        if (dateStr == null || dateStr.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat.setLenient(false);
        return dateFormat.parse(dateStr);
    }

    public Boolean validateInput(CustomCustomer customer) {
        if (customer.getUsername().isEmpty() || customer.getUsername() == null || customer.getMobileNumber().isEmpty() || customer.getMobileNumber() == null || customer.getPassword() == null || customer.getPassword().isEmpty())
            return false;
        if (!isValidMobileNumber(customer.getMobileNumber()))
            return false;

        return true;
    }

    public boolean isValidMobileNumber(String mobileNumber) {

        // If the mobile number is empty, return true (valid).
        if (mobileNumber == null || mobileNumber.isEmpty()) {
            return true;
        }

        // Remove leading "0" if present.
        if (mobileNumber.startsWith("0")) {
            mobileNumber = mobileNumber.substring(1);
        }

        String mobileNumberPattern = "^\\d{9,13}$";
        return Pattern.compile(mobileNumberPattern).matcher(mobileNumber).matches();
    }

    public CustomCustomer findCustomCustomerByPhone(String mobileNumber, String countryCode) {

        if (countryCode == null) {
            countryCode = Constant.COUNTRY_CODE;
        }

        return em.createQuery(Constant.PHONE_QUERY, CustomCustomer.class)
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("countryCode", countryCode)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public CustomCustomer findCustomCustomerById(Long customerId) {
        // Check if customerId is valid
        if (customerId == null) {
            return null;
        }

        return em.createQuery("SELECT c FROM CustomCustomer c WHERE c.id = :customerId", CustomCustomer.class)
                .setParameter("customerId", customerId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public CustomCustomer findCustomCustomerByPhoneWithOtp(String mobileNumber, String countryCode) {

        if (countryCode == null) {
            countryCode = Constant.COUNTRY_CODE;
        }

        return em.createQuery(Constant.PHONE_QUERY_OTP, CustomCustomer.class)
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("countryCode", countryCode)
//                .setParameter("otp", null)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public Map<String, String> validateAddress(String addressLine, String city, String pincode) {
        Map<String, String> errorMessages = new HashMap<>();

        // Validate Address Line: It should not be empty or null
        if (addressLine == null || addressLine.trim().isEmpty()) {
            errorMessages.put("currentAddress", "Address Line cannot be empty.");
        }

        // Validate City: It should only contain alphabets and possibly spaces
        if (city == null || !Pattern.matches("^[a-zA-Z\\s]+$", city)) {
            errorMessages.put("currentCity", "City name should only contain alphabets and spaces.");
        }

        // Validate Pincode: It should be a 6-digit number where the first digit is not zero
        if (pincode == null || !Pattern.matches("^[1-9][0-9]{5}$", pincode)) {
            errorMessages.put("currentPincode", "Pincode should be a 6-digit number starting with a digit from 1 to 9.");
        }

        // Return the list of error messages (if any)
        return errorMessages;
    }

    public List<BigInteger> filterCustomer(List<Long> service_provider_id, List<String> first_names, List<String> last_names, List<String> sub_state_prov_reg, List<String> county, List<Integer> qualification_name, String username, Boolean completed, String authHeader, int page, int limit, String sort) {
        List<Map<String, Object>> response = new ArrayList<>();
        int startPosition = page * limit;
        String jwtToken = authHeader.substring(7);

        if (username != null && !username.isEmpty()) {
            Query query = em.createNativeQuery("SELECT customer_id FROM blc_customer WHERE user_name = :username");
            query.setParameter("username", username);
            return query.getResultList();
        }

        Map<String, String> alias = new HashMap<>();
        Map<String, String> aliasQuery = new HashMap<>();

        aliasQuery.put("sub_state_prov_reg", "JOIN blc_customer_address cust_addr ON cust.customer_id = cust_addr.customer_id JOIN blc_address addr ON cust_addr.address_id = addr.address_id ");
//        aliasQuery.put("overlapping", "JOIN qualification_details qual_details ON qual_details.custom_customer_id = cust.customer_id JOIN qualification qual ON qual_details.qualification_id = qual.qualification_id ");
        aliasQuery.put("qualification_id", "JOIN qualification_details qual_details ON qual_details.custom_customer_id = cust.customer_id JOIN qualification qual ON qual_details.qualification_id = qual.qualification_id ");
        aliasQuery.put("service_provider_id", "JOIN customer_referrer referrer ON cust.customer_id = referrer.customer_id ");
        aliasQuery.put("profile_completed", "JOIN custom_customer cc ON cust.customer_id = cc.customer_id ");
        alias.put("sub_state_prov_reg", "addr");
        alias.put("county", "addr");
        alias.put("first_name", "cust");
        alias.put("last_name", "cust");
        alias.put("service_provider_id", "referrer");
//        alias.put("overlapping", "qual");
        alias.put("qualification_id", "qual_details");
        alias.put("profile_completed", "cc");

        String generalizedQuery = Constant.CUSTOMER_FILTER;
        if ((county != null && !county.isEmpty()) || (sub_state_prov_reg != null && !sub_state_prov_reg.isEmpty())) {
            generalizedQuery += aliasQuery.get("sub_state_prov_reg");
        }
//        if (qualification_name != null && !qualification_name.isEmpty()) {
//            generalizedQuery += aliasQuery.get("overlapping");
//        }
        if (qualification_name != null && !qualification_name.isEmpty()) {
            generalizedQuery += aliasQuery.get("qualification_id");
        }
        if (service_provider_id != null) {
            generalizedQuery += aliasQuery.get("service_provider_id");
        }
        if (completed != null) {
            generalizedQuery += aliasQuery.get("profile_completed");
        }

        generalizedQuery += " WHERE ";

        String[] fieldsNames = {"sub_state_prov_reg", "county", "service_provider_id", "qualification_id", "profile_completed"};
        Object[] fields = {sub_state_prov_reg, county, service_provider_id, qualification_name, completed};

        for (int i = 0; i < fields.length; i++) {
            if (fields[i] != null) {
                generalizedQuery += alias.get(fieldsNames[i]) + "." + fieldsNames[i] + " IN (:" + fieldsNames[i] + ") AND ";
            }
        }

        // Handle first_names with OR conditions for partial matching
        if (first_names != null && !first_names.isEmpty()) {
            generalizedQuery += "(";
            for (int j = 0; j < first_names.size(); j++) {
                if (j > 0) generalizedQuery += " OR ";
                generalizedQuery += "LOWER(cust.first_name) LIKE LOWER(:first_name" + j + ") || '%'";
            }
            generalizedQuery += ") AND ";
        }

        // Handle last_names with OR conditions for partial matching
        if (last_names != null && !last_names.isEmpty()) {
            generalizedQuery += "(";
            for (int j = 0; j < last_names.size(); j++) {
                if (j > 0) generalizedQuery += " OR ";
                generalizedQuery += "LOWER(cust.last_name) LIKE LOWER(:last_name" + j + ") || '%'";
            }
            generalizedQuery += ") AND ";
        }

        generalizedQuery = generalizedQuery.trim();
        int lastSpaceIndex = generalizedQuery.lastIndexOf(" ");
        generalizedQuery = generalizedQuery.substring(0, lastSpaceIndex) + " ORDER by cust.date_updated " + sort;

        System.out.println(generalizedQuery);

        Query query = em.createNativeQuery(generalizedQuery);

        for (int i = 0; i < fields.length; i++) {
            if (fields[i] != null) {
                System.out.println(fieldsNames[i]);
                System.out.println(fields[i]);
                query.setParameter(fieldsNames[i], fields[i]);
            }
        }

        // Set parameters for first_names
        if (first_names != null && !first_names.isEmpty()) {
            for (int j = 0; j < first_names.size(); j++) {
                query.setParameter("first_name" + j, first_names.get(j));
            }
        }

        // Set parameters for last_names
        if (last_names != null && !last_names.isEmpty()) {
            for (int j = 0; j < last_names.size(); j++) {
                query.setParameter("last_name" + j, last_names.get(j));
            }
        }

    /* query.setFirstResult(startPosition);
       query.setMaxResults(limit); */
        List<BigInteger> resultList = query.getResultList();
        return resultList;
    }

    @Transactional
    public Map<String, Object> updateCustomerDocument(Map<Integer, List<MultipartFile>> groupedFiles, Long customerId, String otherDocument, Long qualificationDetailId, String dateOfIssue, String validUpto, String role, Boolean removeFileTypes, HashSet<Document> documentsToSave) throws Exception {

        String dateFormat = "yyyy-MM-dd";
        MultipartFile processedFile = null;

        CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
        if (customCustomer == null) {
            throw new NotFoundException("No data found for this customerId");
        }

        // Response data to be shown.
        Map<String, Object> responseData = new HashMap<>();

        // deleted document messages to be shown.
        List<String> deletedDocumentMessages = new ArrayList<>();

        // One by one fetching the provider documentType with corresponding files uploaded to be saved.
        for (Map.Entry<Integer, List<MultipartFile>> entry : groupedFiles.entrySet()) {
            Integer fileNameId = entry.getKey();

            // Files in that document type respectively.
            List<MultipartFile> fileList = entry.getValue();

            DocumentType documentTypeObj = em.createQuery(
                            Constant.GET_DOCUMENT_TYPE_BY_DOCUMENT_TYPE_ID, DocumentType.class)
                    .setParameter("documentTypeId", fileNameId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (documentTypeObj == null) {
                throw new NotFoundException("Unknown document type for file: " + fileNameId);
            }

            if (documentTypeObj.getDocument_type_id().equals(Constant.DOCUMENT_TYPE_OTHER_ID)) {
                if (otherDocument == null) {
                    throw new IllegalArgumentException("Other Document name cannot be null for uploading other Documents");
                }
                if (otherDocument.trim().isEmpty()) {
                    throw new IllegalArgumentException("Other Document name cannot be empty");
                }
            }

            // If document type is qualification document then qualification details Id cannot be null.
            if (documentTypeObj.getIs_qualification_document().equals(true) && qualificationDetailId == null) {
                throw new IllegalArgumentException("Qualification Detail Id cannot be null for uploading Qualification Documents");
            }

            // If issue date is required for that particular document type it cannot be passed as null.
            if (documentTypeObj.getIs_issue_date_required().equals(true)) {
                if (dateOfIssue == null) {
                    throw new IllegalArgumentException("Date of issue cannot be null");
                }
                if (documentTypeObj.getIs_expiration_date_required().equals(true) && validUpto == null) {
                    throw new IllegalArgumentException("Valid up to (expiration date of document) cannot be null");
                }
            }

            // Now we iterate one by one for document uploaded in that particular document type.
            for (MultipartFile file : fileList) {

                // Validate document
                if (documentTypeObj.getDocument_type_id().equals(Constant.DOCUMENT_TYPE_LIVE_PHOTOGRAPH_ID)) {  // If it's a Live Photo
                    processedFile = documentStorageService.convertToJpg(file);
                    customCustomer.setIsLivePhotoNa(false);
                } else {
                    documentStorageService.validateDocument(file, documentTypeObj);
                }

                Document existingDocument = null;

                // Condition for qualification document or not
                if (qualificationDetailId != null && documentTypeObj.getIs_qualification_document().equals(true)) {
                    existingDocument = em.createQuery(Constant.GET_QUALIFICATION_DETAIL_DOCUMENT_DATA_OF_CUSTOMER, Document.class)
                            .setParameter("customCustomer", customCustomer)
                            .setParameter("documentType", documentTypeObj)
                            .setParameter("qualificationDetailId", qualificationDetailId)
                            .getResultStream()
                            .findFirst()
                            .orElse(null);
                } else {
                    existingDocument = em.createQuery(Constant.GET_DOCUMENT_DATA_OF_CUSTOMER_BY_DOCUMENT_TYPE_ID, Document.class)
                            .setParameter("customCustomer", customCustomer)
                            .setParameter("documentType", documentTypeObj)
                            .getResultStream()
                            .findFirst()
                            .orElse(null);
                }

                // For live photograph we will upload processed image (that's in jpg) else we upload the file as it is.
                if (documentTypeObj.getDocument_type_id().equals(DOCUMENT_TYPE_LIVE_PHOTOGRAPH_ID)) {
                    fileUploadService.uploadFileOnFileServer(processedFile, documentTypeObj.getDocument_type_name(), customerId.toString(), role);
                } else {
                    fileUploadService.uploadFileOnFileServer(file, documentTypeObj.getDocument_type_name(), customerId.toString(), role);
                }

                // If boolean removeFileTypes not null then we remove document other than other document types.
                if (removeFileTypes != null && removeFileTypes && existingDocument != null && fileNameId.equals(Constant.DOCUMENT_TYPE_OTHER_ID)) {
                    String filePath = existingDocument.getFilePath();

                    if (filePath != null) {
                        fileUploadService.deleteFile(customerId, documentTypeObj.getDocument_type_name(), existingDocument.getName(), role);
                    }

                    existingDocument.setDocumentType(null);
                    existingDocument.setFilePath(null);
                    existingDocument.setName(null);
                    existingDocument.setCreatedDate(null);
                    em.persist(existingDocument);
                    documentsToSave.add(existingDocument);

                    deletedDocumentMessages.add(documentTypeObj.getDocument_type_name() + "' has been deleted.");
                    continue;
                }

                if (fileNameId.equals(Constant.DOCUMENT_TYPE_OTHER_ID) && (!file.isEmpty() || file != null)) {
                    String newFileName = file.getOriginalFilename();

                    // Check for existing document with the same name
                    Document existingDocumentWithOtherDocumentType = em.createQuery(Constant.GET_DOCUMENT_DATA_OF_CUSTOMER_BY_DOCUMENT_TYPE_ID + "AND LOWER(d.otherDocument) = LOWER(:otherDocument) ", Document.class)
                            .setParameter("customCustomer", customCustomer)
                            .setParameter("documentType", documentTypeObj)
                            .setParameter("otherDocument", otherDocument.toLowerCase())  // Ensure consistency
                            .getResultStream()
                            .findFirst()
                            .orElse(null);

                    if (existingDocumentWithOtherDocumentType == null) {
                        Document createdDocument = documentStorageService.createDocument(file, documentTypeObj, customCustomer, customerId, role);
                        if (documentTypeObj.getDocument_type_id().equals(13)) {
                            createdDocument.setOtherDocument(otherDocument);
                            entityManager.merge(createdDocument);
                        }
                        documentsToSave.add(createdDocument);
                    } else {

                        String filePath = existingDocumentWithOtherDocumentType.getFilePath();
                        if (filePath != null) {
                            String absolutePath = System.getProperty("user.dir") + "/../test/" + filePath;
                            File oldFile = new File(absolutePath);
                            String oldFileName = oldFile.getName();
                            existingDocumentWithOtherDocumentType.setIsArchived(false);
                            if (!newFileName.equals(oldFileName)) {
                                fileUploadService.deleteFile(customerId, documentTypeObj.getDocument_type_name(), existingDocumentWithOtherDocumentType.getName(), role);
                                documentStorageService.updateOrCreateDocument(existingDocumentWithOtherDocumentType, file, documentTypeObj, customerId, role);
                            }
                        }
                        entityManager.merge(existingDocumentWithOtherDocumentType);
                        documentsToSave.add(existingDocumentWithOtherDocumentType);
                    }
                }

                // If the file is not empty and a document already exists, update the document
                else if (existingDocument != null && (!file.isEmpty() || file != null) && !fileNameId.equals(Constant.DOCUMENT_TYPE_OTHER_ID)) {
                    String filePath = existingDocument.getFilePath();
                    if (documentTypeObj.getDocument_type_id().equals(Constant.DOCUMENT_TYPE_LIVE_PHOTOGRAPH_ID)) {
                        customCustomer.setIsLivePhotoNa(false);
                    }
                    if (qualificationDetailId != null && documentTypeObj.getIs_qualification_document().equals(true)) {
                        QualificationDetails qualificationDetails = findQualificationDetailForCustomer(qualificationDetailId, customCustomer);
                        existingDocument.setIs_qualification_document(true);
                        existingDocument.setQualificationDetails(qualificationDetails);
                    }

                    if (dateOfIssue != null && documentTypeObj.getIs_issue_date_required().equals(true)) {
                        DocumentValidity documentValidity = null;
                        if (existingDocument.getDocumentValidity() == null) {
                            documentValidity = new DocumentValidity();
                            validateDate(dateOfIssue, validUpto, dateFormat);
                            documentValidity.setDate_of_issue(convertStringToDate(dateOfIssue, "yyyy-MM-dd"));
                            if (validUpto == null) {
                                documentValidity.setIs_valid_upto_na(true);
                                documentValidity.setValid_upto(null);
                            } else {
                                documentValidity.setIs_valid_upto_na(false);
                                documentValidity.setValid_upto(convertStringToDate(validUpto, "yyyy-MM-dd"));
                            }
                            documentValidity.setDocument(existingDocument);
                            existingDocument.setDocumentValidity(documentValidity);
                            entityManager.persist(documentValidity);

                        } else if (existingDocument.getDocumentValidity() != null) {
                            documentValidity = existingDocument.getDocumentValidity();
                            validateDate(dateOfIssue, validUpto, dateFormat);
                            documentValidity.setDate_of_issue(convertStringToDate(dateOfIssue, "yyyy-MM-dd"));
                            if (validUpto == null) {
                                documentValidity.setIs_valid_upto_na(true);
                                documentValidity.setValid_upto(null);

                            } else {
                                documentValidity.setIs_valid_upto_na(false);
                                documentValidity.setValid_upto(convertStringToDate(validUpto, "yyyy-MM-dd"));
                            }
                            documentValidity.setDocument(existingDocument);
                            existingDocument.setDocumentValidity(documentValidity);
                            entityManager.merge(documentValidity);
                        }

                    }
                    if (filePath != null) {
                        String absolutePath = System.getProperty("user.dir") + "/../test/" + filePath;
                        File oldFile = new File(absolutePath);
                        String oldFileName = oldFile.getName();

                        // Get the expected filename after any conversion
                        boolean isLivePhoto = documentTypeObj.getDocument_type_id().equals(3);
                        String newFileName = documentStorageService.getConvertedFilename(file, isLivePhoto);

                        existingDocument.setIsArchived(false);

                        if (!newFileName.equals(oldFileName)) {
                            fileUploadService.deleteFile(customerId, documentTypeObj.getDocument_type_name(), existingDocument.getName(), role);
                            if (isLivePhoto) {
                                documentStorageService.updateOrCreateDocument(existingDocument, processedFile, documentTypeObj, customerId, role);
                            } else {
                                documentStorageService.updateOrCreateDocument(existingDocument, file, documentTypeObj, customerId, role);
                            }
                        }
                    }
                    entityManager.merge(existingDocument);
                    documentsToSave.add(existingDocument);

                } else {
                    // If the file is not empty create the document
                    if (!file.isEmpty() || file != null && (fileNameId != 13)) {
                        Document document = null;
                        if (documentTypeObj.getDocument_type_id().equals(3)) {
                            customCustomer.setIsLivePhotoNa(false);
                            document = documentStorageService.createDocument(processedFile, documentTypeObj, customCustomer, customerId, role);
                        } else {
                            document = documentStorageService.createDocument(file, documentTypeObj, customCustomer, customerId, role);
                        }
                        documentsToSave.add(document);
                        if (documentTypeObj.getDocument_type_id().equals(3)) {
                            customCustomer.setIsLivePhotoNa(false);
                        }
                        if (qualificationDetailId != null && documentTypeObj.getIs_qualification_document().equals(true)) {
                            QualificationDetails qualificationDetails = findQualificationDetailForCustomer(qualificationDetailId, customCustomer);
                            document.setIs_qualification_document(true);
                            document.setQualificationDetails(qualificationDetails);
                            entityManager.merge(document);
                            documentsToSave.add(document);
                        }
                        if (dateOfIssue != null && documentTypeObj.getIs_issue_date_required().equals(true)) {
                            DocumentValidity documentValidity = new DocumentValidity();
                            validateDate(dateOfIssue, validUpto, dateFormat);
                            documentValidity.setDate_of_issue(convertStringToDate(dateOfIssue, "yyyy-MM-dd"));
                            if (validUpto == null) {
                                documentValidity.setIs_valid_upto_na(true);
                                documentValidity.setValid_upto(null);
                            } else {
                                documentValidity.setIs_valid_upto_na(false);
                                documentValidity.setValid_upto(convertStringToDate(validUpto, "yyyy-MM-dd"));
                            }
                            documentValidity.setDocument(document);
                            entityManager.persist(documentValidity);
                            document.setDocumentValidity(documentValidity);
                            entityManager.merge(document);
                            documentsToSave.add(document);
                        }
                    }
                }
            }
        }

        entityManager.merge(customCustomer);

        // Upadating the response.
        List<Map<String, Object>> filteredDocuments = new ArrayList<>();
        for (Document document : documentsToSave) {
            if (document.getIsArchived() != null && !document.getIsArchived()) { // Exclude archived documents
                if (document.getFilePath() != null && document.getDocumentType() != null) {
                    Map<String, Object> documentDetails = new HashMap<>();
                    documentDetails.put("documentId", document.getDocumentId());
                    documentDetails.put("name", document.getName());
                    documentDetails.put("filePath", document.getFilePath());
                    documentDetails.put("created_date", document.getCreatedDate());

                    // Add qualification details if applicable
                    if (Boolean.TRUE.equals(document.getIs_qualification_document()) && document.getQualificationDetails() != null) {
                        documentDetails.put("qualification_detail_id", qualificationDetailId);
                    }

                    // Add document validity details if applicable
                    if (document.getDocumentValidity() != null) {

                        Map<String, String> validityDetails = new HashMap<>();
                        validityDetails.put("dateOfIssue", dateOfIssue);
                        validityDetails.put("validUpto", validUpto);

                        documentDetails.put("documentValidity", validityDetails); // Include as nested map
                    }
                    String filePath;
                    // Generate a file URL for the document
                    try {
                        filePath = documentStorageService.encrypt(document.getFilePath());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    String fileUrl = fileService.getFileUrl(filePath, request);
                    /* String fileUrl = fileService.getFileUrl(document.getFilePath(), request);*/
                    Map<String, Object> documentTypeResponse = new HashMap<>();
                    documentTypeResponse.put("document_type_id", document.getDocumentType().getDocument_type_id());
                    if (otherDocument != null && !otherDocument.trim().isEmpty()) {
                        documentTypeResponse.put("document_type_name", otherDocument);
                    } else {
                        documentTypeResponse.put("document_type_name", document.getDocumentType().getDocument_type_name());
                    }
                    documentTypeResponse.put("description", document.getDocumentType().getDescription());
                    documentTypeResponse.put("is_qualification_document", document.getDocumentType().getIs_qualification_document());
                    documentTypeResponse.put("is_issue_date_required", document.getDocumentType().getIs_issue_date_required());
                    documentTypeResponse.put("is_expiration_date_required", document.getDocumentType().getIs_expiration_date_required());
                    documentTypeResponse.put("required_document_types", document.getDocumentType().getRequired_document_types());
                    documentTypeResponse.put("max_document_size", document.getDocumentType().getMax_document_size());
                    documentTypeResponse.put("min_document_size", document.getDocumentType().getMin_document_size());
                    documentTypeResponse.put("sort_order", document.getDocumentType().getSort_order());

                    documentDetails.put("documentType", documentTypeResponse);
                    documentDetails.put("fileUrl", fileUrl);
                    filteredDocuments.add(documentDetails);
                }
            }
        }

        log.info("Deleted Documents logs: {}", deletedDocumentMessages);
        responseData.put("uploadedDocuments", filteredDocuments);
        return responseData;

    }

    public Boolean validateDate(String dateOfIssueStr, String validUptoStr, String dateFormatInString) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatInString);
        dateFormat.setLenient(false);

        try {
            // Validate format
            if (!isValidDateFormat(dateOfIssueStr, dateFormat)) {
                throw new IllegalArgumentException("Date of Issue must be in " + dateFormatInString + " format");
            }

            Date dateOfIssue = dateFormat.parse(dateOfIssueStr);
            Date validUpto = null;
            if (validUptoStr != null) {

                if (!isValidDateFormat(validUptoStr, dateFormat)) {
                    throw new IllegalArgumentException("Valid Upto Date must be in " + dateFormatInString + " format");
                }
                validUpto = dateFormat.parse(validUptoStr);

                // Check if validUpto is before dateOfIssue
                if (validUpto.before(dateOfIssue)) {
                    throw new IllegalArgumentException("Valid Upto Date cannot be before Date of Issue");
                }
            }
            return true;
        } catch (IllegalArgumentException ex) {
            exceptionHandlingService.handleException(ex);
            throw ex; // Rethrow with meaningful context
        } catch (ParseException ex) {
            exceptionHandlingService.handleException(ex);
            throw new IllegalArgumentException("Invalid date format", ex);
        }
    }

    private boolean isValidDateFormat(String dateStr, SimpleDateFormat dateFormat) {
        try {
            dateFormat.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public QualificationDetails findQualificationDetailForCustomer(Long qualificationDetailId, CustomCustomer customCustomer) {
        List<QualificationDetails> qualificationDetails = customCustomer.getQualificationDetailsList();
        QualificationDetails qualificationToFind = null;
        for (QualificationDetails qualificationDetails1 : qualificationDetails) {
            if (qualificationDetails1.getQualification_detail_id().equals(qualificationDetailId)) {
                qualificationToFind = qualificationDetails1;
                break;
            }
        }
        if (qualificationToFind == null) {
            throw new IllegalArgumentException("Qualification details with id " + qualificationDetailId + " does not exists");
        }
        return qualificationToFind;
    }

}
