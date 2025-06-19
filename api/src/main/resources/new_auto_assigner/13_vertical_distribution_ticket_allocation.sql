CREATE OR REPLACE PROCEDURE public.vertical_distribution_ticket_allocation(IN custom_orders bigint[], INOUT available_service_provider bigint[], INOUT assigned_tickets bigint[])
LANGUAGE 'plpgsql'
AS $BODY$

DECLARE
    rank1a BIGINT[] := ARRAY[]::BIGINT[];
    rank1b BIGINT[] := ARRAY[]::BIGINT[];
    rank1c BIGINT[] := ARRAY[]::BIGINT[];
    rank1d BIGINT[] := ARRAY[]::BIGINT[];
    rank2a BIGINT[] := ARRAY[]::BIGINT[];
    rank2b BIGINT[] := ARRAY[]::BIGINT[];
    rank2c BIGINT[] := ARRAY[]::BIGINT[];
    rank2d BIGINT[] := ARRAY[]::BIGINT[];

    allocated_order_ids BIGINT[] := ARRAY[]::BIGINT[];
    current_order BIGINT;
    v_result BOOLEAN;
BEGIN

	RAISE NOTICE '13. Vertical Distribution Ticket Allocation';

    RAISE NOTICE 'Vertical Distribution Ticket Allocation Called';
    RAISE NOTICE 'Total orders received for VDTA: %', array_length(custom_orders, 1);

    -- Fill rank arrays by bifurcating available_service_provider
    CALL public.bifurcate_available_service_providers(
        available_service_provider,
        rank1a, rank1b, rank1c, rank1d,
        rank2a, rank2b, rank2c, rank2d
    );

    IF custom_orders IS NOT NULL THEN
        FOR i IN 1..array_length(custom_orders, 1) LOOP
            current_order := custom_orders[i];
            v_result := false;

            -- Try rank1a
            IF array_length(rank1a, 1) > 0 THEN
                CALL public.process_rank(rank1a, current_order, assigned_tickets, v_result);
                IF v_result THEN
                    allocated_order_ids := array_append(allocated_order_ids, current_order);
                    CONTINUE;
                END IF;
            END IF;

            -- Try rank1b
            IF array_length(rank1b, 1) > 0 THEN
                CALL public.process_rank(rank1b, current_order, assigned_tickets, v_result);
                IF v_result THEN
                    allocated_order_ids := array_append(allocated_order_ids, current_order);
                    CONTINUE;
                END IF;
            END IF;

            -- Try rank1c
            IF array_length(rank1c, 1) > 0 THEN
                CALL public.process_rank(rank1c, current_order, assigned_tickets, v_result);
                IF v_result THEN
                    allocated_order_ids := array_append(allocated_order_ids, current_order);
                    CONTINUE;
                END IF;
            END IF;

            -- Try rank1d
            IF array_length(rank1d, 1) > 0 THEN
                CALL public.process_rank(rank1d, current_order, assigned_tickets, v_result);
                IF v_result THEN
                    allocated_order_ids := array_append(allocated_order_ids, current_order);
                    CONTINUE;
                END IF;
            END IF;

            -- Try rank2a
            IF array_length(rank2a, 1) > 0 THEN
                CALL public.process_rank(rank2a, current_order, assigned_tickets, v_result);
                IF v_result THEN
                    allocated_order_ids := array_append(allocated_order_ids, current_order);
                    CONTINUE;
                END IF;
            END IF;

            -- Try rank2b
            IF array_length(rank2b, 1) > 0 THEN
                CALL public.process_rank(rank2b, current_order, assigned_tickets, v_result);
                IF v_result THEN
                    allocated_order_ids := array_append(allocated_order_ids, current_order);
                    CONTINUE;
                END IF;
            END IF;

            -- Try rank2c
            IF array_length(rank2c, 1) > 0 THEN
                CALL public.process_rank(rank2c, current_order, assigned_tickets, v_result);
                IF v_result THEN
                    allocated_order_ids := array_append(allocated_order_ids, current_order);
                    CONTINUE;
                END IF;
            END IF;

            -- Try rank2d
            IF array_length(rank2d, 1) > 0 THEN
                CALL public.process_rank(rank2d, current_order, assigned_tickets, v_result);
                IF v_result THEN
                    allocated_order_ids := array_append(allocated_order_ids, current_order);
                    CONTINUE;
                END IF;
            END IF;

            -- If none succeeded, order remains unassigned
            RAISE NOTICE 'Order % could not be assigned to any rank', current_order;
        END LOOP;
    END IF;

    -- Remove allocated orders from input orders array
    CALL public.delete_allocated_orders(allocated_order_ids, custom_orders);

    RAISE NOTICE 'Assigned tickets: %', assigned_tickets;
    RAISE NOTICE 'Remaining Order IDs after VDTA: %', custom_orders;
END;
$BODY$;