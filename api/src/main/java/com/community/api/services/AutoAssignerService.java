package com.community.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigInteger;
import java.sql.Types;
import java.util.Arrays;
import java.util.Map;

@Service
public class AutoAssignerService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AutoAssignerService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Call the stored procedure and get the result
    public Long[] callAutoAssigner(BigInteger[] customOrders, BigInteger[] totalAssignedTickets) {
        // Convert BigInteger[] to Long[]
        Long[] customOrdersLong = Arrays.stream(customOrders)
                .map(BigInteger::longValue)
                .toArray(Long[]::new);

        Long[] totalAssignedTicketsLong = Arrays.stream(totalAssignedTickets)
                .map(BigInteger::longValue)
                .toArray(Long[]::new);

        // Create a procedure object for our stored procedure
        AutoAssignerProcedure procedure = new AutoAssignerProcedure(jdbcTemplate.getDataSource());

        // Call the procedure and get the result
        Map<String, Object> result = procedure.execute(customOrdersLong, totalAssignedTicketsLong);

        // Return the result (the assigned tickets)
        return (Long[]) result.get("total_assigned_tickets");
    }

    // StoredProcedure class to define the procedure and its parameters
    private static class AutoAssignerProcedure extends StoredProcedure {

        AutoAssignerProcedure(DataSource dataSource) {
            super(dataSource, "public.auto_assigner");  // Specify the procedure name

            // Declare the parameters: one input and one output
            declareParameter(new SqlParameter("custom_orders", Types.ARRAY));
            declareParameter(new SqlParameter("total_assigned_tickets", Types.ARRAY));
        }

        // Execute the stored procedure
        public Map<String, Object> execute(Long[] customOrders, Long[] totalAssignedTicketsLong) {
          return super.execute(customOrders, totalAssignedTicketsLong);
        }
    }
}
