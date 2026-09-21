package com.likhith.bankingapi.dto.request;

import java.util.List;

import com.likhith.bankingapi.dto.request.shared.MoneyAmountDto;
import com.likhith.bankingapi.entity.enums.DisputeEnums.DisputeReason;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Request payload to file a dispute against a transaction")
public class CreateDisputeRequest {

    @NotBlank(message = "transactionId is required")
    @Schema(example = "TRF-1234567890AB")
    private String transactionId;

    @NotBlank(message = "accountId is required")
    @Schema(example = "ACC-3D2E1F0A9B8C")
    private String accountId;

    @NotNull(message = "disputeReason is required")
    private DisputeReason disputeReason;

    @NotNull(message = "disputedAmount is required")
    @Valid
    private MoneyAmountDto disputedAmount;

    @NotBlank(message = "description is required")
    @Size(max = 1000)
    @Schema(example = "I was charged twice for the same purchase on the same day")
    private String description;

    @Size(max = 10, message = "at most 10 supporting documents are supported")
    private List<String> supportingDocuments;

    @Valid
    private MerchantDetails merchantDetails;

    @NotNull(message = "customerAcknowledgement is required")
    @AssertTrue(message = "customerAcknowledgement must be true to file a dispute")
    private Boolean customerAcknowledgement;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Details of the merchant involved in the disputed transaction")
    public static class MerchantDetails {

        @Schema(example = "Synthetic Coffee Co.")
        private String merchantName;

        @Schema(example = "FOOD_AND_DINING")
        private String merchantCategory;
    }
}
