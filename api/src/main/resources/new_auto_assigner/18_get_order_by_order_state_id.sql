CREATE OR REPLACE PROCEDURE public.get_order_by_order_state_id(
	IN input_order_state_id integer,
	OUT result_order_id bigint[])
LANGUAGE 'plpgsql'
AS $BODY$
BEGIN

	RAISE NOTICE '18. Get Order by Order State Id';

    SELECT ARRAY_AGG(order_id ORDER BY active_end_date ASC NULLS LAST)
        INTO result_order_id
        FROM (
            SELECT DISTINCT ON (os.order_id)
                   os.order_id,
                   sku.active_end_date
            FROM order_state os
            JOIN blc_order o ON o.order_id = os.order_id
            JOIN blc_order_item oi ON oi.order_id = o.order_id
            JOIN blc_order_item_attribute oia ON oia.order_item_id = oi.order_item_id AND oia.name = 'productId'
            JOIN blc_product p ON p.product_id = oia.value::BIGINT
            JOIN blc_sku sku ON sku.sku_id = p.default_sku_id
            WHERE os.order_state_id = input_order_state_id
            ORDER BY os.order_id, sku.active_end_date DESC  -- get latest active_end_date per order
        ) ordered_orders;

    IF result_order_id IS NULL THEN
        RAISE NOTICE 'No Order found with id = %', input_order_state_id;
    END IF;
END;
$BODY$;
