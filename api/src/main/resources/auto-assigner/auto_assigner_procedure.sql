-- PROCEDURE: public.auto_assigner_procedure()

-- DROP PROCEDURE IF EXISTS public.auto_assigner_procedure();

CREATE OR REPLACE PROCEDURE public.auto_assigner_procedure(
	OUT total_assigned_tickets bigint[])
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    custom_orders bigint[];
    p_assigned_tickets BIGINT[];      -- Variable for RBTA output
    v_order_ids_un BIGINT[];
    assigned_tickets BIGINT[];
BEGIN
    -- Call the random_binding_ticket_allocation (RBTA) procedure
    CALL public.random_binding_ticket_allocation(p_assigned_tickets, v_order_ids_un);
    RAISE NOTICE 'RBTA - Unassigned Order IDs: %', v_order_ids_un;

    custom_orders := v_order_ids_un;
    total_assigned_tickets := p_assigned_tickets;

    IF custom_orders IS NOT NULL AND array_length(custom_orders, 1) > 0 THEN
        RAISE NOTICE 'Proceeding to VDTA with Orders: %', custom_orders;

        -- Call the vertical_distribution_ticket_allocation (VDTA) procedure
        CALL public.vertical_distribution_ticket_allocation(custom_orders, assigned_tickets);
    -- Call the vertical_distribution_ticket_allocation (VDTA) procedure

    END IF;
    IF assigned_tickets IS NOT NULL THEN
        FOR i IN 1..array_length(assigned_tickets, 1) LOOP
            total_assigned_tickets := array_append(total_assigned_tickets, assigned_tickets[i]);
        END LOOP;
    END IF;

    -- Optionally, raise notices to show the results of both procedures
    RAISE NOTICE 'RBTA - Assigned Tickets: %, Unassigned Order IDs: %', p_assigned_tickets, v_order_ids_un;
    RAISE NOTICE 'VDTA - Assigned Tickets: %', assigned_tickets;
END;
$BODY$;
ALTER PROCEDURE public.auto_assigner_procedure()
    OWNER TO postgres;
