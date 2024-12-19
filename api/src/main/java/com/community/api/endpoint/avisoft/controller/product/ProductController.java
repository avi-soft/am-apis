package com.community.api.endpoint.avisoft.controller.product;

import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddProductDto;
import com.community.api.dto.AddReserveCategoryDto;
import com.community.api.dto.PhysicalRequirementDto;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.dto.CustomProductWrapper;

import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomGender;
import com.community.api.entity.CustomJobGroup;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductState;

import com.community.api.entity.CustomSector;
import com.community.api.entity.CustomStream;
import com.community.api.entity.CustomSubject;
import com.community.api.entity.OtherItem;
import com.community.api.entity.Qualification;
import com.community.api.entity.Role;
import com.community.api.entity.StateCode;
import com.community.api.services.DistrictService;
import com.community.api.services.GenderService;
import com.community.api.services.OtherItemService;
import com.community.api.services.PhysicalRequirementDtoService;
import com.community.api.services.PostExecutionService;
import com.community.api.services.ProductGenderPhysicalRequirementService;
import com.community.api.services.ResponseService;
import org.broadleafcommerce.common.persistence.Status;

import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.Sku;

import org.broadleafcommerce.core.catalog.service.type.ProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.community.api.services.JobGroupService;
import com.community.api.services.ProductService;
import com.community.api.services.RoleService;
import com.community.api.services.ReserveCategoryService;
import com.community.api.services.ProductReserveCategoryBornBeforeAfterRefService;
import com.community.api.services.ProductReserveCategoryFeePostRefService;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.services.ProductStateService;
import com.community.api.services.ApplicationScopeService;
import com.community.api.services.ReserveCategoryDtoService;

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
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.community.api.component.Constant.*;

    /*

            WHAT THIS CLASS DOES FOR EACH FUNCTION WE HAVE.

     */

@RestController
@RequestMapping(value = "/product-custom", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class ProductController extends CatalogEndpoint {

    public static final String TENTATIVEDATEAFTERACTIVEENDDATE = "Both tentative examination data must be after active end date.";
    public static final String TENTATIVEEXAMDATETOAFTEREXAMDATEFROM = "Tentative exam date to must be either equal or before of tentative exam date from.";
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
    DistrictService districtService;

    @Autowired
    ProductGenderPhysicalRequirementService productGenderPhysicalRequirementService;

    @Autowired
    GenderService genderService;

    @Autowired
    PostExecutionService postExecutionService;

    @Autowired
    public ProductController(ExceptionHandlingService exceptionHandlingService, EntityManager entityManager, JwtUtil jwtTokenUtil, ProductService productService, RoleService roleService, JobGroupService jobGroupService, ProductStateService productStateService, ApplicationScopeService applicationScopeService, ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService, ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService, ReserveCategoryService reserveCategoryService, ReserveCategoryDtoService reserveCategoryDtoService, PhysicalRequirementDtoService physicalRequirementDtoService) {
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
    }

    @Transactional
    @PostMapping("/add/{categoryId}")
    public ResponseEntity<?> addProduct(
            HttpServletRequest request,
            @RequestBody AddProductDto addProductDto,
            @PathVariable Long categoryId,
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(value = "reserveCategoryOthers", required = false) List<String> reserveCategoryOthers,
            @RequestParam(value = "saveDraft", required = false, defaultValue = "false") boolean saveDraft) {

        try {
            String sourceName="add_product";
            List<OtherItem> reserveCategoryOtherList=new ArrayList<>();
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long userId = jwtTokenUtil.extractId(jwtToken);
            if (!productService.addProductAccessAuthorisation(authHeader)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO ADD PRODUCT", HttpStatus.FORBIDDEN);
            }

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Category category = productService.validateCategory(categoryId);

            if (!saveDraft) {
                productService.addProductDtoValidation(addProductDto);
            }
            else
            {
                productService.addProductDtoWithoutValidation(addProductDto);
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

            CustomJobGroup customJobGroup = productService.validateCustomJobGroup(addProductDto.getJobGroup());
            if (customJobGroup == null) {
                return ResponseService.generateErrorResponse("Custom job group not found.", HttpStatus.NOT_FOUND);
            }

            CustomProductState customProductState=null;
                if(!saveDraft)
                {
                    customProductState= productStateService.getProductStateByName(PRODUCT_STATE_NEW);
                    if (customProductState == null) {
                        return ResponseService.generateErrorResponse("Custom product state not found.", HttpStatus.NOT_FOUND);
                    }
                }
                else if(saveDraft)
                {
                     customProductState= productStateService.getProductStateByName(PRODUCT_STATE_DRAFT);
                    {
                        if (customProductState == null) {
                            return ResponseService.generateErrorResponse("Custom product state not found.", HttpStatus.NOT_FOUND);
                        }
                    }
                }

                product.setDefaultSku(sku);

                if(!saveDraft)
                {
                    reserveCategoryOtherList=productService.validateReserveCategory(addProductDto,roleId,userId,sourceName,reserveCategoryOthers,null);
                }
                else if(saveDraft)
                {
                    if(addProductDto.getReservedCategory()!=null)
                    {
                        reserveCategoryOtherList=productService.validateReserveCategory(addProductDto,roleId,userId,sourceName,reserveCategoryOthers,null);
                    }
                }
                CustomGender customGender = productService.validateGenderSpecificField(addProductDto);
                CustomSector customSector = productService.validateSector(addProductDto);

                productService.validateSelectionCriteria(addProductDto);
                Qualification  qualification  = productService.validateQualification(addProductDto);
                CustomStream customStream = productService.validateStream(addProductDto);
                CustomSubject customSubject = productService.validateSubject(addProductDto);

                productService.validateAdmitCardDates(addProductDto);
                productService.validateModificationDates(addProductDto);
                productService.validateLastDateToPayFee(addProductDto);

                if(!saveDraft)
                {
                    productService.validateLinks(addProductDto);
                }
                else if(saveDraft)
                {
                    if(addProductDto.getDownloadNotificationLink()!=null)
                    {
                        if (addProductDto.getDownloadNotificationLink().trim().isEmpty()) {
                            throw new IllegalArgumentException("Notification download link cannot be empty");
                        }
                        addProductDto.setDownloadNotificationLink(addProductDto.getDownloadNotificationLink().trim());
                    }
                    if(addProductDto.getDownloadSyllabusLink()!=null)
                    {
                        if (addProductDto.getDownloadSyllabusLink().trim().isEmpty()) {
                            throw new IllegalArgumentException("Syllabus download link cannot be empty.");
                        }
                        addProductDto.setDownloadSyllabusLink(addProductDto.getDownloadSyllabusLink().trim());
                    }
                }

            productService.validateFormComplexity(addProductDto);

            Role role = productService.getRoleByToken(authHeader);
            Long creatorUserId = productService.getUserIdByToken(authHeader);

            productService.saveCustomProduct(product, addProductDto, customProductState, role, creatorUserId, product.getActiveStartDate(), currentDate);

            if(!saveDraft)
            {
                productReserveCategoryFeePostRefService.saveFeeAndPost(addProductDto.getReservedCategory(), product);
                productReserveCategoryBornBeforeAfterRefService.saveBornBeforeAndBornAfter(addProductDto.getReservedCategory(), product);
            }
            else if(saveDraft)

            {
                if(addProductDto.getReservedCategory()!=null)
                {
                    productReserveCategoryFeePostRefService.saveFeeAndPost(addProductDto.getReservedCategory(), product);
                    productReserveCategoryBornBeforeAfterRefService.saveBornBeforeAndBornAfter(addProductDto.getReservedCategory(), product);
                }
            }

            CustomJobGroup jobGroup = jobGroupService.getJobGroupById(addProductDto.getJobGroup());
            CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());

            StateCode stateCode = null;
            if (addProductDto.getState() != null) {
                stateCode = districtService.getStateByStateId(addProductDto.getState());
            }

            CustomProductWrapper wrapper = new CustomProductWrapper();
            if(!saveDraft)
            {
                if(addProductDto.getPhysicalRequirement()!=null)
                {
                    productService.validatePhysicalRequirement(addProductDto, null);
                    productGenderPhysicalRequirementService.savePhysicalRequirement(addProductDto.getPhysicalRequirement(), product);
                }
                wrapper.wrapDetailsAddProduct(product, addProductDto, jobGroup, customProductState, applicationScope, creatorUserId, role, reserveCategoryService, stateCode, customGender, customSector, qualification, customStream, customSubject, currentDate);
            }
            else if(saveDraft)
            {
                if(addProductDto.getPhysicalRequirement()!=null)
                {
                    productService.validatePhysicalRequirement(addProductDto, null);
                    productGenderPhysicalRequirementService.savePhysicalRequirement(addProductDto.getPhysicalRequirement(), product);
                }
                if(reserveCategoryService!=null)
                {
                    wrapper.wrapDetailsAddProduct(product, addProductDto, jobGroup, customProductState, applicationScope, creatorUserId, role, reserveCategoryService, stateCode, customGender, customSector, qualification, customStream, customSubject, currentDate);
                }else{
                    wrapper.wrapDetailsAddProduct(product, addProductDto, jobGroup, customProductState, applicationScope, creatorUserId, role, null, stateCode, customGender, customSector, qualification, customStream, customSubject, currentDate);
                }
                ResponseEntity responseEntity= ResponseService.generateSuccessResponse("PRODUCT ADDED AS DRAFT SUCCESSFULLY", wrapper, HttpStatus.OK);
                postExecutionService.executePostProcessingLogicInAddProduct(wrapper,reserveCategoryOtherList);
                return  responseEntity;
            }
            ResponseEntity responseEntity= ResponseService.generateSuccessResponse("PRODUCT ADDED SUCCESSFULLY", wrapper, HttpStatus.OK);
            postExecutionService.executePostProcessingLogicInAddProduct(wrapper,reserveCategoryOtherList);
            return  responseEntity;

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
    public ResponseEntity<?> updateProduct(HttpServletRequest request, @RequestBody AddProductDto addProductDto, @PathVariable Long productId, @RequestParam(value = "reserveCategoryOthers", required = false) List<String> reserveCategoryOthers, @RequestHeader(value = "Authorization") String authHeader,  @RequestParam(value = "saveAsDraft", required = false, defaultValue = "false") boolean saveAsDraft) {

        try {
            String sourceName="update_product";
            List<OtherItem> reserveCategoryOtherList=new ArrayList<>();
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
            Product product = catalogService.findProductById(customProduct.getId());

            if (customProduct == null) {
                return ResponseService.generateErrorResponse(Constant.PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }
            Boolean isOtherReserveCategories=false;
            // Validations and checks.
            if (addProductDto.getReservedCategory() != null) {
             reserveCategoryOtherList= productService.validateReserveCategory(addProductDto,roleId,userId,sourceName,reserveCategoryOthers,customProduct);

              for(AddReserveCategoryDto reserveCategoryDto: addProductDto.getReservedCategory())
              {
                  if(reserveCategoryService.getReserveCategoryById(reserveCategoryDto.getReserveCategory()).getReserveCategoryName().equalsIgnoreCase("Others"))
                  {
                      isOtherReserveCategories=true;
                  }
              }

              if(isOtherReserveCategories.equals(false))
              {
                  List<OtherItem> currentOtherItems= customProduct.getOtherItems();
                if (!currentOtherItems.isEmpty()) {
                    Iterator<OtherItem> iterator = currentOtherItems.iterator();
                    while (iterator.hasNext()) {
                        OtherItem otherItem = iterator.next();
                            otherItem.setCustomProduct(null);
                            iterator.remove();
                        }
                }
              }
              else if (isOtherReserveCategories.equals(true))
              {
                  if (!reserveCategoryOtherList.isEmpty()) {
                      List<OtherItem> existingItems = customProduct.getOtherItems();
                      for (OtherItem otherItem : reserveCategoryOtherList) {
                          if (otherItem != null) {
                              otherItem.setCustomProduct(customProduct);
                              existingItems.add(otherItem);
                              entityManager.merge(otherItem);
                          }
                      }
                      entityManager.merge(customProduct);
                  }
              }
                productService.deleteOldReserveCategoryMapping(customProduct);
            }
            productService.updateProductValidation(addProductDto, customProduct);
            if(addProductDto.getPhysicalRequirement() != null) {
                productService.validatePhysicalRequirement(addProductDto, customProduct);
                productService.deleteOldPhysicalRequirement(customProduct);
            }

            // Validation of getActiveEndDate and getGoLiveDate.
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date currentDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date
            customProduct.setModifiedDate(currentDate);

            // Validate dates fields.
            productService.validateAndSetActiveStartDate(addProductDto, customProduct, currentDate);
            productService.validateAndSetActiveEndDate(addProductDto, customProduct, currentDate);
            productService.validateAndSetGoLiveDate(addProductDto,  customProduct, currentDate);
            productService.validateAndSetLastDateToPayFeeDate(addProductDto, customProduct, currentDate);

            productService.validateAndSetModifiedDates(addProductDto, customProduct, currentDate);
            productService.validateAndSetAdmitCardDates(addProductDto, customProduct, currentDate);
            productService.validateAndSetExamDates(addProductDto, customProduct, currentDate);
//            productService.validateAndSetExamDateFromAndExamDateToFields(addProductDto, customProduct);
//            productService.validateExamDateFromAndExamDateTo(addProductDto, customProduct);

            productService.validateProductState(addProductDto, customProduct, authHeader);

            customProduct.setModifierRole(roleService.getRoleByRoleId(jwtTokenUtil.extractRoleId(authHeader.substring(7))));
            customProduct.setModifierUserId(jwtTokenUtil.extractId(authHeader.substring(7)));

            entityManager.merge(customProduct);

            if (addProductDto.getReservedCategory() != null) {
                productReserveCategoryFeePostRefService.saveFeeAndPost(addProductDto.getReservedCategory(), product);
                productReserveCategoryBornBeforeAfterRefService.saveBornBeforeAndBornAfter(addProductDto.getReservedCategory(), product);
            }
            if(addProductDto.getPhysicalRequirement() != null) {
                productGenderPhysicalRequirementService.savePhysicalRequirement(addProductDto.getPhysicalRequirement(), product);
            }
            if(addProductDto.getGenderSpecific()!=null){
                if(addProductDto.getGenderSpecific() == 0) {
                    customProduct.setGenderSpecific(null);
                }else{
                    customProduct.setGenderSpecific(genderService.getGenderByGenderId(addProductDto.getGenderSpecific()));
                }
            }

            List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(productId);
            List<PhysicalRequirementDto> physicalRequirementDtoList = physicalRequirementDtoService.getPhysicalRequirementDto(productId);

            CustomProductWrapper wrapper = new CustomProductWrapper();

            if(saveAsDraft && customProduct.getProductState().getProductState().equalsIgnoreCase("DRAFT"))
            {
                wrapper.wrapDetails(customProduct, reserveCategoryDtoList, physicalRequirementDtoList);
                return ResponseService.generateSuccessResponse("Product is updated and saved as Draft successfully",wrapper,HttpStatus.OK);
            }
            else if(saveAsDraft && !customProduct.getProductState().getProductState().equalsIgnoreCase("DRAFT"))
            {
                wrapper.wrapDetails(customProduct, reserveCategoryDtoList, physicalRequirementDtoList);
                return ResponseService.generateSuccessResponse("Product is updated successfully",wrapper,HttpStatus.OK);
            }
            else if(!saveAsDraft)
            {
                if(customProduct.getProductState().getProductState().equalsIgnoreCase(PRODUCT_STATE_DRAFT))
                {
                   return productService.changeStateProductFromDraftToNew(customProduct,reserveCategoryDtoList,physicalRequirementDtoList,wrapper);
                }
            }
            wrapper.wrapDetails(customProduct, reserveCategoryDtoList, physicalRequirementDtoList);
            return ResponseService.generateSuccessResponse("Product Updated Successfully", wrapper, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse("Illegal Argument Exception: " + illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-product-by-id/{productId}")
    public ResponseEntity<?> retrieveProductById(HttpServletRequest request, @PathVariable("productId") String productIdPath) {

        try {

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

            if ((((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {

                CustomProductWrapper wrapper = new CustomProductWrapper();

                List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(productId);
                List<PhysicalRequirementDto> physicalRequirementDtoList = physicalRequirementDtoService.getPhysicalRequirementDto(productId);

                wrapper.wrapDetails(customProduct, reserveCategoryDtoList, physicalRequirementDtoList);
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
    public ResponseEntity<?> retrieveProducts() {

        try {
            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Product> products = catalogService.findAllProducts(); // find all the products.

            if (products.isEmpty()) {
                return ResponseService.generateErrorResponse(PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }

            List<CustomProductWrapper> responses = new ArrayList<>();
            for (Product product : products) {

                // finding customProduct that resembles with productId.
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());

                if (customProduct != null) {

                    if ((((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {

                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct);

                        responses.add(wrapper);
                    }
                }
            }

            return ResponseService.generateSuccessResponse(PRODUCTFOUNDSUCCESSFULLY, responses, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(SOME_EXCEPTION_OCCURRED + ": " + illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{productId}")
    @Transactional
    public ResponseEntity<?> deleteProduct(@PathVariable("productId") String productIdPath) {
        try {

            Long productId = Long.parseLong(productIdPath);

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId); // Find the Custom Product

            if (customProduct == null || (((Status) customProduct).getArchived() == 'Y')) {
                return ResponseService.generateErrorResponse(PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }

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
    public ResponseEntity<?> getAllNewStateProducts() {

        try {

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Product> products = catalogService.findAllProducts(); // find all the products.

            if (products.isEmpty()) {
                return ResponseService.generateErrorResponse(PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }

            List<CustomProductWrapper> responses = new ArrayList<>();
            for (Product product : products) {

                // finding customProduct that resembles with productId.
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());

                if (customProduct != null) {

                    if ((((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date())) && customProduct.getProductState().getProductState().equals(PRODUCT_STATE_NEW)) {

                        List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(customProduct.getId());
                        List<PhysicalRequirementDto> physicalRequirementDtoList = physicalRequirementDtoService.getPhysicalRequirementDto(customProduct.getId());

                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct, reserveCategoryDtoList, physicalRequirementDtoList);

                        responses.add(wrapper);
                    }

                }
            }

            return ResponseService.generateSuccessResponse(PRODUCTFOUNDSUCCESSFULLY, responses, HttpStatus.OK);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-live-state-products")
    public ResponseEntity<?> getAllLiveStateProducts() {

        try {

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Product> products = catalogService.findAllProducts(); // find all the products.

            if (products.isEmpty()) {
                return ResponseService.generateErrorResponse(PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }

            List<CustomProductWrapper> responses = new ArrayList<>();
            for (Product product : products) {

                // finding customProduct that resembles with productId.
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());

                if (customProduct != null) {

                    if ((((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date())) && !customProduct.getGoLiveDate().after(new Date())) {

                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct);

                        responses.add(wrapper);
                    }
                }
            }

            return ResponseService.generateSuccessResponse(PRODUCTFOUNDSUCCESSFULLY, responses, HttpStatus.OK);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-filter-products")
    public ResponseEntity<?> getFilterProducts(
            @RequestParam(value = "date_from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date dateFrom,
            @RequestParam(value = "date_to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date dateTo,
            @RequestParam(value = "state", required = false) List<Long> state,
            @RequestParam(value = "rejection_status", required = false) List<Long> status,
            @RequestParam(value = "category", required = false) List<Long> categories,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "fee", required = false) Double fee,
            @RequestParam(value = "post", required = false) Integer post,
            @RequestParam(value = "reserve_categories", required = false) List<Long> reserveCategories) {

        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            if(dateFrom != null) {
                String formattedDateFrom = dateFormat.format(dateFrom);
                dateFrom = dateFormat.parse(formattedDateFrom);
            }
            if(dateTo != null) {
                String formattedDateTo = dateFormat.format(dateTo);
                dateTo = dateFormat.parse(formattedDateTo);
            }
            List<CustomProduct> products = productService.filterProducts(state, status, categories, reserveCategories, title, fee, post, dateFrom, dateTo);

            if (products.isEmpty()) {
                return ResponseService.generateSuccessResponse("NO PRODUCTS FOUND WITH THE GIVEN CRITERIA", products, HttpStatus.OK);
            }

            List<CustomProductWrapper> responses = new ArrayList<>();
            for (CustomProduct customProduct : products) {

                if (customProduct != null) {

                    if ((((Status) customProduct).getArchived() != 'Y')) {

                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct);

                        responses.add(wrapper);
                    }
                }
            }

            return ResponseService.generateSuccessResponse("PRODUCTS RETRIEVED SUCCESSFULLY", responses, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllProductsByServiceProvider(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "false") boolean showDraftProducts) {

        try {
            if (authHeader == null || !authHeader.startsWith(Constant.BEARER_CONST)) {
                return ResponseService.generateErrorResponse("Authorization header is missing or invalid.", HttpStatus.UNAUTHORIZED);
            }

            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long userId = jwtTokenUtil.extractId(jwtToken);

            return productService.filterProductsByRoleAndUserId(roleId, userId, page, limit,showDraftProducts);

        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse("EXCEPTION OCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}