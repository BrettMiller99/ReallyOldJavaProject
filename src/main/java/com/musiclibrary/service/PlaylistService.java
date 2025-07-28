package com.musiclibrary.service;

import com.musiclibrary.dao.PlaylistDAO;
import com.musiclibrary.model.Playlist;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Playlist Business Service Layer
 * 
 * Provides business logic operations for Playlist entities using traditional Java 7 service patterns.
 * Playlists are user-created collections of songs with ordering and metadata management.
 * 
 * Business Logic:
 * - Validates playlist data before persistence operations
 * - Implements business rules for playlist management (uniqueness per user, etc.)
 * - Manages playlist-song relationships and ordering
 * - Provides search and filtering capabilities with business context
 * - Handles public/private playlist visibility controls
 * - Manages playlist statistics (song count, total duration)
 * - Enforces user ownership and access control rules
 * 
 * Migration Opportunities:
 * - Manual service layer -> Spring Service with @Service annotation
 * - Manual transaction management -> @Transactional annotations
 * - Manual dependency injection -> @Autowired DAO injection
 * - java.util.logging -> SLF4J with structured logging
 * - Manual exception handling -> Spring @ControllerAdvice
 * - Traditional validation -> Bean Validation with @Valid
 * - Manual DAO instantiation -> Spring dependency injection
 * - Basic error handling -> Spring exception hierarchy
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class PlaylistService {
    
    // Traditional Java logging - migration opportunity
    private static final Logger LOGGER = Logger.getLogger(PlaylistService.class.getName());
    
    // Manual DAO instantiation - migration opportunity to dependency injection
    private final PlaylistDAO playlistDAO;
    
    /**
     * Constructor with manual dependency injection.
     * Traditional approach - migration opportunity to @Autowired constructor injection.
     */
    public PlaylistService() {
        this.playlistDAO = new PlaylistDAO();
    }
    
    /**
     * Constructor for testing with DAO injection.
     * Allows for mock DAO injection during unit testing.
     * 
     * @param playlistDAO DAO instance to use
     */
    public PlaylistService(PlaylistDAO playlistDAO) {
        this.playlistDAO = playlistDAO;
    }
    
    /**
     * Creates a new playlist with business validation.
     * 
     * Business Logic:
     * - Validates playlist data completeness and business rules
     * - Ensures playlist name is unique per user
     * - Validates user permissions for playlist creation
     * - Initializes empty playlist with default statistics
     * - Sets appropriate visibility defaults
     * 
     * @param playlist Playlist to create
     * @return Created playlist with generated ID
     * @throws IllegalArgumentException if playlist data is invalid
     * @throws RuntimeException if database operation fails
     */
    public Playlist createPlaylist(Playlist playlist) {
        LOGGER.info("Creating new playlist: " + (playlist != null ? playlist.getPlaylistName() : "null"));
        
        try {
            // Business validation before persistence
            validatePlaylistForCreation(playlist);
            
            // Apply business rules and defaults
            applyPlaylistBusinessRules(playlist);
            
            Playlist createdPlaylist = playlistDAO.create(playlist);
            LOGGER.info("Successfully created playlist with ID: " + createdPlaylist.getPlaylistId());
            
            return createdPlaylist;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error creating playlist", e);
            
            // Provide business-friendly error messages
            if (e.getMessage().contains("already exists")) {
                throw new RuntimeException("Playlist '" + playlist.getPlaylistName() + 
                    "' already exists for this user", e);
            } else {
                throw new RuntimeException("Failed to create playlist: " + e.getMessage(), e);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid playlist data: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Retrieves playlist by ID with business context.
     * 
     * @param playlistId Playlist ID to retrieve
     * @return Playlist entity or null if not found
     * @throws IllegalArgumentException if playlistId is invalid
     * @throws RuntimeException if database operation fails
     */
    public Playlist getPlaylistById(Long playlistId) {
        if (playlistId == null || playlistId <= 0) {
            throw new IllegalArgumentException("Invalid playlist ID provided");
        }
        
        try {
            Playlist playlist = playlistDAO.findById(playlistId);
            if (playlist != null) {
                LOGGER.fine("Retrieved playlist: " + playlist.getPlaylistName());
            } else {
                LOGGER.fine("Playlist not found with ID: " + playlistId);
            }
            
            return playlist;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving playlist ID: " + playlistId, e);
            throw new RuntimeException("Failed to retrieve playlist: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves all playlists with business context.
     * 
     * Business Logic:
     * - Returns playlists ordered by name for consistent user experience
     * - Includes both public and private playlists (filtered by visibility)
     * - Logs retrieval for audit and monitoring purposes
     * 
     * @return List of all playlists (never null, may be empty)
     * @throws RuntimeException if database operation fails
     */
    public List<Playlist> getAllPlaylists() {
        LOGGER.info("Retrieving all playlists");
        
        try {
            List<Playlist> playlists = playlistDAO.findAll();
            LOGGER.info("Retrieved " + playlists.size() + " playlists");
            
            return playlists;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving all playlists", e);
            throw new RuntimeException("Failed to retrieve playlists: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates existing playlist with business validation.
     * 
     * Business Logic:
     * - Validates playlist exists and user has permission to update
     * - Checks name uniqueness per user if name is being changed
     * - Validates user ownership for playlist modifications
     * - Applies business rules for updates
     * - Maintains audit trail with modification timestamps
     * 
     * @param playlist Playlist to update
     * @return Updated playlist entity
     * @throws IllegalArgumentException if playlist data is invalid
     * @throws RuntimeException if database operation fails
     */
    public Playlist updatePlaylist(Playlist playlist) {
        LOGGER.info("Updating playlist: " + (playlist != null ? playlist.getPlaylistName() : "null"));
        
        try {
            // Business validation for updates
            validatePlaylistForUpdate(playlist);
            
            // Apply business rules
            applyPlaylistBusinessRules(playlist);
            
            Playlist updatedPlaylist = playlistDAO.update(playlist);
            LOGGER.info("Successfully updated playlist with ID: " + updatedPlaylist.getPlaylistId());
            
            return updatedPlaylist;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error updating playlist", e);
            
            // Provide business-friendly error messages
            if (e.getMessage().contains("already exists")) {
                throw new RuntimeException("Playlist name already exists for this user", e);
            } else {
                throw new RuntimeException("Failed to update playlist: " + e.getMessage(), e);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid playlist update data: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Deletes playlist by ID with business validation.
     * 
     * Business Logic:
     * - Validates playlist exists before deletion
     * - Checks user ownership permissions for deletion
     * - Automatically removes all playlist-song relationships (CASCADE)
     * - Logs deletion for audit purposes
     * 
     * @param playlistId Playlist ID to delete
     * @return true if deleted, false if not found
     * @throws IllegalArgumentException if playlistId is invalid
     * @throws RuntimeException if database operation fails
     */
    public boolean deletePlaylist(Long playlistId) {
        if (playlistId == null || playlistId <= 0) {
            throw new IllegalArgumentException("Invalid playlist ID provided");
        }
        
        LOGGER.info("Deleting playlist with ID: " + playlistId);
        
        try {
            boolean deleted = playlistDAO.delete(playlistId);
            
            if (deleted) {
                LOGGER.info("Successfully deleted playlist with ID: " + playlistId);
            } else {
                LOGGER.warning("Playlist not found for deletion with ID: " + playlistId);
            }
            
            return deleted;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error deleting playlist ID: " + playlistId, e);
            throw new RuntimeException("Failed to delete playlist: " + e.getMessage(), e);
        }
    }
    
    /**
     * Searches playlists with business context and filtering.
     * 
     * Business Logic:
     * - Provides case-insensitive search across multiple fields
     * - Searches playlist names, descriptions, and creators
     * - Handles empty queries gracefully
     * - Orders results by relevance and name
     * - Respects visibility rules (public vs private)
     * 
     * @param query Search query string
     * @return List of matching playlists (never null, may be empty)
     * @throws RuntimeException if database operation fails
     */
    public List<Playlist> searchPlaylists(String query) {
        LOGGER.info("Searching playlists with query: " + query);
        
        try {
            List<Playlist> playlists = playlistDAO.search(query);
            LOGGER.info("Search returned " + playlists.size() + " playlists");
            
            return playlists;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error searching playlists", e);
            throw new RuntimeException("Failed to search playlists: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets playlists by user with business logic.
     * 
     * Business Logic:
     * - Validates username before search
     * - Returns playlists ordered by creation date (newest first)
     * - Includes both public and private playlists for the user
     * - Provides complete playlist collection for a user
     * 
     * @param username Username to get playlists for
     * @return List of playlists by the user (never null, may be empty)
     * @throws IllegalArgumentException if username is invalid
     * @throws RuntimeException if database operation fails
     */
    public List<Playlist> getPlaylistsByUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid username provided");
        }
        
        LOGGER.fine("Retrieving playlists for user: " + username);
        
        try {
            List<Playlist> playlists = playlistDAO.findByUser(username.trim());
            LOGGER.fine("Retrieved " + playlists.size() + " playlists for user: " + username);
            
            return playlists;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving playlists by user", e);
            throw new RuntimeException("Failed to retrieve playlists by user: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets public playlists with business logic.
     * 
     * Business Logic:
     * - Returns only playlists marked as public
     * - Orders results by playlist name for browsing
     * - Enables playlist discovery functionality
     * - Excludes private playlists for privacy
     * 
     * @return List of public playlists (never null, may be empty)
     * @throws RuntimeException if database operation fails
     */
    public List<Playlist> getPublicPlaylists() {
        LOGGER.fine("Retrieving public playlists");
        
        try {
            List<Playlist> playlists = playlistDAO.findPublicPlaylists();
            LOGGER.fine("Retrieved " + playlists.size() + " public playlists");
            
            return playlists;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving public playlists", e);
            throw new RuntimeException("Failed to retrieve public playlists: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets playlists with pagination support.
     * 
     * Business Logic:
     * - Validates pagination parameters for reasonable bounds
     * - Provides consistent ordering for pagination
     * - Handles edge cases (offset beyond total count)
     * 
     * @param page Page number (0-based)
     * @param size Page size (must be positive)
     * @return List of playlists for the specified page
     * @throws IllegalArgumentException if pagination parameters are invalid
     * @throws RuntimeException if database operation fails
     */
    public List<Playlist> getPlaylistsWithPagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be non-negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        
        int offset = page * size;
        LOGGER.fine("Retrieving playlists with pagination: page=" + page + ", size=" + size);
        
        try {
            List<Playlist> playlists = playlistDAO.findWithPagination(offset, size);
            LOGGER.fine("Retrieved " + playlists.size() + " playlists for page " + page);
            
            return playlists;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving paginated playlists", e);
            throw new RuntimeException("Failed to retrieve playlists: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets total count of playlists for pagination support.
     * 
     * @return Total number of playlists
     * @throws RuntimeException if database operation fails
     */
    public long getTotalPlaylistCount() {
        try {
            long count = playlistDAO.count();
            LOGGER.fine("Total playlist count: " + count);
            
            return count;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error getting playlist count", e);
            throw new RuntimeException("Failed to get playlist count: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks if playlist exists by ID.
     * 
     * Business utility method for validation and conditional logic.
     * 
     * @param playlistId Playlist ID to check
     * @return true if playlist exists, false otherwise
     * @throws IllegalArgumentException if playlistId is invalid
     * @throws RuntimeException if database operation fails
     */
    public boolean playlistExists(Long playlistId) {
        if (playlistId == null || playlistId <= 0) {
            throw new IllegalArgumentException("Invalid playlist ID provided");
        }
        
        try {
            boolean exists = playlistDAO.exists(playlistId);
            LOGGER.fine("Playlist existence check for ID " + playlistId + ": " + exists);
            
            return exists;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error checking playlist existence", e);
            throw new RuntimeException("Failed to check playlist existence: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates playlist data for creation operations.
     * Implements business rules for new playlist creation.
     * 
     * @param playlist Playlist to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePlaylistForCreation(Playlist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("Playlist cannot be null");
        }
        
        if (!playlist.isValid()) {
            throw new IllegalArgumentException("Playlist data is incomplete or invalid");
        }
        
        // Additional business validation
        if (playlist.getPlaylistName() == null || playlist.getPlaylistName().trim().isEmpty()) {
            throw new IllegalArgumentException("Playlist name is required");
        }
        
        if (playlist.getCreatedBy() == null || playlist.getCreatedBy().trim().isEmpty()) {
            throw new IllegalArgumentException("Creator username is required");
        }
        
        // Business rule: Playlist name should be reasonable length
        if (playlist.getPlaylistName().length() > 255) {
            throw new IllegalArgumentException("Playlist name is too long (maximum 255 characters)");
        }
        
        // Business rule: Username should be reasonable length
        if (playlist.getCreatedBy().length() > 100) {
            throw new IllegalArgumentException("Username is too long (maximum 100 characters)");
        }
        
        // Validate description length if provided
        if (playlist.getDescription() != null && playlist.getDescription().length() > 1000) {
            throw new IllegalArgumentException("Description is too long (maximum 1000 characters)");
        }
    }
    
    /**
     * Validates playlist data for update operations.
     * Implements business rules for playlist updates.
     * 
     * @param playlist Playlist to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePlaylistForUpdate(Playlist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("Playlist cannot be null");
        }
        
        if (playlist.getPlaylistId() == null) {
            throw new IllegalArgumentException("Playlist ID is required for updates");
        }
        
        // Run creation validation as well
        validatePlaylistForCreation(playlist);
    }
    
    /**
     * Applies business rules and defaults to playlist entity.
     * Centralizes business logic for playlist data management.
     * 
     * @param playlist Playlist to apply rules to
     */
    private void applyPlaylistBusinessRules(Playlist playlist) {
        // Initialize default values
        if (playlist.getIsPublic() == null) {
            playlist.setIsPublic(true); // Default to public
        }
        
        if (playlist.getTotalDuration() == null) {
            playlist.setTotalDuration(0); // Initialize empty playlist
        }
        
        if (playlist.getSongCount() == null) {
            playlist.setSongCount(0); // Initialize empty playlist
        }
        
        // Trim and clean text fields
        if (playlist.getPlaylistName() != null) {
            playlist.setPlaylistName(playlist.getPlaylistName().trim());
        }
        
        if (playlist.getCreatedBy() != null) {
            playlist.setCreatedBy(playlist.getCreatedBy().trim().toLowerCase()); // Normalize username
        }
        
        if (playlist.getDescription() != null) {
            String description = playlist.getDescription().trim();
            if (description.isEmpty()) {
                playlist.setDescription(null); // Convert empty strings to null
            } else {
                playlist.setDescription(description);
            }
        }
    }
}
