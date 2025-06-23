package com.ds3.notification_service.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PaymentNotificationDto {

    private String paymentId;
    private String transactionId;
    private String customerName;
    private String customerId;
    private String productName;
    private double amount;
    private LocalDateTime paymentTime;
    private String paymentStatus;
}