package org.example.microservicedevice.controllers;

import org.example.microservicedevice.dtos.UserCacheDTO;
import org.example.microservicedevice.entities.UserCache;
import org.example.microservicedevice.services.UserCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usercache")
public class UserCacheController {
    private final UserCacheService userCacheService;

    @Autowired
    public UserCacheController(UserCacheService userCacheService) {
        this.userCacheService = userCacheService;
    }

    @GetMapping(params = "username")
    public ResponseEntity<UUID> getUserIdByUsername(@RequestParam String username) {
        UUID userId = userCacheService.getUserIdByUsername(username);
        if (userId == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userId);
    }

    // Endpoint pentru toți userii din cache - returnează format compatibil cu frontend
    @GetMapping("/all")
    public ResponseEntity<List<UserCacheDTO>> getAllUsers() {
        List<UserCache> users = userCacheService.getAllUsers();
        List<UserCacheDTO> dtos = users.stream()
            .map(u -> new UserCacheDTO(u.getUserId(), u.getUsername()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
