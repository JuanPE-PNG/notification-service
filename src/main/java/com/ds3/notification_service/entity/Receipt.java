package com.ds3.notification_service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "receipts")
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, unique = true)
    private String paymentId;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private double amount;

    @Column(name = "payment_time", nullable = false)
    private LocalDateTime paymentTime;

    @Column(name = "receipt_content", columnDefinition = "TEXT", nullable = false)
    private String receiptContent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}