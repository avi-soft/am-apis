CREATE OR REPLACE PROCEDURE vertical_distribution_ticket_allocation()
LANGUAGE plpgsql
AS $$
DECLARE
    custom_order RECORD;
    order_record RECORD;
    rank TEXT;
    -- Cursors or temp tables for service provider ranks
    ranks TEXT[] := ARRAY['rank1a', 'rank1b', 'rank1c', 'rank1d', 'rank2a', 'rank2b', 'rank2c', 'rank2d'];
    i INT := 1;
BEGIN
    RAISE INFO 'Vertical Distribution Ticket Allocation';

    -- Log counts
    PERFORM log_order_and_sp_counts();

    -- Call bifurcation procedure to populate service provider queues
    CALL bifurcate_available_service_providers();

    -- Log each rank queue size
    FOR rank IN SELECT unnest(ranks)
    LOOP
        PERFORM log_rank_queue_size(rank);
    END LOOP;

    -- Process all orders from temp table or cursor
    FOR custom_order IN SELECT * FROM custom_orders_queue LOOP
        -- Log current order being processed
        PERFORM log_custom_order(custom_order.order_id);

        -- Fetch the order record
        SELECT *
        INTO order_record
        FROM orders
        WHERE order_id = custom_order.order_id;

        -- Iterate through all ranks and try to process
        i := 1;
        WHILE i <= array_length(ranks, 1) LOOP
            rank := ranks[i];

            IF NOT is_rank_empty(rank) THEN
                IF process_rank(rank, order_record.order_id, custom_order.order_id) THEN
                    -- Remove from the queue if successfully processed
                    DELETE FROM custom_orders_queue WHERE order_id = custom_order.order_id;
                    EXIT;
                END IF;
            END IF;
            i := i + 1;
        END WHILE;
    END LOOP;

    -- Final log
    PERFORM log_assigned_ticket_count();
END;
$$;
