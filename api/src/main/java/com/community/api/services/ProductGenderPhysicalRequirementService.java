package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.AddPhysicalRequirementDto;
import com.community.api.entity.CustomGender;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductGenderPhysicalRequirementRef;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductGenderPhysicalRequirementService {

    private final ExceptionHandlingService exceptionHandlingService;
    private final ProductService productService;
    private final GenderService genderService;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    public ProductGenderPhysicalRequirementService(ExceptionHandlingService exceptionHandlingService, ProductService productService, GenderService genderService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.productService = productService;
        this.genderService = genderService;
    }

    public List<CustomProductGenderPhysicalRequirementRef> getProductGenderPhysicalRequirementByProductId(Long productId) {
        try {
            CustomProduct customProduct = productService.getCustomProductByCustomProductId(productId);

            Query query = entityManager.createQuery(Constant.GET_PRODUCT_GENDER_PHYSICAL_REQUIREMENT, CustomProductGenderPhysicalRequirementRef.class);
            query.setParameter("customProduct", customProduct);
            List<CustomProductGenderPhysicalRequirementRef> productReserveCategoryBornBeforeAfterRefList = query.getResultList();

            return productReserveCategoryBornBeforeAfterRefList;

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    public CustomProductGenderPhysicalRequirementRef getCustomProductGenderPhysicalRequirementRefByProductIdAndGenderId(Long productId, Long genderId) {

        try {
            CustomProduct customProduct = productService.getCustomProductByCustomProductId(productId);
            CustomGender customGender = genderService.getGenderByGenderId(genderId);

            List<CustomProductGenderPhysicalRequirementRef> customProductGenderPhysicalRequirementRefList = entityManager.createQuery("SELECT c FROM CustomProductGenderPhysicalRequirementRef c WHERE c.customProduct = :customProduct AND c.customGender = :customGender", CustomProductGenderPhysicalRequirementRef.class)
                    .setParameter("customProduct", customProduct)
                    .setParameter("customGender", customGender)
                    .getResultList();

            return customProductGenderPhysicalRequirementRefList.get(0);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }

    }

    public boolean removeProductGenderPhysicalRequirementByProductId (CustomProduct customProduct) throws Exception {
        try {

            int rowsAffected = entityManager.createQuery(
                            "DELETE FROM CustomProductGenderPhysicalRequirementRef c WHERE c.customProduct = :customProduct")
                    .setParameter("customProduct", customProduct)
                    .executeUpdate();

            return rowsAffected > 0;

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }
}
