CREATE OR REPLACE PROCEDURE public.update_service_providers_eligibility()
LANGUAGE 'plpgsql'
AS $BODY$

DECLARE
    sp_record RECORD;
    sp_ids BIGINT[];
    total_service_providers INT := 0;
BEGIN

	RAISE NOTICE '2. Update Service Providers eligibility';

    FOR sp_record IN
        SELECT sp.service_provider_id, sp."type", sp.ticket_completed, elig.is_eligible_for_re_ranking
        FROM public.service_provider sp
        JOIN public.service_provider_re_ranking_eligibility elig
            ON sp.service_provider_id = elig.service_provider_id
        WHERE elig.is_admin_overridden = false
          AND sp."type" IS NOT null
          AND sp.approved = true
          AND sp.archived = false
          AND sp.role = 4
    LOOP

	    IF sp_record."type" = 'PROFESSIONAL' THEN
		    -- Treat NULL as false
		    IF sp_record.is_eligible_for_re_ranking IS NULL THEN
		        UPDATE public.service_provider_re_ranking_eligibility
		        SET is_eligible_for_re_ranking = false
		        WHERE service_provider_id = sp_record.service_provider_id;

		        continue;
		    END IF;

		    -- Always validate eligibility against ticket_completed
		    IF sp_record.ticket_completed IS NOT NULL AND sp_record.ticket_completed > 10 THEN
		        UPDATE public.service_provider_re_ranking_eligibility
		        SET is_eligible_for_re_ranking = true
		        WHERE service_provider_id = sp_record.service_provider_id;
		    ELSE
		        UPDATE public.service_provider_re_ranking_eligibility
		        SET is_eligible_for_re_ranking = false
		        WHERE service_provider_id = sp_record.service_provider_id;
		    END IF;

--		    RAISE NOTICE 'PROFESSIONAL !!';

		ELSIF sp_record."type" = 'INDIVIDUAL' THEN
		    -- Treat NULL as false
		    IF sp_record.is_eligible_for_re_ranking IS NULL THEN
		        UPDATE public.service_provider_re_ranking_eligibility
		        SET is_eligible_for_re_ranking = false
		        WHERE service_provider_id = sp_record.service_provider_id;

		        continue;
		    END IF;

		    -- Always validate eligibility against ticket_completed
		    IF sp_record.ticket_completed IS NOT NULL AND sp_record.ticket_completed > 4 THEN
		        UPDATE public.service_provider_re_ranking_eligibility
		        SET is_eligible_for_re_ranking = true
		        WHERE service_provider_id = sp_record.service_provider_id;
		    ELSE
		        UPDATE public.service_provider_re_ranking_eligibility
		        SET is_eligible_for_re_ranking = false
		        WHERE service_provider_id = sp_record.service_provider_id;
		    END IF;

--		    RAISE NOTICE 'INDIVIDUAL !!';

		ELSE
		    RAISE NOTICE 'Unknown type for service_provider_id %', sp_record.service_provider_id;
		END IF;
       total_service_providers := total_service_providers + 1;
    END LOOP;

    RAISE NOTICE 'Total Service Providers: %', total_service_providers;
END;
$BODY$;
