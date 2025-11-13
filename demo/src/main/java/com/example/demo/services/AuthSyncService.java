package com.example.demo.services;

import com.example.demo.dtos.AuthUserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthSyncService {

    private final RestTemplate restTemplate;

    @Value("${auth.service.base-url:http://localhost:8080}")
    private String authServiceBaseUrl;

    public AuthSyncService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void createAuthUser(AuthUserDTO dto) {
        String url = authServiceBaseUrl + "/internal/auth-users";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthUserDTO> request = new HttpEntity<>(dto, headers);
        try {
            restTemplate.postForEntity(url, request, Void.class);
        } catch (RestClientException ex) {
            throw new RuntimeException("Failed to sync create auth user: " + ex.getMessage(), ex);
        }
    }

    public void updateAuthUser(Long id, AuthUserDTO dto) {
        String url = authServiceBaseUrl + "/internal/auth-users/" + id;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthUserDTO> request = new HttpEntity<>(dto, headers);
        try {
            restTemplate.put(url, request);
        } catch (RestClientException ex) {
            throw new RuntimeException("Failed to sync update auth user: " + ex.getMessage(), ex);
        }
    }

    public void deleteAuthUser(Long id) {
        String url = authServiceBaseUrl + "/internal/auth-users/" + id;
        try {
            restTemplate.delete(url);
        } catch (RestClientException ex) {
            throw new RuntimeException("Failed to sync delete auth user: " + ex.getMessage(), ex);
        }
    }
}
