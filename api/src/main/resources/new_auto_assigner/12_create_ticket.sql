CREATE OR REPLACE PROCEDURE public.create_ticket(IN p_ticket_state bigint, IN p_ticket_type bigint, IN p_ticket_status bigint, IN p_assignee bigint, IN p_assignee_role integer, IN p_order_id bigint, OUT v_ticket_id bigint, IN p_task text DEFAULT NULL::text, IN p_target_completion timestamp without time zone DEFAULT  NULL::timestamp without time zone)
LANGUAGE 'plpgsql'  
AS $BODY$
DECLARE
    v_created_date TIMESTAMP := now();
    v_target_date TIMESTAMP;
BEGIN

	RAISE NOTICE '12. create ticket';

    -- Validate Target Completion Date
    IF p_target_completion IS NOT NULL THEN
        IF p_target_completion <= v_created_date THEN
            RAISE EXCEPTION 'TARGET COMPLETION DATE MUST BE IN THE FUTURE';
        END IF;
        v_target_date := p_target_completion;
    ELSE
        v_target_date := v_created_date + interval '4 hours';
    END IF;

    -- Insert ticket
    INSERT INTO custom_service_provider_ticket (
        created_date,
        ticket_assign_time,
        modified_date,
        target_completion_time,
        order_id,
        assignee_user_id,
        assignee_role_id,
        ticket_state_id,
        ticket_type_id,
        ticket_status_id,
        task_desc
    )
    VALUES (
        v_created_date,
        v_created_date,
        v_created_date,
        v_target_date,
        p_order_id,
        p_assignee,
        p_assignee_role,
        p_ticket_state,
        p_ticket_type,
        COALESCE(p_ticket_status, 0),
        p_task
    )
    RETURNING ticket_id INTO v_ticket_id;

    -- Increment ticket count if assignee is a service provider
    IF p_assignee_role = 4 OR p_assignee_role = 2 THEN
        UPDATE service_provider
        SET ticket_assigned = ticket_assigned + 1
        WHERE service_provider_id = p_assignee;
    END IF;

    INSERT INTO public.email_queue (
        archived,
        created_date,
        user_id,
        role_id,
        ticket_id
    ) VALUES (
        false,
        NOW(),
        p_assignee,
        4,
        v_ticket_id
    );

    RAISE NOTICE 'Ticket Created with ID: %', v_ticket_id;

END;
$BODY$;