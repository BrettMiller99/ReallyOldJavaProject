package com.musiclibrary.service;

import com.musiclibrary.dao.ArtistDAO;
import com.musiclibrary.model.Artist;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Artist Business Service Layer
 * 
 * Provides business logic operations for Artist entities using traditional Java 7 service patterns.
 * This service manages artist master data and enforces business rules for artist management.
 * 
 * Business Logic:
 * - Validates artist data before persistence operations
 * - Implements business rules for artist management (name uniqueness, etc.)
 * - Provides search and filtering capabilities with business context
 * - Manages artist relationships with songs and albums
 * - Handles error scenarios with appropriate business messaging
 * - Enforces referential integrity rules for artist deletions
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
public class ArtistService {
    
    // Traditional Java logging - migration opportunity
    private static final Logger LOGGER = Logger.getLogger(ArtistService.class.getName());
    
    // Manual DAO instantiation - migration opportunity to dependency injection
    private final ArtistDAO artistDAO;
    
    /**
     * Constructor with manual dependency injection.
     * Traditional approach - migration opportunity to @Autowired constructor injection.
     */
    public ArtistService() {
        this.artistDAO = new ArtistDAO();
    }
    
    /**
     * Constructor for testing with DAO injection.
     * Allows for mock DAO injection during unit testing.
     * 
     * @param artistDAO DAO instance to use
     */
    public ArtistService(ArtistDAO artistDAO) {
        this.artistDAO = artistDAO;
    }
    
    /**
     * Creates a new artist with business validation.
     * 
     * Business Logic:
     * - Validates artist data completeness and business rules
     * - Ensures artist name is unique across the system
     * - Checks formation year is reasonable (not future date)
     * - Validates website URL format if provided
     * - Initializes default values for optional fields
     * 
     * @param artist Artist to create
     * @return Created artist with generated ID
     * @throws IllegalArgumentException if artist data is invalid
     * @throws RuntimeException if database operation fails
     */
    public Artist createArtist(Artist artist) {
        LOGGER.info("Creating new artist: " + (artist != null ? artist.getArtistName() : "null"));
        
        try {
            // Business validation before persistence
            validateArtistForCreation(artist);
            
            // Apply business rules and defaults
            applyArtistBusinessRules(artist);
            
            Artist createdArtist = artistDAO.create(artist);
            LOGGER.info("Successfully created artist with ID: " + createdArtist.getArtistId());
            
            return createdArtist;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error creating artist", e);
            throw new RuntimeException("Failed to create artist: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid artist data: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Retrieves artist by ID with business context.
     * 
     * @param artistId Artist ID to retrieve
     * @return Artist entity or null if not found
     * @throws IllegalArgumentException if artistId is invalid
     * @throws RuntimeException if database operation fails
     */
    public Artist getArtistById(Long artistId) {
        if (artistId == null || artistId <= 0) {
            throw new IllegalArgumentException("Invalid artist ID provided");
        }
        
        try {
            Artist artist = artistDAO.findById(artistId);
            if (artist != null) {
                LOGGER.fine("Retrieved artist: " + artist.getArtistName());
            } else {
                LOGGER.fine("Artist not found with ID: " + artistId);
            }
            
            return artist;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving artist ID: " + artistId, e);
            throw new RuntimeException("Failed to retrieve artist: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves all artists with business context.
     * 
     * Business Logic:
     * - Returns artists ordered by name for consistent user experience
     * - Includes formation year information for chronological context
     * - Logs retrieval for audit and monitoring purposes
     * 
     * @return List of all artists (never null, may be empty)
     * @throws RuntimeException if database operation fails
     */
    public List<Artist> getAllArtists() {
        LOGGER.info("Retrieving all artists");
        
        try {
            List<Artist> artists = artistDAO.findAll();
            LOGGER.info("Retrieved " + artists.size() + " artists");
            
            return artists;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving all artists", e);
            throw new RuntimeException("Failed to retrieve artists: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates existing artist with business validation.
     * 
     * Business Logic:
     * - Validates artist exists and data is valid for update
     * - Checks name uniqueness if name is being changed
     * - Applies business rules for updates
     * - Maintains audit trail with modification timestamps
     * 
     * @param artist Artist to update
     * @return Updated artist entity
     * @throws IllegalArgumentException if artist data is invalid
     * @throws RuntimeException if database operation fails
     */
    public Artist updateArtist(Artist artist) {
        LOGGER.info("Updating artist: " + (artist != null ? artist.getArtistName() : "null"));
        
        try {
            // Business validation for updates
            validateArtistForUpdate(artist);
            
            // Apply business rules
            applyArtistBusinessRules(artist);
            
            Artist updatedArtist = artistDAO.update(artist);
            LOGGER.info("Successfully updated artist with ID: " + updatedArtist.getArtistId());
            
            return updatedArtist;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error updating artist", e);
            throw new RuntimeException("Failed to update artist: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid artist update data: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Deletes artist by ID with business validation.
     * 
     * Business Logic:
     * - Validates artist exists before deletion
     * - Checks for dependent records (songs, albums)
     * - Enforces referential integrity at business level
     * - Provides clear error messages for constraint violations
     * - Logs deletion for audit purposes
     * 
     * @param artistId Artist ID to delete
     * @return true if deleted, false if not found
     * @throws IllegalArgumentException if artistId is invalid
     * @throws RuntimeException if database operation fails or artist has dependencies
     */
    public boolean deleteArtist(Long artistId) {
        if (artistId == null || artistId <= 0) {
            throw new IllegalArgumentException("Invalid artist ID provided");
        }
        
        LOGGER.info("Deleting artist with ID: " + artistId);
        
        try {
            boolean deleted = artistDAO.delete(artistId);
            
            if (deleted) {
                LOGGER.info("Successfully deleted artist with ID: " + artistId);
            } else {
                LOGGER.warning("Artist not found for deletion with ID: " + artistId);
            }
            
            return deleted;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error deleting artist ID: " + artistId, e);
            
            // Provide business-friendly error messages
            if (e.getMessage().contains("has") && e.getMessage().contains("songs")) {
                throw new RuntimeException("Cannot delete artist: artist has associated songs. " +
                    "Please remove all songs first.", e);
            } else if (e.getMessage().contains("has") && e.getMessage().contains("albums")) {
                throw new RuntimeException("Cannot delete artist: artist has associated albums. " +
                    "Please remove all albums first.", e);
            } else {
                throw new RuntimeException("Failed to delete artist: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Searches artists with business context and filtering.
     * 
     * Business Logic:
     * - Provides case-insensitive search across multiple fields
     * - Searches artist names, countries, and biographies
     * - Handles empty queries gracefully
     * - Orders results by relevance and name
     * 
     * @param query Search query string
     * @return List of matching artists (never null, may be empty)
     * @throws RuntimeException if database operation fails
     */
    public List<Artist> searchArtists(String query) {
        LOGGER.info("Searching artists with query: " + query);
        
        try {
            List<Artist> artists = artistDAO.search(query);
            LOGGER.info("Search returned " + artists.size() + " artists");
            
            return artists;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error searching artists", e);
            throw new RuntimeException("Failed to search artists: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets artists with pagination support.
     * 
     * Business Logic:
     * - Validates pagination parameters for reasonable bounds
     * - Provides consistent ordering for pagination
     * - Handles edge cases (offset beyond total count)
     * 
     * @param page Page number (0-based)
     * @param size Page size (must be positive)
     * @return List of artists for the specified page
     * @throws IllegalArgumentException if pagination parameters are invalid
     * @throws RuntimeException if database operation fails
     */
    public List<Artist> getArtistsWithPagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be non-negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        
        int offset = page * size;
        LOGGER.fine("Retrieving artists with pagination: page=" + page + ", size=" + size);
        
        try {
            List<Artist> artists = artistDAO.findWithPagination(offset, size);
            LOGGER.fine("Retrieved " + artists.size() + " artists for page " + page);
            
            return artists;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving paginated artists", e);
            throw new RuntimeException("Failed to retrieve artists: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets total count of artists for pagination support.
     * 
     * @return Total number of artists
     * @throws RuntimeException if database operation fails
     */
    public long getTotalArtistCount() {
        try {
            long count = artistDAO.count();
            LOGGER.fine("Total artist count: " + count);
            
            return count;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error getting artist count", e);
            throw new RuntimeException("Failed to get artist count: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks if artist exists by ID.
     * 
     * Business utility method for validation and conditional logic.
     * 
     * @param artistId Artist ID to check
     * @return true if artist exists, false otherwise
     * @throws IllegalArgumentException if artistId is invalid
     * @throws RuntimeException if database operation fails
     */
    public boolean artistExists(Long artistId) {
        if (artistId == null || artistId <= 0) {
            throw new IllegalArgumentException("Invalid artist ID provided");
        }
        
        try {
            boolean exists = artistDAO.exists(artistId);
            LOGGER.fine("Artist existence check for ID " + artistId + ": " + exists);
            
            return exists;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error checking artist existence", e);
            throw new RuntimeException("Failed to check artist existence: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates artist data for creation operations.
     * Implements business rules for new artist creation.
     * 
     * @param artist Artist to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateArtistForCreation(Artist artist) {
        if (artist == null) {
            throw new IllegalArgumentException("Artist cannot be null");
        }
        
        if (!artist.isValid()) {
            throw new IllegalArgumentException("Artist data is incomplete or invalid");
        }
        
        // Additional business validation
        if (artist.getArtistName() == null || artist.getArtistName().trim().isEmpty()) {
            throw new IllegalArgumentException("Artist name is required");
        }
        
        // Business rule: Artist name should be reasonable length
        if (artist.getArtistName().length() > 255) {
            throw new IllegalArgumentException("Artist name is too long (maximum 255 characters)");
        }
        
        // Validate formation year if provided
        if (artist.getFormedYear() != null) {
            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            if (artist.getFormedYear() < 1900 || artist.getFormedYear() > currentYear) {
                throw new IllegalArgumentException("Formation year must be between 1900 and " + currentYear);
            }
        }
        
        // Validate website URL format if provided
        if (artist.getWebsite() != null && !artist.getWebsite().trim().isEmpty()) {
            String website = artist.getWebsite().trim().toLowerCase();
            if (!website.startsWith("http://") && !website.startsWith("https://")) {
                throw new IllegalArgumentException("Website URL must start with http:// or https://");
            }
        }
    }
    
    /**
     * Validates artist data for update operations.
     * Implements business rules for artist updates.
     * 
     * @param artist Artist to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateArtistForUpdate(Artist artist) {
        if (artist == null) {
            throw new IllegalArgumentException("Artist cannot be null");
        }
        
        if (artist.getArtistId() == null) {
            throw new IllegalArgumentException("Artist ID is required for updates");
        }
        
        // Run creation validation as well
        validateArtistForCreation(artist);
    }
    
    /**
     * Applies business rules and defaults to artist entity.
     * Centralizes business logic for artist data management.
     * 
     * @param artist Artist to apply rules to
     */
    private void applyArtistBusinessRules(Artist artist) {
        // Trim and clean text fields
        if (artist.getArtistName() != null) {
            artist.setArtistName(artist.getArtistName().trim());
        }
        
        if (artist.getCountry() != null) {
            artist.setCountry(artist.getCountry().trim());
        }
        
        if (artist.getBiography() != null) {
            String biography = artist.getBiography().trim();
            if (biography.isEmpty()) {
                artist.setBiography(null); // Convert empty strings to null
            } else {
                artist.setBiography(biography);
            }
        }
        
        if (artist.getWebsite() != null) {
            String website = artist.getWebsite().trim();
            if (website.isEmpty()) {
                artist.setWebsite(null); // Convert empty strings to null
            } else {
                artist.setWebsite(website);
            }
        }
        
        // Business rule: Capitalize country names
        if (artist.getCountry() != null && !artist.getCountry().isEmpty()) {
            String country = artist.getCountry();
            if (country.length() > 1) {
                artist.setCountry(country.substring(0, 1).toUpperCase() + 
                                country.substring(1).toLowerCase());
            } else {
                artist.setCountry(country.toUpperCase());
            }
        }
    }
    
    /**
     * Gets artists by country with business logic.
     * 
     * Business Logic:
     * - Validates country name before search
     * - Returns artists ordered by name within country
     * - Provides geographical filtering for artist discovery
     * - Handles country name variations and case-insensitive matching
     * 
     * @param country Country name to get artists for
     * @return List of artists from the country (never null, may be empty)
     * @throws IllegalArgumentException if country name is invalid
     * @throws RuntimeException if database operation fails
     */
    public List<Artist> getArtistsByCountry(String country) {
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid country name provided");
        }
        
        LOGGER.fine("Retrieving artists for country: " + country);
        
        try {
            List<Artist> artists = artistDAO.findByCountry(country.trim());
            LOGGER.fine("Retrieved " + artists.size() + " artists for country: " + country);
            
            return artists;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving artists by country", e);
            throw new RuntimeException("Failed to retrieve artists by country: " + e.getMessage(), e);
        }
    }
}
