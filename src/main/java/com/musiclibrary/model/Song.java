package com.musiclibrary.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Song JPA Entity
 * 
 * Represents a song entity in the music library system using JPA for persistence.
 * Migrated from traditional JDBC to Spring Data JPA with proper entity annotations.
 * 
 * Business Logic:
 * - Song must have a name, artist, and track length (core requirements)
 * - Track length is stored in seconds for precise duration calculations
 * - Rating system uses 0-5 scale for user feedback
 * - Play count tracks usage statistics for recommendation algorithms
 * - File path enables integration with actual music file storage
 * 
 * JPA Features:
 * - Entity mapping with @Entity and @Table annotations
 * - Primary key generation with @GeneratedValue
 * - Foreign key relationships with @ManyToOne
 * - Bean validation with @NotNull, @Size, @Min, @Max
 * - Audit fields with @CreationTimestamp and @UpdateTimestamp
 * 
 * @author Music Library Development Team
 * @version 2.0 - Migrated to JPA
 * @since Java 17
 */
@Entity
@Table(name = "songs")
public class Song {
    
    // Primary key with JPA auto-generation
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "song_id")
    private Long songId;
    
    // Required fields with JPA validation
    @NotNull(message = "Song name is required")
    @Size(min = 1, max = 255, message = "Song name must be between 1 and 255 characters")
    @Column(name = "song_name", nullable = false)
    private String songName;      // Required: SongName field
    
    @Column(name = "album_name")
    private String albumName;     // AlbumName field - denormalized for performance
    
    @NotNull(message = "Release date is required")
    @Column(name = "date_released", nullable = false)
    private LocalDate dateReleased;    // Required: DateReleased field
    
    @NotNull(message = "Track length is required")
    @Min(value = 1, message = "Track length must be positive")
    @Column(name = "track_length", nullable = false)
    private Integer trackLength;  // Required: TrackLength field (in seconds)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;          // Many songs belong to one album
    
    @NotNull(message = "Artist is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;        // Many songs belong to one artist - required for business logic
    
    // Additional business-relevant fields with JPA annotations
    @Column(name = "artist_name")
    private String artistName;    // Denormalized for performance - typical legacy pattern
    
    @Column(name = "track_number")
    private Integer trackNumber;  // Track position within album
    
    @Size(max = 100, message = "Genre must not exceed 100 characters")
    @Column(name = "genre")
    private String genre;         // Music genre classification
    
    @Column(name = "file_path")
    private String filePath;      // Path to actual music file
    
    @Column(name = "file_size")
    private Long fileSize;        // File size in bytes
    
    @Column(name = "bitrate")
    private Integer bitrate;      // Audio quality indicator
    
    @Min(value = 0, message = "Rating must be between 0 and 5")
    @Max(value = 5, message = "Rating must be between 0 and 5")
    @Column(name = "rating")
    private Integer rating;       // User rating 0-5 stars
    
    @Min(value = 0, message = "Play count cannot be negative")
    @Column(name = "play_count")
    private Integer playCount;    // Usage tracking for analytics
    
    @Column(name = "last_played")
    private LocalDateTime lastPlayed;      // Most recent play timestamp
    
    @Column(name = "lyrics", columnDefinition = "TEXT")
    private String lyrics;        // Song lyrics content
    
    // Audit fields with JPA automatic timestamping
    @Column(name = "created_date", nullable = false, updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime createdDate;
    
    @Column(name = "last_modified", nullable = false)
    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime lastModified;
    
    /**
     * Default constructor required for JavaBean specification.
     * Essential for reflection-based frameworks and JSON serialization.
     */
    public Song() {
        // Initialize audit timestamps but leave business fields null for proper JPA handling
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Constructor for creating new songs with required fields.
     * Enforces business rule that songs must have basic information.
     * 
     * @param songName Name of the song (required)
     * @param artist Artist entity (required for referential integrity)
     * @param trackLength Duration in seconds (required for playback)
     * @param dateReleased Release date (required for chronological sorting)
     */
    public Song(String songName, Artist artist, Integer trackLength, LocalDate dateReleased) {
        this(); // Call default constructor for initialization
        this.songName = songName;
        this.artist = artist;
        this.trackLength = trackLength;
        this.dateReleased = dateReleased;
    }
    
    // Traditional verbose getter/setter methods - Java 7 pattern
    // Modern equivalent would use Lombok @Data annotation
    
    public Long getSongId() {
        return songId;
    }
    
    public void setSongId(Long songId) {
        this.songId = songId;
    }
    
    public String getSongName() {
        return songName;
    }
    
    public void setSongName(String songName) {
        this.songName = songName;
        this.lastModified = LocalDateTime.now(); // Audit trail
    }
    
    public String getAlbumName() {
        return albumName;
    }
    
    public void setAlbumName(String albumName) {
        this.albumName = albumName;
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDate getDateReleased() {
        return dateReleased;
    }
    
    public void setDateReleased(LocalDate dateReleased) {
        this.dateReleased = dateReleased;
        this.lastModified = LocalDateTime.now();
    }
    
    public Integer getTrackLength() {
        return trackLength;
    }
    
    public void setTrackLength(Integer trackLength) {
        this.trackLength = trackLength;
        this.lastModified = LocalDateTime.now();
    }
    
    public Album getAlbum() {
        return album;
    }
    
    public void setAlbum(Album album) {
        this.album = album;
        this.lastModified = LocalDateTime.now();
    }
    
    public Artist getArtist() {
        return artist;
    }
    
    public void setArtist(Artist artist) {
        this.artist = artist;
        this.lastModified = LocalDateTime.now();
    }
    
    public Long getAlbumId() {
        return album != null ? album.getAlbumId() : null;
    }
    
    public void setAlbumId(Long albumId) {
        this.lastModified = LocalDateTime.now();
    }
    
    public Long getArtistId() {
        return artist != null ? artist.getArtistId() : null;
    }
    
    public void setArtistId(Long artistId) {
        this.lastModified = LocalDateTime.now();
    }
    
    public String getArtistName() {
        return artistName;
    }
    
    public void setArtistName(String artistName) {
        this.artistName = artistName;
        this.lastModified = LocalDateTime.now();
    }
    
    public Integer getTrackNumber() {
        return trackNumber;
    }
    
    public void setTrackNumber(Integer trackNumber) {
        this.trackNumber = trackNumber;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
        this.lastModified = LocalDateTime.now();
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
        this.lastModified = LocalDateTime.now();
    }
    
    public Integer getBitrate() {
        return bitrate;
    }
    
    public void setBitrate(Integer bitrate) {
        this.bitrate = bitrate;
        this.lastModified = LocalDateTime.now();
    }
    
    public Integer getRating() {
        return rating;
    }
    
    /**
     * Sets the song rating with business rule validation.
     * Rating must be between 0-5 stars as per business requirements.
     * 
     * @param rating User rating (0-5)
     * @throws IllegalArgumentException if rating is outside valid range
     */
    public void setRating(Integer rating) {
        if (rating != null && (rating < 0 || rating > 5)) {
            throw new IllegalArgumentException("Rating must be between 0 and 5 stars");
        }
        this.rating = rating;
        this.lastModified = LocalDateTime.now();
    }
    
    public Integer getPlayCount() {
        return playCount;
    }
    
    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Increments play count when song is played.
     * Business logic for tracking song popularity and usage analytics.
     */
    public void incrementPlayCount() {
        if (this.playCount == null) {
            this.playCount = 0;
        }
        this.playCount++;
        this.lastPlayed = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDateTime getLastPlayed() {
        return lastPlayed;
    }
    
    public void setLastPlayed(LocalDateTime lastPlayed) {
        this.lastPlayed = lastPlayed;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getLyrics() {
        return lyrics;
    }
    
    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
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
     * Business logic method to format track length for display.
     * Converts seconds to MM:SS format for user interface.
     * 
     * @return Formatted duration string (e.g., "3:45")
     */
    public String getFormattedDuration() {
        if (trackLength == null || trackLength <= 0) {
            return "0:00";
        }
        
        int minutes = trackLength / 60;
        int seconds = trackLength % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    /**
     * Business validation method to check if song has minimum required data.
     * Used by service layer before persisting songs to database.
     * 
     * @return true if song has all required fields
     */
    public boolean isValid() {
        return songName != null && !songName.trim().isEmpty() &&
               artist != null &&
               trackLength != null && trackLength > 0 &&
               dateReleased != null;
    }
    
    // Traditional toString method - verbose Java 7 approach
    // Modern equivalent would use Lombok @ToString or Apache Commons
    @Override
    public String toString() {
        String displayArtistName = (artist != null) ? artist.getArtistName() : artistName;
        String displayAlbumName = (album != null) ? album.getAlbumName() : albumName;
        
        return "Song{" +
                "songId=" + songId +
                ", songName='" + songName + '\'' +
                ", albumName='" + displayAlbumName + '\'' +
                ", artistName='" + displayArtistName + '\'' +
                ", trackLength=" + trackLength +
                ", dateReleased=" + dateReleased +
                ", genre='" + genre + '\'' +
                ", rating=" + rating +
                ", playCount=" + playCount +
                '}';
    }
    
    // Traditional equals and hashCode - manual implementation
    // Modern equivalent would use Lombok @EqualsAndHashCode
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Song song = (Song) obj;
        
        if (songId != null ? !songId.equals(song.songId) : song.songId != null) return false;
        if (songName != null ? !songName.equals(song.songName) : song.songName != null) return false;
        if (artist != null ? !artist.equals(song.artist) : song.artist != null) return false;
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = songId != null ? songId.hashCode() : 0;
        result = 31 * result + (songName != null ? songName.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        return result;
    }
}
