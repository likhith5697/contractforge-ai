package com.likhith.bankingapi.dto.response;

import java.time.Instant;

import com.likhith.bankingapi.entity.enums.LoanEnums.LoanDocumentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Synthetic upload target issued for the declared document metadata")
public class LoanDocumentResponse {

    @Schema(example = "DOC-1A2B3C4D5E6F")
    private String documentId;

    @Schema(example = "LOAN-1A2B3C4D5E6F")
    private String loanId;

    private LoanDocumentType documentType;

    @Schema(example = "https://uploads.mock-bank.example/loans/LOAN-1A2B3C4D5E6F/DOC-1A2B3C4D5E6F")
    private String uploadUrl;

    @Schema(example = "5f2e1d3c4b5a6978")
    private String uploadToken;

    private Instant expiresAt;

    private Instant createdAt;
}
