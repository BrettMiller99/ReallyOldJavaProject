package com.musiclibrary.servlet;

import com.musiclibrary.model.Artist;
import com.musiclibrary.service.ArtistService;
import com.musiclibrary.util.ServletTestUtil;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ArtistServletTest {
    
    @Mock
    private ArtistService mockArtistService;
    
    private ArtistServlet artistServlet;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        artistServlet = new ArtistServlet();
        
        java.lang.reflect.Field serviceField = ArtistServlet.class.getDeclaredField("artistService");
        serviceField.setAccessible(true);
        serviceField.set(artistServlet, mockArtistService);
    }
    
    @Test
    public void testDoGet_GetAllArtists_Success() throws Exception {
        List<Artist> artists = new ArrayList<Artist>();
        artists.add(createValidArtist());
        when(mockArtistService.getAllArtists()).thenReturn(artists);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("GET", "/api/artists");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        artistServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        ServletTestUtil.verifyContentType(response, "application/json");
        
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertNotNull("Response content should not be null", responseContent);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
    }
    
    @Test
    public void testDoGet_GetArtistById_Success() throws Exception {
        Artist artist = createValidArtist();
        artist.setArtistId(1L);
        when(mockArtistService.getArtistById(1L)).thenReturn(artist);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("GET", "/api/artists/1");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        artistServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain artist data", responseContent.contains("Test Artist"));
    }
    
    @Test
    public void testDoGet_GetArtistById_NotFound() throws Exception {
        when(mockArtistService.getArtistById(999L)).thenReturn(null);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("GET", "/api/artists/999");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        artistServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 404);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain error", responseContent.contains("\"success\":false"));
    }
    
    @Test
    public void testDoGet_WithPagination_Success() throws Exception {
        List<Artist> artists = new ArrayList<Artist>();
        artists.add(createValidArtist());
        when(mockArtistService.getArtistsWithPagination(0, 10)).thenReturn(artists);
        when(mockArtistService.getTotalArtistCount()).thenReturn(1L);
        
        Map<String, String> params = ServletTestUtil.createParamMap("page", "0", "size", "10");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithParams("GET", "/api/artists", params);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        artistServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain pagination data", responseContent.contains("\"totalElements\":1"));
    }
    
    @Test
    public void testDoPost_CreateArtist_Success() throws Exception {
        Artist inputArtist = createValidArtist();
        Artist createdArtist = createValidArtist();
        createdArtist.setArtistId(1L);
        when(mockArtistService.createArtist(any(Artist.class))).thenReturn(createdArtist);
        
        JSONObject artistJson = createValidArtistJson();
        HttpServletRequest request = ServletTestUtil.createMockRequestWithJson("POST", "/api/artists", artistJson);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        artistServlet.doPost(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 201);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
        assertTrue("Response should contain artist ID", responseContent.contains("\"artistId\":1"));
    }
    
    @Test
    public void testDoPost_CreateArtist_InvalidData() throws Exception {
        when(mockArtistService.createArtist(any(Artist.class)))
            .thenThrow(new IllegalArgumentException("Artist name is required"));
        
        JSONObject invalidJson = new JSONObject();
        invalidJson.put("artistName", "");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithJson("POST", "/api/artists", invalidJson);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        artistServlet.doPost(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 400);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain error", responseContent.contains("\"success\":false"));
    }
    
    @Test
    public void testDoPut_UpdateArtist_Success() throws Exception {
        Artist updatedArtist = createValidArtist();
        updatedArtist.setArtistId(1L);
        when(mockArtistService.updateArtist(any(Artist.class))).thenReturn(updatedArtist);
        
        JSONObject artistJson = createValidArtistJson();
        artistJson.put("artistId", 1);
        HttpServletRequest request = ServletTestUtil.createMockRequestWithJson("PUT", "/api/artists/1", artistJson);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        artistServlet.doPut(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
    }
    
    @Test
    public void testDoDelete_DeleteArtist_Success() throws Exception {
        when(mockArtistService.deleteArtist(1L)).thenReturn(true);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("DELETE", "/api/artists/1");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        artistServlet.doDelete(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain success", responseContent.contains("\"success\":true"));
    }
    
    @Test
    public void testDoDelete_DeleteArtist_NotFound() throws Exception {
        when(mockArtistService.deleteArtist(999L)).thenReturn(false);
        
        HttpServletRequest request = ServletTestUtil.createMockRequest("DELETE", "/api/artists/999");
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        artistServlet.doDelete(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 404);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain error", responseContent.contains("\"success\":false"));
    }
    
    @Test
    public void testDoGet_SearchArtists_Success() throws Exception {
        List<Artist> artists = new ArrayList<Artist>();
        artists.add(createValidArtist());
        when(mockArtistService.searchArtists("test")).thenReturn(artists);
        
        Map<String, String> params = ServletTestUtil.createParamMap("search", "test");
        HttpServletRequest request = ServletTestUtil.createMockRequestWithParams("GET", "/api/artists", params);
        HttpServletResponse response = ServletTestUtil.createMockResponse();
        
        artistServlet.doGet(request, response);
        
        ServletTestUtil.verifyStatusCode(response, 200);
        String responseContent = ServletTestUtil.getResponseContent(response);
        assertTrue("Response should contain search results", responseContent.contains("Test Artist"));
    }
    
    private Artist createValidArtist() {
        Artist artist = new Artist();
        artist.setArtistName("Test Artist");
        artist.setFormedYear(1990);
        artist.setCountry("USA");
        artist.setBiography("Test biography");
        artist.setWebsite("http://www.testartist.com");
        artist.setCreatedDate(new Date());
        return artist;
    }
    
    private JSONObject createValidArtistJson() {
        JSONObject json = new JSONObject();
        json.put("artistName", "Test Artist");
        json.put("formedYear", 1990);
        json.put("country", "USA");
        json.put("biography", "Test biography");
        json.put("website", "http://www.testartist.com");
        return json;
    }
}
