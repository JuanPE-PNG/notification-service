package com.ds3.notification_service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.ds3.notification_service.client.UserServiceClient;
import com.ds3.notification_service.controller.NotificationController;
import com.ds3.notification_service.dto.PaymentNotificationDto;
import com.ds3.notification_service.dto.TokenValidationResponse;
import com.ds3.notification_service.entity.Receipt;
import com.ds3.notification_service.repository.ReceiptRepository;
import com.ds3.notification_service.service.ReceiptService;
import com.ds3.notification_service.service.TokenValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
class NotificationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}

@WebMvcTest(NotificationController.class)
class NotificationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReceiptService receiptService;

    @MockBean
    private ReceiptRepository receiptRepository;

    @MockBean
    private TokenValidationService tokenValidationService;

    @MockBean
    private UserServiceClient userServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentNotificationDto paymentDto;
    private Receipt receipt;
    private TokenValidationResponse validAdminResponse;
    private TokenValidationResponse validUserResponse;
    private TokenValidationResponse invalidResponse;

    @BeforeEach
    void setUp() {
        // Configurar DTO de notificación de pago
        paymentDto = new PaymentNotificationDto();
        paymentDto.setPaymentId("PAY-123");
        paymentDto.setTransactionId("TXN-456");
        paymentDto.setCustomerName("Juan Pérez");
        paymentDto.setCustomerId("CUST-001");
        paymentDto.setProductName("Producto Premium");
        paymentDto.setAmount(99.99);
        paymentDto.setPaymentTime(LocalDateTime.now());
        paymentDto.setPaymentStatus("Success");

        // Configurar entidad Receipt
        receipt = new Receipt();
        receipt.setId(1L);
        receipt.setPaymentId("PAY-123");
        receipt.setTransactionId("TXN-456");
        receipt.setCustomerName("Juan Pérez");
        receipt.setCustomerId("CUST-001");
        receipt.setProductName("Producto Premium");
        receipt.setAmount(99.99);
        receipt.setPaymentTime(LocalDateTime.now());
        receipt.setReceiptContent("RECIBO DE PAGO...");
        receipt.setPaymentStatus("Success");
        receipt.setCreatedAt(LocalDateTime.now());

        // Configurar respuestas de validación de token
        validAdminResponse = new TokenValidationResponse();
        validAdminResponse.setValid(true);
        validAdminResponse.setAdmin(true);
        validAdminResponse.setMessage("User is an administrator");

        validUserResponse = new TokenValidationResponse();
        validUserResponse.setValid(true);
        validUserResponse.setAdmin(false);
        validUserResponse.setMessage("Token is valid");

        invalidResponse = new TokenValidationResponse();
        invalidResponse.setValid(false);
        invalidResponse.setAdmin(false);
        invalidResponse.setMessage("Invalid token");
    }

    // Test 1: Notificación de pago exitosa
    @Test
    void testHandlePaymentNotification_Success() throws Exception {
        when(receiptService.saveReceipt(any(PaymentNotificationDto.class))).thenReturn(receipt);

        mockMvc.perform(post("/api/notifications/payment")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paymentDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Recibo guardado con ID: 1"));
    }

    // Test 2: Obtener todos los recibos con acceso de administrador
    @Test
    void testGetAllReceipts_AdminAccess() throws Exception {
        when(tokenValidationService.validateAdminToken(anyString())).thenReturn(validAdminResponse);
        when(receiptRepository.findByPaymentStatusOrderByCreatedAtDesc("Success"))
                .thenReturn(Arrays.asList(receipt));

        mockMvc.perform(get("/api/notifications/receipts")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").value("PAY-123"));
    }

    // Test 3: Obtener todos los recibos sin autorización (corregido para esperar 403)
    @Test
    void testGetAllReceipts_NoAuthorization() throws Exception {
        mockMvc.perform(get("/api/notifications/receipts"))
                .andExpect(status().isForbidden());
    }

    // Test 4: Obtener recibo por paymentId encontrado
    @Test
    void testGetReceiptByPaymentId_Found() throws Exception {
        when(receiptRepository.findByPaymentId("PAY-123")).thenReturn(receipt);

        mockMvc.perform(get("/api/notifications/receipt/PAY-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("PAY-123"))
                .andExpect(jsonPath("$.customerName").value("Juan Pérez"));
    }

    // Test 5: Obtener recibo por paymentId no encontrado
    @Test
    void testGetReceiptByPaymentId_NotFound() throws Exception {
        when(receiptRepository.findByPaymentId("PAY-999")).thenReturn(null);

        mockMvc.perform(get("/api/notifications/receipt/PAY-999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Recibo no encontrado para paymentId: PAY-999"));
    }

    // Test 6: Obtener mis recibos con token válido
    @Test
    void testGetMyReceipts_ValidToken() throws Exception {
        when(tokenValidationService.validateUserToken(anyString())).thenReturn(validUserResponse);
        when(receiptRepository.findByCustomerIdAndPaymentStatusOrderByCreatedAtDesc("CUST-001", "Success"))
                .thenReturn(Arrays.asList(receipt));

        mockMvc.perform(get("/api/notifications/my-receipts")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IkNVU1QtMDAxIn0.signature"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value("CUST-001"));
    }

    // Test 7: Obtener mis recibos sin autorización (corregido para esperar 403)
    @Test
    void testGetMyReceipts_NoAuthorization() throws Exception {
        mockMvc.perform(get("/api/notifications/my-receipts"))
                .andExpect(status().isForbidden());
    }

    // Test 8: Obtener mis recibos con token malformado
    @Test
    void testGetMyReceipts_MalformedToken() throws Exception {
        when(tokenValidationService.validateUserToken(anyString())).thenReturn(validUserResponse);

        mockMvc.perform(get("/api/notifications/my-receipts")
                .header("Authorization", "Bearer malformed.token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No se pudo extraer el id del token")));
    }

    // Test 9: Acceso no autorizado a todos los recibos
    @Test
    void testGetAllReceipts_NonAdminAccess() throws Exception {
        when(tokenValidationService.validateAdminToken(anyString())).thenReturn(invalidResponse);

        mockMvc.perform(get("/api/notifications/receipts")
                .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Solo administradores pueden ver todos los recibos")));
    }

    // Test 10: Token inválido para mis recibos
    @Test
    void testGetMyReceipts_InvalidToken() throws Exception {
        when(tokenValidationService.validateUserToken(anyString())).thenReturn(invalidResponse);

        mockMvc.perform(get("/api/notifications/my-receipts")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Token inválido")));
    }
}

@SpringBootTest
class ReceiptServiceTests {

    @MockBean
    private ReceiptRepository receiptRepository;

    @Autowired
    private ReceiptService receiptService;

    private PaymentNotificationDto paymentDto;

    @BeforeEach
    void setUp() {
        paymentDto = new PaymentNotificationDto();
        paymentDto.setPaymentId("PAY-123");
        paymentDto.setTransactionId("TXN-456");
        paymentDto.setCustomerName("María García");
        paymentDto.setCustomerId("CUST-002");
        paymentDto.setProductName("Servicio Básico");
        paymentDto.setAmount(49.99);
        paymentDto.setPaymentTime(LocalDateTime.of(2024, 1, 15, 10, 30));
        paymentDto.setPaymentStatus("Success");
    }

    // Test 11: Guardar recibo exitosamente
    @Test
    void testSaveReceipt_Success() {
        Receipt savedReceipt = new Receipt();
        savedReceipt.setId(1L);
        savedReceipt.setPaymentId("PAY-123");
        savedReceipt.setReceiptContent("RECIBO DE PAGO...");

        when(receiptRepository.save(any(Receipt.class))).thenReturn(savedReceipt);

        Receipt result = receiptService.saveReceipt(paymentDto);

        assert result.getId() == 1L;
        assert result.getPaymentId().equals("PAY-123");
        assert result.getReceiptContent().contains("RECIBO DE PAGO");
    }
}

@SpringBootTest
class TokenValidationServiceTests {

    @MockBean
    private UserServiceClient userServiceClient;

    @Autowired
    private TokenValidationService tokenValidationService;

    // Test 12: Validar token de administrador válido
    @Test
    void testValidateAdminToken_ValidAdmin() {
        ResponseEntity<String> response = new ResponseEntity<>("Admin validated", HttpStatus.OK);
        when(userServiceClient.validateAdmin(anyString())).thenReturn(response);

        TokenValidationResponse result = tokenValidationService.validateAdminToken("Bearer admin-token");

        assert result.isValid();
        assert result.isAdmin();
        assert result.getMessage().equals("User is an administrator");
    }

    // Test 13: Validar token de usuario válido
    @Test
    void testValidateUserToken_ValidToken() {
        ResponseEntity<String> response = new ResponseEntity<>("Token valid", HttpStatus.OK);
        when(userServiceClient.validateToken(anyString())).thenReturn(response);

        TokenValidationResponse result = tokenValidationService.validateUserToken("Bearer user-token");

        assert result.isValid();
        assert !result.isAdmin();
        assert result.getMessage().equals("Token is valid");
    }

    // Test 14: Validar header de autorización inválido
    @Test
    void testValidateAdminToken_InvalidHeader() {
        TokenValidationResponse result = tokenValidationService.validateAdminToken("InvalidHeader");

        assert !result.isValid();
        assert !result.isAdmin();
        assert result.getMessage().equals("Invalid authorization header");
    }
}
