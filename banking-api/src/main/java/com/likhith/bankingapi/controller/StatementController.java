package com.likhith.bankingapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likhith.bankingapi.common.ApiResponse;
import com.likhith.bankingapi.dto.request.CreateStatementRequest;
import com.likhith.bankingapi.dto.response.StatementRequestResponse;
import com.likhith.bankingapi.service.StatementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/statements")
@RequiredArgsConstructor
@Tag(name = "Statements", description = "Account statement requests")
public class StatementController {

    private static final Logger log = LoggerFactory.getLogger(StatementController.class);

    private final StatementService statementService;

    @PostMapping("/requests")
    @Operation(summary = "Request an account statement")
    public ResponseEntity<ApiResponse<StatementRequestResponse>> requestStatement(
            @Valid @RequestBody CreateStatementRequest request) {
        log.info("Received statement request for account {}", request.getAccountId());
        StatementRequestResponse response = statementService.requestStatement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Statement request received successfully", response));
    }

    @GetMapping("/requests/{statementRequestId}/download")
    @Operation(summary = "Download the generated PDF for a statement request")
    public ResponseEntity<byte[]> downloadStatement(@PathVariable String statementRequestId) {
        byte[] pdf = statementService.getStatementPdf(statementRequestId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + statementRequestId + ".pdf\"")
                .body(pdf);
    }
}
