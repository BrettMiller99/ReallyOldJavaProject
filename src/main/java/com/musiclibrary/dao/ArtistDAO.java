package com.musiclibrary.dao;

import com.musiclibrary.model.Artist;
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
 * Artist Data Access Object Implementation
 * 
 * Provides database operations for Artist entities using traditional Java 7 JDBC patterns.
 * This class manages artist data with emphasis on referential integrity since artists
 * are referenced by songs and albums throughout the system.
 * 
 * Business Logic:
 * - Manages artist master data for the music library catalog
 * - Enforces unique artist names to prevent duplicates
 * - Provides search functionality across artist names and countries
 * - Supports biographical and metadata management for rich artist profiles
 * - Handles cascade operations when artists are deleted (affects songs/albums)
 * - Maintains formation year data for chronological organization
 * 
 * Migration Opportunities:
 * - Manual JDBC -> Spring Data JPA repository
 * - Raw SQL strings -> JPQL or @Query annotations
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
public class ArtistDAO implements BaseDAO<Artist, Long> {
    
    // Traditional Java logging - migration opportunity
    private static final Logger LOGGER = Logger.getLogger(ArtistDAO.class.getName());
    
    // SQL queries using traditional string constants - migration opportunity
    private static final String INSERT_ARTIST = 
        "INSERT INTO artists (artist_name, biography, country, formed_year, website, " +
        "created_date, last_modified) VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM artists WHERE artist_id = ?";
    
    private static final String SELECT_ALL = 
        "SELECT * FROM artists ORDER BY artist_name";
    
    private static final String UPDATE_ARTIST = 
        "UPDATE artists SET artist_name = ?, biography = ?, country = ?, formed_year = ?, " +
        "website = ?, last_modified = ? WHERE artist_id = ?";
    
    private static final String DELETE_BY_ID = 
        "DELETE FROM artists WHERE artist_id = ?";
    
    private static final String COUNT_ALL = 
        "SELECT COUNT(*) FROM artists";
    
    private static final String EXISTS_BY_ID = 
        "SELECT 1 FROM artists WHERE artist_id = ? LIMIT 1";
    
    private static final String SEARCH_ARTISTS = 
        "SELECT * FROM artists WHERE LOWER(artist_name) LIKE LOWER(?) " +
        "OR LOWER(country) LIKE LOWER(?) OR LOWER(biography) LIKE LOWER(?) " +
        "ORDER BY artist_name";
    
    private static final String SELECT_WITH_PAGINATION = 
        "SELECT * FROM artists ORDER BY artist_name LIMIT ? OFFSET ?";
    
    private static final String CHECK_ARTIST_NAME_UNIQUE = 
        "SELECT COUNT(*) FROM artists WHERE LOWER(artist_name) = LOWER(?) AND artist_id != ?";
    
    private static final String GET_ARTIST_SONG_COUNT = 
        "SELECT COUNT(*) FROM songs WHERE artist_id = ?";
    
    private static final String GET_ARTIST_ALBUM_COUNT = 
        "SELECT COUNT(*) FROM albums WHERE artist_id = ?";
    
    /**
     * Creates a new artist in the database.
     * 
     * Business Logic:
     * - Validates artist data including name uniqueness
     * - Auto-generates creation timestamp
     * - Ensures artist name is unique (case-insensitive)
     * - Returns artist with populated ID
     * 
     * @param artist Artist entity to create
     * @return Created artist with generated ID
     * @throws SQLException if database operation fails
     * @throws IllegalArgumentException if artist data is invalid
     */
    @Override
    public Artist create(Artist artist) throws SQLException {
        if (artist == null || !artist.isValid()) {
            throw new IllegalArgumentException("Invalid artist data provided");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            
            // Business rule: Check for duplicate artist names
            if (isArtistNameTaken(connection, artist.getArtistName(), null)) {
                throw new SQLException("Artist name '" + artist.getArtistName() + "' already exists");
            }
            
            statement = connection.prepareStatement(INSERT_ARTIST, Statement.RETURN_GENERATED_KEYS);
            
            // Set parameters using traditional JDBC approach
            int paramIndex = 1;
            statement.setString(paramIndex++, artist.getArtistName());
            statement.setString(paramIndex++, artist.getBiography());
            statement.setString(paramIndex++, artist.getCountry());
            statement.setObject(paramIndex++, artist.getFormedYear()); // Handle null
            statement.setString(paramIndex++, artist.getWebsite());
            
            // Set audit timestamps
            Date now = new Date();
            statement.setTimestamp(paramIndex++, new java.sql.Timestamp(now.getTime()));
            statement.setTimestamp(paramIndex++, new java.sql.Timestamp(now.getTime()));
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating artist failed, no rows affected");
            }
            
            // Retrieve generated key
            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                artist.setArtistId(generatedKeys.getLong(1));
                artist.setCreatedDate(now);
                artist.setLastModified(now);
            } else {
                throw new SQLException("Creating artist failed, no ID obtained");
            }
            
            DatabaseConnection.commitTransaction(connection);
            LOGGER.info("Created artist: " + artist.getArtistName() + " (ID: " + artist.getArtistId() + ")");
            
            return artist;
            
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction(connection);
            LOGGER.log(Level.SEVERE, "Failed to create artist: " + artist.getArtistName(), e);
            throw e;
        } finally {
            DatabaseConnection.closeResources(generatedKeys, statement, connection);
        }
    }
    
    /**
     * Retrieves artist by ID.
     * 
     * @param id Artist ID to retrieve
     * @return Artist entity or null if not found
     * @throws SQLException if database operation fails
     */
    @Override
    public Artist findById(Long id) throws SQLException {
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
                Artist artist = mapResultSetToArtist(resultSet);
                LOGGER.fine("Retrieved artist: " + artist.getArtistName());
                return artist;
            }
            
            return null;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Retrieves all artists ordered by name.
     * 
     * @return List of all artists
     * @throws SQLException if database operation fails
     */
    @Override
    public List<Artist> findAll() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(SELECT_ALL);
            
            List<Artist> artists = new ArrayList<Artist>();
            
            while (resultSet.next()) {
                Artist artist = mapResultSetToArtist(resultSet);
                artists.add(artist);
            }
            
            LOGGER.info("Retrieved " + artists.size() + " artists");
            return artists;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Updates existing artist in database.
     * 
     * Business Logic:
     * - Validates artist exists before update
     * - Checks name uniqueness if name is changed
     * - Updates modification timestamp
     * 
     * @param artist Artist to update
     * @return Updated artist entity
     * @throws SQLException if database operation fails
     */
    @Override
    public Artist update(Artist artist) throws SQLException {
        if (artist == null || artist.getArtistId() == null || !artist.isValid()) {
            throw new IllegalArgumentException("Invalid artist data for update");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            
            // Verify artist exists
            if (!exists(artist.getArtistId())) {
                throw new SQLException("Artist with ID " + artist.getArtistId() + " does not exist");
            }
            
            // Business rule: Check name uniqueness if name changed
            if (isArtistNameTaken(connection, artist.getArtistName(), artist.getArtistId())) {
                throw new SQLException("Artist name '" + artist.getArtistName() + "' already exists");
            }
            
            statement = connection.prepareStatement(UPDATE_ARTIST);
            
            // Set parameters
            int paramIndex = 1;
            statement.setString(paramIndex++, artist.getArtistName());
            statement.setString(paramIndex++, artist.getBiography());
            statement.setString(paramIndex++, artist.getCountry());
            statement.setObject(paramIndex++, artist.getFormedYear());
            statement.setString(paramIndex++, artist.getWebsite());
            
            // Update modification timestamp
            Date now = new Date();
            statement.setTimestamp(paramIndex++, new java.sql.Timestamp(now.getTime()));
            statement.setLong(paramIndex++, artist.getArtistId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating artist failed, no rows affected");
            }
            
            artist.setLastModified(now);
            DatabaseConnection.commitTransaction(connection);
            LOGGER.info("Updated artist: " + artist.getArtistName() + " (ID: " + artist.getArtistId() + ")");
            
            return artist;
            
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction(connection);
            LOGGER.log(Level.SEVERE, "Failed to update artist ID: " + artist.getArtistId(), e);
            throw e;
        } finally {
            DatabaseConnection.closeResources(null, statement, connection);
        }
    }
    
    /**
     * Deletes artist by ID.
     * 
     * Business Logic:
     * - Checks for dependent songs/albums before deletion
     * - Throws exception if artist has associated content
     * - This enforces referential integrity at business level
     * 
     * @param id Artist ID to delete
     * @return true if deleted, false if not found
     * @throws SQLException if database operation fails or artist has dependencies
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
            
            // Business rule: Check for dependent records
            int songCount = getArtistSongCount(connection, id);
            int albumCount = getArtistAlbumCount(connection, id);
            
            if (songCount > 0 || albumCount > 0) {
                throw new SQLException("Cannot delete artist: has " + songCount + 
                    " songs and " + albumCount + " albums");
            }
            
            statement = connection.prepareStatement(DELETE_BY_ID);
            statement.setLong(1, id);
            
            int affectedRows = statement.executeUpdate();
            boolean deleted = affectedRows > 0;
            
            if (deleted) {
                DatabaseConnection.commitTransaction(connection);
                LOGGER.info("Deleted artist with ID: " + id);
            }
            
            return deleted;
            
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction(connection);
            LOGGER.log(Level.SEVERE, "Failed to delete artist ID: " + id, e);
            throw e;
        } finally {
            DatabaseConnection.closeResources(null, statement, connection);
        }
    }
    
    /**
     * Checks if artist exists by ID.
     * 
     * @param id Artist ID to check
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
     * Counts total number of artists.
     * 
     * @return Total artist count
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
                LOGGER.fine("Total artist count: " + count);
                return count;
            }
            
            return 0;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Searches artists by name, country, or biography.
     * 
     * Business Logic:
     * - Case-insensitive search across multiple fields
     * - Uses LIKE pattern matching with wildcards
     * - Orders results alphabetically by artist name
     * 
     * @param query Search query string
     * @return List of matching artists
     * @throws SQLException if database operation fails
     */
    @Override
    public List<Artist> search(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<Artist>();
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(SEARCH_ARTISTS);
            
            String searchPattern = "%" + query.trim() + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            
            resultSet = statement.executeQuery();
            List<Artist> artists = new ArrayList<Artist>();
            
            while (resultSet.next()) {
                Artist artist = mapResultSetToArtist(resultSet);
                artists.add(artist);
            }
            
            LOGGER.info("Search query '" + query + "' returned " + artists.size() + " artists");
            return artists;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Retrieves artists with pagination support.
     * 
     * @param offset Starting position (0-based)
     * @param limit Maximum number of results
     * @return List of artists within specified range
     * @throws SQLException if database operation fails
     */
    @Override
    public List<Artist> findWithPagination(int offset, int limit) throws SQLException {
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
            List<Artist> artists = new ArrayList<Artist>();
            
            while (resultSet.next()) {
                Artist artist = mapResultSetToArtist(resultSet);
                artists.add(artist);
            }
            
            LOGGER.fine("Retrieved " + artists.size() + " artists with pagination (offset=" + 
                       offset + ", limit=" + limit + ")");
            return artists;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Helper method to map ResultSet to Artist entity.
     * Traditional manual mapping approach - migration opportunity to JPA.
     * 
     * @param resultSet ResultSet positioned at current row
     * @return Artist entity populated from ResultSet
     * @throws SQLException if ResultSet access fails
     */
    private Artist mapResultSetToArtist(ResultSet resultSet) throws SQLException {
        Artist artist = new Artist();
        
        // Map basic fields
        artist.setArtistId(resultSet.getLong("artist_id"));
        artist.setArtistName(resultSet.getString("artist_name"));
        artist.setBiography(resultSet.getString("biography"));
        artist.setCountry(resultSet.getString("country"));
        artist.setFormedYear(resultSet.getObject("formed_year") != null ? 
            resultSet.getInt("formed_year") : null);
        artist.setWebsite(resultSet.getString("website"));
        
        // Map audit fields
        java.sql.Timestamp created = resultSet.getTimestamp("created_date");
        if (created != null) {
            artist.setCreatedDate(new Date(created.getTime()));
        }
        
        java.sql.Timestamp modified = resultSet.getTimestamp("last_modified");
        if (modified != null) {
            artist.setLastModified(new Date(modified.getTime()));
        }
        
        return artist;
    }
    
    /**
     * Helper method to check if artist name is already taken.
     * Implements business rule for unique artist names.
     * 
     * @param connection Database connection to use
     * @param artistName Name to check
     * @param excludeId Artist ID to exclude from check (for updates)
     * @return true if name is taken, false if available
     * @throws SQLException if database operation fails
     */
    private boolean isArtistNameTaken(Connection connection, String artistName, Long excludeId) 
            throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            statement = connection.prepareStatement(CHECK_ARTIST_NAME_UNIQUE);
            statement.setString(1, artistName);
            statement.setLong(2, excludeId != null ? excludeId : -1);
            
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
     * Helper method to count songs by artist.
     * Used for referential integrity checks before deletion.
     * 
     * @param connection Database connection to use
     * @param artistId Artist ID to count songs for
     * @return Number of songs by this artist
     * @throws SQLException if database operation fails
     */
    private int getArtistSongCount(Connection connection, Long artistId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            statement = connection.prepareStatement(GET_ARTIST_SONG_COUNT);
            statement.setLong(1, artistId);
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
     * Helper method to count albums by artist.
     * Used for referential integrity checks before deletion.
     * 
     * @param connection Database connection to use
     * @param artistId Artist ID to count albums for
     * @return Number of albums by this artist
     * @throws SQLException if database operation fails
     */
    private int getArtistAlbumCount(Connection connection, Long artistId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            statement = connection.prepareStatement(GET_ARTIST_ALBUM_COUNT);
            statement.setLong(1, artistId);
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
     * Finds artists by country.
     * 
     * Business Logic:
     * - Case-insensitive country name matching
     * - Returns artists ordered by name within country
     * - Provides geographical filtering for artist discovery
     * 
     * @param country Country name to search for
     * @return List of artists from the country
     * @throws SQLException if database operation fails
     */
    public List<Artist> findByCountry(String country) throws SQLException {
        if (country == null || country.trim().isEmpty()) {
            return new ArrayList<Artist>();
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM artists WHERE LOWER(country) = LOWER(?) ORDER BY artist_name";
            statement = connection.prepareStatement(sql);
            statement.setString(1, country.trim());
            
            resultSet = statement.executeQuery();
            
            List<Artist> artists = new ArrayList<Artist>();
            while (resultSet.next()) {
                artists.add(mapResultSetToArtist(resultSet));
            }
            
            LOGGER.fine("Found " + artists.size() + " artists for country: " + country);
            return artists;
            
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
