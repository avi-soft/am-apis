package com.community.api.dto;

import com.community.api.component.Constant;
import com.community.api.entity.AddProductAgeDTO;
import com.community.api.entity.Advertisement;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomGender;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductRejectionStatus;
import com.community.api.entity.CustomProductReserveCategoryBornBeforeAfterRef;
import com.community.api.entity.CustomProductReserveCategoryFeePostRef;
import com.community.api.entity.CustomProductState;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.entity.CustomSector;
import com.community.api.entity.Post;
import com.community.api.entity.Role;
import com.community.api.entity.StateCode;
import com.community.api.services.GenderService;
import com.community.api.services.PostService;
import com.community.api.services.ProductReserveCategoryBornBeforeAfterRefService;
import com.community.api.services.ProductReserveCategoryFeePostRefService;
import com.community.api.services.ReserveCategoryAgeService;
import com.community.api.services.ReserveCategoryService;
import com.community.api.services.SharedUtilityService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.common.rest.api.wrapper.APIWrapper;
import org.broadleafcommerce.common.rest.api.wrapper.BaseWrapper;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.community.api.endpoint.avisoft.controller.Customer.CustomerEndpoint.convertStringToDate;
import static com.community.api.endpoint.avisoft.controller.product.ProductController.getPosts;

public class NewProductWrapper  {

        @Autowired
        private GenderService genderService;
        @Autowired
        private ProductReserveCategoryBornBeforeAfterRefService refService;
        @Autowired
        private EntityManager entityManager;
        @Autowired
        private ProductReserveCategoryFeePostRefService feeService;


        @Autowired
        private PostService postService;
        @JsonProperty("product_id")
        protected Long id;
        @JsonProperty("meta_title")
        protected String metaTitle;
        @JsonProperty("display_template")
        protected String displayTemplate;
        @JsonProperty("meta_description")
        protected String metaDescription;
        @JsonProperty("category_name")
        protected String categoryName;
        @JsonProperty("active_start_date")
        protected Date activeStartDate;
        @JsonProperty("active_end_date")
        protected Date activeEndDate;
        @JsonProperty("go_live_date")
        protected Date activeGoLiveDate;
        @JsonProperty("state")
        protected StateCode state;



        public void wrapDetailsAddProduct(Product product) throws Exception {

            this.id = product.getId();
            this.metaTitle = product.getMetaTitle();
            this.displayTemplate = product.getDisplayTemplate();
            CustomProduct customProduct=entityManager.find(CustomProduct.class,product.getId());
            this.activeGoLiveDate = customProduct.getGoLiveDate();
            if(product.getDefaultCategory()!=null)
                this.categoryName = product.getDefaultCategory().getName();
            else
                this.categoryName=null;
            this.activeGoLiveDate = customProduct.getGoLiveDate();
            this.activeEndDate = product.getDefaultSku().getActiveEndDate();
            this.activeStartDate = product.getDefaultSku().getActiveStartDate();
            this.metaDescription = product.getMetaDescription();
            this.displayTemplate = product.getDisplayTemplate();
        }

}
