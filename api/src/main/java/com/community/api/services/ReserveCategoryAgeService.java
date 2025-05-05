package com.community.api.services;

import com.community.api.dto.ReserveCategoryAgeDto;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductReserveCategoryBornBeforeAfterRef;
import com.community.api.entity.CustomProductReserveCategoryFeePostRef;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.entity.Post;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReserveCategoryAgeService {
    private EntityManager entityManager;

    private ExceptionHandlingService exceptionHandlingService;
    private ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;
    private ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;
    private ReserveCategoryService reserveCategoryService;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setExceptionHandlingService(ExceptionHandlingService exceptionHandlingService) {
        this.exceptionHandlingService = exceptionHandlingService;
    }

    @Autowired
    public void setProductReserveCategoryBornBeforeAfterRefService(ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService) {
        this.productReserveCategoryBornBeforeAfterRefService = productReserveCategoryBornBeforeAfterRefService;
    }

    @Autowired
    public void setProductReserveCategoryFeePostRefService(ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService) {
        this.productReserveCategoryFeePostRefService = productReserveCategoryFeePostRefService;
    }

    @Autowired
    public void setReserveCategoryService(ReserveCategoryService reserveCategoryService) {
        this.reserveCategoryService = reserveCategoryService;
    }

    public List<ReserveCategoryAgeDto> getReserveCategoryDto(Long productId) {
        try {
            List<CustomProductReserveCategoryBornBeforeAfterRef> customProductReserveCategoryBornBeforeAfterRefList = productReserveCategoryBornBeforeAfterRefService.getProductReserveCategoryBornBeforeAfterByProductId(productId);
            List<CustomProductReserveCategoryFeePostRef> customProductReserveCategoryFeePostRefList = productReserveCategoryFeePostRefService.getProductReserveCategoryFeeAndPostByProductId(productId);

            List<ReserveCategoryAgeDto> reserveCategoryDtoList = new ArrayList<>();
           for(CustomProductReserveCategoryBornBeforeAfterRef ref:customProductReserveCategoryBornBeforeAfterRefList)
           {
               ReserveCategoryAgeDto dto=new ReserveCategoryAgeDto();
               dto.setReserveCategory(ref.getCustomReserveCategory().getReserveCategoryName());
               dto.setGenderId(ref.getGender().getGenderId());
               dto.setBornBefore(ref.getBornBefore());
               dto.setBornAfter(ref.getBornAfter());
               dto.setGenderName(ref.getGender().getGenderName());
               dto.setReserveCategoryId(ref.getCustomReserveCategory().getReserveCategoryId());
               dto.setBornAfter(ref.getBornAfter());
               reserveCategoryDtoList.add(dto);
            }
            return reserveCategoryDtoList;

        } catch(Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
    public CustomProductReserveCategoryBornBeforeAfterRef fetchAgeLimitByCategory(CustomProduct customProduct,Long categoryId,Long genderId) {
        try {
            CustomReserveCategory category=reserveCategoryService.getReserveCategoryById(categoryId);
            List<ReserveCategoryAgeDto> categoryAgeDtos = getReserveCategoryDto(customProduct.getId());
            System.out.println(categoryAgeDtos.toString());
            System.out.println("Category:"+category.getReserveCategoryName());
            for (Post post : customProduct.getPosts()) {
                if (post.getAgeRequirement().get(0).getCustomReserveCategory().getReserveCategoryName().equals(category.getReserveCategoryName())&&post.getAgeRequirement().get(0).getGender().getGenderId().equals(genderId))
                    return post.getAgeRequirement().get(0);
            }
            return null;
        } catch (Exception exception) {
            return null;
        }
    }
}
