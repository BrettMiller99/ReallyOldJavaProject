package com.musiclibrary.servlet;

import com.musiclibrary.model.Album;
import com.musiclibrary.service.AlbumService;
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
 * Integration tests for AlbumServlet using traditional Java 7 testing patterns.
 * 
 * Testing Approach:
 * - Tests HTTP request/response processing for album management
 * - Validates JSON serialization and deserialization
 * - Tests all CRUD operations and query parameters
 * - Verifies proper HTTP status codes and error handling
 * - Mocks service layer to isolate servlet logic
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class AlbumServletTest extends ServletTestBase {
    
    @Mock
    private AlbumService mockAlbumService;
    
    private AlbumServlet albumServlet;
    
    @Before
    public void setUp() throws Exception {
        super.setUpServletBase();
        albumServlet = new AlbumServlet();
        
        albumServlet.init(mockServletConfig);
        
        setPrivateField(albumServlet, "albumService", mockAlbumService);
    }
    
    @Test
    public void testDoGet_GetAllAlbums_Success() throws Exception {
        List<Album> albums = new ArrayList<Album>();
        albums.add(createValidAlbum());
        
        when(mockAlbumService.getAllAlbums()).thenReturn(albums);
        
        albumServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockAlbumService, times(1)).getAllAlbums();
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertNotNull("Response should contain data", response.get("data"));
    }
    
    @Test
    public void testDoGet_GetAlbumById_Success() throws Exception {
        Album album = createValidAlbum();
        album.setAlbumId(1L);
        
        setupPathInfo("/1");
        when(mockAlbumService.getAlbumById(1L)).thenReturn(album);
        
        albumServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockAlbumService, times(1)).getAlbumById(1L);
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_SearchAlbums_Success() throws Exception {
        List<Album> albums = new ArrayList<Album>();
        albums.add(createValidAlbum());
        
        setupQueryParameter("search", "test");
        when(mockAlbumService.searchAlbums("test")).thenReturn(albums);
        
        albumServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockAlbumService, times(1)).searchAlbums("test");
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_FilterByArtist_Success() throws Exception {
        List<Album> albums = new ArrayList<Album>();
        albums.add(createValidAlbum());
        
        setupQueryParameter("artist", "Test Artist");
        when(mockAlbumService.getAlbumsByArtistName("Test Artist")).thenReturn(albums);
        
        albumServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockAlbumService, times(1)).getAlbumsByArtistName("Test Artist");
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_FilterByGenre_Success() throws Exception {
        List<Album> albums = new ArrayList<Album>();
        albums.add(createValidAlbum());
        
        setupQueryParameter("genre", "Rock");
        when(mockAlbumService.getAlbumsByGenre("Rock")).thenReturn(albums);
        
        albumServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockAlbumService, times(1)).getAlbumsByGenre("Rock");
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_FilterByYear_Success() throws Exception {
        List<Album> albums = new ArrayList<Album>();
        albums.add(createValidAlbum());
        
        setupQueryParameter("year", "2023");
        when(mockAlbumService.getAlbumsByYear(2023)).thenReturn(albums);
        
        albumServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockAlbumService, times(1)).getAlbumsByYear(2023);
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_Pagination_Success() throws Exception {
        List<Album> albums = new ArrayList<Album>();
        albums.add(createValidAlbum());
        
        setupQueryParameter("page", "0");
        setupQueryParameter("size", "10");
        when(mockAlbumService.getAlbumsWithPagination(0, 10)).thenReturn(albums);
        when(mockAlbumService.getTotalAlbumCount()).thenReturn(1L);
        
        albumServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockAlbumService, times(1)).getAlbumsWithPagination(0, 10);
        verify(mockAlbumService, times(1)).getTotalAlbumCount();
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertNotNull("Response should contain pagination", response.get("pagination"));
    }
    
    @Test
    public void testDoPost_CreateAlbum_Success() throws Exception {
        Album createdAlbum = createValidAlbum();
        createdAlbum.setAlbumId(1L);
        
        setupJsonRequest(createValidAlbumJson());
        when(mockAlbumService.createAlbum(any(Album.class))).thenReturn(createdAlbum);
        
        albumServlet.doPost(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_CREATED);
        verify(mockAlbumService, times(1)).createAlbum(any(Album.class));
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertTrue("Message should indicate creation", 
            response.getString("message").contains("created successfully"));
    }
    
    @Test
    public void testDoPost_InvalidAlbumData_BadRequest() throws Exception {
        setupJsonRequest(createValidAlbumJson());
        when(mockAlbumService.createAlbum(any(Album.class)))
            .thenThrow(new IllegalArgumentException("Album name is required"));
        
        albumServlet.doPost(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        
        JSONObject response = getResponseJson();
        assertFalse("Response should indicate failure", response.getBoolean("success"));
        assertTrue("Error should contain validation message", 
            response.getString("error").contains("Album name is required"));
    }
    
    @Test
    public void testDoPut_UpdateAlbum_Success() throws Exception {
        Album updatedAlbum = createValidAlbum();
        updatedAlbum.setAlbumId(1L);
        
        setupPathInfo("/1");
        setupJsonRequest(createValidAlbumJson());
        when(mockAlbumService.updateAlbum(any(Album.class))).thenReturn(updatedAlbum);
        
        albumServlet.doPut(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockAlbumService, times(1)).updateAlbum(any(Album.class));
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertTrue("Message should indicate update", 
            response.getString("message").contains("updated successfully"));
    }
    
    @Test
    public void testDoDelete_DeleteAlbum_Success() throws Exception {
        setupPathInfo("/1");
        when(mockAlbumService.deleteAlbum(1L)).thenReturn(true);
        
        albumServlet.doDelete(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockAlbumService, times(1)).deleteAlbum(1L);
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertTrue("Message should indicate deletion", 
            response.getString("message").contains("deleted successfully"));
    }
    
    @Test
    public void testDoDelete_AlbumNotFound_NotFound() throws Exception {
        setupPathInfo("/999");
        when(mockAlbumService.deleteAlbum(999L)).thenReturn(false);
        
        albumServlet.doDelete(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(mockAlbumService, times(1)).deleteAlbum(999L);
        
        JSONObject response = getResponseJson();
        assertFalse("Response should indicate failure", response.getBoolean("success"));
        assertTrue("Error should mention not found", 
            response.getString("error").contains("Album not found"));
    }
    
    private Album createValidAlbum() {
        Album album = new Album();
        album.setAlbumName("Test Album");
        album.setArtistId(1L);
        album.setArtistName("Test Artist");
        album.setReleaseDate(new Date());
        album.setGenre("Rock");
        album.setRecordLabel("Test Records");
        album.setTotalTracks(10);
        album.setCreatedDate(new Date());
        return album;
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
