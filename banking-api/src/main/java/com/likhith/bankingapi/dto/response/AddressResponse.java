package com.likhith.bankingapi.dto.response;

import java.time.Instant;
import java.time.LocalDate;

import com.likhith.bankingapi.entity.enums.AddressEnums.AddressType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Address creation result")
public class AddressResponse {

    @Schema(example = "ADR-1A2B3C4D5E6F")
    private String addressId;

    @Schema(example = "CUS-8F3A1C2B9034")
    private String customerId;

    private AddressType addressType;

    private String addressLine1;

    private String city;

    private String country;

    private boolean primary;

    private LocalDate effectiveFrom;

    private Instant createdAt;
}
