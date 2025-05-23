-- PROCEDURE: public.process_rank_for_tickets(bigint[], bigint, bigint, bigint, bigint, boolean, bigint[])

-- DROP PROCEDURE IF EXISTS public.process_rank_for_tickets(bigint[], bigint, bigint, bigint, bigint, boolean, bigint[]);

CREATE OR REPLACE PROCEDURE public.process_rank_for_tickets(
	INOUT ranked_service_provider_ids bigint[],
	IN v_ticket_id bigint,
	IN order_id bigint,
	IN ticket_type_id bigint,
	IN customer_id bigint,
	INOUT assigned boolean,
	INOUT assigned_ticket_ids bigint[])
LANGUAGE 'plpgsql'
AS $BODY$

DECLARE
	sp_id BIGINT;
	sp_max_ticket_size INTEGER;
	sp_ticket_assigned INTEGER;
	sp_ticket_pending INTEGER;
	bandwidth NUMERIC;
	parent_assignee_id BIGINT;
	v_parent_ticket_id BIGINT;
begin

	RAISE NOTICE '15. Process Rank For Tickets';

	-- Fetch parent assignee only if review ticket
	IF ticket_type_id = 2 THEN
		SELECT parent_ticket_id INTO v_parent_ticket_id
		FROM custom_service_provider_ticket
		WHERE parent_ticket_id = v_ticket_id;

		SELECT assignee_user_id INTO parent_assignee_id
		FROM custom_service_provider_ticket
		WHERE ticket_id = v_parent_ticket_id;
	END IF;

	-- Iterate through ranked providers
	FOREACH sp_id IN ARRAY ranked_service_provider_ids LOOP
		-- Skip if bandwidth is 100%
		SELECT COALESCE(sp.maximum_ticket_size, spr.maximum_ticket_size),
			   sp.ticket_assigned,
			   sp.ticket_pending
		INTO sp_max_ticket_size, sp_ticket_assigned, sp_ticket_pending
		FROM service_provider sp
		LEFT JOIN service_provider_rank spr ON sp.rank_id = spr.rank_id
		WHERE sp.service_provider_id = sp_id;

		IF sp_max_ticket_size IS NULL OR sp_max_ticket_size = 0 THEN
			CONTINUE;
		END IF;

		bandwidth := ((sp_ticket_assigned + sp_ticket_pending) * 100.0) / sp_max_ticket_size;

		IF bandwidth >= 100.0 THEN
			CONTINUE;
		END IF;

		-- Skip if assigning review ticket to parent assignee
		IF ticket_type_id = 2 AND sp_id = parent_assignee_id THEN
			CONTINUE;
		END IF;

		-- Try to assign ticket
		CALL allocate_ticket(order_id, sp_id, assigned, assigned_ticket_ids);

		IF assigned THEN
			-- Update service provider stats
			UPDATE service_provider
			SET ticket_assigned = ticket_assigned + 1
			WHERE service_provider_id = sp_id;

			-- Remove SP from pool
			ranked_service_provider_ids := array_remove(ranked_service_provider_ids, sp_id);

			RETURN;
		END IF;
	END LOOP;
END;
$BODY$;
ALTER PROCEDURE public.process_rank_for_tickets(bigint[], bigint, bigint, bigint, bigint, boolean, bigint[])
    OWNER TO postgres;
