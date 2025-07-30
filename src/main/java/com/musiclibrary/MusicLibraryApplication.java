package com.musiclibrary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Application Main Class
 * 
 * This class serves as the entry point for the modernized Music Library API.
 * Replaces the traditional web.xml configuration and servlet container setup
 * with Spring Boot's auto-configuration and embedded server.
 * 
 * Key Features:
 * - Auto-configuration of Spring components and dependencies
 * - Embedded Tomcat server for standalone deployment
 * - Component scanning for controllers, services, and repositories
 * - Automatic database initialization and JPA configuration
 * - Built-in health checks and metrics via Spring Actuator
 * 
 * Migration Benefits:
 * - Eliminates need for web.xml and servlet configuration
 * - Provides production-ready features out of the box
 * - Simplifies deployment and testing
 * - Enables modern Spring Boot development patterns
 * 
 * @author Music Library Development Team
 * @version 2.0
 * @since Java 17
 */
@SpringBootApplication
public class MusicLibraryApplication {
    
    /**
     * Application entry point.
     * Starts the Spring Boot application with embedded server.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(MusicLibraryApplication.class, args);
    }
}
