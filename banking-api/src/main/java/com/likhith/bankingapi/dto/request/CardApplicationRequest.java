package com.likhith.bankingapi.dto.request;

import java.math.BigDecimal;

import com.likhith.bankingapi.entity.enums.CardEnums.CardNetwork;
import com.likhith.bankingapi.entity.enums.CardEnums.CardType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Request payload to apply for a new card")
public class CardApplicationRequest {

    @NotBlank(message = "customerId is required")
    @Schema(example = "CUS-8F3A1C2B9034")
    private String customerId;

    @NotBlank(message = "accountId is required")
    @Schema(example = "ACC-3D2E1F0A9B8C")
    private String accountId;

    @NotNull(message = "cardType is required")
    private CardType cardType;

    @NotNull(message = "cardNetwork is required")
    private CardNetwork cardNetwork;

    @NotBlank(message = "cardholderName is required")
    @Pattern(regexp = "^[A-Za-z ]{2,60}$", message = "cardholderName must contain only letters and spaces")
    @Schema(example = "AARAV SHARMA")
    private String cardholderName;

    @NotNull(message = "isVirtualCard is required")
    private Boolean isVirtualCard;

    @DecimalMin(value = "0.0", message = "requestedCreditLimit must not be negative")
    @Schema(example = "10000.00")
    private BigDecimal requestedCreditLimit;

    @Min(value = 1, message = "billingCycle must be between 1 and 28")
    @Max(value = 28, message = "billingCycle must be between 1 and 28")
    @Schema(example = "5")
    private Integer billingCycle;

    @NotNull(message = "deliveryAddress is required")
    @Valid
    private DeliveryAddress deliveryAddress;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Address to which the physical card should be delivered")
    public static class DeliveryAddress {

        @NotBlank(message = "addressLine1 is required")
        @Schema(example = "742 Evergreen Terrace")
        private String addressLine1;

        @NotBlank(message = "city is required")
        @Schema(example = "Springfield")
        private String city;

        @Schema(example = "IL")
        private String state;

        @NotBlank(message = "postalCode is required")
        @Schema(example = "62704")
        private String postalCode;

        @NotBlank(message = "country is required")
        @Pattern(regexp = "^[A-Z]{2}$", message = "country must be a 2-letter ISO 3166-1 country code")
        @Schema(example = "US")
        private String country;
    }
}
