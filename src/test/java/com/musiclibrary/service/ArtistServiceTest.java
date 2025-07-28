package com.musiclibrary.service;

import com.musiclibrary.dao.ArtistDAO;
import com.musiclibrary.model.Artist;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ArtistService using traditional Java 7 testing patterns.
 * 
 * Testing Approach:
 * - Validates artist business logic and data integrity rules
 * - Tests formation year validation and country name normalization
 * - Verifies error handling for invalid artist data
 * - Tests pagination and search functionality
 * - Demonstrates legacy service testing patterns
 * 
 * Migration Opportunities:
 * - JUnit 4 -> JUnit 5 with improved parameterized tests
 * - Manual date handling -> Java 8 LocalDate
 * - Traditional validation -> Bean Validation with @Valid
 * - Static imports -> more readable fluent assertions
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class ArtistServiceTest {
    
    @Mock
    private ArtistDAO mockArtistDAO;
    
    private ArtistService artistService;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
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
        assertNotNull("Created artist should not be null", result);
        assertEquals("Artist ID should be set", Long.valueOf(1L), result.getArtistId());
        assertEquals("Artist name should match", "Test Artist", result.getArtistName());
        verify(mockArtistDAO, times(1)).create(any(Artist.class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArtist_NullArtist_ThrowsException() {
        // Act & Assert
        artistService.createArtist(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArtist_EmptyArtistName_ThrowsException() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setArtistName("");
        
        // Act & Assert
        artistService.createArtist(artist);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArtist_NullArtistName_ThrowsException() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setArtistName(null);
        
        // Act & Assert
        artistService.createArtist(artist);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArtist_FutureFormationYear_ThrowsException() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setFormationYear(2030); // Future year
        
        // Act & Assert
        artistService.createArtist(artist);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArtist_TooOldFormationYear_ThrowsException() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setFormationYear(1800); // Too old
        
        // Act & Assert
        artistService.createArtist(artist);
    }
    
    @Test(expected = RuntimeException.class)
    public void testCreateArtist_DatabaseError_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Artist artist = createValidArtist();
        when(mockArtistDAO.create(any(Artist.class))).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        artistService.createArtist(artist);
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
        assertNotNull("Retrieved artist should not be null", result);
        assertEquals("Artist ID should match", artistId, result.getArtistId());
        verify(mockArtistDAO, times(1)).findById(artistId);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetArtistById_NullId_ThrowsException() {
        // Act & Assert
        artistService.getArtistById(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetArtistById_InvalidId_ThrowsException() {
        // Act & Assert
        artistService.getArtistById(0L);
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
        assertNotNull("Artists list should not be null", result);
        assertEquals("Should return 2 artists", 2, result.size());
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
        assertNotNull("Updated artist should not be null", result);
        assertEquals("Artist ID should match", Long.valueOf(1L), result.getArtistId());
        verify(mockArtistDAO, times(1)).update(any(Artist.class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateArtist_NullId_ThrowsException() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setArtistId(null);
        
        // Act & Assert
        artistService.updateArtist(artist);
    }
    
    @Test
    public void testDeleteArtist_ValidId_Success() throws SQLException {
        // Arrange
        Long artistId = 1L;
        when(mockArtistDAO.delete(artistId)).thenReturn(true);
        
        // Act
        boolean result = artistService.deleteArtist(artistId);
        
        // Assert
        assertTrue("Delete should return true", result);
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
        assertNotNull("Search results should not be null", result);
        assertEquals("Should return 1 artist", 1, result.size());
        verify(mockArtistDAO, times(1)).search(query);
    }
    
    @Test
    public void testSearchArtists_EmptyQuery_ReturnsEmptyList() {
        // Act
        List<Artist> result = artistService.searchArtists("");
        
        // Assert
        assertNotNull("Search results should not be null", result);
        assertTrue("Search results should be empty", result.isEmpty());
        verify(mockArtistDAO, never()).search(anyString());
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
        assertNotNull("Paginated results should not be null", result);
        assertEquals("Should return 1 artist", 1, result.size());
        verify(mockArtistDAO, times(1)).findWithPagination(0, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetArtistsWithPagination_NegativePage_ThrowsException() {
        // Act & Assert
        artistService.getArtistsWithPagination(-1, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetArtistsWithPagination_InvalidSize_ThrowsException() {
        // Act & Assert
        artistService.getArtistsWithPagination(0, 0);
    }
    
    @Test
    public void testGetTotalArtistCount_Success() throws SQLException {
        // Arrange
        when(mockArtistDAO.count()).thenReturn(5L);
        
        // Act
        long result = artistService.getTotalArtistCount();
        
        // Assert
        assertEquals("Count should match", 5L, result);
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
        assertTrue("Artist should exist", result);
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
        assertFalse("Artist should not exist", result);
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
        assertNotNull("Results should not be null", result);
        assertEquals("Should return 1 artist", 1, result.size());
        verify(mockArtistDAO, times(1)).findByCountry(country);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetArtistsByCountry_EmptyCountry_ThrowsException() {
        // Act & Assert
        artistService.getArtistsByCountry("");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetArtistsByCountry_NullCountry_ThrowsException() {
        // Act & Assert
        artistService.getArtistsByCountry(null);
    }
    
    /**
     * Helper method to create a valid artist for testing.
     * Demonstrates centralized test data creation pattern.
     */
    private Artist createValidArtist() {
        Artist artist = new Artist();
        artist.setArtistName("Test Artist");
        artist.setFormationYear(1990);
        artist.setCountry("USA");
        artist.setGenre("Rock");
        artist.setBiography("Test biography");
        artist.setWebsite("http://www.testartist.com");
        artist.setCreatedDate(new Date());
        return artist;
    }
}
