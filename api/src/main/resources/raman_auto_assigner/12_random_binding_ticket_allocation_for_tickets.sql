-- PROCEDURE: public.random_binding_ticket_allocation_for_tickets(bigint[], bigint[])

-- DROP PROCEDURE IF EXISTS public.random_binding_ticket_allocation_for_tickets(bigint[], bigint[]);

CREATE OR REPLACE PROCEDURE public.random_binding_ticket_allocation_for_tickets(
	INOUT ticket_ids bigint[],
	INOUT assigned_ticket_ids bigint[])
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    v_ticket_id BIGINT;
    v_order_id BIGINT;
    v_customer_id BIGINT;
    v_ticket_type_id BIGINT;
    v_parent_ticket_id BIGINT;
    v_assignee BIGINT;
    ref RECORD;
    v_product_id BIGINT;
    v_creator_user_id BIGINT;
    v_rejected_by BIGINT[];
    v_assigned BOOLEAN := FALSE;
    v_unassigned_ticket_ids BIGINT[];
    v_sp_id BIGINT;
BEGIN

    RAISE NOTICE '12. Starting Random Binding Ticket Allocation (RBTA) for Tickets';

    FOREACH v_ticket_id IN ARRAY ticket_ids LOOP
        v_assigned := FALSE;

        -- Get basic ticket info (excluding rejected_by)
		SELECT ticket_type_id, order_id, parent_ticket_id, assignee_user_id
		INTO v_ticket_type_id, v_order_id, v_parent_ticket_id, v_assignee
		FROM custom_service_provider_ticket
		WHERE ticket_id = v_ticket_id;

		-- Get rejected_by from separate table
		SELECT array_agg(rejected_by_id)
		INTO v_rejected_by
		FROM ticket_rejected_by
		WHERE ticket_id = v_ticket_id;

        -- Determine correct order and customer
        IF v_ticket_type_id = 1 THEN  -- Primary Ticket
            SELECT customer_id INTO v_customer_id FROM blc_order WHERE order_id = v_order_id;
        ELSIF v_ticket_type_id = 2 THEN  -- Review Ticket
            SELECT o.customer_id
            INTO v_customer_id
            FROM custom_service_provider_ticket t
            JOIN blc_order o ON o.order_id = t.order_id
            WHERE t.ticket_id = v_parent_ticket_id;
        ELSE
            RAISE NOTICE 'Invalid ticket type for ticket %', v_ticket_id;
            CONTINUE;
        END IF;

        -- Primary Referrer Assignment
        FOR ref IN
            SELECT r.service_provider_id, r.id
            FROM customer_referrer r
            JOIN service_provider sp ON sp.service_provider_id = r.service_provider_id
            WHERE r.customer_id = v_customer_id AND r.primary_ref = true AND sp.is_active = true AND sp.approved = true AND sp.role IN (2,4)
        LOOP
            IF v_rejected_by IS NULL OR NOT ref.service_provider_id = ANY(v_rejected_by) THEN
                IF v_ticket_type_id = 2 AND v_assignee = ref.id THEN
                    CONTINUE;
                END IF;

                CALL reallocate_ticket(
                    p_order_id           => v_order_id,
                    p_service_provider_id => ref.service_provider_id,
                    p_ticket_id          => v_ticket_id,
                    p_ticket_type_id     => v_ticket_type_id,
                    p_customer_id        => v_customer_id,
                    p_is_review_ticket   => (v_ticket_type_id = 2),
                    p_is_primary_ticket  => (v_ticket_type_id = 1),
                    assigned             => v_assigned,
                    assigned_ticket_ids  => assigned_ticket_ids
                );
                IF v_assigned THEN EXIT; END IF;
            END IF;
        END LOOP;

        IF v_assigned THEN CONTINUE; END IF;

        -- Secondary Referrers
        FOR ref IN
            SELECT r.service_provider_id, r.id
            FROM customer_referrer r
            JOIN service_provider sp ON sp.service_provider_id = r.service_provider_id
            WHERE r.customer_id = v_customer_id AND sp.is_active = true AND sp.approved = true AND sp.role IN (2,4)
        LOOP
            IF v_rejected_by IS NULL OR NOT ref.service_provider_id = ANY(v_rejected_by) THEN
                IF v_ticket_type_id = 2 AND v_assignee = ref.id THEN
                    CONTINUE;
                END IF;

                CALL reallocate_ticket(
                    p_order_id           => v_order_id,
                    p_service_provider_id => ref.service_provider_id,
                    p_ticket_id          => v_ticket_id,
                    p_ticket_type_id     => v_ticket_type_id,
                    p_customer_id        => v_customer_id,
                    p_is_review_ticket   => (v_ticket_type_id = 2),
                    p_is_primary_ticket  => (v_ticket_type_id = 1),
                    assigned             => v_assigned,
                    assigned_ticket_ids  => assigned_ticket_ids
                );
                IF v_assigned THEN EXIT; END IF;
            END IF;
        END LOOP;

        IF v_assigned THEN CONTINUE; END IF;

        -- Fallback: Assign to creator of the product
        SELECT (value)::BIGINT INTO v_product_id
        FROM blc_order_item_attribute
        WHERE order_item_id = (
            SELECT order_item_id FROM blc_order_item WHERE order_id = v_order_id LIMIT 1
        );

        SELECT creator_user_id INTO v_creator_user_id
        FROM custom_product
        WHERE product_id = v_product_id;

        SELECT service_provider_id
		INTO v_sp_id
		FROM service_provider
		WHERE service_provider_id = v_creator_user_id
		  AND role IN (2, 4)
		  AND is_active = TRUE
		  AND approved = TRUE;

	    IF v_sp_id IS NULL THEN
		    CONTINUE;
		END IF;
        IF v_rejected_by IS NULL OR NOT v_creator_user_id = ANY(v_rejected_by) THEN
            IF v_ticket_type_id = 2 AND v_assignee = v_creator_user_id THEN
                CONTINUE;
            END IF;

            CALL reallocate_ticket(
                p_order_id           => v_order_id,
                p_service_provider_id => v_creator_user_id,
                p_ticket_id          => v_ticket_id,
                p_ticket_type_id     => v_ticket_type_id,
                p_customer_id        => v_customer_id,
                p_is_review_ticket   => (v_ticket_type_id = 2),
                p_is_primary_ticket  => (v_ticket_type_id = 1),
                assigned             => v_assigned,
                assigned_ticket_ids  => assigned_ticket_ids
            );
        END IF;

        IF NOT v_assigned THEN
            v_unassigned_ticket_ids := array_append(v_unassigned_ticket_ids, v_ticket_id);
        END IF;
    END LOOP;

    ticket_ids := v_unassigned_ticket_ids;

    RAISE NOTICE 'RBTA Completed. Assigned: %, Unassigned: %',
        array_length(assigned_ticket_ids, 1),
        array_length(ticket_ids, 1);
END;
$BODY$;
