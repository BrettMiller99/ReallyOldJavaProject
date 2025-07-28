package com.musiclibrary.model;

import java.util.Date;

/**
 * Song Model Class
 * 
 * Represents a song entity in the music library system.
 * This class follows traditional Java 7 JavaBean patterns with verbose getter/setter methods.
 * 
 * Business Logic:
 * - Song must have a name, artist, and track length (core requirements)
 * - Track length is stored in seconds for precise duration calculations
 * - Rating system uses 0-5 scale for user feedback
 * - Play count tracks usage statistics for recommendation algorithms
 * - File path enables integration with actual music file storage
 * 
 * Migration Opportunities:
 * - Manual getter/setter generation -> Lombok annotations
 * - Date type -> LocalDateTime/LocalDate (Java 8+)
 * - Manual validation -> Bean Validation annotations
 * - Manual JSON serialization -> Jackson annotations
 * - Traditional constructors -> Builder pattern
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class Song {
    
    // Primary key - traditional long type approach
    private Long songId;
    
    // Required fields as per specification
    private String songName;      // Required: SongName field
    private String albumName;     // Required: AlbumName field  
    private Date dateReleased;    // Required: DateReleased field
    private Integer trackLength;  // Required: TrackLength field (in seconds)
    
    // Additional business-relevant fields
    private Long albumId;         // Foreign key to albums table
    private Long artistId;        // Foreign key to artists table - required for business logic
    private String artistName;    // Denormalized for performance - typical legacy pattern
    private Integer trackNumber;  // Track position within album
    private String genre;         // Music genre classification
    private String filePath;      // Path to actual music file
    private Long fileSize;        // File size in bytes
    private Integer bitrate;      // Audio quality indicator
    private Integer rating;       // User rating 0-5 stars
    private Integer playCount;    // Usage tracking for analytics
    private Date lastPlayed;      // Most recent play timestamp
    private String lyrics;        // Song lyrics content
    
    // Audit fields - traditional enterprise pattern
    private Date createdDate;
    private Date lastModified;
    
    /**
     * Default constructor required for JavaBean specification.
     * Essential for reflection-based frameworks and JSON serialization.
     */
    public Song() {
        // Initialize default values following business rules
        this.rating = 0;
        this.playCount = 0;
        this.createdDate = new Date();
        this.lastModified = new Date();
    }
    
    /**
     * Constructor for creating new songs with required fields.
     * Enforces business rule that songs must have basic information.
     * 
     * @param songName Name of the song (required)
     * @param artistId Artist identifier (required for referential integrity)
     * @param trackLength Duration in seconds (required for playback)
     * @param dateReleased Release date (required for chronological sorting)
     */
    public Song(String songName, Long artistId, Integer trackLength, Date dateReleased) {
        this(); // Call default constructor for initialization
        this.songName = songName;
        this.artistId = artistId;
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
        this.lastModified = new Date(); // Audit trail
    }
    
    public String getAlbumName() {
        return albumName;
    }
    
    public void setAlbumName(String albumName) {
        this.albumName = albumName;
        this.lastModified = new Date();
    }
    
    public Date getDateReleased() {
        return dateReleased;
    }
    
    public void setDateReleased(Date dateReleased) {
        this.dateReleased = dateReleased;
        this.lastModified = new Date();
    }
    
    public Integer getTrackLength() {
        return trackLength;
    }
    
    public void setTrackLength(Integer trackLength) {
        this.trackLength = trackLength;
        this.lastModified = new Date();
    }
    
    public Long getAlbumId() {
        return albumId;
    }
    
    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
        this.lastModified = new Date();
    }
    
    public Long getArtistId() {
        return artistId;
    }
    
    public void setArtistId(Long artistId) {
        this.artistId = artistId;
        this.lastModified = new Date();
    }
    
    public String getArtistName() {
        return artistName;
    }
    
    public void setArtistName(String artistName) {
        this.artistName = artistName;
        this.lastModified = new Date();
    }
    
    public Integer getTrackNumber() {
        return trackNumber;
    }
    
    public void setTrackNumber(Integer trackNumber) {
        this.trackNumber = trackNumber;
        this.lastModified = new Date();
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
        this.lastModified = new Date();
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
        this.lastModified = new Date();
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
        this.lastModified = new Date();
    }
    
    public Integer getBitrate() {
        return bitrate;
    }
    
    public void setBitrate(Integer bitrate) {
        this.bitrate = bitrate;
        this.lastModified = new Date();
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
        this.lastModified = new Date();
    }
    
    public Integer getPlayCount() {
        return playCount;
    }
    
    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
        this.lastModified = new Date();
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
        this.lastPlayed = new Date();
        this.lastModified = new Date();
    }
    
    public Date getLastPlayed() {
        return lastPlayed;
    }
    
    public void setLastPlayed(Date lastPlayed) {
        this.lastPlayed = lastPlayed;
        this.lastModified = new Date();
    }
    
    public String getLyrics() {
        return lyrics;
    }
    
    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
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
               artistId != null &&
               trackLength != null && trackLength > 0 &&
               dateReleased != null;
    }
    
    // Traditional toString method - verbose Java 7 approach
    // Modern equivalent would use Lombok @ToString or Apache Commons
    @Override
    public String toString() {
        return "Song{" +
                "songId=" + songId +
                ", songName='" + songName + '\'' +
                ", albumName='" + albumName + '\'' +
                ", artistName='" + artistName + '\'' +
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
        if (artistId != null ? !artistId.equals(song.artistId) : song.artistId != null) return false;
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = songId != null ? songId.hashCode() : 0;
        result = 31 * result + (songName != null ? songName.hashCode() : 0);
        result = 31 * result + (artistId != null ? artistId.hashCode() : 0);
        return result;
    }
}
