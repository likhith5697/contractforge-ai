package com.likhith.bankingapi.dto.response;

import java.time.Instant;

import com.likhith.bankingapi.entity.enums.CustomerEnums.KycStatus;
import com.likhith.bankingapi.entity.enums.CustomerEnums.OnboardingStatus;
import com.likhith.bankingapi.entity.enums.CustomerEnums.RiskRating;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Customer onboarding result")
public class CustomerResponse {

    @Schema(example = "CUS-8F3A1C2B9034")
    private String customerId;

    @Schema(example = "Aarav Sharma")
    private String fullName;

    @Schema(example = "aarav.sharma@example-mail.com")
    private String email;

    private KycStatus kycStatus;

    private RiskRating riskRating;

    private OnboardingStatus onboardingStatus;

    private Instant createdAt;
}
