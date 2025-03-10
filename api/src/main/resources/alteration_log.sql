
DO $$
DECLARE
    column_type TEXT;
BEGIN
--    -- SIMRAN - 7 JAN 2025
--    IF EXISTS (
--        SELECT 1
--        FROM INFORMATION_SCHEMA.COLUMNS
--        WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'qualification_id'
--    ) THEN
--        ALTER TABLE custom_product DROP COLUMN qualification_id;
--    END IF;
--
--    IF EXISTS (
--        SELECT 1
--        FROM INFORMATION_SCHEMA.COLUMNS
--        WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'advertiser_url'
--    ) THEN
--        ALTER TABLE custom_product DROP COLUMN advertiser_url;
--    END IF;
--
--    IF EXISTS (
--        SELECT 1
--        FROM INFORMATION_SCHEMA.COLUMNS
--        WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'job_group_id'
--    ) THEN
--        ALTER TABLE custom_product DROP COLUMN job_group_id;
--    END IF;
--
--    IF EXISTS (
--        SELECT 1
--        FROM INFORMATION_SCHEMA.COLUMNS
--        WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'notifying_authority'
--    ) THEN
--        ALTER TABLE custom_product DROP COLUMN notifying_authority;
--    END IF;
--
--    IF EXISTS (
--        SELECT 1
--        FROM INFORMATION_SCHEMA.COLUMNS
--        WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'gender_specific_id'
--    ) THEN
--        ALTER TABLE custom_product DROP COLUMN gender_specific_id;
--    END IF;
--
--    IF EXISTS (
--        SELECT 1
--        FROM INFORMATION_SCHEMA.COLUMNS
--        WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'stream_id'
--    ) THEN
--        ALTER TABLE custom_product DROP COLUMN stream_id;
--    END IF;
--
--    IF EXISTS (
--        SELECT 1
--        FROM INFORMATION_SCHEMA.COLUMNS
--        WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'subject_id'
--    ) THEN
--        ALTER TABLE custom_product DROP COLUMN subject_id;
--    END IF;
--
--    -- SIMRAN - 9 JAN 2025
--    IF EXISTS (
--        SELECT 1
--        FROM INFORMATION_SCHEMA.TABLES
--        WHERE table_schema = 'public' AND table_name = 'custom_product'
--    ) THEN
--        IF EXISTS (
--            SELECT 1
--            FROM INFORMATION_SCHEMA.COLUMNS
--            WHERE table_schema = 'public' AND table_name = 'custom_product' AND column_name = 'selection_criteria'
--        ) THEN
--            ALTER TABLE public.custom_product
--            ALTER COLUMN selection_criteria TYPE TEXT;
--        END IF;
--    END IF;
--
--    -- KSHITIJ - 8 JAN 2025
--    IF EXISTS (
--        SELECT 1
--        FROM INFORMATION_SCHEMA.COLUMNS
--        WHERE TABLE_NAME = 'custom_service_provider_ticket' AND COLUMN_NAME = 'ticketid'
--    ) THEN
--        ALTER TABLE custom_service_provider_ticket DROP COLUMN ticketid;
--    END IF;
--
--    -- KSHITIJ - 10 JAN 2025
--    IF NOT EXISTS (
--        SELECT 1
--        FROM blc_country
--        WHERE abbreviation = 'ADD-C' AND name = 'Current Address'
--    ) THEN
--        INSERT INTO blc_country (abbreviation, name)
--        VALUES ('ADD-C', 'Current Address');
--    END IF;
--
--    IF NOT EXISTS (
--        SELECT 1
--        FROM blc_country
--        WHERE abbreviation = 'ADD-P' AND name = 'Permanent Address'
--    ) THEN
--        INSERT INTO blc_country (abbreviation, name)
--        VALUES ('ADD-P', 'Permanent Address');
--    END IF;
DO $$
BEGIN
    -- SIMRAN - 7 JAN 2025
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'qualification_id') THEN
        ALTER TABLE custom_product DROP COLUMN qualification_id;
    END IF;

    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'advertiser_url') THEN
        ALTER TABLE custom_product DROP COLUMN advertiser_url;
    END IF;

    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'job_group_id') THEN
        ALTER TABLE custom_product DROP COLUMN job_group_id;
    END IF;

    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'notifying_authority') THEN
        ALTER TABLE custom_product DROP COLUMN notifying_authority;
    END IF;

    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'gender_specific_id') THEN
        ALTER TABLE custom_product DROP COLUMN gender_specific_id;
    END IF;

    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'stream_id') THEN
        ALTER TABLE custom_product DROP COLUMN stream_id;
    END IF;

    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'subject_id') THEN
        ALTER TABLE custom_product DROP COLUMN subject_id;
    END IF;

IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'post_name')
BEGIN
    ALTER TABLE custom_product DROP COLUMN post_name;
END
-- SIMRAN -7 JAN 2024
-- KSHTIJ -8 JAN 2024
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_service_provider_ticket' AND COLUMN_NAME = 'ticketid')
BEGIN
    ALTER TABLE custom_service_provider_ticket DROP COLUMN ticketid;
END
-- KSHTIJ -8 JAN 2024

--SIMRAN -9 JAN 2025
    IF EXISTS (SELECT 1  FROM INFORMATION_SCHEMA.tables  WHERE table_schema = 'public' AND table_name = 'custom_product') THEN
        IF EXISTS (SELECT 1  FROM information_schema.columns  WHERE table_schema = 'public' AND table_name = 'custom_product'  AND column_name = 'selection_criteria' ) THEN
            ALTER TABLE public.custom_product
            ALTER COLUMN selection_criteria TYPE TEXT;
        END IF;
    END IF;
--SIMRAN -9 JAN 2025
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'post_name') THEN
        ALTER TABLE custom_product DROP COLUMN post_name;
    END IF;

    -- KSHITIJ - 8 JAN 2025
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_service_provider_ticket' AND COLUMN_NAME = 'ticketid') THEN
        ALTER TABLE custom_service_provider_ticket DROP COLUMN ticketid;
    END IF;
    -- KSHITIJ - 8 JAN 2025
    -- KSHITIJ - 10 JAN 2025
      IF NOT EXISTS (SELECT 1 FROM blc_country WHERE abbreviation = 'ADD-C' AND name = 'Current Address') THEN
            INSERT INTO blc_country (abbreviation, name)
            VALUES ('ADD-C', 'Current Address');
        END IF;

        -- Insert 'ADD-P' and 'Permanent Address' if they don't exist
        IF NOT EXISTS (SELECT 1 FROM blc_country WHERE abbreviation = 'ADD-P' AND name = 'Permanent Address') THEN
            INSERT INTO blc_country (abbreviation, name)
            VALUES ('ADD-P', 'Permanent Address');
        END IF;
       -- KSHITIJ - 10 JAN 2025
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'post_name') THEN
        ALTER TABLE custom_product DROP COLUMN post_name;
    END IF;

    -- KSHITIJ - 8 JAN 2025
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_service_provider_ticket' AND COLUMN_NAME = 'ticketid') THEN
        ALTER TABLE custom_service_provider_ticket DROP COLUMN ticketid;
    END IF;
    -- KSHITIJ - 8 JAN 2025

    -- RAMAN - 9 JAN 2025
--    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_customer' AND COLUMN_NAME = 'work_experience') THEN
--        ALTER TABLE custom_customer ALTER COLUMN work_experience TYPE INTEGER USING work_experience::INTEGER;
--    END IF;
--
--    ALTER TABLE custom_customer
--    ALTER COLUMN date_of_birth DROP DEFAULT;
--
--    -- Step 2: Alter the column type to TIMESTAMP
--    ALTER TABLE custom_customer
--    ALTER COLUMN date_of_birth
--    TYPE TIMESTAMP
--    USING CASE
--        WHEN date_of_birth IS NOT NULL THEN TO_TIMESTAMP(date_of_birth::TEXT, 'YYYY-MM-DD')
--        ELSE NULL
--    END;

    -- RAMAN - 9 JAN 2025
--END $$;
--
--    -- RAMAN - 9 JAN 2025

 IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'blc_product'
          AND column_name = 'meta_desc'
    ) THEN
        -- Alter the column if it exists
        ALTER TABLE public.blc_product
        ALTER COLUMN meta_desc TYPE TEXT;
    END IF;

    -- Convert height_cms if necessary
    SELECT data_type INTO column_type
    FROM information_schema.columns
    WHERE table_name = 'custom_customer' AND column_name = 'height_cms';
    IF column_type IS DISTINCT FROM 'double precision' THEN
        ALTER TABLE custom_customer ALTER COLUMN height_cms TYPE DOUBLE PRECISION USING height_cms::DOUBLE PRECISION;
    END IF;

    -- Convert weight_kgs if necessary
    SELECT data_type INTO column_type
    FROM information_schema.columns
    WHERE table_name = 'custom_customer' AND column_name = 'weight_kgs';
    IF column_type IS DISTINCT FROM 'double precision' THEN
        ALTER TABLE custom_customer ALTER COLUMN weight_kgs TYPE DOUBLE PRECISION USING weight_kgs::DOUBLE PRECISION;
    END IF;

    -- Convert chest_size_cms if necessary
    SELECT data_type INTO column_type
    FROM information_schema.columns
    WHERE table_name = 'custom_customer' AND column_name = 'chest_size_cms';
    IF column_type IS DISTINCT FROM 'double precision' THEN
        ALTER TABLE custom_customer ALTER COLUMN chest_size_cms TYPE DOUBLE PRECISION USING chest_size_cms::DOUBLE PRECISION;
    END IF;

    -- Convert shoe_size_inches if necessary
    SELECT data_type INTO column_type
    FROM information_schema.columns
    WHERE table_name = 'custom_customer' AND column_name = 'shoe_size_inches';
    IF column_type IS DISTINCT FROM 'double precision' THEN
        ALTER TABLE custom_customer ALTER COLUMN shoe_size_inches TYPE DOUBLE PRECISION USING shoe_size_inches::DOUBLE PRECISION;
    END IF;

    -- Convert waist_size_cms if necessary
    SELECT data_type INTO column_type
    FROM information_schema.columns
    WHERE table_name = 'custom_customer' AND column_name = 'waist_size_cms';
    IF column_type IS DISTINCT FROM 'double precision' THEN
        ALTER TABLE custom_customer ALTER COLUMN waist_size_cms TYPE DOUBLE PRECISION USING waist_size_cms::DOUBLE PRECISION;
    END IF;

    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'institution') THEN
                UPDATE institution
                SET institution_id=1, created_by='SUPER_ADMIN', created_date=NOW(), institution_location='Others', institution_code='Others', institution_name='Others', modified_by='SUPER_ADMIN', modified_date=NOW()
                WHERE institution_id=1;
            END IF;
       IF EXISTS (
             SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
             WHERE TABLE_NAME = 'subject_details'
             AND COLUMN_NAME = 'qualification_detail_id'
         ) THEN
             EXECUTE 'ALTER TABLE subject_details ALTER COLUMN qualification_detail_id DROP NOT NULL';
         END IF;
END $$;

