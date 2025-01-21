package com.community.api.services;

import com.community.api.dto.ReserveCategoryDto;
import com.community.api.entity.CustomProductReserveCategoryBornBeforeAfterRef;
import com.community.api.entity.CustomProductReserveCategoryFeePostRef;
import com.community.api.entity.OtherItem;
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
    private OtherItemService otherItemService;

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
    @Autowired
    public void setOtherItemService(OtherItemService otherItemService) {
        this.otherItemService = otherItemService;
    }

    public List<ReserveCategoryDto> getReserveCategoryDto(Long productId) {
        try{
            List<CustomProductReserveCategoryBornBeforeAfterRef> customProductReserveCategoryBornBeforeAfterRefList = productReserveCategoryBornBeforeAfterRefService.getProductReserveCategoryBornBeforeAfterByProductId(productId);
            List<CustomProductReserveCategoryFeePostRef> customProductReserveCategoryFeePostRefList = productReserveCategoryFeePostRefService.getProductReserveCategoryFeeAndPostByProductId(productId);

            List<ReserveCategoryDto> reserveCategoryDtoList = new ArrayList<>();
            int otherCategoryIndex=0;
            List<String>otherCategoryNames= new ArrayList<>();
            for(int customProductReserveCategoryBornBeforeAfterRefListIndex = 0; customProductReserveCategoryBornBeforeAfterRefListIndex < customProductReserveCategoryBornBeforeAfterRefList.size(); customProductReserveCategoryBornBeforeAfterRefListIndex++) {
                for (int customProductReserveCategoryFeePostRefListIndex = 0; customProductReserveCategoryFeePostRefListIndex < customProductReserveCategoryFeePostRefList.size(); customProductReserveCategoryFeePostRefListIndex++) {
                    ReserveCategoryDto reserveCategoryDto = new ReserveCategoryDto();
                    reserveCategoryDto.setReserveCategoryId(customProductReserveCategoryBornBeforeAfterRefList.get(customProductReserveCategoryFeePostRefListIndex).getProductReservedCategoryId());
                    reserveCategoryDto.setProductId(productId);
                    reserveCategoryDto.setReserveCategoryId(customProductReserveCategoryBornBeforeAfterRefList.get(customProductReserveCategoryBornBeforeAfterRefListIndex).getCustomReserveCategory().getReserveCategoryId());
                    if (reserveCategoryService.getReserveCategoryById(reserveCategoryDto.getReserveCategoryId()).getReserveCategoryName().equalsIgnoreCase("Others")) {
                        if (otherCategoryIndex == 0) {
                            List<OtherItem> otherItemList = otherItemService.getAllOtherItems();
                            if (otherItemList.isEmpty() || otherItemList == null) {
                                throw new IllegalArgumentException("There is no 'other' category saved for this product ");
                            }

                            for (OtherItem otherItem : otherItemList) {
                                if (otherItem.getCustomProduct().getId().equals(productId)) {
                                    otherCategoryNames.add(otherItem.getTyped_text());
                                }
                            }
                            reserveCategoryDto.setReserveCategory(otherCategoryNames.get(otherCategoryIndex));
                            otherCategoryIndex++;
                        } else if (otherCategoryIndex != 0) {
                            reserveCategoryDto.setReserveCategory(otherCategoryNames.get(otherCategoryIndex));
                        }
                    } else {
                        reserveCategoryDto.setReserveCategory(reserveCategoryService.getReserveCategoryById(reserveCategoryDto.getReserveCategoryId()).getReserveCategoryName());
                    }
//                        reserveCategoryDto.setReserveCategory(customProductReserveCategoryBornBeforeAfterRefList.get(customProductReserveCategoryFeePostRefListIndex).getCustomReserveCategory().getReserveCategoryName());
                    reserveCategoryDto.setPost(customProductReserveCategoryFeePostRefList.get(customProductReserveCategoryFeePostRefListIndex).getPost());
                    reserveCategoryDto.setFee(customProductReserveCategoryFeePostRefList.get(customProductReserveCategoryFeePostRefListIndex).getFee());
                    Long genderId = customProductReserveCategoryFeePostRefList.get(customProductReserveCategoryFeePostRefListIndex).getGender().getGenderId();
                    reserveCategoryDto.setGenderName(genderService.getGenderByGenderId(genderId).getGenderName());
                    reserveCategoryDto.setGenderId(genderId);
                    reserveCategoryDtoList.add(reserveCategoryDto);
                }
            }
            return reserveCategoryDtoList;

        } catch(Exception exception) {
                exceptionHandlingService.handleException(exception);
                return null;
        }
    }
}
