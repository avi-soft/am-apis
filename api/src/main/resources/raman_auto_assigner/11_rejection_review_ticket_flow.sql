-- PROCEDURE: public.rejection_and_review_ticket_logic(bigint[], bigint[])

-- DROP PROCEDURE IF EXISTS public.rejection_and_review_ticket_logic(bigint[], bigint[]);

CREATE OR REPLACE PROCEDURE public.rejection_and_review_ticket_logic(
	INOUT available_service_providers bigint[],
	INOUT assigned_tickets bigint[])
LANGUAGE 'plpgsql'
AS $BODY$

DECLARE
    ticket_state_to_do CONSTANT BIGINT := 1;
    ticket_state_returned CONSTANT BIGINT := 6;
    ticket_type_review CONSTANT BIGINT := 2;
    ticket_type_primary CONSTANT BIGINT := 1;

    tickets_to_process BIGINT[];
BEGIN

	RAISE NOTICE '11. Rejection and Review Ticket Logic';

    -- Step 1: Fetch unassigned tickets in specific states and types
    SELECT ARRAY_AGG(c.ticket_id)
    INTO tickets_to_process
    FROM custom_service_provider_ticket c
    WHERE c.assignee_user_id IS NULL
      AND c.ticket_state_id IN (ticket_state_to_do, ticket_state_returned)
      AND c.ticket_type_id IN (ticket_type_review, ticket_type_primary);

    RAISE NOTICE 'Tickets received for auto-assignment: %', array_length(tickets_to_process, 1);

    -- Step 2: Random Binding Ticket Allocation
--    CALL public.random_binding_ticket_allocation_for_tickets(
--        tickets_to_process,
--        assigned_tickets
--    );

	RAISE NOTICE 'Tickets assigned by rbta for tickets: %', array_length(tickets_to_process, 1);

	-- Step 3: Vertical Distribution Ticket Allocation
    CALL public.vertical_distribution_ticket_allocation_for_tickets(
        tickets_to_process,
        available_service_providers,
        assigned_tickets
    );

    RAISE NOTICE 'Assigned tickets after rejection and review logic: %', assigned_tickets;
END;
$BODY$;
ALTER PROCEDURE public.rejection_and_review_ticket_logic(bigint[], bigint[])
    OWNER TO postgres;
