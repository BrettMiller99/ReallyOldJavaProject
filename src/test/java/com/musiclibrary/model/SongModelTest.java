package com.musiclibrary.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Song model class.
 * 
 * Tests entity behavior including:
 * - Getter and setter methods
 * - Equals and hashCode contracts
 * - Validation logic
 * - Business rule enforcement
 * 
 * @author Music Library Development Team
 * @version 2.0 - JPA Entity Tests
 * @since Java 17
 */
class SongModelTest {

    private Song song;
    private Artist artist;
    private Album album;

    @BeforeEach
    void setUp() {
        artist = new Artist();
        artist.setArtistId(1L);
        artist.setArtistName("Test Artist");
        artist.setCountry("USA");
        artist.setFormedYear(2000);

        album = new Album();
        album.setAlbumId(1L);
        album.setAlbumName("Test Album");
        album.setArtist(artist);

        song = new Song();
        song.setSongId(1L);
        song.setSongName("Test Song");
        song.setArtist(artist);
        song.setAlbum(album);
        song.setDateReleased(LocalDate.of(2023, 1, 1)); // Required for validation
        song.setTrackLength(210); // 3 minutes 30 seconds = 210 seconds
        song.setTrackNumber(1);
        song.setRating(4);
        song.setPlayCount(0);
    }

    @Test
    void testGettersAndSetters() {
        assertThat(song.getSongId()).isEqualTo(1L);
        assertThat(song.getSongName()).isEqualTo("Test Song");
        assertThat(song.getArtist()).isEqualTo(artist);
        assertThat(song.getAlbum()).isEqualTo(album);
        assertThat(song.getTrackLength()).isEqualTo(210);
        assertThat(song.getTrackNumber()).isEqualTo(1);
        assertThat(song.getRating()).isEqualTo(4);
        assertThat(song.getPlayCount()).isEqualTo(0);
    }

    @Test
    void testEqualsAndHashCode_SameObject() {
        assertThat(song).isEqualTo(song);
        assertThat(song.hashCode()).isEqualTo(song.hashCode());
    }

    @Test
    void testEqualsAndHashCode_EqualObjects() {
        Song anotherSong = new Song();
        anotherSong.setSongId(1L);
        anotherSong.setSongName("Test Song");
        anotherSong.setArtist(artist);
        anotherSong.setAlbum(album);
        anotherSong.setTrackLength(210);
        anotherSong.setTrackNumber(1);
        anotherSong.setRating(4);
        anotherSong.setPlayCount(0);

        assertThat(song).isEqualTo(anotherSong);
        assertThat(song.hashCode()).isEqualTo(anotherSong.hashCode());
    }

    @Test
    void testEqualsAndHashCode_DifferentObjects() {
        Song differentSong = new Song();
        differentSong.setSongId(2L);
        differentSong.setSongName("Different Song");

        assertThat(song).isNotEqualTo(differentSong);
        assertThat(song.hashCode()).isNotEqualTo(differentSong.hashCode());
    }

    @Test
    void testEqualsAndHashCode_NullObject() {
        assertThat(song).isNotEqualTo(null);
    }

    @Test
    void testEqualsAndHashCode_DifferentClass() {
        String notASong = "Not a song";
        assertThat(song).isNotEqualTo(notASong);
    }

    @Test
    void testToString() {
        String toString = song.toString();
        assertThat(toString).contains("Test Song");
        assertThat(toString).contains("Test Artist");
        assertThat(toString).contains("4");
    }

    @Test
    void testDefaultConstructor() {
        Song newSong = new Song();
        assertThat(newSong.getSongId()).isNull();
        assertThat(newSong.getSongName()).isNull();
        assertThat(newSong.getArtist()).isNull();
        assertThat(newSong.getAlbum()).isNull();
        assertThat(newSong.getTrackLength()).isNull();
        assertThat(newSong.getTrackNumber()).isNull();
        assertThat(newSong.getRating()).isNull();
        assertThat(newSong.getPlayCount()).isNull();
    }

    @Test
    void testAuditFields() {
        LocalDateTime now = LocalDateTime.now();
        song.setCreatedDate(now);
        song.setLastModified(now);

        assertThat(song.getCreatedDate()).isEqualTo(now);
        assertThat(song.getLastModified()).isEqualTo(now);
    }

    @Test
    void testValidationLogic_ValidSong() {
        assertThat(song.isValid()).isTrue();
    }

    @Test
    void testValidationLogic_InvalidSong_NullName() {
        song.setSongName(null);
        assertThat(song.isValid()).isFalse();
    }

    @Test
    void testValidationLogic_InvalidSong_EmptyName() {
        song.setSongName("");
        assertThat(song.isValid()).isFalse();
    }

    @Test
    void testValidationLogic_InvalidSong_NullArtist() {
        song.setArtist(null);
        assertThat(song.isValid()).isFalse();
    }

    @Test
    void testValidationLogic_ValidSong_NullAlbum() {
        song.setAlbum(null);
        assertThat(song.isValid()).isTrue(); // Album is optional for songs
    }

    @Test
    void testBusinessRules_RatingValidation() {
        try {
            song.setRating(-1);
            assertThat(false).isTrue(); // Should not reach here
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("Rating must be between 0 and 5");
        }

        try {
            song.setRating(6);
            assertThat(false).isTrue(); // Should not reach here
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("Rating must be between 0 and 5");
        }

        song.setRating(0);
        assertThat(song.getRating()).isEqualTo(0);

        song.setRating(5);
        assertThat(song.getRating()).isEqualTo(5);

        song.setRating(3);
        assertThat(song.getRating()).isEqualTo(3);

        song.setRating(null);
        assertThat(song.getRating()).isNull();
    }

    @Test
    void testBusinessRules_TrackNumberValidation() {
        song.setTrackNumber(0);
        assertThat(song.getTrackNumber()).isEqualTo(0);

        song.setTrackNumber(-1);
        assertThat(song.getTrackNumber()).isEqualTo(-1);

        song.setTrackNumber(1);
        assertThat(song.getTrackNumber()).isEqualTo(1);

        song.setTrackNumber(99);
        assertThat(song.getTrackNumber()).isEqualTo(99);
    }

    @Test
    void testBusinessRules_TrackLengthValidation() {
        song.setTrackLength(0);
        assertThat(song.getTrackLength()).isEqualTo(0);

        song.setTrackLength(30);
        assertThat(song.getTrackLength()).isEqualTo(30);

        song.setTrackLength(600);
        assertThat(song.getTrackLength()).isEqualTo(600);
    }

    @Test
    void testPlaybackMethods() {
        assertThat(song.getPlayCount()).isEqualTo(0);

        song.incrementPlayCount();
        assertThat(song.getPlayCount()).isEqualTo(1);

        song.incrementPlayCount();
        assertThat(song.getPlayCount()).isEqualTo(2);

        song.setPlayCount(0);
        assertThat(song.getPlayCount()).isEqualTo(0);
    }

    @Test
    void testDisplayMethods() {
        String toString = song.toString();
        assertThat(toString).contains("Test Song");
        assertThat(toString).contains("210");
    }

    @Test
    void testDurationFormatting() {
        song.setTrackLength(255); // 4 minutes 15 seconds
        String formattedDuration = song.getFormattedDuration();
        assertThat(formattedDuration).isEqualTo("4:15");

        song.setTrackLength(3750); // 1 hour 2 minutes 30 seconds
        formattedDuration = song.getFormattedDuration();
        assertThat(formattedDuration).isEqualTo("62:30");
    }

    @Test
    void testConstructors() {
        Song newSong = new Song("New Song", artist, 240, java.time.LocalDate.of(2023, 1, 1));
        assertThat(newSong.getSongName()).isEqualTo("New Song");
        assertThat(newSong.getArtist()).isEqualTo(artist);
        assertThat(newSong.getTrackLength()).isEqualTo(240);
        assertThat(newSong.getDateReleased()).isEqualTo(java.time.LocalDate.of(2023, 1, 1));
        assertThat(newSong.getSongId()).isNull();
    }

    @Test
    void testBusinessLogicMethods() {
        song.setTrackLength(150); // 2 minutes 30 seconds
        assertThat(song.getTrackLength()).isEqualTo(150);
        assertThat(song.isValid()).isTrue();
        
        song.setTrackLength(480); // 8 minutes
        assertThat(song.getTrackLength()).isEqualTo(480);
        assertThat(song.isValid()).isTrue();
        
        song.setTrackLength(0); // Invalid length
        assertThat(song.isValid()).isFalse();
    }
}
