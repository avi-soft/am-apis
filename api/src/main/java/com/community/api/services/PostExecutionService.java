package com.community.api.services;


import com.community.api.dto.CustomProductWrapper;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.OtherItem;

import com.community.api.dto.PostDto;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.OtherItem;
import com.community.api.entity.Post;

import org.broadleafcommerce.core.catalog.domain.Product;
import org.hibernate.Hibernate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import java.util.stream.Collectors;

@Service
public class PostExecutionService {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ProductReserveCategoryBornBeforeAfterRefService refService;
    @Autowired
    private PostService postService;

    @Autowired
    private ProductService productService;

    @Autowired
    private FileService fileService;

    @Transactional
    @Async("customAsyncExecutor")
    public void savePostsToCustomProduct(List<PostDto> postDto, Product product, List<Post> postList) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());
        if (customProduct == null) {
            throw new IllegalArgumentException("Custom product with id " + product.getId() + " does not exist");
        }
        postList = postList.stream()
                .map(post -> entityManager.contains(post) ? post : entityManager.merge(post))
                .collect(Collectors.toList());
        // Your business logic for saving posts and updating age requirements
        savePostsWithoutAgeRequirement(customProduct, postList);

        postService.updatePostAgeRequirements(postDto, customProduct, postList);
    }

    @Transactional
    public void savePostsWithoutAgeRequirement(CustomProduct customProduct, List<Post> postList) {
        Long totalPostInProduct=0L;
        for (Post post : postList) {
            totalPostInProduct+=post.getPostTotalVacancies();
            post.setProduct(customProduct);
            customProduct.getPosts().add(post);
            entityManager.merge(customProduct);
            entityManager.merge(post); // Persist the post before updating the age requirement
        }
        customProduct.setTotalVacanciesInProduct(totalPostInProduct);
        entityManager.merge(customProduct);
    }



//    @Async
//    @Transactional
//    public void executePostProcessingLogicInAddProduct(CustomProductWrapper wrapper, List<OtherItem> reserveCategoryOtherList) throws InterruptedException {
//        CustomProduct customProduct = entityManager.find(CustomProduct.class, wrapper.getId());
//        if (customProduct == null) {
//            throw new IllegalArgumentException("Custom Product does not exist with id " + wrapper.getId());
//        }
//
//        if (!reserveCategoryOtherList.isEmpty()) {
//            List<OtherItem> existingItems = customProduct.getOtherItems();
//
//            existingItems.clear();
//            for (OtherItem otherItem : reserveCategoryOtherList) {
//                if(otherItem!=null)
//                {
//                    otherItem.setCustomProduct(customProduct);
//                    existingItems.add(otherItem);
//                    entityManager.merge(otherItem);
//                }
//            }
//            entityManager.merge(customProduct);
//            entityManager.flush();
//            Thread.sleep(5000);
//        }
//    }
}
