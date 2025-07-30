package com.musiclibrary.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiclibrary.model.Artist;
import com.musiclibrary.repository.ArtistRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ArtistController REST endpoints.
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
class ArtistControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArtistRepository artistRepository;

    private Artist testArtist;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        artistRepository.deleteAll();

        testArtist = new Artist();
        testArtist.setArtistName("Test Artist");
        testArtist.setCountry("USA");
        testArtist.setFormedYear(2000);
    }

    @Test
    void createArtist_ValidArtist_ShouldReturnCreated() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(post("/api/artists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testArtist)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.artist.artistName").value("Test Artist"))
                .andExpect(jsonPath("$.artist.country").value("USA"))
                .andExpect(jsonPath("$.artist.formedYear").value(2000));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(1000);

        List<Artist> artists = artistRepository.findAll();
        assertThat(artists).hasSize(1);
        assertThat(artists.get(0).getArtistName()).isEqualTo("Test Artist");
    }

    @Test
    void getAllArtists_ShouldReturnArtistList() throws Exception {
        Artist savedArtist = artistRepository.save(testArtist);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/artists"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.artists").isArray())
                .andExpect(jsonPath("$.artists[0].artistName").value("Test Artist"))
                .andExpect(jsonPath("$.artists[0].artistId").value(savedArtist.getArtistId()));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(500);
    }

    @Test
    void getArtistById_ExistingId_ShouldReturnArtist() throws Exception {
        Artist savedArtist = artistRepository.save(testArtist);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/artists/{id}", savedArtist.getArtistId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.artist.artistId").value(savedArtist.getArtistId()))
                .andExpect(jsonPath("$.artist.artistName").value("Test Artist"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(300);
    }

    @Test
    void getArtistById_NonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/artists/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateArtist_ValidUpdate_ShouldReturnUpdatedArtist() throws Exception {
        Artist savedArtist = artistRepository.save(testArtist);
        savedArtist.setArtistName("Updated Artist Name");
        savedArtist.setCountry("Canada");

        long startTime = System.currentTimeMillis();

        mockMvc.perform(put("/api/artists/{id}", savedArtist.getArtistId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedArtist)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.artist.artistName").value("Updated Artist Name"))
                .andExpect(jsonPath("$.artist.country").value("Canada"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(800);

        Artist updatedArtist = artistRepository.findById(savedArtist.getArtistId()).orElse(null);
        assertThat(updatedArtist).isNotNull();
        assertThat(updatedArtist.getArtistName()).isEqualTo("Updated Artist Name");
        assertThat(updatedArtist.getCountry()).isEqualTo("Canada");
    }

    @Test
    void deleteArtist_ExistingId_ShouldReturnOk() throws Exception {
        Artist savedArtist = artistRepository.save(testArtist);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(delete("/api/artists/{id}", savedArtist.getArtistId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Artist deleted successfully"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(500);

        assertThat(artistRepository.findById(savedArtist.getArtistId())).isEmpty();
    }

    @Test
    void searchArtists_ValidQuery_ShouldReturnMatchingArtists() throws Exception {
        artistRepository.save(testArtist);

        Artist anotherArtist = new Artist();
        anotherArtist.setArtistName("Another Artist");
        anotherArtist.setCountry("UK");
        anotherArtist.setFormedYear(1995);
        artistRepository.save(anotherArtist);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/artists")
                .param("search", "Test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.artists").isArray())
                .andExpect(jsonPath("$.artists[0].artistName").value("Test Artist"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(600);
    }

    @Test
    void getArtistsByCountry_ValidCountry_ShouldReturnCountryArtists() throws Exception {
        artistRepository.save(testArtist);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/artists")
                .param("search", "USA"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.artists").isArray());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(400);
    }

    @Test
    void createArtist_InvalidData_ShouldReturnBadRequest() throws Exception {
        Artist invalidArtist = new Artist();

        mockMvc.perform(post("/api/artists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidArtist)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createArtist_DuplicateName_ShouldReturnConflict() throws Exception {
        artistRepository.save(testArtist);

        Artist duplicateArtist = new Artist();
        duplicateArtist.setArtistName("Test Artist");
        duplicateArtist.setCountry("Canada");
        duplicateArtist.setFormedYear(2005);

        mockMvc.perform(post("/api/artists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateArtist)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void performanceTest_BulkOperations_ShouldHandleLoad() throws Exception {
        int numberOfArtists = 50;
        long totalTime = 0;

        for (int i = 0; i < numberOfArtists; i++) {
            Artist artist = new Artist();
            artist.setArtistName("Artist " + i);
            artist.setCountry("Country " + (i % 10));
            artist.setFormedYear(1990 + (i % 30));

            long startTime = System.currentTimeMillis();

            mockMvc.perform(post("/api/artists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(artist)))
                    .andExpect(status().isCreated());

            long endTime = System.currentTimeMillis();
            totalTime += (endTime - startTime);
        }

        double averageResponseTime = (double) totalTime / numberOfArtists;
        assertThat(averageResponseTime).isLessThan(100);

        List<Artist> allArtists = artistRepository.findAll();
        assertThat(allArtists).hasSize(numberOfArtists);
    }
}
