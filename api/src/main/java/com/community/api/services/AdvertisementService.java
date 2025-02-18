package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.AddAdvertisementDto;
import com.community.api.entity.Advertisement;
import com.community.api.entity.BoardUniversity;
import com.community.api.entity.Role;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryImpl;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AdvertisementService {

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    protected CatalogService catalogService;

    @Autowired
    EntityManager entityManager;

    public Category validateSubCategory(Long categoryId) throws Exception {
        try {
            if (categoryId <= 0) throw new IllegalArgumentException("Category id cannot be <= 0.");
            Category category = catalogService.findCategoryById(categoryId);
            if (category == null || ((Status) category).getArchived() == 'Y') {
                throw new IllegalArgumentException("Category not found with this Id.");
            }
            if(category.getDefaultParentCategory() == null) {
                throw new IllegalArgumentException("Category is not a sub category.");
            }
            return category;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught while validating category: " + exception.getMessage() + "\n");
        }
    }

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
            if (!isValidUrl(addAdvertisementDto.getUrl().trim())) {
                throw new IllegalArgumentException("Invalid Advertisement URL format");
            }
            addAdvertisementDto.setUrl(addAdvertisementDto.getUrl().trim());

            if(addAdvertisementDto.getNotifyingAuthority() == null || addAdvertisementDto.getNotifyingAuthority().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Notifying Authority cannot be null or empty");
            }
            addAdvertisementDto.setNotifyingAuthority(addAdvertisementDto.getNotifyingAuthority().trim());

            if(addAdvertisementDto.getNumber() == null || addAdvertisementDto.getNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Number cannot be null or empty");
            }
            addAdvertisementDto.setNumber(addAdvertisementDto.getNumber().trim());

            if(addAdvertisementDto.getNotificationStartDate() == null) {
                throw new IllegalArgumentException("Notification Start Date is required");
            }
            String formattedDate = dateFormat.format(addAdvertisementDto.getNotificationStartDate());
            dateFormat.parse(formattedDate); // Convert formatted date string back to Date

            if(addAdvertisementDto.getNotificationStartDate().after(new Date())) {
                throw new IllegalArgumentException("Notification Start Date cannot be of future");
            }
            if(addAdvertisementDto.getNotificationEndDate() == null) {
                addAdvertisementDto.setNotificationEndDate(null);
            } else {
                formattedDate = dateFormat.format(addAdvertisementDto.getNotificationEndDate());
                dateFormat.parse(formattedDate); // Convert formatted date string back to Date
            }

            if (addAdvertisementDto.getNotificationEndDate() != null && addAdvertisementDto.getNotificationEndDate().before(addAdvertisementDto.getNotificationStartDate())) {
                throw new IllegalArgumentException("Notification end date cannot be before of Notification start date");
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

    public Advertisement getAdvertisementById(Long advertisementId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_ADVERTISEMENT_BY_ID, Advertisement.class);
            query.setParameter("advertisementId", advertisementId);
            List<Advertisement> advertisements = query.getResultList();

            if (!advertisements.isEmpty()) {
                return advertisements.get(0);
            } else {
                return null;
            }
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
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
            advertisement.setNotifyingAuthority(addAdvertisementDto.getNotifyingAuthority());
            advertisement.setNotificationStartDate(addAdvertisementDto.getNotificationStartDate());
            advertisement.setNotificationEndDate(addAdvertisementDto.getNotificationEndDate());
            advertisement.setCategory(category);
            advertisement.setUserId(creatorUserId);
            advertisement.setCreatorRole(role);

            return entityManager.merge(advertisement);
        } catch (PersistenceException persistenceException) {
            exceptionHandlingService.handleException(persistenceException);
            throw new DataIntegrityViolationException("Data Constraint Violation number and url must be unique");
        }
        catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new Exception("Failed to save Advertisement: " + e.getMessage(), e);
        }
    }

    public List<Advertisement> filterAdvertisements(String title, List<Long> categories) throws Exception {

        try {

            // Initialize the JPQL query
            StringBuilder jpql = new StringBuilder("SELECT DISTINCT a FROM Advertisement a ")
                    .append("WHERE 1=1 "); // Use this to simplify appending conditions

            // List to hold query parameters
            List<Category> categoryList = new ArrayList<>();

            if (categories != null && !categories.isEmpty()) {
                for (Long id : categories) {
                    Category category = catalogService.findCategoryById(id);
                    if (category == null) {
                        throw new IllegalArgumentException("NO CATEGORY FOUND WITH THIS ID: " + id);
                    }
                    categoryList.add(category);
                }
                jpql.append("AND a.category IN :categories ");
            }


            if (title != null && !title.isEmpty()) {
                jpql.append("AND a.title LIKE :title ");
            }

            // Create the query with the final JPQL string
            TypedQuery<Advertisement> query = entityManager.createQuery(jpql.toString(), Advertisement.class);

            if (!categoryList.isEmpty()) {
                query.setParameter("categories", categoryList);
            }
            if (title != null && !title.isEmpty()) {
                query.setParameter("title", "%" + title + "%");
            }

            // Execute and return the result
            return query.getResultList();

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException("Illegal Argument Exception Caught: " + illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION CAUGHT: " + exception.getMessage());
        }
    }

    @Transactional
    public Advertisement updateAdvertisement(AddAdvertisementDto advertisementDto,Long advertisementId) throws Exception {
        Date notificationStartDate=null;
        Date notificationEndDate=null;
        Advertisement advertisementToUpdate= entityManager.find(Advertisement.class,advertisementId);
        if(advertisementToUpdate==null)
        {
            throw new IllegalArgumentException("Advertisement with id "+ advertisementId+" not found");
        }
        if(Objects.nonNull(advertisementDto.getTitle()))
        {
            if(advertisementDto.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Title cannot be empty");
            }
            advertisementToUpdate.setTitle(advertisementDto.getTitle().trim());
        }
        if(advertisementDto.getDescription() != null) {
            if (advertisementDto.getDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Description cannot be Empty");
            }
            advertisementToUpdate.setDescription(advertisementDto.getDescription().trim());
        }
        if (Objects.nonNull(advertisementDto.getUrl())) {
            if(advertisementDto.getUrl().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Url cannot be empty");
            }
            // URL validation using regex
            if (!isValidUrl(advertisementDto.getUrl().trim())) {
                throw new IllegalArgumentException("Invalid Advertisement URL format");
            }
            advertisementToUpdate.setUrl(advertisementDto.getUrl().trim());
        }
        if (Objects.nonNull(advertisementDto.getNotifyingAuthority())) {
            if(advertisementDto.getNotifyingAuthority().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Notifying Authority cannot be empty");
            }
            advertisementToUpdate.setNotifyingAuthority(advertisementDto.getNotifyingAuthority().trim());
        }

        if (Objects.nonNull(advertisementDto.getNumber())) {
            if(advertisementDto.getNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Number cannot be empty");
            }
            advertisementToUpdate.setNumber(advertisementDto.getNumber().trim());
        }

        if (Objects.nonNull(advertisementDto.getNotificationStartDate())) {
            String formattedDate = dateFormat.format(advertisementDto.getNotificationStartDate());
            dateFormat.parse(formattedDate);
            notificationStartDate=advertisementDto.getNotificationStartDate();
        }
        else {
            notificationStartDate=advertisementToUpdate.getNotificationStartDate();
        }

        if(notificationStartDate.after(new Date())) {
            throw new IllegalArgumentException("Notification Start Date cannot be of future");
        }

        advertisementToUpdate.setNotificationStartDate(notificationStartDate);

        if (Objects.nonNull(advertisementDto.getNotificationEndDate())) {
            String formattedDate = dateFormat.format(advertisementDto.getNotificationEndDate());
            dateFormat.parse(formattedDate);
            notificationEndDate=advertisementDto.getNotificationEndDate();
        }
        else {
            notificationEndDate=advertisementToUpdate.getNotificationEndDate();
        }

        if (notificationEndDate != null && notificationEndDate.before(notificationStartDate)) {
            throw new IllegalArgumentException("Notification end date cannot be before of Notification start date");
        }
        advertisementToUpdate.setNotificationEndDate(notificationEndDate);

        entityManager.merge(advertisementToUpdate);
        return advertisementToUpdate;
    }

    private boolean isValidUrl(String url) {
        String urlRegex = "^(https?:\\/\\/)?(www\\.)?([\\w.-]+)\\.([a-zA-Z]{2,})([\\/\\w .-]*)*\\/?$";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

}
