package com.musiclibrary.dao;

import com.musiclibrary.model.Playlist;
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
 * Playlist Data Access Object Implementation
 * 
 * Provides database operations for Playlist entities using traditional Java 7 JDBC patterns.
 * Playlists are user-created collections of songs with ordering and metadata management.
 * 
 * Business Logic:
 * - Manages user-created playlist collections with song ordering
 * - Enforces unique playlist names per user to prevent duplicates
 * - Provides search functionality across playlist names and descriptions
 * - Handles playlist-song relationship management (many-to-many)
 * - Supports public/private playlist visibility controls
 * - Maintains playlist statistics (song count, total duration)
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
 * - Manual many-to-many handling -> JPA @ManyToMany annotations
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class PlaylistDAO implements BaseDAO<Playlist, Long> {
    
    private static final Logger LOGGER = Logger.getLogger(PlaylistDAO.class.getName());
    
    // SQL queries for playlist operations - migration opportunity
    private static final String INSERT_PLAYLIST = 
        "INSERT INTO playlists (playlist_name, description, created_by, is_public, " +
        "total_duration, song_count, created_date, last_modified) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM playlists WHERE playlist_id = ?";
    
    private static final String SELECT_ALL = 
        "SELECT * FROM playlists ORDER BY playlist_name";
    
    private static final String UPDATE_PLAYLIST = 
        "UPDATE playlists SET playlist_name = ?, description = ?, created_by = ?, " +
        "is_public = ?, total_duration = ?, song_count = ?, last_modified = ? " +
        "WHERE playlist_id = ?";
    
    private static final String DELETE_BY_ID = 
        "DELETE FROM playlists WHERE playlist_id = ?";
    
    private static final String COUNT_ALL = 
        "SELECT COUNT(*) FROM playlists";
    
    private static final String EXISTS_BY_ID = 
        "SELECT 1 FROM playlists WHERE playlist_id = ? LIMIT 1";
    
    private static final String SEARCH_PLAYLISTS = 
        "SELECT * FROM playlists WHERE LOWER(playlist_name) LIKE LOWER(?) " +
        "OR LOWER(description) LIKE LOWER(?) OR LOWER(created_by) LIKE LOWER(?) " +
        "ORDER BY playlist_name";
    
    private static final String SELECT_WITH_PAGINATION = 
        "SELECT * FROM playlists ORDER BY playlist_name LIMIT ? OFFSET ?";
    
    private static final String CHECK_PLAYLIST_UNIQUE = 
        "SELECT COUNT(*) FROM playlists WHERE LOWER(playlist_name) = LOWER(?) " +
        "AND created_by = ? AND playlist_id != ?";
    
    private static final String SELECT_BY_USER = 
        "SELECT * FROM playlists WHERE created_by = ? ORDER BY created_date DESC";
    
    private static final String SELECT_PUBLIC_PLAYLISTS = 
        "SELECT * FROM playlists WHERE is_public = TRUE ORDER BY playlist_name";
    
    @Override
    public Playlist create(Playlist playlist) throws SQLException {
        if (playlist == null || !playlist.isValid()) {
            throw new IllegalArgumentException("Invalid playlist data provided");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            
            if (isPlaylistNameTaken(connection, playlist.getPlaylistName(), 
                                  playlist.getCreatedBy(), null)) {
                throw new SQLException("Playlist '" + playlist.getPlaylistName() + 
                    "' already exists for user: " + playlist.getCreatedBy());
            }
            
            statement = connection.prepareStatement(INSERT_PLAYLIST, Statement.RETURN_GENERATED_KEYS);
            
            int paramIndex = 1;
            statement.setString(paramIndex++, playlist.getPlaylistName());
            statement.setString(paramIndex++, playlist.getDescription());
            statement.setString(paramIndex++, playlist.getCreatedBy());
            statement.setBoolean(paramIndex++, playlist.getIsPublic() != null ? 
                playlist.getIsPublic() : true);
            statement.setInt(paramIndex++, playlist.getTotalDuration() != null ? 
                playlist.getTotalDuration() : 0);
            statement.setInt(paramIndex++, playlist.getSongCount() != null ? 
                playlist.getSongCount() : 0);
            
            Date now = new Date();
            statement.setTimestamp(paramIndex++, new java.sql.Timestamp(now.getTime()));
            statement.setTimestamp(paramIndex++, new java.sql.Timestamp(now.getTime()));
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating playlist failed, no rows affected");
            }
            
            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                playlist.setPlaylistId(generatedKeys.getLong(1));
                playlist.setCreatedDate(now);
                playlist.setLastModified(now);
            } else {
                throw new SQLException("Creating playlist failed, no ID obtained");
            }
            
            DatabaseConnection.commitTransaction(connection);
            LOGGER.info("Created playlist: " + playlist.getPlaylistName());
            return playlist;
            
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction(connection);
            LOGGER.log(Level.SEVERE, "Failed to create playlist: " + playlist.getPlaylistName(), e);
            throw e;
        } finally {
            DatabaseConnection.closeResources(generatedKeys, statement, connection);
        }
    }
    
    @Override
    public Playlist findById(Long id) throws SQLException {
        if (id == null) return null;
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(SELECT_BY_ID);
            statement.setLong(1, id);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return mapResultSetToPlaylist(resultSet);
            }
            return null;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    @Override
    public List<Playlist> findAll() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(SELECT_ALL);
            
            List<Playlist> playlists = new ArrayList<Playlist>();
            while (resultSet.next()) {
                playlists.add(mapResultSetToPlaylist(resultSet));
            }
            
            LOGGER.info("Retrieved " + playlists.size() + " playlists");
            return playlists;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    @Override
    public Playlist update(Playlist playlist) throws SQLException {
        if (playlist == null || playlist.getPlaylistId() == null || !playlist.isValid()) {
            throw new IllegalArgumentException("Invalid playlist data for update");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            
            if (!exists(playlist.getPlaylistId())) {
                throw new SQLException("Playlist with ID " + playlist.getPlaylistId() + " does not exist");
            }
            
            if (isPlaylistNameTaken(connection, playlist.getPlaylistName(), 
                                  playlist.getCreatedBy(), playlist.getPlaylistId())) {
                throw new SQLException("Playlist name already exists for user");
            }
            
            statement = connection.prepareStatement(UPDATE_PLAYLIST);
            
            int paramIndex = 1;
            statement.setString(paramIndex++, playlist.getPlaylistName());
            statement.setString(paramIndex++, playlist.getDescription());
            statement.setString(paramIndex++, playlist.getCreatedBy());
            statement.setBoolean(paramIndex++, playlist.getIsPublic());
            statement.setInt(paramIndex++, playlist.getTotalDuration());
            statement.setInt(paramIndex++, playlist.getSongCount());
            
            Date now = new Date();
            statement.setTimestamp(paramIndex++, new java.sql.Timestamp(now.getTime()));
            statement.setLong(paramIndex++, playlist.getPlaylistId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating playlist failed, no rows affected");
            }
            
            playlist.setLastModified(now);
            DatabaseConnection.commitTransaction(connection);
            LOGGER.info("Updated playlist: " + playlist.getPlaylistName());
            return playlist;
            
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction(connection);
            LOGGER.log(Level.SEVERE, "Failed to update playlist", e);
            throw e;
        } finally {
            DatabaseConnection.closeResources(null, statement, connection);
        }
    }
    
    @Override
    public boolean delete(Long id) throws SQLException {
        if (id == null) return false;
        
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
                LOGGER.info("Deleted playlist with ID: " + id);
            }
            
            return deleted;
            
        } catch (SQLException e) {
            DatabaseConnection.rollbackTransaction(connection);
            LOGGER.log(Level.SEVERE, "Failed to delete playlist", e);
            throw e;
        } finally {
            DatabaseConnection.closeResources(null, statement, connection);
        }
    }
    
    @Override
    public boolean exists(Long id) throws SQLException {
        if (id == null) return false;
        
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
                return resultSet.getLong(1);
            }
            return 0;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    @Override
    public List<Playlist> search(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<Playlist>();
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(SEARCH_PLAYLISTS);
            
            String searchPattern = "%" + query.trim() + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            
            resultSet = statement.executeQuery();
            List<Playlist> playlists = new ArrayList<Playlist>();
            
            while (resultSet.next()) {
                playlists.add(mapResultSetToPlaylist(resultSet));
            }
            
            LOGGER.info("Search returned " + playlists.size() + " playlists");
            return playlists;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    @Override
    public List<Playlist> findWithPagination(int offset, int limit) throws SQLException {
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
            List<Playlist> playlists = new ArrayList<Playlist>();
            
            while (resultSet.next()) {
                playlists.add(mapResultSetToPlaylist(resultSet));
            }
            
            return playlists;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Business method to find playlists by user.
     */
    public List<Playlist> findByUser(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return new ArrayList<Playlist>();
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(SELECT_BY_USER);
            statement.setString(1, username);
            
            resultSet = statement.executeQuery();
            List<Playlist> playlists = new ArrayList<Playlist>();
            
            while (resultSet.next()) {
                playlists.add(mapResultSetToPlaylist(resultSet));
            }
            
            return playlists;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Business method to find public playlists.
     */
    public List<Playlist> findPublicPlaylists() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(SELECT_PUBLIC_PLAYLISTS);
            
            List<Playlist> playlists = new ArrayList<Playlist>();
            while (resultSet.next()) {
                playlists.add(mapResultSetToPlaylist(resultSet));
            }
            
            return playlists;
            
        } finally {
            DatabaseConnection.closeResources(resultSet, statement, connection);
        }
    }
    
    /**
     * Helper method to map ResultSet to Playlist entity.
     */
    private Playlist mapResultSetToPlaylist(ResultSet resultSet) throws SQLException {
        Playlist playlist = new Playlist();
        
        playlist.setPlaylistId(resultSet.getLong("playlist_id"));
        playlist.setPlaylistName(resultSet.getString("playlist_name"));
        playlist.setDescription(resultSet.getString("description"));
        playlist.setCreatedBy(resultSet.getString("created_by"));
        playlist.setIsPublic(resultSet.getBoolean("is_public"));
        playlist.setTotalDuration(resultSet.getInt("total_duration"));
        playlist.setSongCount(resultSet.getInt("song_count"));
        
        java.sql.Timestamp created = resultSet.getTimestamp("created_date");
        if (created != null) {
            playlist.setCreatedDate(new Date(created.getTime()));
        }
        
        java.sql.Timestamp modified = resultSet.getTimestamp("last_modified");
        if (modified != null) {
            playlist.setLastModified(new Date(modified.getTime()));
        }
        
        return playlist;
    }
    
    /**
     * Helper method to check playlist name uniqueness per user.
     */
    private boolean isPlaylistNameTaken(Connection connection, String playlistName, 
                                      String createdBy, Long excludeId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            statement = connection.prepareStatement(CHECK_PLAYLIST_UNIQUE);
            statement.setString(1, playlistName);
            statement.setString(2, createdBy);
            statement.setLong(3, excludeId != null ? excludeId : -1);
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            return false;
            
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
        }
    }
}
