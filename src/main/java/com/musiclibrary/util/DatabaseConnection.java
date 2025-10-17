package com.musiclibrary.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database Connection Utility Class
 * 
 * Manages database connections using traditional Java 7 JDBC patterns.
 * This class demonstrates legacy enterprise connection management approaches
 * commonly found in older Java applications.
 * 
 * Business Logic:
 * - Provides centralized database connection management
 * - Handles connection pooling through basic implementation
 * - Loads configuration from properties files
 * - Manages database schema initialization
 * - Provides transaction support through manual commit/rollback
 * 
 * Migration Opportunities:
 * - Manual connection management -> Spring Boot DataSource
 * - Properties file configuration -> application.yml
 * - Basic connection pooling -> HikariCP/C3P0
 * - Manual resource cleanup -> try-with-resources
 * - java.util.logging -> SLF4J/Logback
 * - Manual SQL execution -> Spring Data JPA
 * - Traditional exception handling -> @ControllerAdvice
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class DatabaseConnection {
    
    // Traditional Java logging - migration opportunity to SLF4J
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    
    // Configuration properties loaded from file
    private static Properties dbProperties;
    private static String driverClass;
    private static String jdbcUrl;
    private static String username;
    private static String password;
    
    // Simple connection pool variables - basic implementation
    private static int poolSize = 10;
    private static int activeConnections = 0;
    
    // Static initialization block - traditional Java 7 pattern
    static {
        loadDatabaseProperties();
        initializeDatabase();
    }
    
    /**
     * Loads database configuration from properties file.
     * Traditional approach using Properties class and manual file handling.
     * Migration opportunity: Spring Boot's @ConfigurationProperties
     */
    private static void loadDatabaseProperties() {
        InputStream inputStream = null;
        try {
            // Load properties file from classpath - traditional approach
            inputStream = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("database.properties");
                    
            if (inputStream == null) {
                throw new RuntimeException("Unable to find database.properties file");
            }
            
            dbProperties = new Properties();
            dbProperties.load(inputStream);
            
            // Extract connection parameters
            driverClass = dbProperties.getProperty("db.driver");
            jdbcUrl = dbProperties.getProperty("db.url");
            username = dbProperties.getProperty("db.username");
            password = dbProperties.getProperty("db.password");
            
            // Load pool configuration
            String poolSizeStr = dbProperties.getProperty("db.pool.maxActive", "10");
            poolSize = Integer.parseInt(poolSizeStr);
            
            LOGGER.info("Database properties loaded successfully");
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load database properties", e);
            throw new RuntimeException("Database configuration error", e);
        } finally {
            // Manual resource cleanup - migration opportunity for try-with-resources
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to close properties input stream", e);
                }
            }
        }
    }
    
    /**
     * Initializes database by loading JDBC driver and creating schema.
     * Traditional approach with manual driver loading and exception handling.
     */
    private static void initializeDatabase() {
        try {
            // Manual JDBC driver loading - required in Java 7
            Class.forName(driverClass);
            LOGGER.info("JDBC Driver loaded: " + driverClass);
            
            // Test connection and initialize schema
            Connection testConnection = null;
            try {
                testConnection = createConnection();
                createSchema(testConnection);
                LOGGER.info("Database schema initialized successfully");
            } finally {
                if (testConnection != null) {
                    try {
                        testConnection.close();
                    } catch (SQLException e) {
                        LOGGER.log(Level.WARNING, "Failed to close test connection", e);
                    }
                }
            }
            
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "JDBC Driver not found: " + driverClass, e);
            throw new RuntimeException("Database driver error", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed", e);
            throw new RuntimeException("Database initialization error", e);
        }
    }
    
    /**
     * Creates a new database connection using traditional JDBC approach.
     * Basic connection pooling logic with manual tracking.
     * 
     * Business Logic:
     * - Enforces connection pool limits to prevent resource exhaustion
     * - Provides connection with auto-commit disabled for transaction control
     * - Tracks active connections for monitoring
     * 
     * @return Database connection ready for use
     * @throws SQLException if connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        // Simple connection pool check - migration opportunity
        if (activeConnections >= poolSize) {
            throw new SQLException("Connection pool exhausted. Max connections: " + poolSize);
        }
        
        Connection connection = createConnection();
        connection.setAutoCommit(false); // Enable transaction control
        activeConnections++;
        
        LOGGER.fine("Database connection created. Active connections: " + activeConnections);
        return connection;
    }
    
    /**
     * Creates raw database connection without pool management.
     * Direct JDBC connection creation using traditional approach.
     * 
     * @return Raw database connection
     * @throws SQLException if connection fails
     */
    private static Connection createConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
    
    /**
     * Closes database connection and updates pool tracking.
     * Manual connection lifecycle management.
     * 
     * Business Logic:
     * - Ensures proper connection cleanup to prevent leaks
     * - Updates connection pool counters
     * - Handles rollback for uncommitted transactions
     * 
     * @param connection Connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                // Rollback any uncommitted transactions
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                }
                connection.close();
                activeConnections--;
                LOGGER.fine("Database connection closed. Active connections: " + activeConnections);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }
    
    /**
     * Closes database resources safely with traditional exception handling.
     * Utility method for cleanup in finally blocks.
     * Migration opportunity: try-with-resources auto-cleanup
     * 
     * @param resultSet ResultSet to close
     * @param statement Statement to close
     * @param connection Connection to close
     */
    public static void closeResources(ResultSet resultSet, Statement statement, Connection connection) {
        // Manual resource cleanup - traditional Java 7 approach
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing ResultSet", e);
            }
        }
        
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing Statement", e);
            }
        }
        
        closeConnection(connection);
    }
    
    /**
     * Creates database schema by executing SQL script.
     * Traditional approach with manual SQL execution and resource handling.
     * Migration opportunity: Flyway/Liquibase schema migration tools
     * 
     * @param connection Database connection for schema creation
     * @throws SQLException if schema creation fails
     */
    private static void createSchema(Connection connection) throws SQLException {
        InputStream schemaStream = null;
        Statement statement = null;
        
        try {
            // Load schema SQL from classpath
            schemaStream = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("schema.sql");
                    
            if (schemaStream == null) {
                LOGGER.warning("Schema file not found, skipping schema creation");
                return;
            }
            
            // Read SQL content - traditional approach
            StringBuilder sqlContent = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesRead;
            
            while ((bytesRead = schemaStream.read(buffer)) != -1) {
                sqlContent.append(new String(buffer, 0, bytesRead));
            }
            
            // Execute schema SQL with improved parsing for multi-line statements
            statement = connection.createStatement();
            
            // Split SQL content by semicolons and clean up each statement
            String[] rawStatements = sqlContent.toString().split(";");
            List<String> cleanStatements = new ArrayList<>();
            
            for (String rawStmt : rawStatements) {
                String cleanStmt = cleanStatement(rawStmt);
                if (!cleanStmt.isEmpty()) {
                    cleanStatements.add(cleanStmt);
                }
            }
            
            LOGGER.info("Executing " + cleanStatements.size() + " SQL statements...");
            
            for (int i = 0; i < cleanStatements.size(); i++) {
                String sql = cleanStatements.get(i);
                try {
                    LOGGER.info("Executing SQL [" + (i+1) + "]: " + getStatementSummary(sql));
                    statement.execute(sql);
                    LOGGER.info("Successfully executed statement [" + (i+1) + "]");
                } catch (SQLException sqlEx) {
                    LOGGER.severe("Failed to execute SQL statement [" + (i+1) + "]: " + sql);
                    LOGGER.severe("SQL Error: " + sqlEx.getMessage());
                    LOGGER.severe("Error Code: " + sqlEx.getErrorCode());
                    throw new SQLException("Error executing statement " + (i+1) + ": " + sqlEx.getMessage(), sqlEx);
                }
            }
            
            connection.commit();
            LOGGER.info("Database schema created successfully");
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to read schema file", e);
            throw new SQLException("Schema creation failed", e);
        } finally {
            // Manual cleanup - traditional Java 7 pattern
            if (schemaStream != null) {
                try {
                    schemaStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to close schema stream", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Failed to close schema statement", e);
                }
            }
        }
    }
    
    /**
     * Commits transaction manually - traditional approach.
     * Business logic for ensuring data consistency across operations.
     * 
     * @param connection Connection to commit
     * @throws SQLException if commit fails
     */
    public static void commitTransaction(Connection connection) throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.commit();
            LOGGER.fine("Transaction committed successfully");
        }
    }
    
    /**
     * Rolls back transaction manually - traditional error handling.
     * Business logic for maintaining data integrity on errors.
     * 
     * @param connection Connection to rollback
     */
    public static void rollbackTransaction(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                    LOGGER.fine("Transaction rolled back successfully");
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to rollback transaction", e);
            }
        }
    }
    
    /**
     * Gets current active connection count for monitoring.
     * Simple connection pool monitoring - basic implementation.
     * 
     * @return Number of currently active connections
     */
    public static int getActiveConnectionCount() {
        return activeConnections;
    }
    
    /**
     * Gets maximum connection pool size.
     * Configuration accessor for monitoring and debugging.
     * 
     * @return Maximum allowed connections
     */
    public static int getMaxPoolSize() {
        return poolSize;
    }
    
    /**
     * Cleans a SQL statement by removing comments and extra whitespace.
     * Helper method for improved SQL parsing.
     * 
     * @param rawStatement Raw SQL statement
     * @return Cleaned SQL statement
     */
    private static String cleanStatement(String rawStatement) {
        if (rawStatement == null) {
            return "";
        }
        
        // Remove line comments (--)
        String[] lines = rawStatement.split("\n");
        StringBuilder cleaned = new StringBuilder();
        
        for (String line : lines) {
            // Remove comments and trim
            int commentIndex = line.indexOf("--");
            if (commentIndex >= 0) {
                line = line.substring(0, commentIndex);
            }
            line = line.trim();
            
            if (!line.isEmpty()) {
                if (cleaned.length() > 0) {
                    cleaned.append(" ");
                }
                cleaned.append(line);
            }
        }
        
        return cleaned.toString().trim();
    }
    
    /**
     * Gets a summary of an SQL statement for logging.
     * Helper method to provide readable logging output.
     * 
     * @param statement SQL statement
     * @return Statement summary
     */
    private static String getStatementSummary(String statement) {
        if (statement == null || statement.isEmpty()) {
            return "[EMPTY]";
        }
        
        String trimmed = statement.trim();
        String firstLine = trimmed.split("\n")[0].trim();
        
        if (firstLine.length() > 80) {
            return firstLine.substring(0, 77) + "...";
        }
        
        return firstLine;
    }
}
