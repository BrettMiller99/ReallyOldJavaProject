package com.musiclibrary.integration;

import com.musiclibrary.dao.SongDAO;
import com.musiclibrary.model.Song;
import com.musiclibrary.service.SongService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@ExtendWith(MockitoExtension.class)
public class SongServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    private SongService songService;
    private SongDAO songDAO;
    
    @BeforeEach
    public void setUp() throws Exception {
        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("CREATE TABLE IF NOT EXISTS artists (" +
                    "artist_id SERIAL PRIMARY KEY, " +
                    "artist_name VARCHAR(255) NOT NULL)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS songs (" +
                    "song_id SERIAL PRIMARY KEY, " +
                    "song_name VARCHAR(255) NOT NULL, " +
                    "artist_id BIGINT REFERENCES artists(artist_id), " +
                    "track_length INTEGER, " +
                    "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            stmt.execute("INSERT INTO artists (artist_id, artist_name) VALUES (1, 'Test Artist')");
        }
        
        songDAO = new SongDAO();
        songService = new SongService(songDAO);
    }
    
    @Test
    public void testCreateSongWithRealDatabase() throws Exception {
        Song song = new Song();
        song.setSongName("Integration Test Song");
        song.setArtistId(1L);
        song.setTrackLength(180);
        song.setCreatedDate(new Date());
        song.setLastModified(new Date());
        
        Song createdSong = songService.createSong(song);
        
        assertNotNull(createdSong);
        assertNotNull(createdSong.getSongId());
        assertEquals("Integration Test Song", createdSong.getSongName());
        assertEquals(Long.valueOf(1), createdSong.getArtistId());
    }
    
    @Test
    public void testFindAllSongsWithRealDatabase() throws Exception {
        Song song1 = new Song();
        song1.setSongName("Test Song 1");
        song1.setArtistId(1L);
        song1.setTrackLength(120);
        song1.setCreatedDate(new Date());
        song1.setLastModified(new Date());
        
        Song song2 = new Song();
        song2.setSongName("Test Song 2");
        song2.setArtistId(1L);
        song2.setTrackLength(150);
        song2.setCreatedDate(new Date());
        song2.setLastModified(new Date());
        
        songService.createSong(song1);
        songService.createSong(song2);
        
        List<Song> songs = songService.getAllSongs();
        
        assertNotNull(songs);
        assertTrue(songs.size() >= 2);
    }
    
    @Test
    public void testUpdateSongWithRealDatabase() throws Exception {
        Song song = new Song();
        song.setSongName("Original Name");
        song.setArtistId(1L);
        song.setTrackLength(180);
        song.setCreatedDate(new Date());
        song.setLastModified(new Date());
        
        Song createdSong = songService.createSong(song);
        
        createdSong.setSongName("Updated Name");
        createdSong.setTrackLength(200);
        
        Song updatedSong = songService.updateSong(createdSong);
        
        assertNotNull(updatedSong);
        assertEquals("Updated Name", updatedSong.getSongName());
        assertEquals(200, updatedSong.getTrackLength());
    }
}
