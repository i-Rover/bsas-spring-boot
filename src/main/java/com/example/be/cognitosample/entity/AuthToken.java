package com.example.be.cognitosample.entity;

import lombok.Data;

@Data
public class AuthToken {
    private String userName;
    private String accessToken;
    private String idToken;
    private String refreshToken;
    private String tokenType;
    private Integer expiresIn;
}
