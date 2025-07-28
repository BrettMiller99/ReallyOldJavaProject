package com.musiclibrary.model;

import java.util.Date;

/**
 * Playlist Model Class
 * 
 * Represents a user-created playlist entity in the music library system.
 * Playlists contain ordered collections of songs for customized listening experiences.
 * This class follows traditional Java 7 JavaBean patterns.
 * 
 * Business Logic:
 * - Playlist represents user-curated song collections
 * - Each playlist has a unique name per user (composite uniqueness)
 * - Songs are ordered within playlists for sequential playback
 * - Total duration is calculated from constituent songs
 * - Public/private visibility controls playlist sharing
 * - Song count maintains playlist metadata integrity
 * 
 * Migration Opportunities:
 * - Manual getter/setter -> Lombok annotations
 * - Date type -> LocalDateTime (Java 8+)
 * - Manual validation -> Bean Validation annotations
 * - Traditional constructors -> Builder pattern
 * - Manual JSON handling -> Jackson annotations
 * - Basic relationship handling -> JPA @ManyToMany annotations
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class Playlist {
    
    // Primary key using traditional Long wrapper
    private Long playlistId;
    
    // Core playlist information
    private String playlistName;   // Required - playlist identifier
    private String description;    // Optional - playlist description
    private String createdBy;      // Required - user who created playlist
    private Boolean isPublic;      // Visibility control - default true
    private Integer totalDuration; // Total duration in seconds
    private Integer songCount;     // Number of songs in playlist
    
    // Audit trail fields - enterprise standard
    private Date createdDate;
    private Date lastModified;
    
    /**
     * Default no-argument constructor required for JavaBean specification.
     * Initializes default values and audit timestamps.
     */
    public Playlist() {
        this.isPublic = true;          // Default to public visibility
        this.totalDuration = 0;        // Initialize to empty playlist
        this.songCount = 0;            // Initialize to empty playlist
        this.createdBy = "system";     // Default creator
        this.createdDate = new Date();
        this.lastModified = new Date();
    }
    
    /**
     * Constructor for creating new playlist with required information.
     * Enforces business rule that playlist must have name and creator.
     * 
     * @param playlistName Name of the playlist (required)
     * @param createdBy User who created the playlist (required)
     */
    public Playlist(String playlistName, String createdBy) {
        this();
        this.playlistName = playlistName;
        this.createdBy = createdBy;
    }
    
    /**
     * Constructor for creating playlist with additional information.
     * 
     * @param playlistName Name of the playlist (required)
     * @param description Playlist description (optional)
     * @param createdBy User who created the playlist (required)
     * @param isPublic Visibility setting (optional, defaults to true)
     */
    public Playlist(String playlistName, String description, String createdBy, Boolean isPublic) {
        this(playlistName, createdBy);
        this.description = description;
        this.isPublic = isPublic;
    }
    
    // Traditional getter/setter methods following JavaBean conventions
    
    public Long getPlaylistId() {
        return playlistId;
    }
    
    public void setPlaylistId(Long playlistId) {
        this.playlistId = playlistId;
    }
    
    public String getPlaylistName() {
        return playlistName;
    }
    
    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
        this.lastModified = new Date();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.lastModified = new Date();
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        this.lastModified = new Date();
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
        this.lastModified = new Date();
    }
    
    public Integer getTotalDuration() {
        return totalDuration;
    }
    
    public void setTotalDuration(Integer totalDuration) {
        this.totalDuration = totalDuration;
        this.lastModified = new Date();
    }
    
    public Integer getSongCount() {
        return songCount;
    }
    
    public void setSongCount(Integer songCount) {
        this.songCount = songCount;
        this.lastModified = new Date();
    }
    
    public Date getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    public Date getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
    
    /**
     * Business validation method to ensure playlist has required information.
     * Used by service layer before persisting to database.
     * 
     * @return true if playlist has valid data for persistence
     */
    public boolean isValid() {
        return playlistName != null && !playlistName.trim().isEmpty() &&
               createdBy != null && !createdBy.trim().isEmpty();
    }
    
    /**
     * Business logic method to get formatted total duration.
     * Converts seconds to HH:MM:SS format for user interface display.
     * 
     * @return Formatted duration string (e.g., "1:23:45")
     */
    public String getFormattedDuration() {
        if (totalDuration == null || totalDuration <= 0) {
            return "0:00";
        }
        
        int hours = totalDuration / 3600;
        int minutes = (totalDuration % 3600) / 60;
        int seconds = totalDuration % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
    
    /**
     * Business logic method to add song duration to playlist total.
     * Updates both duration and song count when songs are added.
     * 
     * @param songDuration Duration of song being added (in seconds)
     */
    public void addSongDuration(Integer songDuration) {
        if (songDuration != null && songDuration > 0) {
            if (this.totalDuration == null) {
                this.totalDuration = 0;
            }
            if (this.songCount == null) {
                this.songCount = 0;
            }
            
            this.totalDuration += songDuration;
            this.songCount++;
            this.lastModified = new Date();
        }
    }
    
    /**
     * Business logic method to remove song duration from playlist total.
     * Updates both duration and song count when songs are removed.
     * 
     * @param songDuration Duration of song being removed (in seconds)
     */
    public void removeSongDuration(Integer songDuration) {
        if (songDuration != null && songDuration > 0) {
            if (this.totalDuration != null && this.totalDuration >= songDuration) {
                this.totalDuration -= songDuration;
            }
            if (this.songCount != null && this.songCount > 0) {
                this.songCount--;
            }
            this.lastModified = new Date();
        }
    }
    
    /**
     * Business logic method to determine if playlist is empty.
     * 
     * @return true if playlist contains no songs
     */
    public boolean isEmpty() {
        return songCount == null || songCount == 0;
    }
    
    /**
     * Business logic method to get average song duration.
     * Calculates average length of songs in playlist.
     * 
     * @return Average song duration in seconds, or 0 if empty
     */
    public int getAverageSongDuration() {
        if (isEmpty() || totalDuration == null) {
            return 0;
        }
        return totalDuration / songCount;
    }
    
    /**
     * Business logic method to create playlist summary.
     * Generates descriptive text about playlist contents.
     * 
     * @return Human-readable playlist summary
     */
    public String getSummary() {
        if (isEmpty()) {
            return "Empty playlist";
        }
        
        String visibility = (isPublic != null && isPublic) ? "Public" : "Private";
        return String.format("%s playlist with %d song%s (%s)",
                visibility,
                songCount,
                songCount == 1 ? "" : "s",
                getFormattedDuration());
    }
    
    // Traditional toString implementation - verbose Java 7 style
    @Override
    public String toString() {
        return "Playlist{" +
                "playlistId=" + playlistId +
                ", playlistName='" + playlistName + '\'' +
                ", description='" + description + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", isPublic=" + isPublic +
                ", songCount=" + songCount +
                ", totalDuration=" + totalDuration +
                ", createdDate=" + createdDate +
                '}';
    }
    
    // Manual equals and hashCode implementation - Java 7 pattern
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Playlist playlist = (Playlist) obj;
        
        if (playlistId != null ? !playlistId.equals(playlist.playlistId) : playlist.playlistId != null) return false;
        if (playlistName != null ? !playlistName.equals(playlist.playlistName) : playlist.playlistName != null) return false;
        if (createdBy != null ? !createdBy.equals(playlist.createdBy) : playlist.createdBy != null) return false;
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = playlistId != null ? playlistId.hashCode() : 0;
        result = 31 * result + (playlistName != null ? playlistName.hashCode() : 0);
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
        return result;
    }
}
