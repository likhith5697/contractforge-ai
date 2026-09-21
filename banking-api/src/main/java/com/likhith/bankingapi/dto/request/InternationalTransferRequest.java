package com.likhith.bankingapi.dto.request;

import com.likhith.bankingapi.dto.request.shared.MoneyAmountDto;
import com.likhith.bankingapi.entity.enums.TransferEnums.ChargeOption;
import com.likhith.bankingapi.entity.enums.TransferEnums.InternationalPurpose;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
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
@Schema(description = "Request payload for an international wire (SWIFT) transfer")
public class InternationalTransferRequest {

    @NotBlank(message = "sourceAccountId is required")
    @Schema(example = "ACC-3D2E1F0A9B8C")
    private String sourceAccountId;

    @NotNull(message = "beneficiary is required")
    @Valid
    private Beneficiary beneficiary;

    @NotNull(message = "amount is required")
    @Valid
    private MoneyAmountDto amount;

    @NotNull(message = "purposeOfTransfer is required")
    private InternationalPurpose purposeOfTransfer;

    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "intermediaryBankSwift must be a valid SWIFT/BIC code")
    @Schema(example = "CHASUS33XXX")
    private String intermediaryBankSwift;

    @NotNull(message = "chargeOption is required")
    private ChargeOption chargeOption;

    @NotNull(message = "complianceMetadata is required")
    @Valid
    private ComplianceMetadata complianceMetadata;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Overseas beneficiary and bank details")
    public static class Beneficiary {

        @NotBlank(message = "beneficiaryName is required")
        @Schema(example = "Global Trading Ltd")
        private String beneficiaryName;

        @NotNull(message = "beneficiaryAddress is required")
        @Valid
        private BeneficiaryAddress beneficiaryAddress;

        @NotNull(message = "bankDetails is required")
        @Valid
        private BankDetails bankDetails;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Overseas beneficiary's address")
    public static class BeneficiaryAddress {

        @NotBlank(message = "addressLine1 is required")
        @Schema(example = "10 Downing Street")
        private String addressLine1;

        @NotBlank(message = "city is required")
        @Schema(example = "London")
        private String city;

        @NotBlank(message = "country is required")
        @Pattern(regexp = "^[A-Z]{2}$", message = "country must be a 2-letter ISO 3166-1 country code")
        @Schema(example = "GB")
        private String country;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Beneficiary's overseas bank details")
    public static class BankDetails {

        @NotBlank(message = "bankName is required")
        @Schema(example = "Global Trust Bank Plc")
        private String bankName;

        @NotBlank(message = "swiftBic is required")
        @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "swiftBic must be a valid SWIFT/BIC code")
        @Schema(example = "GTBPGB2LXXX")
        private String swiftBic;

        @NotBlank(message = "iban is required")
        @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{10,30}$", message = "iban must be a valid IBAN")
        @Schema(example = "GB29NWBK60161331926819")
        private String iban;

        @NotBlank(message = "bankCountry is required")
        @Pattern(regexp = "^[A-Z]{2}$", message = "bankCountry must be a 2-letter ISO 3166-1 country code")
        @Schema(example = "GB")
        private String bankCountry;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Compliance declarations required for cross-border transfers")
    public static class ComplianceMetadata {

        @NotNull(message = "sanctionsScreeningConsent is required")
        @AssertTrue(message = "sanctionsScreeningConsent must be accepted to proceed with an international transfer")
        private Boolean sanctionsScreeningConsent;

        @NotBlank(message = "sourceOfFundsDeclaration is required")
        @Schema(example = "Business trade proceeds")
        private String sourceOfFundsDeclaration;
    }
}
