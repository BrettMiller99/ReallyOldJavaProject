package com.musiclibrary.servlet;

import com.musiclibrary.model.Playlist;
import com.musiclibrary.service.PlaylistService;
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

public class PlaylistServletTest {
    
    @Mock
    private PlaylistService mockPlaylistService;
    
    private PlaylistServlet playlistServlet;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        playlistServlet = new PlaylistServlet();
        
        java.lang.reflect.Field serviceField = PlaylistServlet.class.getDeclaredField("playlistService");
        serviceField.setAccessible(true);
        serviceField.set(playlistServlet, mockPlaylistService);
    }
    
    @Test
    public void testDoGet_GetAllPlaylists_Success() throws Exception {
        List<Playlist> playlists = new ArrayList<Playlist>();
        playlists.add(createValidPlaylist());
        when(mockPlaylistService.getAllPlaylists()).thenReturn(playlists);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("GET", "/api/playlists");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        playlistServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        ServletTestUtil.verifyContentType(response, "application/json");
        
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertNotNull("Response content should not be null", responseContent);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
    }
    
    @Test
    public void testDoGet_GetPlaylistById_Success() throws Exception {
        Playlist playlist = createValidPlaylist();
        playlist.setPlaylistId(1L);
        when(mockPlaylistService.getPlaylistById(1L)).thenReturn(playlist);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("GET", "/api/playlists/1");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        playlistServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain playlist data", responseContent.contains("Test Playlist"));
    }
    
    @Test
    public void testDoGet_GetPlaylistById_NotFound() throws Exception {
        when(mockPlaylistService.getPlaylistById(999L)).thenReturn(null);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("GET", "/api/playlists/999");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        playlistServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 404);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain error", responseContent.contains("\"success\":false"));
    }
    
    @Test
    public void testDoGet_WithPagination_Success() throws Exception {
        List<Playlist> playlists = new ArrayList<Playlist>();
        playlists.add(createValidPlaylist());
        when(mockPlaylistService.getPlaylistsWithPagination(0, 10)).thenReturn(playlists);
        when(mockPlaylistService.getTotalPlaylistCount()).thenReturn(1L);
        
        Map<String, String> params = ServletTestUtil.createParamMap("page", "0", "size", "10");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithParams("GET", "/api/playlists", params);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        playlistServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain pagination data", responseContent.contains("\"totalElements\":1"));
    }
    
    @Test
    public void testDoPost_CreatePlaylist_Success() throws Exception {
        Playlist inputPlaylist = createValidPlaylist();
        Playlist createdPlaylist = createValidPlaylist();
        createdPlaylist.setPlaylistId(1L);
        when(mockPlaylistService.createPlaylist(any(Playlist.class))).thenReturn(createdPlaylist);
        
        JSONObject playlistJson = createValidPlaylistJson();
        HttpServletRequest request = ServletTestUtil.createMockRequestWithJson("POST", "/api/playlists", playlistJson);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        playlistServlet.doPost(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 201);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
        assertTrue("Response should contain playlist ID", responseContent.contains("\"playlistId\":1"));
    }
    
    @Test
    public void testDoPost_CreatePlaylist_InvalidData() throws Exception {
        when(mockPlaylistService.createPlaylist(any(Playlist.class)))
            .thenThrow(new IllegalArgumentException("Playlist name is required"));
        
        JSONObject invalidJson = new JSONObject();
        invalidJson.put("playlistName", "");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithJson("POST", "/api/playlists", invalidJson);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        playlistServlet.doPost(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 400);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain error", responseContent.contains("\"success\":false"));
    }
    
    @Test
    public void testDoPut_UpdatePlaylist_Success() throws Exception {
        Playlist updatedPlaylist = createValidPlaylist();
        updatedPlaylist.setPlaylistId(1L);
        when(mockPlaylistService.updatePlaylist(any(Playlist.class))).thenReturn(updatedPlaylist);
        
        JSONObject playlistJson = createValidPlaylistJson();
        playlistJson.put("playlistId", 1);
        HttpServletRequest request = ServletTestUtil.createMockRequestWithJson("PUT", "/api/playlists/1", playlistJson);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        playlistServlet.doPut(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
    }
    
    @Test
    public void testDoDelete_DeletePlaylist_Success() throws Exception {
        when(mockPlaylistService.deletePlaylist(1L)).thenReturn(true);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("DELETE", "/api/playlists/1");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        playlistServlet.doDelete(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
    }
    
    @Test
    public void testDoDelete_DeletePlaylist_NotFound() throws Exception {
        when(mockPlaylistService.deletePlaylist(999L)).thenReturn(false);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("DELETE", "/api/playlists/999");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        playlistServlet.doDelete(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 404);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain error", responseContent.contains("\"success\":false"));
    }
    
    @Test
    public void testDoGet_FilterByUser_Success() throws Exception {
        List<Playlist> playlists = new ArrayList<Playlist>();
        playlists.add(createValidPlaylist());
        when(mockPlaylistService.getPlaylistsByUser("testuser")).thenReturn(playlists);
        
        Map<String, String> params = ServletTestUtil.createParamMap("user", "testuser");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithParams("GET", "/api/playlists", params);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        playlistServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain filtered results", responseContent.contains("Test Playlist"));
    }
    
    @Test
    public void testDoGet_SearchPlaylists_Success() throws Exception {
        List<Playlist> playlists = new ArrayList<Playlist>();
        playlists.add(createValidPlaylist());
        when(mockPlaylistService.searchPlaylists("test")).thenReturn(playlists);
        
        Map<String, String> params = ServletTestUtil.createParamMap("search", "test");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithParams("GET", "/api/playlists", params);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        playlistServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain search results", responseContent.contains("Test Playlist"));
    }
    
    private Playlist createValidPlaylist() {
        Playlist playlist = new Playlist();
        playlist.setPlaylistName("Test Playlist");
        playlist.setDescription("Test playlist description");
        playlist.setCreatedBy("testuser");
        playlist.setIsPublic(true);
        playlist.setCreatedDate(new Date());
        return playlist;
    }
    
    private JSONObject createValidPlaylistJson() {
        JSONObject json = new JSONObject();
        json.put("playlistName", "Test Playlist");
        json.put("description", "Test playlist description");
        json.put("createdBy", "testuser");
        json.put("isPublic", true);
        return json;
    }
}
