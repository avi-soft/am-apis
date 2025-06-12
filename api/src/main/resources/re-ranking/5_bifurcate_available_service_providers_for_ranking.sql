CREATE OR REPLACE PROCEDURE public.bifurcate_available_service_providers_for_ranking(
    INOUT professional_service_providers BIGINT[],
    INOUT individual_service_providers BIGINT[],
    IN available_service_providers BIGINT[]
)
LANGUAGE plpgsql
AS $$
DECLARE
    sp_id BIGINT;
    sp_type TEXT;
BEGIN
    professional_service_providers := '{}';
    individual_service_providers := '{}';

    RAISE NOTICE '5. Bifurcating service providers for first-time ranking...';

    FOREACH sp_id IN ARRAY available_service_providers
    LOOP
        SELECT "type"
        INTO sp_type
        FROM public.service_provider
        WHERE service_provider_id = sp_id;

        IF sp_type = 'PROFESSIONAL' THEN
            professional_service_providers := array_append(professional_service_providers, sp_id);
        ELSIF sp_type = 'INDIVIDUAL' THEN
            individual_service_providers := array_append(individual_service_providers, sp_id);
        ELSE
            RAISE NOTICE 'Unknown service provider type for ID %', sp_id;
        END IF;
    END LOOP;

    RAISE NOTICE 'Professional SPs: %', array_length(professional_service_providers, 1);
    RAISE NOTICE 'Individual SPs: %', array_length(individual_service_providers, 1);
END;
$$;

