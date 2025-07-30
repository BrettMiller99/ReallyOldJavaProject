package com.musiclibrary.service;

import com.musiclibrary.model.Artist;
import com.musiclibrary.repository.ArtistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ArtistService using modern Java 17 testing patterns.
 * 
 * Testing Approach:
 * - Validates artist business logic and data integrity rules
 * - Tests formation year validation and country name normalization
 * - Verifies error handling for invalid artist data
 * - Tests pagination and search functionality
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
class ArtistServiceTest {
    
    @Mock
    private ArtistRepository mockArtistRepository;
    
    private ArtistService artistService;
    
    @BeforeEach
    void setUp() {
        artistService = new ArtistService(mockArtistRepository);
    }
    
    @Test
    void testCreateArtist_ValidArtist_Success() {
        // Arrange
        Artist inputArtist = createValidArtist();
        Artist expectedArtist = createValidArtist();
        expectedArtist.setArtistId(1L);
        
        when(mockArtistRepository.save(any(Artist.class))).thenReturn(expectedArtist);
        
        // Act
        Artist result = artistService.createArtist(inputArtist);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getArtistId()).isEqualTo(1L);
        assertThat(result.getArtistName()).isEqualTo("Test Artist");
        verify(mockArtistRepository, times(1)).save(any(Artist.class));
    }
    
    @Test
    void testCreateArtist_NullArtist_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> artistService.createArtist(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Artist cannot be null");
    }
    
    @Test
    void testCreateArtist_EmptyArtistName_ThrowsException() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setArtistName("");
        
        // Act & Assert
        assertThatThrownBy(() -> artistService.createArtist(artist))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Artist data is incomplete or invalid");
    }
    
    @Test
    void testCreateArtist_NullArtistName_ThrowsException() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setArtistName(null);
        
        // Act & Assert
        assertThatThrownBy(() -> artistService.createArtist(artist))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Artist data is incomplete or invalid");
    }
    
    @Test
    void testCreateArtist_FutureFormationYear_ThrowsException() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setFormedYear(2030); // Future year
        
        // Act & Assert
        assertThatThrownBy(() -> artistService.createArtist(artist))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Formation year must be between 1900 and");
    }
    
    @Test
    void testCreateArtist_TooOldFormationYear_ThrowsException() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setFormedYear(1800); // Too old
        
        // Act & Assert
        assertThatThrownBy(() -> artistService.createArtist(artist))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Formation year must be between 1900 and");
    }
    
    @Test
    void testCreateArtist_DatabaseError_ThrowsRuntimeException() {
        // Arrange
        Artist artist = createValidArtist();
        when(mockArtistRepository.save(any(Artist.class))).thenThrow(new RuntimeException("DB error"));
        
        // Act & Assert
        assertThatThrownBy(() -> artistService.createArtist(artist))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to create artist");
    }
    
    @Test
    void testGetArtistById_ValidId_Success() {
        // Arrange
        Long artistId = 1L;
        Artist expectedArtist = createValidArtist();
        expectedArtist.setArtistId(artistId);
        
        when(mockArtistRepository.findById(artistId)).thenReturn(Optional.of(expectedArtist));
        
        // Act
        Artist result = artistService.getArtistById(artistId);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getArtistId()).isEqualTo(artistId);
        verify(mockArtistRepository, times(1)).findById(artistId);
    }
    
    @Test
    void testGetArtistById_NullId_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> artistService.getArtistById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid artist ID provided");
    }
    
    @Test
    void testGetArtistById_InvalidId_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> artistService.getArtistById(0L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid artist ID provided");
    }
    
    @Test
    void testGetAllArtists_Success() {
        // Arrange
        List<Artist> expectedArtists = new ArrayList<Artist>();
        expectedArtists.add(createValidArtist());
        expectedArtists.add(createValidArtist());
        
        when(mockArtistRepository.findAll()).thenReturn(expectedArtists);
        
        // Act
        List<Artist> result = artistService.getAllArtists();
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        verify(mockArtistRepository, times(1)).findAll();
    }
    
    @Test
    void testUpdateArtist_ValidArtist_Success() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setArtistId(1L);
        
        when(mockArtistRepository.save(any(Artist.class))).thenReturn(artist);
        
        // Act
        Artist result = artistService.updateArtist(artist);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getArtistId()).isEqualTo(1L);
        verify(mockArtistRepository, times(1)).save(any(Artist.class));
    }
    
    @Test
    void testUpdateArtist_NullId_ThrowsException() {
        // Arrange
        Artist artist = createValidArtist();
        artist.setArtistId(null);
        
        // Act & Assert
        assertThatThrownBy(() -> artistService.updateArtist(artist))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Artist ID is required for updates");
    }
    
    @Test
    void testDeleteArtist_ValidId_Success() {
        // Arrange
        Long artistId = 1L;
        when(mockArtistRepository.existsById(artistId)).thenReturn(true);
        
        // Act
        boolean result = artistService.deleteArtist(artistId);
        
        // Assert
        assertThat(result).isTrue();
        verify(mockArtistRepository, times(1)).existsById(artistId);
        verify(mockArtistRepository, times(1)).deleteById(artistId);
    }
    
    @Test
    void testSearchArtists_ValidQuery_Success() {
        // Arrange
        String query = "test";
        List<Artist> expectedArtists = new ArrayList<Artist>();
        expectedArtists.add(createValidArtist());
        
        when(mockArtistRepository.searchByName(query)).thenReturn(expectedArtists);
        
        // Act
        List<Artist> result = artistService.searchArtists(query);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(mockArtistRepository, times(1)).searchByName(query);
    }
    
    @Test
    void testSearchArtists_EmptyQuery_ReturnsEmptyList() {
        // Act
        List<Artist> result = artistService.searchArtists("");
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(mockArtistRepository, never()).searchByName(anyString());
    }
    
    @Test
    void testGetArtistsWithPagination_ValidParameters_Success() {
        // Arrange
        int page = 0;
        int size = 10;
        List<Artist> expectedArtists = new ArrayList<Artist>();
        expectedArtists.add(createValidArtist());
        Page<Artist> artistPage = new PageImpl<>(expectedArtists);
        Pageable pageable = PageRequest.of(page, size);
        
        when(mockArtistRepository.findAll(pageable)).thenReturn(artistPage);
        
        // Act
        List<Artist> result = artistService.getArtistsWithPagination(page, size);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(mockArtistRepository, times(1)).findAll(any(Pageable.class));
    }
    
    @Test
    void testGetArtistsWithPagination_NegativePage_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> artistService.getArtistsWithPagination(-1, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Page number must be non-negative");
    }
    
    @Test
    void testGetArtistsWithPagination_InvalidSize_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> artistService.getArtistsWithPagination(0, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("size");
    }
    
    @Test
    void testGetTotalArtistCount_Success() {
        // Arrange
        when(mockArtistRepository.count()).thenReturn(5L);
        
        // Act
        long result = artistService.getTotalArtistCount();
        
        // Assert
        assertThat(result).isEqualTo(5L);
        verify(mockArtistRepository, times(1)).count();
    }
    
    @Test
    void testArtistExists_ExistingArtist_ReturnsTrue() {
        // Arrange
        Long artistId = 1L;
        when(mockArtistRepository.existsById(artistId)).thenReturn(true);
        
        // Act
        boolean result = artistService.artistExists(artistId);
        
        // Assert
        assertThat(result).isTrue();
        verify(mockArtistRepository, times(1)).existsById(artistId);
    }
    
    @Test
    void testArtistExists_NonExistingArtist_ReturnsFalse() {
        // Arrange
        Long artistId = 999L;
        when(mockArtistRepository.existsById(artistId)).thenReturn(false);
        
        // Act
        boolean result = artistService.artistExists(artistId);
        
        // Assert
        assertThat(result).isFalse();
        verify(mockArtistRepository, times(1)).existsById(artistId);
    }
    
    @Test
    void testGetArtistsByCountry_ValidCountry_Success() {
        // Arrange
        String country = "USA";
        List<Artist> expectedArtists = new ArrayList<Artist>();
        expectedArtists.add(createValidArtist());
        
        when(mockArtistRepository.findByCountryIgnoreCase(country)).thenReturn(expectedArtists);
        
        // Act
        List<Artist> result = artistService.getArtistsByCountry(country);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(mockArtistRepository, times(1)).findByCountryIgnoreCase(country);
    }
    
    @Test
    void testGetArtistsByCountry_EmptyCountry_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> artistService.getArtistsByCountry(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid country name provided");
    }
    
    @Test
    void testGetArtistsByCountry_NullCountry_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> artistService.getArtistsByCountry(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid country name provided");
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
        artist.setCreatedDate(LocalDateTime.now());
        return artist;
    }
}
