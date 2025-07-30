package com.musiclibrary.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Database Configuration for Spring Boot Application.
 * 
 * Replaces traditional JDBC connection management with Spring Boot auto-configuration.
 * Enables JPA repositories and transaction management.
 * 
 * Migration Benefits:
 * - Eliminates manual connection pool management
 * - Automatic transaction handling
 * - Built-in connection pooling with HikariCP
 * - Environment-specific configuration support
 * - Health checks and metrics integration
 * 
 * @author Music Library Development Team
 * @version 2.0 - Migrated to Spring Boot
 * @since Java 17
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.musiclibrary.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    
    /**
     * DataSource bean configured from application.yml properties.
     * Replaces manual DatabaseConnection utility class.
     * 
     * Spring Boot automatically configures HikariCP connection pool
     * with optimal settings for performance and reliability.
     */
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }
}
