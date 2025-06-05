package com.ds3.notification_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}