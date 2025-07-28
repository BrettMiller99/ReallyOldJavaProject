package com.musiclibrary.util;

import com.musiclibrary.model.Album;
import com.musiclibrary.model.Artist;
import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.Song;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JSON Utility Class for Manual Serialization/Deserialization
 * 
 * Provides manual JSON handling using traditional Java 7 patterns and basic JSON library.
 * This class demonstrates legacy JSON processing approaches commonly found in older
 * Java enterprise applications before Jackson became standard.
 * 
 * Business Logic:
 * - Converts model entities to JSON format for API responses
 * - Parses JSON requests into model entities for persistence
 * - Handles date formatting and null value processing
 * - Provides consistent JSON structure across all endpoints
 * - Manages error response formatting for client consumption
 * 
 * Migration Opportunities:
 * - Manual JSON handling -> Jackson ObjectMapper with annotations
 * - String concatenation -> Jackson @JsonProperty annotations
 * - Manual date formatting -> Jackson @JsonFormat annotations
 * - Traditional try-catch -> Jackson exception handling
 * - Manual null checks -> Jackson @JsonInclude configuration
 * - Basic JSON library -> Modern Jackson with streaming
 * - Manual field mapping -> Jackson automatic serialization
 * - Static utility methods -> Spring @Component with injection
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class JsonUtil {
    
    // Traditional Java logging - migration opportunity
    private static final Logger LOGGER = Logger.getLogger(JsonUtil.class.getName());
    
    // Date format for consistent JSON date representation
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    // Thread-safe date formatters - Java 7 approach
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    private static final SimpleDateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
    
    /**
     * Converts Song entity to JSON representation.
     * Manual serialization approach - migration opportunity to Jackson annotations.
     * 
     * @param song Song entity to convert
     * @return JSON representation of the song
     */
    public static JSONObject songToJson(Song song) {
        if (song == null) {
            return null;
        }
        
        try {
            JSONObject json = new JSONObject();
            
            // Basic fields - manual mapping
            json.put("songId", song.getSongId());
            json.put("songName", song.getSongName());
            json.put("albumName", song.getAlbumName());
            json.put("albumId", song.getAlbumId());
            json.put("artistId", song.getArtistId());
            json.put("artistName", song.getArtistName());
            json.put("trackNumber", song.getTrackNumber());
            json.put("trackLength", song.getTrackLength());
            json.put("genre", song.getGenre());
            json.put("filePath", song.getFilePath());
            json.put("fileSize", song.getFileSize());
            json.put("bitrate", song.getBitrate());
            json.put("rating", song.getRating());
            json.put("playCount", song.getPlayCount());
            json.put("lyrics", song.getLyrics());
            
            // Date fields with manual formatting
            if (song.getDateReleased() != null) {
                synchronized (dateFormat) {
                    json.put("dateReleased", dateFormat.format(song.getDateReleased()));
                }
            }
            
            if (song.getLastPlayed() != null) {
                synchronized (timestampFormat) {
                    json.put("lastPlayed", timestampFormat.format(song.getLastPlayed()));
                }
            }
            
            if (song.getCreatedDate() != null) {
                synchronized (timestampFormat) {
                    json.put("createdDate", timestampFormat.format(song.getCreatedDate()));
                }
            }
            
            if (song.getLastModified() != null) {
                synchronized (timestampFormat) {
                    json.put("lastModified", timestampFormat.format(song.getLastModified()));
                }
            }
            
            // Business computed fields
            json.put("formattedDuration", song.getFormattedDuration());
            
            return json;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error converting song to JSON", e);
            return null;
        }
    }
    
    /**
     * Converts Artist entity to JSON representation.
     * 
     * @param artist Artist entity to convert
     * @return JSON representation of the artist
     */
    public static JSONObject artistToJson(Artist artist) {
        if (artist == null) {
            return null;
        }
        
        try {
            JSONObject json = new JSONObject();
            
            // Basic fields
            json.put("artistId", artist.getArtistId());
            json.put("artistName", artist.getArtistName());
            json.put("biography", artist.getBiography());
            json.put("country", artist.getCountry());
            json.put("formedYear", artist.getFormedYear());
            json.put("website", artist.getWebsite());
            
            // Audit fields
            if (artist.getCreatedDate() != null) {
                synchronized (timestampFormat) {
                    json.put("createdDate", timestampFormat.format(artist.getCreatedDate()));
                }
            }
            
            if (artist.getLastModified() != null) {
                synchronized (timestampFormat) {
                    json.put("lastModified", timestampFormat.format(artist.getLastModified()));
                }
            }
            
            // Business computed fields
            json.put("displayName", artist.getDisplayName());
            json.put("isBand", artist.isBand());
            
            return json;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error converting artist to JSON", e);
            return null;
        }
    }
    
    /**
     * Converts Album entity to JSON representation.
     * 
     * @param album Album entity to convert
     * @return JSON representation of the album
     */
    public static JSONObject albumToJson(Album album) {
        if (album == null) {
            return null;
        }
        
        try {
            JSONObject json = new JSONObject();
            
            // Basic fields
            json.put("albumId", album.getAlbumId());
            json.put("albumName", album.getAlbumName());
            json.put("artistId", album.getArtistId());
            json.put("artistName", album.getArtistName());
            json.put("genre", album.getGenre());
            json.put("recordLabel", album.getRecordLabel());
            json.put("totalTracks", album.getTotalTracks());
            json.put("albumArtPath", album.getAlbumArtPath());
            
            // Date fields
            if (album.getReleaseDate() != null) {
                synchronized (dateFormat) {
                    json.put("releaseDate", dateFormat.format(album.getReleaseDate()));
                }
            }
            
            if (album.getCreatedDate() != null) {
                synchronized (timestampFormat) {
                    json.put("createdDate", timestampFormat.format(album.getCreatedDate()));
                }
            }
            
            if (album.getLastModified() != null) {
                synchronized (timestampFormat) {
                    json.put("lastModified", timestampFormat.format(album.getLastModified()));
                }
            }
            
            // Business computed fields
            json.put("releaseYear", album.getReleaseYear());
            json.put("displayTitle", album.getDisplayTitle());
            json.put("isSingle", album.isSingle());
            json.put("isEP", album.isEP());
            json.put("isFullAlbum", album.isFullAlbum());
            
            return json;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error converting album to JSON", e);
            return null;
        }
    }
    
    /**
     * Converts Playlist entity to JSON representation.
     * 
     * @param playlist Playlist entity to convert
     * @return JSON representation of the playlist
     */
    public static JSONObject playlistToJson(Playlist playlist) {
        if (playlist == null) {
            return null;
        }
        
        try {
            JSONObject json = new JSONObject();
            
            // Basic fields
            json.put("playlistId", playlist.getPlaylistId());
            json.put("playlistName", playlist.getPlaylistName());
            json.put("description", playlist.getDescription());
            json.put("createdBy", playlist.getCreatedBy());
            json.put("isPublic", playlist.getIsPublic());
            json.put("totalDuration", playlist.getTotalDuration());
            json.put("songCount", playlist.getSongCount());
            
            // Audit fields
            if (playlist.getCreatedDate() != null) {
                synchronized (timestampFormat) {
                    json.put("createdDate", timestampFormat.format(playlist.getCreatedDate()));
                }
            }
            
            if (playlist.getLastModified() != null) {
                synchronized (timestampFormat) {
                    json.put("lastModified", timestampFormat.format(playlist.getLastModified()));
                }
            }
            
            // Business computed fields
            json.put("formattedDuration", playlist.getFormattedDuration());
            json.put("isEmpty", playlist.isEmpty());
            json.put("averageSongDuration", playlist.getAverageSongDuration());
            json.put("summary", playlist.getSummary());
            
            return json;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error converting playlist to JSON", e);
            return null;
        }
    }
    
    /**
     * Converts list of songs to JSON array.
     * 
     * @param songs List of songs to convert
     * @return JSON array representation of songs
     */
    public static JSONArray songsToJsonArray(List<Song> songs) {
        JSONArray jsonArray = new JSONArray();
        
        if (songs != null) {
            for (Song song : songs) {
                JSONObject songJson = songToJson(song);
                if (songJson != null) {
                    jsonArray.put(songJson);
                }
            }
        }
        
        return jsonArray;
    }
    
    /**
     * Converts list of artists to JSON array.
     * 
     * @param artists List of artists to convert
     * @return JSON array representation of artists
     */
    public static JSONArray artistsToJsonArray(List<Artist> artists) {
        JSONArray jsonArray = new JSONArray();
        
        if (artists != null) {
            for (Artist artist : artists) {
                JSONObject artistJson = artistToJson(artist);
                if (artistJson != null) {
                    jsonArray.put(artistJson);
                }
            }
        }
        
        return jsonArray;
    }
    
    /**
     * Converts list of albums to JSON array.
     * 
     * @param albums List of albums to convert
     * @return JSON array representation of albums
     */
    public static JSONArray albumsToJsonArray(List<Album> albums) {
        JSONArray jsonArray = new JSONArray();
        
        if (albums != null) {
            for (Album album : albums) {
                JSONObject albumJson = albumToJson(album);
                if (albumJson != null) {
                    jsonArray.put(albumJson);
                }
            }
        }
        
        return jsonArray;
    }
    
    /**
     * Converts list of playlists to JSON array.
     * 
     * @param playlists List of playlists to convert
     * @return JSON array representation of playlists
     */
    public static JSONArray playlistsToJsonArray(List<Playlist> playlists) {
        JSONArray jsonArray = new JSONArray();
        
        if (playlists != null) {
            for (Playlist playlist : playlists) {
                JSONObject playlistJson = playlistToJson(playlist);
                if (playlistJson != null) {
                    jsonArray.put(playlistJson);
                }
            }
        }
        
        return jsonArray;
    }
    
    /**
     * Creates standardized success response JSON.
     * 
     * @param data Data to include in response
     * @param message Success message
     * @return Standardized success response
     */
    public static JSONObject createSuccessResponse(Object data, String message) {
        JSONObject response = new JSONObject();
        
        try {
            response.put("success", true);
            response.put("message", message != null ? message : "Operation completed successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            if (data != null) {
                response.put("data", data);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating success response", e);
        }
        
        return response;
    }
    
    /**
     * Creates standardized error response JSON.
     * 
     * @param error Error message
     * @param code Error code (optional)
     * @return Standardized error response
     */
    public static JSONObject createErrorResponse(String error, String code) {
        JSONObject response = new JSONObject();
        
        try {
            response.put("success", false);
            response.put("error", error != null ? error : "An error occurred");
            response.put("timestamp", System.currentTimeMillis());
            
            if (code != null) {
                response.put("errorCode", code);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating error response", e);
        }
        
        return response;
    }
    
    /**
     * Creates paginated response JSON.
     * 
     * @param data Data array for current page
     * @param page Current page number (0-based)
     * @param size Page size
     * @param totalElements Total number of elements
     * @return Paginated response structure
     */
    public static JSONObject createPaginatedResponse(JSONArray data, int page, int size, long totalElements) {
        JSONObject response = new JSONObject();
        
        try {
            response.put("success", true);
            response.put("data", data);
            response.put("timestamp", System.currentTimeMillis());
            
            // Pagination metadata
            JSONObject pagination = new JSONObject();
            pagination.put("page", page);
            pagination.put("size", size);
            pagination.put("totalElements", totalElements);
            pagination.put("totalPages", (int) Math.ceil((double) totalElements / size));
            pagination.put("hasNext", (page + 1) * size < totalElements);
            pagination.put("hasPrevious", page > 0);
            
            response.put("pagination", pagination);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating paginated response", e);
        }
        
        return response;
    }
    
    /**
     * Safely gets string value from JSON object.
     * Handles null and missing values gracefully.
     * 
     * @param json JSON object
     * @param key Key to retrieve
     * @return String value or null if not present
     */
    public static String getStringFromJson(JSONObject json, String key) {
        try {
            if (json.has(key) && !json.isNull(key)) {
                return json.getString(key);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting string from JSON: " + key, e);
        }
        return null;
    }
    
    /**
     * Safely gets integer value from JSON object.
     * 
     * @param json JSON object
     * @param key Key to retrieve
     * @return Integer value or null if not present
     */
    public static Integer getIntegerFromJson(JSONObject json, String key) {
        try {
            if (json.has(key) && !json.isNull(key)) {
                return json.getInt(key);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting integer from JSON: " + key, e);
        }
        return null;
    }
    
    /**
     * Safely gets long value from JSON object.
     * 
     * @param json JSON object
     * @param key Key to retrieve
     * @return Long value or null if not present
     */
    public static Long getLongFromJson(JSONObject json, String key) {
        try {
            if (json.has(key) && !json.isNull(key)) {
                return json.getLong(key);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting long from JSON: " + key, e);
        }
        return null;
    }
    
    /**
     * Safely gets boolean value from JSON object.
     * 
     * @param json JSON object
     * @param key Key to retrieve
     * @return Boolean value or null if not present
     */
    public static Boolean getBooleanFromJson(JSONObject json, String key) {
        try {
            if (json.has(key) && !json.isNull(key)) {
                return json.getBoolean(key);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting boolean from JSON: " + key, e);
        }
        return null;
    }
    
    /**
     * Safely gets date value from JSON object.
     * Handles date parsing with the standard format.
     * 
     * @param json JSON object
     * @param key Key to retrieve
     * @return Date value or null if not present/invalid
     */
    public static Date getDateFromJson(JSONObject json, String key) {
        try {
            String dateString = getStringFromJson(json, key);
            if (dateString != null) {
                synchronized (dateFormat) {
                    return dateFormat.parse(dateString);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error parsing date from JSON: " + key, e);
        }
        return null;
    }
}
