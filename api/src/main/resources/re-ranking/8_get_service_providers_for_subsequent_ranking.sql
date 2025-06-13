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
        SELECT service_provider_id
        FROM public.service_provider
        WHERE is_admin_overridden = false
          AND (is_eligible_for_re_ranking = true)
          AND role = 4
          and approved = true
        ORDER BY COALESCE(review_ticket_status_score, 0) + COALESCE(review_ticket_feedback_score, 0) + COALESCE(time_completion_score, 0) desc,
        date_joined ASC  -- or DESC based on your preference
    LOOP
        service_provider_ids := array_append(service_provider_ids, sp_id);
    END LOOP;

    RAISE NOTICE 'Total SPs fetched for subsequent ranking: %', array_length(service_provider_ids, 1);
   END;
$$;