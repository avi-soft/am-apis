-- PROCEDURE: public.vertical_distribution_ticket_allocation(bigint[], bigint[])

-- DROP PROCEDURE IF EXISTS public.vertical_distribution_ticket_allocation(bigint[], bigint[]);

CREATE OR REPLACE PROCEDURE public.vertical_distribution_ticket_allocation(
	INOUT custom_orders bigint[],
	INOUT assigned_tickets bigint[])
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    -- Declare variables for service provider ranks
    rank1a BIGINT[];
    rank1b BIGINT[];
    rank1c BIGINT[];
    rank1d BIGINT[];
    rank2a BIGINT[];
    rank2b BIGINT[];
    rank2c BIGINT[];
    rank2d BIGINT[];
 	allocated_order_ids BIGINT[];
	order_ids BIGINT[];
    -- Declare a variable to hold the current order
    current_order BIGINT;

    -- Declare a variable to hold the service provider ID
    service_provider_id BIGINT;

    -- Declare a variable to hold the assigned ticket
    assigned_ticket BIGINT;

    v_assigned_tickets BIGINT[];  -- Declare output variable for assigned tickets
    v_result BOOLEAN;             -- Declare output variable for result

BEGIN
    -- Log the start of the procedure
	RAISE NOTICE 'Order ids before VDTA orders: %',custom_orders;
    RAISE NOTICE 'Vertical Distribution Ticket Allocation Called';
    RAISE NOTICE 'Total orders received for VDTA: %', array_length(custom_orders, 1);

    -- Call the bifurcate_available_service_providers procedure to get the service providers by rank
    CALL public.bifurcate_available_service_providers(
        rank1a, rank1b, rank1c, rank1d, rank2a, rank2b, rank2c, rank2d
    );

    -- Log the number of service providers in each rank
    RAISE NOTICE 'Service Providers in rank1a: %', array_length(rank1a, 1);
    RAISE NOTICE 'Service Providers in rank1b: %', array_length(rank1b, 1);
    RAISE NOTICE 'Service Providers in rank1c: %', array_length(rank1c, 1);
    RAISE NOTICE 'Service Providers in rank1d: %', array_length(rank1d, 1);
    RAISE NOTICE 'Service Providers in rank2a: %', array_length(rank2a, 1);
    RAISE NOTICE 'Service Providers in rank2b: %', array_length(rank2b, 1);
    RAISE NOTICE 'Service Providers in rank2c: %', array_length(rank2c, 1);
    RAISE NOTICE 'Service Providers in rank2d: %', array_length(rank2d, 1);

    -- Loop through each order in the custom_orders array
    -- Loop through each order in the custom_orders array
IF custom_orders IS NOT NULL THEN
FOR i IN 1..array_length(custom_orders, 1) LOOP
    -- Set the current order
    current_order := custom_orders[i];

    -- Check if rank1a has service providers
    IF rank1a IS NOT NULL AND array_length(rank1a, 1) > 0 THEN
        -- Call the procedure with an array of service provider IDs and an order ID
        CALL public.process_rank(
            rank1a,                -- Service provider IDs (array)
            current_order,         -- Order ID
            v_assigned_tickets,    -- Output parameter for assigned tickets
            v_result               -- Output parameter for result
        );
        RAISE NOTICE 'Processed by rank 1a';

        -- If result is false, continue with the next order
        IF v_result THEN
			allocated_order_ids :=array_append(allocated_order_ids,custom_orders[i]);
            CONTINUE;
        END IF;
    END IF;  -- Closing the IF for rank1a

    IF rank1b IS NOT NULL AND array_length(rank1b, 1) > 0 THEN
        -- Call the procedure with an array of service provider IDs and an order ID
        CALL public.process_rank(
            rank1b,                -- Service provider IDs (array)
            current_order,         -- Order ID
            v_assigned_tickets,    -- Output parameter for assigned tickets
            v_result               -- Output parameter for result
        );
        RAISE NOTICE 'Processed by rank 1b';

        -- If result is false, continue with the next order
        IF v_result THEN
			allocated_order_ids :=array_append(allocated_order_ids,custom_orders[i]);
            CONTINUE;
        END IF;
    END IF;  -- Closing the IF for rank1b

    IF rank1c IS NOT NULL AND array_length(rank1c, 1) > 0 THEN
        -- Call the procedure with an array of service provider IDs and an order ID
        CALL public.process_rank(
            rank1c,                -- Service provider IDs (array)
            current_order,         -- Order ID
            v_assigned_tickets,    -- Output parameter for assigned tickets
            v_result               -- Output parameter for result
        );
        RAISE NOTICE 'Processed by rank 1c';

        -- If result is false, continue with the next order
        IF v_result THEN
			allocated_order_ids :=array_append(allocated_order_ids,custom_orders[i]);
            CONTINUE;
        END IF;
    END IF;  -- Closing the IF for rank1c

    IF rank1d IS NOT NULL AND array_length(rank1d, 1) > 0 THEN
        -- Call the procedure with an array of service provider IDs and an order ID
        CALL public.process_rank(
            rank1d,                -- Service provider IDs (array)
            current_order,         -- Order ID
            v_assigned_tickets,    -- Output parameter for assigned tickets
            v_result               -- Output parameter for result
        );
        RAISE NOTICE 'Processed by rank 1d';

        -- If result is false, continue with the next order
        IF v_result THEN
			allocated_order_ids :=array_append(allocated_order_ids,custom_orders[i]);
            CONTINUE;
        END IF;
    END IF;  -- Closing the IF for rank1d

    IF rank2a IS NOT NULL AND array_length(rank2a, 1) > 0 THEN
        -- Call the procedure with an array of service provider IDs and an order ID
        CALL public.process_rank(
            rank2a,                -- Service provider IDs (array)
            current_order,         -- Order ID
            v_assigned_tickets,    -- Output parameter for assigned tickets
            v_result               -- Output parameter for result
        );
        RAISE NOTICE 'Processed by rank 2a';

        -- If result is false, continue with the next order
        IF v_result THEN
			allocated_order_ids :=array_append(allocated_order_ids,custom_orders[i]);
            CONTINUE;
        END IF;
    END IF;  -- Closing the IF for rank2a

    IF rank2b IS NOT NULL AND array_length(rank2b, 1) > 0 THEN
        -- Call the procedure with an array of service provider IDs and an order ID
        CALL public.process_rank(
            rank2b,                -- Service provider IDs (array)
            current_order,         -- Order ID
            v_assigned_tickets,    -- Output parameter for assigned tickets
            v_result               -- Output parameter for result
        );
        RAISE NOTICE 'Processed by rank 2b';

        -- If result is false, continue with the next order
        IF v_result THEN
			allocated_order_ids :=array_append(allocated_order_ids,custom_orders[i]);
            CONTINUE;
        END IF;
    END IF;  -- Closing the IF for rank2b

    IF rank2c IS NOT NULL AND array_length(rank2c, 1) > 0 THEN
        -- Call the procedure with an array of service provider IDs and an order ID
        CALL public.process_rank(
            rank2c,                -- Service provider IDs (array)
            current_order,         -- Order ID
            v_assigned_tickets,    -- Output parameter for assigned tickets
            v_result               -- Output parameter for result
        );
        RAISE NOTICE 'Processed by rank 2c';

        -- If result is false, continue with the next order
        IF v_result THEN
			allocated_order_ids :=array_append(allocated_order_ids,custom_orders[i]);
            CONTINUE;
        END IF;
    END IF;  -- Closing the IF for rank2c

    IF rank2d IS NOT NULL AND array_length(rank2d, 1) > 0 THEN
        -- Call the procedure with an array of service provider IDs and an order ID
        CALL public.process_rank(
            rank2d,                -- Service provider IDs (array)
            current_order,         -- Order ID
            v_assigned_tickets,    -- Output parameter for assigned tickets
            v_result               -- Output parameter for result
        );
        RAISE NOTICE 'Processed by rank 2d';

        -- If result is false, continue with the next order
        IF v_result THEN
			allocated_order_ids :=array_append(allocated_order_ids,custom_orders[i]);
            CONTINUE;
        END IF;
    END IF;  -- Closing the IF for rank2d

END LOOP;  -- Closing the FOR LOOP
END IF;
-- Assign the result to the output parameter (assigned_tickets)
assigned_tickets := v_assigned_tickets;

-- Log the final assigned tickets
RAISE NOTICE 'Assigned tickets: %, Result: %', assigned_tickets, v_result;

    -- Assign the result to the output parameter (assigned_tickets)
    assigned_tickets := v_assigned_tickets;

    -- Log the final assigned tickets
    RAISE NOTICE 'Assigned tickets: %, Result: %', assigned_tickets, v_result;

	CALL public.delete_allocated_orders(
    allocated_order_ids, -- Array of allocated orders to remove
    custom_orders      -- Array of order IDs to modify
	);
    custom_orders := order_ids;
    RAISE NOTICE 'Order ids after VDTA orders: %',custom_orders;
END;
$BODY$;
ALTER PROCEDURE public.vertical_distribution_ticket_allocation(bigint[], bigint[])
    OWNER TO postgres;
