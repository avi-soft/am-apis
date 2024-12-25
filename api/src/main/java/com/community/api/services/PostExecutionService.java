package com.community.api.services;

import com.community.api.dto.AddProductDto;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.Post;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class PostExecutionService {

    @Autowired
    private EntityManager entityManager;

    @Transactional
    @Async
    public void savePostsToCustomProduct(Product product, List<Post> postList) {
        CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());
        if (customProduct == null) {
            throw new IllegalArgumentException("Custom product with id " + product.getId() + " does not exist");
        }

        // Link posts to the custom product
        for (Post post : postList) {
            post.setProduct(customProduct);
            customProduct.getPosts().add(post);
            entityManager.merge(post);
        }
        entityManager.merge(customProduct);
    }

}
