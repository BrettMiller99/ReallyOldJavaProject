package com.musiclibrary.service;

import com.musiclibrary.dao.AlbumDAO;
import com.musiclibrary.model.Album;
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
 * Unit tests for AlbumService using traditional Java 7 testing patterns.
 * 
 * Testing Approach:
 * - Validates album business logic and artist relationships
 * - Tests release date validation and genre normalization
 * - Verifies error handling for invalid album data
 * - Tests filtering by artist, genre, and year
 * - Demonstrates legacy service testing with manual date handling
 * 
 * Migration Opportunities:
 * - Manual date validation -> Java 8 LocalDate with built-in validation
 * - Traditional assertions -> more expressive fluent assertions
 * - Manual test data setup -> test data builders or factories
 * - JUnit 4 -> JUnit 5 with parameterized and dynamic tests
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class AlbumServiceTest {
    
    @Mock
    private AlbumDAO mockAlbumDAO;
    
    private AlbumService albumService;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        albumService = new AlbumService(mockAlbumDAO);
    }
    
    @Test
    public void testCreateAlbum_ValidAlbum_Success() throws SQLException {
        // Arrange
        Album inputAlbum = createValidAlbum();
        Album expectedAlbum = createValidAlbum();
        expectedAlbum.setAlbumId(1L);
        
        when(mockAlbumDAO.create(any(Album.class))).thenReturn(expectedAlbum);
        
        // Act
        Album result = albumService.createAlbum(inputAlbum);
        
        // Assert
        assertNotNull("Created album should not be null", result);
        assertEquals("Album ID should be set", Long.valueOf(1L), result.getAlbumId());
        assertEquals("Album name should match", "Test Album", result.getAlbumName());
        verify(mockAlbumDAO, times(1)).create(any(Album.class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateAlbum_NullAlbum_ThrowsException() {
        // Act & Assert
        albumService.createAlbum(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateAlbum_EmptyAlbumName_ThrowsException() {
        // Arrange
        Album album = createValidAlbum();
        album.setAlbumName("");
        
        // Act & Assert
        albumService.createAlbum(album);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateAlbum_NullAlbumName_ThrowsException() {
        // Arrange
        Album album = createValidAlbum();
        album.setAlbumName(null);
        
        // Act & Assert
        albumService.createAlbum(album);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateAlbum_NullArtistId_ThrowsException() {
        // Arrange
        Album album = createValidAlbum();
        album.setArtistId(null);
        
        // Act & Assert
        albumService.createAlbum(album);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateAlbum_InvalidArtistId_ThrowsException() {
        // Arrange
        Album album = createValidAlbum();
        album.setArtistId(0L);
        
        // Act & Assert
        albumService.createAlbum(album);
    }
    
    @Test(expected = RuntimeException.class)
    public void testCreateAlbum_DatabaseError_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Album album = createValidAlbum();
        when(mockAlbumDAO.create(any(Album.class))).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        albumService.createAlbum(album);
    }
    
    @Test
    public void testGetAlbumById_ValidId_Success() throws SQLException {
        // Arrange
        Long albumId = 1L;
        Album expectedAlbum = createValidAlbum();
        expectedAlbum.setAlbumId(albumId);
        
        when(mockAlbumDAO.findById(albumId)).thenReturn(expectedAlbum);
        
        // Act
        Album result = albumService.getAlbumById(albumId);
        
        // Assert
        assertNotNull("Retrieved album should not be null", result);
        assertEquals("Album ID should match", albumId, result.getAlbumId());
        verify(mockAlbumDAO, times(1)).findById(albumId);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetAlbumById_NullId_ThrowsException() {
        // Act & Assert
        albumService.getAlbumById(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetAlbumById_InvalidId_ThrowsException() {
        // Act & Assert
        albumService.getAlbumById(0L);
    }
    
    @Test
    public void testGetAllAlbums_Success() throws SQLException {
        // Arrange
        List<Album> expectedAlbums = new ArrayList<Album>();
        expectedAlbums.add(createValidAlbum());
        expectedAlbums.add(createValidAlbum());
        
        when(mockAlbumDAO.findAll()).thenReturn(expectedAlbums);
        
        // Act
        List<Album> result = albumService.getAllAlbums();
        
        // Assert
        assertNotNull("Albums list should not be null", result);
        assertEquals("Should return 2 albums", 2, result.size());
        verify(mockAlbumDAO, times(1)).findAll();
    }
    
    @Test
    public void testUpdateAlbum_ValidAlbum_Success() throws SQLException {
        // Arrange
        Album album = createValidAlbum();
        album.setAlbumId(1L);
        
        when(mockAlbumDAO.update(any(Album.class))).thenReturn(album);
        
        // Act
        Album result = albumService.updateAlbum(album);
        
        // Assert
        assertNotNull("Updated album should not be null", result);
        assertEquals("Album ID should match", Long.valueOf(1L), result.getAlbumId());
        verify(mockAlbumDAO, times(1)).update(any(Album.class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateAlbum_NullId_ThrowsException() {
        // Arrange
        Album album = createValidAlbum();
        album.setAlbumId(null);
        
        // Act & Assert
        albumService.updateAlbum(album);
    }
    
    @Test
    public void testDeleteAlbum_ValidId_Success() throws SQLException {
        // Arrange
        Long albumId = 1L;
        when(mockAlbumDAO.delete(albumId)).thenReturn(true);
        
        // Act
        boolean result = albumService.deleteAlbum(albumId);
        
        // Assert
        assertTrue("Delete should return true", result);
        verify(mockAlbumDAO, times(1)).delete(albumId);
    }
    
    @Test
    public void testSearchAlbums_ValidQuery_Success() throws SQLException {
        // Arrange
        String query = "test";
        List<Album> expectedAlbums = new ArrayList<Album>();
        expectedAlbums.add(createValidAlbum());
        
        when(mockAlbumDAO.search(query)).thenReturn(expectedAlbums);
        
        // Act
        List<Album> result = albumService.searchAlbums(query);
        
        // Assert
        assertNotNull("Search results should not be null", result);
        assertEquals("Should return 1 album", 1, result.size());
        verify(mockAlbumDAO, times(1)).search(query);
    }
    
    @Test
    public void testSearchAlbums_EmptyQuery_ReturnsEmptyList() {
        // Act
        List<Album> result = albumService.searchAlbums("");
        
        // Assert
        assertNotNull("Search results should not be null", result);
        assertTrue("Search results should be empty", result.isEmpty());
        verify(mockAlbumDAO, never()).search(anyString());
    }
    
    @Test
    public void testGetAlbumsByArtist_ValidArtistId_Success() throws SQLException {
        // Arrange
        Long artistId = 1L;
        List<Album> expectedAlbums = new ArrayList<Album>();
        expectedAlbums.add(createValidAlbum());
        
        when(mockAlbumDAO.findByArtist(artistId)).thenReturn(expectedAlbums);
        
        // Act
        List<Album> result = albumService.getAlbumsByArtist(artistId);
        
        // Assert
        assertNotNull("Results should not be null", result);
        assertEquals("Should return 1 album", 1, result.size());
        verify(mockAlbumDAO, times(1)).findByArtist(artistId);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetAlbumsByArtist_NullArtistId_ThrowsException() {
        // Act & Assert
        albumService.getAlbumsByArtist((Long) null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetAlbumsByArtist_InvalidArtistId_ThrowsException() {
        // Act & Assert
        albumService.getAlbumsByArtist(0L);
    }
    
    @Test
    public void testGetAlbumsByArtistName_ValidArtistName_Success() throws SQLException {
        // Arrange
        String artistName = "Test Artist";
        List<Album> expectedAlbums = new ArrayList<Album>();
        expectedAlbums.add(createValidAlbum());
        
        when(mockAlbumDAO.findByArtist(artistName)).thenReturn(expectedAlbums);
        
        // Act
        List<Album> result = albumService.getAlbumsByArtistName(artistName);
        
        // Assert
        assertNotNull("Results should not be null", result);
        assertEquals("Should return 1 album", 1, result.size());
        verify(mockAlbumDAO, times(1)).findByArtist(artistName);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetAlbumsByArtistName_EmptyArtistName_ThrowsException() {
        // Act & Assert
        albumService.getAlbumsByArtistName("");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetAlbumsByArtistName_NullArtistName_ThrowsException() {
        // Act & Assert
        albumService.getAlbumsByArtistName(null);
    }
    
    @Test
    public void testGetAlbumsByGenre_ValidGenre_Success() throws SQLException {
        // Arrange
        String genre = "Rock";
        List<Album> expectedAlbums = new ArrayList<Album>();
        expectedAlbums.add(createValidAlbum());
        
        when(mockAlbumDAO.findByGenre(genre)).thenReturn(expectedAlbums);
        
        // Act
        List<Album> result = albumService.getAlbumsByGenre(genre);
        
        // Assert
        assertNotNull("Results should not be null", result);
        assertEquals("Should return 1 album", 1, result.size());
        verify(mockAlbumDAO, times(1)).findByGenre(genre);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetAlbumsByGenre_EmptyGenre_ThrowsException() {
        // Act & Assert
        albumService.getAlbumsByGenre("");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetAlbumsByGenre_NullGenre_ThrowsException() {
        // Act & Assert
        albumService.getAlbumsByGenre(null);
    }
    
    @Test
    public void testGetAlbumsByYear_ValidYear_Success() throws SQLException {
        // Arrange
        int year = 2020;
        List<Album> expectedAlbums = new ArrayList<Album>();
        expectedAlbums.add(createValidAlbum());
        
        when(mockAlbumDAO.findByYear(year)).thenReturn(expectedAlbums);
        
        // Act
        List<Album> result = albumService.getAlbumsByYear(year);
        
        // Assert
        assertNotNull("Results should not be null", result);
        assertEquals("Should return 1 album", 1, result.size());
        verify(mockAlbumDAO, times(1)).findByYear(year);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetAlbumsByYear_TooOldYear_ThrowsException() {
        // Act & Assert
        albumService.getAlbumsByYear(1800);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetAlbumsByYear_FutureYear_ThrowsException() {
        // Act & Assert
        albumService.getAlbumsByYear(2050);
    }
    
    @Test
    public void testGetAlbumsWithPagination_ValidParameters_Success() throws SQLException {
        // Arrange
        int page = 0;
        int size = 10;
        List<Album> expectedAlbums = new ArrayList<Album>();
        expectedAlbums.add(createValidAlbum());
        
        when(mockAlbumDAO.findWithPagination(0, 10)).thenReturn(expectedAlbums);
        
        // Act
        List<Album> result = albumService.getAlbumsWithPagination(page, size);
        
        // Assert
        assertNotNull("Paginated results should not be null", result);
        assertEquals("Should return 1 album", 1, result.size());
        verify(mockAlbumDAO, times(1)).findWithPagination(0, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetAlbumsWithPagination_NegativePage_ThrowsException() {
        // Act & Assert
        albumService.getAlbumsWithPagination(-1, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetAlbumsWithPagination_InvalidSize_ThrowsException() {
        // Act & Assert
        albumService.getAlbumsWithPagination(0, 0);
    }
    
    @Test
    public void testGetTotalAlbumCount_Success() throws SQLException {
        // Arrange
        when(mockAlbumDAO.count()).thenReturn(3L);
        
        // Act
        long result = albumService.getTotalAlbumCount();
        
        // Assert
        assertEquals("Count should match", 3L, result);
        verify(mockAlbumDAO, times(1)).count();
    }
    
    @Test
    public void testAlbumExists_ExistingAlbum_ReturnsTrue() throws SQLException {
        // Arrange
        Long albumId = 1L;
        when(mockAlbumDAO.exists(albumId)).thenReturn(true);
        
        // Act
        boolean result = albumService.albumExists(albumId);
        
        // Assert
        assertTrue("Album should exist", result);
        verify(mockAlbumDAO, times(1)).exists(albumId);
    }
    
    @Test
    public void testAlbumExists_NonExistingAlbum_ReturnsFalse() throws SQLException {
        // Arrange
        Long albumId = 999L;
        when(mockAlbumDAO.exists(albumId)).thenReturn(false);
        
        // Act
        boolean result = albumService.albumExists(albumId);
        
        // Assert
        assertFalse("Album should not exist", result);
        verify(mockAlbumDAO, times(1)).exists(albumId);
    }
    
    /**
     * Helper method to create a valid album for testing.
     * Demonstrates centralized test data creation with relationships.
     */
    private Album createValidAlbum() {
        Album album = new Album();
        album.setAlbumName("Test Album");
        album.setArtistId(1L);
        album.setReleaseDate(new Date());
        album.setGenre("Rock");
        album.setRecordLabel("Test Records");
        album.setTotalTracks(10);
        album.setCreatedDate(new Date());
        return album;
    }
}
