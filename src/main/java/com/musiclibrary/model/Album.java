package com.musiclibrary.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * Album JPA Entity
 * 
 * Represents a music album entity in the music library system using JPA for persistence.
 * An album contains multiple songs and belongs to a specific artist.
 * Migrated from traditional JDBC to Spring Data JPA with proper entity annotations.
 * 
 * Business Logic:
 * - Album represents a collection of songs released together
 * - Each album must belong to an artist (foreign key relationship)
 * - Release date enables chronological organization and discography tracking
 * - Total tracks count maintains album completeness integrity
 * - Genre classification supports music categorization and filtering
 * - Record label information provides industry context
 * 
 * JPA Features:
 * - Entity mapping with @Entity and @Table annotations
 * - Primary key generation with @GeneratedValue
 * - Foreign key relationships with @ManyToOne and @OneToMany
 * - Bean validation with @NotNull, @Size
 * - Audit fields with @CreationTimestamp and @UpdateTimestamp
 * 
 * @author Music Library Development Team
 * @version 2.0 - Migrated to JPA
 * @since Java 17
 */
@Entity
@Table(name = "albums")
public class Album {
    
    // Primary key with JPA auto-generation
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "album_id")
    private Long albumId;
    
    // Core album information with JPA validation
    @NotNull(message = "Album name is required")
    @Size(min = 1, max = 255, message = "Album name must be between 1 and 255 characters")
    @Column(name = "album_name", nullable = false)
    private String albumName;      // Required - album title
    
    @NotNull(message = "Artist is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;         // Many albums belong to one artist
    
    @Column(name = "artist_name")
    private String artistName;     // Denormalized for performance - legacy pattern
    
    @Column(name = "release_date")
    private LocalDate releaseDate; // Album release date
    
    @Size(max = 100, message = "Genre must not exceed 100 characters")
    @Column(name = "genre")
    private String genre;          // Music genre classification
    
    @Size(max = 255, message = "Record label must not exceed 255 characters")
    @Column(name = "record_label")
    private String recordLabel;    // Publishing label information
    
    @Min(value = 0, message = "Total tracks cannot be negative")
    @Column(name = "total_tracks")
    private Integer totalTracks;   // Number of tracks on album
    
    @Column(name = "album_art_path")
    private String albumArtPath;   // Path to album cover image
    
    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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
     * Initializes audit timestamps but leaves business fields null for proper JPA handling.
     */
    public Album() {
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Constructor for creating new album with required information.
     * Enforces business rule that album must have name and artist.
     * 
     * @param albumName Name of the album (required)
     * @param artist Artist entity (required for referential integrity)
     */
    public Album(String albumName, Artist artist) {
        this();
        this.albumName = albumName;
        this.artist = artist;
    }
    
    /**
     * Constructor for creating album with core information.
     * 
     * @param albumName Name of the album (required)
     * @param artist Artist entity (required)
     * @param releaseDate When album was released (optional)
     * @param genre Album genre classification (optional)
     */
    public Album(String albumName, Artist artist, LocalDate releaseDate, String genre) {
        this(albumName, artist);
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
        this.lastModified = LocalDateTime.now();
    }
    
    public Artist getArtist() {
        return artist;
    }
    
    public void setArtist(Artist artist) {
        this.artist = artist;
        this.lastModified = LocalDateTime.now();
    }
    
    public List<Song> getSongs() {
        return songs;
    }
    
    public void setSongs(List<Song> songs) {
        this.songs = songs;
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
    
    public LocalDate getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getRecordLabel() {
        return recordLabel;
    }
    
    public void setRecordLabel(String recordLabel) {
        this.recordLabel = recordLabel;
        this.lastModified = LocalDateTime.now();
    }
    
    public Integer getTotalTracks() {
        return totalTracks;
    }
    
    public void setTotalTracks(Integer totalTracks) {
        this.totalTracks = totalTracks;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getAlbumArtPath() {
        return albumArtPath;
    }
    
    public void setAlbumArtPath(String albumArtPath) {
        this.albumArtPath = albumArtPath;
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
     * Business validation method to ensure album has required information.
     * Used by service layer before persisting to database.
     * 
     * @return true if album has valid data for persistence
     */
    public boolean isValid() {
        return albumName != null && !albumName.trim().isEmpty() &&
               artist != null;
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
        if (artist != null ? !artist.equals(album.artist) : album.artist != null) return false;
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = albumId != null ? albumId.hashCode() : 0;
        result = 31 * result + (albumName != null ? albumName.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        return result;
    }
}
