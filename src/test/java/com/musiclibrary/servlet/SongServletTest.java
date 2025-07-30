package com.musiclibrary.servlet;

import com.musiclibrary.model.Song;
import com.musiclibrary.service.SongService;
import com.musiclibrary.util.ServletTestUtil;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SongServletTest {
    
    @Mock
    private SongService mockSongService;
    
    private SongServlet songServlet;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        songServlet = new SongServlet();
        
        java.lang.reflect.Field serviceField = SongServlet.class.getDeclaredField("songService");
        serviceField.setAccessible(true);
        serviceField.set(songServlet, mockSongService);
    }
    
    @Test
    public void testDoGet_GetAllSongs_Success() throws Exception {
        List<Song> songs = new ArrayList<Song>();
        songs.add(createValidSong());
        when(mockSongService.getAllSongs()).thenReturn(songs);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("GET", "/api/songs");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        songServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        ServletTestUtil.verifyContentType(response, "application/json");
        
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertNotNull("Response content should not be null", responseContent);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
    }
    
    @Test
    public void testDoGet_GetSongById_Success() throws Exception {
        Song song = createValidSong();
        song.setSongId(1L);
        when(mockSongService.getSongById(1L)).thenReturn(song);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("GET", "/api/songs/1");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        songServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain song data", responseContent.contains("Test Song"));
    }
    
    @Test
    public void testDoGet_GetSongById_NotFound() throws Exception {
        when(mockSongService.getSongById(999L)).thenReturn(null);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("GET", "/api/songs/999");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        songServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 404);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain error", responseContent.contains("\"success\":false"));
    }
    
    @Test
    public void testDoGet_WithPagination_Success() throws Exception {
        List<Song> songs = new ArrayList<Song>();
        songs.add(createValidSong());
        when(mockSongService.getSongsWithPagination(0, 10)).thenReturn(songs);
        when(mockSongService.getTotalSongCount()).thenReturn(1L);
        
        Map<String, String> params = ServletTestUtil.createParamMap("page", "0", "size", "10");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithParams("GET", "/api/songs", params);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        songServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain pagination data", responseContent.contains("\"totalElements\":1"));
    }
    
    @Test
    public void testDoPost_CreateSong_Success() throws Exception {
        Song inputSong = createValidSong();
        Song createdSong = createValidSong();
        createdSong.setSongId(1L);
        when(mockSongService.createSong(any(Song.class))).thenReturn(createdSong);
        
        JSONObject songJson = createValidSongJson();
        HttpServletRequest request = ServletTestUtil.createMockRequestWithJson("POST", "/api/songs", songJson);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        songServlet.doPost(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 201);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
        assertTrue("Response should contain song ID", responseContent.contains("\"songId\":1"));
    }
    
    @Test
    public void testDoPost_CreateSong_InvalidData() throws Exception {
        when(mockSongService.createSong(any(Song.class)))
            .thenThrow(new IllegalArgumentException("Song name is required"));
        
        JSONObject invalidJson = new JSONObject();
        invalidJson.put("songName", "");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithJson("POST", "/api/songs", invalidJson);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        songServlet.doPost(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 400);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain error", responseContent.contains("\"success\":false"));
    }
    
    @Test
    public void testDoPut_UpdateSong_Success() throws Exception {
        Song updatedSong = createValidSong();
        updatedSong.setSongId(1L);
        when(mockSongService.updateSong(any(Song.class))).thenReturn(updatedSong);
        
        JSONObject songJson = createValidSongJson();
        songJson.put("songId", 1);
        HttpServletRequest request = ServletTestUtil.createMockRequestWithJson("PUT", "/api/songs/1", songJson);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        songServlet.doPut(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
    }
    
    @Test
    public void testDoDelete_DeleteSong_Success() throws Exception {
        when(mockSongService.deleteSong(1L)).thenReturn(true);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("DELETE", "/api/songs/1");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        songServlet.doDelete(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
    }
    
    @Test
    public void testDoDelete_DeleteSong_NotFound() throws Exception {
        when(mockSongService.deleteSong(999L)).thenReturn(false);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("DELETE", "/api/songs/999");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        songServlet.doDelete(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 404);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain error", responseContent.contains("\"success\":false"));
    }
    
    @Test
    public void testDoGet_FilterByArtist_Success() throws Exception {
        List<Song> songs = new ArrayList<Song>();
        songs.add(createValidSong());
        when(mockSongService.getSongsByArtist("Test Artist")).thenReturn(songs);
        
        Map<String, String> params = ServletTestUtil.createParamMap("artist", "Test Artist");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithParams("GET", "/api/songs", params);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        songServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain filtered results", responseContent.contains("Test Song"));
    }
    
    @Test
    public void testDoGet_FilterByAlbum_Success() throws Exception {
        List<Song> songs = new ArrayList<Song>();
        songs.add(createValidSong());
        when(mockSongService.getSongsByAlbum("Test Album")).thenReturn(songs);
        
        Map<String, String> params = ServletTestUtil.createParamMap("album", "Test Album");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithParams("GET", "/api/songs", params);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        songServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain filtered results", responseContent.contains("Test Song"));
    }
    
    @Test
    public void testDoGet_SearchSongs_Success() throws Exception {
        List<Song> songs = new ArrayList<Song>();
        songs.add(createValidSong());
        when(mockSongService.searchSongs("test")).thenReturn(songs);
        
        Map<String, String> params = ServletTestUtil.createParamMap("search", "test");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithParams("GET", "/api/songs", params);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        songServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain search results", responseContent.contains("Test Song"));
    }
    
    private Song createValidSong() {
        Song song = new Song();
        song.setSongName("Test Song");
        song.setArtistId(1L);
        song.setArtistName("Test Artist");
        song.setAlbumId(1L);
        song.setTrackNumber(1);
        song.setTrackLength(180);
        song.setDateReleased(new Date());
        song.setGenre("Rock");
        song.setRating(4);
        return song;
    }
    
    private JSONObject createValidSongJson() {
        JSONObject json = new JSONObject();
        json.put("songName", "Test Song");
        json.put("artistId", 1);
        json.put("artistName", "Test Artist");
        json.put("albumId", 1);
        json.put("trackNumber", 1);
        json.put("trackLength", 180);
        json.put("genre", "Rock");
        json.put("rating", 4);
        return json;
    }
}
