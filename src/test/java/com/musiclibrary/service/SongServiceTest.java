package com.musiclibrary.service;

import com.musiclibrary.dao.SongDAO;
import com.musiclibrary.model.Song;
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
 * Unit tests for SongService using traditional Java 7 testing patterns.
 * 
 * Testing Approach:
 * - Uses JUnit 4 for traditional test framework support (pre-Java 8)
 * - Uses Mockito 1.x for mocking DAO dependencies
 * - Tests business logic validation and error handling
 * - Verifies service layer behavior independent of database
 * - Demonstrates legacy testing patterns for migration reference
 * 
 * Migration Opportunities:
 * - JUnit 4 -> JUnit 5 with @ExtendWith annotations
 * - Mockito 1.x -> Mockito 3+ with improved syntax
 * - Manual MockitoAnnotations.initMocks() -> @MockitoJUnitRunner
 * - Traditional assertions -> AssertJ fluent assertions
 * - @Before -> @BeforeEach
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class SongServiceTest {
    
    @Mock
    private SongDAO mockSongDAO;
    
    private SongService songService;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        songService = new SongService(mockSongDAO);
    }
    
    @Test
    public void testCreateSong_ValidSong_Success() throws SQLException {
        // Arrange
        Song inputSong = createValidSong();
        Song expectedSong = createValidSong();
        expectedSong.setSongId(1L);
        
        when(mockSongDAO.create(any(Song.class))).thenReturn(expectedSong);
        
        // Act
        Song result = songService.createSong(inputSong);
        
        // Assert
        assertNotNull("Created song should not be null", result);
        assertEquals("Song ID should be set", Long.valueOf(1L), result.getSongId());
        assertEquals("Song name should match", "Test Song", result.getSongName());
        verify(mockSongDAO, times(1)).create(any(Song.class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateSong_NullSong_ThrowsException() {
        // Act & Assert
        songService.createSong(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateSong_EmptySongName_ThrowsException() {
        // Arrange
        Song song = createValidSong();
        song.setSongName("");
        
        // Act & Assert
        songService.createSong(song);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateSong_NullSongName_ThrowsException() {
        // Arrange
        Song song = createValidSong();
        song.setSongName(null);
        
        // Act & Assert
        songService.createSong(song);
    }
    
    @Test(expected = RuntimeException.class)
    public void testCreateSong_DatabaseError_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Song song = createValidSong();
        when(mockSongDAO.create(any(Song.class))).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        songService.createSong(song);
    }
    
    @Test
    public void testGetSongById_ValidId_Success() throws SQLException {
        // Arrange
        Long songId = 1L;
        Song expectedSong = createValidSong();
        expectedSong.setSongId(songId);
        
        when(mockSongDAO.findById(songId)).thenReturn(expectedSong);
        
        // Act
        Song result = songService.getSongById(songId);
        
        // Assert
        assertNotNull("Retrieved song should not be null", result);
        assertEquals("Song ID should match", songId, result.getSongId());
        verify(mockSongDAO, times(1)).findById(songId);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetSongById_NullId_ThrowsException() {
        // Act & Assert
        songService.getSongById(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetSongById_InvalidId_ThrowsException() {
        // Act & Assert
        songService.getSongById(0L);
    }
    
    @Test
    public void testGetAllSongs_Success() throws SQLException {
        // Arrange
        List<Song> expectedSongs = new ArrayList<Song>();
        expectedSongs.add(createValidSong());
        expectedSongs.add(createValidSong());
        
        when(mockSongDAO.findAll()).thenReturn(expectedSongs);
        
        // Act
        List<Song> result = songService.getAllSongs();
        
        // Assert
        assertNotNull("Songs list should not be null", result);
        assertEquals("Should return 2 songs", 2, result.size());
        verify(mockSongDAO, times(1)).findAll();
    }
    
    @Test
    public void testUpdateSong_ValidSong_Success() throws SQLException {
        // Arrange
        Song song = createValidSong();
        song.setSongId(1L);
        
        when(mockSongDAO.update(any(Song.class))).thenReturn(song);
        
        // Act
        Song result = songService.updateSong(song);
        
        // Assert
        assertNotNull("Updated song should not be null", result);
        assertEquals("Song ID should match", Long.valueOf(1L), result.getSongId());
        verify(mockSongDAO, times(1)).update(any(Song.class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateSong_NullId_ThrowsException() {
        // Arrange
        Song song = createValidSong();
        song.setSongId(null);
        
        // Act & Assert
        songService.updateSong(song);
    }
    
    @Test
    public void testDeleteSong_ValidId_Success() throws SQLException {
        // Arrange
        Long songId = 1L;
        when(mockSongDAO.delete(songId)).thenReturn(true);
        
        // Act
        boolean result = songService.deleteSong(songId);
        
        // Assert
        assertTrue("Delete should return true", result);
        verify(mockSongDAO, times(1)).delete(songId);
    }
    
    @Test
    public void testSearchSongs_ValidQuery_Success() throws SQLException {
        // Arrange
        String query = "test";
        List<Song> expectedSongs = new ArrayList<Song>();
        expectedSongs.add(createValidSong());
        
        when(mockSongDAO.search(query)).thenReturn(expectedSongs);
        
        // Act
        List<Song> result = songService.searchSongs(query);
        
        // Assert
        assertNotNull("Search results should not be null", result);
        assertEquals("Should return 1 song", 1, result.size());
        verify(mockSongDAO, times(1)).search(query);
    }
    
    @Test
    public void testSearchSongs_EmptyQuery_ReturnsEmptyList() {
        // Act
        List<Song> result = songService.searchSongs("");
        
        // Assert
        assertNotNull("Search results should not be null", result);
        assertTrue("Search results should be empty", result.isEmpty());
        try {
            verify(mockSongDAO, never()).search(anyString());
        } catch (SQLException e) {
            fail("Verify should not throw SQLException");
        }
    }
    
    @Test
    public void testGetSongsByArtist_ValidArtist_Success() {
        // Arrange
        String artistName = "Test Artist";
        List<Song> expectedSongs = new ArrayList<Song>();
        expectedSongs.add(createValidSong());
        
        try {
            when(mockSongDAO.findByArtist(artistName)).thenReturn(expectedSongs);
        } catch (SQLException e) {
            fail("Mock setup should not throw SQLException");
        }
        
        // Act
        List<Song> result = songService.getSongsByArtist(artistName);
        
        // Assert
        assertNotNull("Results should not be null", result);
        assertEquals("Should return 1 song", 1, result.size());
        try {
            verify(mockSongDAO, times(1)).findByArtist(artistName);
        } catch (SQLException e) {
            fail("Verify should not throw SQLException");
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetSongsByArtist_EmptyArtist_ThrowsException() {
        // Act & Assert
        songService.getSongsByArtist("");
    }
    
    @Test
    public void testGetSongsByAlbum_ValidAlbum_Success() throws SQLException {
        // Arrange
        String albumName = "Test Album";
        List<Song> expectedSongs = new ArrayList<Song>();
        expectedSongs.add(createValidSong());
        
        when(mockSongDAO.findByAlbum(albumName)).thenReturn(expectedSongs);
        
        // Act
        List<Song> result = songService.getSongsByAlbum(albumName);
        
        // Assert
        assertNotNull("Results should not be null", result);
        assertEquals("Should return 1 song", 1, result.size());
        verify(mockSongDAO, times(1)).findByAlbum(albumName);
    }
    
    /**
     * Helper method to create a valid song for testing.
     * Centralizes test data creation for consistency.
     */
    private Song createValidSong() {
        Song song = new Song();
        song.setSongName("Test Song");
        song.setArtistId(1L);
        song.setArtistName("Test Artist");
        song.setAlbumId(1L);
        song.setTrackNumber(1);
        song.setTrackLength(180); // 3 minutes
        song.setDateReleased(new Date());
        song.setGenre("Rock");
        song.setRating(4); // Integer rating (0-5 stars)
        return song;
    }
}
