CREATE OR REPLACE PROCEDURE public.random_binding_ticket_allocation(IN custom_orders bigint[], INOUT assigned_tickets bigint[])
LANGUAGE 'plpgsql' 
AS $BODY$

DECLARE
    v_order_id BIGINT;
    v_customer_id BIGINT;
    ref RECORD;
    v_product_id BIGINT;
	v_creator_user_id BIGINT;
	unassigned_orders BIGINT[];
    assigned BOOLEAN;
    v_sp_id BIGINT;
BEGIN

    RAISE NOTICE '10. Random Binding Ticket Allocation (RBTA)';

    RAISE NOTICE 'Total Orders received: %', array_length(custom_orders, 1);

    IF custom_orders IS NULL OR array_length(custom_orders, 1) = 0 THEN
		RAISE NOTICE 'No orders to process';
	
	ELSE
	    FOREACH v_order_id IN ARRAY custom_orders
	    LOOP
	        assigned := false;
	       
			RAISE NOTICE 'order id: %', v_order_id;
		
	        SELECT customer_id INTO v_customer_id
	        FROM blc_order
	        WHERE order_id = v_order_id;
	
			-- Double-check if customer exists in customer table (optional)
	        PERFORM 1 FROM custom_customer WHERE customer_id = v_customer_id;
	
	        -- PRIMARY REFERRER assignment
	        FOR ref IN
	            SELECT r.service_provider_id
	            FROM customer_referrer r
	            JOIN service_provider sp ON sp.service_provider_id = r.service_provider_id
	            WHERE r.customer_id = v_customer_id AND r.primary_ref = true AND sp.is_active = true AND sp.approved = true AND sp.role IN (2,4)
	        LOOP
	            CALL public.allocate_ticket(v_order_id, ref.service_provider_id, assigned, assigned_tickets);
	            IF assigned THEN EXIT; END IF;
	        END LOOP;
	
	        IF assigned THEN CONTINUE; END IF;
	
	        -- SECONDARY REFERRERS
	        FOR ref IN
	            SELECT r.service_provider_id
	            FROM customer_referrer r
	            JOIN service_provider sp ON sp.service_provider_id = r.service_provider_id
	            WHERE r.customer_id = v_customer_id AND sp.is_active = true AND sp.approved = true AND sp.role IN (2,4)
	        LOOP
	            CALL public.allocate_ticket(v_order_id, ref.service_provider_id, assigned, assigned_tickets);
	            IF assigned THEN EXIT; END IF;
	        END LOOP;
	
	        IF assigned THEN CONTINUE; END IF;
	
	        -- PRODUCT CREATOR fallback logic
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
 
	        -- Call allocate_ticket only if a matching service provider was found
			IF v_sp_id IS NOT NULL THEN
			    CALL public.allocate_ticket(v_order_id, v_sp_id, assigned, assigned_tickets);
			END IF;
		
	        IF assigned THEN
	            assigned := true;
	        END IF;
	
	        -- If not assigned, keep in unassigned_orders
	        IF NOT assigned THEN
	            unassigned_orders := array_append(unassigned_orders, v_order_id);
	        END IF;
	    END LOOP;

	    -- Replace custom_orders with only unassigned orders
	    custom_orders := unassigned_orders;
	
	    RAISE NOTICE 'RBTA Completed. Assigned: %, Unassigned Remaining: %',
	                 array_length(assigned_tickets, 1),
	                 array_length(custom_orders, 1);
	END IF;
END;
$BODY$;