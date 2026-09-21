package com.likhith.bankingapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likhith.bankingapi.common.ApiResponse;
import com.likhith.bankingapi.dto.request.BillPaymentRequest;
import com.likhith.bankingapi.dto.request.ScheduledPaymentRequest;
import com.likhith.bankingapi.dto.response.PaymentResponse;
import com.likhith.bankingapi.dto.response.ScheduledPaymentResponse;
import com.likhith.bankingapi.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Bill payments, immediate and scheduled")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Pay a bill immediately")
    public ResponseEntity<ApiResponse<PaymentResponse>> payBill(
            @Valid @RequestBody BillPaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        log.info("Received bill payment request for account {}", request.getAccountId());
        PaymentResponse response = paymentService.payBill(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Bill payment completed successfully", response));
    }

    @PostMapping("/scheduled")
    @Operation(summary = "Schedule a future or recurring bill payment")
    public ResponseEntity<ApiResponse<ScheduledPaymentResponse>> schedulePayment(
            @Valid @RequestBody ScheduledPaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        log.info("Received scheduled payment request for account {}", request.getAccountId());
        ScheduledPaymentResponse response = paymentService.schedulePayment(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Payment scheduled successfully", response));
    }
}
