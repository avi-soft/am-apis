CREATE OR REPLACE PROCEDURE public.review_ticket_rejection_flow(INOUT available_service_providers bigint[], INOUT assigned_tickets bigint[])
LANGUAGE 'plpgsql'
AS $BODY$

DECLARE
    ticket_state_returned CONSTANT BIGINT := 6;
    ticket_type_review CONSTANT BIGINT := 2;

    tickets_to_process BIGINT[];
BEGIN

	RAISE NOTICE '15. Review Ticket Rejection Logic';
     
     -- Step 1: Fetch unassigned tickets in specific states and types
	SELECT ARRAY_AGG(c.ticket_id)
	INTO tickets_to_process
	FROM custom_service_provider_ticket c
	WHERE c.assignee_user_id IS NULL
	  AND c.ticket_state_id IN (ticket_state_returned)
	  AND c.ticket_type_id IN (ticket_type_review)
	  AND (
            c.ticket_type_id = 2
            AND EXISTS (
                SELECT 1
                FROM custom_service_provider_ticket pt
                WHERE pt.ticket_id = c.parent_ticket_id
                  AND pt.ticket_type_id = 1
            )
	    );

    RAISE NOTICE 'Tickets received for auto-assignment: %', array_length(tickets_to_process, 1);

   IF tickets_to_process IS NOT NULL AND array_length(tickets_to_process, 1) > 0 THEN
	    -- Step 2: Random Binding Ticket Allocation
	    CALL public.random_binding_ticket_allocation_for_tickets(
	        tickets_to_process,
	        assigned_tickets
	    );
	
		RAISE NOTICE 'Tickets recieved for rvdta for tickets: %', array_length(tickets_to_process, 1);
	
		IF tickets_to_process IS NOT NULL AND array_length(tickets_to_process, 1) > 0 THEN
			-- Step 3: Vertical Distribution Ticket Allocation
		    CALL public.vertical_distribution_ticket_allocation_for_tickets(
		        tickets_to_process,
		        available_service_providers,
		        assigned_tickets
		    );
	   END IF;
   END IF;

    RAISE NOTICE 'Assigned tickets after rejection and review logic: %', assigned_tickets;
END;
$BODY$;