package com.example.be.cognitosample.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

@Configuration
public class CognitoConfig {
    @Value(value="${aws.access-key}")
    private String accessKey;
    @Value(value="${aws.access-secret}")
    private String secretKey;

    private String theAllowedOrigins = "http://127.0.0.1:5173";

    @Bean
    public AWSCognitoIdentityProvider cognitoClient(){
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        return AWSCognitoIdentityProviderClientBuilder.standard()
        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
        .withRegion("ap-southeast-1").build();
    }
}
