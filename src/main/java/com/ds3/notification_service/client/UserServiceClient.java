package com.ds3.notification_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service")
public interface UserServiceClient {
    @GetMapping("/auth/validateToken")
    ResponseEntity<String> validateToken(@RequestParam("token") String token);

    @GetMapping("/admin/validateAdmin")
    ResponseEntity<String> validateAdmin(@RequestHeader("Authorization") String token);
} 