package org.example.microservicedevice.services;


import jakarta.validation.Valid;
import org.example.microservicedevice.dtos.builders.DeviceBuilder;
import org.example.microservicedevice.dtos.validators.DeviceDTO;
import org.example.microservicedevice.dtos.validators.DeviceDetailsDTO;
import org.example.microservicedevice.entities.Device;
import org.example.microservicedevice.handlers.exceptions.model.ResourceNotFoundException;
import org.example.microservicedevice.handlers.exceptions.model.BadRequestException;
import org.example.microservicedevice.repositories.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;
    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository,
                         RestTemplate restTemplate,
                         @Value("${user.service.base-url}") String userServiceBaseUrl) {
        this.deviceRepository = deviceRepository;
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    public List<DeviceDTO> findDevices() {
        List<Device> deviceList = deviceRepository.findAll();
        return deviceList.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public List<DeviceDTO> findDevicesByUserId(UUID userId) {
        return deviceRepository.findByUserId(userId)
                .stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public DeviceDetailsDTO findDeviceById(UUID id) {
        Optional<Device> deviceOptional = deviceRepository.findById(id);
        if (!deviceOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        return DeviceBuilder.toDeviceDetailsDTO(deviceOptional.get());
    }

    private void assertUserExists(UUID userId, HttpServletRequest request) {
        // Allow null userId - device can exist without a user
        if (userId == null) {
            LOGGER.debug("Device has no assigned user (userId is null)");
            return;
        }
        // Validate user existence via People Service
        String url = userServiceBaseUrl + "/user/" + userId;
        try {
            HttpHeaders headers = new HttpHeaders();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null) {
                headers.set("Authorization", authHeader);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            LOGGER.debug("Validated existence of userId {} via People Service", userId);
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.error("User with id {} does not exist in People Service", userId);
            throw new BadRequestException("User with id: " + userId + " does not exist");
        } catch (HttpClientErrorException e) {
            LOGGER.error("Error validating userId {}: {}", userId, e.getStatusCode());
            throw new BadRequestException("Failed to validate user existence: " + e.getStatusCode());
        } catch (ResourceAccessException e) {
            LOGGER.error("People Service unavailable when validating userId {}", userId);
            throw new BadRequestException("People Service unavailable");
        }
    }

    public UUID insert(@Valid DeviceDetailsDTO deviceDTO, HttpServletRequest request) {
        assertUserExists(deviceDTO.getUserId(), request);
        Device device = DeviceBuilder.toEntity(deviceDTO);
        device = deviceRepository.save(device);
        LOGGER.debug("Device with id {} was inserted in db", device.getId());
        return device.getId();
    }

    public void update(UUID id, @Valid DeviceDetailsDTO dto, HttpServletRequest request) {
        Optional<Device> opt = deviceRepository.findById(id);
        if (!opt.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        assertUserExists(dto.getUserId(), request);
        Device d = opt.get();
        d.setName(dto.getName());
        d.setMaxConsumption(dto.getMaxConsumption());
        d.setUserId(dto.getUserId());
        deviceRepository.save(d);
        LOGGER.debug("Device with id {} was updated", id);
    }

    public void delete(UUID id) {
        if (!deviceRepository.existsById(id)) {
            LOGGER.error("Attempted to delete non-existent device with id {}", id);
            throw new ResourceNotFoundException("Device with id: " + id + " not found");
        }
        deviceRepository.deleteById(id);
        LOGGER.debug("Device with id {} was deleted from db", id);
    }

    public void deleteByUserId(UUID userId) {
        deviceRepository.deleteByUserId(userId);
        LOGGER.debug("All devices for user {} were deleted", userId);
    }

    public void detachDevicesFromUser(UUID userId) {
        List<Device> devices = deviceRepository.findByUserId(userId);
        for (Device device : devices) {
            device.setUserId(null);
        }
        deviceRepository.saveAll(devices);
        LOGGER.debug("All devices for user {} were detached (userId set to null)", userId);
    }

    public List<DeviceDTO> findDevicesByUsername(String username) {
        // 1. Call People Service pentru a găsi userId
        UUID userId = getUserIdFromPeopleService(username);
        
        // 2. Găsește devices pentru acel userId
        return findDevicesByUserId(userId);
    }

    private UUID getUserIdFromPeopleService(String username) {
        try {
            String url = userServiceBaseUrl + "/user?username=" + username;
            // Folosim Map pentru a parsa JSON-ul simplu
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> response = restTemplate.getForObject(url, java.util.Map.class);
            
            if (response != null && response.containsKey("id")) {
                String userIdStr = (String) response.get("id");
                return UUID.fromString(userIdStr);
            }
            throw new ResourceNotFoundException("User not found: " + username);
        } catch (Exception e) {
            LOGGER.error("Failed to get userId from People Service for username: {}", username, e);
            throw new ResourceNotFoundException("User not found: " + username);
        }
    }

    public boolean isDeviceOwnedByUser(UUID deviceId, String username) {
        try {
            UUID userId = getUserIdFromPeopleService(username);
            Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
            
            if (!deviceOpt.isPresent()) {
                return false;
            }
            
            Device device = deviceOpt.get();
            return device.getUserId() != null && device.getUserId().equals(userId);
        } catch (Exception e) {
            LOGGER.error("Error checking device ownership for device {} and user {}", deviceId, username, e);
            return false;
        }
    }
}