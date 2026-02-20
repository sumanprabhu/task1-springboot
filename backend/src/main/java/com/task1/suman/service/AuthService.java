package com.task1.suman.service;

import com.task1.suman.dto.AuthResponse;
import com.task1.suman.dto.LoginRequest;
import com.task1.suman.dto.RegisterRequest;
import com.task1.suman.model.Role;
import com.task1.suman.model.User;
import com.task1.suman.repo.RoleRepo;
import com.task1.suman.repo.UserRepo;
import com.task1.suman.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {

        // 1. Check if email already exists
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            return new AuthResponse(null, "Email already exists!");
        }

        // 2. Decide role automatically
        //    First user EVER → ADMIN
        //    Everyone else   → USER
        String roleName;
        if (userRepo.count() == 0) {
            // Database is empty — this is the FIRST user
            roleName = "ADMIN";
        } else {
            // Not the first user — always USER
            roleName = "USER";
        }

        Role role = roleRepo.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException(
                        "Role not found: " + roleName
                ));

        // 3. Create user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setContactNum(request.getContactNum());
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepo.save(user);

        // 4. Generate token
        String token = jwtUtil.generateToken(user.getEmail(), role.getRoleName());

        // 5. Return response with role info
        if (roleName.equals("ADMIN")) {
            return new AuthResponse(token, "Registration successful! You are the first user — assigned as ADMIN.");
        }
        return new AuthResponse(token, "Registration successful!");
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().getRoleName()
        );

        return new AuthResponse(token, "Login successful!");
    }
}