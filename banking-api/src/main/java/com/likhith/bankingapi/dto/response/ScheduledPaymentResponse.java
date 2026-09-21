package com.likhith.bankingapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.likhith.bankingapi.entity.enums.PaymentEnums.PaymentFrequency;
import com.likhith.bankingapi.entity.enums.PaymentEnums.PaymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Scheduled payment result")
public class ScheduledPaymentResponse {

    @Schema(example = "SPM-A1B2C3D4E5F6")
    private String scheduledPaymentId;

    private String accountId;

    private BigDecimal amount;

    private String currency;

    private LocalDate startDate;

    private PaymentFrequency frequency;

    private PaymentStatus status;

    private Instant createdAt;
}
