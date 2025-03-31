package com.community.api.component;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomAdmin;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.ServiceProviderInfra;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.ResponseService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.RoleService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.apache.solr.client.solrj.io.stream.SolrStream;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.persistence.EntityManager;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The type Jwt authentication filter.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = Constant.BEARER_CONST;
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();
    private static final Pattern UNSECURED_URI_PATTERN = Pattern.compile(
//            "^/api/v1/(account|otp|test|files/avisoftdocument/[^/]+/[^/]+|files/[^/]+|avisoftdocument/[^/]+|swagger-ui.html|swagger-resources|v2/api-docs|images|webjars|product-custom/get-product-by-id|category-custom/get-sub-categories|advertisement/get-all-advertisement-by-categoryId).*"
            "^/api/v1/(account|otp|test|files/avisoftdocument/[^/]+/[^/]+|files/[^/]+|avisoftdocument/[^/]+|swagger-ui.html|swagger-resources|v2/api-docs|images|webjars).*"
    );
    private String apiKey="IaJGL98yHnKjnlhKshiWiy1IhZ+uFsKnktaqFX3Dvfg=";
    
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CustomCustomerService customCustomerService;
    @Autowired
    private RoleService roleService;

    @Autowired
    private CustomerService CustomerService;

    /**
     * The Token blacklist.
     */
    @Autowired
    TokenBlacklist tokenBlacklist;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Constant.request=request;
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        try {
            String requestURI = request.getRequestURI();

            if(!isUnsecuredUri(requestURI)&&!isApiKeyRequiredUri(request)) {
                System.out.println("ENTERING BLOCK1");
                String token = request.getHeader("Authorization");
                if(token==null)
                    respondWithUnauthorized(response, "JWT token cannot be empty");
                token = token.trim();
                String jwtToken = token.substring(7);
                if (sharedUtilityService.isBlackListed(jwtToken)) {
                    handleException(response, 403, "Your account is suspended please contact support.");
                }
            }

            if (isUnsecuredUri(requestURI) || bypassimages(requestURI)) {
                System.out.println("ENTERING BLOCK2");
                chain.doFilter(request, response);
                return;
            }
            if (isApiKeyRequiredUri(request) && validateApiKey(request)) {
                System.out.println("ENTERING BLOCK3");
                chain.doFilter(request, response);
                return;
            }
            /*if((checkRole(requestURI,request)).equals(false))
                throw new AccessDeniedException("Access not granted");*/
            boolean responseHandled = authenticateUser(request, response);
            if (!responseHandled) {
                System.out.println("ENTERING BLOCK4");
                chain.doFilter(request, response);
            }else{
                System.out.println("ENTERING BLOCK5");
                return;
            }

        } catch (AccessDeniedException e)
        {
            handleException(response, HttpServletResponse.SC_UNAUTHORIZED,e.getMessage());
        }
        catch (ExpiredJwtException e) {
            handleException(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token is expired");
            logger.error("ExpiredJwtException caught: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("hi");
            handleException(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            exceptionHandling.handleException(e);
            System.out.println("MalformedJwtException caught: {}"+e.getMessage());
        } catch (Exception e) {
            handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            exceptionHandling.handleException(e);

            logger.error("Exception caught: {}", e.getMessage());
        }

    }
    private boolean bypassimages(String requestURI) {
        return UNSECURED_URI_PATTERN.matcher(requestURI).matches();

    }

    private boolean isApiKeyRequiredUri(HttpServletRequest request) {

        String requestURI = request.getRequestURI();
        String path = requestURI.split("\\?")[0].trim();

        List<Pattern> bypassPatterns = Arrays.asList(
                Pattern.compile("^/api/v1/category-custom/get-products-by-category-id/\\d+$"),
                Pattern.compile("^/api/v1/category-custom/get-all-categories$"),
                Pattern.compile("^/api/v1/product-custom/get-product-by-id$"),
                Pattern.compile("^/api/v1/category-custom/get-sub-categories$"),
                Pattern.compile("^/api/v1/product-custom/get-product-by-id$")
        );

        boolean isBypassed = bypassPatterns.stream().anyMatch(pattern -> pattern.matcher(path).matches());
        return isBypassed;
    }

    private boolean validateApiKey(HttpServletRequest request) {
        String requestApiKey = request.getHeader("x-api-key");
        return apiKey.equals(requestApiKey);
    }

    private boolean isUnsecuredUri(String requestURI) {
        return requestURI.startsWith("/api/v1/account")
                || requestURI.startsWith("/api/v1/otp")
                || requestURI.startsWith("/api/v1/category-custom/get-all-categories")
                || requestURI.startsWith("/api/v1/test")
                || requestURI.startsWith("/api/v1/files/avisoftdocument/**")
                || requestURI.startsWith("/api/v1/files/**")
                || requestURI.startsWith("/api/v1/avisoftdocument/**")
                || requestURI.startsWith("/api/v1/swagger-ui.html")
                || requestURI.startsWith("/api/v1/swagger-resources")
                || requestURI.startsWith("/api/v1/v2/api-docs")
                || requestURI.startsWith("/api/v1/images")
                || requestURI.startsWith("/api/v1/webjars")
                || requestURI.matches("^/api/v1/product-custom/get-product-by-id/\\d+$")
                || requestURI.startsWith("/api/v1/category-custom/get-sub-categories")
                || requestURI.matches("^/api/v1/category-custom/get-sub-categories(/.*)?$")
//                || requestURI.startsWith("/api/v1/advertisement/get-all-advertisement-by-categoryId")
                || requestURI.startsWith("/api/v1/category-custom/get-products-by-category-id");
    }

    @Transactional
    private boolean authenticateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("hiiiii");
        final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            respondWithUnauthorized(response, "JWT token cannot be empty");
            return true;
        }

        if (customCustomerService == null) {
            respondWithUnauthorized(response, "CustomCustomerService is null");
            return true;
        }

        String jwt = authorizationHeader.substring(BEARER_PREFIX_LENGTH);
        Long id = jwtUtil.extractId(jwt);

        if (tokenBlacklist.isTokenBlacklisted(jwt)) {
            respondWithUnauthorized(response, "Token has been blacklisted");
            return true;
        }

        if (id == null) {
            respondWithUnauthorized(response, "Invalid details in token");
            return true;
        }
        String ipAdress = request.getRemoteAddr();
        String User_Agent = request.getHeader("User-Agent");

        try {
            if (!jwtUtil.validateToken(jwt, ipAdress, User_Agent)) {
                respondWithUnauthorized(response, "Invalid JWT token");
                return true;
            }
        } catch (ExpiredJwtException e) {
            jwtUtil.logoutUser(jwt);
            respondWithUnauthorized(response, "Token is expired");
            return true;
        }
        Customer customCustomer = null;
        ServiceProviderEntity serviceProvider = null;
        ServiceProviderEntity customAdmin=null;
        if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (roleService.findRoleName(jwtUtil.extractRoleId(jwt)).equals(Constant.roleUser)) {
                customCustomer = CustomerService.readCustomerById(id);
                if (customCustomer != null && jwtUtil.validateToken(jwt, ipAdress, User_Agent)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            customCustomer.getId(), null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    return false;
                } else {
                    jwtUtil.logoutUser(jwt);

                    respondWithUnauthorized(response, "Invalid data provided for this customer");
                    return true;
                }
            } else if (roleService.findRoleName(jwtUtil.extractRoleId(jwt)).equals(Constant.roleServiceProvider)) {
                serviceProvider=entityManager.find(ServiceProviderEntity.class,id);
                if (serviceProvider != null && jwtUtil.validateToken(jwt, ipAdress, User_Agent)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            serviceProvider.getService_provider_id(), null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    return false;
                } else {
                    respondWithUnauthorized(response, "Invalid data provided for this customer");
                    return true;
                }
            }

            else if (roleService.findRoleName(jwtUtil.extractRoleId(jwt)).equals(Constant.ADMIN) || roleService.findRoleName(jwtUtil.extractRoleId(jwt)).equals(Constant.SUPER_ADMIN) || roleService.findRoleName(jwtUtil.extractRoleId(jwt)).equals(Constant.roleAdminServiceProvider)) {
                try {
                    System.out.println("checking");
                    customAdmin = entityManager.find(ServiceProviderEntity.class, id);
                    System.out.println("checked");
                }catch (Exception e)
                {
                    System.out.println(e);
                }
                if (customAdmin != null && jwtUtil.validateToken(jwt, ipAdress, User_Agent)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            customAdmin.getService_provider_id(), null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    return false;
                } else {
                    respondWithUnauthorized(response, "Invalid data provided for this user");
                    return true;
                }
            }
        }
        return false;
    }

    private void respondWithUnauthorized(HttpServletResponse response, String message) throws IOException {
        if (!response.isCommitted()) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"UNAUTHORIZED\",\"status_code\":401,\"message\":\"" + message + "\"}");
            response.getWriter().flush();
        }
    }

    private void handleException(HttpServletResponse response, int statusCode, String message) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(statusCode);
            response.setContentType("application/json");

            String status;
            if (statusCode == HttpServletResponse.SC_BAD_REQUEST) {
                status = "BAD_REQUEST";
            } else if (statusCode == HttpServletResponse.SC_UNAUTHORIZED) {
                status = "UNAUTHORIZED";
            } else {
                status = "ERROR";
            }

            String jsonResponse = String.format(
                    "{\"status\":\"%s\",\"status_code\":%d,\"message\":\"%s\"}",
                    status,
                    statusCode,
                    message
            );
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
        }
    }
}
