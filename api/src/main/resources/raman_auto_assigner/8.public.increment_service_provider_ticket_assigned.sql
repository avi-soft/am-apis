CREATE OR REPLACE PROCEDURE increment_service_provider_ticket_assigned(
    IN v_service_provider_id BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE service_provider
    SET ticket_assigned = COALESCE(ticket_assigned, 0) + 1
    WHERE service_provider_id = v_service_provider_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Service provider with ID % not found.', p_service_provider_id;
    END IF;
END;
$$;
