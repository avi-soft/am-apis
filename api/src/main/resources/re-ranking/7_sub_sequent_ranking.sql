CREATE OR REPLACE PROCEDURE public.subsequent_ranking(
    INOUT professional_service_providers BIGINT[],
    INOUT individual_service_providers BIGINT[]
)
LANGUAGE plpgsql
AS $$
DECLARE
    sp_id BIGINT;
    available_service_providers BIGINT[] := '{}';
    subsequent_ranking_service_providers BIGINT[] := '{}';
    updated_professionals BIGINT[] := '{}';
    updated_individuals BIGINT[] := '{}';
BEGIN

	RAISE NOTICE '7. Subsequent Ranking';

--		Now Fetch All the Service provider whose admin_overridden is 0 and eligible is 1 order by ticket completed
		CALL public.get_service_providers_not_admin_overridden_and_eligibe_order_by_ticket_completed(available_service_providers);

		RAISE NOTICE 'Subsequent Ranking Service Providers: %', available_service_providers;

		call public.bifurcate_available_service_providers_for_ranking(professional_service_providers, individual_service_providers, available_service_providers);

		RAISE NOTICE 'Subsequent Ranking- Individual Service Provider: %', individual_service_providers;
		RAISE NOTICE 'Subsequent Ranking- Professional Service Provider: %', professional_service_providers;

		call public.bifurcate_and_update_rank_of_service_providers(updated_professionals, professional_service_providers, true);
		call public.bifurcate_and_update_rank_of_service_providers(updated_individuals, individual_service_providers, false);

		RAISE NOTICE 'Updated Subsequent Professional Service Providers: %', updated_professionals;
		RAISE NOTICE 'Updated Subsequent Individual Service Providers: %', updated_individuals;

		RAISE NOTICE 'Re-ranking of sub-sequent service provider is completed';

END;
$$;