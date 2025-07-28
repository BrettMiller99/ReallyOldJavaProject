package com.musiclibrary.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Request Logging Filter
 * 
 * Logs incoming HTTP requests for debugging, monitoring, and audit purposes using traditional Java 7 filter patterns.
 * This filter demonstrates classic enterprise Java logging approaches commonly found
 * in legacy applications before Spring Boot actuator and structured logging became standard.
 * 
 * Business Logic:
 * - Logs all incoming HTTP requests with method, URL, and timing information
 * - Provides request/response correlation for debugging purposes
 * - Supports configurable log levels and request body logging
 * - Tracks request processing time for performance monitoring
 * - Includes client information (IP address, user agent) for security audit
 * - Filters sensitive information from logs for security compliance
 * - Supports debugging mode for detailed request/response logging
 * 
 * Migration Opportunities:
 * - Traditional servlet filter -> Spring Boot actuator HTTP trace
 * - Manual request logging -> Micrometer metrics and tracing
 * - java.util.logging -> SLF4J with structured logging (JSON format)
 * - Static configuration -> Spring Boot configuration properties
 * - Manual timing -> Spring Boot actuator timing metrics
 * - Filter-based approach -> Spring Security audit events
 * - Basic correlation -> Distributed tracing with Zipkin/Jaeger
 * - Manual log formatting -> Logback structured logging patterns
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class RequestLoggingFilter implements Filter {
    
    // Traditional Java logging - migration opportunity
    private static final Logger LOGGER = Logger.getLogger(RequestLoggingFilter.class.getName());
    
    // Configuration parameters from web.xml
    private Level logLevel = Level.INFO;
    private boolean logRequestBody = false;
    private boolean logResponseBody = false;
    private boolean includeClientInfo = true;
    private boolean logHeaders = false;
    
    // Request counter for correlation
    private static long requestCounter = 0;
    
    /**
     * Filter initialization - traditional approach.
     * Reads logging configuration from web.xml init parameters.
     * Migration opportunity: Spring Boot logging auto-configuration.
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("Initializing RequestLoggingFilter");
        
        // Read configuration from web.xml init parameters
        String logLevelParam = filterConfig.getInitParameter("log.level");
        if (logLevelParam != null && !logLevelParam.trim().isEmpty()) {
            try {
                this.logLevel = Level.parse(logLevelParam.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                LOGGER.warning("Invalid log level parameter: " + logLevelParam + ", using INFO");
                this.logLevel = Level.INFO;
            }
        }
        
        String logRequestBodyParam = filterConfig.getInitParameter("log.request.body");
        if (logRequestBodyParam != null && !logRequestBodyParam.trim().isEmpty()) {
            this.logRequestBody = Boolean.parseBoolean(logRequestBodyParam.trim());
        }
        
        String logResponseBodyParam = filterConfig.getInitParameter("log.response.body");
        if (logResponseBodyParam != null && !logResponseBodyParam.trim().isEmpty()) {
            this.logResponseBody = Boolean.parseBoolean(logResponseBodyParam.trim());
        }
        
        String includeClientInfoParam = filterConfig.getInitParameter("log.client.info");
        if (includeClientInfoParam != null && !includeClientInfoParam.trim().isEmpty()) {
            this.includeClientInfo = Boolean.parseBoolean(includeClientInfoParam.trim());
        }
        
        String logHeadersParam = filterConfig.getInitParameter("log.headers");
        if (logHeadersParam != null && !logHeadersParam.trim().isEmpty()) {
            this.logHeaders = Boolean.parseBoolean(logHeadersParam.trim());
        }
        
        LOGGER.info("RequestLoggingFilter initialized with configuration:");
        LOGGER.info("  Log Level: " + logLevel);
        LOGGER.info("  Log Request Body: " + logRequestBody);
        LOGGER.info("  Log Response Body: " + logResponseBody);
        LOGGER.info("  Include Client Info: " + includeClientInfo);
        LOGGER.info("  Log Headers: " + logHeaders);
    }
    
    /**
     * Processes incoming requests and logs request/response information.
     * 
     * Business Logic:
     * - Assigns unique request ID for correlation across logs
     * - Logs request details before processing (method, URL, parameters)
     * - Measures and logs request processing time
     * - Logs response status and basic information after processing
     * - Filters sensitive information from logs for security
     * - Supports different log levels for production vs development
     * - Includes client identification for security monitoring
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
        
        // Generate unique request ID for correlation
        long requestId = generateRequestId();
        long startTime = System.currentTimeMillis();
        
        // Log request information
        logRequestStart(httpRequest, requestId);
        
        try {
            // Continue with the request processing
            chain.doFilter(request, response);
            
        } finally {
            // Log response information
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            logRequestEnd(httpRequest, httpResponse, requestId, processingTime);
        }
    }
    
    /**
     * Logs request start information.
     * 
     * @param request HTTP request
     * @param requestId Unique request identifier
     */
    private void logRequestStart(HttpServletRequest request, long requestId) {
        if (!LOGGER.isLoggable(logLevel)) {
            return;
        }
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("REQUEST START [").append(requestId).append("] ");
        logMessage.append(request.getMethod()).append(" ");
        logMessage.append(request.getRequestURI());
        
        // Add query string if present
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.trim().isEmpty()) {
            logMessage.append("?").append(filterSensitiveParameters(queryString));
        }
        
        // Add client information if enabled
        if (includeClientInfo) {
            logMessage.append(" | Client: ").append(getClientInfo(request));
        }
        
        // Add protocol and version
        logMessage.append(" | Protocol: ").append(request.getProtocol());
        
        LOGGER.log(logLevel, logMessage.toString());
        
        // Log headers if enabled
        if (logHeaders && LOGGER.isLoggable(Level.FINE)) {
            logRequestHeaders(request, requestId);
        }
        
        // Log request body if enabled (for POST/PUT requests)
        if (logRequestBody && hasRequestBody(request)) {
            // Note: In a real implementation, you'd need to wrap the request
            // to be able to read the body without consuming it
            LOGGER.log(Level.FINE, "REQUEST BODY [" + requestId + "] - Body logging not implemented in this demo");
        }
    }
    
    /**
     * Logs request end information including response status and timing.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param requestId Unique request identifier
     * @param processingTimeMs Processing time in milliseconds
     */
    private void logRequestEnd(HttpServletRequest request, HttpServletResponse response, 
                              long requestId, long processingTimeMs) {
        if (!LOGGER.isLoggable(logLevel)) {
            return;
        }
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("REQUEST END [").append(requestId).append("] ");
        logMessage.append(request.getMethod()).append(" ");
        logMessage.append(request.getRequestURI());
        logMessage.append(" | Status: ").append(response.getStatus());
        logMessage.append(" | Time: ").append(processingTimeMs).append("ms");
        
        // Add content type if available
        String contentType = response.getContentType();
        if (contentType != null) {
            logMessage.append(" | Content-Type: ").append(contentType);
        }
        
        // Determine log level based on response status
        Level responseLogLevel = determineResponseLogLevel(response.getStatus());
        LOGGER.log(responseLogLevel, logMessage.toString());
        
        // Log performance warning for slow requests
        if (processingTimeMs > 5000) { // 5 seconds
            LOGGER.warning("SLOW REQUEST [" + requestId + "] took " + processingTimeMs + "ms: " +
                         request.getMethod() + " " + request.getRequestURI());
        }
    }
    
    /**
     * Logs request headers for debugging purposes.
     * 
     * @param request HTTP request
     * @param requestId Unique request identifier
     */
    private void logRequestHeaders(HttpServletRequest request, long requestId) {
        StringBuilder headerLog = new StringBuilder();
        headerLog.append("REQUEST HEADERS [").append(requestId).append("] ");
        
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        boolean first = true;
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            if (!first) {
                headerLog.append(", ");
            }
            first = false;
            
            // Filter sensitive headers
            if (isSensitiveHeader(headerName)) {
                headerLog.append(headerName).append(": [FILTERED]");
            } else {
                headerLog.append(headerName).append(": ").append(headerValue);
            }
        }
        
        LOGGER.fine(headerLog.toString());
    }
    
    /**
     * Gets client information for logging.
     * 
     * @param request HTTP request
     * @return Client information string
     */
    private String getClientInfo(HttpServletRequest request) {
        StringBuilder clientInfo = new StringBuilder();
        
        // Client IP address
        String clientIP = getClientIPAddress(request);
        clientInfo.append("IP=").append(clientIP);
        
        // User agent
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            // Truncate long user agent strings
            if (userAgent.length() > 100) {
                userAgent = userAgent.substring(0, 97) + "...";
            }
            clientInfo.append(", UA=").append(userAgent);
        }
        
        return clientInfo.toString();
    }
    
    /**
     * Gets the real client IP address, considering proxy headers.
     * 
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIPAddress(HttpServletRequest request) {
        // Check for forwarded IP headers (common in load balancer setups)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.trim().isEmpty()) {
            return xRealIP.trim();
        }
        
        // Fall back to remote address
        return request.getRemoteAddr();
    }
    
    /**
     * Filters sensitive parameters from query strings for logging.
     * 
     * @param queryString Original query string
     * @return Filtered query string with sensitive values masked
     */
    private String filterSensitiveParameters(String queryString) {
        if (queryString == null) {
            return null;
        }
        
        // List of sensitive parameter names that should be filtered
        String[] sensitiveParams = {"password", "token", "key", "secret", "auth", "credential"};
        
        String filteredQuery = queryString;
        for (String sensitiveParam : sensitiveParams) {
            // Simple regex to replace sensitive parameter values
            filteredQuery = filteredQuery.replaceAll(
                "(?i)" + sensitiveParam + "=[^&]*", 
                sensitiveParam + "=[FILTERED]");
        }
        
        return filteredQuery;
    }
    
    /**
     * Checks if a header contains sensitive information that should be filtered.
     * 
     * @param headerName Header name to check
     * @return true if header is sensitive, false otherwise
     */
    private boolean isSensitiveHeader(String headerName) {
        if (headerName == null) {
            return false;
        }
        
        String lowerHeaderName = headerName.toLowerCase();
        return lowerHeaderName.contains("authorization") || 
               lowerHeaderName.contains("password") ||
               lowerHeaderName.contains("token") ||
               lowerHeaderName.contains("key") ||
               lowerHeaderName.contains("secret") ||
               lowerHeaderName.contains("credential");
    }
    
    /**
     * Checks if the request likely has a request body.
     * 
     * @param request HTTP request
     * @return true if request likely has a body, false otherwise
     */
    private boolean hasRequestBody(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equalsIgnoreCase(method) || 
               "PUT".equalsIgnoreCase(method) || 
               "PATCH".equalsIgnoreCase(method);
    }
    
    /**
     * Determines appropriate log level based on HTTP response status.
     * 
     * @param status HTTP status code
     * @return Appropriate log level
     */
    private Level determineResponseLogLevel(int status) {
        if (status >= 500) {
            return Level.SEVERE; // Server errors
        } else if (status >= 400) {
            return Level.WARNING; // Client errors
        } else if (status >= 300) {
            return Level.INFO; // Redirects
        } else {
            return logLevel; // Success responses use configured level
        }
    }
    
    /**
     * Generates a unique request ID for correlation.
     * Simple implementation - in production, consider using UUID or more sophisticated approach.
     * 
     * @return Unique request identifier
     */
    private synchronized long generateRequestId() {
        return ++requestCounter;
    }
    
    /**
     * Filter cleanup - traditional approach.
     * Migration opportunity: Spring managed filter lifecycle.
     */
    @Override
    public void destroy() {
        LOGGER.info("RequestLoggingFilter destroyed");
    }
}
