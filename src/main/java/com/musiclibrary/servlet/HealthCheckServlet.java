package com.musiclibrary.servlet;

import com.musiclibrary.util.DatabaseConnection;
import com.musiclibrary.util.JsonUtil;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Health Check and Status API Servlet
 * 
 * Provides health check and system status endpoints using traditional Java 7 servlet patterns.
 * This servlet demonstrates basic application monitoring and diagnostics functionality
 * commonly found in enterprise Java applications.
 * 
 * Supported Endpoints:
 * - GET /api/health - Basic health check
 * - GET /api/status - Detailed system status information
 * 
 * Business Logic:
 * - Performs basic application health checks
 * - Tests database connectivity and responsiveness
 * - Provides system status information for monitoring
 * - Returns standardized health check responses for load balancers
 * - Includes uptime and version information
 * - Validates critical system components
 * 
 * Migration Opportunities:
 * - Traditional servlet -> Spring Boot Actuator health endpoints
 * - Manual health checks -> Spring Boot health indicators
 * - Manual JSON responses -> Actuator auto-configuration
 * - Basic status info -> Micrometer metrics integration
 * - Manual database check -> Spring Boot DataSource health indicator
 * - Traditional logging -> Structured logging with correlation IDs
 * - Static configuration -> Spring Boot configuration properties
 * - Manual servlet mapping -> Actuator endpoint auto-configuration
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class HealthCheckServlet extends HttpServlet {
    
    // Traditional Java logging - migration opportunity
    private static final Logger LOGGER = Logger.getLogger(HealthCheckServlet.class.getName());
    
    // Application metadata
    private static final String APPLICATION_NAME = "Music Library API";
    private static final String VERSION = "1.0.0";
    
    // Servlet startup time for uptime calculation
    private long startupTime;
    
    /**
     * Servlet initialization - traditional approach.
     * Migration opportunity: Spring Boot Actuator auto-configuration.
     */
    @Override
    public void init() throws ServletException {
        super.init();
        this.startupTime = System.currentTimeMillis();
        LOGGER.info("HealthCheckServlet initialized at " + new Date(startupTime));
    }
    
    /**
     * Handles GET requests for health check and status information.
     * 
     * Supported patterns:
     * - GET /api/health - Basic health check
     * - GET /api/status - Detailed system status
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.fine("Health check request received: " + request.getRequestURI());
        
        // Set response content type - manual approach
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Parse URL path to determine operation - manual routing
            String requestURI = request.getRequestURI();
            
            if (requestURI.endsWith("/health")) {
                // Basic health check
                handleHealthCheck(response, out);
            } else if (requestURI.endsWith("/status")) {
                // Detailed status information
                handleStatusCheck(response, out);
            } else {
                // Default to basic health check
                handleHealthCheck(response, out);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing health check request", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Health check failed: " + e.getMessage());
        } finally {
            out.close();
        }
    }
    
    /**
     * Handles basic health check requests.
     * Provides simple UP/DOWN status for load balancers and monitoring systems.
     */
    private void handleHealthCheck(HttpServletResponse response, PrintWriter out) throws IOException {
        try {
            // Perform basic health checks
            boolean isHealthy = performHealthChecks();
            
            JSONObject healthResponse = new JSONObject();
            healthResponse.put("status", isHealthy ? "UP" : "DOWN");
            healthResponse.put("timestamp", System.currentTimeMillis());
            healthResponse.put("application", APPLICATION_NAME);
            healthResponse.put("version", VERSION);
            
            if (isHealthy) {
                response.setStatus(HttpServletResponse.SC_OK);
                LOGGER.fine("Health check passed");
            } else {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                LOGGER.warning("Health check failed");
            }
            
            out.print(healthResponse.toString());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error performing health check", e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("status", "DOWN");
            errorResponse.put("error", "Health check error: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            out.print(errorResponse.toString());
        }
    }
    
    /**
     * Handles detailed status check requests.
     * Provides comprehensive system information for monitoring and diagnostics.
     */
    private void handleStatusCheck(HttpServletResponse response, PrintWriter out) throws IOException {
        try {
            JSONObject statusResponse = new JSONObject();
            
            // Basic application information
            statusResponse.put("application", APPLICATION_NAME);
            statusResponse.put("version", VERSION);
            statusResponse.put("timestamp", System.currentTimeMillis());
            
            // Uptime calculation
            long uptime = System.currentTimeMillis() - startupTime;
            statusResponse.put("uptime", uptime);
            statusResponse.put("uptimeFormatted", formatUptime(uptime));
            statusResponse.put("startupTime", startupTime);
            
            // System information
            JSONObject systemInfo = new JSONObject();
            Runtime runtime = Runtime.getRuntime();
            systemInfo.put("javaVersion", System.getProperty("java.version"));
            systemInfo.put("javaVendor", System.getProperty("java.vendor"));
            systemInfo.put("osName", System.getProperty("os.name"));
            systemInfo.put("osVersion", System.getProperty("os.version"));
            systemInfo.put("totalMemory", runtime.totalMemory());
            systemInfo.put("freeMemory", runtime.freeMemory());
            systemInfo.put("maxMemory", runtime.maxMemory());
            systemInfo.put("availableProcessors", runtime.availableProcessors());
            statusResponse.put("system", systemInfo);
            
            // Health check results
            JSONObject healthChecks = new JSONObject();
            
            // Database connectivity check
            boolean dbHealthy = checkDatabaseHealth();
            JSONObject dbHealth = new JSONObject();
            dbHealth.put("status", dbHealthy ? "UP" : "DOWN");
            dbHealth.put("description", "Database connectivity check");
            healthChecks.put("database", dbHealth);
            
            // Memory usage check
            boolean memoryHealthy = checkMemoryHealth();
            JSONObject memoryHealth = new JSONObject();
            memoryHealth.put("status", memoryHealthy ? "UP" : "DOWN");
            memoryHealth.put("description", "Memory usage check");
            memoryHealth.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
            memoryHealth.put("percentUsed", calculateMemoryUsagePercent());
            healthChecks.put("memory", memoryHealth);
            
            statusResponse.put("healthChecks", healthChecks);
            
            // Overall status determination
            boolean overallHealthy = dbHealthy && memoryHealthy;
            statusResponse.put("status", overallHealthy ? "UP" : "DOWN");
            
            if (overallHealthy) {
                response.setStatus(HttpServletResponse.SC_OK);
                LOGGER.fine("Status check completed - system healthy");
            } else {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                LOGGER.warning("Status check completed - system unhealthy");
            }
            
            out.print(statusResponse.toString());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error performing status check", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Status check failed: " + e.getMessage());
        }
    }
    
    /**
     * Performs basic health checks for the application.
     * 
     * @return true if all health checks pass, false otherwise
     */
    private boolean performHealthChecks() {
        try {
            // Check database connectivity
            boolean dbHealthy = checkDatabaseHealth();
            
            // Check memory usage
            boolean memoryHealthy = checkMemoryHealth();
            
            // Add more health checks as needed
            return dbHealthy && memoryHealthy;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Health check failed with exception", e);
            return false;
        }
    }
    
    /**
     * Checks database connectivity and responsiveness.
     * 
     * @return true if database is accessible, false otherwise
     */
    private boolean checkDatabaseHealth() {
        Connection connection = null;
        try {
            // Test database connection
            connection = DatabaseConnection.getConnection();
            if (connection != null && !connection.isClosed()) {
                // Test with a simple query
                connection.createStatement().execute("SELECT 1");
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Database health check failed", e);
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing health check connection", e);
                }
            }
        }
    }
    
    /**
     * Checks memory usage and availability.
     * 
     * @return true if memory usage is acceptable, false otherwise
     */
    private boolean checkMemoryHealth() {
        try {
            double memoryUsagePercent = calculateMemoryUsagePercent();
            
            // Consider unhealthy if memory usage is above 90%
            boolean healthy = memoryUsagePercent < 90.0;
            
            if (!healthy) {
                LOGGER.warning("Memory usage is high: " + memoryUsagePercent + "%");
            }
            
            return healthy;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Memory health check failed", e);
            return false;
        }
    }
    
    /**
     * Calculates current memory usage percentage.
     * 
     * @return memory usage percentage
     */
    private double calculateMemoryUsagePercent() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return ((double) usedMemory / totalMemory) * 100.0;
    }
    
    /**
     * Formats uptime in milliseconds to a human-readable string.
     * 
     * @param uptimeMs uptime in milliseconds
     * @return formatted uptime string
     */
    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;
        
        if (days > 0) {
            return String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", minutes, seconds);
        } else {
            return String.format("%d seconds", seconds);
        }
    }
    
    /**
     * Sends standardized error response.
     * Manual approach - migration opportunity to @ExceptionHandler.
     */
    private void sendErrorResponse(HttpServletResponse response, PrintWriter out, 
                                  int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        JSONObject errorResponse = JsonUtil.createErrorResponse(message, String.valueOf(statusCode));
        out.print(errorResponse.toString());
    }
    
    /**
     * Handles POST requests (not supported for health checks).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        JSONObject errorResponse = JsonUtil.createErrorResponse(
            "POST method not supported for health check endpoints", "405");
        out.print(errorResponse.toString());
        out.close();
    }
    
    /**
     * Handles PUT requests (not supported for health checks).
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        JSONObject errorResponse = JsonUtil.createErrorResponse(
            "PUT method not supported for health check endpoints", "405");
        out.print(errorResponse.toString());
        out.close();
    }
    
    /**
     * Handles DELETE requests (not supported for health checks).
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        JSONObject errorResponse = JsonUtil.createErrorResponse(
            "DELETE method not supported for health check endpoints", "405");
        out.print(errorResponse.toString());
        out.close();
    }
    
    /**
     * Servlet cleanup - traditional approach.
     * Migration opportunity: Spring @PreDestroy or application shutdown hooks.
     */
    @Override
    public void destroy() {
        LOGGER.info("HealthCheckServlet destroyed");
        super.destroy();
    }
}
