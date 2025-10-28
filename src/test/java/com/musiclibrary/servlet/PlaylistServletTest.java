package com.musiclibrary.servlet;

import com.musiclibrary.model.Playlist;
import com.musiclibrary.service.PlaylistService;
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
 * Integration tests for PlaylistServlet using traditional Java 7 testing patterns.
 * 
 * Testing Approach:
 * - Tests HTTP request/response processing for playlist management
 * - Validates JSON serialization and deserialization
 * - Tests all CRUD operations and query parameters
 * - Verifies proper HTTP status codes and error handling
 * - Mocks service layer to isolate servlet logic
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class PlaylistServletTest extends ServletTestBase {
    
    @Mock
    private PlaylistService mockPlaylistService;
    
    private PlaylistServlet playlistServlet;
    
    @Before
    public void setUp() throws Exception {
        super.setUpServletBase();
        playlistServlet = new PlaylistServlet();
        
        playlistServlet.init(mockServletConfig);
        
        setPrivateField(playlistServlet, "playlistService", mockPlaylistService);
    }
    
    @Test
    public void testDoGet_GetAllPlaylists_Success() throws Exception {
        List<Playlist> playlists = new ArrayList<Playlist>();
        playlists.add(createValidPlaylist());
        
        when(mockPlaylistService.getAllPlaylists()).thenReturn(playlists);
        
        playlistServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockPlaylistService, times(1)).getAllPlaylists();
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertNotNull("Response should contain data", response.get("data"));
    }
    
    @Test
    public void testDoGet_GetPlaylistById_Success() throws Exception {
        Playlist playlist = createValidPlaylist();
        playlist.setPlaylistId(1L);
        
        setupPathInfo("/1");
        when(mockPlaylistService.getPlaylistById(1L)).thenReturn(playlist);
        
        playlistServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockPlaylistService, times(1)).getPlaylistById(1L);
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_SearchPlaylists_Success() throws Exception {
        List<Playlist> playlists = new ArrayList<Playlist>();
        playlists.add(createValidPlaylist());
        
        setupQueryParameter("search", "test");
        when(mockPlaylistService.searchPlaylists("test")).thenReturn(playlists);
        
        playlistServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockPlaylistService, times(1)).searchPlaylists("test");
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_FilterByUser_Success() throws Exception {
        List<Playlist> playlists = new ArrayList<Playlist>();
        playlists.add(createValidPlaylist());
        
        setupQueryParameter("user", "testuser");
        when(mockPlaylistService.getPlaylistsByUser("testuser")).thenReturn(playlists);
        
        playlistServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockPlaylistService, times(1)).getPlaylistsByUser("testuser");
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_FilterPublicPlaylists_Success() throws Exception {
        List<Playlist> playlists = new ArrayList<Playlist>();
        playlists.add(createValidPlaylist());
        
        setupQueryParameter("public", "true");
        when(mockPlaylistService.getPublicPlaylists()).thenReturn(playlists);
        
        playlistServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockPlaylistService, times(1)).getPublicPlaylists();
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_Pagination_Success() throws Exception {
        List<Playlist> playlists = new ArrayList<Playlist>();
        playlists.add(createValidPlaylist());
        
        setupQueryParameter("page", "0");
        setupQueryParameter("size", "10");
        when(mockPlaylistService.getPlaylistsWithPagination(0, 10)).thenReturn(playlists);
        when(mockPlaylistService.getTotalPlaylistCount()).thenReturn(1L);
        
        playlistServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockPlaylistService, times(1)).getPlaylistsWithPagination(0, 10);
        verify(mockPlaylistService, times(1)).getTotalPlaylistCount();
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertNotNull("Response should contain pagination", response.get("pagination"));
    }
    
    @Test
    public void testDoPost_CreatePlaylist_Success() throws Exception {
        Playlist createdPlaylist = createValidPlaylist();
        createdPlaylist.setPlaylistId(1L);
        
        setupJsonRequest(createValidPlaylistJson());
        when(mockPlaylistService.createPlaylist(any(Playlist.class))).thenReturn(createdPlaylist);
        
        playlistServlet.doPost(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_CREATED);
        verify(mockPlaylistService, times(1)).createPlaylist(any(Playlist.class));
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertTrue("Message should indicate creation", 
            response.getString("message").contains("created successfully"));
    }
    
    @Test
    public void testDoPost_InvalidPlaylistData_BadRequest() throws Exception {
        setupJsonRequest(createValidPlaylistJson());
        when(mockPlaylistService.createPlaylist(any(Playlist.class)))
            .thenThrow(new IllegalArgumentException("Playlist name is required"));
        
        playlistServlet.doPost(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        
        JSONObject response = getResponseJson();
        assertFalse("Response should indicate failure", response.getBoolean("success"));
        assertTrue("Error should contain validation message", 
            response.getString("error").contains("Playlist name is required"));
    }
    
    @Test
    public void testDoPut_UpdatePlaylist_Success() throws Exception {
        Playlist updatedPlaylist = createValidPlaylist();
        updatedPlaylist.setPlaylistId(1L);
        
        setupPathInfo("/1");
        setupJsonRequest(createValidPlaylistJson());
        when(mockPlaylistService.updatePlaylist(any(Playlist.class))).thenReturn(updatedPlaylist);
        
        playlistServlet.doPut(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockPlaylistService, times(1)).updatePlaylist(any(Playlist.class));
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertTrue("Message should indicate update", 
            response.getString("message").contains("updated successfully"));
    }
    
    @Test
    public void testDoDelete_DeletePlaylist_Success() throws Exception {
        setupPathInfo("/1");
        when(mockPlaylistService.deletePlaylist(1L)).thenReturn(true);
        
        playlistServlet.doDelete(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockPlaylistService, times(1)).deletePlaylist(1L);
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertTrue("Message should indicate deletion", 
            response.getString("message").contains("deleted successfully"));
    }
    
    @Test
    public void testDoDelete_PlaylistNotFound_NotFound() throws Exception {
        setupPathInfo("/999");
        when(mockPlaylistService.deletePlaylist(999L)).thenReturn(false);
        
        playlistServlet.doDelete(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(mockPlaylistService, times(1)).deletePlaylist(999L);
        
        JSONObject response = getResponseJson();
        assertFalse("Response should indicate failure", response.getBoolean("success"));
        assertTrue("Error should mention not found", 
            response.getString("error").contains("Playlist not found"));
    }
    
    private Playlist createValidPlaylist() {
        Playlist playlist = new Playlist();
        playlist.setPlaylistName("Test Playlist");
        playlist.setDescription("Test playlist description");
        playlist.setCreatedBy("testuser");
        playlist.setIsPublic(true);
        playlist.setTotalDuration(0);
        playlist.setSongCount(0);
        playlist.setCreatedDate(new Date());
        return playlist;
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
