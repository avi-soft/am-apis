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
            (36, 'Daman and Diu', 'DD'),
            (37,'Ladakh','LA');
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
    INSERT INTO custom_reserve_category (reserve_category_id, reserve_category_name, reserve_category_description, is_default_category,sort_order)
    VALUES
        (1, 'GEN', 'General', true,0),
        (2, 'SC', 'Schedule Caste', false,1),
        (3, 'ST', 'Schedule Tribe', false,2),
        (4, 'OBC', 'Other Backward Caste', false,3),
        (5, 'OTHERS', 'Others', false,1000),
        (6, 'EWS', 'Economically Weaker Section', false,4),
		(7, 'N/A', 'None of the above', false,999);
END IF;

IF (SELECT COUNT(*) FROM custom_product_rejection_status) = 0 THEN
    INSERT INTO custom_product_rejection_status (rejection_status_id, rejection_status, rejection_status_description)
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
    INSERT INTO custom_stream (stream_id, archived, stream_name, stream_description, created_at, created_by, creator_role)
    VALUES
        (0, 'N', '10th Stream', 'Used map 10th subjects', NOW(), NULL, NULL),

        -- 12th Streams
        (1, 'N', 'SCIENCE', 'Science Stream for Higher Secondary Education', NOW(), NULL, NULL),
        (2, 'N', 'ARTS', 'Arts Stream for Higher Secondary Education', NOW(), NULL, NULL),
        (3, 'N', 'COMMERCE', 'Commerce Stream for Higher Secondary Education', NOW(), NULL, NULL),
        (4, 'N', 'ENGINEERING', 'Engineering Stream for Higher Secondary Education', NOW(), NULL, NULL),

        -- Streams for Bachelor's Degrees
        (5, 'N', 'English Literature', 'Bachelor of Arts or Bachelor of Arts(Honours) in English Literature ', NOW(), NULL, NULL),
        (6, 'N', 'History', 'Bachelor of Arts  or Bachelor of Arts(Honours) in History', NOW(), NULL, NULL),
        (7, 'N', 'Political Science', 'Bachelor of Arts or Bachelor of Arts(Honours) in Political Science', NOW(), NULL, NULL),
        (8, 'N', 'Sociology', 'Bachelor of Arts or Bachelor of Arts(Honours) in Sociology', NOW(), NULL, NULL),
        (9, 'N', 'Psychology', 'Bachelor of Arts or Bachelor of Arts(Honours) in Psychology', NOW(), NULL, NULL),

        (10, 'N', 'Physics', 'Bachelor of Science or Bachelor of Science(Honours) in Physics', NOW(), NULL, NULL),
        (11, 'N', 'Chemistry', 'Bachelor of Science or Bachelor of Science(Honours) in Chemistry', NOW(), NULL, NULL),
        (12, 'N', 'Biology', 'Bachelor of Science or Bachelor of Science(Honours) in Biology', NOW(), NULL, NULL),
        (13, 'N', 'Mathematics', 'Bachelor of Science or Bachelor of Science(Honours) in Mathematics', NOW(), NULL, NULL),
        (14, 'N', 'Computer Science', 'Bachelor of Science or Bachelor of Science(Honours) in Computer Science', NOW(), NULL, NULL),
        (15, 'N', 'Biotechnology', 'Bachelor of Science or Bachelor of Science(Honours) in Biotechnology', NOW(), NULL, NULL),

        (16, 'N', 'Accounting', 'Bachelor of Commerce or Bachelor of Commerce(Honours)  in Accounting', NOW(), NULL, NULL),
        (17, 'N', 'Finance', 'Bachelor of Commerce or Bachelor of Commerce(Honours) in Finance', NOW(), NULL, NULL),
        (18, 'N', 'Marketing', 'Bachelor of Commerce or Bachelor of Commerce(Honours) in Marketing', NOW(), NULL, NULL),

        (19, 'N', 'Mechanical Engineering', 'Bachelor of Technology in Mechanical Engineering', NOW(), NULL, NULL),
        (20, 'N', 'Civil Engineering', 'Bachelor of Technology in Civil Engineering', NOW(), NULL, NULL),
        (21, 'N', 'Electronics Engineering', 'Bachelor of Technology in Electronics Engineering', NOW(), NULL, NULL),
        (22, 'N', 'Aerospace Engineering', 'Bachelor of Engineering in Aerospace Engineering', NOW(), NULL, NULL),

        (23, 'N', 'Business Administration', 'Bachelor of Business Administration', NOW(), NULL, NULL),
        (24, 'N', 'Computer Applications', 'Bachelor of Computer Applications', NOW(), NULL, NULL),
        (25, 'N', 'Pharmaceutical Sciences', 'Bachelor of Pharmacy in Pharmaceutical Sciences', NOW(), NULL, NULL),
        (26, 'N', 'Architecture', 'Bachelor of Architecture in Architectural Design', NOW(), NULL, NULL),
        (27, 'N', 'Urban Planning', 'Bachelor of Planning in Urban Planning', NOW(), NULL, NULL),
        (28, 'N', 'Legal Studies', 'Bachelor of Laws in Legal Studies', NOW(), NULL, NULL),
        (29, 'N', 'Nursing', 'Bachelor of Science in Nursing', NOW(), NULL, NULL),
        (30, 'N', 'Hotel Management', 'Bachelor of Hotel Management in Hospitality and Tourism', NOW(), NULL, NULL),
        (31, 'N', 'Medicine and Surgery', 'Bachelor of Medicine, Bachelor of Surgery', NOW(), NULL, NULL),
        (32, 'N', 'Dentistry', 'Bachelor of Dental Surgery', NOW(), NULL, NULL),
        (33, 'N', 'Ayurvedic Medicine and Surgery', 'Bachelor of Ayurvedic Medicine and Surgery', NOW(), NULL, NULL),
        (34, 'N', 'Homeopathic Medicine and Surgery', 'Bachelor of Homeopathic Medicine and Surgery', NOW(), NULL, NULL),
        (35, 'N', 'Physiotherapy', 'Bachelor of Physiotherapy', NOW(), NULL, NULL),
        (36, 'N', 'Medical Laboratory Technology', 'Bachelor of Medical Laboratory Technology', NOW(), NULL, NULL),
        (37, 'N', 'Hospitality and Catering Technology', 'Bachelor of Hotel Management and Catering Technology', NOW(), NULL, NULL),
        (38, 'N', 'Tourism and Travel Management', 'Bachelor of Tourism and Travel Management', NOW(), NULL, NULL),
        (39, 'N', 'Vocational Studies', 'Bachelor of Vocation', NOW(), NULL, NULL),
        (40, 'N', 'Physical Education', 'Bachelor of Physical Education', NOW(), NULL, NULL),
        (41, 'N', 'Education', 'Bachelor of Education', NOW(), NULL, NULL),
        (42, 'N', 'Library Science', 'Bachelor of Library Science', NOW(), NULL, NULL),

        -- Streams for Master's Degrees
        (43, 'N', 'English Literature', 'Master of Arts in English Literature', NOW(), NULL, NULL),
        (44, 'N', 'History', 'Master of Arts in History', NOW(), NULL, NULL),
        (45, 'N', 'Political Science', 'Master of Arts in Political Science', NOW(), NULL, NULL),
        (46, 'N', 'Sociology', 'Master of Arts in Sociology', NOW(), NULL, NULL),
        (47, 'N', 'Psychology', 'Master of Arts in Psychology', NOW(), NULL, NULL),
        (48, 'N', 'Physics', 'Master of Science in Physics', NOW(), NULL, NULL),
        (49, 'N', 'Chemistry', 'Master of Science in Chemistry', NOW(), NULL, NULL),
        (50, 'N', 'Biology', 'Master of Science in Biology', NOW(), NULL, NULL),
        (51, 'N', 'Mathematics', 'Master of Science in Mathematics', NOW(), NULL, NULL),
        (52, 'N', 'Computer Science', 'Master of Science in Computer Science', NOW(), NULL, NULL),
        (53, 'N', 'Advanced Accounting', 'Master of Commerce in Advanced Accounting', NOW(), NULL, NULL),
        (54, 'N', 'Advanced Computer Science', 'Master of Technology in Advanced Computer Science', NOW(), NULL, NULL),
        (55, 'N', 'Business Management', 'Master of Business Administration in Business Management', NOW(), NULL, NULL),
        (56, 'N', 'Advanced Legal Studies', 'Master of Laws in Advanced Legal Studies', NOW(), NULL, NULL),
        (57, 'N', 'Education', 'Master of Education in Educational Research', NOW(), NULL, NULL),
        (58, 'N', 'Library Science', 'Master of Library Science in Information and Library Management', NOW(), NULL, NULL),
        (59, 'N', 'Electronics Engineering', 'Master of Engineering in Advanced Electronics and Communication Systems', NOW(), NULL, NULL),
        (60, 'N', 'Mechanical Engineering', 'Master of Engineering in Mechanical Design and Automation', NOW(), NULL, NULL),
        (61, 'N', 'Civil Engineering', 'Master of Engineering in Structural and Environmental Engineering', NOW(), NULL, NULL),
        (62, 'N', 'General Medicine', 'Doctor of Medicine in General Medicine and Patient Care', NOW(), NULL, NULL),
        (63, 'N', 'Pediatrics', 'Doctor of Medicine in Pediatric and Neonatal Care', NOW(), NULL, NULL),
        (64, 'N', 'Cardiology', 'Doctor of Medicine in Cardiology and Cardiovascular Treatments', NOW(), NULL, NULL),
        (65, 'N', 'Neurology', 'Doctor of Medicine in Neurology and Brain Disorders', NOW(), NULL, NULL),
        (66, 'N', 'General Surgery', 'Master of Surgery in General and Minimally Invasive Surgical Techniques', NOW(), NULL, NULL),
        (67, 'N', 'Orthopedics', 'Master of Surgery in Orthopedic and Musculoskeletal Surgeries', NOW(), NULL, NULL),
        (68, 'N', 'Ophthalmology', 'Master of Surgery in Ophthalmic Surgery and Vision Sciences', NOW(), NULL, NULL),
        (69, 'N', 'Pharmaceutical Chemistry', 'Master of Pharmacy in Pharmaceutical Chemistry and Drug Formulation', NOW(), NULL, NULL),
        (70, 'N', 'Pharmacognosy', 'Master of Pharmacy in Herbal Medicines and Natural Drug Research', NOW(), NULL, NULL),
        (71, 'N', 'Community Health Nursing', 'Master of Science in Nursing specializing in Community Health and Preventive Care', NOW(), NULL, NULL),
        (72, 'N', 'Psychiatric Nursing', 'Master of Science in Nursing specializing in Mental Health and Psychiatric Care', NOW(), NULL, NULL),
        (73, 'N', 'Agronomy', 'Master of Science in Agriculture with a specialization in Agronomy and Crop Production', NOW(), NULL, NULL),
        (74, 'N', 'Horticulture', 'Master of Science in Agriculture focusing on Advanced Horticulture and Floriculture', NOW(), NULL, NULL),
        (75, 'N', 'Soil Science', 'Master of Science in Agriculture specializing in Soil Science and Land Management', NOW(), NULL, NULL),
        (76, 'N', 'Climate Change Studies', 'Master of Science in Environmental Science focusing on Climate Change and Sustainable Development', NOW(), NULL, NULL),
        (77, 'N', 'Pollution Control', 'Master of Science in Environmental Science with specialization in Industrial and Environmental Pollution Control', NOW(), NULL, NULL),
        (78, 'N', 'Hospitality Management', 'Master of Hotel Management specializing in Luxury Hospitality and Tourism Operations', NOW(), NULL, NULL),
        (79, 'N', 'Child Welfare', 'Master of Social Work in Child Welfare and Protection Services', NOW(), NULL, NULL),
        (80, 'N', 'Community Development', 'Master of Social Work focusing on Community Development and Social Reforms', NOW(), NULL, NULL),
        (81, 'N', 'Textile and Apparel Design', 'Master of Design in Textile and Apparel Design with emphasis on Sustainable Fashion', NOW(), NULL, NULL),
        (82, 'N', 'Luxury Fashion Branding', 'Master of Design in Fashion focusing on Luxury Brand Management and Marketing', NOW(), NULL, NULL),
        (83, 'N', 'Broadcast Journalism', 'Master of Journalism and Mass Communication specializing in Digital and Broadcast Journalism', NOW(), NULL, NULL),
        (84, 'N', 'Public Relations & Corporate Communication', 'Master of Journalism and Mass Communication with a focus on Public Relations and Corporate Branding', NOW(), NULL, NULL),

        --Streams for Diplomas
        (85, 'N', 'Science', 'Diploma in Science and Technology', NOW(), NULL, NULL),
        (86, 'N', 'Engineering & Technology', 'Diploma in Engineering and Applied Technology', NOW(), NULL, NULL),
        (87, 'N', 'Medical Science & Healthcare', 'Diploma in Medical Science and Healthcare Management', NOW(), NULL, NULL),
        (88, 'N', 'Business & Management', 'Diploma in Business Administration and Management', NOW(), NULL, NULL),
        (89, 'N', 'Art, Design & Creative Fields', 'Diploma in Fine Arts and Creative Design', NOW(), NULL, NULL),
        (90, 'N', 'IT & Computer Science', 'Diploma in Information Technology and Computer Science', NOW(), NULL, NULL),
        (91, 'N', 'Law & Legal Studies', 'Diploma in Law and Legal Procedures', NOW(), NULL, NULL),
        (92, 'N', 'Hospitality, Tourism & Travel', 'Diploma in Hospitality, Tourism, and Travel Management', NOW(), NULL, NULL),
        (93, 'N', 'Fashion & Textile', 'Diploma in Fashion and Textile Designing', NOW(), NULL, NULL),
        (94, 'N', 'Agriculture & Horticulture', 'Diploma in Agriculture and Horticulture Practices', NOW(), NULL, NULL),
        (95, 'N', 'Education & Training', 'Diploma in Education and Teacher Training', NOW(), NULL, NULL),
        (96, 'N', 'Social Work & Community Development', 'Diploma in Social Work and Community Development', NOW(), NULL, NULL),
        (97, 'N', 'Finance & Accounting', 'Diploma in Finance and Accounting Management', NOW(), NULL, NULL),
        (98, 'N', 'Environmental Science', 'Diploma in Environmental Science and Sustainable Practices', NOW(), NULL, NULL),
        (99, 'N', 'Media, Communication & Journalism', 'Diploma in Media, Communication, and Journalism', NOW(), NULL, NULL),
        (100, 'N', 'Construction & Architecture', 'Diploma in Construction and Architectural Design', NOW(), NULL, NULL),
        (101, 'N', 'Automobile & Mechanical Engineering', 'Diploma in Automobile and Mechanical Engineering', NOW(), NULL, NULL),
        (102, 'N', 'Media & Entertainment', 'Diploma in Media Production and Entertainment Management', NOW(), NULL, NULL),
        (103, 'N', 'Vocational & Specialized Diplomas', 'Diploma in Vocational and Specialized Trades', NOW(), NULL, NULL),

        --Streams for ITI
        (104, 'N', 'Electrician', 'ITI in Electrician Training and Electrical Maintenance', NOW(), NULL, NULL),
        (105, 'N', 'Fitter', 'ITI in Fitting and Mechanical Assembly', NOW(), NULL, NULL),
        (106, 'N', 'Welder', 'ITI in Welding and Fabrication Techniques', NOW(), NULL, NULL),
        (107, 'N', 'Plumber', 'ITI in Plumbing and Sanitation Maintenance', NOW(), NULL, NULL),
        (108, 'N', 'Carpenter', 'ITI in Carpentry and Woodworking', NOW(), NULL, NULL),
        (109, 'N', 'Mechanic Diesel', 'ITI in Diesel Mechanic and Engine Repair', NOW(), NULL, NULL),
        (110, 'N', 'Mechanic Motor Vehicle', 'ITI in Motor Vehicle Mechanics and Automobile Engineering', NOW(), NULL, NULL),
        (111, 'N', 'Turner', 'ITI in Turner and Machining Operations', NOW(), NULL, NULL),
        (112, 'N', 'Machinist', 'ITI in Machinist and CNC Operations', NOW(), NULL, NULL),
        (113, 'N', 'Electronics Mechanic', 'ITI in Electronics Mechanic and Circuit Design', NOW(), NULL, NULL),
        (114, 'N', 'Information Technology', 'ITI in IT and Software Development', NOW(), NULL, NULL),
        (115, 'N', 'Draughtsman Civil', 'ITI in Civil Draughtsman and Architectural Drawing', NOW(), NULL, NULL),
        (116, 'N', 'Draughtsman Mechanical', 'ITI in Mechanical Draughtsman and CAD Design', NOW(), NULL, NULL),
        (117, 'N', 'Refrigeration and AC Mechanic', 'ITI in Refrigeration and Air Conditioning Technology', NOW(), NULL, NULL),
        (118, 'N', 'Surveyor', 'ITI in Land Surveying and Mapping', NOW(), NULL, NULL),
        (119, 'N', 'Painter', 'ITI in Painting and Surface Coating', NOW(), NULL, NULL),
        (120, 'N', 'Tool and Die Maker', 'ITI in Tool and Die Making Techniques', NOW(), NULL, NULL),
        (121, 'N', 'Stenography & Secretarial Practice', 'ITI in Stenography and Office Administration', NOW(), NULL, NULL),

        --Streams for Doctorate
        (122, 'N', 'PhD in English Literature', 'Doctorate in English Literature and Linguistics', NOW(), NULL, NULL),
        (123, 'N', 'PhD in History', 'Doctorate in Historical Studies and Research', NOW(), NULL, NULL),
        (124, 'N', 'PhD in Political Science', 'Doctorate in Political Science and Public Administration', NOW(), NULL, NULL),
        (125, 'N', 'PhD in Sociology', 'Doctorate in Sociology and Social Research', NOW(), NULL, NULL),
        (126, 'N', 'PhD in Psychology', 'Doctorate in Psychology and Behavioral Sciences', NOW(), NULL, NULL),
        (127, 'N', 'PhD in Physics', 'Doctorate in Theoretical and Applied Physics', NOW(), NULL, NULL),
        (128, 'N', 'PhD in Chemistry', 'Doctorate in Chemistry and Molecular Science', NOW(), NULL, NULL),
        (129, 'N', 'PhD in Biology', 'Doctorate in Biological and Life Sciences', NOW(), NULL, NULL),
        (130, 'N', 'PhD in Mathematics', 'Doctorate in Mathematics and Computational Research', NOW(), NULL, NULL),
        (131, 'N', 'PhD in Computer Science', 'Doctorate in Computer Science and Artificial Intelligence', NOW(), NULL, NULL),
        (132, 'N', 'PhD in Business Management', 'Doctorate in Business Administration and Strategic Management', NOW(), NULL, NULL),
        (133, 'N', 'PhD in Law', 'Doctorate in Law and Legal Studies', NOW(), NULL, NULL),
        (134, 'N', 'PhD in Education', 'Doctorate in Education and Pedagogical Studies', NOW(), NULL, NULL),
        (135, 'N', 'PhD in Environmental Science', 'Doctorate in Environmental Science and Sustainability', NOW(), NULL, NULL),
        (136, 'N', 'PhD in Media & Journalism', 'Doctorate in Media, Communication, and Journalism', NOW(), NULL, NULL),
        (137, 'N', 'PhD in Civil Engineering', 'Doctorate in Civil Engineering and Infrastructure Development', NOW(), NULL, NULL),
        (138, 'N', 'PhD in Mechanical Engineering', 'Doctorate in Mechanical Engineering and Robotics', NOW(), NULL, NULL),
        (139, 'N', 'PhD in Electrical Engineering', 'Doctorate in Electrical and Electronics Engineering', NOW(), NULL, NULL),
        (140, 'N', 'PhD in Medical Science', 'Doctorate in Medical Science and Clinical Research', NOW(), NULL, NULL),
        (141, 'N', 'PhD in Agriculture', 'Doctorate in Agricultural Science and Rural Development', NOW(), NULL, NULL),

        --More streams related to Bachelors
        --BA and BA(HONours)

        (142, 'N', 'Economics', 'Bachelor of Arts or Bachelor of Arts(Honours) in Economics', NOW(), NULL, NULL),
        (143, 'N', 'Philosophy', 'Bachelor of Arts or Bachelor of Arts(Honours) in Philosophy', NOW(), NULL, NULL),
        (144, 'N', 'Geography', 'Bachelor of Arts or Bachelor of Arts(Honours) in Geography', NOW(), NULL, NULL),
        (145, 'N', 'Journalism & Mass Communication', 'Bachelor of Arts or Bachelor of Arts(Honours) in Journalism & Mass Communication', NOW(), NULL, NULL),
        (146, 'N', 'Public Administration', 'Bachelor of Arts or Bachelor of Arts(Honours) in Public Administration', NOW(), NULL, NULL),
        (147, 'N', 'Fine Arts', 'Bachelor of Arts or Bachelor of Arts(Honours) in Fine Arts', NOW(), NULL, NULL),

--BSC and BSC(Honours)
        (148, 'N', 'Microbiology', 'Bachelor of Science or Bachelor of Science(Honours) in Microbiology ', NOW(), NULL, NULL),
        (149, 'N', 'Environmental Science', 'Bachelor of Science or Bachelor of Science(Honours) in Environmental Science ', NOW(), NULL, NULL),
        (150, 'N', 'Computer Science', 'Bachelor of Science or Bachelor of Science(Honours) in Computer Science ', NOW(), NULL, NULL),
        (151, 'N', 'Agriculture', 'Bachelor of Science or Bachelor of Science(Honours) in Agriculture ', NOW(), NULL, NULL),
        (152, 'N', 'Statistics', 'Bachelor of Science or Bachelor of Science(Honours) in Statistics ', NOW(), NULL, NULL),
--BCOM and BCOM(HONS)
        (153, 'N', 'Banking and Insurance', 'Bachelor of Commerce or Bachelor of Commerce(Honours)  in Banking and Insurance', NOW(), NULL, NULL),
        (154, 'N', 'Taxation', 'Bachelor of Commerce or Bachelor of Commerce(Honours)  in Taxation', NOW(), NULL, NULL),
        (155, 'N', 'International Business', 'Bachelor of Commerce or Bachelor of Commerce(Honours)  in International Business', NOW(), NULL, NULL),
        (156, 'N', 'Business Analytics', 'Bachelor of Commerce or Bachelor of Commerce(Honours)  in Business Analytics', NOW(), NULL, NULL),
        (157, 'N', 'Corporate Law', 'Bachelor of Commerce or Bachelor of Commerce(Honours)  in Corporate Law', NOW(), NULL, NULL),
        --BTECh
        (158, 'N', 'Computer Science Engineering', 'Bachelor of Technology in Computer Science', NOW(), NULL, NULL),
        (159, 'N', 'Information Technology', 'Bachelor of Technology in Information Technology', NOW(), NULL, NULL),

        -- Bachelor of Ayurvedic Medicine and Surgery (BAMS)
        (160, 'N', 'Kayachikitsa', 'Bachelor of Ayurvedic Medicine and Surgery in General Medicine', NOW(), NULL, NULL),
        (161, 'N', 'Shalya Tantra', 'Bachelor of Ayurvedic Medicine and Surgery in Surgery', NOW(), NULL, NULL),
        (162, 'N', 'Shalakya Tantra', 'Bachelor of Ayurvedic Medicine and Surgery in ENT & Ophthalmology', NOW(), NULL, NULL),
        (163, 'N', 'Prasuti Tantra & Stri Roga', 'Bachelor of Ayurvedic Medicine and Surgery in Gynecology & Obstetrics', NOW(), NULL, NULL),
        (164, 'N', 'Kaumarbhritya', 'Bachelor of Ayurvedic Medicine and Surgery in Pediatrics', NOW(), NULL, NULL),
        (165, 'N', 'Panchakarma', 'Bachelor of Ayurvedic Medicine and Surgery in Detoxification Therapies', NOW(), NULL, NULL),

        -- Bachelor of Science in Nursing (B.Sc. Nursing)
        (166, 'N', 'Medical-Surgical Nursing', 'Bachelor of Science in Nursing in Medical-Surgical Nursing', NOW(), NULL, NULL),
        (167, 'N', 'Obstetrics & Gynecological Nursing', 'Bachelor of Science in Nursing in Obstetrics & Gynecological Nursing', NOW(), NULL, NULL),
        (168, 'N', 'Pediatric Nursing', 'Bachelor of Science in Nursing in Pediatric Nursing', NOW(), NULL, NULL),
        (169, 'N', 'Psychiatric Nursing', 'Bachelor of Science in Nursing in Psychiatric Nursing', NOW(), NULL, NULL),
        (170, 'N', 'Community Health Nursing', 'Bachelor of Science in Nursing in Community Health Nursing', NOW(), NULL, NULL),
        (171, 'N', 'Critical Care Nursing', 'Bachelor of Science in Nursing in Critical Care Nursing', NOW(), NULL, NULL),

        -- Bachelor of Physiotherapy (BPT)
        (172, 'N', 'Orthopedic Physiotherapy', 'Bachelor of Physiotherapy in Orthopedic Physiotherapy', NOW(), NULL, NULL),
        (173, 'N', 'Neurological Physiotherapy', 'Bachelor of Physiotherapy in Neurological Physiotherapy', NOW(), NULL, NULL),
        (174, 'N', 'Cardiopulmonary Physiotherapy', 'Bachelor of Physiotherapy in Cardiopulmonary Physiotherapy', NOW(), NULL, NULL),
        (175, 'N', 'Sports Physiotherapy', 'Bachelor of Physiotherapy in Sports Physiotherapy', NOW(), NULL, NULL),
        (176, 'N', 'Pediatric Physiotherapy', 'Bachelor of Physiotherapy in Pediatric Physiotherapy', NOW(), NULL, NULL),

        -- Bachelor of Homeopathic Medicine and Surgery (BHMS)
        (177, 'N', 'Homeopathic Pharmacy', 'Bachelor of Homeopathic Medicine and Surgery in Homeopathic Pharmacy', NOW(), NULL, NULL),
        (178, 'N', 'Homeopathic Materia Medica', 'Bachelor of Homeopathic Medicine and Surgery in Homeopathic Materia Medica', NOW(), NULL, NULL),
        (179, 'N', 'Pediatrics', 'Bachelor of Homeopathic Medicine and Surgery in Pediatrics', NOW(), NULL, NULL),
        (180, 'N', 'Psychiatry', 'Bachelor of Homeopathic Medicine and Surgery in Psychiatry', NOW(), NULL, NULL),
        (181, 'N', 'Dermatology', 'Bachelor of Homeopathic Medicine and Surgery in Dermatology', NOW(), NULL, NULL),
        (182, 'N', 'Homeopathic Repertory', 'Bachelor of Homeopathic Medicine and Surgery in Homeopathic Repertory', NOW(), NULL, NULL),

        -- Bachelor of Medical Laboratory Technology (BMLT)
        (183, 'N', 'Clinical Chemistry', 'Bachelor of Medical Laboratory Technology in Clinical Chemistry', NOW(), NULL, NULL),
        (184, 'N', 'Hematology', 'Bachelor of Medical Laboratory Technology in Hematology', NOW(), NULL, NULL),
        (185, 'N', 'Microbiology', 'Bachelor of Medical Laboratory Technology in Microbiology', NOW(), NULL, NULL),
        (186, 'N', 'Immunology', 'Bachelor of Medical Laboratory Technology in Immunology', NOW(), NULL, NULL),
        (187, 'N', 'Blood Banking & Transfusion Technology', 'Bachelor of Medical Laboratory Technology in Blood Banking & Transfusion Technology', NOW(), NULL, NULL),

        -- Bachelor of Hotel Management and Catering Technology (BHMCT)
        (188, 'N', 'Hotel Operations', 'Bachelor of Hotel Management and Catering Technology in Hotel Operations', NOW(), NULL, NULL),
        (189, 'N', 'Food & Beverage Management', 'Bachelor of Hotel Management and Catering Technology in Food & Beverage Management', NOW(), NULL, NULL),
        (190, 'N', 'Hospitality & Tourism Management', 'Bachelor of Hotel Management and Catering Technology in Hospitality & Tourism Management', NOW(), NULL, NULL),
        (191, 'N', 'Event Management', 'Bachelor of Hotel Management and Catering Technology in Event Management', NOW(), NULL, NULL),

        -- Bachelor of Tourism and Travel Management (BTTM)
        (192, 'N', 'Travel Agency Management', 'Bachelor of Tourism and Travel Management in Travel Agency Management', NOW(), NULL, NULL),
        (193, 'N', 'Hospitality Management', 'Bachelor of Tourism and Travel Management in Hospitality Management', NOW(), NULL, NULL),
        (194, 'N', 'Eco-Tourism', 'Bachelor of Tourism and Travel Management in Eco-Tourism', NOW(), NULL, NULL),
        (195, 'N', 'Aviation & Airline Management', 'Bachelor of Tourism and Travel Management in Aviation & Airline Management', NOW(), NULL, NULL),

        -- Bachelor of Vocation (B.Voc)
        (196, 'N', 'Software Development', 'Bachelor of Vocation in Software Development', NOW(), NULL, NULL),
        (197, 'N', 'Banking & Finance', 'Bachelor of Vocation in Banking & Finance', NOW(), NULL, NULL),
        (198, 'N', 'Retail Management', 'Bachelor of Vocation in Retail Management', NOW(), NULL, NULL),
        (199, 'N', 'Healthcare Technology', 'Bachelor of Vocation in Healthcare Technology', NOW(), NULL, NULL),
        (200, 'N', 'Hospitality & Tourism', 'Bachelor of Vocation in Hospitality & Tourism', NOW(), NULL, NULL),
        (201, 'N', 'Food Processing', 'Bachelor of Vocation in Food Processing', NOW(), NULL, NULL),

        -- Bachelor of Physical Education (B.P.Ed)
        (202, 'N', 'Sports Coaching', 'Bachelor of Physical Education in Sports Coaching', NOW(), NULL, NULL),
        (203, 'N', 'Kinesiology', 'Bachelor of Physical Education in Kinesiology', NOW(), NULL, NULL),
        (204, 'N', 'Exercise Physiology', 'Bachelor of Physical Education in Exercise Physiology', NOW(), NULL, NULL),
        (205, 'N', 'Sports Psychology', 'Bachelor of Physical Education in Sports Psychology', NOW(), NULL, NULL),

        -- Bachelor of Education (B.Ed)
        (206, 'N', 'Special Education', 'Bachelor of Education in Special Education', NOW(), NULL, NULL),
        (207, 'N', 'Early Childhood Education', 'Bachelor of Education in Early Childhood Education', NOW(), NULL, NULL),
        (208, 'N', 'Science & Mathematics Education', 'Bachelor of Education in Science & Mathematics Education', NOW(), NULL, NULL),
        (209, 'N', 'Social Science Education', 'Bachelor of Education in Social Science Education', NOW(), NULL, NULL),
        (210, 'N', 'Physical Education', 'Bachelor of Education in Physical Education', NOW(), NULL, NULL),

        -- Bachelor of Library Science (B.Li.Sc)
        (211, 'N', 'Digital Libraries', 'Bachelor of Library Science in Digital Libraries', NOW(), NULL, NULL),
        (212, 'N', 'Information Management', 'Bachelor of Library Science in Information Management', NOW(), NULL, NULL),
        (213, 'N', 'Archival Studies', 'Bachelor of Library Science in Archival Studies', NOW(), NULL, NULL),
        (214, 'N', 'Library Automation', 'Bachelor of Library Science in Library Automation', NOW(), NULL, NULL),
        (215, 'N', 'Others', 'Others', NOW(), NULL, NULL),
        (216, 'N', 'NA', 'NA', NOW(), NULL, NULL);

END IF;


-- Insert into custom_subject table if empty
IF (SELECT COUNT(*) FROM custom_subject) = 0 THEN
    INSERT INTO custom_subject (subject_id, archived, subject_name, subject_description, created_at, created_by, creator_role)
    VALUES
        (1, 'N', 'Mathematics', 'Description of Mathematics', NOW(), NULL, NULL),
          (2, 'N', 'Science', 'Description of Science', NOW(), NULL, NULL),
          (3, 'N', 'Social Science', 'Description of Social Science', NOW(), NULL, NULL),
          (4, 'N', 'English', 'Description of English', NOW(), NULL, NULL),
          (5, 'N', 'Hindi', 'Description of Hindi', NOW(), NULL, NULL),
          (6, 'N', 'Physics', 'Description of Physics', NOW(), NULL, NULL),
          (7, 'N', 'Chemistry', 'Description of Chemistry', NOW(), NULL, NULL),
          (8, 'N', 'Biology', 'Description of Biology', NOW(), NULL, NULL),
          (9, 'N', 'History', 'Description of History', NOW(), NULL, NULL),
          (10, 'N', 'Geography', 'Description of Geography', NOW(), NULL, NULL),
          (11, 'N', 'Civics', 'Description of Civics', NOW(), NULL, NULL),
          (12, 'N', 'Economics', 'Description of Economics', NOW(), NULL, NULL),
          (13, 'N', 'Sanskrit', 'Description of Sanskrit', NOW(), NULL, NULL),
          (14, 'N', 'French', 'Description of French', NOW(), NULL, NULL),
          (15, 'N', 'German', 'Description of German', NOW(), NULL, NULL),
          (16, 'N', 'Punjabi', 'Description of Punjabi', NOW(), NULL, NULL),
          (17, 'N', 'Urdu', 'Description of Urdu', NOW(), NULL, NULL),
          (18, 'N', 'Environmental Science', 'Description of Environmental Science', NOW(), NULL, NULL),
          (19, 'N', 'Home Science', 'Description of Home Science', NOW(), NULL, NULL),
          (20, 'N', 'Computer Science', 'Description of Computer Science', NOW(), NULL, NULL),
          (21, 'N', 'Information Technology', 'Description of Information Technology', NOW(), NULL, NULL),
          (22, 'N', 'Artificial Intelligence', 'Description of Artificial Intelligence', NOW(), NULL, NULL),
          (23, 'N', 'Business Studies', 'Description of Business Studies', NOW(), NULL, NULL),
          (24, 'N', 'Financial Literacy', 'Description of Financial Literacy', NOW(), NULL, NULL),
          (25, 'N', 'Agriculture', 'Description of Agriculture', NOW(), NULL, NULL),
          (26, 'N', 'Physical Education', 'Description of Physical Education', NOW(), NULL, NULL),
          (27, 'N', 'Yoga', 'Description of Yoga', NOW(), NULL, NULL),
          (28, 'N', 'Music', 'Description of Music', NOW(), NULL, NULL),
          (29, 'N', 'Dance', 'Description of Dance', NOW(), NULL, NULL),
          (30, 'N', 'Art & Design', 'Description of Art & Design', NOW(), NULL, NULL),
          (31, 'N', 'Work Education', 'Description of Work Education', NOW(), NULL, NULL),
          (32, 'N', 'Moral Science', 'Description of Moral Science', NOW(), NULL, NULL),
          (33, 'N', 'EVS', 'Description of Environmental Science', NOW(), NULL, NULL),
          (34, 'N', 'Painting', 'Description of Painting', NOW(), NULL, NULL),
          (35, 'N', 'IP', 'Description of Informatics Practices', NOW(), NULL, NULL),
          (36, 'N', 'Fine Arts', 'Description of Fine Arts', NOW(), NULL, NULL),
          (37, 'N', 'Health Care', 'Description of Health Care', NOW(), NULL, NULL),
          (38, 'N', 'Psychology', 'Description of Psychology', NOW(), NULL, NULL),
          (39, 'N', 'Media Studies', 'Description of Media Studies', NOW(), NULL, NULL),
          (40, 'N', 'Biotechnology', 'Description of Biotechnology', NOW(), NULL, NULL),
          (41, 'N', 'Statistics', 'Description of Statistics', NOW(), NULL, NULL),
          (42, 'N', 'Sociology', 'Description of Sociology', NOW(), NULL, NULL),
          (43, 'N', 'Political Science', 'Description of Political Science', NOW(), NULL, NULL),
          (44, 'N', 'Geology', 'Description of Geology', NOW(), NULL, NULL),
          (45, 'N', 'Anthropology', 'Description of Anthropology', NOW(), NULL, NULL),
          (46, 'N', 'Law', 'Description of Law', NOW(), NULL, NULL),
          (47, 'N', 'Philosophy', 'Description of Philosophy', NOW(), NULL, NULL),
          (48, 'N', 'Fashion Design', 'Description of Fashion Design', NOW(), NULL, NULL),
          (49, 'N', 'Informatics', 'Description of Informatics', NOW(), NULL, NULL),
          (50, 'N', 'Literature', 'Description of Literature', NOW(), NULL, NULL),
          (51, 'N', 'Entrepreneurship', 'Description of Entrepreneurship', NOW(), NULL, NULL),
          (52, 'N', 'Arabic', 'Description of Arabic', NOW(), NULL, NULL),
          (53, 'N', 'Accounts', 'Description of Accounts', NOW(), NULL, NULL),
          (54, 'N', 'Others', 'Others', NOW(), NULL, NULL);


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
        -- Andhra Pradesh (AP) districts
        (1, 'Alluri Sitharama Raju', 'AP'),
        (2, 'Anakapalli', 'AP'),
        (3, 'Ananthapuramu', 'AP'),
        (4, 'Annamayya', 'AP'),
        (5, 'Bapatla', 'AP'),
        (6, 'Chittoor', 'AP'),
        (7, 'Dr. B.R. Ambedkar Konaseema', 'AP'),
        (8, 'East Godavari', 'AP'),
        (9, 'Eluru', 'AP'),
        (10, 'Guntur', 'AP'),
        (11, 'Kakinada', 'AP'),
        (12, 'Krishna', 'AP'),
        (13, 'Kurnool', 'AP'),
        (14, 'Nandyal', 'AP'),
        (15, 'NTR', 'AP'),
        (16, 'Palnadu', 'AP'),
        (17, 'Parvathipuram Manyam', 'AP'),
        (18, 'Prakasam', 'AP'),
        (19, 'Sri Potti Sriramulu Nellore', 'AP'),
        (20, 'Sri Sathya Sai', 'AP'),
        (21, 'Srikakulam', 'AP'),
        (22, 'Tirupati', 'AP'),
        (23, 'Visakhapatnam', 'AP'),
        (24, 'Vizianagaram', 'AP'),
        (25, 'West Godavari', 'AP'),
        (26, 'Y.S.R.', 'AP'),

        -- Arunachal Pradesh (AR) districts
        (27, 'Anjaw', 'AR'),
        (28, 'Bichom', 'AR'),
        (29, 'Changlang', 'AR'),
        (30, 'Dibang Valley', 'AR'),
        (31, 'East Kameng', 'AR'),
        (32, 'East Siang', 'AR'),
        (33, 'Kamle', 'AR'),
        (34, 'Keyi Panyor', 'AR'),
        (35, 'Kra Daadi', 'AR'),
        (36, 'Kurung Kumey', 'AR'),
        (37, 'Leparada', 'AR'),
        (38, 'Lohit', 'AR'),
        (39, 'Longding', 'AR'),
        (40, 'Lower Dibang Valley', 'AR'),
        (41, 'Lower Siang', 'AR'),
        (42, 'Lower Subansiri', 'AR'),
        (43, 'Namsai', 'AR'),
        (44, 'Pakke Kessang', 'AR'),
        (45, 'Papum Pare', 'AR'),
        (46, 'Shi Yomi', 'AR'),
        (47, 'Siang', 'AR'),
        (48, 'Tawang', 'AR'),
        (49, 'Tirap', 'AR'),
        (50, 'Upper Siang', 'AR'),
        (51, 'Upper Subansiri', 'AR'),
        (52, 'West Kameng', 'AR'),
        (53, 'West Siang', 'AR'),

        -- Assam (AS) districts
        (54, 'Bajali', 'AS'),
        (55, 'Baksa', 'AS'),
        (56, 'Barpeta', 'AS'),
        (57, 'Biswanath', 'AS'),
        (58, 'Bongaigaon', 'AS'),
        (59, 'Cachar', 'AS'),
        (60, 'Charaideo', 'AS'),
        (61, 'Chirang', 'AS'),
        (62, 'Darrang', 'AS'),
        (63, 'Dhemaji', 'AS'),
        (64, 'Dhubri', 'AS'),
        (65, 'Dibrugarh', 'AS'),
        (66, 'Dima Hasao', 'AS'),
        (67, 'Goalpara', 'AS'),
        (68, 'Golaghat', 'AS'),
        (69, 'Hailakandi', 'AS'),
        (70, 'Hojai', 'AS'),
        (71, 'Jorhat', 'AS'),
        (72, 'Kamrup', 'AS'),
        (73, 'Kamrup Metro', 'AS'),
        (74, 'Karbi Anglong', 'AS'),
        (75, 'Karimganj', 'AS'),
        (76, 'Kokrajhar', 'AS'),
        (77, 'Lakhimpur', 'AS'),
        (78, 'Majuli', 'AS'),
        (79, 'Marigaon', 'AS'),
        (80, 'Nagaon', 'AS'),
        (81, 'Nalbari', 'AS'),
        (82, 'Sivasagar', 'AS'),
        (83, 'Sonitpur', 'AS'),
        (84, 'South Salmara Mancachar', 'AS'),
        (85, 'Tamulpur', 'AS'),
        (86, 'Tinsukia', 'AS'),
        (87, 'Udalguri', 'AS'),
        (88, 'West Karbi Anglong', 'AS'),

        --Bihar

        (89, 'Araria', 'BR'),
        (90, 'Arwal', 'BR'),
        (91, 'Aurangabad', 'BR'),
        (92, 'Banka', 'BR'),
        (93, 'Begusarai', 'BR'),
        (94, 'Bhagalpur', 'BR'),
        (95, 'Bhojpur', 'BR'),
        (96, 'Buxar', 'BR'),
        (97, 'Darbhanga', 'BR'),
        (98, 'Gaya', 'BR'),
        (99, 'Gopalganj', 'BR'),
        (100, 'Jamui', 'BR'),
        (101, 'Jehanabad', 'BR'),
        (102, 'Kaimur (Bhabua)', 'BR'),
        (103, 'Katihar', 'BR'),
        (104, 'Khagaria', 'BR'),
        (105, 'Kishanganj', 'BR'),
        (106, 'Lakhisarai', 'BR'),
        (107, 'Madhepura', 'BR'),
        (108, 'Madhubani', 'BR'),
        (109, 'Munger', 'BR'),
        (110, 'Muzaffarpur', 'BR'),
        (111, 'Nalanda', 'BR'),
        (112, 'Nawada', 'BR'),
        (113, 'Pashchim Champaran', 'BR'),
        (114, 'Patna', 'BR'),
        (115, 'Purbi Champaran', 'BR'),
        (116, 'Purnia', 'BR'),
        (117, 'Rohtas', 'BR'),
        (118, 'Saharsa', 'BR'),
        (119, 'Samastipur', 'BR'),
        (120, 'Saran', 'BR'),
        (121, 'Sheikhpura', 'BR'),
        (122, 'Sheohar', 'BR'),
        (123, 'Sitamarhi', 'BR'),
        (124, 'Siwan', 'BR'),
        (125, 'Supaul', 'BR'),
        (126, 'Vaishali', 'BR'),

        -- Chandigarh (UT) districts
        (127, 'Chandigarh', 'CH'),

        -- Chhattisgarh (CG) districts
        (128, 'Balod', 'CG'),
        (129, 'Balodabazar-Bhatapara', 'CG'),
        (130, 'Balrampur-Ramanujganj', 'CG'),
        (131, 'Bastar', 'CG'),
        (132, 'Bemetara', 'CG'),
        (133, 'Bijapur', 'CG'),
        (134, 'Bilaspur', 'CG'),
        (135, 'Dakshin Bastar Dantewada', 'CG'),
        (136, 'Dhamtari', 'CG'),
        (137, 'Durg', 'CG'),
        (138, 'Gariyaband', 'CG'),
        (139, 'Gaurela-Pendra-Marwahi', 'CG'),
        (140, 'Janjgir-Champa', 'CG'),
        (141, 'Jashpur', 'CG'),
        (142, 'Kabeerdham', 'CG'),
        (143, 'Khairagarh-Chhuikhadan-Gandai', 'CG'),
        (144, 'Kondagaon', 'CG'),
        (145, 'Korba', 'CG'),
        (146, 'Korea', 'CG'),
        (147, 'Mahasamund', 'CG'),
        (148, 'Manendragarh-Chirmiri-Bharatpur', 'CG'),
        (149, 'Mohla-Manpur-Ambagarh Chouki', 'CG'),
        (150, 'Mungeli', 'CG'),
        (151, 'Narayanpur', 'CG'),
        (152, 'Raigarh', 'CG'),
        (153, 'Raipur', 'CG'),
        (154, 'Rajnandgaon', 'CG'),
        (155, 'Sakti', 'CG'),
        (156, 'Sarangarh-Bilaigarh', 'CG'),
        (157, 'Sukma', 'CG'),
        (158, 'Surajpur', 'CG'),
        (159, 'Surguja', 'CG'),
        (160, 'Uttar Bastar Kanker', 'CG'),

-- Dadra and Nagar Haveli (UT) districts and Daman and Diu (UT) districts
        (161, 'Dadra And Nagar Haveli', 'DNHDD'),
        (162, 'Daman', 'DNHDD'),
        (163, 'Diu', 'DNHDD'),

-- Delhi (NCT) districts
        (164, 'Central', 'DL'),
        (165, 'East', 'DL'),
        (166, 'New Delhi', 'DL'),
        (167, 'North', 'DL'),
        (168, 'North East', 'DL'),
        (169, 'North West', 'DL'),
        (170, 'Shahdara', 'DL'),
        (171, 'South', 'DL'),
        (172, 'South East', 'DL'),
        (173, 'South West', 'DL'),
        (174, 'West', 'DL'),

-- Goa districts
        (175, 'North Goa', 'GA'),
        (176, 'South Goa', 'GA'),

-- Gujarat districts
        (177, 'Ahmedabad', 'GJ'),
        (178, 'Amreli', 'GJ'),
        (179, 'Anand', 'GJ'),
        (180, 'Arvalli', 'GJ'),
        (181, 'Banas Kantha', 'GJ'),
        (182, 'Bharuch', 'GJ'),
        (183, 'Bhavnagar', 'GJ'),
        (184, 'Botad', 'GJ'),
        (185, 'Chhotaudepur', 'GJ'),
        (186, 'Dahod', 'GJ'),
        (187, 'Dangs', 'GJ'),
        (188, 'Devbhumi Dwarka', 'GJ'),
        (189, 'Gandhinagar', 'GJ'),
        (190, 'Gir Somnath', 'GJ'),
        (191, 'Jamnagar', 'GJ'),
        (192, 'Junagadh', 'GJ'),
        (193, 'Kachchh', 'GJ'),
        (194, 'Kheda', 'GJ'),
        (195, 'Mahesana', 'GJ'),
        (196, 'Mahisagar', 'GJ'),
        (197, 'Morbi', 'GJ'),
        (198, 'Narmada', 'GJ'),
        (199, 'Navsari', 'GJ'),
        (200, 'Panch Mahals', 'GJ'),
        (201, 'Patan', 'GJ'),
        (202, 'Porbandar', 'GJ'),
        (203, 'Rajkot', 'GJ'),
        (204, 'Sabar Kantha', 'GJ'),
        (205, 'Surat', 'GJ'),
        (206, 'Surendranagar', 'GJ'),
        (207, 'Tapi', 'GJ'),
        (208, 'Vadodara', 'GJ'),
        (209, 'Valsad', 'GJ'),

-- Haryana districts
        (210, 'Ambala', 'HR'),
        (211, 'Bhiwani', 'HR'),
        (212, 'Charkhi Dadri', 'HR'),
        (213, 'Faridabad', 'HR'),
        (214, 'Fatehabad', 'HR'),
        (215, 'Gurugram', 'HR'),
        (216, 'Hisar', 'HR'),
        (217, 'Jhajjar', 'HR'),
        (218, 'Jind', 'HR'),
        (219, 'Kaithal', 'HR'),
        (220, 'Karnal', 'HR'),
        (221, 'Kurukshetra', 'HR'),
        (222, 'Mahendragarh', 'HR'),
        (223, 'Nuh', 'HR'),
        (224, 'Palwal', 'HR'),
        (225, 'Panchkula', 'HR'),
        (226, 'Panipat', 'HR'),
        (227, 'Rewari', 'HR'),
        (228, 'Rohtak', 'HR'),
        (229, 'Sirsa', 'HR'),
        (230, 'Sonipat', 'HR'),
        (231, 'Yamunanagar', 'HR'),

-- Himachal Pradesh districts
        (232, 'Bilaspur', 'HP'),
        (233, 'Chamba', 'HP'),
        (234, 'Hamirpur', 'HP'),
        (235, 'Kangra', 'HP'),
        (236, 'Kinnaur', 'HP'),
        (237, 'Kullu', 'HP'),
        (238, 'Lahaul And Spiti', 'HP'),
        (239, 'Mandi', 'HP'),
        (240, 'Shimla', 'HP'),
        (241, 'Sirmaur', 'HP'),
        (242, 'Solan', 'HP'),
        (243, 'Una', 'HP'),

-- Jammu and Kashmir districts
        (244, 'Anantnag', 'JK'),
        (245, 'Bandipora', 'JK'),
        (246, 'Baramulla', 'JK'),
        (247, 'Budgam', 'JK'),
        (248, 'Doda', 'JK'),
        (249, 'Ganderbal', 'JK'),
        (250, 'Jammu', 'JK'),
        (251, 'Kathua', 'JK'),
        (252, 'Kishtwar', 'JK'),
        (253, 'Kulgam', 'JK'),
        (254, 'Kupwara', 'JK'),
        (255, 'Poonch', 'JK'),
        (256, 'Pulwama', 'JK'),
        (257, 'Rajouri', 'JK'),
        (258, 'Ramban', 'JK'),
        (259, 'Reasi', 'JK'),
        (260, 'Samba', 'JK'),
        (261, 'Shopian', 'JK'),
        (262, 'Srinagar', 'JK'),
        (263, 'Udhampur', 'JK'),

-- Jharkhand districts
        (264, 'Bokaro', 'JH'),
        (265, 'Chatra', 'JH'),
        (266, 'Deoghar', 'JH'),
        (267, 'Dhanbad', 'JH'),
        (268, 'Dumka', 'JH'),
        (269, 'East Singhbum', 'JH'),
        (270, 'Garhwa', 'JH'),
        (271, 'Giridih', 'JH'),
        (272, 'Godda', 'JH'),
        (273, 'Gumla', 'JH'),
        (274, 'Hazaribagh', 'JH'),
        (275, 'Jamtara', 'JH'),
        (276, 'Khunti', 'JH'),
        (277, 'Koderma', 'JH'),
        (278, 'Latehar', 'JH'),
        (279, 'Lohardaga', 'JH'),
        (280, 'Pakur', 'JH'),
        (281, 'Palamu', 'JH'),
        (282, 'Ramgarh', 'JH'),
        (283, 'Ranchi', 'JH'),
        (284, 'Sahebganj', 'JH'),
        (285, 'Saraikela Kharsawan', 'JH'),
        (286, 'Simdega', 'JH'),
        (287, 'West Singhbhum', 'JH'),

  --Karnataka
        (288, 'Bagalkote', 'KA'),
        (289, 'Ballari', 'KA'),
        (290, 'Belagavi', 'KA'),
        (291, 'Bengaluru Rural', 'KA'),
        (292, 'Bengaluru Urban', 'KA'),
        (293, 'Bidar', 'KA'),
        (294, 'Chamarajanagar', 'KA'),
        (295, 'Chikkaballapura', 'KA'),
        (296, 'Chikkamagaluru', 'KA'),
        (297, 'Chitradurga', 'KA'),
        (298, 'Dakshina Kannada', 'KA'),
        (299, 'Davanagere', 'KA'),
        (300, 'Dharwad', 'KA'),
        (301, 'Gadag', 'KA'),
        (302, 'Hassan', 'KA'),
        (303, 'Haveri', 'KA'),
        (304, 'Kalaburagi', 'KA'),
        (305, 'Kodagu', 'KA'),
        (306, 'Kolar', 'KA'),
        (307, 'Koppal', 'KA'),
        (308, 'Mandya', 'KA'),
        (309, 'Mysuru', 'KA'),
        (310, 'Raichur', 'KA'),
        (311, 'Ramanagara', 'KA'),
        (312, 'Shivamogga', 'KA'),
        (313, 'Yadgir', 'KA'),
        (314, 'Tumakuru', 'KA'),
        (315, 'Udupi', 'KA'),
        (316, 'Uttara Kannada', 'KA'),
        (317, 'Vijayanagara', 'KA'),
        (318, 'Vijayapura', 'KA'),

       --  Kerala
        (319, 'Alappuzha', 'KL'),
        (320, 'Ernakulam', 'KL'),
        (321, 'Idukki', 'KL'),
        (322, 'Kannur', 'KL'),
        (323, 'Kasaragod', 'KL'),
        (324, 'Kollam', 'KL'),
        (325, 'Kottayam', 'KL'),
        (326, 'Kozhikode', 'KL'),
        (327, 'Malappuram', 'KL'),
        (328, 'Palakkad', 'KL'),
        (329, 'Pathanamthitta', 'KL'),
        (330, 'Thiruvananthapuram', 'KL'),
        (331, 'Thrissur', 'KL'),
        (332, 'Wayanad', 'KL'),

        --Ladakh
        (333, 'Leh', 'LA'),
        (334, 'Kargil', 'LA'),

        -- Lakshadweep(UT)
        (335, 'Lakshadweep District', 'LD'),

        -- Madhya Pradesh
        (336, 'Agar-Malwa', 'MP'),
        (337, 'Alirajpur', 'MP'),
        (338, 'Anuppur', 'MP'),
        (339, 'Ashoknagar', 'MP'),
        (340, 'Balaghat', 'MP'),
        (341, 'Barwani', 'MP'),
        (342, 'Betul', 'MP'),
        (343, 'Bhind', 'MP'),
        (344, 'Bhopal', 'MP'),
        (345, 'Burhanpur', 'MP'),
        (346, 'Chhatarpur', 'MP'),
        (347, 'Chhindwara', 'MP'),
        (348, 'Damoh', 'MP'),
        (349, 'Datia', 'MP'),
        (350, 'Dewas', 'MP'),
        (351, 'Dhar', 'MP'),
        (352, 'Dindori', 'MP'),
        (353, 'Guna', 'MP'),
        (354, 'Gwalior', 'MP'),
        (355, 'Harda', 'MP'),
        (356, 'Indore', 'MP'),
        (357, 'Jabalpur', 'MP'),
        (358, 'Jhabua', 'MP'),
        (359, 'Katni', 'MP'),
        (360, 'Khandwa (East Nimar)', 'MP'),
        (361, 'Khargone (West Nimar)', 'MP'),
        (362, 'MAUGANJ', 'MP'),
        (363, 'Maihar', 'MP'),
        (364, 'Mandla', 'MP'),
        (365, 'Mandsaur', 'MP'),
        (366, 'Morena', 'MP'),
        (367, 'Narmadapuram', 'MP'),
        (368, 'Narsimhapur', 'MP'),
        (369, 'Neemuch', 'MP'),
        (370, 'Niwari', 'MP'),
        (371, 'Pandhurna', 'MP'),
        (372, 'Panna', 'MP'),
        (373, 'Raisen', 'MP'),
        (374, 'Rajgarh', 'MP'),
        (375, 'Ratlam', 'MP'),
        (376, 'Rewa', 'MP'),
        (377, 'Sagar', 'MP'),
        (378, 'Satna', 'MP'),
        (379, 'Sehore', 'MP'),
        (380, 'Seoni', 'MP'),
        (381, 'Shahdol', 'MP'),
        (382, 'Shajapur', 'MP'),
        (383, 'Sheopur', 'MP'),
        (384, 'Shivpuri', 'MP'),
        (385, 'Sidhi', 'MP'),
        (386, 'Singrauli', 'MP'),
        (387, 'Tikamgarh', 'MP'),
        (388, 'Ujjain', 'MP'),
        (389, 'Umaria', 'MP'),
        (390, 'Vidisha', 'MP'),

        -- Maharashtra
        (391, 'Ahilyanagar', 'MH'),
        (392, 'Akola', 'MH'),
        (393, 'Amravati', 'MH'),
        (394, 'Beed', 'MH'),
        (395, 'Bhandara', 'MH'),
        (396, 'Buldhana', 'MH'),
        (397, 'Chandrapur', 'MH'),
        (398, 'Chhatrapati Sambhajinagar', 'MH'),
        (399, 'Dharashiv', 'MH'),
        (400, 'Dhule', 'MH'),
        (401, 'Gadchiroli', 'MH'),
        (402, 'Gondia', 'MH'),
        (403, 'Hingoli', 'MH'),
        (404, 'Jalgaon', 'MH'),
        (405, 'Jalna', 'MH'),
        (406, 'Kolhapur', 'MH'),
        (407, 'Latur', 'MH'),
        (408, 'Mumbai', 'MH'),
        (409, 'Mumbai Suburban', 'MH'),
        (410, 'Nagpur', 'MH'),
        (411, 'Nanded', 'MH'),
        (412, 'Nandurbar', 'MH'),
        (413, 'Nashik', 'MH'),
        (414, 'Palghar', 'MH'),
        (415, 'Parbhani', 'MH'),
        (416, 'Pune', 'MH'),
        (417, 'Raigad', 'MH'),
        (418, 'Ratnagiri', 'MH'),
        (419, 'Sangli', 'MH'),
        (420, 'Satara', 'MH'),
        (421, 'Sindhudurg', 'MH'),
        (422, 'Solapur', 'MH'),
        (423, 'Thane', 'MH'),
        (424, 'Wardha', 'MH'),
        (425, 'Washim', 'MH'),
        (426, 'Yavatmal', 'MH'),

          -- Manipur
        (427, 'Bishnupur', 'MN'),
        (428, 'Chandel', 'MN'),
        (429, 'Churachandpur', 'MN'),
        (430, 'Imphal East', 'MN'),
        (431, 'Imphal West', 'MN'),
        (432, 'Jiribam', 'MN'),
        (433, 'Kakching', 'MN'),
        (434, 'Kamjong', 'MN'),
        (435, 'Kangpokpi', 'MN'),
        (436, 'Noney', 'MN'),
        (437, 'Pherzawl', 'MN'),
        (438, 'Senapati', 'MN'),
        (439, 'Tamenglong', 'MN'),
        (440, 'Tengnoupal', 'MN'),
        (441, 'Thoubal', 'MN'),
        (442, 'Ukhrul', 'MN'),

        -- Meghalaya
        (443, 'East Garo Hills', 'ML'),
        (444, 'East Jaintia Hills', 'ML'),
        (445, 'East Khasi Hills', 'ML'),
        (446, 'Eastern West Khasi Hills', 'ML'),
        (447, 'North Garo Hills', 'ML'),
        (448, 'Ri Bhoi', 'ML'),
        (449, 'South Garo Hills', 'ML'),
        (450, 'South West Garo Hills', 'ML'),
        (451, 'South West Khasi Hills', 'ML'),
        (452, 'West Garo Hills', 'ML'),
        (453, 'West Jaintia Hills', 'ML'),
        (454, 'West Khasi Hills', 'ML'),

        -- Mizoram
        (455, 'Aizawl', 'MZ'),
        (456, 'Champhai', 'MZ'),
        (457, 'Hnahthial', 'MZ'),
        (458, 'Khawzawl', 'MZ'),
        (459, 'Kolasib', 'MZ'),
        (460, 'Lawngtlai', 'MZ'),
        (461, 'Lunglei', 'MZ'),
        (462, 'Mamit', 'MZ'),
        (463, 'Saitual', 'MZ'),
        (464, 'Serchhip', 'MZ'),
        (465, 'Siaha', 'MZ'),

        -- Nagaland
        (466, 'Chumoukedima', 'NL'),
        (467, 'Dimapur', 'NL'),
        (468, 'Kiphire', 'NL'),
        (469, 'Kohima', 'NL'),
        (470, 'Longleng', 'NL'),
        (471, 'Mokokchung', 'NL'),
        (472, 'Mon', 'NL'),
        (473, 'Niuland', 'NL'),
        (474, 'Noklak', 'NL'),
        (475, 'Peren', 'NL'),
        (476, 'Phek', 'NL'),
        (477, 'Shamator', 'NL'),
        (478, 'Tseminyu', 'NL'),
        (479, 'Tuensang', 'NL'),
        (480, 'Wokha', 'NL'),
        (481, 'Zunheboto', 'NL'),

        -- Odisha
        (482, 'Anugul', 'OD'),
        (483, 'Balangir', 'OD'),
        (484, 'Baleshwar', 'OD'),
        (485, 'Bargarh', 'OD'),
        (486, 'Bhadrak', 'OD'),
        (487, 'Boudh', 'OD'),
        (488, 'Cuttack', 'OD'),
        (489, 'Deogarh', 'OD'),
        (490, 'Dhenkanal', 'OD'),
        (491, 'Gajapati', 'OD'),
        (492, 'Ganjam', 'OD'),
        (493, 'Jagatsinghapur', 'OD'),
        (494, 'Jajapur', 'OD'),
        (495, 'Jharsuguda', 'OD'),
        (496, 'Kalahandi', 'OD'),
        (497, 'Kandhamal', 'OD'),
        (498, 'Kendrapara', 'OD'),
        (499, 'Kendujhar', 'OD'),
        (500, 'Khordha', 'OD'),
        (501, 'Koraput', 'OD'),
        (502, 'Malkangiri', 'OD'),
        (503, 'Mayurbhanj', 'OD'),
        (504, 'Nabarangpur', 'OD'),
        (505, 'Nayagarh', 'OD'),
        (506, 'Nuapada', 'OD'),
        (507, 'Puri', 'OD'),
        (508, 'Rayagada', 'OD'),
        (509, 'Sambalpur', 'OD'),
        (510, 'Sonepur', 'OD'),
        (511, 'Sundargarh', 'OD'),

        -- Puducherry
        (512, 'Karaikal', 'PY'),
        (513, 'Puducherry', 'PY'),

        -- Punjab
        (514, 'Amritsar', 'PB'),
        (515, 'Barnala', 'PB'),
        (516, 'Bathinda', 'PB'),
        (517, 'Faridkot', 'PB'),
        (518, 'Fatehgarh Sahib', 'PB'),
        (519, 'Fazilka', 'PB'),
        (520, 'Ferozepur', 'PB'),
        (521, 'Gurdaspur', 'PB'),
        (522, 'Hoshiarpur', 'PB'),
        (523, 'Jalandhar', 'PB'),
        (524, 'Kapurthala', 'PB'),
        (525, 'Ludhiana', 'PB'),
        (526, 'Malerkotla', 'PB'),
        (527, 'Mansa', 'PB'),
        (528, 'Moga', 'PB'),
        (529, 'Pathankot', 'PB'),
        (530, 'Patiala', 'PB'),
        (531, 'Rupnagar', 'PB'),
        (532, 'S.A.S Nagar', 'PB'),
        (533, 'Sangrur', 'PB'),
        (534, 'Shahid Bhagat Singh Nagar', 'PB'),
        (535, 'Sri Muktsar Sahib', 'PB'),
        (536, 'Tarn Taran', 'PB'),

        -- Rajasthan
        (537, 'Ajmer', 'RJ'),
        (538, 'Alwar', 'RJ'),
        (539, 'Anupgarh', 'RJ'),
        (540, 'Balotra', 'RJ'),
        (541, 'Banswara', 'RJ'),
        (542, 'Baran', 'RJ'),
        (543, 'Barmer', 'RJ'),
        (544, 'Beawar', 'RJ'),
        (545, 'Bharatpur', 'RJ'),
        (546, 'Bhilwara', 'RJ'),
        (547, 'Bikaner', 'RJ'),
        (548, 'Bundi', 'RJ'),
        (549, 'Chittorgarh', 'RJ'),
        (550, 'Churu', 'RJ'),
        (551, 'Dausa', 'RJ'),
        (552, 'Deeg', 'RJ'),
        (553, 'Dholpur', 'RJ'),
        (554, 'Didwana-Kuchaman', 'RJ'),
        (555, 'Dudu', 'RJ'),
        (556, 'Dungarpur', 'RJ'),
        (557, 'Ganganagar', 'RJ'),
        (558, 'Gangapurcity', 'RJ'),
        (559, 'Hanumangarh', 'RJ'),
        (560, 'Jaipur', 'RJ'),
        (561, 'Jaipur (Gramin)', 'RJ'),
        (562, 'Jaisalmer', 'RJ'),
        (563, 'Jalore', 'RJ'),
        (564, 'Jhalawar', 'RJ'),
        (565, 'Jhunjhunu', 'RJ'),
        (566, 'Jodhpur', 'RJ'),
        (567, 'Jodhpur (Gramin)', 'RJ'),
        (568, 'Karauli', 'RJ'),
        (569, 'Kekri', 'RJ'),
        (570, 'Khairthal-Tijara', 'RJ'),
        (571, 'Kota', 'RJ'),
        (572, 'Kotputli-Behror', 'RJ'),
        (573, 'Nagaur', 'RJ'),
        (574, 'Neem Ka Thana', 'RJ'),
        (575, 'Pali', 'RJ'),
        (576, 'Phalodi', 'RJ'),
        (577, 'Shahpura', 'RJ'),
        (578, 'Sikar', 'RJ'),
        (579, 'Sirohi', 'RJ'),
        (580, 'Tonk', 'RJ'),
        (581, 'Udaipur', 'RJ'),
        (582, 'Pratapgarh', 'RJ'),
        (583, 'Rajsamand', 'RJ'),
        (584, 'Salumbar', 'RJ'),
        (585, 'Sanchore', 'RJ'),
        (586, 'Sawai Madhopur', 'RJ'),

        -- Sikkim
        (587, 'Gangtok', 'SK'),
        (588, 'Gyalshing', 'SK'),
        (589, 'Mangan', 'SK'),
        (590, 'Namchi', 'SK'),
        (591, 'Pakyong', 'SK'),
        (592, 'Soreng', 'SK'),

        -- Tamil Nadu
        (593, 'Ariyalur', 'TN'),
        (594, 'Chengalpattu', 'TN'),
        (595, 'Chennai', 'TN'),
        (596, 'Coimbatore', 'TN'),
        (597, 'Cuddalore', 'TN'),
        (598, 'Dharmapuri', 'TN'),
        (599, 'Dindigul', 'TN'),
        (600, 'Erode', 'TN'),
        (601, 'Kallakurichi', 'TN'),
        (602, 'Kancheepuram', 'TN'),
        (603, 'Kanniyakumari', 'TN'),
        (604, 'Karur', 'TN'),
        (605, 'Krishnagiri', 'TN'),
        (606, 'Madurai', 'TN'),
        (607, 'Mayiladuthurai', 'TN'),
        (608, 'Nagapattinam', 'TN'),
        (609, 'Namakkal', 'TN'),
        (610, 'Perambalur', 'TN'),
        (611, 'Pudukkottai', 'TN'),
        (612, 'Ramanathapuram', 'TN'),
        (613, 'Ranipet', 'TN'),
        (614, 'Salem', 'TN'),
        (615, 'Sivaganga', 'TN'),
        (616, 'Tenkasi', 'TN'),
        (617, 'Thanjavur', 'TN'),
        (618, 'Tiruchirappalli', 'TN'),
        (619, 'Tirunelveli', 'TN'),
        (620, 'Tirupathur', 'TN'),
        (621, 'Tiruppur', 'TN'),
        (622, 'Tiruvannamalai', 'TN'),
        (623, 'The Nilgiris', 'TN'),
        (624, 'Theni', 'TN'),
        (625, 'Thiruvallur', 'TN'),
        (626, 'Thiruvarur', 'TN'),
        (627, 'Thoothukkudi', 'TN'),
        (628, 'Vellore', 'TN'),
        (629, 'Viluppuram', 'TN'),
        (630, 'Virudhunagar', 'TN'),

        -- Telangana
        (631, 'Adilabad', 'TS'),
        (632, 'Bhadradri Kothagudem', 'TS'),
        (633, 'Hanumakonda', 'TS'),
        (634, 'Hyderabad', 'TS'),
        (635, 'Jagitial', 'TS'),
        (636, 'Jangoan', 'TS'),
        (637, 'Jayashankar Bhupalapally', 'TS'),
        (638, 'Jogulamba Gadwal', 'TS'),
        (639, 'Kamareddy', 'TS'),
        (640, 'Karimnagar', 'TS'),
        (641, 'Khammam', 'TS'),
        (642, 'Kumuram Bheem Asifabad', 'TS'),
        (643, 'Mahabubabad', 'TS'),
        (644, 'Mahabubnagar', 'TS'),
        (645, 'Mancherial', 'TS'),
        (646, 'Medak', 'TS'),
        (647, 'Medchal Malkajgiri', 'TS'),
        (648, 'Mulugu', 'TS'),
        (649, 'Nagarkurnool', 'TS'),
        (650, 'Nalgonda', 'TS'),
        (651, 'Narayanpet', 'TS'),
        (652, 'Nirmal', 'TS'),
        (653, 'Nizamabad', 'TS'),
        (654, 'Peddapalli', 'TS'),
        (655, 'Rajanna Sircilla', 'TS'),
        (656, 'Ranga Reddy', 'TS'),
        (657, 'Sangareddy', 'TS'),
        (658, 'Siddipet', 'TS'),
        (659, 'Suryapet', 'TS'),
        (660, 'Vikarabad', 'TS'),
        (661, 'Wanaparthy', 'TS'),
        (662, 'Warangal', 'TS'),
        (663, 'Yadadri Bhuvanagiri', 'TS'),

        -- Tripura
        (664, 'Dhalai', 'TR'),
        (665, 'Gomati', 'TR'),
        (666, 'Khowai', 'TR'),
        (667, 'North Tripura', 'TR'),
        (668, 'Sepahijala', 'TR'),
        (669, 'South Tripura', 'TR'),
        (670, 'Unakoti', 'TR'),
        (671, 'West Tripura', 'TR'),

        -- Uttar Pradesh
        (672, 'Agra', 'UP'),
        (673, 'Aligarh', 'UP'),
        (674, 'Ambedkar Nagar', 'UP'),
        (675, 'Amethi', 'UP'),
        (676, 'Amroha', 'UP'),
        (677, 'Auraiya', 'UP'),
        (678, 'Ayodhya', 'UP'),
        (679, 'Azamgarh', 'UP'),
        (680, 'Baghpat', 'UP'),
        (681, 'Bahraich', 'UP'),
        (682, 'Ballia', 'UP'),
        (683, 'Balrampur', 'UP'),
        (684, 'Banda', 'UP'),
        (685, 'Bara Banki', 'UP'),
        (686, 'Bareilly', 'UP'),
        (687, 'Basti', 'UP'),
        (688, 'Bhadohi', 'UP'),
        (689, 'Bijnor', 'UP'),
        (690, 'Budaun', 'UP'),
        (691, 'Bulandshahr', 'UP'),
        (692, 'Chandauli', 'UP'),
        (693, 'Chitrakoot', 'UP'),
        (694, 'Deoria', 'UP'),
        (695, 'Etah', 'UP'),
        (696, 'Etawah', 'UP'),
        (697, 'Farrukhabad', 'UP'),
        (698, 'Fatehpur', 'UP'),
        (699, 'Firozabad', 'UP'),
        (700, 'Gautam Buddha Nagar', 'UP'),
        (701, 'Ghaziabad', 'UP'),
        (702, 'Ghazipur', 'UP'),
        (703, 'Gonda', 'UP'),
        (704, 'Gorakhpur', 'UP'),
        (705, 'Hamirpur', 'UP'),
        (706, 'Hapur', 'UP'),
        (707, 'Hardoi', 'UP'),
        (708, 'Hathras', 'UP'),
        (709, 'Jalaun', 'UP'),
        (710, 'Jaunpur', 'UP'),
        (711, 'Jhansi', 'UP'),
        (712, 'Kheri', 'UP'),
        (713, 'Kushinagar', 'UP'),
        (714, 'Lalitpur', 'UP'),
        (715, 'Lucknow', 'UP'),
        (716, 'Mahoba', 'UP'),
        (717, 'Kannauj', 'UP'),
        (718, 'Kanpur Dehat', 'UP'),
        (719, 'Kanpur Nagar', 'UP'),
        (720, 'Kasganj', 'UP'),
        (721, 'Kaushambi', 'UP'),
        (722, 'Mahrajganj', 'UP'),
        (723, 'Mainpuri', 'UP'),
        (724, 'Mathura', 'UP'),
        (725, 'Mau', 'UP'),
        (726, 'Meerut', 'UP'),
        (727, 'Prayagraj', 'UP'),
        (728, 'Rae Bareli', 'UP'),
        (729, 'Rampur', 'UP'),
        (730, 'Saharanpur', 'UP'),
        (731, 'Sambhal', 'UP'),
        (732, 'Mirzapur', 'UP'),
        (733, 'Moradabad', 'UP'),
        (734, 'Muzaffarnagar', 'UP'),
        (735, 'Pilibhit', 'UP'),
        (736, 'Pratapgarh', 'UP'),
        (737, 'Sant Kabir Nagar', 'UP'),
        (738, 'Shahjahanpur', 'UP'),
        (739, 'Shamli', 'UP'),
        (740, 'Shrawasti', 'UP'),
        (741, 'Siddharthnagar', 'UP'),
        (742, 'Sitapur', 'UP'),
        (743, 'Sonbhadra', 'UP'),
        (744, 'Sultanpur', 'UP'),
        (745, 'Unnao', 'UP'),
        (746, 'Varanasi', 'UP'),

        -- Uttarakhand
        (747, 'Almora', 'UT'),
        (748, 'Bageshwar', 'UT'),
        (749, 'Chamoli', 'UT'),
        (750, 'Champawat', 'UT'),
        (751, 'Dehradun', 'UT'),
        (752, 'Haridwar', 'UT'),
        (753, 'Nainital', 'UT'),
        (754, 'Pauri Garhwal', 'UT'),
        (755, 'Pithoragarh', 'UT'),
        (756, 'Rudra Prayag', 'UT'),
        (757, 'Tehri Garhwal', 'UT'),
        (758, 'Udam Singh Nagar', 'UT'),
        (759, 'Uttar Kashi', 'UT'),

        -- West Bengal
        (760, 'Alipurduar', 'WB'),
        (761, 'Bankura', 'WB'),
        (762, 'Birbhum', 'WB'),
        (763, 'Cooch Behar', 'WB'),
        (764, 'Dakshin Dinajpur', 'WB'),
        (765, 'Darjeeling', 'WB'),
        (766, 'Hooghly', 'WB'),
        (767, 'Howrah', 'WB'),
        (768, 'Jalpaiguri', 'WB'),
        (769, 'Jhargram', 'WB'),
        (770, 'Kalimpong', 'WB'),
        (771, 'Kolkata', 'WB'),
        (772, 'Malda', 'WB'),
        (773, 'Murshidabad', 'WB'),
        (774, 'Nadia', 'WB'),
        (775, 'North 24 Parganas', 'WB'),
        (776, 'Paschim Bardhaman', 'WB'),
        (777, 'Paschim Medinipur', 'WB'),
        (778, 'Purba Bardhaman', 'WB'),
        (779, 'Purba Medinipur', 'WB'),
        (780, 'Purulia', 'WB'),
        (781, 'South 24 Parganas', 'WB'),
        (782, 'Uttar Dinajpur', 'WB'),

        --Andaman and Nicobar Islands
        (783, 'Nicobars', 'AN'),
        (784, 'North And Middle Andaman', 'AN'),
        (785, 'South Andamans', 'AN');

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
        (3, 'BACHELORS/GRADUATION', 'Completed undergraduate degree program', FALSE, TRUE),
        (4, 'MASTERS/POST_GRADUATION', 'Completed postgraduate degree program', FALSE, TRUE),
        (5, 'DOCTORATE', 'Completed doctoral degree program', FALSE, TRUE),
        (6, 'DIPLOMA', 'Completed a diploma program', FALSE, TRUE),
        (7, 'ITI', 'Completed an ITI (Industrial Training Institute) program', FALSE, TRUE),

-- Bachelors Degrees
        (8, 'B.A.', 'Bachelor of Arts', FALSE, TRUE),
        (9, 'B.Sc.', 'Bachelor of Science', FALSE, TRUE),
        (10, 'B.Com.', 'Bachelor of Commerce', FALSE, TRUE),
        (11, 'B.Tech.', 'Bachelor of Technology', FALSE, TRUE),
        (12, 'B.E.', 'Bachelor of Engineering', FALSE, TRUE),
        (13, 'MBBS', 'Bachelor of Medicine, Bachelor of Surgery', FALSE, TRUE),
        (14, 'BDS', 'Bachelor of Dental Surgery', FALSE, TRUE),
        (15, 'BBA', 'Bachelor of Business Administration', FALSE, TRUE),
        (16, 'BCA', 'Bachelor of Computer Applications', FALSE, TRUE),
        (17, 'B.Pharm', 'Bachelor of Pharmacy', FALSE, TRUE),
        (18, 'B.Arch', 'Bachelor of Architecture', FALSE, TRUE),
        (19, 'B.Planning', 'Bachelor of Planning', FALSE, TRUE),
        (20, 'LLB', 'Bachelor of Laws', FALSE, TRUE),
        (21, 'B.A. (Honours)', 'Bachelor of Arts (Honours)', FALSE, TRUE),
        (22, 'B.Sc. (Honours)', 'Bachelor of Science (Honours)', FALSE, TRUE),
        (23, 'B.Com. (Honours)', 'Bachelor of Commerce (Honours)', FALSE, TRUE),
        (24, 'BAMS', 'Bachelor of Ayurvedic Medicine and Surgery', FALSE, TRUE),
        (25, 'B.Sc. (Nursing)', 'Bachelor of Science in Nursing', FALSE, TRUE),
        (26, 'BPT', 'Bachelor of Physiotherapy', FALSE, TRUE),
        (27, 'BHMS', 'Bachelor of Homeopathic Medicine and Surgery', FALSE, TRUE),
        (28, 'BMLT', 'Bachelor of Medical Laboratory Technology', FALSE, TRUE),
        (29, 'BHMCT', 'Bachelor of Hotel Management and Catering Technology', FALSE, TRUE),
        (30, 'BTTM', 'Bachelor of Tourism and Travel Management', FALSE, TRUE),
        (31, 'B.Voc', 'Bachelor of Vocation', FALSE, TRUE),
        (32, 'B.P.Ed', 'Bachelor of Physical Education', FALSE, TRUE),
        (33, 'B.Ed', 'Bachelor of Education', FALSE, TRUE),
        (34, 'B.Li.Sc', 'Bachelor of Library Science', FALSE, TRUE),

-- Masters Degrees
        (35, 'M.A.', 'Master of Arts', FALSE, TRUE),
        (36, 'M.Sc.', 'Master of Science', FALSE, TRUE),
        (37, 'M.Com.', 'Master of Commerce', FALSE, TRUE),
        (38, 'M.Tech.', 'Master of Technology', FALSE, TRUE),
        (39, 'M.E.', 'Master of Engineering', FALSE, TRUE),
        (40, 'MD', 'Doctor of Medicine', FALSE, TRUE),
        (41, 'MS', 'Master of Surgery', FALSE, TRUE),
        (42, 'MBA', 'Master of Business Administration', FALSE, TRUE),
        (43, 'MCA', 'Master of Computer Applications', FALSE, TRUE),
        (44, 'M.Pharm', 'Master of Pharmacy', FALSE, TRUE),
        (45, 'M.Arch', 'Master of Architecture', FALSE, TRUE),
        (46, 'M.Planning', 'Master of Planning', FALSE, TRUE),
        (47, 'LLM', 'Master of Laws', FALSE, TRUE),
        (48, 'M.Ed', 'Master of Education', FALSE, TRUE),
        (49, 'M.Lib.Sc.', 'Master of Library Science', FALSE, TRUE),
        (50, 'MDS', 'Master of Dental Surgery', FALSE, TRUE),
        (51, 'MPT', 'Master of Physiotherapy', FALSE, TRUE),
        (52, 'M.Sc. (Nursing)', 'Master of Science in Nursing', FALSE, TRUE),
        (53, 'M.Sc. (Agriculture)', 'Master of Science in Agriculture', FALSE, TRUE),
        (54, 'M.Sc. (Environmental Science)', 'Master of Science in Environmental Science', FALSE, TRUE),
        (55, 'M.P.Ed', 'Master of Physical Education', FALSE, TRUE),
        (56, 'MHMCT', 'Master of Hotel Management and Catering Technology', FALSE, TRUE),
        (57, 'MSW', 'Master of Social Work', FALSE, TRUE),
        (58, 'M.Des', 'Master of Design in Fashion', FALSE, TRUE),
        (59, 'MJMC', 'Master of Journalism and Mass Communication', FALSE, TRUE),

        (60, 'Others', 'Others', FALSE, TRUE);
END IF;

    -- Mapping for INTERMEDIATE/12th (qualification_id: 2)
  -- 12th Standard Streams (qualification_id = 2)
IF NOT EXISTS (SELECT 1 FROM qualification_stream) THEN

        -- Insert values if the table is empty

        -- 12th Stream
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 2, stream_id
        FROM custom_stream
        WHERE stream_id IN (1, 2, 3, 4);

        -- Bachelor of Arts and BA Honours (qualification_id = 8, 21)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT qualification_id, stream_id
        FROM qualification CROSS JOIN custom_stream
        WHERE qualification_id IN (8, 21)
        AND stream_id IN (5, 6, 7, 8, 9, 142, 143, 144, 145, 146, 147);

        -- Bachelor of Science and BSc Honours (qualification_id = 9, 22)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT qualification_id, stream_id
        FROM qualification CROSS JOIN custom_stream
        WHERE qualification_id IN (9, 22)
        AND stream_id IN (10, 11, 12, 13, 14, 15, 148, 149, 150, 151, 152);

        -- Bachelor of Commerce and BCom Honours (qualification_id = 10, 23)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT qualification_id, stream_id
        FROM qualification CROSS JOIN custom_stream
        WHERE qualification_id IN (10, 23)
        AND stream_id IN (16, 17, 18, 153, 154, 155, 156, 157);

        -- BTech/BE (qualification_id = 11, 12)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT qualification_id, stream_id
        FROM qualification CROSS JOIN custom_stream
        WHERE qualification_id IN (11, 12)
        AND stream_id IN (19, 20, 21, 22, 158, 159);

        -- BAMS (qualification_id = 24)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 24, stream_id
        FROM custom_stream
        WHERE stream_id BETWEEN 160 AND 165;

        -- BSc Nursing (qualification_id = 25)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 25, stream_id
        FROM custom_stream
        WHERE stream_id BETWEEN 166 AND 171;

        -- BPT (qualification_id = 26)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 26, stream_id
        FROM custom_stream
        WHERE stream_id BETWEEN 172 AND 176;

        -- BHMS (qualification_id = 27)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 27, stream_id
        FROM custom_stream
        WHERE stream_id BETWEEN 177 AND 182;

        -- BMLT (qualification_id = 28)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 28, stream_id
        FROM custom_stream
        WHERE stream_id BETWEEN 183 AND 187;

        -- BHMCT (qualification_id = 29)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 29, stream_id
        FROM custom_stream
        WHERE stream_id BETWEEN 188 AND 191;

        -- BTTM (qualification_id = 30)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 30, stream_id
        FROM custom_stream
        WHERE stream_id BETWEEN 192 AND 195;

        -- B.Voc (qualification_id = 31)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 31, stream_id
        FROM custom_stream
        WHERE stream_id BETWEEN 196 AND 201;

        -- B.P.Ed (qualification_id = 32)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 32, stream_id
        FROM custom_stream
        WHERE stream_id BETWEEN 202 AND 205;

        -- B.Ed (qualification_id = 33)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 33, stream_id
        FROM custom_stream
        WHERE stream_id BETWEEN 206 AND 210;

        -- B.Li.Sc (qualification_id = 34)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 34, stream_id
        FROM custom_stream
        WHERE stream_id BETWEEN 211 AND 214;

        -- Masters Degrees
        -- MA (qualification_id = 35)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 35, stream_id
        FROM custom_stream
        WHERE stream_id IN (43, 44, 45, 46, 47);

        -- MSc (qualification_id = 36)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 36, stream_id
        FROM custom_stream
        WHERE stream_id IN (48, 49, 50, 51, 52);

        -- MTech/ME (qualification_id = 38, 39)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT qualification_id, stream_id
        FROM qualification CROSS JOIN custom_stream
        WHERE qualification_id IN (38, 39)
        AND stream_id IN (54, 59, 60, 61);

        -- MD (qualification_id = 40)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 40, stream_id
        FROM custom_stream
        WHERE stream_id IN (62, 63, 64, 65);

        -- MS (qualification_id = 41)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 41, stream_id
        FROM custom_stream
        WHERE stream_id IN (66, 67, 68);

        -- Diplomas (qualification_id = 6)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 6, stream_id
        FROM custom_stream
        WHERE stream_id BETWEEN 85 AND 103;

        -- ITI (qualification_id = 7)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 7, stream_id
        FROM custom_stream
        WHERE stream_id BETWEEN 104 AND 121;

        -- PhD (qualification_id = 5)
        INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT 5, stream_id
        FROM custom_stream
        WHERE stream_id BETWEEN 122 AND 141;

--        INSERT INTO qualification_stream (qualification_id, stream_id)
--        SELECT q.qualification_id, cs.stream_id
--        FROM qualification q
--        CROSS JOIN custom_stream cs
--        WHERE cs.stream_id IN (215, 216)  -- 215 for 'NA' and 216 for 'Others'
--        AND q.qualification_id BETWEEN 1 AND 60;

INSERT INTO qualification_stream (qualification_id, stream_id)
        SELECT q.qualification_id, cs.stream_id
        FROM qualification q
        CROSS JOIN custom_stream cs
        WHERE cs.stream_id IN (215)  -- 215 for 'NA' and 216 for 'Others'
        AND q.qualification_id BETWEEN 1 AND 60;

        INSERT INTO qualification_stream (qualification_id, stream_id)
                SELECT q.qualification_id, cs.stream_id
                FROM qualification q
                CROSS JOIN custom_stream cs
                WHERE cs.stream_id IN (216)  -- 215 for 'NA' and 216 for 'Others'
                AND q.qualification_id BETWEEN 3 AND 60;
    END IF;

      INSERT INTO stream_subject (stream_id, subject_id)
      SELECT * FROM (VALUES
          -- Mapping for 10th Stream
          (0, 1), (0, 2), (0, 3), (0, 4), (0, 5), (0, 6), (0, 7), (0, 8), (0, 9), (0, 10),
          (0, 11), (0, 12), (0, 13), (0, 14), (0, 15), (0, 16), (0, 17), (0, 18), (0, 19),
          (0, 20), (0, 21), (0, 22), (0, 23), (0, 24), (0, 25), (0, 26), (0, 27), (0, 28),
          (0, 29), (0, 30), (0, 31), (0, 32), (0, 33), (0, 34),

          -- Mapping for 12th Science
          (1, 4), (1, 6), (1, 7), (1, 8), (1, 26), (1, 33), (1, 20), (1, 34), (1, 35),
          (1, 36), (1, 37), (1, 38), (1, 12), (1, 39), (1, 40), (1, 41), (1, 52), (1, 1),

          -- Mapping for 12th Arts
          (2, 4), (2, 12), (2, 38), (2, 9), (2, 42), (2, 10), (2, 43), (2, 5), (2, 17),
          (2, 44), (2, 45), (2, 20), (2, 26), (2, 33), (2, 34), (2, 35), (2, 36), (2, 41),
          (2, 46), (2, 47), (2, 48), (2, 49), (2, 50), (2, 51), (2, 52), (2, 53), (2, 1),
          (2, 37), (2, 13), (2, 39),

          -- Mapping for 12th Commerce
          (3, 4), (3, 53), (3, 23), (3, 51), (3, 12), (3, 26), (3, 35), (3, 20), (3, 1),
          (3, 34), (3, 36), (3, 46), (3, 48), (3, 49), (3, 50), (3, 37), (3, 41), (3, 39),

--Mapping for Others Streams
          (215,1),(215,2),(215,3),(215,4),(215,5),(215,6),(215,7),(215,8),(215,9),(215,10),(215,11),(215,12),(215,13),(215,14),(215,15),
          (215,16),(215,17),(215,18),(215,19),(215,20),(215,21),(215,22),(215,23),(215,24),(215,25),(215,26),(215,27),(215,28),(215,29),(215,30),(215,31)
          ,(215,32),(215,33),(215,34),(215,35),(215,36),(215,37),(215,38),(215,39),(215,40),(215,41),(215,42),(215,43),(215,44),(215,45),(215,46)
          ,(215,47),(215,48),(215,49),(215,50),(215,51),(215,52),(215,53),
          -- Others with all
          (0, 54), (1, 54), (2, 54), (3, 54),(215,54)

      ) AS tmp(stream_id, subject_id)
      WHERE NOT EXISTS (SELECT 1 FROM stream_subject)
      ON CONFLICT DO NOTHING;


 IF NOT EXISTS (SELECT 1 FROM typing_text LIMIT 1) THEN
        INSERT INTO typing_text (id, text)
        VALUES
            (1, 'The quick brown fox jumps over the lazy dog near the quiet river, while the bright sun sets in the horizon, casting beautiful hues of orange.'),
            (2, 'A curious cat chased a butterfly through the green meadows, unaware of the gentle breeze swirling around.'),
            (3, 'In the silent night, a lone owl hooted softly as the stars twinkled brightly above the peaceful forest.'),
            (4, 'Beneath the tall mountains, a small village thrived with joy, laughter, and the warmth of togetherness.'),
            (5, 'The adventure begins with a journey through unknown lands, filled with unexpected challenges and thrilling discoveries along the way.');
    END IF;
--
    -- Insert into service_provider_test_status if empty
    IF NOT EXISTS (SELECT 1 FROM service_provider_test_status LIMIT 1) THEN
        INSERT INTO service_provider_test_status (test_status_id, test_status_name, test_status_description, created_at, updated_at, created_by)
        VALUES
            (1, 'New', 'The service provider has registered but has not yet completed the test.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN'),
            (2, 'Completed Test', 'The service provider has completed the required skill tests.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN'),
            (3, 'Approved', 'The service provider submission has been reviewed and approved.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN'),
            (4, 'Rejected', 'The service provider submission was rejected due to not meeting the criteria.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN'),
            (5, 'Suspended', 'The service provider account is currently suspended due to policy violations.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN');
    END IF;

    -- Insert into service_provider_rank if empty
    IF NOT EXISTS (SELECT 1 FROM service_provider_rank LIMIT 1) THEN
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

    -- Insert into custom_admin if empty
    IF NOT EXISTS (SELECT 1 FROM custom_admin LIMIT 1) THEN
        CREATE EXTENSION IF NOT EXISTS pgcrypto;
        INSERT INTO custom_admin (admin_id, role, password, user_name, mobilenumber, country_code, signedup, created_at, created_by)
        VALUES
            (1, 2, crypt('Admin#01', gen_salt('bf', 8)), 'admin', '7740066387', '+91', 0, CURRENT_DATE, 'SUPER_ADMIN'),
            (2, 1, crypt('SuperAdmin#1357', gen_salt('bf', 8)), 'superadmin', '9872548680', '+91', 0, CURRENT_DATE, 'SUPER_ADMIN'),
            (3, 3, crypt('AdminServiceProvider#02', gen_salt('bf', 8)), 'adminserviceprovider', '7710393096', '+91', 0, CURRENT_DATE, 'SUPER_ADMIN');
    END IF;

    -- Insert into scoring_criteria if empty
    IF NOT EXISTS (SELECT 1 FROM scoring_criteria LIMIT 1) THEN
        INSERT INTO scoring_criteria (id, attribute_name, condition, score)
        VALUES
            (1, 'Business Unit / Infrastructure', 'If it is a Business Unit: 20 points', 20),
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
--
    -- Insert into order_state_ref if empty
    IF NOT EXISTS (SELECT 1 FROM order_state_ref LIMIT 1) THEN
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
   IF NOT EXISTS (SELECT 1 FROM custom_document LIMIT 1)THEN
             INSERT INTO custom_document (document_type_id, document_type_name, description, max_document_size, min_document_size,
                 is_qualification_document, is_issue_date_required, is_expiration_date_required, sort_order)
             VALUES
            (1, 'Aadhaar_Card_Front', 'Front side of a government-issued ID card in India.', '200KB', '100KB', FALSE, FALSE, FALSE, 2),
            (2, 'Pan_Card', 'A permanent account number card for tax purposes in India.', '200KB', '100KB', FALSE, FALSE, FALSE, 4),
            (3, 'Live_Passport_Size_Photo', 'A live photo typically used for official documents.', '200KB', '100KB', FALSE, FALSE, FALSE, 5),
            (4, 'Signature', 'A handwritten sign used to authenticate documents.', '100KB', '50KB', FALSE, FALSE, FALSE, 1),
            (5, 'Ews_Certificate', 'Certificate for individuals and families below a certain income threshold to access various benefits and concessions.', '300KB', '200KB', FALSE, TRUE, FALSE, 29),
            (6, 'Caste_Certificate', 'Certifies an individuals caste for reservations and benefits in education and employment.', '300KB', '200KB', FALSE, TRUE, FALSE, 6),
            (7, 'Address_Certificate', 'Verifies an individuals residential address for identity verification and other purposes.', '500KB', '100KB', FALSE, FALSE, FALSE, 26),
            (8, 'Income_Certificate', 'Confirms an individuals or family annual income for applying for government benefits and financial assistance.', '500KB', '100KB', FALSE, FALSE, FALSE, 27),
            (9, 'Driving_License', 'Authorizes an individual to operate motor vehicles, confirming knowledge of traffic laws and vehicle operation skills.', '200KB', '100KB', FALSE, FALSE, FALSE, 28),
            (10, 'Domicile', 'The permanent home or principal residence of a person.', '300KB', '200KB', FALSE, TRUE, FALSE, 9),
            (11, 'Disability_Certificate', 'An outdated term for individuals with physical or mental disabilities, person with a disability is preferred today.', '300KB', '200KB', FALSE, FALSE, FALSE, 10),
            (12, 'Mark_Sheet', 'Mark sheet of Qualification.', '300KB', '200KB', TRUE, FALSE, FALSE, 25),
            (13, 'Others', 'Includes other document types not listed above, tailored to specific needs or contexts.', '500KB', '50KB', FALSE, FALSE, FALSE, 1000),
            (14, 'C_Form_Photo', 'A C Form photo is a standardized ID photo for official documents.', '200KB', '100KB', FALSE, FALSE, FALSE, 24),
            (15, 'Ex_Service_Men', 'Ex Service Men document is required for individuals who have previously worked in the organization and are now no longer employed.', '300KB', '200KB', FALSE, FALSE, FALSE, 11),
            (16, 'Business_Photo', 'A Standard proof of Running Business.', '200KB', '100KB', FALSE, FALSE, FALSE, 23),
            (17, 'Personal_Photo', 'A Personal Photograph of SP.', '200KB', '100KB', FALSE, FALSE, FALSE, 0),
            (18, 'NCC_Certificate_A', 'Ncc CERTIFICATE A', '500KB', '100KB', FALSE, FALSE, FALSE, 12),
            (19, 'NCC_Certificate_B', 'NCC CERTIFICATE B', '500KB', '100KB', FALSE, FALSE, FALSE, 13),
            (20, 'NCC_Certificate_C', 'NCC CERTIFICATE C', '500KB', '100KB', FALSE, FALSE, FALSE, 14),
            (21, 'NSS_Certificate_A', 'NSS CERTIFICATE A', '500KB', '100KB', FALSE, FALSE, FALSE, 15),
            (22, 'Sports_Certificate_State', 'SPORTS CERTIFICATE FOR STATE LEVEL', '200KB', '100KB', FALSE, FALSE, FALSE, 18),
            (23, 'Sports_Certificate_Centre', 'SPORTS CERTIFICATE FOR CENTRE LEVEL.', '200KB', '100KB', FALSE, FALSE, FALSE, 19),
            (24, 'Aadhaar_Card_Backside', 'Back side of a government issued ID card in India.', '200KB', '100KB', FALSE, FALSE, FALSE, 3),
            (25, 'Left_Thumb_Impression', 'The left thumb impression of the individual typically required for identity verification in official documents.', '100KB', '50KB', FALSE, FALSE, FALSE, 20),
            (26, 'Right_Thumb_Impression', 'The right thumb impression of the individual typically required for identity verification in official documents.', '100KB', '50KB', FALSE, FALSE, FALSE, 21),
            (27, 'White_Background_Passport_Size_Photo', 'A white background passport size photo typically used for official documents.', '200KB', '100KB', FALSE, FALSE, FALSE, 22),
            (28, 'NSS_Certificate_B', 'NSS CERTIFICATE B', '500KB', '100KB', TRUE, TRUE, TRUE, 16),
            (29, 'NSS_Certificate_C', 'NSS CERTIFICATE C', '500KB', '100KB', TRUE, FALSE, FALSE, 17),
            (30, 'Other_State_Category', 'Other or State Category which is not present in master list', '300KB', '200KB', FALSE, TRUE, FALSE,7),
            (31, 'Minority_Certificate', 'Minority Certificate', '300KB', '200KB', FALSE, FALSE, FALSE,8);
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
            (13, 1), (13, 2),(13,3) ,(13, 4),
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
            (29, 1), (29, 2), (29, 4),
            (30, 1), (30, 2), (30, 4),
            (31, 1), (31, 2), (31, 4);
    END IF;
--
   IF NOT EXISTS (SELECT 1 FROM board_university WHERE board_university_id = 1) THEN
    INSERT INTO board_university (board_university_id, board_university_name, board_university_location, board_university_code, board_university_type, created_date, modified_date, created_by, modified_by)
    VALUES
        (1, 'Others', 'Others', 'Others', 'Others', NOW(), NOW(), 'SUPER_ADMIN', 'SUPER_ADMIN'),
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
		(1,'Others','Others','Others',NOW(),NOW(),'SUPER_ADMIN','SUPER_ADMIN'),
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
--
    -- Check and insert into vacancy_distribution_type table
    IF NOT EXISTS (SELECT 1 FROM vacancy_distribution_type) THEN
        INSERT INTO vacancy_distribution_type (vacancydistributiontypeid, vacancyDistributionTypeName)
        VALUES
            (1, 'State Wise Distribution'),
            (2, 'Zone Wise Distribution'),
            (3, 'Category Wise Distribution'),
            (4, 'Others');
    END IF;

     IF NOT EXISTS (SELECT 1 FROM custom_mode) THEN
            INSERT INTO custom_mode (custom_mode_id, custom_mode_name)
            VALUES
                (1, 'Email'),
                (2, 'Whatsapp'),
                (3, 'SMS');
    END IF;

END $$;

