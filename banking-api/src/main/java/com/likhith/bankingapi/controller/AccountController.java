package com.likhith.bankingapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likhith.bankingapi.common.ApiResponse;
import com.likhith.bankingapi.dto.request.AddBeneficiaryRequest;
import com.likhith.bankingapi.dto.request.OpenAccountRequest;
import com.likhith.bankingapi.dto.response.AccountResponse;
import com.likhith.bankingapi.dto.response.BeneficiaryResponse;
import com.likhith.bankingapi.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account opening and beneficiary management")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Open a new bank account for an existing customer")
    public ResponseEntity<ApiResponse<AccountResponse>> openAccount(@Valid @RequestBody OpenAccountRequest request) {
        log.info("Received open-account request for customer {}", request.getCustomerId());
        AccountResponse response = accountService.openAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Account opened successfully", response));
    }

    @PostMapping("/{accountId}/beneficiaries")
    @Operation(summary = "Add a beneficiary to an existing account")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> addBeneficiary(
            @PathVariable String accountId,
            @Valid @RequestBody AddBeneficiaryRequest request) {
        log.info("Received add-beneficiary request for account {}", accountId);
        BeneficiaryResponse response = accountService.addBeneficiary(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Beneficiary added successfully", response));
    }
}
