CREATE OR REPLACE PROCEDURE public.primary_ticket_rejection_flow(INOUT available_service_providers bigint[], INOUT assigned_tickets bigint[])
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    ticket_state_returned CONSTANT BIGINT := 6;
    ticket_type_primary CONSTANT BIGINT := 1;

    tickets_to_process BIGINT[];
BEGIN

	RAISE NOTICE '3. Primary Ticket Rejection Flow';
     
     -- Step 1: Fetch unassigned tickets in specific states and types
	SELECT ARRAY_AGG(c.ticket_id)
	INTO tickets_to_process
	FROM custom_service_provider_ticket c
	WHERE c.assignee_user_id IS NULL
	  AND c.ticket_state_id IN (ticket_state_returned)
	  AND c.ticket_type_id IN (ticket_type_primary);

    RAISE NOTICE 'Tickets received for auto-assignment(REJECTED PRIMARY TICKETS): %', array_length(tickets_to_process, 1);
	raise notice 'tickets for rejection flow: %', tickets_to_process;

    IF tickets_to_process IS NOT NULL AND array_length(tickets_to_process, 1) > 0 THEN
	    -- Step 2: Random Binding Ticket Allocation
	    CALL public.random_binding_ticket_allocation_for_tickets(
	        tickets_to_process,
	        assigned_tickets
	    );
	
		RAISE NOTICE 'Tickets recieved for VDTA(REJCTED PRIMARY TICKETS): %', array_length(tickets_to_process, 1);
	
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