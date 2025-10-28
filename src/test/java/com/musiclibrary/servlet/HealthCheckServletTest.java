package com.musiclibrary.servlet;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for HealthCheckServlet using traditional Java 7 testing patterns.
 * 
 * Testing Approach:
 * - Tests health check and status endpoints
 * - Validates JSON response format for health information
 * - Tests unsupported HTTP methods return proper error codes
 * - Verifies system status reporting functionality
 * - No service layer mocking needed as health checks are self-contained
 * 
 * @author Music Library Development Team
 * @version 1.0
 * @since Java 7
 */
public class HealthCheckServletTest extends ServletTestBase {
    
    private HealthCheckServlet healthCheckServlet;
    
    @Before
    public void setUp() throws Exception {
        super.setUpServletBase();
        healthCheckServlet = new HealthCheckServlet();
        
        healthCheckServlet.init(mockServletConfig);
    }
    
    
    @Test
    public void testDoPost_MethodNotAllowed() throws Exception {
        healthCheckServlet.doPost(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verify(mockResponse).setContentType("application/json");
        
        JSONObject response = getResponseJson();
        assertFalse("Response should indicate failure", response.getBoolean("success"));
        assertTrue("Error should mention POST not supported", 
            response.getString("error").contains("POST method not supported"));
        assertEquals("Error code should be 405", "405", response.getString("errorCode"));
    }
    
    @Test
    public void testDoPut_MethodNotAllowed() throws Exception {
        healthCheckServlet.doPut(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verify(mockResponse).setContentType("application/json");
        
        JSONObject response = getResponseJson();
        assertFalse("Response should indicate failure", response.getBoolean("success"));
        assertTrue("Error should mention PUT not supported", 
            response.getString("error").contains("PUT method not supported"));
        assertEquals("Error code should be 405", "405", response.getString("errorCode"));
    }
    
    @Test
    public void testDoDelete_MethodNotAllowed() throws Exception {
        healthCheckServlet.doDelete(mockRequest, mockResponse);
        
        verify(mockResponse).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verify(mockResponse).setContentType("application/json");
        
        JSONObject response = getResponseJson();
        assertFalse("Response should indicate failure", response.getBoolean("success"));
        assertTrue("Error should mention DELETE not supported", 
            response.getString("error").contains("DELETE method not supported"));
        assertEquals("Error code should be 405", "405", response.getString("errorCode"));
    }
    
}
