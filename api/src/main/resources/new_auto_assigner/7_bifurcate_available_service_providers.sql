CREATE OR REPLACE PROCEDURE public.bifurcate_available_service_providers(IN available_service_provider_ids bigint[], OUT rank1a bigint[], OUT rank1b bigint[], OUT rank1c bigint[], OUT rank1d bigint[], OUT rank2a bigint[], OUT rank2b bigint[], OUT rank2c bigint[], OUT rank2d bigint[])
LANGUAGE 'plpgsql' 
AS $BODY$

DECLARE
    service_provider_record RECORD;
BEGIN

	RAISE NOTICE '7. Bifurcate Available Service Provider';

--     Initialize output arrays
    rank1a := ARRAY[]::BIGINT[];
    rank1b := ARRAY[]::BIGINT[];
    rank1c := ARRAY[]::BIGINT[];
    rank1d := ARRAY[]::BIGINT[];
    rank2a := ARRAY[]::BIGINT[];
    rank2b := ARRAY[]::BIGINT[];
    rank2c := ARRAY[]::BIGINT[];
    rank2d := ARRAY[]::BIGINT[];
  
--     Iterate through filtered service providers
    FOR service_provider_record IN
        SELECT sp.service_provider_id, spr.rank_name, spr.rank_id
            FROM service_provider sp
            JOIN service_provider_rank_mapping m ON sp.service_provider_id = m.service_provider_id
            JOIN service_provider_rank spr ON m.rank_id = spr.rank_id
            WHERE sp.service_provider_id = ANY(available_service_provider_ids)
              AND sp.is_active = TRUE
    LOOP
        CASE service_provider_record.rank_id
            WHEN 1 THEN rank1a := array_append(rank1a, service_provider_record.service_provider_id);
            WHEN 2 THEN rank1b := array_append(rank1b, service_provider_record.service_provider_id);
            WHEN 3 THEN rank1c := array_append(rank1c, service_provider_record.service_provider_id);
            WHEN 4 THEN rank1d := array_append(rank1d, service_provider_record.service_provider_id);
            WHEN 5 THEN rank2a := array_append(rank2a, service_provider_record.service_provider_id);
            WHEN 6 THEN rank2b := array_append(rank2b, service_provider_record.service_provider_id);
            WHEN 7 THEN rank2c := array_append(rank2c, service_provider_record.service_provider_id);
            WHEN 8 THEN rank2d := array_append(rank2d, service_provider_record.service_provider_id);
            ELSE
                RAISE NOTICE 'Unrecognized rank: % for service_provider_id: %', service_provider_record.rank_name, service_provider_record.service_provider_id;
        END CASE;
    END LOOP;
END;
$BODY$;