CREATE OR REPLACE PROCEDURE public.review_ticket_normal_flow(INOUT available_service_providers bigint[], INOUT assigned_tickets bigint[])
LANGUAGE 'plpgsql'
AS $BODY$

DECLARE
    ticket_state_to_do CONSTANT BIGINT := 1;
    ticket_type_review CONSTANT BIGINT := 2;
    ticket_type_primary CONSTANT BIGINT := 1;

    tickets_to_process BIGINT[];
BEGIN

	RAISE NOTICE '16. Review Ticket Normal Flow';
     
     -- Step 1: Fetch unassigned review tickets in TODO state, sorted by active_end_date
     	SELECT ARRAY_AGG(ticket_id ORDER BY active_end_date ASC NULLS LAST)
            INTO tickets_to_process
            FROM (
                SELECT DISTINCT ON (c.ticket_id)
                    c.ticket_id,
                    sku.active_end_date
                FROM custom_service_provider_ticket c
                -- Join parent ticket (pt)
                JOIN custom_service_provider_ticket pt ON pt.ticket_id = c.parent_ticket_id
                -- Join order via parent ticket
                JOIN blc_order o ON o.order_id = pt.order_id
                JOIN blc_order_item oi ON oi.order_id = o.order_id
                JOIN blc_order_item_attribute oia
                    ON oia.order_item_id = oi.order_item_id AND oia.name = 'productId'
                JOIN blc_product p ON p.product_id = oia.value::BIGINT
                JOIN blc_sku sku ON sku.sku_id = p.default_sku_id
                WHERE c.assignee_user_id IS NULL
                  AND c.ticket_state_id = ticket_state_to_do
                  AND c.ticket_type_id = ticket_type_review
                  AND pt.ticket_type_id = ticket_type_primary  -- Ensure parent is primary
                  AND c.archived = false
                ORDER BY c.ticket_id, sku.active_end_date DESC
            ) ordered_tickets;

    RAISE NOTICE 'Tickets received for auto-assignment(REVIEW TICKET FLOW): %', array_length(tickets_to_process, 1);
	
    IF tickets_to_process IS NOT NULL AND array_length(tickets_to_process, 1) > 0 THEN
	    -- Step 2: Random Binding Ticket Allocation
	    CALL public.random_binding_ticket_allocation_for_tickets(
	        tickets_to_process,
	        assigned_tickets
	    );
		
		RAISE NOTICE 'Tickets recieved for vdta for tickets(REVIEW TICKET FLOW): %', array_length(tickets_to_process, 1);
		
		IF tickets_to_process IS NOT NULL AND array_length(tickets_to_process, 1) > 0 THEN
			-- Step 3: Vertical Distribution Ticket Allocation
		    CALL public.vertical_distribution_ticket_allocation_for_tickets(
		        tickets_to_process,
		        available_service_providers,
		        assigned_tickets
		    );
	   END IF;
	  
    END IF;

    RAISE NOTICE 'REVIEW TICKET Assigned tickets: %', assigned_tickets;
END;
$BODY$;