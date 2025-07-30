package com.musiclibrary.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * Playlist JPA Entity
 * 
 * Represents a user-created playlist entity in the music library system using JPA for persistence.
 * Playlists contain ordered collections of songs for customized listening experiences.
 * Migrated from traditional JDBC to Spring Data JPA with proper entity annotations.
 * 
 * Business Logic:
 * - Playlist represents user-curated song collections
 * - Each playlist has a unique name per user (composite uniqueness)
 * - Songs are ordered within playlists for sequential playback
 * - Total duration is calculated from constituent songs
 * - Public/private visibility controls playlist sharing
 * - Song count maintains playlist metadata integrity
 * 
 * JPA Features:
 * - Entity mapping with @Entity and @Table annotations
 * - Primary key generation with @GeneratedValue
 * - Many-to-many relationships with @ManyToMany for songs
 * - Bean validation with @NotNull, @Size
 * - Audit fields with @CreationTimestamp and @UpdateTimestamp
 * - Composite unique constraint on playlist name and creator
 * 
 * @author Music Library Development Team
 * @version 2.0 - Migrated to JPA
 * @since Java 17
 */
@Entity
@Table(name = "playlists", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"playlist_name", "created_by"})
})
public class Playlist {
    
    // Primary key with JPA auto-generation
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "playlist_id")
    private Long playlistId;
    
    // Core playlist information with JPA validation
    @NotNull(message = "Playlist name is required")
    @Size(min = 1, max = 255, message = "Playlist name must be between 1 and 255 characters")
    @Column(name = "playlist_name", nullable = false)
    private String playlistName;   // Required - playlist identifier
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;    // Optional - playlist description
    
    @NotNull(message = "Creator is required")
    @Size(min = 1, max = 100, message = "Creator name must be between 1 and 100 characters")
    @Column(name = "created_by", nullable = false)
    private String createdBy;      // Required - user who created playlist
    
    @Column(name = "is_public")
    private Boolean isPublic;      // Visibility control - default true
    
    @Min(value = 0, message = "Total duration cannot be negative")
    @Column(name = "total_duration")
    private Integer totalDuration; // Total duration in seconds
    
    @Min(value = 0, message = "Song count cannot be negative")
    @Column(name = "song_count")
    private Integer songCount;     // Number of songs in playlist
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "playlist_songs",
        joinColumns = @JoinColumn(name = "playlist_id"),
        inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private List<Song> songs;
    
    // Audit fields with JPA automatic timestamping
    @Column(name = "created_date", nullable = false, updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime createdDate;
    
    @Column(name = "last_modified", nullable = false)
    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime lastModified;
    
    /**
     * Default no-argument constructor required for JavaBean specification.
     * Initializes default values and audit timestamps.
     */
    public Playlist() {
        this.isPublic = true;          // Default to public visibility
        this.totalDuration = 0;        // Initialize to empty playlist
        this.songCount = 0;            // Initialize to empty playlist
        this.createdBy = "system";     // Default creator
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
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
        this.lastModified = LocalDateTime.now();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        this.lastModified = LocalDateTime.now();
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
        this.lastModified = LocalDateTime.now();
    }
    
    public Integer getTotalDuration() {
        return totalDuration;
    }
    
    public void setTotalDuration(Integer totalDuration) {
        this.totalDuration = totalDuration;
        this.lastModified = LocalDateTime.now();
    }
    
    public Integer getSongCount() {
        return songCount;
    }
    
    public void setSongCount(Integer songCount) {
        this.songCount = songCount;
        this.lastModified = LocalDateTime.now();
    }
    
    public List<Song> getSongs() {
        return songs;
    }
    
    public void setSongs(List<Song> songs) {
        this.songs = songs;
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(LocalDateTime lastModified) {
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
            this.lastModified = LocalDateTime.now();
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
            this.lastModified = LocalDateTime.now();
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
