package com.musiclibrary.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiclibrary.model.Album;
import com.musiclibrary.model.Artist;
import com.musiclibrary.model.Song;
import com.musiclibrary.repository.AlbumRepository;
import com.musiclibrary.repository.ArtistRepository;
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
 * Integration tests for SongController REST endpoints.
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
class SongControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistRepository artistRepository;

    private Artist testArtist;
    private Album testAlbum;
    private Song testSong;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
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
    }

    @Test
    void createSong_ValidSong_ShouldReturnCreated() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSong)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.song.songName").value("Test Song"))
                .andExpect(jsonPath("$.song.trackNumber").value(1))
                .andExpect(jsonPath("$.song.rating").value(4));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(1000);

        List<Song> songs = songRepository.findAll();
        assertThat(songs).hasSize(1);
        assertThat(songs.get(0).getSongName()).isEqualTo("Test Song");
    }

    @Test
    void getAllSongs_ShouldReturnSongList() throws Exception {
        Song savedSong = songRepository.save(testSong);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/songs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.songs").isArray())
                .andExpect(jsonPath("$.songs[0].songName").value("Test Song"))
                .andExpect(jsonPath("$.songs[0].songId").value(savedSong.getSongId()));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(500);
    }

    @Test
    void getSongById_ExistingId_ShouldReturnSong() throws Exception {
        Song savedSong = songRepository.save(testSong);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/songs/{id}", savedSong.getSongId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.song.songId").value(savedSong.getSongId()))
                .andExpect(jsonPath("$.song.songName").value("Test Song"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(300);
    }

    @Test
    void getSongById_NonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/songs/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSong_ValidUpdate_ShouldReturnUpdatedSong() throws Exception {
        Song savedSong = songRepository.save(testSong);
        savedSong.setSongName("Updated Song Name");
        savedSong.setRating(5);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(put("/api/songs/{id}", savedSong.getSongId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedSong)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.song.songName").value("Updated Song Name"))
                .andExpect(jsonPath("$.song.rating").value(5));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(800);

        Song updatedSong = songRepository.findById(savedSong.getSongId()).orElse(null);
        assertThat(updatedSong).isNotNull();
        assertThat(updatedSong.getSongName()).isEqualTo("Updated Song Name");
        assertThat(updatedSong.getRating()).isEqualTo(5);
    }

    @Test
    void deleteSong_ExistingId_ShouldReturnNoContent() throws Exception {
        Song savedSong = songRepository.save(testSong);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(delete("/api/songs/{id}", savedSong.getSongId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Song deleted successfully"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(500);

        assertThat(songRepository.findById(savedSong.getSongId())).isEmpty();
    }

    @Test
    void searchSongs_ValidQuery_ShouldReturnMatchingSongs() throws Exception {
        songRepository.save(testSong);

        Song anotherSong = new Song();
        anotherSong.setSongName("Another Song");
        anotherSong.setArtist(testArtist);
        anotherSong.setAlbum(testAlbum);
        anotherSong.setDateReleased(LocalDate.of(2023, 2, 1));
        anotherSong.setTrackLength(255); // 4 minutes 15 seconds = 255 seconds
        anotherSong.setTrackNumber(2);
        anotherSong.setRating(3);
        anotherSong.setPlayCount(0);
        songRepository.save(anotherSong);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/songs")
                .param("search", "Test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.songs").isArray())
                .andExpect(jsonPath("$.songs[0].songName").value("Test Song"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(600);
    }

    @Test
    void getSongsByArtist_ValidArtistId_ShouldReturnArtistSongs() throws Exception {
        songRepository.save(testSong);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/songs")
                .param("search", testArtist.getArtistName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.songs").isArray());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(400);
    }

    @Test
    void getSongsByAlbum_ValidAlbumId_ShouldReturnAlbumSongs() throws Exception {
        songRepository.save(testSong);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/songs")
                .param("search", testAlbum.getAlbumName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.songs").isArray());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(400);
    }

    @Test
    void recordPlayback_ValidSongId_ShouldIncrementPlaybackCount() throws Exception {
        Song savedSong = songRepository.save(testSong);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(post("/api/songs/{id}/play", savedSong.getSongId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(300);

        Song updatedSong = songRepository.findById(savedSong.getSongId()).orElse(null);
        assertThat(updatedSong).isNotNull();
        assertThat(updatedSong.getPlayCount()).isEqualTo(1);
    }

    @Test
    void createSong_InvalidData_ShouldReturnBadRequest() throws Exception {
        Song invalidSong = new Song();

        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidSong)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void performanceTest_HighVolumePlaybacks_ShouldHandleLoad() throws Exception {
        Song savedSong = songRepository.save(testSong);

        int numberOfPlaybacks = 100;
        long totalTime = 0;

        for (int i = 0; i < numberOfPlaybacks; i++) {
            long startTime = System.currentTimeMillis();

            mockMvc.perform(post("/api/songs/{id}/play", savedSong.getSongId()))
                    .andExpect(status().isOk());

            long endTime = System.currentTimeMillis();
            totalTime += (endTime - startTime);
        }

        double averageResponseTime = (double) totalTime / numberOfPlaybacks;
        assertThat(averageResponseTime).isLessThan(50);

        Song finalSong = songRepository.findById(savedSong.getSongId()).orElse(null);
        assertThat(finalSong).isNotNull();
        assertThat(finalSong.getPlayCount()).isEqualTo(numberOfPlaybacks);
    }
}
