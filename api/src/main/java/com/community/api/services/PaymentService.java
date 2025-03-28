package com.community.api.services;

import com.community.api.component.JwtUtil;
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
    public double[] balances(Long spId)
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
    }
}
