CREATE OR REPLACE PROCEDURE public.vertical_distribution_ticket_allocation_for_tickets(INOUT ticket_ids bigint[], IN service_provider_ids bigint[], INOUT assigned_ticket_ids bigint[])
LANGUAGE 'plpgsql'
AS $BODY$

DECLARE
    v_ticket_id BIGINT;
    v_order_id BIGINT;
    v_parent_ticket_id BIGINT;
    v_ticket_type_id BIGINT;
    v_customer_id BIGINT;
    v_assigned BOOLEAN := FALSE;
    v_remaining_ticket_ids BIGINT[];

    -- Ranked queues
    rank1a BIGINT[] := ARRAY[]::BIGINT[];
    rank1b BIGINT[] := ARRAY[]::BIGINT[];
    rank1c BIGINT[] := ARRAY[]::BIGINT[];
    rank1d BIGINT[] := ARRAY[]::BIGINT[];
    rank2a BIGINT[] := ARRAY[]::BIGINT[];
    rank2b BIGINT[] := ARRAY[]::BIGINT[];
    rank2c BIGINT[] := ARRAY[]::BIGINT[];
    rank2d BIGINT[] := ARRAY[]::BIGINT[];
BEGIN
	
	RAISE NOTICE '6. Vertical Distribution Ticket Allocation for Tickets';

    RAISE NOTICE 'Vertical Distribution Ticket Allocation for Tickets Started';
    RAISE NOTICE 'Total Tickets: %, Total Providers: %', array_length(ticket_ids, 1), array_length(service_provider_ids, 1);

    -- Step 1: Bifurcate available service providers into rank arrays
    CALL bifurcate_available_service_providers(
        service_provider_ids,
        rank1a, rank1b, rank1c, rank1d,
        rank2a, rank2b, rank2c, rank2d
    );

    -- Step 2: Process each ticket
    FOREACH v_ticket_id IN ARRAY ticket_ids LOOP
        v_assigned := FALSE;
		
       RAISE NOTICE 'ticket id is: %', v_ticket_id;
      
        -- Fetch ticket type and order information
        SELECT ticket_type_id, parent_ticket_id, order_id
        INTO v_ticket_type_id, v_parent_ticket_id, v_order_id
        FROM custom_service_provider_ticket
        WHERE ticket_id = v_ticket_id;

        -- Get order_id and customer_id based on ticket type
        IF v_ticket_type_id = 1 THEN
            SELECT customer_id INTO v_customer_id FROM blc_order WHERE order_id = v_order_id;
        ELSIF v_ticket_type_id = 2 THEN
            SELECT o.customer_id, o.order_id
            INTO v_customer_id, v_order_id
            FROM custom_service_provider_ticket t
            JOIN blc_order o ON o.order_id = t.order_id
            WHERE t.ticket_id = v_parent_ticket_id;
        ELSE
            RAISE NOTICE 'Skipping ticket % with invalid ticket_type_id %', v_ticket_id, v_ticket_type_id;
            v_remaining_ticket_ids := array_append(v_remaining_ticket_ids, v_ticket_id);
            CONTINUE;
        END IF;

        -- Step 3: Try allocating from ranked queues
        IF array_length(rank1a, 1) > 0 THEN
            CALL process_rank_for_tickets(rank1a, v_ticket_id, v_order_id, v_ticket_type_id, v_customer_id, v_assigned, assigned_ticket_ids);
            IF v_assigned THEN CONTINUE; END IF;
        END IF;

        IF array_length(rank1b, 1) > 0 THEN
            CALL process_rank_for_tickets(rank1b, v_ticket_id, v_order_id, v_ticket_type_id, v_customer_id, v_assigned, assigned_ticket_ids);
            IF v_assigned THEN CONTINUE; END IF;
        END IF;

        IF array_length(rank1c, 1) > 0 THEN
            CALL process_rank_for_tickets(rank1c, v_ticket_id, v_order_id, v_ticket_type_id, v_customer_id, v_assigned, assigned_ticket_ids);
            IF v_assigned THEN CONTINUE; END IF;
        END IF;

        IF array_length(rank1d, 1) > 0 THEN
            CALL process_rank_for_tickets(rank1d, v_ticket_id, v_order_id, v_ticket_type_id, v_customer_id, v_assigned, assigned_ticket_ids);
            IF v_assigned THEN CONTINUE; END IF;
        END IF;

        IF array_length(rank2a, 1) > 0 THEN
            CALL process_rank_for_tickets(rank2a, v_ticket_id, v_order_id, v_ticket_type_id, v_customer_id, v_assigned, assigned_ticket_ids);
            IF v_assigned THEN CONTINUE; END IF;
        END IF;

        IF array_length(rank2b, 1) > 0 THEN
            CALL process_rank_for_tickets(rank2b, v_ticket_id, v_order_id, v_ticket_type_id, v_customer_id, v_assigned, assigned_ticket_ids);
            IF v_assigned THEN CONTINUE; END IF;
        END IF;

        IF array_length(rank2c, 1) > 0 THEN
            CALL process_rank_for_tickets(rank2c, v_ticket_id, v_order_id, v_ticket_type_id, v_customer_id, v_assigned, assigned_ticket_ids);
            IF v_assigned THEN CONTINUE; END IF;
        END IF;

        IF array_length(rank2d, 1) > 0 THEN
            CALL process_rank_for_tickets(rank2d, v_ticket_id, v_order_id, v_ticket_type_id, v_customer_id, v_assigned, assigned_ticket_ids);
            IF v_assigned THEN CONTINUE; END IF;
        END IF;

        -- If not assigned
        v_remaining_ticket_ids := array_append(v_remaining_ticket_ids, v_ticket_id);
    END LOOP;

    ticket_ids := v_remaining_ticket_ids;

    RAISE NOTICE 'VDTA Complete. Assigned: %, Remaining: %',
        array_length(assigned_ticket_ids, 1),
        array_length(ticket_ids, 1);
END;
$BODY$;