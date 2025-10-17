package com.musiclibrary.servlet;

import com.musiclibrary.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

public class AuthServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AuthServlet.class.getName());
    
    private static final String DEMO_USERNAME = "admin";
    private static final String DEMO_PASSWORD = "password123";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            JSONObject requestJson = new JSONObject(sb.toString());
            String username = requestJson.optString("username");
            String password = requestJson.optString("password");
            
            if (DEMO_USERNAME.equals(username) && DEMO_PASSWORD.equals(password)) {
                String token = JwtUtil.generateToken(username);
                
                JSONObject responseJson = new JSONObject();
                responseJson.put("success", true);
                responseJson.put("token", token);
                responseJson.put("username", username);
                responseJson.put("message", "Authentication successful");
                
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(responseJson.toString());
                
                LOGGER.info("Successful authentication for user: " + username);
            } else {
                JSONObject responseJson = new JSONObject();
                responseJson.put("success", false);
                responseJson.put("error", "Invalid credentials");
                responseJson.put("message", "Username or password is incorrect");
                
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(responseJson.toString());
                
                LOGGER.warning("Failed authentication attempt for user: " + username);
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error processing authentication request: " + e.getMessage());
            
            JSONObject responseJson = new JSONObject();
            responseJson.put("success", false);
            responseJson.put("error", "Authentication error");
            responseJson.put("message", "An error occurred during authentication");
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(responseJson.toString());
        }
    }
}
