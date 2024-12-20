package com.community.api.services;

import com.community.api.dto.AddAdvertisementDto;
import com.community.api.entity.Advertisement;
import com.community.api.entity.Role;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class AdvertisementService {

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    EntityManager entityManager;

    public void validateAdvertisement(AddAdvertisementDto addAdvertisementDto) throws Exception {
        try {
            if(addAdvertisementDto.getTitle() == null || addAdvertisementDto.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Title cannot be null or empty");
            }
            addAdvertisementDto.setTitle(addAdvertisementDto.getTitle().trim());

            if(addAdvertisementDto.getDescription() != null) {
                if (addAdvertisementDto.getDescription().trim().isEmpty()) {
                    throw new IllegalArgumentException("Advertisement Description cannot be Empty");
                }
                addAdvertisementDto.setDescription(addAdvertisementDto.getDescription().trim());
            }

            if(addAdvertisementDto.getUrl() == null || addAdvertisementDto.getUrl().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Url cannot be null or empty");
            }
            addAdvertisementDto.setUrl(addAdvertisementDto.getUrl().trim());

            if(addAdvertisementDto.getActiveStartDate() == null) {
                throw new IllegalArgumentException("Active Start Date is required");
            }
            String formattedDate = dateFormat.format(addAdvertisementDto.getActiveStartDate());
            dateFormat.parse(formattedDate); // Convert formatted date string back to Date

            if(addAdvertisementDto.getActiveEndDate() == null) {
                addAdvertisementDto.setActiveEndDate(addAdvertisementDto.getActiveStartDate());
            } else {
                formattedDate = dateFormat.format(addAdvertisementDto.getActiveEndDate());
                dateFormat.parse(formattedDate); // Convert formatted date string back to Date
            }

            if (addAdvertisementDto.getActiveEndDate().before(addAdvertisementDto.getActiveStartDate())) {
                throw new IllegalArgumentException("Active end date cannot be before of active start date");
            }

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new ParseException(parseException.getMessage() + "\n", parseException.getErrorOffset());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage() + "\n");
        }
    }

    @Transactional
    public Advertisement saveAdvertisement (AddAdvertisementDto addAdvertisementDto, Long creatorUserId, Role role, CategoryImpl category) throws Exception {
        try {

           /* // Start building the SQL query
            StringBuilder sql = new StringBuilder("INSERT INTO advertisement (title, number, creator_user_id, creator_role_id, created_date, active_start_date, active_end_date, url, category_id");
            StringBuilder values = new StringBuilder("VALUES (:title, :number , :creatorUserId, :role, :currentDate, :url, :categoryId");

            // Dynamically add columns and values based on non-null fields
            if (addAdvertisementDto.getDescription() != null) {
                sql.append(", description");
                values.append(", :description");
            }

            // Complete the SQL statement
            sql.append(") ").append(values).append(")");

            String formattedDate = dateFormat.format(new Date());
            Date createdDate  = dateFormat.parse(formattedDate); // Convert formatted date string back to Date
            // Create the query
            var query = entityManager.createNativeQuery(sql.toString())
                    .setParameter("title", addAdvertisementDto.getTitle())
                    .setParameter("creatorUserId", creatorUserId)
                    .setParameter("role", role)
                    .setParameter("number", addAdvertisementDto.getNumber())
                    .setParameter("url", addAdvertisementDto.getUrl())
                    .setParameter("categoryId", category.getId())
                    .setParameter("currentDate", createdDate);

            // Set parameters conditionally
            if (addAdvertisementDto.getDescription() != null) {
                query.setParameter("description", addAdvertisementDto.getDescription());
            }

            // Execute the update
            query.executeUpdate();
*/
            String formattedDate = dateFormat.format(new Date());
            Date createdDate  = dateFormat.parse(formattedDate); // Convert formatted date string back to Date

            Advertisement advertisement = new Advertisement();
            advertisement.setTitle(addAdvertisementDto.getTitle());
            advertisement.setUrl(addAdvertisementDto.getUrl());
            advertisement.setDescription(addAdvertisementDto.getDescription());
            advertisement.setNumber(addAdvertisementDto.getNumber());
            advertisement.setCreatedDate(createdDate);
            advertisement.setActiveStartDate(addAdvertisementDto.getActiveStartDate());
            advertisement.setActiveEndDate(addAdvertisementDto.getActiveEndDate());
            advertisement.setCategory(category);
            advertisement.setUserId(creatorUserId);
            advertisement.setCreatorRole(role);

            return entityManager.merge(advertisement);
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new Exception("Failed to save Advertisement: " + e.getMessage(), e);
        }
    }
}
