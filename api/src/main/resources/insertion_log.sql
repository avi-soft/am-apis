DO $$
BEGIN
    -- Check if there are no records in custom_zones
    IF (SELECT COUNT(*) FROM custom_zones) = 0 THEN
        -- Insert zones into custom_zones table
        INSERT INTO custom_zones (zone_id, zone_name) VALUES
            (1, 'NORTH ZONE'),
            (2, 'SOUTH ZONE'),
            (3, 'EAST ZONE'),
            (4, 'WEST ZONE'),
            (5, 'CENTRAL ZONE'),
            (6, 'NORTH-EAST ZONE'),
            (7, 'SPECIAL UNION TERRITORIES ZONE');
    END IF;

    -- Check if there are no records in custom_state_codes
    IF (SELECT COUNT(*) FROM custom_state_codes) = 0 THEN
        -- Insert data into custom_state_codes table
        INSERT INTO custom_state_codes (state_code_id, state_name, state_code) VALUES
            (1, 'Andhra Pradesh', 'AP'),
            (2, 'Arunachal Pradesh', 'AR'),
            (3, 'Assam', 'AS'),
            (4, 'Bihar', 'BR'),
            (5, 'Chhattisgarh', 'CG'),
            (6, 'Goa', 'GA'),
            (7, 'Gujarat', 'GJ'),
            (8, 'Haryana', 'HR'),
            (9, 'Himachal Pradesh', 'HP'),
            (10, 'Jharkhand', 'JH'),
            (11, 'Karnataka', 'KA'),
            (12, 'Kerala', 'KL'),
            (13, 'Madhya Pradesh', 'MP'),
            (14, 'Maharashtra', 'MH'),
            (15, 'Manipur', 'MN'),
            (16, 'Meghalaya', 'ML'),
            (17, 'Mizoram', 'MZ'),
            (18, 'Nagaland', 'NL'),
            (19, 'Odisha', 'OD'),
            (20, 'Punjab', 'PB'),
            (21, 'Rajasthan', 'RJ'),
            (22, 'Sikkim', 'SK'),
            (23, 'Tamil Nadu', 'TN'),
            (24, 'Telangana', 'TS'),
            (25, 'Tripura', 'TR'),
            (26, 'Uttar Pradesh', 'UP'),
            (27, 'Uttarakhand', 'UK'),
            (28, 'West Bengal', 'WB'),
            (29, 'Jammu and Kashmir', 'JK'),
            (30, 'Andaman and Nicobar Islands', 'AN'),
            (31, 'Chandigarh', 'CH'),
            (32, 'Dadra and Nagar Haveli and Daman and Diu', 'DN'),
            (33, 'Lakshadweep', 'LD'),
            (34, 'Delhi', 'DL'),
            (35, 'Puducherry', 'PY'),
            (36, 'Daman and Diu', 'DD');
    END IF;
	IF (SELECT COUNT(*) FROM custom_product_state) = 0 THEN
    INSERT INTO custom_product_state (product_state_id, product_state, product_state_description)
    VALUES
        (1, 'NEW', 'New State.'),
        (2, 'MODIFIED', 'Modified State.'),
        (3, 'APPROVED', 'Approved State.'),
        (4, 'REJECTED', 'Rejected State.'),
        (5, 'LIVE', 'Live State.'),
        (6, 'EXPIRED', 'Expired State.'),
        (7, 'DRAFT', 'Draft State.');
     END IF;

	 IF (SELECT COUNT(*) FROM custom_ticket_state) = 0 THEN
    INSERT INTO custom_ticket_state (ticket_state_id, ticket_state, ticket_state_description)
    VALUES
        (1, 'TO-DO', 'Ticket is not assigned to any service provider'),
        (2, 'IN-PROGRESS', 'It is  under progress'),
        (3, 'ON-HOLD', 'It is on hold'),
        (4, 'IN-REVIEW', 'It is rejected'),
        (5, 'CLOSE', 'Closed successfully');
END IF;
IF (SELECT COUNT(*) FROM custom_ticket_status) = 0 THEN
    INSERT INTO custom_ticket_status (ticket_status_id, ticket_status, ticket_status_description)
    VALUES
        (0, 'TO-DO', 'Ticket not assigned'),
        (1, 'NOT-REACHABLE', 'User is unreachable'),
        (2, 'VALIDATING-DOCUMENT', 'Validating documents'),
        (3, 'MISSING-DOCUMENT', 'Missing documents'),
        (4, 'USER-NOT-REACHABLE', 'User Not reachable'),
        (5, 'UPLOADING-DOCUMENT', 'Uploading documents'),
        (6, 'FILLING-PERSONAL-DETAILS', 'Filling personal details'),
        (7, 'SOME-OTHER-STATUS', 'Some other status'),
        (8, 'COMPLETED', 'Successfully Completed'),
        (9, 'INCOMPLETE', 'Can not complete'),
        (10, 'FORM-COMPLETED-REVIEW', 'Form is completed but requires review'),
        (11, 'PROVIDER-HELP-REVIEW', 'SP is stuck');
END IF;


IF (SELECT COUNT(*) FROM custom_ticket_type) = 0 THEN
    INSERT INTO custom_ticket_type (ticket_type_id, ticket_type, ticekt_type_description)
    VALUES
        (1, 'PRIMARY', 'Primary ticket of SP'),
        (2, 'REVIEW-TICKET', 'Review ticket of SP'),
        (3, 'MISCELLANEOUS', 'Miscellaneous (any other ticket)');
END IF;

IF (SELECT COUNT(*) FROM order_state_ref) = 0 THEN
    INSERT INTO order_state_ref (order_state_id, order_state_name, order_state_description)
    VALUES
        (1, 'NEW', 'Order is generated'),
        (2, 'AUTO_ASSIGNED', 'Order automatically assigned.'),
        (3, 'UNASSIGNED', 'Order is unassigned.'),
        (4, 'ASSIGNED', 'Order assigned.'),
        (5, 'RETURNED', 'Order returned.'),
        (6, 'IN_PROGRESS', 'Order is in progress.'),
        (7, 'COMPLETED', 'Order completed.'),
        (8, 'IN_REVIEW', 'Order is in review.');
END IF;
IF (SELECT COUNT(*) FROM order_ticket_linkage) = 0 THEN
    INSERT INTO order_ticket_linkage (linkage_id, order_state_id, ticket_state_id, ticket_status_id)
    VALUES
        (1, 1, 0, 0),
        (2, 3, 1, 0),
        (3, 4, 2, 0),
        (4, 6, 2, 2),
        (5, 6, 2, 5),
        (6, 6, 3, 3),
        (7, 6, 3, 4),
        (8, 6, 2, 6),
        (9, 7, 5, 8),
        (10, 7, 5, 9),
        (11, 6, 4, 10),
        (12, 6, 4, 11);
END IF;

IF (SELECT COUNT(*) FROM custom_order_status) = 0 THEN
    INSERT INTO custom_order_status (order_status_id, order_status_name, order_state_id, order_status_description)
    VALUES
        (1, 'AUTO_ASSIGNED', 2, 'Order automatically assigned.'),
        (2, 'UNASSIGNED', 3, 'Order is unassigned.'),
        (3, 'ASSIGNED_BY_SUPER_ADMIN', 4, 'Order assigned by super admin.'),
        (4, 'ASSIGNED_BY_AUTO_ASSIGNER', 4, 'Order assigned by Auto Assigner.'),
        (5, 'CANNOT_BE_DONE', 5, 'Order cannot be done.'),
        (6, 'DUPLICATE_ORDER', 5, 'Order is a duplicate.'),
        (7, 'IN_PROGRESS', 6, 'Order is in progress.'),
        (8, 'FULFILLED', 7, 'Order fulfilled.'),
        (9, 'DUPLICATE', 7, 'Order duplicate.'),
        (10, 'DUMMY_ORDER', 7, 'Order not valid or created as a test.'),
        (11, 'STUDENT_UNREACHABLE', 7, 'Order could not be completed because the student/customer was not reachable.'),
        (12, 'DOCUMENT_NOT_AVAILABLE', 7, 'Necessary document to complete the order was unavailable.'),
        (13, 'NEW_ORDER', 1, 'New Order Generated');
END IF;

IF (SELECT COUNT(*) FROM custom_job_group) = 0 THEN
    INSERT INTO custom_job_group (job_group_id, job_group, job_group_description)
    VALUES
        (1, 'A', 'Executive Management'),
        (2, 'B', 'Professional and Technical'),
        (3, 'C', 'Administrative and Support'),
        (4, 'D', 'Entry-Level and Labor');
END IF;

IF (SELECT COUNT(*) FROM custom_application_scope) = 0 THEN
    INSERT INTO custom_application_scope (application_scope_id, application_scope, application_scope_description)
    VALUES
        (1, 'STATE', 'State level operations.'),
        (2, 'CENTER', 'Center level operations.');
END IF;

IF (SELECT COUNT(*) FROM custom_reserve_category) = 0 THEN
    INSERT INTO custom_reserve_category (reserve_category_id, reserve_category_name, reserve_category_description, is_default_category)
    VALUES
        (1, 'GEN', 'General', true),
        (2, 'SC', 'Schedule Caste', false),
        (3, 'ST', 'Schedule Tribe', false),
        (4, 'OBC', 'Other Backward Caste', false),
        (5, 'OTHERS', 'Others', false),
        (6, 'EWS', 'Economically Weaker Section', false),
		(7, 'N/A', 'None of the above', false);
END IF;

IF (SELECT COUNT(*) FROM custom_product_rejection_status) = 0 THEN
    INSERT INTO custom_product_rejection_status (rejection_status_id, rejection_status, rejection_status_descriptio)
    VALUES
        (1, 'TO-BE-MODIFIED', 'Product needs modification to get approved.'),
        (2, 'DUPLICATE', 'There is already a product present with these details.'),
        (3, 'IRRELEVANT', 'The product is irrelevant.'),
        (4, 'UNFEASIBLE', 'The product is not feasible to exist.');
END IF;

IF (SELECT COUNT(*) FROM custom_gender) = 0 THEN
    INSERT INTO custom_gender (gender_id, gender_code, gender_name)
    VALUES
        (1, 'M', 'MALE'),
        (2, 'F', 'FEMALE'),
		(3, 'O', 'OTHERS'),
        (4, 'N/A', 'N/A');
END IF;

IF (SELECT COUNT(*) FROM custom_sector) = 0 THEN
    INSERT INTO custom_sector (sector_id, sector_name, sector_description)
    VALUES
        (1, 'HEALTHCARE', 'Forms related to patient care and medical services.'),
        (2, 'EDUCATION', 'Forms for student enrollment and academic records.'),
        (3, 'FINANCE', 'Forms for loans, taxes, and financial services.'),
        (4, 'GOVERNMENT', 'Forms for taxes and civic registration.'),
        (5, 'HUMAN_RESOURCES', 'Forms for job applications and employee management.'),
        (6, 'REAL_ESTATE', 'Forms for property transactions and leases.'),
        (7, 'INSURANCE', 'Forms for claims and policy management.'),
        (8, 'RETAIL', 'Forms for customer feedback and warranties.'),
        (9, 'TRANSPORTATION', 'Forms for shipping and travel documentation.'),
        (10, 'LEGAL', 'Forms for legal processes and documentation.');
END IF;
-- Insert into custom_stream table if empty
IF (SELECT COUNT(*) FROM custom_stream) = 0 THEN
    INSERT INTO custom_stream (stream_id, archived, stream_name, stream_description, created_at, updated_at, creator_role)
    VALUES
        (1, 'N', 'SCIENCE', 'Description of Science', NOW(), NULL, NULL),
        (2, 'N', 'ARTS', 'Description of Arts', NOW(), NULL, NULL),
        (3, 'N', 'COMMERCE', 'Description of Commerce', NOW(), NULL, NULL),
        (4, 'N', 'ENGINEERING', 'Description of Engineering', NOW(), NULL, NULL),
        (5, 'N', 'MEDICINE', 'Description of Medicine', NOW(), NULL, NULL),
        (6, 'N', 'HUMANITIES', 'Description of Humanities', NOW(), NULL, NULL),
        (7, 'N', 'SOCIAL SCIENCES', 'Description of Social Sciences', NOW(), NULL, NULL),
        (8, 'N', 'TECHNOLOGY', 'Description of Technology', NOW(), NULL, NULL),
        (9, 'N', 'MATHEMATICS', 'Description of Mathematics', NOW(), NULL, NULL),
        (10, 'N', 'DESIGN', 'Description of Design', NOW(), NULL, NULL);
END IF;

-- Insert into custom_subject table if empty
IF (SELECT COUNT(*) FROM custom_subject) = 0 THEN
    INSERT INTO custom_subject (subject_id, archived, subject_name, subject_description, created_at, updated_at, creator_role)
    VALUES
        (1, 'N', 'Mathematics', 'Description of Mathematics', NOW(), NULL, NULL),
        (2, 'N', 'Physics', 'Description of Physics', NOW(), NULL, NULL),
        (3, 'N', 'Chemistry', 'Description of Chemistry', NOW(), NULL, NULL),
        (4, 'N', 'Biology', 'Description of Biology', NOW(), NULL, NULL),
        (5, 'N', 'English', 'Description of English', NOW(), NULL, NULL),
        (6, 'N', 'History', 'Description of History', NOW(), NULL, NULL),
        (7, 'N', 'Geography', 'Description of Geography', NOW(), NULL, NULL),
        (8, 'N', 'Computer Science', 'Description of Computer Science', NOW(), NULL, NULL),
        (9, 'N', 'Art', 'Description of Art', NOW(), NULL, NULL),
        (10, 'N', 'Physical Education', 'Description of Physical Education', NOW(), NULL, NULL);
END IF;

-- Insert into custom_role_table if empty
IF (SELECT COUNT(*) FROM custom_role_table) = 0 THEN
    INSERT INTO custom_role_table (role_id, role_name, created_at, updated_at, created_by)
    VALUES
        (1, 'SUPER_ADMIN', NOW(), NOW(), 'SUPER_ADMIN'),
        (2, 'ADMIN', NOW(), NOW(), 'SUPER_ADMIN'),
        (3, 'ADMIN_SERVICE_PROVIDER', NOW(), NOW(), 'SUPER_ADMIN'),
        (4, 'SERVICE_PROVIDER', NOW(), NOW(), 'SUPER_ADMIN'),
        (5, 'CUSTOMER', NOW(), NOW(), 'SUPER_ADMIN');
END IF;

-- Insert into custom_districts table if empty
IF (SELECT COUNT(*) FROM custom_districts) = 0 THEN
    INSERT INTO custom_districts (district_id, district_name, state_code)
    VALUES
        (1, 'Bilaspur', 'HP'),
        (2, 'Chamba', 'HP'),
        (3, 'Hamirpur', 'HP'),
        (4, 'Kangra', 'HP'),
        (5, 'Kinnaur', 'HP'),
        (6, 'Kullu', 'HP'),
        (7, 'Lahaul and Spiti', 'HP'),
        (8, 'Mandi', 'HP'),
        (9, 'Shimla', 'HP'),
        (10, 'Sirmaur', 'HP'),
        (11, 'Solan', 'HP'),
        (12, 'Una', 'HP'),
        (13, 'Jammu', 'JK'),
        (14, 'Samba', 'JK'),
        (15, 'Kathua', 'JK'),
        (16, 'Udhampur', 'JK'),
        (17, 'Reasi', 'JK'),
        (18, 'Ramban', 'JK'),
        (19, 'Doda', 'JK'),
        (20, 'Poonch', 'JK'),
        (21, 'Rajouri', 'JK'),
        (22, 'Anantnag', 'JK'),
        (23, 'Kishtwar', 'JK'),
        (24, 'Srinagar', 'JK'),
        (25, 'Baramulla', 'JK'),
        (26, 'Pulwama', 'JK'),
        (27, 'Shopian', 'JK'),
        (28, 'Anantnag', 'JK'),
        (29, 'Bandipora', 'JK'),
        (30, 'Ganderbal', 'JK'),
        (31, 'Kulgam', 'JK'),
        (32, 'Amritsar', 'PB'),
        (33, 'Barnala', 'PB'),
        (34, 'Bathinda', 'PB'),
        (35, 'Faridkot', 'PB'),
        (36, 'Fatehgarh Sahib', 'PB'),
        (37, 'Fazilka', 'PB'),
        (38, 'Ferozepur', 'PB'),
        (39, 'Gurdaspur', 'PB'),
        (40, 'Hoshiarpur', 'PB'),
        (41, 'Jalandhar', 'PB'),
        (42, 'Kapurthala', 'PB'),
        (43, 'Ludhiana', 'PB'),
        (44, 'Mansa', 'PB'),
        (45, 'Moga', 'PB'),
        (46, 'Mohali', 'PB'),
        (47, 'Pathankot', 'PB'),
        (48, 'Patiala', 'PB'),
        (49, 'Rupnagar', 'PB'),
        (50, 'Sangrur', 'PB'),
        (51, 'Tarn Taran', 'PB'),
        (52, 'Ambala', 'HR'),
        (53, 'Bhiwani', 'HR'),
        (54, 'Faridabad', 'HR'),
        (55, 'Fatehabad', 'HR'),
        (56, 'Gurgaon', 'HR'),
        (57, 'Hisar', 'HR'),
        (58, 'Jhajjar', 'HR'),
        (59, 'Jind', 'HR'),
        (60, 'Kaithal', 'HR'),
        (61, 'Karnal', 'HR'),
        (62, 'Mahendragarh', 'HR'),
        (63, 'Mewat', 'HR'),
        (64, 'Palwal', 'HR'),
        (65, 'Panchkula', 'HR'),
        (66, 'Panipat', 'HR'),
        (67, 'Rewari', 'HR'),
        (68, 'Sirsa', 'HR'),
        (69, 'Sonipat', 'HR'),
        (70, 'Yamunanagar', 'HR');
END IF;

-- For custom_service_provider_address_ref table
IF (SELECT COUNT(*) FROM custom_service_provider_address_ref) = 0 THEN
    INSERT INTO custom_service_provider_address_ref (address_type_id, address_name)
    VALUES
        (1, 'OFFICE_ADDRESS'),
        (2, 'CURRENT_ADDRESS'),
        (3, 'BILLING_ADDRESS'),
        (4, 'MAILING_ADDRESS');
END IF;

-- For custom_service_provider_language table
IF (SELECT COUNT(*) FROM custom_service_provider_language) = 0 THEN
    INSERT INTO custom_service_provider_language (language_id, language_name)
    VALUES
        (1, 'Hindi'),
        (2, 'Bengali'),
        (3, 'Telugu'),
        (4, 'Marathi'),
        (5, 'Tamil'),
        (6, 'Gujarati'),
        (7, 'Punjabi');
END IF;

-- For custom_service_provider_infra table
IF (SELECT COUNT(*) FROM custom_service_provider_infra) = 0 THEN
    INSERT INTO custom_service_provider_infra (infra_id, infra_name)
    VALUES
        (1, 'DESKTOP'),
        (2, 'SCANNER'),
        (3, 'LAPTOP'),
        (4, 'PRINTER'),
        (5, 'INTERNET_BROADBAND');
END IF;

-- For custom_skill_set table
IF (SELECT COUNT(*) FROM custom_skill_set) = 0 THEN
    INSERT INTO custom_skill_set (skill_id, skill_name)
    VALUES
        (1, 'Form Filling Knowledge/Expertise'),
        (2, 'Resizing & Uploading Image/Document'),
        (3, 'Executing Online Payment/Transactions'),
        (4, 'Apply To Various Government Schemes');
END IF;
-- Insert into service_provider_status if empty
IF NOT EXISTS (SELECT 1 FROM serviceproviderstatus) THEN
    INSERT INTO serviceproviderstatus (status_id, status_name, description, created_at, updated_at, created_by)
    VALUES
        (1, 'DOCUMENTS_SUBMISSION_PENDING', 'Documents submission is pending', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN'),
        (2, 'APPLIED', 'Application has been submitted', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN'),
        (3, 'APPROVAL_PENDING', 'Application is awaiting approval', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN'),
        (4, 'APPROVED', 'Application has been approved', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN');
END IF;

-- Insert into qualification if empty
IF NOT EXISTS (SELECT 1 FROM qualification) THEN
    INSERT INTO qualification (qualification_id, qualification_name, qualification_description, is_subjects_required,is_stream_required)
    VALUES
        (1, 'MATRICULATION/10th', 'Completed secondary education or equivalent', TRUE, FALSE),
        (2, 'INTERMEDIATE/12th', 'Completed higher secondary education or equivalent', TRUE, TRUE),
        (3, 'BACHELORS', 'Completed undergraduate degree program', FALSE, TRUE),
        (4, 'MASTERS', 'Completed postgraduate degree program', FALSE, TRUE),
        (5, 'DOCTORATE', 'Completed doctoral degree program', FALSE, TRUE),
        (6, 'DIPLOMA', 'Completed a diploma program', FALSE, TRUE),
        (7, 'ITI', 'Completed an ITI (Industrial Training Institute) program', FALSE, TRUE);
END IF;

-- Insert into typing_text if empty
IF NOT EXISTS (SELECT 1 FROM typing_text) THEN
    INSERT INTO typing_text (id, text)
    VALUES
        (1, 'The quick brown fox jumps over the lazy dog near the quiet river, while the bright sun sets in the horizon, casting beautiful hues of orange.'),
        (2, 'A curious cat chased a butterfly through the green meadows, unaware of the gentle breeze swirling around.'),
        (3, 'In the silent night, a lone owl hooted softly as the stars twinkled brightly above the peaceful forest.'),
        (4, 'Beneath the tall mountains, a small village thrived with joy, laughter, and the warmth of togetherness.'),
        (5, 'The adventure begins with a journey through unknown lands, filled with unexpected challenges and thrilling discoveries along the way.');
END IF;

-- Insert into service_provider_test_status if empty
IF NOT EXISTS (SELECT 1 FROM service_provider_test_status) THEN
    INSERT INTO service_provider_test_status (test_status_id, test_status_name, test_status_description, created_at, updated_at, created_by)
    VALUES
        (1, 'New', 'The service provider has registered but has not yet completed the test.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN'),
        (2, 'Completed Test', 'The service provider has completed the required skill tests.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN'),
        (3, 'Approved', 'The service provider submission has been reviewed and approved.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN'),
        (4, 'Rejected', 'The service provider submission was rejected due to not meeting the criteria.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN'),
        (5, 'Suspended', 'The service provider account is currently suspended due to policy violations.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN');
END IF;

-- Insert into service_provider_rank if empty
IF NOT EXISTS (SELECT 1 FROM service_provider_rank) THEN
    INSERT INTO service_provider_rank (rank_id, rank_name, rank_description, created_at, updated_at, created_by, maximum_ticket_size, maximum_binding_size)
    VALUES
        (1, '1a', 'The PROFESSIONAL service provider score is between 75-100 points', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN', 12, 50),
        (2, '1b', 'The PROFESSIONAL service provider score is between 50-75 points', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN', 6, 25),
        (3, '1c', 'The PROFESSIONAL service provider score is between 25-50 points', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN', 4, 17),
        (4, '1d', 'The PROFESSIONAL service provider score is between 0-25 points', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN', 3, 13),
        (5, '2a', 'The INDIVIDUAL service provider score is between 75-100 points', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN', 6, 25),
        (6, '2b', 'The INDIVIDUAL service provider score is between 50-75 points', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN', 3, 13),
        (7, '2c', 'The INDIVIDUAL service provider score is between 25-50 points', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN', 2, 8),
        (8, '2d', 'The INDIVIDUAL service provider score is between 0-25 points', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN', 2, 6);
END IF;

    IF NOT EXISTS (SELECT 1 FROM custom_admin LIMIT 1) THEN
	    CREATE EXTENSION IF NOT EXISTS pgcrypto;
        INSERT INTO custom_admin (admin_id, role, password, user_name, mobilenumber, country_code, signedup, created_at, created_by)
            VALUES
                (1, 2, crypt('Admin#01'::text, gen_salt('bf', 8)::text), 'admin', '7740066387', '+91', 0, CURRENT_DATE, 'SUPER_ADMIN'),
                (2, 1, crypt('SuperAdmin#1357'::text, gen_salt('bf', 8)::text), 'superadmin', '9872548680', '+91', 0, CURRENT_DATE, 'SUPER_ADMIN'),
                (3, 3, crypt('AdminServiceProvider#02'::text, gen_salt('bf', 8)::text), 'adminserviceprovider', '7710393096', '+91', 0, CURRENT_DATE, 'SUPER_ADMIN');
    END IF;

	 IF NOT EXISTS (SELECT 1 FROM scoring_criteria) THEN
        INSERT INTO scoring_criteria (id, attribute_name,condition,score)
        VALUES
            (1, 'Business Unit / Infrastructure', 'If its a Business Unit: 20 points', 20),
            (2, 'Work Experience', '1 year work experience', 5),
            (3, 'Work Experience', '2 years work experience', 10),
            (4, 'Work Experience', '3 years work experience', 15),
            (5, 'Work Experience', '5 or more years work experience', 20),
            (6, 'Qualification', 'Service Provider is graduated or above qualified', 10),
            (7, 'Qualification', 'Service Provider is 12th passed', 5),
            (8, 'Technical Expertise', 'Each skill will score 2 points', 2),
            (9, 'Technical Expertise', 'Service Provider having equal to or more than 5 skills', 10),
            (10, 'Staff', 'More than 4 staff members', 10),
            (11, 'Staff', '2 staff members', 5),
            (12, 'Staff', 'Individual (no staff)', 0),
            (13, 'Infrastructure', 'Service Provider having Equal to 5 or more than 5 infrastructures', 20),
            (14, 'Infrastructure', 'Service Provider having between 2 and 4 infrastructures', 10),
            (15, 'Infrastructure', 'Service Provider having 1 infrastructure', 5),
            (16, 'Infrastructure', 'Service Provider having 0 infrastructure', 0),
            (17, 'PartTimeOrFullTime', 'Service Provider who is Full time', 10),
            (18, 'PartTimeOrFullTime', 'Service Provider who is Part time', 0);
    END IF;

    -- Check and insert into order_state_ref table
    IF NOT EXISTS (SELECT 1 FROM order_state_ref) THEN
        INSERT INTO order_state_ref (order_state_id, order_state_name, order_state_description)
        VALUES
            (1, 'NEW', 'Order is generated'),
            (2, 'AUTO_ASSIGNED', 'Order automatically assigned.'),
            (3, 'UNASSIGNED', 'Order is unassigned.'),
            (4, 'ASSIGNED', 'Order assigned.'),
            (5, 'RETURNED', 'Order returned.'),
            (6, 'IN_PROGRESS', 'Order is in progress.'),
            (7, 'COMPLETED', 'Order completed.'),
            (8, 'IN_REVIEW', 'Order is in review.');
    END IF;

    -- Check and insert into custom_document table
    IF NOT EXISTS (SELECT 1 FROM custom_document) THEN
        INSERT INTO custom_document (document_type_id, document_type_name, description, max_document_size, min_document_size, is_qualification_document, is_issue_date_required, is_expiration_date_required)
        VALUES
            (1, 'Aadhaar_Card_Front', 'Front side of a government-issued ID card in India.', '200KB', '100KB', FALSE, FALSE, FALSE),
            (2, 'Pan_Card', 'A permanent account number card for tax purposes in India.', '200KB', '100KB', FALSE, FALSE, FALSE),
            (3, 'Live_Passport_Size_Photo', 'A live photo typically used for official documents.', '200KB', '100KB', FALSE, FALSE, FALSE),
            (4, 'Signature', 'A handwritten sign used to authenticate documents.', '100KB', '50KB', FALSE, FALSE, FALSE),
            (5, 'Ews_Certificate', 'Certificate for individuals and families below a certain income threshold to access various benefits and concessions.', '300KB', '200KB', TRUE, TRUE, TRUE),
            (6, 'Caste_Certificate', 'Certifies an individuals caste for reservations and benefits in education and employment.', '300KB', '200KB', FALSE, FALSE, TRUE),
            (7, 'Address_Certificate', 'Verifies an individual’s residential address for identity verification and other purposes.', '500KB', '100KB', FALSE, FALSE, TRUE),
            (8, 'Income_Certificate', 'Confirms an individual’s or family’s annual income for applying for government benefits and financial assistance.', '500KB', '100KB', TRUE, TRUE, FALSE),
            (9, 'Driving_License', 'Authorizes an individual to operate motor vehicles, confirming knowledge of traffic laws and vehicle operation skills.', '200KB', '100KB', FALSE, FALSE, FALSE),
            (10, 'Domicile', 'The permanent home or principal residence of a person.', '300KB', '200KB', FALSE, FALSE, TRUE),
            (11, 'Disability_Certificate', 'An outdated term for individuals with physical or mental disabilities; "person with a disability" is preferred today.', '300KB', '200KB', FALSE, TRUE, TRUE),
            (12, 'Mark_Sheet', 'Mark sheet of Qualification', '300KB', '200KB', TRUE, TRUE, FALSE),
            (13, 'Others', 'Includes other document types not listed above, tailored to specific needs or contexts.', '200KB', '100KB', FALSE, FALSE, FALSE),
            (14, 'C-Form_Photo', 'A C Form photo is a standardized ID photo for official documents.', '200KB', '100KB', FALSE, FALSE, FALSE),
            (15, 'Ex-Service_Men', 'Ex-Service Men document is required for individuals who have previously worked in the organization and are now no longer employed.', '300KB', '200KB', FALSE, FALSE, TRUE),
            (16, 'Business_Photo', 'A Standard proof of Running Business.', '200KB', '100KB', FALSE, FALSE, TRUE),
            (17, 'Personal_Photo', 'A Personal Photograph of SP.', '200KB', '100KB', FALSE, FALSE, FALSE);
    END IF;

    -- Check and insert into file_type_name table
    IF NOT EXISTS (SELECT 1 FROM filetype) THEN
        INSERT INTO filetype (file_type_id,file_type_name)
        VALUES
            (1, 'PNG'),
            (2, 'JPG'),
            (3, 'PDF'),
            (4, 'JPEG');
    END IF;

	  IF NOT EXISTS (SELECT 1 FROM document_file_types LIMIT 1) THEN
        -- Insert records into document_file_types
        INSERT INTO document_file_types (document_type_id, file_type_id)
        VALUES
            (1, 2), (1, 4),
            (2, 2), (2, 4),
            (3, 2), (3, 4),
            (4, 2), (4, 4),
            (5, 2), (5, 4),
            (6, 1), (6, 2), (6, 4),
            (7, 2), (7, 4),
            (8, 2), (8, 4),
            (9, 1), (9, 2), (9, 4),
            (10, 2), (10, 4),
            (11, 2), (11, 4),
            (12, 2), (12, 4),
            (13, 1), (13, 2), (13, 4),
            (14, 2), (14, 4),
            (15, 2), (15, 4),
            (16, 2), (16, 4),
            (17, 2), (17, 4),
            (18, 2), (18, 4),
            (19, 2), (19, 4),
            (20, 2), (20, 4),
            (21, 1), (21, 2), (21, 4),
            (22, 2), (22, 4),
            (23, 1), (23, 2), (23, 4),
            (24, 2), (24, 4),
            (25, 2), (25, 4),
            (26, 2), (26, 4),
            (27, 2), (27, 4),
            (28, 1), (28, 2), (28, 4),
            (29, 1), (29, 2), (29, 4);
    END IF;

   IF NOT EXISTS (SELECT 1 FROM board_university WHERE board_university_id = 1) THEN
    INSERT INTO board_university (board_university_id, board_university_name, board_university_location, board_university_code, board_university_type, created_date, modified_date, created_by, modified_by)
    VALUES
        (1, 'Others', 'NA', 'Not Applicable', 'NA', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (2, 'Central Board of Secondary Education', 'Delhi', 'CBSE', 'BOARD', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (3, 'Jawaharlal Nehru University', 'Delhi', 'JNU', 'UNIVERSITY', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (4, 'Uttar Pradesh Board', 'Lucknow', 'UPB', 'BOARD', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (5, 'Punjab University', 'Chandigarh', 'PU', 'UNIVERSITY', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (6, 'Maharashtra State Board', 'Mumbai', 'MSB', 'BOARD', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (7, 'Rajasthan University', 'Jaipur', 'RU', 'UNIVERSITY', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (8, 'Karnataka State Board', 'Bangalore', 'KSB', 'BOARD', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (9, 'Tamil Nadu State Board', 'Chennai', 'TNSB', 'BOARD', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (10, 'University of Mumbai', 'Mumbai', 'UM', 'UNIVERSITY', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (11, 'Osmania University', 'Hyderabad', 'OU', 'UNIVERSITY', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (12, 'West Bengal State University', 'Kolkata', 'WBSU', 'UNIVERSITY', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (13, 'University of Calcutta', 'Kolkata', 'CU', 'UNIVERSITY', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (14, 'Andhra Pradesh Board', 'Vijayawada', 'APB', 'BOARD', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (15, 'University of Madras', 'Chennai', 'UM', 'UNIVERSITY', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (16, 'University of Kerala', 'Thiruvananthapuram', 'UK', 'UNIVERSITY', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (17, 'Gujarat Secondary and Higher Secondary Education Board', 'Gandhinagar', 'GSHSEB', 'BOARD', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (18, 'University of Pune', 'Pune', 'PU', 'UNIVERSITY', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (19, 'University of Rajasthan', 'Jaipur', 'UR', 'UNIVERSITY', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (20, 'University of Allahabad', 'Allahabad', 'UA', 'UNIVERSITY', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN');
END IF;


   IF NOT EXISTS (SELECT 1 FROM institution WHERE institution_id = 1) THEN
    INSERT INTO institution (institution_id, institution_name, institution_location, institution_code, created_date, modified_date, created_by, modified_by)
    VALUES
        (1, 'All India Institute of Medical Sciences', 'New Delhi', 'AIIMS', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (2, 'Indian Institute of Technology Bombay', 'Mumbai', 'IITB', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (3, 'Indian Institute of Science', 'Bangalore', 'IISC', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (4, 'National Institute of Technology Tiruchirappalli', 'Tiruchirappalli', 'NITT', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (5, 'Delhi Technological University', 'New Delhi', 'DTU', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (6, 'Jawaharlal Nehru Technological University', 'Hyderabad', 'JNTUH', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (7, 'Banaras Hindu University', 'Varanasi', 'BHU', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (8, 'University of Hyderabad', 'Hyderabad', 'UOH', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (9, 'Vellore Institute of Technology', 'Vellore', 'VIT', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (10, 'Manipal Academy of Higher Education', 'Manipal', 'MAHE', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (11, 'Amity University', 'Noida', 'AU', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (12, 'Birla Institute of Technology and Science', 'Pilani', 'BITS', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (13, 'SRM Institute of Science and Technology', 'Chennai', 'SRM', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (14, 'Christ University', 'Bangalore', 'CU', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (15, 'Savitribai Phule Pune University', 'Pune', 'SPPU', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (16, 'Indian Statistical Institute', 'Kolkata', 'ISI', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (17, 'Tata Institute of Fundamental Research', 'Mumbai', 'TIFR', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (18, 'National Law School of India University', 'Bangalore', 'NLSIU', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (19, 'Indian Institute of Technology Kanpur', 'Kanpur', 'IITK', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (20, 'Indian Institute of Technology Delhi', 'New Delhi', 'IITD', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (21, 'Jamia Millia Islamia', 'New Delhi', 'JMI', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (22, 'Aligarh Muslim University', 'Aligarh', 'AMU', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (23, 'Visva-Bharati University', 'Santiniketan', 'VBU', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (24, 'Indian Institute of Management Ahmedabad', 'Ahmedabad', 'IIMA', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (25, 'Indian Institute of Management Bangalore', 'Bangalore', 'IIMB', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (26, 'Indian Institute of Technology Kharagpur', 'Kharagpur', 'IITKGP', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (27, 'Indian Institute of Technology Madras', 'Chennai', 'IITM', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (28, 'Indian Institute of Technology Guwahati', 'Guwahati', 'IITG', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (29, 'Indian School of Business', 'Hyderabad', 'ISB', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (30, 'University of Mysore', 'Mysore', 'UOM', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (31, 'Anna University', 'Chennai', 'AU', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (32, 'University of Delhi', 'New Delhi', 'DU', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (33, 'University of Calicut', 'Calicut', 'UOC', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (34, 'Guru Nanak Dev University', 'Amritsar', 'GNDU', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
        (35, 'Punjab University', 'Chandigarh', 'PU', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
		(36, 'Jaypee Institute of Information and Technology', 'Noida', 'JIIT', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN');
	END IF;

    -- Check and insert into vacancy_distribution_type table
    IF NOT EXISTS (SELECT 1 FROM vacancy_distribution_type) THEN
        INSERT INTO vacancy_distribution_type (vacancydistributiontypeid, vacancyDistributionTypeName)
        VALUES
            (1, 'State Wise Distribution'),
            (2, 'Zone Wise Distribution'),
            (3, 'Category Wise Distribution'),
            (4, 'Others');
    END IF;

END $$;

