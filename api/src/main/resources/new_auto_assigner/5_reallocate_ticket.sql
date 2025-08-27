CREATE OR REPLACE PROCEDURE public.reallocate_ticket(IN p_order_id bigint, IN p_service_provider_id bigint, IN p_ticket_id bigint, IN p_ticket_type_id bigint, IN p_customer_id bigint, IN p_is_review_ticket boolean, IN p_is_primary_ticket boolean, INOUT assigned boolean, INOUT assigned_ticket_ids bigint[])
LANGUAGE 'plpgsql'
AS $BODY$

DECLARE
    v_max_tickets INT;
    v_current_total INT;
    v_is_active BOOLEAN;
    v_now TIMESTAMP := CURRENT_TIMESTAMP;
    v_target_completion_date TIMESTAMP;
    v_product_active_end_date TIMESTAMP;
   	v_order_item_id BIGINT;
    v_product_id BIGINT;
    v_sku_id BIGINT;
BEGIN

	RAISE NOTICE '5. Re-Allocate Ticket';

    -- Fetch active status and limits
    SELECT is_active, ticket_assigned + ticket_pending,
           COALESCE(maximum_ticket_size, ranking_maximum_ticket_size)
    INTO v_is_active, v_current_total, v_max_tickets
    FROM (
        SELECT sp.is_active,
               sp.ticket_assigned,
               sp.ticket_pending,
               sp.maximum_ticket_size,
               r.maximum_ticket_size AS ranking_maximum_ticket_size
        FROM service_provider sp
        LEFT JOIN service_provider_rank_mapping m
               ON sp.service_provider_id = m.service_provider_id
        LEFT JOIN service_provider_rank r
               ON m.rank_id = r.rank_id
        WHERE sp.service_provider_id = p_service_provider_id
    ) AS sub;

--	RAISE 'v_is_active % and v_current_total % and v_max_tickets %', v_is_active, v_current_total, v_max_tickets;
    IF v_is_active AND v_current_total < v_max_tickets THEN

    	-- Step 1: Get order_item_id from blc_order_item
	    SELECT order_item_id
	    INTO v_order_item_id
	    FROM blc_order_item
	    WHERE order_id = p_order_id
	    LIMIT 1;

	    -- Step 2: Get product_id from blc_order_item_attribute (as a string, cast to BIGINT)
	    SELECT value::BIGINT
	    INTO v_product_id
	    FROM blc_order_item_attribute
	    WHERE order_item_id = v_order_item_id
	      AND name = 'productId'
	    LIMIT 1;
	
	    -- Step 3: Get default_sku_id from blc_product
	    SELECT default_sku_id
	    INTO v_sku_id
	    FROM blc_product
	    WHERE product_id = v_product_id;
	
	    -- Step 4: Get active_end_date from blc_sku
	    SELECT active_end_date
	    INTO v_product_active_end_date
	    FROM blc_sku
	    WHERE sku_id = v_sku_id;

	   -- Conditionally update active_end_date for review tickets
       IF p_is_review_ticket THEN
           v_product_active_end_date := v_product_active_end_date + INTERVAL '4 days';
       END IF;

	    v_target_completion_date = CASE 
                WHEN p_is_review_ticket THEN v_now + INTERVAL '4 days'
                WHEN p_is_primary_ticket THEN v_now + INTERVAL '4 hours'
                ELSE NULL
        END IF;
        raise notice 'target completion date is: %', v_target_completion_date;
        raise notice 'product_active_end_date is: %', v_product_active_end_date;
        raise notice 'product id is: %', v_product_id;

        IF v_target_completion_date IS NOT NULL AND v_target_completion_date < v_product_active_end_date THEN
	        -- Assign ticket state and status
	        UPDATE custom_service_provider_ticket
	        SET ticket_state_id = 1,  -- TO-DO
	            ticket_status_id = 0, -- Initial status
	            assignee_user_id = p_service_provider_id,
	            assignee_role_id = 4,
	            modified_date = v_now,
	            ticket_assign_time = v_now,
	            target_completion_time = CASE 
	                WHEN p_is_review_ticket THEN v_now + INTERVAL '4 days'
	                WHEN p_is_primary_ticket THEN v_now + INTERVAL '4 hours'
	                ELSE NULL
	            END
	        WHERE ticket_id = p_ticket_id;

	        -- Update order state

	        IF p_is_primary_ticket THEN
                UPDATE order_state
                SET
                    order_state_id = 2,
                    modified_date = NOW()
                WHERE
                    order_id = p_order_id;
            END IF;

	        -- Increment SP ticket count
	        UPDATE service_provider
	        SET ticket_assigned = ticket_assigned + 1
	        WHERE service_provider_id = p_service_provider_id;
	
	        -- Append ticket to result
	        assigned_ticket_ids := array_append(assigned_ticket_ids, p_ticket_id);
	        assigned := TRUE;

            INSERT INTO public.email_queue (
                archived,
                created_date,
                user_id,
                role_id,
                ticket_id
            ) VALUES (
                false,
                NOW(),
                p_service_provider_id,
                4,
                p_ticket_id
            );
       
       ELSE
       	RAISE NOTICE 'Cannot assign as the product active date is before its new completion date';
       END IF;
    ELSE
        RAISE NOTICE 'Service Provider inactive or ticket limit exceeded %', p_service_provider_id;
    END IF;
END;
$BODY$;