package com.likhith.bankingapi.dto.request;

import com.likhith.bankingapi.dto.request.shared.ChannelMetadataDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
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
@Schema(description = "Request payload to activate an issued card")
public class ActivateCardRequest {

    @NotBlank(message = "activationCode is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "activationCode must be a 6-digit code")
    @Schema(example = "482913")
    private String activationCode;

    @NotBlank(message = "lastFourDigits is required")
    @Pattern(regexp = "^[0-9]{4}$", message = "lastFourDigits must be exactly 4 digits")
    @Schema(example = "4821")
    private String lastFourDigits;

    @NotNull(message = "expiryMonth is required")
    @Min(value = 1, message = "expiryMonth must be between 1 and 12")
    @Max(value = 12, message = "expiryMonth must be between 1 and 12")
    @Schema(example = "9")
    private Integer expiryMonth;

    @NotNull(message = "expiryYear is required")
    @Min(value = 2024, message = "expiryYear must be a valid future year")
    @Schema(example = "2030")
    private Integer expiryYear;

    @NotNull(message = "cvvVerified is required")
    @AssertTrue(message = "cvvVerified must be true to activate the card")
    private Boolean cvvVerified;

    @NotNull(message = "deviceMetadata is required")
    @Valid
    private ChannelMetadataDto deviceMetadata;
}
