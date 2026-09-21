package com.likhith.bankingapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.PaymentEnums.PaymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Bill payment result")
public class PaymentResponse {

    @Schema(example = "PAY-A1B2C3D4E5F6")
    private String paymentId;

    private String accountId;

    private String billerId;

    private BigDecimal amount;

    private String currency;

    private PaymentStatus status;

    private Instant createdAt;
}
