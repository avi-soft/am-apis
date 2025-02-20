-- PROCEDURE: public.create_ticket(bigint, timestamp without time zone, bigint, bigint, bigint, bigint, integer, integer, bigint, text)

-- DROP PROCEDURE IF EXISTS public.create_ticket(bigint, timestamp without time zone, bigint, bigint, bigint, bigint, integer, integer, bigint, text);

CREATE OR REPLACE PROCEDURE public.create_ticket(
	IN p_order_id bigint,
	IN p_target_completion_time timestamp without time zone,
	IN p_ticket_type_id bigint,
	IN p_ticket_state_id bigint,
	IN p_ticket_status_id bigint,
	IN p_assignee_user_id bigint,
	IN p_assignee_role_id integer,
	IN p_creator_role_id integer,
	IN p_creator_user_id bigint,
	IN p_task_desc text,
	OUT p_ticket_id bigint)
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    v_created_date TIMESTAMP := CURRENT_TIMESTAMP;
    v_modified_date TIMESTAMP := CURRENT_TIMESTAMP;
    v_target_completion_time TIMESTAMP;
BEGIN
    -- Validation for target completion time (must be in the future)
    IF p_target_completion_time IS NULL OR p_target_completion_time <= v_created_date THEN
        RAISE EXCEPTION 'TARGET COMPLETION TIME MUST BE IN THE FUTURE';
    END IF;

    -- Set target completion time (add 4 hours if not provided)
    IF p_target_completion_time IS NULL THEN
        v_target_completion_time := v_created_date + INTERVAL '4 hours';
    ELSE
        v_target_completion_time := p_target_completion_time;
    END IF;

    -- Insert the ticket into the custom_service_provider_ticket table
    INSERT INTO custom_service_provider_ticket (
        ticket_assign_time,
        target_completion_time,
        created_date,
        modified_date,
        order_id,
        creator_role_id,
        creator_user_id,
        assignee_role_id,
        assignee_user_id,
        ticket_state_id,
        ticket_type_id,
        ticket_status_id,
        task_desc
    )
    VALUES (
        v_created_date,  -- ticket_assign_time
        v_target_completion_time,  -- target_completion_time
        v_created_date,  -- created_date
        v_modified_date,  -- modified_date
        p_order_id,  -- order_id
        p_creator_role_id,  -- creator_role_id
        p_creator_user_id,  -- creator_user_id
        p_assignee_role_id,  -- assignee_role_id
        p_assignee_user_id,  -- assignee_user_id
        p_ticket_state_id,  -- ticket_state_id
        p_ticket_type_id,  -- ticket_type_id
        p_ticket_status_id,  -- ticket_status_id
        p_task_desc  -- task_desc
    )
    RETURNING ticket_id INTO p_ticket_id;  -- Set the OUT parameter to the ticket_id

    -- Update assignee's ticket count if the assignee role is 4 (assuming role 4 requires updating)
    IF p_assignee_role_id = 4 THEN
        UPDATE service_provider
        SET ticket_assigned = ticket_assigned + 1
        WHERE service_provider_id = p_assignee_user_id;
    END IF;

    -- Return the ticket ID as the OUT parameter
    -- No need for a RAISE NOTICE here as it is now an OUT parameter

END;
$BODY$;
ALTER PROCEDURE public.create_ticket(bigint, timestamp without time zone, bigint, bigint, bigint, bigint, integer, integer, bigint, text)
    OWNER TO postgres;
