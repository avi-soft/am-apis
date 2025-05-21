CREATE OR REPLACE PROCEDURE public.primary_ticket_normal_flow(
    IN available_service_providers BIGINT[],      -- Input: List of available SPs
    OUT assigned_tickets BIGINT[]                 -- Output: List of assigned ticket IDs
)
LANGUAGE plpgsql
AS $$
DECLARE
    order_state_id CONSTANT INTEGER := 1;         -- "NEW" order state
    custom_orders BIGINT[];                       -- Orders in NEW state
BEGIN
    -- Step 1: Simulate fetching order state = 1 (NEW)
    IF order_state_id IS NULL THEN
        RAISE EXCEPTION 'No Order State Ref Found with id 1 (NEW).';
    END IF;

    -- Step 2: Fetch custom orders with order_state_id = 1
    CALL public.get_order_state_by_order_state_id(order_state_id, custom_orders);

    IF custom_orders IS NULL OR array_length(custom_orders, 1) = 0 THEN
        RAISE EXCEPTION 'No Orders to Assign';
    END IF;

    RAISE NOTICE 'Number of custom orders in NEW state: %', array_length(custom_orders, 1);

    assigned_tickets := ARRAY[]::BIGINT[]; -- initialize as empty array

    -- Step 3: RBTA logic — will append to assigned_tickets
    CALL public.random_binding_ticket_allocation(custom_orders, assigned_tickets);

    -- Step 4: VDTA logic — will also append to assigned_tickets
    CALL public.vertical_distribution_ticket_allocation(custom_orders, available_service_providers, assigned_tickets);

    RAISE NOTICE 'Final Assigned Tickets (RBTA + VDTA): %', assigned_tickets;
END;
$$;

ALTER PROCEDURE public.primary_ticket_normal_flow(
    IN available_service_providers BIGINT[],
    OUT assigned_tickets BIGINT[]
) OWNER TO postgres;
