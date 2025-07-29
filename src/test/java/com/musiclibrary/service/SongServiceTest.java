package com.musiclibrary.service;

import com.musiclibrary.dao.SongDAO;
import com.musiclibrary.model.Song;
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
 * Unit tests for SongService using modern Java 17 testing patterns.
 * 
 * Testing Approach:
 * - Uses JUnit 5 for modern test framework support
 * - Uses Mockito 5.x for mocking DAO dependencies
 * - Tests business logic validation and error handling
 * - Verifies service layer behavior independent of database
 * - Demonstrates modern testing patterns after migration
 * 
 * @author Music Library Development Team
 * @version 2.0
 * @since Java 17
 */
@ExtendWith(MockitoExtension.class)
public class SongServiceTest {
    
    @Mock
    private SongDAO mockSongDAO;
    
    private SongService songService;
    
    @BeforeEach
    public void setUp() {
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
        assertNotNull(result, "Created song should not be null");
        assertEquals(Long.valueOf(1L), result.getSongId(), "Song ID should be set");
        assertEquals("Test Song", result.getSongName(), "Song name should match");
        verify(mockSongDAO, times(1)).create(any(Song.class));
    }
    
    @Test
    public void testCreateSong_NullSong_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            songService.createSong(null);
        });
    }
    
    @Test
    public void testCreateSong_EmptySongName_ThrowsException() {
        // Arrange
        Song song = createValidSong();
        song.setSongName("");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            songService.createSong(song);
        });
    }
    
    @Test
    public void testCreateSong_NullSongName_ThrowsException() {
        // Arrange
        Song song = createValidSong();
        song.setSongName(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            songService.createSong(song);
        });
    }
    
    @Test
    public void testCreateSong_DatabaseError_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Song song = createValidSong();
        when(mockSongDAO.create(any(Song.class))).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            songService.createSong(song);
        });
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
        assertNotNull(result, "Retrieved song should not be null");
        assertEquals(songId, result.getSongId(), "Song ID should match");
        verify(mockSongDAO, times(1)).findById(songId);
    }
    
    @Test
    public void testGetSongById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            songService.getSongById(null);
        });
    }
    
    @Test
    public void testGetSongById_InvalidId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            songService.getSongById(0L);
        });
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
        assertNotNull(result, "Songs list should not be null");
        assertEquals(2, result.size(), "Should return 2 songs");
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
        assertNotNull(result, "Updated song should not be null");
        assertEquals(Long.valueOf(1L), result.getSongId(), "Song ID should match");
        verify(mockSongDAO, times(1)).update(any(Song.class));
    }
    
    @Test
    public void testUpdateSong_NullId_ThrowsException() {
        // Arrange
        Song song = createValidSong();
        song.setSongId(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            songService.updateSong(song);
        });
    }
    
    @Test
    public void testDeleteSong_ValidId_Success() throws SQLException {
        // Arrange
        Long songId = 1L;
        when(mockSongDAO.delete(songId)).thenReturn(true);
        
        // Act
        boolean result = songService.deleteSong(songId);
        
        // Assert
        assertTrue(result, "Delete should return true");
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
        assertNotNull(result, "Search results should not be null");
        assertEquals(1, result.size(), "Should return 1 song");
        verify(mockSongDAO, times(1)).search(query);
    }
    
    @Test
    public void testSearchSongs_EmptyQuery_ReturnsEmptyList() {
        // Act
        List<Song> result = songService.searchSongs("");
        
        // Assert
        assertNotNull(result, "Search results should not be null");
        assertTrue(result.isEmpty(), "Search results should be empty");
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
        assertNotNull(result, "Results should not be null");
        assertEquals(1, result.size(), "Should return 1 song");
        try {
            verify(mockSongDAO, times(1)).findByArtist(artistName);
        } catch (SQLException e) {
            fail("Verify should not throw SQLException");
        }
    }
    
    @Test
    public void testGetSongsByArtist_EmptyArtist_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            songService.getSongsByArtist("");
        });
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
        assertNotNull(result, "Results should not be null");
        assertEquals(1, result.size(), "Should return 1 song");
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
