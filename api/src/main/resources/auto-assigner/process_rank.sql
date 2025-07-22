-- PROCEDURE: public.process_rank(bigint[], bigint, bigint[])

-- DROP PROCEDURE IF EXISTS public.process_rank(bigint[], bigint, bigint[]);

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
    v_assignee_role_id INT := 4; -- Assuming role 4 is for service providers
    i INT;
    available_service_provider_ids bigint[] := '{}'; -- Array to store available service providers
	unavailable_service_provider_ids bigint[] := '{}';
    original_length INT; -- Variable to store the original length of input array
BEGIN
    -- Store the original length of input service provider IDs array
    original_length := array_length(p_service_provider_ids, 1);

    -- Reversing the array and storing it in the variable
    SELECT array(
        SELECT p_service_provider_ids[j]
        FROM generate_series(array_length(p_service_provider_ids, 1), 1, -1) AS j
    ) INTO reversed_service_provider_ids;

    -- Loop through the array of service provider IDs in forward order
    FOR i IN 1..array_length(reversed_service_provider_ids, 1) LOOP
        v_service_provider_id := reversed_service_provider_ids[i];

        -- Fetch service provider details
        SELECT maximum_ticket_size, ticket_pending, ticket_assigned
        INTO v_maximum_ticket_size, v_ticket_pending, v_ticket_assigned
        FROM service_provider
        WHERE service_provider_id = v_service_provider_id;

        -- Calculate bandwidth
        IF v_maximum_ticket_size IS NOT NULL THEN
            v_bandwidth := (v_ticket_assigned + v_ticket_pending)::DOUBLE PRECISION / v_maximum_ticket_size * 100;
        ELSE
            -- If maximum_ticket_size is null, use a default value (e.g., from ranking)
            v_bandwidth := (v_ticket_assigned + v_ticket_pending)::DOUBLE PRECISION / 100 * 100; -- Replace 100 with the default value
        END IF;

        -- Check if bandwidth is within limits
        IF v_bandwidth >= 100.0 THEN
            RAISE NOTICE 'Service Provider limit exceeded for the day - serviceProvider details: %', v_service_provider_id;
            -- Skip the service provider with exceeded bandwidth
            unavailable_service_provider_ids := array_append(unavailable_service_provider_ids, v_service_provider_id);
            CONTINUE;
        ELSE
            -- If bandwidth is within limits, add this provider to the available list
            available_service_provider_ids := array_append(available_service_provider_ids, v_service_provider_id);
        END IF;
    END LOOP;

    -- If there are available service providers
    IF array_length(available_service_provider_ids, 1) > 0 THEN
        -- Proceed with ticket creation for the first available service provider
        v_service_provider_id := available_service_provider_ids[1];

        CALL public.create_ticket(
            p_order_id::bigint,  -- Pass p_order_id
            (CURRENT_TIMESTAMP + INTERVAL '4 hours')::timestamp without time zone,  -- Cast to timestamp without time zone
            1::bigint,  -- Explicit cast to bigint
            1::bigint,  -- Explicit cast to bigint
            0::bigint,  -- Explicit cast to bigint
            388::bigint,  -- Explicit cast to bigint
            4::integer,  -- Explicit cast to integer
            1::integer,  -- Explicit cast to integer
            1::bigint,  -- Explicit cast to bigint
            'ghyhy'::text,  -- Explicit cast to text
            v_ticket_id  -- Pass the OUT parameter for ticket ID
        );

        -- Update order state to assigned
        UPDATE order_state
        SET order_state_id = 4 -- Assuming 4 is the assigned state
        WHERE order_id = p_order_id;

        -- Increment ticket_assigned for the service provider
        UPDATE service_provider
        SET ticket_assigned = ticket_assigned + 1
        WHERE service_provider_id = v_service_provider_id;

        -- Log success and exit
        RAISE NOTICE 'Order with id: % is assigned to Service Provider with id: % with ticket id: %', p_order_id, v_service_provider_id, v_ticket_id;
        v_assigned_tickets :=array_append(v_assigned_tickets, v_ticket_id);
        -- Output the final list of available service providers
        RAISE NOTICE 'Final list of available service providers: %', available_service_provider_ids;
    ELSE
        -- If no service provider is available
        RAISE NOTICE 'No available service provider for order id: %', p_order_id;
    END IF;

    -- Return true if the size of the available service provider list is the same as the original input array
    IF array_length(unavailable_service_provider_ids, 1) = array_length(p_service_provider_ids, 1) THEN
        v_result := false;
    ELSE
        v_result := true;
    END IF;

END;
$BODY$;
ALTER PROCEDURE public.process_rank(bigint[], bigint, bigint[])
    OWNER TO postgres;
