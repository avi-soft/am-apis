package com.community.api.component;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.RoleService;

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
    RoleService roleService;
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
    public void blacklistToken(String token,Long exp) {
        try {
            blacklistedTokens.put(token, exp);  
            Long id = jwtUtil.extractId(token);
            Integer role=jwtUtil.extractRoleId(token);
            if(roleService.findRoleName(role).equals(Constant.roleUser))
            {
            CustomCustomer existingCustomer = em.find(CustomCustomer.class,id);
            if (existingCustomer != null) {
                existingCustomer.setToken(null);
                em.merge(existingCustomer);
            } else {
                throw new RuntimeException("Customer not found for the given token");
            }
            }
            if(roleService.findRoleName(role).equals(Constant.serviceProviderRoles))
            {
            ServiceProviderEntity existintServiceProviderEntity = em.find(ServiceProviderEntity.class,id);
            if (existintServiceProviderEntity != null) {
                existintServiceProviderEntity.setToken(null);
                em.merge(existintServiceProviderEntity);
            } else {
                throw new RuntimeException("SP not found for the given token");
            }
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
