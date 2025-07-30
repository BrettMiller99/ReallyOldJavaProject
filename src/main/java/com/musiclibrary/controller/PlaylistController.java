package com.musiclibrary.controller;

import com.musiclibrary.model.Playlist;
import com.musiclibrary.service.PlaylistService;
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
 * Playlist REST API Controller
 * 
 * Provides HTTP endpoints for Playlist entity CRUD operations using modern Spring Boot patterns.
 * This controller replaces the traditional PlaylistServlet with Spring REST annotations,
 * automatic JSON serialization, and dependency injection.
 * 
 * Supported Endpoints:
 * - GET /api/playlists - List all playlists (with optional pagination and search)
 * - GET /api/playlists/{id} - Get playlist by ID
 * - POST /api/playlists - Create new playlist
 * - PUT /api/playlists/{id} - Update existing playlist
 * - DELETE /api/playlists/{id} - Delete playlist by ID
 * 
 * @author Music Library Development Team
 * @version 2.0
 * @since Java 17
 */
@RestController
@RequestMapping("/api/playlists")
@Validated
public class PlaylistController {
    
    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);
    
    private final PlaylistService playlistService;
    
    @Autowired
    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPlaylists(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String search) {
        
        logger.info("GET /api/playlists - page: {}, size: {}, search: {}", page, size, search);
        
        if (size > 50) {
            size = 50;
        }
        
        try {
            List<Playlist> playlists;
            long totalCount;
            
            if (search != null && !search.trim().isEmpty()) {
                playlists = playlistService.searchPlaylists(search.trim());
                totalCount = playlists.size();
            } else {
                playlists = playlistService.getPlaylistsWithPagination(page, size);
                totalCount = playlistService.getTotalPlaylistCount();
            }
            
            Map<String, Object> response = Map.of(
                "playlists", playlists,
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "totalElements", totalCount,
                    "totalPages", (totalCount + size - 1) / size
                ),
                "success", true
            );
            
            logger.info("Successfully retrieved {} playlists", playlists.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving playlists", e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to retrieve playlists: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPlaylistById(@PathVariable @Min(1) Long id) {
        logger.info("GET /api/playlists/{}", id);
        
        try {
            Playlist playlist = playlistService.getPlaylistById(id);
            if (playlist == null) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Playlist not found with ID: " + id,
                    "success", false
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = Map.of(
                "playlist", playlist,
                "success", true
            );
            
            logger.info("Successfully retrieved playlist with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving playlist with ID: " + id, e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to retrieve playlist: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPlaylist(@Valid @RequestBody Playlist playlist) {
        logger.info("POST /api/playlists - creating playlist: {}", playlist.getPlaylistName());
        
        try {
            Playlist createdPlaylist = playlistService.createPlaylist(playlist);
            
            Map<String, Object> response = Map.of(
                "playlist", createdPlaylist,
                "message", "Playlist created successfully",
                "success", true
            );
            
            logger.info("Successfully created playlist with ID: {}", createdPlaylist.getPlaylistId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid playlist data: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "error", e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error creating playlist", e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to create playlist: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePlaylist(
            @PathVariable @Min(1) Long id, 
            @Valid @RequestBody Playlist playlist) {
        
        logger.info("PUT /api/playlists/{} - updating playlist", id);
        
        try {
            playlist.setPlaylistId(id);
            Playlist updatedPlaylist = playlistService.updatePlaylist(playlist);
            
            if (updatedPlaylist == null) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Playlist not found with ID: " + id,
                    "success", false
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = Map.of(
                "playlist", updatedPlaylist,
                "message", "Playlist updated successfully",
                "success", true
            );
            
            logger.info("Successfully updated playlist with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid playlist data for update: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "error", e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error updating playlist with ID: " + id, e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to update playlist: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePlaylist(@PathVariable @Min(1) Long id) {
        logger.info("DELETE /api/playlists/{}", id);
        
        try {
            boolean deleted = playlistService.deletePlaylist(id);
            
            if (!deleted) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Playlist not found with ID: " + id,
                    "success", false
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = Map.of(
                "message", "Playlist deleted successfully",
                "success", true
            );
            
            logger.info("Successfully deleted playlist with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting playlist with ID: " + id, e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to delete playlist: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Map<String, Object>> addSongToPlaylist(
            @PathVariable @Min(1) Long playlistId,
            @PathVariable @Min(1) Long songId) {
        
        logger.info("POST /api/playlists/{}/songs/{} - adding song to playlist", playlistId, songId);
        
        try {
            Playlist updatedPlaylist = playlistService.addSongToPlaylist(playlistId, songId);
            
            if (updatedPlaylist == null) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Playlist or song not found",
                    "success", false
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = Map.of(
                "playlist", updatedPlaylist,
                "message", "Song added to playlist successfully",
                "success", true
            );
            
            logger.info("Successfully added song {} to playlist {}", songId, playlistId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error adding song to playlist", e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to add song to playlist: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Map<String, Object>> removeSongFromPlaylist(
            @PathVariable @Min(1) Long playlistId,
            @PathVariable @Min(1) Long songId) {
        
        logger.info("DELETE /api/playlists/{}/songs/{} - removing song from playlist", playlistId, songId);
        
        try {
            Playlist updatedPlaylist = playlistService.removeSongFromPlaylist(playlistId, songId);
            
            if (updatedPlaylist == null) {
                Map<String, Object> errorResponse = Map.of(
                    "error", "Playlist or song not found",
                    "success", false
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = Map.of(
                "playlist", updatedPlaylist,
                "message", "Song removed from playlist successfully",
                "success", true
            );
            
            logger.info("Successfully removed song {} from playlist {}", songId, playlistId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error removing song from playlist", e);
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to remove song from playlist: " + e.getMessage(),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
