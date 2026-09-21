package com.likhith.bankingapi.dto.response;

import java.time.Instant;

import com.likhith.bankingapi.entity.enums.KycEnums.KycVerificationStatus;
import com.likhith.bankingapi.entity.enums.KycEnums.VerificationType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "KYC verification result")
public class KycVerificationResponse {

    @Schema(example = "KYC-1A2B3C4D5E6F")
    private String verificationId;

    private String customerId;

    private VerificationType verificationType;

    private KycVerificationStatus status;

    private Instant createdAt;
}
