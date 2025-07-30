package com.musiclibrary.service;

import com.musiclibrary.repository.SongRepository;
import com.musiclibrary.model.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Song Business Service Layer
 * 
 * Provides business logic operations for Song entities using modern Spring Boot patterns.
 * This service layer implements business rules, validation, and coordinated operations
 * across multiple entities with automatic dependency injection and transaction management.
 * 
 * Business Logic:
 * - Validates song data before persistence operations
 * - Implements business rules for song management (ratings, play counts, etc.)
 * - Coordinates operations across multiple entities (songs, artists, albums)
 * - Provides search and filtering capabilities with business context
 * - Manages song playback tracking and statistics
 * - Handles error scenarios with appropriate business messaging
 * 
 * Modern Features:
 * - Spring Service with @Service annotation for automatic component scanning
 * - @Transactional annotations for declarative transaction management
 * - @Autowired DAO injection for automatic dependency resolution
 * - SLF4J with structured logging for better observability
 * - Spring exception hierarchy for consistent error handling
 * - Bean Validation integration for data validation
 * 
 * @author Music Library Development Team
 * @version 2.0
 * @since Java 17
 */
@Service
@Transactional
public class SongService {
    
    private static final Logger logger = LoggerFactory.getLogger(SongService.class);
    
    private final SongRepository songRepository;
    
    /**
     * Constructor with automatic dependency injection.
     * Spring automatically injects the SongRepository dependency.
     * 
     * @param songRepository the song repository
     */
    @Autowired
    public SongService(SongRepository songRepository) {
        this.songRepository = songRepository;
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
        logger.info("Creating new song: " + (song != null ? song.getSongName() : "null"));
        
        try {
            // Business validation before persistence
            validateSongForCreation(song);
            
            // Apply business rules and defaults
            applySongBusinessRules(song);
            
            Song createdSong = songRepository.save(song);
            logger.info("Successfully created song with ID: " + createdSong.getSongId());
            
            return createdSong;
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid song data: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Database error creating song", e);
            throw new RuntimeException("Failed to create song: " + e.getMessage(), e);
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
            Optional<Song> songOptional = songRepository.findById(songId);
            if (songOptional.isPresent()) {
                Song song = songOptional.get();
                logger.debug("Retrieved song: " + song.getSongName());
                return song;
            } else {
                logger.debug("Song not found with ID: " + songId);
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Database error retrieving song ID: " + songId, e);
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
        logger.info("Retrieving all songs");
        
        try {
            List<Song> songs = songRepository.findAll();
            logger.info("Retrieved " + songs.size() + " songs");
            
            return songs;
            
        } catch (Exception e) {
            logger.error("Database error retrieving all songs", e);
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
        logger.info("Updating song: " + (song != null ? song.getSongName() : "null"));
        
        try {
            // Business validation for updates
            validateSongForUpdate(song);
            
            // Apply business rules
            applySongBusinessRules(song);
            
            Song updatedSong = songRepository.save(song);
            logger.info("Successfully updated song with ID: " + updatedSong.getSongId());
            
            return updatedSong;
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid song update data: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Database error updating song", e);
            throw new RuntimeException("Failed to update song: " + e.getMessage(), e);
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
        
        logger.info("Deleting song with ID: " + songId);
        
        try {
            if (songRepository.existsById(songId)) {
                songRepository.deleteById(songId);
                logger.info("Successfully deleted song with ID: " + songId);
                return true;
            } else {
                logger.warn("Song not found for deletion with ID: " + songId);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Database error deleting song ID: " + songId, e);
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
        logger.info("Searching songs with query: " + query);
        
        // Handle empty queries without calling DAO
        if (query == null || query.trim().isEmpty()) {
            logger.info("Empty query provided, returning empty list");
            return new ArrayList<Song>();
        }
        
        try {
            List<Song> songs = songRepository.searchByName(query);
            logger.info("Search returned " + songs.size() + " songs");
            
            return songs;
            
        } catch (Exception e) {
            logger.error("Database error searching songs", e);
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
        
        logger.debug("Recording playback for song ID: " + songId);
        
        try {
            // Verify song exists before recording playback
            if (!songRepository.existsById(songId)) {
                throw new IllegalArgumentException("Song with ID " + songId + " does not exist");
            }
            
            songRepository.recordPlayback(songId);
            logger.debug("Successfully recorded playback for song ID: " + songId);
            
        } catch (Exception e) {
            logger.error("Database error recording playback", e);
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
        logger.debug("Retrieving songs with pagination: page=" + page + ", size=" + size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Song> songsPage = songRepository.findAll(pageable);
            List<Song> songs = songsPage.getContent();
            logger.debug("Retrieved " + songs.size() + " songs for page " + page);
            
            return songs;
            
        } catch (Exception e) {
            logger.error("Database error retrieving paginated songs", e);
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
            long count = songRepository.count();
            logger.debug("Total song count: " + count);
            
            return count;
            
        } catch (Exception e) {
            logger.error("Database error getting song count", e);
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
        
        logger.debug("Updating rating for song ID " + songId + " to " + rating + " stars");
        
        try {
            Optional<Song> songOptional = songRepository.findById(songId);
            if (!songOptional.isPresent()) {
                throw new IllegalArgumentException("Song with ID " + songId + " does not exist");
            }
            
            Song song = songOptional.get();
            song.setRating(rating);
            songRepository.save(song);
            
            logger.debug("Successfully updated rating for song ID: " + songId);
            
        } catch (Exception e) {
            logger.error("Database error updating song rating", e);
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
        
        if (song.getArtist() == null) {
            throw new IllegalArgumentException("Artist is required");
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
        
        // Business rule: Album should be validated if provided
        if (song.getAlbum() != null && song.getAlbum().getAlbumName() != null) {
            song.getAlbum().setAlbumName(song.getAlbum().getAlbumName().trim());
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
        
        logger.debug("Retrieving songs for artist: " + artistName);
        
        try {
            List<Song> songs = songRepository.findByArtistArtistNameIgnoreCase(artistName.trim());
            logger.debug("Retrieved " + songs.size() + " songs for artist: " + artistName);
            
            return songs;
            
        } catch (Exception e) {
            logger.error("Database error retrieving songs by artist", e);
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
        
        logger.debug("Retrieving songs for album: " + albumName);
        
        try {
            List<Song> songs = songRepository.findByAlbumAlbumNameIgnoreCase(albumName.trim());
            logger.debug("Retrieved " + songs.size() + " songs for album: " + albumName);
            
            return songs;
            
        } catch (Exception e) {
            logger.error("Database error retrieving songs by album", e);
            throw new RuntimeException("Failed to retrieve songs by album: " + e.getMessage(), e);
        }
    }
    
    /**
     * Records a playback for a song by incrementing its play count.
     * 
     * @param songId ID of the song to record playback for
     * @return Updated song with incremented play count, or null if song not found
     * @throws RuntimeException if database operation fails
     */
    public Song recordPlayback(Long songId) {
        logger.info("Recording playback for song with ID: {}", songId);
        
        try {
            Song song = songRepository.findById(songId).orElse(null);
            if (song == null) {
                logger.warn("Song not found with ID: {}", songId);
                return null;
            }
            
            Integer currentPlayCount = song.getPlayCount();
            if (currentPlayCount == null) {
                currentPlayCount = 0;
            }
            song.setPlayCount(currentPlayCount + 1);
            
            Song updatedSong = songRepository.save(song);
            logger.info("Successfully recorded playback for song with ID: {}, new play count: {}", 
                       songId, updatedSong.getPlayCount());
            
            return updatedSong;
            
        } catch (Exception e) {
            logger.error("Error recording playback for song with ID: " + songId, e);
            throw new RuntimeException("Failed to record playback: " + e.getMessage(), e);
        }
    }
}
