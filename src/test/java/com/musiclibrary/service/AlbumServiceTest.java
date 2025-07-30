package com.musiclibrary.service;

import com.musiclibrary.model.Album;
import com.musiclibrary.model.Artist;
import com.musiclibrary.repository.AlbumRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AlbumService using modern Java 17 testing patterns.
 * 
 * Testing Approach:
 * - Validates album business logic and artist relationships
 * - Tests release date validation and genre normalization
 * - Verifies error handling for invalid album data
 * - Tests filtering by artist, genre, and year
 * - Uses JUnit 5 with modern assertions and Mockito extensions
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
class AlbumServiceTest {
    
    @Mock
    private AlbumRepository mockAlbumRepository;
    
    private AlbumService albumService;
    
    @BeforeEach
    void setUp() {
        albumService = new AlbumService(mockAlbumRepository);
    }
    
    @Test
    void testCreateAlbum_ValidAlbum_Success() {
        // Arrange
        Album inputAlbum = createValidAlbum();
        Album expectedAlbum = createValidAlbum();
        expectedAlbum.setAlbumId(1L);
        
        when(mockAlbumRepository.save(any(Album.class))).thenReturn(expectedAlbum);
        
        // Act
        Album result = albumService.createAlbum(inputAlbum);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAlbumId()).isEqualTo(1L);
        assertThat(result.getAlbumName()).isEqualTo("Test Album");
        verify(mockAlbumRepository, times(1)).save(any(Album.class));
    }
    
    @Test
    void testCreateAlbum_NullAlbum_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> albumService.createAlbum(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Album cannot be null");
    }
    
    @Test
    void testCreateAlbum_EmptyAlbumName_ThrowsException() {
        // Arrange
        Album album = createValidAlbum();
        album.setAlbumName("");
        
        // Act & Assert
        assertThatThrownBy(() -> albumService.createAlbum(album))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Album data is incomplete or invalid");
    }
    
    @Test
    void testCreateAlbum_NullAlbumName_ThrowsException() {
        // Arrange
        Album album = createValidAlbum();
        album.setAlbumName(null);
        
        // Act & Assert
        assertThatThrownBy(() -> albumService.createAlbum(album))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Album data is incomplete or invalid");
    }
    
    @Test
    void testCreateAlbum_NullArtistId_ThrowsException() {
        // Arrange
        Album album = createValidAlbum();
        album.setArtist(null);
        
        // Act & Assert
        assertThatThrownBy(() -> albumService.createAlbum(album))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Album data is incomplete or invalid");
    }
    
    @Test
    void testCreateAlbum_InvalidArtistId_ThrowsException() {
        // Arrange
        Album album = createValidAlbum();
        Artist invalidArtist = new Artist();
        invalidArtist.setArtistId(0L);
        album.setArtist(invalidArtist);
        
        // Act & Assert
        assertThatThrownBy(() -> albumService.createAlbum(album))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Valid Artist ID is required");
    }
    
    @Test
    void testCreateAlbum_DatabaseError_ThrowsRuntimeException() {
        // Arrange
        Album album = createValidAlbum();
        when(mockAlbumRepository.save(any(Album.class))).thenThrow(new RuntimeException("DB error"));
        
        // Act & Assert
        assertThatThrownBy(() -> albumService.createAlbum(album))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to create album");
    }
    
    @Test
    void testGetAlbumById_ValidId_Success() {
        // Arrange
        Long albumId = 1L;
        Album expectedAlbum = createValidAlbum();
        expectedAlbum.setAlbumId(albumId);
        
        when(mockAlbumRepository.findById(albumId)).thenReturn(Optional.of(expectedAlbum));
        
        // Act
        Album result = albumService.getAlbumById(albumId);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAlbumId()).isEqualTo(albumId);
        verify(mockAlbumRepository, times(1)).findById(albumId);
    }
    
    @Test
    void testGetAlbumById_NullId_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> albumService.getAlbumById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid album ID provided");
    }
    
    @Test
    void testGetAlbumById_InvalidId_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> albumService.getAlbumById(0L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid album ID provided");
    }
    
    @Test
    void testGetAllAlbums_Success() {
        // Arrange
        List<Album> expectedAlbums = new ArrayList<Album>();
        expectedAlbums.add(createValidAlbum());
        expectedAlbums.add(createValidAlbum());
        
        when(mockAlbumRepository.findAll()).thenReturn(expectedAlbums);
        
        // Act
        List<Album> result = albumService.getAllAlbums();
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        verify(mockAlbumRepository, times(1)).findAll();
    }
    
    @Test
    void testUpdateAlbum_ValidAlbum_Success() {
        // Arrange
        Album album = createValidAlbum();
        album.setAlbumId(1L);
        
        when(mockAlbumRepository.save(any(Album.class))).thenReturn(album);
        
        // Act
        Album result = albumService.updateAlbum(album);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAlbumId()).isEqualTo(1L);
        verify(mockAlbumRepository, times(1)).save(any(Album.class));
    }
    
    @Test
    void testUpdateAlbum_NullId_ThrowsException() {
        // Arrange
        Album album = createValidAlbum();
        album.setAlbumId(null);
        
        // Act & Assert
        assertThatThrownBy(() -> albumService.updateAlbum(album))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Album ID is required for updates");
    }
    
    @Test
    void testDeleteAlbum_ValidId_Success() {
        // Arrange
        Long albumId = 1L;
        when(mockAlbumRepository.existsById(albumId)).thenReturn(true);
        
        // Act
        boolean result = albumService.deleteAlbum(albumId);
        
        // Assert
        assertThat(result).isTrue();
        verify(mockAlbumRepository, times(1)).existsById(albumId);
        verify(mockAlbumRepository, times(1)).deleteById(albumId);
    }
    
    @Test
    void testSearchAlbums_ValidQuery_Success() {
        // Arrange
        String query = "test";
        List<Album> expectedAlbums = new ArrayList<Album>();
        expectedAlbums.add(createValidAlbum());
        
        when(mockAlbumRepository.searchByName(query)).thenReturn(expectedAlbums);
        
        // Act
        List<Album> result = albumService.searchAlbums(query);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(mockAlbumRepository, times(1)).searchByName(query);
    }
    
    @Test
    void testSearchAlbums_EmptyQuery_ReturnsEmptyList() {
        // Act
        List<Album> result = albumService.searchAlbums("");
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(mockAlbumRepository, never()).searchByName(anyString());
    }
    
    @Test
    void testGetAlbumsByArtist_ValidArtistId_Success() {
        // Arrange
        Long artistId = 1L;
        List<Album> expectedAlbums = new ArrayList<Album>();
        expectedAlbums.add(createValidAlbum());
        
        when(mockAlbumRepository.findByArtistArtistId(artistId)).thenReturn(expectedAlbums);
        
        // Act
        List<Album> result = albumService.getAlbumsByArtist(artistId);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(mockAlbumRepository, times(1)).findByArtistArtistId(artistId);
    }
    
    @Test
    void testGetAlbumsByArtist_NullArtistId_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> albumService.getAlbumsByArtist((Long) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid artist ID provided");
    }
    
    @Test
    void testGetAlbumsByArtist_InvalidArtistId_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> albumService.getAlbumsByArtist(0L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid artist ID provided");
    }
    
    @Test
    void testGetAlbumsByArtistName_ValidArtistName_Success() {
        // Arrange
        String artistName = "Test Artist";
        List<Album> expectedAlbums = new ArrayList<Album>();
        expectedAlbums.add(createValidAlbum());
        
        when(mockAlbumRepository.findByArtistArtistNameIgnoreCase(artistName)).thenReturn(expectedAlbums);
        
        // Act
        List<Album> result = albumService.getAlbumsByArtistName(artistName);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(mockAlbumRepository, times(1)).findByArtistArtistNameIgnoreCase(artistName);
    }
    
    @Test
    void testGetAlbumsByArtistName_EmptyArtistName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> albumService.getAlbumsByArtistName(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid artist name provided");
    }
    
    @Test
    void testGetAlbumsByArtistName_NullArtistName_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> albumService.getAlbumsByArtistName(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid artist name provided");
    }
    
    @Test
    void testGetAlbumsByGenre_ValidGenre_Success() {
        // Arrange
        String genre = "Rock";
        List<Album> expectedAlbums = new ArrayList<Album>();
        expectedAlbums.add(createValidAlbum());
        
        when(mockAlbumRepository.findByGenreIgnoreCase(genre)).thenReturn(expectedAlbums);
        
        // Act
        List<Album> result = albumService.getAlbumsByGenre(genre);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(mockAlbumRepository, times(1)).findByGenreIgnoreCase(genre);
    }
    
    @Test
    void testGetAlbumsByGenre_EmptyGenre_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> albumService.getAlbumsByGenre(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid genre name provided");
    }
    
    @Test
    void testGetAlbumsByGenre_NullGenre_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> albumService.getAlbumsByGenre(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid genre name provided");
    }
    
    @Test
    void testGetAlbumsByYear_ValidYear_Success() {
        // Arrange
        int year = 2020;
        List<Album> expectedAlbums = new ArrayList<Album>();
        expectedAlbums.add(createValidAlbum());
        
        when(mockAlbumRepository.findByReleaseYear(year)).thenReturn(expectedAlbums);
        
        // Act
        List<Album> result = albumService.getAlbumsByYear(year);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(mockAlbumRepository, times(1)).findByReleaseYear(year);
    }
    
    @Test
    void testGetAlbumsByYear_TooOldYear_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> albumService.getAlbumsByYear(1800))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid release year: 1800");
    }
    
    @Test
    void testGetAlbumsByYear_FutureYear_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> albumService.getAlbumsByYear(2050))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid release year: 2050");
    }
    
    @Test
    void testGetAlbumsWithPagination_ValidParameters_Success() {
        // Arrange
        int page = 0;
        int size = 10;
        List<Album> expectedAlbums = new ArrayList<Album>();
        expectedAlbums.add(createValidAlbum());
        Page<Album> albumPage = new PageImpl<>(expectedAlbums);
        Pageable pageable = PageRequest.of(page, size);
        
        when(mockAlbumRepository.findAll(pageable)).thenReturn(albumPage);
        
        // Act
        List<Album> result = albumService.getAlbumsWithPagination(page, size);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(mockAlbumRepository, times(1)).findAll(any(Pageable.class));
    }
    
    @Test
    void testGetAlbumsWithPagination_NegativePage_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> albumService.getAlbumsWithPagination(-1, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Page number must be non-negative");
    }
    
    @Test
    void testGetAlbumsWithPagination_InvalidSize_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> albumService.getAlbumsWithPagination(0, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("size");
    }
    
    @Test
    void testGetTotalAlbumCount_Success() {
        // Arrange
        when(mockAlbumRepository.count()).thenReturn(3L);
        
        // Act
        long result = albumService.getTotalAlbumCount();
        
        // Assert
        assertThat(result).isEqualTo(3L);
        verify(mockAlbumRepository, times(1)).count();
    }
    
    @Test
    void testAlbumExists_ExistingAlbum_ReturnsTrue() {
        // Arrange
        Long albumId = 1L;
        when(mockAlbumRepository.existsById(albumId)).thenReturn(true);
        
        // Act
        boolean result = albumService.albumExists(albumId);
        
        // Assert
        assertThat(result).isTrue();
        verify(mockAlbumRepository, times(1)).existsById(albumId);
    }
    
    @Test
    void testAlbumExists_NonExistingAlbum_ReturnsFalse() {
        // Arrange
        Long albumId = 999L;
        when(mockAlbumRepository.existsById(albumId)).thenReturn(false);
        
        // Act
        boolean result = albumService.albumExists(albumId);
        
        // Assert
        assertThat(result).isFalse();
        verify(mockAlbumRepository, times(1)).existsById(albumId);
    }
    
    /**
     * Helper method to create a valid album for testing.
     * Demonstrates centralized test data creation with relationships.
     */
    private Album createValidAlbum() {
        Album album = new Album();
        album.setAlbumName("Test Album");
        
        Artist artist = new Artist();
        artist.setArtistId(1L);
        artist.setArtistName("Test Artist");
        album.setArtist(artist);
        album.setArtistName("Test Artist");
        
        album.setReleaseDate(LocalDate.now());
        album.setGenre("Rock");
        album.setRecordLabel("Test Records");
        album.setTotalTracks(10);
        album.setCreatedDate(LocalDateTime.now());
        return album;
    }
}
