CREATE OR REPLACE PROCEDURE public.auto_assigner_procedure(OUT total_assigned_tickets bigint[])
LANGUAGE 'plpgsql' 
AS $BODY$

DECLARE
    available_service_providers BIGINT[];         -- IDs of available SPs
    primary_rejected_assigned_tickets BIGINT[] := ARRAY[]::BIGINT[];
    primary_assigned_tickets BIGINT[] := ARRAY[]::BIGINT[];            -- Tickets from primary flow (RBTA + VDTA)

    rejected_review_tickets BIGINT[] := ARRAY[]::BIGINT[];            -- Tickets from rejection/review logic
    review_rejected_assigned_tickets BIGINT[] := ARRAY[]::BIGINT[];
    review_assigned_tickets BIGINT[] := ARRAY[]::BIGINT[];
BEGIN

    RAISE NOTICE '1. Auto-Assigner';

    -- Step 1: Fetch all active and approved Service Providers using stored procedure
    CALL public.get_active_and_approved_service_providers(available_service_providers);

    IF available_service_providers IS NULL OR array_length(available_service_providers, 1) = 0 THEN
        RAISE EXCEPTION 'No active and approved Service Providers found.';
    END IF;

    RAISE NOTICE '--> Available Service Providers: %', available_service_providers;

    -- Step 2: Call Primary Ticket Flow (RBTA + VDTA)
    CALL public.primary_ticket_rejection_flow(available_service_providers, primary_rejected_assigned_tickets);
    RAISE NOTICE '--> Total Primary Rejected Tickets Assigned: %', primary_rejected_assigned_tickets;

    CALL public.primary_ticket_normal_flow(available_service_providers, primary_assigned_tickets); --
    RAISE NOTICE '--> Total Primary Tickets Assigned: %', primary_assigned_tickets;

    -- Step 3: Call Review Ticket Flow
    CALL public.review_ticket_rejection_flow(available_service_providers, review_rejected_assigned_tickets);
    RAISE NOTICE '--> Total Review Rejected Tickets Assigned: %', review_rejected_assigned_tickets;
   
    CALL public.review_ticket_normal_flow(available_service_providers, review_assigned_tickets);
    RAISE NOTICE '--> Total Review Ticket Assigned: %', review_assigned_tickets;

    -- Step 4: Merge both ticket assignments
    total_assigned_tickets := primary_rejected_assigned_tickets || primary_assigned_tickets || review_rejected_assigned_tickets || review_assigned_tickets;

    RAISE NOTICE 'Total Tickets Assigned: %', total_assigned_tickets;
   
END;
$BODY$;