CREATE OR REPLACE PROCEDURE public.get_order_state_by_order_state_id(
    IN input_order_state_id INTEGER,
    OUT result_order_state_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    SELECT order_state_id
    INTO result_order_state_id
    FROM order_state_ref
    WHERE order_state_id = input_order_state_id
    LIMIT 1;

    IF result_order_state_id IS NULL THEN
        RAISE EXCEPTION 'No Order State Ref found with id = %', input_order_state_id;
    END IF;
END;
$$;

ALTER PROCEDURE public.get_order_state_by_order_state_id(INTEGER, OUT INTEGER)
    OWNER TO postgres;
