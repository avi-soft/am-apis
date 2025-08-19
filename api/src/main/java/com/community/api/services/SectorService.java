package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.AddSectorDto;
import com.community.api.entity.CustomSector;
import com.community.api.services.exception.ExceptionHandlingService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Service
public class SectorService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomSector> getAllSector(Boolean archived) {
        try {
            List<CustomSector> sectorList = entityManager.createQuery(Constant.GET_ALL_SECTOR, CustomSector.class).setParameter("archived",archived)
                    .getResultList();
            return sectorList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return Collections.emptyList();
        }
    }

    public CustomSector getSectorBySectorId(Long sectorId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_SECTOR_BY_SECTOR_ID, CustomSector.class);
            query.setParameter("sectorId", sectorId);
            List<CustomSector> sector = query.getResultList();

            if (!sector.isEmpty()) {
                return sector.get(0);
            } else {
                return null;
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
    public Boolean validateAddSubjectDto(AddSectorDto addSectorDto) throws Exception {
        try{
            if(addSectorDto.getSectorName() == null || addSectorDto.getSectorDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("SECTOR NAME CANNOT BE NULL OR EMPTY");
            }
            if(addSectorDto.getSectorDescription() != null && addSectorDto.getSectorDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("SECTOR DESCRIPTION CANNOT BE EMPTY");
            }
            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: "+ exception.getMessage());
        }
    }

    @Transactional
    public CustomSector addSector(AddSectorDto addSectorDto) throws IllegalArgumentException, Exception {
        try {
            // Check if sector name is provided
            if (addSectorDto.getSectorName() == null || addSectorDto.getSectorName().trim().isEmpty()) {
                throw new IllegalArgumentException("Sector name is required");
            }

            // Check for duplicate sector name (case insensitive)
            Query query = entityManager.createQuery(
                    "SELECT s FROM CustomSector s WHERE LOWER(s.sectorName) = LOWER(:sectorName)",
                    CustomSector.class);
            query.setParameter("sectorName", addSectorDto.getSectorName());
            List<CustomSector> sectors = query.getResultList();

            if (!sectors.isEmpty()) {
                throw new IllegalArgumentException("Sector with this name already exists");
            }

            // Create new sector entity
            CustomSector newSector = new CustomSector();
            newSector.setSectorName(addSectorDto.getSectorName());
            newSector.setSectorDescription(addSectorDto.getSectorDescription());
            newSector.setArchived(false); // Default to not archived

            // Persist the new sector
            entityManager.persist(newSector);
            return newSector;

        } catch (IllegalArgumentException e) {
            throw e; // Re-throw validation exceptions directly
        } catch (Exception e) {
            throw new Exception("Failed to add sector: " + e.getMessage());
        }
    }
    @Transactional
    public CustomSector editSector(Long sectorId, AddSectorDto addSectorDto)
            throws IllegalArgumentException, Exception {

        try {
            // Find existing sector
            CustomSector existingSector = entityManager.find(CustomSector.class, sectorId);
            if (existingSector == null) {
                throw new IllegalArgumentException("Sector not found with id: " + sectorId);
            }

            // Validate sector name
            if (addSectorDto.getSectorName() == null || addSectorDto.getSectorName().trim().isEmpty()) {
                throw new IllegalArgumentException("Sector name is required");
            }

            // Check for duplicate sector name (excluding current sector)
            Query query = entityManager.createQuery(
                    "SELECT s FROM CustomSector s WHERE LOWER(s.sectorName) = LOWER(:sectorName) AND s.sectorId != :sectorId",
                    CustomSector.class);
            query.setParameter("sectorName", addSectorDto.getSectorName());
            query.setParameter("sectorId", sectorId);
            List<CustomSector> sectors = query.getResultList();

            if (!sectors.isEmpty()) {
                throw new IllegalArgumentException("Sector with this name already exists");
            }

            // Update the sector
            existingSector.setSectorName(addSectorDto.getSectorName());

            // Description can be null, so only set if provided
            if (addSectorDto.getSectorDescription() != null) {
                existingSector.setSectorDescription(addSectorDto.getSectorDescription());
            }

            // Merge changes
            entityManager.merge(existingSector);
            return existingSector;

        } catch (IllegalArgumentException e) {
            throw e; // Re-throw validation exceptions directly
        } catch (Exception e) {
            throw new Exception("Failed to update sector: " + e.getMessage());
        }
    }
    @Transactional
    public CustomSector manageSector(Long sectorId,Boolean archive) throws NotFoundException {
        CustomSector customSector=entityManager.find(CustomSector.class,sectorId);
        if(customSector==null)
            throw new NotFoundException("Sector not found");
        if (archive) {
            if (Boolean.TRUE.equals(customSector.getArchived())) {
                throw new IllegalArgumentException("Sector is already archived");
            }
            customSector.setArchived(true);
            // Optionally move to high sort order when archiving
        } else {
            if (Boolean.FALSE.equals(customSector.getArchived())) {
                throw new IllegalArgumentException("Sector is already active");
            }
            customSector.setArchived(false);
            // When unarchiving, assign next available sort order
        }

        entityManager.merge(customSector);
        return customSector;
    }
    public List<Object[]> getCompressedProductsBySector(List<Long> sectorIds, Integer offset, Integer limit, List<Long> categoryId) {
        try {
            System.out.println(categoryId);
            StringBuilder sql = new StringBuilder("SELECT cs.sector_id, cs.sector_description, cs.sector_name, " +
                    "STRING_AGG(CAST(c.product_id AS TEXT), ',' ORDER BY c.product_id DESC) AS product_ids " +
                    "FROM custom_product c " +
                    "JOIN blc_product bp on bp.product_id=c.product_id " +
                    "JOIN custom_sector cs ON cs.sector_id = c.sector_id " +
                    "JOIN blc_sku s ON s.sku_id = bp.default_sku_id " +
                    "WHERE bp.archived = 'N' " +
                    "AND c.product_state_id NOT IN (7) " +
                    "AND (s.active_end_date IS NULL OR s.active_end_date >= CURRENT_TIMESTAMP) " +
                    "AND c.go_live_date <= CURRENT_TIMESTAMP ");

            if (categoryId != null && !categoryId.isEmpty()) {
                sql.append( "AND bp.default_category_id IN (:categoryId) ");
            }
            if (sectorIds != null && !sectorIds.isEmpty()) {
                sql.append("AND c.sector_id IN (:sectorIds) ");
            }

            sql.append("GROUP BY cs.sector_id, cs.sector_description, cs.sector_name " +
                    "ORDER BY cs.sector_id DESC " +
                    "LIMIT :limit OFFSET :offset");
            System.out.println(sql.toString());
            Query query = entityManager.createNativeQuery(sql.toString());
            if (categoryId != null && !categoryId.isEmpty()) {
                query.setParameter("categoryId", categoryId);
            }
            if (sectorIds != null && !sectorIds.isEmpty()) {
                query.setParameter("sectorIds", sectorIds);
            }
            query.setParameter("limit", limit);
            query.setParameter("offset", offset);
            System.out.println(query.getResultList().size());
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch compressed products by sector: " + e.getMessage(), e);
        }
    }
    public List<BigInteger> getCompressedProductBySector(Long sectorId, Integer offset, Integer limit, List<Long> categoryId) {
        try {
            StringBuilder sql = new StringBuilder("SELECT c.product_id " +
                    "FROM custom_product c " +
                    "JOIN custom_sector cs ON cs.sector_id = c.sector_id " +
                    "JOIN blc_category_product_xref cp ON cp.product_id = c.product_id " +
                    "JOIN blc_product bp ON c.product_id = bp.product_id " +
                    "JOIN blc_sku s ON s.sku_id = bp.default_sku_id " +
                    "WHERE bp.archived = 'N' " +
                    "AND c.product_state_id NOT IN (7) ");
            if (categoryId != null && !categoryId.isEmpty()) {
                sql.append( "AND bp.default_category_id IN (:categoryId) ");
            }

            sql.append("AND (s.active_end_date IS NULL OR s.active_end_date >= CURRENT_TIMESTAMP) " +
                    "AND c.go_live_date <= CURRENT_TIMESTAMP " +
                    "AND c.sector_id = :sectorId " +
                    "ORDER BY c.last_modified DESC");

            Query query = entityManager.createNativeQuery(sql.toString());
            query.setParameter("sectorId", sectorId);
            if (categoryId != null && !categoryId.isEmpty()) {
                query.setParameter("categoryId", categoryId);
            }

            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch compressed products by sector: " + e.getMessage(), e);
        }
    }
    public BigInteger getCompressedProductsBySectorCount(List<Long> sectorIds, List<Long> categoryId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT cs.sector_id) " +
                "FROM custom_product c " +
                "JOIN custom_sector cs ON cs.sector_id = c.sector_id " +
                "JOIN blc_category_product_xref cp ON cp.product_id = c.product_id " +
                "JOIN blc_product bp ON c.product_id = bp.product_id " +
                "JOIN blc_sku s ON s.sku_id = bp.default_sku_id " +
                "WHERE bp.archived = 'N' " +
                "AND c.product_state_id NOT IN (7) " );
        if (categoryId != null && !categoryId.isEmpty()) {
            sql.append( "AND bp.default_category_id IN (:categoryId) ");
        }

        sql.append(
                "AND (s.active_end_date IS NULL OR s.active_end_date >= CURRENT_TIMESTAMP) " +
                        "AND c.go_live_date <= CURRENT_TIMESTAMP ");

        if (sectorIds != null && !sectorIds.isEmpty()) {
            sql.append("AND c.sector_id IN (:sectorIds) ");
        }

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            if (categoryId != null && !categoryId.isEmpty()) {
                query.setParameter("categoryId", categoryId);
            }
            if (sectorIds != null && !sectorIds.isEmpty()) {
                query.setParameter("sectorIds", sectorIds);
            }
            return (BigInteger) query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Failed to count sectors: " + e.getMessage(), e);
        }
    }
    public BigInteger getCompressedProductsCount(Long sectorId, List<Long> categoryId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT c.product_id) " +
                "FROM custom_product c " +
                "JOIN custom_sector cs ON cs.sector_id = c.sector_id " +
                "JOIN blc_category_product_xref cp ON cp.product_id = c.product_id " +
                "JOIN blc_product bp ON c.product_id = bp.product_id " +
                "JOIN blc_sku s ON s.sku_id = bp.default_sku_id " +
                "WHERE bp.archived = 'N' " +
                "AND c.product_state_id NOT IN (7) ");
        if (categoryId != null && !categoryId.isEmpty()) {
            sql.append( "AND bp.default_category_id IN (:categoryId) ");
        }

        sql.append(
                "AND (s.active_end_date IS NULL OR s.active_end_date >= CURRENT_TIMESTAMP) " +
                        "AND c.go_live_date <= CURRENT_TIMESTAMP " +
                        "AND c.sector_id = :sectorId");

        try {
            Query query = entityManager.createNativeQuery(sql.toString());
            query.setParameter("sectorId", sectorId);
            if (categoryId != null && !categoryId.isEmpty()) {
                query.setParameter("categoryId", categoryId);
            }
            return (BigInteger) query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Failed to count sectors: " + e.getMessage(), e);
        }
    }
}
