package com.musiclibrary.controller;

import com.musiclibrary.model.Album;
import com.musiclibrary.service.AlbumService;
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
 * Album REST API Controller
 * 
 * Provides HTTP endpoints for Album entity CRUD operations using modern Spring Boot patterns.
 * This controller replaces the traditional AlbumServlet with Spring REST annotations,
 * automatic JSON serialization, and dependency injection.
 * 
 * Supported Endpoints:
 * - GET /api/albums - List all albums (with optional pagination and search)
 * - GET /api/albums/{id} - Get album by ID
 * - POST /api/albums - Create new album
 * - PUT /api/albums/{id} - Update existing album
 * - DELETE /api/albums/{id} - Delete album by ID
 * 
 * @author Music Library Development Team
 * @version 2.0
 * @since Java 17
 */
@RestController
@RequestMapping("/api/albums")
@Validated
public class AlbumController {
    
    private static final Logger logger = LoggerFactory.getLogger(AlbumController.class);
    
    private final AlbumService albumService;
    
    @Autowired
    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAlbums(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String search) {
        
        logger.info("GET /api/albums - page: {}, size: {}, search: {}", page, size, search);
        
        if (size > 50) {
            size = 50;
        }
        
        try {
            List<Album> albums;
            long totalCount;
            
            if (search != null && !search.trim().isEmpty()) {
                albums = albumService.searchAlbums(search.trim());
                totalCount = albums.size();
            } else {
                albums = albumService.getAlbumsWithPagination(page, size);
                totalCount = albumService.getTotalAlbumCount();
            }
            
            Map<String, Object> response = Map.of(
                "albums", albums,
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "totalElements", totalCount,
                    "totalPages", (totalCount + size - 1) / size
                ),
                "success", true
            );
            
            logger.info("Successfully retrieved {} albums", albums.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving albums", e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to retrieve albums: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAlbumById(@PathVariable @Min(1) Long id) {
        logger.info("GET /api/albums/{}", id);
        
        try {
            Album album = albumService.getAlbumById(id);
            if (album == null) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Album not found with ID: " + id,
                    "success", false
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = Map.of(
                "album", album,
                "success", true
            );
            
            logger.info("Successfully retrieved album with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving album with ID: " + id, e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to retrieve album: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAlbum(@Valid @RequestBody Album album) {
        logger.info("POST /api/albums - creating album: {}", album.getAlbumName());
        
        try {
            Album createdAlbum = albumService.createAlbum(album);
            
            Map<String, Object> response = Map.of(
                "album", createdAlbum,
                "message", "Album created successfully",
                "success", true
            );
            
            logger.info("Successfully created album with ID: {}", createdAlbum.getAlbumId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid album data: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "error", e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error creating album", e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to create album: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAlbum(
            @PathVariable @Min(1) Long id, 
            @Valid @RequestBody Album album) {
        
        logger.info("PUT /api/albums/{} - updating album", id);
        
        try {
            album.setAlbumId(id);
            Album updatedAlbum = albumService.updateAlbum(album);
            
            if (updatedAlbum == null) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Album not found with ID: " + id,
                    "success", false
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = Map.of(
                "album", updatedAlbum,
                "message", "Album updated successfully",
                "success", true
            );
            
            logger.info("Successfully updated album with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid album data for update: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "error", e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error updating album with ID: " + id, e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to update album: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAlbum(@PathVariable @Min(1) Long id) {
        logger.info("DELETE /api/albums/{}", id);
        
        try {
            boolean deleted = albumService.deleteAlbum(id);
            
            if (!deleted) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Album not found with ID: " + id,
                    "success", false
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = Map.of(
                "message", "Album deleted successfully",
                "success", true
            );
            
            logger.info("Successfully deleted album with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting album with ID: " + id, e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to delete album: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
