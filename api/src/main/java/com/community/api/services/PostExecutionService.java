package com.community.api.services;

import com.community.api.dto.PostDto;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.Post;

import org.broadleafcommerce.core.catalog.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

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
    @Async("customAsyncExecutor")  // Use custom async executor defined in AsyncConfig
    public void savePostsToCustomProduct(List<PostDto> postDto, Product product, List<Post> postList) {
        try {
            // Introduce a 1-second delay before execution
            Thread.sleep(1000);  // 1000 milliseconds = 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Handle interruption
        }

        // Now the business logic will execute after the 1-second delay
        CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());
        if (customProduct == null) {
            throw new IllegalArgumentException("Custom product with id " + product.getId() + " does not exist");
        }

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
