CREATE OR REPLACE PROCEDURE public.process_rank_for_tickets(INOUT ranked_service_provider_ids bigint[], IN v_ticket_id bigint, IN order_id bigint, IN ticket_type_id bigint, IN customer_id bigint, INOUT assigned boolean, INOUT assigned_ticket_ids bigint[])
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
	is_review_ticket BOOLEAN := FALSE;
	is_primary_ticket BOOLEAN := FALSE;
BEGIN
	RAISE NOTICE '8. Process Rank For Tickets';

	-- Determine if it's a review or primary ticket
	IF ticket_type_id = 2 THEN
		is_review_ticket := TRUE;

		SELECT parent_ticket_id INTO v_parent_ticket_id
		FROM custom_service_provider_ticket
		WHERE ticket_id = v_ticket_id;

		SELECT assignee_user_id INTO parent_assignee_id
		FROM custom_service_provider_ticket
		WHERE ticket_id = v_parent_ticket_id;
	ELSE
		is_primary_ticket := TRUE;
	END IF;

	-- Iterate through ranked providers
	FOREACH sp_id IN ARRAY ranked_service_provider_ids LOOP
		-- Fetch SP capacity
		SELECT COALESCE(sp.maximum_ticket_size, spr.maximum_ticket_size),
               sp.ticket_assigned,
               sp.ticket_pending
        INTO sp_max_ticket_size, sp_ticket_assigned, sp_ticket_pending
        FROM service_provider sp
        LEFT JOIN service_provider_rank_mapping m ON sp.service_provider_id = m.service_provider_id
        LEFT JOIN service_provider_rank spr ON m.rank_id = spr.rank_id
        WHERE sp.service_provider_id = sp_id;

		-- Skip if max ticket size not set
		IF sp_max_ticket_size IS NULL OR sp_max_ticket_size = 0 THEN
			CONTINUE;
		END IF;

		-- Calculate bandwidth
		bandwidth := ((sp_ticket_assigned + sp_ticket_pending) * 100.0) / sp_max_ticket_size;

		-- Skip if assigning review ticket to parent assignee
		IF is_review_ticket AND sp_id = parent_assignee_id THEN
			CONTINUE;
		END IF;

		-- Try to reassign ticket
		CALL reallocate_ticket(
			order_id,
			sp_id,
			v_ticket_id,
			ticket_type_id,
			customer_id,
			is_review_ticket,
			is_primary_ticket,
			assigned,
			assigned_ticket_ids
		);

		IF assigned THEN
	       
			-- Check bandwidth again and remove only if full
			IF bandwidth >= 100.0 THEN
				ranked_service_provider_ids := array_remove(ranked_service_provider_ids, sp_id);
			END IF;

			RETURN;
		END IF;

		-- Remove SP from pool if bandwidth is already full
		IF bandwidth >= 100.0 THEN
			ranked_service_provider_ids := array_remove(ranked_service_provider_ids, sp_id);
		END IF;
	END LOOP;
END;
$BODY$;