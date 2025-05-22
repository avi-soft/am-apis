CREATE OR REPLACE PROCEDURE bifurcate_available_service_providers()
LANGUAGE plpgsql
AS $$
DECLARE
    sp RECORD;
BEGIN
    -- Loop through all available service providers
    FOR sp IN SELECT * FROM available_service_providers LOOP
        BEGIN
            -- Insert into appropriate rank queue table based on rank name
            CASE sp.rank_name
                WHEN '1a' THEN
                    INSERT INTO rank1a_queue VALUES (sp.*);
                WHEN '1b' THEN
                    INSERT INTO rank1b_queue VALUES (sp.*);
                WHEN '1c' THEN
                    INSERT INTO rank1c_queue VALUES (sp.*);
                WHEN '1d' THEN
                    INSERT INTO rank1d_queue VALUES (sp.*);
                WHEN '2a' THEN
                    INSERT INTO rank2a_queue VALUES (sp.*);
                WHEN '2b' THEN
                    INSERT INTO rank2b_queue VALUES (sp.*);
                WHEN '2c' THEN
                    INSERT INTO rank2c_queue VALUES (sp.*);
                WHEN '2d' THEN
                    INSERT INTO rank2d_queue VALUES (sp.*);
                ELSE
                    -- Optional: log or handle unrecognized rank
                    RAISE NOTICE 'Unrecognized rank: %', sp.rank_name;
            END CASE;

        EXCEPTION WHEN OTHERS THEN
            -- You can replace this with a call to an exception handler procedure
            RAISE EXCEPTION 'Exception during bifurcation: %', SQLERRM;
        END;
    END LOOP;
END;
$$;
