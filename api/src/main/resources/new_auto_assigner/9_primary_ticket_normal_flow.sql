CREATE OR REPLACE PROCEDURE public.primary_ticket_normal_flow(IN available_service_providers bigint[], OUT assigned_tickets bigint[])
LANGUAGE 'plpgsql'
AS $BODY$

DECLARE
    v_order_state_id CONSTANT INTEGER := 1;         -- "NEW" order state
    custom_orders BIGINT[];                       -- Orders in NEW state
BEGIN

	RAISE NOTICE '9. PRIMARY TICKET STORED PROCEDURE';

    -- Step 1: Simulate fetching order state = 1 (NEW)
    IF v_order_state_id IS NULL THEN
        RAISE EXCEPTION 'No Order State Ref Found with id 1 (NEW).';
    END IF;

    -- Step 2: Fetch custom orders with order_state_id = 1
    CALL public.get_order_by_order_state_id(v_order_state_id, custom_orders);

    IF custom_orders IS NULL OR array_length(custom_orders, 1) = 0 THEN
        RAISE NOTICE 'No Orders to Assign';
    END IF;

    RAISE NOTICE 'Number of custom orders in NEW state: %', array_length(custom_orders, 1);

    assigned_tickets := ARRAY[]::BIGINT[]; -- initialize as empty array

    -- Step 3: RBTA logic — will append to assigned_tickets
    IF custom_orders IS NOT NULL AND array_length(custom_orders, 1) > 0 THEN
	    CALL public.random_binding_ticket_allocation(custom_orders, assigned_tickets);
		RAISE NOTICE 'Assigned Tickets (RBTA) {for normal flow}: %', assigned_tickets;
	END IF;

    RAISE NOTICE 'AFter RBTA Order is %', custom_orders;

	IF custom_orders IS NOT NULL AND array_length(custom_orders, 1) > 0 THEN
    -- Step 4: VDTA logic — will also append to assigned_tickets
    CALL public.vertical_distribution_ticket_allocation(custom_orders, available_service_providers, assigned_tickets);
    RAISE NOTICE 'Final Assigned Tickets (RBTA + VDTA) {for normal flow}: %', assigned_tickets;
    END IF;

END;
$BODY$;