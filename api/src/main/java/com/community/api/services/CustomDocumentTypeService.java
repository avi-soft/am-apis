package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.configuration.ImageSizeConfig;
import com.community.api.dto.DocumentTypeDto;
import com.community.api.entity.FileType;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.utils.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CustomDocumentTypeService
{
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandlingService;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    RoleService roleService;
    @Transactional
    public DocumentType addDocumentTypes(DocumentTypeDto documentTypesToBeSaved, String authHeader) {
        String jwtToken = authHeader.substring(7);

        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);

        String role = roleService.getRoleByRoleId(roleId).getRole_name();

        DocumentType documentTypeToBeSaved = new DocumentType();
        int id = findMax() + 1;
        int secondHighestSortOrder = getSecondHighestSortOrder();
        int sortOrderToBeInserted = secondHighestSortOrder + 1;

        // Validate document type name
        if (documentTypesToBeSaved.getDocument_type_name() == null || documentTypesToBeSaved.getDocument_type_name().trim().isEmpty()) {
            throw new IllegalArgumentException("DocumentType name cannot be empty or consist only of whitespace");
        }

        // Validate description
        if (documentTypesToBeSaved.getDescription() == null || documentTypesToBeSaved.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("DocumentType description cannot be empty or consist only of whitespace");
        }

        // Validate qualification document flag
        if (documentTypesToBeSaved.getIs_qualification_document() == null) {
            throw new IllegalArgumentException("You have to select whether the document type is qualification document or not");
        }

        // Validate and format max document size
        if (documentTypesToBeSaved.getMax_document_size() == null || documentTypesToBeSaved.getMax_document_size().trim().isEmpty()) {
            throw new IllegalArgumentException("DocumentType maximum size cannot be empty or consist only of whitespace");
        }
        validateAndFormatSize(documentTypesToBeSaved.getMax_document_size(), "Maximum");

        // Validate and format min document size
        if (documentTypesToBeSaved.getMin_document_size() == null || documentTypesToBeSaved.getMin_document_size().trim().isEmpty()) {
            throw new IllegalArgumentException("DocumentType minimum size cannot be empty or consist only of whitespace");
        }
        validateAndFormatSize(documentTypesToBeSaved.getMin_document_size(), "Minimum");

        // Validate that max size is greater than min size
        validateSizeComparison(documentTypesToBeSaved.getMin_document_size(), documentTypesToBeSaved.getMax_document_size());

        // Check for duplicate document names
        List<DocumentType> documentTypes = getAllDocumentTypes();
        for (DocumentType existingDocumentType : documentTypes) {
            if (existingDocumentType.getDocument_type_name().equalsIgnoreCase(documentTypesToBeSaved.getDocument_type_name())) {
                throw new IllegalArgumentException("Duplicate document name not allowed");
            }
        }

        // Validate and fetch file types
        List<FileType> validFileTypes = new ArrayList<>();
        if (documentTypesToBeSaved.getRequired_document_type_ids() != null && !documentTypesToBeSaved.getRequired_document_type_ids().isEmpty()) {
            for (Integer fileTypeId : documentTypesToBeSaved.getRequired_document_type_ids()) {
                FileType existingFileType = entityManager.find(FileType.class,fileTypeId);
                if(existingFileType==null)
                {
                    throw new IllegalArgumentException("File type with ID " + fileTypeId + " does not exist");
                }
                validFileTypes.add(existingFileType);
            }
        }

        if(documentTypesToBeSaved.getMin_width_dimension_in_mm()!=null && documentTypesToBeSaved.getMax_width_dimension_in_mm()!=null)
        {
            if(documentTypesToBeSaved.getMin_width_dimension_in_mm()>documentTypesToBeSaved.getMax_width_dimension_in_mm())
            {
                throw new IllegalArgumentException("Min width dimension cannot be larger than the max width dimension");
            }
        }
        if(documentTypesToBeSaved.getMin_height_dimension_in_mm()!=null && documentTypesToBeSaved.getMax_height_dimension_in_mm()!=null)
        {
            if(documentTypesToBeSaved.getMin_height_dimension_in_mm()>documentTypesToBeSaved.getMax_height_dimension_in_mm())
            {
                throw new IllegalArgumentException("Min height dimension cannot be larger than the max height dimension");
            }
        }
        documentTypeToBeSaved.setDocument_type_id(id);
        documentTypeToBeSaved.setDocument_type_name(documentTypesToBeSaved.getDocument_type_name());
        documentTypeToBeSaved.setDescription(documentTypesToBeSaved.getDescription());
        documentTypeToBeSaved.setIs_qualification_document(documentTypesToBeSaved.getIs_qualification_document());
        documentTypeToBeSaved.setMax_document_size(documentTypesToBeSaved.getMax_document_size().toUpperCase());
        documentTypeToBeSaved.setMin_document_size(documentTypesToBeSaved.getMin_document_size().toUpperCase());
        documentTypeToBeSaved.setSort_order(sortOrderToBeInserted);
        documentTypeToBeSaved.setRequired_document_types(validFileTypes);
        entityManager.persist(documentTypeToBeSaved);

        return documentTypeToBeSaved;
    }

    private void validateAndFormatSize(String size, String sizeType) {
        if (size == null || size.trim().isEmpty()) {
            throw new IllegalArgumentException(sizeType + " size cannot be empty");
        }

        String trimmedSize = size.trim().toUpperCase();

        // Regular expression to match size format: number followed by MB, KB, or GB
        String sizePattern = "^\\d+(\\.\\d+)?(B|KB|MB|GB|TB|PB|EB)$";

        if (!trimmedSize.matches(sizePattern)) {
            throw new IllegalArgumentException(sizeType + " size must be in format like '3MB', '500KB', or '1.5GB'");
        }

        String numericPart = trimmedSize.replaceAll("[A-Z]", "");
        String unit = trimmedSize.replaceAll("[0-9.]", "");

        try {
            double sizeValue = Double.parseDouble(numericPart);
            if (sizeValue <= 0) {
                throw new IllegalArgumentException(sizeType + " size must be greater than 0");
            }

            // Convert input size to bytes
            long bytes = 0;
            switch (unit) {
                case "B":
                    bytes = (long) sizeValue;
                    break;
                case "KB":
                    bytes = (long) (sizeValue * 1024);
                    break;
                case "MB":
                    bytes = (long) (sizeValue * 1024 * 1024);
                    break;
                case "GB":
                    bytes = (long) (sizeValue * 1024 * 1024 * 1024);
                    break;
                case "TB":
                    bytes = (long) (sizeValue * 1024 * 1024 * 1024 * 1024);
                    break;
                case "PB":
                    bytes = (long) (sizeValue * 1024 * 1024 * 1024 * 1024 * 1024);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported unit: " + unit);
            }

            long maxBytes = 10L * 1024 * 1024 * 1024;

            if (bytes > maxBytes) {
                throw new IllegalArgumentException(sizeType + " size cannot exceed 10GB");
            }

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(sizeType + " size contains invalid numeric value");
        }
    }

    private void validateSizeComparison(String minSize, String maxSize) {
            long minSizeInBytes = ImageSizeConfig.convertToBytes(minSize.trim().toUpperCase());
            long maxSizeInBytes =ImageSizeConfig.convertToBytes(maxSize.trim().toUpperCase());

            if (minSizeInBytes >= maxSizeInBytes) {
                throw new IllegalArgumentException("Maximum size must be greater than minimum size");
            }
    }

    public List<DocumentType> getAllDocumentTypes() {
        TypedQuery<DocumentType> query = entityManager.createQuery(
                Constant.GET_ALL_DOCUMENT_TYPES, DocumentType.class);
        List<DocumentType> documentTypeList = query.getResultList(); // then execute
        return documentTypeList;
    }

    public int findMax() {
        return  entityManager.createQuery("SELECT COALESCE(MAX(t.id), 0) FROM DocumentType t", Integer.class).getSingleResult();
    }

    @Transactional
    public DocumentType updateDocumentType(Integer documentTypeId, DocumentTypeDto documentType,String authHeader){
        String jwtToken = authHeader.substring(7);

        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);

        String role = roleService.getRoleByRoleId(roleId).getRole_name();
        DocumentType documentTypeToUpdate= entityManager.find(DocumentType.class,documentTypeId);
        if(documentTypeToUpdate==null)
        {
            throw new IllegalArgumentException("DocumentType with id "+ documentTypeId+" not found");
        }
        List<DocumentType> documentTypes = getAllDocumentTypes();
        if (Objects.nonNull(documentType.getDocument_type_name())) {
            for (DocumentType existingDocumentType : documentTypes) {
                if (existingDocumentType.getDocument_type_name().equalsIgnoreCase(documentType.getDocument_type_name()) && !existingDocumentType.getDocument_type_id().equals(documentTypeId)) {
                    throw new IllegalArgumentException("Duplicate document type name not allowed");
                }
            }
            documentTypeToUpdate.setDocument_type_name(documentType.getDocument_type_name());
        }
        if (Objects.nonNull(documentType.getDescription())) {
            if(documentType.getDescription().trim().isEmpty())
            {
                throw new IllegalArgumentException("DocumentType description cannot be empty or consist only of whitespace");
            }
            documentTypeToUpdate.setDescription(documentType.getDescription());
        }
        if (Objects.nonNull(documentType.getIs_qualification_document())) {
            documentTypeToUpdate.setIs_qualification_document(documentType.getIs_qualification_document());
        }
        if (Objects.nonNull(documentType.getMax_document_size())) {
            if(documentType.getMax_document_size().trim().isEmpty())
            {
                throw new IllegalArgumentException("Maximum document size cannot be empty");
            }
            validateAndFormatSize(documentType.getMax_document_size(), "Maximum");
            documentTypeToUpdate.setMax_document_size(documentType.getMax_document_size());
        }
        if(Objects.nonNull(documentType.getMin_document_size()))
        {
            if(documentType.getMin_document_size().trim().isEmpty())
            {
                throw new IllegalArgumentException("Minimum document size cannot be empty");
            }
            validateAndFormatSize(documentType.getMin_document_size(), "Minimum");
            documentTypeToUpdate.setMin_document_size(documentType.getMin_document_size());
        }

        if(documentType.getMax_document_size()!=null && documentType.getMin_document_size()!=null)
        {
            validateSizeComparison(documentType.getMin_document_size(), documentType.getMax_document_size());
        }
        else if(documentType.getMax_document_size() != null)
        {
            validateSizeComparison(documentTypeToUpdate.getMin_document_size(), documentType.getMax_document_size());
        }
        else if(documentType.getMin_document_size() != null)
        {
            validateSizeComparison(documentType.getMin_document_size(), documentTypeToUpdate.getMin_document_size());
        }
        Double minWidth=0.0;
        Double maxWidth=0.0;
        Double minHeight=0.0;
        Double maxHeight=0.0;
        if(documentType.getMin_width_dimension_in_mm()!=null)
        {
            minWidth=documentType.getMin_width_dimension_in_mm();
            documentTypeToUpdate.setMin_width_dimension_in_mm(minWidth);
        }
        else {

            minWidth= documentTypeToUpdate.getMin_width_dimension_in_mm()!=null ?documentTypeToUpdate.getMin_width_dimension_in_mm(): null;
        }

        if(documentType.getMax_width_dimension_in_mm()!=null)
        {
            maxWidth=documentType.getMax_width_dimension_in_mm();
            documentTypeToUpdate.setMax_width_dimension_in_mm(maxWidth);
        }
        else {
            maxWidth= documentTypeToUpdate.getMax_width_dimension_in_mm()!=null ?documentTypeToUpdate.getMax_width_dimension_in_mm(): null;
        }

        if(documentType.getMin_height_dimension_in_mm()!=null)
        {
            minHeight=documentType.getMin_height_dimension_in_mm();
            documentTypeToUpdate.setMin_height_dimension_in_mm(minHeight);
        }
        else {
            minHeight= documentTypeToUpdate.getMin_height_dimension_in_mm()!=null ?documentTypeToUpdate.getMin_height_dimension_in_mm(): null;
        }

        if(documentType.getMax_height_dimension_in_mm()!=null)
        {
            maxHeight=documentType.getMax_height_dimension_in_mm();
            documentTypeToUpdate.setMax_height_dimension_in_mm(maxHeight);
        }
        else {
            maxHeight= documentTypeToUpdate.getMax_height_dimension_in_mm()!=null ?documentTypeToUpdate.getMax_height_dimension_in_mm(): null;
        }

        if(minWidth!=null && maxWidth!=null)
        {
            if(minWidth>maxWidth)
            {
                throw new IllegalArgumentException("Min width dimension cannot be larger than the max width dimension");
            }
        }
        if(minHeight!=null && maxHeight!=null)
        {
            if(minHeight>maxHeight)
            {
                throw new IllegalArgumentException("Min height dimension cannot be larger than the max height dimension");
            }
        }

        // Validate and fetch file types
        List<FileType> validFileTypes = new ArrayList<>();
        if (documentType.getRequired_document_type_ids() != null && documentType.getRequired_document_type_ids().isEmpty()) {
            documentTypeToUpdate.getRequired_document_types().clear();
            documentTypeToUpdate.setRequired_document_types(new ArrayList<>());
        }
        if (documentType.getRequired_document_type_ids() != null && !documentType.getRequired_document_type_ids().isEmpty()) {
            documentTypeToUpdate.getRequired_document_types().clear();
            for (Integer fileTypeId : documentType.getRequired_document_type_ids()) {
                FileType existingFileType = entityManager.find(FileType.class,fileTypeId);
                if(existingFileType==null)
                {
                    throw new IllegalArgumentException("File type with ID " + fileTypeId + " does not exist");
                }
                validFileTypes.add(existingFileType);
            }
            documentTypeToUpdate.setRequired_document_types(validFileTypes);
        }
        return entityManager.merge(documentTypeToUpdate);
    }
    @Transactional
    public DocumentType manageDocumentType(Integer documentTypeId, Boolean active) throws Exception {
        try {
            DocumentType subject = entityManager.find(DocumentType.class, documentTypeId);
            if (subject == null) {
                throw new IllegalArgumentException("Subject not found");
            }

            if (active) {
                if (subject.getArchived()) {
                    throw new IllegalArgumentException("Subject already archived");
                }
                subject.setArchived(true);
            } else {
                if (!subject.getArchived()) {
                    throw new IllegalArgumentException("Subject already unarchived");
                }
                subject.setArchived(false);
            }

            entityManager.merge(subject);
            return subject;
        } catch (Exception e) {
            throw e;
        }
    }

    public DocumentType getDocumentTypeById(Integer documentTypeId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_DOCUMENT_TYPE_BY_ID, DocumentType.class);
            query.setParameter("documentTypeId", documentTypeId);
            List<DocumentType> documentTypes = query.getResultList();

            if (!documentTypes.isEmpty()) {
                if(documentTypes.get(0).getArchived().equals(true)){
                    throw new IllegalArgumentException("Subject is already Archived");
                }
                return documentTypes.get(0);
            }
            return null;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException("Illegal Exception Caught: " + illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new IllegalArgumentException("Exception Caught: " + exception.getMessage());
        }
    }


    public Integer getSecondHighestSortOrder() {
        String jpql = "SELECT d.sort_order FROM DocumentType d WHERE d.sort_order IS NOT NULL ORDER BY d.sort_order DESC";
        List<Integer> sortOrders = entityManager.createQuery(jpql, Integer.class)
                .setMaxResults(2)
                .getResultList();
        return sortOrders.size() == 2 ? sortOrders.get(1) : null;
    }

}

