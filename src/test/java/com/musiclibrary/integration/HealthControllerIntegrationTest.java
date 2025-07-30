package com.musiclibrary.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for HealthController REST endpoints.
 * 
 * Comprehensive testing including health checks, database connectivity,
 * and performance metrics.
 * 
 * @author Music Library Development Team
 * @version 2.0 - Spring Boot Integration Tests
 * @since Java 17
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HealthControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void healthCheck_ShouldReturnOk() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("Music Library API"))
                .andExpect(jsonPath("$.version").value("2.0.0"))
                .andExpect(jsonPath("$.timestamp").exists());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(200);
    }

    @Test
    void statusCheck_ShouldReturnDetailedStatus() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.application").value("Music Library API"))
                .andExpect(jsonPath("$.version").value("2.0.0"))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.uptime").exists())
                .andExpect(jsonPath("$.system").exists())
                .andExpect(jsonPath("$.healthChecks").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(300);
    }

    @Test
    void statusCheck_ShouldIncludeSystemInformation() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.system.javaVersion").exists())
                .andExpect(jsonPath("$.system.javaVendor").exists())
                .andExpect(jsonPath("$.system.osName").exists())
                .andExpect(jsonPath("$.system.totalMemory").exists())
                .andExpect(jsonPath("$.system.freeMemory").exists())
                .andExpect(jsonPath("$.healthChecks.database").exists())
                .andExpect(jsonPath("$.healthChecks.memory").exists());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(250);
    }

    @Test
    void healthCheck_ShouldValidateResponseStructure() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("Music Library API"))
                .andExpect(jsonPath("$.version").value("2.0.0"))
                .andExpect(jsonPath("$.timestamp").isNumber());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(200);
    }

    @Test
    void statusCheck_ShouldIncludeDatabaseHealthCheck() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.healthChecks.database.status").exists())
                .andExpect(jsonPath("$.healthChecks.database.description").value("Database connectivity check"));
    }

    @Test
    void statusCheck_ShouldIncludeMemoryHealthCheck() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.healthChecks.memory.status").exists())
                .andExpect(jsonPath("$.healthChecks.memory.description").value("Memory usage check"))
                .andExpect(jsonPath("$.healthChecks.memory.usedMemory").exists())
                .andExpect(jsonPath("$.healthChecks.memory.percentUsed").exists());
    }

    @Test
    void statusCheck_ShouldIncludeUptimeFormatting() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uptime").exists())
                .andExpect(jsonPath("$.uptimeFormatted").exists())
                .andExpect(jsonPath("$.startupTime").exists());
    }

    @Test
    void performanceTest_HealthEndpoints_ShouldHandleLoad() throws Exception {
        int numberOfRequests = 50;
        long totalTime = 0;

        for (int i = 0; i < numberOfRequests; i++) {
            long startTime = System.currentTimeMillis();

            mockMvc.perform(get("/api/health"))
                    .andExpect(status().isOk());

            long endTime = System.currentTimeMillis();
            totalTime += (endTime - startTime);
        }

        double averageResponseTime = (double) totalTime / numberOfRequests;
        assertThat(averageResponseTime).isLessThan(100);
    }

    @Test
    void performanceTest_StatusEndpoints_ShouldHandleLoad() throws Exception {
        int numberOfRequests = 25;
        long totalTime = 0;

        for (int i = 0; i < numberOfRequests; i++) {
            long startTime = System.currentTimeMillis();

            mockMvc.perform(get("/api/status"))
                    .andExpect(status().isOk());

            long endTime = System.currentTimeMillis();
            totalTime += (endTime - startTime);
        }

        double averageResponseTime = (double) totalTime / numberOfRequests;
        assertThat(averageResponseTime).isLessThan(150);
    }
}
