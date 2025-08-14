package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.AddAdvertisementDto;
import com.community.api.entity.Advertisement;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.Role;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryImpl;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class AdvertisementService {

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    protected CatalogService catalogService;
    @Autowired
    protected ProductService productService;
    @Autowired
    ExceptionHandlingService exceptionHandlingService;
    @Autowired
    EntityManager entityManager;
    @Autowired
    JdbcTemplate jdbcTemplate;

    public Category validateSubCategory(Long categoryId) throws Exception {
        try {
            if (categoryId <= 0) throw new IllegalArgumentException("Category id cannot be <= 0.");
            Category category = catalogService.findCategoryById(categoryId);
            if (category == null || ((Status) category).getArchived() == 'Y') {
                throw new IllegalArgumentException("Category not found with this Id.");
            }
            if (category.getDefaultParentCategory() == null) {
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
            if (addAdvertisementDto.getTitle() == null || addAdvertisementDto.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Title cannot be null or empty");
            }
            addAdvertisementDto.setTitle(addAdvertisementDto.getTitle().trim());

            if (addAdvertisementDto.getDescription() != null) {
           /*     if (addAdvertisementDto.getDescription().trim().isEmpty()) {
                    throw new IllegalArgumentException("Advertisement Description cannot be Empty");
                }*/
                addAdvertisementDto.setDescription(addAdvertisementDto.getDescription().trim());
            }

          /*  if(addAdvertisementDto.getUrl() == null || addAdvertisementDto.getUrl().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Url cannot be null or empty");
            }*/
            if (addAdvertisementDto.getUrl() != null) {
                if ((!isValidUrl(addAdvertisementDto.getUrl().trim()))) {
                    throw new IllegalArgumentException("Invalid Advertisement URL format");
                }
                if (addAdvertisementDto.getUrl() != null || !addAdvertisementDto.getUrl().trim().isEmpty()) {
                    addAdvertisementDto.setUrl(addAdvertisementDto.getUrl().trim());
                }
            }
            /*if(addAdvertisementDto.getNotifyingAuthority() == null || addAdvertisementDto.getNotifyingAuthority().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Notifying Authority cannot be null or empty");
            }*/
            if (addAdvertisementDto.getNotifyingAuthority() != null && !addAdvertisementDto.getNotifyingAuthority().trim().isEmpty()) {
                addAdvertisementDto.setNotifyingAuthority(addAdvertisementDto.getNotifyingAuthority().trim());
            }

         /*   if(addAdvertisementDto.getNumber() == null || addAdvertisementDto.getNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Number cannot be null or empty");
            }*/
            if (addAdvertisementDto.getNumber() != null && !addAdvertisementDto.getNumber().trim().isEmpty()) {
                String number = addAdvertisementDto.getNumber().trim();
                String queryStr = "SELECT q FROM Advertisement q";
                TypedQuery<Advertisement> query = entityManager.createQuery(queryStr, Advertisement.class);
                List<Advertisement> existingAdvertisements = query.getResultList();
                for (Advertisement existingAdvertisement : existingAdvertisements) {
                    if (existingAdvertisement.getNumber() != null) {
                        if (existingAdvertisement.getNumber().equalsIgnoreCase(addAdvertisementDto.getNumber())) {
                            throw new IllegalArgumentException("Advertisement with number " + number + " already exists.");
                        }
                    }
                }
            }

            if (addAdvertisementDto.getNotificationStartDate() == null) {
                throw new IllegalArgumentException("Notification Start Date is required");
            }
            String formattedDate = dateFormat.format(addAdvertisementDto.getNotificationStartDate());
            dateFormat.parse(formattedDate); // Convert formatted date string back to Date

       /*     if(addAdvertisementDto.getNotificationStartDate().after(new Date())) {
                throw new IllegalArgumentException("Notification Start Date cannot be of future");
            }*/
            if (addAdvertisementDto.getNotificationEndDate() == null) {
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
    public Advertisement saveAdvertisement(AddAdvertisementDto addAdvertisementDto, Long creatorUserId, Role role, CategoryImpl category) throws Exception {
        try {

            String formattedDate = dateFormat.format(new Date());
            Date createdDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date

            Advertisement advertisement = new Advertisement();
            advertisement.setTitle(addAdvertisementDto.getTitle());
            advertisement.setUrl(addAdvertisementDto.getUrl());
            advertisement.setDescription(addAdvertisementDto.getDescription());
            advertisement.setNumber(addAdvertisementDto.getNumber());
            advertisement.setCreatedDate(createdDate);
            advertisement.setModifiedDate(createdDate);
            advertisement.setNotifyingAuthority(addAdvertisementDto.getNotifyingAuthority());
            advertisement.setNotificationStartDate(addAdvertisementDto.getNotificationStartDate());
            advertisement.setNotificationEndDate(addAdvertisementDto.getNotificationEndDate());
            advertisement.setCategory(category);
            advertisement.setUserId(creatorUserId);
            advertisement.setAdditionalComments(addAdvertisementDto.getAdditionalComments());
            advertisement.setCreatorRole(role);

            return entityManager.merge(advertisement);
        } catch (PersistenceException persistenceException) {
            exceptionHandlingService.handleException(persistenceException);
            throw new DataIntegrityViolationException("Advertisement number must be unique");
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new Exception("Failed to save Advertisement: " + e.getMessage(), e);
        }
    }

    public List<Advertisement> filterAdvertisements(String title, List<Long> categories, List<Long> subCategories, Long creatorId, Boolean all) throws Exception {
        jdbcTemplate.execute("CALL update_advertisement_product_counts()");
        try {

            // Initialize the JPQL query
            StringBuilder jpql = new StringBuilder("SELECT DISTINCT a FROM Advertisement a ")
                    .append("WHERE 1=1 "); // Use this to simplify appending conditions

            // List to hold query parameters
            List<Category> categoryList = new ArrayList<>();
            List<Category> subCategoryList = new ArrayList<>();

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

            if (subCategories != null && !subCategories.isEmpty()) {
                for (Long id : subCategories) {
                    Category subCategory = catalogService.findCategoryById(id);
                    if (subCategory == null) {
                        throw new IllegalArgumentException("NO SUB CATEGORY FOUND WITH THIS ID: " + id);
                    }
                    if (subCategory.getDefaultParentCategory() == null) {
                        throw new IllegalArgumentException("Advertisement with id " + id + " is not a sub category");
                    }

                    boolean isActive = (((Status) subCategory).getArchived() != 'Y' && subCategory.getActiveEndDate() == null) ||
                            (((Status) subCategory).getArchived() != 'Y' && subCategory.getActiveEndDate().after(new Date()));
                    if (!isActive) {
                        throw new IllegalArgumentException("Advertisement is either archived or expired");
                    }
                    subCategoryList.add(subCategory);
                }
                jpql.append("AND a.category IN :subCategories ");
            }
            if (!all) {
                jpql.append(" AND a.productCount > 0 AND (a.notificationEndDate is null or a.notificationEndDate > CURRENT_TIMESTAMP)  ");
            }
            jpql.append(" AND a.archived = 'N' ");
            if (title != null && !title.isEmpty()) {
                jpql.append("AND LOWER(a.title) LIKE LOWER(:title) ");
            }
            if (creatorId != null) {
                jpql.append("AND a.userId = :uid  ");
            }
            jpql.append("ORDER BY a.modifiedDate DESC");

            System.out.println(jpql);
            // Create the query with the final JPQL string
            TypedQuery<Advertisement> query = entityManager.createQuery(jpql.toString(), Advertisement.class);

            if (!categoryList.isEmpty()) {
                query.setParameter("categories", categoryList);
            }
            if (title != null && !title.isEmpty()) {
                query.setParameter("title", "%" + title + "%");
            }
            if (creatorId != null) {
                query.setParameter("uid", creatorId);
            }
            if (!subCategoryList.isEmpty()) {
                query.setParameter("subCategories", subCategoryList);
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
    public Advertisement updateAdvertisement(AddAdvertisementDto advertisementDto, Long advertisementId) throws Exception {
        jdbcTemplate.execute("CALL update_advertisement_product_counts()");
        Date notificationStartDate = null;
        Date notificationEndDate = null;
        Advertisement advertisementToUpdate = entityManager.find(Advertisement.class, advertisementId);
        if (advertisementToUpdate == null) {
            throw new IllegalArgumentException("Advertisement with id " + advertisementId + " not found");
        }
        if (advertisementDto.getNotificationStartDate() == null || advertisementDto.getNotificationEndDate() == null)
            throw new IllegalArgumentException("Both the Notification start date and end date cannot be null");
        if (advertisementDto.getNewNotificationStartDate() != null) {
            Date today = org.apache.commons.lang3.time.DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH);
            Date notificationStart = org.apache.commons.lang3.time.DateUtils.truncate(advertisementDto.getNewNotificationStartDate(), java.util.Calendar.DAY_OF_MONTH);

            if (notificationStart.before(today)) {
                throw new IllegalArgumentException("Notification start date cannot be in the past");
            }
        }
        if (advertisementToUpdate.getArchived().equals('Y'))
            throw new IllegalArgumentException("Advertisement with id " + advertisementId + " is archived");
        if (advertisementDto.getNewNotificationStartDate() != null && !advertisementDto.getNewNotificationStartDate().equals(advertisementToUpdate.getNotificationStartDate()) && advertisementToUpdate.getProductCount() > 0)
            throw new IllegalArgumentException("Cannot edit the advertisement as it is currently LIVE. Modifying the start date may impact the associated products.");
        if (advertisementToUpdate.getCategory() != null) {
            List<CustomProduct> customProducts = productService.getAllProductsByAdvertisementId(advertisementToUpdate);
            if (customProducts != null && !customProducts.isEmpty()) {
                if (advertisementDto.getNewNotificationStartDate() != null && !advertisementDto.getNewNotificationStartDate().equals(advertisementToUpdate.getNotificationStartDate()) && advertisementToUpdate.getProductCount() > 0)
                    throw new IllegalArgumentException("Cannot edit the advertisement as it is currently LIVE. Modifying the start date may impact the associated products.");
            }
        }

        advertisementToUpdate.setAdditionalComments(advertisementDto.getAdditionalComments());
        if (Objects.nonNull(advertisementDto.getTitle())) {
            if (advertisementDto.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Title cannot be empty");
            }
            advertisementToUpdate.setTitle(advertisementDto.getTitle().trim());
        }
        if (advertisementDto.getDescription() != null) {
           /* if (advertisementDto.getDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Description cannot be Empty");
            }*/
            advertisementToUpdate.setDescription(advertisementDto.getDescription().trim());
        }
        if (Objects.nonNull(advertisementDto.getUrl())) {
          /*  if(advertisementDto.getUrl().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Url cannot be empty");
            }*/
            // URL validation using regex
            if (!isValidUrl(advertisementDto.getUrl().trim())) {
                throw new IllegalArgumentException("Invalid Advertisement URL format");
            }
            advertisementToUpdate.setUrl(advertisementDto.getUrl().trim());
        }
        if (Objects.nonNull(advertisementDto.getNotifyingAuthority())) {
           /* if(advertisementDto.getNotifyingAuthority().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Notifying Authority cannot be empty");
            }*/
            advertisementToUpdate.setNotifyingAuthority(advertisementDto.getNotifyingAuthority().trim());
        }

        if (Objects.nonNull(advertisementDto.getNumber())) {
           /* if(advertisementDto.getNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertisement Number cannot be empty");
            }*/
            if (advertisementDto.getNumber() != null && !advertisementDto.getNumber().trim().isEmpty()) {
                String number = advertisementDto.getNumber().trim();
                String queryStr = "SELECT q FROM Advertisement q";
                TypedQuery<Advertisement> query = entityManager.createQuery(queryStr, Advertisement.class);
                List<Advertisement> existingAdvertisements = query.getResultList();
                for (Advertisement existingAdvertisement : existingAdvertisements) {
                    if (existingAdvertisement.getNumber() != null) {
                        if (existingAdvertisement.getNumber().equalsIgnoreCase(advertisementDto.getNumber()) && !existingAdvertisement.getAdvertisementId().equals(advertisementId)) {
                            throw new IllegalArgumentException("Advertisement with number " + number + " already exists.");
                        }
                    }

                }
                advertisementDto.setNumber(number);
            }

            advertisementToUpdate.setNumber(advertisementDto.getNumber().trim());
        }

        if (Objects.nonNull(advertisementDto.getNotificationStartDate())) {
            String formattedDate = dateFormat.format(advertisementDto.getNotificationStartDate());
            dateFormat.parse(formattedDate);
            notificationStartDate = advertisementDto.getNotificationStartDate();
        } else {
            notificationStartDate = advertisementToUpdate.getNotificationStartDate();
        }

     /*   if(notificationStartDate.after(new Date())) {
            throw new IllegalArgumentException("Notification Start Date cannot be of future");
        }*/
        if (advertisementDto.getNewNotificationStartDate() != null) {
            advertisementToUpdate.setNotificationStartDate(advertisementDto.getNewNotificationStartDate());
        }
       /* else
            advertisementToUpdate.setNotificationStartDate(notificationStartDate);
*/
        if (Objects.nonNull(advertisementDto.getNotificationEndDate())) {
            String formattedDate = dateFormat.format(advertisementDto.getNotificationEndDate());
            dateFormat.parse(formattedDate);
            notificationEndDate = advertisementDto.getNotificationEndDate();
        } else {
            notificationEndDate = advertisementToUpdate.getNotificationEndDate();
        }
        if (advertisementDto.getNewNotificationStartDate() != null && advertisementDto.getNewNotificationStartDate().after(notificationEndDate)) {
            throw new IllegalArgumentException("Notification end date cannot be before of Notification start date");
        }
        if (notificationEndDate != null && notificationEndDate.before(notificationStartDate)) {
            throw new IllegalArgumentException("Notification end date cannot be before of Notification start date");
        }
        advertisementToUpdate.setNotificationEndDate(notificationEndDate);

        advertisementToUpdate.setModifiedDate(new Date());
        entityManager.merge(advertisementToUpdate);
        return advertisementToUpdate;
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Object[]> getAdvCompressed(List<Long> categoryIds, Integer offset, Integer limit) {
        String sql = "SELECT " +
                "a.advertisement_id, " +
                "a.description, " +
                "a.title, " +
                "string_agg(CAST(c.product_id AS TEXT), ',' ORDER BY c.product_id DESC) AS product_ids " +
                "FROM advertisement a " +
                "JOIN custom_product c ON c.advertisement_id = a.advertisement_id " +
                "JOIN blc_product bp ON c.product_id = bp.product_id " +
                "JOIN blc_sku s ON s.sku_id = bp.default_sku_id " +
                "WHERE a.category_id IN (?1) " +
                "AND (a.active_end_date IS NULL OR a.active_end_date >= CURRENT_TIMESTAMP) " +
                "AND a.archived = 'N' " +
                "AND bp.archived = 'N' " +
                "AND c.product_state_id NOT IN (7) " +
                "AND c.is_approved = 'Y'" +
                "AND (s.active_end_date IS NULL OR s.active_end_date >= CURRENT_TIMESTAMP) " +
                "AND c.go_live_date <= CURRENT_TIMESTAMP " +
                "GROUP BY a.advertisement_id, a.description, a.title " +
                "ORDER BY a.modified_date DESC";

        try {
            List<Object[]> rows = entityManager.createNativeQuery(sql)
                    .setParameter(1, categoryIds)
                    .getResultList();

            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch compressed advertisements: " + e.getMessage(), e);
        }
    }

    public BigInteger getAdvCompressedCount(List<Long> categoryIds) {
        String sql = "SELECT " +
                " COUNT(DISTINCT a.advertisement_id) " +
                "FROM advertisement a " +
                "JOIN custom_product c ON c.advertisement_id = a.advertisement_id " +
                "JOIN blc_product bp ON c.product_id = bp.product_id " +
                "JOIN blc_sku s ON s.sku_id = bp.default_sku_id " +
                "WHERE a.category_id IN (:categoryIds) " +
                "AND (a.active_end_date IS NULL OR a.active_end_date >=CURRENT_TIMESTAMP) " +
                "AND a.archived = 'N' " +
                "AND bp.archived = 'N' " +
                "AND c.product_state_id NOT IN (7) " +
                "AND (s.active_end_date IS NULL OR s.active_end_date >= CURRENT_TIMESTAMP) " +
                "AND c.go_live_date <= CURRENT_TIMESTAMP ";

        try {
            BigInteger count = (BigInteger) entityManager.createNativeQuery(sql)
                    .setParameter("categoryIds", categoryIds)
                    .getSingleResult();

            return count;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch compressed advertisements: " + e.getMessage(), e);
        }
    }
}
