package com.musiclibrary.dao;

import com.musiclibrary.model.Album;
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
 * Album Data Access Object Implementation
 * 
 * Provides database operations for Album entities using traditional Java 7 JDBC patterns.
 * Albums serve as intermediate entities between artists and songs, representing
 * collections of songs released together.
 * 
 * Business Logic:
 * - Manages album catalog data with artist relationships
 * - Enforces unique album names per artist to prevent duplicates
 * - Provides search functionality across album names, genres, and labels
 * - Supports chronological organization by release dates
 * - Handles track count management for album completeness
 * - Maintains artist denormalization for performance optimization
 * 
 * Migration Opportunities:
 * - Manual JDBC -> Spring Data JPA repository
 * - Raw SQL strings -> JPQL with @Query annotations
 * - Manual ResultSet mapping -> JPA entity mapping
 * - Traditional exception handling -> @Transactional rollback
 * - Manual connection management -> @Autowired DataSource
 * - java.util.logging -> SLF4J with structured logging
 * - ArrayList collections -> Spring Data Page/Slice
 * - Manual validation -> Bean Validation with @Valid
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class AlbumDAO implements BaseDAO<Album, Long> {
    
    // Traditional Java logging - migration opportunity
    private static final Logger LOGGER = Logger.getLogger(AlbumDAO.class.getName());
    
    // SQL queries using traditional string constants - migration opportunity
    private static final String INSERT_ALBUM = 
        "INSERT INTO albums (album_name, artist_id, release_date, genre, record_label, " +
        "total_tracks, album_art_path, created_date, last_modified) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT a.*, ar.artist_name FROM albums a " +
        "LEFT JOIN artists ar ON a.artist_id = ar.artist_id WHERE a.album_id = ?";
    
    private static final String SELECT_ALL = 
        "SELECT a.*, ar.artist_name FROM albums a " +
        "LEFT JOIN artists ar ON a.artist_id = ar.artist_id ORDER BY a.album_name";
    
    private static final String UPDATE_ALBUM = 
        "UPDATE albums SET album_name = ?, artist_id = ?, release_date = ?, genre = ?, " +
        "record_label = ?, total_tracks = ?, album_art_path = ?, last_modified = ? " +
        "WHERE album_id = ?";
    
    private static final String DELETE_BY_ID = 
        "DELETE FROM albums WHERE album_id = ?";
    
    private static final String COUNT_ALL = 
        "SELECT COUNT(*) FROM albums";
    
    private static final String EXISTS_BY_ID = 
        "SELECT 1 FROM albums WHERE album_id = ? LIMIT 1";
    
    private static final String SEARCH_ALBUMS = 
        "SELECT a.*, ar.artist_name FROM albums a " +
        "LEFT JOIN artists ar ON a.artist_id = ar.artist_id " +
        "WHERE LOWER(a.album_name) LIKE LOWER(?) " +
        "OR LOWER(a.genre) LIKE LOWER(?) " +
        "OR LOWER(a.record_label) LIKE LOWER(?) " +
        "OR LOWER(ar.artist_name) LIKE LOWER(?) " +
        "ORDER BY a.album_name";
    
    private static final String SELECT_WITH_PAGINATION = 
        "SELECT a.*, ar.artist_name FROM albums a " +
        "LEFT JOIN artists ar ON a.artist_id = ar.artist_id " +
        "ORDER BY a.album_name LIMIT ? OFFSET ?";
    
    private static final String CHECK_ALBUM_UNIQUE = 
        "SELECT COUNT(*) FROM albums WHERE LOWER(album_name) = LOWER(?) " +
        "AND artist_id = ? AND album_id != ?";
    
    private static final String SELECT_BY_ARTIST = 
        "SELECT a.*, ar.artist_name FROM albums a " +
        "LEFT JOIN artists ar ON a.artist_id = ar.artist_id " +
        "WHERE a.artist_id = ? ORDER BY a.release_date DESC";
    
    private static final String GET_ALBUM_SONG_COUNT = 
        "SELECT COUNT(*) FROM songs WHERE album_id = ?";
    
    /**
     * Creates a new album in the database.
     * 
     * Business Logic:
     * - Validates album data including uniqueness per artist
     * - Auto-generates creation timestamp
     * - Ensures album name is unique per artist
     * - Looks up artist name for denormalization
     * - Returns album with populated ID
     * 
     * @param album Album entity to create
     * @return Created album with generated ID
     * @throws SQLException if database operation fails
     * @throws IllegalArgumentException if album data is invalid
     */
    @Override
    public Album create(Album album) throws SQLException {
        if (album == null || !album.isValid()) {
            throw new IllegalArgumentException("Invalid album data provided");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            
            // Business rule: Check for duplicate album name per artist
            if (isAlbumNameTaken(connection, album.getAlbumName(), 
                               album.getArtistId(), null)) {
                throw new SQLException("Album '" + album.getAlbumName() + 
                    "' already exists for this artist");
            }
            
            // Look up artist name for denormalization
            if (album.getArtistName() == null && album.getArtistId() != null) {
                album.setArtistName(lookupArtistName(connection, album.getArtistId()));
            }
            
            statement = connection.prepareStatement(INSERT_ALBUM, Statement.RETURN_GENERATED_KEYS);
            
            // Set parameters using traditional JDBC approach
            int paramIndex = 1;
            statement.setString(paramIndex++, album.getAlbumName());
            statement.setLong(paramIndex++, album.getArtistId());
            statement.setDate(paramIndex++, album.getReleaseDate() != null ? 
                new java.sql.Date(album.getReleaseDate().getTime()) : null);
            statement.setString(paramIndex++, album.getGenre());
            statement.setString(paramIndex++, album.getRecordLabel());
            statement.setObject(paramIndex++, album.getTotalTracks());
            statement.setString(paramIndex++, album.getAlbumArtPath());
            
            // Set audit timestamps
            Date now = new Date();
            statement.setTimestamp(paramIndex++, new java.sql.Timestamp(now.getTime()));
            statement.setTimestamp(paramIndex++, new java.sql.Timestamp(now.getTime()));
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating album failed, no rows affected");
            }
            
            // Retrieve generated key
            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                album.setAlbumId(generatedKeys.getLong(1));
                album.setCreatedDate(now);
                album.setLastModified(now);
            } else {
                throw new SQLException("Creating album failed, no ID obtained");
            }
            
            DatabaseConnection.commitTransaction(connection);
            LOGGER.info("Created album: " + album.getAlbumName() + " (ID: " + album.getAlbumId() + ")");
            
            return album;
            
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction(connection);
            LOGGER.log(Level.SEVERE, "Failed to create album: " + album.getAlbumName(), e);
            throw e;
        } finally {
            DatabaseConnection.closeResources(generatedKeys, statement, connection);
        }
    }
    
    /**
     * Retrieves album by ID with artist information.
     * 
     * @param id Album ID to retrieve
     * @return Album entity or null if not found
     * @throws SQLException if database operation fails
     */
    @Override
    public Album findById(Long id) throws SQLException {
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
                Album album = mapResultSetToAlbum(resultSet);
                LOGGER.fine("Retrieved album: " + album.getAlbumName());
                return album;
            }
            
            return null;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Retrieves all albums with artist information.
     * 
     * @return List of all albums
     * @throws SQLException if database operation fails
     */
    @Override
    public List<Album> findAll() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(SELECT_ALL);
            
            List<Album> albums = new ArrayList<Album>();
            
            while (resultSet.next()) {
                Album album = mapResultSetToAlbum(resultSet);
                albums.add(album);
            }
            
            LOGGER.info("Retrieved " + albums.size() + " albums");
            return albums;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Updates existing album in database.
     * 
     * Business Logic:
     * - Validates album exists before update
     * - Checks name uniqueness per artist if name is changed
     * - Updates modification timestamp
     * - Maintains artist relationship integrity
     * 
     * @param album Album to update
     * @return Updated album entity
     * @throws SQLException if database operation fails
     */
    @Override
    public Album update(Album album) throws SQLException {
        if (album == null || album.getAlbumId() == null || !album.isValid()) {
            throw new IllegalArgumentException("Invalid album data for update");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            
            // Verify album exists
            if (!exists(album.getAlbumId())) {
                throw new SQLException("Album with ID " + album.getAlbumId() + " does not exist");
            }
            
            // Business rule: Check name uniqueness per artist if changed
            if (isAlbumNameTaken(connection, album.getAlbumName(), 
                               album.getArtistId(), album.getAlbumId())) {
                throw new SQLException("Album '" + album.getAlbumName() + 
                    "' already exists for this artist");
            }
            
            // Look up artist name if needed
            if (album.getArtistName() == null && album.getArtistId() != null) {
                album.setArtistName(lookupArtistName(connection, album.getArtistId()));
            }
            
            statement = connection.prepareStatement(UPDATE_ALBUM);
            
            // Set parameters
            int paramIndex = 1;
            statement.setString(paramIndex++, album.getAlbumName());
            statement.setLong(paramIndex++, album.getArtistId());
            statement.setDate(paramIndex++, album.getReleaseDate() != null ? 
                new java.sql.Date(album.getReleaseDate().getTime()) : null);
            statement.setString(paramIndex++, album.getGenre());
            statement.setString(paramIndex++, album.getRecordLabel());
            statement.setObject(paramIndex++, album.getTotalTracks());
            statement.setString(paramIndex++, album.getAlbumArtPath());
            
            // Update modification timestamp
            Date now = new Date();
            statement.setTimestamp(paramIndex++, new java.sql.Timestamp(now.getTime()));
            statement.setLong(paramIndex++, album.getAlbumId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating album failed, no rows affected");
            }
            
            album.setLastModified(now);
            DatabaseConnection.commitTransaction(connection);
            LOGGER.info("Updated album: " + album.getAlbumName() + " (ID: " + album.getAlbumId() + ")");
            
            return album;
            
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction(connection);
            LOGGER.log(Level.SEVERE, "Failed to update album ID: " + album.getAlbumId(), e);
            throw e;
        } finally {
            DatabaseConnection.closeResources(null, statement, connection);
        }
    }
    
    /**
     * Deletes album by ID.
     * 
     * Business Logic:
     * - Checks for dependent songs before deletion
     * - Warns if album has associated songs but allows deletion (CASCADE)
     * - Foreign key constraints handle song cleanup automatically
     * 
     * @param id Album ID to delete
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
            
            // Business info: Check for dependent songs (informational only)
            int songCount = getAlbumSongCount(connection, id);
            if (songCount > 0) {
                LOGGER.info("Deleting album with " + songCount + " associated songs");
            }
            
            statement = connection.prepareStatement(DELETE_BY_ID);
            statement.setLong(1, id);
            
            int affectedRows = statement.executeUpdate();
            boolean deleted = affectedRows > 0;
            
            if (deleted) {
                DatabaseConnection.commitTransaction(connection);
                LOGGER.info("Deleted album with ID: " + id);
            }
            
            return deleted;
            
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction(connection);
            LOGGER.log(Level.SEVERE, "Failed to delete album ID: " + id, e);
            throw e;
        } finally {
            DatabaseConnection.closeResources(null, statement, connection);
        }
    }
    
    /**
     * Checks if album exists by ID.
     * 
     * @param id Album ID to check
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
     * Counts total number of albums.
     * 
     * @return Total album count
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
                LOGGER.fine("Total album count: " + count);
                return count;
            }
            
            return 0;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Searches albums by name, genre, label, or artist.
     * 
     * Business Logic:
     * - Case-insensitive search across multiple fields
     * - Includes artist name in search results
     * - Uses LIKE pattern matching with wildcards
     * - Orders results alphabetically by album name
     * 
     * @param query Search query string
     * @return List of matching albums
     * @throws SQLException if database operation fails
     */
    @Override
    public List<Album> search(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<Album>();
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(SEARCH_ALBUMS);
            
            String searchPattern = "%" + query.trim() + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            statement.setString(4, searchPattern);
            
            resultSet = statement.executeQuery();
            List<Album> albums = new ArrayList<Album>();
            
            while (resultSet.next()) {
                Album album = mapResultSetToAlbum(resultSet);
                albums.add(album);
            }
            
            LOGGER.info("Search query '" + query + "' returned " + albums.size() + " albums");
            return albums;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Retrieves albums with pagination support.
     * 
     * @param offset Starting position (0-based)
     * @param limit Maximum number of results
     * @return List of albums within specified range
     * @throws SQLException if database operation fails
     */
    @Override
    public List<Album> findWithPagination(int offset, int limit) throws SQLException {
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
            List<Album> albums = new ArrayList<Album>();
            
            while (resultSet.next()) {
                Album album = mapResultSetToAlbum(resultSet);
                albums.add(album);
            }
            
            LOGGER.fine("Retrieved " + albums.size() + " albums with pagination (offset=" + 
                       offset + ", limit=" + limit + ")");
            return albums;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Business method to find albums by artist.
     * Retrieves all albums for a specific artist ordered by release date.
     * 
     * @param artistId Artist ID to find albums for
     * @return List of albums by the artist
     * @throws SQLException if database operation fails
     */
    public List<Album> findByArtist(Long artistId) throws SQLException {
        if (artistId == null) {
            return new ArrayList<Album>();
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(SELECT_BY_ARTIST);
            statement.setLong(1, artistId);
            
            resultSet = statement.executeQuery();
            List<Album> albums = new ArrayList<Album>();
            
            while (resultSet.next()) {
                Album album = mapResultSetToAlbum(resultSet);
                albums.add(album);
            }
            
            LOGGER.fine("Retrieved " + albums.size() + " albums for artist ID: " + artistId);
            return albums;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Helper method to map ResultSet to Album entity.
     * Traditional manual mapping approach - migration opportunity to JPA.
     * 
     * @param resultSet ResultSet positioned at current row
     * @return Album entity populated from ResultSet
     * @throws SQLException if ResultSet access fails
     */
    private Album mapResultSetToAlbum(ResultSet resultSet) throws SQLException {
        Album album = new Album();
        
        // Map basic fields
        album.setAlbumId(resultSet.getLong("album_id"));
        album.setAlbumName(resultSet.getString("album_name"));
        album.setArtistId(resultSet.getLong("artist_id"));
        album.setArtistName(resultSet.getString("artist_name")); // From JOIN
        album.setGenre(resultSet.getString("genre"));
        album.setRecordLabel(resultSet.getString("record_label"));
        album.setTotalTracks(resultSet.getObject("total_tracks") != null ? 
            resultSet.getInt("total_tracks") : null);
        album.setAlbumArtPath(resultSet.getString("album_art_path"));
        
        // Map release date - traditional Java Date handling
        java.sql.Date releaseDate = resultSet.getDate("release_date");
        if (releaseDate != null) {
            album.setReleaseDate(new Date(releaseDate.getTime()));
        }
        
        // Map audit fields
        java.sql.Timestamp created = resultSet.getTimestamp("created_date");
        if (created != null) {
            album.setCreatedDate(new Date(created.getTime()));
        }
        
        java.sql.Timestamp modified = resultSet.getTimestamp("last_modified");
        if (modified != null) {
            album.setLastModified(new Date(modified.getTime()));
        }
        
        return album;
    }
    
    /**
     * Helper method to check if album name is taken per artist.
     * Implements business rule for unique album names per artist.
     * 
     * @param connection Database connection to use
     * @param albumName Album name to check
     * @param artistId Artist ID for uniqueness scope
     * @param excludeId Album ID to exclude from check (for updates)
     * @return true if name is taken, false if available
     * @throws SQLException if database operation fails
     */
    private boolean isAlbumNameTaken(Connection connection, String albumName, 
                                   Long artistId, Long excludeId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            statement = connection.prepareStatement(CHECK_ALBUM_UNIQUE);
            statement.setString(1, albumName);
            statement.setLong(2, artistId);
            statement.setLong(3, excludeId != null ? excludeId : -1);
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            
            return false;
            
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
     * Helper method to count songs in an album.
     * Used for informational purposes during album operations.
     * 
     * @param connection Database connection to use
     * @param albumId Album ID to count songs for
     * @return Number of songs in the album
     * @throws SQLException if database operation fails
     */
    private int getAlbumSongCount(Connection connection, Long albumId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            statement = connection.prepareStatement(GET_ALBUM_SONG_COUNT);
            statement.setLong(1, albumId);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            
            return 0;
            
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
     * Finds albums by artist name.
     * 
     * Business Logic:
     * - Case-insensitive artist name matching
     * - Returns albums ordered by release date (newest first)
     * - Provides complete discography for an artist by name
     * 
     * @param artistName Artist name to search for
     * @return List of albums by the artist (never null, may be empty)
     * @throws SQLException if database operation fails
     */
    public List<Album> findByArtist(String artistName) throws SQLException {
        if (artistName == null || artistName.trim().isEmpty()) {
            return new ArrayList<Album>();
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            String sql = "SELECT a.*, ar.artist_name as artist_name_lookup " +
                        "FROM albums a LEFT JOIN artists ar ON a.artist_id = ar.artist_id " +
                        "WHERE LOWER(ar.artist_name) = LOWER(?) " +
                        "ORDER BY a.release_date DESC, a.album_name";
            statement = connection.prepareStatement(sql);
            statement.setString(1, artistName.trim());
            
            resultSet = statement.executeQuery();
            
            List<Album> albums = new ArrayList<Album>();
            while (resultSet.next()) {
                albums.add(mapResultSetToAlbum(resultSet));
            }
            
            LOGGER.fine("Found " + albums.size() + " albums for artist: " + artistName);
            return albums;
            
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
     * Finds albums by genre.
     * 
     * Business Logic:
     * - Case-insensitive genre name matching
     * - Returns albums ordered by name within genre
     * - Provides genre-based filtering for music discovery
     * 
     * @param genre Genre name to search for
     * @return List of albums in the genre
     * @throws SQLException if database operation fails
     */
    public List<Album> findByGenre(String genre) throws SQLException {
        if (genre == null || genre.trim().isEmpty()) {
            return new ArrayList<Album>();
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            String sql = "SELECT a.*, ar.artist_name as artist_name_lookup " +
                        "FROM albums a LEFT JOIN artists ar ON a.artist_id = ar.artist_id " +
                        "WHERE LOWER(a.genre) = LOWER(?) " +
                        "ORDER BY a.album_name";
            statement = connection.prepareStatement(sql);
            statement.setString(1, genre.trim());
            
            resultSet = statement.executeQuery();
            
            List<Album> albums = new ArrayList<Album>();
            while (resultSet.next()) {
                albums.add(mapResultSetToAlbum(resultSet));
            }
            
            LOGGER.fine("Found " + albums.size() + " albums for genre: " + genre);
            return albums;
            
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
     * Finds albums by release year.
     * 
     * Business Logic:
     * - Exact year matching using YEAR() function
     * - Returns albums ordered by name within year
     * - Provides chronological filtering for music discovery
     * 
     * @param year Release year to search for
     * @return List of albums released in the year
     * @throws SQLException if database operation fails
     */
    public List<Album> findByYear(int year) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            String sql = "SELECT a.*, ar.artist_name as artist_name_lookup " +
                        "FROM albums a LEFT JOIN artists ar ON a.artist_id = ar.artist_id " +
                        "WHERE YEAR(a.release_date) = ? " +
                        "ORDER BY a.album_name";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, year);
            
            resultSet = statement.executeQuery();
            
            List<Album> albums = new ArrayList<Album>();
            while (resultSet.next()) {
                albums.add(mapResultSetToAlbum(resultSet));
            }
            
            LOGGER.fine("Found " + albums.size() + " albums for year: " + year);
            return albums;
            
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
