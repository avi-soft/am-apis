-- PROCEDURE: public.allocate_ticket_to_service_provider(bigint, bigint, bigint, bigint, bigint, integer, integer, bigint, text)

-- DROP PROCEDURE IF EXISTS public.allocate_ticket_to_service_provider(bigint, bigint, bigint, bigint, bigint, integer, integer, bigint, text);

CREATE OR REPLACE PROCEDURE public.allocate_ticket_to_service_provider(
	IN p_order_id bigint,
	IN p_assignee_user_id bigint,
	IN p_ticket_type_id bigint,
	IN p_ticket_state_id bigint,
	IN p_ticket_status_id bigint,
	IN p_assignee_role_id integer,
	IN p_creator_role_id integer,
	IN p_creator_user_id bigint,
	IN p_task_desc text,
	OUT p_ticket_id bigint)
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    -- Declare local variables to hold the fetched data from the service_provider table
    v_ticket_assigned INT;
    v_ticket_pending INT;
    v_max_ticket_size INT;
    v_is_active BOOLEAN;
    v_target_completion_time TIMESTAMP := CURRENT_TIMESTAMP + INTERVAL '4 hours';  -- Default to 4 hours in the future
BEGIN
    -- Fetch the service provider's details based on the assignee ID
    SELECT ticket_assigned, ticket_pending, maximum_ticket_size, is_active
    INTO v_ticket_assigned, v_ticket_pending, v_max_ticket_size, v_is_active
    FROM service_provider
    WHERE service_provider_id = p_assignee_user_id;

    -- Check if the service provider is active and the ticket assignment is within limits
    IF v_is_active THEN
        -- Check if the service provider can handle more tickets based on current assignments
        IF (v_ticket_assigned + v_ticket_pending) < v_max_ticket_size THEN
            -- Call the create_ticket procedure to assign a new ticket to the service provider
            CALL public.create_ticket(
                p_order_id := p_order_id,
                p_target_completion_time := v_target_completion_time,  -- Pass a future timestamp
                p_ticket_type_id := p_ticket_type_id,
                p_ticket_state_id := p_ticket_state_id,
                p_ticket_status_id := p_ticket_status_id,
                p_assignee_user_id := p_assignee_user_id,
                p_assignee_role_id := p_assignee_role_id,
                p_creator_role_id := p_creator_role_id,
                p_creator_user_id := p_creator_user_id,
                p_task_desc := p_task_desc,
                p_ticket_id := p_ticket_id  -- Pass p_ticket_id as an OUT parameter
            );

            -- After creating the ticket, update the order state to "Assigned" (Assuming the "Assigned" state is 2)
            UPDATE order_state
            SET order_state_id = 2
            WHERE order_id = p_order_id;

            -- Optionally log the ticket creation for debugging
            RAISE NOTICE 'Ticket successfully allocated to service provider % with ticket ID: %', p_assignee_user_id, p_ticket_id;

        ELSE
            -- If the service provider has exceeded the max ticket size, raise an exception
            RAISE NOTICE 'Service Provider ID % has exceeded maximum ticket size', p_assignee_user_id;
        END IF;
    ELSE
        -- If the service provider is not active, raise an exception
        RAISE NOTICE 'Service Provider ID % is not active and cannot be assigned tickets', p_assignee_user_id;
    END IF;
END;
$BODY$;
ALTER PROCEDURE public.allocate_ticket_to_service_provider(bigint, bigint, bigint, bigint, bigint, integer, integer, bigint, text)
    OWNER TO postgres;
