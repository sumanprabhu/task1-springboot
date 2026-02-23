package com.task1.suman.security;

import com.task1.suman.repo.UserRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    //                        ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    //    "Run this filter ONCE for every request"
    //    (Sometimes filters can run multiple times
    //     due to internal forwarding — this prevents that)

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepo userRepo;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/") || path.startsWith("/ai/chat");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,    // The incoming request
            HttpServletResponse response,  // The outgoing response
            FilterChain filterChain        // The chain of other filters
    ) throws ServletException, IOException {

        // ========== STEP 1: Get the Authorization header ==========
        String authHeader = request.getHeader("Authorization");
        // Example: "Bearer eyJhbGciOiJIUzI1NiJ9..."

        String token = null;
        String email = null;

        // ========== STEP 2: Check if header exists and starts with "Bearer " ==========
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            //                          ↑↑↑
            //  "Bearer eyJhbG..." → remove first 7 characters ("Bearer ")
            //  Result: "eyJhbG..."  (just the token)

            email = jwtUtil.extractEmail(token);
            // Extract email from token payload
        }

        // ========== STEP 3: Validate and set authentication ==========
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //                 ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
            //  "Is no one currently authenticated for this request?"
            //  We don't want to authenticate AGAIN if already done

            // Find the user in database
            UserDetails userDetails = userRepo.findByEmail(email)
                    .orElse(null);

            if (userDetails != null && jwtUtil.validateToken(token, email)) {
                // Token is VALID! Tell Spring Security this user is legit

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,                // WHO (the user)
                                null,                       // password (not needed, token is proof)
                                userDetails.getAuthorities() // ROLES (ROLE_ADMIN, ROLE_USER)
                        );
                //  ↑↑↑
                //  This is Spring Security's way of saying:
                //  "This person is authenticated with these roles"

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );
                // Attach extra request details (IP address, etc.)

                SecurityContextHolder.getContext().setAuthentication(authToken);
                //  ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
                //  SAVE the authentication in Spring's Security Context
                //  Now Spring knows: "This request is from an authenticated user"
                //
                //  Think of it as:
                //  Guard stamps your hand ✋ →
                //  Now everyone inside knows you're verified
            }
        }

        // ========== STEP 4: Continue to next filter/controller ==========
        filterChain.doFilter(request, response);
        //  ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
        //  "I'm done checking. Pass the request to the next filter."
        //  If we DON'T call this, the request gets STUCK here!
    }
}