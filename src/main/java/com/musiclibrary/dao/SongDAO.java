package com.musiclibrary.dao;

import com.musiclibrary.model.Song;
import com.musiclibrary.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Song Data Access Object Implementation
 * 
 * Provides database operations for Song entities using traditional Java 7 JDBC patterns.
 * This class demonstrates legacy enterprise data access approaches commonly found
 * in older Java applications before JPA/Hibernate became standard.
 * 
 * Business Logic:
 * - Manages all CRUD operations for songs in the music library
 * - Enforces referential integrity with artists and albums
 * - Provides search functionality across song names, artists, and albums
 * - Supports play count tracking and rating management
 * - Handles denormalized artist names for performance optimization
 * - Maintains audit trail with creation and modification timestamps
 * 
 * Migration Opportunities:
 * - Manual JDBC -> Spring Data JPA repository
 * - Raw SQL strings -> JPQL or Criteria API queries
 * - Manual ResultSet mapping -> JPA entity mapping
 * - Traditional exception handling -> @Transactional rollback
 * - Manual connection management -> @Autowired DataSource
 * - java.util.logging -> SLF4J with Logback
 * - Vector collections -> ArrayList or Spring Data Page
 * - Manual validation -> Bean Validation annotations
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class SongDAO implements BaseDAO<Song, Long> {
    
    // Traditional Java logging - migration opportunity
    private static final Logger LOGGER = Logger.getLogger(SongDAO.class.getName());
    
    // SQL queries using traditional string constants - migration opportunity
    private static final String INSERT_SONG = 
        "INSERT INTO songs (song_name, album_id, artist_id, track_number, " +
        "track_length, date_released, genre, file_path, file_size, bitrate, rating, " +
        "play_count, last_played, lyrics, created_date, last_modified) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM songs WHERE song_id = ?";
    
    private static final String SELECT_ALL = 
        "SELECT s.*, a.artist_name as artist_name_lookup " +
        "FROM songs s LEFT JOIN artists a ON s.artist_id = a.artist_id " +
        "ORDER BY s.song_name";
    
    private static final String UPDATE_SONG = 
        "UPDATE songs SET song_name = ?, album_id = ?, artist_id = ?, " +
        "track_number = ?, track_length = ?, date_released = ?, genre = ?, file_path = ?, " +
        "file_size = ?, bitrate = ?, rating = ?, play_count = ?, last_played = ?, " +
        "lyrics = ?, last_modified = ? WHERE song_id = ?";
    
    private static final String DELETE_BY_ID = 
        "DELETE FROM songs WHERE song_id = ?";
    
    private static final String COUNT_ALL = 
        "SELECT COUNT(*) FROM songs";
    
    private static final String EXISTS_BY_ID = 
        "SELECT 1 FROM songs WHERE song_id = ? LIMIT 1";
    
    private static final String SEARCH_SONGS = 
        "SELECT s.*, a.artist_name as artist_name_lookup " +
        "FROM songs s LEFT JOIN artists a ON s.artist_id = a.artist_id " +
        "WHERE LOWER(s.song_name) LIKE LOWER(?) " +
        "OR LOWER(a.artist_name) LIKE LOWER(?) " +
        "OR LOWER(s.genre) LIKE LOWER(?) " +
        "ORDER BY s.song_name";
    
    private static final String SELECT_WITH_PAGINATION = 
        "SELECT s.*, a.artist_name as artist_name_lookup " +
        "FROM songs s LEFT JOIN artists a ON s.artist_id = a.artist_id " +
        "ORDER BY s.song_name LIMIT ? OFFSET ?";
    
    private static final String INCREMENT_PLAY_COUNT = 
        "UPDATE songs SET play_count = play_count + 1, last_played = ?, last_modified = ? " +
        "WHERE song_id = ?";
    
    /**
     * Creates a new song in the database.
     * 
     * Business Logic:
     * - Validates song data before insertion
     * - Auto-generates creation timestamp
     * - Looks up artist name for denormalization
     * - Returns song with populated ID
     * 
     * @param song Song entity to create
     * @return Created song with generated ID
     * @throws SQLException if database operation fails
     * @throws IllegalArgumentException if song data is invalid
     */
    @Override
    public Song create(Song song) throws SQLException {
        if (song == null || !song.isValid()) {
            throw new IllegalArgumentException("Invalid song data provided");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            
            // Look up artist name if not provided - business logic for denormalization
            if (song.getArtistName() == null && song.getArtistId() != null) {
                song.setArtistName(lookupArtistName(connection, song.getArtistId()));
            }
            
            statement = connection.prepareStatement(INSERT_SONG, Statement.RETURN_GENERATED_KEYS);
            
            // Set parameters using traditional JDBC approach - migration opportunity
            int paramIndex = 1;
            statement.setString(paramIndex++, song.getSongName());
            statement.setObject(paramIndex++, song.getAlbumId()); // Handle null values
            statement.setLong(paramIndex++, song.getArtistId());
            statement.setObject(paramIndex++, song.getTrackNumber());
            statement.setInt(paramIndex++, song.getTrackLength());
            statement.setDate(paramIndex++, song.getDateReleased() != null ? 
                new java.sql.Date(song.getDateReleased().getTime()) : null);
            statement.setString(paramIndex++, song.getGenre());
            statement.setString(paramIndex++, song.getFilePath());
            statement.setObject(paramIndex++, song.getFileSize());
            statement.setObject(paramIndex++, song.getBitrate());
            statement.setObject(paramIndex++, song.getRating());
            statement.setInt(paramIndex++, song.getPlayCount() != null ? song.getPlayCount() : 0);
            statement.setTimestamp(paramIndex++, song.getLastPlayed() != null ? 
                new java.sql.Timestamp(song.getLastPlayed().getTime()) : null);
            statement.setString(paramIndex++, song.getLyrics());
            
            // Set audit timestamps
            Date now = new Date();
            statement.setTimestamp(paramIndex++, new java.sql.Timestamp(now.getTime()));
            statement.setTimestamp(paramIndex++, new java.sql.Timestamp(now.getTime()));
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating song failed, no rows affected");
            }
            
            // Retrieve generated key - traditional JDBC approach
            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                song.setSongId(generatedKeys.getLong(1));
                song.setCreatedDate(now);
                song.setLastModified(now);
            } else {
                throw new SQLException("Creating song failed, no ID obtained");
            }
            
            DatabaseConnection.commitTransaction(connection);
            LOGGER.info("Created song: " + song.getSongName() + " (ID: " + song.getSongId() + ")");
            
            return song;
            
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction(connection);
            LOGGER.log(Level.SEVERE, "Failed to create song: " + song.getSongName(), e);
            throw e;
        } finally {
            DatabaseConnection.closeResources(generatedKeys, statement, connection);
        }
    }
    
    /**
     * Retrieves song by ID with complete information.
     * 
     * @param id Song ID to retrieve
     * @return Song entity or null if not found
     * @throws SQLException if database operation fails
     */
    @Override
    public Song findById(Long id) throws SQLException {
        if (id == null) {
            return null;
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(SELECT_BY_ID);
            statement.setLong(1, id);
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                Song song = mapResultSetToSong(resultSet);
                LOGGER.fine("Retrieved song: " + song.getSongName());
                return song;
            }
            
            return null;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Retrieves all songs with artist name lookup.
     * Migration opportunity: Add pagination by default.
     * 
     * @return List of all songs
     * @throws SQLException if database operation fails
     */
    @Override
    public List<Song> findAll() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(SELECT_ALL);
            
            // Using ArrayList instead of Vector for better performance
            // but keeping traditional approach for migration demonstration
            List<Song> songs = new ArrayList<Song>();
            
            while (resultSet.next()) {
                Song song = mapResultSetToSong(resultSet);
                // Use artist name from lookup if denormalized name is missing
                if (song.getArtistName() == null) {
                    String lookupName = resultSet.getString("artist_name_lookup");
                    song.setArtistName(lookupName);
                }
                songs.add(song);
            }
            
            LOGGER.info("Retrieved " + songs.size() + " songs");
            return songs;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Updates existing song in database.
     * 
     * Business Logic:
     * - Validates song exists before update
     * - Updates modification timestamp
     * - Maintains referential integrity
     * 
     * @param song Song to update
     * @return Updated song entity
     * @throws SQLException if database operation fails
     */
    @Override
    public Song update(Song song) throws SQLException {
        if (song == null || song.getSongId() == null || !song.isValid()) {
            throw new IllegalArgumentException("Invalid song data for update");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            
            // Verify song exists
            if (!exists(song.getSongId())) {
                throw new SQLException("Song with ID " + song.getSongId() + " does not exist");
            }
            
            // Look up artist name if needed
            if (song.getArtistName() == null && song.getArtistId() != null) {
                song.setArtistName(lookupArtistName(connection, song.getArtistId()));
            }
            
            statement = connection.prepareStatement(UPDATE_SONG);
            // Set parameters - traditional JDBC approach
            int paramIndex = 1;
            statement.setString(paramIndex++, song.getSongName());
            statement.setObject(paramIndex++, song.getAlbumId());
            statement.setLong(paramIndex++, song.getArtistId());
            statement.setObject(paramIndex++, song.getTrackNumber());
            statement.setInt(paramIndex++, song.getTrackLength());
            statement.setDate(paramIndex++, song.getDateReleased() != null ? 
                new java.sql.Date(song.getDateReleased().getTime()) : null);
            statement.setString(paramIndex++, song.getGenre());
            statement.setString(paramIndex++, song.getFilePath());
            statement.setObject(paramIndex++, song.getFileSize());
            statement.setObject(paramIndex++, song.getBitrate());
            statement.setObject(paramIndex++, song.getRating());
            statement.setObject(paramIndex++, song.getPlayCount());
            statement.setTimestamp(paramIndex++, song.getLastPlayed() != null ? 
                new java.sql.Timestamp(song.getLastPlayed().getTime()) : null);
            statement.setString(paramIndex++, song.getLyrics());
            
            // Update modification timestamp
            Date now = new Date();
            statement.setTimestamp(paramIndex++, new java.sql.Timestamp(now.getTime()));
            statement.setLong(paramIndex++, song.getSongId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating song failed, no rows affected");
            }
            
            song.setLastModified(now);
            DatabaseConnection.commitTransaction(connection);
            LOGGER.info("Updated song: " + song.getSongName() + " (ID: " + song.getSongId() + ")");
            
            return song;
            
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction(connection);
            LOGGER.log(Level.SEVERE, "Failed to update song ID: " + song.getSongId(), e);
            throw e;
        } finally {
            DatabaseConnection.closeResources(null, statement, connection);
        }
    }
    
    /**
     * Deletes song by ID.
     * 
     * @param id Song ID to delete
     * @return true if deleted, false if not found
     * @throws SQLException if database operation fails
     */
    @Override
    public boolean delete(Long id) throws SQLException {
        if (id == null) {
            return false;
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(DELETE_BY_ID);
            statement.setLong(1, id);
            
            int affectedRows = statement.executeUpdate();
            boolean deleted = affectedRows > 0;
            
            if (deleted) {
                DatabaseConnection.commitTransaction(connection);
                LOGGER.info("Deleted song with ID: " + id);
            }
            
            return deleted;
            
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction(connection);
            LOGGER.log(Level.SEVERE, "Failed to delete song ID: " + id, e);
            throw e;
        } finally {
            DatabaseConnection.closeResources(null, statement, connection);
        }
    }
    
    /**
     * Checks if song exists by ID.
     * 
     * @param id Song ID to check
     * @return true if exists, false otherwise
     * @throws SQLException if database operation fails
     */
    @Override
    public boolean exists(Long id) throws SQLException {
        if (id == null) {
            return false;
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(EXISTS_BY_ID);
            statement.setLong(1, id);
            resultSet = statement.executeQuery();
            
            return resultSet.next();
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Counts total number of songs.
     * 
     * @return Total song count
     * @throws SQLException if database operation fails
     */
    @Override
    public long count() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(COUNT_ALL);
            
            if (resultSet.next()) {
                long count = resultSet.getLong(1);
                LOGGER.fine("Total song count: " + count);
                return count;
            }
            
            return 0;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Searches songs by name, artist, or genre.
     * 
     * Business Logic:
     * - Case-insensitive search across multiple fields
     * - Uses LIKE pattern matching with wildcards
     * - Includes artist name lookup for complete results
     * 
     * @param query Search query string
     * @return List of matching songs
     * @throws SQLException if database operation fails
     */
    @Override
    public List<Song> search(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<Song>();
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(SEARCH_SONGS);
            
            String searchPattern = "%" + query.trim() + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            
            resultSet = statement.executeQuery();
            List<Song> songs = new ArrayList<Song>();
            
            while (resultSet.next()) {
                Song song = mapResultSetToSong(resultSet);
                // Use artist name from lookup if denormalized name is missing
                if (song.getArtistName() == null) {
                    String lookupName = resultSet.getString("artist_name_lookup");
                    song.setArtistName(lookupName);
                }
                songs.add(song);
            }
            
            LOGGER.info("Search query '" + query + "' returned " + songs.size() + " results");
            return songs;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Retrieves songs with pagination support.
     * 
     * @param offset Starting position (0-based)
     * @param limit Maximum number of results
     * @return List of songs within specified range
     * @throws SQLException if database operation fails
     */
    @Override
    public List<Song> findWithPagination(int offset, int limit) throws SQLException {
        if (offset < 0 || limit <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(SELECT_WITH_PAGINATION);
            statement.setInt(1, limit);
            statement.setInt(2, offset);
            
            resultSet = statement.executeQuery();
            List<Song> songs = new ArrayList<Song>();
            
            while (resultSet.next()) {
                Song song = mapResultSetToSong(resultSet);
                if (song.getArtistName() == null) {
                    String lookupName = resultSet.getString("artist_name_lookup");
                    song.setArtistName(lookupName);
                }
                songs.add(song);
            }
            
            LOGGER.fine("Retrieved " + songs.size() + " songs with pagination (offset=" + 
                       offset + ", limit=" + limit + ")");
            return songs;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Business method to increment play count when song is played.
     * Updates both play count and last played timestamp.
     * 
     * @param songId ID of song being played
     * @throws SQLException if database operation fails
     */
    public void recordPlayback(Long songId) throws SQLException {
        if (songId == null) {
            return;
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(INCREMENT_PLAY_COUNT);
            
            Date now = new Date();
            statement.setTimestamp(1, new java.sql.Timestamp(now.getTime()));
            statement.setTimestamp(2, new java.sql.Timestamp(now.getTime()));
            statement.setLong(3, songId);
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                DatabaseConnection.commitTransaction(connection);
                LOGGER.fine("Recorded playback for song ID: " + songId);
            }
            
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction(connection);
            LOGGER.log(Level.WARNING, "Failed to record playback for song ID: " + songId, e);
            throw e;
        } finally {
            DatabaseConnection.closeResources(null, statement, connection);
        }
    }
    
    /**
     * Helper method to map ResultSet to Song entity.
     * Traditional manual mapping approach - migration opportunity to JPA.
     * 
     * @param resultSet ResultSet positioned at current row
     * @return Song entity populated from ResultSet
     * @throws SQLException if ResultSet access fails
     */
    private Song mapResultSetToSong(ResultSet resultSet) throws SQLException {
        Song song = new Song();
        
        // Map basic fields
        song.setSongId(resultSet.getLong("song_id"));
        song.setSongName(resultSet.getString("song_name"));
        song.setAlbumId(resultSet.getObject("album_id") != null ? 
            resultSet.getLong("album_id") : null);
        song.setArtistId(resultSet.getLong("artist_id"));
        song.setArtistName(resultSet.getString("artist_name"));
        song.setTrackNumber(resultSet.getObject("track_number") != null ? 
            resultSet.getInt("track_number") : null);
        song.setTrackLength(resultSet.getInt("track_length"));
        
        // Map dates - traditional Java Date handling
        java.sql.Date releaseDate = resultSet.getDate("date_released");
        if (releaseDate != null) {
            song.setDateReleased(new Date(releaseDate.getTime()));
        }
        
        // Map optional fields
        song.setGenre(resultSet.getString("genre"));
        song.setFilePath(resultSet.getString("file_path"));
        song.setFileSize(resultSet.getObject("file_size") != null ? 
            resultSet.getLong("file_size") : null);
        song.setBitrate(resultSet.getObject("bitrate") != null ? 
            resultSet.getInt("bitrate") : null);
        song.setRating(resultSet.getObject("rating") != null ? 
            resultSet.getInt("rating") : null);
        song.setPlayCount(resultSet.getObject("play_count") != null ? 
            resultSet.getInt("play_count") : null);
        
        // Map timestamp fields
        java.sql.Timestamp lastPlayed = resultSet.getTimestamp("last_played");
        if (lastPlayed != null) {
            song.setLastPlayed(new Date(lastPlayed.getTime()));
        }
        
        song.setLyrics(resultSet.getString("lyrics"));
        
        // Map audit fields
        java.sql.Timestamp created = resultSet.getTimestamp("created_date");
        if (created != null) {
            song.setCreatedDate(new Date(created.getTime()));
        }
        
        java.sql.Timestamp modified = resultSet.getTimestamp("last_modified");
        if (modified != null) {
            song.setLastModified(new Date(modified.getTime()));
        }
        
        return song;
    }
    
    /**
     * Helper method to lookup artist name by ID.
     * Used for denormalization performance optimization.
     * 
     * @param connection Database connection to use
     * @param artistId Artist ID to lookup
     * @return Artist name or null if not found
     * @throws SQLException if database operation fails
     */
    private String lookupArtistName(Connection connection, Long artistId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            statement = connection.prepareStatement("SELECT artist_name FROM artists WHERE artist_id = ?");
            statement.setLong(1, artistId);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getString("artist_name");
            }
            
            return null;
            
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Failed to close ResultSet", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Failed to close Statement", e);
                }
            }
        }
    }
    
    /**
     * Finds songs by artist name.
     * 
     * Business Logic:
     * - Case-insensitive artist name matching
     * - Returns songs ordered by album and track number
     * - Includes complete song information with artist lookup
     * 
     * @param artistName Artist name to search for
     * @return List of songs by the artist
     * @throws SQLException if database operation fails
     */
    public List<Song> findByArtist(String artistName) throws SQLException {
        if (artistName == null || artistName.trim().isEmpty()) {
            return new ArrayList<Song>();
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            String sql = "SELECT s.*, a.artist_name as artist_name_lookup " +
                        "FROM songs s LEFT JOIN artists a ON s.artist_id = a.artist_id " +
                        "WHERE LOWER(s.artist_name) = LOWER(?) " +
                        "ORDER BY s.album_id, s.track_number, s.song_name";
            statement = connection.prepareStatement(sql);
            statement.setString(1, artistName.trim());
            
            resultSet = statement.executeQuery();
            
            List<Song> songs = new ArrayList<Song>();
            while (resultSet.next()) {
                songs.add(mapResultSetToSong(resultSet));
            }
            
            LOGGER.fine("Found " + songs.size() + " songs for artist: " + artistName);
            return songs;
            
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing ResultSet", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing PreparedStatement", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing Connection", e);
                }
            }
        }
    }
    
    /**
     * Finds songs by album name.
     * 
     * Business Logic:
     * - Case-insensitive album name matching
     * - Returns songs ordered by track number
     * - Includes complete track listing for an album
     * 
     * @param albumName Album name to search for
     * @return List of songs in the album
     * @throws SQLException if database operation fails
     */
    public List<Song> findByAlbum(String albumName) throws SQLException {
        if (albumName == null || albumName.trim().isEmpty()) {
            return new ArrayList<Song>();
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            String sql = "SELECT s.*, a.artist_name as artist_name_lookup, alb.album_name " +
                        "FROM songs s " +
                        "LEFT JOIN artists a ON s.artist_id = a.artist_id " +
                        "LEFT JOIN albums alb ON s.album_id = alb.album_id " +
                        "WHERE LOWER(alb.album_name) = LOWER(?) " +
                        "ORDER BY s.track_number, s.song_name";
            statement = connection.prepareStatement(sql);
            statement.setString(1, albumName.trim());
            
            resultSet = statement.executeQuery();
            
            List<Song> songs = new ArrayList<Song>();
            while (resultSet.next()) {
                songs.add(mapResultSetToSong(resultSet));
            }
            
            LOGGER.fine("Found " + songs.size() + " songs for album: " + albumName);
            return songs;
            
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing ResultSet", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing PreparedStatement", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing Connection", e);
                }
            }
        }
    }
}
