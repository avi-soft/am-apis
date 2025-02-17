package com.community.api.services;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class DeviceDetectorService {

    private static final String CLIENT_HEADER = "X-Client-Type";
    private static final String MOBILE_APP_IDENTIFIER = "MOBILE_APP";
    private static final String WEB_IDENTIFIER = "WEB";

    public boolean isRequestFromMobileApp(HttpServletRequest request) {
        String clientType = request.getHeader(CLIENT_HEADER);
        System.out.println("X-Client-Type Header: " + clientType);  // Debugging

        return MOBILE_APP_IDENTIFIER.equalsIgnoreCase(clientType);
    }

    public boolean isRequestFromWebsite(HttpServletRequest request) {
        String clientType = request.getHeader(CLIENT_HEADER);
        System.out.println("X-Client-Type Header: " + clientType);  // Debugging

        return WEB_IDENTIFIER.equalsIgnoreCase(clientType);
    }
}
