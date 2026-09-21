package com.likhith.bankingapi.dto.request;

import java.math.BigDecimal;

import com.likhith.bankingapi.entity.enums.CommonEnums.RelationshipType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Request payload to register a new beneficiary against an account")
public class AddBeneficiaryRequest {

    @NotBlank(message = "beneficiaryName is required")
    @Schema(example = "Rohan Verma")
    private String beneficiaryName;

    @Size(max = 40)
    @Schema(example = "Roomie")
    private String nickname;

    @NotNull(message = "relationship is required")
    private RelationshipType relationship;

    @NotNull(message = "bankDetails is required")
    @Valid
    private BankDetails bankDetails;

    @Valid
    private ContactInfo contactInfo;

    @NotNull(message = "dailyTransferLimit is required")
    @DecimalMin(value = "0.0", message = "dailyTransferLimit must not be negative")
    @Schema(example = "5000.00")
    private BigDecimal dailyTransferLimit;

    @NotNull(message = "isTrusted is required")
    private Boolean isTrusted;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Beneficiary's bank account details")
    public static class BankDetails {

        @NotBlank(message = "bankName is required")
        @Schema(example = "Synthetic National Bank")
        private String bankName;

        @NotBlank(message = "accountNumber is required")
        @Pattern(regexp = "^[0-9]{8,18}$", message = "accountNumber must be numeric, 8 to 18 digits")
        @Schema(example = "123456789012")
        private String accountNumber;

        @NotBlank(message = "routingCode is required")
        @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "routingCode must match IFSC-style pattern e.g. SYNB0001234")
        @Schema(example = "SYNB0001234")
        private String routingCode;

        @Schema(example = "Downtown Branch")
        private String branchName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Optional contact details for the beneficiary")
    public static class ContactInfo {

        @Email(message = "email must be a well-formed email address")
        private String email;

        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "phone must be in E.164 format")
        private String phone;
    }
}
