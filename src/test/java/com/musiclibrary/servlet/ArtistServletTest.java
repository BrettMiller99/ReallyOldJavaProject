package com.musiclibrary.servlet;

import com.musiclibrary.model.Artist;
import com.musiclibrary.service.ArtistService;
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
 * Integration tests for ArtistServlet using traditional Java 7 testing patterns.
 * 
 * Testing Approach:
 * - Tests HTTP request/response processing for artist management
 * - Validates JSON serialization and deserialization
 * - Tests all CRUD operations and query parameters
 * - Verifies proper HTTP status codes and error handling
 * - Mocks service layer to isolate servlet logic
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class ArtistServletTest extends ServletTestBase {
    
    @Mock
    private ArtistService mockArtistService;
    
    private ArtistServlet artistServlet;
    
    @Before
    public void setUp() throws Exception {
        super.setUpServletBase();
        artistServlet = new ArtistServlet();
        
        artistServlet.init(mockServletConfig);
        
        setPrivateField(artistServlet, "artistService", mockArtistService);
    }
    
    @Test
    public void testDoGet_GetAllArtists_Success() throws Exception {
        List<Artist> artists = new ArrayList<Artist>();
        artists.add(createValidArtist());
        
        when(mockArtistService.getAllArtists()).thenReturn(artists);
        
        artistServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockArtistService, times(1)).getAllArtists();
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertNotNull("Response should contain data", response.get("data"));
    }
    
    @Test
    public void testDoGet_GetArtistById_Success() throws Exception {
        Artist artist = createValidArtist();
        artist.setArtistId(1L);
        
        setupPathInfo("/1");
        when(mockArtistService.getArtistById(1L)).thenReturn(artist);
        
        artistServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockArtistService, times(1)).getArtistById(1L);
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_GetArtistById_NotFound() throws Exception {
        setupPathInfo("/999");
        when(mockArtistService.getArtistById(999L)).thenReturn(null);
        
        artistServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(mockArtistService, times(1)).getArtistById(999L);
        
        JSONObject response = getResponseJson();
        assertFalse("Response should indicate failure", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_InvalidArtistId_BadRequest() throws Exception {
        setupPathInfo("/invalid");
        
        artistServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        
        JSONObject response = getResponseJson();
        assertFalse("Response should indicate failure", response.getBoolean("success"));
        assertTrue("Error message should mention invalid ID", 
            response.getString("error").contains("Invalid artist ID format"));
    }
    
    @Test
    public void testDoGet_SearchArtists_Success() throws Exception {
        List<Artist> artists = new ArrayList<Artist>();
        artists.add(createValidArtist());
        
        setupQueryParameter("search", "test");
        when(mockArtistService.searchArtists("test")).thenReturn(artists);
        
        artistServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockArtistService, times(1)).searchArtists("test");
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_FilterByCountry_Success() throws Exception {
        List<Artist> artists = new ArrayList<Artist>();
        artists.add(createValidArtist());
        
        setupQueryParameter("country", "USA");
        when(mockArtistService.getArtistsByCountry("USA")).thenReturn(artists);
        
        artistServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockArtistService, times(1)).getArtistsByCountry("USA");
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
    }
    
    @Test
    public void testDoGet_Pagination_Success() throws Exception {
        List<Artist> artists = new ArrayList<Artist>();
        artists.add(createValidArtist());
        
        setupQueryParameter("page", "0");
        setupQueryParameter("size", "10");
        when(mockArtistService.getArtistsWithPagination(0, 10)).thenReturn(artists);
        when(mockArtistService.getTotalArtistCount()).thenReturn(1L);
        
        artistServlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockArtistService, times(1)).getArtistsWithPagination(0, 10);
        verify(mockArtistService, times(1)).getTotalArtistCount();
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertNotNull("Response should contain pagination", response.get("pagination"));
    }
    
    @Test
    public void testDoPost_CreateArtist_Success() throws Exception {
        Artist inputArtist = createValidArtist();
        Artist createdArtist = createValidArtist();
        createdArtist.setArtistId(1L);
        
        setupJsonRequest(createValidArtistJson());
        when(mockArtistService.createArtist(any(Artist.class))).thenReturn(createdArtist);
        
        artistServlet.doPost(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_CREATED);
        verify(mockArtistService, times(1)).createArtist(any(Artist.class));
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertTrue("Message should indicate creation", 
            response.getString("message").contains("created successfully"));
    }
    
    @Test
    public void testDoPost_EmptyRequestBody_BadRequest() throws Exception {
        setupJsonRequest("");
        
        artistServlet.doPost(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        
        JSONObject response = getResponseJson();
        assertFalse("Response should indicate failure", response.getBoolean("success"));
        assertTrue("Error should mention required body", 
            response.getString("error").contains("Request body is required"));
    }
    
    @Test
    public void testDoPost_InvalidArtistData_BadRequest() throws Exception {
        setupJsonRequest(createValidArtistJson());
        when(mockArtistService.createArtist(any(Artist.class)))
            .thenThrow(new IllegalArgumentException("Artist name is required"));
        
        artistServlet.doPost(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        
        JSONObject response = getResponseJson();
        assertFalse("Response should indicate failure", response.getBoolean("success"));
        assertTrue("Error should contain validation message", 
            response.getString("error").contains("Artist name is required"));
    }
    
    @Test
    public void testDoPut_UpdateArtist_Success() throws Exception {
        Artist updatedArtist = createValidArtist();
        updatedArtist.setArtistId(1L);
        
        setupPathInfo("/1");
        setupJsonRequest(createValidArtistJson());
        when(mockArtistService.updateArtist(any(Artist.class))).thenReturn(updatedArtist);
        
        artistServlet.doPut(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockArtistService, times(1)).updateArtist(any(Artist.class));
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertTrue("Message should indicate update", 
            response.getString("message").contains("updated successfully"));
    }
    
    @Test
    public void testDoPut_MissingArtistId_BadRequest() throws Exception {
        setupJsonRequest(createValidArtistJson());
        
        artistServlet.doPut(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        
        JSONObject response = getResponseJson();
        assertFalse("Response should indicate failure", response.getBoolean("success"));
        assertTrue("Error should mention required ID", 
            response.getString("error").contains("Artist ID is required"));
    }
    
    @Test
    public void testDoDelete_DeleteArtist_Success() throws Exception {
        setupPathInfo("/1");
        when(mockArtistService.deleteArtist(1L)).thenReturn(true);
        
        artistServlet.doDelete(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        verify(mockArtistService, times(1)).deleteArtist(1L);
        
        JSONObject response = getResponseJson();
        assertTrue("Response should be successful", response.getBoolean("success"));
        assertTrue("Message should indicate deletion", 
            response.getString("message").contains("deleted successfully"));
    }
    
    @Test
    public void testDoDelete_ArtistNotFound_NotFound() throws Exception {
        setupPathInfo("/999");
        when(mockArtistService.deleteArtist(999L)).thenReturn(false);
        
        artistServlet.doDelete(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(mockArtistService, times(1)).deleteArtist(999L);
        
        JSONObject response = getResponseJson();
        assertFalse("Response should indicate failure", response.getBoolean("success"));
        assertTrue("Error should mention not found", 
            response.getString("error").contains("Artist not found"));
    }
    
    private Artist createValidArtist() {
        Artist artist = new Artist();
        artist.setArtistName("Test Artist");
        artist.setBiography("Test biography");
        artist.setCountry("USA");
        artist.setFormedYear(1990);
        artist.setWebsite("http://www.testartist.com");
        artist.setCreatedDate(new Date());
        return artist;
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
