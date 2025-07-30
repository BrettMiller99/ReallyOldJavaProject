package com.musiclibrary.controller;

import com.musiclibrary.model.Song;
import com.musiclibrary.service.SongService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;

/**
 * Song REST API Controller
 * 
 * Provides HTTP endpoints for Song entity CRUD operations using modern Spring Boot patterns.
 * This controller replaces the traditional SongServlet with Spring REST annotations,
 * automatic JSON serialization, and dependency injection.
 * 
 * Supported Endpoints:
 * - GET /api/songs - List all songs (with optional pagination and search)
 * - GET /api/songs/{id} - Get song by ID
 * - POST /api/songs - Create new song
 * - PUT /api/songs/{id} - Update existing song
 * - DELETE /api/songs/{id} - Delete song by ID
 * 
 * Modern Features:
 * - Automatic dependency injection with @Autowired
 * - Automatic JSON serialization/deserialization with Jackson
 * - Path variable and request parameter binding
 * - Bean validation with @Valid annotations
 * - Structured logging with SLF4J
 * - Consistent error handling with ResponseEntity
 * - Type-safe request/response handling
 * 
 * Migration Benefits:
 * - Eliminates manual HTTP method routing
 * - Removes manual JSON parsing and response building
 * - Provides automatic validation and error handling
 * - Reduces boilerplate code significantly
 * - Enables better testing with Spring Test framework
 * 
 * @author Music Library Development Team
 * @version 2.0
 * @since Java 17
 */
@RestController
@RequestMapping("/api/songs")
@Validated
public class SongController {
    
    private static final Logger logger = LoggerFactory.getLogger(SongController.class);
    
    private final SongService songService;
    
    @Autowired
    public SongController(SongService songService) {
        this.songService = songService;
    }
    
    /**
     * Get all songs with optional pagination and search.
     * 
     * @param page page number (default: 0)
     * @param size page size (default: 10, max: 100)
     * @param search optional search query
     * @return list of songs with pagination metadata
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSongs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String search) {
        
        logger.info("GET /api/songs - page: {}, size: {}, search: {}", page, size, search);
        
        if (size > 100) {
            size = 100;
        }
        
        try {
            List<Song> songs;
            long totalCount;
            
            if (search != null && !search.trim().isEmpty()) {
                songs = songService.searchSongs(search.trim());
                totalCount = songs.size();
            } else {
                songs = songService.getSongsWithPagination(page, size);
                totalCount = songService.getTotalSongCount();
            }
            
            Map<String, Object> response = Map.of(
                "songs", songs,
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "totalElements", totalCount,
                    "totalPages", (totalCount + size - 1) / size
                ),
                "success", true
            );
            
            logger.info("Successfully retrieved {} songs", songs.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving songs", e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to retrieve songs: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get song by ID.
     * 
     * @param id song ID
     * @return song details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSongById(@PathVariable @Min(1) Long id) {
        logger.info("GET /api/songs/{}", id);
        
        try {
            Song song = songService.getSongById(id);
            if (song == null) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Song not found with ID: " + id,
                    "success", false
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = Map.of(
                "song", song,
                "success", true
            );
            
            logger.info("Successfully retrieved song with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving song with ID: " + id, e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to retrieve song: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Create new song.
     * 
     * @param song song data from request body
     * @return created song with ID
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSong(@Valid @RequestBody Song song) {
        logger.info("POST /api/songs - creating song: {}", song.getSongName());
        
        try {
            Song createdSong = songService.createSong(song);
            
            Map<String, Object> response = Map.of(
                "song", createdSong,
                "message", "Song created successfully",
                "success", true
            );
            
            logger.info("Successfully created song with ID: {}", createdSong.getSongId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid song data: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "error", e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error creating song", e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to create song: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Update existing song.
     * 
     * @param id song ID to update
     * @param song updated song data
     * @return updated song
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSong(
            @PathVariable @Min(1) Long id, 
            @Valid @RequestBody Song song) {
        
        logger.info("PUT /api/songs/{} - updating song", id);
        
        try {
            song.setSongId(id);
            Song updatedSong = songService.updateSong(song);
            
            if (updatedSong == null) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Song not found with ID: " + id,
                    "success", false
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = Map.of(
                "song", updatedSong,
                "message", "Song updated successfully",
                "success", true
            );
            
            logger.info("Successfully updated song with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid song data for update: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "error", e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error updating song with ID: " + id, e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to update song: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Delete song by ID.
     * 
     * @param id song ID to delete
     * @return deletion confirmation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSong(@PathVariable @Min(1) Long id) {
        logger.info("DELETE /api/songs/{}", id);
        
        try {
            boolean deleted = songService.deleteSong(id);
            
            if (!deleted) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Song not found with ID: " + id,
                    "success", false
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = Map.of(
                "message", "Song deleted successfully",
                "success", true
            );
            
            logger.info("Successfully deleted song with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting song with ID: " + id, e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to delete song: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/{id}/play")
    public ResponseEntity<Map<String, Object>> recordPlayback(@PathVariable @Min(1) Long id) {
        logger.info("POST /api/songs/{}/play - recording playback", id);
        
        try {
            Song song = songService.recordPlayback(id);
            
            if (song == null) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Song not found with ID: " + id,
                    "success", false
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = Map.of(
                "song", song,
                "message", "Playback recorded successfully",
                "success", true
            );
            
            logger.info("Successfully recorded playback for song with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error recording playback for song with ID: " + id, e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to record playback: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
