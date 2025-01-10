package com.community.api.services;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Service
public class PostExecutionService {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ProductReserveCategoryBornBeforeAfterRefService refService;
    @Autowired
    private PostService postService;

    @Autowired
    private FileService fileService;

    @Transactional
    @Async("customAsyncExecutor")
    public void savePostsToCustomProduct(List<PostDto> postDto, Product product, List<Post> postList, List<OtherItem> otherItemList) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());
        if (customProduct == null) {
            throw new IllegalArgumentException("Custom product with id " + product.getId() + " does not exist");
        }

        // Clear existing other items
        if (customProduct.getOtherItems() != null) {
            customProduct.getOtherItems().clear();
            entityManager.merge(customProduct);
            entityManager.flush();
        }

        int otherItemIndex = 0;
        for (Post post : postList) {
            if (post.getVacancyDistributionTypes().get(0).getVacancyDistributionTypeId().equals(4)) {
                if (otherItemIndex < otherItemList.size()) {
                    OtherItem otherItem = otherItemList.get(otherItemIndex);

                    // Set relationships
                    otherItem.setCustomProduct(customProduct);

                    // Fetch and update the post
                    Post managedPost = entityManager.find(Post.class, post.getPostId());
                    otherItem.setPost(managedPost);

                    // Save the other item
                    entityManager.merge(otherItem);

                    // Update directly in the database
                    entityManager.createNativeQuery(
                                    "UPDATE other_item SET post_id = :postId, product_id = :productId WHERE other_item_id = :itemId")
                            .setParameter("postId", managedPost.getPostId())
                            .setParameter("productId", customProduct.getId())
                            .setParameter("itemId", otherItem.getOther_item_id())
                            .executeUpdate();

                    otherItemIndex++;
                }
            }
        }

        entityManager.flush();

        // Your business logic for saving posts and updating age requirements
        savePostsWithoutAgeRequirement(customProduct, postList);
        postService.updatePostAgeRequirements(postDto, customProduct, postList);
    }

    @Transactional
    public void savePostsWithoutAgeRequirement(CustomProduct customProduct, List<Post> postList) {
        for (Post post : postList) {
            post.setProduct(customProduct);
            customProduct.getPosts().add(post);
            entityManager.merge(customProduct);
            entityManager.merge(post); // Persist the post before updating the age requirement
        }
    }

}
