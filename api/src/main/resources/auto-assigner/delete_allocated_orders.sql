-- PROCEDURE: public.delete_allocated_orders(bigint[], bigint[])

-- DROP PROCEDURE IF EXISTS public.delete_allocated_orders(bigint[], bigint[]);

CREATE OR REPLACE PROCEDURE public.delete_allocated_orders(
	IN allocated_orders bigint[],
	INOUT order_ids bigint[])
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    -- Declare a temporary array to hold the filtered order_ids
    temp_order_ids BIGINT[] := '{}';
    order_id BIGINT;
BEGIN
	IF order_ids IS NOT NULL THEN
    -- Loop through the order_ids array
    FOREACH order_id IN ARRAY order_ids
    LOOP
        -- Check if the order_id is not in the allocated_orders array
        IF NOT order_id = ANY(allocated_orders) THEN
            -- If not in allocated_orders, add it to temp_order_ids
            temp_order_ids := array_append(temp_order_ids, order_id);
        END IF;
    END LOOP;
    END IF;
    -- Update the order_ids array with the filtered values
    order_ids := temp_order_ids;

    -- Optionally, raise notice to show the updated order_ids
    RAISE NOTICE 'Updated order_ids: %', order_ids;
END;
$BODY$;
ALTER PROCEDURE public.delete_allocated_orders(bigint[], bigint[])
    OWNER TO postgres;
