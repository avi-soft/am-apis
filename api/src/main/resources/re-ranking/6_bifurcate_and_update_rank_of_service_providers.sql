CREATE OR REPLACE PROCEDURE public.bifurcate_and_update_rank_of_service_providers(
    INOUT updated_service_providers BIGINT[],
    IN available_service_providers BIGINT[],
    IN is_professional BOOLEAN
)
LANGUAGE plpgsql
AS $$
DECLARE
    sp_record RECORD;
    total_count INT;
    group_size INT;
    rank_offset INT := 0;
    current_rank INT;
    counter INT := 0;
    existing_rank INT;
    sp_id BIGINT;
begin

    RAISE NOTICE '6. Bifurcate and update Rank of service provider';

    -- Determine rank offset based on type
    IF is_professional THEN
        rank_offset := 0;  -- ranks 1-4
    ELSE
        rank_offset := 4;  -- ranks 5-8
    END IF;

    -- Calculate total and group size
    SELECT COUNT(*) INTO total_count
    FROM service_provider
    WHERE service_provider_id = ANY(available_service_providers);

    IF total_count = 0 THEN
        RAISE NOTICE 'No service providers to rank.';
        RETURN;
    END IF;

    group_size := CEIL(total_count / 4.0);  -- dividing into 4 groups

    -- Iterate available service provider

    FOREACH sp_id IN ARRAY available_service_providers
    LOOP
        current_rank := (counter / group_size) + 1 + rank_offset;

        -- Fetch current rank
        SELECT rank_id INTO existing_rank
        FROM service_provider_rank_mapping
        WHERE service_provider_id = sp_id;

        -- Only update if rank has changed
        IF existing_rank IS DISTINCT FROM current_rank THEN
            UPDATE service_provider_rank_mapping
            SET rank_id = current_rank
            WHERE service_provider_id = sp_id;

            updated_service_providers := array_append(updated_service_providers, sp_id);
        END IF;

        counter := counter + 1;
    END LOOP;

    RAISE NOTICE 'Assigned ranks to % service providers', counter;
END;
$$;