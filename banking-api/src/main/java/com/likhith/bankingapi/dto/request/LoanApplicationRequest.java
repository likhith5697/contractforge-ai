package com.likhith.bankingapi.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.likhith.bankingapi.dto.request.shared.MoneyAmountDto;
import com.likhith.bankingapi.entity.enums.CommonEnums.RelationshipType;
import com.likhith.bankingapi.entity.enums.CustomerEnums.EmploymentType;
import com.likhith.bankingapi.entity.enums.LoanEnums.CollateralType;
import com.likhith.bankingapi.entity.enums.LoanEnums.LoanType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Request payload to submit a new loan application")
public class LoanApplicationRequest {

    @NotBlank(message = "customerId is required")
    @Schema(example = "CUS-8F3A1C2B9034")
    private String customerId;

    @NotNull(message = "loanType is required")
    private LoanType loanType;

    @NotNull(message = "requestedAmount is required")
    @Valid
    private MoneyAmountDto requestedAmount;

    @NotNull(message = "tenureMonths is required")
    @Min(value = 3, message = "tenureMonths must be at least 3")
    @Max(value = 360, message = "tenureMonths must not exceed 360")
    @Schema(example = "36")
    private Integer tenureMonths;

    @NotBlank(message = "purpose is required")
    @Size(max = 200)
    @Schema(example = "Home renovation")
    private String purpose;

    @NotNull(message = "employmentDetails is required")
    @Valid
    private EmploymentDetails employmentDetails;

    @Valid
    @Size(max = 3, message = "at most 3 co-applicants are supported")
    private List<CoApplicant> coApplicants;

    @NotNull(message = "collateral is required")
    @Valid
    private Collateral collateral;

    @DecimalMin(value = "0.0", message = "existingLoanEmi must not be negative")
    @Schema(example = "250.00")
    private BigDecimal existingLoanEmi;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Applicant's current employment and income")
    public static class EmploymentDetails {

        @NotNull(message = "employmentType is required")
        private EmploymentType employmentType;

        @NotNull(message = "monthlyIncome is required")
        @DecimalMin(value = "0.0", message = "monthlyIncome must not be negative")
        @Schema(example = "7500.00")
        private BigDecimal monthlyIncome;

        @Schema(example = "Synthetic Tech Corp")
        private String employerName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "A co-applicant contributing to the loan")
    public static class CoApplicant {

        @NotBlank(message = "customerId is required for co-applicant")
        @Schema(example = "CUS-11AA22BB33CC")
        private String customerId;

        @NotNull(message = "relationship is required for co-applicant")
        private RelationshipType relationship;

        @DecimalMin(value = "0.0", message = "incomeContribution must not be negative")
        @Schema(example = "2000.00")
        private BigDecimal incomeContribution;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Collateral offered against the loan, if any")
    public static class Collateral {

        @NotNull(message = "collateralType is required")
        private CollateralType collateralType;

        @DecimalMin(value = "0.0", message = "estimatedValue must not be negative")
        @Schema(example = "45000.00")
        private BigDecimal estimatedValue;
    }
}
