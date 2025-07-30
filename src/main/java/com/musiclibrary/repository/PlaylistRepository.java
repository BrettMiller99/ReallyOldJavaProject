package com.musiclibrary.repository;

import com.musiclibrary.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for Playlist entities.
 * 
 * Replaces the traditional PlaylistDAO with modern Spring Data JPA approach.
 * Provides automatic CRUD operations and custom query methods.
 * 
 * Migration Benefits:
 * - Eliminates 160+ lines of manual JDBC code
 * - Automatic transaction management
 * - Built-in pagination and sorting support
 * - Type-safe query methods
 * - Automatic SQL generation from method names
 * 
 * @author Music Library Development Team
 * @version 2.0 - Migrated to Spring Data JPA
 * @since Java 17
 */
@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    
    
    /**
     * Find playlists by creator/user (case-insensitive).
     * Replaces PlaylistDAO.findByUser() method.
     */
    List<Playlist> findByCreatedByIgnoreCase(String createdBy);
    
    /**
     * Find public playlists only.
     * Replaces PlaylistDAO.findPublicPlaylists() method.
     */
    List<Playlist> findByIsPublicTrue();
    
    /**
     * Find private playlists only.
     * Useful for user's personal playlist management.
     */
    List<Playlist> findByIsPublicFalse();
    
    /**
     * Find playlists by creator and visibility.
     * Combines user filtering with public/private filtering.
     */
    List<Playlist> findByCreatedByIgnoreCaseAndIsPublic(String createdBy, Boolean isPublic);
    
    /**
     * Find playlists with song count greater than specified value.
     * Useful for finding substantial playlists.
     */
    List<Playlist> findBySongCountGreaterThan(Integer minSongs);
    
    /**
     * Find playlists with total duration greater than specified value.
     * Useful for finding longer playlists.
     */
    List<Playlist> findByTotalDurationGreaterThan(Integer minDuration);
    
    /**
     * Search playlists by name containing search term (case-insensitive).
     * Replaces PlaylistDAO.search() method functionality.
     */
    @Query("SELECT p FROM Playlist p WHERE LOWER(p.playlistName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Playlist> searchByName(@Param("searchTerm") String searchTerm);
    
    /**
     * Search playlists by description containing search term.
     * Useful for content-based search.
     */
    @Query("SELECT p FROM Playlist p WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Playlist> searchByDescription(@Param("searchTerm") String searchTerm);
    
    /**
     * Find playlists ordered by name alphabetically.
     * Replaces PlaylistDAO.findWithPagination() ordering.
     */
    List<Playlist> findByOrderByPlaylistNameAsc();
    
    /**
     * Find playlists ordered by creation date (newest first).
     * Useful for chronological browsing.
     */
    List<Playlist> findByOrderByCreatedDateDesc();
    
    /**
     * Find playlists ordered by song count (largest first).
     * Useful for finding most comprehensive playlists.
     */
    List<Playlist> findByOrderBySongCountDesc();
    
    /**
     * Check if playlist exists by name and creator (for duplicate prevention).
     * Replaces manual validation logic in PlaylistDAO.
     */
    boolean existsByPlaylistNameAndCreatedByIgnoreCase(String playlistName, String createdBy);
    
    /**
     * Count playlists by creator for statistics.
     */
    long countByCreatedBy(String createdBy);
    
    /**
     * Count public playlists for analytics.
     */
    long countByIsPublicTrue();
    
    /**
     * Find popular public playlists (public playlists with many songs).
     * Combines visibility and popularity criteria.
     */
    @Query("SELECT p FROM Playlist p WHERE p.isPublic = true AND p.songCount >= :minSongs ORDER BY p.songCount DESC")
    List<Playlist> findPopularPublicPlaylists(@Param("minSongs") Integer minSongs);
    
    /**
     * Find empty playlists (for cleanup or user guidance).
     */
    List<Playlist> findBySongCount(Integer songCount);
}
