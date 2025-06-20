CREATE OR REPLACE PROCEDURE public.run_auto_assigner_with_retries()
LANGUAGE plpgsql
AS $$
DECLARE
    max_retries INT := 3;
    retry_count INT := 0;
    delay_seconds INT := 3;
    success BOOLEAN := FALSE;
   v_total_assigned_tickets BIGINT[];
BEGIN

	RAISE NOTICE '19. Run Auto Assigner With Retries';
    WHILE retry_count < max_retries LOOP
        BEGIN
            -- Call your actual auto-assigner procedure
            CALL public.auto_assigner_procedure(v_total_assigned_tickets);

            -- If no exception was thrown, mark success and exit
            success := TRUE;
            EXIT;

        EXCEPTION WHEN OTHERS THEN
            -- Log the failure
            RAISE WARNING 'Attempt % failed: %', retry_count + 1, SQLERRM;

            -- Wait for delay_seconds before retrying
            PERFORM pg_sleep(delay_seconds);

            -- Increment retry counter
            retry_count := retry_count + 1;
        END;
    END LOOP;

    IF NOT success THEN
        RAISE EXCEPTION 'Auto-assigner failed after % retries.', max_retries;
    END IF;
END;
$$;
