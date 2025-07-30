package com.musiclibrary.servlet;

import com.musiclibrary.model.Album;
import com.musiclibrary.service.AlbumService;
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

public class AlbumServletTest {
    
    @Mock
    private AlbumService mockAlbumService;
    
    private AlbumServlet albumServlet;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        albumServlet = new AlbumServlet();
        
        java.lang.reflect.Field serviceField = AlbumServlet.class.getDeclaredField("albumService");
        serviceField.setAccessible(true);
        serviceField.set(albumServlet, mockAlbumService);
    }
    
    @Test
    public void testDoGet_GetAllAlbums_Success() throws Exception {
        List<Album> albums = new ArrayList<Album>();
        albums.add(createValidAlbum());
        when(mockAlbumService.getAllAlbums()).thenReturn(albums);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("GET", "/api/albums");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        albumServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        ServletTestUtil.verifyContentType(response, "application/json");
        
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertNotNull("Response content should not be null", responseContent);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
    }
    
    @Test
    public void testDoGet_GetAlbumById_Success() throws Exception {
        Album album = createValidAlbum();
        album.setAlbumId(1L);
        when(mockAlbumService.getAlbumById(1L)).thenReturn(album);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("GET", "/api/albums/1");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        albumServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain album data", responseContent.contains("Test Album"));
    }
    
    @Test
    public void testDoGet_GetAlbumById_NotFound() throws Exception {
        when(mockAlbumService.getAlbumById(999L)).thenReturn(null);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("GET", "/api/albums/999");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        albumServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 404);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain error", responseContent.contains("\"success\":false"));
    }
    
    @Test
    public void testDoGet_WithPagination_Success() throws Exception {
        List<Album> albums = new ArrayList<Album>();
        albums.add(createValidAlbum());
        when(mockAlbumService.getAlbumsWithPagination(0, 10)).thenReturn(albums);
        when(mockAlbumService.getTotalAlbumCount()).thenReturn(1L);
        
        Map<String, String> params = ServletTestUtil.createParamMap("page", "0", "size", "10");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithParams("GET", "/api/albums", params);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        albumServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain pagination data", responseContent.contains("\"totalElements\":1"));
    }
    
    @Test
    public void testDoPost_CreateAlbum_Success() throws Exception {
        Album inputAlbum = createValidAlbum();
        Album createdAlbum = createValidAlbum();
        createdAlbum.setAlbumId(1L);
        when(mockAlbumService.createAlbum(any(Album.class))).thenReturn(createdAlbum);
        
        JSONObject albumJson = createValidAlbumJson();
        HttpServletRequest request = ServletTestUtil.createMockRequestWithJson("POST", "/api/albums", albumJson);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        albumServlet.doPost(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 201);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
        assertTrue("Response should contain album ID", responseContent.contains("\"albumId\":1"));
    }
    
    @Test
    public void testDoPost_CreateAlbum_InvalidData() throws Exception {
        when(mockAlbumService.createAlbum(any(Album.class)))
            .thenThrow(new IllegalArgumentException("Album name is required"));
        
        JSONObject invalidJson = new JSONObject();
        invalidJson.put("albumName", "");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithJson("POST", "/api/albums", invalidJson);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        albumServlet.doPost(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 400);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain error", responseContent.contains("\"success\":false"));
    }
    
    @Test
    public void testDoPut_UpdateAlbum_Success() throws Exception {
        Album updatedAlbum = createValidAlbum();
        updatedAlbum.setAlbumId(1L);
        when(mockAlbumService.updateAlbum(any(Album.class))).thenReturn(updatedAlbum);
        
        JSONObject albumJson = createValidAlbumJson();
        albumJson.put("albumId", 1);
        HttpServletRequest request = ServletTestUtil.createMockRequestWithJson("PUT", "/api/albums/1", albumJson);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        albumServlet.doPut(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
    }
    
    @Test
    public void testDoDelete_DeleteAlbum_Success() throws Exception {
        when(mockAlbumService.deleteAlbum(1L)).thenReturn(true);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("DELETE", "/api/albums/1");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        albumServlet.doDelete(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
    }
    
    @Test
    public void testDoDelete_DeleteAlbum_NotFound() throws Exception {
        when(mockAlbumService.deleteAlbum(999L)).thenReturn(false);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("DELETE", "/api/albums/999");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        albumServlet.doDelete(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 404);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain error", responseContent.contains("\"success\":false"));
    }
    
    @Test
    public void testDoGet_FilterByArtist_Success() throws Exception {
        List<Album> albums = new ArrayList<Album>();
        albums.add(createValidAlbum());
        when(mockAlbumService.getAlbumsByArtistName("Test Artist")).thenReturn(albums);
        
        Map<String, String> params = ServletTestUtil.createParamMap("artist", "Test Artist");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithParams("GET", "/api/albums", params);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        albumServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain filtered results", responseContent.contains("Test Album"));
    }
    
    @Test
    public void testDoGet_FilterByGenre_Success() throws Exception {
        List<Album> albums = new ArrayList<Album>();
        albums.add(createValidAlbum());
        when(mockAlbumService.getAlbumsByGenre("Rock")).thenReturn(albums);
        
        Map<String, String> params = ServletTestUtil.createParamMap("genre", "Rock");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithParams("GET", "/api/albums", params);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        albumServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain filtered results", responseContent.contains("Test Album"));
    }
    
    private Album createValidAlbum() {
        Album album = new Album();
        album.setAlbumName("Test Album");
        album.setArtistId(1L);
        album.setArtistName("Test Artist");
        album.setReleaseDate(new Date());
        album.setGenre("Rock");
        album.setRecordLabel("Test Label");
        album.setCreatedDate(new Date());
        return album;
    }
    
    private JSONObject createValidAlbumJson() {
        JSONObject json = new JSONObject();
        json.put("albumName", "Test Album");
        json.put("artistId", 1);
        json.put("artistName", "Test Artist");
        json.put("releaseDate", new Date().toString());
        json.put("genre", "Rock");
        json.put("recordLabel", "Test Label");
        return json;
    }
}
