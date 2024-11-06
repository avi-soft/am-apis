package com.community.api.dto;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.search.domain.SearchCriteria;
import org.broadleafcommerce.core.search.service.SearchService;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CustomTicketWrapper extends BaseWrapper implements APIWrapper<Product>{
    @JsonProperty("ticket_id")
    protected Long id;

    @JsonProperty("created_date")
    protected Date createdDate;

    @JsonProperty("modified_date")
    protected Date modifiedDate;

    @JsonProperty("creator")
    protected

    @JsonProperty("assigned_to")
    protected ServiceProviderEntity;

    @JsonProperty("total_products")
    Integer totalProducts;

    @JsonProperty("products")
    List<CustomProductWrapper> products;

    public void wrapDetailsCategory(Category category, List<CustomProductWrapper> products, HttpServletRequest request) {

        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.longDescription = category.getLongDescription();
        this.active = category.isActive();
        this.displayTemplate = category.getDisplayTemplate();
        this.activeStartDate = category.getActiveStartDate();
        this.activeEndDate = category.getActiveEndDate();
        this.url = category.getUrl();
        this.urlKey = category.getUrlKey();
        this.archived = ((Status) category).getArchived();
        this.products = products;
        if (products == null) {
            this.totalProducts = 0;
        } else {
            this.totalProducts = products.size();
        }

        Integer productLimit = (Integer) request.getAttribute("productLimit");
        Integer productOffset = (Integer) request.getAttribute("productOffset");
        Integer subcategoryLimit = (Integer) request.getAttribute("subcategoryLimit");
        Integer subcategoryOffset = (Integer) request.getAttribute("subcategoryOffset");
        if (productLimit != null && productOffset == null) {
            productOffset = 1;
        }

        if (productLimit != null && productOffset != null) {
            SearchService searchService = this.getSearchService();
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.setPage(productOffset);
            searchCriteria.setPageSize(productLimit);
            searchCriteria.setFilterCriteria(new HashMap());

        }

        if (category instanceof Status) {
            this.archived = ((Status) category).getArchived();
        }

    }
}
