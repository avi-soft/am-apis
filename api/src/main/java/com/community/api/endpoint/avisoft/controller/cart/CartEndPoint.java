package com.community.api.endpoint.avisoft.controller.cart;

import com.broadleafcommerce.rest.api.endpoint.BaseEndpoint;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.EligibilityResult;
import com.community.api.endpoint.avisoft.controller.Customer.CustomerEndpoint;
import com.community.api.endpoint.avisoft.controller.Document.DocumentEndpoint;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CombinedOrderDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomerReferrer;
import com.community.api.entity.OrderConsent;
import com.community.api.entity.OrderCustomerDetailsDTO;
import com.community.api.entity.Post;
import com.community.api.entity.RazorpayDetails;
import com.community.api.entity.Role;
import com.community.api.entity.ShortAccessToken;
import com.community.api.services.CartService;
import com.community.api.services.CustomerAddressFetcher;
import com.community.api.services.DocumentStorageService;
import com.community.api.services.GenderService;
import com.community.api.services.OrderDTOService;
import com.community.api.services.OrderStatusByStateService;
import com.community.api.services.PdfEditService;
import com.community.api.services.ProductReserveCategoryFeePostRefService;
import com.community.api.services.ReserveCategoryService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.broadleafcommerce.common.currency.domain.BroadleafCurrency;
import org.broadleafcommerce.common.currency.service.BroadleafCurrencyService;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderAttributeImpl;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.OrderItemService;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.core.order.service.call.OrderItemRequest;
import org.broadleafcommerce.core.order.service.type.OrderStatus;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.community.api.component.Constant.*;
import static com.community.api.services.ServiceProvider.ServiceProviderServiceImpl.getLongList;

@RestController
@RequestMapping(value = "/cart",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
)
public class CartEndPoint extends BaseEndpoint {
    private CustomerService customerService;
    private OrderService orderService;
    private CatalogService catalogService;
    private ExceptionHandlingImplement exceptionHandling;

    private EntityManager entityManager;
    private OrderItemService orderItemService;
    private CartService cartService;
    private ResponseService responseService;
    private SharedUtilityService sharedUtilityService;
    private ReserveCategoryService reserveCategoryService;
    private ProductReserveCategoryFeePostRefService reserveCategoryFeePostRefService;
    private OrderDTOService orderDTOService;
    private GenderService genderService;


    @Value("${razorpay.key.id}")
    private String razorpayId;
    @Value("${razorpay.key.secret}")
    private String razorpaySecret;
    @Value(("${razorpay.webhook.secret}"))
    private String razorpayWebhookSecret;

    @Autowired
    BroadleafCurrencyService broadleafCurrencyService;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private RoleService roleService;


    private RazorpayClient razorpayCLient;

    @PostConstruct
    public void init() throws RazorpayException {
        this.razorpayCLient = new RazorpayClient(razorpayId, razorpaySecret);
    }

    // Setter-based injection
    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setGenderService(GenderService genderService) {
        this.genderService = genderService;
    }

    @Autowired
    private CustomerEndpoint customerEndpoint;

    @Autowired
    public void setOrderDTOService(OrderDTOService orderDTOService) {
        this.orderDTOService = orderDTOService;
    }

    @Autowired
    private CustomerAddressFetcher addressFetcher;

    @Autowired
    public void setSharedUtilityService(SharedUtilityService sharedUtilityService) {
        this.sharedUtilityService = sharedUtilityService;
    }

    @Autowired
    private ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;


    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }

    @Autowired
    public void setReserveCategoryService(ReserveCategoryService reserveCategoryService) {
        this.reserveCategoryService = reserveCategoryService;
    }

    @Autowired
    public void setReserveCategoryFeePostRefService(ProductReserveCategoryFeePostRefService reserveCategoryFeePostRefService) {
        this.reserveCategoryFeePostRefService = reserveCategoryFeePostRefService;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    private OrderStatusByStateService orderStatusByStateService;

    @Autowired
    public void setCatalogService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setOrderItemService(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }


    @Transactional
    @RequestMapping(value = "empty/{customerId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> emptyTheCart(@PathVariable Long customerId,@RequestHeader(value = "Authorization")String authHeader) { //@TODO-empty cart should remove each item one by one
        try {
            if(!verifyUser(authHeader,customerId))
                return ResponseService.generateErrorResponse("Forbidden Access",HttpStatus.FORBIDDEN);
            Long id = Long.valueOf(customerId);
            if (isAnyServiceNull()) {
                return ResponseService.generateErrorResponse("One or more Services not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (id == null)
                return ResponseService.generateErrorResponse("Customer Id not specified", HttpStatus.BAD_REQUEST);
            Customer customer = customerService.readCustomerById(customerId);//finding the customer to get cart associated with it
            Order cart = null;
            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer not found for this Id", HttpStatus.NOT_FOUND);
            } else {
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
                cart = this.orderService.findCartForCustomer(customer);
                if (cart == null) {
                    return ResponseService.generateErrorResponse("Cart Not Found", HttpStatus.NOT_FOUND);
                }
                if (cart.getOrderItems().isEmpty())
                    return ResponseService.generateErrorResponse("Cart already empty", HttpStatus.OK);
                if (cart.getStatus().equals(OrderStatus.IN_PROCESS)) {//ensuring its cart and not an order
                    List<OrderItem> items = cart.getOrderItems();
                    Iterator<OrderItem> iterator = items.iterator();

                    while (iterator.hasNext()) {
                        OrderItem item = iterator.next();
                        iterator.remove();
                        Product product = findProductFromItemAttribute(item);
                        CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());
                        if (customCustomer != null && customProduct != null) {
                            if (!customCustomer.getCartRecoveryLog().contains(customProduct))
                                customCustomer.getCartRecoveryLog().add(customProduct);
                        }
                        entityManager.remove(item);
                    }
                    entityManager.merge(cart);
                    entityManager.merge(customCustomer);
                    return ResponseService.generateSuccessResponse("Cart is empty now", null, HttpStatus.OK);
                } else
                    return ResponseService.generateErrorResponse("Error removing all items from cart", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {

            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error removing all items from cart : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "add-to-cart/{customerId}/{productId}", method = RequestMethod.POST)
    public ResponseEntity<?> addToCart(@PathVariable long customerId, @PathVariable long productId, @RequestBody Map<String, Object> map,@RequestHeader(value = "Authorization")String authHeader) {
        try {
            if(!verifyUser(authHeader,customerId))
                return ResponseService.generateErrorResponse("Forbidden Access",HttpStatus.FORBIDDEN);
            Long id = Long.valueOf(customerId);
            if (isAnyServiceNull()) {
                return ResponseService.generateErrorResponse("One or more Services not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (id == null)
                return ResponseService.generateErrorResponse("Customer Id not specified", HttpStatus.BAD_REQUEST);
            Customer customer = customerService.readCustomerById(customerId);
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customerId);
            if (customer == null || customCustomer == null) {
                return ResponseService.generateErrorResponse("Customer not found for this Id", HttpStatus.NOT_FOUND);
            }
            if (customer.getFirstName() == null ||
                    customer.getLastName() == null ||
                    customer.getEmailAddress() == null ||
                    customCustomer.getCategory() == null ||
                    customer.getUsername() == null ||
                    customer.getPassword() == null ||
                    customCustomer.getGender() == null) {
                return ResponseService.generateErrorResponse(
                        "Please complete your account setup and fill out the 'My Profile' section before setting up the cart.",
                        HttpStatus.BAD_REQUEST
                );
            }

            Order cart = orderService.findCartForCustomer(customer);
            if (cart == null) {
                cart = orderService.createNewCartForCustomer(customer);
                cart.setOrderNumber("C-" + customerId);
                /*  cart.setName("CART-"+customerId);*/
            }
            Product product = catalogService.findProductById(productId);
            if (product == null) {
                return ResponseService.generateErrorResponse("Product not found", HttpStatus.NOT_FOUND);
            }
            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);
            List<Long> postPreference = getLongList(map, "postPreference");
            if (postPreference.isEmpty() && customProduct.getPosts().size() > 1)
                return ResponseService.generateErrorResponse("Post Preference cannot be empty", HttpStatus.BAD_REQUEST);

            Long reserveCategoryId = reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId();
             Long  genderId = genderService.getGenderByName(customCustomer.getGender()).getGenderId();
            if (reserveCategoryId == null)
                return ResponseService.generateErrorResponse("Invalid Category", HttpStatus.INTERNAL_SERVER_ERROR);
            double noReserveCategoryFee = 0.0;

            /*if(reserveCategoryService.getReserveCategoryFee(productId,reserveCategoryId,genderId)==null) {
                return ResponseService.generateErrorResponse("Cannot add product to cart :Fee not specified for your category and gender", HttpStatus.UNPROCESSABLE_ENTITY);
               // noReserveCategoryFee=reserveCategoryService.getReserveCategoryFee(productId,1L,1L);//1 for general
            }*/

            /*if(productReserveCategoryFeePostRefService.getCustomProductReserveCategoryFeePostRefByProductIdAndReserveCategoryId(product.getId(),.getFee()==null)
            {

            }*/
            if ((((Status) customProduct).getArchived() == 'Y' || !customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {
                return ResponseService.generateErrorResponse("Cannot add an Archived/Expired product", HttpStatus.BAD_REQUEST);
            }
            OrderItemRequest orderItemRequest = new OrderItemRequest();
            orderItemRequest.setProduct(product);
            orderItemRequest.setOrder(cart);
            orderItemRequest.setQuantity(1);
            orderItemRequest.setCategory(product.getCategory());
            orderItemRequest.setItemName(product.getName());
            Map<String, String> atrtributes = orderItemRequest.getItemAttributes();
            atrtributes.put("productId", product.getId().toString());
            List<Long> actualPostIds = new ArrayList<>();
            for (Post post : customProduct.getPosts()) {
                actualPostIds.add(post.getPostId());
            }
            if (customProduct.getPosts().size() >= 2) {
                for (Long pId : postPreference) {
                    if (!actualPostIds.contains(pId))
                        return ResponseService.generateErrorResponse("Invalid post id in preference list", HttpStatus.BAD_REQUEST);
                }
                if (postPreference.size() < 1 && customProduct.getPosts().size() >= 1)
                    return ResponseService.generateErrorResponse("Need to provide atleast one post for preference", HttpStatus.BAD_REQUEST);
                if (postPreference.size() > customProduct.getPosts().size())
                    return ResponseService.generateErrorResponse("Invalid post ids provided", HttpStatus.BAD_REQUEST);
                String postPreferenceString = postPreference.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                atrtributes.put("postPreference", postPreferenceString);
            } else if (customProduct.getPosts().size() == 1) {
                postPreference.removeAll(postPreference);
                postPreference = new ArrayList<>(postPreference);
                postPreference.clear();
                postPreference.add(customProduct.getPosts().get(0).getPostId());
                String postPreferenceString = postPreference.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                atrtributes.put("postPreference", postPreferenceString);
            } else {
                atrtributes.put("postPreference", "NO_AVAILABLE_POSTS");
            }
            orderItemRequest.setItemAttributes(atrtributes);
            OrderItem orderItem = orderItemService.createOrderItem(orderItemRequest);
            List<OrderItem> items = cart.getOrderItems();
            Map<String, Object> responseBody = new HashMap<>();
            boolean flag = false;
            for (OrderItem existingOrderItem : items) {
                if (Long.parseLong(existingOrderItem.getOrderItemAttributes().get("productId").getValue()) == productId) {
                    flag = true;
                    return ResponseService.generateErrorResponse(Constant.CANNOT_ADD_MORE_THAN_ONE_FORM, HttpStatus.UNPROCESSABLE_ENTITY);
                }
            }
            if (!flag)
                items.add(orderItem);
            cart.setOrderItems(items);
            responseBody.put("cart_id", cart.getId());
            responseBody.put("added_product_id", orderItem.getOrderItemAttributes().get("productId").getValue());
            return ResponseService.generateSuccessResponse("Cart updated", responseBody, HttpStatus.OK);

        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {

            return ResponseService.generateErrorResponse("Error adding item to cart : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "number-of-items/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveCartItemsCount(@PathVariable long customerId,@RequestHeader(value = "Authorization")String authHeader) {
        try {
            Long id = Long.valueOf(customerId);
            if(!verifyUser(authHeader,customerId))
                return ResponseService.generateErrorResponse("Forbidden Access",HttpStatus.FORBIDDEN);
            if (id == null)
                return ResponseService.generateErrorResponse("Customer Id not specified", HttpStatus.BAD_REQUEST);
            if (isAnyServiceNull()) {
                return ResponseService.generateErrorResponse("One or more Serivces not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            Map<String, Object> responseBody = new HashMap<>();
            if (customer != null) {
                if (orderService.findCartForCustomer(customer) != null) {
                    responseBody.put("number_of_items", orderService.findCartForCustomer(customer).getOrderItems().size());
                    return ResponseService.generateSuccessResponse("Items in cart :", responseBody, HttpStatus.OK);
                } else
                    return ResponseService.generateErrorResponse("No items found", HttpStatus.NOT_FOUND);
            } else
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);

        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving cart", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @JsonBackReference
    @RequestMapping(value = "preview-cart/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveCartItems(@PathVariable long customerId, @RequestHeader(value = "inFunctionCall", required = false, defaultValue = "false") boolean inFunctionCall,@RequestHeader(value = "Authorization")String authHeader) {
        try {
            if(!verifyUser(authHeader,customerId))
                return ResponseService.generateErrorResponse("Forbidden Access",HttpStatus.FORBIDDEN);
            Customer customer = customerService.readCustomerById(customerId);
            Order cart = orderService.findCartForCustomer(customer);
            if (cart == null)
                return ResponseService.generateErrorResponse("Cart not activated", HttpStatus.OK);
            double productFee = 0.0;
            Double individualFee = 0.0;
            List<OrderItem> archievedItems = new ArrayList<>();
            Long id = Long.valueOf(customerId);
            if (id == null)
                return ResponseService.generateErrorResponse("Customer Id not specified", HttpStatus.BAD_REQUEST);
            Double subTotal = 0.0;
//            Double platformfee = 10.0;
            Double totalPlatformFee =  0.0;
            if (isAnyServiceNull()) {
                return ResponseService.generateErrorResponse("One or more Serivces not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (customer == null) {
                return ResponseService.generateErrorResponse("customer does not exist", HttpStatus.NOT_FOUND);
            }
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customerId);
            Long reserveCategoryId = reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId();
            List<Product> listOfProducts = new ArrayList<>();
            List<OrderItem> orderItemList = cart.getOrderItems();
            if (orderItemList != null && (!orderItemList.isEmpty())) {
                Map<String, Object> response = new HashMap<>();
                List<Map<String, Object>> products = new ArrayList<>();
                for (OrderItem orderItem : orderItemList) {
                    Product product = findProductFromItemAttribute(orderItem);
                    CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());
                    if (product != null) {
                        if ((((Status) customProduct).getArchived() == 'Y' || !customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {
                            archievedItems.add(orderItem);
                            continue;
                        }
                        totalPlatformFee =totalPlatformFee+ customProduct.getPlatformFee();
                        EligibilityResult result = cartService.checkCustomerEligibilityDetailed(customCustomer, customProduct, false);
                       /* EligibilityResult result=new EligibilityResult();
                        result.setStatus(CartService.EligibilityStatus.ELIGIBLE);*/
                        Map<String, Object> productDetails = sharedUtilityService.createProductResponseMap(product, orderItem, customCustomer, genderService.getGenderByName(customCustomer.getGender()).getGenderId(),result);

                       products.add(productDetails);
                        individualFee = null;

// 1. Check for ALL category and ALL gender
                        individualFee = reserveCategoryService.getReserveCategoryFee(
                                product.getId(),
                                reserveCategoryService.getReserveCategoryById(RESERVED_CATEGORY_ALL).getReserveCategoryId(),
                                genderService.getGenderByGenderId(GENDER_ALL).getGenderId()
                        );

// 2. Check for ALL category and actual gender
                        if (individualFee == null) {
                            individualFee = reserveCategoryService.getReserveCategoryFee(
                                    product.getId(),
                                    reserveCategoryService.getReserveCategoryById(RESERVED_CATEGORY_ALL).getReserveCategoryId(),
                                    genderService.getGenderByName(customCustomer.getGender()).getGenderId()
                            );
                        }

// 3. Check for actual category and ALL gender
                        if (individualFee == null) {
                            individualFee = reserveCategoryService.getReserveCategoryFee(
                                    product.getId(),
                                    reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId(),
                                    genderService.getGenderByGenderId(GENDER_ALL).getGenderId()
                            );
                        }

// 4. Check for actual category and actual gender
                        if (individualFee == null) {
                            individualFee = reserveCategoryService.getReserveCategoryFee(
                                    product.getId(),
                                    reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId(),
                                    genderService.getGenderByName(customCustomer.getGender()).getGenderId()
                            );
                        }

// 5. Fallback to General category (1L) with actual gender
                        if (individualFee == null) {
                            individualFee = reserveCategoryService.getReserveCategoryFee(
                                    product.getId(),
                                    1L, // hardcoded general category
                                    genderService.getGenderByName(customCustomer.getGender()).getGenderId()
                            );
                        }

// 6. Final fallback to 0.0 if still null
                        if (individualFee == null) {
                            individualFee = 0.0;
                        }
                    }
                    productFee = productFee + individualFee;

                }
                subTotal = totalPlatformFee + productFee;
                response.put("cart_id", cart.getId());
                response.put("products", products.toArray());
                response.put("sub_total", subTotal);
                response.put("price", productFee);
                response.put("total_platform_fee", totalPlatformFee);
                for (OrderItem orderItem : archievedItems) {
                    cart.getOrderItems().remove(orderItem);
                }
                archievedItems.clear();
                if (!inFunctionCall)
                    return ResponseService.generateSuccessResponse("Cart items", response, HttpStatus.OK);
                else
                    return ResponseService.generateSuccessResponse("Cart items after modifying post preference", response, HttpStatus.OK);
            } else
                return ResponseService.generateErrorResponse("No items in cart", HttpStatus.OK);

        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving cart Items", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "remove-item/{customerId}/{orderItemId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeCartItems(
            @PathVariable long customerId,
            @PathVariable Long orderItemId,
        @RequestHeader(value = "Authorization")String authHeader){
        try {
            Long id = Long.valueOf(customerId);
            if (id == null)
                return ResponseService.generateErrorResponse("Customer Id not specified", HttpStatus.BAD_REQUEST);
            if(!verifyUser(authHeader,customerId))
                return ResponseService.generateErrorResponse("Forbidden Access",HttpStatus.FORBIDDEN);
            if (isAnyServiceNull()) {
                return ResponseService.generateErrorResponse("One or more Services not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("Invalid request: Customer does not exist", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
            Order cart = orderService.findCartForCustomer(customer);
            if (cart == null || cart.getOrderItems() == null) {
                return ResponseService.generateErrorResponse("Cart Empty", HttpStatus.OK);
            }
            OrderItem orderItemToRemove = null;
            for (OrderItem orderItem : cart.getOrderItems()) {
                if (orderItem.getId().equals(orderItemId)) {
                    orderItemToRemove = orderItem;
                    break;
                }
            }
            if (orderItemToRemove == null) {
                return ResponseService.generateErrorResponse("Item to remove not found", HttpStatus.NOT_FOUND);
            }
            long pid = Long.parseLong(orderItemToRemove.getOrderItemAttributes().get("productId").getValue());
            CustomProduct customProduct = entityManager.find(CustomProduct.class, pid);
            if (customProduct != null) {
                if (!customCustomer.getCartRecoveryLog().contains(customProduct))
                    customCustomer.getCartRecoveryLog().add(customProduct);
            }
            boolean itemRemoved = cartService.removeItemFromCart(cart, orderItemId);
            /*OrderItem orderItem=entityManager.find(OrderItem.class,orderItemId);*/
            if (itemRemoved) {
                return ResponseService.generateSuccessResponse("Item Removed", null, HttpStatus.OK);
            } else {
                return ResponseService.generateErrorResponse("Error removing item from cart: item not present in cart", HttpStatus.NOT_FOUND);
            }

        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            e.printStackTrace();
            return ResponseService.generateErrorResponse("Error deleting", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Autowired
    DocumentEndpoint documentEndpoint;
    @Value("${order.policy.path}")
    private String policyPath;
    @Value("${file.server.url}")
    private String fileServerUrl;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    DocumentStorageService documentStorageService;

    @Transactional
    @GetMapping("/policy")
    public ResponseEntity<?>getOrderPolicy(HttpServletRequest request,@RequestHeader(value = "Authorization", required = false)String authHeader) throws Exception {
        System.out.println(fileServerUrl+"/"+policyPath);
      /*  String jwtToken = authHeader.substring(7);
        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
        Long userId=jwtTokenUtil.extractId(jwtToken);*/
        TypedQuery<ShortAccessToken> query = entityManager.createQuery(
                "SELECT s FROM ShortAccessToken s WHERE s.userId = :uid AND s.role = :role",
                ShortAccessToken.class
        );
        query.setParameter("uid", 22L);
        query.setParameter("role", 1);
        String ip = request.getRemoteAddr();
        String token=jwtUtil.generateShortLivedToken(22L, 1, ip);
        List<ShortAccessToken> resultList = query.getResultList();

        if (resultList.isEmpty()) {
            ShortAccessToken shortAccessToken = ShortAccessToken.builder()
                    .userId(22L)
                    .token(token)
                    .role(1)
                    .expired(false)
                    .build();
            entityManager.persist(shortAccessToken);
        } else {
            ShortAccessToken shortAccessToken = resultList.get(0);
            shortAccessToken.setToken(token);
            shortAccessToken.setExpired(false);
            entityManager.merge(shortAccessToken);
        }
        /*pdfEditService.sendPdfToApi(pdfEditService.createPdfInMemory());*/
        Map<String,String>respone=new HashMap<>();
        respone.put("policy_url",fileServerUrl+"/"+documentStorageService.encrypt(policyPath)+"?x9f3a="+token);
        respone.put("seed", documentEndpoint.generateUniqueId());
        return ResponseService.generateSuccessResponse("policy_url",respone,HttpStatus.OK);
    }
    @Transactional
    @RequestMapping(value = "place-order/{customerId}", method = RequestMethod.POST)
    public ResponseEntity<?> placeOrder(@PathVariable Long customerId, @RequestBody Map<String, Object> map, @RequestHeader(value = "Authorization") String authHeader) {
        try {
            String orderAcknowledgementId= (String) map.get("ack");
            if(orderAcknowledgementId==null)
                return ResponseService.generateErrorResponse("Need to provide user consent",HttpStatus.BAD_REQUEST);
            Boolean bypass=false;
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);

            if(!verifyUser(authHeader,customerId))
                return ResponseService.generateErrorResponse("Forbidden Access",HttpStatus.FORBIDDEN);
            CustomProduct customProduct = null;
            /* Long id = Long.valueOf(customerId);*/
            List<Long> orderItemIds = getLongList(map, "orderItemIds");
            if (customerId == null)
                return ResponseService.generateErrorResponse("Customer Id not specified", HttpStatus.BAD_REQUEST);
            Map<String, Object> responseMap = new HashMap<>();
            List<Order> individualOrders = new ArrayList<>();
            Customer customer = customerService.readCustomerById(customerId);
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customerId);
            if (customer == null || customCustomer == null)
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            Order cart = orderService.findCartForCustomer(customer);
            if (cart == null)
                return ResponseService.generateErrorResponse("Cart not found", HttpStatus.NOT_FOUND);
            if (cart.getOrderItems().isEmpty())
                return ResponseService.generateErrorResponse("Cart is empty", HttpStatus.NOT_FOUND);
            List<Long> cartItemIds = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            int batchNumber = 0;
            if (customCustomer.getNumberOfOrders() == null)
                batchNumber = 1;
            else {
                batchNumber = customCustomer.getNumberOfOrders();
                ++batchNumber;
            }
            for (OrderItem orderItem : cart.getOrderItems()) {
                cartItemIds.add(orderItem.getId());
            }
            if (orderItemIds.isEmpty())
                return ResponseService.generateErrorResponse("No items Selected", HttpStatus.BAD_REQUEST);

            for (Long orderItemId : orderItemIds) {
                if (!cartItemIds.contains(orderItemId)) {
                    errors.add("Order Item Id : " + orderItemId + " does not belong to cart");
                }
            }
            List<Order> newOrders = new ArrayList<>();
            if (!errors.isEmpty())
                return ResponseService.generateErrorResponse("Error Placing order : " + errors.toString(), HttpStatus.BAD_REQUEST);
            JSONObject options = new JSONObject();
            double totalAmt=0.0;
            for (OrderItem orderItem : cart.getOrderItems()) {
                if (orderItemIds.contains(orderItem.getId())) {
                    Product product = findProductFromItemAttribute(orderItem);
                    if (product != null)
                        customProduct = entityManager.find(CustomProduct.class, product.getId());
                    Double   individualFee = null;



// 1. Check for ALL category and ALL gender
                    individualFee = reserveCategoryService.getReserveCategoryFee(
                            product.getId(),
                            reserveCategoryService.getReserveCategoryById(RESERVED_CATEGORY_ALL).getReserveCategoryId(),
                            genderService.getGenderByGenderId(GENDER_ALL).getGenderId()
                    );

// 2. Check for ALL category and actual gender
                    if (individualFee == null) {
                        individualFee = reserveCategoryService.getReserveCategoryFee(
                                product.getId(),
                                reserveCategoryService.getReserveCategoryById(RESERVED_CATEGORY_ALL).getReserveCategoryId(),
                                genderService.getGenderByName(customCustomer.getGender()).getGenderId()
                        );
                    }

// 3. Check for actual category and ALL gender
                    if (individualFee == null) {
                        individualFee = reserveCategoryService.getReserveCategoryFee(
                                product.getId(),
                                reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId(),
                                genderService.getGenderByGenderId(GENDER_ALL).getGenderId()
                        );
                    }

// 4. Check for actual category and actual gender
                    if (individualFee == null) {
                        individualFee = reserveCategoryService.getReserveCategoryFee(
                                product.getId(),
                                reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId(),
                                genderService.getGenderByName(customCustomer.getGender()).getGenderId()
                        );
                    }

// 5. Fallback to General category (1L) with actual gender
                    if (individualFee == null) {
                        individualFee = reserveCategoryService.getReserveCategoryFee(
                                product.getId(),
                                1L, // hardcoded general category
                                genderService.getGenderByName(customCustomer.getGender()).getGenderId()
                        );
                    }

// 6. Final fallback to 0.0 if still null
                    if (individualFee == null) {
                        individualFee = 0.0;
                    }
                    if (individualFee == null)
                        individualFee = 0.0;
                    totalAmt+=customProduct.getPlatformFee()+individualFee;
                    System.out.println("total price is "+totalAmt);
                }
            }
            options.put("amount", (totalAmt* 100));
            if(totalAmt<=0) {
                /*return ResponseService.generateErrorResponse("Razorpay cannot trigger order generation as amount is <= 0",HttpStatus.UNPROCESSABLE_ENTITY);*/
                bypass = true;
            }
            else {
                options.put("currency", "INR");
                options.put("receipt", customer.getEmailAddress());
            }
            com.razorpay.Order razorpayOrder=null;
            if(!bypass) {
                razorpayOrder = razorpayCLient.orders.create(options);
            }
            for (OrderItem orderItem : cart.getOrderItems()) {
                if (orderItemIds.contains(orderItem.getId())) {
                    Product product = findProductFromItemAttribute(orderItem);
                    if (product != null)
                        customProduct = entityManager.find(CustomProduct.class, product.getId());

                    Order individualOrder = orderService.createNamedOrderForCustomer(orderItem.getName(), customer);
                    individualOrder.setCustomer(customer);
                    individualOrder.setEmailAddress(customer.getEmailAddress());

                    OrderItemRequest orderItemRequest = new OrderItemRequest();
                    orderItemRequest.setProduct(product);
                    individualOrder.setCustomer(customer);
                    orderItemRequest.setOrder(individualOrder);
                    orderItemRequest.setQuantity(1);
                    orderItemRequest.setCategory(product.getCategory());
                    orderItemRequest.setItemName(product.getDisplayTemplate());
                    Map<String, String> atrtributes = orderItemRequest.getItemAttributes();
                    atrtributes.put("productId", product.getId().toString());
                    //atrtributes.put("assigneeSPId",null);
                    orderItemRequest.setItemAttributes(atrtributes);
                    OrderItem orderItemForIndividualOrder = orderItemService.createOrderItem(orderItemRequest);
                    individualOrder.addOrderItem(orderItemForIndividualOrder);
                    Double platformFee = 0.0;
                    if (customProduct.getPlatformFee() != null)
                        platformFee = customProduct.getPlatformFee();
                    Double individualFee = reserveCategoryService.getReserveCategoryFee(product.getId(), reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId(), genderService.getGenderByName(customCustomer.getGender()).getGenderId());//1 for general
                    if (individualFee == null)
                        individualFee = 0.0;
                    Money subTotal = new Money(platformFee+individualFee);
                    individualOrder.setSubTotal(subTotal);
                    individualOrder.setOrderNumber("O-" + customer.getId() + "-B-" + batchNumber);
                    //Checking for cost according to the category and gender of the customer
                    Double totalCost =null;
                    totalCost = reserveCategoryService.getReserveCategoryFee(
                                    product.getId(),
                                    reserveCategoryService.getReserveCategoryById(RESERVED_CATEGORY_ALL).getReserveCategoryId(),
                                    genderService.getGenderByGenderId(GENDER_ALL).getGenderId()
                            );

// 2. Check for ALL category and actual gender
                    if (totalCost == null) {
                        totalCost = reserveCategoryService.getReserveCategoryFee(
                                product.getId(),
                                reserveCategoryService.getReserveCategoryById(RESERVED_CATEGORY_ALL).getReserveCategoryId(),
                                genderService.getGenderByName(customCustomer.getGender()).getGenderId()
                        );
                    }

// 3. Check for actual category and ALL gender
                    if (totalCost == null) {
                        totalCost = reserveCategoryService.getReserveCategoryFee(
                                product.getId(),
                                reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId(),
                                genderService.getGenderByGenderId(GENDER_ALL).getGenderId()
                        );
                    }

// 4. Check for actual category and actual gender
                    if (totalCost == null) {
                        totalCost = reserveCategoryService.getReserveCategoryFee(
                                product.getId(),
                                reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId(),
                                genderService.getGenderByName(customCustomer.getGender()).getGenderId()
                        );
                    }

// 5. Fallback to General category (1L) with actual gender
                    if (totalCost == null) {
                        totalCost = reserveCategoryService.getReserveCategoryFee(
                                product.getId(),
                                1L, // hardcoded general category
                                genderService.getGenderByName(customCustomer.getGender()).getGenderId()
                        );
                    }

// 6. Final fallback to 0.0 if still null
                    if (totalCost == null) {
                        totalCost = 0.0;
                    }
                    totalCost = totalCost + platformFee;
                    Money total = new Money(totalCost);
                    individualOrder.setTotal(total);
                    LocalDateTime localDateTime = LocalDateTime.now();
                    Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                    individualOrder.setSubmitDate(date);
                    individualOrder.setSubmitDate(date);
                    String retrievedPostPreferenceString = (String) (orderItem.getOrderItemAttributes().get("postPreference").getValue());
                    OrderAttributeImpl orderAttribute = new OrderAttributeImpl();
                    orderAttribute.setOrder(individualOrder);
                    orderAttribute.setName("postPreference");
                    orderAttribute.setValue(retrievedPostPreferenceString);
                    individualOrder.getOrderAttributes().put("postPreference", orderAttribute);
                    entityManager.merge(individualOrder);
                    individualOrder.setEmailAddress(customer.getEmailAddress());
                    CustomOrderState orderState = new CustomOrderState();

                    if (razorpayOrder != null) {
                        individualOrder.setOrderNumber(razorpayOrder.get("id"));
                        OrderStatus orderStatus = new OrderStatus("CREATED", null);
                        individualOrder.setStatus(orderStatus);

                        //creating razorpay order
                        RazorpayDetails razorpayDetails=new RazorpayDetails();
                        razorpayDetails.setOrderId(individualOrder.getId());
                        razorpayDetails.setRazorpayOrderId(razorpayOrder.get("id"));
                        razorpayDetails.setTimeStamp(LocalDateTime.now());
                        razorpayDetails.setStatus("initiated");
                        entityManager.persist(razorpayDetails);
                        //

                    }else if(bypass)
                    {
                        individualOrder.setOrderNumber("N/A");
                        OrderStatus orderStatus = new OrderStatus("NEW", null);
                        individualOrder.setStatus(orderStatus);

                        //creating razorpay order
                        RazorpayDetails razorpayDetails=new RazorpayDetails();
                        razorpayDetails.setOrderId(individualOrder.getId());
                        razorpayDetails.setRazorpayOrderId("N/A");
                        razorpayDetails.setTimeStamp(LocalDateTime.now());
                        razorpayDetails.setStatus("N/A");
                        entityManager.persist(razorpayDetails);
                    }
                    else
                        return ResponseService.generateErrorResponse("Error creating order : RAZORPAY_EXCEPTION", HttpStatus.INTERNAL_SERVER_ERROR);
                    if(bypass)
                        orderState.setOrderStateId((ORDER_STATE_NEW.getOrderStateId()));
                    else
                        orderState.setOrderStateId((ORDER_STATE_CREATED.getOrderStateId()));
                    orderState.setOrderId(individualOrder.getId());
                    orderState.setModifiedDate(new Date());
                    orderState.setModifierUserId(customerId);
                    orderState.setModifierRole(role);
                    // Integer orderStatusId=orderStatusByStateService.getOrderStatusByOrderStateId(ORDER_STATE_NEW.getOrderStateId()).get(0).getOrderStatusId();
                    //orderState.setOrderStatusId(orderStatusId);
                    //orderState.setOrderStatusId(orderStatusId);
                    entityManager.persist(orderState);
                    customerEndpoint.setReferrerForCustomer(customerId, customProduct.getUserId(), false, authHeader);
                    OrderConsent orderConsent=new OrderConsent();
                    orderConsent.setOrderId(individualOrder.getId());
                    orderConsent.setUserId(individualOrder.getCustomer().getId());
                    orderConsent.setAckId(orderAcknowledgementId);
                    orderConsent.setTimeStamp(new Date());
                    entityManager.persist(orderConsent);
                    individualOrders.add(individualOrder);

                }
            }
//            responseMap.put("Orders",  );
            customCustomer.setNumberOfOrders(batchNumber);

            entityManager.merge(cart);
            List<CombinedOrderDTO> orderDTOS = new ArrayList<>();
            for (Order order : individualOrders) {
                CustomOrderState orderState = entityManager.find(CustomOrderState.class, order.getId());
                OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(customerId, customer.getFirstName() + " " + customCustomer.getLastName(), customer.getEmailAddress(), customCustomer.getMobileNumber(), addressFetcher.fetch(customer), customer.getUsername());
                orderDTOS.add(orderDTOService.wrapOrder(order, orderState, null, customerDetailsDTO));
            }
            if(bypass)
            {
                for(Long orderItemId:orderItemIds)
                    cartService.removeItemFromCart(cart,orderItemId);
                return ResponseService.generateSuccessResponse("Order Placed successfully", orderDTOS, HttpStatus.OK);
            }
            else
                return ResponseService.generateSuccessResponse("Order Created", orderDTOS, HttpStatus.OK);
        } catch (RazorpayException razorpayException) {
            razorpayException.printStackTrace();
            return ResponseService.generateErrorResponse("Error creating order due to a Razorpay Exception", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {

            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error creating order " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PutMapping("{customerId}/confirm-order")
    public ResponseEntity<?> confirmOrderStatus(@PathVariable Long customerId,@RequestParam List<Long> orderIds, @RequestBody(required = false) Map<String, String> paymentStatus,@RequestParam(required = false,defaultValue = "false")Boolean failed,@RequestHeader(value = "Authorization")String authHeader) {

        if(customerId==null)
            return ResponseService.generateErrorResponse("Customer id is required",HttpStatus.BAD_REQUEST);
        Customer customer=customerService.readCustomerById(customerId);
        if(customer==null)
            return ResponseService.generateErrorResponse("Customer not found",HttpStatus.BAD_REQUEST);

        String status=null;
        if(!failed) {
            String razorpayOrderId = paymentStatus.get("razorpay_order_id");
            String razorpayPaymentId = paymentStatus.get("razorpay_payment_id");
            String razorpaySignature = paymentStatus.get("razorpay_signature");
             status= getPaymentStatus(razorpayPaymentId);
            if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null || status == null) {
                return ResponseService.generateErrorResponse("Missing required payment verification fields", HttpStatus.BAD_REQUEST);
            }


            try {
                String data = razorpayOrderId + "|" + razorpayPaymentId;
                String generatedSignature = sharedUtilityService.hmacSha256(data, razorpaySecret); // Use your actual Razorpay key secret

                if (!generatedSignature.equals(razorpaySignature)) {
                    return ResponseService.generateErrorResponse("Signature verification failed", HttpStatus.UNAUTHORIZED);
                }
            } catch (Exception e) {
                return ResponseService.generateErrorResponse("Error verifying Razorpay signature: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else
        {
            RazorpayDetails razorpayDetails=entityManager.find(RazorpayDetails.class,orderIds.get(0));
        }


        // Rest of the order processing (same as before)
        BroadleafCurrency broadleafCurrency = broadleafCurrencyService.findDefaultBroadleafCurrency();

        boolean isFailed = false;
        List<CombinedOrderDTO> orderDTOS = new ArrayList<>();
        Order order;
        for (Long orderId : orderIds) {

             order = orderService.findOrderById(orderId);

            if(order!=null&&!verifyUser(authHeader,order.getCustomer().getId()))
                return ResponseService.generateErrorResponse("Forbidden Access",HttpStatus.FORBIDDEN);
            if(!order.getCustomer().getId().equals(customerId))
                return ResponseService.generateErrorResponse("Order do not belong to selected customer",HttpStatus.BAD_REQUEST);
            RazorpayDetails details=entityManager.find(RazorpayDetails.class,orderId);
            if(details.getVerified()!=null&&details.getVerified())
            {
                return ResponseService.generateErrorResponse("Cannot verify payment : Order with id : "+orderId.longValue()+" already verified",HttpStatus.FORBIDDEN);
            }
            if(!failed&&!details.getStatus().equals(status))
            {
                Map<String,String>jsonObject=new HashMap<>();
                jsonObject.put("razorpay_payment_status",details.getStatus());
                jsonObject.put("recieved_status",status);

                return ResponseService.generateSuccessResponse("ORDER FAILED:Status mismatch",jsonObject,HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (order == null) {
                return ResponseService.generateErrorResponse("Cannot find order with ID: " + orderId, HttpStatus.NOT_FOUND);
            }
            OrderStatus orderStatus;
            CustomOrderState customOrderState = entityManager.find(CustomOrderState.class, orderId);
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, order.getCustomer().getId());

            if ("captured".equalsIgnoreCase(status)) {

                orderStatus = new OrderStatus("NEW", null);
                details.setTimeStamp(LocalDateTime.now());
                details.setVerified(true);
                order.setStatus(orderStatus);
                customOrderState.setOrderStateId(ORDER_STATE_NEW.getOrderStateId());
                customOrderState.setOrderStatusId(1);
                customOrderState.setModifiedDate(new Date());
                order.setSubmitDate(new Date());

                OrderItem orderItem = order.getOrderItems().get(0);
                System.out.println("Order item"+orderItem.getId());
                Product product = findProductFromItemAttribute(orderItem);
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());
                customProduct.getPurchasedBy().add(customCustomer.getId());
                ServiceProviderEntity refSp = entityManager.find(ServiceProviderEntity.class, customProduct.getUserId());

                if (refSp != null) {
                    Query query = entityManager.createNativeQuery(Constant.CHECK_FOR_REPEATED_REF);
                    query.setParameter("customerId", customCustomer.getId());
                    query.setParameter("spId", customProduct.getUserId());
                    Integer result = ((BigInteger) query.getSingleResult()).intValue();

                    if (result == 0) {
                        CustomerReferrer customerReferrer = new CustomerReferrer();
                        customerReferrer.setCustomer(customCustomer);
                        customerReferrer.setServiceProvider(refSp);
                        customerReferrer.setCreatedAt(LocalDateTime.now());
                        customCustomer.getMyReferrer().add(customerReferrer);
                    }
                }
                try {
                    System.out.println("calling removal");
                    Order cart = orderService.findCartForCustomer(customer);
                    OrderItem orderItemToRemove = null;
                    for (OrderItem orderItem1 : cart.getOrderItems()) {
                        System.out.println("order item id current "+orderItem.getId());
                        System.out.println(findProductFromItemAttribute(orderItem).getId());
                        System.out.println("order item inside current "+orderItem1.getId());
                        System.out.println(findProductFromItemAttribute(orderItem1).getId());
                        if (findProductFromItemAttribute(orderItem).getId().equals(findProductFromItemAttribute(orderItem1).getId())) {
                            orderItemToRemove = orderItem1;
                            break;
                        }
                    }
                    cartService.removeItemFromCart(cart, orderItemToRemove.getId());
                    entityManager.merge(cart);
                    System.out.println("removal done");
                    entityManager.merge(customCustomer);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            } else if ((failed)||"failed".equalsIgnoreCase(status)) {
                isFailed = true;
                details.setVerified(true);
                orderStatus = new OrderStatus("PAYMENT_FAILED", null);
                details.setTimeStamp(LocalDateTime.now());
                details.setStatus("failed");
                details.setVerified(true);
                order.setStatus(orderStatus);
                customOrderState.setOrderStateId(ORDER_STATE_FAILED.getOrderStateId());
                customOrderState.setOrderStatusId(null);
                customOrderState.setModifiedDate(new Date());
                order.setSubmitDate(new Date());
                entityManager.merge(order);
            } else {
                return ResponseService.generateErrorResponse("Invalid payment status: " + status, HttpStatus.BAD_REQUEST);
            }

            order.setCurrency(broadleafCurrency);
            entityManager.merge(order);

            // Prepare DTO
            Product product = findProductFromItemAttribute(order.getOrderItems().get(0));
            customer = customerService.readCustomerById(order.getCustomer().getId());
            OrderCustomerDetailsDTO customerDetailsDTO = new OrderCustomerDetailsDTO(
                    customer.getId(),
                    customer.getFirstName() + " " + customer.getLastName(),
                    customer.getEmailAddress(),
                    customCustomer.getMobileNumber(),
                    addressFetcher.fetch(customer),
                    customer.getUsername()
            );
            orderDTOS.add(orderDTOService.wrapOrder(order, customOrderState, null, customerDetailsDTO));
        }

        if (!isFailed) {
            /*emptyTheCart(customerId,authHeader);*/
            return ResponseService.generateSuccessResponse("Order placed successfully", orderDTOS, HttpStatus.OK);
        } else {

            return ResponseService.generateErrorResponse("Failed to place order", HttpStatus.PAYMENT_REQUIRED);
        }
    }

    @RequestMapping(value ="cart-recovery-log/{customerId}",method = RequestMethod.GET)
    public ResponseEntity<?>getCartRecoveryLog(@PathVariable Long customerId,@RequestHeader(value = "Authorization")String authHeader)
    {
        try{
            Long id = Long.valueOf(customerId);
            if(id==null)
                return ResponseService.generateErrorResponse("Customer Id not specified",HttpStatus.BAD_REQUEST);
            if(!verifyUser(authHeader,customerId))
                return ResponseService.generateErrorResponse("Forbidden Access",HttpStatus.FORBIDDEN);
            CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customerId);
                if(customCustomer==null)
                    return ResponseService.generateErrorResponse("Cannot find customer for this id",HttpStatus.NOT_FOUND);
                List<Map<String,Object>>productList=new ArrayList<>();
                for(Product product:customCustomer.getCartRecoveryLog())
                {
                    productList.add(sharedUtilityService.createProductResponseMap(product,null,customCustomer,genderService.getGenderByName(customCustomer.getGender()).getGenderId(),null));
                }
                return ResponseService.generateSuccessResponse("Cart Recovery Log : ",productList,HttpStatus.OK);

            }catch (NumberFormatException e) {
                return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
            }catch (Exception e) {

            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error fetching recovery log", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @RequestMapping(value = "{customerId}/update-preference/{productId}", method = RequestMethod.PUT)
    public ResponseEntity<?> updatePreference(@PathVariable Long customerId,@PathVariable Long productId,@RequestBody Map<String, Object> map,@RequestParam Long orderItemId,@RequestHeader(value = "Authorization")String authHeader) {
        try {
            if(!verifyUser(authHeader,customerId))
                return ResponseService.generateErrorResponse("Forbidden Access",HttpStatus.FORBIDDEN);
            List<Long> postPreference = getLongList(map, "postPreference");
            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);
            if (customProduct == null)
                return ResponseService.generateErrorResponse("Invalid product id provided", HttpStatus.NOT_FOUND);
            if(customProduct.getPosts().size()>=1) {
                List<Long> actualPostIds = new ArrayList<>();
                for (Post post : customProduct.getPosts()) {
                    actualPostIds.add(post.getPostId());
                }
                for (Long pId : postPreference) {
                    if (!actualPostIds.contains(pId))
                        return ResponseService.generateErrorResponse("Invalid post id in preference list", HttpStatus.BAD_REQUEST);
                }
                if (postPreference.size() < 1)
                    return ResponseService.generateErrorResponse("Need to provide atleast one post for preference", HttpStatus.BAD_REQUEST);
                if (postPreference.size() > customProduct.getPosts().size())
                    return ResponseService.generateErrorResponse("Invalid post ids provided", HttpStatus.BAD_REQUEST);
                String postPreferenceString = postPreference.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                String sql = "UPDATE blc_order_item_attribute " +
                        "SET value = ? " +
                        "WHERE order_item_id = ? " +
                        "AND name = 'postPreference' " +
                        "AND EXISTS (SELECT 1 FROM blc_order_item WHERE order_item_id = ?)";
                int rowsUpdated = jdbcTemplate.update(sql, postPreferenceString, orderItemId, orderItemId);
                if (rowsUpdated >= 0) {
                    return retrieveCartItems(customerId, true,authHeader);
                }
            }else
                return ResponseService.generateErrorResponse("No Posts available for product",HttpStatus.NOT_FOUND);
        }catch (PersistenceException persistenceException)
        {
            exceptionHandling.handleException(persistenceException);
        } catch(Exception exception)
        {
            exceptionHandling.handleException(exception);
        }
        return ResponseService.generateErrorResponse("Error updating post preference", HttpStatus.BAD_REQUEST);

}

    private boolean isAnyServiceNull() {
        return customerService == null || orderService == null || catalogService == null;
    }

    public Product findProductFromItemAttribute(OrderItem orderItem) {
        Long productId = Long.parseLong(orderItem.getOrderItemAttributes().get("productId").getValue());
        Product product = catalogService.findProductById(productId);
        return product;
    }


    //constanlty updates order's status

    @Transactional
    @PostMapping("/order-events")
    public void handleWebhook(@RequestHeader("X-Razorpay-Signature") String razorpaySignature,
                                @RequestBody String payload) {

        System.out.println("SERVER HAS CALLED THE WEBHOOK");
        try {
            // Verify webhook signature to confirm authenticity
            boolean isValid = Utils.verifyWebhookSignature(payload, razorpaySignature,razorpayWebhookSecret);

            if (!isValid) {
                throw new Exception("SIGNATURE VERIFICATION FAILED");
            }
            System.out.println(payload);

            // Parse the payload JSON (use your preferred JSON library)
            JSONObject webhookData = new JSONObject(payload);
            String event = webhookData.getString("event");
            System.out.println(event);
            JSONObject paymentEntity = webhookData.getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");
            System.out.println("order id:" + paymentEntity.getString("order_id"));
            Query query = entityManager.createNativeQuery("SELECT order_id from blc_order where order_number = :rzpId");
            query.setParameter("rzpId", paymentEntity.getString("order_id"));
            List<BigInteger> orderIds = query.getResultList();
                    // Extract payment info and update order status to PAID
                    for(BigInteger id:orderIds) {
                        System.out.println("orderId" + id);
                        Order order=orderService.findOrderById(id.longValue());
                        if(broadleafCurrencyService.findDefaultBroadleafCurrency()==null) {
                            BroadleafCurrency broadleafCurrency = broadleafCurrencyService.create();
                            broadleafCurrency.setFriendlyName("INDIAN RUPEE");
                            broadleafCurrency.setCurrencyCode("INR");
                            broadleafCurrency.setDefaultFlag(true);
                        }
                        else
                            order.setCurrency(broadleafCurrencyService.findDefaultBroadleafCurrency());
                        RazorpayDetails details = entityManager.find(RazorpayDetails.class, id.longValue());
                        details.setStatus(paymentEntity.getString("status"));
                        details.setRazorpayPaymentId(paymentEntity.getString("id"));
                        entityManager.merge(details);
                        entityManager.merge(order);
                    }
        } catch (Exception e) {
            System.out.println("Exception : "+e.getMessage());
        }
    }
    public String getPaymentStatus(String paymentId) {
        String uri = "https://api.razorpay.com/v1/payments/" + paymentId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(razorpayId, razorpaySecret);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            String status = jsonNode.get("status").asText();  // e.g. "captured", "failed", etc.
            return status;

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    public Boolean verifyUser(String authHeader,Long userId)
    {
        String jwtToken = authHeader.substring(7);
        Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
        String roleName= roleService.findRoleName(roleId);
        Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
        return roleUser.equals(roleName) && tokenUserId.equals(userId);
    }
    @Transactional
    public void removeCartItems(Long customerId, Long orderItemId) {
        Customer customer = customerService.readCustomerById(customerId);
        Order cart = orderService.findCartForCustomer(customer);

        if (cart == null || cart.getOrderItems() == null) {
            System.out.println("No items found");
            return;
        }

        OrderItem orderItemToRemove = null;
        for (OrderItem orderItem : cart.getOrderItems()) {
            if (orderItem.getId().equals(orderItemId)) {
                orderItemToRemove = orderItem;
                break;
            }
        }

        if (orderItemToRemove != null) {
            cart.getOrderItems().remove(orderItemToRemove); // remove from list
            entityManager.remove(entityManager.contains(orderItemToRemove) ? orderItemToRemove : entityManager.merge(orderItemToRemove)); // remove from DB
            entityManager.merge(cart); // optional if cascade is set
            System.out.println("Item removed successfully.");
        } else {
            System.out.println("Item not found in cart.");
        }
    }
    }

