package com.likhith.bankingapi.dto.request;

import java.time.LocalDate;

import com.likhith.bankingapi.dto.request.shared.MoneyAmountDto;
import com.likhith.bankingapi.entity.enums.PaymentEnums.BillerCategory;
import com.likhith.bankingapi.entity.enums.PaymentEnums.PaymentFrequency;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to schedule one or more future bill payments")
public class ScheduledPaymentRequest {

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

    @NotNull(message = "scheduleDetails is required")
    @Valid
    private ScheduleDetails scheduleDetails;

    @NotNull(message = "autoPayEnabled is required")
    private Boolean autoPayEnabled;

    @Min(value = 0, message = "notifyBeforeDays must not be negative")
    @Max(value = 30, message = "notifyBeforeDays must not exceed 30")
    @Schema(example = "3")
    private Integer notifyBeforeDays;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Recurrence configuration for the scheduled payment")
    public static class ScheduleDetails {

        @NotNull(message = "startDate is required")
        @Future(message = "startDate must be in the future")
        @Schema(example = "2026-05-01")
        private LocalDate startDate;

        @NotNull(message = "frequency is required")
        private PaymentFrequency frequency;

        @Min(value = 1, message = "numberOfOccurrences must be at least 1")
        @Schema(example = "12")
        private Integer numberOfOccurrences;

        @Schema(example = "2027-05-01")
        private LocalDate endDate;
    }
}
