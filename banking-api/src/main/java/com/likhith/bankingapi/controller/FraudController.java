package com.likhith.bankingapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likhith.bankingapi.common.ApiResponse;
import com.likhith.bankingapi.dto.request.CreateFraudCaseRequest;
import com.likhith.bankingapi.dto.response.FraudCaseResponse;
import com.likhith.bankingapi.service.FraudCaseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
@Tag(name = "Fraud", description = "Fraud case management")
public class FraudController {

    private static final Logger log = LoggerFactory.getLogger(FraudController.class);

    private final FraudCaseService fraudCaseService;

    @PostMapping("/cases")
    @Operation(summary = "Open a new fraud investigation case")
    public ResponseEntity<ApiResponse<FraudCaseResponse>> createCase(
            @Valid @RequestBody CreateFraudCaseRequest request) {
        log.info("Received fraud case request for account {}", request.getAccountId());
        FraudCaseResponse response = fraudCaseService.createCase(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Fraud case opened successfully", response));
    }
}
