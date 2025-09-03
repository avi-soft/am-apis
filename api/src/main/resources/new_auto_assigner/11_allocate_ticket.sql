CREATE OR REPLACE PROCEDURE public.allocate_ticket(IN p_order_id bigint, IN p_service_provider_id bigint, INOUT p_assigned boolean, INOUT p_assigned_ticket_ids bigint[])
LANGUAGE 'plpgsql'
AS $BODY$

DECLARE
    max_ticket_size INT;
    current_assigned INT;
    current_pending INT;
    ticket_id BIGINT;
    is_active BOOLEAN;
BEGIN

	RAISE NOTICE '11. Allocate ticket';

    -- Fetch service provider details
    SELECT sp.maximum_ticket_size, sp.ticket_assigned, sp.ticket_pending, sp.is_active
    INTO max_ticket_size, current_assigned, current_pending, is_active
    FROM service_provider sp
    WHERE sp.service_provider_id = p_service_provider_id;

    IF is_active AND (
        (max_ticket_size IS NOT NULL AND current_assigned + current_pending < max_ticket_size)
        OR (current_assigned + current_pending < (
            SELECT r.maximum_ticket_size
                FROM service_provider_rank r
                WHERE r.rank_id = (
                    SELECT m.rank_id
                    FROM service_provider_rank_mapping m
                    WHERE m.service_provider_id = p_service_provider_id
                )
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
        UPDATE order_state
        SET
            order_state_id = 2,
            modified_date = NOW()
        WHERE
            order_id = p_order_id;

        -- Initialize assigned_tickets array if null
                IF p_assigned_ticket_ids IS NULL THEN
                    p_assigned_ticket_ids := ARRAY[]::BIGINT[];
                END IF;

        -- Append the ticket_id to output list
        p_assigned_ticket_ids := array_append(p_assigned_ticket_ids, ticket_id);
        p_assigned := TRUE;

    ELSE
        RAISE NOTICE 'Service provider capacity full or not active: %', p_service_provider_id;
        p_assigned := FALSE;
    END IF;
END;
$BODY$;