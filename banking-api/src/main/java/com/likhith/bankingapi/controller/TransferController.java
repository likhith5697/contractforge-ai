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
import com.likhith.bankingapi.dto.request.DomesticTransferRequest;
import com.likhith.bankingapi.dto.request.InternationalTransferRequest;
import com.likhith.bankingapi.dto.response.InternationalTransferResponse;
import com.likhith.bankingapi.dto.response.TransferResponse;
import com.likhith.bankingapi.service.TransferService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Domestic and international fund transfers")
public class TransferController {

    private static final Logger log = LoggerFactory.getLogger(TransferController.class);

    private final TransferService transferService;

    @PostMapping
    @Operation(summary = "Create a domestic funds transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> createDomesticTransfer(
            @Valid @RequestBody DomesticTransferRequest request,
            @Parameter(description = "Client-generated key to safely retry this request")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        log.info("Received domestic transfer request from {} to {}", request.getSourceAccountId(),
                request.getDestinationAccountId());
        TransferResponse response = transferService.createDomesticTransfer(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Transfer completed successfully", response));
    }

    @PostMapping("/international")
    @Operation(summary = "Create an international wire (SWIFT) transfer")
    public ResponseEntity<ApiResponse<InternationalTransferResponse>> createInternationalTransfer(
            @Valid @RequestBody InternationalTransferRequest request,
            @Parameter(description = "Client-generated key to safely retry this request")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        log.info("Received international transfer request from {}", request.getSourceAccountId());
        InternationalTransferResponse response = transferService.createInternationalTransfer(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("International transfer initiated successfully", response));
    }
}
