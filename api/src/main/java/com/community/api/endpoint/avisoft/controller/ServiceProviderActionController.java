package com.community.api.endpoint.avisoft.controller;

import com.community.api.component.Constant;
import com.community.api.configuration.ImageSizeConfig;
import com.community.api.dto.CommunicationRequest;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.*;
import com.community.api.services.DocumentStorageService;
import com.community.api.services.EmailService;
import com.community.api.services.FileService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.jpa.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    HttpServletRequest request;

//    @Autowired
//    private WhatsappService whatsappService; //  need to implement this later

    @PostMapping("/communicate")
    @Transactional
    public ResponseEntity<?> communicateWithCustomers(
            @RequestParam Long serviceProviderId,
            @RequestParam List<Long> customerIds,
            @RequestParam List<Integer> modes,
            @RequestParam (required = false) String contentText,
            @RequestParam (required = false)String subject,
            @RequestParam(required = false) List<MultipartFile> files){
        try {
            // Validate service provider
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (serviceProvider == null) {
                return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.NOT_FOUND);
            }

            if(customerIds==null || customerIds.isEmpty())
            {
                return ResponseService.generateErrorResponse("You have to select atleast one customer to communicate with", HttpStatus.BAD_REQUEST);
            }
            for(Long customerId: customerIds)
            {
                CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customerId);
                if(customCustomer==null)
                {
                    return ResponseService.generateErrorResponse("Customer with id "+ customerId +" does not exist",HttpStatus.NOT_FOUND);
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
            if(contentText==null && (files==null || files.isEmpty()))
            {
                return ResponseService.generateErrorResponse("Either you have to provide text or any file.Both cannot be null",HttpStatus.BAD_REQUEST);
            }
            String contentTextTrimmed=null;
            if(contentText!=null)
            {
                contentTextTrimmed=contentText.trim();
            }
            String subjectTrimmed=null;
            if(subject!=null)
            {
                subjectTrimmed=subject.trim();
            }
            if ((contentText == null || (contentTextTrimmed.isEmpty())) &&
                    (files == null || files.isEmpty())) {
                return ResponseService.generateErrorResponse(
                        "Either you have to provide text or any file. Both cannot be empty",
                        HttpStatus.BAD_REQUEST
                );
            }

            // Create communication content
            CommunicationContent content = new CommunicationContent();
            content.setServiceProvider(serviceProvider);
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
            if (files != null && !files.isEmpty()) {
                if(files.size()==1)
                {
                    if( files.get(0).getContentType()!=null)
                    {
                        contentFiles = processFiles(files, content);
                        content.setContentFiles(contentFiles);
                    }
                }

               else if(files.size()>1)
               {
                   contentFiles = processFiles(files, content);
                   content.setContentFiles(contentFiles);
               }

            }

            entityManager.persist(content);

            // Create a single action log for all customers
            ActionLog actionLog = new ActionLog();
            actionLog.setServiceProvider(serviceProvider);
            actionLog.setContent(content);

            // Fetch and set all customers
            List<CustomCustomer> customers = customerIds.stream()
                    .map(id -> entityManager.find(CustomCustomer.class, id))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            actionLog.setCustomCustomers(customers);

            // Validate and set custom modes
            for (Integer customModeId : modes) {
                CustomMode customMode = entityManager.find(CustomMode.class, customModeId);
                if (customMode == null) {
                    throw new IllegalArgumentException("Custom mode with id " + customModeId + " does not exist");
                }
            }

            List<CustomMode> modeList = modes.stream()
                    .map(modeId -> entityManager.find(CustomMode.class, modeId))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            actionLog.setCustomModes(modeList);
            actionLog.setActionTimestamp(LocalDateTime.now());

            // Send communications based on selected modes
            List<File> tempFiles = new ArrayList<>();
            if (files != null && !files.isEmpty()) {
                if(files.size()==1)
                {
                    if( files.get(0).getContentType()!=null)
                    {
                        tempFiles = createTemporaryFiles(files);
                    }
                }

                else if(files.size()>1)
                {
                    tempFiles = createTemporaryFiles(files);
                }

            }

            try {
                // Send communications based on selected modes
                StringBuilder deliveryStatus = new StringBuilder();
                for (CustomMode mode : modeList) {
                    try {
                        if (mode.getCustomModeId().equals(1)) { // Email mode
                            // Collect all email addresses for this mode
                            List<String> emailAddresses = customers.stream()
                                    .map(CustomCustomer::getEmailAddress)
                                    .collect(Collectors.toList());

                            // Send to all recipients
                            emailService.sendEmailWithAttachments(
                                    emailAddresses,
                                    content.getSubject(),
                                    content.getContentText(),
                                    tempFiles
                            );
                        }
                        // Add other communication modes here...
                    } catch (Exception e) {
                        // Add mode-specific failure to delivery status
                        deliveryStatus.append(String.format("Failed for mode %d: %s; ",
                                mode.getCustomModeId(), e.getMessage()));
                    }
                }

                actionLog.setDeliveryStatus(deliveryStatus.length() > 0 ?
                        "PARTIALLY_FAILED: " + deliveryStatus.toString() : "SUCCESS");

            } finally {
                // Clean up temporary files after all communications are done
                cleanupTemporaryFiles(tempFiles);
            }


            entityManager.persist(actionLog);
            entityManager.flush();

            return ResponseService.generateSuccessResponse(
                    "Communication sent successfully",
                    convertToDTO(actionLog),
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
        String dbPath = "avisoftdocument/SERVICE_PROVIDER/Communications/" + purpose;
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
        dto.put("customerIds", actionLog.getCustomCustomers().stream().map(CustomCustomer::getId).collect(Collectors.toList()));
        dto.put("deliveryStatus", actionLog.getDeliveryStatus());
        dto.put("serviceProviderId", actionLog.getServiceProvider().getService_provider_id());
        dto.put("actionTimestamp", actionLog.getActionTimestamp());
        dto.put("modes", actionLog.getCustomModes().stream().map(CustomMode::getCustomModeId).collect(Collectors.toList()));
        dto.put("content",actionLog.getContent());
        return dto;
    }

    @GetMapping("/communications/{serviceProviderId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCommunicationHistory(@PathVariable Long serviceProviderId) {
        try {
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (serviceProvider == null) {
                return ResponseService.generateErrorResponse(
                        "Service Provider not found",
                        HttpStatus.NOT_FOUND
                );
            }

            // JPQL Query with Sorting
            String jpql = """
            SELECT DISTINCT al FROM ActionLog al
            LEFT JOIN FETCH al.content c
            WHERE al.serviceProvider.service_provider_id = :serviceProviderId
            ORDER BY al.actionTimestamp DESC
            """;

            List<ActionLog> actionLogs = entityManager.createQuery(jpql, ActionLog.class)
                    .setParameter("serviceProviderId", serviceProviderId)
                    .setHint(QueryHints.HINT_FETCH_SIZE, 50)  // Optimize performance
                    .setHint(QueryHints.HINT_READONLY, true)  // Optimize performance
                    .getResultList();

            // Initialize customCustomers and customModes separately
            actionLogs.forEach(log -> {
                Hibernate.initialize(log.getCustomCustomers());  // Fetch separately
                Hibernate.initialize(log.getCustomModes());  // Fetch separately
            });

            // Ensure sorting (if DB sorting fails)
            actionLogs.sort(Comparator.comparing(ActionLog::getActionTimestamp).reversed());

            // Convert to DTO
            List<Map<String, Object>> communicationHistory = actionLogs.stream()
                    .map(this::convertToCommunicationDTO)
                    .collect(Collectors.toList());

            return ResponseService.generateSuccessResponse(
                    "Communication history retrieved successfully",
                    communicationHistory,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            // **Throw RuntimeException to Let Spring Manage Rollback**
            throw new RuntimeException("Failed to retrieve communication history", e);
        }
    }


    private Map<String, Object> convertToCommunicationDTO(ActionLog actionLog) {
        Map<String, Object> dto = new HashMap<>();

        dto.put("actionLogId", actionLog.getActionLogId());
        dto.put("timestamp", actionLog.getActionTimestamp());
        dto.put("deliveryStatus", actionLog.getDeliveryStatus());

        // Handle null content
        CommunicationContent content = actionLog.getContent();
        Map<String, Object> contentDetails = new HashMap<>();
        contentDetails.put("subject", content != null ? content.getSubject() : null);
        contentDetails.put("contentText", content != null ? content.getContentText() : null);

        // Handle file attachments safely
        if (content != null && content.getContentFiles() != null && !content.getContentFiles().isEmpty()) {
            List<Map<String, Object>> files = content.getContentFiles().stream()
                    .map(file -> {
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("fileName", file.getFileName());
                        fileInfo.put("size", formatFileSize(file.getSize()));
                        fileInfo.put("fileUrl",fileService.getFileUrl(file.getFilePath(),request));
                        fileInfo.put("fileTypes", file.getFileTypes().stream()
                                .map(FileType::getFile_type_name)
                                .collect(Collectors.toList()));
                        return fileInfo;
                    })
                    .collect(Collectors.toList());
            contentDetails.put("files", files);
        }

        dto.put("content", contentDetails);

        // Recipients
        List<Map<String, Object>> customCustomers = actionLog.getCustomCustomers().stream()
                .map(customer -> {
                    Map<String, Object> recipientInfo = new HashMap<>();
                    recipientInfo.put("customerId", customer.getId());
                    recipientInfo.put("emailAddress", customer.getEmailAddress());
                    recipientInfo.put("name", customer.getFirstName() + " " + customer.getLastName());
                    return recipientInfo;
                })
                .collect(Collectors.toList());
        dto.put("customCustomers", customCustomers);

        // Communication modes
        List<String> modes = actionLog.getCustomModes().stream()
                .map(CustomMode::getCustomModeName)
                .collect(Collectors.toList());
        dto.put("customModes", modes);

        return dto;
    }


    private String formatFileSize(Long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}