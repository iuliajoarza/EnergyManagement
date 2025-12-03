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
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;
    private final SyncPublisherService syncPublisherService;
    private final UserCacheService userCacheService;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository,
                         SyncPublisherService syncPublisherService,
                         UserCacheService userCacheService) {
        this.deviceRepository = deviceRepository;
        this.syncPublisherService = syncPublisherService;
        this.userCacheService = userCacheService;
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
        // Validate user existence via user cache
        if (!userCacheService.userExists(userId)) {
            LOGGER.error("User with id {} does not exist in user cache", userId);
            throw new BadRequestException("User with id: " + userId + " does not exist");
        }
        LOGGER.debug("Validated existence of userId {} via user cache", userId);
    }

    public UUID insert(@Valid DeviceDetailsDTO deviceDTO, HttpServletRequest request) {
        assertUserExists(deviceDTO.getUserId(), request);
        Device device = DeviceBuilder.toEntity(deviceDTO);
        device = deviceRepository.save(device);
        LOGGER.debug("Device with id {} was inserted in db", device.getId());
        
        // Publish device sync event
        syncPublisherService.publishDeviceSync(device.getId().toString(), device.getName());
        
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
        syncPublisherService.publishDeviceDeleted(id.toString());
        LOGGER.debug("Device with id {} was deleted from db", id);
    }

    public void deleteByUserId(UUID userId) {
        List<Device> devices = deviceRepository.findByUserId(userId);
        deviceRepository.deleteByUserId(userId);
        // Publish deletion event for each device
        for (Device device : devices) {
            syncPublisherService.publishDeviceDeleted(device.getId().toString());
        }
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
        UUID userId = userCacheService.getUserIdByUsername(username);
        if (userId == null) {
            LOGGER.error("User with username {} not found in user cache", username);
            throw new ResourceNotFoundException("User not found: " + username);
        }
        return userId;
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

    public java.util.List<org.example.microservicedevice.dtos.UserCacheDTO> getAllUsersFromCache() {
        return userCacheService.getAllUsers().stream()
            .map(u -> new org.example.microservicedevice.dtos.UserCacheDTO(u.getUserId(), u.getUsername()))
            .collect(java.util.stream.Collectors.toList());
    }

    public UUID getUserIdByUsername(String username) {
        return userCacheService.getUserIdByUsername(username);
    }
}