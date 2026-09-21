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
import com.likhith.bankingapi.dto.request.MerchantPaymentRequest;
import com.likhith.bankingapi.dto.response.MerchantPaymentResponse;
import com.likhith.bankingapi.service.MerchantPaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/merchant-payments")
@RequiredArgsConstructor
@Tag(name = "Merchant Payments", description = "Point-of-sale and online merchant payments")
public class MerchantPaymentController {

    private static final Logger log = LoggerFactory.getLogger(MerchantPaymentController.class);

    private final MerchantPaymentService merchantPaymentService;

    @PostMapping
    @Operation(summary = "Make a payment to a merchant")
    public ResponseEntity<ApiResponse<MerchantPaymentResponse>> pay(
            @Valid @RequestBody MerchantPaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        log.info("Received merchant payment request for merchant {}", request.getMerchantId());
        MerchantPaymentResponse response = merchantPaymentService.pay(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Merchant payment completed successfully", response));
    }
}
