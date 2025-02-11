package com.community.api.services;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

@Service
public class DeviceDetectorService {

    // Improved regex to match various mobile and tablet devices
    private static final String MOBILE_REGEX = ".*(android|webos|iphone|ipad|ipod|blackberry|windows phone|mobile).*";
    private static final Pattern MOBILE_PATTERN = Pattern.compile(MOBILE_REGEX, Pattern.CASE_INSENSITIVE);

    public boolean isMobileOrTablet(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        System.out.println("User-Agent: " + userAgent);  // Log the User-Agent string for debugging
        if (userAgent == null) {
            return false;
        }

        boolean isMobile = MOBILE_PATTERN.matcher(userAgent).matches();
        System.out.println("Matches mobile/tablet regex: " + isMobile);  // Log the match result
        return isMobile;
    }
}