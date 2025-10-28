package com.musiclibrary.servlet;

import com.musiclibrary.model.Song;
import com.musiclibrary.service.SongService;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for SongServlet using traditional Java 7 testing patterns.
 * 
 * Testing Approach:
 * - Tests HTTP request/response processing for song management
 * - Validates JSON serialization and deserialization
 * - Tests all CRUD operations and query parameters
 * - Verifies proper HTTP status codes and error handling
 * - Mocks service layer to isolate servlet logic
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class SongServletTest extends ServletTestBase {
    
    @Mock
    private SongService mockSongService;
    
    private SongServlet songServlet;
    
    @Before
    public void setUp() throws Exception {
        super.setUpServletBase();
        songServlet = new SongServlet();
        
        songServlet.init(mockServletConfig);
        
        setPrivateField(songServlet, "songService", mockSongService);
    }
    
    @Test
    public void testDoGet_GetAllSongs_Success() throws Exception {
        List<Song> songs = new ArrayList<Song>();
        songs.add(createValidSong());
        
        when(mockSongService.getAllSongs()).thenReturn(songs);
        
        songServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockSongService, times(1)).getAllSongs();
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertNotNull("Response should contain data", response.get("data"));
    }
    
    @Test
    public void testDoGet_GetSongById_Success() throws Exception {
        Song song = createValidSong();
        song.setSongId(1L);
        
        setupPathInfo("/1");
        when(mockSongService.getSongById(1L)).thenReturn(song);
        
        songServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockSongService, times(1)).getSongById(1L);
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_SearchSongs_Success() throws Exception {
        List<Song> songs = new ArrayList<Song>();
        songs.add(createValidSong());
        
        setupQueryParameter("search", "test");
        when(mockSongService.searchSongs("test")).thenReturn(songs);
        
        songServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockSongService, times(1)).searchSongs("test");
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_FilterByArtist_Success() throws Exception {
        List<Song> songs = new ArrayList<Song>();
        songs.add(createValidSong());
        
        setupQueryParameter("artist", "Test Artist");
        when(mockSongService.getSongsByArtist("Test Artist")).thenReturn(songs);
        
        songServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockSongService, times(1)).getSongsByArtist("Test Artist");
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_FilterByAlbum_Success() throws Exception {
        List<Song> songs = new ArrayList<Song>();
        songs.add(createValidSong());
        
        setupQueryParameter("album", "Test Album");
        when(mockSongService.getSongsByAlbum("Test Album")).thenReturn(songs);
        
        songServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockSongService, times(1)).getSongsByAlbum("Test Album");
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_Pagination_Success() throws Exception {
        List<Song> songs = new ArrayList<Song>();
        songs.add(createValidSong());
        
        setupQueryParameter("page", "0");
        setupQueryParameter("size", "10");
        when(mockSongService.getSongsWithPagination(0, 10)).thenReturn(songs);
        when(mockSongService.getTotalSongCount()).thenReturn(1L);
        
        songServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockSongService, times(1)).getSongsWithPagination(0, 10);
        verify(mockSongService, times(1)).getTotalSongCount();
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertNotNull("Response should contain pagination", response.get("pagination"));
    }
    
    @Test
    public void testDoPost_CreateSong_Success() throws Exception {
        Song createdSong = createValidSong();
        createdSong.setSongId(1L);
        
        setupJsonRequest(createValidSongJson());
        when(mockSongService.createSong(any(Song.class))).thenReturn(createdSong);
        
        songServlet.doPost(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_CREATED);
        verify(mockSongService, times(1)).createSong(any(Song.class));
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertTrue("Message should indicate creation", 
            response.getString("message").contains("created successfully"));
    }
    
    @Test
    public void testDoPost_InvalidSongData_BadRequest() throws Exception {
        setupJsonRequest(createValidSongJson());
        when(mockSongService.createSong(any(Song.class)))
            .thenThrow(new IllegalArgumentException("Song name is required"));
        
        songServlet.doPost(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        
        JSONObject response = getResponseJson();
        assertFalse("Response should indicate failure", response.getBoolean("success"));
        assertTrue("Error should contain validation message", 
            response.getString("error").contains("Song name is required"));
    }
    
    @Test
    public void testDoPut_UpdateSong_Success() throws Exception {
        Song updatedSong = createValidSong();
        updatedSong.setSongId(1L);
        
        setupPathInfo("/1");
        setupJsonRequest(createValidSongJson());
        when(mockSongService.updateSong(any(Song.class))).thenReturn(updatedSong);
        
        songServlet.doPut(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockSongService, times(1)).updateSong(any(Song.class));
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertTrue("Message should indicate update", 
            response.getString("message").contains("updated successfully"));
    }
    
    @Test
    public void testDoDelete_DeleteSong_Success() throws Exception {
        setupPathInfo("/1");
        when(mockSongService.deleteSong(1L)).thenReturn(true);
        
        songServlet.doDelete(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockSongService, times(1)).deleteSong(1L);
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertTrue("Message should indicate deletion", 
            response.getString("message").contains("deleted successfully"));
    }
    
    @Test
    public void testDoDelete_SongNotFound_NotFound() throws Exception {
        setupPathInfo("/999");
        when(mockSongService.deleteSong(999L)).thenReturn(false);
        
        songServlet.doDelete(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(mockSongService, times(1)).deleteSong(999L);
        
        JSONObject response = getResponseJson();
        assertFalse("Response should indicate failure", response.getBoolean("success"));
        assertTrue("Error should mention not found", 
            response.getString("error").contains("Song not found"));
    }
    
    private Song createValidSong() {
        Song song = new Song();
        song.setSongName("Test Song");
        song.setAlbumName("Test Album");
        song.setArtistName("Test Artist");
        song.setAlbumId(1L);
        song.setArtistId(1L);
        song.setTrackNumber(1);
        song.setTrackLength(180);
        song.setGenre("Rock");
        song.setRating(4);
        song.setPlayCount(0);
        song.setCreatedDate(new Date());
        return song;
    }
    
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
