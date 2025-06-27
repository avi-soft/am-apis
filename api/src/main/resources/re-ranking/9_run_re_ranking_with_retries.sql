CREATE OR REPLACE PROCEDURE public.run_re_ranking_with_retries()
LANGUAGE plpgsql
AS $$
DECLARE
    max_retries INT := 3;
    retry_count INT := 0;
    delay_seconds INT := 300;
    success BOOLEAN := FALSE;
BEGIN
    RAISE NOTICE 'Starting Re-Ranking With Retries';

    WHILE retry_count < max_retries LOOP
        BEGIN
            -- Call the actual re-ranking procedure
            CALL public.re_ranking_procedure();

            -- Mark as success and exit the loop
            success := TRUE;
            EXIT;

        EXCEPTION WHEN OTHERS THEN
            -- Log the failure
            RAISE WARNING 'Re-Ranking attempt % failed: %', retry_count + 1, SQLERRM;

            -- Delay before next attempt
            PERFORM pg_sleep(delay_seconds);

            -- Increment retry count
            retry_count := retry_count + 1;
        END;
    END LOOP;

    IF NOT success THEN
        RAISE EXCEPTION 'Re-Ranking failed after % retries.', max_retries;
    END IF;

    RAISE NOTICE 'Re-Ranking procedure completed successfully.';
END;
$$;
