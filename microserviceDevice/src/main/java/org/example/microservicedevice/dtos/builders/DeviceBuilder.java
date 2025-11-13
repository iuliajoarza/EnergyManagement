package org.example.microservicedevice.dtos.builders;

import org.example.microservicedevice.dtos.validators.DeviceDTO;
import org.example.microservicedevice.dtos.validators.DeviceDetailsDTO;
import org.example.microservicedevice.entities.Device;

public class DeviceBuilder {

    private DeviceBuilder() {
    }

    public static DeviceDTO toDeviceDTO(Device device) {
        return new DeviceDTO(
                device.getId(),
                device.getName(),
                device.getMaxConsumption(),
                device.getUserId()
        );
    }

    public static DeviceDetailsDTO toDeviceDetailsDTO(Device device) {
        return new DeviceDetailsDTO(
                device.getId(),
                device.getName(),
                device.getMaxConsumption(),
                device.getUserId()
        );
    }

    public static Device toEntity(DeviceDetailsDTO deviceDetailsDTO) {
        return new Device(
                deviceDetailsDTO.getName(),
                deviceDetailsDTO.getMaxConsumption(),
                deviceDetailsDTO.getUserId()
        );
    }
}

