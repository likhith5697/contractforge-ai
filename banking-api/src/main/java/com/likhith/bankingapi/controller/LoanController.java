package com.likhith.bankingapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likhith.bankingapi.common.ApiResponse;
import com.likhith.bankingapi.dto.request.LoanApplicationRequest;
import com.likhith.bankingapi.dto.request.UploadLoanDocumentRequest;
import com.likhith.bankingapi.dto.response.LoanApplicationResponse;
import com.likhith.bankingapi.dto.response.LoanDocumentResponse;
import com.likhith.bankingapi.service.LoanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Loan applications and supporting document metadata")
public class LoanController {

    private static final Logger log = LoggerFactory.getLogger(LoanController.class);

    private final LoanService loanService;

    @PostMapping("/applications")
    @Operation(summary = "Submit a new loan application")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> submitApplication(
            @Valid @RequestBody LoanApplicationRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        log.info("Received loan application for customer {}", request.getCustomerId());
        LoanApplicationResponse response = loanService.submitApplication(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Loan application submitted successfully", response));
    }

    @PostMapping("/{loanId}/documents")
    @Operation(summary = "Register document metadata for a loan application")
    public ResponseEntity<ApiResponse<LoanDocumentResponse>> uploadDocumentMetadata(
            @PathVariable String loanId,
            @Valid @RequestBody UploadLoanDocumentRequest request) {
        log.info("Received document metadata for loan {}", loanId);
        LoanDocumentResponse response = loanService.uploadDocumentMetadata(loanId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Document metadata registered successfully", response));
    }
}
