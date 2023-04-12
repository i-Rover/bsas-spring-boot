package com.example.be.cognitosample.entity;

import lombok.Data;

@Data
public class UserSignUpRequest {
    private String username;
    private String email;
    private String password;
}
