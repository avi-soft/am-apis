DO $$
DECLARE
  total_assigned_tickets BIGINT[];
BEGIN
  CALL public.auto_assigner_procedure(total_assigned_tickets);
  RAISE NOTICE 'Assigned Tickets: %', array_length(total_assigned_tickets, 1);
END $$;
