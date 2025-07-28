package com.musiclibrary.service;

import com.musiclibrary.dao.AlbumDAO;
import com.musiclibrary.model.Album;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Album Business Service Layer
 * 
 * Provides business logic operations for Album entities using traditional Java 7 service patterns.
 * Albums serve as intermediate entities between artists and songs, representing collections
 * of songs released together by an artist.
 * 
 * Business Logic:
 * - Validates album data before persistence operations
 * - Implements business rules for album management (uniqueness per artist, etc.)
 * - Manages album-artist relationships and referential integrity
 * - Provides search and filtering capabilities with business context
 * - Handles release date validation and chronological ordering
 * - Manages track count consistency with actual song records
 * 
 * Migration Opportunities:
 * - Manual service layer -> Spring Service with @Service annotation
 * - Manual transaction management -> @Transactional annotations
 * - Manual dependency injection -> @Autowired DAO injection
 * - java.util.logging -> SLF4J with structured logging
 * - Manual exception handling -> Spring @ControllerAdvice
 * - Traditional validation -> Bean Validation with @Valid
 * - Date handling -> LocalDate (Java 8+)
 * - Manual DAO instantiation -> Spring dependency injection
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class AlbumService {
    
    // Traditional Java logging - migration opportunity
    private static final Logger LOGGER = Logger.getLogger(AlbumService.class.getName());
    
    // Manual DAO instantiation - migration opportunity to dependency injection
    private final AlbumDAO albumDAO;
    
    /**
     * Constructor with manual dependency injection.
     * Traditional approach - migration opportunity to @Autowired constructor injection.
     */
    public AlbumService() {
        this.albumDAO = new AlbumDAO();
    }
    
    /**
     * Constructor for testing with DAO injection.
     * Allows for mock DAO injection during unit testing.
     * 
     * @param albumDAO DAO instance to use
     */
    public AlbumService(AlbumDAO albumDAO) {
        this.albumDAO = albumDAO;
    }
    
    /**
     * Creates a new album with business validation.
     * 
     * Business Logic:
     * - Validates album data completeness and business rules
     * - Ensures album name is unique per artist
     * - Validates release date is not in the future
     * - Checks artist exists before creating album
     * - Initializes track count to zero for new albums
     * 
     * @param album Album to create
     * @return Created album with generated ID
     * @throws IllegalArgumentException if album data is invalid
     * @throws RuntimeException if database operation fails
     */
    public Album createAlbum(Album album) {
        LOGGER.info("Creating new album: " + (album != null ? album.getAlbumName() : "null"));
        
        try {
            // Business validation before persistence
            validateAlbumForCreation(album);
            
            // Apply business rules and defaults
            applyAlbumBusinessRules(album);
            
            Album createdAlbum = albumDAO.create(album);
            LOGGER.info("Successfully created album with ID: " + createdAlbum.getAlbumId());
            
            return createdAlbum;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error creating album", e);
            
            // Provide business-friendly error messages
            if (e.getMessage().contains("already exists")) {
                throw new RuntimeException("Album '" + album.getAlbumName() + 
                    "' already exists for this artist", e);
            } else {
                throw new RuntimeException("Failed to create album: " + e.getMessage(), e);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid album data: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Retrieves album by ID with business context.
     * 
     * @param albumId Album ID to retrieve
     * @return Album entity or null if not found
     * @throws IllegalArgumentException if albumId is invalid
     * @throws RuntimeException if database operation fails
     */
    public Album getAlbumById(Long albumId) {
        if (albumId == null || albumId <= 0) {
            throw new IllegalArgumentException("Invalid album ID provided");
        }
        
        try {
            Album album = albumDAO.findById(albumId);
            if (album != null) {
                LOGGER.fine("Retrieved album: " + album.getAlbumName());
            } else {
                LOGGER.fine("Album not found with ID: " + albumId);
            }
            
            return album;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving album ID: " + albumId, e);
            throw new RuntimeException("Failed to retrieve album: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves all albums with business context.
     * 
     * Business Logic:
     * - Returns albums ordered by name for consistent user experience
     * - Includes artist name information for complete album context
     * - Logs retrieval for audit and monitoring purposes
     * 
     * @return List of all albums (never null, may be empty)
     * @throws RuntimeException if database operation fails
     */
    public List<Album> getAllAlbums() {
        LOGGER.info("Retrieving all albums");
        
        try {
            List<Album> albums = albumDAO.findAll();
            LOGGER.info("Retrieved " + albums.size() + " albums");
            
            return albums;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving all albums", e);
            throw new RuntimeException("Failed to retrieve albums: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates existing album with business validation.
     * 
     * Business Logic:
     * - Validates album exists and data is valid for update
     * - Checks name uniqueness per artist if name is being changed
     * - Validates artist relationship integrity
     * - Applies business rules for updates
     * - Maintains audit trail with modification timestamps
     * 
     * @param album Album to update
     * @return Updated album entity
     * @throws IllegalArgumentException if album data is invalid
     * @throws RuntimeException if database operation fails
     */
    public Album updateAlbum(Album album) {
        LOGGER.info("Updating album: " + (album != null ? album.getAlbumName() : "null"));
        
        try {
            // Business validation for updates
            validateAlbumForUpdate(album);
            
            // Apply business rules
            applyAlbumBusinessRules(album);
            
            Album updatedAlbum = albumDAO.update(album);
            LOGGER.info("Successfully updated album with ID: " + updatedAlbum.getAlbumId());
            
            return updatedAlbum;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error updating album", e);
            
            // Provide business-friendly error messages
            if (e.getMessage().contains("already exists")) {
                throw new RuntimeException("Album name already exists for this artist", e);
            } else {
                throw new RuntimeException("Failed to update album: " + e.getMessage(), e);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid album update data: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Deletes album by ID with business validation.
     * 
     * Business Logic:
     * - Validates album exists before deletion
     * - Checks for dependent songs (informational only - CASCADE handles cleanup)
     * - Logs deletion for audit purposes
     * - Allows deletion even with associated songs (foreign key CASCADE)
     * 
     * @param albumId Album ID to delete
     * @return true if deleted, false if not found
     * @throws IllegalArgumentException if albumId is invalid
     * @throws RuntimeException if database operation fails
     */
    public boolean deleteAlbum(Long albumId) {
        if (albumId == null || albumId <= 0) {
            throw new IllegalArgumentException("Invalid album ID provided");
        }
        
        LOGGER.info("Deleting album with ID: " + albumId);
        
        try {
            boolean deleted = albumDAO.delete(albumId);
            
            if (deleted) {
                LOGGER.info("Successfully deleted album with ID: " + albumId);
            } else {
                LOGGER.warning("Album not found for deletion with ID: " + albumId);
            }
            
            return deleted;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error deleting album ID: " + albumId, e);
            throw new RuntimeException("Failed to delete album: " + e.getMessage(), e);
        }
    }
    
    /**
     * Searches albums with business context and filtering.
     * 
     * Business Logic:
     * - Provides case-insensitive search across multiple fields
     * - Searches album names, genres, labels, and artist names
     * - Handles empty queries gracefully
     * - Orders results by relevance and name
     * 
     * @param query Search query string
     * @return List of matching albums (never null, may be empty)
     * @throws RuntimeException if database operation fails
     */
    public List<Album> searchAlbums(String query) {
        LOGGER.info("Searching albums with query: " + query);
        
        try {
            List<Album> albums = albumDAO.search(query);
            LOGGER.info("Search returned " + albums.size() + " albums");
            
            return albums;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error searching albums", e);
            throw new RuntimeException("Failed to search albums: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets albums by artist name with business logic.
     * 
     * Business Logic:
     * - Validates artist name before search
     * - Returns albums ordered by release date (newest first)
     * - Provides complete discography for an artist by name
     * 
     * @param artistName Artist name to get albums for
     * @return List of albums by the artist (never null, may be empty)
     * @throws IllegalArgumentException if artist name is invalid
     * @throws RuntimeException if database operation fails
     */
    public List<Album> getAlbumsByArtistName(String artistName) {
        if (artistName == null || artistName.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid artist name provided");
        }
        
        LOGGER.fine("Retrieving albums for artist name: " + artistName);
        
        try {
            List<Album> albums = albumDAO.findByArtist(artistName.trim());
            LOGGER.fine("Retrieved " + albums.size() + " albums for artist: " + artistName);
            
            return albums;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving albums by artist", e);
            throw new RuntimeException("Failed to retrieve albums by artist: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets albums with pagination support.
     * 
     * Business Logic:
     * - Validates pagination parameters for reasonable bounds
     * - Provides consistent ordering for pagination
     * - Handles edge cases (offset beyond total count)
     * 
     * @param page Page number (0-based)
     * @param size Page size (must be positive)
     * @return List of albums for the specified page
     * @throws IllegalArgumentException if pagination parameters are invalid
     * @throws RuntimeException if database operation fails
     */
    public List<Album> getAlbumsWithPagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be non-negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        
        int offset = page * size;
        LOGGER.fine("Retrieving albums with pagination: page=" + page + ", size=" + size);
        
        try {
            List<Album> albums = albumDAO.findWithPagination(offset, size);
            LOGGER.fine("Retrieved " + albums.size() + " albums for page " + page);
            
            return albums;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving paginated albums", e);
            throw new RuntimeException("Failed to retrieve albums: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets total count of albums for pagination support.
     * 
     * @return Total number of albums
     * @throws RuntimeException if database operation fails
     */
    public long getTotalAlbumCount() {
        try {
            long count = albumDAO.count();
            LOGGER.fine("Total album count: " + count);
            
            return count;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error getting album count", e);
            throw new RuntimeException("Failed to get album count: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks if album exists by ID.
     * 
     * Business utility method for validation and conditional logic.
     * 
     * @param albumId Album ID to check
     * @return true if album exists, false otherwise
     * @throws IllegalArgumentException if albumId is invalid
     * @throws RuntimeException if database operation fails
     */
    public boolean albumExists(Long albumId) {
        if (albumId == null || albumId <= 0) {
            throw new IllegalArgumentException("Invalid album ID provided");
        }
        
        try {
            boolean exists = albumDAO.exists(albumId);
            LOGGER.fine("Album existence check for ID " + albumId + ": " + exists);
            
            return exists;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error checking album existence", e);
            throw new RuntimeException("Failed to check album existence: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates album data for creation operations.
     * Implements business rules for new album creation.
     * 
     * @param album Album to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateAlbumForCreation(Album album) {
        if (album == null) {
            throw new IllegalArgumentException("Album cannot be null");
        }
        
        if (!album.isValid()) {
            throw new IllegalArgumentException("Album data is incomplete or invalid");
        }
        
        // Additional business validation
        if (album.getAlbumName() == null || album.getAlbumName().trim().isEmpty()) {
            throw new IllegalArgumentException("Album name is required");
        }
        
        if (album.getArtistId() == null) {
            throw new IllegalArgumentException("Artist ID is required");
        }
        
        // Business rule: Album name should be reasonable length
        if (album.getAlbumName().length() > 255) {
            throw new IllegalArgumentException("Album name is too long (maximum 255 characters)");
        }
        
        // Validate release date if provided
        if (album.getReleaseDate() != null) {
            Date now = new Date();
            if (album.getReleaseDate().after(now)) {
                throw new IllegalArgumentException("Release date cannot be in the future");
            }
            
            // Business rule: Release date should not be too far in the past
            @SuppressWarnings("deprecation")
            int releaseYear = album.getReleaseDate().getYear() + 1900;
            if (releaseYear < 1900) {
                throw new IllegalArgumentException("Release date cannot be before 1900");
            }
        }
        
        // Validate total tracks if provided
        if (album.getTotalTracks() != null && album.getTotalTracks() < 0) {
            throw new IllegalArgumentException("Total tracks cannot be negative");
        }
    }
    
    /**
     * Validates album data for update operations.
     * Implements business rules for album updates.
     * 
     * @param album Album to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateAlbumForUpdate(Album album) {
        if (album == null) {
            throw new IllegalArgumentException("Album cannot be null");
        }
        
        if (album.getAlbumId() == null) {
            throw new IllegalArgumentException("Album ID is required for updates");
        }
        
        // Run creation validation as well
        validateAlbumForCreation(album);
    }
    
    /**
     * Applies business rules and defaults to album entity.
     * Centralizes business logic for album data management.
     * 
     * @param album Album to apply rules to
     */
    private void applyAlbumBusinessRules(Album album) {
        // Initialize default values
        if (album.getTotalTracks() == null) {
            album.setTotalTracks(0);
        }
        
        // Trim and clean text fields
        if (album.getAlbumName() != null) {
            album.setAlbumName(album.getAlbumName().trim());
        }
        
        if (album.getGenre() != null) {
            String genre = album.getGenre().trim();
            if (genre.isEmpty()) {
                album.setGenre(null); // Convert empty strings to null
            } else {
                album.setGenre(genre);
            }
        }
        
        if (album.getRecordLabel() != null) {
            String label = album.getRecordLabel().trim();
            if (label.isEmpty()) {
                album.setRecordLabel(null); // Convert empty strings to null
            } else {
                album.setRecordLabel(label);
            }
        }
        
        if (album.getAlbumArtPath() != null) {
            String artPath = album.getAlbumArtPath().trim();
            if (artPath.isEmpty()) {
                album.setAlbumArtPath(null); // Convert empty strings to null
            } else {
                album.setAlbumArtPath(artPath);
            }
        }
        
        // Business rule: Capitalize genre names
        if (album.getGenre() != null && !album.getGenre().isEmpty()) {
            String genre = album.getGenre();
            if (genre.length() > 1) {
                album.setGenre(genre.substring(0, 1).toUpperCase() + 
                              genre.substring(1).toLowerCase());
            } else {
                album.setGenre(genre.toUpperCase());
            }
        }
    }
    
    /**
     * Gets albums by genre with business logic.
     * 
     * Business Logic:
     * - Validates genre name before search
     * - Returns albums ordered by name within genre
     * - Provides genre-based filtering for music discovery
     * - Handles genre name variations and case-insensitive matching
     * 
     * @param genre Genre name to get albums for
     * @return List of albums in the genre (never null, may be empty)
     * @throws IllegalArgumentException if genre name is invalid
     * @throws RuntimeException if database operation fails
     */
    public List<Album> getAlbumsByGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid genre name provided");
        }
        
        LOGGER.fine("Retrieving albums for genre: " + genre);
        
        try {
            List<Album> albums = albumDAO.findByGenre(genre.trim());
            LOGGER.fine("Retrieved " + albums.size() + " albums for genre: " + genre);
            
            return albums;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving albums by genre", e);
            throw new RuntimeException("Failed to retrieve albums by genre: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets albums by release year with business logic.
     * 
     * Business Logic:
     * - Validates year is reasonable (not negative or far future)
     * - Returns albums ordered by name within year
     * - Provides chronological filtering for music discovery
     * - Handles year-based browsing and filtering
     * 
     * @param year Release year to get albums for
     * @return List of albums released in the year (never null, may be empty)
     * @throws IllegalArgumentException if year is invalid
     * @throws RuntimeException if database operation fails
     */
    public List<Album> getAlbumsByYear(int year) {
        if (year < 1900 || year > 2030) {
            throw new IllegalArgumentException("Invalid release year: " + year);
        }
        
        LOGGER.fine("Retrieving albums for year: " + year);
        
        
        try {
            List<Album> albums = albumDAO.findByYear(year);
            LOGGER.fine("Retrieved " + albums.size() + " albums for year: " + year);
            
            return albums;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving albums by year", e);
            throw new RuntimeException("Failed to retrieve albums by year: " + e.getMessage(), e);
        }
    }
}
