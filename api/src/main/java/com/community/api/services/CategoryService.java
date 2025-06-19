package com.community.api.services;

import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.util.List;

@Service
public class CategoryService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<BigInteger> getAllProductsByCategoryId(Long categoryId) {
        String sqlWithProductJoin = "SELECT cp.product_id FROM blc_category_product_xref cp " +
                "INNER JOIN custom_product p ON cp.product_id = p.product_id " +
                "WHERE cp.category_id = :categoryId " +
                "ORDER BY p.created_date DESC";

        try {
            return entityManager.createNativeQuery(sqlWithProductJoin).setParameter("categoryId", categoryId).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("FAILED TO GET CATEGORY_PRODUCT: " + e.getMessage(), e);
        }
    }
    public List<Object[]> getAllProductsByCategoryIdCompressed(Long categoryId, Integer offset, Integer limit) {
        String sql = "SELECT cp.product_id, bp.meta_title " +
                "FROM blc_category_product_xref cp " +
                "INNER JOIN custom_product p ON cp.product_id = p.product_id " +
                "INNER JOIN blc_product bp ON bp.product_id = p.product_id " +
                "INNER JOIN blc_sku bs ON bp.default_sku_id = bs.sku_id " +
                "WHERE cp.category_id = :categoryId " +
                "AND bp.archived = 'N' " +
                "AND (bs.active_end_date IS NULL OR bs.active_end_date >= CURRENT_DATE) " +
                "AND p.go_live_date <= CURRENT_DATE " +
                "AND p.product_state_id NOT IN (7) " +
                "AND p.is_approved = TRUE " +
                "ORDER BY p.created_date DESC " +
                "LIMIT :limit OFFSET :offset";

        try {
            List<Object[]> rows = entityManager.createNativeQuery(sql)
                    .setParameter("categoryId", categoryId)
                    .setParameter("limit", limit)
                    .setParameter("offset", offset)
                    .getResultList();

            System.out.println("⚠️ Query result size = " + rows.size());
            for (Object[] r : rows) {
                System.out.println("ROW => productId: " + r[0] + ", metaTitle: " + r[1]);
            }

            return rows;
        } catch (Exception e) {
            throw new RuntimeException("FAILED TO FETCH CATEGORY PRODUCTS: " + e.getMessage(), e);
        }
    }
    public BigInteger getAllProductsByCategoryIdCount(Long categoryId) {
        String countSql = "SELECT COUNT(*) " +
                "FROM blc_category_product_xref cp " +
                "INNER JOIN custom_product p ON cp.product_id = p.product_id " +
                "INNER JOIN blc_product bp ON bp.product_id = p.product_id " +
                "INNER JOIN blc_sku bs ON bp.default_sku_id = bs.sku_id " +
                "WHERE cp.category_id = ? " +
                "AND bp.archived = 'N' " +
                "AND (bs.active_end_date IS NULL OR bs.active_end_date >= CURRENT_DATE) " +
                "AND p.go_live_date <= CURRENT_DATE " +
                "AND p.product_state_id NOT IN (7) " +
                "AND p.is_approved = TRUE";

        try {
            return (BigInteger) entityManager.createNativeQuery(countSql)
                    .setParameter(1, categoryId)
                    .getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("FAILED TO COUNT CATEGORY_PRODUCT: " + e.getMessage(), e);
        }
    }

}
