package com.ds3.notification_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ds3.notification_service.entity.Receipt;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findAllByOrderByCreatedAtDesc();
    Receipt findByPaymentId(String paymentId);
    List<Receipt> findByCustomerIdAndPaymentStatusOrderByCreatedAtDesc(String customerId, String paymentStatus);
    List<Receipt> findByPaymentStatusOrderByCreatedAtDesc(String paymentStatus);
}
