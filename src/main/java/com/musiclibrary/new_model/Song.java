package com.musiclibrary.new_model;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

/**
 * Enhanced Song Model Class
 * 
 * Represents a comprehensive song entity in the music library system.
 * This enhanced version includes extensive metadata, relationships, and modern design patterns.
 * 
 * Key Enhancements:
 * - Comprehensive audio metadata (BPM, key, mood, energy level)
 * - Detailed technical specifications (format, quality, codec)
 * - Rich content metadata (lyrics, credits, songwriters)
 * - User engagement metrics (ratings, play stats, favorites)
 * - Advanced categorization (genres, tags, moods)
 * - Relationship management (featured artists, contributors)
 * - Accessibility and discovery features
 * 
 * Business Logic:
 * - Songs can have multiple genres and featured artists
 * - Comprehensive user engagement tracking for recommendations
 * - Rich metadata supports advanced search and filtering
 * - Audio analysis data enables playlist generation algorithms
 * - Copyright and licensing information for legal compliance
 * 
 * Migration Opportunities:
 * - Could use Builder pattern for complex object construction
 * - Consider JPA annotations for ORM mapping
 * - Bean Validation annotations for input validation
 * - Jackson annotations for JSON serialization
 * 
 * @author Music Library Development Team
 * @version 2.0
 * @since Java 8+
 */
public class Song {
    
    // Primary Identifiers
    private Long songId;
    private String songName;
    private String isrc;                    // International Standard Recording Code
    
    // Core Relationships
    private Long albumId;
    private String albumName;
    private Long primaryArtistId;
    private String primaryArtistName;
    private Set<Long> featuredArtistIds;    // Featured/collaborating artists
    private Set<String> featuredArtistNames;
    
    // Release Information
    private LocalDateTime releaseDate;
    private LocalDateTime originalReleaseDate;  // Different from album release
    private String releasedBy;              // Record label/distributor
    private String country;                 // Country of release
    private String language;                // Primary language of vocals
    
    // Audio Technical Specifications
    private Duration trackLength;           // Using Duration instead of seconds
    private Integer trackNumber;            // Position in album
    private Integer discNumber;             // Multi-disc albums
    private String audioFormat;             // MP3, FLAC, WAV, etc.
    private Integer bitrate;                // Audio quality (kbps)
    private Integer sampleRate;             // Audio quality (Hz)
    private String codec;                   // Audio codec used
    private Long fileSize;                  // File size in bytes
    private String filePath;                // Path to audio file
    private String checksum;                // File integrity verification
    
    // Musical Analysis & Metadata
    private Double bpm;                     // Beats per minute
    private String musicalKey;              // Key signature (C Major, F# Minor, etc.)
    private String timeSignature;           // 4/4, 3/4, etc.
    private Double energy;                  // Energy level 0.0-1.0
    private Double danceability;            // Danceability score 0.0-1.0
    private Double valence;                 // Positivity/mood score 0.0-1.0
    private Double acousticness;            // Acoustic vs electronic 0.0-1.0
    private Double instrumentalness;        // Likelihood of being instrumental
    private Double speechiness;             // Spoken word vs music ratio
    private Double loudness;                // Overall loudness in dB
    
    // Content & Creative Information
    private String lyrics;                  // Full song lyrics
    private String lyricsLanguage;          // Language of lyrics
    private Set<String> lyricists;          // Lyric writers
    private Set<String> composers;          // Music composers
    private Set<String> producers;          // Song producers
    private Set<String> writers;            // Additional writers/contributors
    private String publisher;               // Music publisher
    private String copyright;               // Copyright information
    
    // Categorization & Discovery
    private Set<String> genres;             // Multiple genres supported
    private Set<String> subgenres;          // More specific categorization
    private Set<String> moods;              // Emotional categorization
    private Set<String> tags;               // User-generated tags
    private String decade;                  // 1960s, 1970s, etc.
    private String era;                     // Classical, Modern, Contemporary
    
    // User Engagement & Statistics
    private Double averageRating;           // Average user rating 0.0-5.0
    private Integer totalRatings;           // Number of ratings received
    private Long totalPlayCount;            // All-time play count
    private Long recentPlayCount;           // Recent period play count
    private Long skipCount;                 // Number of times skipped
    private Long favoriteCount;             // Number of users who favorited
    private Double popularityScore;         // Algorithm-calculated popularity
    private LocalDateTime lastPlayed;       // Most recent play timestamp
    
    // Content Advisory & Accessibility
    private Boolean explicitContent;        // Contains explicit lyrics
    private String contentAdvisory;         // Reason for advisory rating
    private Boolean instrumentalVersion;    // Is this an instrumental version
    private Boolean liveRecording;          // Live vs studio recording
    private Boolean remix;                  // Is this a remix
    private String remixArtist;             // Who created the remix
    private Boolean karaoke;                // Karaoke/backing track version
    
    // Album Context
    private Boolean isBonus;                // Bonus track on album
    private Boolean isHidden;               // Hidden/secret track
    private Boolean isSingle;               // Released as single
    private LocalDateTime singleReleaseDate; // Single release date if different
    
    // Commercial & Legal
    private Boolean available;              // Currently available for play
    private String availabilityRegion;      // Geographic availability
    private Double price;                   // Purchase price if applicable
    private Boolean streamable;             // Available for streaming
    private Boolean downloadable;           // Available for download
    private String licenseType;             // License terms
    private LocalDateTime licenseExpiry;    // When license expires
    
    // Audit Information
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    private String lastModifiedBy;
    private Integer version;                // Version control for updates
    
    /**
     * Default constructor initializes collections and sets default values
     */
    public Song() {
        this.featuredArtistIds = new HashSet<>();
        this.featuredArtistNames = new HashSet<>();
        this.genres = new HashSet<>();
        this.subgenres = new HashSet<>();
        this.moods = new HashSet<>();
        this.tags = new HashSet<>();
        this.lyricists = new HashSet<>();
        this.composers = new HashSet<>();
        this.producers = new HashSet<>();
        this.writers = new HashSet<>();
        
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.version = 1;
        this.explicitContent = false;
        this.instrumentalVersion = false;
        this.liveRecording = false;
        this.remix = false;
        this.karaoke = false;
        this.isBonus = false;
        this.isHidden = false;
        this.isSingle = false;
        this.available = true;
        this.streamable = true;
        this.downloadable = false;
        this.averageRating = 0.0;
        this.totalRatings = 0;
        this.totalPlayCount = 0L;
        this.recentPlayCount = 0L;
        this.skipCount = 0L;
        this.favoriteCount = 0L;
        this.popularityScore = 0.0;
    }
    
    // Getters and Setters (following traditional JavaBean pattern)
    
    public Long getSongId() { return songId; }
    public void setSongId(Long songId) { this.songId = songId; }
    
    public String getSongName() { return songName; }
    public void setSongName(String songName) { this.songName = songName; }
    
    public String getIsrc() { return isrc; }
    public void setIsrc(String isrc) { this.isrc = isrc; }
    
    public Long getAlbumId() { return albumId; }
    public void setAlbumId(Long albumId) { this.albumId = albumId; }
    
    public String getAlbumName() { return albumName; }
    public void setAlbumName(String albumName) { this.albumName = albumName; }
    
    public Long getPrimaryArtistId() { return primaryArtistId; }
    public void setPrimaryArtistId(Long primaryArtistId) { this.primaryArtistId = primaryArtistId; }
    
    public String getPrimaryArtistName() { return primaryArtistName; }
    public void setPrimaryArtistName(String primaryArtistName) { this.primaryArtistName = primaryArtistName; }
    
    public Set<Long> getFeaturedArtistIds() { return featuredArtistIds; }
    public void setFeaturedArtistIds(Set<Long> featuredArtistIds) { this.featuredArtistIds = featuredArtistIds; }
    
    public Set<String> getFeaturedArtistNames() { return featuredArtistNames; }
    public void setFeaturedArtistNames(Set<String> featuredArtistNames) { this.featuredArtistNames = featuredArtistNames; }
    
    public LocalDateTime getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDateTime releaseDate) { this.releaseDate = releaseDate; }
    
    public LocalDateTime getOriginalReleaseDate() { return originalReleaseDate; }
    public void setOriginalReleaseDate(LocalDateTime originalReleaseDate) { this.originalReleaseDate = originalReleaseDate; }
    
    public String getReleasedBy() { return releasedBy; }
    public void setReleasedBy(String releasedBy) { this.releasedBy = releasedBy; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public Duration getTrackLength() { return trackLength; }
    public void setTrackLength(Duration trackLength) { this.trackLength = trackLength; }
    
    public Integer getTrackNumber() { return trackNumber; }
    public void setTrackNumber(Integer trackNumber) { this.trackNumber = trackNumber; }
    
    public Integer getDiscNumber() { return discNumber; }
    public void setDiscNumber(Integer discNumber) { this.discNumber = discNumber; }
    
    public String getAudioFormat() { return audioFormat; }
    public void setAudioFormat(String audioFormat) { this.audioFormat = audioFormat; }
    
    public Integer getBitrate() { return bitrate; }
    public void setBitrate(Integer bitrate) { this.bitrate = bitrate; }
    
    public Integer getSampleRate() { return sampleRate; }
    public void setSampleRate(Integer sampleRate) { this.sampleRate = sampleRate; }
    
    public String getCodec() { return codec; }
    public void setCodec(String codec) { this.codec = codec; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
    
    public Double getBpm() { return bpm; }
    public void setBpm(Double bpm) { this.bpm = bpm; }
    
    public String getMusicalKey() { return musicalKey; }
    public void setMusicalKey(String musicalKey) { this.musicalKey = musicalKey; }
    
    public String getTimeSignature() { return timeSignature; }
    public void setTimeSignature(String timeSignature) { this.timeSignature = timeSignature; }
    
    public Double getEnergy() { return energy; }
    public void setEnergy(Double energy) { this.energy = energy; }
    
    public Double getDanceability() { return danceability; }
    public void setDanceability(Double danceability) { this.danceability = danceability; }
    
    public Double getValence() { return valence; }
    public void setValence(Double valence) { this.valence = valence; }
    
    public Double getAcousticness() { return acousticness; }
    public void setAcousticness(Double acousticness) { this.acousticness = acousticness; }
    
    public Double getInstrumentalness() { return instrumentalness; }
    public void setInstrumentalness(Double instrumentalness) { this.instrumentalness = instrumentalness; }
    
    public Double getSpeechiness() { return speechiness; }
    public void setSpeechiness(Double speechiness) { this.speechiness = speechiness; }
    
    public Double getLoudness() { return loudness; }
    public void setLoudness(Double loudness) { this.loudness = loudness; }
    
    public String getLyrics() { return lyrics; }
    public void setLyrics(String lyrics) { this.lyrics = lyrics; }
    
    public String getLyricsLanguage() { return lyricsLanguage; }
    public void setLyricsLanguage(String lyricsLanguage) { this.lyricsLanguage = lyricsLanguage; }
    
    public Set<String> getLyricists() { return lyricists; }
    public void setLyricists(Set<String> lyricists) { this.lyricists = lyricists; }
    
    public Set<String> getComposers() { return composers; }
    public void setComposers(Set<String> composers) { this.composers = composers; }
    
    public Set<String> getProducers() { return producers; }
    public void setProducers(Set<String> producers) { this.producers = producers; }
    
    public Set<String> getWriters() { return writers; }
    public void setWriters(Set<String> writers) { this.writers = writers; }
    
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    
    public String getCopyright() { return copyright; }
    public void setCopyright(String copyright) { this.copyright = copyright; }
    
    public Set<String> getGenres() { return genres; }
    public void setGenres(Set<String> genres) { this.genres = genres; }
    
    public Set<String> getSubgenres() { return subgenres; }
    public void setSubgenres(Set<String> subgenres) { this.subgenres = subgenres; }
    
    public Set<String> getMoods() { return moods; }
    public void setMoods(Set<String> moods) { this.moods = moods; }
    
    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    
    public String getDecade() { return decade; }
    public void setDecade(String decade) { this.decade = decade; }
    
    public String getEra() { return era; }
    public void setEra(String era) { this.era = era; }
    
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    
    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }
    
    public Long getTotalPlayCount() { return totalPlayCount; }
    public void setTotalPlayCount(Long totalPlayCount) { this.totalPlayCount = totalPlayCount; }
    
    public Long getRecentPlayCount() { return recentPlayCount; }
    public void setRecentPlayCount(Long recentPlayCount) { this.recentPlayCount = recentPlayCount; }
    
    public Long getSkipCount() { return skipCount; }
    public void setSkipCount(Long skipCount) { this.skipCount = skipCount; }
    
    public Long getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Long favoriteCount) { this.favoriteCount = favoriteCount; }
    
    public Double getPopularityScore() { return popularityScore; }
    public void setPopularityScore(Double popularityScore) { this.popularityScore = popularityScore; }
    
    public LocalDateTime getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(LocalDateTime lastPlayed) { this.lastPlayed = lastPlayed; }
    
    public Boolean getExplicitContent() { return explicitContent; }
    public void setExplicitContent(Boolean explicitContent) { this.explicitContent = explicitContent; }
    
    public String getContentAdvisory() { return contentAdvisory; }
    public void setContentAdvisory(String contentAdvisory) { this.contentAdvisory = contentAdvisory; }
    
    public Boolean getInstrumentalVersion() { return instrumentalVersion; }
    public void setInstrumentalVersion(Boolean instrumentalVersion) { this.instrumentalVersion = instrumentalVersion; }
    
    public Boolean getLiveRecording() { return liveRecording; }
    public void setLiveRecording(Boolean liveRecording) { this.liveRecording = liveRecording; }
    
    public Boolean getRemix() { return remix; }
    public void setRemix(Boolean remix) { this.remix = remix; }
    
    public String getRemixArtist() { return remixArtist; }
    public void setRemixArtist(String remixArtist) { this.remixArtist = remixArtist; }
    
    public Boolean getKaraoke() { return karaoke; }
    public void setKaraoke(Boolean karaoke) { this.karaoke = karaoke; }
    
    public Boolean getIsBonus() { return isBonus; }
    public void setIsBonus(Boolean isBonus) { this.isBonus = isBonus; }
    
    public Boolean getIsHidden() { return isHidden; }
    public void setIsHidden(Boolean isHidden) { this.isHidden = isHidden; }
    
    public Boolean getIsSingle() { return isSingle; }
    public void setIsSingle(Boolean isSingle) { this.isSingle = isSingle; }
    
    public LocalDateTime getSingleReleaseDate() { return singleReleaseDate; }
    public void setSingleReleaseDate(LocalDateTime singleReleaseDate) { this.singleReleaseDate = singleReleaseDate; }
    
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
    
    public String getLicenseType() { return licenseType; }
    public void setLicenseType(String licenseType) { this.licenseType = licenseType; }
    
    public LocalDateTime getLicenseExpiry() { return licenseExpiry; }
    public void setLicenseExpiry(LocalDateTime licenseExpiry) { this.licenseExpiry = licenseExpiry; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    
    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
    
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    
    // Utility Methods
    
    /**
     * Updates the last modified timestamp and increments version
     */
    public void touch() {
        this.lastModified = LocalDateTime.now();
        this.version++;
    }
    
    /**
     * Gets track length in seconds for backward compatibility
     */
    public Integer getTrackLengthSeconds() {
        return trackLength != null ? (int) trackLength.getSeconds() : null;
    }
    
    /**
     * Sets track length from seconds for backward compatibility
     */
    public void setTrackLengthSeconds(Integer seconds) {
        this.trackLength = seconds != null ? Duration.ofSeconds(seconds) : null;
    }
    
    /**
     * Adds a genre to the song
     */
    public void addGenre(String genre) {
        if (genres == null) genres = new HashSet<>();
        genres.add(genre);
    }
    
    /**
     * Adds a featured artist
     */
    public void addFeaturedArtist(Long artistId, String artistName) {
        if (featuredArtistIds == null) featuredArtistIds = new HashSet<>();
        if (featuredArtistNames == null) featuredArtistNames = new HashSet<>();
        featuredArtistIds.add(artistId);
        featuredArtistNames.add(artistName);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(songId, song.songId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(songId);
    }
    
    @Override
    public String toString() {
        return "Song{" +
                "songId=" + songId +
                ", songName='" + songName + '\'' +
                ", primaryArtistName='" + primaryArtistName + '\'' +
                ", albumName='" + albumName + '\'' +
                ", trackLength=" + trackLength +
                ", genres=" + genres +
                '}';
    }
}
