package com.ds3.notification_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentNotificationDto {

    private String paymentId;
    private String transactionId;
    private String customerName;
    private String customerId;
    private String productName;
    private double amount;
    private LocalDateTime paymentTime;
}