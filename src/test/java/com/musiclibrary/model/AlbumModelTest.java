package com.musiclibrary.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Album model class.
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
class AlbumModelTest {

    private Album album;
    private Artist artist;

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
        album.setReleaseDate(LocalDate.of(2023, 1, 1));
        album.setGenre("Rock");
        album.setRecordLabel("Test Records");
        album.setTotalTracks(10);
    }

    @Test
    void testGettersAndSetters() {
        assertThat(album.getAlbumId()).isEqualTo(1L);
        assertThat(album.getAlbumName()).isEqualTo("Test Album");
        assertThat(album.getArtist()).isEqualTo(artist);
        assertThat(album.getReleaseDate()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(album.getGenre()).isEqualTo("Rock");
        assertThat(album.getRecordLabel()).isEqualTo("Test Records");
        assertThat(album.getTotalTracks()).isEqualTo(10);
    }

    @Test
    void testEqualsAndHashCode_SameObject() {
        assertThat(album).isEqualTo(album);
        assertThat(album.hashCode()).isEqualTo(album.hashCode());
    }

    @Test
    void testEqualsAndHashCode_EqualObjects() {
        Album anotherAlbum = new Album();
        anotherAlbum.setAlbumId(1L);
        anotherAlbum.setAlbumName("Test Album");
        anotherAlbum.setArtist(artist);
        anotherAlbum.setReleaseDate(LocalDate.of(2023, 1, 1));
        anotherAlbum.setGenre("Rock");
        anotherAlbum.setRecordLabel("Test Records");
        anotherAlbum.setTotalTracks(10);

        assertThat(album).isEqualTo(anotherAlbum);
        assertThat(album.hashCode()).isEqualTo(anotherAlbum.hashCode());
    }

    @Test
    void testEqualsAndHashCode_DifferentObjects() {
        Album differentAlbum = new Album();
        differentAlbum.setAlbumId(2L);
        differentAlbum.setAlbumName("Different Album");

        assertThat(album).isNotEqualTo(differentAlbum);
        assertThat(album.hashCode()).isNotEqualTo(differentAlbum.hashCode());
    }

    @Test
    void testEqualsAndHashCode_NullObject() {
        assertThat(album).isNotEqualTo(null);
    }

    @Test
    void testEqualsAndHashCode_DifferentClass() {
        String notAnAlbum = "Not an album";
        assertThat(album).isNotEqualTo(notAnAlbum);
    }

    @Test
    void testToString() {
        String toString = album.toString();
        assertThat(toString).contains("Test Album");
        assertThat(toString).contains("Rock");
        assertThat(toString).contains("Test Records");
    }

    @Test
    void testDefaultConstructor() {
        Album newAlbum = new Album();
        assertThat(newAlbum.getAlbumId()).isNull();
        assertThat(newAlbum.getAlbumName()).isNull();
        assertThat(newAlbum.getArtist()).isNull();
        assertThat(newAlbum.getReleaseDate()).isNull();
        assertThat(newAlbum.getGenre()).isNull();
        assertThat(newAlbum.getRecordLabel()).isNull();
        assertThat(newAlbum.getTotalTracks()).isNull();
    }

    @Test
    void testAuditFields() {
        LocalDateTime now = LocalDateTime.now();
        album.setCreatedDate(now);
        album.setLastModified(now);

        assertThat(album.getCreatedDate()).isEqualTo(now);
        assertThat(album.getLastModified()).isEqualTo(now);
    }

    @Test
    void testValidationLogic_ValidAlbum() {
        assertThat(album.isValid()).isTrue();
    }

    @Test
    void testValidationLogic_InvalidAlbum_NullName() {
        album.setAlbumName(null);
        assertThat(album.isValid()).isFalse();
    }

    @Test
    void testValidationLogic_InvalidAlbum_EmptyName() {
        album.setAlbumName("");
        assertThat(album.isValid()).isFalse();
    }

    @Test
    void testValidationLogic_InvalidAlbum_NullArtist() {
        album.setArtist(null);
        assertThat(album.isValid()).isFalse();
    }

    @Test
    void testBusinessLogicMethods() {
        album.setTotalTracks(2);
        assertThat(album.isSingle()).isTrue();
        
        album.setTotalTracks(5);
        assertThat(album.isEP()).isTrue();
        
        album.setTotalTracks(12);
        assertThat(album.isFullAlbum()).isTrue();
    }

    @Test
    void testDisplayMethods() {
        String displayTitle = album.getDisplayTitle();
        assertThat(displayTitle).contains("Test Album");

        String releaseYear = album.getReleaseYear();
        assertThat(releaseYear).isNotNull();
    }

    @Test
    void testConstructors() {
        Album newAlbum = new Album("New Album", artist);
        assertThat(newAlbum.getAlbumName()).isEqualTo("New Album");
        assertThat(newAlbum.getArtist()).isEqualTo(artist);
        assertThat(newAlbum.getAlbumId()).isNull();
        
        Album fullAlbum = new Album("Full Album", artist, LocalDate.of(2023, 6, 1), "Pop");
        assertThat(fullAlbum.getAlbumName()).isEqualTo("Full Album");
        assertThat(fullAlbum.getArtist()).isEqualTo(artist);
        assertThat(fullAlbum.getReleaseDate()).isEqualTo(LocalDate.of(2023, 6, 1));
        assertThat(fullAlbum.getGenre()).isEqualTo("Pop");
    }
}
