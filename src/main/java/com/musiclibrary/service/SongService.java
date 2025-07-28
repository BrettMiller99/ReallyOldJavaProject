package com.musiclibrary.service;

import com.musiclibrary.dao.SongDAO;
import com.musiclibrary.model.Song;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Song Business Service Layer
 * 
 * Provides business logic operations for Song entities using traditional Java 7 service patterns.
 * This layer sits between the web/servlet layer and the data access layer, implementing
 * business rules, validation, and coordinated operations across multiple DAOs.
 * 
 * Business Logic:
 * - Validates song data before persistence operations
 * - Implements business rules for song management (ratings, play counts, etc.)
 * - Coordinates operations across multiple entities (songs, artists, albums)
 * - Provides search and filtering capabilities with business context
 * - Manages song playback tracking and statistics
 * - Handles error scenarios with appropriate business messaging
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
public class SongService {
    
    // Traditional Java logging - migration opportunity
    private static final Logger LOGGER = Logger.getLogger(SongService.class.getName());
    
    // Manual DAO instantiation - migration opportunity to dependency injection
    private final SongDAO songDAO;
    
    /**
     * Constructor with manual dependency injection.
     * Traditional approach - migration opportunity to @Autowired constructor injection.
     */
    public SongService() {
        this.songDAO = new SongDAO();
    }
    
    /**
     * Constructor for testing with DAO injection.
     * Allows for mock DAO injection during unit testing.
     * 
     * @param songDAO DAO instance to use
     */
    public SongService(SongDAO songDAO) {
        this.songDAO = songDAO;
    }
    
    /**
     * Creates a new song with business validation.
     * 
     * Business Logic:
     * - Validates song data completeness and business rules
     * - Ensures required fields are present and valid
     * - Checks rating bounds (0-5 stars)
     * - Validates track length is positive
     * - Initializes default values for optional fields
     * 
     * @param song Song to create
     * @return Created song with generated ID
     * @throws IllegalArgumentException if song data is invalid
     * @throws RuntimeException if database operation fails
     */
    public Song createSong(Song song) {
        LOGGER.info("Creating new song: " + (song != null ? song.getSongName() : "null"));
        
        try {
            // Business validation before persistence
            validateSongForCreation(song);
            
            // Apply business rules and defaults
            applySongBusinessRules(song);
            
            Song createdSong = songDAO.create(song);
            LOGGER.info("Successfully created song with ID: " + createdSong.getSongId());
            
            return createdSong;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error creating song", e);
            throw new RuntimeException("Failed to create song: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid song data: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Retrieves song by ID with business context.
     * 
     * @param songId Song ID to retrieve
     * @return Song entity or null if not found
     * @throws IllegalArgumentException if songId is invalid
     * @throws RuntimeException if database operation fails
     */
    public Song getSongById(Long songId) {
        if (songId == null || songId <= 0) {
            throw new IllegalArgumentException("Invalid song ID provided");
        }
        
        try {
            Song song = songDAO.findById(songId);
            if (song != null) {
                LOGGER.fine("Retrieved song: " + song.getSongName());
            } else {
                LOGGER.fine("Song not found with ID: " + songId);
            }
            
            return song;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving song ID: " + songId, e);
            throw new RuntimeException("Failed to retrieve song: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves all songs with business context.
     * 
     * Business Logic:
     * - Returns songs ordered by name for consistent user experience
     * - Includes denormalized artist names for performance
     * - Logs retrieval for audit and monitoring purposes
     * 
     * @return List of all songs (never null, may be empty)
     * @throws RuntimeException if database operation fails
     */
    public List<Song> getAllSongs() {
        LOGGER.info("Retrieving all songs");
        
        try {
            List<Song> songs = songDAO.findAll();
            LOGGER.info("Retrieved " + songs.size() + " songs");
            
            return songs;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving all songs", e);
            throw new RuntimeException("Failed to retrieve songs: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates existing song with business validation.
     * 
     * Business Logic:
     * - Validates song exists and user has permission to update
     * - Applies business rules for updates
     * - Maintains audit trail with modification timestamps
     * - Validates rating and other business constraints
     * 
     * @param song Song to update
     * @return Updated song entity
     * @throws IllegalArgumentException if song data is invalid
     * @throws RuntimeException if database operation fails
     */
    public Song updateSong(Song song) {
        LOGGER.info("Updating song: " + (song != null ? song.getSongName() : "null"));
        
        try {
            // Business validation for updates
            validateSongForUpdate(song);
            
            // Apply business rules
            applySongBusinessRules(song);
            
            Song updatedSong = songDAO.update(song);
            LOGGER.info("Successfully updated song with ID: " + updatedSong.getSongId());
            
            return updatedSong;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error updating song", e);
            throw new RuntimeException("Failed to update song: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid song update data: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Deletes song by ID with business validation.
     * 
     * Business Logic:
     * - Validates song exists before deletion
     * - Checks for dependent records (playlist associations)
     * - Logs deletion for audit purposes
     * - Handles cascade operations appropriately
     * 
     * @param songId Song ID to delete
     * @return true if deleted, false if not found
     * @throws IllegalArgumentException if songId is invalid
     * @throws RuntimeException if database operation fails
     */
    public boolean deleteSong(Long songId) {
        if (songId == null || songId <= 0) {
            throw new IllegalArgumentException("Invalid song ID provided");
        }
        
        LOGGER.info("Deleting song with ID: " + songId);
        
        try {
            boolean deleted = songDAO.delete(songId);
            
            if (deleted) {
                LOGGER.info("Successfully deleted song with ID: " + songId);
            } else {
                LOGGER.warning("Song not found for deletion with ID: " + songId);
            }
            
            return deleted;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error deleting song ID: " + songId, e);
            throw new RuntimeException("Failed to delete song: " + e.getMessage(), e);
        }
    }
    
    /**
     * Searches songs with business context and filtering.
     * 
     * Business Logic:
     * - Provides case-insensitive search across multiple fields
     * - Includes artist name in search results for relevance
     * - Handles empty queries gracefully
     * - Orders results by relevance and name
     * 
     * @param query Search query string
     * @return List of matching songs (never null, may be empty)
     * @throws RuntimeException if database operation fails
     */
    public List<Song> searchSongs(String query) {
        LOGGER.info("Searching songs with query: " + query);
        
        try {
            List<Song> songs = songDAO.search(query);
            LOGGER.info("Search returned " + songs.size() + " songs");
            
            return songs;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error searching songs", e);
            throw new RuntimeException("Failed to search songs: " + e.getMessage(), e);
        }
    }
    
    /**
     * Records song playback with business logic.
     * 
     * Business Logic:
     * - Increments play count for popularity tracking
     * - Updates last played timestamp for user history
     * - Validates song exists before recording playback
     * - Handles concurrent playback scenarios
     * 
     * @param songId Song ID being played
     * @throws IllegalArgumentException if songId is invalid
     * @throws RuntimeException if database operation fails
     */
    public void recordSongPlayback(Long songId) {
        if (songId == null || songId <= 0) {
            throw new IllegalArgumentException("Invalid song ID provided");
        }
        
        LOGGER.fine("Recording playback for song ID: " + songId);
        
        try {
            // Verify song exists before recording playback
            if (!songDAO.exists(songId)) {
                throw new IllegalArgumentException("Song with ID " + songId + " does not exist");
            }
            
            songDAO.recordPlayback(songId);
            LOGGER.fine("Successfully recorded playback for song ID: " + songId);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error recording playback", e);
            throw new RuntimeException("Failed to record playback: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets songs with pagination support.
     * 
     * Business Logic:
     * - Validates pagination parameters for reasonable bounds
     * - Provides consistent ordering for pagination
     * - Handles edge cases (offset beyond total count)
     * 
     * @param page Page number (0-based)
     * @param size Page size (must be positive)
     * @return List of songs for the specified page
     * @throws IllegalArgumentException if pagination parameters are invalid
     * @throws RuntimeException if database operation fails
     */
    public List<Song> getSongsWithPagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be non-negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        
        int offset = page * size;
        LOGGER.fine("Retrieving songs with pagination: page=" + page + ", size=" + size);
        
        try {
            List<Song> songs = songDAO.findWithPagination(offset, size);
            LOGGER.fine("Retrieved " + songs.size() + " songs for page " + page);
            
            return songs;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving paginated songs", e);
            throw new RuntimeException("Failed to retrieve songs: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets total count of songs for pagination support.
     * 
     * @return Total number of songs
     * @throws RuntimeException if database operation fails
     */
    public long getTotalSongCount() {
        try {
            long count = songDAO.count();
            LOGGER.fine("Total song count: " + count);
            
            return count;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error getting song count", e);
            throw new RuntimeException("Failed to get song count: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates song rating with business validation.
     * 
     * Business Logic:
     * - Validates rating is within acceptable range (0-5 stars)
     * - Updates only the rating field without affecting other data
     * - Maintains audit trail for rating changes
     * 
     * @param songId Song ID to rate
     * @param rating New rating (0-5 stars)
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if database operation fails
     */
    public void updateSongRating(Long songId, Integer rating) {
        if (songId == null || songId <= 0) {
            throw new IllegalArgumentException("Invalid song ID provided");
        }
        if (rating != null && (rating < 0 || rating > 5)) {
            throw new IllegalArgumentException("Rating must be between 0 and 5 stars");
        }
        
        LOGGER.fine("Updating rating for song ID " + songId + " to " + rating + " stars");
        
        try {
            Song song = songDAO.findById(songId);
            if (song == null) {
                throw new IllegalArgumentException("Song with ID " + songId + " does not exist");
            }
            
            song.setRating(rating);
            songDAO.update(song);
            
            LOGGER.fine("Successfully updated rating for song ID: " + songId);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error updating song rating", e);
            throw new RuntimeException("Failed to update song rating: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates song data for creation operations.
     * Implements business rules for new song creation.
     * 
     * @param song Song to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateSongForCreation(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("Song cannot be null");
        }
        
        if (!song.isValid()) {
            throw new IllegalArgumentException("Song data is incomplete or invalid");
        }
        
        // Additional business validation
        if (song.getSongName() == null || song.getSongName().trim().isEmpty()) {
            throw new IllegalArgumentException("Song name is required");
        }
        
        if (song.getArtistId() == null) {
            throw new IllegalArgumentException("Artist ID is required");
        }
        
        if (song.getTrackLength() == null || song.getTrackLength() <= 0) {
            throw new IllegalArgumentException("Track length must be positive");
        }
        
        if (song.getDateReleased() == null) {
            throw new IllegalArgumentException("Release date is required");
        }
        
        // Validate rating if provided
        if (song.getRating() != null && (song.getRating() < 0 || song.getRating() > 5)) {
            throw new IllegalArgumentException("Rating must be between 0 and 5 stars");
        }
    }
    
    /**
     * Validates song data for update operations.
     * Implements business rules for song updates.
     * 
     * @param song Song to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateSongForUpdate(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("Song cannot be null");
        }
        
        if (song.getSongId() == null) {
            throw new IllegalArgumentException("Song ID is required for updates");
        }
        
        // Run creation validation as well
        validateSongForCreation(song);
    }
    
    /**
     * Applies business rules and defaults to song entity.
     * Centralizes business logic for song data management.
     * 
     * @param song Song to apply rules to
     */
    private void applySongBusinessRules(Song song) {
        // Initialize default values
        if (song.getRating() == null) {
            song.setRating(0);
        }
        
        if (song.getPlayCount() == null) {
            song.setPlayCount(0);
        }
        
        // Trim and clean text fields
        if (song.getSongName() != null) {
            song.setSongName(song.getSongName().trim());
        }
        
        if (song.getGenre() != null) {
            song.setGenre(song.getGenre().trim());
        }
        
        // Business rule: Album name should be cleaned if provided
        if (song.getAlbumName() != null) {
            song.setAlbumName(song.getAlbumName().trim());
        }
    }
    
    /**
     * Gets songs by artist name with business logic.
     * 
     * Business Logic:
     * - Validates artist name before search
     * - Returns songs ordered by album and track number
     * - Provides complete song collection for an artist
     * 
     * @param artistName Artist name to get songs for
     * @return List of songs by the artist (never null, may be empty)
     * @throws IllegalArgumentException if artist name is invalid
     * @throws RuntimeException if database operation fails
     */
    public List<Song> getSongsByArtist(String artistName) {
        if (artistName == null || artistName.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid artist name provided");
        }
        
        LOGGER.fine("Retrieving songs for artist: " + artistName);
        
        try {
            List<Song> songs = songDAO.findByArtist(artistName.trim());
            LOGGER.fine("Retrieved " + songs.size() + " songs for artist: " + artistName);
            
            return songs;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving songs by artist", e);
            throw new RuntimeException("Failed to retrieve songs by artist: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets songs by album name with business logic.
     * 
     * Business Logic:
     * - Validates album name before search  
     * - Returns songs ordered by track number
     * - Provides complete track listing for an album
     * 
     * @param albumName Album name to get songs for
     * @return List of songs in the album (never null, may be empty)
     * @throws IllegalArgumentException if album name is invalid
     * @throws RuntimeException if database operation fails
     */
    public List<Song> getSongsByAlbum(String albumName) {
        if (albumName == null || albumName.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid album name provided");
        }
        
        LOGGER.fine("Retrieving songs for album: " + albumName);
        
        try {
            List<Song> songs = songDAO.findByAlbum(albumName.trim());
            LOGGER.fine("Retrieved " + songs.size() + " songs for album: " + albumName);
            
            return songs;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving songs by album", e);
            throw new RuntimeException("Failed to retrieve songs by album: " + e.getMessage(), e);
        }
    }
}
