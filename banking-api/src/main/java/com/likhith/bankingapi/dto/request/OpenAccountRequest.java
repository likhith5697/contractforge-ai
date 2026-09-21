package com.likhith.bankingapi.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.likhith.bankingapi.dto.request.shared.ChannelMetadataDto;
import com.likhith.bankingapi.entity.enums.AccountEnums.AccountPurpose;
import com.likhith.bankingapi.entity.enums.AccountEnums.AccountType;
import com.likhith.bankingapi.entity.enums.CommonEnums.RelationshipType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to open a new bank account for an existing customer")
public class OpenAccountRequest {

    @NotBlank(message = "customerId is required")
    @Schema(example = "CUS-8F3A1C2B9034")
    private String customerId;

    @NotNull(message = "accountType is required")
    private AccountType accountType;

    @NotBlank(message = "currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO 4217 code")
    @Schema(example = "USD")
    private String currency;

    @NotNull(message = "initialDeposit is required")
    @DecimalMin(value = "0.0", message = "initialDeposit must not be negative")
    @Schema(example = "1000.00")
    private BigDecimal initialDeposit;

    @NotBlank(message = "branchCode is required")
    @Pattern(regexp = "^BR-[0-9]{4}$", message = "branchCode must match pattern BR-####")
    @Schema(example = "BR-0451")
    private String branchCode;

    private AccountPurpose purpose;

    @NotNull(message = "nomineeDetails is required")
    @Valid
    private NomineeDetails nomineeDetails;

    @NotNull(message = "channelMetadata is required")
    @Valid
    private ChannelMetadataDto channelMetadata;

    @Valid
    @Size(max = 3, message = "at most 3 joint holders are supported")
    private List<JointHolder> jointHolders;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Nominee registered against the account")
    public static class NomineeDetails {

        @NotBlank(message = "nomineeName is required")
        @Schema(example = "Priya Sharma")
        private String nomineeName;

        @NotNull(message = "relationship is required")
        private RelationshipType relationship;

        @NotNull(message = "sharePercentage is required")
        @Min(value = 1, message = "sharePercentage must be at least 1")
        @Max(value = 100, message = "sharePercentage must not exceed 100")
        @Schema(example = "100")
        private Integer sharePercentage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "An additional joint account holder")
    public static class JointHolder {

        @NotBlank(message = "customerId is required for joint holder")
        @Schema(example = "CUS-11AA22BB33CC")
        private String customerId;

        @NotNull(message = "relationshipType is required for joint holder")
        private RelationshipType relationshipType;
    }
}
