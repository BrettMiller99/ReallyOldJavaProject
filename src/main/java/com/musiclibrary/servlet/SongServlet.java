package com.musiclibrary.servlet;

import com.musiclibrary.model.Song;
import com.musiclibrary.service.SongService;
import com.musiclibrary.util.JsonUtil;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Song REST API Servlet
 * 
 * Provides HTTP endpoints for Song entity CRUD operations using traditional Java 7 servlet patterns.
 * This servlet demonstrates classic enterprise Java web development approaches commonly found
 * in legacy applications before Spring MVC and REST annotations became standard.
 * 
 * Supported Endpoints:
 * - GET /api/songs - List all songs (with optional pagination and search)
 * - GET /api/songs/{id} - Get song by ID
 * - POST /api/songs - Create new song
 * - PUT /api/songs/{id} - Update existing song
 * - DELETE /api/songs/{id} - Delete song by ID
 * 
 * Business Logic:
 * - Handles HTTP request/response processing for song management
 * - Validates request parameters and JSON payloads
 * - Delegates business operations to SongService layer
 * - Provides consistent JSON response format for API clients
 * - Implements proper HTTP status codes and error handling
 * - Supports pagination and search functionality
 * - Includes audit logging for API operations
 * 
 * Migration Opportunities:
 * - Traditional servlet -> Spring @RestController with @RequestMapping
 * - Manual HTTP method routing -> @GetMapping, @PostMapping, etc.
 * - Manual JSON parsing -> @RequestBody with Jackson deserialization
 * - Manual response building -> @ResponseBody with automatic serialization
 * - HttpServletRequest parsing -> Spring @PathVariable and @RequestParam
 * - Manual error handling -> @ExceptionHandler and @ControllerAdvice
 * - Manual service instantiation -> @Autowired dependency injection
 * - Traditional logging -> SLF4J with structured logging
 * - Manual validation -> Bean Validation with @Valid annotations
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class SongServlet extends HttpServlet {
    
    // Traditional Java logging - migration opportunity
    private static final Logger LOGGER = Logger.getLogger(SongServlet.class.getName());
    
    // Manual service instantiation - migration opportunity to dependency injection
    private SongService songService;
    
    // Configuration parameters from web.xml
    private int maxPageSize = 100;
    
    /**
     * Servlet initialization - traditional approach.
     * Migration opportunity: Spring @PostConstruct or application startup configuration.
     */
    @Override
    public void init() throws ServletException {
        super.init();
        
        // Initialize service layer manually
        this.songService = new SongService();
        
        // Read configuration from web.xml init parameters
        String maxPageSizeParam = getInitParameter("max.page.size");
        if (maxPageSizeParam != null) {
            try {
                this.maxPageSize = Integer.parseInt(maxPageSizeParam);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid max.page.size parameter: " + maxPageSizeParam, e);
            }
        }
        
        LOGGER.info("SongServlet initialized with maxPageSize: " + maxPageSize);
    }
    
    /**
     * Handles GET requests for song retrieval.
     * 
     * Supported patterns:
     * - GET /api/songs - List all songs (with optional pagination)
     * - GET /api/songs/{id} - Get specific song by ID
     * - GET /api/songs?search=query - Search songs
     * - GET /api/songs?page=0&size=10 - Paginated listing
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.info("GET request to SongServlet: " + request.getRequestURI());
        
        // Set response content type - manual approach
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Parse URL path to determine operation - manual routing
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // List songs with optional filtering and pagination
                handleGetAllSongs(request, response, out);
            } else {
                // Extract song ID from path
                String songIdStr = pathInfo.substring(1); // Remove leading slash
                try {
                    Long songId = Long.parseLong(songIdStr);
                    handleGetSongById(songId, response, out);
                } catch (NumberFormatException e) {
                    sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                        "Invalid song ID format: " + songIdStr);
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing GET request", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Internal server error: " + e.getMessage());
        } finally {
            out.close();
        }
    }
    
    /**
     * Handles POST requests for song creation.
     * 
     * Expected JSON payload:
     * {
     *   "songName": "Song Title",
     *   "albumName": "Album Name",
     *   "artistName": "Artist Name",
     *   "trackLength": 180,
     *   "dateReleased": "2023-01-01",
     *   ...
     * }
     * 
     * @param request HTTP request with JSON payload
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.info("POST request to SongServlet");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Parse JSON request body manually - migration opportunity
            String jsonString = readRequestBody(request);
            if (jsonString == null || jsonString.trim().isEmpty()) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Request body is required");
                return;
            }
            
            JSONObject jsonRequest = new JSONObject(jsonString);
            
            // Convert JSON to Song entity manually
            Song song = createSongFromJson(jsonRequest);
            
            // Delegate to service layer
            Song createdSong = songService.createSong(song);
            
            // Build success response
            JSONObject responseJson = JsonUtil.createSuccessResponse(
                JsonUtil.songToJson(createdSong), 
                "Song created successfully");
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(responseJson.toString());
            
            LOGGER.info("Successfully created song with ID: " + createdSong.getSongId());
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid song data", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating song", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to create song: " + e.getMessage());
        } finally {
            out.close();
        }
    }
    
    /**
     * Handles PUT requests for song updates.
     * 
     * Expected URL: PUT /api/songs/{id}
     * Expected JSON payload: Same as POST but with optional fields
     * 
     * @param request HTTP request with JSON payload
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.info("PUT request to SongServlet: " + request.getRequestURI());
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Extract song ID from path
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Song ID is required for updates");
                return;
            }
            
            String songIdStr = pathInfo.substring(1);
            Long songId;
            try {
                songId = Long.parseLong(songIdStr);
            } catch (NumberFormatException e) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Invalid song ID format: " + songIdStr);
                return;
            }
            
            // Parse JSON request body
            String jsonString = readRequestBody(request);
            if (jsonString == null || jsonString.trim().isEmpty()) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Request body is required");
                return;
            }
            
            JSONObject jsonRequest = new JSONObject(jsonString);
            
            // Convert JSON to Song entity
            Song song = createSongFromJson(jsonRequest);
            song.setSongId(songId); // Ensure ID matches path parameter
            
            // Delegate to service layer
            Song updatedSong = songService.updateSong(song);
            
            // Build success response
            JSONObject responseJson = JsonUtil.createSuccessResponse(
                JsonUtil.songToJson(updatedSong), 
                "Song updated successfully");
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(responseJson.toString());
            
            LOGGER.info("Successfully updated song with ID: " + songId);
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid song update data", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating song", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to update song: " + e.getMessage());
        } finally {
            out.close();
        }
    }
    
    /**
     * Handles DELETE requests for song removal.
     * 
     * Expected URL: DELETE /api/songs/{id}
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.info("DELETE request to SongServlet: " + request.getRequestURI());
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Extract song ID from path
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Song ID is required for deletion");
                return;
            }
            
            String songIdStr = pathInfo.substring(1);
            Long songId;
            try {
                songId = Long.parseLong(songIdStr);
            } catch (NumberFormatException e) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Invalid song ID format: " + songIdStr);
                return;
            }
            
            // Delegate to service layer
            boolean deleted = songService.deleteSong(songId);
            
            if (deleted) {
                JSONObject responseJson = JsonUtil.createSuccessResponse(null, 
                    "Song deleted successfully");
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(responseJson.toString());
                LOGGER.info("Successfully deleted song with ID: " + songId);
            } else {
                sendErrorResponse(response, out, HttpServletResponse.SC_NOT_FOUND, 
                    "Song not found with ID: " + songId);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting song", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to delete song: " + e.getMessage());
        } finally {
            out.close();
        }
    }
    
    /**
     * Handles listing all songs with optional filtering and pagination.
     */
    private void handleGetAllSongs(HttpServletRequest request, HttpServletResponse response, 
                                  PrintWriter out) throws IOException {
        
        // Parse query parameters manually - migration opportunity
        String searchQuery = request.getParameter("search");
        String pageParam = request.getParameter("page");
        String sizeParam = request.getParameter("size");
        String artistParam = request.getParameter("artist");
        String albumParam = request.getParameter("album");
        
        try {
            List<Song> songs;
            
            // Handle different query scenarios
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                // Search functionality
                songs = songService.searchSongs(searchQuery.trim());
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.songsToJsonArray(songs), 
                    "Search completed successfully");
                out.print(responseJson.toString());
                
            } else if (artistParam != null && !artistParam.trim().isEmpty()) {
                // Filter by artist
                songs = songService.getSongsByArtist(artistParam.trim());
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.songsToJsonArray(songs), 
                    "Songs by artist retrieved successfully");
                out.print(responseJson.toString());
                
            } else if (albumParam != null && !albumParam.trim().isEmpty()) {
                // Filter by album
                songs = songService.getSongsByAlbum(albumParam.trim());
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.songsToJsonArray(songs), 
                    "Songs by album retrieved successfully");
                out.print(responseJson.toString());
                
            } else if (pageParam != null || sizeParam != null) {
                // Paginated listing
                int page = 0;
                int size = 20; // Default page size
                
                if (pageParam != null) {
                    try {
                        page = Integer.parseInt(pageParam);
                        if (page < 0) page = 0;
                    } catch (NumberFormatException e) {
                        sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                            "Invalid page parameter: " + pageParam);
                        return;
                    }
                }
                
                if (sizeParam != null) {
                    try {
                        size = Integer.parseInt(sizeParam);
                        if (size <= 0) size = 20;
                        if (size > maxPageSize) size = maxPageSize;
                    } catch (NumberFormatException e) {
                        sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                            "Invalid size parameter: " + sizeParam);
                        return;
                    }
                }
                
                songs = songService.getSongsWithPagination(page, size);
                long totalCount = songService.getTotalSongCount();
                
                JSONObject responseJson = JsonUtil.createPaginatedResponse(
                    JsonUtil.songsToJsonArray(songs), page, size, totalCount);
                out.print(responseJson.toString());
                
            } else {
                // Get all songs (no pagination)
                songs = songService.getAllSongs();
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.songsToJsonArray(songs), 
                    "All songs retrieved successfully");
                out.print(responseJson.toString());
            }
            
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving songs", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to retrieve songs: " + e.getMessage());
        }
    }
    
    /**
     * Handles retrieving a specific song by ID.
     */
    private void handleGetSongById(Long songId, HttpServletResponse response, 
                                  PrintWriter out) throws IOException {
        try {
            Song song = songService.getSongById(songId);
            
            if (song != null) {
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.songToJson(song), 
                    "Song retrieved successfully");
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(responseJson.toString());
            } else {
                sendErrorResponse(response, out, HttpServletResponse.SC_NOT_FOUND, 
                    "Song not found with ID: " + songId);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving song by ID: " + songId, e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to retrieve song: " + e.getMessage());
        }
    }
    
    /**
     * Reads the request body as a string.
     * Manual approach - migration opportunity to Spring @RequestBody.
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        
        return buffer.toString();
    }
    
    /**
     * Creates Song entity from JSON object.
     * Manual mapping - migration opportunity to Jackson ObjectMapper.
     */
    private Song createSongFromJson(JSONObject json) {
        Song song = new Song();
        
        // Map basic fields
        song.setSongName(JsonUtil.getStringFromJson(json, "songName"));
        song.setAlbumName(JsonUtil.getStringFromJson(json, "albumName"));
        song.setArtistName(JsonUtil.getStringFromJson(json, "artistName"));
        song.setAlbumId(JsonUtil.getLongFromJson(json, "albumId"));
        song.setArtistId(JsonUtil.getLongFromJson(json, "artistId"));
        song.setTrackNumber(JsonUtil.getIntegerFromJson(json, "trackNumber"));
        song.setTrackLength(JsonUtil.getIntegerFromJson(json, "trackLength"));
        song.setGenre(JsonUtil.getStringFromJson(json, "genre"));
        song.setFilePath(JsonUtil.getStringFromJson(json, "filePath"));
        song.setFileSize(JsonUtil.getLongFromJson(json, "fileSize"));
        song.setBitrate(JsonUtil.getIntegerFromJson(json, "bitrate"));
        song.setRating(JsonUtil.getIntegerFromJson(json, "rating"));
        song.setPlayCount(JsonUtil.getIntegerFromJson(json, "playCount"));
        song.setLyrics(JsonUtil.getStringFromJson(json, "lyrics"));
        
        // Handle date field
        Date dateReleased = JsonUtil.getDateFromJson(json, "dateReleased");
        if (dateReleased != null) {
            song.setDateReleased(dateReleased);
        }
        
        return song;
    }
    
    /**
     * Sends standardized error response.
     * Manual approach - migration opportunity to @ExceptionHandler.
     */
    private void sendErrorResponse(HttpServletResponse response, PrintWriter out, 
                                  int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        JSONObject errorResponse = JsonUtil.createErrorResponse(message, String.valueOf(statusCode));
        out.print(errorResponse.toString());
    }
    
    /**
     * Servlet cleanup - traditional approach.
     * Migration opportunity: Spring @PreDestroy or application shutdown hooks.
     */
    @Override
    public void destroy() {
        LOGGER.info("SongServlet destroyed");
        super.destroy();
    }
}
