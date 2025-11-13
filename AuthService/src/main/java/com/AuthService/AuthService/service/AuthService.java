package com.AuthService.AuthService.service;

import com.AuthService.AuthService.dto.UserCredentialDTO;
import com.AuthService.AuthService.entity.Role;
import com.AuthService.AuthService.entity.UserCredential;
import com.AuthService.AuthService.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserCredentialRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(UserCredentialDTO dto){
        if (dto == null || !StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new RuntimeException("Username and password are required");
        }
        if (repository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        UserCredential user = new UserCredential();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // Convert string role to Role enum
        Role role = Role.ROLE_USER; // default
        if (dto.getRole() != null && !dto.getRole().isEmpty()) {
            try {
                String roleStr = dto.getRole().toUpperCase();
                if (!roleStr.startsWith("ROLE_")) {
                    roleStr = "ROLE_" + roleStr;
                }
                role = Role.valueOf(roleStr);
            } catch (IllegalArgumentException e) {
                role = Role.ROLE_USER;
            }
        }
        user.setRole(role);
        
        repository.save(user);
        return "User registered successfully";
    }

    public String generateToken(String username, String password) {
        var userOpt = repository.findByUsername(username);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return jwtService.generateToken(username, userOpt.get().getRole().name());
        }
        throw new RuntimeException("Invalid credentials");
    }

    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }
}
