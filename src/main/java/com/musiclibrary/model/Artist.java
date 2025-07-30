package com.musiclibrary.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * Artist JPA Entity
 * 
 * Represents a music artist entity in the music library system using JPA for persistence.
 * Migrated from traditional JDBC to Spring Data JPA with proper entity annotations.
 * 
 * Business Logic:
 * - Artist represents individual musicians or bands in the music catalog
 * - Artist name must be unique across the system for data integrity
 * - Biography and metadata support rich artist information display
 * - Formation year enables chronological organization and historical context
 * - Website links provide external resource integration
 * 
 * JPA Features:
 * - Entity mapping with @Entity and @Table annotations
 * - Primary key generation with @GeneratedValue
 * - One-to-many relationships with @OneToMany
 * - Bean validation with @NotNull, @Size
 * - Audit fields with @CreationTimestamp and @UpdateTimestamp
 * - Unique constraint on artist name for data integrity
 * 
 * @author Music Library Development Team
 * @version 2.0 - Migrated to JPA
 * @since Java 17
 */
@Entity
@Table(name = "artists", uniqueConstraints = {
    @UniqueConstraint(columnNames = "artist_name")
})
public class Artist {
    
    // Primary key with JPA auto-generation
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artist_id")
    private Long artistId;
    
    // Core artist information with JPA validation
    @NotNull(message = "Artist name is required")
    @Size(min = 1, max = 255, message = "Artist name must be between 1 and 255 characters")
    @Column(name = "artist_name", nullable = false, unique = true)
    private String artistName;    // Required - unique artist identifier
    
    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;     // Optional - artist background information
    
    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Column(name = "country")
    private String country;       // Optional - artist origin country
    
    @Min(value = 1800, message = "Formation year must be after 1800")
    @Max(value = 2100, message = "Formation year must be before 2100")
    @Column(name = "formed_year")
    private Integer formedYear;   // Optional - when artist/band was formed
    
    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    @Column(name = "website")
    private String website;       // Optional - official artist website
    
    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Album> albums;   // One artist has many albums
    
    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Song> songs;     // One artist has many songs
    
    // Audit fields with JPA automatic timestamping
    @Column(name = "created_date", nullable = false, updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime createdDate;
    
    @Column(name = "last_modified", nullable = false)
    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime lastModified;
    
    /**
     * Default no-argument constructor required for JavaBean specification.
     * Initializes audit fields with current timestamp.
     */
    public Artist() {
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Constructor for creating new artist with required information.
     * Enforces business rule that artist must have a name for identification.
     * 
     * @param artistName Name of the artist or band (required)
     */
    public Artist(String artistName) {
        this();
        this.artistName = artistName;
    }
    
    /**
     * Constructor for creating artist with core information.
     * 
     * @param artistName Name of the artist (required)
     * @param country Country of origin (optional)
     * @param formedYear Year artist was formed (optional)
     */
    public Artist(String artistName, String country, Integer formedYear) {
        this(artistName);
        this.country = country;
        this.formedYear = formedYear;
    }
    
    // Traditional getter/setter methods following JavaBean conventions
    
    public Long getArtistId() {
        return artistId;
    }
    
    public void setArtistId(Long artistId) {
        this.artistId = artistId;
    }
    
    public String getArtistName() {
        return artistName;
    }
    
    public void setArtistName(String artistName) {
        this.artistName = artistName;
        this.lastModified = LocalDateTime.now(); // Update modification timestamp
    }
    
    public String getBiography() {
        return biography;
    }
    
    public void setBiography(String biography) {
        this.biography = biography;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
        this.lastModified = LocalDateTime.now();
    }
    
    public Integer getFormedYear() {
        return formedYear;
    }
    
    public void setFormedYear(Integer formedYear) {
        this.formedYear = formedYear;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
        this.lastModified = LocalDateTime.now();
    }
    
    public List<Album> getAlbums() {
        return albums;
    }
    
    public void setAlbums(List<Album> albums) {
        this.albums = albums;
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
     * Business validation method to ensure artist has required information.
     * Used by service layer before persisting to database.
     * 
     * @return true if artist has valid data for persistence
     */
    public boolean isValid() {
        return artistName != null && !artistName.trim().isEmpty();
    }
    
    /**
     * Business logic method to get display name with formation year.
     * Useful for UI display when showing artist information.
     * 
     * @return Formatted artist display name
     */
    public String getDisplayName() {
        if (formedYear != null) {
            return artistName + " (" + formedYear + ")";
        }
        return artistName;
    }
    
    /**
     * Business logic method to determine if artist is a band or solo artist.
     * Simple heuristic based on common naming patterns.
     * 
     * @return true if likely a band, false if likely solo artist
     */
    public boolean isBand() {
        if (artistName == null) {
            return false;
        }
        
        String lowerName = artistName.toLowerCase();
        return lowerName.startsWith("the ") || 
               lowerName.contains(" band") ||
               lowerName.contains(" orchestra") ||
               lowerName.contains(" ensemble");
    }
    
    // Traditional toString implementation - verbose Java 7 style
    @Override
    public String toString() {
        return "Artist{" +
                "artistId=" + artistId +
                ", artistName='" + artistName + '\'' +
                ", country='" + country + '\'' +
                ", formedYear=" + formedYear +
                ", website='" + website + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
    
    // Manual equals and hashCode implementation - Java 7 pattern
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Artist artist = (Artist) obj;
        
        if (artistId != null ? !artistId.equals(artist.artistId) : artist.artistId != null) return false;
        if (artistName != null ? !artistName.equals(artist.artistName) : artist.artistName != null) return false;
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = artistId != null ? artistId.hashCode() : 0;
        result = 31 * result + (artistName != null ? artistName.hashCode() : 0);
        return result;
    }
}
