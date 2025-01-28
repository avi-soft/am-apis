package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.AddReserveCategoryDto;
import com.community.api.entity.AddProductAgeDTO;
import com.community.api.entity.CustomGender;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductReserveCategoryBornBeforeAfterRef;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.entity.Post;
import com.community.api.services.exception.ExceptionHandlingService;
import io.swagger.models.auth.In;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.community.api.endpoint.avisoft.controller.Customer.CustomerEndpoint.convertStringToSQLDate;

@Service
public class ProductReserveCategoryBornBeforeAfterRefService {

    private final ExceptionHandlingService exceptionHandlingService;
    private final ProductService productService;
    private final ReserveCategoryService reserveCategoryService;
    private final GenderService genderService;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    public ProductReserveCategoryBornBeforeAfterRefService(ExceptionHandlingService exceptionHandlingService, ProductService productService, ReserveCategoryService reserveCategoryService,GenderService genderService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.productService = productService;
        this.reserveCategoryService = reserveCategoryService;
        this.genderService=genderService;
    }

    public List<CustomProductReserveCategoryBornBeforeAfterRef> getProductReserveCategoryBornBeforeAfterByProductId(Long productId) {
        try {
            CustomProduct customProduct = productService.getCustomProductByCustomProductId(productId);

            Query query = entityManager.createQuery(Constant.GET_PRODUCT_RESERVECATEGORY_BORNBEFORE_BORNAFTER, CustomProductReserveCategoryBornBeforeAfterRef.class);
            query.setParameter("customProduct", customProduct);
            List<CustomProductReserveCategoryBornBeforeAfterRef> productReserveCategoryBornBeforeAfterRefList = query.getResultList();

            return productReserveCategoryBornBeforeAfterRefList;

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
    public void saveBornBeforeAndBornAfter(List<AddProductAgeDTO> addReserveCategoryDtos, CustomProduct product, Post post) {
        try {
            List<CustomProductReserveCategoryBornBeforeAfterRef>resultList=new ArrayList<>();
            for(AddProductAgeDTO addReserveCategoryDto:addReserveCategoryDtos) {
                CustomReserveCategory reserveCategory = reserveCategoryService.getReserveCategoryById(addReserveCategoryDto.getReserveCategory());
                Date bornAfter = addReserveCategoryDto.getBornAfter();
                Date bornBefore = addReserveCategoryDto.getBornBefore();
                CustomGender gender = genderService.getGenderByGenderId(addReserveCategoryDto.getGender());
                CustomProductReserveCategoryBornBeforeAfterRef ref = new CustomProductReserveCategoryBornBeforeAfterRef();
                if(addReserveCategoryDto.getBornBeofreAfter().equals(true))
                {
                    ref.setBornBefore(bornBefore);
                    ref.setBornAfter(bornAfter);
                    ref.setMaximumAge(null);
                    ref.setMinimumAge(null);
                    ref.setAsOfDate(null);
                }
                else{
                    ref.setBornBefore(null);
                    ref.setBornAfter(null);
                    ref.setMaximumAge(addReserveCategoryDto.getMaxAge());
                    ref.setMinimumAge(addReserveCategoryDto.getMinAge());
                    ref.setAsOfDate(convertStringToSQLDate(addReserveCategoryDto.getAsOfDate(), "yyyy-MM-dd"));
                }

                ref.setCustomReserveCategory(reserveCategory);
                ref.setCustomProduct(product);
                ref.setGender(gender);
                ref.setBornBeforeAfter(addReserveCategoryDto.getBornBeofreAfter());
                ref.setProductReservedCategoryId(addReserveCategoryDto.getReserveCategory());
                ref.setPost(post);
                // Use merge instead of persist
                CustomProductReserveCategoryBornBeforeAfterRef mergedRef = entityManager.merge(ref);
                resultList.add(mergedRef);
            }  post.setAgeRequirement(resultList);
            entityManager.merge(post);
        } catch(Exception exception){
                exceptionHandlingService.handleException(exception);
            }
    }

    public CustomProductReserveCategoryBornBeforeAfterRef getCustomProductReserveCategoryBornBeforeAfterRefByProductIdAndReserveCategoryId(Long productId, Long reserveCategoryId) {

        try {
            CustomProduct customProduct = productService.getCustomProductByCustomProductId(productId);
            CustomReserveCategory customReserveCategory = reserveCategoryService.getReserveCategoryById(reserveCategoryId);

            List<CustomProductReserveCategoryBornBeforeAfterRef> customProductReserveCategoryBornBeforeAfterRefList = entityManager.createQuery("SELECT c FROM CustomProductReserveCategoryBornBeforeAfterRef c WHERE c.customProduct = :customProduct AND c.customReserveCategory = :customReserveCategory", CustomProductReserveCategoryBornBeforeAfterRef.class)
                    .setParameter("customProduct", customProduct)
                    .setParameter("customReserveCategory", customReserveCategory)
                    .getResultList();

            return customProductReserveCategoryBornBeforeAfterRefList.get(0);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }

    }
    public CustomProductReserveCategoryBornBeforeAfterRef getCustomProductReserveCategoryBornBeforeAfterRefByUId(Long uid){
        try {
            List<CustomProductReserveCategoryBornBeforeAfterRef> customProductReserveCategoryBornBeforeAfterRefList = entityManager.createQuery("SELECT c FROM CustomProductReserveCategoryBornBeforeAfterRef c WHERE productReservedCategoryId = :uid", CustomProductReserveCategoryBornBeforeAfterRef.class)
                    .setParameter("uid", uid)
                    .getResultList();

            return customProductReserveCategoryBornBeforeAfterRefList.get(0);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }

    }

    @Transactional
    public boolean removeProductReserveCategoryBornBeforeAfterByProductId(CustomProduct customProduct) throws Exception {
        try {
            // First, clear the associations in the join table
            String clearJoinTableQuery = "DELETE FROM post_age_requirement pa " +
                    "WHERE pa.age_requirement_id IN " +
                    "(SELECT c.product_reserve_category_id FROM custom_product_reserve_category_born_before_after_reference c " +
                    "WHERE c.product_id = :productId)";

            entityManager.createNativeQuery(clearJoinTableQuery)
                    .setParameter("productId", customProduct.getId())
                    .executeUpdate();

            // Then delete the main records
            String deleteMainQuery = "DELETE FROM custom_product_reserve_category_born_before_after_reference " +
                    "WHERE product_id = :productId";

            int rowsAffected = entityManager.createNativeQuery(deleteMainQuery)
                    .setParameter("productId", customProduct.getId())
                    .executeUpdate();

            return rowsAffected > 0;

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public CustomProductReserveCategoryBornBeforeAfterRef findByPost(Post post) {
        try {
            return entityManager.createQuery(
                            "SELECT c FROM CustomProductReserveCategoryBornBeforeAfterRef c WHERE c.post = :post",
                            CustomProductReserveCategoryBornBeforeAfterRef.class)
                    .setParameter("post", post)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
