package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReserveCategoryService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomReserveCategory> getAllReserveCategory(Boolean archived) {
        try {
            if (archived == null)
                archived = false;
            List<CustomReserveCategory> reserveCategories = entityManager
                    .createNativeQuery(Constant.GET_ALL_RESERVED_CATEGORY, CustomReserveCategory.class)
                    .setParameter("archived", archived)
                    .getResultList();

            // Filter out entries with ID 0
            List<CustomReserveCategory> filteredList = reserveCategories.stream()
                    .filter(category -> category.getReserveCategoryId() != 0)
                    .collect(Collectors.toList());

            return filteredList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    public CustomReserveCategory getReserveCategoryById(Long reserveCategoryId) {
        try {
            Query query = entityManager.createQuery(Constant.GET_RESERVED_CATEGORY_BY_ID, CustomReserveCategory.class);
            query.setParameter("reserveCategoryId", reserveCategoryId);
            List<CustomReserveCategory> reserveCategory = query.getResultList();
            if (!reserveCategory.isEmpty()) {
                return reserveCategory.get(0);
            } else {
                return null;
            }
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    public CustomReserveCategory getCategoryByName(String name) {
        try {
            return entityManager.createQuery(Constant.GET_RESERVE_CATEGORY_BY_ID, CustomReserveCategory.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    public Double getReserveCategoryFee(Long pid, Long reserveCategoryId, Long genderId) {
        Query query = entityManager.createNativeQuery(Constant.GET_RESERVE_CATEGORY_FEE);
        query.setParameter("pid", pid);
        query.setParameter("reserveCategoryId", reserveCategoryId);
        query.setParameter("genderId", genderId);

        try {
            return (Double) query.getSingleResult();
        } catch (NoResultException e) {
            exceptionHandlingService.handleException(e);
            return null; // Return null if no result is found
        }
    }

    public Long getMaxReserveCategoryId() throws Exception {
        try {
            String query = "SELECT MAX(reserve_category_id) FROM custom_reserve_category";
            Query nquery = entityManager.createNativeQuery(query);
            return ((Number) nquery.getSingleResult()).longValue();
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Integer getNextSortOrder() throws Exception {
        try {
            String query = "SELECT COALESCE(MAX(sort_order), 0) FROM custom_reserve_category WHERE sort_order < 1000";
            Query nquery = entityManager.createNativeQuery(query);
            Integer maxSortOrder = ((Number) nquery.getSingleResult()).intValue();
            return maxSortOrder + 1;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    @Transactional
    public CustomReserveCategory addReserveCategory(CustomReserveCategory reserveCategory)
            throws IllegalArgumentException, Exception {
        try {
            // Validate input
            if (reserveCategory.getReserveCategoryId() != null) {
                throw new IllegalArgumentException("Cannot provide reserve category ID when adding");

            }

            if (reserveCategory.getReserveCategoryName() == null || reserveCategory.getReserveCategoryName().isEmpty()) {
                throw new IllegalArgumentException("Reserve category name is required");
            }

            // Check for duplicate names
            Query query = entityManager.createQuery(
                    "SELECT r FROM CustomReserveCategory r WHERE LOWER(r.reserveCategoryName) = LOWER(:name)",
                    CustomReserveCategory.class);
            query.setParameter("name", reserveCategory.getReserveCategoryName());
            if (!query.getResultList().isEmpty()) {
                throw new IllegalArgumentException("Reserve category with this name already exists");
            }

            // Set default values
            reserveCategory.setReserveCategoryId(getMaxReserveCategoryId() + 1);
            reserveCategory.setArchived(false);

            if (reserveCategory.getSortOrder() == null || reserveCategory.getSortOrder() >= 1000) {
                // Assign next available sort order for normal categories
                reserveCategory.setSortOrder(getNextSortOrder());
            }

            if (reserveCategory.getReserveCategoryId() == null) {
                reserveCategory.setIsReservedCategory(false);
            }

            reserveCategory.setIsReservedCategory(false);
            entityManager.persist(reserveCategory);
            return reserveCategory;
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw e;
        }
    }

    @Transactional
    public CustomReserveCategory editReserveCategory(Long reserveCategoryId, CustomReserveCategory reserveCategory)
            throws IllegalArgumentException, Exception {
        try {
            CustomReserveCategory existingCategory = entityManager.find(CustomReserveCategory.class, reserveCategoryId);
            if (existingCategory == null) {
                throw new IllegalArgumentException("Reserve category not found");
            }

            // Validate input
            if (reserveCategory.getReserveCategoryId() != null &&
                    !reserveCategory.getReserveCategoryId().equals(reserveCategoryId)) {
                throw new IllegalArgumentException("Cannot change reserve category ID");
            }

            if (reserveCategory.getReserveCategoryName() != null) {
                // Check for duplicate names
                Query query = entityManager.createQuery(
                        "SELECT r FROM CustomReserveCategory r WHERE LOWER(r.reserveCategoryName) = LOWER(:name) AND r.reserveCategoryId != :id",
                        CustomReserveCategory.class);
                query.setParameter("name", reserveCategory.getReserveCategoryName());
                query.setParameter("id", reserveCategoryId);
                if (!query.getResultList().isEmpty()) {
                    throw new IllegalArgumentException("Reserve category with this name already exists");
                }
                existingCategory.setReserveCategoryName(reserveCategory.getReserveCategoryName());
            }

            if (reserveCategory.getReserveCategoryDescription() != null) {
                existingCategory.setReserveCategoryDescription(reserveCategory.getReserveCategoryDescription());
            }

            if (reserveCategory.getIsReservedCategory() != null) {
                existingCategory.setIsReservedCategory(reserveCategory.getIsReservedCategory());
            }

            // Only update sort order if explicitly provided and valid
            if (reserveCategory.getSortOrder() != null && reserveCategory.getSortOrder() < 1000) {
                existingCategory.setSortOrder(reserveCategory.getSortOrder());
            }

            entityManager.merge(existingCategory);
            return existingCategory;
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw e;
        }
    }

    @Transactional
    public CustomReserveCategory manageReserveCategory(Long reserveCategoryId, Boolean archive)
            throws IllegalArgumentException, Exception {
        try {
            CustomReserveCategory existingCategory = entityManager.find(CustomReserveCategory.class, reserveCategoryId);
            if (existingCategory == null) {
                throw new IllegalArgumentException("Reserve category not found");
            }

            if (archive) {
                if (existingCategory.getIsReservedCategory()) {
                    throw new IllegalArgumentException("Cannot archive default reserve category");
                }
                if (Boolean.TRUE.equals(existingCategory.getArchived())) {
                    throw new IllegalArgumentException("Reserve category is already archived");
                }
                existingCategory.setArchived(true);
                // Optionally move to high sort order when archiving
                existingCategory.setSortOrder(1000 + existingCategory.getReserveCategoryId().intValue());
            } else {
                if (Boolean.FALSE.equals(existingCategory.getArchived())) {
                    throw new IllegalArgumentException("Reserve category is already active");
                }
                existingCategory.setArchived(false);
                // When unarchiving, assign next available sort order
                existingCategory.setSortOrder(getNextSortOrder());
            }

            entityManager.merge(existingCategory);
            return existingCategory;
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw e;
        }
    }
}
