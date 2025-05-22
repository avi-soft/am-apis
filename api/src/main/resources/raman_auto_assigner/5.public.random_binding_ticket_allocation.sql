-- Stored Procedure: random_binding_ticket_allocation
-- Description: Implements the RBTA logic for ticket assignment

CREATE OR REPLACE PROCEDURE public.random_binding_ticket_allocation(
    IN custom_orders jsonb
)
LANGUAGE plpgsql
AS $$
DECLARE
    custom_order jsonb;
    order_id bigint;
    customer_id bigint;
    ref record;
    product_id bigint;
    assigned boolean;
BEGIN
    RAISE NOTICE 'Random Binding Ticket Allocation (RBTA)';
    RAISE NOTICE 'Total Orders received by RBTA are: %', jsonb_array_length(custom_orders);

    FOR custom_order IN SELECT * FROM jsonb_array_elements(custom_orders)
    LOOP
        assigned := false;
        order_id := (custom_order ->> 'orderId')::bigint;

        -- Fetch Order and Customer from order_id
        SELECT customer_id INTO customer_id
        FROM orders
        WHERE id = order_id;

        -- PRIMARY BINDING LOGIC
        FOR ref IN
            SELECT r.*, sp.*
            FROM customer_referrer r
            JOIN service_provider sp ON r.service_provider_id = sp.service_provider_id
            WHERE r.customer_id = customer_id AND r.primary_ref = true AND sp.is_active = true
        LOOP
            CALL public.allocate_ticket(order_id, ref.service_provider_id, custom_order);
            assigned := true;
            EXIT;
        END LOOP;

        IF assigned THEN
            CONTINUE;
        END IF;

        -- SECONDARY REFERRERS
        FOR ref IN
            SELECT r.*, sp.*
            FROM customer_referrer r
            JOIN service_provider sp ON r.service_provider_id = sp.service_provider_id
            WHERE r.customer_id = customer_id AND sp.is_active = true
        LOOP
            CALL public.allocate_ticket(order_id, ref.service_provider_id, custom_order);
            assigned := true;
            EXIT;
        END LOOP;

        IF assigned THEN
            CONTINUE;
        END IF;

        -- CREATOR OF THE PRODUCT LOGIC
        SELECT (attrs -> 'productId')::text::bigint INTO product_id
        FROM order_item_attributes
        WHERE order_item_id = (
            SELECT id FROM order_items WHERE order_id = order_id LIMIT 1
        );

        SELECT user_id INTO customer_id
        FROM custom_product
        WHERE custom_product_id = product_id;

        CALL public.allocate_ticket(order_id, customer_id, custom_order);
    END LOOP;

    RAISE NOTICE 'RBTA Completed';
END;
$$;