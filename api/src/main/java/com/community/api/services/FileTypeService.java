package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.FileType;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileTypeService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ExceptionHandlingService exceptionHandlingService;

    @Transactional
    public List<FileType> getAllRandomFileTypes(Boolean archived) {
        try {
            TypedQuery<FileType> typedQuery = entityManager.createQuery(Constant.GET_ALL_FILE_TYPE, FileType.class);
            typedQuery.setParameter("archived", archived);
            List<FileType> fileTypes = typedQuery.getResultList();
            return fileTypes;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw exception;
        }
    }

    @Transactional
    public List<FileType> getAllArchivedNonArchivedRandomFileTypes() {
        try {
            TypedQuery<FileType> typedQuery = entityManager.createQuery(Constant.GET_ALL_ARCHIVED_NONARCHIVED_FILE_TYPE, FileType.class);
            return typedQuery.getResultList();
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw exception;
        }
    }

    @Transactional
    public List<FileType> addAllRandomFileTypes(List<FileType> fileTypes) throws Exception {
        try {
            List<FileType> fileTypesListToAdd = new ArrayList<>();
            for (FileType fileType : fileTypes) {
                FileType fileTypeToAdd = new FileType();
                int id = findMax() + 1;
                if (fileType.getFile_type_name() == null || fileType.getFile_type_name().trim().isEmpty()) {
                    throw new IllegalArgumentException("File type cannot be empty or consist only of whitespace");
                }
                List<FileType> existingFileType = getAllArchivedNonArchivedRandomFileTypes();
                for (FileType existingFileType1 : existingFileType) {
                    if (existingFileType1.getFile_type_name().trim().equalsIgnoreCase(fileType.getFile_type_name().trim())) {
                        throw new IllegalArgumentException("File Type with name '" + fileType.getFile_type_name().trim() + "' already exists");
                    }
                }
                fileTypeToAdd.setFile_type_id(id);
                fileTypeToAdd.setFile_type_name(fileType.getFile_type_name().trim());
                fileTypesListToAdd.add(fileTypeToAdd);
                entityManager.persist(fileTypeToAdd);
            }
            return fileTypesListToAdd;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public int findMax() {
        try {
            return entityManager.createQuery("SELECT COALESCE(MAX(t.id), 0) FROM FileType t", Integer.class).getSingleResult();
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw exception;
        }
    }

    @Transactional
    public FileType archiveOrUnarchiveFileType(Integer fileTypeId, Boolean archive) {
        FileType fileType = entityManager.find(FileType.class, fileTypeId);
        if (fileType == null) {
            throw new IllegalArgumentException("No file type exists in db with id " + fileTypeId);
        }
        if (archive) {
            if (fileType.getArchived().equals(true)) {
                throw new IllegalArgumentException("File type is already archived");
            }
            fileType.setArchived(true);
        } else {
            if (!fileType.getArchived()) {
                throw new IllegalArgumentException("File type already unarchived");
            }
            fileType.setArchived(false);
        }
        entityManager.merge(fileType);
        return fileType;
    }

    @Transactional
    public FileType updateFileType(FileType fileType, Integer fileTypeId) throws Exception {
        try {
            FileType fileTypeToUpdate = entityManager.find(FileType.class, fileTypeId);
            if (fileTypeToUpdate == null) {
                throw new IllegalArgumentException("File type not found");
            }
            if (fileType.getFile_type_name() != null) {
                if (fileType.getFile_type_name().trim().isEmpty()) {
                    throw new IllegalArgumentException("File type cannot be empty or consist only of whitespace");
                }
                List<FileType> existingFileType = getAllArchivedNonArchivedRandomFileTypes();
                for (FileType existingFileType1 : existingFileType) {
                    if (existingFileType1.getFile_type_name().trim().equalsIgnoreCase(fileType.getFile_type_name().trim()) && !existingFileType1.getFile_type_id().equals(fileTypeId)) {
                        throw new IllegalArgumentException("File Type with name '" + fileType.getFile_type_name().trim() + "' already exists");
                    }
                }
                fileTypeToUpdate.setFile_type_name(fileType.getFile_type_name().trim());
            }
            entityManager.merge(fileTypeToUpdate);
            return fileTypeToUpdate;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }
}
