package com.example.monitoring.repository;

import com.example.monitoring.entity.DeviceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceInfoRepository extends JpaRepository<DeviceInfo, String> {
    Optional<DeviceInfo> findByDeviceId(String deviceId);
}
