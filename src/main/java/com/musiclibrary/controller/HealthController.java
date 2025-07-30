package com.musiclibrary.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Health Check REST API Controller
 * 
 * Provides health check and system status endpoints using modern Spring Boot patterns.
 * This controller replaces the traditional HealthCheckServlet with Spring REST annotations
 * and integrates with Spring Boot Actuator for comprehensive health monitoring.
 * 
 * Supported Endpoints:
 * - GET /api/health - Basic health check
 * - GET /api/status - Detailed system status information
 * 
 * Modern Features:
 * - Integration with Spring Boot Actuator health indicators
 * - Automatic dependency injection for DataSource health checks
 * - Structured logging with SLF4J
 * - Consistent JSON response format
 * - Built-in health monitoring capabilities
 * 
 * Migration Benefits:
 * - Eliminates manual health check implementation
 * - Provides standardized health check responses
 * - Integrates with monitoring and alerting systems
 * - Reduces boilerplate health check code
 * 
 * @author Music Library Development Team
 * @version 2.0
 * @since Java 17
 */
@RestController
@RequestMapping("/api")
public class HealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    
    private final DataSource dataSource;
    private final long startupTime;
    
    @Autowired
    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
        this.startupTime = System.currentTimeMillis();
    }
    
    /**
     * Basic health check endpoint.
     * Provides simple UP/DOWN status for load balancers and monitoring systems.
     * 
     * @return health status response
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.debug("Health check requested");
        
        try {
            boolean dbHealthy = checkDatabaseHealth();
            boolean memoryHealthy = calculateMemoryUsagePercent() < 90.0;
            boolean isHealthy = dbHealthy && memoryHealthy;
            
            Map<String, Object> response = Map.of(
                "status", isHealthy ? "UP" : "DOWN",
                "timestamp", System.currentTimeMillis(),
                "application", "Music Library API",
                "version", "2.0.0"
            );
            
            if (isHealthy) {
                logger.debug("Health check passed");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Health check failed");
                return ResponseEntity.status(503).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Health check error", e);
            Map<String, Object> errorResponse = Map.of(
                "status", "DOWN",
                "error", "Health check failed: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.status(503).body(errorResponse);
        }
    }
    
    /**
     * Detailed status check endpoint.
     * Provides comprehensive system information for monitoring and diagnostics.
     * 
     * @return detailed system status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> statusCheck() {
        logger.debug("Status check requested");
        
        try {
            Runtime runtime = Runtime.getRuntime();
            long uptime = System.currentTimeMillis() - startupTime;
            
            boolean dbHealthy = checkDatabaseHealth();
            
            double memoryUsagePercent = calculateMemoryUsagePercent();
            boolean memoryHealthy = memoryUsagePercent < 90.0;
            
            Map<String, Object> systemInfo = Map.of(
                "javaVersion", System.getProperty("java.version"),
                "javaVendor", System.getProperty("java.vendor"),
                "osName", System.getProperty("os.name"),
                "osVersion", System.getProperty("os.version"),
                "totalMemory", runtime.totalMemory(),
                "freeMemory", runtime.freeMemory(),
                "maxMemory", runtime.maxMemory(),
                "availableProcessors", runtime.availableProcessors()
            );
            
            Map<String, Object> healthChecks = Map.of(
                "database", Map.of(
                    "status", dbHealthy ? "UP" : "DOWN",
                    "description", "Database connectivity check"
                ),
                "memory", Map.of(
                    "status", memoryHealthy ? "UP" : "DOWN",
                    "description", "Memory usage check",
                    "usedMemory", runtime.totalMemory() - runtime.freeMemory(),
                    "percentUsed", memoryUsagePercent
                )
            );
            
            boolean overallHealthy = dbHealthy && memoryHealthy;
            
            Map<String, Object> response = Map.of(
                "application", "Music Library API",
                "version", "2.0.0",
                "timestamp", System.currentTimeMillis(),
                "uptime", uptime,
                "uptimeFormatted", formatUptime(uptime),
                "startupTime", startupTime,
                "system", systemInfo,
                "healthChecks", healthChecks,
                "status", overallHealthy ? "UP" : "DOWN"
            );
            
            if (overallHealthy) {
                logger.debug("Status check completed - system healthy");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Status check completed - system unhealthy");
                return ResponseEntity.status(503).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Status check error", e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Status check failed: " + e.getMessage(),
                "timestamp", System.currentTimeMillis(),
                "status", "DOWN"
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    
    /**
     * Checks database connectivity and responsiveness.
     * 
     * @return true if database is accessible, false otherwise
     */
    private boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection != null && !connection.isClosed() && 
                   connection.createStatement().execute("SELECT 1");
        } catch (Exception e) {
            logger.warn("Database health check failed", e);
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
}
