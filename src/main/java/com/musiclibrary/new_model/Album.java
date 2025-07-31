package com.musiclibrary.new_model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

/**
 * Enhanced Album Model Class
 * 
 * Represents a comprehensive music album entity with extensive metadata,
 * commercial information, and relationship management.
 * 
 * Key Enhancements:
 * - Detailed release and format information
 * - Commercial success metrics and chart performance
 * - Production credits and collaboration details
 * - Multiple format and version support
 * - User engagement and rating systems
 * - Awards and recognition tracking
 * - Distribution and availability management
 * 
 * @author Music Library Development Team
 * @version 2.0
 * @since Java 8+
 */
public class Album {
    
    // Primary Identifiers
    private Long albumId;
    private String albumName;
    private String upc;                     // Universal Product Code
    private String catalogNumber;           // Record label catalog number
    private String barcode;                 // Physical product barcode
    
    // Core Relationships
    private Long primaryArtistId;
    private String primaryArtistName;
    private Set<Long> collaboratingArtistIds;
    private Set<String> collaboratingArtistNames;
    
    // Release Information
    private LocalDate releaseDate;
    private LocalDate originalReleaseDate;  // First release if reissued
    private String releaseType;             // Studio, Live, Compilation, etc.
    private String albumType;               // LP, EP, Single, etc.
    private String country;                 // Country of release
    private Set<String> releaseRegions;     // All countries released
    
    // Label and Production
    private String recordLabel;
    private String distributor;
    private Set<String> producers;
    private Set<String> executiveProducers;
    private Set<String> engineers;
    private Set<String> mixingEngineers;
    private Set<String> masteringEngineers;
    private String recordingStudio;
    private Set<String> additionalStudios;
    
    // Content Information
    private Integer totalTracks;
    private Integer totalDiscs;
    private Duration totalDuration;
    private Set<String> genres;
    private Set<String> subgenres;
    private Set<String> languages;          // Languages of lyrics
    
    // Format and Version Information
    private Set<String> availableFormats;   // CD, Vinyl, Digital, Cassette
    private Boolean isRemaster;
    private Boolean isDeluxeEdition;
    private Boolean isLimitedEdition;
    private Integer limitedEditionSize;
    private String editionDescription;
    private Boolean isExplicit;
    private String contentAdvisory;
    
    // Visual and Media
    private String albumArtUrl;
    private String backCoverUrl;
    private String bookletUrl;
    private Set<String> additionalImages;
    private String videoUrl;                // Album trailer/documentary
    
    // Commercial Performance
    private Long totalSales;
    private Long digitalSales;
    private Long physicalSales;
    private Long streamingCount;
    private Double averageRating;
    private Integer totalRatings;
    private Double popularityScore;
    
    // Chart Performance
    private Integer peakChartPosition;
    private String peakChartCountry;
    private Set<String> chartEntries;       // "US Billboard 200: #5"
    private Set<String> certifications;     // Gold, Platinum, etc.
    private LocalDate certificationDate;
    
    // Awards and Recognition
    private Set<String> awards;             // Grammy, Mercury Prize, etc.
    private Set<String> nominations;
    private Set<String> criticalAcclaim;    // Review scores, accolades
    
    // User Engagement
    private Long favoriteCount;
    private Long shareCount;
    private LocalDateTime lastPlayed;
    private Long playCount;
    private Double completionRate;          // How often played fully
    
    // Availability and Commercial
    private Boolean available;
    private String availabilityRegion;
    private Double price;
    private Boolean streamable;
    private Boolean downloadable;
    private Boolean physicallyAvailable;
    private LocalDate outOfPrintDate;
    
    // Related Albums and Series
    private String albumSeries;             // Greatest Hits, Best of, etc.
    private Integer seriesNumber;           // Volume 1, 2, etc.
    private Long originalAlbumId;           // If this is a reissue
    private Set<Long> relatedAlbumIds;      // Other versions, compilations
    
    // Copyright and Legal
    private String copyright;
    private String publisherRights;
    private String licenseType;
    private LocalDateTime licenseExpiry;
    
    // Tracks Management
    private Set<Long> trackIds;             // Song IDs in album
    private Set<String> bonusTrackIds;      // Special/bonus tracks
    private Set<String> hiddenTrackIds;     // Hidden tracks
    
    // Technical Information
    private String audioFormat;             // FLAC, MP3, etc.
    private Integer bitRate;
    private Integer sampleRate;
    private String dynamicRange;
    private Boolean remasterQuality;
    
    // Marketing and Promotion
    private LocalDate announcementDate;
    private Set<String> singleReleases;     // Singles from this album
    private String promotionalCampaign;
    private Set<String> musicVideos;
    private Boolean hasTour;                // Supporting tour
    private String tourName;
    
    // Audit Information
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    private String lastModifiedBy;
    private Integer version;
    
    /**
     * Default constructor
     */
    public Album() {
        this.collaboratingArtistIds = new HashSet<>();
        this.collaboratingArtistNames = new HashSet<>();
        this.releaseRegions = new HashSet<>();
        this.producers = new HashSet<>();
        this.executiveProducers = new HashSet<>();
        this.engineers = new HashSet<>();
        this.mixingEngineers = new HashSet<>();
        this.masteringEngineers = new HashSet<>();
        this.additionalStudios = new HashSet<>();
        this.genres = new HashSet<>();
        this.subgenres = new HashSet<>();
        this.languages = new HashSet<>();
        this.availableFormats = new HashSet<>();
        this.additionalImages = new HashSet<>();
        this.chartEntries = new HashSet<>();
        this.certifications = new HashSet<>();
        this.awards = new HashSet<>();
        this.nominations = new HashSet<>();
        this.criticalAcclaim = new HashSet<>();
        this.relatedAlbumIds = new HashSet<>();
        this.trackIds = new HashSet<>();
        this.bonusTrackIds = new HashSet<>();
        this.hiddenTrackIds = new HashSet<>();
        this.singleReleases = new HashSet<>();
        this.musicVideos = new HashSet<>();
        
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.version = 1;
        this.isRemaster = false;
        this.isDeluxeEdition = false;
        this.isLimitedEdition = false;
        this.isExplicit = false;
        this.available = true;
        this.streamable = true;
        this.downloadable = false;
        this.physicallyAvailable = true;
        this.remasterQuality = false;
        this.hasTour = false;
        this.totalSales = 0L;
        this.digitalSales = 0L;
        this.physicalSales = 0L;
        this.streamingCount = 0L;
        this.averageRating = 0.0;
        this.totalRatings = 0;
        this.popularityScore = 0.0;
        this.favoriteCount = 0L;
        this.shareCount = 0L;
        this.playCount = 0L;
        this.completionRate = 0.0;
        this.totalTracks = 0;
        this.totalDiscs = 1;
    }
    
    // Getters and Setters
    
    public Long getAlbumId() { return albumId; }
    public void setAlbumId(Long albumId) { this.albumId = albumId; }
    
    public String getAlbumName() { return albumName; }
    public void setAlbumName(String albumName) { this.albumName = albumName; }
    
    public String getUpc() { return upc; }
    public void setUpc(String upc) { this.upc = upc; }
    
    public String getCatalogNumber() { return catalogNumber; }
    public void setCatalogNumber(String catalogNumber) { this.catalogNumber = catalogNumber; }
    
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    
    public Long getPrimaryArtistId() { return primaryArtistId; }
    public void setPrimaryArtistId(Long primaryArtistId) { this.primaryArtistId = primaryArtistId; }
    
    public String getPrimaryArtistName() { return primaryArtistName; }
    public void setPrimaryArtistName(String primaryArtistName) { this.primaryArtistName = primaryArtistName; }
    
    public Set<Long> getCollaboratingArtistIds() { return collaboratingArtistIds; }
    public void setCollaboratingArtistIds(Set<Long> collaboratingArtistIds) { this.collaboratingArtistIds = collaboratingArtistIds; }
    
    public Set<String> getCollaboratingArtistNames() { return collaboratingArtistNames; }
    public void setCollaboratingArtistNames(Set<String> collaboratingArtistNames) { this.collaboratingArtistNames = collaboratingArtistNames; }
    
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    
    public LocalDate getOriginalReleaseDate() { return originalReleaseDate; }
    public void setOriginalReleaseDate(LocalDate originalReleaseDate) { this.originalReleaseDate = originalReleaseDate; }
    
    public String getReleaseType() { return releaseType; }
    public void setReleaseType(String releaseType) { this.releaseType = releaseType; }
    
    public String getAlbumType() { return albumType; }
    public void setAlbumType(String albumType) { this.albumType = albumType; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public Set<String> getReleaseRegions() { return releaseRegions; }
    public void setReleaseRegions(Set<String> releaseRegions) { this.releaseRegions = releaseRegions; }
    
    public String getRecordLabel() { return recordLabel; }
    public void setRecordLabel(String recordLabel) { this.recordLabel = recordLabel; }
    
    public String getDistributor() { return distributor; }
    public void setDistributor(String distributor) { this.distributor = distributor; }
    
    public Set<String> getProducers() { return producers; }
    public void setProducers(Set<String> producers) { this.producers = producers; }
    
    public Set<String> getExecutiveProducers() { return executiveProducers; }
    public void setExecutiveProducers(Set<String> executiveProducers) { this.executiveProducers = executiveProducers; }
    
    public Set<String> getEngineers() { return engineers; }
    public void setEngineers(Set<String> engineers) { this.engineers = engineers; }
    
    public Set<String> getMixingEngineers() { return mixingEngineers; }
    public void setMixingEngineers(Set<String> mixingEngineers) { this.mixingEngineers = mixingEngineers; }
    
    public Set<String> getMasteringEngineers() { return masteringEngineers; }
    public void setMasteringEngineers(Set<String> masteringEngineers) { this.masteringEngineers = masteringEngineers; }
    
    public String getRecordingStudio() { return recordingStudio; }
    public void setRecordingStudio(String recordingStudio) { this.recordingStudio = recordingStudio; }
    
    public Set<String> getAdditionalStudios() { return additionalStudios; }
    public void setAdditionalStudios(Set<String> additionalStudios) { this.additionalStudios = additionalStudios; }
    
    public Integer getTotalTracks() { return totalTracks; }
    public void setTotalTracks(Integer totalTracks) { this.totalTracks = totalTracks; }
    
    public Integer getTotalDiscs() { return totalDiscs; }
    public void setTotalDiscs(Integer totalDiscs) { this.totalDiscs = totalDiscs; }
    
    public Duration getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Duration totalDuration) { this.totalDuration = totalDuration; }
    
    public Set<String> getGenres() { return genres; }
    public void setGenres(Set<String> genres) { this.genres = genres; }
    
    public Set<String> getSubgenres() { return subgenres; }
    public void setSubgenres(Set<String> subgenres) { this.subgenres = subgenres; }
    
    public Set<String> getLanguages() { return languages; }
    public void setLanguages(Set<String> languages) { this.languages = languages; }
    
    public Set<String> getAvailableFormats() { return availableFormats; }
    public void setAvailableFormats(Set<String> availableFormats) { this.availableFormats = availableFormats; }
    
    public Boolean getIsRemaster() { return isRemaster; }
    public void setIsRemaster(Boolean isRemaster) { this.isRemaster = isRemaster; }
    
    public Boolean getIsDeluxeEdition() { return isDeluxeEdition; }
    public void setIsDeluxeEdition(Boolean isDeluxeEdition) { this.isDeluxeEdition = isDeluxeEdition; }
    
    public Boolean getIsLimitedEdition() { return isLimitedEdition; }
    public void setIsLimitedEdition(Boolean isLimitedEdition) { this.isLimitedEdition = isLimitedEdition; }
    
    public Integer getLimitedEditionSize() { return limitedEditionSize; }
    public void setLimitedEditionSize(Integer limitedEditionSize) { this.limitedEditionSize = limitedEditionSize; }
    
    public String getEditionDescription() { return editionDescription; }
    public void setEditionDescription(String editionDescription) { this.editionDescription = editionDescription; }
    
    public Boolean getIsExplicit() { return isExplicit; }
    public void setIsExplicit(Boolean isExplicit) { this.isExplicit = isExplicit; }
    
    public String getContentAdvisory() { return contentAdvisory; }
    public void setContentAdvisory(String contentAdvisory) { this.contentAdvisory = contentAdvisory; }
    
    public String getAlbumArtUrl() { return albumArtUrl; }
    public void setAlbumArtUrl(String albumArtUrl) { this.albumArtUrl = albumArtUrl; }
    
    public String getBackCoverUrl() { return backCoverUrl; }
    public void setBackCoverUrl(String backCoverUrl) { this.backCoverUrl = backCoverUrl; }
    
    public String getBookletUrl() { return bookletUrl; }
    public void setBookletUrl(String bookletUrl) { this.bookletUrl = bookletUrl; }
    
    public Set<String> getAdditionalImages() { return additionalImages; }
    public void setAdditionalImages(Set<String> additionalImages) { this.additionalImages = additionalImages; }
    
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    
    public Long getTotalSales() { return totalSales; }
    public void setTotalSales(Long totalSales) { this.totalSales = totalSales; }
    
    public Long getDigitalSales() { return digitalSales; }
    public void setDigitalSales(Long digitalSales) { this.digitalSales = digitalSales; }
    
    public Long getPhysicalSales() { return physicalSales; }
    public void setPhysicalSales(Long physicalSales) { this.physicalSales = physicalSales; }
    
    public Long getStreamingCount() { return streamingCount; }
    public void setStreamingCount(Long streamingCount) { this.streamingCount = streamingCount; }
    
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    
    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }
    
    public Double getPopularityScore() { return popularityScore; }
    public void setPopularityScore(Double popularityScore) { this.popularityScore = popularityScore; }
    
    public Integer getPeakChartPosition() { return peakChartPosition; }
    public void setPeakChartPosition(Integer peakChartPosition) { this.peakChartPosition = peakChartPosition; }
    
    public String getPeakChartCountry() { return peakChartCountry; }
    public void setPeakChartCountry(String peakChartCountry) { this.peakChartCountry = peakChartCountry; }
    
    public Set<String> getChartEntries() { return chartEntries; }
    public void setChartEntries(Set<String> chartEntries) { this.chartEntries = chartEntries; }
    
    public Set<String> getCertifications() { return certifications; }
    public void setCertifications(Set<String> certifications) { this.certifications = certifications; }
    
    public LocalDate getCertificationDate() { return certificationDate; }
    public void setCertificationDate(LocalDate certificationDate) { this.certificationDate = certificationDate; }
    
    public Set<String> getAwards() { return awards; }
    public void setAwards(Set<String> awards) { this.awards = awards; }
    
    public Set<String> getNominations() { return nominations; }
    public void setNominations(Set<String> nominations) { this.nominations = nominations; }
    
    public Set<String> getCriticalAcclaim() { return criticalAcclaim; }
    public void setCriticalAcclaim(Set<String> criticalAcclaim) { this.criticalAcclaim = criticalAcclaim; }
    
    public Long getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Long favoriteCount) { this.favoriteCount = favoriteCount; }
    
    public Long getShareCount() { return shareCount; }
    public void setShareCount(Long shareCount) { this.shareCount = shareCount; }
    
    public LocalDateTime getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(LocalDateTime lastPlayed) { this.lastPlayed = lastPlayed; }
    
    public Long getPlayCount() { return playCount; }
    public void setPlayCount(Long playCount) { this.playCount = playCount; }
    
    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
    
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    
    public String getAvailabilityRegion() { return availabilityRegion; }
    public void setAvailabilityRegion(String availabilityRegion) { this.availabilityRegion = availabilityRegion; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Boolean getStreamable() { return streamable; }
    public void setStreamable(Boolean streamable) { this.streamable = streamable; }
    
    public Boolean getDownloadable() { return downloadable; }
    public void setDownloadable(Boolean downloadable) { this.downloadable = downloadable; }
    
    public Boolean getPhysicallyAvailable() { return physicallyAvailable; }
    public void setPhysicallyAvailable(Boolean physicallyAvailable) { this.physicallyAvailable = physicallyAvailable; }
    
    public LocalDate getOutOfPrintDate() { return outOfPrintDate; }
    public void setOutOfPrintDate(LocalDate outOfPrintDate) { this.outOfPrintDate = outOfPrintDate; }
    
    public String getAlbumSeries() { return albumSeries; }
    public void setAlbumSeries(String albumSeries) { this.albumSeries = albumSeries; }
    
    public Integer getSeriesNumber() { return seriesNumber; }
    public void setSeriesNumber(Integer seriesNumber) { this.seriesNumber = seriesNumber; }
    
    public Long getOriginalAlbumId() { return originalAlbumId; }
    public void setOriginalAlbumId(Long originalAlbumId) { this.originalAlbumId = originalAlbumId; }
    
    public Set<Long> getRelatedAlbumIds() { return relatedAlbumIds; }
    public void setRelatedAlbumIds(Set<Long> relatedAlbumIds) { this.relatedAlbumIds = relatedAlbumIds; }
    
    public String getCopyright() { return copyright; }
    public void setCopyright(String copyright) { this.copyright = copyright; }
    
    public String getPublisherRights() { return publisherRights; }
    public void setPublisherRights(String publisherRights) { this.publisherRights = publisherRights; }
    
    public String getLicenseType() { return licenseType; }
    public void setLicenseType(String licenseType) { this.licenseType = licenseType; }
    
    public LocalDateTime getLicenseExpiry() { return licenseExpiry; }
    public void setLicenseExpiry(LocalDateTime licenseExpiry) { this.licenseExpiry = licenseExpiry; }
    
    public Set<Long> getTrackIds() { return trackIds; }
    public void setTrackIds(Set<Long> trackIds) { this.trackIds = trackIds; }
    
    public Set<String> getBonusTrackIds() { return bonusTrackIds; }
    public void setBonusTrackIds(Set<String> bonusTrackIds) { this.bonusTrackIds = bonusTrackIds; }
    
    public Set<String> getHiddenTrackIds() { return hiddenTrackIds; }
    public void setHiddenTrackIds(Set<String> hiddenTrackIds) { this.hiddenTrackIds = hiddenTrackIds; }
    
    public String getAudioFormat() { return audioFormat; }
    public void setAudioFormat(String audioFormat) { this.audioFormat = audioFormat; }
    
    public Integer getBitRate() { return bitRate; }
    public void setBitRate(Integer bitRate) { this.bitRate = bitRate; }
    
    public Integer getSampleRate() { return sampleRate; }
    public void setSampleRate(Integer sampleRate) { this.sampleRate = sampleRate; }
    
    public String getDynamicRange() { return dynamicRange; }
    public void setDynamicRange(String dynamicRange) { this.dynamicRange = dynamicRange; }
    
    public Boolean getRemasterQuality() { return remasterQuality; }
    public void setRemasterQuality(Boolean remasterQuality) { this.remasterQuality = remasterQuality; }
    
    public LocalDate getAnnouncementDate() { return announcementDate; }
    public void setAnnouncementDate(LocalDate announcementDate) { this.announcementDate = announcementDate; }
    
    public Set<String> getSingleReleases() { return singleReleases; }
    public void setSingleReleases(Set<String> singleReleases) { this.singleReleases = singleReleases; }
    
    public String getPromotionalCampaign() { return promotionalCampaign; }
    public void setPromotionalCampaign(String promotionalCampaign) { this.promotionalCampaign = promotionalCampaign; }
    
    public Set<String> getMusicVideos() { return musicVideos; }
    public void setMusicVideos(Set<String> musicVideos) { this.musicVideos = musicVideos; }
    
    public Boolean getHasTour() { return hasTour; }
    public void setHasTour(Boolean hasTour) { this.hasTour = hasTour; }
    
    public String getTourName() { return tourName; }
    public void setTourName(String tourName) { this.tourName = tourName; }
    
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
    
    public void addCollaboratingArtist(Long artistId, String artistName) {
        if (collaboratingArtistIds == null) collaboratingArtistIds = new HashSet<>();
        if (collaboratingArtistNames == null) collaboratingArtistNames = new HashSet<>();
        collaboratingArtistIds.add(artistId);
        collaboratingArtistNames.add(artistName);
    }
    
    public Integer getTotalDurationSeconds() {
        return totalDuration != null ? (int) totalDuration.getSeconds() : null;
    }
    
    public void setTotalDurationSeconds(Integer seconds) {
        this.totalDuration = seconds != null ? Duration.ofSeconds(seconds) : null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Album album = (Album) o;
        return Objects.equals(albumId, album.albumId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(albumId);
    }
    
    @Override
    public String toString() {
        return "Album{" +
                "albumId=" + albumId +
                ", albumName='" + albumName + '\'' +
                ", primaryArtistName='" + primaryArtistName + '\'' +
                ", releaseDate=" + releaseDate +
                ", totalTracks=" + totalTracks +
                ", genres=" + genres +
                '}';
    }
}
