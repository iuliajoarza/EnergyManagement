package com.example.monitoring.controller;

import com.example.monitoring.entity.HourlyEnergyConsumption;
import com.example.monitoring.repository.HourlyEnergyConsumptionRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/monitoring/energy")
@CrossOrigin(origins = "*")
public class EnergyConsumptionController {
    private final HourlyEnergyConsumptionRepository hourlyRepository;

    public EnergyConsumptionController(HourlyEnergyConsumptionRepository hourlyRepository) {
        this.hourlyRepository = hourlyRepository;
    }

    @GetMapping("/hourly/{deviceId}")
    public ResponseEntity<List<HourlyEnergyConsumption>> getHourlyConsumption(
            @PathVariable String deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        List<HourlyEnergyConsumption> data = hourlyRepository
                .findByDeviceIdAndHourTimestampBetween(deviceId, startOfDay, endOfDay);
        
        return ResponseEntity.ok(data);
    }

    @GetMapping("/hourly")
    public ResponseEntity<List<HourlyEnergyConsumption>> getAllHourlyConsumption() {
        return ResponseEntity.ok(hourlyRepository.findAll());
    }
}
