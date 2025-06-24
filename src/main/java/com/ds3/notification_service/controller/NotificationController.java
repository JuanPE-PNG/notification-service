package com.ds3.notification_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ds3.notification_service.dto.PaymentNotificationDto;
import com.ds3.notification_service.dto.TokenValidationResponse;
import com.ds3.notification_service.entity.Receipt;
import com.ds3.notification_service.repository.ReceiptRepository;
import com.ds3.notification_service.service.ReceiptService;
import com.ds3.notification_service.service.TokenValidationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final ReceiptService receiptService;
    private final ReceiptRepository receiptRepository;
    private final TokenValidationService tokenValidationService;

    @Autowired
    public NotificationController(ReceiptService receiptService,
                                  ReceiptRepository receiptRepository,
                                  TokenValidationService tokenValidationService) {
        this.receiptService = receiptService;
        this.receiptRepository = receiptRepository;
        this.tokenValidationService = tokenValidationService;
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
    public ResponseEntity<?> getAllReceipts(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Se requiere autorización para acceder a este recurso");
        }
        TokenValidationResponse validation = tokenValidationService.validateAdminToken(authorization);
        if (!validation.isValid() || !validation.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solo administradores pueden ver todos los recibos. " + validation.getMessage());
        }
        var receipts = receiptRepository.findByPaymentStatusOrderByCreatedAtDesc("Success");
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

    @GetMapping("/my-receipts")
    public ResponseEntity<?> getMyReceipts(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Se requiere autorización para acceder a este recurso");
        }
        TokenValidationResponse validation = tokenValidationService.validateUserToken(authorization);
        if (!validation.isValid()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token inválido: " + validation.getMessage());
        }
        // Extraer el customerId del token (asumimos que es el claim 'id')
        String token = authorization.substring(7);
        String customerId;
        try {
            // Decodificar el JWT sin validación (solo para demo, en prod usar una lib segura)
            String[] parts = token.split("\\.");
            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> claims = mapper.readValue(payloadJson, java.util.Map.class);
            customerId = String.valueOf(claims.get("id"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se pudo extraer el id del token: " + e.getMessage());
        }
        var receipts = receiptRepository.findByCustomerIdAndPaymentStatusOrderByCreatedAtDesc(customerId, "Success");
        return ResponseEntity.ok(receipts);
    }
}