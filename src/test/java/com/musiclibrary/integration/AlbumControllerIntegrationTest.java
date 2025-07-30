package com.musiclibrary.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiclibrary.model.Album;
import com.musiclibrary.model.Artist;
import com.musiclibrary.repository.AlbumRepository;
import com.musiclibrary.repository.ArtistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AlbumController REST endpoints.
 * 
 * Tests the complete request-response cycle including:
 * - HTTP request handling
 * - JSON serialization/deserialization
 * - Service layer integration
 * - Database persistence
 * - Performance metrics
 * 
 * @author Music Library Development Team
 * @version 2.0 - Spring Boot Integration Tests
 * @since Java 17
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class AlbumControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistRepository artistRepository;

    private Artist testArtist;
    private Album testAlbum;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
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
    }

    @Test
    void createAlbum_ValidAlbum_ShouldReturnCreated() throws Exception {
        long startTime = System.currentTimeMillis();

        MvcResult result = mockMvc.perform(post("/api/albums")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAlbum)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.album.albumName").value("Test Album"))
                .andExpect(jsonPath("$.album.genre").value("Rock"))
                .andExpect(jsonPath("$.message").value("Album created successfully"))
                .andReturn();

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(1000);

        List<Album> albums = albumRepository.findAll();
        assertThat(albums).hasSize(1);
        assertThat(albums.get(0).getAlbumName()).isEqualTo("Test Album");
    }

    @Test
    void getAllAlbums_ShouldReturnAlbumList() throws Exception {
        Album savedAlbum = albumRepository.save(testAlbum);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/albums"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.albums").isArray())
                .andExpect(jsonPath("$.albums[0].albumName").value("Test Album"))
                .andExpect(jsonPath("$.albums[0].albumId").value(savedAlbum.getAlbumId()))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(500);
    }

    @Test
    void getAlbumById_ExistingId_ShouldReturnAlbum() throws Exception {
        Album savedAlbum = albumRepository.save(testAlbum);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/albums/{id}", savedAlbum.getAlbumId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.album.albumId").value(savedAlbum.getAlbumId()))
                .andExpect(jsonPath("$.album.albumName").value("Test Album"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(300);
    }

    @Test
    void getAlbumById_NonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/albums/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAlbum_ValidUpdate_ShouldReturnUpdatedAlbum() throws Exception {
        Album savedAlbum = albumRepository.save(testAlbum);
        savedAlbum.setAlbumName("Updated Album Name");

        long startTime = System.currentTimeMillis();

        mockMvc.perform(put("/api/albums/{id}", savedAlbum.getAlbumId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedAlbum)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.album.albumName").value("Updated Album Name"))
                .andExpect(jsonPath("$.message").value("Album updated successfully"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(800);

        Album updatedAlbum = albumRepository.findById(savedAlbum.getAlbumId()).orElse(null);
        assertThat(updatedAlbum).isNotNull();
        assertThat(updatedAlbum.getAlbumName()).isEqualTo("Updated Album Name");
    }

    @Test
    void deleteAlbum_ExistingId_ShouldReturnOk() throws Exception {
        Album savedAlbum = albumRepository.save(testAlbum);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(delete("/api/albums/{id}", savedAlbum.getAlbumId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Album deleted successfully"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(500);

        assertThat(albumRepository.findById(savedAlbum.getAlbumId())).isEmpty();
    }

    @Test
    void searchAlbums_ValidQuery_ShouldReturnMatchingAlbums() throws Exception {
        albumRepository.save(testAlbum);

        Album anotherAlbum = new Album();
        anotherAlbum.setAlbumName("Another Album");
        anotherAlbum.setArtist(testArtist);
        anotherAlbum.setReleaseDate(LocalDate.of(2023, 6, 1));
        anotherAlbum.setGenre("Pop");
        anotherAlbum.setRecordLabel("Pop Records");
        anotherAlbum.setTotalTracks(12);
        albumRepository.save(anotherAlbum);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/albums")
                .param("search", "Test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.albums").isArray())
                .andExpect(jsonPath("$.albums[0].albumName").value("Test Album"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(600);
    }


    @Test
    void createAlbum_InvalidData_ShouldReturnBadRequest() throws Exception {
        Album invalidAlbum = new Album();

        mockMvc.perform(post("/api/albums")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAlbum)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void performanceTest_ConcurrentRequests_ShouldHandleLoad() throws Exception {
        Album savedAlbum = albumRepository.save(testAlbum);

        int numberOfRequests = 10;
        long[] responseTimes = new long[numberOfRequests];

        for (int i = 0; i < numberOfRequests; i++) {
            long startTime = System.currentTimeMillis();

            mockMvc.perform(get("/api/albums/{id}", savedAlbum.getAlbumId()))
                    .andExpect(status().isOk());

            long endTime = System.currentTimeMillis();
            responseTimes[i] = endTime - startTime;
        }

        double averageResponseTime = java.util.Arrays.stream(responseTimes)
                .average()
                .orElse(0.0);

        assertThat(averageResponseTime).isLessThan(200);

        long maxResponseTime = java.util.Arrays.stream(responseTimes).max().orElse(0L);
        assertThat(maxResponseTime).isLessThan(500);
    }
}
