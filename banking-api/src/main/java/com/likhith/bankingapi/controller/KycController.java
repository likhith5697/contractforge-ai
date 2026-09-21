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
import com.likhith.bankingapi.dto.request.KycVerificationRequest;
import com.likhith.bankingapi.dto.response.KycVerificationResponse;
import com.likhith.bankingapi.service.KycService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC", description = "Know-your-customer verification")
public class KycController {

    private static final Logger log = LoggerFactory.getLogger(KycController.class);

    private final KycService kycService;

    @PostMapping("/verifications")
    @Operation(summary = "Record a KYC verification attempt for a customer")
    public ResponseEntity<ApiResponse<KycVerificationResponse>> verify(
            @Valid @RequestBody KycVerificationRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        log.info("Received KYC verification request for customer {}", request.getCustomerId());
        KycVerificationResponse response = kycService.verify(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("KYC verification recorded successfully", response));
    }
}
