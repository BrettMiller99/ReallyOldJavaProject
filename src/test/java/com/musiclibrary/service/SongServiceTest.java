package com.musiclibrary.service;

import com.musiclibrary.model.Song;
import com.musiclibrary.model.Artist;
import com.musiclibrary.model.Album;
import com.musiclibrary.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SongService using modern Java 17 testing patterns.
 * 
 * Testing Approach:
 * - Uses JUnit 5 with @ExtendWith for automatic mock injection
 * - Uses modern Mockito with improved syntax
 * - Tests business logic validation and error handling
 * - Verifies service layer behavior independent of database
 * - Uses AssertJ fluent assertions for better readability
 * 
 * Modern Features:
 * - JUnit 5 with @ExtendWith for automatic mock injection
 * - AssertJ fluent assertions for better readability
 * - Modern exception testing with assertThatThrownBy
 * - Automatic mock initialization via MockitoExtension
 * 
 * @author Music Library Development Team
 * @version 2.0
 * @since Java 17
 */
@ExtendWith(MockitoExtension.class)
class SongServiceTest {
    
    @Mock
    private SongRepository mockSongRepository;
    
    private SongService songService;
    
    @BeforeEach
    void setUp() {
        songService = new SongService(mockSongRepository);
    }
    
    @Test
    void testCreateSong_ValidSong_Success() {
        // Arrange
        Song inputSong = createValidSong();
        Song expectedSong = createValidSong();
        expectedSong.setSongId(1L);
        
        when(mockSongRepository.save(any(Song.class))).thenReturn(expectedSong);
        
        // Act
        Song result = songService.createSong(inputSong);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSongId()).isEqualTo(1L);
        assertThat(result.getSongName()).isEqualTo("Test Song");
        verify(mockSongRepository, times(1)).save(any(Song.class));
    }
    
    @Test
    void testCreateSong_NullSong_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> songService.createSong(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Song cannot be null");
    }
    
    @Test
    void testCreateSong_EmptySongName_ThrowsException() {
        // Arrange
        Song song = createValidSong();
        song.setSongName("");
        
        // Act & Assert
        assertThatThrownBy(() -> songService.createSong(song))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Song data is incomplete or invalid");
    }
    
    @Test
    void testCreateSong_NullSongName_ThrowsException() {
        // Arrange
        Song song = createValidSong();
        song.setSongName(null);
        
        // Act & Assert
        assertThatThrownBy(() -> songService.createSong(song))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Song data is incomplete or invalid");
    }
    
    @Test
    void testCreateSong_DatabaseError_ThrowsRuntimeException() {
        // Arrange
        Song song = createValidSong();
        when(mockSongRepository.save(any(Song.class))).thenThrow(new RuntimeException("DB error"));
        
        // Act & Assert
        assertThatThrownBy(() -> songService.createSong(song))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to create song");
    }
    
    @Test
    void testGetSongById_ValidId_Success() {
        // Arrange
        Long songId = 1L;
        Song expectedSong = createValidSong();
        expectedSong.setSongId(songId);
        
        when(mockSongRepository.findById(songId)).thenReturn(Optional.of(expectedSong));
        
        // Act
        Song result = songService.getSongById(songId);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSongId()).isEqualTo(songId);
        verify(mockSongRepository, times(1)).findById(songId);
    }
    
    @Test
    void testGetSongById_NullId_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> songService.getSongById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid song ID provided");
    }
    
    @Test
    void testGetSongById_InvalidId_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> songService.getSongById(0L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid song ID provided");
    }
    
    @Test
    void testGetAllSongs_Success() {
        // Arrange
        List<Song> expectedSongs = new ArrayList<Song>();
        expectedSongs.add(createValidSong());
        expectedSongs.add(createValidSong());
        
        when(mockSongRepository.findAll()).thenReturn(expectedSongs);
        
        // Act
        List<Song> result = songService.getAllSongs();
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        verify(mockSongRepository, times(1)).findAll();
    }
    
    @Test
    void testUpdateSong_ValidSong_Success() {
        // Arrange
        Song song = createValidSong();
        song.setSongId(1L);
        
        when(mockSongRepository.save(any(Song.class))).thenReturn(song);
        
        // Act
        Song result = songService.updateSong(song);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSongId()).isEqualTo(1L);
        verify(mockSongRepository, times(1)).save(any(Song.class));
    }
    
    @Test
    void testUpdateSong_NullId_ThrowsException() {
        // Arrange
        Song song = createValidSong();
        song.setSongId(null);
        
        // Act & Assert
        assertThatThrownBy(() -> songService.updateSong(song))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Song ID is required for updates");
    }
    
    @Test
    void testDeleteSong_ValidId_Success() {
        // Arrange
        Long songId = 1L;
        when(mockSongRepository.existsById(songId)).thenReturn(true);
        
        // Act
        boolean result = songService.deleteSong(songId);
        
        // Assert
        assertThat(result).isTrue();
        verify(mockSongRepository, times(1)).existsById(songId);
        verify(mockSongRepository, times(1)).deleteById(songId);
    }
    
    @Test
    void testSearchSongs_ValidQuery_Success() {
        // Arrange
        String query = "test";
        List<Song> expectedSongs = new ArrayList<Song>();
        expectedSongs.add(createValidSong());
        
        when(mockSongRepository.searchByName(query)).thenReturn(expectedSongs);
        
        // Act
        List<Song> result = songService.searchSongs(query);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(mockSongRepository, times(1)).searchByName(query);
    }
    
    @Test
    void testSearchSongs_EmptyQuery_ReturnsEmptyList() {
        // Act
        List<Song> result = songService.searchSongs("");
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(mockSongRepository, never()).searchByName(anyString());
    }
    
    @Test
    void testGetSongsByArtist_ValidArtist_Success() {
        // Arrange
        String artistName = "Test Artist";
        List<Song> expectedSongs = new ArrayList<Song>();
        expectedSongs.add(createValidSong());
        
        when(mockSongRepository.findByArtistArtistNameIgnoreCase(artistName)).thenReturn(expectedSongs);
        
        // Act
        List<Song> result = songService.getSongsByArtist(artistName);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(mockSongRepository, times(1)).findByArtistArtistNameIgnoreCase(artistName);
    }
    
    @Test
    void testGetSongsByArtist_EmptyArtist_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> songService.getSongsByArtist(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid artist name provided");
    }
    
    @Test
    void testGetSongsByAlbum_ValidAlbum_Success() {
        // Arrange
        String albumName = "Test Album";
        List<Song> expectedSongs = new ArrayList<Song>();
        expectedSongs.add(createValidSong());
        
        when(mockSongRepository.findByAlbumAlbumNameIgnoreCase(albumName)).thenReturn(expectedSongs);
        
        // Act
        List<Song> result = songService.getSongsByAlbum(albumName);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(mockSongRepository, times(1)).findByAlbumAlbumNameIgnoreCase(albumName);
    }
    
    @Test
    void testGetSongsByAlbum_EmptyAlbum_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> songService.getSongsByAlbum(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid album name provided");
    }
    
    /**
     * Helper method to create a valid song for testing.
     * Centralizes test data creation for consistency.
     */
    private Song createValidSong() {
        Song song = new Song();
        song.setSongName("Test Song");
        
        Artist artist = new Artist();
        artist.setArtistId(1L);
        artist.setArtistName("Test Artist");
        song.setArtist(artist);
        song.setArtistName("Test Artist");
        
        Album album = new Album();
        album.setAlbumId(1L);
        album.setAlbumName("Test Album");
        album.setArtist(artist);
        song.setAlbum(album);
        
        song.setTrackNumber(1);
        song.setTrackLength(180); // 3 minutes
        song.setDateReleased(LocalDate.now());
        song.setGenre("Rock");
        song.setRating(4); // Integer rating (0-5 stars)
        return song;
    }
}
