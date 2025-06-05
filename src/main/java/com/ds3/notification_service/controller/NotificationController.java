package com.ds3.notification_service.controller;

import com.ds3.notification_service.dto.PaymentNotificationDto;
import com.ds3.notification_service.service.ReceiptService;
import com.ds3.notification_service.entity.Receipt;
import com.ds3.notification_service.repository.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final ReceiptService receiptService;
    private final ReceiptRepository receiptRepository;

    @Autowired
    public NotificationController(ReceiptService receiptService,
                                  ReceiptRepository receiptRepository) {
        this.receiptService = receiptService;
        this.receiptRepository = receiptRepository; // Inyección añadida
    }

    @PostMapping("/payment")
    public ResponseEntity<String> handlePaymentNotification(@RequestBody PaymentNotificationDto paymentDto) {
        try {
            Receipt savedReceipt = receiptService.saveReceipt(paymentDto);
            System.out.println("Recibo generado:\n" + savedReceipt.getReceiptContent());
            return ResponseEntity.ok("Recibo guardado con ID: " + savedReceipt.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la notificación: " + e.getMessage());
        }
    }

    @GetMapping("/receipts")
    public ResponseEntity<List<Receipt>> getAllReceipts() {
        List<Receipt> receipts = receiptRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(receipts);
    }

    @GetMapping("/receipt/{paymentId}") // Ruta corregida
    public ResponseEntity<?> getReceiptByPaymentId(@PathVariable String paymentId) {
        Receipt receipt = receiptRepository.findByPaymentId(paymentId);
        if (receipt != null) {
            return ResponseEntity.ok(receipt);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Recibo no encontrado para paymentId: " + paymentId);
        }
    }
}