package com.musiclibrary.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiclibrary.model.Album;
import com.musiclibrary.model.Artist;
import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.Song;
import com.musiclibrary.repository.AlbumRepository;
import com.musiclibrary.repository.ArtistRepository;
import com.musiclibrary.repository.PlaylistRepository;
import com.musiclibrary.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PlaylistController REST endpoints.
 * 
 * Comprehensive testing including CRUD operations, performance metrics,
 * and error handling scenarios.
 * 
 * @author Music Library Development Team
 * @version 2.0 - Spring Boot Integration Tests
 * @since Java 17
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlaylistControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistRepository artistRepository;

    private Artist testArtist;
    private Album testAlbum;
    private Song testSong;
    private Playlist testPlaylist;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        playlistRepository.deleteAll();
        songRepository.deleteAll();
        albumRepository.deleteAll();
        artistRepository.deleteAll();

        testArtist = new Artist();
        testArtist.setArtistName("Test Artist");
        testArtist.setCountry("USA");
        testArtist.setFormedYear(2000);
        testArtist = artistRepository.save(testArtist);

        testAlbum = new Album();
        testAlbum.setAlbumName("Test Album");
        testAlbum.setArtist(testArtist);
        testAlbum.setReleaseDate(LocalDate.of(2023, 1, 1));
        testAlbum.setGenre("Rock");
        testAlbum.setRecordLabel("Test Records");
        testAlbum.setTotalTracks(10);
        testAlbum = albumRepository.save(testAlbum);

        testSong = new Song();
        testSong.setSongName("Test Song");
        testSong.setArtist(testArtist);
        testSong.setAlbum(testAlbum);
        testSong.setDateReleased(LocalDate.of(2023, 1, 1));
        testSong.setTrackLength(210); // 3 minutes 30 seconds = 210 seconds
        testSong.setTrackNumber(1);
        testSong.setRating(4);
        testSong.setPlayCount(0);
        testSong = songRepository.save(testSong);

        testPlaylist = new Playlist();
        testPlaylist.setPlaylistName("Test Playlist");
        testPlaylist.setCreatedBy("testuser");
        testPlaylist.setIsPublic(true);
        testPlaylist.setDescription("A test playlist");
    }

    @Test
    void createPlaylist_ValidPlaylist_ShouldReturnCreated() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(post("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPlaylist)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.playlist.playlistName").value("Test Playlist"))
                .andExpect(jsonPath("$.playlist.createdBy").value("testuser"))
                .andExpect(jsonPath("$.playlist.isPublic").value(true));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(1000);

        List<Playlist> playlists = playlistRepository.findAll();
        assertThat(playlists).hasSize(1);
        assertThat(playlists.get(0).getPlaylistName()).isEqualTo("Test Playlist");
    }

    @Test
    void getAllPlaylists_ShouldReturnPlaylistList() throws Exception {
        Playlist savedPlaylist = playlistRepository.save(testPlaylist);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/playlists"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.playlists").isArray())
                .andExpect(jsonPath("$.playlists[0].playlistName").value("Test Playlist"))
                .andExpect(jsonPath("$.playlists[0].playlistId").value(savedPlaylist.getPlaylistId()));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(500);
    }

    @Test
    void getPlaylistById_ExistingId_ShouldReturnPlaylist() throws Exception {
        Playlist savedPlaylist = playlistRepository.save(testPlaylist);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/playlists/{id}", savedPlaylist.getPlaylistId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.playlist.playlistId").value(savedPlaylist.getPlaylistId()))
                .andExpect(jsonPath("$.playlist.playlistName").value("Test Playlist"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(300);
    }

    @Test
    void getPlaylistById_NonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/playlists/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePlaylist_ValidUpdate_ShouldReturnUpdatedPlaylist() throws Exception {
        Playlist savedPlaylist = playlistRepository.save(testPlaylist);
        savedPlaylist.setPlaylistName("Updated Playlist Name");
        savedPlaylist.setDescription("Updated description");

        long startTime = System.currentTimeMillis();

        mockMvc.perform(put("/api/playlists/{id}", savedPlaylist.getPlaylistId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedPlaylist)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.playlist.playlistName").value("Updated Playlist Name"))
                .andExpect(jsonPath("$.playlist.description").value("Updated description"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(800);

        Playlist updatedPlaylist = playlistRepository.findById(savedPlaylist.getPlaylistId()).orElse(null);
        assertThat(updatedPlaylist).isNotNull();
        assertThat(updatedPlaylist.getPlaylistName()).isEqualTo("Updated Playlist Name");
        assertThat(updatedPlaylist.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void deletePlaylist_ExistingId_ShouldReturnNoContent() throws Exception {
        Playlist savedPlaylist = playlistRepository.save(testPlaylist);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(delete("/api/playlists/{id}", savedPlaylist.getPlaylistId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Playlist deleted successfully"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(500);

        assertThat(playlistRepository.findById(savedPlaylist.getPlaylistId())).isEmpty();
    }

    @Test
    void searchPlaylists_ValidQuery_ShouldReturnMatchingPlaylists() throws Exception {
        playlistRepository.save(testPlaylist);

        Playlist anotherPlaylist = new Playlist();
        anotherPlaylist.setPlaylistName("Another Playlist");
        anotherPlaylist.setCreatedBy("anotheruser");
        anotherPlaylist.setIsPublic(true);
        anotherPlaylist.setDescription("Another test playlist");
        playlistRepository.save(anotherPlaylist);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/playlists")
                .param("search", "Test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.playlists").isArray())
                .andExpect(jsonPath("$.playlists[0].playlistName").value("Test Playlist"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(600);
    }

    @Test
    void getPlaylistsByUser_ValidUserId_ShouldReturnUserPlaylists() throws Exception {
        playlistRepository.save(testPlaylist);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/playlists")
                .param("search", "testuser"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.playlists").isArray());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(400);
    }

    @Test
    void getPublicPlaylists_ShouldReturnOnlyPublicPlaylists() throws Exception {
        playlistRepository.save(testPlaylist);

        Playlist privatePlaylist = new Playlist();
        privatePlaylist.setPlaylistName("Private Playlist");
        privatePlaylist.setCreatedBy("testuser");
        privatePlaylist.setIsPublic(false);
        privatePlaylist.setDescription("A private playlist");
        playlistRepository.save(privatePlaylist);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/playlists"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.playlists").isArray());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(400);
    }

    @Test
    void addSongToPlaylist_ValidIds_ShouldReturnUpdatedPlaylist() throws Exception {
        Playlist savedPlaylist = playlistRepository.save(testPlaylist);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(post("/api/playlists/{playlistId}/songs/{songId}", 
                savedPlaylist.getPlaylistId(), testSong.getSongId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(500);
    }

    @Test
    void removeSongFromPlaylist_ValidIds_ShouldReturnUpdatedPlaylist() throws Exception {
        Playlist savedPlaylist = playlistRepository.save(testPlaylist);

        mockMvc.perform(post("/api/playlists/{playlistId}/songs/{songId}", 
                savedPlaylist.getPlaylistId(), testSong.getSongId()))
                .andExpect(status().isOk());

        long startTime = System.currentTimeMillis();

        mockMvc.perform(delete("/api/playlists/{playlistId}/songs/{songId}", 
                savedPlaylist.getPlaylistId(), testSong.getSongId()))
                .andExpect(status().isOk());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(500);
    }

    @Test
    void createPlaylist_InvalidData_ShouldReturnBadRequest() throws Exception {
        Playlist invalidPlaylist = new Playlist();

        mockMvc.perform(post("/api/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPlaylist)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void performanceTest_PlaylistOperations_ShouldHandleLoad() throws Exception {
        int numberOfPlaylists = 20;
        long totalTime = 0;

        for (int i = 0; i < numberOfPlaylists; i++) {
            Playlist playlist = new Playlist();
            playlist.setPlaylistName("Playlist " + i);
            playlist.setCreatedBy("user" + (i % 5));
            playlist.setIsPublic(i % 2 == 0);
            playlist.setDescription("Test playlist " + i);

            long startTime = System.currentTimeMillis();

            mockMvc.perform(post("/api/playlists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(playlist)))
                    .andExpect(status().isCreated());

            long endTime = System.currentTimeMillis();
            totalTime += (endTime - startTime);
        }

        double averageResponseTime = (double) totalTime / numberOfPlaylists;
        assertThat(averageResponseTime).isLessThan(150);

        List<Playlist> allPlaylists = playlistRepository.findAll();
        assertThat(allPlaylists).hasSize(numberOfPlaylists);
    }
}
