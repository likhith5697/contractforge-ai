package com.likhith.bankingapi.dto.request;

import java.time.LocalDate;

import com.likhith.bankingapi.entity.enums.AddressEnums.AddressType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
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
@Schema(description = "Request payload to add an address to an existing customer")
public class AddAddressRequest {

    @NotNull(message = "addressType is required")
    private AddressType addressType;

    @NotBlank(message = "addressLine1 is required")
    @Schema(example = "742 Evergreen Terrace")
    private String addressLine1;

    @Schema(example = "Suite 4B")
    private String addressLine2;

    @NotBlank(message = "city is required")
    @Schema(example = "Springfield")
    private String city;

    @Schema(example = "IL")
    private String state;

    @NotBlank(message = "postalCode is required")
    @Pattern(regexp = "^[A-Za-z0-9\\- ]{3,12}$", message = "postalCode has an invalid format")
    @Schema(example = "62704")
    private String postalCode;

    @NotBlank(message = "country is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "country must be a 2-letter ISO 3166-1 country code")
    @Schema(example = "US")
    private String country;

    @NotNull(message = "isPrimary is required")
    private Boolean isPrimary;

    @NotNull(message = "effectiveFrom is required")
    @Schema(example = "2026-01-01")
    private LocalDate effectiveFrom;

    @Valid
    private GeoLocation geoLocation;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Optional geo-coordinates for the address")
    public static class GeoLocation {

        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        @Schema(example = "39.7817")
        private Double latitude;

        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        @Schema(example = "-89.6501")
        private Double longitude;
    }
}
