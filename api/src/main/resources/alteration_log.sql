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

    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'post_name') THEN
        ALTER TABLE custom_product DROP COLUMN post_name;
    END IF;

    -- KSHITIJ - 8 JAN 2025
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_service_provider_ticket' AND COLUMN_NAME = 'ticketid') THEN
        ALTER TABLE custom_service_provider_ticket DROP COLUMN ticketid;
    END IF;
    -- KSHITIJ - 8 JAN 2025

    -- RAMAN - 9 JAN 2025
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_customer' AND COLUMN_NAME = 'work_experience') THEN
        ALTER TABLE custom_customer ALTER COLUMN work_experience TYPE INTEGER USING work_experience::INTEGER;
    END IF;

    -- Check if the column exists
        IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_customer' AND COLUMN_NAME = 'date_of_birth') THEN
            -- Alter the column data type to TIMESTAMP
            ALTER TABLE custom_customer
            ALTER COLUMN date_of_birth
            TYPE TIMESTAMP
        END IF;
    -- RAMAN - 9 JAN 2025
END $$;