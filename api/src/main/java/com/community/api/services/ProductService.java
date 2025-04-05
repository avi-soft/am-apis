package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddProductDto;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.DivisionProjectionDTO;
import com.community.api.dto.QualificationEligibilityDto;
import com.community.api.dto.ReserveCategoryAgeDto;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.dto.CategoryDistributionDto;
import com.community.api.dto.DistrictDistributionDto;
import com.community.api.dto.PostDto;
import com.community.api.dto.DistrictCategoryDistributionDto;
import com.community.api.dto.ZoneDistributionDto;
import com.community.api.dto.StateDistributionDto;
import com.community.api.dto.GenderDistributionDto;
import com.community.api.dto.DivisionDistributionDto;
import com.community.api.dto.DivisionCategoryDistributionDto;
import com.community.api.entity.Advertisement;
import com.community.api.entity.OtherItem;
import com.community.api.entity.Qualification;
import com.community.api.entity.Districts;
import com.community.api.entity.CustomProductRejectionStatus;
import com.community.api.entity.CustomGender;
import com.community.api.entity.StateCode;
import com.community.api.entity.Privileges;
import com.community.api.entity.Role;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomProductState;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.entity.CustomJobGroup;
import com.community.api.entity.CustomSubject;
import com.community.api.entity.CustomStream;
import com.community.api.entity.CustomSector;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.Post;
import com.community.api.entity.OtherDistribution;
import com.community.api.services.exception.ExceptionHandlingService;
import javassist.NotFoundException;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import static com.community.api.component.Constant.*;
import static com.community.api.component.Constant.PRODUCTNOTFOUND;
import static com.community.api.endpoint.avisoft.controller.product.ProductController.*;

@Service
public class ProductService {

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;

    @Autowired
    ReserveCategoryDtoService reserveCategoryDtoService;
    @Autowired
    ProductStateService productStateService;
    @Autowired
    private ReserveCategoryAgeService reserveCategoryAgeService;
    @Autowired
    ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;
    @Autowired
    ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;
    @Autowired
    ReserveCategoryService reserveCategoryService;
    @Autowired
    RoleService roleService;
    @Autowired
    PrivilegeService privilegeService;
    @Autowired
    ApplicationScopeService applicationScopeService;
    @Autowired
    JwtUtil jwtTokenUtil;
    @Autowired
    ExceptionHandlingService exceptionHandlingService;
    @Autowired
    JobGroupService jobGroupService;
    @Autowired
    ProductRejectionStatusService productRejectionStatusService;
    @Autowired
    DistrictService districtService;
    @Autowired
    GenderService genderService;
    @Autowired
    SectorService sectorService;
    @Autowired
    QualificationService qualificationService;
    @Autowired
    StreamService streamService;
    @Autowired
    SubjectService subjectService;
    @Autowired
    ProductGenderPhysicalRequirementService productGenderPhysicalRequirementService;
    @Autowired
    AdvertisementService advertisementService;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ResponseService responseService;

    @Autowired
    ZoneDivisionService zoneDivisionService;

    public void saveCustomProduct(Product product, AddProductDto addProductDto, CustomProductState productState, Role role, Long creatorUserId, Date modifiedDate, Date currentDate) {

        try {

            // Start building the SQL query
            StringBuilder sql = new StringBuilder("INSERT INTO custom_product (product_id, creator_user_id, creator_role_id, last_modified, product_state_id, created_date, advertisement_id");
            StringBuilder values = new StringBuilder("VALUES (:productId, :creatorUserId, :role, :lastModified, :productState, :currentDate, :advertisement");

            // Dynamically add columns and values based on non-null fields

            if (addProductDto.getApplicationScope() != null) {
                sql.append(", application_scope_id");
                values.append(", :applicationScope");
            }

            if (addProductDto.getExamDateFrom() != null) {
                sql.append(", exam_date_from");
                values.append(", :examDateFrom");
            }

            if (addProductDto.getExamDateTo() != null) {
                sql.append(", exam_date_to");
                values.append(", :examDateTo");
            }

            if (addProductDto.getGoLiveDate() != null) {
                sql.append(", go_live_date");
                values.append(", :goLiveDate");
            }

            if (addProductDto.getPlatformFee() != null) {
                sql.append(", platform_fee");
                values.append(", :platformFee");
            }

            if (addProductDto.getPriorityLevel() != null) {
                sql.append(", priority_level");
                values.append(", :priorityLevel");
            }

            if (addProductDto.getAdmitCardDateFrom() != null) {
                sql.append(", admit_card_date_from");
                values.append(", :admitCardDateFrom");
            }

            if (addProductDto.getAdmitCardDateTo() != null) {
                sql.append(", admit_card_date_to");
                values.append(", :admitCardDateTo");
            }

            if (addProductDto.getModificationDateFrom() != null) {
                sql.append(", modification_date_from");
                values.append(", :modificationDateFrom");
            }

            if (addProductDto.getModificationDateTo() != null) {
                sql.append(", modification_date_to");
                values.append(", :modificationDateTo");
            }

            if (addProductDto.getState() != null) {
                sql.append(", state_id");
                values.append(", :state");
            }

            if (addProductDto.getLastDateToPayFee() != null) {
                sql.append(", last_date_to_pay_fee");
                values.append(", :lastDateToPayFee");
            }

            if (addProductDto.getDownloadNotificationLink() != null) {
                sql.append(", download_notification_link");
                values.append(", :downloadNotificationLink");
            }

            if (addProductDto.getDownloadSyllabusLink() != null) {
                sql.append(", download_syllabus_link");
                values.append(", :downloadSyllabusLink");
            }

            if (addProductDto.getFormComplexity() != null) {
                sql.append(", form_complexity");
                values.append(", :formComplexity");
            }

            if (addProductDto.getSector() != null) {
                sql.append(", sector_id");
                values.append(", :sectorId");
            }

            if (addProductDto.getSelectionCriteria() != null) {
                sql.append(", selection_criteria");
                values.append(", :selectionCriteria");
            }

            if(addProductDto.getIsReviewRequired()!=null)
            {
                sql.append(", is_review_required");
                values.append(", :isReviewRequired");
            }
            if(addProductDto.getOtherInfo()!=null)
            {
                sql.append(", other_info");
                values.append(", :otherInfo");
            }
            if(addProductDto.getIsMultiplePostSameFee()!=null)
            {
                sql.append(", is_multiple_post_same_fee");
                values.append(", :isMultiplePostSameFee");
            }

            // Complete the SQL statement
            sql.append(") ").append(values).append(")");

            // Create the query
            var query = entityManager.createNativeQuery(sql.toString())
                    .setParameter("productId", product)
                    .setParameter("creatorUserId", creatorUserId)
                    .setParameter("role", role)
                    .setParameter("lastModified", modifiedDate)
                    .setParameter("currentDate", currentDate)
                    .setParameter("advertisement", addProductDto.getAdvertisement());

            // Set parameters conditionally

            if (addProductDto.getApplicationScope() != null) {
                query.setParameter("applicationScope", addProductDto.getApplicationScope());
            }

            if (addProductDto.getExamDateFrom() != null) {
                query.setParameter("examDateFrom", new Timestamp(addProductDto.getExamDateFrom().getTime()));
            }

            query.setParameter("productState", productState);

            if (addProductDto.getState() != null) {
                query.setParameter("state", addProductDto.getState());
            }

            if (addProductDto.getExamDateTo() != null) {
                query.setParameter("examDateTo", new Timestamp(addProductDto.getExamDateTo().getTime()));
            }

            if (addProductDto.getGoLiveDate() != null) {
                query.setParameter("goLiveDate", new Timestamp(addProductDto.getGoLiveDate().getTime()));
            }

            if (addProductDto.getPlatformFee() != null) {
                query.setParameter("platformFee", addProductDto.getPlatformFee());
            }

            if (addProductDto.getPriorityLevel() != null) {
                query.setParameter("priorityLevel", addProductDto.getPriorityLevel());
            }

            if (addProductDto.getAdmitCardDateFrom() != null) {
                query.setParameter("admitCardDateFrom", new Timestamp(addProductDto.getAdmitCardDateFrom().getTime()));
            }

            if (addProductDto.getAdmitCardDateTo() != null) {
                query.setParameter("admitCardDateTo", new Timestamp(addProductDto.getAdmitCardDateTo().getTime()));
            }

            if (addProductDto.getModificationDateFrom() != null) {
                query.setParameter("modificationDateFrom", new Timestamp(addProductDto.getModificationDateFrom().getTime()));
            }

            if (addProductDto.getModificationDateTo() != null) {
                query.setParameter("modificationDateTo", new Timestamp(addProductDto.getModificationDateTo().getTime()));
            }

            if (addProductDto.getLastDateToPayFee() != null) {
                query.setParameter("lastDateToPayFee", new Timestamp(addProductDto.getLastDateToPayFee().getTime()));
            }

            if (addProductDto.getDownloadNotificationLink() != null) {
                query.setParameter("downloadNotificationLink", addProductDto.getDownloadNotificationLink());
            }

            if (addProductDto.getDownloadSyllabusLink() != null) {
                query.setParameter("downloadSyllabusLink", addProductDto.getDownloadSyllabusLink());
            }

            if (addProductDto.getFormComplexity() != null) {
                query.setParameter("formComplexity", addProductDto.getFormComplexity());
            }

            if (addProductDto.getSector() != null) {
                query.setParameter("sectorId", addProductDto.getSector());
            }

            if (addProductDto.getSelectionCriteria() != null) {
                query.setParameter("selectionCriteria", addProductDto.getSelectionCriteria());
            }

            if(addProductDto.getIsReviewRequired()!=null)
            {
                query.setParameter("isReviewRequired",addProductDto.getIsReviewRequired());
            } if(addProductDto.getOtherInfo()!=null)
            {
                query.setParameter("otherInfo",addProductDto.getOtherInfo());
            }
            if(addProductDto.getIsMultiplePostSameFee()!=null)
            {
                query.setParameter("isMultiplePostSameFee",addProductDto.getIsReviewRequired());
            }

            // Execute the update
            query.executeUpdate();

        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new RuntimeException("Failed to save Custom Product: " + e.getMessage(), e);
        }
    }


    public List<CustomProduct> getCustomProducts() throws Exception {
        try {
            String sql = "SELECT * FROM custom_product";
            return entityManager.createNativeQuery(sql, CustomProduct.class).getResultList();

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Failed to retrieve CustomProducts: " + exception.getMessage(), exception);
        }
    }

    public CustomProduct getCustomProductByCustomProductId(Long productId) {
        String sql = "SELECT c FROM CustomProduct c WHERE c.id = :productId";
        return entityManager.createQuery(sql, CustomProduct.class).setParameter("productId", productId).getResultList().get(0);
    }

    @Transactional
    public void removeCategoryProductFromCategoryProductRefTable(Long categoryId, Long productId) {
        String sql = "DELETE FROM blc_category_product_xref WHERE product_id = :productId AND category_id = :categoryId";
        try {
            entityManager.createNativeQuery(sql)
                    .setParameter("productId", productId)
                    .setParameter("categoryId", categoryId)
                    .executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to Delete Category_Product: " + e.getMessage(), e);
        }
    }

    public Map<String, String> getRequestParamBasedOnQueryString(String queryString) throws UnsupportedEncodingException {
        if (queryString != null) {

            String[] params = queryString.split("&"); // Split the query string by '&' to get each parameter

            // Create a map to hold parameters
            Map<String, String> paramMap = new HashMap<>();

            // Process each parameter
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];

                    // Encode the value to UTF-8
                    value = URLEncoder.encode(value, "UTF-8"); // may throw exception.

                    paramMap.put(key, value);
                }
            }
            return paramMap;
        } else {
            return null;
        }
    }

    public List<CustomProduct> filterProducts(List<Long> states, List<Long> statuses, List<Long> categories,
                                              List<Long> reserveCategories, String title, Double fee,
                                              Integer post, Date startRange, Date endRange) throws Exception {
        try {
            // Initialize the JPQL query
            StringBuilder jpql = new StringBuilder("SELECT DISTINCT p FROM CustomProduct p ")
                    .append("JOIN CustomProductReserveCategoryFeePostRef r ON r.customProduct = p ")
                    .append("JOIN SkuImpl s ON s.defaultProduct = p ")
                    .append("WHERE 1=1 "); // Use this to simplify appending conditions

            // List to hold query parameters
            List<CustomProductState> customProductStates = new ArrayList<>();
            List<CustomProductRejectionStatus> productRejectionStatuses = new ArrayList<>();
            List<Category> categoryList = new ArrayList<>();
            List<CustomReserveCategory> customReserveCategoryList = new ArrayList<>();

            // Conditionally build the query
            if (states != null && !states.isEmpty()) {
                for (Long id : states) {
                    CustomProductState productState = productStateService.getProductStateById(id);
                    if (productState == null) {
                        throw new IllegalArgumentException("NO PRODUCT STATE FOUND WITH THIS ID: " + id);
                    }
                    customProductStates.add(productState);
                }
                jpql.append("AND p.productState IN :states ");
            }

            if (statuses != null && !statuses.isEmpty()) {
                for (Long id : statuses) {
                    CustomProductRejectionStatus productRejectionStatus = productRejectionStatusService.getAllRejectionStatusByRejectionStatusId(id);
                    if (productRejectionStatus == null) {
                        throw new IllegalArgumentException("NO PRODUCT STATUS FOUND WITH THIS ID: " + id);
                    }
                    productRejectionStatuses.add(productRejectionStatus);
                }

                // Explicitly filter for non-null rejection status that matches the specified values
                jpql.append("AND p.rejectionStatus IS NOT NULL AND p.rejectionStatus IN :statuses ");
            }

            if (categories != null && !categories.isEmpty()) {
                boolean anyValidCategory = false;
                for (Long id : categories) {
                    Category category = catalogService.findCategoryById(id);
                    if (category == null) {
                        throw new IllegalArgumentException("NO CATEGORY FOUND WITH THIS ID: " + id);
                    }

                    // Check if category is active and not archived
                    if ((((Status) category).getArchived() != 'Y' && category.getActiveEndDate() == null) ||
                            (((Status) category).getArchived() != 'Y' && category.getActiveEndDate().after(new Date()))) {
                        categoryList.add(category);
                        anyValidCategory = true;
                    }
                }

                if (anyValidCategory) {
                    jpql.append(" AND p.defaultCategory IN :categories ");
                } else {
                    // If all requested categories are archived or inactive, return no results
                    throw new IllegalArgumentException("All requested categories are archived or inactive");
                }
            }

            if (reserveCategories != null && !reserveCategories.isEmpty()) {
                for (Long id : reserveCategories) {
                    customReserveCategoryList.add(reserveCategoryService.getReserveCategoryById(id));
                }
                jpql.append("AND r.customReserveCategory IN :reserveCategories ");
            }

            if (title != null && !title.isEmpty()) {
                String[] words = title.split("\\s+");
                if (words.length > 0) {
                    jpql.append("AND (");
                    for (int i = 0; i < words.length; i++) {
                        if (i > 0) {
                            jpql.append(" AND ");
                        }
                        jpql.append("LOWER(p.metaTitle) LIKE LOWER(:titleWord").append(i).append(") ");
                    }
                    jpql.append(") ");
                }
            }

            if (fee != null) {
                jpql.append("AND r.fee = :fee ");
            }

            if (post != null) {
                jpql.append("AND SIZE(p.posts) = :post ");
            }

            // Filter for exact date match, ignoring time portion
            if (startRange != null) {
                jpql.append("AND p.examDateFrom IS NOT NULL ");
                jpql.append("AND FUNCTION('DATE', p.examDateFrom) = FUNCTION('DATE', :startRange) ");
            }

            if (endRange != null) {
                jpql.append("AND p.examDateTo IS NOT NULL ");
                jpql.append("AND FUNCTION('DATE', p.examDateTo) = FUNCTION('DATE', :endRange) ");
            }

            // Create the query with the final JPQL string
            TypedQuery<CustomProduct> query = entityManager.createQuery(jpql.toString(), CustomProduct.class);

            // Set parameters
            if (!customProductStates.isEmpty()) {
                query.setParameter("states", customProductStates);
            }
            if (!productRejectionStatuses.isEmpty()) {
                query.setParameter("statuses", productRejectionStatuses);
            }
            if (!categoryList.isEmpty()) {
                query.setParameter("categories", categoryList);
            }
            if (!customReserveCategoryList.isEmpty()) {
                query.setParameter("reserveCategories", customReserveCategoryList);
            }
            if (title != null && !title.isEmpty()) {
                String[] words = title.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    query.setParameter("titleWord" + i, "%" + words[i].toLowerCase() + "%");
                }
            }
            if (fee != null) {
                query.setParameter("fee", fee);
            }
            if (post != null) {
                query.setParameter("post", post);
            }
            if (startRange != null) {
                query.setParameter("startRange", startRange);
            }
            if (endRange != null) {
                query.setParameter("endRange", endRange);
            }

            // Execute and return the result
            return query.getResultList();
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION CAUGHT: " + exception.getMessage());
        }
    }

    public ResponseEntity<?> filterProductsByRoleAndUserId(Integer roleId, Long userId, int page, int limit, boolean showDraftProducts) {
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT p FROM CustomProduct p JOIN p.creatoRole r ");

        Map<String, Object> queryParams = new HashMap<>();

        // Check if the role exists
        if (roleId != null) {
            Role role = entityManager.find(Role.class, roleId);
            if (role == null) {
                throw new IllegalArgumentException("No role exists with id " + roleId);
            }

            if (!role.getRole_name().equalsIgnoreCase(ADMIN) && !role.getRole_name().equalsIgnoreCase(SUPER_ADMIN)) {
                jpql.append("WHERE r.role_id = :roleId ");
                queryParams.put("roleId", roleId);

                if (userId != null) {
                    jpql.append("AND p.userId = :userId ");
                    queryParams.put("userId", userId);
                }
            } else {
                // For Admin or Superadmin, they can see all products
                jpql.append("WHERE 1=1 ");
            }
        } else {
            jpql.append("WHERE 1=1 ");
        }

        // Add filter for non-archived products - using the correct path through archiveStatus
        jpql.append("AND p.archiveStatus.archived != 'Y' ");

        if (showDraftProducts) {
            jpql.append("AND p.productState.productState = :draftState ");
            queryParams.put("draftState", "DRAFT");
        }

        // Get the count query
        String countJpql = jpql.toString().replace("SELECT DISTINCT p", "SELECT COUNT(DISTINCT p)");

        // Execute count query for pagination
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        queryParams.forEach(countQuery::setParameter);
        long totalProducts = countQuery.getSingleResult();

        int totalPages = (int) Math.ceil((double) totalProducts / limit);

        if (page >= totalPages && page != 0 && totalProducts > 0) {
            throw new IllegalArgumentException("No more products available");
        }

        // Execute the query with pagination
        TypedQuery<CustomProduct> query = entityManager.createQuery(jpql.toString(), CustomProduct.class);
        queryParams.forEach(query::setParameter);

        int startPosition = page * limit;
        query.setFirstResult(startPosition);
        query.setMaxResults(limit);
        List<CustomProduct> products = query.getResultList();

        if (products.isEmpty() && page == 0) {
            if (showDraftProducts) {
                return ResponseService.generateSuccessResponse("Draft Product list is empty", products, HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("PRODUCT LIST IS EMPTY", products, HttpStatus.OK);
        }

        List<CustomProductWrapper> responses = new ArrayList<>();
        for (CustomProduct customProduct : products) {
            CustomProductWrapper wrapper = new CustomProductWrapper();
            wrapper.wrapDetails(customProduct);
            responses.add(wrapper);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("products", responses);
        response.put("currentPage", page);
        response.put("totalItems", totalProducts);
        response.put("totalPages", totalPages);

        if (showDraftProducts) {
            return ResponseService.generateSuccessResponse("Draft Products are retrieved successfully", response, HttpStatus.OK);
        }

        return ResponseService.generateSuccessResponse("PRODUCTS RETRIEVED SUCCESSFULLY", response, HttpStatus.OK);
    }

    public boolean addProductAccessAuthorisation(String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();

            Long userId = null;
            if (role.equals(Constant.SUPER_ADMIN) || role.equals(Constant.ADMIN)) {
                return true;

                // -> NEED TO ADD THE USER_ID OF ADMIN OR SUPER ADMIN.

            } else if (role.equals(Constant.SERVICE_PROVIDER)) {
                userId = jwtTokenUtil.extractId(jwtToken);
                List<Privileges> privileges = privilegeService.getServiceProviderPrivilege(userId);

                for (Privileges privilege : privileges) {
                    if (privilege.getPrivilege_name().equals(Constant.PRIVILEGE_ADD_PRODUCT)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }

    public Category validateCategory(Long categoryId) throws Exception {
        try {
            if (categoryId <= 0) throw new IllegalArgumentException("Category id cannot be <= 0.");
            Category category = catalogService.findCategoryById(categoryId);
            if (category == null || ((Status) category).getArchived() == 'Y') {
                throw new IllegalArgumentException("Category not found with this Id.");
            }
            return category;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught while validating category: " + exception.getMessage() + "\n");
        }
    }

    public boolean addProductDtoValidation(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getQuantity() != null) {
                if (addProductDto.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity cannot be <= 0.");
                }
            } else {
                addProductDto.setQuantity(Constant.DEFAULT_QUANTITY);
            }

            if (addProductDto.getPlatformFee() != null) {
                if (addProductDto.getPlatformFee() <= 0) {
                    throw new IllegalArgumentException("Platform fee cannot be <= 0.");
                }
            } else {
                addProductDto.setPlatformFee(DEFAULT_PLATFORM_FEE);
            }

            if (addProductDto.getPriorityLevel() != null) {
                if (addProductDto.getPriorityLevel() <= 0 || addProductDto.getPriorityLevel() > 5) {
                    throw new IllegalArgumentException("Priority level must lie between 1-5.");
                }
            } else {
                addProductDto.setPriorityLevel(DEFAULT_PRIORITY_LEVEL);
            }

            if (addProductDto.getMetaTitle() == null || addProductDto.getMetaTitle().trim().isEmpty()) {
                throw new IllegalArgumentException(PRODUCTTITLENOTGIVEN);
            } else {
                addProductDto.setMetaTitle(addProductDto.getMetaTitle().trim());
            }

            if (addProductDto.getDisplayTemplate() == null || addProductDto.getDisplayTemplate().trim().isEmpty()) {
                addProductDto.setDisplayTemplate(addProductDto.getMetaTitle());
            } else {
                addProductDto.setDisplayTemplate(addProductDto.getDisplayTemplate().trim());
            }

/*            if (addProductDto.getMetaDescription() == null || addProductDto.getMetaDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Description cannot be null or empty.");
            } else {
                addProductDto.setMetaDescription(addProductDto.getMetaDescription().trim());
            }
             */

            String formattedDate = dateFormat.format(new Date());
            Date activeStartDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date

            if (addProductDto.getActiveEndDate() == null || addProductDto.getGoLiveDate() == null || addProductDto.getActiveStartDate() == null) {
                throw new IllegalArgumentException("Active start date, active end date, and go live date cannot be empty.");
            }
            dateFormat.parse(dateFormat.format(addProductDto.getActiveStartDate()));
            dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
            dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));
            Date activeDateStart = stripTime(addProductDto.getActiveStartDate());
            Date activeDateEnd = stripTime(addProductDto.getActiveEndDate());
            if (!addProductDto.getActiveEndDate().after(activeStartDate)) {
                throw new IllegalArgumentException("Expiration date cannot be before or equal of current date.");
            } else if (!addProductDto.getGoLiveDate().before(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Go live date cannot be after or equal of active end date.");
            } else if (activeDateStart.after(activeDateEnd)) {
                throw new IllegalArgumentException("Active start date cannot be after active end date.");
            } else if (!isSameOrFutureDate(addProductDto.getGoLiveDate())) {
                throw new IllegalArgumentException("Go live date cannot be past of current date.");
            }

            if(addProductDto.getExamDateFrom()!=null)
            {
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                Date examDateFrom = stripTime(addProductDto.getExamDateFrom());
                Date activeEndDate = stripTime(addProductDto.getActiveEndDate());

                if (!examDateFrom.after(activeEndDate)) {
                    throw new IllegalArgumentException("Tentative examination date from must be after active end date.");
                }
            }
            if(addProductDto.getExamDateTo()!=null)
            {
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));
                Date examDateTo = stripTime(addProductDto.getExamDateTo());
                Date activeEndDate = stripTime(addProductDto.getActiveEndDate());

                if (!examDateTo.after(activeEndDate))
                {
                    throw new IllegalArgumentException("tentative examination date to must be after active end date");
                }
            }
            if(addProductDto.getExamDateFrom()!=null && addProductDto.getExamDateTo()!=null )
            {
                Date examDateTo = stripTime(addProductDto.getExamDateTo());
                Date examDateFrom = stripTime(addProductDto.getExamDateFrom());
                if (examDateTo.before(examDateFrom)) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }
            }

            if(addProductDto.getAdvertisement() == null || addProductDto.getAdvertisement() <= 0) {
                throw new IllegalArgumentException("Advertisement cannot be null or <= 0.");
            }

            Advertisement advertisement = advertisementService.getAdvertisementById(addProductDto.getAdvertisement());
            if (advertisement == null) {
                throw new NoSuchElementException("Advertisement not found.");
            }

            if (addProductDto.getApplicationScope() == null || addProductDto.getApplicationScope() <= 0) {
                throw new IllegalArgumentException("Application scope cannot be null or <= 0.");
            }

            CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());
            if (applicationScope == null) {
                throw new NoSuchElementException("application scope not found.");
            }

            if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_CENTER)) {

                if (addProductDto.getState() != null) {
                    throw new IllegalArgumentException("State cannot be given if application scope " + applicationScope.getApplicationScope());
                }
                if (addProductDto.getDomicileRequired() != null && addProductDto.getDomicileRequired()) {
                    throw new IllegalArgumentException("Domicile required cannot be true if application scope " + applicationScope.getApplicationScope());
                }
                addProductDto.setDomicileRequired(false);

            } else if (applicationScope.getApplicationScope().equals(APPLICATION_SCOPE_STATE)) {
                if (addProductDto.getDomicileRequired() == null || addProductDto.getState() == null) {
                    throw new IllegalArgumentException("For application scope: " + applicationScope.getApplicationScope() + " domicile and state cannot be null.");
                }

                if (addProductDto.getState() <= 0) {
                    throw new IllegalArgumentException("State cannot be <= 0.");
                }

                StateCode state = districtService.getStateByStateId(addProductDto.getState());
                if (state == null) {
                    throw new NoSuchElementException("State not found.");
                }
            }

            if (addProductDto.getReservedCategory() == null || addProductDto.getReservedCategory().isEmpty()) {
                throw new IllegalArgumentException("Reserve category must not be null or empty.");
            }

            if(addProductDto.getIsReviewRequired()==null)
            {
                addProductDto.setIsReviewRequired(true);
            }

            if(addProductDto.getIsMultiplePostSameFee()==null)
            {
                throw new IllegalArgumentException("You have to select whether multiple post have same fees");
            }

            if(addProductDto.getPosts()==null || addProductDto.getPosts().isEmpty())
            {
                throw new IllegalArgumentException("Post cannot be null or empty");
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (NoSuchElementException noSuchElementException) {
            exceptionHandlingService.handleException(noSuchElementException);
            throw new IllegalArgumentException(noSuchElementException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new ParseException(parseException.getMessage() + "\n", parseException.getErrorOffset());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage() + "\n");
        }
    }

    public boolean addProductDtoWithoutValidation(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getQuantity() != null) {
                if (addProductDto.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity cannot be <= 0.");
                }
            } else {
                addProductDto.setQuantity(Constant.DEFAULT_QUANTITY);
            }

            if (addProductDto.getPlatformFee() != null) {
                if (addProductDto.getPlatformFee() <= 0) {
                    throw new IllegalArgumentException("Platform fee cannot be <= 0.");
                }
            } else {
                addProductDto.setPlatformFee(DEFAULT_PLATFORM_FEE);
            }

            if (addProductDto.getPriorityLevel() != null) {
                if (addProductDto.getPriorityLevel() <= 0 || addProductDto.getPriorityLevel() > 5) {
                    throw new IllegalArgumentException("Priority level must lie between 1-5.");
                }
            } else {
                addProductDto.setPriorityLevel(DEFAULT_PRIORITY_LEVEL);
            }

            if (addProductDto.getMetaTitle() == null || addProductDto.getMetaTitle().trim().isEmpty()) {
                throw new IllegalArgumentException(PRODUCTTITLENOTGIVEN);
            } else {
                addProductDto.setMetaTitle(addProductDto.getMetaTitle().trim());
            }

            if(addProductDto.getDisplayTemplate()!=null)
            {
                addProductDto.setDisplayTemplate(addProductDto.getDisplayTemplate().trim());
            }

           /* if (addProductDto.getMetaDescription() == null || addProductDto.getMetaDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Description cannot be null or empty.");
            } else {
                addProductDto.setMetaDescription(addProductDto.getMetaDescription().trim());
            }
            */
            String formattedDate = dateFormat.format(new Date());
            Date activeStartDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date

            if (addProductDto.getActiveEndDate() == null || addProductDto.getGoLiveDate() == null || addProductDto.getActiveStartDate() == null) {
                throw new IllegalArgumentException("Active start date, active end date, and go live date cannot be empty.");
            }
            dateFormat.parse(dateFormat.format(addProductDto.getActiveStartDate()));
            dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
            dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));
            Date activeDateStart = stripTime(addProductDto.getActiveStartDate());
            Date activeDateEnd = stripTime(addProductDto.getActiveEndDate());

            if (!addProductDto.getActiveEndDate().after(activeStartDate)) {
                throw new IllegalArgumentException("Expiration date cannot be before or equal of current date.");
            } else if (!addProductDto.getGoLiveDate().before(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Go live date cannot be after or equal of active end date.");
            }else if (activeDateStart.after(activeDateEnd)) {
                throw new IllegalArgumentException("Active start date cannot be after active end date.");
            } else if (!isSameOrFutureDate(addProductDto.getGoLiveDate())) {
                throw new IllegalArgumentException("Go live date cannot be past of current date.");
            }
            if(addProductDto.getExamDateFrom()!=null)
            {
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
            }
            if(addProductDto.getExamDateTo()!=null)
            {
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));
            }

            if(addProductDto.getExamDateFrom()!=null)
            {
                Date examDateFrom = stripTime(addProductDto.getExamDateFrom());
                Date activeEndDate = stripTime(addProductDto.getActiveEndDate());

                if (!examDateFrom.after(activeEndDate)) {
                    throw new IllegalArgumentException("Tentative examination date from must be after active end date.");
                }
            }
            if(addProductDto.getExamDateTo()!=null)
            {
                Date examDateTo = stripTime(addProductDto.getExamDateTo());
                Date activeEndDate = stripTime(addProductDto.getActiveEndDate());

                if (!examDateTo.after(activeEndDate))
                {
                    throw new IllegalArgumentException("tentative examination date to must be after active end date");
                }
            }
            if(addProductDto.getExamDateFrom()!=null && addProductDto.getExamDateTo()!=null )
            {
                Date examDateTo = stripTime(addProductDto.getExamDateTo());
                Date examDateFrom = stripTime(addProductDto.getExamDateFrom());
                if (examDateTo.before(examDateFrom)) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }
            }

            if (addProductDto.getApplicationScope() !=null) {
                CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());
                if (applicationScope == null) {
                    throw new NoSuchElementException("application scope not found.");
                }

                if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_CENTER)) {

                    if (addProductDto.getState() != null) {
                        throw new IllegalArgumentException("State cannot be given if application scope " + applicationScope.getApplicationScope());
                    }
                    if (addProductDto.getDomicileRequired() != null && addProductDto.getDomicileRequired()) {
                        throw new IllegalArgumentException("Domicile required cannot be true if application scope " + applicationScope.getApplicationScope());
                    }
                    addProductDto.setDomicileRequired(false);

                } else if (applicationScope.getApplicationScope().equals(APPLICATION_SCOPE_STATE)) {
                    if (addProductDto.getDomicileRequired() == null || addProductDto.getState() == null) {
                        throw new IllegalArgumentException("For application scope: " + applicationScope.getApplicationScope() + " domicile and state cannot be null.");
                    }

                    if (addProductDto.getState() <= 0) {
                        throw new IllegalArgumentException("State cannot be <= 0.");
                    }

                    StateCode state = districtService.getStateByStateId(addProductDto.getState());
                    if (state == null) {
                        throw new NoSuchElementException("State not found.");
                    }
                }
            }
            if(addProductDto.getIsReviewRequired()==null)
            {
                addProductDto.setIsReviewRequired(true);
            }
            if (addProductDto.getReservedCategory() == null || addProductDto.getReservedCategory().isEmpty()) {
                throw new IllegalArgumentException("Reserve category must not be null or empty.");
            }

            if (addProductDto.getIsMultiplePostSameFee() != null) {
                if(addProductDto.getPosts()==null || addProductDto.getPosts().isEmpty())
                {
                    throw new IllegalArgumentException("Post cannot be null or empty");
                }
            }


            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (NoSuchElementException noSuchElementException) {
            exceptionHandlingService.handleException(noSuchElementException);
            throw new IllegalArgumentException(noSuchElementException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new ParseException(parseException.getMessage() + "\n", parseException.getErrorOffset());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage() + "\n");
        }
    }

    public void validateUpdateFields(CustomProduct customProduct) throws Exception {
        try
        {
            if (customProduct.getDisplayTemplate() == null || customProduct.getDisplayTemplate().trim().isEmpty()) {
                throw new IllegalArgumentException("Display Template cannot be null to move Product from Draft to NEW state ");
            }

            if (customProduct.getExamDateFrom() == null || customProduct.getExamDateTo() == null) {
                throw new IllegalArgumentException("Exam Date-From and Exam Date-To cannot be null to move Product from Draft to NEW state ");
            }

            if (customProduct.getCustomApplicationScope() == null) {
                throw new IllegalArgumentException("Application scope cannot be null to move Product from Draft to NEW state ");
            }
            if(customProduct.getPosts()==null || customProduct.getPosts().isEmpty())
            {
                throw new IllegalArgumentException("Posts cannot be empty or null to move Product from Draft to NEW state");
            }
        }
        catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        }
        catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage() + "\n");
        }
    }

    public ResponseEntity<?> changeStateProductFromDraftToNew(CustomProduct customProduct, CustomProductWrapper wrapper) throws Exception {
        try{
            validateUpdateFields(customProduct);
            CustomProductState customProductState=null;
            customProductState= productStateService.getProductStateByName(PRODUCT_STATE_NEW);
            if (customProductState == null) {
                return ResponseService.generateErrorResponse("Custom product state not found.", HttpStatus.NOT_FOUND);
            }
            customProduct.setProductState(customProductState);
            List<Post>postList= customProduct.getPosts();
            wrapper.wrapDetails(customProduct,postList,null,productReserveCategoryFeePostRefService);
            return ResponseService.generateSuccessResponse("Product is saved as NEW Product",wrapper,HttpStatus.OK);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        }
        catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage() + "\n");
        }
    }

    public CustomJobGroup validateCustomJobGroup(Long customJobGroupId) throws Exception {
        try {
            CustomJobGroup jobGroup = jobGroupService.getJobGroupById(customJobGroupId);
            return jobGroup;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION WHILE VALIDATING ADD PRODUCT DTO: " + exception.getMessage() + "\n");
        }
    }

    public Role getRoleByToken(String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);
            return role;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }

    public Long getUserIdByToken(String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);
            Long userId = jwtTokenUtil.extractId(jwtToken);

            return userId;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }

    public boolean validateReserveCategory(AddProductDto addProductDto) throws Exception {
        try {

            if (addProductDto.getReservedCategory().isEmpty()) {
                throw new IllegalArgumentException("Reserve category cannot be empty.");
            }
            Set<Long> reserveCategoryId = new HashSet<>();
            Set<Integer>genderCategoryComboSet=new HashSet<>();

            Date currentDate = new Date(); // Current date for comparison
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);

            calendar.add(Calendar.YEAR, -105);
            Date minBornAfterDate = calendar.getTime();
            calendar.add(Calendar.YEAR, 100);
            Date maxBornBeforeDate = calendar.getTime();

            for (int reserveCategoryIndex = 0; reserveCategoryIndex < addProductDto.getReservedCategory().size(); reserveCategoryIndex++) {
                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory() == null || addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory() <= 0) {
                    throw new IllegalArgumentException("Reserve category id cannot be null or <= 0.");
                }if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getGender() == null || addProductDto.getReservedCategory().get(reserveCategoryIndex).getGender() <= 0) {
                    throw new IllegalArgumentException("Gender id cannot be null or <= 0.");
                }
                CustomGender gender=genderService.getGenderByGenderId(addProductDto.getReservedCategory().get(reserveCategoryIndex).getGender());
                if(gender==null)
                    throw new NotFoundException("Invalid gender id");
                CustomReserveCategory category=reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());
                if(category==null)
                    throw new NotFoundException("Invalid category id");
                int genderAndCategoryCombo=(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory().intValue())*10+(addProductDto.getReservedCategory().get(reserveCategoryIndex).getGender().intValue());
                if(gender.getGenderName().equals(Constant.NO_GENDER)&&category.getReserveCategoryName().equals(Constant.NO_CATEGORY)&&addProductDto.getReservedCategory().size()>1)
                {
                    throw new IllegalArgumentException("This product is set to be category and gender independent, so no additional category/gender fees can be applied.");
                }
                if(!genderCategoryComboSet.add(genderAndCategoryCombo))
                {
                    throw new IllegalArgumentException("Duplicate combination of gender and reserve category not allowed.");
                }
                /*if(gender.getGenderName().equals(Constant.NO_GENDER))
                {
                    Boolean result=checkForOpenGender(gender,addProductDto);
                    if(result)
                        throw new IllegalArgumentException("This product is set to be gender independent, so no additional gender fees can be applied.");
                }

                if(category.getReserveCategoryName().equals(Constant.NO_CATEGORY))
                {
                    Boolean result=checkForOpenCategory(category,addProductDto);
                    if(result)
                        throw new IllegalArgumentException("This product is set to be category independent, so no additional category fees can be applied.");
                }*/
                reserveCategoryId.add(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());

                CustomReserveCategory reserveCategory = reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());
                if (reserveCategory == null) {
                    throw new IllegalArgumentException("Reserve category not found with id: " + addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());
                }

                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getFee() == null || addProductDto.getReservedCategory().get(reserveCategoryIndex).getFee() < 0) {
                    throw new IllegalArgumentException("Fee cannot be null or <= 0.");
                }

                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getPost() == null) {
                    addProductDto.getReservedCategory().get(reserveCategoryIndex).setPost(Constant.DEFAULT_QUANTITY);
                } else if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getPost() <= 0) {
                    throw new IllegalArgumentException(POSTLESSTHANORZERO);
                }

              /*  if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore() == null || addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter() == null) {
                    throw new IllegalArgumentException("Born before date and born after date cannot be empty.");
                }

                dateFormat.parse(dateFormat.format(addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter()));
                dateFormat.parse(dateFormat.format(addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore()));

                if (!addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore().before(new Date()) || !addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter().before(new Date())) {
                    throw new IllegalArgumentException("Born before date and born after date must be of past.");
                } else if (!addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter().before(addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore())) {
                    throw new IllegalArgumentException("Born after date must be past of born before date.");
                }

                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter().before(minBornAfterDate)) {
                    throw new IllegalArgumentException("Born after date cannot be more than 105 years in the past.");
                }
                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore().after(maxBornBeforeDate)) {
                    throw new IllegalArgumentException("Born before date must be at least 5 years in the past.");
                }*/
            }

            /*if (reserveCategoryId.size() != addProductDto.getReservedCategory().size()) {
                throw new IllegalArgumentException("Duplicate reserve categories not allowed.");
            }*/

            return true;
        } catch (NotFoundException | IllegalArgumentException notFoundException) {
            exceptionHandlingService.handleException(notFoundException);
            throw new IllegalArgumentException(notFoundException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating reserve category: " + exception.getMessage());
        }
    }
    public static Map<String, Date> calculateDateRange(Date asOfDate, int minAge, int maxAge) {
        LocalDate asOfLocalDate = asOfDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate bornBeforeLocalDate = asOfLocalDate.minusYears(minAge).plusDays(1);
        LocalDate bornAfterLocalDate = asOfLocalDate.minusYears(maxAge).minusDays(1);

        ZonedDateTime bornBeforeDateTime = bornBeforeLocalDate.atStartOfDay(ZoneId.of("Z"));
        ZonedDateTime bornAfterDateTime = bornAfterLocalDate.atStartOfDay(ZoneId.of("Z"));

        Date bornBeforeDate = Date.from(bornBeforeDateTime.toInstant());
        Date bornAfterDate = Date.from(bornAfterDateTime.toInstant());

        Map<String, Date> dateMap = new HashMap<>();
        dateMap.put("bornBeforeDate", bornBeforeDate);
        dateMap.put("bornAfterDate", bornAfterDate);

        return dateMap;
    }
    //****************************************
    //FOR FUTURE USE IF NEEDED
    /*public Boolean checkForOpenCategory(CustomReserveCategory openCategory,AddProductDto addProductDto)
    {
        Boolean flag=false;
        Boolean contains=false;
        for(AddReserveCategoryDto reserveCategory:addProductDto.getReservedCategory())
        {
            CustomReserveCategory reserveCategoryEntity=reserveCategoryService.getReserveCategoryById(reserveCategory.getReserveCategory());
            if(reserveCategoryEntity.getReserveCategoryName().equals(openCategory.getReserveCategoryName())&&contains.equals(false)) {
                contains = true;
                continue;
            }
            if(!reserveCategoryEntity.getReserveCategoryName().equals(openCategory.getReserveCategoryName())&&contains.equals(true))
                return true;
        }
        return flag;
    }
    public Boolean checkForOpenGender(CustomGender openGender,AddProductDto addProductDto)
    {
        Boolean flag=false;
        Boolean contains=false;
        for(AddReserveCategoryDto reserveCategory:addProductDto.getReservedCategory())
        {
            CustomGender genderEntity=genderService.getGenderByGenderId(reserveCategory.getGender());
            if(genderEntity.getGenderName().equals(openGender.getGenderName())&&contains.equals(false)) {
                contains = true;
                continue;
            }
            if(!genderEntity.getGenderName().equals(openGender.getGenderName())&&contains.equals(true))
                return true;
        }
        return flag;
    }*/
    //****************************************

    public boolean updateProductAccessAuthorisation(String authHeader, Long productId) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();

            if (productId <= 0) {
                throw new IllegalArgumentException("PRODUCT ID CANNOT BE <= 0");
            }
            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);
            if (customProduct == null || ((Status) customProduct).getArchived() == 'Y') {
                throw new IllegalArgumentException(PRODUCTNOTFOUND);
            }
            // if (!customProduct.getProductState().getProductState().equals(PRODUCT_STATE_MODIFIED) && !customProduct.getProductState().getProductState().equals(PRODUCT_STATE_NEW)) {
            //     throw new IllegalArgumentException("PRODUCT CAN ONLY BE MODIFIED IF IT IS IN NEW AND MODIFIED STATE");
            // }
            Long userId = null;
            if (role.equals(Constant.SUPER_ADMIN) || role.equals(Constant.ADMIN)) {
                return true;

                // -> NEED TO ADD THE USER_ID OF ADMIN OR SUPER ADMIN.

            } else if (role.equals(Constant.SERVICE_PROVIDER)) {

                userId = jwtTokenUtil.extractId(jwtToken);
                if (customProduct.getCreatoRole().getRole_name().equals(role) && customProduct.getUserId().equals(userId)) {
                    return true;
                }

                List<Privileges> privileges = privilegeService.getServiceProviderPrivilege(userId);
                for (Privileges privilege : privileges) {
                    if (privilege.getPrivilege_name().equals(Constant.PRIVILEGE_UPDATE_PRODUCT)) {
                        return true;
                    }
                }
            }

            return false;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }

    public boolean updateProductValidation(AddProductDto addProductDto, CustomProduct customProduct) throws Exception {
        try {
            if (addProductDto.getQuantity() != null) {
                if (addProductDto.getQuantity() <= 0) {
                    throw new IllegalArgumentException("QUANTITY CANNOT BE EMPTY <= 0");
                }
                customProduct.getDefaultSku().setQuantityAvailable(addProductDto.getQuantity());
            }

            if (addProductDto.getPriorityLevel() != null) {
                if (addProductDto.getPriorityLevel() <= 0 || addProductDto.getPriorityLevel() > 5) {
                    throw new IllegalArgumentException("PRIORITY LEVEL MUST BE BETWEEN 1-5");
                }
                customProduct.setPriorityLevel(addProductDto.getPriorityLevel());
            }

            if (addProductDto.getMetaTitle() != null && !addProductDto.getMetaTitle().trim().isEmpty()) {
                addProductDto.setMetaTitle(addProductDto.getMetaTitle().trim());
                customProduct.setMetaTitle(addProductDto.getMetaTitle());
                customProduct.getDefaultSku().setName(addProductDto.getMetaTitle());
            }

            if (addProductDto.getDisplayTemplate() != null && !addProductDto.getDisplayTemplate().trim().isEmpty()) {
                customProduct.setDisplayTemplate(addProductDto.getDisplayTemplate().trim());
            }

            if ((addProductDto.getPriorityLevel() != null) && (addProductDto.getPriorityLevel() <= 0 || addProductDto.getPriorityLevel() > 5)) {
                throw new IllegalArgumentException("PRIORITY LEVEL MUST LIE BETWEEN 1-5");
            }
            if (addProductDto.getMetaDescription() != null && !addProductDto.getMetaDescription().trim().isEmpty()) {
                addProductDto.setMetaDescription(addProductDto.getMetaDescription().trim());
                customProduct.setMetaDescription(addProductDto.getMetaDescription());
                customProduct.getDefaultSku().setDescription(addProductDto.getMetaDescription());
            }

            if (addProductDto.getPlatformFee() != null) {
                if (addProductDto.getPlatformFee() <= 0) {
                    throw new IllegalArgumentException("PLATFORM FEE CANNOT BE LESS THAN OR EQUAL TO ZERO");
                }
                customProduct.setPlatformFee(addProductDto.getPlatformFee());
            }

            if (addProductDto.getApplicationScope() != null) {
                CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());
                if (applicationScope == null) {
                    throw new IllegalArgumentException("NO APPLICATION SCOPE EXISTS WITH THIS ID");
                }
                if (customProduct.getCustomApplicationScope() != null) {
                    if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_STATE) && customProduct.getCustomApplicationScope().getApplicationScope().equals(Constant.APPLICATION_SCOPE_STATE)) {
                        if (addProductDto.getState() != null && districtService.getStateByStateId(addProductDto.getState()) != null) {
                            customProduct.setState(districtService.getStateByStateId(addProductDto.getState()));
                            customProduct.setCustomApplicationScope(applicationScope);
                        } else {
                            throw new IllegalArgumentException("STATE NOT FOUND");
                        }

                        if (addProductDto.getDomicileRequired() != null) {
                            customProduct.setDomicileRequired(addProductDto.getDomicileRequired());
                            customProduct.setCustomApplicationScope(applicationScope);
                        }
                    } else if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_STATE) && customProduct.getCustomApplicationScope().getApplicationScope().equals(Constant.APPLICATION_SCOPE_CENTER)) {
                        if (addProductDto.getState() == null || addProductDto.getDomicileRequired() == null) {
                            throw new IllegalArgumentException("DOMICILE AND STATE ARE REQUIRED FIELDS FOR STATE APPLICATION SCOPE");
                        }

                        if (districtService.getStateByStateId(addProductDto.getState()) != null) {
                            customProduct.setState(districtService.getStateByStateId(addProductDto.getState()));
                        } else {
                            throw new IllegalArgumentException("STATE IS NOT FOUND");
                        }
                        customProduct.setDomicileRequired(addProductDto.getDomicileRequired());
                        customProduct.setCustomApplicationScope(applicationScope);
                    } else if (applicationScope.getApplicationScope().equals(APPLICATION_SCOPE_CENTER)) {
                        if (addProductDto.getState() != null) {
                            throw new IllegalArgumentException("STATE NOT REQUIRED IN CASE OF CENTER LEVEL APPLICATION SCOPE");
                        }
                        if (addProductDto.getDomicileRequired() != null && addProductDto.getDomicileRequired()) {
                            throw new IllegalArgumentException("DOMICILE IS NOT REQUIRED IN CASE OF CENTER APPLICATION SCOPE");
                        }
                        addProductDto.setDomicileRequired(false);
                        addProductDto.setState(null);
                        customProduct.setState(null);
                        customProduct.setDomicileRequired(addProductDto.getDomicileRequired());
                        customProduct.setCustomApplicationScope(applicationScope);
                    }
                }
                else if(customProduct.getCustomApplicationScope()==null)
                {
                    if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_CENTER)) {
                        if (addProductDto.getState() != null) {
                            throw new IllegalArgumentException("State cannot be given if application scope " + applicationScope.getApplicationScope());
                        }
                        if (addProductDto.getDomicileRequired() != null && addProductDto.getDomicileRequired()) {
                            throw new IllegalArgumentException("Domicile required cannot be true if application scope " + applicationScope.getApplicationScope());
                        }
                        addProductDto.setDomicileRequired(false);
                        customProduct.setDomicileRequired(false);
                        customProduct.setState(null);
                        customProduct.setCustomApplicationScope(applicationScope);

                    } else if (applicationScope.getApplicationScope().equals(APPLICATION_SCOPE_STATE)) {
                        if (addProductDto.getDomicileRequired() == null || addProductDto.getState() == null) {
                            throw new IllegalArgumentException("For application scope: " + applicationScope.getApplicationScope() + " domicile and state cannot be null.");
                        }

                        if (addProductDto.getState() <= 0) {
                            throw new IllegalArgumentException("State cannot be <= 0.");
                        }

                        StateCode state = districtService.getStateByStateId(addProductDto.getState());
                        if (state == null) {
                            throw new NoSuchElementException("State not found.");
                        }
                        customProduct.setDomicileRequired(addProductDto.getDomicileRequired());
                        customProduct.setState(state);
                        customProduct.setCustomApplicationScope(applicationScope);

                    }
                }
            }

//            else if(customProduct.getCustomApplicationScope().getApplicationScope()!=null) {
//                if (customProduct.getCustomApplicationScope().getApplicationScope().equals(APPLICATION_SCOPE_STATE)) {
//                    if (addProductDto.getState() != null) {
//                        StateCode stateCode = districtService.getStateByStateId(addProductDto.getState());
//                        customProduct.setState(stateCode);
//                    }
//                    if (addProductDto.getDomicileRequired() != null) {
//                        customProduct.setDomicileRequired(addProductDto.getDomicileRequired());
//                    }
//                }
//            }

            if (addProductDto.getState() != null) {
                CustomSector customSector = sectorService.getSectorBySectorId(addProductDto.getSector());
                customProduct.setSector(customSector);
            }

            if (addProductDto.getFormComplexity() != null) {
                if (addProductDto.getFormComplexity() < 0 || addProductDto.getFormComplexity() > 5) {
                    throw new IllegalArgumentException("Form complexity must lie between 1 and 5");
                }
                customProduct.setFormComplexity(addProductDto.getFormComplexity());
            }

            if (addProductDto.getSelectionCriteria() != null) {
                customProduct.setSelectionCriteria(addProductDto.getSelectionCriteria());
            }

            if (addProductDto.getSector() != null) {
                CustomSector customSector = sectorService.getSectorBySectorId(addProductDto.getSector());
                customProduct.setSector(customSector);
            }

            if (addProductDto.getDownloadNotificationLink() != null) {
                addProductDto.setDownloadNotificationLink(addProductDto.getDownloadNotificationLink().trim());
                customProduct.setDownloadNotificationLink(addProductDto.getDownloadNotificationLink());
            }

            if (addProductDto.getDownloadSyllabusLink() != null) {
                addProductDto.setDownloadSyllabusLink(addProductDto.getDownloadSyllabusLink().trim());
                customProduct.setDownloadSyllabusLink(addProductDto.getDownloadSyllabusLink());
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new Exception(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATION: " + exception.getMessage() + "\n");
        }
    }

    public Boolean validateAndSetActiveStartDate(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getActiveStartDate() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getActiveStartDate()));

                if (addProductDto.getActiveEndDate() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
                    Date activeDateEnd= stripTime(addProductDto.getActiveEndDate());
                    Date activeDateStart=stripTime(addProductDto.getActiveStartDate());
                    if (activeDateStart.after(activeDateEnd)) {
                        throw new IllegalArgumentException("Active start date cannot be after active end date.");
                    }
                } else {
                    Date activeDateEnd= stripTime(customProduct.getActiveEndDate());
                    Date activeDateStart=stripTime(addProductDto.getActiveStartDate());
                    if (activeDateStart.after(activeDateEnd)) {
                        throw new IllegalArgumentException("Active start date cannot be after active end date.");
                    }
                }
                customProduct.setActiveStartDate(addProductDto.getActiveStartDate());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating active start date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean validateAndSetGoLiveDate(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getGoLiveDate() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));

                if(createdDate!=null)
                {
                    if(!isSameOrFutureDate(addProductDto.getGoLiveDate()))
                    {
                        throw new IllegalArgumentException("Go live date cannot be past of current date.");
                    }
                }

                if (addProductDto.getActiveEndDate() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
                    if (!addProductDto.getGoLiveDate().before(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("Go live date must be before active end date.");
                    }
                } else {
                    if (!addProductDto.getGoLiveDate().before(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException("Go live date must be before active end date.");
                    }
                }
                customProduct.setGoLiveDate(addProductDto.getGoLiveDate());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating go live date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean validateAndSetActiveEndDate(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getActiveEndDate() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));

                if(addProductDto.getGoLiveDate() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));
                    if(!addProductDto.getGoLiveDate().before(addProductDto.getActiveEndDate())){
                        throw new IllegalArgumentException("Active end date has be future of go Live Date");
                    }
                }else {
                    if(!customProduct.getGoLiveDate().before(addProductDto.getActiveEndDate())){
                        throw new IllegalArgumentException("Active end date has be future of go Live Date");
                    }
                }
                if(addProductDto.getActiveStartDate() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getActiveStartDate()));
                    Date activeDateEnd= stripTime(addProductDto.getActiveEndDate());
                    Date activeDateStart=stripTime(addProductDto.getActiveStartDate());
                    if (activeDateStart.after(activeDateEnd)) {
                        throw new IllegalArgumentException("Active start date cannot be after active end date.");
                    }
                } else {
                    Date activeDateEnd= stripTime(addProductDto.getActiveEndDate());
                    Date activeDateStart=stripTime(customProduct.getActiveStartDate());
                    if (activeDateStart.after(activeDateEnd)) {
                        throw new IllegalArgumentException("Active start date cannot be after active end date.");
                    }
                }

                if (addProductDto.getLastDateToPayFee() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
                    if (!addProductDto.getActiveEndDate().before(addProductDto.getLastDateToPayFee()) &&
                            !addProductDto.getActiveEndDate().equals(addProductDto.getLastDateToPayFee())) {
                        throw new IllegalArgumentException("active end date must be before or equal to the last date to pay fee.");
                    }
                } else if (addProductDto.getModificationDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getModificationDateFrom()));
                    if (!addProductDto.getActiveEndDate().before(addProductDto.getModificationDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of modification date from.");
                    }
                } else if (addProductDto.getAdmitCardDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateFrom()));
                    if (!addProductDto.getActiveEndDate().before(addProductDto.getAdmitCardDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of admit card from.");
                    }
                } else if (addProductDto.getExamDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                    if (!addProductDto.getActiveEndDate().before(addProductDto.getExamDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of exam date from.");
                    }
                } else if (customProduct.getLateDateToPayFee() != null) {
                    if (!addProductDto.getActiveEndDate().before(addProductDto.getLastDateToPayFee()) && !addProductDto.getActiveEndDate().equals(addProductDto.getLastDateToPayFee())) {
                        throw new IllegalArgumentException("active end date must be before or equal to the last date to pay fee.");
                    }
                } else if (customProduct.getModificationDateFrom() != null) {
                    if (!addProductDto.getActiveEndDate().before(customProduct.getModificationDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of modification date from.");
                    }
                } else if (customProduct.getAdmitCardDateFrom() != null) {
                    if (!addProductDto.getActiveEndDate().before(customProduct.getAdmitCardDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of admit card from.");
                    }
                } else if (customProduct.getExamDateFrom() != null) {
                    if (!addProductDto.getActiveEndDate().before(customProduct.getExamDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of exam date from.");
                    }
                }
                customProduct.setActiveEndDate(addProductDto.getActiveEndDate());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating active start date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean validateAndSetLastDateToPayFeeDate(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            // Check if lastDateToPayFee is null or empty (for when an empty value is passed)
            if (addProductDto.getLastDateToPayFee() == null) {
                // If last date to pay fee is null or empty, set it to null in the custom product
                customProduct.setLateDateToPayFee(null);
                return true;
            }

            // Proceed with validation only if the date is not null
            dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));

            // Your existing validation checks
            if (addProductDto.getActiveEndDate() != null) {
                if (addProductDto.getLastDateToPayFee().before(addProductDto.getActiveEndDate())) {
                    throw new IllegalArgumentException("Last day to pay fee cannot be before of active end date.");
                }
            } else if (customProduct.getActiveEndDate() != null) {
                if (addProductDto.getLastDateToPayFee().before(customProduct.getActiveEndDate())) {
                    throw new IllegalArgumentException("Last day to pay fee cannot be before of active end date.");
                }
            }

            // Additional validation checks remain the same
            if (addProductDto.getModificationDateFrom() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
                if (!addProductDto.getLastDateToPayFee().before(addProductDto.getModificationDateFrom())) {
                    throw new IllegalArgumentException("last date to pay fee have to be before of modified date from.");
                }
            } else if (customProduct.getModificationDateFrom() != null) {
                if (!addProductDto.getLastDateToPayFee().before(customProduct.getModificationDateFrom())) {
                    throw new IllegalArgumentException("last date to pay fee have to be before of modified date from.");
                }
            }

            if (addProductDto.getAdmitCardDateFrom() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
                if (!addProductDto.getLastDateToPayFee().before(addProductDto.getAdmitCardDateFrom())) {
                    throw new IllegalArgumentException("last date to pay fee have to be before of admit card from.");
                }
            } else if (customProduct.getAdmitCardDateFrom() != null) {
                if (!addProductDto.getLastDateToPayFee().before(customProduct.getAdmitCardDateFrom())) {
                    throw new IllegalArgumentException("last date to pay fee have to be before of admit card from.");
                }
            }

            if (addProductDto.getExamDateFrom() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
                if (!addProductDto.getLastDateToPayFee().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException("last date to pay fee have to be before of exam date from.");
                }
            } else if (customProduct.getExamDateFrom() != null) {
                if (!addProductDto.getLastDateToPayFee().before(customProduct.getExamDateFrom())) {
                    throw new IllegalArgumentException("last date to pay fee have to be before of exam date from.");
                }
            }

            // Set the validated date
            customProduct.setLateDateToPayFee(addProductDto.getLastDateToPayFee());
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating active start date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean validateAndSetModifiedDates(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            // Case 1: Both dates are null - set both as null in customProduct and return
            if (addProductDto.getModificationDateFrom() == null && addProductDto.getModificationDateTo() == null) {
                customProduct.setModificationDateFrom(null);
                customProduct.setModificationDateTo(null);
                return true;
            }

            // Case 2: Only ModificationDateFrom is provided
            if (addProductDto.getModificationDateFrom() != null && addProductDto.getModificationDateTo() == null) {
                dateFormat.parse(dateFormat.format(addProductDto.getModificationDateFrom()));

                // Validate ModificationDateFrom against other dates
                validateModificationDateFrom(addProductDto, customProduct);

                // Set values in customProduct
                customProduct.setModificationDateFrom(addProductDto.getModificationDateFrom());
                customProduct.setModificationDateTo(null);
                return true;
            }

            // Case 3: Only ModificationDateTo is provided
            if (addProductDto.getModificationDateFrom() == null && addProductDto.getModificationDateTo() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getModificationDateTo()));

                // Validate ModificationDateTo against other dates
                validateModificationDateTo(addProductDto, customProduct);

                // Set values in customProduct
                customProduct.setModificationDateFrom(null);
                customProduct.setModificationDateTo(addProductDto.getModificationDateTo());
                return true;
            }

            // Case 4: Both dates are provided - full validation
            dateFormat.parse(dateFormat.format(addProductDto.getModificationDateFrom()));
            dateFormat.parse(dateFormat.format(addProductDto.getModificationDateTo()));

            // Check if ModificationDateFrom is after ModificationDateTo
            if (addProductDto.getModificationDateFrom().after(addProductDto.getModificationDateTo())) {
                throw new IllegalArgumentException("Modified date from must be before or equal of modified date to.");
            }

            // Perform all validations
            validateModificationDateFrom(addProductDto, customProduct);
            validateModificationDateTo(addProductDto, customProduct);

            // Set values in customProduct
            customProduct.setModificationDateFrom(addProductDto.getModificationDateFrom());
            customProduct.setModificationDateTo(addProductDto.getModificationDateTo());
            return true;

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating modification dates: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    // Helper method to validate ModificationDateFrom
    private void validateModificationDateFrom(AddProductDto addProductDto, CustomProduct customProduct) {
        // Check against LastDateToPayFee
        if (addProductDto.getLastDateToPayFee() != null) {
            if (!addProductDto.getModificationDateFrom().after(addProductDto.getLastDateToPayFee())) {
                throw new IllegalArgumentException("Modified date from must be after last date to pay fee.");
            }
        } else if (customProduct.getLateDateToPayFee() != null) {
            if (!addProductDto.getModificationDateFrom().after(customProduct.getLateDateToPayFee())) {
                throw new IllegalArgumentException("Modified date from must be after last date to pay fee.");
            }
        }

        // Check against ActiveEndDate
        if (addProductDto.getActiveEndDate() != null) {
            if (!addProductDto.getModificationDateFrom().after(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Modified date from must be after active end date.");
            }
        } else if (customProduct.getActiveEndDate() != null) {
            if (!addProductDto.getModificationDateFrom().after(customProduct.getActiveEndDate())) {
                throw new IllegalArgumentException("Modified date from must be after active end date.");
            }
        }
    }

    // Helper method to validate ModificationDateTo
    private void validateModificationDateTo(AddProductDto addProductDto, CustomProduct customProduct) {
        // Check against AdmitCardDateFrom
        if (addProductDto.getAdmitCardDateFrom() != null) {
            if (!addProductDto.getModificationDateTo().before(addProductDto.getAdmitCardDateFrom())) {
                throw new IllegalArgumentException("Modified date to must be before or equal of admit card date from.");
            }
        } else if (customProduct.getAdmitCardDateFrom() != null) {
            if (!addProductDto.getModificationDateTo().before(customProduct.getAdmitCardDateFrom())) {
                throw new IllegalArgumentException("Modified date to must be before or equal of admit card date from.");
            }
        }

        // Check against ExamDateFrom
        if (addProductDto.getExamDateFrom() != null) {
            if (!addProductDto.getModificationDateTo().before(addProductDto.getExamDateFrom())) {
                throw new IllegalArgumentException("Modified date to must be before or equal of exam date from.");
            }
        } else if (customProduct.getExamDateFrom() != null) {
            if (!addProductDto.getModificationDateTo().before(customProduct.getExamDateFrom())) {
                throw new IllegalArgumentException("Modified date to must be before or equal of exam date from.");
            }
        }
    }

    public Boolean validateAndSetExamDates(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            // Case 1: If both dates are provided, validate them
            if (addProductDto.getExamDateFrom() != null && addProductDto.getExamDateTo() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

                // Check if from date is before to date
                if (addProductDto.getExamDateFrom().after(addProductDto.getExamDateTo())) {
                    throw new IllegalArgumentException("Exam date from must be before or equal of exam date to.");
                }

                // Perform all other validation checks
                validateExamDatesAgainstOtherDates(addProductDto, customProduct);

                // Set both dates
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());
                customProduct.setExamDateTo(addProductDto.getExamDateTo());
            }
            // Case 2: If only from date is provided
            else if (addProductDto.getExamDateFrom() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));

                // Perform validation checks
                validateExamDatesAgainstOtherDates(addProductDto, customProduct);

                // Set both dates
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());
                customProduct.setExamDateTo(null);
            }
            // Case 3: If only to date is provided
            else if (addProductDto.getExamDateTo() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

                if (customProduct.getExamDateFrom() != null) {
                    addProductDto.setExamDateFrom(customProduct.getExamDateFrom());
                }

                // Perform validation checks
                validateExamDatesAgainstOtherDates(addProductDto, customProduct);

                // Set both dates
                customProduct.setExamDateFrom(null);
                customProduct.setExamDateTo(addProductDto.getExamDateTo());
            }
            // Case 4: If both dates are null
            else {
                // Set both dates to null
                customProduct.setExamDateFrom(null);
                customProduct.setExamDateTo(null);
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating exam dates: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    // Helper method to centralize validation logic
    private void validateExamDatesAgainstOtherDates(AddProductDto addProductDto, CustomProduct customProduct) {
        // Validation against admit card dates
        if (addProductDto.getAdmitCardDateTo() != null) {
            if (!addProductDto.getExamDateFrom().after(addProductDto.getAdmitCardDateTo())) {
                throw new IllegalArgumentException("Exam date from must be after of admit card date to.");
            }
        } else if (customProduct.getAdmitCardDateTo() != null) {
            if (!addProductDto.getExamDateFrom().after(customProduct.getAdmitCardDateTo())) {
                throw new IllegalArgumentException("Exam date from must be after of admit card to.");
            }
        }

        // Validation against modification dates
        if (addProductDto.getModificationDateTo() != null) {
            if (!addProductDto.getExamDateFrom().after(addProductDto.getModificationDateTo())) {
                throw new IllegalArgumentException("Exam date from must be after of modified date to.");
            }
        } else if (customProduct.getModificationDateTo() != null) {
            if (!addProductDto.getExamDateFrom().after(customProduct.getModificationDateTo())) {
                throw new IllegalArgumentException("Exam date from must be after of modified date to.");
            }
        }

        // Validation against fee payment dates
        if (addProductDto.getLastDateToPayFee() != null) {
            if (!addProductDto.getExamDateFrom().after(addProductDto.getLastDateToPayFee())) {
                throw new IllegalArgumentException("Exam date from must be after of last date to pay fee.");
            }
        } else if (customProduct.getLateDateToPayFee() != null) {
            if (!addProductDto.getExamDateFrom().after(customProduct.getLateDateToPayFee())) {
                throw new IllegalArgumentException("Exam date from must be after of last date to pay fee.");
            }
        }

        // Validation against active end dates
        if (addProductDto.getActiveEndDate() != null) {
            if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Exam date from must be after of active end date.");
            }
        } else if (customProduct.getActiveEndDate() != null) {
            if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate())) {
                throw new IllegalArgumentException("Exam date from must be after of active end date.");
            }
        }
    }

    public Boolean validateAndSetAdmitCardDates(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            // Case 1: If both dates are provided, validate them
            if (addProductDto.getAdmitCardDateFrom() != null && addProductDto.getAdmitCardDateTo() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateFrom()));
                dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateTo()));

                // Check if from date is before to date
                if (addProductDto.getAdmitCardDateFrom().after(addProductDto.getAdmitCardDateTo())) {
                    throw new IllegalArgumentException("Admit card date from must be before or equal of admit card date to.");
                }

                // Perform all other validation checks
                validateAdmitCardDatesAgainstOtherDates(addProductDto, customProduct);

                // Set both dates
                customProduct.setAdmitCardDateFrom(addProductDto.getAdmitCardDateFrom());
                customProduct.setAdmitCardDateTo(addProductDto.getAdmitCardDateTo());
            }
            // Case 2: If only from date is provided
            else if (addProductDto.getAdmitCardDateFrom() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateFrom()));
//                addProductDto.setAdmitCardDateTo(addProductDto.getAdmitCardDateFrom());

                // Perform validation checks
                validateAdmitCardDatesAgainstOtherDates(addProductDto, customProduct);

                // Set both dates
                customProduct.setAdmitCardDateFrom(addProductDto.getAdmitCardDateFrom());
                customProduct.setAdmitCardDateTo(null);
            }
            // Case 3: If only to date is provided
            else if (addProductDto.getAdmitCardDateTo() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateTo()));

                if (customProduct.getAdmitCardDateFrom() != null) {
                    addProductDto.setAdmitCardDateFrom(customProduct.getAdmitCardDateFrom());
                }

                // Perform validation checks
                validateAdmitCardDatesAgainstOtherDates(addProductDto, customProduct);

                // Set both dates
                customProduct.setAdmitCardDateFrom(null);
                customProduct.setAdmitCardDateTo(addProductDto.getAdmitCardDateTo());
            }
            // Case 4: If both dates are null
            else {
                // Set both dates to null
                customProduct.setAdmitCardDateFrom(null);
                customProduct.setAdmitCardDateTo(null);
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating admit card dates: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    // Helper method to centralize validation logic for admit card dates
    private void validateAdmitCardDatesAgainstOtherDates(AddProductDto addProductDto, CustomProduct customProduct) {
        // Validation against modification dates
        if (addProductDto.getModificationDateTo() != null) {
            if (!addProductDto.getAdmitCardDateFrom().after(addProductDto.getModificationDateTo())) {
                throw new IllegalArgumentException("Admit card date from must be after modification date to.");
            }
        } else if (customProduct.getModificationDateTo() != null) {
            if (!addProductDto.getAdmitCardDateFrom().after(customProduct.getModificationDateTo())) {
                throw new IllegalArgumentException("Admit card date from must be after modification date.");
            }
        }

        // Validation against fee payment dates - Fix bug in original code that was checking ModificationDateTo
        if (addProductDto.getLastDateToPayFee() != null) {
            if (!addProductDto.getAdmitCardDateFrom().after(addProductDto.getLastDateToPayFee())) {
                throw new IllegalArgumentException("Admit card date from must be after last date to pay fee.");
            }
        } else if (customProduct.getLateDateToPayFee() != null) {
            if (!addProductDto.getAdmitCardDateFrom().after(customProduct.getLateDateToPayFee())) {
                throw new IllegalArgumentException("Admit card date from must be after last date to pay fee.");
            }
        }

        // Validation against active end dates
        if (addProductDto.getActiveEndDate() != null) {
            if (!addProductDto.getAdmitCardDateFrom().after(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Admit card date from must be after active end date.");
            }
        } else if (customProduct.getActiveEndDate() != null) {
            if (!addProductDto.getAdmitCardDateFrom().after(customProduct.getActiveEndDate())) {
                throw new IllegalArgumentException("Admit card date from must be after active end date.");
            }
        }

        // Validation against exam dates
        if (addProductDto.getExamDateFrom() != null) {
            if(addProductDto.getAdmitCardDateTo()!=null)
            {
                if (!addProductDto.getAdmitCardDateTo().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException("Admit card date to must be before or equal of exam date from.");
                }
            }

        } else if (customProduct.getExamDateFrom() != null) {
            if(addProductDto.getAdmitCardDateTo()!=null) {
                if (!addProductDto.getAdmitCardDateTo().before(customProduct.getExamDateFrom())) {
                    throw new IllegalArgumentException("Admit card date to must be before or equal of exam date from.");
                }
            }
        }
    }

    public Boolean validateAndSetActiveStartDateActiveEndDateAndGoLiveDateFields(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getActiveEndDate() != null && addProductDto.getGoLiveDate() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
                dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));

                if (addProductDto.getGoLiveDate().before(createdDate)) {
                    throw new IllegalArgumentException("GO LIVE DATE HAS TO OF FUTURE OF CURRENT DATE");
                } else if (!addProductDto.getActiveEndDate().after(customProduct.getActiveStartDate())) {
                    throw new IllegalArgumentException("ACTIVE END DATE CANNOT BE BEFORE OR EQUAL OF ACTIVE START DATE");
                } else if (!addProductDto.getActiveEndDate().after(addProductDto.getGoLiveDate()) || !addProductDto.getGoLiveDate().after(customProduct.getActiveStartDate())) {
                    throw new IllegalArgumentException("GO LIVE DATE CANNOT BE BEFORE OR EQUAL OF GO LIVE DATE AND BEFORE OR EQUAL OF ACTIVE START DATE");
                } else if (addProductDto.getExamDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));

                    if (!addProductDto.getActiveEndDate().before(addProductDto.getExamDateFrom())) {
                        throw new IllegalArgumentException("ACTIVE END DATE CANNOT BE AFTER OR EQUAL OF EXAM DATE FROM DATE");
                    }
                } else {
                    if (!addProductDto.getActiveEndDate().before(customProduct.getExamDateFrom())) {
                        throw new IllegalArgumentException("ACTIVE END DATE CANNOT BE AFTER OR EQUAL OF EXAM DATE FROM DATE");
                    }
                }
                customProduct.getDefaultSku().setActiveEndDate(addProductDto.getActiveEndDate());
                customProduct.setGoLiveDate(addProductDto.getGoLiveDate());

            } else if (addProductDto.getActiveEndDate() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));

                if (!addProductDto.getActiveEndDate().after(customProduct.getActiveStartDate())) {
                    throw new IllegalArgumentException("ACTIVE END DATE CANNOT BE BEFORE OR EQUAL OF ACTIVE START DATE");
                } else if (!addProductDto.getActiveEndDate().after(customProduct.getGoLiveDate())) {
                    throw new IllegalArgumentException("ACTIVE END DATE CANNOT BE BEFORE OR EQUAL OF GO LIVE DATE");
                } else if (addProductDto.getExamDateFrom() != null) {

                    dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("EXAM DATE FROM MUST BE AFTER ACTIVE END DATE");
                    }
                } else {
                    if (!customProduct.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("EXAM DATE FROM MUST BE AFTER ACTIVE END DATE");
                    }
                }

                customProduct.getDefaultSku().setActiveEndDate(addProductDto.getActiveEndDate());
            } else if (addProductDto.getGoLiveDate() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));

                if (!addProductDto.getGoLiveDate().after(customProduct.getActiveStartDate())) {
                    throw new IllegalArgumentException("GO LIVE DATE CANNOT BE BEFORE OR EQUAL OF ACTIVE START DATE");
                } else if (!customProduct.getActiveEndDate().after(addProductDto.getGoLiveDate())) {
                    throw new IllegalArgumentException("GO LIVE DATE CANNOT BE AFTER AND EQUAL OF EXPIRY DATE");
                }
                customProduct.setGoLiveDate(addProductDto.getGoLiveDate());
            }

            return true;
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("PARSE EXCEPTION CAUGHT WHILE VALIDATING ADD PRODUCT DTO: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public Boolean validateAndSetExamDateFromAndExamDateToFields(AddProductDto addProductDto, CustomProduct customProduct) throws Exception {
        try {
            if (addProductDto.getExamDateFrom() != null && addProductDto.getExamDateTo() != null) {

                // Validation on date for being wrong types. -> these needs to be changed or we have to add exception.
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

                if (addProductDto.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }

                if (addProductDto.getActiveEndDate() != null) {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate()) || !addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEDATEAFTERACTIVEENDDATE);
                    }

                } else {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate()) || !addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEDATEAFTERACTIVEENDDATE);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());
                customProduct.setExamDateTo(addProductDto.getExamDateTo());

            } else if (addProductDto.getExamDateFrom() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                if (customProduct.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }

                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                } else {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());

            } else if (addProductDto.getExamDateTo() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));
                if (addProductDto.getExamDateTo().before(customProduct.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }
                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                } else {
                    if (!addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("TENTATIVE EXAMINATION DATA MUST BE AFTER ACTIVE END DATE");
                    }
                }
                customProduct.setExamDateTo(addProductDto.getExamDateTo());
            }
            return true;

        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("PARSE EXCEPTION CAUGHT WHILE VALIDATING ADD PRODUCT DTO: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean validateExamDateFromAndExamDateTo(AddProductDto addProductDto, CustomProduct customProduct) throws Exception {
        try {
            if (addProductDto.getExamDateFrom() != null && addProductDto.getExamDateTo() != null) {

                // Validation on date for being wrong types. -> these needs to be changed or we have to add exception.
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

                if (addProductDto.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }

                if (addProductDto.getActiveEndDate() != null) {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate()) || !addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEDATEAFTERACTIVEENDDATE);
                    }

                } else {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate()) || !addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEDATEAFTERACTIVEENDDATE);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());
                customProduct.setExamDateTo(addProductDto.getExamDateTo());

            } else if (addProductDto.getExamDateFrom() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                if (customProduct.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }

                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                } else {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());

            } else if (addProductDto.getExamDateTo() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));
                if (addProductDto.getExamDateTo().before(customProduct.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }
                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                } else {
                    if (!addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("TENTATIVE EXAMINATION DATA MUST BE AFTER ACTIVE END DATE");
                    }
                }
                customProduct.setExamDateTo(addProductDto.getExamDateTo());

            }
            return true;
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("PARSE EXCEPTION CAUGHT WHILE VALIDATING ADD PRODUCT DTO: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean validateProductState(AddProductDto addProductDto, CustomProduct customProduct, String authHeader) throws Exception {
        try {
            if (addProductDto.getProductState() != null) {

                String jwtToken = authHeader.substring(7);
                Long userId = jwtTokenUtil.extractId(jwtToken);

                Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
                String role = roleService.findRoleName(roleId);

                if (customProduct.getUserId().equals(userId)) {
                    throw new IllegalArgumentException("SERVICE PROVIDER WHO CREATED THE PRODUCT CANNOT CHANGE ITS STATE");
                }

                CustomProductState customProductState = productStateService.getProductStateById(addProductDto.getProductState());
                if (customProductState == null) {
                    throw new IllegalArgumentException("NO PRODUCT STATE EXIST WITH THIS ID");
                }

                if(customProductState.getProductState().equals("DRAFT") && !customProduct.getProductState().getProductState().equals("DRAFT")) {
                    throw new IllegalArgumentException("PRODUCT STATE CANNOT BE CHANGED FROM ACTUAL PRODUCT STATES TO DRAFT STATE.");
                }

                if (role.equals(Constant.SERVICE_PROVIDER)) {
                    if ((!customProduct.getProductState().getProductState().equals(Constant.PRODUCT_STATE_NEW) && !customProduct.getProductState().getProductState().equals(Constant.PRODUCT_STATE_MODIFIED)) || (!customProductState.getProductState().equals(PRODUCT_STATE_APPROVED) && !customProductState.getProductState().equals(PRODUCT_STATE_REJECTED))) {
                        throw new IllegalArgumentException("PRODUCT STATE ONLY CHANGE FROM NEW/MODIFIABLE TO APPROVED OR REJECTED STATE");
                    }
                    List<Privileges> privileges = privilegeService.getServiceProviderPrivilege(userId);
                    for (Privileges privilege : privileges) {
                        if ((privilege.getPrivilege_name().equals(Constant.PRIVILEGE_APPROVE_PRODUCT) && customProductState.getProductState().equals(Constant.PRODUCT_STATE_APPROVED))) {
                            customProduct.setProductState(customProductState);
                            return true;
                        } else if ((privilege.getPrivilege_name().equals(Constant.PRIVILEGE_REJECT_PRODUCT) && customProductState.getProductState().equals(Constant.PRODUCT_STATE_REJECTED))) {
                            if (addProductDto.getRejectionStatus() == null) {
                                throw new IllegalArgumentException("REJECTION STATUS CANNOT BE NULL IF PRODUCT IS REJECTED");
                            }
                            CustomProductRejectionStatus productRejectionStatus = productRejectionStatusService.getAllRejectionStatusByRejectionStatusId(addProductDto.getRejectionStatus());
                            if (productRejectionStatus == null) {
                                throw new IllegalArgumentException("NO PRODUCT REJECTION STATUS IS FOUND");
                            }
                            customProduct.setProductState(customProductState);
                            customProduct.setRejectionStatus(productRejectionStatus);
                            return true;
                        }
                    }
                    throw new IllegalArgumentException("Not have privilege to perform action.");
                } else if (role.equals(Constant.ADMIN) || role.equals(Constant.SUPER_ADMIN)) {
                    if (customProductState.getProductState().equals("REJECTED")) {
                        if(addProductDto.getRejectionStatus() == null) {
                            throw new IllegalArgumentException("REJECTION STATE CANNOT BE NULL IF PRODUCT IS REJECTED");
                        } else {
                            CustomProductRejectionStatus productRejectionStatus = productRejectionStatusService.getAllRejectionStatusByRejectionStatusId(addProductDto.getRejectionStatus());
                            if (productRejectionStatus == null) {
                                throw new IllegalArgumentException("NO PRODUCT REJECTION STATUS IS FOUND");
                            }
                            customProduct.setRejectionStatus(productRejectionStatus);
                        }
                    } else if (!customProduct.getProductState().getProductState().equals(PRODUCT_STATE_MODIFIED) && customProductState.getProductState().equals(PRODUCT_STATE_MODIFIED)) {
                        throw new IllegalArgumentException("PRODUCT STATE CANNOT MOVE FROM ANY OTHER STATE TO MODIFIED STATE");
                    } else if (!customProduct.getProductState().getProductState().equals(PRODUCT_STATE_DRAFT) && customProductState.getProductState().equals(PRODUCT_STATE_DRAFT)) {
                        throw new IllegalArgumentException("PRODUCT STATE CANNOT MOVE FROM ANY OTHER STATE TO DRAFT STATE");
                    }
                    customProduct.setProductState(customProductState);

                    return true;
                } else {
                    throw new IllegalArgumentException("Role not Service provider and admin or super admin");
                }
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean deleteOldReserveCategoryMapping(CustomProduct customProduct) throws Exception {
        try {
            productReserveCategoryFeePostRefService.removeProductReserveCategoryFeeAndPostByProductId(customProduct);
            productReserveCategoryBornBeforeAfterRefService.removeProductReserveCategoryBornBeforeAfterByProductId(customProduct);
            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean deleteOldPhysicalRequirement(CustomProduct customProduct) throws Exception {
        try {
            productGenderPhysicalRequirementService.removeProductGenderPhysicalRequirementByProductId(customProduct);
            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean validateAdmitCardDates(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getAdmitCardDateFrom() == null && addProductDto.getAdmitCardDateTo() == null) {
                return true;
            }

            if (addProductDto.getAdmitCardDateFrom() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateFrom()));
            }
            if (addProductDto.getAdmitCardDateTo() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateTo()));
            }

            if (addProductDto.getAdmitCardDateFrom() != null && addProductDto.getAdmitCardDateTo() != null) {
                if (addProductDto.getAdmitCardDateFrom().after(addProductDto.getAdmitCardDateTo())) {
                    throw new IllegalArgumentException("Admit card date from cannot be of future of admit card date to.");
                }
            } else if (addProductDto.getAdmitCardDateFrom() != null) {
                addProductDto.setAdmitCardDateTo(addProductDto.getAdmitCardDateFrom());
            } else if (addProductDto.getAdmitCardDateTo() != null) {
                addProductDto.setAdmitCardDateFrom(addProductDto.getAdmitCardDateTo());
            }

            if (!addProductDto.getExamDateFrom().after(addProductDto.getAdmitCardDateTo())) {
                throw new IllegalArgumentException("Admit card to cannot be future of exam date from.");
            }

            if (addProductDto.getModificationDateTo() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getModificationDateTo()));
                if (!addProductDto.getAdmitCardDateFrom().after(addProductDto.getModificationDateTo())) {
                    throw new IllegalArgumentException("Admit card date from must be of future of modification date to.");
                }
            } else if (addProductDto.getLastDateToPayFee() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
                if (!addProductDto.getAdmitCardDateFrom().after(addProductDto.getLastDateToPayFee())) {
                    throw new IllegalArgumentException("Admit card date from must be of future of last date to pay application fee.");
                }
            } else {
                if (!addProductDto.getAdmitCardDateFrom().after(addProductDto.getActiveEndDate())) {
                    throw new IllegalArgumentException("Admit card date from must be of future of active end date.");
                }
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("PARSE EXCEPTION CAUGHT WHILE VALIDATING ADMIT CARD DATES: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean validateModificationDates(AddProductDto addProductDto) throws Exception {
        try {

            if (addProductDto.getModificationDateFrom() == null && addProductDto.getModificationDateTo() == null) {
                return true;
            }

            if (addProductDto.getModificationDateFrom() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getModificationDateFrom()));
            }
            if (addProductDto.getModificationDateTo() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getModificationDateTo()));
            }

            if (addProductDto.getModificationDateFrom() != null && addProductDto.getModificationDateTo() != null) {
                if (addProductDto.getModificationDateFrom().after(addProductDto.getModificationDateTo())) {
                    throw new IllegalArgumentException("Modification date from cannot be of future of modification date to.");
                }

            } else if (addProductDto.getAdmitCardDateFrom() != null) {
                addProductDto.setModificationDateTo(addProductDto.getModificationDateFrom());
            } else if (addProductDto.getAdmitCardDateTo() != null) {
                addProductDto.setModificationDateFrom(addProductDto.getModificationDateTo());
            }

            if (addProductDto.getAdmitCardDateFrom() != null) {
                if (addProductDto.getModificationDateTo().after(addProductDto.getAdmitCardDateFrom())) {
                    throw new IllegalArgumentException("Modification date to cannot be of future of admit card date from.");
                }
            } else {
                if(addProductDto.getExamDateFrom()!=null)
                {
                    if (addProductDto.getModificationDateTo().after(addProductDto.getExamDateFrom())) {
                        throw new IllegalArgumentException("Modification date to cannot be of future of exam date from");
                    }
                }
            }

            if (addProductDto.getLastDateToPayFee() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));

                if (!addProductDto.getModificationDateFrom().after(addProductDto.getLastDateToPayFee())) {
                    throw new IllegalArgumentException("Modification date from has to be future of last date to pay application fee.");
                }
            } else {
                if (!addProductDto.getModificationDateFrom().after(addProductDto.getActiveEndDate())) {
                    throw new IllegalArgumentException("Modification date from has to be future of active end date.");
                }
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating modification dates: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception occurred while validating modification dates: " + exception.getMessage());
        }
    }

    public boolean validateLastDateToPayFee(AddProductDto addProductDto) throws Exception {
        try {

            if (addProductDto.getLastDateToPayFee() == null) {
                return true;
            }

            if (addProductDto.getLastDateToPayFee() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
            }

            if (addProductDto.getModificationDateFrom() != null) {
                if (addProductDto.getLastDateToPayFee().after(addProductDto.getModificationDateFrom())) {
                    throw new IllegalArgumentException("Last date to pay fee cannot be after or equal to modifying date from.");
                }
            } else if (addProductDto.getAdmitCardDateFrom() != null) {
                if (addProductDto.getLastDateToPayFee().after(addProductDto.getAdmitCardDateFrom())) {
                    throw new IllegalArgumentException("Last date to pay fee cannot be after or equal to admit card date from.");
                }
            } else {
                if(addProductDto.getExamDateFrom()!=null)
                {
                    if (addProductDto.getLastDateToPayFee().after(addProductDto.getExamDateFrom())) {
                        throw new IllegalArgumentException("Last date to pay fee cannot be after or equal to exam date from.");
                    }
                }
            }
            if (addProductDto.getLastDateToPayFee() != null) {
                if (addProductDto.getLastDateToPayFee().before(addProductDto.getActiveEndDate())) {
                    throw new IllegalArgumentException("Last date to pay application fee must be on or after the active end date.");
                }
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating last date to pay application fee: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception occurred while validating last date to pay application fee: " + exception.getMessage());
        }
    }

    public boolean validateLinks(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getDownloadNotificationLink() != null) {
                addProductDto.setDownloadNotificationLink(addProductDto.getDownloadNotificationLink().trim());
            }

            if (addProductDto.getDownloadSyllabusLink() != null) {
                addProductDto.setDownloadSyllabusLink(addProductDto.getDownloadSyllabusLink().trim());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception occurred while validating links: " + exception.getMessage());
        }
    }

    public boolean validateFormComplexity(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getFormComplexity() == null) {
                addProductDto.setFormComplexity(1L);
            } else if (addProductDto.getFormComplexity() <= 0 || addProductDto.getFormComplexity() > 5) {
                throw new IllegalArgumentException("Form complexity must lie in range 1-5.");
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception occurred while validating form complexity: " + exception.getMessage());
        }
    }

    public boolean validatePhysicalRequirement(PostDto postDto, CustomProduct customProduct) throws Exception {
        try {
            if (postDto.getPhysicalRequirements() == null) {
                return true;
            }
            if (!postDto.getPhysicalRequirements().isEmpty()) {
                Set<Long> genderId = new HashSet<>();

                for (int physicalAttributeIndex = 0; physicalAttributeIndex < postDto.getPhysicalRequirements().size(); physicalAttributeIndex++) {
                    if (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getGenderId() == null || postDto.getPhysicalRequirements().get(physicalAttributeIndex).getGenderId() <= 0) {
                        throw new IllegalArgumentException("GENDER ID CANNOT BE NULL OR <= 0");
                    }
                    genderId.add(postDto.getPhysicalRequirements().get(physicalAttributeIndex).getGenderId());

                    CustomGender customGender = genderService.getGenderByGenderId(postDto.getPhysicalRequirements().get(physicalAttributeIndex).getGenderId());
                    if (customGender == null) {
                        throw new IllegalArgumentException("GENDER NOT FOUND WITH ID: " + postDto.getPhysicalRequirements().get(physicalAttributeIndex).getGenderId());
                    }

                    if (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getHeight() == null || postDto.getPhysicalRequirements().get(physicalAttributeIndex).getHeight() > MAX_HEIGHT || postDto.getPhysicalRequirements().get(physicalAttributeIndex).getHeight() < MIN_HEIGHT) {
                        throw new IllegalArgumentException("HEIGHT IS MANDATORY FIELD AND MUST BE LESS THAN " + MAX_HEIGHT + " AND GREATER THAN " + MIN_HEIGHT);
                    }
                    if (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getWeight() == null || postDto.getPhysicalRequirements().get(physicalAttributeIndex).getWeight() > MAX_WEIGHT || postDto.getPhysicalRequirements().get(physicalAttributeIndex).getWeight() < MIN_WEIGHT) {
                        throw new IllegalArgumentException("WEIGHT IS MANDATORY FIELD AND MUST BE LESS THAN " + MAX_WEIGHT + " AND GREATER THAN " + MIN_WEIGHT);
                    }

                    if (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getShoeSize() != null && (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getShoeSize() > MAX_SHOE_SIZE || postDto.getPhysicalRequirements().get(physicalAttributeIndex).getShoeSize() < MIN_SHOE_SIZE)) {
                        throw new IllegalArgumentException("SHOE SIZE MUST BE LESS THAN " + MAX_SHOE_SIZE + " AND GREATER THAN " + MIN_SHOE_SIZE);
                    }
                    if (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getWaistSize() != null && (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getWaistSize() > MAX_WAIST_SIZE || postDto.getPhysicalRequirements().get(physicalAttributeIndex).getWaistSize() < MIN_WAIST_SIZE)) {
                        throw new IllegalArgumentException("WAIST SIZE MUST BE LESS THAN " + MAX_WAIST_SIZE + " AND GREATER THAN " + MIN_WAIST_SIZE);
                    }

                    if (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getChestSize() != null && (postDto.getPhysicalRequirements().get(physicalAttributeIndex).getChestSize() > MAX_CHEST_SIZE || postDto.getPhysicalRequirements().get(physicalAttributeIndex).getChestSize() < MIN_CHEST_SIZE)) {
                        throw new IllegalArgumentException("CHEST SIZE MUST BE LESS THAN " + MAX_CHEST_SIZE + " AND GREATER THAN " + MIN_CHEST_SIZE);
                    }

                }

                if (genderId.size() != postDto.getPhysicalRequirements().size()) {
                    throw new IllegalArgumentException("DUPLICATE GENDER NOT ALLOWED");
                }
            }

            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION WHILE VALIDATING PHYSICAL REQUIREMENTS: " + exception.getMessage() + "\n");
        }
    }

    public boolean validateQualificationRequirement(PostDto postDto) throws Exception {
        try {
            Set<QualificationEligibilityDto> seenSet = new HashSet<>();
            if (postDto.getQualificationEligibility() == null) {
                return true;
            }
            for (QualificationEligibilityDto dto : postDto.getQualificationEligibility()) {
                if (!seenSet.add(dto)) {
                    throw new IllegalArgumentException("Duplicate Qualification Eligibility found for the post : " +postDto.getPostName());
                }
            }
                for (QualificationEligibilityDto qualificationEligibilityDto : postDto.getQualificationEligibility()) {

                    if (qualificationEligibilityDto.getIsPercentage() == null)
                        throw new IllegalArgumentException("Please specify whether the qualification eligibility  is based on CGPA or percentage.");
                    //Validate Qualification ids
                    if (!qualificationEligibilityDto.getIsPercentage()) {
                        if(qualificationEligibilityDto.getPercentage()!=null)
                            throw new IllegalArgumentException("Percentage should not be provided when selecting CGPA. Please provide only the CGPA.");
                        if (qualificationEligibilityDto.getCgpa() != null) {
                            double cgpa = qualificationEligibilityDto.getCgpa();
                            // proceed with cgpa logic
                        } else {
                            // handle missing cgpa gracefully (e.g., throw a custom exception or set default)
                            throw new IllegalArgumentException("CGPA is required when isPercentage is false");
                        }
                    }
                    else
                    {
                        if (qualificationEligibilityDto.getCgpa() != null)
                            throw new IllegalArgumentException("CGPA should not be provided when selecting percentage. Please provide only the percentage.");
                        if(qualificationEligibilityDto.getPercentage()==null)
                            throw new IllegalArgumentException("Need to provide percentage");
                        else if(qualificationEligibilityDto.getPercentage()>100||qualificationEligibilityDto.getPercentage()<0)
                            throw new IllegalArgumentException("Invalid Percentage value. It should be between 0 and 100");
                    }
                    if (qualificationEligibilityDto.getQualificationIds() == null) {
                        throw new IllegalArgumentException("Qualification cannot be null");
                    } else if (qualificationEligibilityDto.getQualificationIds() != null) {
                        if (qualificationEligibilityDto.getQualificationIds().isEmpty()) {
                            throw new IllegalArgumentException("Qualification cannot be empty");
                        } else if (!qualificationEligibilityDto.getQualificationIds().isEmpty()) {
                            if (qualificationEligibilityDto.getQualificationIds().size() > 1) {
                                throw new IllegalArgumentException("Enter only one qualification (Highest)");
                            }
                            Set<Integer> qualificationIdSet = new HashSet<>();
                            List<Integer> qualificationIds = qualificationEligibilityDto.getQualificationIds();
                            for (Integer qualificationId : qualificationIds) {
                                Qualification qualification = entityManager.find(Qualification.class, qualificationId);
                                if (qualification == null) {
                                    throw new IllegalArgumentException("Qualification with id " + qualificationId + " does not exist");
                                }
                                qualificationIdSet.add(qualificationId);
                            }
                            if (qualificationIdSet.size() != qualificationIds.size()) {
                                throw new IllegalArgumentException("DUPLICATE QUALIFICATION NOT ALLOWED");
                            }
                        }
                    }

                    //Validate Subjects
                    if (qualificationEligibilityDto.getCustomSubjectIds() != null) {
                        if (!qualificationEligibilityDto.getCustomSubjectIds().isEmpty()) {
                            Set<Long> subjectIdsSet = new HashSet<>();
                            List<Long> subjectIds = qualificationEligibilityDto.getCustomSubjectIds();
                            for (Long subjectId : subjectIds) {
                                CustomSubject customSubject = entityManager.find(CustomSubject.class, subjectId);
                                if (customSubject == null) {
                                    throw new IllegalArgumentException("Subject with id " + subjectId + " does not exist");
                                }
                                subjectIdsSet.add(subjectId);
                            }
                            if (subjectIdsSet.size() != subjectIds.size()) {
                                throw new IllegalArgumentException("DUPLICATE SUBJECTS NOT ALLOWED");
                            }
                        }
                    }

                    //Validate Streams
                    if (qualificationEligibilityDto.getCustomStreamIds() != null) {
                        if (!qualificationEligibilityDto.getCustomStreamIds().isEmpty()) {
                            Set<Long> streamIdSet = new HashSet<>();
                            List<Long> streamIds = qualificationEligibilityDto.getCustomStreamIds();
                            for (Long streamId : streamIds) {
                                CustomStream customStream = entityManager.find(CustomStream.class, streamId);
                                if (customStream == null) {
                                    throw new IllegalArgumentException("Stream with id " + streamId + " does not exist");
                                }
                                streamIdSet.add(streamId);
                            }
                            if (streamIdSet.size() != streamIds.size()) {
                                throw new IllegalArgumentException("DUPLICATE STREAMS NOT ALLOWED");
                            }
                        }
                    }

                    if (qualificationEligibilityDto.getCustomReserveCategoryId() != null) {
                        CustomReserveCategory customReserveCategory = entityManager.find(CustomReserveCategory.class, qualificationEligibilityDto.getCustomReserveCategoryId());
                        if (customReserveCategory == null) {
                            throw new IllegalArgumentException("Reserve Category does not exists with id " + qualificationEligibilityDto.getCustomReserveCategoryId());
                        }
                    }

                    if (qualificationEligibilityDto.getPercentage() != null) {
                        if (qualificationEligibilityDto.getPercentage() > 100 || qualificationEligibilityDto.getPercentage() < 0) {
                            throw new IllegalArgumentException("Percentage cannot be less than 0 and greater than 100");
                        }
                    }
                }
            return true;
        }
        catch (IllegalArgumentException illegalArgumentException)
        {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
        }
        catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage() + "\n");
        }
    }

    public void validateDistrictStateRelationship(StateDistributionDto stateDistribution) {
        if (!Boolean.TRUE.equals(stateDistribution.getIsDistrictDistribution())) {
            return;
        }

        // Get state code using EntityManager
        StateCode stateCode = entityManager.find(StateCode.class, stateDistribution.getStateCodeId());
        if (stateCode == null) {
            throw new IllegalArgumentException("Invalid state code: " + stateDistribution.getStateCodeId());
        }

        List<DistrictDistributionDto> districtDistributions = stateDistribution.getDistrictDistributions();
        if (districtDistributions == null || districtDistributions.isEmpty()) {
            throw new IllegalArgumentException("District distributions are required when isDistrictDistribution is true");
        }

        // Get all districts for this state
        List<Districts> stateDistricts = districtService.findDistrictsByStateCode(stateCode.getState_code());
        Set<Integer> validDistrictIds = stateDistricts.stream()
                .map(Districts::getDistrict_id)
                .collect(Collectors.toSet());

        // Validate each district in the distribution
        for (DistrictDistributionDto districtDto : districtDistributions) {
            if (!validDistrictIds.contains(districtDto.getDistrictId().intValue())) {
                // Find the actual state code for this district if it exists
                Districts district = entityManager.find(Districts.class, districtDto.getDistrictId().intValue());
                if (district == null) {
                    throw new IllegalArgumentException("District not found with id: " + districtDto.getDistrictId());
                }

                throw new IllegalArgumentException(
                        String.format("District with ID %d belongs to state %s, not state %s",
                                districtDto.getDistrictId(), district.getState_code(), stateCode.getState_code()));
            }
        }
    }

    public boolean validatePostRequirement(AddProductDto addProductDto, Integer roleId,Long userId) throws Exception {
        List<PostDto> postDtos = addProductDto.getPosts();

        if(addProductDto.getIsMultiplePostSameFee()!=null)
        {
            if(!Boolean.TRUE.equals(addProductDto.getIsMultiplePostSameFee()))
            {
                if(postDtos.size()>1)
                {
                    throw new IllegalArgumentException("Only one post can be saved because multiple posts of this product does not have same fees");
                }
            }
        }

        for (PostDto postDto : postDtos) {
            validatePostBasics(postDto);
            validateVacancyDistribution(postDto);
            // Validate vacancy distribution only if distribution types are present
            List<Integer> distributionTypes = postDto.getVacancyDistributionTypeIds();
            if (distributionTypes != null && !distributionTypes.isEmpty()) {
                if (distributionTypes.contains(1)) {
                    validateStateDistribution(postDto);
                } else if (distributionTypes.contains(2)) {
                    validateZoneDistribution(postDto);
                } else if (distributionTypes.contains(3)) {
                    validateGenderDistribution(postDto, postDto.getGenderWiseDistribution());
                }
                  else if(distributionTypes.contains(4))
                {
                    validateOtherVacancyDistribution(postDto);
                }
            }
            if(postDto.getPhysicalRequirements()!=null)
            {
                validatePhysicalRequirement(postDto, null);
            }
            if(postDto.getQualificationEligibility()!=null&&!postDto.getQualificationEligibility().isEmpty()) {
                for (QualificationEligibilityDto qualificationEligibilityDto : postDto.getQualificationEligibility()) {
                    if (qualificationEligibilityDto.getQualificationIds() != null) {
                        validateQualificationRequirement(postDto);
                    }
                }
            }
        }
        return true;
    }
    private void validatePostBasics(PostDto postDto) {
        if (postDto.getPostName() == null || postDto.getPostName().trim().isEmpty()) {
            throw new IllegalArgumentException("Post name cannot be null or empty");
        }
        if (!postDto.getPostName().matches("^[a-zA-Z0-9/_\\-(),.\"' \\[\\]{}]*$")) {
            throw new IllegalArgumentException("Post name can only contain alphanumeric values, /_-(),.\"' []{}, and cannot have leading spaces.");
        }
        if (postDto.getPostTotalVacancies() == null || postDto.getPostTotalVacancies() < 0) {
            throw new IllegalArgumentException("Invalid Post Total Vacancies");
        }
    }

    private void validateVacancyDistribution(PostDto postDto) {
        List<Integer> vacancyDistributionTypeIds = postDto.getVacancyDistributionTypeIds();
        Long postTotalVacancies = postDto.getPostTotalVacancies();
        GenderDistributionDto genderDistributionDto = postDto.getGenderWiseDistribution();

        // Case: No distribution type selected (empty or null list)
        if (vacancyDistributionTypeIds == null || vacancyDistributionTypeIds.isEmpty()) {
            if ((postDto.getStateDistributions() != null && !postDto.getStateDistributions().isEmpty()) || (postDto.getZoneDistributions() != null && !postDto.getZoneDistributions().isEmpty()) || (postDto.getGenderWiseDistribution() != null && !isDtoEmpty(postDto.getGenderWiseDistribution()))) {
                throw new IllegalArgumentException("No any distribution can be given if vacancy Distribution Type Id is null or empty");
            }
        }

        if(vacancyDistributionTypeIds!=null )
        {
            if (vacancyDistributionTypeIds.size() > 1) {
                throw new IllegalArgumentException("Exactly one vacancy distribution type is required.");
            }
            if(!vacancyDistributionTypeIds.isEmpty())
            {
                int distributionTypeId = vacancyDistributionTypeIds.get(0);
                switch (distributionTypeId) {
                    case 1:
                        validateStatesDistribution(postDto.getStateDistributions(), postTotalVacancies);
                        break;
                    case 2:
                        validateZonesDistribution(postDto.getZoneDistributions(), postTotalVacancies);
                        break;
                    case 3:
                        validateGenderDistribution(postDto, genderDistributionDto);
                        break;
                    case 4:
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid vacancy distribution type: " + distributionTypeId);
                }
            }
        }
    }

    private void validateStatesDistribution(List<StateDistributionDto> stateDistributions, Long postTotalVacancies) {
        if (stateDistributions == null || stateDistributions.isEmpty()) {
            throw new IllegalArgumentException("State distributions are required");
        }

        long totalStateVacancies = 0;
        for (StateDistributionDto state : stateDistributions) {
            long stateVacancies = validateStateDistribution(state);
            totalStateVacancies += stateVacancies;
        }

        if (totalStateVacancies != postTotalVacancies) {
            throw new IllegalArgumentException(
                    String.format("Total state vacancies (%d) must equal post total vacancies (%d)",
                            totalStateVacancies, postTotalVacancies));
        }
    }

    private long validateStateDistribution(StateDistributionDto state) {
        if (state.getStateCodeId() == null) {
            throw new IllegalArgumentException("State code ID is required");
        }

        if (Boolean.TRUE.equals(state.getIsDistrictDistribution())) {
            return validateDistrictBasedState(state);
        } else {
            return validateNonDistrictBasedState(state);
        }
    }

    private long validateDistrictBasedState(StateDistributionDto state) {
        // For district-based distribution, state level gender fields are not required
        if (state.getDistrictDistributions() == null || state.getDistrictDistributions().isEmpty()) {
            throw new IllegalArgumentException("District distributions are required when isDistrictDistribution is true");
        }

        long totalDistrictVacancies = 0;
        for (DistrictDistributionDto district : state.getDistrictDistributions()) {
            long districtVacancies = validateDistrictDistribution(district);
            totalDistrictVacancies += districtVacancies;
        }

        return totalDistrictVacancies;
    }

    private long validateDistrictDistribution(DistrictDistributionDto district) {
        if (district.getDistrictId() == null) {
            throw new IllegalArgumentException("District ID is required");
        }

        if (Boolean.TRUE.equals(district.getIsGenderWise())) {
            return validateGenderWiseDistrict(district);
        } else {
            return validateNonGenderWiseDistrict(district);
        }
    }

    private long validateGenderWiseDistrict(DistrictDistributionDto district) {
        if (district.getMaleVacancy() == null || district.getFemaleVacancy() == null) {
            throw new IllegalArgumentException("Male and female vacancies are required for gender-wise district distribution");
        }

        long totalGenderVacancies = district.getMaleVacancy() + district.getFemaleVacancy();

        if (!district.getCategoryDistributions().isEmpty()) {
            long categorySum = district.getCategoryDistributions().stream()
                    .mapToLong(DistrictCategoryDistributionDto::getVacancyCount)
                    .sum();

            if (categorySum != totalGenderVacancies) {
                throw new IllegalArgumentException(
                        String.format("Category total (%d) must equal gender total (%d) for district %d",
                                categorySum, totalGenderVacancies, district.getDistrictId()));
            }
        }

        return totalGenderVacancies;
    }

    private long validateNonGenderWiseDistrict(DistrictDistributionDto district) {
        if (!district.getCategoryDistributions().isEmpty()) {
            return district.getCategoryDistributions().stream()
                    .mapToLong(DistrictCategoryDistributionDto::getVacancyCount)
                    .sum();
        } else {
            if (district.getTotalVacancy() == null) {
                throw new IllegalArgumentException(
                        "Total vacancy is required for non-gender-wise district without category distribution");
            }
            return district.getTotalVacancy();
        }
    }

    private long validateNonDistrictBasedState(StateDistributionDto state) {
        if (Boolean.TRUE.equals(state.getIsGenderWise())) {
            return validateGenderWiseState(state);
        } else {
            return validateNonGenderWiseState(state);
        }
    }

    private long validateGenderWiseState(StateDistributionDto state) {
        if (state.getMaleVacancy() == null || state.getFemaleVacancy() == null) {
            throw new IllegalArgumentException("Male and female vacancies are required for gender-wise state distribution");
        }

        long totalGenderVacancies = state.getMaleVacancy() + state.getFemaleVacancy();

        if (!state.getCategoryDistributions().isEmpty()) {
            long categorySum = state.getCategoryDistributions().stream()
                    .mapToLong(CategoryDistributionDto::getCategoryVacancies)
                    .sum();

            if (categorySum != totalGenderVacancies) {
                throw new IllegalArgumentException(
                        String.format("Category total (%d) must equal gender total (%d) for state %d",
                                categorySum, totalGenderVacancies, state.getStateCodeId()));
            }
        }

        return totalGenderVacancies;
    }

    private long validateNonGenderWiseState(StateDistributionDto state) {
        if (!state.getCategoryDistributions().isEmpty()) {
            return state.getCategoryDistributions().stream()
                    .mapToLong(CategoryDistributionDto::getCategoryVacancies)
                    .sum();
        } else {
            if (state.getTotalVacanciesInState() == null) {
                throw new IllegalArgumentException(
                        "Total vacancies is required for non-gender-wise state without category distribution");
            }
            return state.getTotalVacanciesInState();
        }
    }

    public void validateZoneDistributionRelationship(ZoneDistributionDto zoneDistribution) {
        // Skip validation if not division distribution
        if (!Boolean.TRUE.equals(zoneDistribution.getIsDivisionDistribution())) {
            return;
        }

        if (zoneDistribution.getZoneId() == null) {
            throw new IllegalArgumentException("Zone ID is required for validation.");
        }

        // Get all valid division IDs for this zone
        List<DivisionProjectionDTO> validDivisionIds;
        try {
            validDivisionIds = zoneDivisionService.getDivisionsByZoneId(zoneDistribution.getZoneId());
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("Invalid zone ID: " + zoneDistribution.getZoneId(), e);
        }

        // Validate division distributions
        List<DivisionDistributionDto> divisionDistributions = zoneDistribution.getDivisionDistributions();
        if (divisionDistributions == null || divisionDistributions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Division distributions are required when isDivisionDistribution is true");
        }

        // Validate each division ID belongs to the zone
        for (DivisionDistributionDto divisionDto : divisionDistributions) {
            if (divisionDto.getDivisionId() == null) {
                throw new IllegalArgumentException("Division ID cannot be null");
            }
            List<Integer>ids=new ArrayList<>();
            for(DivisionProjectionDTO dto :validDivisionIds)
            {
                ids.add(dto.getDivisionId());
            }

            if (!ids.contains(divisionDto.getDivisionId().intValue())) {
                throw new IllegalArgumentException(
                        String.format("Division ID %d is not associated with Zone ID %d",
                                divisionDto.getDivisionId(), zoneDistribution.getZoneId()));
            }
        }

    }

    private void validateZonesDistribution(List<ZoneDistributionDto> zoneDistributions, Long postTotalVacancies) {
        if (zoneDistributions == null || zoneDistributions.isEmpty()) {
            throw new IllegalArgumentException("Zone distributions are required");
        }

        long totalZoneVacancies = 0;
        for (ZoneDistributionDto zone : zoneDistributions) {
            long zoneVacancies = validateZoneDistribution(zone);
            totalZoneVacancies += zoneVacancies;
        }

        if (totalZoneVacancies != postTotalVacancies) {
            throw new IllegalArgumentException(
                    String.format("Total zone vacancies (%d) must equal post total vacancies (%d)",
                            totalZoneVacancies, postTotalVacancies));
        }
    }

    private long validateZoneDistribution(ZoneDistributionDto zone) {
        if (zone.getZoneId() == null) {
            throw new IllegalArgumentException("Zone ID is required");
        }

        if (Boolean.TRUE.equals(zone.getIsDivisionDistribution())) {
            return validateDivisionBasedZone(zone);
        } else {
            return validateNonDivisionBasedZone(zone);
        }
    }

    private long validateDivisionBasedZone(ZoneDistributionDto zone) {
        if (zone.getDivisionDistributions() == null || zone.getDivisionDistributions().isEmpty()) {
            throw new IllegalArgumentException("Division distributions are required when isDivisionDistribution is true");
        }

        long totalDivisionVacancies = 0;
        for (DivisionDistributionDto division : zone.getDivisionDistributions()) {
            long divisionVacancies = validateDivisionDistribution(division);
            totalDivisionVacancies += divisionVacancies;
        }

        return totalDivisionVacancies;
    }

    private long validateDivisionDistribution(DivisionDistributionDto division) {
        if (division.getDivisionId() == null) {
            throw new IllegalArgumentException("Division ID is required");
        }

        if (Boolean.TRUE.equals(division.getIsGenderWise())) {
            return validateGenderWiseDivision(division);
        } else {
            return validateNonGenderWiseDivision(division);
        }
    }

    private long validateGenderWiseDivision(DivisionDistributionDto division) {
        if (division.getMaleVacancy() == null || division.getFemaleVacancy() == null) {
            throw new IllegalArgumentException("Male and female vacancies are required for gender-wise division distribution");
        }

        long totalGenderVacancies = division.getMaleVacancy() + division.getFemaleVacancy();

        if (!division.getCategoryDistributions().isEmpty()) {
            long categorySum = division.getCategoryDistributions().stream()
                    .mapToLong(DivisionCategoryDistributionDto::getVacancyCount)
                    .sum();

            if (categorySum != totalGenderVacancies) {
                throw new IllegalArgumentException(
                        String.format("Category total (%d) must equal gender total (%d) for division %d",
                                categorySum, totalGenderVacancies, division.getDivisionId()));
            }
        }
        return totalGenderVacancies;
    }

    private long validateNonGenderWiseDivision(DivisionDistributionDto division) {
        if (!division.getCategoryDistributions().isEmpty()) {
            return division.getCategoryDistributions().stream()
                    .mapToLong(DivisionCategoryDistributionDto::getVacancyCount)
                    .sum();
        } else {
            if (division.getTotalVacancy() == null) {
                throw new IllegalArgumentException(
                        "Total vacancy is required for non-gender-wise division without category distribution");
            }
            return division.getTotalVacancy();
        }
    }

    private long validateNonDivisionBasedZone(ZoneDistributionDto zone) {
        if (Boolean.TRUE.equals(zone.getIsGenderWise())) {
            return validateGenderWiseZone(zone);
        } else {
            return validateNonGenderWiseZone(zone);
        }
    }

    private long validateGenderWiseZone(ZoneDistributionDto zone) {
        if (zone.getMaleVacancy() == null || zone.getFemaleVacancy() == null) {
            throw new IllegalArgumentException("Male and female vacancies are required for gender-wise zone distribution");
        }

        int totalGenderVacancies = zone.getMaleVacancy() + zone.getFemaleVacancy();

        if (!zone.getCategoryDistributions().isEmpty()) {
            int categorySum = zone.getCategoryDistributions().stream()
                    .mapToInt(CategoryDistributionDto::getCategoryVacancies)
                    .sum();

            if (categorySum != totalGenderVacancies) {
                throw new IllegalArgumentException(
                        String.format("Category total (%d) must equal gender total (%d) for zone %d",
                                categorySum, totalGenderVacancies, zone.getZoneId()));
            }
        }
        return totalGenderVacancies;
    }

    private long validateNonGenderWiseZone(ZoneDistributionDto zone) {
        if (!zone.getCategoryDistributions().isEmpty()) {
            return zone.getCategoryDistributions().stream()
                    .mapToLong(CategoryDistributionDto::getCategoryVacancies)
                    .sum();
        } else {
            if (zone.getTotalVacanciesInZone() == null) {
                throw new IllegalArgumentException(
                        "Total vacancies is required for non-gender-wise zone without category distribution");
            }
            return zone.getTotalVacanciesInZone();
        }
    }


    private void validateCategoryDistributions(List<CategoryDistributionDto> categoryDistributions, Long totalVacancy) {
        Long categoryVacancySum = categoryDistributions.stream()
                .filter(category -> category.getCategoryVacancies() != null)  // Ensure no null categoryVacancies
                .mapToLong(CategoryDistributionDto::getCategoryVacancies)
                .sum();

        if (!categoryVacancySum.equals(totalVacancy)) {
            throw new IllegalArgumentException("Sum of category vacancies must equal the total vacancies.");
        }

        for (CategoryDistributionDto categoryDistribution : categoryDistributions) {
            if (categoryDistribution.getCategoryId() == null || categoryDistribution.getCategoryVacancies() == null) {
                throw new IllegalArgumentException("Category ID and vacancies must be provided for each category.");
            }
        }
    }

    private void validateStateDistribution(PostDto postDto) {
        if (postDto.getStateDistributions() == null || postDto.getStateDistributions().isEmpty()) {
            throw new IllegalArgumentException("You have to distribute the vacancies State-wise");
        }
        if (postDto.getZoneDistributions() != null && !postDto.getZoneDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies Zone wise");
        }
        if (postDto.getOtherDistributions() != null && !postDto.getOtherDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot give other distributions");
        }
        if (postDto.getGenderWiseDistribution() != null && !isDtoEmpty(postDto.getGenderWiseDistribution()) ) {
            throw new IllegalArgumentException("You cannot distribute vacancies Gender wise");
        }
        for (StateDistributionDto stateDistribution : postDto.getStateDistributions()) {
            validateDistrictStateRelationship(stateDistribution);
        }
    }

    private void validateZoneDistribution(PostDto postDto) {
        if (postDto.getZoneDistributions() == null || postDto.getZoneDistributions().isEmpty()) {
            throw new IllegalArgumentException("You have to distribute the vacancies Zone-wise");
        }
        if (postDto.getStateDistributions() != null && !postDto.getStateDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies State wise");
        }
        if (postDto.getOtherDistributions() != null && !postDto.getOtherDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot give other distributions");
        }
        if (postDto.getGenderWiseDistribution() != null && !isDtoEmpty(postDto.getGenderWiseDistribution())) {
            throw new IllegalArgumentException("You cannot distribute vacancies Gender wise");
        }
        for (ZoneDistributionDto zoneDistribution : postDto.getZoneDistributions()) {
            validateZoneDistributionRelationship(zoneDistribution);
        }
    }

    private void validateGenderDistribution(PostDto postDto, GenderDistributionDto genderDto) {
        // First validate basic gender distribution
        if (postDto.getZoneDistributions() != null && !postDto.getZoneDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies Zone wise");
        }
        if (postDto.getStateDistributions() != null && !postDto.getStateDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies State wise");
        }
        if (postDto.getOtherDistributions() != null && !postDto.getOtherDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot give other distributions");
        }
        // Additional validation for category distributions when type is 3
        List<CategoryDistributionDto> categoryDtos = genderDto.getCategoryDistributionDtos();
        if (categoryDtos == null || categoryDtos.isEmpty()) {
            throw new IllegalArgumentException("Category distributions are required when distribution type is 3");
        }
        validateBasicGenderDistribution(postDto, genderDto);

        // Validate category distributions match total
        Long totalVacancy = genderDto.getTotalVacancy();
        if(totalVacancy==null)
        {
            totalVacancy= postDto.getPostTotalVacancies();
        }
        validateCategoryDistributions(categoryDtos, totalVacancy);
    }

    private void validateBasicGenderDistribution(PostDto postDto, GenderDistributionDto genderDto) {
        if (genderDto == null) {
            throw new IllegalArgumentException("Gender distribution data must be provided");
        } if (postDto.getZoneDistributions() != null && !postDto.getZoneDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies Zone wise");
        }
        if (postDto.getStateDistributions() != null && !postDto.getStateDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies State wise");
        }
        if (postDto.getOtherDistributions() != null && !postDto.getOtherDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot give other distributions");
        }
        Long postTotalVacancies = postDto.getPostTotalVacancies();
        boolean isGenderWise = Boolean.TRUE.equals(genderDto.getIsGenderWise());

        if (isGenderWise && (genderDto.getCategoryDistributionDtos()!=null&& !genderDto.getCategoryDistributionDtos().isEmpty()) ) {
            // Case 1: Gender-wise is true
            if (genderDto.getMaleVacancy() == null || genderDto.getFemaleVacancy() == null) {
                throw new IllegalArgumentException("Male and Female vacancy counts must be provided when gender-wise is enabled");
            }

            // Auto-calculate total vacancy
            Long calculatedTotalVacancy = genderDto.getMaleVacancy() + genderDto.getFemaleVacancy();
            genderDto.setTotalVacancy(calculatedTotalVacancy);

            if (!calculatedTotalVacancy.equals(postTotalVacancies)) {
                throw new IllegalArgumentException("Sum of male and female vacancies must equal post total vacancies");
            }
        } else {
            // Case 2: Gender-wise is false
            if((genderDto.getCategoryDistributionDtos()==null || genderDto.getCategoryDistributionDtos().isEmpty()) )
            {
                if (genderDto.getTotalVacancy() == null) {
                    throw new IllegalArgumentException("Total vacancy must be provided when gender-wise is disabled");
                }

                if (!genderDto.getTotalVacancy().equals(postTotalVacancies)) {
                    throw new IllegalArgumentException("Total vacancy must equal post total vacancies");
                }
            }

        }
    }

    public void validateOtherVacancyDistribution(PostDto postDto) {
        if (postDto.getZoneDistributions() != null && !postDto.getZoneDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies Zone wise");
        }
        if (postDto.getStateDistributions() != null && !postDto.getStateDistributions().isEmpty()) {
            throw new IllegalArgumentException("You cannot distribute vacancies State wise");
        }
        if(postDto.getGenderWiseDistribution()!=null&& !isDtoEmpty(postDto.getGenderWiseDistribution()))
        {
            throw new IllegalArgumentException("You cannot distribute vacancies category wise");
        }
        List<OtherDistribution> otherDistributions = postDto.getOtherDistributions();

        // Check if the list is empty
        if (otherDistributions == null || otherDistributions.isEmpty()) {
            throw new IllegalArgumentException("OtherDistribution list cannot be empty for VacancyTypeId 4.");
        }

        long totalVacanciesSum = 0L;

        // Validate each OtherDistribution in the list
        for (OtherDistribution distribution : otherDistributions) {
            if (distribution.getOtherDistributionValue() == null || distribution.getOtherDistributionValue().trim().isEmpty()) {
                throw new IllegalArgumentException("OtherDistributionValue cannot be null or empty.");
            }

            if (distribution.getTotalVacancy() == null) {
                throw new IllegalArgumentException("TotalVacancy cannot be null.");
            }

            // Add the totalVacancy to the sum
            totalVacanciesSum += distribution.getTotalVacancy();
        }

        // Check if the sum matches postTotalVacancies
        if (totalVacanciesSum != postDto.getPostTotalVacancies()) {
            throw new IllegalArgumentException("The sum of total vacancies in OtherDistributions must equal PostTotalVacancies.");
        }
    }

    private boolean isDtoEmpty(Object dto) {
        return Arrays.stream(dto.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .allMatch(field -> {
                    try {
                        return field.get(dto) == null;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error accessing field value", e);
                    }
                });
    }

    public CustomSector validateSector(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getSector() != null) {
                CustomSector customSector = sectorService.getSectorBySectorId(addProductDto.getSector());
                if (customSector == null) {
                    throw new IllegalArgumentException("No sector found with this id.");
                }
                return customSector;
            }
            return null;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating sector: " + exception.getMessage() + "\n");
        }
    }

    public Boolean validateSelectionCriteria(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getSelectionCriteria() != null) {
                addProductDto.setSelectionCriteria(addProductDto.getSelectionCriteria().trim());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating selection criteria: " + exception.getMessage() + "\n");
        }
    }

    public Advertisement validateAdvertisement(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getAdvertisement() != null) {
                Advertisement advertisement = advertisementService.getAdvertisementById(addProductDto.getAdvertisement());
                if (advertisement == null) {
                    throw new IllegalArgumentException("Advertisement not found with this id.");
                }
                return advertisement;
            }
            return null;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating advertisement: " + exception.getMessage() + "\n");
        }
    }

    public List<CustomProduct> getAllProductsByAdvertisementId (Advertisement advertisement) throws Exception {
        try {
            String sql = "SELECT c FROM CustomProduct c WHERE c.advertisement = :advertisementId";
            return entityManager.createQuery(sql, CustomProduct.class).setParameter("advertisementId", advertisement).getResultList();// Use this to simplify appending conditions
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception occured while fetching product w.r.t advertisement: " + exception.getMessage() + "\n");
        }
    }

    private boolean isSameOrFutureDate(Date dateToValidate) {
        // Strip time from both dates
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(dateToValidate);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        // Compare only the date parts
        return !cal1.before(cal2);
    }
    private Date stripTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}