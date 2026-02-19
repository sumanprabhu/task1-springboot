package com.task1.suman.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration      // "This class contains configuration/settings"
@EnableWebSecurity  // "Enable Spring Security for this project"
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // This method defines ALL security rules

        http
                //Disable Cross Origin Resource Sharing error
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 1. Disable CSRF (not needed for REST APIs with JWT)
                .csrf(csrf -> csrf.disable())
                //  ↑↑↑↑
                //  CSRF = Cross-Site Request Forgery protection
                //  Needed for browser forms, NOT for REST APIs
                //  Since we use JWT tokens, we don't need CSRF

                // 2. Define URL access rules
                .authorizeHttpRequests(auth -> auth

                                // PUBLIC endpoints — no token needed
                                .requestMatchers("/auth/**").permitAll()
                                //  ↑↑↑↑
                                //  /auth/register and /auth/login are PUBLIC
                                //  Anyone can register and login without a token!

                                // ADMIN only endpoints
                                .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/users/**").hasRole("ADMIN")
                                //  ↑↑↑↑
                                //  Only ADMIN can DELETE users

                                // Everything else needs authentication (any valid token)
                                .anyRequest().authenticated()
                        //  ↑↑↑↑
                        //  All other URLs need a valid token
                )

                // 3. Make it STATELESS (no sessions, we use JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                //  ↑↑↑↑
                //  STATELESS = Don't create sessions on the server
                //  Every request must carry its own JWT token
                //  Server doesn't remember anything between requests

                // 4. Add our JWT Filter BEFORE Spring's default filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        //  ↑↑↑↑
        //  "Run MY JwtFilter BEFORE the default authentication filter"
        //
        //  Filter order:
        //  Request → [JwtFilter] → [UsernamePasswordFilter] → Controller
        //                ↑ OUR filter checks token first!

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        //  ↑↑↑↑
        //  BCrypt = A HASHING algorithm for passwords
        //
        //  "password123" → "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
        //
        //  HASHING ≠ ENCRYPTION
        //  Encryption: can be reversed (decrypted)
        //  Hashing:    CANNOT be reversed (one-way)
        //
        //  Even if database is hacked, hackers see:
        //  "$2a$10$N9qo8u..." NOT "password123"
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
        //  ↑↑↑↑
        //  AuthenticationManager = The guy who verifies login credentials
        //  We need this bean so we can use it in AuthService
        //  to verify email + password during login
    }
}