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
import com.likhith.bankingapi.dto.request.CreateDisputeRequest;
import com.likhith.bankingapi.dto.response.DisputeResponse;
import com.likhith.bankingapi.service.DisputeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
@Tag(name = "Disputes", description = "Transaction dispute filing")
public class DisputeController {

    private static final Logger log = LoggerFactory.getLogger(DisputeController.class);

    private final DisputeService disputeService;

    @PostMapping
    @Operation(summary = "File a dispute against a transaction")
    public ResponseEntity<ApiResponse<DisputeResponse>> fileDispute(
            @Valid @RequestBody CreateDisputeRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        log.info("Received dispute request for transaction {}", request.getTransactionId());
        DisputeResponse response = disputeService.fileDispute(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Dispute filed successfully", response));
    }
}
