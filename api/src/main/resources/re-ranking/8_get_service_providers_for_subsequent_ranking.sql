CREATE OR REPLACE PROCEDURE public.get_service_providers_not_admin_overridden_and_eligibe_order_by_ticket_completed(
    INOUT service_provider_ids BIGINT[]
)
LANGUAGE plpgsql
AS $$
DECLARE
    sp_id BIGINT;
BEGIN

	RAISE NOTICE '8. Get Service Providers which are not Admin Overridden and Eligible and are approved order by ticket completed';
    service_provider_ids := '{}';  -- Initialize empty array

    FOR sp_id IN
        SELECT sp.service_provider_id
        FROM public.service_provider sp
        JOIN public.service_provider_re_ranking_eligibility elig
            ON sp.service_provider_id = elig.service_provider_id
        JOIN public.service_provider_re_ranking_score score
            ON sp.service_provider_id = score.service_provider_id
        JOIN public.service_provider_rank_mapping rank_map
             ON sp.service_provider_id = rank_map.service_provider_id
        WHERE elig.is_admin_overridden = false
          AND elig.is_eligible_for_re_ranking = true
          AND sp.role = 4
          AND sp.approved = true
          AND sp.archived = false
        ORDER BY
          COALESCE(score.review_ticket_status_score, 0) +
          COALESCE(score.review_ticket_feedback_score, 0) +
          COALESCE(score.time_completion_score, 0) DESC,
          sp.date_joined ASC
    LOOP
        service_provider_ids := array_append(service_provider_ids, sp_id);
    END LOOP;

    RAISE NOTICE 'Total SPs fetched for subsequent ranking: %', array_length(service_provider_ids, 1);
   END;
$$;