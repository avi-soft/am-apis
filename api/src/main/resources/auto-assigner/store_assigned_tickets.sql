-- PROCEDURE: public.store_assigned_tickets(integer, integer)

-- DROP PROCEDURE IF EXISTS public.store_assigned_tickets(integer, integer);

CREATE OR REPLACE PROCEDURE public.store_assigned_tickets(
	IN p_order_id integer,
	IN p_order_state_id integer)
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    order_state_list INTEGER[];  -- Declare an array to store the order_state_ids temporarily
BEGIN
    -- Add the provided values into the array (just for the duration of the procedure execution)
    order_state_list := array[p_order_id, p_order_state_id];

    -- Log the contents of the array (just for illustration)
    RAISE NOTICE 'Stored order_id and order_state_id: %', order_state_list;

    -- If needed, you can do further operations on this array
    -- For example, logging the contents or processing the values.
END;
$BODY$;
ALTER PROCEDURE public.store_assigned_tickets(integer, integer)
    OWNER TO postgres;
