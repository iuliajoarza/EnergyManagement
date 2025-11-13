package org.example.microservicedevice.repositories;

import org.example.microservicedevice.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

    List<Device> findByName(String name);

    List<Device> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);

    @Query(value = "SELECT d " +
            "FROM Device d " +
            "WHERE LOWER(d.name) = LOWER(:name)")
    Optional<Device> findByNameCaseInsensitive(@Param("name") String name);

}
