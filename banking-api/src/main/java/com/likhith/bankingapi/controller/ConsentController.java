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
import com.likhith.bankingapi.dto.request.CreateConsentRequest;
import com.likhith.bankingapi.dto.response.ConsentResponse;
import com.likhith.bankingapi.service.ConsentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/consents")
@RequiredArgsConstructor
@Tag(name = "Consents", description = "Customer consent management")
public class ConsentController {

    private static final Logger log = LoggerFactory.getLogger(ConsentController.class);

    private final ConsentService consentService;

    @PostMapping
    @Operation(summary = "Record a customer's consent decision")
    public ResponseEntity<ApiResponse<ConsentResponse>> recordConsent(
            @Valid @RequestBody CreateConsentRequest request) {
        log.info("Received consent request for customer {}", request.getCustomerId());
        ConsentResponse response = consentService.recordConsent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Consent recorded successfully", response));
    }
}
