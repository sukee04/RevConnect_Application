package com.project.revconnect.config;

import com.project.revconnect.model.UserPrincipal;
import com.project.revconnect.repository.UserRepository;
import com.project.revconnect.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String subject = null;
        Long userId = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
            try {
                subject = jwtService.extractUserName(token);
                userId = jwtService.extractUserId(token);
            } catch (Exception e) {
                System.out.println("JWT Validation Error: " + e.getMessage());
            }
        }

        if ((subject != null || userId != null) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = null;

            if (userId != null) {
                userDetails = userRepository.findById(userId)
                        .map(UserPrincipal::new)
                        .orElse(null);
            }

            if (userDetails == null && subject != null) {
                try {
                    userDetails = userDetailsService.loadUserByUsername(subject);
                } catch (UsernameNotFoundException ignored) {
                    userDetails = null;
                }
            }

            if (userDetails != null && jwtService.validateToken(token)) {

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
