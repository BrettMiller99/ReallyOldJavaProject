package com.musiclibrary.new_model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

/**
 * Enhanced Artist Model Class
 * 
 * Represents a comprehensive music artist/band entity with extensive metadata.
 * This enhanced version includes detailed biographical, professional, and statistical information.
 * 
 * Key Enhancements:
 * - Comprehensive biographical and professional information
 * - Band/solo artist distinction with member management
 * - Social media and external platform integration
 * - Performance and touring information
 * - User engagement and statistical metrics
 * - Awards and recognition tracking
 * - Commercial and industry relationships
 * 
 * @author Music Library Development Team
 * @version 2.0
 * @since Java 8+
 */
public class Artist {
    
    // Primary Identifiers
    private Long artistId;
    private String artistName;
    private String legalName;               // Real/legal name if different
    private Set<String> aliases;            // Stage names, former names
    private String spotifyId;               // External platform IDs
    private String appleMusicId;
    private String lastFmId;
    
    // Basic Information
    private String biography;
    private String shortBio;                // Brief description
    private LocalDate birthDate;            // For solo artists
    private LocalDate deathDate;            // If deceased
    private String birthPlace;
    private String currentLocation;
    private String nationality;
    private Set<String> genres;
    private Set<String> instruments;        // Instruments played
    
    // Career Information
    private LocalDate careerStart;
    private LocalDate careerEnd;            // If inactive
    private Boolean isActive;
    private String status;                  // Active, Hiatus, Disbanded, etc.
    private Set<String> labels;             // Record labels
    private String manager;
    private String bookingAgent;
    
    // Band/Group Information
    private Boolean isBand;                 // Band vs solo artist
    private LocalDate formedDate;
    private LocalDate disbandedDate;
    private String formationLocation;
    private Set<String> currentMembers;
    private Set<String> pastMembers;
    private Set<String> memberRoles;        // Lead vocals, guitar, etc.
    
    // Online Presence
    private String officialWebsite;
    private String facebookUrl;
    private String twitterHandle;
    private String instagramHandle;
    private String youtubeChannel;
    private String spotifyUrl;
    private String appleMusicUrl;
    
    // Statistics and Metrics
    private Long totalPlays;
    private Long monthlyListeners;
    private Double averageRating;
    private Integer totalRatings;
    private Long followerCount;
    private Double popularityScore;
    
    // Albums and Discography
    private Integer albumCount;
    private Integer singleCount;
    private Integer epCount;
    private LocalDate lastReleaseDate;
    private LocalDate nextReleaseDate;
    
    // Tours and Performances
    private String currentTour;
    private LocalDate lastTourDate;
    private LocalDate nextTourDate;
    private Integer totalConcerts;
    private Set<String> tourHistory;
    
    // Awards and Recognition
    private Set<String> awards;             // Grammy, Billboard, etc.
    private Set<String> nominations;
    private Set<String> achievements;       // Platinum albums, chart positions
    
    // Commercial Information
    private Boolean available;              // Currently available for streaming
    private String availabilityRegion;
    private Boolean verified;               // Verified artist status
    private String managementCompany;
    private String publishingCompany;
    
    // Images and Media
    private String profileImageUrl;
    private String bannerImageUrl;
    private Set<String> photoUrls;
    private String logoUrl;
    
    // Audit Information
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    private String lastModifiedBy;
    private Integer version;
    
    /**
     * Default constructor
     */
    public Artist() {
        this.aliases = new HashSet<>();
        this.genres = new HashSet<>();
        this.instruments = new HashSet<>();
        this.labels = new HashSet<>();
        this.currentMembers = new HashSet<>();
        this.pastMembers = new HashSet<>();
        this.memberRoles = new HashSet<>();
        this.tourHistory = new HashSet<>();
        this.awards = new HashSet<>();
        this.nominations = new HashSet<>();
        this.achievements = new HashSet<>();
        this.photoUrls = new HashSet<>();
        
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.version = 1;
        this.isActive = true;
        this.isBand = false;
        this.available = true;
        this.verified = false;
        this.totalPlays = 0L;
        this.monthlyListeners = 0L;
        this.averageRating = 0.0;
        this.totalRatings = 0;
        this.followerCount = 0L;
        this.popularityScore = 0.0;
        this.albumCount = 0;
        this.singleCount = 0;
        this.epCount = 0;
        this.totalConcerts = 0;
    }
    
    // Getters and Setters
    
    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }
    
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    
    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    
    public Set<String> getAliases() { return aliases; }
    public void setAliases(Set<String> aliases) { this.aliases = aliases; }
    
    public String getSpotifyId() { return spotifyId; }
    public void setSpotifyId(String spotifyId) { this.spotifyId = spotifyId; }
    
    public String getAppleMusicId() { return appleMusicId; }
    public void setAppleMusicId(String appleMusicId) { this.appleMusicId = appleMusicId; }
    
    public String getLastFmId() { return lastFmId; }
    public void setLastFmId(String lastFmId) { this.lastFmId = lastFmId; }
    
    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }
    
    public String getShortBio() { return shortBio; }
    public void setShortBio(String shortBio) { this.shortBio = shortBio; }
    
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    
    public LocalDate getDeathDate() { return deathDate; }
    public void setDeathDate(LocalDate deathDate) { this.deathDate = deathDate; }
    
    public String getBirthPlace() { return birthPlace; }
    public void setBirthPlace(String birthPlace) { this.birthPlace = birthPlace; }
    
    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }
    
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    
    public Set<String> getGenres() { return genres; }
    public void setGenres(Set<String> genres) { this.genres = genres; }
    
    public Set<String> getInstruments() { return instruments; }
    public void setInstruments(Set<String> instruments) { this.instruments = instruments; }
    
    public LocalDate getCareerStart() { return careerStart; }
    public void setCareerStart(LocalDate careerStart) { this.careerStart = careerStart; }
    
    public LocalDate getCareerEnd() { return careerEnd; }
    public void setCareerEnd(LocalDate careerEnd) { this.careerEnd = careerEnd; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Set<String> getLabels() { return labels; }
    public void setLabels(Set<String> labels) { this.labels = labels; }
    
    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }
    
    public String getBookingAgent() { return bookingAgent; }
    public void setBookingAgent(String bookingAgent) { this.bookingAgent = bookingAgent; }
    
    public Boolean getIsBand() { return isBand; }
    public void setIsBand(Boolean isBand) { this.isBand = isBand; }
    
    public LocalDate getFormedDate() { return formedDate; }
    public void setFormedDate(LocalDate formedDate) { this.formedDate = formedDate; }
    
    public LocalDate getDisbandedDate() { return disbandedDate; }
    public void setDisbandedDate(LocalDate disbandedDate) { this.disbandedDate = disbandedDate; }
    
    public String getFormationLocation() { return formationLocation; }
    public void setFormationLocation(String formationLocation) { this.formationLocation = formationLocation; }
    
    public Set<String> getCurrentMembers() { return currentMembers; }
    public void setCurrentMembers(Set<String> currentMembers) { this.currentMembers = currentMembers; }
    
    public Set<String> getPastMembers() { return pastMembers; }
    public void setPastMembers(Set<String> pastMembers) { this.pastMembers = pastMembers; }
    
    public Set<String> getMemberRoles() { return memberRoles; }
    public void setMemberRoles(Set<String> memberRoles) { this.memberRoles = memberRoles; }
    
    public String getOfficialWebsite() { return officialWebsite; }
    public void setOfficialWebsite(String officialWebsite) { this.officialWebsite = officialWebsite; }
    
    public String getFacebookUrl() { return facebookUrl; }
    public void setFacebookUrl(String facebookUrl) { this.facebookUrl = facebookUrl; }
    
    public String getTwitterHandle() { return twitterHandle; }
    public void setTwitterHandle(String twitterHandle) { this.twitterHandle = twitterHandle; }
    
    public String getInstagramHandle() { return instagramHandle; }
    public void setInstagramHandle(String instagramHandle) { this.instagramHandle = instagramHandle; }
    
    public String getYoutubeChannel() { return youtubeChannel; }
    public void setYoutubeChannel(String youtubeChannel) { this.youtubeChannel = youtubeChannel; }
    
    public String getSpotifyUrl() { return spotifyUrl; }
    public void setSpotifyUrl(String spotifyUrl) { this.spotifyUrl = spotifyUrl; }
    
    public String getAppleMusicUrl() { return appleMusicUrl; }
    public void setAppleMusicUrl(String appleMusicUrl) { this.appleMusicUrl = appleMusicUrl; }
    
    public Long getTotalPlays() { return totalPlays; }
    public void setTotalPlays(Long totalPlays) { this.totalPlays = totalPlays; }
    
    public Long getMonthlyListeners() { return monthlyListeners; }
    public void setMonthlyListeners(Long monthlyListeners) { this.monthlyListeners = monthlyListeners; }
    
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    
    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }
    
    public Long getFollowerCount() { return followerCount; }
    public void setFollowerCount(Long followerCount) { this.followerCount = followerCount; }
    
    public Double getPopularityScore() { return popularityScore; }
    public void setPopularityScore(Double popularityScore) { this.popularityScore = popularityScore; }
    
    public Integer getAlbumCount() { return albumCount; }
    public void setAlbumCount(Integer albumCount) { this.albumCount = albumCount; }
    
    public Integer getSingleCount() { return singleCount; }
    public void setSingleCount(Integer singleCount) { this.singleCount = singleCount; }
    
    public Integer getEpCount() { return epCount; }
    public void setEpCount(Integer epCount) { this.epCount = epCount; }
    
    public LocalDate getLastReleaseDate() { return lastReleaseDate; }
    public void setLastReleaseDate(LocalDate lastReleaseDate) { this.lastReleaseDate = lastReleaseDate; }
    
    public LocalDate getNextReleaseDate() { return nextReleaseDate; }
    public void setNextReleaseDate(LocalDate nextReleaseDate) { this.nextReleaseDate = nextReleaseDate; }
    
    public String getCurrentTour() { return currentTour; }
    public void setCurrentTour(String currentTour) { this.currentTour = currentTour; }
    
    public LocalDate getLastTourDate() { return lastTourDate; }
    public void setLastTourDate(LocalDate lastTourDate) { this.lastTourDate = lastTourDate; }
    
    public LocalDate getNextTourDate() { return nextTourDate; }
    public void setNextTourDate(LocalDate nextTourDate) { this.nextTourDate = nextTourDate; }
    
    public Integer getTotalConcerts() { return totalConcerts; }
    public void setTotalConcerts(Integer totalConcerts) { this.totalConcerts = totalConcerts; }
    
    public Set<String> getTourHistory() { return tourHistory; }
    public void setTourHistory(Set<String> tourHistory) { this.tourHistory = tourHistory; }
    
    public Set<String> getAwards() { return awards; }
    public void setAwards(Set<String> awards) { this.awards = awards; }
    
    public Set<String> getNominations() { return nominations; }
    public void setNominations(Set<String> nominations) { this.nominations = nominations; }
    
    public Set<String> getAchievements() { return achievements; }
    public void setAchievements(Set<String> achievements) { this.achievements = achievements; }
    
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    
    public String getAvailabilityRegion() { return availabilityRegion; }
    public void setAvailabilityRegion(String availabilityRegion) { this.availabilityRegion = availabilityRegion; }
    
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    
    public String getManagementCompany() { return managementCompany; }
    public void setManagementCompany(String managementCompany) { this.managementCompany = managementCompany; }
    
    public String getPublishingCompany() { return publishingCompany; }
    public void setPublishingCompany(String publishingCompany) { this.publishingCompany = publishingCompany; }
    
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    
    public String getBannerImageUrl() { return bannerImageUrl; }
    public void setBannerImageUrl(String bannerImageUrl) { this.bannerImageUrl = bannerImageUrl; }
    
    public Set<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(Set<String> photoUrls) { this.photoUrls = photoUrls; }
    
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    
    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
    
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    
    // Utility Methods
    
    public void touch() {
        this.lastModified = LocalDateTime.now();
        this.version++;
    }
    
    public void addGenre(String genre) {
        if (genres == null) genres = new HashSet<>();
        genres.add(genre);
    }
    
    public void addAlias(String alias) {
        if (aliases == null) aliases = new HashSet<>();
        aliases.add(alias);
    }
    
    public void addInstrument(String instrument) {
        if (instruments == null) instruments = new HashSet<>();
        instruments.add(instrument);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artist artist = (Artist) o;
        return Objects.equals(artistId, artist.artistId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(artistId);
    }
    
    @Override
    public String toString() {
        return "Artist{" +
                "artistId=" + artistId +
                ", artistName='" + artistName + '\'' +
                ", isBand=" + isBand +
                ", genres=" + genres +
                ", isActive=" + isActive +
                '}';
    }
}
