package com.community.api.component;

import com.community.api.entity.CustomOrderState;
import org.broadleafcommerce.core.order.service.type.OrderStatus;

import javax.servlet.http.HttpServletRequest;

public class Constant {

    public static final long MAX_REFERRER_FILE_SIZE = 9 * 1024 * 1024;
    public static final Integer PERMANENT_ADDRESS_ID=5;
    public static final Integer CURRENT_ADDRESS_ID=2;
    public static final Integer OFFICE_ADDRESS_ID=1;
    public static final long MAX_FILE_SIZE_FOR_OVERALL_DOCUMENTS = 1 * 1024 * 1024;
    public static final int RANDOM_RESIZED_DOCUMENT_TYPE_ID= 33;
    public static final int RANDOM_PDF_DOCUMENT_TYPE_ID= 34;
    public static final int RANDOM_SIGNATURE_DOCUMENT_TYPE_ID= 35;
    public static final int RESIZED_IMAGE_DOCUMENT_TYPE_ID= 36;
    public static final int UPLOADED_PDF_DOCUMENT_TYPE_ID= 37;
    public static final int SIGNATURE_IMAGE_DOCUMENT_TYPE_ID= 38;
    public static final Long AND_OPERATOR_ID= 1L;
    public static String COUNTRY_CODE = "+91";
    public static String PHONE_QUERY = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode";
    public static String PHONE_QUERY_OTP = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode AND c.otp=:otp";
    public static String ID_QUERY = "SELECT c FROM CustomCustomer c WHERE c.customer_id = :customer_id";
    public static final String FIND_ALL_QUALIFICATIONS_QUERY = "SELECT dt FROM Qualification dt where dt.archived =:archived ORDER BY dt.sort_order ASC";
    public static final String FIND_ALL_SERVICE_PROVIDER_TEST_STATUS_QUERY= "SELECT q FROM ServiceProviderTestStatus q";
    public static final String FIND_ALL_BOARD_UNIVERSITY_QUERY = "SELECT q FROM BoardUniversity q WHERE q.archived = :archived ORDER BY q.sortOrder ASC";

    public static final String FIND_ALL_INSTITUTION_QUERY = "SELECT q FROM Institution q WHERE q.archived = :archived ORDER BY q.sortOrder ASC";
    public static final String FIND_ALL_SERVICE_PROVIDER_TEST_RANK_QUERY= "SELECT q FROM ServiceProviderRank q ORDER BY q.rank_id";
    public static final String FIND_SERVICE_PROVIDER_RANK_BY_SERVICE_PROVIDER_RANK_ID = "SELECT r FROM ServiceProviderRank r WHERE r.rank_id = :serviceProviderRankId";
    public static final String GET_ALL_DOCUMENT_TYPES="SELECT dt FROM DocumentType dt where dt.archived=:archived ORDER BY dt.sort_order ASC";
    public static final String GET_ALL_ARCHIVE_UNARCHIVE_DOCUMENT_TYPES="SELECT dt FROM DocumentType dt ORDER BY dt.sort_order ASC";      ////ALL WITHOUT ANY ARCHIVE OR UNARCHIVE CONDITION
    public static final String GET_ALL_RANDOM_TYPING_TEXT="SELECT q FROM TypingText q where q.archived = :archived";
    public static final String GET_ALL_ARCHIVE_UNARCHIVE_RANDOM_TYPING_TEXT="SELECT q FROM TypingText q";
    public static final String GET_ALL_FILE_TYPE="SELECT q FROM FileType q where q.archived = :archived";
    public static final String GET_ALL_ARCHIVED_NONARCHIVED_FILE_TYPE="SELECT q FROM FileType q";
    public static final String GET_ALL_SCORING_CRITERIA="SELECT q FROM ScoringCriteria q";

    public static String PHONE_QUERY_SERVICE_PROVIDER = "SELECT c FROM ServiceProviderEntity c WHERE c.mobileNumber = :mobileNumber AND c.country_code = :country_code";
    public static String PHONE_QUERY_SERVICE_PROVIDER_FILTER = "SELECT c FROM ServiceProviderEntity c WHERE c.mobileNumber = :mobileNumber AND c.country_code = :country_code AND approved = true AND role =4 ";

    public static String PHONE_QUERY_ADMIN="SELECT c FROM CustomAdmin c WHERE c.mobileNumber = :mobileNumber AND c.country_code = :country_code";
    public static String USERNAME_QUERY_SERVICE_PROVIDER = "SELECT c FROM ServiceProviderEntity c WHERE c.user_name = :username";
    public static String USERNAME_QUERY_CUSTOM_ADMIN = "SELECT c FROM CustomAdmin c WHERE c.user_name = :username";
    public static final String ADMIN = "ADMIN";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String SERVICE_PROVIDER = "SERVICE_PROVIDER";
    public static final String USER = "USER";
    public static final int INITIAL_STATUS = 1;
    public static final Long INITIAL_TEST_STATUS = 1L;
    public static final Long TEST_COMPLETED_STATUS = 2L;
    public static final Long APPROVED_SP = 3L;
    public static final Long REJECTED_SP = 4L;
    public static final Long SUSPENDED_SP = 5L;

    public static String STATE_CODE_QUERY = "SELECT s FROM StateCode s WHERE s.state_name = :state_name";
    public static final String APPLIED_FORM_QUERY = "SELECT DISTINCT o.order_id FROM blc_order o JOIN order_state os ON o.order_id = os.order_id WHERE o.customer_id = :customerId AND o.tax_override IS NULL AND os.order_state_id NOT IN (5, 999)";
    public static final String SP_USERNAME_QUERY = "SELECT s FROM ServiceProviderEntity s WHERE s.user_name LIKE :username";
    public static final String SP_EMAIL_QUERY = "SELECT s FROM ServiceProviderEntity s WHERE s.primary_email LIKE :email";
    public static final String jpql = "SELECT a FROM ServiceProviderAddressRef a";
    public static String DISTRICT_ALL_QUERY = "SELECT d from Districts d where archived =:archived";
    public static String DISTRICT_QUERY = "SELECT d from Districts d WHERE d.state_code = :state_code and archived = :archived";
    public static String DISTRICT_QUERY_ALL = "SELECT d from Districts d WHERE d.state_code = :state_code";
    public static String FIND_DISTRICT = "SELECT d.district_name from Districts d where d.district_id = :district_id";
    public static String FIND_DISTRICT_ID_BY_NAME = "SELECT d.district_id from Districts d where d.district_name = :district_name";
    public static String FIND_DISTRICT_BY_NAME = "SELECT d from Districts d where d.district_name = :district";
    public static String FIND_STATE = "SELECT s.state_name from StateCode s where s.state_id = :state_id";
    public static String FIND_STATE_ID_BY_NAME = "SELECT s.state_id from StateCode s where s.state_name = :state_name";
    public static String FETCH_ROLE = "SELECT r.role_name FROM Role r WHERE r.role_id = :role_id";
    public static final String roleUser = "CUSTOMER";
    public static final String roleSuperAdmin = "SUPER_ADMIN";
    public static final String roleAdmin="ADMIN";
    public static final String roleAdminServiceProvider="ADMIN_SERVICE_PROVIDER";
    public static final String roleServiceProvider = "SERVICE_PROVIDER";
    public static final String roleServiceProviderAdmin = "ADMIN_SERVICE_PROVIDER";
    public static String GET_SKILLS_COUNT = "SELECT COUNT(*) FROM Skill";
    public static String GET_ALL_SKILLS = "SELECT s FROM Skill s";
    public static String GET_LANGUAGES_COUNT = "SELECT COUNT(*) FROM ServiceProviderLanguage";
    public static String GET_ALL_LANGUAGES = "SELECT s FROM ServiceProviderLanguage s";
    public static String OTP_SERVICE_PROVIDER = "SELECT c.otp FROM ServiceProviderEntity c WHERE c.mobileNumber = :mobileNumber";
    public static String serviceProviderRoles = "SELECT c.privilege_id FROM service_provider_privileges c WHERE c.service_provider_id = :serviceProviderId";
    public static String GET_PRIVILEGES_COUNT = "SELECT COUNT(*) FROM Privileges";
    public static String GET_ALL_PRIVILEGES = "SELECT p FROM Privileges s";
    public static String GET_INFRA_COUNT = "SELECT COUNT(*) FROM ServiceProviderInfra";
    public static String GET_INFRA_LIST = "SELECT s FROM ServiceProviderInfra s";
    public static String GET_SERVICE_PROVIDER_DEFAULT_ADDRESS="SELECT a from ServiceProviderAddressRef a where address_name =:address_name";
    public static String GET_COUNT_OF_ROLES="Select COUNT(*) from Role";
    public static String GET_COUNT_OF_STATUS="Select COUNT(*) from ServiceProviderStatus";
    public static String GET_ALL_STATUS="Select s from ServiceProviderStatus s";
    public static String GET_ALL_ROLES="Select r from Role r";
    public static String SOME_EXCEPTION_OCCURRED = "Some exception occurred";
    public static String NUMBER_FORMAT_EXCEPTION = "Number format exception";
    public static String CATALOG_SERVICE_NOT_INITIALIZED = "Catalog service not initialized";
    public static String GET_STATES_LIST="Select s from StateCode s where archived= :archived and isState = true";
    public static String GET_STATES_LIST_ALL="Select s from StateCode s";
    public static String GET_DIVISIONS_LIST="Select s from StateCode s where archived= :archived and isZone = true";
    public static String GET_QUALIFICATIONS_COUNT = "SELECT COUNT(*) FROM Qualification";
    public static String GET_BOARD_UNIVERSITY_COUNT="SELECT COUNT(*) FROM BoardUniversity";
    public static String GET_DOCUMENT_TYPE_COUNT="SELECT COUNT(*) FROM DocumentType";
    public static String GET_INSTITUTION_COUNT="SELECT COUNT(*) FROM Institution";
    public static String GET_TYPING_TEXT_COUNT = "SELECT COUNT(*) FROM TypingText";
    public static String GET_FILE_TYPE_COUNT = "SELECT COUNT(*) FROM FileType";
    public static Integer CUSTOMER_ROLE_ID= 5;

    public static String GET_ORDER_ITEM_PRODUCT="Select p.product_id from custom_order_item_product p where p.order_item_id =:orderItemId";
    public static String CANNOT_ADD_MORE_THAN_ONE_FORM="You can only add one of this form. Please choose a different form if you need more";
    public static String MOBILE_NUMBER_CONSTRAINT_IN_CUSTOM_ADMIN="ALTER TABLE custom_admin " + "ADD CONSTRAINT chk_mobile_number " + "CHECK (mobilenumber ~ '^[+]?[0-9]{9,13}$')";
    public static String PASSWORD_CONSTRAINT_IN_CUSTOM_ADMIN="ALTER TABLE custom_admin "+ "ADD CONSTRAINT chk_password_length "+ "CHECK (char_length(password) = 60)";
    public static String GET_ALL_APPLICATION_SCOPE = "SELECT * FROM custom_application_scope";
    public static String GET_ALL_STATES = "SELECT * FROM state_codes";
    public static String GET_ALL_RESERVED_CATEGORY = "SELECT * FROM custom_reserve_category where archived =:archived ORDER BY sort_order ASC";
    public static String GET_COUNT_OF_JOB_ROLE = "SELECT COUNT(c) FROM CustomJobGroup c";
    public static String GET_ALL_JOB_GROUP = "SELECT s FROM CustomJobGroup s";
    public static String GET_APPLICATION_SCOPE_BY_ID = "SELECT c FROM CustomApplicationScope c WHERE c.applicationScopeId = :applicationScopeId";
    public static Integer DEFAULT_QUANTITY = 100000;
    public static Integer DEFAULT_PRIORITY_LEVEL = 3;
    public static String GET_JOB_GROUP_BY_ID = "SELECT c FROM CustomJobGroup c WHERE c.jobGroupId = :jobGroupId";
    public static String GET_ADVERTISEMENT_BY_ID = "SELECT a FROM Advertisement a WHERE a.advertisementId = :advertisementId";
    public static String GET_ALL_PRODUCT_STATE = "SELECT c FROM CustomProductState c";
    public static String GET_PRODUCT_STATE_BY_ID = "SELECT c FROM CustomProductState c WHERE c.productStateId = :productStateId";
    public static String GET_PRODUCT_STATE_BY_NAME = "SELECT c FROM CustomProductState c WHERE c.productState = :productStateName";
    public static String PRODUCT_STATE_NEW = "NEW";
    public static String PRODUCT_STATE_DRAFT="DRAFT";
    public static String PRODUCT_STATE_MODIFIED = "MODIFIED";
    public static String PRODUCT_STATE_LIVE = "LIVE";
    public static String PRODUCT_STATE_APPROVED = "APPROVED";
    public static String PRODUCT_STATE_RESUBMIT = "RESUBMIT";
    public static String PRODUCT_STATE_RESUBMITTED = "RESUBMITTED";
    public static String PRODUCT_STATE_EXPIRED = "EXPIRED";
    public static String PRODUCT_STATE_END = "END";
    public static String PRODUCT_STATE_REJECTED = "REJECTED";

    public static String SERVICE_PROVIDER_PRIVILEGE = "SELECT privilege_id FROM service_provider_privileges WHERE service_provider_id = :serviceProviderId";
    public static String GET_ROLE_BY_ROLE_ID = "SELECT r FROM Role r WHERE r.role_id = :roleId";
    public static String PRIVILEGE_ADD_PRODUCT = "ADD_PRODUCT";
    public static String PRIVILEGE_COMMUNICATION_WITH_CUSTOMERS = "COMMUNICATE_WITH_CUSTOMERS";
    public static String PRIVILEGE_ADD_DOCUMENT_TYPE = "ADD_DOCUMENT_TYPE";
    public static String PRIVILEGE_TICKET = "PRIVILEGE_TICKET";
    public static String GET_PRODUCT_RESERVECATEGORY_BORNBEFORE_BORNAFTER = "SELECT c FROM CustomProductReserveCategoryBornBeforeAfterRef c WHERE c.customProduct = :customProduct";
    public static String GET_PRODUCT_RESERVECATEGORY_FEE_POST = "SELECT c FROM CustomProductReserveCategoryFeePostRef c WHERE c.customProduct = :customProduct";
    public static String ADD_PRODUCT_RESERVECATEOGRY_BORNBEFORE_BORNAFTER = "INSERT INTO custom_product_reserve_category_born_before_after_reference (product_id, reserve_category_id, born_before, born_after, gender_id, born_before_after, maximum_age, minimum_age) VALUES (:productId, :reserveCategoryId, :bornBefore, :bornAfter, :genderId, :bornBeforeAfter, :maximumAge, :minimumAge) RETURNING product_reserve_category_id";
    public static String ADD_PRODUCT_RESERVECATEOGRY_FEE_POST = "INSERT INTO custom_product_reserve_category_fee_post_reference (product_id, reserve_category_id, fee, post ,gender_id,fee_additional_comments,is_other_or_state_category,other_or_state_category,running_field,gender_running_field) VALUES (:productId, :reserveCategoryId, :fee, :post ,:genderId, :fee_additional_comments, :is_other_or_state_category, :other_or_state_category, :running_field, :gender_running_field)";
    public static String GET_RESERVED_CATEGORY_BY_ID = "SELECT c FROM CustomReserveCategory c WHERE c.reserveCategoryId = :reserveCategoryId";
    public static String APPLICATION_SCOPE_STATE = "STATE";
    public static String PRIVILEGE_UPDATE_PRODUCT = "UPDATE_PRODUCT";
    public static String PRIVILEGE_DELETE_PRODUCT = "DELETE_PRODUCT";
    public static String APPLICATION_SCOPE_CENTER = "CENTER";
    public static String PRIVILEGE_APPROVE_PRODUCT = "APPROVE_PRODUCT";
    public static String PRIVILEGE_REJECT_PRODUCT = "REJECT_PRODUCT";
    public static final String PRODUCTNOTFOUND = "Product not Found";
    public static final String CATEGORYNOTFOUND = "Category not Found";
    public static final String PRODUCTTITLENOTGIVEN = "Product MetaTitle not Given";
    public static final int MAX_REQUEST_SIZE=100;
    public static  final int MAX_NESTED_KEY_SIZE=100;
    public static final String GET_ALL_SERVICE_PROVIDERS="Select s from ServiceProviderEntity s";
    public static final String GET_ALL_CUSTOMERS="Select c from CustomCustomer c";

    public static final String GET_ALL_TICKET_STATE = "SELECT c FROM CustomTicketState c";
    public static final String GET_TICKET_STATE_BY_TICKET_STATE_ID = "SELECT c FROM CustomTicketState c WHERE c.ticketStateId = :ticketStateId";
    public static final String GET_ALL_TICKET_TYPE = "SELECT c FROM CustomTicketType c";
    public static final String GET_TICKET_TYPE_BY_TICKET_TYPE_ID = "SELECT c FROM CustomTicketType c WHERE c.ticketTypeId = :ticketTypeId";
    public static final String GET_ALL_TICKET_STATUS = "SELECT c FROM CustomTicketStatus c";
    public static final String GET_TICKET_STATE_BY_TICKET_STATUS_ID = "SELECT c FROM CustomTicketStatus c WHERE c.ticketStatusId = :ticketStatusId";
    public static final String GET_SP_REFERRED_CANDIDATES="Select s.customer_id from customer_referrer s Where s.service_provider_id =:service_provider_id";
    public static final Double DEFAULT_PLATFORM_FEE = 0d;

    public static final String GET_ALL_REJECTION_STATUS = "SELECT c FROM CustomProductRejectionStatus c";
    public static final String GET_REJECTION_STATUS_BY_REJECTION_ID = "SELECT c FROM CustomProductRejectionStatus c WHERE c.rejectionStatusId = :rejectionStatusId";
    public static final String GET_STATE_BY_STATE_ID = "SELECT c FROM StateCode c WHERE c.state_id = :stateId";
    public static final String GET_STATE_BY_STATE_NAME = "SELECT c FROM StateCode c WHERE c.state_name = :state";
    public static final String GET_DISTRICT_BY_DISTRICT_NAME = "SELECT c FROM Districts c WHERE c.district_name = :district AND c.state_code = :state";
    public static final String GET_STATE_BY_STATE_CODE = "SELECT c FROM StateCode c WHERE c.state_code = :code";
    public static final String GET_ALL_GENDER = "SELECT c FROM CustomGender c where archived =:archived";
    public static final String GET_GENDER_BY_GENDER_ID = "SELECT c FROM CustomGender c WHERE c.genderId = :genderId";
    public static final String GET_GENDER_BY_GENDER_NAME = "SELECT c FROM CustomGender c WHERE c.genderName = :genderName";
    public static final Double MAX_HEIGHT = 300d;
    public static final Double MIN_HEIGHT = 50d;
    public static final Double MAX_WEIGHT = 700d;
    public static final Double MIN_WEIGHT = 2d;
    public static final Double MAX_SHOE_SIZE = 12d;
    public static final Double MIN_SHOE_SIZE = 2d;
    public static final Double MAX_WAIST_SIZE = 120d;
    public static final Double MIN_WAIST_SIZE = 10d;
    public static final Double MAX_CHEST_SIZE = 125d;
    public static final Double MIN_CHEST_SIZE = 20d;
    public static final String GET_RESERVE_CATEGORY_BY_ID= "SELECT r FROM CustomReserveCategory r WHERE r.reserveCategoryName = :name";
    public static final String GET_PRODUCT_GENDER_PHYSICAL_REQUIREMENT = "SELECT c FROM CustomProductGenderPhysicalRequirementRef c WHERE c.customProduct = :customProduct";
    public static final String GET_RESERVE_CATEGORY_FEE= "SELECT p.fee FROM custom_product_reserve_category_fee_post_reference p WHERE p.product_id = :pid AND p.reserve_category_id = :reserveCategoryId AND p.gender_id = :genderId";
    public static final String GET_ALL_SUBJECT = "SELECT c FROM CustomSubject c WHERE c.archived = :archived ORDER BY sortOrder ASC";
    public static final String GET_ALL_SUBJECT_ARCHIVE_UNARCHIVE = "SELECT c FROM CustomSubject c ORDER BY sortOrder ASC";
    public static final String GET_ALL_STREAM = "SELECT c FROM CustomStream c WHERE c.archived = :archived ORDER BY sortOrder  ASC ";
    public static final String GET_ALL_STREAM_ARCHIVE_NONARCHIVE = "SELECT c FROM CustomStream c ORDER BY sortOrder  ASC ";
    public static final String GET_SUBJECT_BY_SUBJECT_ID = "SELECT c FROM CustomSubject c WHERE c.subjectId = :subjectId";
    public static final String GET_DOCUMENT_TYPE_BY_ID = "SELECT c FROM DocumentType c WHERE c.document_type_id = :documentTypeId";
    public static final String GET_SUBJECT_BY_SUBJECT_NAME = "SELECT c FROM CustomSubject c WHERE LOWER(c.subjectName) = LOWER(:subjectName) AND c.archived != 'Y'";
    public static final String GET_STREAM_BY_STREAM_ID = "SELECT c FROM CustomStream c WHERE c.streamId = :streamId";
    public static final String GET_STREAM_BY_STREAM_NAME = "SELECT c FROM CustomStream c WHERE LOWER(c.streamName) = LOWER(:streamName) AND c.archived != 'Y'";
    public static final String GET_ALL_SECTOR = "SELECT c FROM CustomSector c where archived = :archived";
    public static final String GET_SECTOR_BY_SECTOR_ID = "SELECT c FROM CustomSector c WHERE c.sectorId = :sectorId";
    public static final String GET_QUALIFICATION_BY_ID = "SELECT c FROM Qualification c WHERE c.qualification_id = :qualificationId";
    public static final String PINCODE_REGEXP="^\\d{6}$";
    public static final String CITY_REGEXP="^[A-Za-z\\\\s]+$";
    public static final String EMAIL_REGEXP="^[\\w-\\.]+@[\\w-]+\\.[a-zA-Z]{2,}$";
    public static final String GET_ALL_ORDERS_OF_ONE_CUSTOMER="SELECT o from blc_ ";
    public static final String GET_ORDERS_USING_CUSTOMER_ID = "SELECT CAST(o.order_id AS BIGINT) FROM blc_order o WHERE o.order_number LIKE :orderNumber and  tax_override is NULL";
    public static final String CHECK_FOR_REPEATED_REF="SELECT COUNT(*) FROM customer_referrer c WHERE c.customer_id = :customerId AND c.service_provider_id = :spId";
    public static final String GET_ALL_ORDERS="SELECT order_id FROM order_state";
    public static final String SEARCH_ORDER_QUERY="SELECT o.order_id FROM order_state o WHERE o.order_state_id =:orderStateId";
    public static final String GET_NEW_ORDERS="SELECT o.order_id FROM order_state o WHERE o.order_state_id = 1";
    public static final String GET_SP_ORDER_REQUEST="SELECT o.order_request_id FROM SP_orders_requests o WHERE o.order_id = :orderId AND o.service_provider_id = :serviceProviderId ";
    public static final String GET_ONE_SP_ORDER_REQUEST="SELECT o.order_request_id FROM SP_orders_requests o WHERE o.service_provider_id = :serviceProviderId AND o.request_Status = :requestStatus";
    public static final String GET_ONE_SP_ALL_ORDER_REQUEST="SELECT o.order_request_id FROM SP_orders_requests o WHERE o.service_provider_id = :serviceProviderId" ;
    public static final String SP_REQUEST_ACTION_ACCEPT="ACCEPT";
    public static final String SP_REQUEST_ACTION_RETURN="RETURN";
    public static final String SP_REQUEST_ACTION_VIEW="VIEW";
    public static final String NOT_ELIGIBLE_SP="SELECT s.service_provider_id FROM sp_orders_requests s WHERE order_id = :orderId AND request_status ='RETURNED'";
    public static final OrderStatus ORDER_STATUS_NEW = new OrderStatus("NEW", "NEW", true);
    public static final OrderStatus ORDER_STATUS_COMPLETED = new OrderStatus("COMPLETED", "COMPLETED", true);
    public static final OrderStatus ORDER_STATUS_IN_REVIEW = new OrderStatus("IN_REVIEW", "IN_REVIEW", true);
    public static final OrderStatus ORDER_STATUS_ASSIGNED = new OrderStatus("ASSIGNED", "ASSIGNED", true);
    public static final OrderStatus ORDER_STATUS_AUTO_ASSIGNED = new OrderStatus("AUTO_ASSIGNED", "AUTO_ASSIGNED", true);
    public static final OrderStatus ORDER_STATUS_IN_PROGRESS = new OrderStatus("IN_PROGRESS", "IN_PROGRESS", true);
    public static final OrderStatus ORDER_STATUS_IN_CART = new OrderStatus("IN_PROCESS", "IN_PROCESS", true);

    public static final OrderStatus ORDER_STATUS_UNASSIGNED = new OrderStatus("UNASSIGNED", "UNASSIGNED", true);
    public static final CustomOrderState ORDER_STATE_COMPLETED = new CustomOrderState(7);
    public static final CustomOrderState ORDER_STATE_CREATED= new CustomOrderState(0);
    public static final CustomOrderState ORDER_STATE_FAILED = new CustomOrderState(999);
    public static final CustomOrderState ORDER_STATE_NEW = new CustomOrderState(1);
    public static final CustomOrderState ORDER_STATE_IN_REVIEW = new CustomOrderState(8);
    public static final CustomOrderState ORDER_STATE_ASSIGNED = new CustomOrderState(4);
    public static final CustomOrderState ORDER_STATE_CANCELLED = new CustomOrderState(9);
    public static final CustomOrderState ORDER_STATE_REFUND_SUCCESS = new CustomOrderState(10);
    public static final CustomOrderState ORDER_STATE_REFUND_FAIL = new CustomOrderState(11);
    public static final Long TICKET_STATE_RETURNED = 6L;
    public static final Long TICKET_STATE_ON_HOLD = 3L;
    public static final Long TICKET_STATE_CLOSE = 5L;
    public static final Long TICKET_STATE_SUPPORT = 7L;
    public static final Long TICKET_STATUS_BDWL = 12L;
    public static final Long TICKET_STATUS_OTHER = 13L;

    public static final Long OTHERS_GENDER_ID = 3L;
    public static final Long OTHERS_CATEGORY_ID = 6L;
    public static final Integer OTHERS_QUALIFICATION_ID= 60;
    public static final Integer BACHELORS_QUALIFICATION= 3;
    public static final Integer MASTERS_QUALIFICATION= 4;
    public static final Integer MATRICULATION_QUALIFICATION= 1;
    public static final Long MATRICULATION_IMPLICIT_STREAM_ID= 0L;
    public static final Long OTHERS_SUBJECT_ID = 54L;
    public static final Long OTHERS_STREAM_ID = 215L;


    public static final CustomOrderState ORDER_STATE_AUTO_ASSIGNED = new CustomOrderState(2);
    public static final CustomOrderState ORDER_STATE_IN_PROGRESS = new CustomOrderState(6);
    public static final CustomOrderState ORDER_STATE_UNASSIGNED = new CustomOrderState(3);
    public static final CustomOrderState ORDER_STATE_RETURNED = new CustomOrderState(5);
    public static HttpServletRequest request=null;


    public static final String GET_ALL_ORDER_STATE = "SELECT c FROM OrderStateRef c";
    public static final String GET_ORDER_STATE_BY_ORDER_STATE_ID = "SELECT c FROM OrderStateRef c WHERE c.orderStateId = :orderStateId";
    public static final String GET_ORDER_STATE_BY_ORDER_STATE_NAME = "SELECT c FROM OrderStateRef c WHERE c.orderStateName = :orderStateName";
    public static final String GET_ORDERS_BY_ORDER_STATE_ID = "SELECT c FROM CustomOrderState c WHERE c.orderStateId = :orderStateId";
    public static final String GET_ORDERS_BY_ORDER_ID = "SELECT c FROM CustomOrderState c WHERE c.orderId = :orderId";

    public static final String GET_CUSTOM_SERVICE_PROVIDER_TICKET_BY_TICKET_ID = "SELECT c FROM CustomServiceProviderTicket c WHERE c.ticketId = :ticketId";
    public static final String GET_CUSTOM_SERVICE_PROVIDER_TICKET_BY_ORDER_ID = "SELECT c FROM CustomServiceProviderTicket c WHERE c.order = :orderId";
    public static final String GET_CUSTOM_SERVICE_PROVIDER_TICKET_BY_PARENT_TICKET_ID = "SELECT c FROM CustomServiceProviderTicket c WHERE c.parentTicket = :parentTicketId";
    public static final String GET_PRIMARY_TICKET="SELECT c.ticket_id from custom_service_provider_ticket c where c.order_id =:orderId and c.ticket_type_id = 1";
    public static final String GET_TICKET_STATUS_LINKED_WITH_TICKET_STATE="SELECT c.ticket_status_id from order_ticket_linkage c WHERE c.ticket_state_id =:ticketStateId AND c.ticket_type_id = :ticketTypeId";
    public static final String GET_TICKET_STATE_LINKED_WITH_TICKET_STATE = "SELECT t.ticket_state_id_to from ticket_state_linkage t WHERE t.ticket_state_id_from = :ticketStateIdFrom AND t.role_id IN :roleIds AND t.ticket_type_id = :ticketTypeId";
    public static final String GET_ORDER_STATE_LINKED_WITH_TICKET="SELECT c.order_state_id from order_ticket_linkage c WHERE c.ticket_state_id =:ticketStateId";
    public static final String BEARER_CONST= "Bearer ";
    public static final String FETCH_DOCUMENT_TO_ARCHIVE = "UPDATE %s SET archived = true WHERE %s = :userId AND document_type_id = :documentTypeId AND archived = false";
    public static final String FETCH_DOCUMENT_TO_ARCHIVE_FOR_QUALIFICATION = "UPDATE %s SET archived = true WHERE %s = :userId AND document_type_id = :documentTypeId AND archived = false AND qualification_detail_id = :qualificationDetailId";
    public static final Long TICKET_STATE_IN_REVIEW=4L;
    public static final Long TICKET_STATE_TO_DO=1L;
    public static final Long TICKET_STATE_IN_PROGRESS=2L;
    public static final Long TICKET_STATUS_IN_REVIEW_HELP=11L;

    public static final String FETCH_DOCUMENT_TO_ARCHIVE_ID = "Select documentid FROM %s WHERE %s = :userId AND document_type_id = :documentTypeId AND archived = false";
    public static final String FETCH_DOCUMENT_TO_ARCHIVE_ID_FOR_QUALIFICATION = "Select documentid FROM %s WHERE %s = :userId AND document_type_id = :documentTypeId AND archived = false AND qualification_detail_id = :qualificationDetailId";
    public static final String GET_TICKET_HISTORY_BY_TICKET_ID = "SELECT * FROM custom_ticket_history WHERE ticket_id = :ticketId ORDER BY modified_date DESC";
    public static final String GET_DIVISION_BY_ZONE="SELECT c.division_id from zone_divisions c join custom_state_codes s on c.division_id = s.state_id where c.archived=false and c.zone_id =:zoneId and s.archived = false Order by division_id ASC";
    public static final String GET_ALL_ZONES="SELECT z FROM Zone z where archived = :archived";
    public static final String GET_ZONE_LINKED_TO_DIVISION="SELECT z.zone_id from zone_divisions z where z.division_id =:divisionId";
    public static final String NO_CATEGORY="N/A";
    public static final String NO_GENDER="N/A";

    //public static final String CUSTOMER_FILTER_REFERRER= "SELECT DISTINCT cust.customer_id FROM customer_referrer referrer JOIN blc_customer cust ON referrer.customer_id = cust.customer_id JOIN blc_customer_address cust_addr ON referrer.customer_id = cust_addr.customer_id JOIN blc_address addr ON cust_addr.address_id = addr.address_id JOIN qualification_details qual_details ON qual_details.custom_customer_id = cust.customer_id JOIN qualification qual ON qual_details.qualification_id = qual.qualification_id AND qual_details.date_of_passing = (SELECT MAX(date_of_passing) FROM qualification_details WHERE custom_customer_id = cust.customer_id) WHERE addr.country ='ADD-P'";
    public static final String ACTION_ACTIVATE="activate";
    public static final String ACTION_REJECT="reject";
    public static final String ACTION_SUSPEND="suspend";
    public static final String ACTION_APPROVE="approve";
    public static final String CUSTOMER_FILTER= "SELECT cust.customer_id FROM blc_customer cust ";
    public static final String WELCOME_BODY_TEMPLATE = "Hello, %s!\n" +
            "Welcome to Application Marketplace. Explore forms and get them filled effortlessly.\n\n" +
            "Best regards,\n" +
            "Admin";
    public static final String WELCOME_SUBJECT="System Message";
    public static final Integer SUPER_ADMIN_PRIVILEGES=4;

    public static final String GET_ORDER_TICKET_LINKAGE_BY_TICKET_STATE_AND_TICKET_STATUS = "SELECT c FROM OrderTicketLinkage c WHERE c.ticketStateId = :ticketStateId AND c.ticketStatusId = :ticketStatusId AND c.ticketTypeId = :ticketTypeId";
    public static final String GET_TICKET_STATE_LINKAGE_BY_TICKET_TYPE_AND_TICKET_FROM_AND_TICKET = "SELECT t FROM TicketStateLinkage t WHERE t.ticketStateIdFrom = :ticketStateIdFrom AND t.ticketStateIdTo = :ticketStateIdTo AND t.ticketTypeId = :ticketTypeId AND t.roleId IN (:roleId)";

    public static final String GET_ALL_WORK_QUALITY = "SELECT c FROM CustomWorkQuality c";
    public static final String GET_TICKET_TYPE_BY_WORK_QUALITY_ID = "SELECT c FROM CustomWorkQuality c WHERE c.workQualityId = :workQualityId";

    public static final Long TICKET_TYPE_ID_OF_PRIMARY_TICKET = 1L;
    public static final Long TICKET_TYPE_ID_OF_REVIEW_TICKET = 2L;
    public static final Long TICKET_TYPE_ID_OF_MISCELLANEOUS_TICKET = 3L;

    public static final Integer DOCUMENT_TYPE_OTHER_ID = 13;
    public static final Integer DOCUMENT_TYPE_LIVE_PHOTOGRAPH_ID = 3;
    public static final Integer DOCUMENT_TYPE_MARK_SHEET_ID = 12;
    public static final Integer DOCUMENT_TYPE_TICKET_DOCUMENT_ID = 32;
    public static final Integer DOCUMENT_TYPE_C_FORM = 14;
    public static final String GET_DOCUMENT_TYPE_BY_DOCUMENT_TYPE_ID = "SELECT dt FROM DocumentType dt WHERE dt.document_type_id = :documentTypeId";

    public static final String GET_QUALIFICATION_DETAIL_DOCUMENT_DATA_OF_CUSTOMER = "SELECT d FROM Document d WHERE d.custom_customer = :customCustomer AND d.documentType = :documentType AND (d.qualificationDetails.qualification_detail_id = :qualificationDetailId ) AND d.name IS NOT NULL";
    public static final String GET_DOCUMENT_DATA_OF_CUSTOMER_BY_DOCUMENT_TYPE_ID = "SELECT d FROM Document d WHERE d.custom_customer = :customCustomer AND d.documentType = :documentType AND d.name IS NOT NULL ";

    public static final String GET_OTHER_DOCUMENT_DATA_OF_SERVICE_PROVIDER_BY_DOCUMENT_TYPE_ID = "SELECT d FROM ServiceProviderDocument d WHERE d.serviceProviderEntity = :serviceProviderEntity AND d.documentType = :documentType AND (:otherDocument IS NULL OR LOWER(d.otherDocument) = LOWER(:otherDocument)) AND d.name = :documentName AND d.name IS NOT NULL";
    public static final String GET_DOCUMENT_DATA_OF_SERVICE_PROVIDER_BY_DOCUMENT_TYPE_ID = "SELECT d FROM ServiceProviderDocument d WHERE d.serviceProviderEntity = :serviceProviderEntity AND d.documentType = :documentType AND d.name IS NOT NULL";

    public static final String GET_DOCUMENT_DATA_OF_SERVICE_PROVIDER_BY_DOCUMENT_TYPE_ID_AND_TICKET = "SELECT d FROM ServiceProviderDocument d WHERE d.serviceProviderEntity = :serviceProviderEntity AND d.documentType = :documentType AND d.serviceProviderTicket = :serviceProviderTicket AND d.name IS NOT NULL";


    public static final String GET_SERVICE_PROVIDER_CONDITION_ADMIN_OVERRIDDEN = "SELECT sp FROM ServiceProviderEntity sp WHERE sp.adminOverridden = :adminOverridden AND sp.type IS NOT NULL";

    public static final String SERVICE_PROVIDER_PROFESSIONAL = "PROFESSIONAL";
    public static final String SERVICE_PROVIDER_INDIVIDUAL = "INDIVIDUAL";

    public static final Long PROFESSIONAL_SERVICE_PROVIDER_NEW_LIMIT = 10L;
    public static final Long INDIVIDUAL_SERVICE_PROVIDER_NEW_LIMIT = 4L;


    public static final Long REVIEW_TICKET_STATUS_SUCCESS = 5L;
    public static final Long REVIEW_TICKET_STATUS_FAIL = -5L;

    public static final Long TIME_COMPLETION_SUCCESS = 5L;
    public static final Long TIME_COMPLETION_FAIL = -5L;

    public static final Long REVIEW_TICKET_FEEDBACK_HIGH = 5L;
    public static final Long REVIEW_TICKET_FEEDBACK_LOW = -5L;


    public static final Long GENDER_ALL=4L;
    public static final Long RESERVED_CATEGORY_ALL=7L;


    //add constants above this query//*******************************************************************************
    public static final String recosQuery =
            "WITH overlappings AS (" +
                    "    SELECT qualification_id " +
                    "    FROM qualification " +
                    "    WHERE qualification_id IN (:qualificationIds) " +
                    "), " +
                    "customer_qualifications AS (" +
                    "    SELECT " +
                    "        qd.qualification_id, " +
                    "        qd.cumulative_percentage_value, " +
                    "        qd.grade_value, " +
                    "        qd.division_value, " +
                    "        qd.is_division, " +
                    "        qd.is_grade " +
                    "    FROM qualification_details qd " +
                    "    WHERE qd.custom_customer_id = :customerId " +
                    "), " +
                    "products_with_requirements AS (" +
                    "    SELECT DISTINCT p.product_id " +
                    "    FROM custom_product p " +
                    "    JOIN post_details post ON post.product_id = p.product_id " +
                    "    JOIN blc_product bp ON bp.product_id = p.product_id " +
                    "    JOIN blc_sku sku ON sku.sku_id = bp.default_sku_id " +
                    "    JOIN custom_product_reserve_category_fee_post_reference fee " +
                    "        ON p.product_id = fee.product_id " +
                    "        AND (fee.reserve_category_id = :reserveCategoryId OR fee.reserve_category_id = 7) " +
                    "    LEFT JOIN custom_product_reserve_category_born_before_after_reference rf " +
                    "        ON post.postid = rf.post_id " +
                    "    LEFT JOIN qualification_eligibility qf " +
                    "        ON qf.post_id = post.postid " +
                    "    LEFT JOIN qualification_eligibility_qualifications qd " +
                    "        ON qf.qualification_eligibility_id = qd.qualification_eligibility_id " +
                    "    LEFT JOIN customer_qualifications cq " +
                    "        ON qd.qualification_id = cq.qualification_id " +
                    "    WHERE (sku.active_end_date >= CURRENT_TIMESTAMP) " +
                    "    AND p.soft_delete = 'N' " +
                    "    AND (" +
                    "        (qd.qualification_id IS NULL) " +
                    "        OR (qd.qualification_id IN (:qualificationIds)) " +
                    "        OR (qd.qualification_id IN (SELECT qualification_id FROM overlappings)) " +
                    "    ) " +
                    "    AND (" +
                    "        (rf.gender_id IS NULL OR rf.gender_id IN (:genderId, 4)) " +
                    "    ) " +
                    "    AND (" +
                    "        (rf.minimum_age IS NULL AND rf.maximum_age IS NULL) " +
                    "        OR (:age BETWEEN rf.minimum_age AND rf.maximum_age) " +
                    "    ) " +
                    "    AND (" +
                    "        (qf.is_appearing = true) " +
                    "        OR (qf.percentage IS NULL AND qf.cgpa IS NULL) " +
                    "        OR (" +
                    "            qf.is_percentage = true AND " +
                    "            qf.percentage IS NOT NULL AND " +
                    "            (cq.cumulative_percentage_value IS NOT NULL AND cq.cumulative_percentage_value >= qf.percentage) " +
                    "        ) " +
                    "        OR (" +
                    "            qf.is_percentage = false AND " +
                    "            qf.cgpa IS NOT NULL AND " +
                    "            (cq.cumulative_percentage_value IS NOT NULL AND cq.cumulative_percentage_value >= qf.cgpa) " +
                    "        ) " +
                    "    ) " +
                    "), " +
                    "products_without_requirements AS (" +
                    "    SELECT p.product_id " +
                    "    FROM custom_product p " +
                    "    JOIN blc_product bp ON bp.product_id = p.product_id " +
                    "    JOIN blc_sku sku ON sku.sku_id = bp.default_sku_id " +
                    "    JOIN custom_product_reserve_category_fee_post_reference fee " +
                    "        ON p.product_id = fee.product_id " +
                    "        AND (fee.reserve_category_id = :reserveCategoryId OR fee.reserve_category_id = 7) " +
                    "    WHERE sku.active_end_date >= CURRENT_TIMESTAMP " +
                    "    AND p.soft_delete = 'N' " +
                    "    AND NOT EXISTS (" +
                    "        SELECT 1 FROM post_details pd " +
                    "        JOIN custom_product_reserve_category_born_before_after_reference rf ON pd.postid = rf.post_id " +
                    "        WHERE pd.product_id = p.product_id " +
                    "    ) " +
                    "    AND NOT EXISTS (" +
                    "        SELECT 1 FROM post_details pd " +
                    "        JOIN qualification_eligibility qe ON pd.postid = qe.post_id " +
                    "        WHERE pd.product_id = p.product_id " +
                    "    ) " +
                    "), " +
                    "all_products AS (" +
                    "    SELECT product_id FROM products_with_requirements " +
                    "    UNION " +
                    "    SELECT product_id FROM products_without_requirements " +
                    "), " +
                    "numbered_products AS (" +
                    "    SELECT " +
                    "        product_id, " +
                    "        ROW_NUMBER() OVER (ORDER BY product_id DESC) as row_num " +
                    "    FROM all_products " +
                    ") " +
                    "SELECT product_id " +
                    "FROM numbered_products " +
                    "WHERE row_num > :offset AND row_num <= (:offset + :limit) " +
                    "ORDER BY row_num";

    public static final String recosCount =
            "WITH overlappings AS (" +
                    "    SELECT qualification_id " +
                    "    FROM qualification " +
                    "    WHERE qualification_id IN (:qualificationIds) " +
                    "), " +
                    "customer_qualifications AS (" +
                    "    SELECT " +
                    "        qd.qualification_id, " +
                    "        qd.cumulative_percentage_value, " +
                    "        qd.grade_value, " +
                    "        qd.division_value, " +
                    "        qd.is_division, " +
                    "        qd.is_grade " +
                    "    FROM qualification_details qd " +
                    "    WHERE qd.custom_customer_id = :customerId " +
                    "), " +
                    "products_with_requirements AS (" +
                    "    SELECT p.product_id " +
                    "    FROM custom_product p " +
                    "    JOIN post_details post ON post.product_id = p.product_id " +
                    "    JOIN blc_product bp ON bp.product_id = p.product_id " +
                    "    JOIN blc_sku sku ON sku.sku_id = bp.default_sku_id " +
                    "    JOIN custom_product_reserve_category_fee_post_reference fee " +
                    "        ON p.product_id = fee.product_id " +
                    "        AND (fee.reserve_category_id = :reserveCategoryId OR fee.reserve_category_id = 7) " +
                    "    LEFT JOIN custom_product_reserve_category_born_before_after_reference rf " +
                    "        ON post.postid = rf.post_id " +
                    "    LEFT JOIN qualification_eligibility qf " +
                    "        ON qf.post_id = post.postid " +
                    "    LEFT JOIN qualification_eligibility_qualifications qd " +
                    "        ON qf.qualification_eligibility_id = qd.qualification_eligibility_id " +
                    "    LEFT JOIN customer_qualifications cq " +
                    "        ON qd.qualification_id = cq.qualification_id " +
                    "    WHERE (sku.active_end_date >= CURRENT_TIMESTAMP) " +
                    "    AND p.soft_delete = 'N' " +
                    "    AND (" +
                    "        (qd.qualification_id IS NULL) " +
                    "        OR (qd.qualification_id IN (:qualificationIds)) " +
                    "        OR (qd.qualification_id IN (SELECT qualification_id FROM overlappings)) " +
                    "    ) " +
                    "    AND (" +
                    "        (rf.gender_id IS NULL OR rf.gender_id IN (:genderId, 4)) " +
                    "    ) " +
                    "    AND (" +
                    "        (rf.minimum_age IS NULL AND rf.maximum_age IS NULL) " +
                    "        OR (:age BETWEEN rf.minimum_age AND rf.maximum_age) " +
                    "    ) " +
                    "    AND (" +
                    "        (qf.is_appearing = true) " +
                    "        OR (qf.percentage IS NULL AND qf.cgpa IS NULL) " +
                    "        OR (" +
                    "            qf.is_percentage = true AND " +
                    "            qf.percentage IS NOT NULL AND " +
                    "            (cq.cumulative_percentage_value IS NOT NULL AND cq.cumulative_percentage_value >= qf.percentage) " +
                    "        ) " +
                    "        OR (" +
                    "            qf.is_percentage = false AND " +
                    "            qf.cgpa IS NOT NULL AND " +
                    "            (cq.cumulative_percentage_value IS NOT NULL AND cq.cumulative_percentage_value >= qf.cgpa) " +
                    "        ) " +
                    "    ) " +
                    "), " +
                    "products_without_requirements AS (" +
                    "    SELECT p.product_id " +
                    "    FROM custom_product p " +
                    "    JOIN blc_product bp ON bp.product_id = p.product_id " +
                    "    JOIN blc_sku sku ON sku.sku_id = bp.default_sku_id " +
                    "    JOIN custom_product_reserve_category_fee_post_reference fee " +
                    "        ON p.product_id = fee.product_id " +
                    "        AND (fee.reserve_category_id = :reserveCategoryId OR fee.reserve_category_id = 7) " +
                    "    WHERE sku.active_end_date >= CURRENT_TIMESTAMP " +
                    "    AND p.soft_delete = 'N' " +
                    "    AND NOT EXISTS (" +
                    "        SELECT 1 FROM post_details pd " +
                    "        JOIN custom_product_reserve_category_born_before_after_reference rf ON pd.postid = rf.post_id " +
                    "        WHERE pd.product_id = p.product_id " +
                    "    ) " +
                    "    AND NOT EXISTS (" +
                    "        SELECT 1 FROM post_details pd " +
                    "        JOIN qualification_eligibility qe ON pd.postid = qe.post_id " +
                    "        WHERE pd.product_id = p.product_id " +
                    "    ) " +
                    "), " +
                    "all_products AS (" +
                    "    SELECT product_id FROM products_with_requirements " +
                    "    UNION " +  // UNION removes duplicates between the two sets
                    "    SELECT product_id FROM products_without_requirements " +
                    ") " +
                    "SELECT COUNT(*) " +
                    "FROM all_products";


    public   static String KEY="2025202220202512";
    //add constants above this query//*******************************************************************************

}
