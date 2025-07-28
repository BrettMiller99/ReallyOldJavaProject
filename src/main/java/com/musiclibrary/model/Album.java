package com.musiclibrary.model;

import java.util.Date;

/**
 * Album Model Class
 * 
 * Represents a music album entity in the music library system.
 * An album contains multiple songs and belongs to a specific artist.
 * This class follows traditional Java 7 JavaBean patterns.
 * 
 * Business Logic:
 * - Album represents a collection of songs released together
 * - Each album must belong to an artist (foreign key relationship)
 * - Release date enables chronological organization and discography tracking
 * - Total tracks count maintains album completeness integrity
 * - Genre classification supports music categorization and filtering
 * - Record label information provides industry context
 * 
 * Migration Opportunities:
 * - Manual getter/setter -> Lombok annotations
 * - Date type -> LocalDate (Java 8+)
 * - Manual validation -> Bean Validation annotations
 * - Traditional constructors -> Builder pattern
 * - Manual JSON handling -> Jackson annotations
 * - Basic relationship handling -> JPA @ManyToOne annotations
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class Album {
    
    // Primary key using traditional Long wrapper
    private Long albumId;
    
    // Core album information
    private String albumName;      // Required - album title
    private Long artistId;         // Required - foreign key to artists table
    private String artistName;     // Denormalized for performance - legacy pattern
    private Date releaseDate;      // Album release date
    private String genre;          // Music genre classification
    private String recordLabel;    // Publishing label information
    private Integer totalTracks;   // Number of tracks on album
    private String albumArtPath;   // Path to album cover image
    
    // Audit trail fields - enterprise standard
    private Date createdDate;
    private Date lastModified;
    
    /**
     * Default no-argument constructor required for JavaBean specification.
     * Initializes default values and audit timestamps.
     */
    public Album() {
        this.totalTracks = 0;
        this.createdDate = new Date();
        this.lastModified = new Date();
    }
    
    /**
     * Constructor for creating new album with required information.
     * Enforces business rule that album must have name and artist.
     * 
     * @param albumName Name of the album (required)
     * @param artistId Artist identifier (required for referential integrity)
     */
    public Album(String albumName, Long artistId) {
        this();
        this.albumName = albumName;
        this.artistId = artistId;
    }
    
    /**
     * Constructor for creating album with core information.
     * 
     * @param albumName Name of the album (required)
     * @param artistId Artist identifier (required)
     * @param releaseDate When album was released (optional)
     * @param genre Album genre classification (optional)
     */
    public Album(String albumName, Long artistId, Date releaseDate, String genre) {
        this(albumName, artistId);
        this.releaseDate = releaseDate;
        this.genre = genre;
    }
    
    // Traditional getter/setter methods following JavaBean conventions
    
    public Long getAlbumId() {
        return albumId;
    }
    
    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }
    
    public String getAlbumName() {
        return albumName;
    }
    
    public void setAlbumName(String albumName) {
        this.albumName = albumName;
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
    
    public Date getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
        this.lastModified = new Date();
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
        this.lastModified = new Date();
    }
    
    public String getRecordLabel() {
        return recordLabel;
    }
    
    public void setRecordLabel(String recordLabel) {
        this.recordLabel = recordLabel;
        this.lastModified = new Date();
    }
    
    public Integer getTotalTracks() {
        return totalTracks;
    }
    
    public void setTotalTracks(Integer totalTracks) {
        this.totalTracks = totalTracks;
        this.lastModified = new Date();
    }
    
    public String getAlbumArtPath() {
        return albumArtPath;
    }
    
    public void setAlbumArtPath(String albumArtPath) {
        this.albumArtPath = albumArtPath;
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
     * Business validation method to ensure album has required information.
     * Used by service layer before persisting to database.
     * 
     * @return true if album has valid data for persistence
     */
    public boolean isValid() {
        return albumName != null && !albumName.trim().isEmpty() &&
               artistId != null;
    }
    
    /**
     * Business logic method to get formatted release year.
     * Extracts year from release date for display purposes.
     * 
     * @return Release year as string, or "Unknown" if not available
     */
    public String getReleaseYear() {
        if (releaseDate == null) {
            return "Unknown";
        }
        
        // Traditional Java 7 date handling - migration opportunity
        @SuppressWarnings("deprecation")
        int year = releaseDate.getYear() + 1900;
        return String.valueOf(year);
    }
    
    /**
     * Business logic method to create album display title.
     * Combines album name with artist for complete identification.
     * 
     * @return Formatted album display title
     */
    public String getDisplayTitle() {
        if (artistName != null) {
            return albumName + " by " + artistName;
        }
        return albumName;
    }
    
    /**
     * Business logic method to determine if album is a single.
     * Albums with 1-3 tracks are typically considered singles.
     * 
     * @return true if album appears to be a single release
     */
    public boolean isSingle() {
        return totalTracks != null && totalTracks <= 3;
    }
    
    /**
     * Business logic method to determine if album is an EP.
     * Extended Plays typically have 4-6 tracks.
     * 
     * @return true if album appears to be an EP
     */
    public boolean isEP() {
        return totalTracks != null && totalTracks >= 4 && totalTracks <= 6;
    }
    
    /**
     * Business logic method to determine if album is full-length.
     * Full albums typically have 7 or more tracks.
     * 
     * @return true if album appears to be a full-length release
     */
    public boolean isFullAlbum() {
        return totalTracks != null && totalTracks >= 7;
    }
    
    // Traditional toString implementation - verbose Java 7 style
    @Override
    public String toString() {
        return "Album{" +
                "albumId=" + albumId +
                ", albumName='" + albumName + '\'' +
                ", artistName='" + artistName + '\'' +
                ", releaseDate=" + releaseDate +
                ", genre='" + genre + '\'' +
                ", recordLabel='" + recordLabel + '\'' +
                ", totalTracks=" + totalTracks +
                '}';
    }
    
    // Manual equals and hashCode implementation - Java 7 pattern
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Album album = (Album) obj;
        
        if (albumId != null ? !albumId.equals(album.albumId) : album.albumId != null) return false;
        if (albumName != null ? !albumName.equals(album.albumName) : album.albumName != null) return false;
        if (artistId != null ? !artistId.equals(album.artistId) : album.artistId != null) return false;
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = albumId != null ? albumId.hashCode() : 0;
        result = 31 * result + (albumName != null ? albumName.hashCode() : 0);
        result = 31 * result + (artistId != null ? artistId.hashCode() : 0);
        return result;
    }
}
