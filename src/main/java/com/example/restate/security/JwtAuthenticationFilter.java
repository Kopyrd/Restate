package com.example.restate.security;

import com.example.restate.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, @Lazy UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Wrap the response to ensure content is preserved
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // Skip authentication for login and register endpoints
        String requestPath = request.getRequestURI();
        if (requestPath.contains("/api/auth/login") || requestPath.contains("/api/auth/register")) {
            filterChain.doFilter(request, responseWrapper);
            responseWrapper.copyBodyToResponse();
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        // If no Authorization header, continue without authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, responseWrapper);
            responseWrapper.copyBodyToResponse();
            return;
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Successfully authenticated user: {}", username);
                } else {
                    log.warn("Invalid JWT token for user: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        // Continue the filter chain with the wrapped response
        filterChain.doFilter(request, responseWrapper);

        // Copy the content from the wrapper to the original response
        responseWrapper.copyBodyToResponse();
    }
}
