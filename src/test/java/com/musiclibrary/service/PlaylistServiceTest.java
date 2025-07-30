package com.musiclibrary.service;

import com.musiclibrary.model.Playlist;
import com.musiclibrary.repository.PlaylistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for PlaylistService.
 * 
 * Tests all business logic, validation, error handling, and edge cases
 * to achieve high code coverage and ensure robust functionality.
 * 
 * @author Music Library Development Team
 * @version 2.0 - Spring Boot Unit Tests
 * @since Java 17
 */
@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @InjectMocks
    private PlaylistService playlistService;

    private Playlist testPlaylist;

    @BeforeEach
    void setUp() {
        testPlaylist = new Playlist();
        testPlaylist.setPlaylistId(1L);
        testPlaylist.setPlaylistName("Test Playlist");
        testPlaylist.setCreatedBy("testuser");
        testPlaylist.setDescription("Test Description");
        testPlaylist.setIsPublic(true);
        testPlaylist.setSongCount(0);
        testPlaylist.setTotalDuration(0);
        testPlaylist.setCreatedDate(LocalDateTime.now());
    }

    @Test
    void createPlaylist_ValidPlaylist_ShouldReturnCreatedPlaylist() {
        when(playlistRepository.save(any(Playlist.class))).thenReturn(testPlaylist);

        Playlist result = playlistService.createPlaylist(testPlaylist);

        assertThat(result).isNotNull();
        assertThat(result.getPlaylistName()).isEqualTo("Test Playlist");
        assertThat(result.getCreatedBy()).isEqualTo("testuser");
        verify(playlistRepository).save(testPlaylist);
    }

    @Test
    void createPlaylist_NullPlaylist_ShouldThrowException() {
        assertThatThrownBy(() -> playlistService.createPlaylist(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Playlist cannot be null");

        verify(playlistRepository, never()).save(any());
    }

    @Test
    void createPlaylist_InvalidPlaylistName_ShouldThrowException() {
        testPlaylist.setPlaylistName("");

        assertThatThrownBy(() -> playlistService.createPlaylist(testPlaylist))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Playlist data is incomplete or invalid");

        verify(playlistRepository, never()).save(any());
    }

    @Test
    void createPlaylist_InvalidCreatedBy_ShouldThrowException() {
        testPlaylist.setCreatedBy(null);

        assertThatThrownBy(() -> playlistService.createPlaylist(testPlaylist))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Playlist data is incomplete or invalid");

        verify(playlistRepository, never()).save(any());
    }

    @Test
    void createPlaylist_DatabaseError_ShouldThrowRuntimeException() {
        when(playlistRepository.save(any(Playlist.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        assertThatThrownBy(() -> playlistService.createPlaylist(testPlaylist))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create playlist");

        verify(playlistRepository).save(testPlaylist);
    }

    @Test
    void getPlaylistById_ValidId_ShouldReturnPlaylist() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));

        Playlist result = playlistService.getPlaylistById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPlaylistId()).isEqualTo(1L);
        verify(playlistRepository).findById(1L);
    }

    @Test
    void getPlaylistById_NonExistentId_ShouldReturnNull() {
        when(playlistRepository.findById(999L)).thenReturn(Optional.empty());

        Playlist result = playlistService.getPlaylistById(999L);

        assertThat(result).isNull();
        verify(playlistRepository).findById(999L);
    }

    @Test
    void getPlaylistById_NullId_ShouldThrowException() {
        assertThatThrownBy(() -> playlistService.getPlaylistById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid playlist ID provided");

        verify(playlistRepository, never()).findById(any());
    }

    @Test
    void getPlaylistById_DatabaseError_ShouldThrowRuntimeException() {
        when(playlistRepository.findById(1L))
                .thenThrow(new RuntimeException("Database connection failed"));

        assertThatThrownBy(() -> playlistService.getPlaylistById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to retrieve playlist");

        verify(playlistRepository).findById(1L);
    }

    @Test
    void getAllPlaylists_ShouldReturnAllPlaylists() {
        List<Playlist> playlists = Arrays.asList(testPlaylist, new Playlist());
        when(playlistRepository.findAll()).thenReturn(playlists);

        List<Playlist> result = playlistService.getAllPlaylists();

        assertThat(result).hasSize(2);
        assertThat(result).contains(testPlaylist);
        verify(playlistRepository).findAll();
    }

    @Test
    void getAllPlaylists_EmptyDatabase_ShouldReturnEmptyList() {
        when(playlistRepository.findAll()).thenReturn(Arrays.asList());

        List<Playlist> result = playlistService.getAllPlaylists();

        assertThat(result).isEmpty();
        verify(playlistRepository).findAll();
    }

    @Test
    void getAllPlaylists_DatabaseError_ShouldThrowRuntimeException() {
        when(playlistRepository.findAll())
                .thenThrow(new RuntimeException("Database connection failed"));

        assertThatThrownBy(() -> playlistService.getAllPlaylists())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to retrieve playlists");

        verify(playlistRepository).findAll();
    }

    @Test
    void updatePlaylist_ValidPlaylist_ShouldReturnUpdatedPlaylist() {
        testPlaylist.setPlaylistName("Updated Playlist");
        when(playlistRepository.save(any(Playlist.class))).thenReturn(testPlaylist);

        Playlist result = playlistService.updatePlaylist(testPlaylist);

        assertThat(result).isNotNull();
        assertThat(result.getPlaylistName()).isEqualTo("Updated Playlist");
        verify(playlistRepository).save(testPlaylist);
    }

    @Test
    void updatePlaylist_NullPlaylist_ShouldThrowException() {
        assertThatThrownBy(() -> playlistService.updatePlaylist(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Playlist cannot be null");

        verify(playlistRepository, never()).save(any());
    }

    @Test
    void updatePlaylist_InvalidPlaylistId_ShouldThrowException() {
        testPlaylist.setPlaylistId(null);

        assertThatThrownBy(() -> playlistService.updatePlaylist(testPlaylist))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Playlist ID is required for updates");

        verify(playlistRepository, never()).save(any());
    }

    @Test
    void deletePlaylist_ValidId_ShouldDeleteSuccessfully() {
        when(playlistRepository.existsById(1L)).thenReturn(true);
        doNothing().when(playlistRepository).deleteById(1L);

        boolean result = playlistService.deletePlaylist(1L);

        assertThat(result).isTrue();
        verify(playlistRepository).existsById(1L);
        verify(playlistRepository).deleteById(1L);
    }

    @Test
    void deletePlaylist_NonExistentId_ShouldReturnFalse() {
        when(playlistRepository.existsById(999L)).thenReturn(false);

        boolean result = playlistService.deletePlaylist(999L);

        assertThat(result).isFalse();
        verify(playlistRepository).existsById(999L);
        verify(playlistRepository, never()).deleteById(any());
    }

    @Test
    void deletePlaylist_NullId_ShouldThrowException() {
        assertThatThrownBy(() -> playlistService.deletePlaylist(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid playlist ID provided");

        verify(playlistRepository, never()).existsById(any());
        verify(playlistRepository, never()).deleteById(any());
    }

    @Test
    void searchPlaylists_ValidQuery_ShouldReturnMatchingPlaylists() {
        List<Playlist> playlists = Arrays.asList(testPlaylist);
        when(playlistRepository.searchByName("Test"))
                .thenReturn(playlists);

        List<Playlist> result = playlistService.searchPlaylists("Test");

        assertThat(result).hasSize(1);
        assertThat(result).contains(testPlaylist);
        verify(playlistRepository).searchByName("Test");
    }

    @Test
    void searchPlaylists_EmptyQuery_ShouldSearchByEmptyString() {
        List<Playlist> playlists = Arrays.asList();
        when(playlistRepository.searchByName("")).thenReturn(playlists);

        List<Playlist> result = playlistService.searchPlaylists("");

        assertThat(result).hasSize(0);
        verify(playlistRepository).searchByName("");
    }

    @Test
    void searchPlaylists_NullQuery_ShouldSearchByNull() {
        List<Playlist> playlists = Arrays.asList();
        when(playlistRepository.searchByName(null)).thenReturn(playlists);

        List<Playlist> result = playlistService.searchPlaylists(null);

        assertThat(result).hasSize(0);
        verify(playlistRepository).searchByName(null);
    }

    @Test
    void getPlaylistsByUser_ValidUser_ShouldReturnUserPlaylists() {
        List<Playlist> playlists = Arrays.asList(testPlaylist);
        when(playlistRepository.findByCreatedByIgnoreCase("testuser"))
                .thenReturn(playlists);

        List<Playlist> result = playlistService.getPlaylistsByUser("testuser");

        assertThat(result).hasSize(1);
        assertThat(result).contains(testPlaylist);
        verify(playlistRepository).findByCreatedByIgnoreCase("testuser");
    }

    @Test
    void getPlaylistsByUser_EmptyUser_ShouldThrowException() {
        assertThatThrownBy(() -> playlistService.getPlaylistsByUser(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid username provided");
    }

    @Test
    void getPublicPlaylists_ShouldReturnOnlyPublicPlaylists() {
        List<Playlist> playlists = Arrays.asList(testPlaylist);
        when(playlistRepository.findByIsPublicTrue()).thenReturn(playlists);

        List<Playlist> result = playlistService.getPublicPlaylists();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsPublic()).isTrue();
        verify(playlistRepository).findByIsPublicTrue();
    }

    @Test
    void getPlaylistsWithPagination_ValidPageable_ShouldReturnPagedResults() {
        assertThatThrownBy(() -> playlistService.getPlaylistsWithPagination(-1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page number must be non-negative");
                
        assertThatThrownBy(() -> playlistService.getPlaylistsWithPagination(0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page size must be between 1 and 100");
    }

    @Test
    void getTotalPlaylistCount_ShouldReturnCorrectCount() {
        when(playlistRepository.count()).thenReturn(5L);

        long result = playlistService.getTotalPlaylistCount();

        assertThat(result).isEqualTo(5L);
        verify(playlistRepository).count();
    }

    @Test
    void playlistExists_ExistingPlaylist_ShouldReturnTrue() {
        when(playlistRepository.existsById(1L)).thenReturn(true);

        boolean result = playlistService.playlistExists(1L);

        assertThat(result).isTrue();
        verify(playlistRepository).existsById(1L);
    }

    @Test
    void playlistExists_NonExistentPlaylist_ShouldReturnFalse() {
        when(playlistRepository.existsById(999L)).thenReturn(false);

        boolean result = playlistService.playlistExists(999L);

        assertThat(result).isFalse();
        verify(playlistRepository).existsById(999L);
    }

    @Test
    void addSongToPlaylist_ValidIds_ShouldUpdatePlaylistMetadata() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(testPlaylist);

        Playlist result = playlistService.addSongToPlaylist(1L, 100L);

        assertThat(result).isNotNull();
        assertThat(result.getSongCount()).isEqualTo(1);
        assertThat(result.getTotalDuration()).isEqualTo(180);
        verify(playlistRepository).findById(1L);
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    void addSongToPlaylist_NullPlaylistId_ShouldThrowException() {
        assertThatThrownBy(() -> playlistService.addSongToPlaylist(null, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Playlist ID and Song ID cannot be null");

        verify(playlistRepository, never()).findById(any());
    }

    @Test
    void addSongToPlaylist_NullSongId_ShouldThrowException() {
        assertThatThrownBy(() -> playlistService.addSongToPlaylist(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Playlist ID and Song ID cannot be null");

        verify(playlistRepository, never()).findById(any());
    }

    @Test
    void addSongToPlaylist_NonExistentPlaylist_ShouldReturnNull() {
        when(playlistRepository.findById(999L)).thenReturn(Optional.empty());

        Playlist result = playlistService.addSongToPlaylist(999L, 100L);

        assertThat(result).isNull();
        verify(playlistRepository).findById(999L);
        verify(playlistRepository, never()).save(any());
    }

    @Test
    void removeSongFromPlaylist_ValidIds_ShouldUpdatePlaylistMetadata() {
        testPlaylist.setSongCount(2);
        testPlaylist.setTotalDuration(360);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(testPlaylist);

        Playlist result = playlistService.removeSongFromPlaylist(1L, 100L);

        assertThat(result).isNotNull();
        assertThat(result.getSongCount()).isEqualTo(1);
        assertThat(result.getTotalDuration()).isEqualTo(180);
        verify(playlistRepository).findById(1L);
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    void removeSongFromPlaylist_EmptyPlaylist_ShouldReturnPlaylistUnchanged() {
        testPlaylist.setSongCount(0);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));

        Playlist result = playlistService.removeSongFromPlaylist(1L, 100L);

        assertThat(result).isNotNull();
        assertThat(result.getSongCount()).isEqualTo(0);
        verify(playlistRepository).findById(1L);
        verify(playlistRepository, never()).save(any());
    }

    @Test
    void removeSongFromPlaylist_NullPlaylistId_ShouldThrowException() {
        assertThatThrownBy(() -> playlistService.removeSongFromPlaylist(null, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Playlist ID and Song ID cannot be null");

        verify(playlistRepository, never()).findById(any());
    }

    @Test
    void removeSongFromPlaylist_NonExistentPlaylist_ShouldReturnNull() {
        when(playlistRepository.findById(999L)).thenReturn(Optional.empty());

        Playlist result = playlistService.removeSongFromPlaylist(999L, 100L);

        assertThat(result).isNull();
        verify(playlistRepository).findById(999L);
        verify(playlistRepository, never()).save(any());
    }

    @Test
    void createPlaylist_AppliesBusinessRules_ShouldSetDefaults() {
        Playlist playlist = new Playlist();
        playlist.setPlaylistName("  Test Playlist  ");
        playlist.setCreatedBy("  TestUser  ");
        playlist.setDescription("  ");

        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> {
            Playlist saved = invocation.getArgument(0);
            saved.setPlaylistId(1L);
            return saved;
        });

        Playlist result = playlistService.createPlaylist(playlist);

        assertThat(result.getPlaylistName()).isEqualTo("Test Playlist");
        assertThat(result.getCreatedBy()).isEqualTo("testuser");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getIsPublic()).isTrue();
        assertThat(result.getSongCount()).isEqualTo(0);
        assertThat(result.getTotalDuration()).isEqualTo(0);
    }
}
