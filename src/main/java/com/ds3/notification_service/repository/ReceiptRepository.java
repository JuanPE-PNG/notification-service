package com.ds3.notification_service.repository;

import com.ds3.notification_service.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findAllByOrderByCreatedAtDesc();
    Receipt findByPaymentId(String paymentId);
}
