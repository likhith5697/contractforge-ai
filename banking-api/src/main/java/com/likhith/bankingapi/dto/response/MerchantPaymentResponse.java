package com.likhith.bankingapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.likhith.bankingapi.entity.enums.MerchantPaymentEnums.MerchantPaymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Merchant payment result")
public class MerchantPaymentResponse {

    @Schema(example = "MPY-1A2B3C4D5E6F")
    private String merchantPaymentId;

    private String merchantName;

    private BigDecimal amount;

    private String currency;

    private MerchantPaymentStatus status;

    private Instant createdAt;
}
