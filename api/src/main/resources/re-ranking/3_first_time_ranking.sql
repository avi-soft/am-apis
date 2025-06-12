CREATE OR REPLACE PROCEDURE public.first_time_ranking(
    INOUT professional_service_providers BIGINT[],
    INOUT individual_service_providers BIGINT[]
)
LANGUAGE plpgsql
AS $$
DECLARE
    sp_id BIGINT;
    available_service_providers BIGINT[] := '{}';
    updated_professionals BIGINT[] := '{}';
    updated_individuals BIGINT[] := '{}';
BEGIN

	RAISE NOTICE '3. First Time Ranking';

--		Now Fetch All the Service provider whose admin_overridden is 0 and eligible is 0 order by skill test score
		CALL public.get_service_providers_not_admin_overridden_nor_eligibe_order_by_skill_test(available_service_providers);

		RAISE NOTICE 'First Time Ranking Service Providers: %', available_service_providers;

		call public.bifurcate_available_service_providers_for_ranking(professional_service_providers, individual_service_providers, available_service_providers);

		RAISE NOTICE 'First Time Ranking- Individual Service Provider: %', individual_service_providers;
		RAISE NOTICE 'First Time Ranking- Professional Service Provider: %', professional_service_providers;

		call public.bifurcate_and_update_rank_of_service_providers(updated_professionals, professional_service_providers, true);
		call public.bifurcate_and_update_rank_of_service_providers(updated_individuals, individual_service_providers, false);

		RAISE NOTICE 'Updated First Time Professional Service Providers: %', updated_professionals;
		RAISE NOTICE 'Updated First Time Individual Service Providers: %', updated_individuals;

		RAISE NOTICE 'Re-ranking of first time service provider is completed';

END;
$$;
