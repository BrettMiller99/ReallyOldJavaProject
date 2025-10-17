package com.musiclibrary.filter;

import com.musiclibrary.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Logger;

public class AuthenticationFilter implements Filter {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("Authentication Filter initialized");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        if (requestURI.endsWith("/api/health")) {
            chain.doFilter(request, response);
            return;
        }
        
        if ("OPTIONS".equals(method)) {
            chain.doFilter(request, response);
            return;
        }
        
        String authHeader = httpRequest.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.warning("Missing or invalid Authorization header for: " + requestURI);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Authentication required\",\"message\":\"Please provide a valid JWT token\"}");
            return;
        }
        
        String token = authHeader.substring(7);
        
        if (!JwtUtil.validateToken(token) || JwtUtil.isTokenExpired(token)) {
            LOGGER.warning("Invalid or expired JWT token for: " + requestURI);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Invalid token\",\"message\":\"Token is invalid or expired\"}");
            return;
        }
        
        String username = JwtUtil.getUsernameFromToken(token);
        httpRequest.setAttribute("username", username);
        
        LOGGER.info("Authenticated user: " + username + " for: " + requestURI);
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        LOGGER.info("Authentication Filter destroyed");
    }
}
