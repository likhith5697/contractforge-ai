package com.likhith.bankingapi.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.likhith.bankingapi.entity.enums.KycEnums.VerificationChannel;
import com.likhith.bankingapi.entity.enums.KycEnums.VerificationType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to record a KYC verification attempt for a customer")
public class KycVerificationRequest {

    @NotBlank(message = "customerId is required")
    @Schema(example = "CUS-8F3A1C2B9034")
    private String customerId;

    @NotNull(message = "verificationType is required")
    private VerificationType verificationType;

    @NotNull(message = "verificationChannel is required")
    private VerificationChannel verificationChannel;

    @NotNull(message = "documentDetails is required")
    @Valid
    private DocumentDetails documentDetails;

    @NotNull(message = "consentGiven is required")
    @AssertTrue(message = "consentGiven must be true to perform KYC verification")
    private Boolean consentGiven;

    @Size(max = 10, message = "at most 10 riskFlags are supported")
    private List<String> riskFlags;

    @Schema(example = "AGENT-4471")
    private String verifiedBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Identity document used for this verification")
    public static class DocumentDetails {

        @NotBlank(message = "documentNumber is required")
        @Schema(example = "SYN-DOC-8842190")
        private String documentNumber;

        @NotBlank(message = "issuingAuthority is required")
        @Schema(example = "Synthetic Identity Authority")
        private String issuingAuthority;

        @Future(message = "expiryDate must be in the future")
        @Schema(example = "2030-01-01")
        private LocalDate expiryDate;
    }
}
