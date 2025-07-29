package com.musiclibrary.servlet;

import com.musiclibrary.model.Playlist;
import com.musiclibrary.service.PlaylistService;
import com.musiclibrary.util.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Playlist REST API Servlet
 * 
 * Provides HTTP endpoints for Playlist entity CRUD operations using traditional Java 7 servlet patterns.
 * Demonstrates classic enterprise Java web development for playlist management functionality.
 * 
 * Supported Endpoints:
 * - GET /api/playlists - List all playlists (with optional pagination and search)
 * - GET /api/playlists/{id} - Get playlist by ID
 * - POST /api/playlists - Create new playlist
 * - PUT /api/playlists/{id} - Update existing playlist
 * - DELETE /api/playlists/{id} - Delete playlist by ID
 * 
 * Business Logic:
 * - Handles HTTP request/response processing for playlist management
 * - Validates request parameters and JSON payloads for playlist data
 * - Delegates business operations to PlaylistService layer
 * - Provides consistent JSON response format for API clients
 * - Implements proper HTTP status codes and error handling
 * - Supports pagination, search, and filtering functionality
 * - Includes audit logging for playlist API operations
 * - Enforces business rules for playlist ownership and visibility
 * - Manages playlist-song relationships through service layer
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
public class PlaylistServlet extends HttpServlet {
    
    // Traditional Java logging - migration opportunity
    private static final Logger LOGGER = Logger.getLogger(PlaylistServlet.class.getName());
    
    // Manual service instantiation - migration opportunity to dependency injection
    private PlaylistService playlistService;
    
    // Configuration parameters from web.xml
    private int maxPageSize = 25;
    
    /**
     * Servlet initialization - traditional approach.
     * Migration opportunity: Spring @PostConstruct or application startup configuration.
     */
    @Override
    public void init() throws ServletException {
        super.init();
        
        // Initialize service layer manually
        this.playlistService = new PlaylistService();
        
        // Read configuration from web.xml init parameters
        String maxPageSizeParam = getInitParameter("max.page.size");
        if (maxPageSizeParam != null) {
            try {
                this.maxPageSize = Integer.parseInt(maxPageSizeParam);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid max.page.size parameter: " + maxPageSizeParam, e);
            }
        }
        
        LOGGER.info("PlaylistServlet initialized with maxPageSize: " + maxPageSize);
    }
    
    /**
     * Handles GET requests for playlist retrieval.
     * 
     * Supported patterns:
     * - GET /api/playlists - List all playlists (with optional pagination)
     * - GET /api/playlists/{id} - Get specific playlist by ID
     * - GET /api/playlists?search=query - Search playlists
     * - GET /api/playlists?page=0&size=10 - Paginated listing
     * - GET /api/playlists?user=username - Filter by user/creator
     * - GET /api/playlists?public=true - Filter by visibility (public playlists only)
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.info("GET request to PlaylistServlet: " + request.getRequestURI());
        
        // Set response content type - manual approach
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Parse URL path to determine operation - manual routing
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // List playlists with optional filtering and pagination
                handleGetAllPlaylists(request, response, out);
            } else {
                // Extract playlist ID from path
                String playlistIdStr = pathInfo.substring(1); // Remove leading slash
                try {
                    Long playlistId = Long.parseLong(playlistIdStr);
                    handleGetPlaylistById(playlistId, response, out);
                } catch (NumberFormatException e) {
                    sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                        "Invalid playlist ID format: " + playlistIdStr);
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
     * Handles POST requests for playlist creation.
     * 
     * Expected JSON payload:
     * {
     *   "playlistName": "My Playlist",
     *   "description": "Description of the playlist",
     *   "createdBy": "username",
     *   "isPublic": true
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
        
        LOGGER.info("POST request to PlaylistServlet");
        
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
            
            // Convert JSON to Playlist entity manually
            Playlist playlist = createPlaylistFromJson(jsonRequest);
            
            // Delegate to service layer
            Playlist createdPlaylist = playlistService.createPlaylist(playlist);
            
            // Build success response
            JSONObject responseJson = JsonUtil.createSuccessResponse(
                JsonUtil.playlistToJson(createdPlaylist), 
                "Playlist created successfully");
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(responseJson.toString());
            
            LOGGER.info("Successfully created playlist with ID: " + createdPlaylist.getPlaylistId());
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid playlist data", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating playlist", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to create playlist: " + e.getMessage());
        } finally {
            out.close();
        }
    }
    
    /**
     * Handles PUT requests for playlist updates.
     * 
     * Expected URL: PUT /api/playlists/{id}
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
        
        LOGGER.info("PUT request to PlaylistServlet: " + request.getRequestURI());
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Extract playlist ID from path
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Playlist ID is required for updates");
                return;
            }
            
            String playlistIdStr = pathInfo.substring(1);
            Long playlistId;
            try {
                playlistId = Long.parseLong(playlistIdStr);
            } catch (NumberFormatException e) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Invalid playlist ID format: " + playlistIdStr);
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
            
            // Convert JSON to Playlist entity
            Playlist playlist = createPlaylistFromJson(jsonRequest);
            playlist.setPlaylistId(playlistId); // Ensure ID matches path parameter
            
            // Delegate to service layer
            Playlist updatedPlaylist = playlistService.updatePlaylist(playlist);
            
            // Build success response
            JSONObject responseJson = JsonUtil.createSuccessResponse(
                JsonUtil.playlistToJson(updatedPlaylist), 
                "Playlist updated successfully");
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(responseJson.toString());
            
            LOGGER.info("Successfully updated playlist with ID: " + playlistId);
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid playlist update data", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating playlist", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to update playlist: " + e.getMessage());
        } finally {
            out.close();
        }
    }
    
    /**
     * Handles DELETE requests for playlist removal.
     * 
     * Expected URL: DELETE /api/playlists/{id}
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.info("DELETE request to PlaylistServlet: " + request.getRequestURI());
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Extract playlist ID from path
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Playlist ID is required for deletion");
                return;
            }
            
            String playlistIdStr = pathInfo.substring(1);
            Long playlistId;
            try {
                playlistId = Long.parseLong(playlistIdStr);
            } catch (NumberFormatException e) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Invalid playlist ID format: " + playlistIdStr);
                return;
            }
            
            // Delegate to service layer
            boolean deleted = playlistService.deletePlaylist(playlistId);
            
            if (deleted) {
                JSONObject responseJson = JsonUtil.createSuccessResponse(null, 
                    "Playlist deleted successfully");
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(responseJson.toString());
                LOGGER.info("Successfully deleted playlist with ID: " + playlistId);
            } else {
                sendErrorResponse(response, out, HttpServletResponse.SC_NOT_FOUND, 
                    "Playlist not found with ID: " + playlistId);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting playlist", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to delete playlist: " + e.getMessage());
        } finally {
            out.close();
        }
    }
    
    /**
     * Handles listing all playlists with optional filtering and pagination.
     */
    private void handleGetAllPlaylists(HttpServletRequest request, HttpServletResponse response, 
                                      PrintWriter out) throws IOException {
        
        // Parse query parameters manually - migration opportunity
        String searchQuery = request.getParameter("search");
        String pageParam = request.getParameter("page");
        String sizeParam = request.getParameter("size");
        String userParam = request.getParameter("user");
        String publicParam = request.getParameter("public");
        
        try {
            List<Playlist> playlists;
            
            // Handle different query scenarios
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                // Search functionality
                playlists = playlistService.searchPlaylists(searchQuery.trim());
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.playlistsToJsonArray(playlists), 
                    "Playlist search completed successfully");
                out.print(responseJson.toString());
                
            } else if (userParam != null && !userParam.trim().isEmpty()) {
                // Filter by user/creator
                playlists = playlistService.getPlaylistsByUser(userParam.trim());
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.playlistsToJsonArray(playlists), 
                    "Playlists by user retrieved successfully");
                out.print(responseJson.toString());
                
            } else if (publicParam != null && "true".equalsIgnoreCase(publicParam.trim())) {
                // Filter for public playlists only
                playlists = playlistService.getPublicPlaylists();
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.playlistsToJsonArray(playlists), 
                    "Public playlists retrieved successfully");
                out.print(responseJson.toString());
                
            } else if (pageParam != null || sizeParam != null) {
                // Paginated listing
                int page = 0;
                int size = 10; // Default page size for playlists
                
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
                        if (size <= 0) size = 10;
                        if (size > maxPageSize) size = maxPageSize;
                    } catch (NumberFormatException e) {
                        sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                            "Invalid size parameter: " + sizeParam);
                        return;
                    }
                }
                
                playlists = playlistService.getPlaylistsWithPagination(page, size);
                long totalCount = playlistService.getTotalPlaylistCount();
                
                JSONObject responseJson = JsonUtil.createPaginatedResponse(
                    JsonUtil.playlistsToJsonArray(playlists), page, size, totalCount);
                out.print(responseJson.toString());
                
            } else {
                // Get all playlists (no pagination)
                playlists = playlistService.getAllPlaylists();
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.playlistsToJsonArray(playlists), 
                    "All playlists retrieved successfully");
                out.print(responseJson.toString());
            }
            
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving playlists", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to retrieve playlists: " + e.getMessage());
        }
    }
    
    /**
     * Handles retrieving a specific playlist by ID.
     */
    private void handleGetPlaylistById(Long playlistId, HttpServletResponse response, 
                                      PrintWriter out) throws IOException {
        try {
            Playlist playlist = playlistService.getPlaylistById(playlistId);
            
            if (playlist != null) {
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.playlistToJson(playlist), 
                    "Playlist retrieved successfully");
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(responseJson.toString());
            } else {
                sendErrorResponse(response, out, HttpServletResponse.SC_NOT_FOUND, 
                    "Playlist not found with ID: " + playlistId);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving playlist by ID: " + playlistId, e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to retrieve playlist: " + e.getMessage());
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
     * Creates Playlist entity from JSON object.
     * Manual mapping - migration opportunity to Jackson ObjectMapper.
     */
    private Playlist createPlaylistFromJson(JSONObject json) {
        Playlist playlist = new Playlist();
        
        // Map basic fields
        playlist.setPlaylistName(JsonUtil.getStringFromJson(json, "playlistName"));
        playlist.setDescription(JsonUtil.getStringFromJson(json, "description"));
        playlist.setCreatedBy(JsonUtil.getStringFromJson(json, "createdBy"));
        playlist.setIsPublic(JsonUtil.getBooleanFromJson(json, "isPublic"));
        
        // Optional fields that might be provided
        playlist.setTotalDuration(JsonUtil.getIntegerFromJson(json, "totalDuration"));
        playlist.setSongCount(JsonUtil.getIntegerFromJson(json, "songCount"));
        
        return playlist;
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
        LOGGER.info("PlaylistServlet destroyed");
        super.destroy();
    }
}
