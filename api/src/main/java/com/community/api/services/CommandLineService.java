package com.community.api.services;

import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomJobGroup;
import com.community.api.entity.CustomProductState;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketType;
import com.community.api.entity.Districts;
import com.community.api.entity.Qualification;
import com.community.api.entity.Role;
import com.community.api.entity.ServiceProviderAddressRef;
import com.community.api.entity.ServiceProviderInfra;
import com.community.api.entity.ServiceProviderLanguage;
import com.community.api.entity.Skill;
import com.community.api.entity.StateCode;
import com.community.api.entity.*;
import com.community.api.utils.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.management.Query;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.community.api.component.Constant.MOBILE_NUMBER_CONSTRAINT_IN_CUSTOM_ADMIN;
import static com.community.api.component.Constant.PASSWORD_CONSTRAINT_IN_CUSTOM_ADMIN;
import static org.broadleafcommerce.common.util.sql.importsql.DemoSqlServerSingleLineSqlCommandExtractor.CURRENT_TIMESTAMP;

@Component
public class CommandLineService implements CommandLineRunner {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ZoneDivisionService zoneDivisionService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        zoneDivisionService.populateZoneDivision();
        // Check if data already exists to avoid duplication
        if (entityManager.createQuery("SELECT COUNT(c) FROM CustomProductState c", Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomProductState(1L, "NEW", "New State."));
            entityManager.persist(new CustomProductState(2L, "MODIFIED", "Modified State."));
            entityManager.persist(new CustomProductState(3L, "APPROVED", "Approved State."));
            entityManager.persist(new CustomProductState(4L, "REJECTED", "Rejected State."));
            entityManager.persist(new CustomProductState(5L, "LIVE", "Live State."));
            entityManager.persist(new CustomProductState(6L, "EXPIRED", "Expired State."));
            entityManager.persist(new CustomProductState(7L, "DRAFT", "Draft State."));
        }

        if (entityManager.createQuery("SELECT COUNT(c) FROM CustomTicketState c", Long.class).getSingleResult() == 0) {
            entityManager.merge(new CustomTicketState(1L, "TO-DO", "Ticket is not assigned to any service provider"));
            entityManager.merge(new CustomTicketState(2L, "IN-PROGRESS", "It's under progress"));
            entityManager.merge(new CustomTicketState(3L, "ON-HOLD", "It's on hold"));
            entityManager.merge(new CustomTicketState(4L, "IN-REVIEW", "It's rejected"));
            entityManager.merge(new CustomTicketState(5L, "CLOSE", "Closed successfully"));
        }

        if (entityManager.createQuery("SELECT COUNT(c) FROM CustomTicketStatus c", Long.class).getSingleResult() == 0) {
            entityManager.merge(new CustomTicketStatus(1L, "NOT-REACHABLE", "User is unreachable"));
            entityManager.merge(new CustomTicketStatus(2L, "VALIDATING-DOCUMENT", "Validating documents"));
            entityManager.merge(new CustomTicketStatus(3L, "MISSING-DOCUMENT", "Missing documents"));
            entityManager.merge(new CustomTicketStatus(4L, "USER-NOT-REACHABLE", "User Not reachable"));
            entityManager.merge(new CustomTicketStatus(5L, "UPLOADING-DOCUMENT", "Uploading documents"));
            entityManager.merge(new CustomTicketStatus(6L, "FILLING-PERSONAL-DETAILS", "Filling personal details"));
            entityManager.merge(new CustomTicketStatus(7L, "SOME-OTHER-STATUS", "Some other status"));
        }

        if (entityManager.createQuery("SELECT COUNT(c) FROM CustomTicketType c", Long.class).getSingleResult() == 0) {
            entityManager.merge(new CustomTicketType(1L, "PRIMARY", "Primary ticket of SP"));
            entityManager.merge(new CustomTicketType(2L, "REVIEW-TICKET", "Review ticket of SP"));
            entityManager.merge(new CustomTicketType(3L, "MISCELLANEOUS", "Miscellaneous (any other ticket)"));
        }

        if (entityManager.createQuery("SELECT COUNT(o) FROM OrderStateRef o", Long.class).getSingleResult() == 0) {
            entityManager.persist(new OrderStateRef(1, "NEW", "Order is generated"));
            entityManager.persist(new OrderStateRef(2, "AUTO_ASSIGNED", "Order automatically assigned."));
            entityManager.persist(new OrderStateRef(3, "UNASSIGNED", "Order is unassigned."));
            entityManager.persist(new OrderStateRef(4, "ASSIGNED", "Order assigned."));
            entityManager.persist(new OrderStateRef(5, "RETURNED", "Order returned."));
            entityManager.persist(new OrderStateRef(6, "IN_PROGRESS", "Order is in progress."));
            entityManager.persist(new OrderStateRef(7, "COMPLETED", "Order completed."));
            entityManager.persist(new OrderStateRef(8, "IN_REVIEW", "Order is in review."));
        }
        if (entityManager.createQuery("SELECT COUNT(o) FROM OrderTicketLinkage o", Long.class).getSingleResult() == 0) {
            entityManager.persist((new OrderTicketLinkage(1,1,null,null)));
            entityManager.persist((new OrderTicketLinkage(2,3,1L,null)));
            entityManager.persist((new OrderTicketLinkage(3,4,1L,null)));
            entityManager.persist((new OrderTicketLinkage(4,6,2L,2L)));
            entityManager.persist((new OrderTicketLinkage(5,6,2L,5L)));
            entityManager.persist((new OrderTicketLinkage(6,6,3L,3L)));
            entityManager.persist((new OrderTicketLinkage(7,6,3L,4L)));
            entityManager.persist((new OrderTicketLinkage(8,6,4L,6L)));
            entityManager.persist((new OrderTicketLinkage(9,7,5L,null)));
        }
        if(entityManager.createQuery("SELECT COUNT(c) FROM CustomOrderStatus c",Long.class).getSingleResult()==0)
        {
            // AUTO_ASSIGNED (ID 1)
            entityManager.persist(new CustomOrderStatus(1, "AUTO_ASSIGNED", 2, "Order automatically assigned."));
            // UNASSIGNED (ID 2)
            entityManager.persist(new CustomOrderStatus(2, "UNASSIGNED", 3, "Order is unassigned."));
            // ASSIGNED (ID 3)
            entityManager.persist(new CustomOrderStatus(3, "ASSIGNED_BY_SUPER_ADMIN", 4, "Order assigned by super admin."));
            entityManager.persist(new CustomOrderStatus(4, "ASSIGNED_BY_AUTO_ASSIGNER", 4, "Order assigned by Auto Assigner."));
            // RETURNED (ID 4)
            entityManager.persist(new CustomOrderStatus(5, "CANNOT_BE_DONE", 5, "Order cannot be done."));
            entityManager.persist(new CustomOrderStatus(6, "DUPLICATE_ORDER", 5, "Order is a duplicate."));
            // IN_PROGRESS (ID 5)
            entityManager.persist(new CustomOrderStatus(7, "IN_PROGRESS", 6, "Order is in progress."));
            // COMPLETED (ID 6)
            entityManager.persist(new CustomOrderStatus(8, "FULFILLED", 7, "Order fulfilled."));
            entityManager.persist(new CustomOrderStatus(9, "DUPLICATE", 7, "Order duplicate."));
            entityManager.persist(new CustomOrderStatus(10, "DUMMY_ORDER", 7, "Order not valid or created as a test."));
            entityManager.persist(new CustomOrderStatus(11, "STUDENT_UNREACHABLE", 7, "Order could not be completed because the student/customer was not reachable."));
            entityManager.persist(new CustomOrderStatus(12, "DOCUMENT_NOT_AVAILABLE", 7, "Necessary document to complete the order was unavailable."));
            entityManager.persist(new CustomOrderStatus(13, "NEW_ORDER", 1, "New Order Generated"));
        }

        if(entityManager.createQuery("SELECT COUNT(c) FROM CustomOrderStatus c",Long.class).getSingleResult()==0)
        {
            // AUTO_ASSIGNED (ID 1)
            entityManager.persist(new CustomOrderStatus(1, "AUTO_ASSIGNED", 1, "Order automatically assigned."));
            // UNASSIGNED (ID 2)
            entityManager.persist(new CustomOrderStatus(2, "UNASSIGNED", 2, "Order is unassigned."));
            // ASSIGNED (ID 3)
            entityManager.persist(new CustomOrderStatus(3, "ASSIGNED_BY_SUPER_ADMIN", 3, "Order assigned by super admin."));
            entityManager.persist(new CustomOrderStatus(4, "ASSIGNED_BY_AUTO_ASSIGNER", 3, "Order assigned by Auto Assigner."));
            // RETURNED (ID 4)
            entityManager.persist(new CustomOrderStatus(5, "CANNOT_BE_DONE", 4, "Order cannot be done."));
            entityManager.persist(new CustomOrderStatus(6, "DUPLICATE_ORDER", 4, "Order is a duplicate."));
            // IN_PROGRESS (ID 5)
            entityManager.persist(new CustomOrderStatus(7, "IN_PROGRESS", 5, "Order is in progress."));
            // COMPLETED (ID 6)
            entityManager.persist(new CustomOrderStatus(8, "FULFILLED", 6, "Order fulfilled."));
            entityManager.persist(new CustomOrderStatus(9, "DUPLICATE", 6, "Order duplicate."));
            entityManager.persist(new CustomOrderStatus(10, "DUMMY_ORDER", 6, "Order not valid or created as a test."));
            entityManager.persist(new CustomOrderStatus(11, "STUDENT_UNREACHABLE", 6, "Order could not be completed because the student/customer was not reachable."));
            entityManager.persist(new CustomOrderStatus(12, "DOCUMENT_NOT_AVAILABLE", 6, "Necessary document to complete the order was unavailable."));
        }
        if(entityManager.createQuery("SELECT COUNT(c) FROM CustomJobGroup c", Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomJobGroup(1L, 'A', "Executive Management"));
            entityManager.persist(new CustomJobGroup(2L, 'B', "Professional and Technical"));
            entityManager.persist(new CustomJobGroup(3L, 'C', "Administrative and Support"));
            entityManager.persist(new CustomJobGroup(4L, 'D', "Entry-Level and Labor"));
        }

        if (entityManager.createQuery("SELECT COUNT(c) FROM CustomApplicationScope c", Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomApplicationScope(1L, "STATE", "State level operations."));
            entityManager.persist(new CustomApplicationScope(2L, "CENTER", "Center level operations."));
        }

        if (entityManager.createQuery("SELECT COUNT(c) FROM CustomReserveCategory c", Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomReserveCategory(1L, "GEN", "General", true));
            entityManager.persist(new CustomReserveCategory(2L, "SC", "Schedule Caste", false));
            entityManager.persist(new CustomReserveCategory(3L, "ST", "Schedule Tribe", false));
            entityManager.persist(new CustomReserveCategory(4L, "OBC", "Other Backward Caste", false));
            entityManager.persist(new CustomReserveCategory(5L, "OTHERS", "Others", false));
            entityManager.persist(new CustomReserveCategory(6L, "EWS", "Economically Weaker Section", false));
        }

        if(entityManager.createQuery("SELECT COUNT(c) FROM CustomProductRejectionStatus c", Long.class).getSingleResult() == 0) {
            entityManager.merge(new CustomProductRejectionStatus(1L, "TO-BE-MODIFIED", "Product needs modification to get approved."));
            entityManager.merge(new CustomProductRejectionStatus(2L, "DUPLICATE", "There is already a product present with these details."));
            entityManager.merge(new CustomProductRejectionStatus(3L, "IRRELEVANT", "The product is irrelevant."));
            entityManager.merge(new CustomProductRejectionStatus(4L, "UNFEASIBLE", "The product is not feasible to exists."));
        }

        if(entityManager.createQuery("SELECT COUNT(c) FROM CustomGender c", Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomGender(1L, 'M', "MALE"));
            entityManager.persist(new CustomGender(2L, 'F', "FEMALE"));
            entityManager.persist(new CustomGender(3L, 'O', "OTHERS"));
        }

        if(entityManager.createQuery("SELECT COUNT(c) FROM CustomSector c", Long.class).getSingleResult() == 0) {
            entityManager.merge(new CustomSector(1L, "HEALTHCARE", "Forms related to patient care and medical services."));
            entityManager.merge(new CustomSector(2L, "EDUCATION", "Forms for student enrollment and academic records."));
            entityManager.merge(new CustomSector(3L, "FINANCE", "Forms for loans, taxes, and financial services."));
            entityManager.merge(new CustomSector(4L, "GOVERNMENT", "Forms for taxes and civic registration."));
            entityManager.merge(new CustomSector(5L, "HUMAN_RESOURCES", "Forms for job applications and employee management."));
            entityManager.merge(new CustomSector(6L, "REAL_ESTATE", "Forms for property transactions and leases."));
            entityManager.merge(new CustomSector(7L, "INSURANCE", "Forms for claims and policy management."));
            entityManager.merge(new CustomSector(8L, "RETAIL", "Forms for customer feedback and warranties."));
            entityManager.merge(new CustomSector(9L, "TRANSPORTATION", "Forms for shipping and travel documentation."));
            entityManager.merge(new CustomSector(10L, "LEGAL", "Forms for legal processes and documentation."));
        }

        if(entityManager.createQuery("SELECT COUNT(c) FROM CustomStream c", Long.class).getSingleResult() == 0) {
            entityManager.merge(new CustomStream(1L, 'N', "SCIENCE", "Description of Science", new Date(), null, null));
            entityManager.merge(new CustomStream(2L, 'N', "ARTS", "Description of Arts", new Date(), null, null));
            entityManager.merge(new CustomStream(3L, 'N', "COMMERCE", "Description of Commerce", new Date(), null, null));
            entityManager.merge(new CustomStream(4L, 'N', "ENGINEERING", "Description of Engineering", new Date(), null, null));
            entityManager.merge(new CustomStream(5L, 'N', "MEDICINE", "Description of Medicine", new Date(), null, null));
            entityManager.merge(new CustomStream(6L, 'N', "HUMANITIES", "Description of Humanities", new Date(), null, null));
            entityManager.merge(new CustomStream(7L, 'N', "SOCIAL SCIENCES", "Description of Social Sciences", new Date(), null, null));
            entityManager.merge(new CustomStream(8L, 'N', "TECHNOLOGY", "Description of Technology", new Date(), null, null));
            entityManager.merge(new CustomStream(9L, 'N', "MATHEMATICS", "Description of Mathematics", new Date(), null, null));
            entityManager.merge(new CustomStream(10L, 'N', "DESIGN", "Description of Design", new Date(), null, null));
        }

        if(entityManager.createQuery("SELECT COUNT(s) FROM CustomSubject s", Long.class).getSingleResult() == 0) {
            entityManager.merge(new CustomSubject(1L, 'N', "Mathematics", "Description of Mathematics", new Date(), null, null));
            entityManager.merge(new CustomSubject(2L, 'N', "Physics", "Description of Physics", new Date(), null, null));
            entityManager.merge(new CustomSubject(3L, 'N', "Chemistry", "Description of Chemistry", new Date(), null, null));
            entityManager.merge(new CustomSubject(4L, 'N', "Biology", "Description of Biology", new Date(), null, null));
            entityManager.merge(new CustomSubject(5L, 'N', "English", "Description of English", new Date(), null, null));
            entityManager.merge(new CustomSubject(6L, 'N', "History", "Description of History", new Date(), null, null));
            entityManager.merge(new CustomSubject(7L, 'N', "Geography", "Description of Geography", new Date(), null, null));
            entityManager.merge(new CustomSubject(8L, 'N',"Computer Science", "Description of Computer Science", new Date(), null, null));
            entityManager.merge(new CustomSubject(9L, 'N', "Art", "Description of Art", new Date(), null, null));
            entityManager.merge(new CustomSubject(10L, 'N', "Physical Education", "Description of Physical Education", new Date(), null, null));
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        if (entityManager.createQuery("SELECT COUNT(r) FROM Role r", Long.class).getSingleResult() == 0) {
            entityManager.merge(new Role(1, "SUPER_ADMIN", date.toString(),date.toString(), "SUPER_ADMIN"));
            entityManager.merge(new Role(2, "ADMIN", date.toString(), date.toString(), "SUPER_ADMIN"));
            entityManager.merge(new Role(3, "ADMIN_SERVICE_PROVIDER", date.toString(), date.toString(), "SUPER_ADMIN"));
            entityManager.merge(new Role(4, "SERVICE_PROVIDER", date.toString(), date.toString(), "SUPER_ADMIN"));
            entityManager.merge(new Role(5, "CUSTOMER", date.toString(), date.toString(), "SUPER_ADMIN"));
        }
        Long count = entityManager.createQuery("SELECT COUNT(d) FROM Districts d", Long.class).getSingleResult();

        if (count == 0) {
            // Insert data into the Districts table with explicit ids
            entityManager.persist(new Districts(1, "Bilaspur", "HP"));
            entityManager.persist(new Districts(2, "Chamba", "HP"));
            entityManager.persist(new Districts(3, "Hamirpur", "HP"));
            entityManager.persist(new Districts(4, "Kangra", "HP"));
            entityManager.persist(new Districts(5, "Kinnaur", "HP"));
            entityManager.persist(new Districts(6, "Kullu", "HP"));
            entityManager.persist(new Districts(7, "Lahaul and Spiti", "HP"));
            entityManager.persist(new Districts(8, "Mandi", "HP"));
            entityManager.persist(new Districts(9, "Shimla", "HP"));
            entityManager.persist(new Districts(10, "Sirmaur", "HP"));
            entityManager.persist(new Districts(11, "Solan", "HP"));
            entityManager.persist(new Districts(12, "Una", "HP"));

            // Jammu Division
            entityManager.persist(new Districts(13, "Jammu", "JK"));
            entityManager.persist(new Districts(14, "Samba", "JK"));
            entityManager.persist(new Districts(15, "Kathua", "JK"));
            entityManager.persist(new Districts(16, "Udhampur", "JK"));
            entityManager.persist(new Districts(17, "Reasi", "JK"));
            entityManager.persist(new Districts(18, "Ramban", "JK"));
            entityManager.persist(new Districts(19, "Doda", "JK"));
            entityManager.persist(new Districts(20, "Poonch", "JK"));
            entityManager.persist(new Districts(21, "Rajouri", "JK"));
            entityManager.persist(new Districts(22, "Anantnag", "JK"));
            entityManager.persist(new Districts(23, "Kishtwar", "JK"));

            // Kashmir Division
            entityManager.persist(new Districts(24, "Srinagar", "JK"));
            entityManager.persist(new Districts(25, "Baramulla", "JK"));
            entityManager.persist(new Districts(26, "Pulwama", "JK"));
            entityManager.persist(new Districts(27, "Shopian", "JK"));
            entityManager.persist(new Districts(28, "Anantnag", "JK"));
            entityManager.persist(new Districts(29, "Bandipora", "JK"));
            entityManager.persist(new Districts(30, "Ganderbal", "JK"));
            entityManager.persist(new Districts(31, "Kulgam", "JK"));

            // Punjab
            entityManager.persist(new Districts(32, "Amritsar", "PB"));
            entityManager.persist(new Districts(33, "Barnala", "PB"));
            entityManager.persist(new Districts(34, "Bathinda", "PB"));
            entityManager.persist(new Districts(35, "Faridkot", "PB"));
            entityManager.persist(new Districts(36, "Fatehgarh Sahib", "PB"));
            entityManager.persist(new Districts(37, "Fazilka", "PB"));
            entityManager.persist(new Districts(38, "Ferozepur", "PB"));
            entityManager.persist(new Districts(39, "Gurdaspur", "PB"));
            entityManager.persist(new Districts(40, "Hoshiarpur", "PB"));
            entityManager.persist(new Districts(41, "Jalandhar", "PB"));
            entityManager.persist(new Districts(42, "Kapurthala", "PB"));
            entityManager.persist(new Districts(43, "Ludhiana", "PB"));
            entityManager.persist(new Districts(44, "Mansa", "PB"));
            entityManager.persist(new Districts(45, "Moga", "PB"));
            entityManager.persist(new Districts(46, "Mohali", "PB"));
            entityManager.persist(new Districts(47, "Pathankot", "PB"));
            entityManager.persist(new Districts(48, "Patiala", "PB"));
            entityManager.persist(new Districts(49, "Rupnagar", "PB"));
            entityManager.persist(new Districts(50, "Sangrur", "PB"));
            entityManager.persist(new Districts(51, "Tarn Taran", "PB"));

            // Haryana
            entityManager.persist(new Districts(52, "Ambala", "HR"));
            entityManager.persist(new Districts(53, "Bhiwani", "HR"));
            entityManager.persist(new Districts(54, "Faridabad", "HR"));
            entityManager.persist(new Districts(55, "Fatehabad", "HR"));
            entityManager.persist(new Districts(56, "Gurgaon", "HR"));
            entityManager.persist(new Districts(57, "Hisar", "HR"));
            entityManager.persist(new Districts(58, "Jhajjar", "HR"));
            entityManager.persist(new Districts(59, "Jind", "HR"));
            entityManager.persist(new Districts(60, "Kaithal", "HR"));
            entityManager.persist(new Districts(61, "Karnal", "HR"));
            entityManager.persist(new Districts(62, "Mahendragarh", "HR"));
            entityManager.persist(new Districts(63, "Mewat", "HR"));
            entityManager.persist(new Districts(64, "Palwal", "HR"));
            entityManager.persist(new Districts(65, "Panchkula", "HR"));
            entityManager.persist(new Districts(66, "Panipat", "HR"));
            entityManager.persist(new Districts(67, "Rewari", "HR"));
            entityManager.persist(new Districts(68, "Sirsa", "HR"));
            entityManager.persist(new Districts(69, "Sonipat", "HR"));
            entityManager.persist(new Districts(70, "Yamunanagar", "HR"));
        }

        count = entityManager.createQuery("SELECT COUNT(s) FROM StateCode s", Long.class).getSingleResult();

        if (count == 0) {

            // Insert data into the StateCode table
            entityManager.persist(new StateCode(1, "Andhra Pradesh", "AP"));
            entityManager.persist(new StateCode(2, "Arunachal Pradesh", "AR"));
            entityManager.persist(new StateCode(3, "Assam", "AS"));
            entityManager.persist(new StateCode(4, "Bihar", "BR"));
            entityManager.persist(new StateCode(5, "Chhattisgarh", "CG"));
            entityManager.persist(new StateCode(6, "Goa", "GA"));
            entityManager.persist(new StateCode(7, "Gujarat", "GJ"));
            entityManager.persist(new StateCode(8, "Haryana", "HR"));
            entityManager.persist(new StateCode(9, "Himachal Pradesh", "HP"));
            entityManager.persist(new StateCode(10, "Jharkhand", "JH"));
            entityManager.persist(new StateCode(11, "Karnataka", "KA"));
            entityManager.persist(new StateCode(12, "Kerala", "KL"));
            entityManager.persist(new StateCode(13, "Madhya Pradesh", "MP"));
            entityManager.persist(new StateCode(14, "Maharashtra", "MH"));
            entityManager.persist(new StateCode(15, "Manipur", "MN"));
            entityManager.persist(new StateCode(16, "Meghalaya", "ML"));
            entityManager.persist(new StateCode(17, "Mizoram", "MZ"));
            entityManager.persist(new StateCode(18, "Nagaland", "NL"));
            entityManager.persist(new StateCode(19, "Odisha", "OD"));
            entityManager.persist(new StateCode(20, "Punjab", "PB"));
            entityManager.persist(new StateCode(21, "Rajasthan", "RJ"));
            entityManager.persist(new StateCode(22, "Sikkim", "SK"));
            entityManager.persist(new StateCode(23, "Tamil Nadu", "TN"));
            entityManager.persist(new StateCode(24, "Telangana", "TS"));
            entityManager.persist(new StateCode(25, "Tripura", "TR"));
            entityManager.persist(new StateCode(26, "Uttar Pradesh", "UP"));
            entityManager.persist(new StateCode(27, "Uttarakhand", "UK"));
            entityManager.persist(new StateCode(28, "West Bengal", "WB"));

            // Union Territories
            entityManager.persist(new StateCode(30, "Andaman and Nicobar Islands", "AN"));
            entityManager.persist(new StateCode(31, "Chandigarh", "CH"));
            entityManager.persist(new StateCode(32, "Dadra and Nagar Haveli and Daman and Diu", "DN"));
            entityManager.persist(new StateCode(33, "Lakshadweep", "LD"));
            entityManager.persist(new StateCode(34, "Delhi", "DL"));
            entityManager.persist(new StateCode(35, "Puducherry", "PY"));
            entityManager.persist(new StateCode(36, "Daman and Diu", "DD"));
        }
        count = entityManager.createQuery("SELECT COUNT(a) FROM ServiceProviderAddressRef a", Long.class).getSingleResult();

        if (count == 0) {

            // Insert data into the ServiceProviderAddress table
            entityManager.persist(new ServiceProviderAddressRef(1, "OFFICE_ADDRESS"));
            entityManager.persist(new ServiceProviderAddressRef(2, "CURRENT_ADDRESS"));
            entityManager.persist(new ServiceProviderAddressRef(3, "BILLING_ADDRESS"));
            entityManager.persist(new ServiceProviderAddressRef(4, "MAILING_ADDRESS"));
        }
        count = entityManager.createQuery("SELECT COUNT(l) FROM ServiceProviderLanguage l", Long.class).getSingleResult();

        if (count == 0) {
            entityManager.persist(new ServiceProviderLanguage(1, "Hindi"));
            entityManager.persist(new ServiceProviderLanguage(2, "Bengali"));
            entityManager.persist(new ServiceProviderLanguage(3, "Telugu"));
            entityManager.persist(new ServiceProviderLanguage(4, "Marathi"));
            entityManager.persist(new ServiceProviderLanguage(5, "Tamil"));
            entityManager.persist(new ServiceProviderLanguage(6, "Gujarati"));
            entityManager.persist(new ServiceProviderLanguage(7, "Punjabi"));
        }

        count = entityManager.createQuery("SELECT COUNT(i) FROM ServiceProviderInfra i", Long.class).getSingleResult();

        if (count == 0) {
            entityManager.persist(new ServiceProviderInfra(1, "DESKTOP"));
            entityManager.persist(new ServiceProviderInfra(2, "SCANNER"));
            entityManager.persist(new ServiceProviderInfra(3, "LAPTOP"));
            entityManager.persist(new ServiceProviderInfra(4, "PRINTER"));
            entityManager.persist(new ServiceProviderInfra(5, "INTERNET_BROADBAND"));
        }

        count = entityManager.createQuery("SELECT COUNT(s) FROM Skill s", Long.class).getSingleResult();

        if (count == 0) {
            entityManager.persist(new Skill(1, "Form Filling Knowledge/Expertise"));
            entityManager.persist(new Skill(2, "Resizing & Uploading Image/Document"));
            entityManager.persist(new Skill(3, "Executing Online Payment/Transactions"));
            entityManager.persist(new Skill(4, "Apply To Various Government Schemes"));
        }
        count = entityManager.createQuery("SELECT COUNT(s) FROM ServiceProviderStatus s", Long.class).getSingleResult();

        if (count == 0) {
            // Get current date and time as a formatted string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String now = LocalDateTime.now().format(formatter);

            // Create new instances of ServiceProviderStatus
            ServiceProviderStatus status1 = new ServiceProviderStatus(1, "DOCUMENTS_SUBMISSION_PENDING", "Documents submission is pending", now, now, "SUPER_ADMIN");
            ServiceProviderStatus status2 = new ServiceProviderStatus(2, "APPLIED", "Application has been submitted", now, now, "SUPER_ADMIN");
            ServiceProviderStatus status3 = new ServiceProviderStatus(3, "APPROVAL_PENDING", "Application is awaiting approval", now, now, "SUPER_ADMIN");
            ServiceProviderStatus status4 = new ServiceProviderStatus(4, "APPROVED", "Application has been approved", now, now, "SUPER_ADMIN");

            // Persist the instances
            entityManager.persist(status1);
            entityManager.persist(status2);
            entityManager.persist(status3);
            entityManager.persist(status4);
        }
        count = entityManager.createQuery("SELECT COUNT(e) FROM Qualification e", Long.class).getSingleResult();

        if (count == 0) {
            entityManager.persist(new Qualification(1, "MATRICULATION/10th", "Completed secondary education or equivalent",true,false));
            entityManager.persist(new Qualification(2, "INTERMEDIATE/12th", "Completed higher secondary education or equivalent",true,true));
            entityManager.persist(new Qualification(3, "BACHELORS", "Completed undergraduate degree program",false,true));
            entityManager.persist(new Qualification(4, "MASTERS", "Completed postgraduate degree program",false,true));
            entityManager.persist(new Qualification(5, "DOCTORATE", "Completed doctoral degree program",false,true));
            entityManager.persist(new Qualification(6, "DIPLOMA", "Completed a diploma program",false,true));
            entityManager.persist(new Qualification(7, "ITI", "Completed an ITI (Industrial Training Institute) program",false,true));
        }

        count = entityManager.createQuery("SELECT COUNT(e) FROM TypingText e", Long.class).getSingleResult();
        if (count == 0) {
            entityManager.merge(new TypingText(1L, "The quick brown fox jumps over the lazy dog near the quiet river, while the bright sun sets in the horizon, casting beautiful hues of orange."));
            entityManager.merge(new TypingText(2L, "A curious cat chased a butterfly through the green meadows, unaware of the gentle breeze swirling around."));
            entityManager.merge(new TypingText(3L, "In the silent night, a lone owl hooted softly as the stars twinkled brightly above the peaceful forest."));
            entityManager.merge(new TypingText(4L, "Beneath the tall mountains, a small village thrived with joy, laughter, and the warmth of togetherness."));
            entityManager.merge(new TypingText(5L, "The adventure begins with a journey through unknown lands, filled with unexpected challenges and thrilling discoveries along the way."));
        }

        count = entityManager.createQuery("SELECT count(e) FROM ServiceProviderTestStatus e", Long.class).getSingleResult();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);

        if (count == 0) {
            entityManager.persist(new ServiceProviderTestStatus(1L, "New", "The service provider has registered but has not yet completed the test.", now, now, "SUPER_ADMIN"));
            entityManager.persist(new ServiceProviderTestStatus(2L, "Completed Test", "The service provider has completed the required skill tests.", now, now, "SUPER_ADMIN"));
            entityManager.persist(new ServiceProviderTestStatus(3L, "Approved", "The service provider's submission has been reviewed and approved.", now, now, "SUPER_ADMIN"));
            entityManager.persist(new ServiceProviderTestStatus(4L, "Rejected", "The service provider's submission was rejected due to not meeting the criteria.", now, now, "SUPER_ADMIN"));
            entityManager.persist(new ServiceProviderTestStatus(5L, "Suspended", "The service provider account is currently suspended due to policy violations.", now, now, "SUPER_ADMIN"));
        }
        count = entityManager.createQuery("SELECT count(e) FROM ServiceProviderRank e", Long.class).getSingleResult();

        if (count == 0) {
            entityManager.persist(new ServiceProviderRank(1L, "1a", "The PROFESSIONAL service provider's score is between 75-100 points", now, now, "SUPER_ADMIN", 12, 50));
            entityManager.persist(new ServiceProviderRank(2L, "1b", "The PROFESSIONAL service provider's score is between 50-75 points", now, now, "SUPER_ADMIN", 6, 25));
            entityManager.persist(new ServiceProviderRank(3L, "1c", "The PROFESSIONAL service provider's score is between 25-50 points", now, now, "SUPER_ADMIN", 4,17));
            entityManager.persist(new ServiceProviderRank(4L, "1d", "The PROFESSIONAL service provider's score is between 0-25 points", now, now, "SUPER_ADMIN", 3, 13));
            entityManager.persist(new ServiceProviderRank(5L, "2a", "The INDIVIDUAL service provider's score is between 75-100 points", now, now, "SUPER_ADMIN", 6, 25));
            entityManager.persist(new ServiceProviderRank(6L, "2b", "The INDIVIDUAL service provider's score is between 50-75 points", now, now, "SUPER_ADMIN", 3, 13));
            entityManager.persist(new ServiceProviderRank(7L, "2c", "The INDIVIDUAL service provider's score is between 25-50 points", now, now, "SUPER_ADMIN", 2, 8));
            entityManager.persist(new ServiceProviderRank(8L, "2d", "The INDIVIDUAL service provider's score is between 0-25 points", now, now, "SUPER_ADMIN", 2, 6));
        }

        count= entityManager.createQuery("SELECT count(e) FROM CustomAdmin e", Long.class).getSingleResult();
        Date currentDate = new Date();
        if(count==0)
        {
            entityManager.createNativeQuery(MOBILE_NUMBER_CONSTRAINT_IN_CUSTOM_ADMIN).executeUpdate();
            entityManager.createNativeQuery(PASSWORD_CONSTRAINT_IN_CUSTOM_ADMIN).executeUpdate();
            entityManager.merge(new CustomAdmin(1L,2,passwordEncoder.encode("Admin#01"),"admin","7740066387","+91",0,currentDate,"SUPER_ADMIN"));
            entityManager.merge(new CustomAdmin(2L,1,passwordEncoder.encode("SuperAdmin#1357"),"superadmin","9872548680","+91",0,currentDate,"SUPER_ADMIN"));
            entityManager.merge(new CustomAdmin(3L,3,passwordEncoder.encode("AdminServiceProvider#02"),"adminserviceprovider","7710393096","+91",0,currentDate,"SUPER_ADMIN"));
        }

        count = entityManager.createQuery("SELECT count(e) FROM ScoringCriteria e", Long.class).getSingleResult();

        if (count == 0) {

            // Business Unit / Infrastructure Scoring
            entityManager.merge(new ScoringCriteria(1L, "Business Unit / Infrastructure", "If it's a Business Unit: 20 points", 20));

            // Work Experience Scoring
            entityManager.merge(new ScoringCriteria(2L, "Work Experience", "1 year work experience", 5));
            entityManager.merge(new ScoringCriteria(3L, "Work Experience", "2 years work experience", 10));
            entityManager.merge(new ScoringCriteria(4L, "Work Experience", "3 years work experience", 15));
            entityManager.merge(new ScoringCriteria(5L, "Work Experience", "5 or more years work experience", 20));

            // Qualification Scoring
            entityManager.merge(new ScoringCriteria(6L, "Qualification", "Service Provider is graduated or above qualified", 10));
            entityManager.merge(new ScoringCriteria(7L, "Qualification", "Service Provider is 12th passed", 5));

            // Technical Expertise Scoring
            entityManager.merge(new ScoringCriteria(8L, "Technical Expertise", "Each skill will score 2 points", 2));
            entityManager.merge(new ScoringCriteria(9L, "Technical Expertise", "Service Provider having equal to or more than 5 skills", 10));

            // Staff Scoring
            entityManager.merge(new ScoringCriteria(10L, "Staff", "More than 4 staff members", 10));
            entityManager.merge(new ScoringCriteria(11L, "Staff", "2 staff members", 5));
            entityManager.merge(new ScoringCriteria(12L, "Staff", "Individual (no staff)", 0));

            //Infra Scoring (For individual)
            entityManager.merge(new ScoringCriteria(13L,"Infrastructure","Service Provider having Equal to 5 or more than 5 infrastructures",20));
            entityManager.merge(new ScoringCriteria(14L,"Infrastructure","Service Provider having between 2 and 4 infrastructures",10));
            entityManager.merge(new ScoringCriteria(15L,"Infrastructure","Service Provider having 1 infrastructure",5));
            entityManager.merge(new ScoringCriteria(16L,"Infrastructure","Service Provider having 0 infrastructure",0));

            //PartTimeOrFullTime Scoring (For Individual)
            entityManager.merge(new ScoringCriteria(17L,"PartTimeOrFullTime","Service Provider who is Full time",10));
            entityManager.merge(new ScoringCriteria(18L,"PartTimeOrFullTime","Service Provider who is Part time",0));
        }
        if (entityManager.createQuery("SELECT COUNT(o) FROM OrderStateRef o", Long.class).getSingleResult() == 0) {
            entityManager.persist(new OrderStateRef(1, "NEW", "Order is generated"));
            entityManager.persist(new OrderStateRef(2, "AUTO_ASSIGNED", "Order automatically assigned."));
            entityManager.persist(new OrderStateRef(3, "UNASSIGNED", "Order is unassigned."));
            entityManager.persist(new OrderStateRef(4, "ASSIGNED", "Order assigned."));
            entityManager.persist(new OrderStateRef(5, "RETURNED", "Order returned."));
            entityManager.persist(new OrderStateRef(6, "IN_PROGRESS", "Order is in progress."));
            entityManager.persist(new OrderStateRef(7, "COMPLETED", "Order completed."));
            entityManager.persist(new OrderStateRef(8, "IN_REVIEW", "Order is in review."));
        }

        count = entityManager.createQuery("SELECT count(dt) FROM DocumentType dt", Long.class).getSingleResult();

        if (count == 0) {
            entityManager.persist(new DocumentType(1,  "Aadhaar_Card_Front","Front side of a government-issued ID card in India.", "200KB", "100KB",false,false,false));
            entityManager.persist(new DocumentType(2,  "Pan_Card","A permanent account number card for tax purposes in India.",  "200KB", "100KB",false,false,false));
            entityManager.persist(new DocumentType(3,  "Live_Passport_Size_Photo","A live photo typically used for official documents.",  "200KB", "100KB",false,false,false));
            entityManager.persist(new DocumentType(4,  "Signature", "A handwritten sign used to authenticate documents.", "100KB", "50KB",false,false,false));
            entityManager.persist(new DocumentType(5,  "Ews_Certificate","Certificate for individuals and families below a certain income threshold to access various benefits and concessions.",  "300KB", "200KB",false,true,true));
            entityManager.persist(new DocumentType(6,  "Caste_Certificate","Certifies an individual's caste for reservations and benefits in education and employment.",  "300KB", "200KB",false,true,false));
            entityManager.persist(new DocumentType(7, "Address_Certificate","Verifies an individual’s residential address for identity verification and other purposes.",  "500KB", "100KB",false,true,false));
            entityManager.persist(new DocumentType(8, "Income_Certificate","Confirms an individual’s or family’s annual income for applying for government benefits and financial assistance.",  "500KB", "100KB",false,true,true));
            entityManager.persist(new DocumentType(9, "Driving_License","Authorizes an individual to operate motor vehicles, confirming knowledge of traffic laws and vehicle operation skills.",  "200KB", "100KB",false,true,true));
            entityManager.persist(new DocumentType(10, "Domicile", "The permanent home or principal residence of a person.", "300KB", "200KB",false,true,true));
            entityManager.persist(new DocumentType(11, "Disability_Certificate","An outdated term for individuals with physical or mental disabilities; 'person with a disability' is preferred today.",  "300KB", "200KB",false,true,true));
            entityManager.persist(new DocumentType(12, "Mark_Sheet","Mark sheet of Qualification",  "300KB", "200KB",true,false,false));
            entityManager.persist(new DocumentType(13, "Others", "Includes other document types not listed above, tailored to specific needs or contexts.", "200KB", "100KB",false,false,false));
            entityManager.persist(new DocumentType(14, "C-Form_Photo", "A C Form photo is a standardized ID photo for official documents.", "200KB", "100KB",false,false,false));
            entityManager.persist(new DocumentType(15, "Ex-Service_Men","Ex-Service Men document is required for individuals who have previously worked in the organization and are now no longer employed.",  "300KB", "200KB",false,false,false));
            entityManager.persist(new DocumentType(16, "Business_Photo","A Standard proof of Running Business.",  "200KB", "100KB",false,false,false));
            entityManager.persist(new DocumentType(17, "Personal_Photo", "A Personal Photograph of SP.", "200KB", "100KB",false,false,false));
            entityManager.persist(new DocumentType(18, "NCC_Certificate_A","NCC CERTIFICATE A.",  "500KB", "100KB",false,true,true));
            entityManager.persist(new DocumentType(19, "NCC_Certificate_B", "NCC CERTIFICATE B.", "500KB", "100KB",false,true,true));
            entityManager.persist(new DocumentType(20, "NCC_Certificate_C", "NCC CERTIFICATE C.", "500KB", "100KB",false,true,true));
            entityManager.persist(new DocumentType(21, "NSS_Certificate_A","NSS CERTIFICATE A",  "500KB", "100KB",false,true,true));
            entityManager.persist(new DocumentType(22, "Sports_Certificate-State","SPORTS CERTIFICATE FOR STATE LEVEL",  "200KB", "100KB",false,true,true));
            entityManager.persist(new DocumentType(23, "Sports_Certificate-Centre", "SPORTS CERTIFICATE FOR CENTRE LEVEL", "200KB", "100KB",false,true,true));
            entityManager.persist(new DocumentType(24, "Aadhaar_Card_Backside", "Back side of a government-issued ID card in India.", "200KB", "100KB",false,false,false));
            entityManager.persist(new DocumentType(25, "Left_Thumb_Impression", "The left thumb impression of the individual, typically required for identity verification in official documents.", "100KB", "50KB",false,true,true));
            entityManager.persist(new DocumentType(26, "Right_Thumb_Impression", "The right thumb impression of the individual, typically required for identity verification in official documents.", "100KB", "50KB",false,true,true));
            entityManager.persist(new DocumentType(27, "White_Background_Passport_Size_Photo", "A white background passport size photo typically used for official documents", "200KB", "100KB",false,false,false));
            entityManager.persist(new DocumentType(28, "NSS_Certificate_B","NSS CERTIFICATE B",  "500KB", "100KB",false,true,true));
            entityManager.persist(new DocumentType(29, "NSS_Certificate_C","NSS CERTIFICATE C",  "500KB", "100KB",false,true,true));

          }
        if(entityManager.createQuery("SELECT COUNT(o) FROM FileType o",Long.class).getSingleResult()==0)
        {
            entityManager.merge(new FileType(1,"PNG"));
            entityManager.merge(new FileType(2, "JPG"));
            entityManager.merge(new FileType(3, "PDF"));
            entityManager.merge(new FileType(4,"JPEG"));
        }
         count = entityManager.createQuery(
                        "SELECT COUNT(df) FROM DocumentType dt JOIN dt.required_document_types df", Long.class)
                .getSingleResult();

        if (count == 0) {
            String sql = "INSERT INTO document_file_types (document_type_id, file_type_id) VALUES " +
                    "(1, 2),(1,4), (2, 2),(2,4), (3, 2),(3,4), (4,2),(4,4), " +
                    "(5 , 2),(5,4), (6,1),(6,2), (6,4),(7, 2),(7,4), (8, 2),(8,4), (9, 1), " +
                    "(9, 2),(9,4), (10, 2),(10,4), (11, 2),(11,4), " +
                    "(12, 2),(12,4), (13, 1), (13, 2),(13,4), (14, 2), (14,4)," +
                    "(15, 2),(15,4), (16, 2),(16,4), (17, 2),(17,4),(18, 2),(18,4), (19, 2),(19,4), (20, 2),(20,4), " +
                    "(22,2),(22,4), (21, 1), (21, 2),(21,4), (23, 1), (23, 2),(23,4)," +
                    "(24, 2), (24,4), (25, 2),(25,4), " +
                    "(26, 2),(26,4), (27, 2),(27,4),(28, 1), (28, 2),  (28,4)," +
                    "(29, 1), (29, 2),(29,4)";
            entityManager.createNativeQuery(sql).executeUpdate();
        }

         count = entityManager.createQuery("SELECT count(b) FROM BoardUniversity b", Long.class).getSingleResult();
         if (count == 0) {
            entityManager.merge(new BoardUniversity(1L,"Others", "NA","Not Applicable","NA",now,now,"SUPER_ADMIN","SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(2L, "Central Board of Secondary Education", "Delhi", "CBSE", "BOARD", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(3L, "Jawaharlal Nehru University", "Delhi", "JNU", "UNIVERSITY", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(4L, "Uttar Pradesh Board", "Lucknow", "UPB", "BOARD", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(5L, "Punjab University", "Chandigarh", "PU", "UNIVERSITY", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(6L, "Maharashtra State Board", "Mumbai", "MSB", "BOARD", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(7L, "Rajasthan University", "Jaipur", "RU", "UNIVERSITY", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(8L, "Karnataka State Board", "Bangalore", "KSB", "BOARD", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(9L, "Tamil Nadu State Board", "Chennai", "TNSB", "BOARD", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(10L, "University of Mumbai", "Mumbai", "UM", "UNIVERSITY", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(11L, "Osmania University", "Hyderabad", "OU", "UNIVERSITY", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(12L, "West Bengal State University", "Kolkata", "WBSU", "UNIVERSITY", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(13L, "University of Calcutta", "Kolkata", "CU", "UNIVERSITY", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(14L, "Andhra Pradesh Board", "Vijayawada", "APB", "BOARD", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(15L, "University of Madras", "Chennai", "UM", "UNIVERSITY", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(16L, "University of Kerala", "Thiruvananthapuram", "UK", "UNIVERSITY", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(17L, "Gujarat Secondary and Higher Secondary Education Board", "Gandhinagar", "GSHSEB", "BOARD", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(18L, "University of Pune", "Pune", "PU", "UNIVERSITY", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(19L, "University of Rajasthan", "Jaipur", "UR", "UNIVERSITY", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new BoardUniversity(20L, "University of Allahabad", "Allahabad", "UA", "UNIVERSITY", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
        }
         //***********ZONES
        count = entityManager.createQuery("SELECT count(z) FROM Zone z", Long.class).getSingleResult();
        if (count == 0) {
            // Insert zones into Zone table with matching IDs
            entityManager.merge(new Zone(1, "NORTH ZONE")); // Matches Northern Zone ID
            entityManager.merge(new Zone(2, "SOUTH ZONE")); // Matches Southern Zone ID
            entityManager.merge(new Zone(3, "EAST ZONE"));  // Matches Eastern Zone ID
            entityManager.merge(new Zone(4, "WEST ZONE"));  // Matches Western Zone ID
            entityManager.merge(new Zone(5, "CENTRAL ZONE")); // Matches Central Zone ID
            entityManager.merge(new Zone(6, "NORTH-EAST ZONE")); // Matches North-Eastern Zone ID
            entityManager.merge(new Zone(7, "SPECIAL UNION TERRITORIES ZONE")); // Matches Special UTs Zone ID
        }


        String alterQuery = "ALTER TABLE custom_customer ALTER COLUMN token TYPE VARCHAR(512)";
        javax.persistence.Query query = entityManager.createNativeQuery(alterQuery);
        query.executeUpdate();


        long institutionCount = entityManager.createQuery("SELECT count(i) FROM Institution i", Long.class).getSingleResult();
        if (institutionCount == 0) {
            entityManager.merge(new Institution(1L, "All India Institute of Medical Sciences", "New Delhi", "AIIMS", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(2L, "Indian Institute of Technology Bombay", "Mumbai", "IITB", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(3L, "Indian Institute of Science", "Bangalore", "IISC", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(4L, "National Institute of Technology Tiruchirappalli", "Tiruchirappalli", "NITT", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(5L, "Delhi Technological University", "New Delhi", "DTU", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(6L, "Jawaharlal Nehru Technological University", "Hyderabad", "JNTUH", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(7L, "Banaras Hindu University", "Varanasi", "BHU", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(8L, "University of Hyderabad", "Hyderabad", "UOH", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(9L, "Vellore Institute of Technology", "Vellore", "VIT", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(10L, "Manipal Academy of Higher Education", "Manipal", "MAHE", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(11L, "Amity University", "Noida", "AU", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(12L, "Birla Institute of Technology and Science", "Pilani", "BITS", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(13L, "SRM Institute of Science and Technology", "Chennai", "SRM", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(14L, "Christ University", "Bangalore", "CU", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(15L, "Savitribai Phule Pune University", "Pune", "SPPU", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(16L, "Indian Statistical Institute", "Kolkata", "ISI", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(17L, "Tata Institute of Fundamental Research", "Mumbai", "TIFR", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(18L, "National Law School of India University", "Bangalore", "NLSIU", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(19L, "Indian Institute of Technology Kanpur", "Kanpur", "IITK", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(20L, "Indian Institute of Technology Delhi", "New Delhi", "IITD", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(21L, "Jamia Millia Islamia", "New Delhi", "JMI", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(22L, "Aligarh Muslim University", "Aligarh", "AMU", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(23L, "Visva-Bharati University", "Santiniketan", "VBU", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(24L, "Indian Institute of Management Ahmedabad", "Ahmedabad", "IIMA", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(25L, "Indian Institute of Management Bangalore", "Bangalore", "IIMB", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(26L, "Indian Institute of Technology Kharagpur", "Kharagpur", "IITKGP", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(27L, "Indian Institute of Technology Madras", "Chennai", "IITM", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(28L, "Indian Institute of Technology Guwahati", "Guwahati", "IITG", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(29L, "Indian School of Business", "Hyderabad", "ISB", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(30L, "University of Mysore", "Mysore", "UOM", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(31L, "Anna University", "Chennai", "AU", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(32L, "University of Delhi", "New Delhi", "DU", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(33L, "University of Calicut", "Calicut", "UOC", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(34L, "Guru Nanak Dev University", "Amritsar", "GNDU", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
            entityManager.merge(new Institution(35L, "Punjab University", "Chandigarh", "PU", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
        }
    }
}
