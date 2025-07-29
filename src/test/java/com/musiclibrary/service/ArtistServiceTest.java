package com.musiclibrary.service;

import com.musiclibrary.dao.ArtistDAO;
import com.musiclibrary.model.Artist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ArtistService using modern Java 17 testing patterns.
 * 
 * Testing Approach:
 * - Validates artist business logic and data integrity rules
 * - Tests formation year validation and country name normalization
 * - Verifies error handling for invalid artist data
 * - Tests pagination and search functionality
 * - Demonstrates modern service testing patterns after migration
 * 
 * @author Music Library Development Team
 * @version 2.0
 * @since Java 17
 */
@ExtendWith(MockitoExtension.class)
public class ArtistServiceTest {
    
    @Mock
    private ArtistDAO mockArtistDAO;
    
    private ArtistService artistService;
    
    @BeforeEach
    public void setUp() {
        artistService = new ArtistService(mockArtistDAO);
    }
    
    @Test
    public void testCreateArtist_ValidArtist_Success() throws SQLException {
        // Arrange
        Artist inputArtist = createValidArtist();
        Artist expectedArtist = createValidArtist();
        expectedArtist.setArtistId(1L);
        
        when(mockArtistDAO.create(any(Artist.class))).thenReturn(expectedArtist);
        
        // Act
        Artist result = artistService.createArtist(inputArtist);
        
        // Assert
        assertNotNull(result, "Created artist should not be null");
        assertEquals(Long.valueOf(1L), result.getArtistId(), "Artist ID should be set");
        assertEquals("Test Artist", result.getArtistName(), "Artist name should match");
        verify(mockArtistDAO, times(1)).create(any(Artist.class));
    }
    
    @Test
    public void testCreateArtist_NullArtist_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            artistService.createArtist(null);
        });
    }
    
    @Test
    public void testCreateArtist_EmptyArtistName_ThrowsException() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setArtistName("");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            artistService.createArtist(artist);
        });
    }
    
    @Test
    public void testCreateArtist_NullArtistName_ThrowsException() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setArtistName(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            artistService.createArtist(artist);
        });
    }
    
    @Test
    public void testCreateArtist_FutureFormationYear_ThrowsException() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setFormedYear(2030); // Future year
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            artistService.createArtist(artist);
        });
    }
    
    @Test
    public void testCreateArtist_TooOldFormationYear_ThrowsException() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setFormedYear(1800); // Too old
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            artistService.createArtist(artist);
        });
    }
    
    @Test
    public void testCreateArtist_DatabaseError_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Artist artist = createValidArtist();
        when(mockArtistDAO.create(any(Artist.class))).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            artistService.createArtist(artist);
        });
    }
    
    @Test
    public void testGetArtistById_ValidId_Success() throws SQLException {
        // Arrange
        Long artistId = 1L;
        Artist expectedArtist = createValidArtist();
        expectedArtist.setArtistId(artistId);
        
        when(mockArtistDAO.findById(artistId)).thenReturn(expectedArtist);
        
        // Act
        Artist result = artistService.getArtistById(artistId);
        
        // Assert
        assertNotNull(result, "Retrieved artist should not be null");
        assertEquals(artistId, result.getArtistId(), "Artist ID should match");
        verify(mockArtistDAO, times(1)).findById(artistId);
    }
    
    @Test
    public void testGetArtistById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            artistService.getArtistById(null);
        });
    }
    
    @Test
    public void testGetArtistById_InvalidId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            artistService.getArtistById(0L);
        });
    }
    
    @Test
    public void testGetAllArtists_Success() throws SQLException {
        // Arrange
        List<Artist> expectedArtists = new ArrayList<Artist>();
        expectedArtists.add(createValidArtist());
        expectedArtists.add(createValidArtist());
        
        when(mockArtistDAO.findAll()).thenReturn(expectedArtists);
        
        // Act
        List<Artist> result = artistService.getAllArtists();
        
        // Assert
        assertNotNull(result, "Artists list should not be null");
        assertEquals(2, result.size(), "Should return 2 artists");
        verify(mockArtistDAO, times(1)).findAll();
    }
    
    @Test
    public void testUpdateArtist_ValidArtist_Success() throws SQLException {
        // Arrange
        Artist artist = createValidArtist();
        artist.setArtistId(1L);
        
        when(mockArtistDAO.update(any(Artist.class))).thenReturn(artist);
        
        // Act
        Artist result = artistService.updateArtist(artist);
        
        // Assert
        assertNotNull(result, "Updated artist should not be null");
        assertEquals(Long.valueOf(1L), result.getArtistId(), "Artist ID should match");
        verify(mockArtistDAO, times(1)).update(any(Artist.class));
    }
    
    @Test
    public void testUpdateArtist_NullId_ThrowsException() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setArtistId(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            artistService.updateArtist(artist);
        });
    }
    
    @Test
    public void testDeleteArtist_ValidId_Success() throws SQLException {
        // Arrange
        Long artistId = 1L;
        when(mockArtistDAO.delete(artistId)).thenReturn(true);
        
        // Act
        boolean result = artistService.deleteArtist(artistId);
        
        // Assert
        assertTrue(result, "Delete should return true");
        verify(mockArtistDAO, times(1)).delete(artistId);
    }
    
    @Test
    public void testSearchArtists_ValidQuery_Success() throws SQLException {
        // Arrange
        String query = "test";
        List<Artist> expectedArtists = new ArrayList<Artist>();
        expectedArtists.add(createValidArtist());
        
        when(mockArtistDAO.search(query)).thenReturn(expectedArtists);
        
        // Act
        List<Artist> result = artistService.searchArtists(query);
        
        // Assert
        assertNotNull(result, "Search results should not be null");
        assertEquals(1, result.size(), "Should return 1 artist");
        verify(mockArtistDAO, times(1)).search(query);
    }
    
    @Test
    public void testSearchArtists_EmptyQuery_ReturnsEmptyList() {
        // Act
        List<Artist> result = artistService.searchArtists("");
        
        // Assert
        assertNotNull(result, "Search results should not be null");
        assertTrue(result.isEmpty(), "Search results should be empty");
        try {
            verify(mockArtistDAO, never()).search(anyString());
        } catch (SQLException e) {
            fail("Verify should not throw SQLException");
        }
    }
    
    @Test
    public void testGetArtistsWithPagination_ValidParameters_Success() throws SQLException {
        // Arrange
        int page = 0;
        int size = 10;
        List<Artist> expectedArtists = new ArrayList<Artist>();
        expectedArtists.add(createValidArtist());
        
        when(mockArtistDAO.findWithPagination(0, 10)).thenReturn(expectedArtists);
        
        // Act
        List<Artist> result = artistService.getArtistsWithPagination(page, size);
        
        // Assert
        assertNotNull(result, "Paginated results should not be null");
        assertEquals(1, result.size(), "Should return 1 artist");
        verify(mockArtistDAO, times(1)).findWithPagination(0, 10);
    }
    
    @Test
    public void testGetArtistsWithPagination_NegativePage_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            artistService.getArtistsWithPagination(-1, 10);
        });
    }
    
    @Test
    public void testGetArtistsWithPagination_InvalidSize_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            artistService.getArtistsWithPagination(0, 0);
        });
    }
    
    @Test
    public void testGetTotalArtistCount_Success() throws SQLException {
        // Arrange
        when(mockArtistDAO.count()).thenReturn(5L);
        
        // Act
        long result = artistService.getTotalArtistCount();
        
        // Assert
        assertEquals(5L, result, "Count should match");
        verify(mockArtistDAO, times(1)).count();
    }
    
    @Test
    public void testArtistExists_ExistingArtist_ReturnsTrue() throws SQLException {
        // Arrange
        Long artistId = 1L;
        when(mockArtistDAO.exists(artistId)).thenReturn(true);
        
        // Act
        boolean result = artistService.artistExists(artistId);
        
        // Assert
        assertTrue(result, "Artist should exist");
        verify(mockArtistDAO, times(1)).exists(artistId);
    }
    
    @Test
    public void testArtistExists_NonExistingArtist_ReturnsFalse() throws SQLException {
        // Arrange
        Long artistId = 999L;
        when(mockArtistDAO.exists(artistId)).thenReturn(false);
        
        // Act
        boolean result = artistService.artistExists(artistId);
        
        // Assert
        assertFalse(result, "Artist should not exist");
        verify(mockArtistDAO, times(1)).exists(artistId);
    }
    
    @Test
    public void testGetArtistsByCountry_ValidCountry_Success() throws SQLException {
        // Arrange
        String country = "USA";
        List<Artist> expectedArtists = new ArrayList<Artist>();
        expectedArtists.add(createValidArtist());
        
        when(mockArtistDAO.findByCountry(country)).thenReturn(expectedArtists);
        
        // Act
        List<Artist> result = artistService.getArtistsByCountry(country);
        
        // Assert
        assertNotNull(result, "Results should not be null");
        assertEquals(1, result.size(), "Should return 1 artist");
        verify(mockArtistDAO, times(1)).findByCountry(country);
    }
    
    @Test
    public void testGetArtistsByCountry_EmptyCountry_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            artistService.getArtistsByCountry("");
        });
    }
    
    @Test
    public void testGetArtistsByCountry_NullCountry_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            artistService.getArtistsByCountry(null);
        });
    }
    
    /**
     * Helper method to create a valid artist for testing.
     * Demonstrates centralized test data creation pattern.
     */
    private Artist createValidArtist() {
        Artist artist = new Artist();
        artist.setArtistName("Test Artist");
        artist.setFormedYear(1990);
        artist.setCountry("USA");
        // Note: Artist model doesn't have genre field - genre is at song/album level
        artist.setBiography("Test biography");
        artist.setWebsite("http://www.testartist.com");
        artist.setCreatedDate(new Date());
        return artist;
    }
}
