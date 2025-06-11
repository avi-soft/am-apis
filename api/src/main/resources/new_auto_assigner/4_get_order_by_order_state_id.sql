-- PROCEDURE: public.get_order_by_order_state_id(integer)

-- DROP PROCEDURE IF EXISTS public.get_order_by_order_state_id(integer);

CREATE OR REPLACE PROCEDURE public.get_order_by_order_state_id(
	IN input_order_state_id integer,
	OUT result_order_id bigint[])
LANGUAGE 'plpgsql'
AS $BODY$
BEGIN

	RAISE NOTICE '4. Get Order by Order State Id';

    SELECT array_agg(order_id)
    INTO result_order_id
    FROM order_state
    WHERE order_state_id = input_order_state_id
    LIMIT 1;

    IF result_order_id IS NULL THEN
        RAISE NOTICE 'No Order found with id = %', input_order_state_id;
    END IF;
END;
$BODY$;
ALTER PROCEDURE public.get_order_by_order_state_id(integer)
    OWNER TO postgres;
