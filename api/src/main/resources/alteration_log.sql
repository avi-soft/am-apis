DO $$
BEGIN
-- SIMRAN -7 JAN 2024
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'qualification_id')
BEGIN
    ALTER TABLE custom_product DROP COLUMN qualification_id;
END

IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'advertiser_url')
BEGIN
    ALTER TABLE custom_product DROP COLUMN advertiser_url;
END

IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'job_group_id')
BEGIN
    ALTER TABLE custom_product DROP COLUMN job_group_id;
END

IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'notifying_authority')
BEGIN
    ALTER TABLE custom_product DROP COLUMN notifying_authority;
END

IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'gender_specific_id')
BEGIN
    ALTER TABLE custom_product DROP COLUMN gender_specific_id;
END

IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'stream_id')
BEGIN
    ALTER TABLE custom_product DROP COLUMN stream_id;
END

IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_product' AND COLUMN_NAME = 'subject_id')
BEGIN
    ALTER TABLE custom_product DROP COLUMN subject_id;
END

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
END $$;