package com.musiclibrary.repository;

import com.musiclibrary.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Artist entities.
 * 
 * Replaces the traditional ArtistDAO with modern Spring Data JPA approach.
 * Provides automatic CRUD operations and custom query methods.
 * 
 * Migration Benefits:
 * - Eliminates 150+ lines of manual JDBC code
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
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    
    
    /**
     * Find artist by name (case-insensitive).
     * Replaces ArtistDAO.findByName() method.
     * Returns Optional to handle null cases safely.
     */
    Optional<Artist> findByArtistNameIgnoreCase(String artistName);
    
    /**
     * Find artists by country (case-insensitive).
     * Replaces ArtistDAO.findByCountry() method.
     */
    List<Artist> findByCountryIgnoreCase(String country);
    
    /**
     * Find artists formed in a specific year.
     * Useful for historical browsing.
     */
    List<Artist> findByFormedYear(Integer year);
    
    /**
     * Find artists formed between two years.
     * Useful for era-based filtering.
     */
    List<Artist> findByFormedYearBetween(Integer startYear, Integer endYear);
    
    /**
     * Find artists formed after a specific year.
     * Useful for finding newer artists.
     */
    List<Artist> findByFormedYearGreaterThan(Integer year);
    
    /**
     * Search artists by name containing search term (case-insensitive).
     * Replaces ArtistDAO.search() method functionality.
     */
    @Query("SELECT a FROM Artist a WHERE LOWER(a.artistName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Artist> searchByName(@Param("searchTerm") String searchTerm);
    
    /**
     * Search artists by biography containing search term.
     * Useful for content-based search.
     */
    @Query("SELECT a FROM Artist a WHERE LOWER(a.biography) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Artist> searchByBiography(@Param("searchTerm") String searchTerm);
    
    /**
     * Find artists ordered by name alphabetically.
     * Replaces ArtistDAO.findWithPagination() ordering.
     */
    List<Artist> findByOrderByArtistNameAsc();
    
    /**
     * Find artists ordered by formation year (oldest first).
     * Useful for chronological browsing.
     */
    List<Artist> findByOrderByFormedYearAsc();
    
    /**
     * Check if artist exists by name (for duplicate prevention).
     * Replaces manual validation logic in ArtistDAO.
     */
    boolean existsByArtistNameIgnoreCase(String artistName);
    
    /**
     * Count artists by country for statistics.
     */
    long countByCountry(String country);
    
    /**
     * Find artists with albums (artists who have released music).
     * Uses JOIN to find only artists with associated albums.
     */
    @Query("SELECT DISTINCT a FROM Artist a JOIN a.albums")
    List<Artist> findArtistsWithAlbums();
    
    /**
     * Find artists with songs (artists who have released individual tracks).
     * Uses JOIN to find only artists with associated songs.
     */
    @Query("SELECT DISTINCT a FROM Artist a JOIN a.songs")
    List<Artist> findArtistsWithSongs();
    
    /**
     * Find artists by partial name match (for autocomplete functionality).
     * More flexible than exact name search.
     */
    @Query("SELECT a FROM Artist a WHERE LOWER(a.artistName) LIKE LOWER(CONCAT(:prefix, '%'))")
    List<Artist> findByNameStartingWith(@Param("prefix") String prefix);
}
