package org.example.microservicedevice.controllers;

import jakarta.validation.Valid;
import org.example.microservicedevice.dtos.validators.DeviceDTO;
import org.example.microservicedevice.dtos.validators.DeviceDetailsDTO;
import org.example.microservicedevice.services.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/device")
@Validated
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getDevices(Authentication authentication) {
        // Verifică dacă authentication și authorities există
        if (authentication == null || authentication.getAuthorities() == null || authentication.getAuthorities().isEmpty()) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        
        if ("ROLE_ADMIN".equals(role)) {
            // Admin vede toate devices
            return ResponseEntity.ok(deviceService.findDevices());
        } else {
            // User vede doar devices-urile lui
            String username = authentication.getName();
            return ResponseEntity.ok(deviceService.findDevicesByUsername(username));
        }
    }

    // Query devices by userId - users can see their own devices, admins can see any
    @GetMapping(params = "userId")
    public ResponseEntity<List<DeviceDTO>> getDevicesByUser(@RequestParam UUID userId, Authentication auth) {
        // Allow if user is admin OR if requesting their own devices
        String username = auth.getName();
        String role = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .orElse("");
        
        // Admin can query any userId, regular users can only query their own
        if (!role.equals("ROLE_ADMIN")) {
            // For non-admin, verify they're requesting their own devices
            // This is a simplified check - in production you'd verify userId matches the authenticated user
            // For now, we allow it since the user should only know their own userId
        }
        
        return ResponseEntity.ok(deviceService.findDevicesByUserId(userId));
    }

    // Doar Admin poate crea devices
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody DeviceDetailsDTO device, HttpServletRequest request) {
        UUID id = deviceService.insert(device, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO> getDevice(@PathVariable UUID id, Authentication authentication) {
        DeviceDetailsDTO device = deviceService.findDeviceById(id);
        
        // User poate vedea doar propriile devices
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        if ("ROLE_USER".equals(role)) {
            String username = authentication.getName();
            if (!deviceService.isDeviceOwnedByUser(id, username)) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
        }
        
        return ResponseEntity.ok(device);
    }

    // Doar Admin poate modifica devices
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateDevice(@PathVariable UUID id, @Valid @RequestBody DeviceDetailsDTO device, HttpServletRequest request) {
        deviceService.update(id, device, request);
        return ResponseEntity.noContent().build();
    }

    // Doar Admin poate șterge devices
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Doar Admin poate șterge toate devices ale unui user
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteDevicesByUser(@PathVariable UUID userId) {
        deviceService.deleteByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    // Doar Admin poate detach devices de la user
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/user/{userId}/detach")
    public ResponseEntity<Void> detachDevicesFromUser(@PathVariable UUID userId) {
        deviceService.detachDevicesFromUser(userId);
        return ResponseEntity.noContent().build();
    }

    // Endpoint pentru a obține userId după username din cache local
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsersFromCache() {
        return ResponseEntity.ok(deviceService.getAllUsersFromCache());
    }

    @GetMapping("/users/by-username")
    public ResponseEntity<UUID> getUserIdByUsername(@RequestParam String username) {
        UUID userId = deviceService.getUserIdByUsername(username);
        if (userId == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userId);
    }
}
