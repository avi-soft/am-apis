package com.community.api.services;

import com.community.api.dto.CustomProductWrapper;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.OtherItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@Service
public class PostExecutionService {

    @Autowired
    EntityManager entityManager;

    @Async
    @Transactional
    public void executePostProcessingLogicInAddProduct(CustomProductWrapper wrapper, List<OtherItem> reserveCategoryOtherList) throws InterruptedException {
            CustomProduct customProduct = entityManager.find(CustomProduct.class, wrapper.getId());
            if (customProduct == null) {
                throw new IllegalArgumentException("Custom Product does not exist with id " + wrapper.getId());
            }

            if (!reserveCategoryOtherList.isEmpty()) {
                List<OtherItem> existingItems = customProduct.getOtherItems();

                existingItems.clear();
                for (OtherItem otherItem : reserveCategoryOtherList) {
                    if(otherItem!=null)
                    {
                        otherItem.setCustomProduct(customProduct);
                        existingItems.add(otherItem);
                        entityManager.merge(otherItem);
                    }
                }
                entityManager.merge(customProduct);
                entityManager.flush();
                Thread.sleep(5000);
            }
    }

}
