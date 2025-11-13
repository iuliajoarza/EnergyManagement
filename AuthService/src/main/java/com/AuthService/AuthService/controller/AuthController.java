package com.AuthService.AuthService.controller;

import com.AuthService.AuthService.dto.AuthRequest;
import com.AuthService.AuthService.dto.UserCredentialDTO;
import com.AuthService.AuthService.entity.UserCredential;
import com.AuthService.AuthService.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserCredentialDTO user) {
        log.info("POST /auth/register called with username: {}", user.getUsername());
        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        String token = authService.generateToken(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(token);
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validate(@RequestParam String token) {
        return ResponseEntity.ok(authService.validateToken(token));
    }

    @GetMapping("/admin/test")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Hello Admin");
    }

    @GetMapping("/user/test")
    public ResponseEntity<String> userOnly() {
        return ResponseEntity.ok("Hello User");
    }
}
