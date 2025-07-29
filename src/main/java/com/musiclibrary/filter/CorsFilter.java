package com.musiclibrary.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * CORS (Cross-Origin Resource Sharing) Filter
 * 
 * Handles Cross-Origin Resource Sharing for web clients using traditional Java 7 filter patterns.
 * This filter demonstrates classic enterprise Java web security approaches commonly found
 * in legacy applications before Spring Security became standard.
 * 
 * Business Logic:
 * - Enables cross-origin requests for web-based API clients
 * - Configures allowed origins, methods, and headers
 * - Handles preflight OPTIONS requests for complex CORS scenarios
 * - Provides security controls for cross-domain API access
 * - Supports configurable CORS policies through web.xml parameters
 * - Implements proper CORS response headers for browser compliance
 * 
 * Migration Opportunities:
 * - Traditional servlet filter -> Spring Security CORS configuration
 * - Manual CORS headers -> @CrossOrigin annotations on controllers
 * - web.xml configuration -> Spring Boot CORS auto-configuration
 * - Manual filter registration -> Spring Security filter chain
 * - Static configuration -> Spring configuration properties
 * - Traditional logging -> Structured logging with request correlation
 * - Manual header management -> Spring CORS registry configuration
 * - Filter-based approach -> Spring Security @EnableWebSecurity
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class CorsFilter implements Filter {
    
    // Traditional Java logging - migration opportunity
    private static final Logger LOGGER = Logger.getLogger(CorsFilter.class.getName());
    
    // CORS configuration parameters from web.xml
    private String allowedOrigins = "*";
    private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
    private String allowedHeaders = "Content-Type,Authorization,X-Requested-With";
    private String maxAge = "3600";
    private boolean allowCredentials = false;
    
    /**
     * Filter initialization - traditional approach.
     * Reads CORS configuration from web.xml init parameters.
     * Migration opportunity: Spring Boot CORS auto-configuration.
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("Initializing CorsFilter");
        
        // Read configuration from web.xml init parameters
        String originsParam = filterConfig.getInitParameter("cors.allowed.origins");
        if (originsParam != null && !originsParam.trim().isEmpty()) {
            this.allowedOrigins = originsParam.trim();
        }
        
        String methodsParam = filterConfig.getInitParameter("cors.allowed.methods");
        if (methodsParam != null && !methodsParam.trim().isEmpty()) {
            this.allowedMethods = methodsParam.trim();
        }
        
        String headersParam = filterConfig.getInitParameter("cors.allowed.headers");
        if (headersParam != null && !headersParam.trim().isEmpty()) {
            this.allowedHeaders = headersParam.trim();
        }
        
        String maxAgeParam = filterConfig.getInitParameter("cors.max.age");
        if (maxAgeParam != null && !maxAgeParam.trim().isEmpty()) {
            this.maxAge = maxAgeParam.trim();
        }
        
        String credentialsParam = filterConfig.getInitParameter("cors.allow.credentials");
        if (credentialsParam != null && !credentialsParam.trim().isEmpty()) {
            this.allowCredentials = Boolean.parseBoolean(credentialsParam.trim());
        }
        
        LOGGER.info("CorsFilter initialized with configuration:");
        LOGGER.info("  Allowed Origins: " + allowedOrigins);
        LOGGER.info("  Allowed Methods: " + allowedMethods);
        LOGGER.info("  Allowed Headers: " + allowedHeaders);
        LOGGER.info("  Max Age: " + maxAge);
        LOGGER.info("  Allow Credentials: " + allowCredentials);
    }
    
    /**
     * Processes incoming requests and applies CORS headers.
     * 
     * Business Logic:
     * - Identifies cross-origin requests by checking Origin header
     * - Applies appropriate CORS response headers for browser compliance
     * - Handles preflight OPTIONS requests for complex CORS scenarios
     * - Validates origins against configured allowed origins (if not wildcard)
     * - Sets security-related CORS headers for credential handling
     * - Logs CORS request details for debugging and monitoring
     * 
     * @param request Servlet request
     * @param response Servlet response
     * @param chain Filter chain for request processing
     * @throws IOException if I/O error occurs
     * @throws ServletException if servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        // Cast to HTTP-specific request/response objects
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Get origin header to determine if this is a cross-origin request
        String origin = httpRequest.getHeader("Origin");
        String method = httpRequest.getMethod();
        
        LOGGER.fine("Processing CORS request: " + method + " " + httpRequest.getRequestURI() + 
                   " from origin: " + origin);
        
        // Always add CORS headers for API endpoints
        applyCorsHeaders(httpRequest, httpResponse, origin);
        
        // Handle preflight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(method)) {
            handlePreflightRequest(httpRequest, httpResponse);
            return; // Don't continue with the filter chain for preflight requests
        }
        
        // Continue with the request processing
        chain.doFilter(request, response);
    }
    
    /**
     * Applies CORS headers to the HTTP response.
     * 
     * Business Logic:
     * - Sets Access-Control-Allow-Origin based on configuration and request origin
     * - Configures allowed methods for cross-origin requests
     * - Specifies allowed headers for complex requests
     * - Sets cache duration for preflight responses
     * - Handles credential policies for authenticated requests
     * - Validates origin against whitelist if configured (not wildcard)
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param origin Origin header from the request
     */
    private void applyCorsHeaders(HttpServletRequest request, HttpServletResponse response, String origin) {
        // Handle Access-Control-Allow-Origin
        if (origin != null) {
            if ("*".equals(allowedOrigins)) {
                // Allow all origins
                response.setHeader("Access-Control-Allow-Origin", "*");
            } else {
                // Check if origin is in allowed list
                if (isOriginAllowed(origin)) {
                    response.setHeader("Access-Control-Allow-Origin", origin);
                    LOGGER.fine("Origin allowed: " + origin);
                } else {
                    LOGGER.warning("Origin not allowed: " + origin);
                    // Don't set CORS headers for disallowed origins
                    return;
                }
            }
        }
        
        // Set other CORS headers
        response.setHeader("Access-Control-Allow-Methods", allowedMethods);
        response.setHeader("Access-Control-Allow-Headers", allowedHeaders);
        response.setHeader("Access-Control-Max-Age", maxAge);
        
        // Handle credentials
        if (allowCredentials) {
            response.setHeader("Access-Control-Allow-Credentials", "true");
            // Note: Cannot use wildcard origin with credentials
            if ("*".equals(allowedOrigins)) {
                LOGGER.warning("Cannot use wildcard origin with credentials enabled");
            }
        }
        
        // Expose common headers that clients might need
        response.setHeader("Access-Control-Expose-Headers", 
            "Content-Length,Content-Type,Date,Server,X-Requested-With");
    }
    
    /**
     * Handles preflight OPTIONS requests for complex CORS scenarios.
     * 
     * Business Logic:
     * - Responds to browser preflight requests before complex cross-origin requests
     * - Validates requested method and headers against configuration
     * - Returns appropriate status codes for preflight validation
     * - Provides detailed CORS policy information to the browser
     * - Logs preflight request details for debugging
     * 
     * @param request HTTP request (OPTIONS method)
     * @param response HTTP response
     * @throws IOException if I/O error occurs
     */
    private void handlePreflightRequest(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String requestedMethod = request.getHeader("Access-Control-Request-Method");
        String requestedHeaders = request.getHeader("Access-Control-Request-Headers");
        
        LOGGER.fine("Handling preflight request - Method: " + requestedMethod + 
                   ", Headers: " + requestedHeaders);
        
        // Validate requested method
        if (requestedMethod != null && isMethodAllowed(requestedMethod)) {
            // Method is allowed, preflight successful
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Access-Control-Allow-Methods", allowedMethods);
            LOGGER.fine("Preflight request approved for method: " + requestedMethod);
        } else {
            // Method not allowed
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            LOGGER.warning("Preflight request denied for method: " + requestedMethod);
            return;
        }
        
        // Validate requested headers if present
        if (requestedHeaders != null && !areHeadersAllowed(requestedHeaders)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            LOGGER.warning("Preflight request denied for headers: " + requestedHeaders);
            return;
        }
        
        // Set content length to 0 for preflight response
        response.setContentLength(0);
    }
    
    /**
     * Checks if the given origin is allowed based on configuration.
     * 
     * @param origin Origin to check
     * @return true if origin is allowed, false otherwise
     */
    private boolean isOriginAllowed(String origin) {
        if ("*".equals(allowedOrigins)) {
            return true;
        }
        
        // Split allowed origins by comma and check each
        String[] origins = allowedOrigins.split(",");
        for (String allowedOrigin : origins) {
            if (allowedOrigin.trim().equals(origin)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if the given HTTP method is allowed based on configuration.
     * 
     * @param method HTTP method to check
     * @return true if method is allowed, false otherwise
     */
    private boolean isMethodAllowed(String method) {
        if (method == null) {
            return false;
        }
        
        String[] methods = allowedMethods.split(",");
        for (String allowedMethod : methods) {
            if (allowedMethod.trim().equalsIgnoreCase(method)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if the requested headers are allowed based on configuration.
     * 
     * @param requestedHeaders Comma-separated list of requested headers
     * @return true if all headers are allowed, false otherwise
     */
    private boolean areHeadersAllowed(String requestedHeaders) {
        if (requestedHeaders == null || requestedHeaders.trim().isEmpty()) {
            return true;
        }
        
        String[] requestedHeaderArray = requestedHeaders.split(",");
        String[] allowedHeaderArray = allowedHeaders.split(",");
        
        // Check each requested header
        for (String requestedHeader : requestedHeaderArray) {
            String trimmedRequested = requestedHeader.trim().toLowerCase();
            boolean headerAllowed = false;
            
            // Check against allowed headers
            for (String allowedHeader : allowedHeaderArray) {
                if (allowedHeader.trim().toLowerCase().equals(trimmedRequested)) {
                    headerAllowed = true;
                    break;
                }
            }
            
            // Also allow standard headers that browsers always send
            if (!headerAllowed && isStandardHeader(trimmedRequested)) {
                headerAllowed = true;
            }
            
            if (!headerAllowed) {
                LOGGER.warning("Requested header not allowed: " + trimmedRequested);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Checks if a header is a standard browser header that should always be allowed.
     * 
     * @param header Header name (lowercase)
     * @return true if it's a standard header, false otherwise
     */
    private boolean isStandardHeader(String header) {
        // Standard headers that browsers automatically include
        String[] standardHeaders = {
            "accept", "accept-language", "content-language", "content-type",
            "cache-control", "expires", "last-modified", "pragma"
        };
        
        for (String standardHeader : standardHeaders) {
            if (standardHeader.equals(header)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Filter cleanup - traditional approach.
     * Migration opportunity: Spring managed filter lifecycle.
     */
    @Override
    public void destroy() {
        LOGGER.info("CorsFilter destroyed");
    }
}
