CREATE OR REPLACE PROCEDURE public.re_ranking_procedure(
	)
LANGUAGE 'plpgsql'
AS $BODY$


DECLARE
    total_re_ranking_of_ticket BIGINT[];         		        -- Total service_providers that are re-ranked
    professional_first_time_service_providers BIGINT[] := '{}'; -- first time Ranking of professional service providers
    professional_subsequent_service_providers BIGINT[] := '{}'; -- subsequent re-ranking of professional service providers
    individual_first_time_service_providers BIGINT[] := '{}';   -- first time Ranking of individual service providers
    individual_subsequent_service_providers BIGINT[] := '{}';   -- subsequent re-ranking of individual service providers
BEGIN

	RAISE NOTICE '1. Re-Ranking of Service Providers';

    -- Step 1: Update the eligibilty of all the service providers
    CALL public.update_service_providers_eligibility();

    -- Step 2: First Time Ranking
    CALL public.first_time_ranking(professional_first_time_service_providers, individual_first_time_service_providers);
	RAISE NOTICE 'Professional first time ranked service providers: %', professional_first_time_service_providers;
	RAISE NOTICE 'Individual first time ranked service providers: %', individual_first_time_service_providers;

    -- Step 3: SubSequent Ranking of Service Provider
	CALL public.subsequent_ranking(professional_subsequent_service_providers, individual_subsequent_service_providers);
	RAISE NOTICE 'Professional subsequent ranked service providers: %', professional_subsequent_service_providers;
	RAISE NOTICE 'Individual subsequent ranked service providers: %', individual_subsequent_service_providers;


    RAISE NOTICE 'Total Re-Ranking of Service Providers is Completed ' ;
END;
$BODY$;
