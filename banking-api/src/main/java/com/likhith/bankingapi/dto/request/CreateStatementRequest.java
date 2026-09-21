package com.likhith.bankingapi.dto.request;

import java.time.LocalDate;

import com.likhith.bankingapi.entity.enums.StatementEnums.StatementDeliveryMethod;
import com.likhith.bankingapi.entity.enums.StatementEnums.StatementFormat;
import com.likhith.bankingapi.entity.enums.StatementEnums.StatementPeriodType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Request payload to request an account statement")
public class CreateStatementRequest {

    @NotBlank(message = "accountId is required")
    @Schema(example = "ACC-3D2E1F0A9B8C")
    private String accountId;

    @NotNull(message = "periodType is required")
    private StatementPeriodType periodType;

    @Valid
    private CustomRange customRange;

    @NotNull(message = "format is required")
    private StatementFormat format;

    @NotNull(message = "deliveryMethod is required")
    private StatementDeliveryMethod deliveryMethod;

    @Email(message = "deliveryEmail must be a well-formed email address")
    private String deliveryEmail;

    @NotNull(message = "passwordProtected is required")
    private Boolean passwordProtected;

    @Pattern(regexp = "^[a-z]{2}$", message = "language must be a 2-letter lowercase ISO 639-1 code")
    @Schema(example = "en")
    private String language;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Custom date range, required when periodType is CUSTOM_RANGE")
    public static class CustomRange {

        @Schema(example = "2026-01-01")
        private LocalDate startDate;

        @Schema(example = "2026-03-31")
        private LocalDate endDate;
    }
}
