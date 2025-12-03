package com.example.monitoring.repository;

import com.example.monitoring.entity.HourlyEnergyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HourlyEnergyConsumptionRepository extends JpaRepository<HourlyEnergyConsumption, Long> {
    Optional<HourlyEnergyConsumption> findByDeviceIdAndHourTimestamp(String deviceId, LocalDateTime hourTimestamp);
    List<HourlyEnergyConsumption> findByDeviceIdAndHourTimestampBetween(String deviceId, LocalDateTime start, LocalDateTime end);
}
