package com.musiclibrary.integration;

import com.musiclibrary.util.JwtUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(MockitoExtension.class)
public class ApiEndpointIntegrationTest {
    
    private String validToken;
    
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/";
    }
    
    @BeforeEach
    public void setUp() {
        validToken = JwtUtil.generateToken("admin");
    }
    
    @Test
    public void testHealthEndpointWithoutAuthentication() {
        given()
            .when()
                .get("/api/health")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("timestamp", notNullValue());
    }
    
    @Test
    public void testAuthenticationEndpoint() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"admin\",\"password\":\"password123\"}")
            .when()
                .post("/auth/login")
            .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("token", notNullValue())
                .body("username", equalTo("admin"));
    }
    
    @Test
    public void testAuthenticationWithInvalidCredentials() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"admin\",\"password\":\"wrongpassword\"}")
            .when()
                .post("/auth/login")
            .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("error", equalTo("Invalid credentials"));
    }
    
    @Test
    public void testGetArtistsWithAuthentication() {
        given()
            .header("Authorization", "Bearer " + validToken)
            .when()
                .get("/api/artists")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }
    
    @Test
    public void testGetArtistsWithoutAuthentication() {
        given()
            .when()
                .get("/api/artists")
            .then()
                .statusCode(401)
                .body("error", equalTo("Authentication required"));
    }
    
    @Test
    public void testGetSongsWithAuthentication() {
        given()
            .header("Authorization", "Bearer " + validToken)
            .when()
                .get("/api/songs")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }
    
    @Test
    public void testCreateSongWithAuthentication() {
        String songJson = "{" +
                "\"songName\":\"API Test Song\"," +
                "\"artistId\":1," +
                "\"trackLength\":180," +
                "\"dateReleased\":\"2023-01-01\"" +
                "}";
        
        given()
            .header("Authorization", "Bearer " + validToken)
            .contentType(ContentType.JSON)
            .body(songJson)
            .when()
                .post("/api/songs")
            .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("success", equalTo(true));
    }
    
    @Test
    public void testCreateSongWithoutAuthentication() {
        String songJson = "{" +
                "\"songName\":\"API Test Song\"," +
                "\"artistId\":1," +
                "\"trackLength\":180" +
                "}";
        
        given()
            .contentType(ContentType.JSON)
            .body(songJson)
            .when()
                .post("/api/songs")
            .then()
                .statusCode(401)
                .body("error", equalTo("Authentication required"));
    }
    
    @Test
    public void testCorsHeaders() {
        given()
            .header("Origin", "http://localhost:3000")
            .header("Access-Control-Request-Method", "GET")
            .when()
                .options("/api/artists")
            .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", containsString("localhost"));
    }
    
    @Test
    public void testInvalidTokenAuthentication() {
        given()
            .header("Authorization", "Bearer invalid-token")
            .when()
                .get("/api/artists")
            .then()
                .statusCode(401)
                .body("error", equalTo("Invalid token"));
    }
}
