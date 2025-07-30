package com.musiclibrary.repository;

import com.musiclibrary.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA Repository for Album entities.
 * 
 * Replaces the traditional AlbumDAO with modern Spring Data JPA approach.
 * Provides automatic CRUD operations and custom query methods.
 * 
 * Migration Benefits:
 * - Eliminates 180+ lines of manual JDBC code
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
public interface AlbumRepository extends JpaRepository<Album, Long> {
    
    
    /**
     * Find albums by artist ID.
     * Replaces AlbumDAO.findByArtist() method for ID-based queries.
     */
    List<Album> findByArtistArtistId(Long artistId);
    
    /**
     * Find albums by artist name (case-insensitive).
     * Replaces AlbumDAO.findByArtist() method.
     */
    List<Album> findByArtistArtistNameIgnoreCase(String artistName);
    
    /**
     * Find albums by genre (case-insensitive).
     * Replaces AlbumDAO.findByGenre() method.
     */
    List<Album> findByGenreIgnoreCase(String genre);
    
    /**
     * Find albums released in a specific year.
     * Replaces AlbumDAO.findByYear() method.
     */
    @Query("SELECT a FROM Album a WHERE YEAR(a.releaseDate) = :year")
    List<Album> findByReleaseYear(@Param("year") int year);
    
    /**
     * Find albums by record label (case-insensitive).
     * Useful for label-specific queries.
     */
    List<Album> findByRecordLabelIgnoreCase(String recordLabel);
    
    /**
     * Find albums released between two dates.
     * Useful for date range filtering.
     */
    List<Album> findByReleaseDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find albums by total tracks count.
     * Useful for filtering by album type (single, EP, full album).
     */
    List<Album> findByTotalTracks(Integer totalTracks);
    
    /**
     * Find albums with track count greater than specified value.
     * Useful for finding full-length albums.
     */
    List<Album> findByTotalTracksGreaterThan(Integer minTracks);
    
    /**
     * Search albums by name containing search term (case-insensitive).
     * Replaces AlbumDAO.search() method functionality.
     */
    @Query("SELECT a FROM Album a WHERE LOWER(a.albumName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Album> searchByName(@Param("searchTerm") String searchTerm);
    
    /**
     * Find albums ordered by release date (newest first).
     * Useful for chronological browsing.
     */
    List<Album> findByOrderByReleaseDateDesc();
    
    /**
     * Find albums ordered by album name alphabetically.
     * Replaces AlbumDAO.findWithPagination() ordering.
     */
    List<Album> findByOrderByAlbumNameAsc();
    
    /**
     * Count albums by artist for statistics.
     */
    long countByArtistArtistName(String artistName);
    
    /**
     * Count albums by genre for analytics.
     */
    long countByGenre(String genre);
    
    /**
     * Find recent albums released after specified date.
     * Useful for "new releases" functionality.
     */
    List<Album> findByReleaseDateAfterOrderByReleaseDateDesc(LocalDate date);
    
    /**
     * Check if album exists by name and artist (for duplicate prevention).
     * Replaces manual validation logic in AlbumDAO.
     */
    boolean existsByAlbumNameAndArtistArtistName(String albumName, String artistName);
}
