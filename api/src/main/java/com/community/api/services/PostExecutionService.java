package com.community.api.services;

import com.community.api.dto.AddProductDto;
import com.community.api.dto.PostDto;
import com.community.api.entity.AddProductAgeDTO;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.Post;
import com.community.api.utils.Document;
import com.community.api.utils.ServiceProviderDocument;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.community.api.component.Constant.request;

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
    public void savePostsToCustomProduct(List<PostDto>postDto, Product product, List<Post> postList) {
        CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());
        if (customProduct == null) {
            throw new IllegalArgumentException("Custom product with id " + product.getId() + " does not exist");
        }
        // Save custom product first
        System.out.println(customProduct.getId());
        savePostsWithoutAgeRequirement(customProduct, postList);
        postService.updatePostAgeRequirements(postDto,customProduct,postList);
        System.out.println("Async completed!!!!!!!!!");
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

    @Async("customAsyncExecutor")
    public CompletableFuture<List<Map<String, Object>>> returnCustomerDocuments(List<Document> customerDocuments) {
        List<Map<String, Object>> filteredDocuments = new ArrayList<>();

        // If no documents exist, return an empty list
        if (customerDocuments == null || customerDocuments.isEmpty()) {
            return CompletableFuture.completedFuture(filteredDocuments);
        }

        // Process existing documents
        for (Document document : customerDocuments) {
            if (document.getIsArchived() != null && !document.getIsArchived()) { // Exclude archived documents
                if (document.getFilePath() != null && document.getDocumentType() != null) {
                    Map<String, Object> documentDetails = new HashMap<>();
                    documentDetails.put("documentId", document.getDocumentId());
                    documentDetails.put("name", document.getName());
                    documentDetails.put("filePath", document.getFilePath());

                    // Add qualification details if applicable
                    if (Boolean.TRUE.equals(document.getIs_qualification_document()) && document.getQualificationDetails() != null) {
                        documentDetails.put("qualification_detail_id", document.getQualificationDetails().getQualification_detail_id());
                    }

                    // Add document validity details if applicable
                    if (document.getDocumentValidity() != null) {
                        documentDetails.put("documentValidity", document.getDocumentValidity());
                    }

                    // Generate a file URL for the document
                    String fileUrl = fileService.getFileUrl(document.getFilePath(), request);
                    documentDetails.put("fileUrl", fileUrl);

                    documentDetails.put("documentType", document.getDocumentType());
                    filteredDocuments.add(documentDetails);
                }
            }
        }

        return CompletableFuture.completedFuture(filteredDocuments);
    }

    @Async("customAsyncExecutor")
    public CompletableFuture<List<Map<String, Object>>> returnServiceProvider(List<ServiceProviderDocument> serviceProviderDocuments) {
        List<Map<String, Object>> filteredDocuments = new ArrayList<>();
        if (serviceProviderDocuments == null || serviceProviderDocuments.isEmpty()) {
            return CompletableFuture.completedFuture(filteredDocuments);
        }
        for (ServiceProviderDocument document :serviceProviderDocuments) {
            if (document.getIsArchived() != null && !document.getIsArchived()) { // Exclude archived documents
                if (document.getFilePath() != null && document.getDocumentType() != null) {
                    Map<String, Object> documentDetails = new HashMap<>();
                    documentDetails.put("documentId", document.getDocumentId());
                    documentDetails.put("name", document.getName());
                    documentDetails.put("filePath", document.getFilePath());

                    // Add qualification details if applicable
                    if (Boolean.TRUE.equals(document.getIs_qualification_document()) && document.getQualificationDetails() != null) {
                        documentDetails.put("qualification_detail_id", document.getQualificationDetails().getQualification_detail_id());
                    }

                    // Add document validity details if applicable
                    if (document.getDocumentValidity() != null) {
                        documentDetails.put("documentValidity", document.getDocumentValidity());
                    }

                    // Generate a file URL for the document
                    String fileUrl = fileService.getFileUrl(document.getFilePath(), request);
                    documentDetails.put("fileUrl", fileUrl);

                    documentDetails.put("documentType", document.getDocumentType());
                    filteredDocuments.add(documentDetails);
                }
            }
        }
        return CompletableFuture.completedFuture(filteredDocuments);
    }

}
