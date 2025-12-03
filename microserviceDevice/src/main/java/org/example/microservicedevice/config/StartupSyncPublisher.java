package org.example.microservicedevice.config;

import org.example.microservicedevice.entities.Device;
import org.example.microservicedevice.repositories.DeviceRepository;
import org.example.microservicedevice.services.SyncPublisherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StartupSyncPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(StartupSyncPublisher.class);
    private final DeviceRepository deviceRepository;
    private final SyncPublisherService syncPublisherService;

    public StartupSyncPublisher(DeviceRepository deviceRepository, SyncPublisherService syncPublisherService) {
        this.deviceRepository = deviceRepository;
        this.syncPublisherService = syncPublisherService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void publishExistingDevices() {
        LOGGER.info("Publishing sync events for all existing devices...");
        List<Device> devices = deviceRepository.findAll();
        for (Device device : devices) {
            syncPublisherService.publishDeviceSync(device.getId().toString(), device.getName());
        }
        LOGGER.info("Published sync events for {} devices", devices.size());
    }
}
