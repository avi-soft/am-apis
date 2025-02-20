-- PROCEDURE: public.get_new_orders()

-- DROP PROCEDURE IF EXISTS public.get_new_orders();

CREATE OR REPLACE PROCEDURE public.get_new_orders(
	OUT order_ids bigint[])
LANGUAGE 'plpgsql'
AS $BODY$
BEGIN
    -- Select all order_ids where the order_state_id is 1
    SELECT array_agg(order_id) INTO order_ids
    FROM order_state
    WHERE order_state_id = 1;

    -- If no rows are found, return an empty array
    IF order_ids IS NULL THEN
        order_ids := '{}';  -- empty array
    END IF;
END;
$BODY$;
ALTER PROCEDURE public.get_new_orders()
    OWNER TO postgres;
