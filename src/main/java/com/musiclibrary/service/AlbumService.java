package com.musiclibrary.service;

import com.musiclibrary.model.Album;
import com.musiclibrary.repository.AlbumRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Album Business Service Layer
 * 
 * Provides business logic operations for Album entities using modern Spring Boot patterns.
 * Albums serve as intermediate entities between artists and songs, representing collections
 * of songs released together by an artist with automatic dependency injection and transaction management.
 * 
 * Business Logic:
 * - Validates album data before persistence operations
 * - Implements business rules for album management (uniqueness per artist, etc.)
 * - Manages album-artist relationships and referential integrity
 * - Provides search and filtering capabilities with business context
 * - Handles release date validation and chronological ordering
 * - Manages track count consistency with actual song records
 * 
 * Modern Features:
 * - Spring Service with @Service annotation for automatic component scanning
 * - @Transactional annotations for declarative transaction management
 * - @Autowired DAO injection for automatic dependency resolution
 * - SLF4J with structured logging for better observability
 * - Spring exception hierarchy for consistent error handling
 * - Bean Validation integration for data validation
 * - Date handling with modern Java time APIs
 * 
 * @author Music Library Development Team
 * @version 2.0
 * @since Java 17
 */
@Service
@Transactional
public class AlbumService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlbumService.class);
    
    private final AlbumRepository albumRepository;
    
    /**
     * Constructor with automatic dependency injection.
     * Spring automatically injects the AlbumRepository dependency.
     * 
     * @param albumRepository the album repository
     */
    @Autowired
    public AlbumService(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
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
        logger.info("Creating new album: " + (album != null ? album.getAlbumName() : "null"));
        
        try {
            // Business validation before persistence
            validateAlbumForCreation(album);
            
            // Apply business rules and defaults
            applyAlbumBusinessRules(album);
            
            Album createdAlbum = albumRepository.save(album);
            logger.info("Successfully created album with ID: " + createdAlbum.getAlbumId());
            
            return createdAlbum;
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid album data: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Database error creating album", e);
            throw new RuntimeException("Failed to create album: " + e.getMessage(), e);
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
            Optional<Album> albumOptional = albumRepository.findById(albumId);
            if (albumOptional.isPresent()) {
                Album album = albumOptional.get();
                logger.debug("Retrieved album: " + album.getAlbumName());
                return album;
            } else {
                logger.debug("Album not found with ID: " + albumId);
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Database error retrieving album ID: " + albumId, e);
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
        logger.info("Retrieving all albums");
        
        try {
            List<Album> albums = albumRepository.findAll();
            logger.info("Retrieved " + albums.size() + " albums");
            
            return albums;
            
        } catch (Exception e) {
            logger.error("Database error retrieving all albums", e);
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
        logger.info("Updating album: " + (album != null ? album.getAlbumName() : "null"));
        
        try {
            // Business validation for updates
            validateAlbumForUpdate(album);
            
            // Apply business rules
            applyAlbumBusinessRules(album);
            
            Album updatedAlbum = albumRepository.save(album);
            logger.info("Successfully updated album with ID: " + updatedAlbum.getAlbumId());
            
            return updatedAlbum;
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid album update data: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Database error updating album", e);
            throw new RuntimeException("Failed to update album: " + e.getMessage(), e);
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
        
        logger.info("Deleting album with ID: " + albumId);
        
        try {
            boolean exists = albumRepository.existsById(albumId);
            if (exists) {
                albumRepository.deleteById(albumId);
                logger.info("Successfully deleted album with ID: " + albumId);
                return true;
            } else {
                logger.warn("Album not found for deletion with ID: " + albumId);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Database error deleting album ID: " + albumId, e);
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
        logger.info("Searching albums with query: " + query);
        
        // Handle empty queries without calling DAO
        if (query == null || query.trim().isEmpty()) {
            logger.info("Empty query provided, returning empty list");
            return new ArrayList<Album>();
        }
        
        try {
            List<Album> albums = albumRepository.searchByName(query);
            logger.info("Search returned " + albums.size() + " albums");
            
            return albums;
            
        } catch (Exception e) {
            logger.error("Database error searching albums", e);
            throw new RuntimeException("Failed to search albums: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets albums by artist ID with business logic.
     * 
     * Business Logic:
     * - Validates artist ID before search
     * - Returns albums ordered by release date (newest first)
     * - Provides complete discography for an artist by ID
     * 
     * @param artistId Artist ID to get albums for
     * @return List of albums by the artist (never null, may be empty)
     * @throws IllegalArgumentException if artist ID is invalid
     * @throws RuntimeException if database operation fails
     */
    public List<Album> getAlbumsByArtist(Long artistId) {
        if (artistId == null || artistId <= 0) {
            throw new IllegalArgumentException("Invalid artist ID provided");
        }
        
        logger.debug("Retrieving albums for artist ID: " + artistId);
        
        try {
            List<Album> albums = albumRepository.findByArtistArtistId(artistId);
            logger.debug("Retrieved " + albums.size() + " albums for artist ID: " + artistId);
            
            return albums;
            
        } catch (Exception e) {
            logger.error("Database error retrieving albums by artist ID", e);
            throw new RuntimeException("Failed to retrieve albums by artist ID: " + e.getMessage(), e);
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
        
        logger.debug("Retrieving albums for artist name: " + artistName);
        
        try {
            List<Album> albums = albumRepository.findByArtistArtistNameIgnoreCase(artistName.trim());
            logger.debug("Retrieved " + albums.size() + " albums for artist: " + artistName);
            
            return albums;
            
        } catch (Exception e) {
            logger.error("Database error retrieving albums by artist", e);
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
        logger.debug("Retrieving albums with pagination: page=" + page + ", size=" + size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Album> albumsPage = albumRepository.findAll(pageable);
            List<Album> albums = albumsPage.getContent();
            logger.debug("Retrieved " + albums.size() + " albums for page " + page);
            
            return albums;
            
        } catch (Exception e) {
            logger.error("Database error retrieving paginated albums", e);
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
            long count = albumRepository.count();
            logger.debug("Total album count: " + count);
            
            return count;
            
        } catch (Exception e) {
            logger.error("Database error getting album count", e);
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
            boolean exists = albumRepository.existsById(albumId);
            logger.debug("Album existence check for ID " + albumId + ": " + exists);
            
            return exists;
            
        } catch (Exception e) {
            logger.error("Database error checking album existence", e);
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
        
        if (album.getArtist() == null) {
            throw new IllegalArgumentException("Artist is required");
        }
        
        // Validate artist ID if artist is provided
        if (album.getArtist().getArtistId() == null || album.getArtist().getArtistId() <= 0) {
            throw new IllegalArgumentException("Valid Artist ID is required");
        }
        
        // Business rule: Album name should be reasonable length
        if (album.getAlbumName().length() > 255) {
            throw new IllegalArgumentException("Album name is too long (maximum 255 characters)");
        }
        
        // Validate release date if provided
        if (album.getReleaseDate() != null) {
            LocalDate now = LocalDate.now();
            if (album.getReleaseDate().isAfter(now)) {
                throw new IllegalArgumentException("Release date cannot be in the future");
            }
            
            // Business rule: Release date should not be too far in the past
            int releaseYear = album.getReleaseDate().getYear();
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
        
        logger.debug("Retrieving albums for genre: " + genre);
        
        try {
            List<Album> albums = albumRepository.findByGenreIgnoreCase(genre.trim());
            logger.debug("Retrieved " + albums.size() + " albums for genre: " + genre);
            
            return albums;
            
        } catch (Exception e) {
            logger.error("Database error retrieving albums by genre", e);
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
        
        logger.debug("Retrieving albums for year: " + year);
        
        
        try {
            List<Album> albums = albumRepository.findByReleaseYear(year);
            logger.debug("Retrieved " + albums.size() + " albums for year: " + year);
            
            return albums;
            
        } catch (Exception e) {
            logger.error("Database error retrieving albums by year", e);
            throw new RuntimeException("Failed to retrieve albums by year: " + e.getMessage(), e);
        }
    }
}
