package com.AuthService.AuthService.controller;

import com.AuthService.AuthService.dto.UserCredentialDTO;
import com.AuthService.AuthService.entity.Role;
import com.AuthService.AuthService.entity.UserCredential;
import com.AuthService.AuthService.repository.UserCredentialRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/auth-users")
@RequiredArgsConstructor
@Tag(name = "Internal Auth Users", description = "Internal CRUD operations for AuthService users")
public class InternalUserController {
    private final UserCredentialRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Create internal auth user", description = "Used internally to add a new user")
    @ApiResponse(responseCode = "200", description = "User created successfully")
    @PostMapping
    public void createUser(@RequestBody UserCredentialDTO dto) {
        UserCredential user = UserCredential.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.ROLE_USER)
                .build();
        repository.save(user);
    }

    @Operation(summary = "Update internal auth user", description = "Update existing user by ID")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @PutMapping("/{id}")
    public void updateUser(@PathVariable Long id, @RequestBody UserCredentialDTO dto) {
        UserCredential existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existing.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        existing.setRole(Role.ROLE_USER);
        repository.save(existing);
    }

    @Operation(summary = "Delete internal auth user", description = "Delete user by ID")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
