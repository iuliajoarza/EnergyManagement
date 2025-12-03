package com.example.monitoring.controller;

import com.example.monitoring.entity.DeviceData;
import com.example.monitoring.repository.DeviceDataRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ConsumptionController {
    private final DeviceDataRepository repository;

    public ConsumptionController(DeviceDataRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/consumption")
    public Map<Integer, Double> getHourlyConsumption(@RequestParam String deviceId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.atTime(LocalTime.MAX);
        List<DeviceData> data = repository.findAll().stream()
            .filter(d -> d.getDeviceId().equals(deviceId))
            .filter(d -> !d.getTimestamp().isBefore(start) && !d.getTimestamp().isAfter(end))
            .collect(Collectors.toList());
        return data.stream().collect(Collectors.groupingBy(
            d -> d.getTimestamp().getHour(),
            Collectors.summingDouble(DeviceData::getMeasurementValue)
        ));
    }
}
