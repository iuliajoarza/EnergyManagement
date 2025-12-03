package org.example.microservicedevice.repositories;

import org.example.microservicedevice.entities.UserCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCacheRepository extends JpaRepository<UserCache, UUID> {
    Optional<UserCache> findByUsername(String username);
    boolean existsByUserId(UUID userId);
}
