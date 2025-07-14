package com.community.api.services;

import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomerReferrer;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;

@Service
public class ReferrerService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<CustomerReferrer> getSortedReferrers(CustomCustomer customer) {
        List<CustomerReferrer> referrers = customer.getMyReferrer();

        referrers.sort((r1, r2) -> {
            // Get the max ticket size from rank if max_ticket_size is not available
            Integer maxTicketSize1 = r1.getServiceProvider().getMaximumTicketSize() != null ? r1.getServiceProvider().getMaximumTicketSize() : r1.getServiceProvider().getRanking().getMaximumTicketSize();
            Integer maxTicketSize2 = r2.getServiceProvider().getMaximumTicketSize() != null ? r2.getServiceProvider().getMaximumTicketSize() : r2.getServiceProvider().getRanking().getMaximumTicketSize();

            // Avoid division by zero by ensuring maxTicketSize is not 0
            if (maxTicketSize1 == 0) maxTicketSize1 = 1;
            if (maxTicketSize2 == 0) maxTicketSize2 = 1;

            // Calculate bandwidth for both referrers
            double bandwidth1 = (double) (r1.getServiceProvider().getTicketAssigned() + r1.getServiceProvider().getTicketPending()) / maxTicketSize1 * 100;
            double bandwidth2 = (double) (r2.getServiceProvider().getTicketAssigned() + r2.getServiceProvider().getTicketPending()) / maxTicketSize2 * 100;

            // Sort by bandwidth (descending order)
            return Double.compare(bandwidth2, bandwidth1); // for descending order
        });

        // Return the sorted list
        Collections.reverse(referrers);
        return referrers;
    }

    private int getMaxTicketSize(Long spId) {
        String sql = """
            SELECT r.maximum_ticket_size
            FROM service_provider_rank_mapping m
            JOIN service_provider_rank r ON m.rank_id = r.rank_id
            WHERE m.service_provider_id = :spId
        """;
        return ((Number) entityManager.createNativeQuery(sql)
                .setParameter("spId", spId)
                .getSingleResult()).intValue();
    }
}
