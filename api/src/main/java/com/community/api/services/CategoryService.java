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
}
