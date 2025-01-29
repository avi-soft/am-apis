package com.community.api.endpoint.avisoft.controller;

import com.community.api.dto.CommunicationRequest;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.*;
import com.community.api.services.EmailService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/service-provider-actions")
public class ServiceProviderActionController {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    private EmailService emailService; // You'll need to implement this

//    @Autowired
//    private WhatsappService whatsappService; // You'll need to implement this

    @PostMapping("/communicate/{serviceProviderId}")
    @Transactional
    public ResponseEntity<?> communicateWithCustomers(
            @RequestBody CommunicationRequest request,
            @PathVariable Long serviceProviderId) {
        try {
            // Validate service provider
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (serviceProvider == null) {
                return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.NOT_FOUND);
            }

            if(request.getSubject()==null)
            {
                return ResponseService.generateErrorResponse("subject/title of message cannot be null",HttpStatus.BAD_REQUEST);
            }
            if(request.getSubject().trim().isEmpty())
            {
                return ResponseService.generateErrorResponse("subject/title of message cannot be empty",HttpStatus.BAD_REQUEST);
            }
            if(request.getContentText()==null && (request.getFiles()==null || request.getFiles().isEmpty()))
            {
                return ResponseService.generateErrorResponse("Either you have to provide text or any file.Both cannot be null",HttpStatus.BAD_REQUEST);
            }
            if ((request.getContentText() == null || request.getContentText().isEmpty()) &&
                    (request.getFiles() == null || request.getFiles().isEmpty())) {
                return ResponseService.generateErrorResponse(
                        "Either you have to provide text or any file. Both cannot be empty",
                        HttpStatus.BAD_REQUEST
                );
            }

            // Create communication content
            CommunicationContent content = new CommunicationContent();
            content.setServiceProvider(serviceProvider);
            content.setContentText(request.getContentText().trim());
            content.setSubject(request.getSubject().trim());

            // Handle file attachments if any
            if (request.getFiles() != null && !request.getFiles().isEmpty()) {
                List<ContentFile> contentFiles = processFiles(request.getFiles(), content);
                content.setContentFiles(contentFiles);
            }

            entityManager.persist(content);

            // Create a single action log for all customers
            ActionLog actionLog = new ActionLog();
            actionLog.setServiceProvider(serviceProvider);
            actionLog.setContent(content);

            // Fetch and set all customers
            List<CustomCustomer> customers = request.getCustomerIds().stream()
                    .map(id -> entityManager.find(CustomCustomer.class, id))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            actionLog.setCustomCustomers(customers);

            // Validate and set custom modes
            for (Integer customModeId : request.getModes()) {
                CustomMode customMode = entityManager.find(CustomMode.class, customModeId);
                if (customMode == null) {
                    throw new IllegalArgumentException("Custom mode with id " + customModeId + " does not exist");
                }
            }

            List<CustomMode> modes = request.getModes().stream()
                    .map(modeId -> entityManager.find(CustomMode.class, modeId))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            actionLog.setCustomModes(modes);
            actionLog.setActionTimestamp(LocalDateTime.now());

            // Send communications based on selected modes
            StringBuilder deliveryStatus = new StringBuilder();
            for (CustomMode mode : modes) {
                for (CustomCustomer customer : customers) {
                    try {
                        if (mode.getCustomModeId().equals(1)) { // Assuming 1 corresponds to "email"
                            emailService.sendEmail(customer.getEmailAddress(), content);
                        }
                        //        else if (mode.getCustomModeId() == 2) { // Assuming 2 corresponds to "whatsapp"
                        //            whatsappService.sendMessage(customer.getPhone(), content);
                        //        }
                    } catch (Exception e) {
                        deliveryStatus.append(String.format("Failed for customer %d via mode %d: %s; ",
                                customer.getId(), mode.getCustomModeId(), e.getMessage()));
                    }
                }
            }

            actionLog.setDeliveryStatus(deliveryStatus.length() > 0 ?
                    "PARTIALLY_FAILED: " + deliveryStatus.toString() : "SUCCESS");

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
        return files.stream().map(file -> {
            ContentFile contentFile = new ContentFile();
            contentFile.setFileName(file.getOriginalFilename());
            contentFile.setSize(file.getSize());
            contentFile.setCommunicationContent(content);
            // Set file path after saving file to your storage
            contentFile.setFilePath(saveFile(file));
            // Set appropriate file types
            contentFile.setFileTypes(determineFileTypes(file.getContentType()));
            return contentFile;
        }).collect(Collectors.toList());
    }

    private String saveFile(MultipartFile file) {
        // Implement file saving logic
        return "path/to/saved/file";
    }

    private List<FileType> determineFileTypes(String contentType) {
        // Implement file type determination logic
        return new ArrayList<>();
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

    @GetMapping("/logs")
    public ResponseEntity<?> getActionLogs(
            @RequestParam(required = false) Long spId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Integer modeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String deliveryStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // First get the IDs of matching action logs
            StringBuilder idQueryBuilder = new StringBuilder(
                    "SELECT DISTINCT al.actionLogId FROM ActionLog al " +
                            "WHERE 1=1");

            Map<String, Object> parameters = new HashMap<>();

            if (spId != null) {
                idQueryBuilder.append(" AND al.serviceProvider.service_provider_id = :spId");
                parameters.put("spId", spId);
            }

            if (customerId != null) {
                idQueryBuilder.append(" AND :customerId IN (SELECT cc.id FROM al.customCustomers cc)");
                parameters.put("customerId", customerId);
            }

            if (modeId != null) {
                idQueryBuilder.append(" AND :modeId IN (SELECT cm.customModeId FROM al.customModes cm)");
                parameters.put("modeId", modeId);
            }

            if (startDate != null) {
                idQueryBuilder.append(" AND al.actionTimestamp >= :startDate");
                parameters.put("startDate", startDate);
            }

            if (endDate != null) {
                idQueryBuilder.append(" AND al.actionTimestamp <= :endDate");
                parameters.put("endDate", endDate);
            }

            if (deliveryStatus != null) {
                idQueryBuilder.append(" AND al.deliveryStatus LIKE :deliveryStatus");
                parameters.put("deliveryStatus", "%" + deliveryStatus + "%");
            }

            idQueryBuilder.append(" ORDER BY al.actionTimestamp DESC");

            // Get paginated IDs
            TypedQuery<Long> idQuery = entityManager.createQuery(idQueryBuilder.toString(), Long.class);
            parameters.forEach(idQuery::setParameter);
            idQuery.setFirstResult(page * size);
            idQuery.setMaxResults(size);
            List<Long> actionLogIds = idQuery.getResultList();

            List<ActionLog> actionLogs = new ArrayList<>();
            if (!actionLogIds.isEmpty()) {
                // Get full action logs with base data
                actionLogs = entityManager.createQuery(
                                "SELECT DISTINCT al FROM ActionLog al " +
                                        "LEFT JOIN FETCH al.serviceProvider " +
                                        "LEFT JOIN FETCH al.content c " +
                                        "LEFT JOIN FETCH c.contentFiles " +
                                        "WHERE al.actionLogId IN :ids " +
                                        "ORDER BY al.actionTimestamp DESC", ActionLog.class)
                        .setParameter("ids", actionLogIds)
                        .getResultList();

                // Fetch customers separately
                entityManager.createQuery(
                                "SELECT DISTINCT al FROM ActionLog al " +
                                        "LEFT JOIN FETCH al.customCustomers " +
                                        "WHERE al.actionLogId IN :ids")
                        .setParameter("ids", actionLogIds)
                        .getResultList();

                // Fetch modes separately
                entityManager.createQuery(
                                "SELECT DISTINCT al FROM ActionLog al " +
                                        "LEFT JOIN FETCH al.customModes " +
                                        "WHERE al.actionLogId IN :ids")
                        .setParameter("ids", actionLogIds)
                        .getResultList();
            }

            // Count total results
            String countQueryString = "SELECT COUNT(DISTINCT al.actionLogId) FROM ActionLog al WHERE 1=1";
            if (!parameters.isEmpty()) {
                countQueryString = idQueryBuilder.toString()
                        .replace("SELECT DISTINCT al.actionLogId", "SELECT COUNT(DISTINCT al.actionLogId)")
                        .substring(0, idQueryBuilder.toString().indexOf(" ORDER BY"));
            }
            TypedQuery<Long> countQuery = entityManager.createQuery(countQueryString, Long.class);
            parameters.forEach(countQuery::setParameter);
            Long totalElements = countQuery.getSingleResult();

            // Convert to DTOs
            List<Map<String, Object>> dtos = actionLogs.stream()
                    .map(this::convertToDetailedDTO)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("content", dtos);
            response.put("totalElements", totalElements);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalPages", (int) Math.ceil((double) totalElements / size));

            return ResponseService.generateSuccessResponse(
                    "Action logs retrieved successfully",
                    response,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            return ResponseService.generateErrorResponse(
                    "Failed to retrieve action logs: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private Map<String, Object> convertToDetailedDTO(ActionLog actionLog) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("actionLogId", actionLog.getActionLogId());
        dto.put("timestamp", actionLog.getActionTimestamp());
        dto.put("deliveryStatus", actionLog.getDeliveryStatus());

        // Service Provider details
        Map<String, Object> spDetails = new HashMap<>();
        spDetails.put("id", actionLog.getServiceProvider().getService_provider_id());
        dto.put("serviceProvider", spDetails);

        // Customer details
        List<Map<String, Object>> customerDetails = actionLog.getCustomCustomers().stream()
                .map(customer -> {
                    Map<String, Object> customerMap = new HashMap<>();
                    customerMap.put("id", customer.getId());
                    customerMap.put("emailAddress", customer.getEmailAddress());
                    return customerMap;
                })
                .collect(Collectors.toList());
        dto.put("customers", customerDetails);

        // Communication modes
        List<Map<String, Object>> modeDetails = actionLog.getCustomModes().stream()
                .map(mode -> {
                    Map<String, Object> modeMap = new HashMap<>();
                    modeMap.put("id", mode.getCustomModeId());
                    return modeMap;
                })
                .collect(Collectors.toList());
        dto.put("communicationModes", modeDetails);

        // Content details
        Map<String, Object> contentDetails = new HashMap<>();
        contentDetails.put("id", actionLog.getContent().getContentId());
        contentDetails.put("subject", actionLog.getContent().getSubject());
        contentDetails.put("contentText", actionLog.getContent().getContentText());

        if (actionLog.getContent().getContentFiles() != null) {
            List<Map<String, Object>> fileDetails = actionLog.getContent().getContentFiles().stream()
                    .map(file -> {
                        Map<String, Object> fileMap = new HashMap<>();
                        fileMap.put("fileName", file.getFileName());
                        fileMap.put("filePath", file.getFilePath());
                        return fileMap;
                    })
                    .collect(Collectors.toList());
            contentDetails.put("files", fileDetails);
        }

        dto.put("content", contentDetails);

        return dto;
    }
}