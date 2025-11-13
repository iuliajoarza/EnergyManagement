package com.AuthService.AuthService.dto;

import lombok.Data;

@Data
public class UserCredentialDTO {
    private String username;
    private String password;
    private String role;
}
