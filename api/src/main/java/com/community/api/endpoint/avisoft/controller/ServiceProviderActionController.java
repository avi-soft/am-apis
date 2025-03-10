package com.community.api.endpoint.avisoft.controller;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.configuration.ImageSizeConfig;
import com.community.api.dto.CommunicationRequest;
import com.community.api.dto.FormDataOnly;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.*;
import com.community.api.services.DocumentStorageService;
import com.community.api.services.EmailService;
import com.community.api.services.FileService;
import com.community.api.services.PrivilegeService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.apache.commons.io.FilenameUtils;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.jpa.QueryHints;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/service-provider-actions")
public class ServiceProviderActionController {

    @Autowired
    private DocumentStorageService fileUploadService;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    private EmailService emailService;


    @Autowired
    private FileService fileService;
    @Autowired
    private final JwtUtil jwtTokenUtil;

    @Autowired
    private final RoleService roleService;

    @Autowired
    private PrivilegeService privilegeService;

    @Autowired
    HttpServletRequest request;

    public ServiceProviderActionController(JwtUtil jwtTokenUtil, RoleService roleService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.roleService = roleService;
    }

//    @Autowired
//    private WhatsappService whatsappService; //  need to implement this later

    @PostMapping(value="/communicate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<?> communicateWithCustomers(
            @FormDataOnly CommunicationRequest request,
            @RequestHeader(value = "Authorization") String authHeader){
        try {
            // Validate service provider
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long userId = jwtTokenUtil.extractId(jwtToken);
            if (!actionAccess(authHeader)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO COMMUNICATE WITH CUSTOMER", HttpStatus.FORBIDDEN);
            }
            ServiceProviderEntity serviceProvider=null;
            CustomAdmin customAdmin=null;
            if(roleService.findRoleName(roleId).equals(Constant.SERVICE_PROVIDER))
            {
                 serviceProvider = entityManager.find(ServiceProviderEntity.class, userId);
                if (serviceProvider == null) {
                    return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.NOT_FOUND);
                }

                if(serviceProvider.getMyReferrals()==null)
                {
                    throw new IllegalArgumentException("Service Provider with id "+ serviceProvider.getService_provider_id()+ " do not have referred customers");
                }
                else if(serviceProvider.getMyReferrals().isEmpty())
                {
                    throw new IllegalArgumentException("Service Provider with id "+ serviceProvider.getService_provider_id()+ " do not have referred customers");
                }
            }
            if(roleService.findRoleName(roleId).equals(Constant.roleAdmin) || roleService.findRoleName(roleId).equals(Constant.roleSuperAdmin) || roleService.findRoleName(roleId).equals(Constant.roleAdminServiceProvider))
            {
                customAdmin=entityManager.find(CustomAdmin.class,userId);
                if(customAdmin==null)
                {
                    return ResponseService.generateErrorResponse("Custom Admin with id" + userId+ " does not exist",HttpStatus.NOT_FOUND);
                }
            }

            // Process comma-separated modes if needed
            List<Integer> modesList = getModesListFromRequest(request);
            if(modesList == null)
            {
                throw new IllegalArgumentException("Modes cannot be null");
            }
            if(modesList.isEmpty())
            {
                throw new IllegalArgumentException("You have to select atleast one mode");
            }

            // Process comma-separated customerIds if needed
            List<Long> customerIdsList = getCustomerIdsListFromRequest(request);
            if(customerIdsList == null || customerIdsList.isEmpty())
            {
                return ResponseService.generateErrorResponse("You have to select atleast one customer to communicate with", HttpStatus.BAD_REQUEST);
            }

            for(Long customerId: customerIdsList)
            {
                CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customerId);
                if(customCustomer==null)
                {
                    return ResponseService.generateErrorResponse("Customer with id "+ customerId +" does not exist",HttpStatus.NOT_FOUND);
                }
            }
            if(roleService.findRoleName(roleId).equals(Constant.SERVICE_PROVIDER))
            {
                assert serviceProvider != null;
                if(!serviceProvider.getMyReferrals().isEmpty())
                {
                    List<CustomerReferrer> referrers= serviceProvider.getMyReferrals();
                    List<Long> referrerIds= new ArrayList<>();
                    for(CustomerReferrer customerReferrer: referrers)
                    {
                        referrerIds.add(customerReferrer.getCustomer().getId());
                    }
                    for(Long customerId: customerIdsList)
                    {
                        if(!referrerIds.contains(customerId))
                        {
                            return ResponseService.generateErrorResponse("Customer with id " + customerId+ " is not the referrer of Service provider",HttpStatus.BAD_REQUEST);
                        }
                    }
                }
            }

           /* if(subject==null)
            {
                return ResponseService.generateErrorResponse("subject/title of message cannot be null",HttpStatus.BAD_REQUEST);
            }
            if(subject.trim().isEmpty())
            {
                return ResponseService.generateErrorResponse("subject/title of message cannot be empty",HttpStatus.BAD_REQUEST);
            }*/
            if(request.getContentText()==null && (request.getFiles()==null || request.getFiles().isEmpty()))
            {
                return ResponseService.generateErrorResponse("Either you have to provide text or any file.Both cannot be null",HttpStatus.BAD_REQUEST);
            }
            String contentTextTrimmed=null;
            if(request.getContentText()!=null)
            {
                contentTextTrimmed=request.getContentText().trim();
            }
            String subjectTrimmed=null;
            if(request.getSubject()!=null)
            {
                subjectTrimmed=request.getSubject().trim();
            }

            boolean hasValidContent = contentTextTrimmed != null && !contentTextTrimmed.isEmpty();
            boolean hasValidFiles = false;

            // Check if files are effectively non-empty
            if (request.getFiles() != null && !request.getFiles().isEmpty()) {
                if (request.getFiles().size() == 1) {
                    // Single file case
                    hasValidFiles = request.getFiles().get(0).getContentType() != null;
                } else {
                    // Multiple files case
                    hasValidFiles = request.getFiles().stream()
                            .anyMatch(file -> file.getContentType() != null);
                }
            }

            if (!hasValidContent && !hasValidFiles) {
                return ResponseService.generateErrorResponse(
                        "Either you have to provide text or valid files. Both cannot be empty",
                        HttpStatus.BAD_REQUEST
                );
            }
            // Create communication content
            CommunicationContent content = new CommunicationContent();
            if(serviceProvider!=null)
            {
                content.setServiceProvider(serviceProvider);
            }
            if(customAdmin!=null)
            {
                content.setAdmin(customAdmin);
            }
            if(contentTextTrimmed!=null)
            {
                content.setContentText(contentTextTrimmed);
            }
            else{
                content.setContentText(null);
            }
            if(subjectTrimmed!=null )
            {
                content.setSubject(subjectTrimmed);
            }
            else{
                content.setSubject(null);
            }

            List<ContentFile> contentFiles = new ArrayList<>();
            if (request.getFiles() != null && !request.getFiles().isEmpty()) {
                if(request.getFiles().size()==1)
                {
                    if( request.getFiles().get(0).getContentType()!=null)
                    {
                        contentFiles = processFiles(request.getFiles(), content);
                        content.setContentFiles(contentFiles);
                    }
                }

                else if(request.getFiles().size()>1)
                {
                    contentFiles = processFiles(request.getFiles(), content);
                    content.setContentFiles(contentFiles);
                }

            }

            entityManager.persist(content);

            // Create a single action log for all customers
            ActionLog actionLog = new ActionLog();
            if(serviceProvider!=null)
            {
                actionLog.setServiceProvider(serviceProvider);
            }
            if(customAdmin!=null)
            {
                actionLog.setAdmin(customAdmin);
            }
            actionLog.setRole(roleService.getRoleByRoleId(roleId));
            actionLog.setContent(content);

            // Validate and set custom modes
            for (Integer customModeId : modesList) {
                CustomMode customMode = entityManager.find(CustomMode.class, customModeId);
                if (customMode == null) {
                    throw new IllegalArgumentException("Custom mode with id " + customModeId + " does not exist");
                }
            }

            List<CustomMode> modeList = modesList.stream()
                    .map(modeId -> entityManager.find(CustomMode.class, modeId))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            actionLog.setCustomModes(modeList);
            actionLog.setActionTimestamp(LocalDateTime.now());

            List<CustomCustomer> allCustomers = customerIdsList.stream()
                    .map(id -> entityManager.find(CustomCustomer.class, id))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // Separate customers based on email availability
            List<CustomCustomer> customersWithEmail = new ArrayList<>();
            List<CustomCustomer> customersWithoutEmail = new ArrayList<>();

            for (CustomCustomer customer : allCustomers) {
                if (customer.getEmailAddress() != null && !customer.getEmailAddress().trim().isEmpty()) {
                    customersWithEmail.add(customer);
                } else {
                    customersWithoutEmail.add(customer);
                }
            }

            // Check if any customer has an email address before proceeding
            if (customersWithEmail.isEmpty()) {
                return ResponseService.generateErrorResponse(
                        "Communication is not done via Email.Not even a single selected customer has an email address to receive communication",
                        HttpStatus.BAD_REQUEST
                );
            }

            // Set both lists in action log
            actionLog.setCustomersWithEmail(customersWithEmail);
            actionLog.setCustomersWithoutEmail(customersWithoutEmail);

            StringBuilder deliveryStatus = new StringBuilder();

            // Send communications based on selected modes
            List<File> tempFiles = new ArrayList<>();
            if (request.getFiles() != null && !request.getFiles().isEmpty()) {
                if (request.getFiles().size() == 1) {
                    if (request.getFiles().get(0).getContentType() != null) {
                        tempFiles = createTemporaryFiles(request.getFiles());
                    }
                } else if (request.getFiles().size() > 1) {
                    tempFiles = createTemporaryFiles(request.getFiles());
                }
            }

            try {
                for (CustomMode mode : modeList) {
                    try {
                        if (mode.getCustomModeId().equals(1)) { // Email mode
                            if (!customersWithEmail.isEmpty()) {
                                List<String> emailAddresses = customersWithEmail.stream()
                                        .map(CustomCustomer::getEmailAddress)
                                        .collect(Collectors.toList());

                                emailService.sendEmailWithAttachments(
                                        emailAddresses,
                                        content.getSubject(),
                                        content.getContentText(),
                                        tempFiles
                                );
                            }
                        }
                        // Add other communication modes here...
                    } catch (Exception e) {
                        deliveryStatus.append(String.format("Failed for mode %d: %s; ",
                                mode.getCustomModeId(), e.getMessage()));
                    }
                }

                String finalStatus = deliveryStatus.length() > 0 ?
                        deliveryStatus.toString() :
                        (customersWithoutEmail.isEmpty() ? "SUCCESS" : "PARTIALLY_FAILED: Some customers don't have email addresses");

                actionLog.setDeliveryStatus(finalStatus);

            } finally {
                cleanupTemporaryFiles(tempFiles);
            }

            entityManager.persist(actionLog);
            entityManager.flush();

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("customersWithEmail", customersWithEmail.stream()
                    .map(customer -> Map.of(
                            "id", customer.getId(),
                            "name", (customer.getFirstName() != null ? customer.getFirstName() : "")
                                    + " " + (customer.getLastName() != null ? customer.getLastName() : ""),
                            "emailAddress", customer.getEmailAddress() != null ? customer.getEmailAddress() : ""
                    ))
                    .collect(Collectors.toList()));

            responseData.put("customersWithoutEmail", customersWithoutEmail.stream()
                    .map(customer -> Map.of(
                            "id", customer.getId(),
                            // Safely handle first and last name concatenation
                            "name", (customer.getFirstName() != null ? customer.getFirstName() : "")
                                    + " " + (customer.getLastName() != null ? customer.getLastName() : ""),
                            "emailAddress", customer.getEmailAddress() != null ? customer.getEmailAddress() : ""
                    ))
                    .collect(Collectors.toList()));
            responseData.put("actionLog", convertToDTO(actionLog));
            responseData.put("totalCustomersEmailed", customersWithEmail.size());

            return ResponseService.generateSuccessResponse(
                    "Communication processed",
                    responseData,
                    HttpStatus.OK
            );

        }
        catch (IllegalArgumentException e)
        {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse(
                    "Failed to process communication: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // Helper method to parse comma-separated customer IDs
    private List<Long> getCustomerIdsListFromRequest(CommunicationRequest request) {
        List<Long> result = new ArrayList<>();

        // Handle the case where customerIds is already a List<Long>
        if (request.getCustomerIds() instanceof List) {
            return (List<Long>) request.getCustomerIds();
        }

        // Handle string representation (comma-separated values)
        if (request.getCustomerIds() instanceof String) {
            String customerIdsStr = (String) request.getCustomerIds();
            if (customerIdsStr != null && !customerIdsStr.trim().isEmpty()) {
                String[] idsArray = customerIdsStr.split(",");
                for (String id : idsArray) {
                    try {
                        result.add(Long.parseLong(id.trim()));
                    } catch (NumberFormatException e) {
                        // Skip invalid IDs
                    }
                }
            }
        }

        return result;
    }

    // Helper method to parse comma-separated modes
    private List<Integer> getModesListFromRequest(CommunicationRequest request) {
        List<Integer> result = new ArrayList<>();

        // Handle the case where modes is already a List<Integer>
        if (request.getModes() instanceof List) {
            return (List<Integer>) request.getModes();
        }

        // Handle string representation (comma-separated values)
        if (request.getModes() instanceof String) {
            String modesStr = (String) request.getModes();
            if (modesStr != null && !modesStr.trim().isEmpty()) {
                String[] modesArray = modesStr.split(",");
                for (String mode : modesArray) {
                    try {
                        result.add(Integer.parseInt(mode.trim()));
                    } catch (NumberFormatException e) {
                        // Skip invalid modes
                    }
                }
            }
        }

        return result;
    }

    private List<ContentFile> processFiles(List<MultipartFile> files, CommunicationContent content) {
        List<ContentFile> contentFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                ContentFile contentFile = saveFile(file, "");
                contentFile.setCommunicationContent(content);
                fileUploadService.uploadFileOnFileServer(file, "Communications", "", "SERVICE_PROVIDER");
                contentFiles.add(contentFile);
            }
            catch (IllegalArgumentException e)
            {
                exceptionHandlingService.handleException(e);
                throw new IllegalArgumentException(e.getMessage());
            }
            catch (Exception e) {
                exceptionHandlingService.handleException(e);
                throw new RuntimeException("Failed to process file: " + file.getOriginalFilename(), e);
            }
        }

        return contentFiles;
    }

    public ContentFile saveFile(MultipartFile file, String purpose) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalStateException("File is missing or empty");
        }
        // Validate file type and size
        if (!isValidFileType(file)) {
            throw new IllegalArgumentException("Invalid file type. Only supported files are allowed.");
        }
        if (file.getSize() > Constant.MAX_REFERRER_FILE_SIZE) {
            String maxFileSize = ImageSizeConfig.convertBytesToReadableSize(Constant.MAX_REFERRER_FILE_SIZE);
            throw new IllegalArgumentException("File size should be below "+ maxFileSize);
        }
        // Construct the file path
        String dbPath = "avisoftdocument/SERVICE_PROVIDER/Communications" + purpose;
        String filePath = dbPath + File.separator + file.getOriginalFilename();


        // Upload file to file server
        fileUploadService.uploadFileOnFileServer(file, "Communications", purpose, "SERVICE_PROVIDER");

        // Create and populate the ContentFile entity
        ContentFile contentFile = new ContentFile();
        contentFile.setFileName(file.getOriginalFilename());
        contentFile.setFilePath(filePath);
        contentFile.setSize(file.getSize());
        contentFile.setFileTypes(determineFileTypes(file.getContentType()));

        return contentFile;
    }

    private boolean isValidFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return false;

        return true;
    }

    private List<FileType> determineFileTypes(String contentType) {
        // Query for the appropriate FileType from the database
        String fileTypeName = contentType.split("/")[1].toUpperCase();
        TypedQuery<FileType> query = entityManager.createQuery(
                "SELECT ft FROM FileType ft WHERE ft.file_type_name = :typeName",
                FileType.class
        );
        query.setParameter("typeName", fileTypeName);

        List<FileType> fileTypes = query.getResultList();
        if (fileTypes.isEmpty()) {
            // If file type doesn't exist, create a default one
            FileType fileType = new FileType();
            fileType.setFile_type_name(fileTypeName);
            entityManager.persist(fileType);
            fileTypes = Collections.singletonList(fileType);
        }

        return fileTypes;
    }

    private List<File> createTemporaryFiles(List<MultipartFile> files) throws IOException {
        List<File> tempFiles = new ArrayList<>();

        for (MultipartFile multipartFile : files) {
            File tempFile = File.createTempFile(
                    FilenameUtils.getBaseName(multipartFile.getOriginalFilename()),
                    "." + FilenameUtils.getExtension(multipartFile.getOriginalFilename())
            );

            multipartFile.transferTo(tempFile);
            tempFiles.add(tempFile);
        }

        return tempFiles;
    }

    private void cleanupTemporaryFiles(List<File> tempFiles) {
        for (File file : tempFiles) {
            try {
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                // Log error but don't throw - this is cleanup
                exceptionHandlingService.handleException(e);
            }
        }
    }

    private Map<String, Object> convertToDTO(ActionLog actionLog) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("actionLogId", actionLog.getActionLogId());
        List<Long> customerIds = Stream.concat(
                actionLog.getCustomersWithEmail().stream().map(CustomCustomer::getId),
                actionLog.getCustomersWithoutEmail().stream().map(CustomCustomer::getId)
        ).collect(Collectors.toList());

        dto.put("customerIds", customerIds);
        dto.put("deliveryStatus", actionLog.getDeliveryStatus());
        if(actionLog.getServiceProvider()!=null)
        {
            dto.put("serviceProviderId", actionLog.getServiceProvider().getService_provider_id());
        }
        if(actionLog.getAdmin()!=null)
        {
            dto.put("adminId", actionLog.getAdmin().getAdmin_id());
        }
        dto.put("roleId",actionLog.getRole().getRole_id());
        dto.put("actionTimestamp", actionLog.getActionTimestamp());
        dto.put("modes", actionLog.getCustomModes().stream().map(CustomMode::getCustomModeId).collect(Collectors.toList()));
        dto.put("content",actionLog.getContent());
        return dto;
    }

    @GetMapping("/get-communications")
    @Transactional(readOnly = true)
    public ResponseEntity getCommunicationHistory(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long userId = jwtTokenUtil.extractId(jwtToken);

            if (!actionAccess(authHeader)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO VIEW THE COMMUNICATION", HttpStatus.FORBIDDEN);
            }

            // JPQL Query based on role
            String jpql;
            if (roleService.findRoleName(roleId).equalsIgnoreCase(Constant.SERVICE_PROVIDER)) {
                ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, userId);
                if (serviceProvider == null) {
                    return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.NOT_FOUND);
                }

                jpql = """
            SELECT DISTINCT al FROM ActionLog al
            LEFT JOIN FETCH al.content c
            WHERE al.serviceProvider.service_provider_id = :userId
            ORDER BY al.actionTimestamp DESC
            """;
            }
            else if (roleService.findRoleName(roleId).equalsIgnoreCase(Constant.roleAdmin) ||
                    roleService.findRoleName(roleId).equalsIgnoreCase(Constant.roleSuperAdmin) ||
                    roleService.findRoleName(roleId).equalsIgnoreCase(Constant.roleAdminServiceProvider)) {
                CustomAdmin customAdmin = entityManager.find(CustomAdmin.class, userId);
                if (customAdmin == null) {
                    return ResponseService.generateErrorResponse("Custom Admin with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
                }

                jpql = """
            SELECT DISTINCT al FROM ActionLog al
            LEFT JOIN FETCH al.content c
            WHERE al.admin.admin_id = :userId
            ORDER BY al.actionTimestamp DESC
            """;
            }
            else {
                return ResponseService.generateErrorResponse("Invalid role", HttpStatus.FORBIDDEN);
            }

            List<ActionLog> actionLogs = entityManager.createQuery(jpql, ActionLog.class)
                    .setParameter("userId", userId)
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .setHint(QueryHints.HINT_FETCH_SIZE, 50)
                    .setHint(QueryHints.HINT_READONLY, true)
                    .getResultList();

            // Initialize related collections
            actionLogs.forEach(log -> {
                Hibernate.initialize(log.getCustomersWithEmail());
                Hibernate.initialize(log.getCustomersWithoutEmail());
                Hibernate.initialize(log.getCustomModes());
            });

            // Convert to DTO
            List<Map<String, Object>> communicationHistory = actionLogs.stream()
                    .map(this::convertToCommunicationDTO)
                    .collect(Collectors.toList());

            // Count total records for pagination
            String countJpql = roleService.findRoleName(roleId).equals(Constant.SERVICE_PROVIDER) ?
                    """
                    SELECT COUNT(DISTINCT al) FROM ActionLog al
                    WHERE al.serviceProvider.service_provider_id = :userId
                    """ :
                    """
                    SELECT COUNT(DISTINCT al) FROM ActionLog al
                    WHERE al.admin.admin_id = :userId
                    """;

            Long totalRecords = entityManager.createQuery(countJpql, Long.class)
                    .setParameter("userId", userId)
                    .getSingleResult();

            // Prepare pagination response
            Map<String, Object> response = new HashMap<>();
            response.put("communications", communicationHistory);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalRecords", totalRecords);
            response.put("totalPages", (int) Math.ceil((double) totalRecords / size));

            return ResponseService.generateSuccessResponse(
                    "Communication history retrieved successfully",
                    response,
                    HttpStatus.OK
            );
        }
        catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new RuntimeException("Failed to retrieve communication history", e);
        }
    }

    private Map<String, Object> convertToCommunicationDTO(ActionLog actionLog) {
        Map<String, Object> dto = new HashMap<>();

        // Handle content details
        CommunicationContent content = actionLog.getContent();
        Map<String, Object> contentDetails = new HashMap<>();
        contentDetails.put("subject", content != null ? content.getSubject() : null);
        contentDetails.put("contentText", content != null ? content.getContentText() : null);
        List<Map<String, Object>> files = Optional.ofNullable(content.getContentFiles())
                .filter(f -> !f.isEmpty())
                .map(f -> f.stream()
                        .map(file -> {
                            Map<String, Object> fileInfo = new HashMap<>();
                            fileInfo.put("fileName", file.getFileName());
                            fileInfo.put("size", formatFileSize(file.getSize()));
                            fileInfo.put("fileUrl", fileService.getFileUrl(file.getFilePath(), request));
                            fileInfo.put("fileTypes", file.getFileTypes().stream()
                                    .map(FileType::getFile_type_name)
                                    .collect(Collectors.toList()));
                            return fileInfo;
                        })
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        contentDetails.put("files", files);
        dto.put("content", contentDetails);
        dto.put("deliveryStatus", actionLog.getDeliveryStatus());
        // Recipients without emails
        List<Map<String, Object>> customersWithoutEmail = actionLog.getCustomersWithoutEmail().stream()
                .map(customer -> {
                    Map<String, Object> recipientInfo = new HashMap<>();
                    recipientInfo.put("customerId", customer.getId());
                    String name = (customer.getFirstName() != null ? customer.getFirstName() : "")
                            + " " + (customer.getLastName() != null ? customer.getLastName() : "");
                    recipientInfo.put("name", name.trim()); // Remove unnecessary whitespace
                    recipientInfo.put("emailAddress", customer.getEmailAddress() != null ? customer.getEmailAddress() : "");
                    return recipientInfo;
                })
                .collect(Collectors.toList());

        // Recipients with emails
        List<Map<String, Object>> customersWithEmail = actionLog.getCustomersWithEmail().stream()
                .map(customer -> {
                    Map<String, Object> recipientInfo = new HashMap<>();
                    recipientInfo.put("customerId", customer.getId());
                    String name=(customer.getFirstName()!=null ?customer.getFirstName() : "") + " " + (customer.getLastName()!=null ? customer.getLastName():"");
                    recipientInfo.put("name",name.trim());
                    recipientInfo.put("emailAddress", customer.getEmailAddress()!=null ? customer.getEmailAddress():"");
                    return recipientInfo;
                })
                .collect(Collectors.toList());
        dto.put("customersWithEmail", customersWithEmail);
        dto.put("customersWithoutEmail", customersWithoutEmail);
        dto.put("timestamp", actionLog.getActionTimestamp());
        dto.put("actionLogId", actionLog.getActionLogId());
        dto.put("modes", actionLog.getCustomModes().stream().map(CustomMode::getCustomModeId).collect(Collectors.toList()));
        return dto;
    }

    private String formatFileSize(Long bytes) {
        if (bytes == null) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    public boolean actionAccess(String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();

            Long userId = null;
            if (role.equals(Constant.SUPER_ADMIN) || role.equals(Constant.ADMIN) || role.equals(Constant.roleAdminServiceProvider)) {
                return true;
            } else if (role.equals(Constant.SERVICE_PROVIDER)) {
                return true;
            }
            return false;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }
}