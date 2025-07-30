package com.musiclibrary.servlet;

import org.json.JSONObject;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;

import static org.mockito.Mockito.*;

/**
 * Base class for servlet integration tests providing common mock utilities.
 * 
 * Testing Approach:
 * - Provides mock HTTP request/response objects for servlet testing
 * - Includes helper methods for JSON request body creation and response parsing
 * - Follows existing service test patterns with JUnit 4 and Mockito
 * - Centralizes common servlet testing utilities
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public abstract class ServletTestBase {
    
    @Mock
    protected HttpServletRequest mockRequest;
    
    @Mock
    protected HttpServletResponse mockResponse;
    
    @Mock
    protected ServletConfig mockServletConfig;
    
    protected StringWriter responseWriter;
    protected PrintWriter printWriter;
    
    @Before
    public void setUpServletBase() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        
        when(mockResponse.getWriter()).thenReturn(printWriter);
        
        when(mockServletConfig.getInitParameter("max.page.size")).thenReturn("100");
        when(mockServletConfig.getInitParameter(anyString())).thenReturn(null);
    }
    
    /**
     * Creates mock request with JSON body.
     */
    protected void setupJsonRequest(String jsonBody) throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(jsonBody));
        when(mockRequest.getReader()).thenReturn(reader);
    }
    
    /**
     * Creates mock request with path info.
     */
    protected void setupPathInfo(String pathInfo) {
        when(mockRequest.getPathInfo()).thenReturn(pathInfo);
    }
    
    /**
     * Creates mock request with query parameters.
     */
    protected void setupQueryParameter(String name, String value) {
        when(mockRequest.getParameter(name)).thenReturn(value);
    }
    
    /**
     * Gets response content as string.
     */
    protected String getResponseContent() {
        printWriter.flush();
        return responseWriter.toString();
    }
    
    /**
     * Parses response content as JSON.
     */
    protected JSONObject getResponseJson() {
        String content = getResponseContent();
        return new JSONObject(content);
    }
    
    /**
     * Creates valid artist JSON for testing.
     */
    protected String createValidArtistJson() {
        JSONObject json = new JSONObject();
        json.put("artistName", "Test Artist");
        json.put("biography", "Test biography");
        json.put("country", "USA");
        json.put("formedYear", 1990);
        json.put("website", "http://www.testartist.com");
        return json.toString();
    }
    
    /**
     * Creates valid album JSON for testing.
     */
    protected String createValidAlbumJson() {
        JSONObject json = new JSONObject();
        json.put("albumName", "Test Album");
        json.put("artistId", 1L);
        json.put("artistName", "Test Artist");
        json.put("releaseDate", "2023-01-01");
        json.put("genre", "Rock");
        json.put("recordLabel", "Test Records");
        json.put("totalTracks", 10);
        return json.toString();
    }
    
    /**
     * Creates valid song JSON for testing.
     */
    protected String createValidSongJson() {
        JSONObject json = new JSONObject();
        json.put("songName", "Test Song");
        json.put("albumName", "Test Album");
        json.put("artistName", "Test Artist");
        json.put("albumId", 1L);
        json.put("artistId", 1L);
        json.put("trackNumber", 1);
        json.put("trackLength", 180);
        json.put("genre", "Rock");
        json.put("rating", 4);
        json.put("playCount", 0);
        return json.toString();
    }
    
    /**
     * Creates valid playlist JSON for testing.
     */
    protected String createValidPlaylistJson() {
        JSONObject json = new JSONObject();
        json.put("playlistName", "Test Playlist");
        json.put("description", "Test playlist description");
        json.put("createdBy", "testuser");
        json.put("isPublic", true);
        json.put("totalDuration", 0);
        json.put("songCount", 0);
        return json.toString();
    }
}
