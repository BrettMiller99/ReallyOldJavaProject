package com.musiclibrary.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Base Data Access Object Interface
 * 
 * Defines common CRUD operations for all entities in the music library system.
 * This interface follows traditional Java 7 DAO pattern widely used in enterprise applications.
 * 
 * Business Logic:
 * - Provides standard database operations (Create, Read, Update, Delete)
 * - Enforces consistent data access patterns across all entities
 * - Supports both single entity and bulk operations
 * - Enables transaction management through SQLException propagation
 * - Provides search and filtering capabilities for business requirements
 * 
 * Migration Opportunities:
 * - Traditional DAO pattern -> Spring Data JPA repositories
 * - Manual SQLException handling -> @Transactional annotations
 * - Raw SQL operations -> JPA query methods
 * - Manual entity mapping -> JPA entity annotations
 * - Basic List return types -> Spring Data Page/Slice
 * - Traditional interfaces -> Spring Data query derivation
 * 
 * @param <T> Entity type that this DAO manages
 * @param <ID> Primary key type for the entity
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public interface BaseDAO<T, ID> {
    
    /**
     * Creates a new entity in the database.
     * Business rule: Entity must pass validation before persistence.
     * 
     * @param entity Entity to create (must not be null)
     * @return Created entity with generated ID populated
     * @throws SQLException if database operation fails
     * @throws IllegalArgumentException if entity is invalid
     */
    T create(T entity) throws SQLException;
    
    /**
     * Retrieves entity by primary key.
     * Business rule: Returns null if entity does not exist.
     * 
     * @param id Primary key of entity to retrieve
     * @return Entity instance or null if not found
     * @throws SQLException if database operation fails
     */
    T findById(ID id) throws SQLException;
    
    /**
     * Retrieves all entities of this type.
     * Business rule: Returns empty list if no entities exist.
     * Migration opportunity: Add pagination support.
     * 
     * @return List of all entities (never null, may be empty)
     * @throws SQLException if database operation fails
     */
    List<T> findAll() throws SQLException;
    
    /**
     * Updates existing entity in database.
     * Business rule: Entity must exist and pass validation.
     * 
     * @param entity Entity to update (must have valid ID)
     * @return Updated entity instance
     * @throws SQLException if database operation fails
     * @throws IllegalArgumentException if entity is invalid
     */
    T update(T entity) throws SQLException;
    
    /**
     * Deletes entity by primary key.
     * Business rule: No error if entity does not exist.
     * 
     * @param id Primary key of entity to delete
     * @return true if entity was deleted, false if not found
     * @throws SQLException if database operation fails
     */
    boolean delete(ID id) throws SQLException;
    
    /**
     * Checks if entity exists by primary key.
     * Business utility method for validation and conditional logic.
     * 
     * @param id Primary key to check
     * @return true if entity exists, false otherwise
     * @throws SQLException if database operation fails
     */
    boolean exists(ID id) throws SQLException;
    
    /**
     * Counts total number of entities.
     * Business method for pagination and analytics.
     * 
     * @return Total count of entities
     * @throws SQLException if database operation fails
     */
    long count() throws SQLException;
    
    /**
     * Searches entities by text query.
     * Business requirement for user search functionality.
     * Implementation varies by entity type.
     * 
     * @param query Search text (case-insensitive)
     * @return List of matching entities (never null, may be empty)
     * @throws SQLException if database operation fails
     */
    List<T> search(String query) throws SQLException;
    
    /**
     * Retrieves entities with pagination support.
     * Business method for large dataset handling.
     * Migration opportunity: Spring Data Pageable interface.
     * 
     * @param offset Starting position (0-based)
     * @param limit Maximum number of results
     * @return List of entities within specified range
     * @throws SQLException if database operation fails
     * @throws IllegalArgumentException if offset/limit are invalid
     */
    List<T> findWithPagination(int offset, int limit) throws SQLException;
}
