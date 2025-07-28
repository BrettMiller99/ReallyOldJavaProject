package com.musiclibrary.servlet;

import com.musiclibrary.model.Artist;
import com.musiclibrary.service.ArtistService;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Artist REST API Servlet
 * 
 * Provides HTTP endpoints for Artist entity CRUD operations using traditional Java 7 servlet patterns.
 * Demonstrates classic enterprise Java web development for artist management functionality.
 * 
 * Supported Endpoints:
 * - GET /api/artists - List all artists (with optional pagination and search)
 * - GET /api/artists/{id} - Get artist by ID
 * - POST /api/artists - Create new artist
 * - PUT /api/artists/{id} - Update existing artist
 * - DELETE /api/artists/{id} - Delete artist by ID
 * 
 * Business Logic:
 * - Handles HTTP request/response processing for artist management
 * - Validates request parameters and JSON payloads for artist data
 * - Delegates business operations to ArtistService layer
 * - Provides consistent JSON response format for API clients
 * - Implements proper HTTP status codes and error handling
 * - Supports pagination, search, and filtering functionality
 * - Includes audit logging for artist API operations
 * - Enforces business rules for artist uniqueness and validation
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
public class ArtistServlet extends HttpServlet {
    
    // Traditional Java logging - migration opportunity
    private static final Logger LOGGER = Logger.getLogger(ArtistServlet.class.getName());
    
    // Manual service instantiation - migration opportunity to dependency injection
    private ArtistService artistService;
    
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
        this.artistService = new ArtistService();
        
        // Read configuration from web.xml init parameters
        String maxPageSizeParam = getInitParameter("max.page.size");
        if (maxPageSizeParam != null) {
            try {
                this.maxPageSize = Integer.parseInt(maxPageSizeParam);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid max.page.size parameter: " + maxPageSizeParam, e);
            }
        }
        
        LOGGER.info("ArtistServlet initialized with maxPageSize: " + maxPageSize);
    }
    
    /**
     * Handles GET requests for artist retrieval.
     * 
     * Supported patterns:
     * - GET /api/artists - List all artists (with optional pagination)
     * - GET /api/artists/{id} - Get specific artist by ID
     * - GET /api/artists?search=query - Search artists
     * - GET /api/artists?page=0&size=10 - Paginated listing
     * - GET /api/artists?country=USA - Filter by country
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.info("GET request to ArtistServlet: " + request.getRequestURI());
        
        // Set response content type - manual approach
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Parse URL path to determine operation - manual routing
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // List artists with optional filtering and pagination
                handleGetAllArtists(request, response, out);
            } else {
                // Extract artist ID from path
                String artistIdStr = pathInfo.substring(1); // Remove leading slash
                try {
                    Long artistId = Long.parseLong(artistIdStr);
                    handleGetArtistById(artistId, response, out);
                } catch (NumberFormatException e) {
                    sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                        "Invalid artist ID format: " + artistIdStr);
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
     * Handles POST requests for artist creation.
     * 
     * Expected JSON payload:
     * {
     *   "artistName": "Artist Name",
     *   "biography": "Artist biography",
     *   "country": "Country",
     *   "formedYear": 1990,
     *   "website": "http://artist.com"
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
        
        LOGGER.info("POST request to ArtistServlet");
        
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
            
            // Convert JSON to Artist entity manually
            Artist artist = createArtistFromJson(jsonRequest);
            
            // Delegate to service layer
            Artist createdArtist = artistService.createArtist(artist);
            
            // Build success response
            JSONObject responseJson = JsonUtil.createSuccessResponse(
                JsonUtil.artistToJson(createdArtist), 
                "Artist created successfully");
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(responseJson.toString());
            
            LOGGER.info("Successfully created artist with ID: " + createdArtist.getArtistId());
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid artist data", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating artist", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to create artist: " + e.getMessage());
        } finally {
            out.close();
        }
    }
    
    /**
     * Handles PUT requests for artist updates.
     * 
     * Expected URL: PUT /api/artists/{id}
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
        
        LOGGER.info("PUT request to ArtistServlet: " + request.getRequestURI());
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Extract artist ID from path
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Artist ID is required for updates");
                return;
            }
            
            String artistIdStr = pathInfo.substring(1);
            Long artistId;
            try {
                artistId = Long.parseLong(artistIdStr);
            } catch (NumberFormatException e) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Invalid artist ID format: " + artistIdStr);
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
            
            // Convert JSON to Artist entity
            Artist artist = createArtistFromJson(jsonRequest);
            artist.setArtistId(artistId); // Ensure ID matches path parameter
            
            // Delegate to service layer
            Artist updatedArtist = artistService.updateArtist(artist);
            
            // Build success response
            JSONObject responseJson = JsonUtil.createSuccessResponse(
                JsonUtil.artistToJson(updatedArtist), 
                "Artist updated successfully");
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(responseJson.toString());
            
            LOGGER.info("Successfully updated artist with ID: " + artistId);
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid artist update data", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating artist", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to update artist: " + e.getMessage());
        } finally {
            out.close();
        }
    }
    
    /**
     * Handles DELETE requests for artist removal.
     * 
     * Expected URL: DELETE /api/artists/{id}
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.info("DELETE request to ArtistServlet: " + request.getRequestURI());
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Extract artist ID from path
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Artist ID is required for deletion");
                return;
            }
            
            String artistIdStr = pathInfo.substring(1);
            Long artistId;
            try {
                artistId = Long.parseLong(artistIdStr);
            } catch (NumberFormatException e) {
                sendErrorResponse(response, out, HttpServletResponse.SC_BAD_REQUEST, 
                    "Invalid artist ID format: " + artistIdStr);
                return;
            }
            
            // Delegate to service layer
            boolean deleted = artistService.deleteArtist(artistId);
            
            if (deleted) {
                JSONObject responseJson = JsonUtil.createSuccessResponse(null, 
                    "Artist deleted successfully");
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(responseJson.toString());
                LOGGER.info("Successfully deleted artist with ID: " + artistId);
            } else {
                sendErrorResponse(response, out, HttpServletResponse.SC_NOT_FOUND, 
                    "Artist not found with ID: " + artistId);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting artist", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to delete artist: " + e.getMessage());
        } finally {
            out.close();
        }
    }
    
    /**
     * Handles listing all artists with optional filtering and pagination.
     */
    private void handleGetAllArtists(HttpServletRequest request, HttpServletResponse response, 
                                    PrintWriter out) throws IOException {
        
        // Parse query parameters manually - migration opportunity
        String searchQuery = request.getParameter("search");
        String pageParam = request.getParameter("page");
        String sizeParam = request.getParameter("size");
        String countryParam = request.getParameter("country");
        
        try {
            List<Artist> artists;
            
            // Handle different query scenarios
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                // Search functionality
                artists = artistService.searchArtists(searchQuery.trim());
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.artistsToJsonArray(artists), 
                    "Artist search completed successfully");
                out.print(responseJson.toString());
                
            } else if (countryParam != null && !countryParam.trim().isEmpty()) {
                // Filter by country
                artists = artistService.getArtistsByCountry(countryParam.trim());
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.artistsToJsonArray(artists), 
                    "Artists by country retrieved successfully");
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
                
                artists = artistService.getArtistsWithPagination(page, size);
                long totalCount = artistService.getTotalArtistCount();
                
                JSONObject responseJson = JsonUtil.createPaginatedResponse(
                    JsonUtil.artistsToJsonArray(artists), page, size, totalCount);
                out.print(responseJson.toString());
                
            } else {
                // Get all artists (no pagination)
                artists = artistService.getAllArtists();
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.artistsToJsonArray(artists), 
                    "All artists retrieved successfully");
                out.print(responseJson.toString());
            }
            
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving artists", e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to retrieve artists: " + e.getMessage());
        }
    }
    
    /**
     * Handles retrieving a specific artist by ID.
     */
    private void handleGetArtistById(Long artistId, HttpServletResponse response, 
                                    PrintWriter out) throws IOException {
        try {
            Artist artist = artistService.getArtistById(artistId);
            
            if (artist != null) {
                JSONObject responseJson = JsonUtil.createSuccessResponse(
                    JsonUtil.artistToJson(artist), 
                    "Artist retrieved successfully");
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(responseJson.toString());
            } else {
                sendErrorResponse(response, out, HttpServletResponse.SC_NOT_FOUND, 
                    "Artist not found with ID: " + artistId);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving artist by ID: " + artistId, e);
            sendErrorResponse(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to retrieve artist: " + e.getMessage());
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
     * Creates Artist entity from JSON object.
     * Manual mapping - migration opportunity to Jackson ObjectMapper.
     */
    private Artist createArtistFromJson(JSONObject json) {
        Artist artist = new Artist();
        
        // Map basic fields
        artist.setArtistName(JsonUtil.getStringFromJson(json, "artistName"));
        artist.setBiography(JsonUtil.getStringFromJson(json, "biography"));
        artist.setCountry(JsonUtil.getStringFromJson(json, "country"));
        artist.setFormedYear(JsonUtil.getIntegerFromJson(json, "formedYear"));
        artist.setWebsite(JsonUtil.getStringFromJson(json, "website"));
        
        return artist;
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
        LOGGER.info("ArtistServlet destroyed");
        super.destroy();
    }
}
