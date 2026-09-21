package com.likhith.bankingapi.dto.response;

import java.time.Instant;

import com.likhith.bankingapi.entity.enums.ConsentEnums.ConsentStatus;
import com.likhith.bankingapi.entity.enums.ConsentEnums.ConsentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Recorded consent decision")
public class ConsentResponse {

    @Schema(example = "CNS-1A2B3C4D5E6F")
    private String consentId;

    private String customerId;

    private ConsentType consentType;

    private ConsentStatus status;

    private Instant expiresAt;

    private Instant createdAt;
}
