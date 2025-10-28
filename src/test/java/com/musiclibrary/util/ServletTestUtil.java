package com.musiclibrary.util;

import org.json.JSONObject;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class ServletTestUtil {
    
    private static Map<HttpServletResponse, StringWriter> responseWriters = new HashMap<HttpServletResponse, StringWriter>();
    
    public static HttpServletRequest createMockRequest(String method, String uri) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getPathInfo()).thenReturn(extractPathInfo(uri));
        return request;
    }
    
    public static HttpServletRequest createMockRequestWithJson(String method, String uri, JSONObject json) throws Exception {
        HttpServletRequest request = createMockRequest(method, uri);
        StringReader stringReader = new StringReader(json.toString());
        BufferedReader bufferedReader = new BufferedReader(stringReader);
        when(request.getReader()).thenReturn(bufferedReader);
        when(request.getContentType()).thenReturn("application/json");
        return request;
    }
    
    public static HttpServletRequest createMockRequestWithParams(String method, String uri, Map<String, String> params) {
        HttpServletRequest request = createMockRequest(method, uri);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            when(request.getParameter(entry.getKey())).thenReturn(entry.getValue());
        }
        return request;
    }
    
    public static HttpServletResponse createMockResponse() throws Exception {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        
        responseWriters.put(response, stringWriter);
        return response;
    }
    
    public static String getResponseContent(HttpServletResponse response) throws Exception {
        PrintWriter writer = response.getWriter();
        writer.flush();
        StringWriter stringWriter = responseWriters.get(response);
        if (stringWriter != null) {
            return stringWriter.toString();
        }
        return "";
    }
    
    public static Map<String, String> createParamMap(String... keyValuePairs) {
        Map<String, String> params = new HashMap<String, String>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            params.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return params;
    }
    
    private static String extractPathInfo(String uri) {
        if (uri.contains("/api/")) {
            String[] parts = uri.split("/api/[^/]+");
            if (parts.length > 1) {
                return parts[1];
            }
        }
        return null;
    }
    
    public static JSONObject parseJsonResponse(String responseContent) throws Exception {
        if (responseContent == null || responseContent.trim().isEmpty()) {
            return null;
        }
        return new JSONObject(responseContent);
    }
    
    public static void verifyStatusCode(HttpServletResponse response, int expectedStatus) {
        verify(response).setStatus(expectedStatus);
    }
    
    public static void verifyContentType(HttpServletResponse response, String expectedContentType) {
        verify(response).setContentType(expectedContentType);
    }
}
