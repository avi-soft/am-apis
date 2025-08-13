CREATE OR REPLACE PROCEDURE public.primary_ticket_rejection_flow(INOUT available_service_providers bigint[], INOUT assigned_tickets bigint[])
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    ticket_state_returned CONSTANT BIGINT := 6;
    ticket_type_primary CONSTANT BIGINT := 1;

    tickets_to_process BIGINT[];
BEGIN

	RAISE NOTICE '3. Primary Ticket Rejection Flow';
     
     -- Step 1: Fetch unassigned primary tickets in returned state
         SELECT ARRAY_AGG(ticket_id ORDER BY active_end_date ASC NULLS LAST)
         INTO tickets_to_process
         FROM (
             SELECT DISTINCT ON (c.ticket_id)
                    c.ticket_id,
                    sku.active_end_date
             FROM custom_service_provider_ticket c
             JOIN blc_order o ON c.order_id = o.order_id
             JOIN blc_order_item oi ON oi.order_id = o.order_id
             JOIN blc_order_item_attribute oia
                 ON oia.order_item_id = oi.order_item_id AND oia.name = 'productId'
             JOIN blc_product p ON p.product_id = oia.value::BIGINT
             JOIN blc_sku sku ON sku.sku_id = p.default_sku_id
             WHERE c.assignee_user_id IS NULL
               AND c.ticket_state_id = ticket_state_returned
               AND c.ticket_type_id = ticket_type_primary
               AND c.archived = false
             ORDER BY c.ticket_id, sku.active_end_date DESC
         ) ordered_tickets;

    RAISE NOTICE 'Tickets received for auto-assignment(REJECTED PRIMARY TICKETS): %', array_length(tickets_to_process, 1);
	raise notice 'tickets for rejection flow: %', tickets_to_process;

    IF tickets_to_process IS NOT NULL AND array_length(tickets_to_process, 1) > 0 THEN
	    -- Step 2: Random Binding Ticket Allocation
	    CALL public.random_binding_ticket_allocation_for_tickets(
	        tickets_to_process,
	        assigned_tickets
	    );
	
		RAISE NOTICE 'Tickets received for VDTA(REJECTED PRIMARY TICKETS): %', array_length(tickets_to_process, 1);
	
		-- Step 3: Vertical Distribution Ticket Allocation
	    CALL public.vertical_distribution_ticket_allocation_for_tickets(
	        tickets_to_process,
	        available_service_providers,
	        assigned_tickets
	    );
    END IF;

    RAISE NOTICE 'REJECTED PRIMARY TICKET Assigned tickets: %', assigned_tickets;
END;
$BODY$;