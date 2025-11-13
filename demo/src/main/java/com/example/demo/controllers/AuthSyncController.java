package com.example.demo.controllers;

import com.example.demo.dtos.AuthUserDTO;
import com.example.demo.services.AuthSyncService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth-sync")
public class AuthSyncController {

    private final AuthSyncService authSyncService;

    public AuthSyncController(AuthSyncService authSyncService) {
        this.authSyncService = authSyncService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody AuthUserDTO dto) {
        authSyncService.createAuthUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody AuthUserDTO dto) {
        authSyncService.updateAuthUser(id, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        authSyncService.deleteAuthUser(id);
        return ResponseEntity.noContent().build();
    }
}
