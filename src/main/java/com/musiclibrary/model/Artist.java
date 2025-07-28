package com.musiclibrary.model;

import java.util.Date;

/**
 * Artist Model Class
 * 
 * Represents a music artist entity in the music library system.
 * This class follows traditional Java 7 JavaBean patterns for enterprise compatibility.
 * 
 * Business Logic:
 * - Artist represents individual musicians or bands in the music catalog
 * - Artist name must be unique across the system for data integrity
 * - Biography and metadata support rich artist information display
 * - Formation year enables chronological organization and historical context
 * - Website links provide external resource integration
 * 
 * Migration Opportunities:
 * - Manual getter/setter -> Lombok annotations
 * - Date type -> LocalDateTime (Java 8+)
 * - Manual validation -> Bean Validation (@NotNull, @Size, etc.)
 * - Traditional constructors -> Builder pattern
 * - Manual JSON handling -> Jackson annotations
 * - Basic String fields -> Optional wrapper for nullable fields
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class Artist {
    
    // Primary key using traditional Long wrapper
    private Long artistId;
    
    // Core artist information
    private String artistName;    // Required - unique artist identifier
    private String biography;     // Optional - artist background information
    private String country;       // Optional - artist origin country
    private Integer formedYear;   // Optional - when artist/band was formed
    private String website;       // Optional - official artist website
    
    // Audit trail fields - standard enterprise pattern
    private Date createdDate;
    private Date lastModified;
    
    /**
     * Default no-argument constructor required for JavaBean specification.
     * Initializes audit fields with current timestamp.
     */
    public Artist() {
        this.createdDate = new Date();
        this.lastModified = new Date();
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
        this.lastModified = new Date(); // Update modification timestamp
    }
    
    public String getBiography() {
        return biography;
    }
    
    public void setBiography(String biography) {
        this.biography = biography;
        this.lastModified = new Date();
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
        this.lastModified = new Date();
    }
    
    public Integer getFormedYear() {
        return formedYear;
    }
    
    public void setFormedYear(Integer formedYear) {
        this.formedYear = formedYear;
        this.lastModified = new Date();
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
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
