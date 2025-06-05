package com.ds3.notification_service.service;

import com.ds3.notification_service.dto.PaymentNotificationDto;
import com.ds3.notification_service.entity.Receipt;
import com.ds3.notification_service.repository.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReceiptService {
    private ReceiptRepository receiptRepository;

    @Autowired
    public ReceiptService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    public String generateReceipt(PaymentNotificationDto paymentDto) {
        return String.format(
                "RECIBO DE PAGO\n\n" +
                        "Orden de compra: %s\n\n" +
                        "Transacción: %s\n" +
                        "Fecha: %s\n\n" +
                        "Cliente: %s (ID: %s)\n\n" +
                        "Producto: %s\n" +
                        "Monto: $%.2f\n\n" +
                        "---\nGracias por su compra!",
                paymentDto.getPaymentId(),
                paymentDto.getTransactionId(),
                paymentDto.getPaymentTime().toString(),
                paymentDto.getCustomerName(),
                paymentDto.getCustomerId(),
                paymentDto.getProductName(),
                paymentDto.getAmount()
        );
    }

    public Receipt saveReceipt(PaymentNotificationDto paymentDto) {
        String receiptContent = generateReceipt(paymentDto);

        Receipt receipt = new Receipt();
        receipt.setPaymentId(paymentDto.getPaymentId());
        receipt.setTransactionId(paymentDto.getTransactionId());
        receipt.setCustomerName(paymentDto.getCustomerName());
        receipt.setCustomerId(paymentDto.getCustomerId());
        receipt.setProductName(paymentDto.getProductName());
        receipt.setAmount(paymentDto.getAmount());
        receipt.setPaymentTime(paymentDto.getPaymentTime());
        receipt.setReceiptContent(receiptContent);

        return receiptRepository.save(receipt);
    }
}
