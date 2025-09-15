package com.community.api.endpoint.avisoft.controller.Category;

import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddCategoryDto;
import com.community.api.dto.CategoryDto;
import com.community.api.dto.CategoryExtDTO;
import com.community.api.dto.CategoryProductDTO;
import com.community.api.dto.CustomCategoryWrapper;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.ProductCompressedDTO;
import com.community.api.entity.CustomProduct;
import com.community.api.services.CategoryService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.community.api.component.Constant.SOME_EXCEPTION_OCCURRED;

@RestController
@RequestMapping(value = "/category-custom")
public class CategoryController extends CatalogEndpoint {

    private static final String CATALOGSERVICENOTINITIALIZED = "CATALOG SERVICE IS NOT INITIATED.";
    private static final String SOMEEXCEPTIONOCCURRED = "SOME EXCEPTION OCCURRED";
    private static final String CATEGORYCANNOTBELESSTHANOREQAULZERO = "CATEGORY ID CANNOT BE LESS OR EQUAL TO ZERO";

    private final ExceptionHandlingService exceptionHandlingService;
    private final CategoryService categoryService;
    @Autowired
    JwtUtil jwtTokenUtil;
    @Autowired
    RoleService roleService;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public CategoryController(ExceptionHandlingService exceptionHandlingService, CategoryService categoryService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.categoryService = categoryService;
    }

    @Authorize(value = {Constant.roleSuperAdmin, Constant.roleAdmin, Constant.roleServiceProvider})
    @PostMapping("/add")
    public ResponseEntity<?> addCategory(HttpServletRequest request, @RequestBody AddCategoryDto addCategoryDto) {
        try {

            CategoryImpl categoryImpl = new CategoryImpl();

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(CATALOGSERVICENOTINITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (addCategoryDto.getName() == null || addCategoryDto.getName().trim().isEmpty()) {
                return ResponseService.generateErrorResponse("CATEGORY TITLE CANNOT BE EMPTY OR NULL", HttpStatus.BAD_REQUEST);
            }
            addCategoryDto.setName(addCategoryDto.getName().trim());
            categoryImpl.setName(addCategoryDto.getName());

            if (addCategoryDto.getDisplayTemplate() == null) {
                addCategoryDto.setDisplayTemplate(addCategoryDto.getName());
            } else {
                if (addCategoryDto.getDisplayTemplate().trim().isEmpty()) {
                    return ResponseService.generateErrorResponse("DISPLAY TEMPLATE CANNOT BE EMPTY", HttpStatus.BAD_REQUEST);
                }
                addCategoryDto.setDisplayTemplate(addCategoryDto.getDisplayTemplate().trim());
            }
            categoryImpl.setDisplayTemplate(addCategoryDto.getDisplayTemplate());

            if (addCategoryDto.getDescription() != null && !addCategoryDto.getDescription().trim().isEmpty()) {
                addCategoryDto.setDescription(addCategoryDto.getDescription().trim());
                categoryImpl.setDescription(addCategoryDto.getDescription());
            } else {
                return ResponseService.generateErrorResponse("CATEGORY DESCRIPTION CANNOT BE EMPTY OR NULL", HttpStatus.BAD_REQUEST);
            }

            if (addCategoryDto.getLongDescription() != null && !addCategoryDto.getLongDescription().trim().isEmpty()) {
                addCategoryDto.setLongDescription(addCategoryDto.getLongDescription().trim());
                categoryImpl.setLongDescription(addCategoryDto.getLongDescription());
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(new Date());
            Date activeStartDate = dateFormat.parse(formattedDate);

            addCategoryDto.setActiveStartDate(activeStartDate);
            categoryImpl.setActiveStartDate(addCategoryDto.getActiveStartDate());
            if (addCategoryDto.getActiveEndDate() != null && !addCategoryDto.getActiveEndDate().after(addCategoryDto.getActiveStartDate())) {
                return ResponseService.generateErrorResponse("ACTIVE END DATE CANNOT BE BEFORE OR EQUAL TO ACTIVE START DATE(CURRENT DATE)", HttpStatus.BAD_REQUEST);
            }
            categoryImpl.setActiveEndDate(addCategoryDto.getActiveEndDate());

            Category category = catalogService.saveCategory(categoryImpl);

            CustomCategoryWrapper wrapper = new CustomCategoryWrapper();
            wrapper.wrapDetailsCategory(category, null, request);
            return ResponseService.generateSuccessResponse("CATEGORY ADDED SUCCESSFULLY", wrapper, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(SOMEEXCEPTIONOCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/get-all-categories")
    public ResponseEntity<?> getCategories(
            HttpServletRequest request,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "30") int limit) {
        try {
            if (catalogService == null) {
                return ResponseService.generateErrorResponse("CATALOG SERVICE IS NULL", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Category> categories = this.catalogService.findAllCategories();
            List<CustomCategoryWrapper> activeCategories = new ArrayList<>();

            for (Category category : categories) {
                if (category.getDefaultParentCategory() == null) {
                    if ((((Status) category).getArchived() != 'Y' && category.getActiveEndDate() == null) ||
                            (((Status) category).getArchived() != 'Y' && category.getActiveEndDate().after(new Date()))) {

                        List<BigInteger> productIdList = categoryService.getAllProductsByCategoryId(category.getId());
                        List<CustomProductWrapper> products = new ArrayList<>();

                        for (BigInteger productId : productIdList) {
                            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId.longValue());

                            if (customProduct != null && (((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate() != null &&
                                    customProduct.getDefaultSku().getActiveEndDate().after(new Date())) &&
                                    customProduct.getProductState().getProductState().equals(Constant.PRODUCT_STATE_NEW)) {

                                CustomProductWrapper wrapper = new CustomProductWrapper();
                                wrapper.wrapDetails(customProduct);
                                products.add(wrapper);
                            }
                        }
                        CustomCategoryWrapper wrapper = new CustomCategoryWrapper();
                        wrapper.wrapDetailsCategory(category, products, request);
                        activeCategories.add(wrapper);
                    }
                }
            }

            // Pagination logic
            int totalItems = activeCategories.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);

            // Validate offset index
            if (fromIndex >= totalItems && offset != 0) {
                return ResponseService.generateErrorResponse("PAGE INDEX OUT OF RANGE", HttpStatus.BAD_REQUEST);
            }

            List<CustomCategoryWrapper> paginatedCategories = (totalItems > 0) ? activeCategories.subList(fromIndex, toIndex) : new ArrayList<>();

            // Construct paginated response
            Map<String, Object> response = new HashMap<>();
            response.put("categories", paginatedCategories);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            // If no categories found, return a message but still maintain structure
            String message = totalItems > 0 ? "CATEGORIES FOUND SUCCESSFULLY" : "CATEGORIES LIST IS EMPTY";

            return ResponseService.generateSuccessResponse(message, response, HttpStatus.OK);

        } catch (IllegalArgumentException exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(SOMEEXCEPTIONOCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping(value = "/get-sub-categories")
    public ResponseEntity<?> getSubCategories(
            HttpServletRequest request,
            @RequestParam(value = "category", required = false) List<Long> parentCategories,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "30") int limit) {

        try {
            if (catalogService == null) {
                return ResponseService.generateErrorResponse("CATALOG SERVICE IS NULL", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Category> categories = this.catalogService.findAllCategories();
            List<CustomCategoryWrapper> activeCategories = new ArrayList<>();

            // Filtering active sub-categories
            for (Category category : categories) {
                if (category.getDefaultParentCategory() != null) {
                    boolean isParentMatching = parentCategories == null || parentCategories.isEmpty() ||
                            parentCategories.contains(category.getDefaultParentCategory().getId());

                    boolean isActive = (((Status) category).getArchived() != 'Y' && category.getActiveEndDate() == null) ||
                            (((Status) category).getArchived() != 'Y' && category.getActiveEndDate().after(new Date()));

                    if (isParentMatching && isActive) {
                        CustomCategoryWrapper wrapper = new CustomCategoryWrapper();
                        wrapper.wrapDetailsCategory(category, null, request);
                        activeCategories.add(wrapper);
                    }
                }
            }

            // Pagination logic
            int totalItems = activeCategories.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);

            // Validate offset request
            if (fromIndex >= totalItems && offset != 0) {
                return ResponseService.generateErrorResponse("PAGE INDEX OUT OF RANGE", HttpStatus.BAD_REQUEST);
            }

            List<CustomCategoryWrapper> paginatedList = (totalItems > 0) ? activeCategories.subList(fromIndex, toIndex) : new ArrayList<>();

            // Construct paginated response
            Map<String, Object> response = new HashMap<>();
            response.put("categories", paginatedList);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            // If no categories found, return a message but still maintain structure
            String message = totalItems > 0 ? "CATEGORIES FOUND SUCCESSFULLY" : "CATEGORIES LIST IS EMPTY";

            return ResponseService.generateSuccessResponse(message, response, HttpStatus.OK);

        } catch (IllegalArgumentException exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse("SOME EXCEPTION OCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping(value = "/get-products-by-category-id/{id}")
    public ResponseEntity<?> getProductsByCategoryId(
            HttpServletRequest request,
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "30") int limit,
            @RequestParam(value = "ext", required = false, defaultValue = "false") Boolean ext,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Integer roleId = 5;
            String role = Constant.roleUser;
            if (authHeader != null) {
                String jwtToken = authHeader.substring(7);
                Long userId = jwtTokenUtil.extractId(jwtToken);

                roleId = jwtTokenUtil.extractRoleId(jwtToken);
                role = roleService.findRoleName(roleId);
            }

            if (catalogService == null) {
                return ResponseService.generateErrorResponse("CATALOG SERVICE IS NULL", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Long categoryId = Long.parseLong(id);
            if (categoryId <= 0) {
                return ResponseService.generateErrorResponse(CATEGORYCANNOTBELESSTHANOREQAULZERO, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Category category = this.catalogService.findCategoryById(categoryId);
            if (category == null) {
                return ResponseService.generateErrorResponse("CATEGORY NOT FOUND", HttpStatus.NOT_FOUND);
            } else if (((Status) category).getArchived() == 'Y') {
                return ResponseService.generateErrorResponse("CATEGORY IS ARCHIVED", HttpStatus.NOT_FOUND);
            }
            if (ext) {
                List<Object[]> rows = categoryService.getAllProductsByCategoryIdCompressed(categoryId, offset, limit);
                System.out.println("res" + rows);
                BigInteger count = categoryService.getAllProductsByCategoryIdCount(categoryId);
                List<ProductCompressedDTO> products = new ArrayList<>();
                for (Object[] row : rows) {
                    System.out.println("inside loop");
                    ProductCompressedDTO dto = new ProductCompressedDTO();
                    dto.setProductId(((BigInteger) row[0]).longValue());
                    dto.setMetaTitle((String) row[1]);
                    products.add(dto);
                    /* activeCategories.add(dto);*/
                }
                CategoryProductDTO categoryProductDTO = new CategoryProductDTO();
                categoryProductDTO.setCategoryId(categoryId);
                categoryProductDTO.setCategoryName(category.getName());
                categoryProductDTO.setProducts(products);
                int totalItems = count.intValue();
                int totalPages = (int) Math.ceil((double) totalItems / limit);
                int fromIndex = offset * limit;
                Map<String, Object> response = new HashMap<>();
                response.put("category", categoryProductDTO);
                response.put("totalItems", totalItems);
                response.put("totalPages", totalPages);
                response.put("currentPage", offset);
                return ResponseService.generateSuccessResponse("CATEGORY DATA FOUND", response, HttpStatus.OK);
            }
            List<BigInteger> productIdList = categoryService.getAllProductsByCategoryId(categoryId);
            List<CustomProductWrapper> products = new ArrayList<>();

            for (BigInteger productId : productIdList) {
                CustomProduct customProduct = entityManager.find(CustomProduct.class, productId.longValue());

                if (role.equalsIgnoreCase(Constant.roleUser)) {
                    if (customProduct != null &&
                            ((Status) customProduct).getArchived() != 'Y' &&
                            customProduct.getDefaultSku().getActiveEndDate().after(new Date()) &&
                            !customProduct.getGoLiveDate().after(new Date()) &&
                            !customProduct.getProductState().getProductState().equalsIgnoreCase(Constant.PRODUCT_STATE_DRAFT) &&
                            customProduct.getIsApproved()) {

                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct);
                        products.add(wrapper);
                    }
                } else {
                    if (customProduct != null && (((Status) customProduct).getArchived() != 'Y' &&
                            customProduct.getDefaultSku().getActiveEndDate().after(new Date())) &&
                            customProduct.getProductState().getProductState().equals(Constant.PRODUCT_STATE_NEW)) {

                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct);
                        products.add(wrapper);
                    }
                }
            }

            // Pagination logic
            int totalItems = products.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);

            if (fromIndex >= totalItems && offset != 0) {
                return ResponseService.generateErrorResponse("Page index out of range", HttpStatus.BAD_REQUEST);
            }

            List<CustomProductWrapper> paginatedProducts = products.subList(fromIndex, toIndex);

            CustomCategoryWrapper categoryWrapper = new CustomCategoryWrapper();
            categoryWrapper.wrapDetailsCategory(category, paginatedProducts, request);

            // Construct paginated response
            Map<String, Object> response = new HashMap<>();
            response.put("category", categoryWrapper);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            return ResponseService.generateSuccessResponse("CATEGORY DATA FOUND", response, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(SOMEEXCEPTIONOCCURRED + ": " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/remove/{categoryId}")
    public ResponseEntity<?> removeCategoryById(HttpServletRequest request, @PathVariable("categoryId") String id, @RequestParam(value = "productLimit", defaultValue = "20") int productLimit, @RequestParam(value = "productOffset", defaultValue = "1") int productOffset, @RequestParam(value = "subcategoryLimit", defaultValue = "20") int subcategoryLimit, @RequestParam(value = "subcategoryOffset", defaultValue = "1") int subcategoryOffset) {
        try {
            if (catalogService == null) {
                return ResponseService.generateErrorResponse("CATALOG SERVICE IS NULL", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Long categoryId = Long.parseLong(id);
            if (categoryId <= 0) {
                return ResponseService.generateErrorResponse(CATEGORYCANNOTBELESSTHANOREQAULZERO, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Category category = this.catalogService.findCategoryById(categoryId);

            if (category != null && ((Status) category).getArchived() != 'Y') {
                List<BigInteger> productIdList = categoryService.getAllProductsByCategoryId(categoryId);

                for (BigInteger productId : productIdList) {
                    CustomProduct customProduct = entityManager.find(CustomProduct.class, productId.longValue());

                    if (customProduct != null && (((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {
                        catalogService.removeProduct(customProduct.getDefaultSku().getDefaultProduct());
                    }
                }
                catalogService.removeCategory(category);
                return ResponseService.generateSuccessResponse("CATEGORY DELETED SUCCESSFULLY", "DELETED", HttpStatus.OK);

            } else {
                return ResponseService.generateErrorResponse("CATEGORY NOT FOUND", HttpStatus.NOT_FOUND);
            }

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(SOMEEXCEPTIONOCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping(value = "/update/{categoryId}")
    public ResponseEntity<?> updateCategoryById(HttpServletRequest request, @RequestBody AddCategoryDto addCategoryDto, @PathVariable("categoryId") String id) {
        try {

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(CATALOGSERVICENOTINITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Long categoryId = Long.parseLong(id);

            if (categoryId <= 0) {
                return ResponseService.generateErrorResponse(CATEGORYCANNOTBELESSTHANOREQAULZERO, HttpStatus.BAD_REQUEST);
            }

            Category category = this.catalogService.findCategoryById(categoryId);

            if (category != null && ((Status) category).getArchived() != 'Y') {

                if (addCategoryDto.getName() != null && !addCategoryDto.getName().trim().isEmpty()) { // trim works on nonNull values only.
                    category.setName(addCategoryDto.getName().trim());
                }
                if (addCategoryDto.getDescription() != null && !addCategoryDto.getDescription().trim().isEmpty()) {
                    category.setDescription(addCategoryDto.getDescription().trim());
                }
                if (addCategoryDto.getActiveEndDate() != null && !addCategoryDto.getActiveEndDate().after(addCategoryDto.getActiveStartDate()) && !addCategoryDto.getActiveEndDate().after(new Date())) {
                    return ResponseService.generateErrorResponse("ACTIVE END DATE CANNOT BE BEFORE OR EQUAL TO ACTIVE START DATE(CURRENT DATE)", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                if (addCategoryDto.getDisplayTemplate() != null && !addCategoryDto.getDisplayTemplate().trim().isEmpty()) {
                    category.setDisplayTemplate(addCategoryDto.getDisplayTemplate().trim());
                }

                category = catalogService.saveCategory(category); // Save the updated category

                List<BigInteger> productIdList = categoryService.getAllProductsByCategoryId(categoryId);
                List<CustomProductWrapper> products = new ArrayList<>();

                for (BigInteger productId : productIdList) {
                    CustomProduct customProduct = entityManager.find(CustomProduct.class, productId.longValue());

                    if (customProduct != null && (((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {
                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct);
                        products.add(wrapper);
                    }
                }

                CustomCategoryWrapper wrapper = new CustomCategoryWrapper(); // Wrap and return the updated category details
                wrapper.wrapDetailsCategory(category, products, request);
                return ResponseService.generateSuccessResponse("CATEGORY UPDATED SUCCESSFULLY", wrapper, HttpStatus.OK);

            } else {
                return ResponseService.generateErrorResponse("CATEGORY NOT FOUND.", HttpStatus.NOT_FOUND);
            }
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(SOMEEXCEPTIONOCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/get-all-categories-info")
    public ResponseEntity<?> getCategoriesInfo(
            HttpServletRequest request,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "30") int limit,
            @RequestParam(value = "ext", defaultValue = "false", required = false) Boolean ext) {
        try {
            if (ext) {
                List<CategoryExtDTO> activeCategories = new ArrayList<>();
                Query query = entityManager.createNativeQuery(
                        "SELECT category_id, name FROM blc_category " +
                                "WHERE archived = 'N' " +
                                "AND default_parent_category_id IS NULL " +
                                "AND (active_end_date IS NULL OR active_end_date > CURRENT_DATE) " +
                                "ORDER BY category_id ASC " +
                                "LIMIT :limit OFFSET :offset");
                Query queryToCount = entityManager.createNativeQuery(
                        "SELECT COUNT(category_id) FROM blc_category " +
                                "WHERE archived = 'N' " +
                                "AND default_parent_category_id IS NULL " +
                                "AND (active_end_date IS NULL OR active_end_date > CURRENT_DATE) ");

                query.setParameter("limit", limit);
                query.setParameter("offset", offset);
                List<Object[]> rows = query.getResultList();
                for (Object[] row : rows) {
                    CategoryExtDTO dto = new CategoryExtDTO();
                    dto.setId(((BigInteger) row[0]).longValue());
                    dto.setName((String) row[1]);
                    activeCategories.add(dto);
                }
                int totalItems = ((BigInteger) queryToCount.getSingleResult()).intValue();
                int totalPages = (int) Math.ceil((double) totalItems / limit);
                int fromIndex = offset * limit;


                if (fromIndex >= totalItems && offset != 0) {
                    return ResponseService.generateErrorResponse("PAGE INDEX OUT OF RANGE", HttpStatus.BAD_REQUEST);
                }


                Map<String, Object> response = new HashMap<>();
                response.put("categories", activeCategories);
                response.put("totalItems", totalItems);
                response.put("totalPages", totalPages);
                response.put("currentPage", offset);

                String message = totalItems > 0 ? "CATEGORIES INFO FOUND SUCCESSFULLY" : "CATEGORIES INFO IS EMPTY";

                return ResponseService.generateSuccessResponse(message, response, HttpStatus.OK);
            }
            //NEW LOGIC ⤴️
            //OLD LOGIC ⤵️
            if (catalogService == null) {
                return ResponseService.generateErrorResponse("CATALOG SERVICE IS NULL", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Category> categories = catalogService.findAllCategories();
            List<CategoryDto> activeCategories = new ArrayList<>();

            for (Category category : categories) {
                if (category.getDefaultParentCategory() == null) {
                    if ((((Status) category).getArchived() != 'Y' && category.getActiveEndDate() == null) ||
                            (((Status) category).getArchived() != 'Y' && category.getActiveEndDate().after(new Date()))) {

                        CategoryDto dto = new CategoryDto(category);
                        activeCategories.add(dto);
                    }
                }
            }

            // Pagination logic
            int totalItems = activeCategories.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);

            if (fromIndex >= totalItems && offset != 0) {
                return ResponseService.generateErrorResponse("PAGE INDEX OUT OF RANGE", HttpStatus.BAD_REQUEST);
            }

            List<CategoryDto> paginatedCategories = (totalItems > 0)
                    ? activeCategories.subList(fromIndex, toIndex)
                    : new ArrayList<>();

            Map<String, Object> response = new HashMap<>();
            response.put("categories", paginatedCategories);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            String message = totalItems > 0 ? "CATEGORIES INFO FOUND SUCCESSFULLY" : "CATEGORIES INFO IS EMPTY";

            return ResponseService.generateSuccessResponse(message, response, HttpStatus.OK);

        } catch (IllegalArgumentException exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(SOMEEXCEPTIONOCCURRED + ": " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


}
