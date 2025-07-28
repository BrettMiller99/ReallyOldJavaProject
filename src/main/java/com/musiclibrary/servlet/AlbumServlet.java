package com.musiclibrary.servlet;

import com.musiclibrary.model.Album;
import com.musiclibrary.service.AlbumService;
import com.musiclibrary.util.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Album REST API Servlet
 * 
 * Provides HTTP endpoints for Album entity CRUD operations using traditional Java 7 servlet patterns.
 * Demonstrates classic enterprise Java web development for album management functionality.
 * 
 * Supported Endpoints:
 * - GET /api/albums - List all albums (with optional pagination and search)
 * - GET /api/albums/{id} - Get album by ID
 * - POST /api/albums - Create new album
 * - PUT /api/albums/{id} - Update existing album
 * - DELETE /api/albums/{id} - Delete album by ID
 * 
 * Business Logic:
 * - Handles HTTP request/response processing for album management
 * - Validates request parameters and JSON payloads for album data
 * - Delegates business operations to AlbumService layer
 * - Provides consistent JSON response format for API clients
 * - Implements proper HTTP status codes and error handling
 * - Supports pagination, search, and filtering functionality
 * - Includes audit logging for album API operations
 * - Enforces business rules for album-artist relationships and validation
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
public class AlbumServlet extends HttpServlet {
    
    // Traditional Java logging - migration opportunity
    private static final Logger LOGGER = Logger.getLogger(AlbumServlet.class.getName());
    
    // Manual service instantiation - migration opportunity to dependency injection
    private AlbumService albumService;
    
    // Configuration parameters from web.xml
    private int maxPageSize = 50;
    
    /**
     * Servlet initialization - traditional approach.
     * Migration opportunity: Spring @PostConstruct or application startup configuration.
     */
    @Override
    public void init() throws ServletException {
        super.init();
        
        // Initialize service layer manually
        this.albumService = new AlbumService();
        
        // Read configuration from web.xml init parameters
        String maxPageSizeParam = getInitParameter("max.page.size");
        if (maxPageSizeParam != null) {
            try {
                this.maxPageSize = Integer.parseInt(maxPageSizeParam);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid max.page.size parameter: " + maxPageSizeParam, e);
            }
        }
        
        LOGGER.info("AlbumServlet initialized with maxPageSize: " + maxPageSize);
    }
    
    /**
     * Handles GET requests for album retrieval.
     * 
     * Supported patterns:
     * - GET /api/albums - List all albums (with optional pagination)
     * - GET /api/albums/{id} - Get specific album by ID
     * - GET /api/albums?search=query - Search albums
     * - GET /api/albums?page=0&size=10 - Paginated listing
     * - GET /api/albums?artist=artistName - Filter by artist
     * - GET /api/albums?genre=genre - Filter by genre
     * - GET /api/albums?year=2023 - Filter by release year
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.info("GET request to AlbumServlet: " + request.getRequestURI());
        
        // Set response content type - manual approach
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Parse URL path to determine operation - manual routing
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // List albums with optional filtering and pagination
                handleGetAllAlbums(request, response, out);
            } else {
                // Extract album ID from path
                String albumIdStr = pathInfo.substring(1); // Remove leading slash
                try {
                    Long albumId = Long.parseLong(albumIdStr);
                    handleGetAlbumById(albumId, response, out);
                } catch (NumberFormatException e) {
                    sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                        "Invalid album ID format: " + albumIdStr);
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
     * Handles POST requests for album creation.
     * 
     * Expected JSON payload:
     * {
     *   "albumName": "Album Title",
     *   "artistId": 1,
     *   "artistName": "Artist Name",
     *   "releaseDate": "2023-01-01",
     *   "genre": "Rock",
     *   "recordLabel": "Record Label",
     *   "totalTracks": 12,
     *   "albumArtPath": "/path/to/art.jpg"
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
        
        LOGGER.info("POST request to AlbumServlet");
        
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
            
            // Convert JSON to Album entity manually
            Album album = createAlbumFromJson(jsonRequest);
            
            // Delegate to service layer
            Album createdAlbum = albumService.createAlbum(album);
            
            // Build success response
            JSONObject responseJson = JsonUtil.createSuccessResponse(
                JsonUtil.albumToJson(createdAlbum), 
                "Album created successfully");
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(responseJson.toString());
            
            LOGGER.info("Successfully created album with ID: " + createdAlbum.getAlbumId());
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid album data", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating album", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to create album: " + e.getMessage());
        } finally {
            out.close();
        }
    }
    
    /**
     * Handles PUT requests for album updates.
     * 
     * Expected URL: PUT /api/albums/{id}
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
        
        LOGGER.info("PUT request to AlbumServlet: " + request.getRequestURI());
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Extract album ID from path
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Album ID is required for updates");
                return;
            }
            
            String albumIdStr = pathInfo.substring(1);
            Long albumId;
            try {
                albumId = Long.parseLong(albumIdStr);
            } catch (NumberFormatException e) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Invalid album ID format: " + albumIdStr);
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
            
            // Convert JSON to Album entity
            Album album = createAlbumFromJson(jsonRequest);
            album.setAlbumId(albumId); // Ensure ID matches path parameter
            
            // Delegate to service layer
            Album updatedAlbum = albumService.updateAlbum(album);
            
            // Build success response
            JSONObject responseJson = JsonUtil.createSuccessResponse(
                JsonUtil.albumToJson(updatedAlbum), 
                "Album updated successfully");
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(responseJson.toString());
            
            LOGGER.info("Successfully updated album with ID: " + albumId);
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid album update data", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating album", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to update album: " + e.getMessage());
        } finally {
            out.close();
        }
    }
    
    /**
     * Handles DELETE requests for album removal.
     * 
     * Expected URL: DELETE /api/albums/{id}
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.info("DELETE request to AlbumServlet: " + request.getRequestURI());
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Extract album ID from path
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Album ID is required for deletion");
                return;
            }
            
            String albumIdStr = pathInfo.substring(1);
            Long albumId;
            try {
                albumId = Long.parseLong(albumIdStr);
            } catch (NumberFormatException e) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Invalid album ID format: " + albumIdStr);
                return;
            }
            
            // Delegate to service layer
            boolean deleted = albumService.deleteAlbum(albumId);
            
            if (deleted) {
                JSONObject responseJson = JsonUtil.createSuccessResponse(null, 
                    "Album deleted successfully");
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(responseJson.toString());
                LOGGER.info("Successfully deleted album with ID: " + albumId);
            } else {
                sendErrorResponse(response, out, HttpServletResponse.SC_NOT_FOUND, 
                    "Album not found with ID: " + albumId);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting album", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to delete album: " + e.getMessage());
        } finally {
            out.close();
        }
    }
    
    /**
     * Handles listing all albums with optional filtering and pagination.
     */
    private void handleGetAllAlbums(HttpServletRequest request, HttpServletResponse response, 
                                   PrintWriter out) throws IOException {
        
        // Parse query parameters manually - migration opportunity
        String searchQuery = request.getParameter("search");
        String pageParam = request.getParameter("page");
        String sizeParam = request.getParameter("size");
        String artistParam = request.getParameter("artist");
        String genreParam = request.getParameter("genre");
        String yearParam = request.getParameter("year");
        
        try {
            List<Album> albums;
            
            // Handle different query scenarios
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                // Search functionality
                albums = albumService.searchAlbums(searchQuery.trim());
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.albumsToJsonArray(albums), 
                    "Album search completed successfully");
                out.print(responseJson.toString());
                
            } else if (artistParam != null && !artistParam.trim().isEmpty()) {
                // Filter by artist
                albums = albumService.getAlbumsByArtistName(artistParam.trim());
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.albumsToJsonArray(albums), 
                    "Albums by artist retrieved successfully");
                out.print(responseJson.toString());
                
            } else if (genreParam != null && !genreParam.trim().isEmpty()) {
                // Filter by genre
                albums = albumService.getAlbumsByGenre(genreParam.trim());
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.albumsToJsonArray(albums), 
                    "Albums by genre retrieved successfully");
                out.print(responseJson.toString());
                
            } else if (yearParam != null && !yearParam.trim().isEmpty()) {
                // Filter by release year
                try {
                    int year = Integer.parseInt(yearParam.trim());
                    albums = albumService.getAlbumsByYear(year);
                    JSONObject responseJson = JsonUtil.createSuccessResponse(
                        JsonUtil.albumsToJsonArray(albums), 
                        "Albums by year retrieved successfully");
                    out.print(responseJson.toString());
                } catch (NumberFormatException e) {
                    sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                        "Invalid year parameter: " + yearParam);
                    return;
                }
                
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
                
                albums = albumService.getAlbumsWithPagination(page, size);
                long totalCount = albumService.getTotalAlbumCount();
                
                JSONObject responseJson = JsonUtil.createPaginatedResponse(
                    JsonUtil.albumsToJsonArray(albums), page, size, totalCount);
                out.print(responseJson.toString());
                
            } else {
                // Get all albums (no pagination)
                albums = albumService.getAllAlbums();
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.albumsToJsonArray(albums), 
                    "All albums retrieved successfully");
                out.print(responseJson.toString());
            }
            
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving albums", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to retrieve albums: " + e.getMessage());
        }
    }
    
    /**
     * Handles retrieving a specific album by ID.
     */
    private void handleGetAlbumById(Long albumId, HttpServletResponse response, 
                                   PrintWriter out) throws IOException {
        try {
            Album album = albumService.getAlbumById(albumId);
            
            if (album != null) {
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.albumToJson(album), 
                    "Album retrieved successfully");
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(responseJson.toString());
            } else {
                sendErrorResponse(response, out, HttpServletResponse.SC_NOT_FOUND, 
                    "Album not found with ID: " + albumId);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving album by ID: " + albumId, e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to retrieve album: " + e.getMessage());
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
     * Creates Album entity from JSON object.
     * Manual mapping - migration opportunity to Jackson ObjectMapper.
     */
    private Album createAlbumFromJson(JSONObject json) {
        Album album = new Album();
        
        // Map basic fields
        album.setAlbumName(JsonUtil.getStringFromJson(json, "albumName"));
        album.setArtistId(JsonUtil.getLongFromJson(json, "artistId"));
        album.setArtistName(JsonUtil.getStringFromJson(json, "artistName"));
        album.setGenre(JsonUtil.getStringFromJson(json, "genre"));
        album.setRecordLabel(JsonUtil.getStringFromJson(json, "recordLabel"));
        album.setTotalTracks(JsonUtil.getIntegerFromJson(json, "totalTracks"));
        album.setAlbumArtPath(JsonUtil.getStringFromJson(json, "albumArtPath"));
        
        // Handle date field
        Date releaseDate = JsonUtil.getDateFromJson(json, "releaseDate");
        if (releaseDate != null) {
            album.setReleaseDate(releaseDate);
        }
        
        return album;
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
        LOGGER.info("AlbumServlet destroyed");
        super.destroy();
    }
}
