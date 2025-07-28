# Music Library API

A comprehensive RESTful API for managing digital music collections, built with traditional Java enterprise technologies. This application provides complete music library management capabilities including artists, albums, songs, and playlists with full CRUD operations and advanced search functionality.

## 🎵 Features

### Core Functionality
- **Artist Management**: Create, update, and manage artist profiles with biographical information
- **Album Management**: Organize music collections by albums with metadata and release information
- **Song Management**: Detailed song information including track length, genre, ratings, and play counts
- **Playlist Management**: Create and manage custom playlists with song ordering capabilities
- **Advanced Search**: Search across artists, albums, and songs with filtering and pagination
- **Health Monitoring**: Built-in health check and system status endpoints

### Business Logic
- **Data Validation**: Comprehensive validation for all music metadata
- **Relationship Management**: Proper handling of artist-album-song relationships
- **Rating System**: Song rating and play count tracking
- **Date Handling**: Release date validation and chronological organization
- **Genre Management**: Consistent genre categorization and filtering

## 🏗 Architecture

The application follows traditional Java enterprise patterns with clear separation of concerns:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Layer     │    │  Service Layer  │    │   Data Layer    │
│   (Servlets)    │───▶│  (Business)     │───▶│     (DAO)       │
│                 │    │   Logic         │    │                 │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ • HTTP Handling │    │ • Validation    │    │ • Database      │
│ • JSON I/O      │    │ • Business      │    │   Operations    │
│ • Request       │    │   Rules         │    │ • SQL Queries   │
│   Routing       │    │ • Error         │    │ • Connection    │
│ • CORS Support  │    │   Handling      │    │   Management    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Technology Stack
- **Java 7**: Core programming language with enterprise patterns
- **Servlet API 3.0**: Web layer and HTTP request handling
- **JDBC**: Direct database connectivity and SQL operations
- **H2 Database**: In-memory database for development and testing
- **Maven**: Build automation and dependency management
- **JUnit 4**: Unit testing framework
- **Mockito**: Mocking framework for testing
- **JSON**: Manual JSON serialization and deserialization

## 🚀 Getting Started

### Prerequisites
- Java 8 or higher (compiled with Java 8 for broader compatibility)
- Maven 3.6 or higher
- Git for source code management

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ReallyOldJavaProject
   ```

2. **Build the application**
   ```bash
   mvn clean compile
   ```

3. **Run tests**
   ```bash
   mvn test
   ```

4. **Start the development server**
   ```bash
   mvn jetty:run
   ```

The application will be available at `http://localhost:8080`

### Database Setup

The application uses an H2 in-memory database that is automatically initialized with sample data on startup. The database schema includes:

- **Artists**: Artist profiles with biographical information
- **Albums**: Album metadata with artist relationships
- **Songs**: Detailed song information with album and artist references
- **Playlists**: User-created playlists with song associations

## 📡 API Endpoints

### Artists API
- `GET /api/artists` - List all artists
- `GET /api/artists/{id}` - Get artist by ID
- `POST /api/artists` - Create new artist
- `PUT /api/artists/{id}` - Update artist
- `DELETE /api/artists/{id}` - Delete artist
- `GET /api/artists/search?q={query}` - Search artists
- `GET /api/artists?country={country}` - Filter by country

### Albums API
- `GET /api/albums` - List all albums
- `GET /api/albums/{id}` - Get album by ID
- `POST /api/albums` - Create new album
- `PUT /api/albums/{id}` - Update album
- `DELETE /api/albums/{id}` - Delete album
- `GET /api/albums/search?q={query}` - Search albums
- `GET /api/albums?artist={artist}` - Filter by artist
- `GET /api/albums?genre={genre}` - Filter by genre
- `GET /api/albums?year={year}` - Filter by release year

### Songs API
- `GET /api/songs` - List all songs
- `GET /api/songs/{id}` - Get song by ID
- `POST /api/songs` - Create new song
- `PUT /api/songs/{id}` - Update song
- `DELETE /api/songs/{id}` - Delete song
- `GET /api/songs/search?q={query}` - Search songs
- `GET /api/songs?artist={artist}` - Filter by artist
- `GET /api/songs?album={album}` - Filter by album

### Playlists API
- `GET /api/playlists` - List all playlists
- `GET /api/playlists/{id}` - Get playlist by ID
- `POST /api/playlists` - Create new playlist
- `PUT /api/playlists/{id}` - Update playlist
- `DELETE /api/playlists/{id}` - Delete playlist
- `POST /api/playlists/{id}/songs` - Add song to playlist
- `DELETE /api/playlists/{id}/songs/{songId}` - Remove song from playlist

### System API
- `GET /api/health` - Health check and system status
- `GET /api/health/status` - Detailed system diagnostics

### Query Parameters

Most list endpoints support pagination and filtering:
- `page` - Page number (0-based)
- `size` - Number of items per page
- `sort` - Sort field and direction

## 📋 Request/Response Examples

### Create Artist
```bash
curl -X POST http://localhost:8080/api/artists \
  -H "Content-Type: application/json" \
  -d '{
    "artistName": "The Beatles",
    "formationYear": 1960,
    "country": "United Kingdom",
    "genre": "Rock",
    "biography": "Legendary British rock band"
  }'
```

### Search Songs
```bash
curl "http://localhost:8080/api/songs/search?q=love&page=0&size=10"
```

### Get Artist Albums
```bash
curl "http://localhost:8080/api/albums?artist=The Beatles"
```

## 🔧 Configuration

### Database Configuration
Edit `src/main/resources/database.properties`:
```properties
db.url=jdbc:h2:mem:musiclibrary;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
db.username=sa
db.password=
db.driver=org.h2.Driver
```

### Web Configuration
The application uses traditional `web.xml` configuration located at:
`src/main/webapp/WEB-INF/web.xml`

Key configurations include:
- Servlet mappings for API endpoints
- CORS filter for cross-origin requests
- Request logging filter for monitoring
- Error page mappings

## 🧪 Testing

The application includes comprehensive unit tests for all business logic:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=SongServiceTest

# Run tests with coverage
mvn test jacoco:report
```

### Test Coverage
- **Service Layer**: Complete business logic validation
- **DAO Layer**: Database operation testing with mocks
- **Model Layer**: Entity validation and relationship testing
- **Integration Tests**: End-to-end API testing

## 📊 Database Schema

### Core Tables
- **artists**: Artist master data with biographical information
- **albums**: Album metadata with artist relationships
- **songs**: Song details with album and artist references
- **playlists**: User-created playlist definitions
- **playlist_songs**: Many-to-many relationship between playlists and songs

### Relationships
- Artists → Albums (One-to-Many)
- Albums → Songs (One-to-Many)
- Artists → Songs (One-to-Many, denormalized for performance)
- Playlists ↔ Songs (Many-to-Many)

## 🔍 Business Rules

### Validation Rules
- **Artist Names**: Required, must be unique, 1-255 characters
- **Formation Years**: Must be between 1900 and current year
- **Album Names**: Required, must be unique per artist
- **Song Names**: Required, track length must be positive
- **Release Dates**: Cannot be in the future
- **Ratings**: Must be between 0.0 and 5.0

### Data Integrity
- Referential integrity enforced through foreign keys
- Cascade deletion for dependent records
- Automatic timestamp management for creation and modification
- Genre normalization and validation

## 🚨 Error Handling

The API provides consistent error responses:

```json
{
  "success": false,
  "error": "Artist name is required",
  "timestamp": "2023-07-28T10:30:00Z"
}
```

### HTTP Status Codes
- `200 OK` - Successful operation
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## 📈 Performance Considerations

### Database Optimization
- Proper indexing on frequently queried fields
- Connection pooling for database efficiency
- Prepared statements to prevent SQL injection
- Batch operations for bulk data processing

### Caching Strategy
- Artist name denormalization for query performance
- Result set caching for frequently accessed data
- Connection reuse and resource management

## 🔒 Security Features

- **SQL Injection Prevention**: Parameterized queries throughout
- **Input Validation**: Comprehensive server-side validation
- **CORS Support**: Configurable cross-origin resource sharing
- **Error Information**: Sanitized error messages in production

## 🛠 Development

### Project Structure
```
src/
├── main/
│   ├── java/com/musiclibrary/
│   │   ├── model/          # Entity classes
│   │   ├── dao/            # Data access objects
│   │   ├── service/        # Business logic layer
│   │   ├── servlet/        # Web layer controllers
│   │   ├── filter/         # HTTP filters
│   │   └── util/           # Utility classes
│   ├── resources/
│   │   ├── database.properties
│   │   └── schema.sql
│   └── webapp/WEB-INF/
│       └── web.xml
└── test/
    └── java/com/musiclibrary/
        └── service/        # Unit tests
```

### Adding New Features

1. **Model**: Define entity classes in `com.musiclibrary.model`
2. **DAO**: Create data access objects in `com.musiclibrary.dao`
3. **Service**: Implement business logic in `com.musiclibrary.service`
4. **Servlet**: Add web endpoints in `com.musiclibrary.servlet`
5. **Configuration**: Update `web.xml` with new servlet mappings
6. **Tests**: Add unit tests for all new functionality

## 🤝 Contributing

This project follows traditional Java enterprise development practices:

1. Follow existing code patterns and conventions
2. Add comprehensive unit tests for new functionality
3. Document business logic and validation rules
4. Maintain backwards compatibility
5. Update API documentation for new endpoints

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For questions and support regarding the Music Library API:
- Review the API documentation above
- Check the unit tests for usage examples
- Examine the sample database schema and data
- Reference the business logic documentation in service classes

---

**Music Library API** - A comprehensive solution for digital music collection management using proven Java enterprise technologies.
