package com.community.api.services;

import com.community.api.dto.ReserveCategoryDto;
import com.community.api.entity.CustomProductReserveCategoryBornBeforeAfterRef;
import com.community.api.entity.CustomProductReserveCategoryFeePostRef;
import com.community.api.services.exception.ExceptionHandlingService;
import com.google.common.annotations.GwtCompatible;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReserveCategoryDtoService {
    private EntityManager entityManager;

    private ExceptionHandlingService exceptionHandlingService;
    private ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;
    private ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;
    private ReserveCategoryService reserveCategoryService;

    @Autowired
    GenderService genderService;

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

    public List<ReserveCategoryDto> getReserveCategoryDto(Long productId) {
        try{
            List<CustomProductReserveCategoryBornBeforeAfterRef> customProductReserveCategoryBornBeforeAfterRefList = productReserveCategoryBornBeforeAfterRefService.getProductReserveCategoryBornBeforeAfterByProductId(productId);
            List<CustomProductReserveCategoryFeePostRef> customProductReserveCategoryFeePostRefList = productReserveCategoryFeePostRefService.getProductReserveCategoryFeeAndPostByProductId(productId);

            List<ReserveCategoryDto> reserveCategoryDtoList = new ArrayList<>();
                for(int customProductReserveCategoryFeePostRefListIndex = 0; customProductReserveCategoryFeePostRefListIndex < customProductReserveCategoryFeePostRefList.size(); customProductReserveCategoryFeePostRefListIndex++) {
                        ReserveCategoryDto reserveCategoryDto = new ReserveCategoryDto();
                        reserveCategoryDto.setReserveCategoryId(customProductReserveCategoryBornBeforeAfterRefList.get(customProductReserveCategoryFeePostRefListIndex).getProductReservedCategoryId());
                        reserveCategoryDto.setProductId(productId);
                        reserveCategoryDto.setReserveCategory(customProductReserveCategoryBornBeforeAfterRefList.get(customProductReserveCategoryFeePostRefListIndex).getCustomReserveCategory().getReserveCategoryName());
                        reserveCategoryDto.setPost(customProductReserveCategoryFeePostRefList.get(customProductReserveCategoryFeePostRefListIndex).getPost());
                        reserveCategoryDto.setFee(customProductReserveCategoryFeePostRefList.get(customProductReserveCategoryFeePostRefListIndex).getFee());
                        Long genderId=customProductReserveCategoryFeePostRefList.get(customProductReserveCategoryFeePostRefListIndex).getGender().getGenderId();
                        reserveCategoryDto.setGenderName(genderService.getGenderByGenderId(genderId).getGenderName());
                        reserveCategoryDto.setGenderId(genderId);
                        reserveCategoryDtoList.add(reserveCategoryDto);
                }
            return reserveCategoryDtoList;

        } catch(Exception exception) {
                exceptionHandlingService.handleException(exception);
                return null;
        }
    }
}
