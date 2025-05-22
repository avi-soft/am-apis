CREATE OR REPLACE PROCEDURE create_ticket(
    IN p_ticket_state BIGINT,
    IN p_ticket_type BIGINT,
    IN p_ticket_status BIGINT,
    IN p_assignee BIGINT,
    IN p_assignee_role INTEGER,
    IN p_creator_role_id INTEGER,
    IN p_creator_id BIGINT,
    IN p_order_id BIGINT,
    IN p_task TEXT DEFAULT NULL,
    IN p_target_completion TIMESTAMP DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_created_date TIMESTAMP := now();
    v_target_date TIMESTAMP;
    v_ticket_id BIGINT;
BEGIN
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
        ticket_assign_date,
        modified_date,
        target_completion_date,
        order_id,
        assignee,
        assignee_role,
        ticket_state,
        ticket_type,
        ticket_status,
        "desc",
        user_id,
        creator_role
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
        p_task,
        p_creator_id,
        p_creator_role_id
    )
    RETURNING id INTO v_ticket_id;

    -- Increment ticket count if assignee is a service provider
    IF p_assignee_role = 4 THEN
        UPDATE service_provider
        SET ticket_assigned = ticket_assigned + 1
        WHERE service_provider_id = p_assignee;
    END IF;

    RAISE NOTICE 'Ticket Created with ID: %', v_ticket_id;

END;
$$;
