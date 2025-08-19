CREATE OR REPLACE PROCEDURE public.process_rank(
	IN p_service_provider_ids bigint[],
	IN p_order_id bigint,
	INOUT v_assigned_tickets bigint[],
	OUT v_result boolean)
LANGUAGE 'plpgsql'
AS $BODY$

DECLARE
    reversed_service_provider_ids bigint[];
    v_service_provider_id BIGINT;
    v_maximum_ticket_size INT;
    v_ticket_pending INT;
    v_ticket_assigned INT;
    v_bandwidth DOUBLE PRECISION;
    v_ticket_id BIGINT;
    i INT;
    available_service_provider_ids bigint[] := '{}';
    unavailable_service_provider_ids bigint[] := '{}';
BEGIN

	RAISE NOTICE '14. Process Rank';

    -- Reverse the service provider IDs array
    SELECT array(
        SELECT p_service_provider_ids[j]
        FROM generate_series(array_length(p_service_provider_ids, 1), 1, -1) AS j
    ) INTO reversed_service_provider_ids;

    -- Loop through reversed service providers
    FOR i IN 1..array_length(reversed_service_provider_ids, 1) LOOP
        v_service_provider_id := reversed_service_provider_ids[i];

--        SELECT maximum_ticket_size, ticket_pending, ticket_assigned
--        INTO v_maximum_ticket_size, v_ticket_pending, v_ticket_assigned
--        FROM service_provider
--        WHERE service_provider_id = v_service_provider_id;

        SELECT COALESCE(sp.maximum_ticket_size, spr.maximum_ticket_size),
                       sp.ticket_assigned,
                       sp.ticket_pending
        INTO v_maximum_ticket_size, v_ticket_assigned, v_ticket_pending
        FROM service_provider sp
        LEFT JOIN service_provider_rank_mapping m ON sp.service_provider_id = v_service_provider_id
        LEFT JOIN service_provider_rank spr ON m.rank_id = spr.rank_id
        WHERE sp.service_provider_id = v_service_provider_id;

        IF v_maximum_ticket_size IS NOT NULL THEN
            v_bandwidth := (v_ticket_assigned + v_ticket_pending)::DOUBLE PRECISION / v_maximum_ticket_size * 100;
        ELSE
            v_bandwidth := (v_ticket_assigned + v_ticket_pending)::DOUBLE PRECISION / 100 * 100;
        END IF;

        IF v_bandwidth >= 100 THEN
            RAISE NOTICE 'Service Provider limit exceeded: %', v_service_provider_id;
            unavailable_service_provider_ids := array_append(unavailable_service_provider_ids, v_service_provider_id);
            CONTINUE;
        ELSE
            available_service_provider_ids := array_append(available_service_provider_ids, v_service_provider_id);
        END IF;
    END LOOP;

    IF array_length(available_service_provider_ids, 1) > 0 THEN
        v_service_provider_id := available_service_provider_ids[1];

        CALL public.create_ticket(
            1,               -- p_ticket_state (example: 1)
            1,               -- p_ticket_type (example: 1)
            1,               -- p_ticket_status (example: 1)
            v_service_provider_id, -- p_assignee
            4,               -- p_assignee_role (4 for service provider)
            p_order_id,      -- p_order_id
            v_ticket_id      -- OUT v_ticket_id
        );

        -- Update order_state to assigned (assuming order_state_id = 4 means assigned)
        UPDATE order_state
        SET
            order_state_id = 2,
            modified_date = NOW()
        WHERE
            order_id = p_order_id;

        RAISE NOTICE 'Order % assigned to service provider % with ticket %', p_order_id, v_service_provider_id, v_ticket_id;

        v_assigned_tickets := array_append(v_assigned_tickets, v_ticket_id);

    ELSE
        RAISE NOTICE 'No available service provider for order %', p_order_id;
    END IF;

    -- If all providers unavailable, return false, else true
    IF array_length(unavailable_service_provider_ids, 1) = array_length(p_service_provider_ids, 1) THEN
        v_result := false;
    ELSE
        v_result := true;
    END IF;
END;
$BODY$;
