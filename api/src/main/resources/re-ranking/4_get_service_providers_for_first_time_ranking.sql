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
        SELECT service_provider_id
        FROM public.service_provider
        WHERE is_admin_overridden = false
          AND (is_eligible_for_re_ranking = false)
          AND role = 4
          and approved = true
        ORDER BY COALESCE(writtentestscore, 0) + COALESCE(imageuploadscore, 0) desc,
        date_joined ASC  -- or DESC based on your preference
    LOOP
        service_provider_ids := array_append(service_provider_ids, sp_id);
    END LOOP;

    RAISE NOTICE 'Total SPs fetched for first-time ranking: %', array_length(service_provider_ids, 1);
   END;
$$;
