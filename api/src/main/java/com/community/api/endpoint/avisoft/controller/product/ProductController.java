package com.community.api.endpoint.avisoft.controller.product;

import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.*;
import com.community.api.endpoint.avisoft.controller.ServiceProviderActionController;
import com.community.api.entity.Advertisement;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomGender;
import com.community.api.entity.CustomJobGroup;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductReserveCategoryBornBeforeAfterRef;
import com.community.api.entity.ProductEvents;
import com.community.api.entity.StateCode;
import com.community.api.entity.Role;

import com.community.api.entity.CustomProductState;
import com.community.api.entity.CustomSubject;
import com.community.api.entity.CustomStream;
import com.community.api.entity.CustomSector;
import com.community.api.entity.Post;

import com.community.api.services.PostService;
import com.community.api.services.ProductService;
import com.community.api.services.RoleService;
import com.community.api.services.ResponseService;
import com.community.api.services.AdvertisementService;
import com.community.api.services.DistrictService;
import com.community.api.services.GenderService;
import com.community.api.services.ProductGenderPhysicalRequirementService;
import com.community.api.services.ReserveCategoryAgeService;
import com.community.api.services.ReserveCategoryDtoService;
import com.community.api.services.JobGroupService;
import com.community.api.services.ProductStateService;
import com.community.api.services.ReserveCategoryService;
import com.community.api.services.ProductReserveCategoryFeePostRefService;
import com.community.api.services.ProductReserveCategoryBornBeforeAfterRefService;
import com.community.api.services.PostExecutionService;
import com.community.api.services.ApplicationScopeService;
import com.community.api.services.PhysicalRequirementDtoService;
import com.community.api.services.SharedUtilityService;
import lombok.Lombok;
import org.broadleafcommerce.common.persistence.Status;

import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.Sku;

import org.broadleafcommerce.core.catalog.service.type.ProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.community.api.services.exception.ExceptionHandlingService;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.community.api.component.Constant.*;
import static com.mchange.v2.ser.SerializableUtils.deepCopy;

    /*

            WHAT THIS CLASS DOES FOR EACH FUNCTION WE HAVE.

     */

@RestController
@RequestMapping(value = "/product-custom", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class ProductController extends CatalogEndpoint {

    public static final String TENTATIVEDATEAFTERACTIVEENDDATE = "Both tentative examination data must be after active end date.";
    public static final String TENTATIVEEXAMDATETOAFTEREXAMDATEFROM = "Tentative exam date from must be before or equal of tentative exam date to.";
    public static final String TENTATIVEEXAMDATEAFTERACTIVEENDDATE = "Tentative examination date must be after active end date.";
    public static final String POSTLESSTHANORZERO = "Number of post cannot be less than or equal to zero.";
    public static final String PRODUCTNOTFOUND = "Product not found.";
    public static final String PRODUCTFOUNDSUCCESSFULLY = "Products found successfully";

    private final ExceptionHandlingService exceptionHandlingService;
    private final EntityManager entityManager;
    private final JwtUtil jwtTokenUtil;
    private final ProductService productService;
    private final RoleService roleService;
    private final JobGroupService jobGroupService;
    private final ProductStateService productStateService;
    private final ApplicationScopeService applicationScopeService;
    private final ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;
    private final ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;
    private final ReserveCategoryService reserveCategoryService;
    private final ReserveCategoryDtoService reserveCategoryDtoService;
    private final PhysicalRequirementDtoService physicalRequirementDtoService;

    @Autowired
    AdvertisementService advertisementService;

    @Autowired
    DistrictService districtService;

    @Autowired
    SharedUtilityService sharedUtilityService;

    @Value("${origin.url}")
    private String origin;

    @Autowired
    private ServiceProviderActionController serviceProviderActionController;

    @Autowired
    private ReserveCategoryAgeService reserveCategoryAgeService;

    @Autowired
    ProductGenderPhysicalRequirementService productGenderPhysicalRequirementService;

    @Autowired
    GenderService genderService;

    @Autowired
    private PostExecutionService postExecutionService;

    @Autowired
    PostService postService;
    public ProductController(ExceptionHandlingService exceptionHandlingService, EntityManager entityManager, JwtUtil jwtTokenUtil, ProductService productService, RoleService roleService, JobGroupService jobGroupService, ProductStateService productStateService, ApplicationScopeService applicationScopeService, ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService, ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService, ReserveCategoryService reserveCategoryService, ReserveCategoryDtoService reserveCategoryDtoService, PhysicalRequirementDtoService physicalRequirementDtoService,GenderService genderService) {

        this.exceptionHandlingService = exceptionHandlingService;
        this.entityManager = entityManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.productService = productService;
        this.roleService = roleService;
        this.jobGroupService = jobGroupService;
        this.productStateService = productStateService;
        this.applicationScopeService = applicationScopeService;
        this.productReserveCategoryBornBeforeAfterRefService = productReserveCategoryBornBeforeAfterRefService;
        this.productReserveCategoryFeePostRefService = productReserveCategoryFeePostRefService;
        this.reserveCategoryService = reserveCategoryService;
        this.reserveCategoryDtoService = reserveCategoryDtoService;
        this.physicalRequirementDtoService = physicalRequirementDtoService;
        this.genderService=genderService;
    }

    @Transactional
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(
            HttpServletRequest request,
            @RequestBody AddProductDto addProductDto,
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(value = "saveDraft", required = false, defaultValue = "false") boolean saveDraft) {

        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long userId = jwtTokenUtil.extractId(jwtToken);
            if (!productService.addProductAccessAuthorisation(authHeader)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO ADD PRODUCT", HttpStatus.FORBIDDEN);
            }

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Advertisement advertisement = productService.validateAdvertisement(addProductDto);

            Long categoryId = advertisement.getCategory().getDefaultParentCategory().getId();
            Category category = productService.validateCategory(categoryId);

            if (!saveDraft) {
                productService.addProductDtoValidation(addProductDto);
            } else {
                productService.addProductDtoWithoutValidation(addProductDto);
            }
            if (addProductDto.getSector() != null) {
                if (addProductDto.getSector() != 1000 && addProductDto.getSectorRunningField() != null) {
                    return ResponseService.generateErrorResponse("Cannot add running field for sector except OTHERS", HttpStatus.BAD_REQUEST);
                } else if (addProductDto.getSector() == 1000 && (addProductDto.getSectorRunningField() == null || addProductDto.getSectorRunningField().trim().isEmpty())) {
                    return ResponseService.generateErrorResponse("Running field requried when selecting sector : OTHERS", HttpStatus.BAD_REQUEST);
                }
            }
            Product product = catalogService.createProduct(ProductType.PRODUCT);
            product.setMetaTitle(addProductDto.getMetaTitle());
            product.setDisplayTemplate(addProductDto.getDisplayTemplate());
            product.setMetaDescription(addProductDto.getMetaDescription());
            product.setDefaultCategory(category);
            product.setCategory(category);

            product = catalogService.saveProduct(product);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(new Date());
            Date currentDate = dateFormat.parse(formattedDate);

            Sku sku = catalogService.createSku();
            sku.setActiveStartDate(addProductDto.getActiveStartDate());
            sku.setName(addProductDto.getMetaTitle());
            sku.setQuantityAvailable(addProductDto.getQuantity());
            sku.setDescription(addProductDto.getMetaDescription());
            sku.setActiveEndDate(addProductDto.getActiveEndDate());
            sku.setDefaultProduct(product);

            CustomProductState customProductState = null;
            if (!saveDraft) {
                customProductState = productStateService.getProductStateByName(PRODUCT_STATE_NEW);
                if (customProductState == null) {
                    return ResponseService.generateErrorResponse("Custom product state not found.", HttpStatus.NOT_FOUND);
                }
            } else if (saveDraft) {
                customProductState = productStateService.getProductStateByName(PRODUCT_STATE_DRAFT);
                {
                    if (customProductState == null) {
                        return ResponseService.generateErrorResponse("Custom product state not found.", HttpStatus.NOT_FOUND);
                    }
                }
            }

            product.setDefaultSku(sku);

            if (!saveDraft) {
                List<AddReserveCategoryDto> reservedCategories = addProductDto.getReservedCategory();
                if (reservedCategories != null && !reservedCategories.isEmpty()) {
                    for (AddReserveCategoryDto dto : reservedCategories) {
                        Boolean isOther = dto.getIsOtherOrStateCategory();
                        String otherText = dto.getOtherOrStateCategory();

                        if (Boolean.TRUE.equals(isOther)) {
                            // If true, field must be filled
                            if (otherText == null || otherText.trim().isEmpty()) {
                                return ResponseService.generateErrorResponse(
                                        "Other_or_state_category must be provided when is_other_or_state_category is true.",
                                        HttpStatus.BAD_REQUEST
                                );
                            }
                            if (!otherText.matches("^[a-zA-Z0-9 ]*$")) {
                                throw new IllegalArgumentException("Only alphanumeric characters are allowed in otherOrStateCategory");
                            }

                        } else {
                            // If false/null, field must NOT be filled
                            if (otherText != null && !otherText.trim().isEmpty()) {
                                return ResponseService.generateErrorResponse(
                                        "other_or_state_category should not be provided when is_other_or_state_category is false.",
                                        HttpStatus.BAD_REQUEST
                                );
                            }
                        }
                    }
                }
                productService.validateReserveCategory(addProductDto);
            } else if (saveDraft) {
                if (addProductDto.getReservedCategory() != null) {
                    productService.validateReserveCategory(addProductDto);
                }
            }
            CustomSector customSector = productService.validateSector(addProductDto);

            productService.validateSelectionCriteria(addProductDto);

            productService.validateAdmitCardDates(addProductDto);
            productService.validateModificationDates(addProductDto);
            productService.validateLastDateToPayFee(addProductDto);

            if (!saveDraft) {
                productService.validateLinks(addProductDto);
            } else if (saveDraft) {
                if (addProductDto.getDownloadNotificationLink() != null) {
                    addProductDto.setDownloadNotificationLink(addProductDto.getDownloadNotificationLink().trim());
                }
                if (addProductDto.getDownloadSyllabusLink() != null) {
                    addProductDto.setDownloadSyllabusLink(addProductDto.getDownloadSyllabusLink().trim());
                }
            }

            productService.validateFormComplexity(addProductDto);

            Role role = productService.getRoleByToken(authHeader);
            Long creatorUserId = productService.getUserIdByToken(authHeader);
            List<Post> postList = new ArrayList<>();
            if (!saveDraft) {
                if (addProductDto.getPosts() != null && !addProductDto.getPosts().isEmpty()) {
                    productService.validatePostRequirement(addProductDto, roleId, userId);
                    postList = postService.savePosts(addProductDto.getPosts(), product);
                }
            } else if (saveDraft && addProductDto.getPosts() != null) {
                productService.validatePostRequirement(addProductDto, roleId, userId);
                postList = postService.savePosts(addProductDto.getPosts(), product);
            }
            productService.saveCustomProduct(product, addProductDto, customProductState, role, creatorUserId, product.getActiveStartDate(), currentDate);

            if (!saveDraft) {
                productReserveCategoryFeePostRefService.saveFeeAndPost(addProductDto.getReservedCategory(), product);
            } else if (saveDraft) {
                if (addProductDto.getReservedCategory() != null) {
                    productReserveCategoryFeePostRefService.saveFeeAndPost(addProductDto.getReservedCategory(), product);
                }
            }
            CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());

            StateCode stateCode = null;
            if (addProductDto.getState() != null) {
                stateCode = districtService.getStateByStateId(addProductDto.getState());
            }
            CustomProductWrapper wrapper = new CustomProductWrapper();
            Long totalVacanciesInProduct = 0L;
            if (saveDraft) {
                if (postList != null && !postList.isEmpty()) {
                    for (Post post : postList) {
                        totalVacanciesInProduct += post.getPostTotalVacancies();
                    }
                    postExecutionService.savePostsToCustomProduct(addProductDto.getPosts(), product, postList);
                }
                if (reserveCategoryService != null) {
                    wrapper.wrapDetailsAddProduct(product, addProductDto, customProductState, applicationScope, creatorUserId, role, reserveCategoryService, stateCode, customSector, currentDate, advertisement, genderService, entityManager, postList, addProductDto.getPosts(), totalVacanciesInProduct, (long) addProductDto.getPosts().size());
                } else {
                    wrapper.wrapDetailsAddProduct(product, addProductDto, customProductState, applicationScope, creatorUserId, role, null, stateCode, customSector, currentDate, advertisement, genderService, entityManager, postList, addProductDto.getPosts(), totalVacanciesInProduct, (long) addProductDto.getPosts().size());
                }

                ResponseEntity<?> response = ResponseService.generateSuccessResponse("PRODUCT ADDED AS DRAFT SUCCESSFULLY", wrapper, HttpStatus.OK);
                return response;
            }
            if (postList != null && !postList.isEmpty()) {
                for (Post post : postList) {
                    if (post.getPostTotalVacancies() != null)
                        totalVacanciesInProduct += post.getPostTotalVacancies();
                }
                postExecutionService.savePostsToCustomProduct(addProductDto.getPosts(), product, postList);
            }
            wrapper.wrapDetailsAddProduct(product, addProductDto, customProductState, applicationScope, creatorUserId, role, reserveCategoryService, stateCode, customSector, currentDate, advertisement, genderService, entityManager, postList, addProductDto.getPosts(), totalVacanciesInProduct, (long) addProductDto.getPosts().size());
            ResponseEntity<?> response = ResponseService.generateSuccessResponse("PRODUCT ADDED SUCCESSFULLY", wrapper, HttpStatus.OK);
            return response;

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PutMapping("/update/{productId}")
    public ResponseEntity<?> updateProduct(HttpServletRequest request, @RequestBody AddProductDto addProductDto, @PathVariable Long productId, @RequestHeader(value = "Authorization") String authHeader, @RequestParam(value = "saveAsDraft", required = false, defaultValue = "false") boolean saveAsDraft) {

        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long userId = jwtTokenUtil.extractId(jwtToken);

            if (!productService.updateProductAccessAuthorisation(authHeader, productId)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO UPDATE PRODUCT", HttpStatus.FORBIDDEN);
            }

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);
            CustomProduct originalProduct = (CustomProduct) deepCopy(customProduct); // Deep clone before mutation
            Product product = catalogService.findProductById(customProduct.getId());

            if (customProduct == null) {
                return ResponseService.generateErrorResponse(Constant.PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }

//            // Validations and checks.
            productService.updateProductValidation(addProductDto, customProduct);

            // Validation of getActiveEndDate and getGoLiveDate.
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date currentDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date
            customProduct.setModifiedDate(currentDate);
            customProduct.setAdmitCardDateFrom(originalProduct.getAdmitCardDateFrom());
            System.out.println(originalProduct.getAdmitCardDateFrom());
            customProduct.setAdmitCardDateTo(originalProduct.getAdmitCardDateTo());
            customProduct.setModificationDateFrom(originalProduct.getModificationDateFrom());
            customProduct.setModificationDateTo(originalProduct.getModificationDateTo());
            customProduct.setExamDateFrom(originalProduct.getExamDateFrom());
            customProduct.setExamDateTo(originalProduct.getExamDateTo());
            if(addProductDto.getProductState()==null){
                // Validate dates fields.
                productService.validateAndSetActiveStartDate(addProductDto, customProduct, currentDate);
                productService.validateAndSetActiveEndDate(addProductDto, customProduct, currentDate);
                productService.validateAndSetGoLiveDate(addProductDto, customProduct, currentDate);
                productService.validateAndSetLastDateToPayFeeDate(addProductDto, customProduct, currentDate);

                productService.validateAndSetModifiedDates(addProductDto, customProduct, currentDate);
                productService.validateAndSetAdmitCardDates(addProductDto, customProduct, currentDate);
                productService.validateAndSetExamDates(addProductDto, customProduct, currentDate);
            }
//            productService.validateAndSetExamDateFromAndExamDateToFields(addProductDto, customProduct);
//            productService.validateExamDateFromAndExamDateTo(addProductDto, customProduct);

            productService.validateProductState(addProductDto, customProduct, authHeader);

            customProduct.setModifierRole(roleService.getRoleByRoleId(jwtTokenUtil.extractRoleId(authHeader.substring(7))));
            customProduct.setModifierUserId(jwtTokenUtil.extractId(authHeader.substring(7)));

            if (addProductDto.getReservedCategory() != null) {
                productService.validateReserveCategory(addProductDto);
            }
            if (addProductDto.getIsReviewRequired() != null) {
                customProduct.setIsReviewRequired(addProductDto.getIsReviewRequired());
            }
            if (addProductDto.getOtherInfo() != null) {
                customProduct.setOtherInfo(addProductDto.getOtherInfo());
            }

            if (addProductDto.getIsMultiplePostSameFee() != null) {
                if (addProductDto.getIsMultiplePostSameFee().equals(true)) {
                   /* if(addProductDto.getPosts()!=null) {
                        if (addProductDto.getPosts().isEmpty()) {
                            throw new IllegalArgumentException("You have to fill at least one post");
                        }
                    }*/
                    customProduct.setIsMultiplePostSameFee(addProductDto.getIsMultiplePostSameFee());
                } else if (addProductDto.getIsMultiplePostSameFee().equals(false)) {
                    if (addProductDto.getPosts() != null) {
                        /*if(addProductDto.getPosts().isEmpty())
                        {
                            throw new IllegalArgumentException("You have to fill at least one post");
                        }*/
                        if (!addProductDto.getPosts().isEmpty()) {
                            if (addProductDto.getPosts().size() > 1) {
                                throw new IllegalArgumentException("Only one post is allowed to save if multiple posts does not have same fees");
                            }
                        }

                    } else if (addProductDto.getPosts() == null || addProductDto.getPosts().isEmpty()) {
                        if (customProduct.getPosts().size() > 1) {
                            throw new IllegalArgumentException("Only one post is allowed to save if multiple posts does not have same fees.Either set it to true or you have to delete the multiple posts and save only one");
                        }
                    }
                }
                customProduct.setIsMultiplePostSameFee(addProductDto.getIsMultiplePostSameFee());
            }

            List<Post> postList = new ArrayList<>();
            if (addProductDto.getPosts() != null) {
                if (!addProductDto.getPosts().isEmpty()) {
                    productService.validatePostRequirement(addProductDto, roleId, userId);
                    postList = postService.savePosts(addProductDto.getPosts(), product);
                    List<Post> postsToDelete = new ArrayList<>(customProduct.getPosts());

                    for (Post post : postsToDelete) {
                        // First handle the @ManyToOne relationship in CustomProductReserveCategoryBornBeforeAfterRef
                        CustomProductReserveCategoryBornBeforeAfterRef ref =
                                productReserveCategoryBornBeforeAfterRefService.findByPost(post);

                        if (ref != null) {
                            // Clear the @ManyToOne relationship
                            ref.setPost(null);
                            entityManager.merge(ref);
                        }

                        // Clear the @ManyToMany relationship from Post
                        if (post.getAgeRequirement() != null) {
                            post.getAgeRequirement().clear();
                            entityManager.merge(post);
                        }
                        if (post.getReligion() != null) {
                            post.getReligion().clear();
                            entityManager.merge(post);
                        }

                        // Remove the post from custom product
                        post.setProduct(null);
                        customProduct.getPosts().remove(post);
                        entityManager.remove(entityManager.contains(post) ? post : entityManager.merge(post));
                    }
                    entityManager.flush();

                    // Add new posts

                    // Set the relationships for new posts
                    for (Post newPost : postList) {
                        newPost.setProduct(customProduct);
                        if (customProduct.getPosts() == null) {
                            customProduct.setPosts(new ArrayList<>());
                        }
                        customProduct.getPosts().add(newPost);
                        entityManager.merge(newPost);
                    }
                    entityManager.flush();
                }
            }

            if (addProductDto.getReservedCategory() != null) {
                productService.deleteOldReserveCategoryMapping(customProduct);
                productReserveCategoryFeePostRefService.saveFeeAndPost(addProductDto.getReservedCategory(), product);
            }
            if (addProductDto.getPosts() != null) {
                if (!addProductDto.getPosts().isEmpty()) {
                    postExecutionService.savePostsWithoutAgeRequirement(customProduct, postList);
                    postService.updatePostAgeRequirements(addProductDto.getPosts(), customProduct, postList);
                }
            }
           /* if(addProductDto.getReserveCategoryAge()!=null)
            {
                productReserveCategoryBornBeforeAfterRefService.saveBornBeforeAndBornAfter(addProductDto.getReserveCategoryAge(),product,pos);
            }*/
            if (addProductDto.getIsResubmitProduct() != null) {
                if (addProductDto.getIsResubmitProduct().equals(true)) {
                    CustomProductState customProductState = entityManager.find(CustomProductState.class, 9L);
                    if (customProductState == null) {
                        throw new IllegalArgumentException("Custom Product with this state does not exit");
                    }
                    customProduct.setProductState(customProductState);
                }
            }

            CustomProductWrapper wrapper = new CustomProductWrapper();

            if (saveAsDraft && customProduct.getProductState().getProductState().equalsIgnoreCase("DRAFT")) {
                entityManager.merge(customProduct);
                wrapper.wrapDetails(customProduct, null, null, productReserveCategoryFeePostRefService);
                return ResponseService.generateSuccessResponse("Product is updated and saved as Draft successfully", wrapper, HttpStatus.OK);
            } else if (saveAsDraft && !customProduct.getProductState().getProductState().equalsIgnoreCase("DRAFT")) {
                entityManager.merge(customProduct);
                wrapper.wrapDetails(customProduct, null, null, productReserveCategoryFeePostRefService);
                return ResponseService.generateSuccessResponse("Product is updated successfully", wrapper, HttpStatus.OK);
            } else if (!saveAsDraft) {
                if (customProduct.getProductState().getProductState().equalsIgnoreCase(PRODUCT_STATE_DRAFT)) {
                    entityManager.merge(customProduct);
                    return productService.changeStateProductFromDraftToNew(customProduct, wrapper);
                }
            }
            System.out.println("admit card date is "+customProduct.getAdmitCardDateFrom());
List<String>diff= sharedUtilityService.getDifferences(customProduct,originalProduct);
            System.out.println(diff);
            entityManager.merge(customProduct);
            List<PostProjectionDTO> postProjectionDTOS = getPosts(postList);
            wrapper.wrapDetails(customProduct, null, postProjectionDTOS, productReserveCategoryFeePostRefService);
            if(!customProduct.getPurchasedBy().isEmpty()) {
                Query query = entityManager.createQuery("Select MAX(eventId) from ProductEvents where productId = :productId");
                query.setParameter("productId", productId);
                Boolean communicate = true;
                Long id = (Long) query.getSingleResult();
                ProductEvents productEvents = null;


                if (id == null) {
                    productEvents = new ProductEvents();
                    productEvents.setLastUpdate(LocalDateTime.now());
                    productEvents.setSummaryOfUpdate(null);
                    productEvents.setProductId(productId);

                } else {
                    productEvents = entityManager.find(ProductEvents.class, id);
                    if (Duration.between(productEvents.getLastUpdate(), LocalDateTime.now()).toMinutes() >= 10) {
                        productEvents = new ProductEvents();
                        productEvents.setLastUpdate(LocalDateTime.now());
                        productEvents.setSummaryOfUpdate(null);
                        productEvents.setProductId(productId);
                    } else
                        communicate = false;
                }
                if (communicate) {
                    CommunicationRequest communicationRequest = new CommunicationRequest();
                    CustomProduct customProductSession = getProductWithPurchasers(customProduct.getId());
                    communicationRequest.setUserIds(customProductSession.getPurchasedBy());
                    communicationRequest.setSubject("Product Update Notification");
                    List<Integer> modes = new ArrayList<>();
                    modes.add(1);
                    communicationRequest.setModes(modes);
                    communicationRequest.setContentText(
                            "Hello,\n\n" +
                                    "We would like to inform you that an update has been made to a form associated with a product you recently purchased.\n\n" +
                                    "Changes: " + diff.toString() + "\n\n" +
                                    "To view the latest changes, please visit:\n" +
                                    "https://dev-next-am-public-ui.vercel.app/product-details/" + product.getId() + "\n\n" +
                                    "Thank you,\n" +
                                    "System Administrator"
                    );
                    entityManager.persist(productEvents);
                    System.out.println("Calling email trigger");
                    ResponseEntity<?> response = serviceProviderActionController.communicateWithCustomersDummy(communicationRequest, 5, authHeader, true);
                }
            }
            if((customProduct.getProductState().getProductStateId()==1L||customProduct.getProductState().getProductStateId()==3L)&&addProductDto.getProductState()!=3) {
                CustomProductState productState=entityManager.find(CustomProductState.class,2L);
                customProduct.setProductState(productState);
            }
            entityManager.merge(customProduct);
            return ResponseService.generateSuccessResponse("Product Updated Successfully", wrapper, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @GetMapping("/get-product-by-id/{productId}")
    public ResponseEntity<?> retrieveProductById(@PathVariable("productId") String productIdPath, @RequestHeader(value = "Authorization",required = false) String authHeader) {

        try {
            Integer roleId=5;
            Long userId=null;
            if(authHeader!=null) {
                String jwtToken = authHeader.substring(7);
                roleId= jwtTokenUtil.extractRoleId(jwtToken);
                userId = jwtTokenUtil.extractId(jwtToken);
            }
            String recOrigin = request.getHeader("Origin");

            Long productId = Long.parseLong(productIdPath);
            if (productId <= 0) {
                return ResponseService.generateErrorResponse("PRODUCT ID CANNOT BE <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);

            if (customProduct == null) {
                return ResponseService.generateErrorResponse(PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }
            boolean allowExpiredAccess=false;
            if(authHeader!=null)
            {
                allowExpiredAccess =
                        roleId == 1 || roleId == 2 ||
                                (customProduct.getCreatoRole() != null &&
                                        roleId.equals(customProduct.getCreatoRole().getRole_id()) &&
                                        userId != null && userId.equals(customProduct.getUserId()));
            }
            //set views
            if (recOrigin != null) {
                if (origin.trim().equals(recOrigin.trim())) {
                    customProduct.setViews(customProduct.getViews() + 1);
                    entityManager.merge(customProduct);
                }
            }

            boolean isArchived = ((Status) customProduct).getArchived() == 'Y';
            boolean isExpired = customProduct.getDefaultSku().getActiveEndDate().before(new Date());

            if ((!isArchived && !isExpired) || allowExpiredAccess) {
                CustomProductWrapper wrapper = new CustomProductWrapper();
                List<Post> postList = customProduct.getPosts();
                List<PostProjectionDTO> postProjectionDTOS = getPosts(postList);
                wrapper.wrapDetails(customProduct, postList, postProjectionDTOS, productReserveCategoryFeePostRefService);
                return ResponseService.generateSuccessResponse("PRODUCT FOUND", wrapper, HttpStatus.OK);
            } else {
                return ResponseService.generateErrorResponse("PRODUCT IS EITHER ARCHIVED OR EXPIRED", HttpStatus.NOT_FOUND);
            }

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(Constant.NUMBER_FORMAT_EXCEPTION + ": " + illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/get-all-products")
    public ResponseEntity<?> retrieveProducts(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        try {
            if (offset < 0) {
                throw new IllegalArgumentException("Offset for pagination cannot be a negative number");
            }
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit for pagination cannot be a negative number or 0");
            }
            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Product> products = catalogService.findAllProducts();

            if (products.isEmpty()) {
                return ResponseService.generateErrorResponse(PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }

            List<CustomProductWrapper> responses = new ArrayList<>();
            for (Product product : products) {
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());

                if (customProduct != null &&
                        (((Status) customProduct).getArchived() != 'Y' &&
                                customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {

                    CustomProductWrapper wrapper = new CustomProductWrapper();
                    wrapper.wrapDetails(customProduct);
                    responses.add(wrapper);
                }
            }

            // Calculate pagination details
            int totalItems = responses.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int currentPage = offset;
            int start = Math.min(offset * limit, totalItems);
            int end = Math.min(start + limit, totalItems);

            if (start >= totalItems) {
                return ResponseService.generateErrorResponse("No more products available", HttpStatus.NOT_FOUND);
            }

            List<CustomProductWrapper> paginatedResponses = responses.subList(start, end);

            // Create response with pagination info
            Map<String, Object> response = new HashMap<>();
            response.put("products", paginatedResponses);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", currentPage);

            return ResponseService.generateSuccessResponse(PRODUCTFOUNDSUCCESSFULLY, response, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @DeleteMapping("/delete/{productId}")
    @Transactional
    public ResponseEntity<?> deleteProduct(@PathVariable("productId") String productIdPath,
                                           @RequestHeader(value = "Authorization") String authHeader) {
        try {

            Long productId = Long.parseLong(productIdPath);

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId); // Find the Custom Product

            if (customProduct == null || (((Status) customProduct).getArchived() == 'Y')) {
                return ResponseService.generateErrorResponse(PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }
            if (!productService.deleteProductAccessAuthorisation(authHeader)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO DELETE PRODUCT", HttpStatus.FORBIDDEN);
            }

            Role role = productService.getRoleByToken(authHeader);
            Long modifierUserId = productService.getUserIdByToken(authHeader);

            customProduct.setModifierUserId(modifierUserId);
            customProduct.setModifierRole(role);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(new Date());
            Date modifiedDate = dateFormat.parse(formattedDate);
            customProduct.setModifiedDate(modifiedDate);

            entityManager.merge(customProduct);

            catalogService.removeProduct(customProduct.getDefaultSku().getDefaultProduct()); // Make it archive from the DB.

            return ResponseService.generateSuccessResponse("PRODUCT DELETED SUCCESSFULLY", "DELETED", HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-new-state-products")
    public ResponseEntity<?> getAllNewStateProducts(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        try {
            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Product> products = catalogService.findAllProducts(); // Retrieve all products.

            if (products.isEmpty()) {
                return ResponseService.generateErrorResponse(PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }

            List<CustomProductWrapper> responses = new ArrayList<>();
            for (Product product : products) {
                // Find the CustomProduct that matches productId
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());

                if (customProduct != null) {
                    boolean isActive = ((Status) customProduct).getArchived() != 'Y' &&
                            customProduct.getDefaultSku().getActiveEndDate().after(new Date());
                    boolean isNewState = customProduct.getProductState().getProductState().equals(PRODUCT_STATE_NEW);

                    if (isActive && isNewState) {
                        List<Post> postList = customProduct.getPosts();
                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct, postList, null, productReserveCategoryFeePostRefService);
                        responses.add(wrapper);
                    }
                }
            }

            // Pagination details
            int totalItems = responses.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int currentPage = offset;

            int fromIndex = Math.min(offset * limit, totalItems);
            int toIndex = Math.min(fromIndex + limit, totalItems);

            if (fromIndex >= totalItems) {
                return ResponseService.generateErrorResponse("No more products available", HttpStatus.NOT_FOUND);
            }

            List<CustomProductWrapper> paginatedResponses = responses.subList(fromIndex, toIndex);

            // Response with pagination metadata
            Map<String, Object> response = new HashMap<>();
            response.put("products", paginatedResponses);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", currentPage);

            return ResponseService.generateSuccessResponse(PRODUCTFOUNDSUCCESSFULLY, response, HttpStatus.OK);

        } catch (IllegalArgumentException exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>("SOME EXCEPTION OCCURRED: " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-all-live-state-products")
    public ResponseEntity<?> getAllLiveStateProducts(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        try {
            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Product> products = catalogService.findAllProducts(); // Retrieve all products.

            if (products.isEmpty()) {
                return ResponseService.generateErrorResponse(PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }

            List<CustomProductWrapper> responses = new ArrayList<>();
            for (Product product : products) {
                // Find the CustomProduct that matches productId
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());

                if (customProduct != null) {
                    boolean isActive = ((Status) customProduct).getArchived() != 'Y' &&
                            customProduct.getDefaultSku().getActiveEndDate().after(new Date());
                    boolean isLive = !customProduct.getGoLiveDate().after(new Date()) && customProduct.getProductState().getProductState().equals(PRODUCT_STATE_LIVE);

                    if (isActive && isLive) {
                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct);
                        responses.add(wrapper);
                    }
                }
            }

            // Pagination details
            int totalItems = responses.size();
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int currentPage = offset;

            int fromIndex = Math.min(offset * limit, totalItems);
            int toIndex = Math.min(fromIndex + limit, totalItems);

            if (fromIndex >= totalItems) {
                return ResponseService.generateErrorResponse("No more products available", HttpStatus.NOT_FOUND);
            }

            List<CustomProductWrapper> paginatedResponses = responses.subList(fromIndex, toIndex);

            // Response with pagination metadata
            Map<String, Object> response = new HashMap<>();
            response.put("products", paginatedResponses);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", currentPage);

            return ResponseService.generateSuccessResponse(PRODUCTFOUNDSUCCESSFULLY, response, HttpStatus.OK);

        } catch (IllegalArgumentException exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>("SOME EXCEPTION OCCURRED: " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-filter-products")
    public ResponseEntity<?> getFilterProducts(
            @RequestParam(value = "date_from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFrom,
            @RequestParam(value = "date_to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateTo,
            @RequestParam(value = "state", required = false) List<Long> state,
            @RequestParam(value = "rejection_status", required = false) List<Long> rejection_status,
            @RequestParam(value = "category", required = false) List<Long> categories,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "fee", required = false) Double fee,
            @RequestParam(value = "post", required = false) Integer post,
            @RequestParam(value = "reserve_categories", required = false) List<Long> reserveCategories,
            @RequestParam(value = "product_ids", required = false) List<Long> productIds,
            @RequestParam(value = "isExpired", required = false) boolean isExpired,
            @RequestParam(value = "all", required = false, defaultValue = "false") boolean all,
            @RequestHeader(name = "Authorization") String authHeader,
            @RequestParam(name = "myProducts", defaultValue = "false", required = false) Boolean myProducts,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);
            Long createdById = null;

            Role roleEntity = roleService.getRoleByRoleId(roleId);
            if (roleServiceProviderAdmin.equals(roleEntity.getRole_name()) || roleServiceProvider.equals(roleEntity.getRole_name())) {
                myProducts = true;
            }
            if ((Constant.roleAdmin.equals(roleEntity.getRole_name()) || roleSuperAdmin.equals(roleEntity.getRole_name())) && !myProducts) {
                createdById = null;
            } else if ((Constant.roleAdmin.equals(roleEntity.getRole_name()) || roleSuperAdmin.equals(roleEntity.getRole_name())) && myProducts) {
                createdById = tokenUserId;
            } else {
                createdById = tokenUserId;
            }
            if (offset < 0) {
                throw new IllegalArgumentException("Offset for pagination cannot be a negative number");
            }
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit for pagination cannot be a negative number or 0");
            }

          /*  if (isExpired && (roleId != 1 && roleId != 2)) {
                return ResponseService.generateErrorResponse(
                        "You are not authorized to view expired products.",
                        HttpStatus.FORBIDDEN
                );
            }*/
            // Date formatting
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (dateFrom != null) {
                dateFrom = dateFormat.parse(dateFormat.format(dateFrom));
            }
            if (dateTo != null) {
                dateTo = dateFormat.parse(dateFormat.format(dateTo));
            }
            List<CustomProduct> products = null;
            Map<String, Object> opresponse = new HashMap<>();
           /* if(all&&!(roleAdmin.equals(role.getRole_name())||roleSuperAdmin.equals(role.getRole_name())))
            {
                return ResponseService.generateErrorResponse("You are not authorized to view all products.",HttpStatus.FORBIDDEN);
            }*/
            if (!all) {
                // Fetch filtered products
                opresponse = productService.filterProducts(
                        state, rejection_status, categories, reserveCategories,
                        title, fee, post, dateFrom, dateTo, isExpired, offset, limit, all, createdById, productIds
                );
            } else {
                opresponse = productService.filterProducts(
                        state, rejection_status, categories, reserveCategories,
                        title, fee, post, dateFrom, dateTo, null, offset, limit, all, createdById, productIds
                );
            }
            products = (List<CustomProduct>) opresponse.get("products");
            if (products.isEmpty()) {
                return ResponseService.generateSuccessResponse("NO PRODUCTS FOUND WITH THE GIVEN CRITERIA", new ArrayList<>(), HttpStatus.OK);
            }

            // Filtering out archived products
            int skipped = 0;
            List<CustomProductWrapper> responses = new ArrayList<>();
            for (CustomProduct customProduct : products) {
                /* if (customProduct != null && ((Status) customProduct).getArchived() != 'Y') {*/
                CustomProductWrapper wrapper = new CustomProductWrapper();
                List<Post> postList = customProduct.getPosts();
                List<PostProjectionDTO> postProjectionDTOS = getPosts(customProduct.getPosts());
                wrapper.wrapDetails(customProduct, postList, postProjectionDTOS, productReserveCategoryFeePostRefService);
                responses.add(wrapper);
            } /*else
                {
                    skipped++;
                }*/


           /* CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

            Root<CustomProduct> root = countQuery.from(CustomProduct.class);

// SELECT COUNT(p) FROM CustomProduct p
            countQuery.select(criteriaBuilder.count(root));

            TypedQuery<Long> query = entityManager.createQuery(countQuery);*/

            // Pagination logic
            int totalItems = (Integer) opresponse.get("count");
            int totalPages = (int) Math.ceil((double) totalItems / limit);
            int fromIndex = offset * limit;
            int toIndex = Math.min(fromIndex + limit, totalItems);

            if (offset >= totalPages && offset != 0) {
                throw new IllegalArgumentException("No more products availabe");
            }
            // Validate offset request
            if (fromIndex >= totalItems && offset != 0) {
                return ResponseService.generateErrorResponse("Page index out of range", HttpStatus.BAD_REQUEST);
            }


            // Construct paginated response
            Map<String, Object> response = new HashMap<>();
            response.put("products", responses);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", offset);

            return ResponseService.generateSuccessResponse("PRODUCTS RETRIEVED SUCCESSFULLY", response, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllProductsByServiceProvider(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "false") boolean showDraftProducts,
            @RequestParam(value = "state", required = false) List<Long> state,
            @RequestParam(value = "rejection_status", required = false) List<Long> rejection_status,
            @RequestParam(value = "category", required = false) List<Long> categories,
            @RequestParam(value = "product_ids", required = false) List<Long> productIds,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "fee", required = false) Double fee,
            @RequestParam(value = "post", required = false) Integer post,
            @RequestParam(value = "reserve_categories", required = false) List<Long> reserveCategories,
            @RequestParam(value = "date_from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFrom,
            @RequestParam(value = "date_to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateTo) {

        try {
            if (authHeader == null || !authHeader.startsWith(Constant.BEARER_CONST)) {
                return ResponseService.generateErrorResponse("Authorization header is missing or invalid.", HttpStatus.UNAUTHORIZED);
            }
            if (offset < 0) {
                throw new IllegalArgumentException("Offset for pagination cannot be a negative number");
            }
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit for pagination cannot be a negative number or 0");
            }
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long userId = jwtTokenUtil.extractId(jwtToken);

            if (dateFrom != null || dateTo != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                if (dateFrom != null) {
                    dateFrom = dateFormat.parse(dateFormat.format(dateFrom));
                }

                if (dateTo != null) {
                    dateTo = dateFormat.parse(dateFormat.format(dateTo));
                }
            }
            return productService.filterProductsByRoleAndUserId(roleId, userId, offset, limit, showDraftProducts, state, rejection_status, categories, reserveCategories, title, fee, post, dateFrom, dateTo, productIds);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse("EXCEPTION OCCURRED: " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public static List<PostProjectionDTO> getPosts(List<Post> postList) {
        List<PostProjectionDTO> postProjectionDTOS = new ArrayList<>();
        for (Post post : postList) {
            PostProjectionDTO postProjectionDTO = new PostProjectionDTO();
            postProjectionDTO.setPostId(post.getPostId());
            postProjectionDTO.setPostName(post.getPostName());
            postProjectionDTO.setPostCode(post.getPostCode());
            postProjectionDTO.setPostTotalVacancies(post.getPostTotalVacancies());
            postProjectionDTO.setVacancyDistributionTypeIds(post.getVacancyDistributionTypes());
            postProjectionDTO.setStateDistributions(post.getStateDistributions());
            postProjectionDTO.setZoneDistributions(post.getZoneDistributions());
            postProjectionDTO.setGenderWiseDistribution(post.getGenderWiseDistribution());
            postProjectionDTO.setOtherDistributions(post.getOtherDistributions());
            postProjectionDTO.setAdditionalComments(post.getAdditionalComments());
            postProjectionDTO.setStateDistributionAdditionalComments(post.getStateDistributionAdditionalComments());
            postProjectionDTO.setZoneDistributionAdditionalComments(post.getZoneDistributionAdditionalComments());
            postProjectionDTO.setGenderDistributionAdditionalComments(post.getGenderDistributionAdditionalComments());
            postProjectionDTO.setQualificationAdditionalComments(post.getQualificationAdditionalComments());
            postProjectionDTO.setPhysicalAdditionalComments(post.getPhysicalAdditionalComments());
            postProjectionDTO.setOtherDistributionAdditionalComments(post.getOtherDistributionAdditionalComments());
            postProjectionDTO.setReserveCatAgeAdditionalComments(post.getReserveCatAgeAdditionalComments());
            postProjectionDTO.setTotalSeatsVisible(post.getTotalSeatsVisible());
            postProjectionDTO.setAdditionalEligibility(post.getAdditionalEligibility());
            postProjectionDTO.setReligionAdditionalComments(post.getReligionAdditionalComments());
            postProjectionDTO.setIncomeAdditionalComments(post.getIncomeAdditionalComments());
            postProjectionDTO.setReligion(post.getReligion());
            postProjectionDTO.setIncome(post.getIncome());
            List<ReserveCategoryAgeDto> reserveCategoryAgeDtosToSet = new ArrayList<>();
            for (CustomProductReserveCategoryBornBeforeAfterRef ageRequirementEntity : post.getAgeRequirement()) {
                ReserveCategoryAgeDto reserveCategoryAgeDto = new ReserveCategoryAgeDto();
                reserveCategoryAgeDto.setReserveCategoryId(ageRequirementEntity.getCustomReserveCategory().getReserveCategoryId());
                reserveCategoryAgeDto.setReserveCategory(ageRequirementEntity.getCustomReserveCategory().getReserveCategoryName());
                reserveCategoryAgeDto.setPost(Math.toIntExact(post.getPostId()));
                reserveCategoryAgeDto.setBornAfter(ageRequirementEntity.getBornAfter());
                reserveCategoryAgeDto.setBornBefore(ageRequirementEntity.getBornBefore());
                reserveCategoryAgeDto.setBornBeforeAfter(ageRequirementEntity.getBornBeforeAfter());
                reserveCategoryAgeDto.setGenderId(ageRequirementEntity.getGender().getGenderId());
                reserveCategoryAgeDto.setGenderName(ageRequirementEntity.getGender().getGenderName());
                reserveCategoryAgeDto.setMinAge(ageRequirementEntity.getMinimumAge());
                reserveCategoryAgeDto.setMaxAge(ageRequirementEntity.getMaximumAge());
                reserveCategoryAgeDto.setCategoryRunningField(ageRequirementEntity.getCategoryRunningField());
                reserveCategoryAgeDto.setGenderRunningField(ageRequirementEntity.getGenderRunningField());
                reserveCategoryAgeDto.setAsOfDate(ageRequirementEntity.getAsOfDate());
                reserveCategoryAgeDtosToSet.add(reserveCategoryAgeDto);
            }
            postProjectionDTO.setReserveCategoryAge(reserveCategoryAgeDtosToSet);
            postProjectionDTO.setQualificationEligibility(post.getQualificationEligibility());
            postProjectionDTO.setPhysicalRequirements(post.getPhysicalRequirements());
            postProjectionDTOS.add(postProjectionDTO);
        }
        return postProjectionDTOS;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;


    // Run every one hour
    @Scheduled(cron = "0 0 * * * *")
    @PutMapping("/update-product-resources")
    public void updateProductStates() {
        try {
            jdbcTemplate.execute("CALL update_all_product_states();");
            System.out.println("Product states updated successfully at midnight.");
        } catch (Exception e) {
            System.err.println("Error updating product states: " + e.getMessage());
        }
    }
    @Transactional
    public CustomProduct getProductWithPurchasers(Long id) {
        CustomProduct product = entityManager.find(CustomProduct.class,id);
        product.getPurchasedBy().size(); // triggers initialization
        return product;
    }
}