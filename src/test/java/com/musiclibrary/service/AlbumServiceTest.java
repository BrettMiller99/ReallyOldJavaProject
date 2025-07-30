package com.musiclibrary.service;

import com.musiclibrary.dao.AlbumDAO;
import com.musiclibrary.model.Album;
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
 * Unit tests for AlbumService using modern Java 21 testing patterns.
 * 
 * Testing Approach:
 * - Validates album business logic and artist relationships
 * - Tests release date validation and genre normalization
 * - Verifies error handling for invalid album data
 * - Tests filtering by artist, genre, and year
 * - Demonstrates modern service testing patterns after migration
 * 
 * @author Music Library Development Team
 * @version 2.0
 * @since Java 21
 */
@ExtendWith(MockitoExtension.class)
public class AlbumServiceTest {
    
    @Mock
    private AlbumDAO mockAlbumDAO;
    
    private AlbumService albumService;
    
    @BeforeEach
    public void setUp() {
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
        assertNotNull(result, "Created album should not be null");
        assertEquals(Long.valueOf(1L), result.getAlbumId(), "Album ID should be set");
        assertEquals("Test Album", result.getAlbumName(), "Album name should match");
        verify(mockAlbumDAO, times(1)).create(any(Album.class));
    }
    
    @Test
    public void testCreateAlbum_NullAlbum_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.createAlbum(null);
        });
    }
    
    @Test
    public void testCreateAlbum_EmptyAlbumName_ThrowsException() {
        // Arrange
        Album album = createValidAlbum();
        album.setAlbumName("");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.createAlbum(album);
        });
    }
    
    @Test
    public void testCreateAlbum_NullAlbumName_ThrowsException() {
        // Arrange
        Album album = createValidAlbum();
        album.setAlbumName(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.createAlbum(album);
        });
    }
    
    @Test
    public void testCreateAlbum_NullArtistId_ThrowsException() {
        // Arrange
        Album album = createValidAlbum();
        album.setArtistId(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.createAlbum(album);
        });
    }
    
    @Test
    public void testCreateAlbum_InvalidArtistId_ThrowsException() {
        // Arrange
        Album album = createValidAlbum();
        album.setArtistId(0L);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.createAlbum(album);
        });
    }
    
    @Test
    public void testCreateAlbum_DatabaseError_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Album album = createValidAlbum();
        when(mockAlbumDAO.create(any(Album.class))).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            albumService.createAlbum(album);
        });
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
        assertNotNull(result, "Retrieved album should not be null");
        assertEquals(albumId, result.getAlbumId(), "Album ID should match");
        verify(mockAlbumDAO, times(1)).findById(albumId);
    }
    
    @Test
    public void testGetAlbumById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.getAlbumById(null);
        });
    }
    
    @Test
    public void testGetAlbumById_InvalidId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.getAlbumById(0L);
        });
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
        assertNotNull(result, "Albums list should not be null");
        assertEquals(2, result.size(), "Should return 2 albums");
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
        assertNotNull(result, "Updated album should not be null");
        assertEquals(Long.valueOf(1L), result.getAlbumId(), "Album ID should match");
        verify(mockAlbumDAO, times(1)).update(any(Album.class));
    }
    
    @Test
    public void testUpdateAlbum_NullId_ThrowsException() {
        // Arrange
        Album album = createValidAlbum();
        album.setAlbumId(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.updateAlbum(album);
        });
    }
    
    @Test
    public void testDeleteAlbum_ValidId_Success() throws SQLException {
        // Arrange
        Long albumId = 1L;
        when(mockAlbumDAO.delete(albumId)).thenReturn(true);
        
        // Act
        boolean result = albumService.deleteAlbum(albumId);
        
        // Assert
        assertTrue(result, "Delete should return true");
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
        assertNotNull(result, "Search results should not be null");
        assertEquals(1, result.size(), "Should return 1 album");
        verify(mockAlbumDAO, times(1)).search(query);
    }
    
    @Test
    public void testSearchAlbums_EmptyQuery_ReturnsEmptyList() {
        // Act
        List<Album> result = albumService.searchAlbums("");
        
        // Assert
        assertNotNull(result, "Search results should not be null");
        assertTrue(result.isEmpty(), "Search results should be empty");
        try {
            verify(mockAlbumDAO, never()).search(anyString());
        } catch (SQLException e) {
            fail("Verify should not throw SQLException");
        }
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
        assertNotNull(result, "Results should not be null");
        assertEquals(1, result.size(), "Should return 1 album");
        verify(mockAlbumDAO, times(1)).findByArtist(artistId);
    }
    
    @Test
    public void testGetAlbumsByArtist_NullArtistId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.getAlbumsByArtist((Long) null);
        });
    }
    
    @Test
    public void testGetAlbumsByArtist_InvalidArtistId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.getAlbumsByArtist(0L);
        });
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
        assertNotNull(result, "Results should not be null");
        assertEquals(1, result.size(), "Should return 1 album");
        verify(mockAlbumDAO, times(1)).findByArtist(artistName);
    }
    
    @Test
    public void testGetAlbumsByArtistName_EmptyArtistName_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.getAlbumsByArtistName("");
        });
    }
    
    @Test
    public void testGetAlbumsByArtistName_NullArtistName_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.getAlbumsByArtistName(null);
        });
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
        assertNotNull(result, "Results should not be null");
        assertEquals(1, result.size(), "Should return 1 album");
        verify(mockAlbumDAO, times(1)).findByGenre(genre);
    }
    
    @Test
    public void testGetAlbumsByGenre_EmptyGenre_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.getAlbumsByGenre("");
        });
    }
    
    @Test
    public void testGetAlbumsByGenre_NullGenre_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.getAlbumsByGenre(null);
        });
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
        assertNotNull(result, "Results should not be null");
        assertEquals(1, result.size(), "Should return 1 album");
        verify(mockAlbumDAO, times(1)).findByYear(year);
    }
    
    @Test
    public void testGetAlbumsByYear_TooOldYear_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.getAlbumsByYear(1800);
        });
    }
    
    @Test
    public void testGetAlbumsByYear_FutureYear_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.getAlbumsByYear(2050);
        });
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
        assertNotNull(result, "Paginated results should not be null");
        assertEquals(1, result.size(), "Should return 1 album");
        verify(mockAlbumDAO, times(1)).findWithPagination(0, 10);
    }
    
    @Test
    public void testGetAlbumsWithPagination_NegativePage_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.getAlbumsWithPagination(-1, 10);
        });
    }
    
    @Test
    public void testGetAlbumsWithPagination_InvalidSize_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            albumService.getAlbumsWithPagination(0, 0);
        });
    }
    
    @Test
    public void testGetTotalAlbumCount_Success() throws SQLException {
        // Arrange
        when(mockAlbumDAO.count()).thenReturn(3L);
        
        // Act
        long result = albumService.getTotalAlbumCount();
        
        // Assert
        assertEquals(3L, result, "Count should match");
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
        assertTrue(result, "Album should exist");
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
        assertFalse(result, "Album should not exist");
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
