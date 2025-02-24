-- PROCEDURE: public.random_binding_ticket_allocation()

-- DROP PROCEDURE IF EXISTS public.random_binding_ticket_allocation();

CREATE OR REPLACE PROCEDURE public.random_binding_ticket_allocation(
	OUT p_assigned_tickets bigint[],
	OUT v_order_ids_un bigint[])
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    -- Declare necessary variables for order, customer, service provider, etc.
    v_order_id BIGINT;
    v_customer_id BIGINT;
    v_service_provider_id BIGINT;
    v_ticket_id BIGINT;
    v_assigned BOOLEAN := FALSE;
    v_product_id BIGINT;
    v_user_id BIGINT;
    v_primary_referrer BOOLEAN;
    v_is_active BOOLEAN;
    v_max_ticket_size INT;
    v_ticket_assigned INT;
    v_ticket_pending INT;
    v_referrer_service_provider_id BIGINT;
    v_order_ids BIGINT[]; -- Array to hold the order ids
BEGIN
    -- Initialize the output arrays
    p_assigned_tickets := ARRAY[]::BIGINT[];
    v_order_ids_un := ARRAY[]::BIGINT[];

    -- Fetch order ids with order_state_id = 1 using the previously created procedure
    CALL get_new_orders(v_order_ids);

    -- Loop through each order id retrieved from the procedure
    FOREACH v_order_id IN ARRAY v_order_ids LOOP
        -- Fetch Order details
        SELECT customer_id INTO v_customer_id
        FROM blc_order
        WHERE order_id = v_order_id;

        -- Fetch Customer details (this appears redundant if the customer already exists in blc_order)
        SELECT customer_id INTO v_customer_id
        FROM custom_customer
        WHERE customer_id = v_customer_id;

        -- Fetch Referrers for the customer
        FOR v_referrer_service_provider_id IN
            SELECT service_provider_id
            FROM customer_referrer
            WHERE customer_id = v_customer_id LOOP

            -- Fetch Service Provider details for the referrer
            SELECT service_provider_id, is_active, maximum_ticket_size, ticket_assigned, ticket_pending
            INTO v_service_provider_id, v_is_active, v_max_ticket_size, v_ticket_assigned, v_ticket_pending
            FROM service_provider
            WHERE service_provider_id = v_referrer_service_provider_id;

            -- Check if the referrer is active and within the ticket size limits
            IF v_is_active = TRUE AND (v_ticket_assigned + v_ticket_pending < v_max_ticket_size) THEN
                -- Allocate the ticket to this service provider by calling the ticket allocation procedure
                CALL allocate_ticket_to_service_provider(
                    p_order_id := v_order_id,
                    p_assignee_user_id := v_service_provider_id, -- Service provider ID
                    p_ticket_type_id := 1,  -- Default ticket type (1)
                    p_ticket_state_id := 1, -- Default ticket state (1)
                    p_ticket_status_id := 0, -- Default ticket status (1)
                    p_assignee_role_id := 4, -- Role ID for service provider (Assuming 4 for service provider role)
                    p_creator_role_id := 1, -- Role ID for the creator (Assuming 1 for creator role)
                    p_creator_user_id := v_customer_id, -- Customer who created the order (creator)
                    p_task_desc := 'Ticket allocated to referrer', -- Task description
                    p_ticket_id := v_ticket_id  -- The new ticket ID will be returned here
                );

                -- Set assigned flag to true and exit the referrer loop
                v_assigned := TRUE;
                EXIT;
            END IF;
        END LOOP;

        -- If no ticket was assigned, try assigning to the product creator
        IF NOT v_assigned THEN
            -- Fetch product details from the first order item
            SELECT a.value::BIGINT INTO v_product_id
            FROM blc_order_item oi
            JOIN blc_order o ON oi.order_id = o.order_id
            JOIN blc_order_item_attribute a ON a.order_item_id = oi.order_item_id
            WHERE o.order_id = v_order_id
              AND a.name = 'productId'
              AND a.order_item_id = oi.order_item_id;

            -- Fetch the user (creator) associated with the product
            SELECT creator_user_id INTO v_user_id
            FROM custom_product
            WHERE product_id = v_product_id;

            -- Allocate ticket to the creator of the product
            CALL allocate_ticket_to_service_provider(
                p_order_id := v_order_id,
                p_assignee_user_id := v_user_id, -- User ID of the product creator
                p_ticket_type_id := 1,  -- Default ticket type (1)
                p_ticket_state_id := 1, -- Default ticket state (1)
                p_ticket_status_id := 0, -- Default ticket status (1)
                p_assignee_role_id := 4, -- Role ID for service provider (Assuming 4 for service provider role)
                p_creator_role_id := 1, -- Role ID for the creator (Assuming 1 for creator role)
                p_creator_user_id := v_customer_id, -- Customer who created the order (creator)
                p_task_desc := 'Ticket allocated to product creator', -- Task description
                p_ticket_id := v_ticket_id  -- The new ticket ID will be returned here
            );
            IF v_ticket_id IS NULL THEN
                RAISE NOTICE 'appending % v_order_id', v_order_id;
                v_order_ids_un := array_append(v_order_ids_un, v_order_id);
                CONTINUE;
            END IF;
            v_assigned := TRUE;
        END IF;

        -- If the ticket is assigned, add the ticket id to the output array
        IF v_assigned THEN
            p_assigned_tickets := array_append(p_assigned_tickets, v_ticket_id);
        END IF;

        -- Reset assigned flag for the next iteration
        v_assigned := FALSE;
    END LOOP;

    RAISE NOTICE 'unassigned orders by RBTA %', v_order_ids_un;
END;
$BODY$;
ALTER PROCEDURE public.random_binding_ticket_allocation()
    OWNER TO postgres;
