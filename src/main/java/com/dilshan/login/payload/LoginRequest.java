package com.dilshan.login.payload;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}