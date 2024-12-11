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
import java.time.format.DateTimeFormatter;

import static org.broadleafcommerce.common.util.sql.importsql.DemoSqlServerSingleLineSqlCommandExtractor.CURRENT_TIMESTAMP;

@Component
public class CommandLineService implements CommandLineRunner {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
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
            entityManager.merge(new CustomStream(1L, "SCIENCE", "Description of Science"));
            entityManager.merge(new CustomStream(2L, "ARTS", "Description of Arts"));
            entityManager.merge(new CustomStream(3L, "COMMERCE", "Description of Commerce"));
            entityManager.merge(new CustomStream(4L, "ENGINEERING", "Description of Engineering"));
            entityManager.merge(new CustomStream(5L, "MEDICINE", "Description of Medicine"));
            entityManager.merge(new CustomStream(6L, "HUMANITIES", "Description of Humanities"));
            entityManager.merge(new CustomStream(7L, "SOCIAL SCIENCES", "Description of Social Sciences"));
            entityManager.merge(new CustomStream(8L, "TECHNOLOGY", "Description of Technology"));
            entityManager.merge(new CustomStream(9L, "MATHEMATICS", "Description of Mathematics"));
            entityManager.merge(new CustomStream(10L, "DESIGN", "Description of Design"));
        }

        if(entityManager.createQuery("SELECT COUNT(s) FROM CustomSubject s", Long.class).getSingleResult() == 0) {
            entityManager.merge(new CustomSubject(1L, "Mathematics", "Description of Mathematics"));
            entityManager.merge(new CustomSubject(2L, "Physics", "Description of Physics"));
            entityManager.merge(new CustomSubject(3L, "Chemistry", "Description of Chemistry"));
            entityManager.merge(new CustomSubject(4L, "Biology", "Description of Biology"));
            entityManager.merge(new CustomSubject(5L, "English", "Description of English"));
            entityManager.merge(new CustomSubject(6L, "History", "Description of History"));
            entityManager.merge(new CustomSubject(7L, "Geography", "Description of Geography"));
            entityManager.merge(new CustomSubject(8L, "Computer Science", "Description of Computer Science"));
            entityManager.merge(new CustomSubject(9L, "Art", "Description of Art"));
            entityManager.merge(new CustomSubject(10L, "Physical Education", "Description of Physical Education"));
        }

        if (entityManager.createQuery("SELECT COUNT(r) FROM Role r", Long.class).getSingleResult() == 0) {
            entityManager.merge(new Role(1, "SUPER_ADMIN", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, "SUPER_ADMIN"));
            entityManager.merge(new Role(2, "ADMIN", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, "SUPER_ADMIN"));
            entityManager.merge(new Role(3, "ADMIN_SERVICE_PROVIDER", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, "SUPER_ADMIN"));
            entityManager.merge(new Role(4, "SERVICE_PROVIDER", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, "SUPER_ADMIN"));
            entityManager.merge(new Role(5, "CUSTOMER", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, "SUPER_ADMIN"));
        }

        Long count = entityManager.createQuery("SELECT COUNT(d) FROM Districts d", Long.class).getSingleResult();

        if (count == 0) {
            // Andhra Pradesh (AP) districts
            entityManager.persist(new Districts(1, "Anantapur", "AP"));
            entityManager.persist(new Districts(2, "Chittoor", "AP"));
            entityManager.persist(new Districts(3, "East Godavari", "AP"));
            entityManager.persist(new Districts(4, "Guntur", "AP"));
            entityManager.persist(new Districts(5, "Krishna", "AP"));
            entityManager.persist(new Districts(6, "Kurnool", "AP"));
            entityManager.persist(new Districts(7, "Nellore", "AP"));
            entityManager.persist(new Districts(8, "Prakasam", "AP"));
            entityManager.persist(new Districts(9, "Srikakulam", "AP"));
            entityManager.persist(new Districts(10, "Visakhapatnam", "AP"));
            entityManager.persist(new Districts(11, "Vizianagaram", "AP"));
            entityManager.persist(new Districts(12, "West Godavari", "AP"));
            entityManager.persist(new Districts(13, "YSR Kadapa", "AP"));

// Arunachal Pradesh (AR) districts
            entityManager.persist(new Districts(14, "Tawang", "AR"));
            entityManager.persist(new Districts(15, "West Kameng", "AR"));
            entityManager.persist(new Districts(16, "East Kameng", "AR"));
            entityManager.persist(new Districts(17, "Papum Pare", "AR"));
            entityManager.persist(new Districts(18, "Kurung Kumey", "AR"));
            entityManager.persist(new Districts(19, "Kra Daadi", "AR"));
            entityManager.persist(new Districts(20, "Lower Subansiri", "AR"));
            entityManager.persist(new Districts(21, "Upper Subansiri", "AR"));
            entityManager.persist(new Districts(22, "West Siang", "AR"));
            entityManager.persist(new Districts(23, "East Siang", "AR"));
            entityManager.persist(new Districts(24, "Siang", "AR"));
            entityManager.persist(new Districts(25, "Upper Siang", "AR"));
            entityManager.persist(new Districts(26, "Lower Siang", "AR"));
            entityManager.persist(new Districts(27, "Lower Dibang Valley", "AR"));
            entityManager.persist(new Districts(28, "Dibang Valley", "AR"));
            entityManager.persist(new Districts(29, "Anjaw", "AR"));
            entityManager.persist(new Districts(30, "Lohit", "AR"));
            entityManager.persist(new Districts(31, "Namsai", "AR"));
            entityManager.persist(new Districts(32, "Changlang", "AR"));
            entityManager.persist(new Districts(33, "Tirap", "AR"));
            entityManager.persist(new Districts(34, "Longding", "AR"));

// Assam (AS) districts
            entityManager.persist(new Districts(35, "Baksa", "AS"));
            entityManager.persist(new Districts(36, "Barpeta", "AS"));
            entityManager.persist(new Districts(37, "Biswanath", "AS"));
            entityManager.persist(new Districts(38, "Bongaigaon", "AS"));
            entityManager.persist(new Districts(39, "Cachar", "AS"));
            entityManager.persist(new Districts(40, "Charaideo", "AS"));
            entityManager.persist(new Districts(41, "Chirang", "AS"));
            entityManager.persist(new Districts(42, "Darrang", "AS"));
            entityManager.persist(new Districts(43, "Dhemaji", "AS"));
            entityManager.persist(new Districts(44, "Dhubri", "AS"));
            entityManager.persist(new Districts(45, "Dibrugarh", "AS"));
            entityManager.persist(new Districts(46, "Goalpara", "AS"));
            entityManager.persist(new Districts(47, "Golaghat", "AS"));
            entityManager.persist(new Districts(48, "Hailakandi", "AS"));
            entityManager.persist(new Districts(49, "Hojai", "AS"));
            entityManager.persist(new Districts(50, "Jorhat", "AS"));
            entityManager.persist(new Districts(51, "Kamrup Metropolitan", "AS"));
            entityManager.persist(new Districts(52, "Kamrup", "AS"));
            entityManager.persist(new Districts(53, "Karbi Anglong", "AS"));
            entityManager.persist(new Districts(54, "Karimganj", "AS"));
            entityManager.persist(new Districts(55, "Kokrajhar", "AS"));
            entityManager.persist(new Districts(56, "Lakhimpur", "AS"));
            entityManager.persist(new Districts(57, "Majuli", "AS"));
            entityManager.persist(new Districts(58, "Morigaon", "AS"));
            entityManager.persist(new Districts(59, "Nagaon", "AS"));
            entityManager.persist(new Districts(60, "Nalbari", "AS"));
            entityManager.persist(new Districts(61, "Dima Hasao", "AS"));
            entityManager.persist(new Districts(62, "Sivasagar", "AS"));
            entityManager.persist(new Districts(63, "Sonitpur", "AS"));
            entityManager.persist(new Districts(64, "South Salmara-Mankachar", "AS"));
            entityManager.persist(new Districts(65, "Tinsukia", "AS"));
            entityManager.persist(new Districts(66, "Udalguri", "AS"));
            entityManager.persist(new Districts(67, "West Karbi Anglong", "AS"));

// Bihar (BR) districts
            entityManager.persist(new Districts(68, "Araria", "BR"));
            entityManager.persist(new Districts(69, "Arwal", "BR"));
            entityManager.persist(new Districts(70, "Aurangabad", "BR"));
            entityManager.persist(new Districts(71, "Banka", "BR"));
            entityManager.persist(new Districts(72, "Begusarai", "BR"));
            entityManager.persist(new Districts(73, "Bhagalpur", "BR"));
            entityManager.persist(new Districts(74, "Bhojpur", "BR"));
            entityManager.persist(new Districts(75, "Buxar", "BR"));
            entityManager.persist(new Districts(76, "Darbhanga", "BR"));
            entityManager.persist(new Districts(77, "East Champaran (Motihari)", "BR"));
            entityManager.persist(new Districts(78, "Gaya", "BR"));
            entityManager.persist(new Districts(79, "Gopalganj", "BR"));
            entityManager.persist(new Districts(80, "Jamui", "BR"));
            entityManager.persist(new Districts(81, "Jehanabad", "BR"));
            entityManager.persist(new Districts(82, "Kaimur (Bhabua)", "BR"));
            entityManager.persist(new Districts(83, "Katihar", "BR"));
            entityManager.persist(new Districts(84, "Khagaria", "BR"));
            entityManager.persist(new Districts(85, "Kishanganj", "BR"));
            entityManager.persist(new Districts(86, "Lakhisarai", "BR"));
            entityManager.persist(new Districts(87, "Madhepura", "BR"));
            entityManager.persist(new Districts(88, "Madhubani", "BR"));
            entityManager.persist(new Districts(89, "Munger (Monghyr)", "BR"));
            entityManager.persist(new Districts(90, "Muzaffarpur", "BR"));
            entityManager.persist(new Districts(91, "Nalanda", "BR"));
            entityManager.persist(new Districts(92, "Nawada", "BR"));
            entityManager.persist(new Districts(93, "Patna", "BR"));
            entityManager.persist(new Districts(94, "Purnia (Purnea)", "BR"));
            entityManager.persist(new Districts(95, "Rohtas", "BR"));
            entityManager.persist(new Districts(96, "Saharsa", "BR"));
            entityManager.persist(new Districts(97, "Samastipur", "BR"));
            entityManager.persist(new Districts(98, "Saran", "BR"));
            entityManager.persist(new Districts(99, "Sheikhpura", "BR"));
            entityManager.persist(new Districts(100, "Sheohar", "BR"));
            entityManager.persist(new Districts(101, "Sitamarhi", "BR"));
            entityManager.persist(new Districts(102, "Supaul", "BR"));
            entityManager.persist(new Districts(103, "Vaishali", "BR"));
            entityManager.persist(new Districts(104, "West Champaran (Bagaha)", "BR"));

// Chandigarh (UT) districts
            entityManager.persist(new Districts(105, "Chandigarh", "CH"));

// Chhattisgarh (CG) districts
            entityManager.persist(new Districts(106, "Balod", "CG"));
            entityManager.persist(new Districts(107, "Baloda Bazar", "CG"));
            entityManager.persist(new Districts(108, "Balrampur", "CG"));
            entityManager.persist(new Districts(109, "Bastar", "CG"));
            entityManager.persist(new Districts(110, "Bemetara", "CG"));
            entityManager.persist(new Districts(111, "Bijapur", "CG"));
            entityManager.persist(new Districts(112, "Bilaspur", "CG"));
            entityManager.persist(new Districts(113, "Dantewada (South Bastar)", "CG"));
            entityManager.persist(new Districts(114, "Dhamtari", "CG"));
            entityManager.persist(new Districts(115, "Durg", "CG"));
            entityManager.persist(new Districts(116, "Gariaband", "CG"));
            entityManager.persist(new Districts(117, "Janjgir-Champa", "CG"));
            entityManager.persist(new Districts(118, "Jashpur", "CG"));
            entityManager.persist(new Districts(119, "Kabirdham", "CG"));
            entityManager.persist(new Districts(120, "Kanker", "CG"));
            entityManager.persist(new Districts(121, "Korba", "CG"));
            entityManager.persist(new Districts(122, "Kondagaon", "CG"));
            entityManager.persist(new Districts(123, "Mahasamund", "CG"));
            entityManager.persist(new Districts(124, "Mungeli", "CG"));
            entityManager.persist(new Districts(125, "Narayanpur", "CG"));
            entityManager.persist(new Districts(126, "Raigarh", "CG"));
            entityManager.persist(new Districts(127, "Raipur", "CG"));
            entityManager.persist(new Districts(128, "Rajnandgaon", "CG"));
            entityManager.persist(new Districts(129, "Sarguja", "CG"));
            entityManager.persist(new Districts(130, "Surajpur", "CG"));
            entityManager.persist(new Districts(131, "Surguja", "CG"));
// Dadra and Nagar Haveli (UT) districts
            entityManager.persist(new Districts(132, "Dadra & Nagar Haveli", "DN"));

// Daman and Diu (UT) districts
            entityManager.persist(new Districts(133, "Daman", "DD"));
            entityManager.persist(new Districts(134, "Diu", "DD"));

// Delhi (NCT) districts
            entityManager.persist(new Districts(135, "Central Delhi", "DL"));
            entityManager.persist(new Districts(136, "East Delhi", "DL"));
            entityManager.persist(new Districts(137, "New Delhi", "DL"));
            entityManager.persist(new Districts(138, "North Delhi", "DL"));
            entityManager.persist(new Districts(139, "North East Delhi", "DL"));
            entityManager.persist(new Districts(140, "North West Delhi", "DL"));
            entityManager.persist(new Districts(141, "Shahdara", "DL"));
            entityManager.persist(new Districts(142, "South Delhi", "DL"));
            entityManager.persist(new Districts(143, "South East Delhi", "DL"));
            entityManager.persist(new Districts(144, "South West Delhi", "DL"));
            entityManager.persist(new Districts(145, "West Delhi", "DL"));

// Goa districts
            entityManager.persist(new Districts(146, "North Goa", "GA"));
            entityManager.persist(new Districts(147, "South Goa", "GA"));

// Gujarat (State Code: GJ)
            entityManager.persist(new Districts(148, "Ahmedabad", "GJ"));
            entityManager.persist(new Districts(149, "Amreli", "GJ"));
            entityManager.persist(new Districts(150, "Anand", "GJ"));
            entityManager.persist(new Districts(151, "Aravalli", "GJ"));
            entityManager.persist(new Districts(152, "Banaskantha (Palanpur)", "GJ"));
            entityManager.persist(new Districts(153, "Bharuch", "GJ"));
            entityManager.persist(new Districts(154, "Bhavnagar", "GJ"));
            entityManager.persist(new Districts(155, "Botad", "GJ"));
            entityManager.persist(new Districts(156, "Chhota Udepur", "GJ"));
            entityManager.persist(new Districts(157, "Dahod", "GJ"));
            entityManager.persist(new Districts(158, "Dangs (Ahwa)", "GJ"));
            entityManager.persist(new Districts(159, "Devbhoomi Dwarka", "GJ"));
            entityManager.persist(new Districts(160, "Gandhinagar", "GJ"));
            entityManager.persist(new Districts(161, "Gir Somnath", "GJ"));
            entityManager.persist(new Districts(162, "Jamnagar", "GJ"));
            entityManager.persist(new Districts(163, "Junagadh", "GJ"));
            entityManager.persist(new Districts(164, "Kachchh", "GJ"));
            entityManager.persist(new Districts(165, "Kheda (Nadiad)", "GJ"));
            entityManager.persist(new Districts(166, "Mahisagar", "GJ"));
            entityManager.persist(new Districts(167, "Mehsana", "GJ"));
            entityManager.persist(new Districts(168, "Morbi", "GJ"));
            entityManager.persist(new Districts(169, "Narmada (Rajpipla)", "GJ"));
            entityManager.persist(new Districts(170, "Navsari", "GJ"));
            entityManager.persist(new Districts(171, "Panchmahal (Godhra)", "GJ"));
            entityManager.persist(new Districts(172, "Patan", "GJ"));
            entityManager.persist(new Districts(173, "Porbandar", "GJ"));
            entityManager.persist(new Districts(174, "Rajkot", "GJ"));
            entityManager.persist(new Districts(175, "Sabarkantha (Himmatnagar)", "GJ"));
            entityManager.persist(new Districts(176, "Surat", "GJ"));
            entityManager.persist(new Districts(177, "Surendranagar", "GJ"));
            entityManager.persist(new Districts(178, "Tapi (Vyara)", "GJ"));
            entityManager.persist(new Districts(179, "Vadodara", "GJ"));
            entityManager.persist(new Districts(180, "Valsad", "GJ"));

// Haryana (State Code: HR)
            entityManager.persist(new Districts(181, "Ambala", "HR"));
            entityManager.persist(new Districts(182, "Bhiwani", "HR"));
            entityManager.persist(new Districts(183, "Charkhi Dadri", "HR"));
            entityManager.persist(new Districts(184, "Faridabad", "HR"));
            entityManager.persist(new Districts(185, "Fatehabad", "HR"));
            entityManager.persist(new Districts(186, "Gurgaon", "HR"));
            entityManager.persist(new Districts(187, "Hisar", "HR"));
            entityManager.persist(new Districts(188, "Jhajjar", "HR"));
            entityManager.persist(new Districts(189, "Jind", "HR"));
            entityManager.persist(new Districts(190, "Kaithal", "HR"));
            entityManager.persist(new Districts(191, "Karnal", "HR"));
            entityManager.persist(new Districts(192, "Kurukshetra", "HR"));
            entityManager.persist(new Districts(193, "Mahendragarh", "HR"));
            entityManager.persist(new Districts(194, "Mewat", "HR"));
            entityManager.persist(new Districts(195, "Palwal", "HR"));
            entityManager.persist(new Districts(196, "Panchkula", "HR"));
            entityManager.persist(new Districts(197, "Panipat", "HR"));
            entityManager.persist(new Districts(198, "Rewari", "HR"));
            entityManager.persist(new Districts(199, "Rohtak", "HR"));
            entityManager.persist(new Districts(200, "Sirsa", "HR"));
            entityManager.persist(new Districts(201, "Sonipat", "HR"));
            entityManager.persist(new Districts(202, "Yamunanagar", "HR"));

// Himachal Pradesh (State Code: HP)
            entityManager.persist(new Districts(203, "Bilaspur", "HP"));
            entityManager.persist(new Districts(204, "Chamba", "HP"));
            entityManager.persist(new Districts(205, "Hamirpur", "HP"));
            entityManager.persist(new Districts(206, "Kangra", "HP"));
            entityManager.persist(new Districts(207, "Kinnaur", "HP"));
            entityManager.persist(new Districts(208, "Kullu", "HP"));
            entityManager.persist(new Districts(209, "Lahaul & Spiti", "HP"));
            entityManager.persist(new Districts(210, "Mandi", "HP"));
            entityManager.persist(new Districts(211, "Shimla", "HP"));
            entityManager.persist(new Districts(212, "Sirmaur (Sirmour)", "HP"));
            entityManager.persist(new Districts(213, "Solan", "HP"));
            entityManager.persist(new Districts(214, "Una", "HP"));

// Jammu and Kashmir (State Code: JK)
            entityManager.persist(new Districts(215, "Anantnag", "JK"));
            entityManager.persist(new Districts(216, "Bandipore", "JK"));
            entityManager.persist(new Districts(217, "Baramulla", "JK"));
            entityManager.persist(new Districts(218, "Budgam", "JK"));
            entityManager.persist(new Districts(219, "Doda", "JK"));
            entityManager.persist(new Districts(220, "Ganderbal", "JK"));
            entityManager.persist(new Districts(221, "Jammu", "JK"));
            entityManager.persist(new Districts(222, "Kargil", "JK"));
            entityManager.persist(new Districts(223, "Kathua", "JK"));
            entityManager.persist(new Districts(224, "Kishtwar", "JK"));
            entityManager.persist(new Districts(225, "Kulgam", "JK"));
            entityManager.persist(new Districts(226, "Kupwara", "JK"));
            entityManager.persist(new Districts(227, "Leh", "JK"));
            entityManager.persist(new Districts(228, "Poonch", "JK"));
            entityManager.persist(new Districts(229, "Pulwama", "JK"));
            entityManager.persist(new Districts(230, "Rajouri", "JK"));
            entityManager.persist(new Districts(231, "Ramban", "JK"));
            entityManager.persist(new Districts(232, "Reasi", "JK"));
            entityManager.persist(new Districts(233, "Samba", "JK"));
            entityManager.persist(new Districts(234, "Shopian", "JK"));
            entityManager.persist(new Districts(235, "Srinagar", "JK"));
            entityManager.persist(new Districts(236, "Udhampur", "JK"));

// Jharkhand (State Code: JH)
            entityManager.persist(new Districts(237, "Bokaro", "JH"));
            entityManager.persist(new Districts(238, "Chatra", "JH"));
            entityManager.persist(new Districts(239, "Deoghar", "JH"));
            entityManager.persist(new Districts(240, "Dhanbad", "JH"));
            entityManager.persist(new Districts(241, "Dumka", "JH"));
            entityManager.persist(new Districts(242, "East Singhbhum", "JH"));
            entityManager.persist(new Districts(243, "Garhwa", "JH"));
            entityManager.persist(new Districts(244, "Giridih", "JH"));
            entityManager.persist(new Districts(245, "Godda", "JH"));
            entityManager.persist(new Districts(246, "Gumla", "JH"));
            entityManager.persist(new Districts(247, "Hazaribag", "JH"));
            entityManager.persist(new Districts(248, "Jamtara", "JH"));
            entityManager.persist(new Districts(249, "Khunti", "JH"));
            entityManager.persist(new Districts(250, "Koderma", "JH"));
            entityManager.persist(new Districts(251, "Latehar", "JH"));
            entityManager.persist(new Districts(252, "Lohardaga", "JH"));
            entityManager.persist(new Districts(253, "Pakur", "JH"));
            entityManager.persist(new Districts(254, "Palamu", "JH"));
            entityManager.persist(new Districts(255, "Ramgarh", "JH"));
            entityManager.persist(new Districts(256, "Ranchi", "JH"));
            entityManager.persist(new Districts(257, "Sahibganj", "JH"));
            entityManager.persist(new Districts(258, "Seraikela-Kharsawan", "JH"));
            entityManager.persist(new Districts(259, "Simdega", "JH"));
            entityManager.persist(new Districts(260, "West Singhbhum", "JH"));

// Karnataka
            entityManager.persist(new Districts(261, "Bagalkot", "KA"));
            entityManager.persist(new Districts(262, "Ballari (Bellary)", "KA"));
            entityManager.persist(new Districts(263, "Belagavi (Belgaum)", "KA"));
            entityManager.persist(new Districts(264, "Bengaluru (Bangalore) Rural", "KA"));
            entityManager.persist(new Districts(265, "Bengaluru (Bangalore) Urban", "KA"));
            entityManager.persist(new Districts(266, "Bidar", "KA"));
            entityManager.persist(new Districts(267, "Chamarajanagar", "KA"));
            entityManager.persist(new Districts(268, "Chikballapur", "KA"));
            entityManager.persist(new Districts(269, "Chikkamagaluru (Chikmagalur)", "KA"));
            entityManager.persist(new Districts(270, "Chitradurga", "KA"));
            entityManager.persist(new Districts(271, "Dakshina Kannada", "KA"));
            entityManager.persist(new Districts(272, "Davangere", "KA"));
            entityManager.persist(new Districts(273, "Dharwad", "KA"));
            entityManager.persist(new Districts(274, "Gadag", "KA"));
            entityManager.persist(new Districts(275, "Hassan", "KA"));
            entityManager.persist(new Districts(276, "Haveri", "KA"));
            entityManager.persist(new Districts(277, "Kalaburagi (Gulbarga)", "KA"));
            entityManager.persist(new Districts(278, "Kodagu", "KA"));
            entityManager.persist(new Districts(279, "Kolar", "KA"));
            entityManager.persist(new Districts(280, "Koppal", "KA"));
            entityManager.persist(new Districts(281, "Mandya", "KA"));
            entityManager.persist(new Districts(282, "Mysuru (Mysore)", "KA"));
            entityManager.persist(new Districts(283, "Raichur", "KA"));
            entityManager.persist(new Districts(284, "Ramanagara", "KA"));
            entityManager.persist(new Districts(285, "Shivamogga (Shimoga)", "KA"));
            entityManager.persist(new Districts(286, "Tumakuru (Tumkur)", "KA"));
            entityManager.persist(new Districts(287, "Udupi", "KA"));
            entityManager.persist(new Districts(288, "Uttara Kannada (Karwar)", "KA"));
            entityManager.persist(new Districts(289, "Vijayapura (Bijapur)", "KA"));
            entityManager.persist(new Districts(290, "Yadgir", "KA"));

// KL
            entityManager.persist(new Districts(291, "Alappuzha", "KL"));
            entityManager.persist(new Districts(292, "Ernakulam", "KL"));
            entityManager.persist(new Districts(293, "Idukki", "KL"));
            entityManager.persist(new Districts(294, "Kannur", "KL"));
            entityManager.persist(new Districts(295, "Kasaragod", "KL"));
            entityManager.persist(new Districts(296, "Kollam", "KL"));
            entityManager.persist(new Districts(297, "Kottayam", "KL"));
            entityManager.persist(new Districts(298, "Kozhikode", "KL"));
            entityManager.persist(new Districts(299, "Malappuram", "KL"));
            entityManager.persist(new Districts(300, "Palakkad", "KL"));
            entityManager.persist(new Districts(301, "Pathanamthitta", "KL"));
            entityManager.persist(new Districts(302, "Thiruvananthapuram", "KL"));
            entityManager.persist(new Districts(303, "Thrissur", "KL"));
            entityManager.persist(new Districts(304, "Wayanad", "KL"));

// Lakshadweep(UT)
            entityManager.persist(new Districts(305, "Agatti", "LD"));
            entityManager.persist(new Districts(306, "Amini", "LD"));
            entityManager.persist(new Districts(307, "Androth", "LD"));
            entityManager.persist(new Districts(308, "Bithra", "LD"));
            entityManager.persist(new Districts(309, "Chethlath", "LD"));
            entityManager.persist(new Districts(310, "Kavaratti", "LD"));
            entityManager.persist(new Districts(311, "Kadmath", "LD"));
            entityManager.persist(new Districts(312, "Kalpeni", "LD"));
            entityManager.persist(new Districts(313, "Kilthan", "LD"));
            entityManager.persist(new Districts(314, "Minicoy", "LD"));

// Madhya Pradesh
            entityManager.persist(new Districts(315, "Agar Malwa", "MP"));
            entityManager.persist(new Districts(316, "Alirajpur", "MP"));
            entityManager.persist(new Districts(317, "Anuppur", "MP"));
            entityManager.persist(new Districts(318, "Ashoknagar", "MP"));
            entityManager.persist(new Districts(319, "Balaghat", "MP"));
            entityManager.persist(new Districts(320, "Barwani", "MP"));
            entityManager.persist(new Districts(321, "Betul", "MP"));
            entityManager.persist(new Districts(322, "Bhind", "MP"));
            entityManager.persist(new Districts(323, "Bhopal", "MP"));
            entityManager.persist(new Districts(324, "Burhanpur", "MP"));
            entityManager.persist(new Districts(325, "Chhatarpur", "MP"));
            entityManager.persist(new Districts(326, "Chhindwara", "MP"));
            entityManager.persist(new Districts(327, "Damoh", "MP"));
            entityManager.persist(new Districts(328, "Datia", "MP"));
            entityManager.persist(new Districts(329, "Dewas", "MP"));
            entityManager.persist(new Districts(330, "Dhar", "MP"));
            entityManager.persist(new Districts(331, "Dindori", "MP"));
            entityManager.persist(new Districts(332, "Guna", "MP"));
            entityManager.persist(new Districts(333, "Gwalior", "MP"));
            entityManager.persist(new Districts(334, "Harda", "MP"));
            entityManager.persist(new Districts(335, "Hoshangabad", "MP"));
            entityManager.persist(new Districts(336, "Indore", "MP"));
            entityManager.persist(new Districts(337, "Jabalpur", "MP"));
            entityManager.persist(new Districts(338, "Jhabua", "MP"));
            entityManager.persist(new Districts(339, "Katni", "MP"));
            entityManager.persist(new Districts(340, "Khandwa", "MP"));
            entityManager.persist(new Districts(341, "Khargone", "MP"));
            entityManager.persist(new Districts(342, "Mandla", "MP"));
            entityManager.persist(new Districts(343, "Mandsaur", "MP"));
            entityManager.persist(new Districts(344, "Morena", "MP"));
            entityManager.persist(new Districts(345, "Narsinghpur", "MP"));
            entityManager.persist(new Districts(346, "Neemuch", "MP"));
            entityManager.persist(new Districts(347, "Panna", "MP"));
            entityManager.persist(new Districts(348, "Raisen", "MP"));
            entityManager.persist(new Districts(349, "Rajgarh", "MP"));
            entityManager.persist(new Districts(350, "Ratlam", "MP"));
            entityManager.persist(new Districts(351, "Rewa", "MP"));
            entityManager.persist(new Districts(352, "Sagar", "MP"));
            entityManager.persist(new Districts(353, "Satna", "MP"));
            entityManager.persist(new Districts(354, "Sehore", "MP"));
            entityManager.persist(new Districts(355, "Seoni", "MP"));
            entityManager.persist(new Districts(356, "Shahdol", "MP"));
            entityManager.persist(new Districts(357, "Shajapur", "MP"));
            entityManager.persist(new Districts(358, "Sheopur", "MP"));
            entityManager.persist(new Districts(359, "Shivpuri", "MP"));
            entityManager.persist(new Districts(360, "Singrauli", "MP"));
            entityManager.persist(new Districts(361, "Tikamgarh", "MP"));
            entityManager.persist(new Districts(362, "Ujjain", "MP"));
            entityManager.persist(new Districts(363, "Umaria", "MP"));
            entityManager.persist(new Districts(364, "Vidisha", "MP"));

// Maharashtra
            entityManager.persist(new Districts(365, "Ahmednagar", "MH"));
            entityManager.persist(new Districts(366, "Akola", "MH"));
            entityManager.persist(new Districts(367, "Amravati", "MH"));
            entityManager.persist(new Districts(368, "Aurangabad", "MH"));
            entityManager.persist(new Districts(369, "Bhandara", "MH"));
            entityManager.persist(new Districts(370, "Beed", "MH"));
            entityManager.persist(new Districts(371, "Buldhana", "MH"));
            entityManager.persist(new Districts(372, "Chandrapur", "MH"));
            entityManager.persist(new Districts(373, "Dhule", "MH"));
            entityManager.persist(new Districts(374, "Gadchiroli", "MH"));
            entityManager.persist(new Districts(375, "Gondia", "MH"));
            entityManager.persist(new Districts(376, "Hingoli", "MH"));
            entityManager.persist(new Districts(377, "Jalgaon", "MH"));
            entityManager.persist(new Districts(378, "Jalna", "MH"));
            entityManager.persist(new Districts(379, "Kolhapur", "MH"));
            entityManager.persist(new Districts(380, "Latur", "MH"));
            entityManager.persist(new Districts(381, "Mumbai City", "MH"));
            entityManager.persist(new Districts(382, "Mumbai Suburban", "MH"));
            entityManager.persist(new Districts(383, "Nagpur", "MH"));
            entityManager.persist(new Districts(384, "Nanded", "MH"));
            entityManager.persist(new Districts(385, "Nandurbar", "MH"));
            entityManager.persist(new Districts(386, "Nashik", "MH"));
            entityManager.persist(new Districts(387, "Osmanabad", "MH"));
            entityManager.persist(new Districts(388, "Palghar", "MH"));
            entityManager.persist(new Districts(389, "Parbhani", "MH"));
            entityManager.persist(new Districts(390, "Pune", "MH"));
            entityManager.persist(new Districts(391, "Raigad", "MH"));
            entityManager.persist(new Districts(392, "Ratnagiri", "MH"));
            entityManager.persist(new Districts(393, "Sangli", "MH"));
            entityManager.persist(new Districts(394, "Satara", "MH"));
            entityManager.persist(new Districts(395, "Sindhudurg", "MH"));
            entityManager.persist(new Districts(396, "Solapur", "MH"));
            entityManager.persist(new Districts(397, "Thane", "MH"));
            entityManager.persist(new Districts(398, "Wardha", "MH"));
            entityManager.persist(new Districts(399, "Washim", "MH"));
            entityManager.persist(new Districts(400, "Yavatmal", "MH"));
            // Manipur
            entityManager.persist(new Districts(401, "Bishnupur", "MN"));
            entityManager.persist(new Districts(402, "Chandel", "MN"));
            entityManager.persist(new Districts(403, "Churachandpur", "MN"));
            entityManager.persist(new Districts(404, "Imphal East", "MN"));
            entityManager.persist(new Districts(405, "Imphal West", "MN"));
            entityManager.persist(new Districts(406, "Jiribam", "MN"));
            entityManager.persist(new Districts(407, "Kakching", "MN"));
            entityManager.persist(new Districts(408, "Kamjong", "MN"));
            entityManager.persist(new Districts(409, "Kangpokpi", "MN"));
            entityManager.persist(new Districts(410, "Noney", "MN"));
            entityManager.persist(new Districts(411, "Pherzawl", "MN"));
            entityManager.persist(new Districts(412, "Senapati", "MN"));
            entityManager.persist(new Districts(413, "Tamenglong", "MN"));
            entityManager.persist(new Districts(414, "Tengnoupal", "MN"));
            entityManager.persist(new Districts(415, "Thoubal", "MN"));
            entityManager.persist(new Districts(416, "Ukhrul", "MN"));

// Meghalaya
            entityManager.persist(new Districts(417, "East Garo Hills", "ML"));
            entityManager.persist(new Districts(418, "East Jaintia Hills", "ML"));
            entityManager.persist(new Districts(419, "East Khasi Hills", "ML"));
            entityManager.persist(new Districts(420, "North Garo Hills", "ML"));
            entityManager.persist(new Districts(421, "Ri Bhoi", "ML"));
            entityManager.persist(new Districts(422, "South Garo Hills", "ML"));
            entityManager.persist(new Districts(423, "South West Garo Hills", "ML"));
            entityManager.persist(new Districts(424, "South West Khasi Hills", "ML"));
            entityManager.persist(new Districts(425, "West Garo Hills", "ML"));
            entityManager.persist(new Districts(426, "West Jaintia Hills", "ML"));
            entityManager.persist(new Districts(427, "West Khasi Hills", "ML"));

// Mizoram
            entityManager.persist(new Districts(428, "Aizawl", "MZ"));
            entityManager.persist(new Districts(429, "Champhai", "MZ"));
            entityManager.persist(new Districts(430, "Kolasib", "MZ"));
            entityManager.persist(new Districts(431, "Lawngtlai", "MZ"));
            entityManager.persist(new Districts(432, "Lunglei", "MZ"));
            entityManager.persist(new Districts(433, "Mamit", "MZ"));
            entityManager.persist(new Districts(434, "Saiha", "MZ"));
            entityManager.persist(new Districts(435, "Serchhip", "MZ"));

// Nagaland
            entityManager.persist(new Districts(436, "Dimapur", "NL"));
            entityManager.persist(new Districts(437, "Kiphire", "NL"));
            entityManager.persist(new Districts(438, "Kohima", "NL"));
            entityManager.persist(new Districts(439, "Longleng", "NL"));
            entityManager.persist(new Districts(440, "Mokokchung", "NL"));
            entityManager.persist(new Districts(441, "Mon", "NL"));
            entityManager.persist(new Districts(442, "Peren", "NL"));
            entityManager.persist(new Districts(443, "Phek", "NL"));
            entityManager.persist(new Districts(444, "Tuensang", "NL"));
            entityManager.persist(new Districts(445, "Wokha", "NL"));
            entityManager.persist(new Districts(446, "Zunheboto", "NL"));

// Odisha
            entityManager.persist(new Districts(447, "Angul", "OD"));
            entityManager.persist(new Districts(448, "Balangir", "OD"));
            entityManager.persist(new Districts(449, "Balasore", "OD"));
            entityManager.persist(new Districts(450, "Bargarh", "OD"));
            entityManager.persist(new Districts(451, "Bhadrak", "OD"));
            entityManager.persist(new Districts(452, "Boudh", "OD"));
            entityManager.persist(new Districts(453, "Cuttack", "OD"));
            entityManager.persist(new Districts(454, "Deogarh", "OD"));
            entityManager.persist(new Districts(455, "Dhenkanal", "OD"));
            entityManager.persist(new Districts(456, "Gajapati", "OD"));
            entityManager.persist(new Districts(457, "Ganjam", "OD"));
            entityManager.persist(new Districts(458, "Jagatsinghapur", "OD"));
            entityManager.persist(new Districts(459, "Jajpur", "OD"));
            entityManager.persist(new Districts(460, "Jharsuguda", "OD"));
            entityManager.persist(new Districts(461, "Kalahandi", "OD"));
            entityManager.persist(new Districts(462, "Kandhamal", "OD"));
            entityManager.persist(new Districts(463, "Kendrapara", "OD"));
            entityManager.persist(new Districts(464, "Kendujhar (Keonjhar)", "OD"));
            entityManager.persist(new Districts(465, "Khordha", "OD"));
            entityManager.persist(new Districts(466, "Koraput", "OD"));
            entityManager.persist(new Districts(467, "Malkangiri", "OD"));
            entityManager.persist(new Districts(468, "Mayurbhanj", "OD"));
            entityManager.persist(new Districts(469, "Nabarangpur", "OD"));
            entityManager.persist(new Districts(470, "Nayagarh", "OD"));
            entityManager.persist(new Districts(471, "Nuapada", "OD"));
            entityManager.persist(new Districts(472, "Puri", "OD"));
            entityManager.persist(new Districts(473, "Rayagada", "OD"));
            entityManager.persist(new Districts(474, "Sambalpur", "OD"));
            entityManager.persist(new Districts(475, "Sonepur", "OD"));
            entityManager.persist(new Districts(476, "Sundargarh", "OD"));

// Puducherry
            entityManager.persist(new Districts(477, "Karaikal", "PY"));
            entityManager.persist(new Districts(478, "Mahe", "PY"));
            entityManager.persist(new Districts(479, "Pondicherry", "PY"));
            entityManager.persist(new Districts(480, "Yanam", "PY"));

// Punjab
            entityManager.persist(new Districts(481, "Amritsar", "PB"));
            entityManager.persist(new Districts(482, "Barnala", "PB"));
            entityManager.persist(new Districts(483, "Bathinda", "PB"));
            entityManager.persist(new Districts(484, "Faridkot", "PB"));
            entityManager.persist(new Districts(485, "Fatehgarh Sahib", "PB"));
            entityManager.persist(new Districts(486, "Fazilka", "PB"));
            entityManager.persist(new Districts(487, "Ferozepur", "PB"));
            entityManager.persist(new Districts(488, "Gurdaspur", "PB"));
            entityManager.persist(new Districts(489, "Hoshiarpur", "PB"));
            entityManager.persist(new Districts(490, "Jalandhar", "PB"));
            entityManager.persist(new Districts(491, "Kapurthala", "PB"));
            entityManager.persist(new Districts(492, "Ludhiana", "PB"));
            entityManager.persist(new Districts(493, "Mansa", "PB"));
            entityManager.persist(new Districts(494, "Moga", "PB"));
            entityManager.persist(new Districts(495, "Muktsar", "PB"));
            entityManager.persist(new Districts(496, "Nawanshahr (Shahid Bhagat Singh Nagar)", "PB"));
            entityManager.persist(new Districts(497, "Pathankot", "PB"));
            entityManager.persist(new Districts(498, "Patiala", "PB"));
            entityManager.persist(new Districts(499, "Rupnagar", "PB"));
            entityManager.persist(new Districts(500, "Sahibzada Ajit Singh Nagar (Mohali)", "PB"));
            entityManager.persist(new Districts(501, "Sangrur", "PB"));
            entityManager.persist(new Districts(502, "Tarn Taran", "PB"));

// Rajasthan
            entityManager.persist(new Districts(503, "Ajmer", "RJ"));
            entityManager.persist(new Districts(504, "Alwar", "RJ"));
            entityManager.persist(new Districts(505, "Banswara", "RJ"));
            entityManager.persist(new Districts(506, "Baran", "RJ"));
            entityManager.persist(new Districts(507, "Barmer", "RJ"));
            entityManager.persist(new Districts(508, "Bharatpur", "RJ"));
            entityManager.persist(new Districts(509, "Bhilwara", "RJ"));
            entityManager.persist(new Districts(510, "Bikaner", "RJ"));
            entityManager.persist(new Districts(511, "Bundi", "RJ"));
            entityManager.persist(new Districts(512, "Chittorgarh", "RJ"));
            entityManager.persist(new Districts(513, "Churu", "RJ"));
            entityManager.persist(new Districts(514, "Dausa", "RJ"));
            entityManager.persist(new Districts(515, "Dholpur", "RJ"));
            entityManager.persist(new Districts(516, "Dungarpur", "RJ"));
            entityManager.persist(new Districts(517, "Hanumangarh", "RJ"));
            entityManager.persist(new Districts(518, "Jaipur", "RJ"));
            entityManager.persist(new Districts(519, "Jaisalmer", "RJ"));
            entityManager.persist(new Districts(520, "Jalore", "RJ"));
            entityManager.persist(new Districts(521, "Jhalawar", "RJ"));
            entityManager.persist(new Districts(522, "Jhunjhunu", "RJ"));
            entityManager.persist(new Districts(523, "Jodhpur", "RJ"));
            entityManager.persist(new Districts(524, "Karauli", "RJ"));
            entityManager.persist(new Districts(525, "Kota", "RJ"));
            entityManager.persist(new Districts(526, "Nagaur", "RJ"));
            entityManager.persist(new Districts(527, "Pali", "RJ"));
            entityManager.persist(new Districts(528, "Pratapgarh", "RJ"));
            entityManager.persist(new Districts(529, "Rajsamand", "RJ"));
            entityManager.persist(new Districts(530, "Sawai Madhopur", "RJ"));
            entityManager.persist(new Districts(531, "Sikar", "RJ"));
            entityManager.persist(new Districts(532, "Sirohi", "RJ"));
            entityManager.persist(new Districts(533, "Sri Ganganagar", "RJ"));
            entityManager.persist(new Districts(534, "Tonk", "RJ"));
            entityManager.persist(new Districts(535, "Udaipur", "RJ"));

// Sikkim
            entityManager.persist(new Districts(536, "East SK", "SK"));
            entityManager.persist(new Districts(537, "North SK", "SK"));
            entityManager.persist(new Districts(538, "South SK", "SK"));
            entityManager.persist(new Districts(539, "West SK", "SK"));

// Tamil Nadu
            entityManager.persist(new Districts(540, "Ariyalur", "TN"));
            entityManager.persist(new Districts(541, "Chennai", "TN"));
            entityManager.persist(new Districts(542, "Coimbatore", "TN"));
            entityManager.persist(new Districts(543, "Cuddalore", "TN"));
            entityManager.persist(new Districts(544, "Dharmapuri", "TN"));
            entityManager.persist(new Districts(545, "Dindigul", "TN"));
            entityManager.persist(new Districts(546, "Erode", "TN"));
            entityManager.persist(new Districts(547, "Kanchipuram", "TN"));
            entityManager.persist(new Districts(548, "Kanyakumari", "TN"));
            entityManager.persist(new Districts(549, "Karur", "TN"));
            entityManager.persist(new Districts(550, "Krishnagiri", "TN"));
            entityManager.persist(new Districts(551, "Madurai", "TN"));
            entityManager.persist(new Districts(552, "Nagapattinam", "TN"));
            entityManager.persist(new Districts(553, "Namakkal", "TN"));
            entityManager.persist(new Districts(554, "Nilgiris", "TN"));
            entityManager.persist(new Districts(555, "Perambalur", "TN"));
            entityManager.persist(new Districts(556, "Pudukkottai", "TN"));
            entityManager.persist(new Districts(557, "Ramanathapuram", "TN"));
            entityManager.persist(new Districts(558, "Salem", "TN"));
            entityManager.persist(new Districts(559, "Sivaganga", "TN"));
            entityManager.persist(new Districts(560, "Thanjavur", "TN"));
            entityManager.persist(new Districts(561, "Theni", "TN"));
            entityManager.persist(new Districts(562, "Thoothukudi (Tuticorin)", "TN"));
            entityManager.persist(new Districts(563, "Tiruchirappalli", "TN"));
            entityManager.persist(new Districts(564, "Tirunelveli", "TN"));
            entityManager.persist(new Districts(565, "Tiruppur", "TN"));
            entityManager.persist(new Districts(566, "Tiruvallur", "TN"));
            entityManager.persist(new Districts(567, "Tiruvannamalai", "TN"));
            entityManager.persist(new Districts(568, "Tiruvarur", "TN"));
            entityManager.persist(new Districts(569, "Vellore", "TN"));
            entityManager.persist(new Districts(570, "Viluppuram", "TN"));
            entityManager.persist(new Districts(571, "Virudhunagar", "TN"));

            // Telangana
            entityManager.persist(new Districts(572, "Adilabad", "TS"));
            entityManager.persist(new Districts(573, "Bhadradri Kothagudem", "TS"));
            entityManager.persist(new Districts(574, "Hyderabad", "TS"));
            entityManager.persist(new Districts(575, "Jagtial", "TS"));
            entityManager.persist(new Districts(576, "Jangaon", "TS"));
            entityManager.persist(new Districts(577, "Jayashankar Bhoopalpally", "TS"));
            entityManager.persist(new Districts(578, "Jogulamba Gadwal", "TS"));
            entityManager.persist(new Districts(579, "Kamareddy", "TS"));
            entityManager.persist(new Districts(580, "Karimnagar", "TS"));
            entityManager.persist(new Districts(581, "Khammam", "TS"));
            entityManager.persist(new Districts(582, "Komaram Bheem Asifabad", "TS"));
            entityManager.persist(new Districts(583, "Mahabubabad", "TS"));
            entityManager.persist(new Districts(584, "Mahabubnagar", "TS"));
            entityManager.persist(new Districts(585, "Mancherial", "TS"));
            entityManager.persist(new Districts(586, "Medak", "TS"));
            entityManager.persist(new Districts(587, "Medchal", "TS"));
            entityManager.persist(new Districts(588, "Nagarkurnool", "TS"));
            entityManager.persist(new Districts(589, "Nalgonda", "TS"));
            entityManager.persist(new Districts(590, "Nirmal", "TS"));
            entityManager.persist(new Districts(591, "Nizamabad", "TS"));
            entityManager.persist(new Districts(592, "Peddapalli", "TS"));
            entityManager.persist(new Districts(593, "Rajanna Sircilla", "TS"));
            entityManager.persist(new Districts(594, "Rangareddy", "TS"));
            entityManager.persist(new Districts(595, "Sangareddy", "TS"));
            entityManager.persist(new Districts(596, "Siddipet", "TS"));
            entityManager.persist(new Districts(597, "Suryapet", "TS"));
            entityManager.persist(new Districts(598, "Vikarabad", "TS"));
            entityManager.persist(new Districts(599, "Wanaparthy", "TS"));
            entityManager.persist(new Districts(600, "Warangal (Rural)", "TS"));
            entityManager.persist(new Districts(601, "Warangal (Urban)", "TS"));
            entityManager.persist(new Districts(602, "Yadadri Bhuvanagiri", "TS"));

// Tripura
            entityManager.persist(new Districts(603, "Dhalai", "TR"));
            entityManager.persist(new Districts(604, "Gomati", "TR"));
            entityManager.persist(new Districts(605, "Khowai", "TR"));
            entityManager.persist(new Districts(606, "North Tripura", "TR"));
            entityManager.persist(new Districts(607, "Sepahijala", "TR"));
            entityManager.persist(new Districts(608, "South Tripura", "TR"));
            entityManager.persist(new Districts(609, "Unakoti", "TR"));
            entityManager.persist(new Districts(610, "West Tripura", "TR"));

// Uttarakhand
            entityManager.persist(new Districts(611, "Almora", "UK"));
            entityManager.persist(new Districts(612, "Bageshwar", "UK"));
            entityManager.persist(new Districts(613, "Chamoli", "UK"));
            entityManager.persist(new Districts(614, "Champawat", "UK"));
            entityManager.persist(new Districts(615, "Dehradun", "UK"));
            entityManager.persist(new Districts(616, "Haridwar", "UK"));
            entityManager.persist(new Districts(617, "Nainital", "UK"));
            entityManager.persist(new Districts(618, "Pauri Garhwal", "UK"));
            entityManager.persist(new Districts(619, "Pithoragarh", "UK"));
            entityManager.persist(new Districts(620, "Rudraprayag", "UK"));
            entityManager.persist(new Districts(621, "Tehri Garhwal", "UK"));
            entityManager.persist(new Districts(622, "Udham Singh Nagar", "UK"));
            entityManager.persist(new Districts(623, "Uttarkashi", "UK"));

// Uttar Pradesh
            entityManager.persist(new Districts(624, "Agra", "UP"));
            entityManager.persist(new Districts(625, "Aligarh", "UP"));
            entityManager.persist(new Districts(626, "Allahabad", "UP"));
            entityManager.persist(new Districts(627, "Ambedkar Nagar", "UP"));
            entityManager.persist(new Districts(628, "Amethi (Chatrapati Sahuji Mahraj Nagar)", "UP"));
            entityManager.persist(new Districts(629, "Amroha (J.P. Nagar)", "UP"));
            entityManager.persist(new Districts(630, "Auraiya", "UP"));
            entityManager.persist(new Districts(631, "Azamgarh", "UP"));
            entityManager.persist(new Districts(632, "Baghpat", "UP"));
            entityManager.persist(new Districts(633, "Bahraich", "UP"));
            entityManager.persist(new Districts(634, "Ballia", "UP"));
            entityManager.persist(new Districts(635, "Balrampur", "UP"));
            entityManager.persist(new Districts(636, "Banda", "UP"));
            entityManager.persist(new Districts(637, "Barabanki", "UP"));
            entityManager.persist(new Districts(638, "Bareilly", "UP"));
            entityManager.persist(new Districts(639, "Basti", "UP"));
            entityManager.persist(new Districts(640, "Bhadohi", "UP"));
            entityManager.persist(new Districts(641, "Bijnor", "UP"));
            entityManager.persist(new Districts(642, "Budaun", "UP"));
            entityManager.persist(new Districts(643, "Bulandshahr", "UP"));
            entityManager.persist(new Districts(644, "Chandauli", "UP"));
            entityManager.persist(new Districts(645, "Chitrakoot", "UP"));
            entityManager.persist(new Districts(646, "Deoria", "UP"));
            entityManager.persist(new Districts(647, "Etah", "UP"));
            entityManager.persist(new Districts(648, "Etawah", "UP"));
            entityManager.persist(new Districts(649, "Faizabad", "UP"));
            entityManager.persist(new Districts(650, "Farrukhabad", "UP"));
            entityManager.persist(new Districts(651, "Fatehpur", "UP"));
            entityManager.persist(new Districts(652, "Firozabad", "UP"));
            entityManager.persist(new Districts(653, "Gautam Buddha Nagar", "UP"));
            entityManager.persist(new Districts(654, "Ghaziabad", "UP"));
            entityManager.persist(new Districts(655, "Ghazipur", "UP"));
            entityManager.persist(new Districts(656, "Gonda", "UP"));
            entityManager.persist(new Districts(657, "Gorakhpur", "UP"));
            entityManager.persist(new Districts(658, "Hamirpur", "UP"));
            entityManager.persist(new Districts(659, "Hapur (Panchsheel Nagar)", "UP"));
            entityManager.persist(new Districts(660, "Hardoi", "UP"));
            entityManager.persist(new Districts(661, "Hathras", "UP"));
            entityManager.persist(new Districts(662, "Jalaun", "UP"));
            entityManager.persist(new Districts(663, "Jaunpur", "UP"));
            entityManager.persist(new Districts(664, "Jhansi", "UP"));
            entityManager.persist(new Districts(665, "Kannauj", "UP"));
            entityManager.persist(new Districts(666, "Kanpur Dehat", "UP"));
            entityManager.persist(new Districts(667, "Kanpur Nagar", "UP"));
            entityManager.persist(new Districts(668, "Kanshiram Nagar (Kasganj)", "UP"));
            entityManager.persist(new Districts(669, "Kaushambi", "UP"));
            entityManager.persist(new Districts(670, "Kushinagar (Padrauna)", "UP"));
            entityManager.persist(new Districts(671, "Lakhimpur - Kheri", "UP"));
            entityManager.persist(new Districts(672, "Lalitpur", "UP"));
            entityManager.persist(new Districts(673, "Lucknow", "UP"));
            entityManager.persist(new Districts(674, "Maharajganj", "UP"));
            entityManager.persist(new Districts(675, "Mahoba", "UP"));
            entityManager.persist(new Districts(676, "Mainpuri", "UP"));
            entityManager.persist(new Districts(677, "Mathura", "UP"));
            entityManager.persist(new Districts(678, "Mau", "UP"));
            entityManager.persist(new Districts(679, "Meerut", "UP"));
            entityManager.persist(new Districts(680, "Mirzapur", "UP"));
            entityManager.persist(new Districts(681, "Moradabad", "UP"));
            entityManager.persist(new Districts(682, "Muzaffarnagar", "UP"));
            entityManager.persist(new Districts(683, "Pilibhit", "UP"));
            entityManager.persist(new Districts(684, "Pratapgarh", "UP"));
            entityManager.persist(new Districts(685, "RaeBareli", "UP"));
            entityManager.persist(new Districts(686, "Rampur", "UP"));
            entityManager.persist(new Districts(687, "Saharanpur", "UP"));
            entityManager.persist(new Districts(688, "Sambhal (Bhim Nagar)", "UP"));
            entityManager.persist(new Districts(689, "Sant Kabir Nagar", "UP"));
            entityManager.persist(new Districts(690, "Shahjahanpur", "UP"));
            entityManager.persist(new Districts(691, "Shamali (Prabuddh Nagar)", "UP"));
            entityManager.persist(new Districts(692, "Shravasti", "UP"));
            entityManager.persist(new Districts(693, "Siddharth Nagar", "UP"));
            entityManager.persist(new Districts(694, "Sitapur", "UP"));
            entityManager.persist(new Districts(695, "Sonbhadra", "UP"));
            entityManager.persist(new Districts(696, "Sultanpur", "UP"));
            entityManager.persist(new Districts(697, "Unnao", "UP"));
            entityManager.persist(new Districts(698, "Varanasi", "UP"));

// West Bengal

            entityManager.persist(new Districts(699, "Alipurduar", "WB"));
            entityManager.persist(new Districts(700, "Bankura", "WB"));
            entityManager.persist(new Districts(701, "Birbhum", "WB"));
            entityManager.persist(new Districts(702, "Cooch Behar", "WB"));
            entityManager.persist(new Districts(703, "Dakshin Dinajpur", "WB"));
            entityManager.persist(new Districts(704, "Hooghly", "WB"));
            entityManager.persist(new Districts(705, "Howrah", "WB"));
            entityManager.persist(new Districts(706, "Jalpaiguri", "WB"));
            entityManager.persist(new Districts(707, "Jhargram", "WB"));
            entityManager.persist(new Districts(708, "Kalimpong", "WB"));
            entityManager.persist(new Districts(709, "Kolkata", "WB"));
            entityManager.persist(new Districts(710, "Maldah", "WB"));
            entityManager.persist(new Districts(711, "Murshidabad", "WB"));
            entityManager.persist(new Districts(712, "Nadia", "WB"));
            entityManager.persist(new Districts(713, "North 24 Parganas", "WB"));
            entityManager.persist(new Districts(714, "Paschim Bardhaman", "WB"));
            entityManager.persist(new Districts(715, "Paschim Medinipur", "WB"));
            entityManager.persist(new Districts(716, "Purba Bardhaman", "WB"));
            entityManager.persist(new Districts(717, "Purba Medinipur", "WB"));
            entityManager.persist(new Districts(718, "South 24 Parganas", "WB"));
            entityManager.persist(new Districts(719, "Uttar Dinajpur", "WB"));
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
            entityManager.persist(new StateCode(29, "Jammu and Kashmir", "JK"));

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
            entityManager.persist(new Qualification(1L, "MATRICULATION/10th", "Completed secondary education or equivalent"));
            entityManager.persist(new Qualification(2L, "INTERMEDIATE/12th", "Completed higher secondary education or equivalent"));
            entityManager.persist(new Qualification(3L, "BACHELORS", "Completed undergraduate degree program"));
            entityManager.persist(new Qualification(4L, "MASTERS", "Completed postgraduate degree program"));
            entityManager.persist(new Qualification(5L, "DOCTORATE", "Completed doctoral degree program"));
            entityManager.persist(new Qualification(6L, "DIPLOMA", "Completed an undergraduate or vocational course, certifying knowledge and skills in a specific field."));
            entityManager.persist(new Qualification(7L, "ITI", "Completed Industrial Training Institute (ITI) certification, typically required for vocational and technical qualifications."));

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
        if(count==0)
        {
            entityManager.merge(new CustomAdmin(1L,2,passwordEncoder.encode("Admin#01"),"admin","7740066387","+91",0,now,"SUPER_ADMIN"));
            entityManager.merge(new CustomAdmin(2L,1,passwordEncoder.encode("SuperAdmin#1357"),"superadmin","9872548680","+91",0,now,"SUPER_ADMIN"));
            entityManager.merge(new CustomAdmin(3L,3,passwordEncoder.encode("AdminServiceProvider#02"),"adminserviceprovider","7710393096","+91",0,now,"SUPER_ADMIN"));
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
            entityManager.persist(new DocumentType(1,  "Aadhaar_Card_Front","Front side of a government-issued ID card in India.", "200KB", "100KB"));
            entityManager.persist(new DocumentType(2,  "Pan_Card","A permanent account number card for tax purposes in India.",  "200KB", "100KB"));
            entityManager.persist(new DocumentType(3,  "Live_Passport_Size_Photo","A live photo typically used for official documents.",  "200KB", "100KB"));
            entityManager.persist(new DocumentType(4,  "Signature", "A handwritten sign used to authenticate documents.", "200KB", "100KB"));
            entityManager.persist(new DocumentType(5,  "Ews_Certificate","Certificate for individuals and families below a certain income threshold to access various benefits and concessions.",  "500KB", "100KB"));
            entityManager.persist(new DocumentType(6,  "Diploma", "Completed an undergraduate or vocational course, certifying knowledge and skills in a specific field.", "300KB", "200KB"));
            entityManager.persist(new DocumentType(7,  "Graduation","Awarded upon completion of a degree program, signifying fulfillment of academic requirements in a specific discipline.",  "300KB", "200KB"));
            entityManager.persist(new DocumentType(8,  "Post_Graduation",  "Issued after completing a postgraduate degree, acknowledging advanced training in a specialized field.", "300KB", "200KB"));
            entityManager.persist(new DocumentType(9,  "Caste_Certificate","Certifies an individual's caste for reservations and benefits in education and employment.",  "500KB", "100KB"));
            entityManager.persist(new DocumentType(10, "Address_Certificate","Verifies an individual’s residential address for identity verification and other purposes.",  "500KB", "100KB"));
            entityManager.persist(new DocumentType(11, "Income_Certificate","Confirms an individual’s or family’s annual income for applying for government benefits and financial assistance.",  "500KB", "100KB"));
            entityManager.persist(new DocumentType(12, "Driving_License","Authorizes an individual to operate motor vehicles, confirming knowledge of traffic laws and vehicle operation skills.",  "200KB", "100KB"));
            entityManager.persist(new DocumentType(13, "Others", "Includes other document types not listed above, tailored to specific needs or contexts.", "200KB", "100KB"));
            entityManager.persist(new DocumentType(14, "Matriculation/10th","Completed secondary education or equivalent.",  "300KB", "200KB"));
            entityManager.persist(new DocumentType(15, "Intermediate/12th", "Completed higher secondary education or equivalent.", "300KB", "200KB"));
            entityManager.persist(new DocumentType(16, "Bachelors", "Completed undergraduate degree program education.", "300KB", "200KB"));
            entityManager.persist(new DocumentType(17, "Masters","Completed postgraduate degree program education.",  "300KB", "200KB"));
            entityManager.persist(new DocumentType(18, "Doctorate", "Completed doctoral degree program education.", "300KB", "200KB"));
            entityManager.persist(new DocumentType(19, "Domicile", "The permanent home or principal residence of a person.", "500KB", "100KB"));
            entityManager.persist(new DocumentType(20, "Disability_Certificate","An outdated term for individuals with physical or mental disabilities; 'person with a disability' is preferred today.",  "500KB", "100KB"));
            entityManager.persist(new DocumentType(21, "C-Form_Photo", "A C Form photo is a standardized ID photo for official documents.", "200KB", "100KB"));
            entityManager.persist(new DocumentType(22, "Ex-Service_Men","Ex-Service Men document is required for individuals who have previously worked in the organization and are now no longer employed.",  "500KB", "100KB"));
            entityManager.persist(new DocumentType(23, "Business_Photo","A Standard proof of Running Business.",  "200KB", "100KB"));
            entityManager.persist(new DocumentType(24, "Personal_Photo", "A Personal Photograph of SP.", "200KB", "100KB"));
            entityManager.persist(new DocumentType(25, "NCC_Certificate_A","NCC CERTIFICATE A.",  "500KB", "100KB"));
            entityManager.persist(new DocumentType(26, "NCC_Certificate_B", "NCC CERTIFICATE B.", "500KB", "100KB"));
            entityManager.persist(new DocumentType(27, "NCC_Certificate_C", "NCC CERTIFICATE C.", "500KB", "100KB"));
            entityManager.persist(new DocumentType(28, "NSS_Certificate_A","NSS CERTIFICATE A",  "500KB", "100KB"));
            entityManager.persist(new DocumentType(29, "Sports_Certificate-State","SPORTS CERTIFICATE FOR STATE LEVEL",  "200KB", "100KB"));
            entityManager.persist(new DocumentType(30, "Sports_Certificate-Centre", "SPORTS CERTIFICATE FOR CENTRE LEVEL", "200KB", "100KB"));
            entityManager.persist(new DocumentType(31, "Aadhaar_Card_Backside", "Back side of a government-issued ID card in India.", "200KB", "100KB"));
            entityManager.persist(new DocumentType(32, "Left_Thumb_Impression", "The left thumb impression of the individual, typically required for identity verification in official documents.", "200KB", "100KB"));
            entityManager.persist(new DocumentType(33, "Right_Thumb_Impression", "The right thumb impression of the individual, typically required for identity verification in official documents.", "200KB", "100KB"));
            entityManager.persist(new DocumentType(34, "ITI", "Completed Industrial Training Institute (ITI) certification, typically required for vocational and technical qualifications.", "300KB", "200KB"));
            entityManager.persist(new DocumentType(35, "White_Background_Passport_Size_Photo", "A white background passport size photo typically used for official documents", "200KB", "100KB"));
            entityManager.persist(new DocumentType(36, "NSS_Certificate_B","NSS CERTIFICATE B",  "500KB", "100KB"));
            entityManager.persist(new DocumentType(37, "NSS_Certificate_C","NSS CERTIFICATE C",  "500KB", "100KB"));

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
                    "(1, 2),(1,4), (2, 3), (3, 1), (3, 2),(3,4), (4,2),(4,4), " +
                    "(5 , 3), (6,2), (6,4),(7, 2),(7,4), (8, 2),(8,4), (9, 1), " +
                    "(9, 2),(9,4),(10, 1), (10, 2),(10,4), (11, 1), (11, 2),(11,4), " +
                    "(12, 1), (12, 2),(12,4), (13, 1), (13, 2),(13,4), (14, 2), (14,4)," +
                    "(15, 2),(15,4), (16, 2),(16,4), (17, 2),(17,4),(18, 2),(18,4), (19, 2),(19,4), (20, 2),(20,4), " +
                    "(22,2),(22,4), (21, 1), (21, 2),(21,4), (23, 1), (23, 2),(23,4)," +
                    "(24, 1), (24, 2), (24,4),(25, 1), (25, 2),(25,4), (26, 1), " +
                    "(26, 2),(26,4), (27, 1), (27, 2),(27,4),(28, 1), (28, 2),  (28,4)," +
                    "(29, 1), (29, 2),(29,4), (30, 1), (30, 2),(30,4),(31,2),(31,4)," +
                    "(32,2),(32,4),(33,2),(33,4),(34,2),(34,4),(35,2),(35,4),"+
                    "(36,2),(36,4),(37,2),(37,4)";

            entityManager.createNativeQuery(sql).executeUpdate();
        }
        count = entityManager.createQuery("SELECT count(b) FROM BoardUniversity b", Long.class).getSingleResult();
        if (count == 0) {

            entityManager.merge(new BoardUniversity(1L, "Delhi University", "Delhi", "DU", "UNIVERSITY", now, now, "SUPER_ADMIN", "SUPER_ADMIN"));
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


        String alterQuery = "ALTER TABLE custom_customer ALTER COLUMN token TYPE VARCHAR(512)";
        javax.persistence.Query query = entityManager.createNativeQuery(alterQuery);
        query.executeUpdate();
    }
}
