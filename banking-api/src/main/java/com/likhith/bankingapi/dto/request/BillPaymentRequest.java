package com.likhith.bankingapi.dto.request;

import java.time.LocalDate;

import com.likhith.bankingapi.dto.request.shared.ChannelMetadataDto;
import com.likhith.bankingapi.dto.request.shared.MoneyAmountDto;
import com.likhith.bankingapi.entity.enums.PaymentEnums.BillerCategory;
import com.likhith.bankingapi.entity.enums.PaymentEnums.PaymentMethod;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to pay a bill immediately")
public class BillPaymentRequest {

    @NotBlank(message = "accountId is required")
    @Schema(example = "ACC-3D2E1F0A9B8C")
    private String accountId;

    @NotBlank(message = "billerId is required")
    @Schema(example = "BLR-4471")
    private String billerId;

    @NotNull(message = "billerCategory is required")
    private BillerCategory billerCategory;

    @NotBlank(message = "consumerNumber is required")
    @Schema(example = "CONS-99001122")
    private String consumerNumber;

    @NotNull(message = "amount is required")
    @Valid
    private MoneyAmountDto amount;

    @NotNull(message = "billDetails is required")
    @Valid
    private BillDetails billDetails;

    @NotNull(message = "paymentMethod is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "channelMetadata is required")
    @Valid
    private ChannelMetadataDto channelMetadata;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Details of the bill being paid")
    public static class BillDetails {

        @NotBlank(message = "billNumber is required")
        @Schema(example = "BILL-2026-04-001")
        private String billNumber;

        @Past(message = "billDate must be in the past")
        @Schema(example = "2026-04-01")
        private LocalDate billDate;

        @FutureOrPresent(message = "dueDate must be today or in the future")
        @Schema(example = "2026-04-20")
        private LocalDate dueDate;
    }
}
