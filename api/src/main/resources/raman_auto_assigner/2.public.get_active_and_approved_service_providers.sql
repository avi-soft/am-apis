CREATE OR REPLACE PROCEDURE public.get_active_and_approved_service_providers(
    OUT available_sp_ids BIGINT[]
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_test_status_id CONSTANT BIGINT := 3;
    found_status INT;
BEGIN
    -- Check if test status exists
    SELECT COUNT(*) INTO found_status
    FROM service_provider
    WHERE test_status_id = v_test_status_id;

    IF found_status = 0 THEN
        RAISE EXCEPTION 'No Test Status is found with this id: %', test_status_id;
    END IF;

    -- Get list of approved and active service providers with role = 4
    SELECT ARRAY_AGG(service_provider_id)
    INTO available_sp_ids
    FROM service_provider
    WHERE test_status_id = v_test_status_id
      AND is_active = TRUE
      AND approved = TRUE
      AND role = 4;

    IF available_sp_ids IS NULL OR array_length(available_sp_ids, 1) = 0 THEN
        RAISE EXCEPTION 'No Active and Approved Service Providers found with Test Status = %', test_status_id;
    END IF;

    RAISE NOTICE 'Found % available service providers.', array_length(available_sp_ids, 1);
END;
$$;

ALTER PROCEDURE public.get_active_and_approved_service_providers(OUT available_sp_ids bigint[])
    OWNER TO postgres;
