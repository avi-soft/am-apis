package com.community.api.services;

import com.community.api.component.JwtUtil;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import org.springframework.stereotype.Service;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
    private final EntityManager entityManager;


    // Constructor Injection
    public PaymentService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    /*public double[] balances(Long spId)
    {
        double[]result=new double[3];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate currentDate = LocalDate.now();
        String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        LocalDate date = LocalDate.parse(formattedDate, formatter);

        LocalDate firstOfMonth = date.withDayOfMonth(1);

        System.out.println("date"+firstOfMonth);

        Query queryAmount = entityManager.createNativeQuery("SELECT COALESCE(SUM(e.pending), 0) FROM Earnings e WHERE e.date < :inputDate AND e.provider_id = :id AND settled = false");
        queryAmount.setParameter("inputDate", java.sql.Date.valueOf(firstOfMonth));
        queryAmount.setParameter("id", spId);
        Double lastMonth = (Double) queryAmount.getSingleResult();
        Query queryAmountCarryOver = entityManager.createNativeQuery("SELECT COALESCE(SUM(e.carry_over), 0)  FROM Earnings e WHERE e.date < :inputDate AND e.provider_id = :id AND settled = true");
        queryAmountCarryOver.setParameter("inputDate", java.sql.Date.valueOf(firstOfMonth));
        queryAmountCarryOver.setParameter("id", spId);
        Double lastMonthCarryOver=0.0;
        try {
            lastMonthCarryOver = (Double) queryAmountCarryOver.getSingleResult();
        }catch (Exception e)
        {
            lastMonthCarryOver=0.0;
        }
        queryAmount=entityManager.createNativeQuery("SELECT COALESCE(SUM(e.pending), 0) FROM Earnings e WHERE e.date >= :inputDate AND e.provider_id = :id AND settled = false");
        queryAmount.setParameter("inputDate", java.sql.Date.valueOf(firstOfMonth));
        queryAmount.setParameter("id", spId);
        Double thisMonth = (Double) queryAmount.getSingleResult();
        Query queryAmountCarryOver2 = entityManager.createNativeQuery("SELECT COALESCE(SUM(e.carry_over), 0)  FROM Earnings e WHERE e.date >=:inputDate AND e.provider_id = :id AND settled = true");
        queryAmountCarryOver2.setParameter("inputDate", java.sql.Date.valueOf(firstOfMonth));
        queryAmountCarryOver2.setParameter("id", spId);
        Double thisMonthCarryOver = 0.0;
        try{
            thisMonthCarryOver=(Double) queryAmountCarryOver2.getSingleResult();
        }catch (Exception e)
        {
            thisMonthCarryOver=0.0;
        }
        Map<String,Object> resultMap=new HashMap<>();
        result[0]=lastMonth+lastMonthCarryOver;
        result[1]=thisMonth+thisMonthCarryOver;
        return result;
    }*/
    public double[] balances(Long spId) {
        double[] result = new double[4]; // [lastMonthPending, thisMonthPending, surplus, availableBalance]

        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);

        // Last month pending earnings (before 1st of this month)
        Query lastMonthQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(e.pending), 0) " +
                        "FROM Earnings e WHERE e.date < :startOfMonth AND e.provider_id = :id AND e.settled = false"
        );
        lastMonthQuery.setParameter("startOfMonth", java.sql.Date.valueOf(firstOfMonth));
        lastMonthQuery.setParameter("id", spId);
        Double lastMonthPending = (Double) lastMonthQuery.getSingleResult();

        // This month pending earnings (from 1st of this month onward)
        Query thisMonthQuery = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(e.pending), 0) " +
                        "FROM Earnings e WHERE e.date >= :startOfMonth AND e.provider_id = :id AND e.settled = false"
        );
        thisMonthQuery.setParameter("startOfMonth", java.sql.Date.valueOf(firstOfMonth));
        thisMonthQuery.setParameter("id", spId);
        Double thisMonthPending = (Double) thisMonthQuery.getSingleResult();

        // Fetch provider surplus (loan)
        ServiceProviderEntity provider = entityManager.find(ServiceProviderEntity.class, spId);
        Double surplus = (provider != null && provider.getSurplus() != null) ? provider.getSurplus() : 0.0;

        // Total pending earnings
        double totalPending = lastMonthPending + thisMonthPending;

        // Adjust available balance by subtracting negative surplus (loan)
        double availableBalance = totalPending;

        if (surplus < 0) {
            availableBalance = totalPending + surplus; // subtract the loan
            if (availableBalance < 0) availableBalance = 0; // can't go negative
        }

        // Assign results
        result[0] = lastMonthPending;
        result[1] = thisMonthPending;
        result[2] = surplus;           // loan (negative if exists)
        result[3] = availableBalance;  // actual usable balance

        return result;
    }


}
