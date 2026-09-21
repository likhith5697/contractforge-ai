package com.likhith.bankingapi.dto.request;

import com.likhith.bankingapi.dto.request.shared.ChannelMetadataDto;
import com.likhith.bankingapi.dto.request.shared.MoneyAmountDto;
import com.likhith.bankingapi.entity.enums.TransferEnums.PurposeCode;
import com.likhith.bankingapi.entity.enums.TransferEnums.TransferType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for a domestic funds transfer between two accounts")
public class DomesticTransferRequest {

    @NotBlank(message = "sourceAccountId is required")
    @Schema(example = "ACC-3D2E1F0A9B8C")
    private String sourceAccountId;

    @NotBlank(message = "destinationAccountId is required")
    @Schema(example = "ACC-77AA88BB99CC")
    private String destinationAccountId;

    @NotNull(message = "amount is required")
    @Valid
    private MoneyAmountDto amount;

    @NotNull(message = "transferType is required")
    private TransferType transferType;

    @NotNull(message = "purposeCode is required")
    private PurposeCode purposeCode;

    @Size(max = 140, message = "remarks must not exceed 140 characters")
    @Schema(example = "Rent payment for April")
    private String remarks;

    @FutureOrPresent(message = "scheduledDate must be today or in the future")
    private LocalDate scheduledDate;

    @Valid
    private BeneficiaryDetails beneficiaryDetails;

    @NotNull(message = "channelMetadata is required")
    @Valid
    private ChannelMetadataDto channelMetadata;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Optional reference to a saved beneficiary")
    public static class BeneficiaryDetails {

        @Schema(example = "BEN-4C5D6E7F8A9B")
        private String beneficiaryId;

        @Schema(example = "Rohan Verma")
        private String beneficiaryName;
    }
}
