package com.musiclibrary.repository;

import com.musiclibrary.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA Repository for Song entities.
 * 
 * Replaces the traditional SongDAO with modern Spring Data JPA approach.
 * Provides automatic CRUD operations and custom query methods.
 * 
 * Migration Benefits:
 * - Eliminates 200+ lines of manual JDBC code
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
public interface SongRepository extends JpaRepository<Song, Long> {
    
    
    
    /**
     * Find songs by artist name (case-insensitive).
     * Replaces SongDAO.findByArtist() method.
     */
    List<Song> findByArtistArtistNameIgnoreCase(String artistName);
    
    /**
     * Find songs by album name (case-insensitive).
     * Replaces SongDAO.findByAlbum() method.
     */
    List<Song> findByAlbumAlbumNameIgnoreCase(String albumName);
    
    /**
     * Find songs by genre and release date after specified date.
     * Demonstrates compound query method naming.
     */
    List<Song> findByGenreAndDateReleasedAfter(String genre, LocalDate date);
    
    /**
     * Find songs by rating greater than or equal to specified value.
     * Ordered by play count descending for popularity ranking.
     */
    List<Song> findByRatingGreaterThanEqualOrderByPlayCountDesc(Integer minRating);
    
    /**
     * Find songs by track length between min and max duration.
     * Useful for filtering by song duration.
     */
    List<Song> findByTrackLengthBetween(Integer minLength, Integer maxLength);
    
    /**
     * Find songs released in a specific year.
     * Extracts year from LocalDate for filtering.
     */
    @Query("SELECT s FROM Song s WHERE YEAR(s.dateReleased) = :year")
    List<Song> findByReleaseYear(@Param("year") int year);
    
    /**
     * Find popular songs with minimum rating, ordered by play count.
     * Replaces complex JDBC query from SongDAO.
     */
    @Query("SELECT s FROM Song s WHERE s.rating >= :minRating ORDER BY s.playCount DESC")
    List<Song> findPopularSongs(@Param("minRating") Integer minRating);
    
    /**
     * Search songs by name containing search term (case-insensitive).
     * Replaces SongDAO.search() method functionality.
     */
    @Query("SELECT s FROM Song s WHERE LOWER(s.songName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Song> searchByName(@Param("searchTerm") String searchTerm);
    
    /**
     * Full-text search across song name and lyrics.
     * Uses native SQL for advanced text search capabilities.
     * Note: H2 database doesn't support MATCH/AGAINST, so using LIKE for compatibility.
     */
    @Query(value = "SELECT * FROM songs WHERE LOWER(song_name) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(lyrics) LIKE LOWER(CONCAT('%', ?1, '%'))", nativeQuery = true)
    List<Song> fullTextSearch(String searchTerm);
    
    /**
     * Record song playback by incrementing play count.
     * Updates play count and last played timestamp.
     */
    @Query("UPDATE Song s SET s.playCount = s.playCount + 1, s.lastPlayed = CURRENT_TIMESTAMP WHERE s.songId = :songId")
    void recordPlayback(@Param("songId") Long songId);
    
    /**
     * Find songs with pagination support.
     * Replaces SongDAO.findWithPagination() method.
     * Note: Pagination is handled by Spring Data JPA Pageable parameter in service layer.
     */
    List<Song> findByOrderBySongNameAsc();
    
    /**
     * Count songs by artist for statistics.
     */
    long countByArtistArtistName(String artistName);
    
    /**
     * Count songs by genre for analytics.
     */
    long countByGenre(String genre);
}
