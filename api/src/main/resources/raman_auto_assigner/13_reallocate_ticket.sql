-- PROCEDURE: public.reallocate_ticket(bigint, bigint, bigint, bigint, bigint, boolean, boolean, boolean, bigint[])

-- DROP PROCEDURE IF EXISTS public.reallocate_ticket(bigint, bigint, bigint, bigint, bigint, boolean, boolean, boolean, bigint[]);

CREATE OR REPLACE PROCEDURE public.reallocate_ticket(
	IN p_order_id bigint,
	IN p_service_provider_id bigint,
	IN p_ticket_id bigint,
	IN p_ticket_type_id bigint,
	IN p_customer_id bigint,
	IN p_is_review_ticket boolean,
	IN p_is_primary_ticket boolean,
	INOUT assigned boolean,
	INOUT assigned_ticket_ids bigint[])
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    v_max_tickets INT;
    v_current_total INT;
    v_is_active BOOLEAN;
    v_now TIMESTAMP := CURRENT_TIMESTAMP;
    v_target_completion_date TIMESTAMP;
    v_product_active_end_date TIMESTAMP;
BEGIN

	RAISE NOTICE '13. Allocate Ticket';

    -- Fetch active status and limits
    SELECT is_active, ticket_assigned + ticket_pending,
           COALESCE(maximum_ticket_size, ranking_maximum_ticket_size)
    INTO v_is_active, v_current_total, v_max_tickets
    FROM (
        SELECT sp.is_active, sp.ticket_assigned, sp.ticket_pending,
               sp.maximum_ticket_size, r.maximum_ticket_size AS ranking_maximum_ticket_size
        FROM service_provider sp
        LEFT JOIN service_provider_rank r ON r.rank_id = sp.rank_id
        WHERE sp.service_provider_id = p_service_provider_id
    ) AS sub;

--	RAISE 'v_is_active % and v_current_total % and v_max_tickets %', v_is_active, v_current_total, v_max_tickets;
    IF v_is_active AND v_current_total < v_max_tickets THEN

        -- Assign ticket state and status
        UPDATE custom_service_provider_ticket
        SET ticket_state_id = 1,  -- TO-DO
            ticket_status_id = 0, -- Initial status
            assignee_user_id = p_service_provider_id,
            assignee_role_id = 4,
            modified_date = v_now,
            ticket_assign_time = v_now,
            target_completion_time = CASE
                WHEN p_is_review_ticket THEN v_now + INTERVAL '2 hours'
                WHEN p_is_primary_ticket THEN v_now + INTERVAL '4 hours'
                ELSE NULL
            END
        WHERE ticket_id = p_ticket_id;

        IF p_is_review_ticket THEN
            SELECT (value)::BIGINT
            INTO v_product_active_end_date
            FROM blc_order_item_attribute
            WHERE order_item_id = (
                SELECT order_item_id FROM blc_order_item WHERE order_id = p_order_id LIMIT 1
            );

            SELECT active_end_date INTO v_product_active_end_date
            FROM custom_product
            WHERE product_id = v_product_active_end_date;

            IF v_target_completion_date >= v_product_active_end_date THEN
                RAISE NOTICE 'Cannot assign: target completion date >= product end date';
                RETURN;
            END IF;
        END IF;

        -- Update order state
        UPDATE order_state
        SET order_state_id = 2 -- assuming 1002 = ASSIGNED
        WHERE order_id = p_order_id;

        -- Increment SP ticket count
        UPDATE service_provider
        SET ticket_assigned = ticket_assigned + 1
        WHERE service_provider_id = p_service_provider_id;

        -- Append ticket to result
        assigned_ticket_ids := array_append(assigned_ticket_ids, p_ticket_id);
        assigned := TRUE;
    ELSE
        RAISE NOTICE 'Service Provider inactive or ticket limit exceeded %', p_service_provider_id;
    END IF;
END;
$BODY$;
ALTER PROCEDURE public.reallocate_ticket(bigint, bigint, bigint, bigint, bigint, boolean, boolean, boolean, bigint[])
    OWNER TO postgres;
