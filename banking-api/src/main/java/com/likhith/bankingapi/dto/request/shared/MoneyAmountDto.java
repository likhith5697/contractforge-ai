package com.likhith.bankingapi.dto.request.shared;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A monetary amount with an ISO-4217 currency code")
public class MoneyAmountDto {

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than zero")
    @Schema(example = "2500.00")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO 4217 code, e.g. USD")
    @Schema(example = "USD")
    private String currency;
}
