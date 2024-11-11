package com.community.api.component;
import com.community.api.entity.CustomCustomer;
import com.community.api.services.CustomCustomerService;
import io.jsonwebtoken.ExpiredJwtException;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Token blacklist.
 */
@Service
public class TokenBlacklist {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    EntityManager em;

    @Autowired
    CustomCustomerService customCustomerService;
    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Blacklist token.
     *
     * @param token          the token
     * @param expirationTime the expiration time
     */
    @Transactional
    public void blacklistToken(String token) {
        try {
            Date expirationTime = jwtUtil.getExpiryTime(token);
            blacklistedTokens.put(token, expirationTime.getTime());  // Storing the token with its expiration time in the blacklist
            Long id = jwtUtil.extractId(token);

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerById(id);

            if (existingCustomer != null) {
                existingCustomer.setToken(null);
                em.persist(existingCustomer);
            } else {
                throw new RuntimeException("Customer not found for the given token");
            }

        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to blacklist token", e);
        }
    }

    /**
     * Is token blacklisted boolean.
     *
     * @param token the token
     * @return the boolean
     */
    public boolean isTokenBlacklisted(String token) {
        Long expirationTime = blacklistedTokens.get(token);

        if (expirationTime != null && expirationTime > System.currentTimeMillis()) {
            return true;
        } else {
            blacklistedTokens.remove(token);
            return false;
        }
    }


    /**
     * Clean expired tokens.
     */
//    @Scheduled(fixedRate = 60000) // 1 minutes interval
    @Scheduled(fixedRate = 36000000)  // 10 hour interval
    public void cleanExpiredTokens() {
        long currentTime = System.currentTimeMillis();

        Iterator<Map.Entry<String, Long>> iterator = blacklistedTokens.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            long expirationTime = entry.getValue();

            if (expirationTime < currentTime) {
                iterator.remove();
            }
        }

        System.out.println("Expired tokens cleaned up from blacklist");
    }
}
