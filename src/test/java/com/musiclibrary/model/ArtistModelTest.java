package com.musiclibrary.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Artist model class.
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
class ArtistModelTest {

    private Artist artist;

    @BeforeEach
    void setUp() {
        artist = new Artist();
        artist.setArtistId(1L);
        artist.setArtistName("Test Artist");
        artist.setCountry("USA");
        artist.setFormedYear(2000);
    }

    @Test
    void testGettersAndSetters() {
        assertThat(artist.getArtistId()).isEqualTo(1L);
        assertThat(artist.getArtistName()).isEqualTo("Test Artist");
        assertThat(artist.getCountry()).isEqualTo("USA");
        assertThat(artist.getFormedYear()).isEqualTo(2000);
    }

    @Test
    void testEqualsAndHashCode_SameObject() {
        assertThat(artist).isEqualTo(artist);
        assertThat(artist.hashCode()).isEqualTo(artist.hashCode());
    }

    @Test
    void testEqualsAndHashCode_EqualObjects() {
        Artist anotherArtist = new Artist();
        anotherArtist.setArtistId(1L);
        anotherArtist.setArtistName("Test Artist");
        anotherArtist.setCountry("USA");
        anotherArtist.setFormedYear(2000);

        assertThat(artist).isEqualTo(anotherArtist);
        assertThat(artist.hashCode()).isEqualTo(anotherArtist.hashCode());
    }

    @Test
    void testEqualsAndHashCode_DifferentObjects() {
        Artist differentArtist = new Artist();
        differentArtist.setArtistId(2L);
        differentArtist.setArtistName("Different Artist");

        assertThat(artist).isNotEqualTo(differentArtist);
        assertThat(artist.hashCode()).isNotEqualTo(differentArtist.hashCode());
    }

    @Test
    void testEqualsAndHashCode_NullObject() {
        assertThat(artist).isNotEqualTo(null);
    }

    @Test
    void testEqualsAndHashCode_DifferentClass() {
        String notAnArtist = "Not an artist";
        assertThat(artist).isNotEqualTo(notAnArtist);
    }

    @Test
    void testToString() {
        String toString = artist.toString();
        assertThat(toString).contains("Test Artist");
        assertThat(toString).contains("USA");
        assertThat(toString).contains("2000");
    }

    @Test
    void testDefaultConstructor() {
        Artist newArtist = new Artist();
        assertThat(newArtist.getArtistId()).isNull();
        assertThat(newArtist.getArtistName()).isNull();
        assertThat(newArtist.getCountry()).isNull();
        assertThat(newArtist.getFormedYear()).isNull();
    }

    @Test
    void testAuditFields() {
        LocalDateTime now = LocalDateTime.now();
        artist.setCreatedDate(now);
        artist.setLastModified(now);

        assertThat(artist.getCreatedDate()).isEqualTo(now);
        assertThat(artist.getLastModified()).isEqualTo(now);
    }

    @Test
    void testValidationLogic_ValidArtist() {
        assertThat(artist.isValid()).isTrue();
    }

    @Test
    void testValidationLogic_InvalidArtist_NullName() {
        artist.setArtistName(null);
        assertThat(artist.isValid()).isFalse();
    }

    @Test
    void testValidationLogic_InvalidArtist_EmptyName() {
        artist.setArtistName("");
        assertThat(artist.isValid()).isFalse();
    }

    @Test
    void testValidationLogic_InvalidArtist_BlankName() {
        artist.setArtistName("   ");
        assertThat(artist.isValid()).isFalse();
    }

    @Test
    void testBusinessRules_FormationYearValidation() {
        artist.setFormedYear(1950);
        assertThat(artist.getFormedYear()).isEqualTo(1950);

        artist.setFormedYear(2023);
        assertThat(artist.getFormedYear()).isEqualTo(2023);
        
        artist.setFormedYear(null);
        assertThat(artist.getFormedYear()).isNull();
    }

    @Test
    void testBusinessRules_CountryValidation() {
        artist.setCountry(null);
        assertThat(artist.getCountry()).isNull();

        artist.setCountry("");
        assertThat(artist.getCountry()).isEmpty();

        artist.setCountry("   ");
        assertThat(artist.getCountry()).isEqualTo("   ");

        artist.setCountry("USA");
        assertThat(artist.getCountry()).isEqualTo("USA");
    }

    @Test
    void testDisplayMethods() {
        String displayName = artist.getDisplayName();
        assertThat(displayName).contains("Test Artist");
        assertThat(displayName).contains("2000");
    }

    @Test
    void testBusinessLogicMethods() {
        artist.setArtistName("The Beatles");
        assertThat(artist.isBand()).isTrue();
        
        artist.setArtistName("John Lennon");
        assertThat(artist.isBand()).isFalse();
        
        artist.setArtistName("Symphony Orchestra");
        assertThat(artist.isBand()).isTrue();
    }

    @Test
    void testConstructors() {
        Artist newArtist = new Artist("New Artist");
        assertThat(newArtist.getArtistName()).isEqualTo("New Artist");
        assertThat(newArtist.getArtistId()).isNull();
        
        Artist fullArtist = new Artist("Full Artist", "Canada", 1995);
        assertThat(fullArtist.getArtistName()).isEqualTo("Full Artist");
        assertThat(fullArtist.getCountry()).isEqualTo("Canada");
        assertThat(fullArtist.getFormedYear()).isEqualTo(1995);
    }
}
