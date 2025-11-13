package com.AuthService.AuthService.repository;

import com.AuthService.AuthService.entity.UserCredential;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserCredentialRepository extends CrudRepository<UserCredential, Long> {
    Optional<UserCredential> findByUsername(String username);
}
