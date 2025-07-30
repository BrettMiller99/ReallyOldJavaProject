package com.musiclibrary.controller;

import com.musiclibrary.model.Artist;
import com.musiclibrary.service.ArtistService;
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
 * Artist REST API Controller
 * 
 * Provides HTTP endpoints for Artist entity CRUD operations using modern Spring Boot patterns.
 * This controller replaces the traditional ArtistServlet with Spring REST annotations,
 * automatic JSON serialization, and dependency injection.
 * 
 * Supported Endpoints:
 * - GET /api/artists - List all artists (with optional pagination and search)
 * - GET /api/artists/{id} - Get artist by ID
 * - POST /api/artists - Create new artist
 * - PUT /api/artists/{id} - Update existing artist
 * - DELETE /api/artists/{id} - Delete artist by ID
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
 * @author Music Library Development Team
 * @version 2.0
 * @since Java 17
 */
@RestController
@RequestMapping("/api/artists")
@Validated
public class ArtistController {
    
    private static final Logger logger = LoggerFactory.getLogger(ArtistController.class);
    
    private final ArtistService artistService;
    
    @Autowired
    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }
    
    /**
     * Get all artists with optional pagination and search.
     * 
     * @param page page number (default: 0)
     * @param size page size (default: 10, max: 50)
     * @param search optional search query
     * @return list of artists with pagination metadata
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllArtists(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String search) {
        
        logger.info("GET /api/artists - page: {}, size: {}, search: {}", page, size, search);
        
        if (size > 50) {
            size = 50;
        }
        
        try {
            List<Artist> artists;
            long totalCount;
            
            if (search != null && !search.trim().isEmpty()) {
                artists = artistService.searchArtists(search.trim());
                totalCount = artists.size();
            } else {
                artists = artistService.getArtistsWithPagination(page, size);
                totalCount = artistService.getTotalArtistCount();
            }
            
            Map<String, Object> response = Map.of(
                "artists", artists,
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "totalElements", totalCount,
                    "totalPages", (totalCount + size - 1) / size
                ),
                "success", true
            );
            
            logger.info("Successfully retrieved {} artists", artists.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving artists", e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to retrieve artists: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get artist by ID.
     * 
     * @param id artist ID
     * @return artist details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getArtistById(@PathVariable @Min(1) Long id) {
        logger.info("GET /api/artists/{}", id);
        
        try {
            Artist artist = artistService.getArtistById(id);
            if (artist == null) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Artist not found with ID: " + id,
                    "success", false
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = Map.of(
                "artist", artist,
                "success", true
            );
            
            logger.info("Successfully retrieved artist with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving artist with ID: " + id, e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to retrieve artist: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Create new artist.
     * 
     * @param artist artist data from request body
     * @return created artist with ID
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createArtist(@Valid @RequestBody Artist artist) {
        logger.info("POST /api/artists - creating artist: {}", artist.getArtistName());
        
        try {
            Artist createdArtist = artistService.createArtist(artist);
            
            Map<String, Object> response = Map.of(
                "artist", createdArtist,
                "message", "Artist created successfully",
                "success", true
            );
            
            logger.info("Successfully created artist with ID: {}", createdArtist.getArtistId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid artist data: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "error", e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error creating artist", e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to create artist: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Update existing artist.
     * 
     * @param id artist ID to update
     * @param artist updated artist data
     * @return updated artist
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateArtist(
            @PathVariable @Min(1) Long id, 
            @Valid @RequestBody Artist artist) {
        
        logger.info("PUT /api/artists/{} - updating artist", id);
        
        try {
            artist.setArtistId(id);
            Artist updatedArtist = artistService.updateArtist(artist);
            
            if (updatedArtist == null) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Artist not found with ID: " + id,
                    "success", false
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = Map.of(
                "artist", updatedArtist,
                "message", "Artist updated successfully",
                "success", true
            );
            
            logger.info("Successfully updated artist with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid artist data for update: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "error", e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error updating artist with ID: " + id, e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to update artist: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Delete artist by ID.
     * 
     * @param id artist ID to delete
     * @return deletion confirmation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteArtist(@PathVariable @Min(1) Long id) {
        logger.info("DELETE /api/artists/{}", id);
        
        try {
            boolean deleted = artistService.deleteArtist(id);
            
            if (!deleted) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Artist not found with ID: " + id,
                    "success", false
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = Map.of(
                "message", "Artist deleted successfully",
                "success", true
            );
            
            logger.info("Successfully deleted artist with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting artist with ID: " + id, e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to delete artist: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
