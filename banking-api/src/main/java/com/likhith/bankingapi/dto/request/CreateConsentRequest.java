package com.likhith.bankingapi.dto.request;

import java.time.Instant;
import java.util.List;

import com.likhith.bankingapi.dto.request.shared.ChannelMetadataDto;
import com.likhith.bankingapi.entity.enums.ConsentEnums.ConsentType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to record a customer's consent decision")
public class CreateConsentRequest {

    @NotBlank(message = "customerId is required")
    @Schema(example = "CUS-8F3A1C2B9034")
    private String customerId;

    @NotNull(message = "consentType is required")
    private ConsentType consentType;

    @NotNull(message = "granted is required")
    private Boolean granted;

    @NotBlank(message = "consentVersion is required")
    @Schema(example = "v2.3")
    private String consentVersion;

    @NotBlank(message = "purposeDescription is required")
    @Size(max = 300)
    @Schema(example = "Share transaction history with a third-party budgeting app")
    private String purposeDescription;

    @NotEmpty(message = "dataScopes must contain at least one item")
    @Schema(example = "[\"TRANSACTION_HISTORY\", \"ACCOUNT_BALANCE\"]")
    private List<String> dataScopes;

    @Future(message = "expiresAt must be in the future")
    private Instant expiresAt;

    @NotNull(message = "channelMetadata is required")
    @Valid
    private ChannelMetadataDto channelMetadata;
}
