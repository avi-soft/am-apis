package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.Image;
import com.community.api.entity.Qualification;
import com.community.api.entity.FileType;
import com.community.api.utils.DocumentType;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileTypeService
{

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public List<FileType> getAllRandomFileTypes()
    {
        TypedQuery<FileType> typedQuery= entityManager.createQuery(Constant.GET_ALL_FILE_TYPE,FileType.class);
        List<FileType> fileTypes = typedQuery.getResultList();
        return fileTypes;
    }

    @Transactional
    public List<FileType> addAllRandomFileTypes(List<FileType> fileTypes)
    {
        List<FileType> fileTypesListToAdd = new ArrayList<>();
        for(FileType fileType : fileTypes)
        {
            FileType fileTypeToAdd =new FileType();
            int id = findCount() + 1;
            if (fileType.getFile_type_name() == null || fileType.getFile_type_name().trim().isEmpty()) {
                throw new IllegalArgumentException("File type cannot be empty or consist only of whitespace");
            }
            List<FileType> existingFileType = getAllRandomFileTypes();
            for (FileType existingFileType1: existingFileType) {
                if (existingFileType1.getFile_type_name().equalsIgnoreCase(fileType.getFile_type_name())) {
                    throw new IllegalArgumentException("File Type with name '"+fileType.getFile_type_name()+"' already exists");
                }
            }
            fileTypeToAdd.setFile_type_id(id);
            fileTypeToAdd.setFile_type_name(fileType.getFile_type_name());
            fileTypesListToAdd.add(fileTypeToAdd);
            entityManager.persist(fileTypeToAdd);
        }
        return fileTypesListToAdd;
    }

    public int findCount() {
        String queryString = Constant.GET_FILE_TYPE_COUNT;
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult().intValue();
    }

    @Transactional
    public FileType archiveOrUnarchiveFileType(Integer fileTypeId, Boolean archive)
    {
        FileType fileType= entityManager.find(FileType.class,fileTypeId);
        if(fileType==null)
        {
            throw new IllegalArgumentException("No file type exists in db with id "+ fileTypeId);
        }
        if (archive) {
            if(fileType.getArchived().equals(true))
            {
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
    public FileType updateFileType(FileType fileType, Integer fileTypeId)
    {
        FileType fileTypeToUpdate= entityManager.find(FileType.class,fileTypeId);
        if(fileTypeToUpdate==null)
        {
            throw new IllegalArgumentException("File type not found");
        }
        if(fileTypeToUpdate.getArchived().equals(true))
        {
            throw new IllegalArgumentException("File type is archived");
        }
        if(fileType.getFile_type_name()!=null)
        {
            if (fileType.getFile_type_name().trim().isEmpty()) {
                throw new IllegalArgumentException("File type cannot be empty or consist only of whitespace");
            }
            List<FileType> existingFileType = getAllRandomFileTypes();
            for (FileType existingFileType1: existingFileType) {
                if (existingFileType1.getFile_type_name().equalsIgnoreCase(fileType.getFile_type_name()) && !existingFileType1.getFile_type_id().equals(fileTypeId)) {
                    throw new IllegalArgumentException("File Type with name '"+fileType.getFile_type_name()+"' already exists");
                }
            }
            fileTypeToUpdate.setFile_type_name(fileType.getFile_type_name());
        }
        entityManager.merge(fileTypeToUpdate);
        return fileTypeToUpdate;
    }
}
