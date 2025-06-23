package com.ds3.notification_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ds3.notification_service.client.UserServiceClient;
import com.ds3.notification_service.dto.TokenValidationResponse;

@Service
public class TokenValidationService {
    @Autowired
    private UserServiceClient userServiceClient;

    public TokenValidationResponse validateAdminToken(String authorizationHeader) {
        TokenValidationResponse response = new TokenValidationResponse();
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.setValid(false);
            response.setAdmin(false);
            response.setMessage("Invalid authorization header");
            return response;
        }
        try {
            ResponseEntity<String> adminResponse = userServiceClient.validateAdmin(authorizationHeader);
            if (adminResponse.getStatusCode() == HttpStatus.OK) {
                response.setValid(true);
                response.setAdmin(true);
                response.setMessage("User is an administrator");
            } else {
                response.setValid(false);
                response.setAdmin(false);
                response.setMessage("User is not an administrator");
            }
        } catch (Exception e) {
            response.setValid(false);
            response.setAdmin(false);
            response.setMessage("Error validating token: " + e.getMessage());
        }
        return response;
    }

    public TokenValidationResponse validateUserToken(String authorizationHeader) {
        TokenValidationResponse response = new TokenValidationResponse();
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.setValid(false);
            response.setAdmin(false);
            response.setMessage("Invalid authorization header");
            return response;
        }
        try {
            String token = authorizationHeader.substring(7);
            ResponseEntity<String> userResponse = userServiceClient.validateToken(token);
            if (userResponse.getStatusCode() == HttpStatus.OK) {
                response.setValid(true);
                response.setAdmin(false);
                response.setMessage("Token is valid");
            } else {
                response.setValid(false);
                response.setAdmin(false);
                response.setMessage("Token is not valid");
            }
        } catch (Exception e) {
            response.setValid(false);
            response.setAdmin(false);
            response.setMessage("Error validating token: " + e.getMessage());
        }
        return response;
    }
} 