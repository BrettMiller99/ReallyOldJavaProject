-- Music Library API Database Schema
-- 
-- This schema represents traditional relational database design patterns
-- commonly found in Java 7 enterprise applications.
-- 
-- Migration opportunities:
-- - Manual DDL -> JPA/Hibernate entity annotations
-- - Basic foreign keys -> JPA relationships (@OneToMany, @ManyToMany)
-- - VARCHAR lengths -> JPA validation annotations
-- - Manual indexing -> JPA @Index annotations

-- Artists table - Master data for music artists
CREATE TABLE IF NOT EXISTS artists (
    artist_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    artist_name VARCHAR(255) NOT NULL,
    biography TEXT,
    country VARCHAR(100),
    formed_year INTEGER,
    website VARCHAR(500),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Traditional indexing approach
    CONSTRAINT uk_artist_name UNIQUE (artist_name)
);

-- Albums table - Album information with artist relationship
CREATE TABLE IF NOT EXISTS albums (
    album_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    album_name VARCHAR(255) NOT NULL,
    artist_id BIGINT NOT NULL,
    release_date DATE,
    genre VARCHAR(100),
    record_label VARCHAR(255),
    total_tracks INTEGER DEFAULT 0,
    album_art_path VARCHAR(500),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Traditional foreign key constraints
    CONSTRAINT fk_album_artist FOREIGN KEY (artist_id) REFERENCES artists(artist_id) ON DELETE CASCADE,
    CONSTRAINT uk_album_artist UNIQUE (album_name, artist_id)
);

-- Songs table - Individual song records with album and artist relationships
CREATE TABLE IF NOT EXISTS songs (
    song_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    song_name VARCHAR(255) NOT NULL,
    album_id BIGINT,
    artist_id BIGINT NOT NULL,
    track_number INTEGER,
    track_length INTEGER NOT NULL, -- in seconds, as required
    date_released DATE, -- required field from specification
    genre VARCHAR(100),
    file_path VARCHAR(500),
    file_size BIGINT,
    bitrate INTEGER,
    rating INTEGER DEFAULT 0, -- 0-5 stars
    play_count INTEGER DEFAULT 0,
    last_played TIMESTAMP,
    lyrics TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Traditional constraint and indexing patterns
    CONSTRAINT fk_song_album FOREIGN KEY (album_id) REFERENCES albums(album_id) ON DELETE SET NULL,
    CONSTRAINT fk_song_artist FOREIGN KEY (artist_id) REFERENCES artists(artist_id) ON DELETE CASCADE,
    CONSTRAINT chk_rating CHECK (rating >= 0 AND rating <= 5),
    CONSTRAINT chk_track_length CHECK (track_length > 0)
);

-- Playlists table - User-created playlists
CREATE TABLE IF NOT EXISTS playlists (
    playlist_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    playlist_name VARCHAR(255) NOT NULL,
    description TEXT,
    created_by VARCHAR(100) DEFAULT 'system',
    is_public BOOLEAN DEFAULT TRUE,
    total_duration INTEGER DEFAULT 0, -- total duration in seconds
    song_count INTEGER DEFAULT 0,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_playlist_name UNIQUE (playlist_name, created_by)
);

-- Playlist_Songs table - Many-to-many relationship between playlists and songs
CREATE TABLE IF NOT EXISTS playlist_songs (
    playlist_id BIGINT NOT NULL,
    song_id BIGINT NOT NULL,
    position_order INTEGER NOT NULL,
    added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (playlist_id, song_id),
    CONSTRAINT fk_ps_playlist FOREIGN KEY (playlist_id) REFERENCES playlists(playlist_id) ON DELETE CASCADE,
    CONSTRAINT fk_ps_song FOREIGN KEY (song_id) REFERENCES songs(song_id) ON DELETE CASCADE,
    CONSTRAINT uk_playlist_position UNIQUE (playlist_id, position_order)
);

-- Create traditional indexes for performance - manual approach typical of Java 7 era
-- These indexes are created after all tables to ensure proper execution order
CREATE INDEX IF NOT EXISTS idx_songs_artist ON songs(artist_id);
CREATE INDEX IF NOT EXISTS idx_songs_album ON songs(album_id);
CREATE INDEX IF NOT EXISTS idx_songs_genre ON songs(genre);
CREATE INDEX IF NOT EXISTS idx_songs_date_released ON songs(date_released);
CREATE INDEX IF NOT EXISTS idx_albums_artist ON albums(artist_id);
CREATE INDEX IF NOT EXISTS idx_albums_genre ON albums(genre);
CREATE INDEX IF NOT EXISTS idx_playlist_songs_playlist ON playlist_songs(playlist_id);

-- Insert sample data for development and testing
-- This demonstrates traditional INSERT statements vs modern data fixtures
INSERT INTO artists (artist_name, biography, country, formed_year, website) VALUES 
('The Beatles', 'Legendary British rock band formed in Liverpool in 1960.', 'United Kingdom', 1960, 'http://www.thebeatles.com'),
('Led Zeppelin', 'English rock band formed in London in 1968.', 'United Kingdom', 1968, 'http://www.ledzeppelin.com'),
('Pink Floyd', 'English rock band formed in London in 1965.', 'United Kingdom', 1965, 'http://www.pinkfloyd.com');

INSERT INTO albums (album_name, artist_id, release_date, genre, record_label, total_tracks) VALUES 
('Abbey Road', 1, '1969-09-26', 'Rock', 'Apple Records', 17),
('Led Zeppelin IV', 2, '1971-11-08', 'Rock', 'Atlantic Records', 8),
('The Dark Side of the Moon', 3, '1973-03-01', 'Progressive Rock', 'Harvest Records', 9);

INSERT INTO songs (song_name, album_id, artist_id, track_number, track_length, date_released, genre, rating) VALUES 
('Come Together', 1, 1, 1, 259, '1969-09-26', 'Rock', 5),
('Something', 1, 1, 2, 182, '1969-09-26', 'Rock', 5),
('Stairway to Heaven', 2, 2, 4, 482, '1971-11-08', 'Rock', 5),
('Black Dog', 2, 2, 1, 295, '1971-11-08', 'Rock', 4),
('Time', 3, 3, 4, 413, '1973-03-01', 'Progressive Rock', 5),
('Money', 3, 3, 6, 382, '1973-03-01', 'Progressive Rock', 4);

INSERT INTO playlists (playlist_name, description, created_by) VALUES 
('Classic Rock Hits', 'Best classic rock songs of all time', 'admin'),
('Driving Songs', 'Perfect songs for long drives', 'admin');

INSERT INTO playlist_songs (playlist_id, song_id, position_order) VALUES 
(1, 1, 1), (1, 3, 2), (1, 5, 3),
(2, 3, 1), (2, 4, 2), (2, 6, 3);
