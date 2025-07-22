CREATE OR REPLACE PROCEDURE public.get_service_providers_not_admin_overridden_nor_eligibe_order_by_skill_test(
    INOUT service_provider_ids BIGINT[]
)
LANGUAGE plpgsql
AS $$
DECLARE
    sp_id BIGINT;
BEGIN

	raise notice '4. Get Service Providers which are not Admin Overridden Nor Eligible and are approved order by skill test';
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
          AND elig.is_eligible_for_re_ranking = false
          AND sp.role = 4
          AND sp.approved = true
          AND sp.archived = false
        ORDER BY COALESCE(sp.writtentestscore, 0) + COALESCE(sp.imageuploadscore, 0) DESC,
                 sp.date_joined ASC  -- or DESC
    LOOP
        service_provider_ids := array_append(service_provider_ids, sp_id);
    END LOOP;

    RAISE NOTICE 'Total SPs fetched for first-time ranking: %', array_length(service_provider_ids, 1);
   END;
$$;
