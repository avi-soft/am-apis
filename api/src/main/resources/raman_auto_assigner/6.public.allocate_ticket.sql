CREATE OR REPLACE PROCEDURE public.allocate_ticket(
    IN p_order_id BIGINT,
    IN p_service_provider_id BIGINT,
    IN p_custom_order_state_id BIGINT,
    IN p_customer_id BIGINT,
    INOUT p_assigned BOOLEAN,
    INOUT p_assigned_ticket_ids BIGINT[] -- Optional: to track assigned ticket IDs
)
LANGUAGE plpgsql
AS $$
DECLARE
    max_ticket_size INT;
    current_assigned INT;
    current_pending INT;
    ticket_id BIGINT;
    is_active BOOLEAN;
BEGIN
    -- Fetch service provider details
    SELECT sp.maximum_ticket_size, sp.ticket_assigned, sp.ticket_pending, sp.is_active
    INTO max_ticket_size, current_assigned, current_pending, is_active
    FROM service_provider_entity sp
    WHERE sp.service_provider_id = p_service_provider_id;

    IF is_active AND (
        (max_ticket_size IS NOT NULL AND current_assigned + current_pending < max_ticket_size)
        OR (current_assigned + current_pending < (
            SELECT r.maximum_ticket_size
            FROM service_provider_entity sp
            JOIN ranking r ON sp.ranking_id = r.ranking_id
            WHERE sp.service_provider_id = p_service_provider_id
        ))
    ) THEN
        -- Call create_ticket procedure
        CALL create_ticket(
            1, -- ticketState
            1, -- ticketType
            0, -- ticketStatus
            p_service_provider_id,
            4, -- assigneeRole
            p_order_id,
            ticket_id -- OUT
        );

        -- Update the order state to assigned
        UPDATE custom_order_state
        SET order_state_id = 2 -- Assuming 2 is ASSIGNED
        WHERE id = p_custom_order_state_id;

        -- Increment assigned ticket count
        CALL increment_ticket_assigned(p_service_provider_id);

        -- Append the ticket_id to output list
        p_assigned_ticket_ids := array_append(p_assigned_ticket_ids, ticket_id);
        p_assigned := TRUE;

    ELSE
        RAISE NOTICE 'Service provider capacity full or not active: %', p_service_provider_id;
        p_assigned := FALSE;
    END IF;
END;
$$;
