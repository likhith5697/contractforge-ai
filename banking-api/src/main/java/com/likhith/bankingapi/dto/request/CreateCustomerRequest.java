package com.likhith.bankingapi.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.likhith.bankingapi.entity.enums.CustomerEnums.DocumentType;
import com.likhith.bankingapi.entity.enums.CustomerEnums.EmploymentType;
import com.likhith.bankingapi.entity.enums.CustomerEnums.Gender;
import com.likhith.bankingapi.entity.enums.CustomerEnums.SourceOfFunds;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
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
@Schema(description = "Request payload to onboard a new synthetic customer")
public class CreateCustomerRequest {

    @NotNull(message = "personalInfo is required")
    @Valid
    private PersonalInfo personalInfo;

    @NotNull(message = "contactInfo is required")
    @Valid
    private ContactInfo contactInfo;

    @NotNull(message = "identityDocument is required")
    @Valid
    private IdentityDocument identityDocument;

    @NotNull(message = "employmentInfo is required")
    @Valid
    private EmploymentInfo employmentInfo;

    @NotNull(message = "residentialAddress is required")
    @Valid
    private ResidentialAddress residentialAddress;

    @NotNull(message = "riskProfile is required")
    @Valid
    private RiskProfile riskProfile;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Basic personal identity details")
    public static class PersonalInfo {

        @NotBlank(message = "firstName is required")
        @Size(max = 60)
        @Schema(example = "Aarav")
        private String firstName;

        @NotBlank(message = "lastName is required")
        @Size(max = 60)
        @Schema(example = "Sharma")
        private String lastName;

        @NotNull(message = "dateOfBirth is required")
        @Past(message = "dateOfBirth must be in the past")
        @Schema(example = "1990-05-14")
        private LocalDate dateOfBirth;

        @NotNull(message = "gender is required")
        private Gender gender;

        @NotBlank(message = "nationality is required")
        @Pattern(regexp = "^[A-Z]{2}$", message = "nationality must be a 2-letter ISO 3166-1 country code")
        @Schema(example = "IN")
        private String nationality;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Contact details")
    public static class ContactInfo {

        @NotBlank(message = "email is required")
        @Email(message = "email must be a well-formed email address")
        @Schema(example = "aarav.sharma@example-mail.com")
        private String email;

        @NotBlank(message = "phoneNumber is required")
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "phoneNumber must be in E.164 format, e.g. +14155552671")
        @Schema(example = "+14155552671")
        private String phoneNumber;

        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "alternatePhoneNumber must be in E.164 format")
        private String alternatePhoneNumber;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Government issued identity document used for onboarding")
    public static class IdentityDocument {

        @NotNull(message = "documentType is required")
        private DocumentType documentType;

        @NotBlank(message = "documentNumber is required")
        @Size(max = 40)
        @Schema(example = "SYN-DOC-8842190")
        private String documentNumber;

        @NotBlank(message = "issuingCountry is required")
        @Pattern(regexp = "^[A-Z]{2}$", message = "issuingCountry must be a 2-letter ISO 3166-1 country code")
        @Schema(example = "IN")
        private String issuingCountry;

        @NotNull(message = "expiryDate is required")
        @Future(message = "expiryDate must be in the future")
        @Schema(example = "2030-01-01")
        private LocalDate expiryDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Employment and income details")
    public static class EmploymentInfo {

        @NotBlank(message = "occupation is required")
        @Schema(example = "Software Engineer")
        private String occupation;

        @Schema(example = "Synthetic Tech Corp")
        private String employerName;

        @NotNull(message = "employmentType is required")
        private EmploymentType employmentType;

        @NotNull(message = "annualIncome is required")
        @DecimalMin(value = "0.0", message = "annualIncome must not be negative")
        @Schema(example = "95000.00")
        private BigDecimal annualIncome;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Residential address at time of onboarding")
    public static class ResidentialAddress {

        @NotBlank(message = "addressLine1 is required")
        @Schema(example = "221B Synthetic Lane")
        private String addressLine1;

        @NotBlank(message = "city is required")
        @Schema(example = "Springfield")
        private String city;

        @Schema(example = "IL")
        private String state;

        @NotBlank(message = "postalCode is required")
        @Schema(example = "62704")
        private String postalCode;

        @NotBlank(message = "country is required")
        @Pattern(regexp = "^[A-Z]{2}$", message = "country must be a 2-letter ISO 3166-1 country code")
        @Schema(example = "US")
        private String country;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Compliance risk profile inputs")
    public static class RiskProfile {

        @NotNull(message = "sourceOfFunds is required")
        private SourceOfFunds sourceOfFunds;

        @NotNull(message = "expectedMonthlyTurnover is required")
        @DecimalMin(value = "0.0", message = "expectedMonthlyTurnover must not be negative")
        @Schema(example = "5000.00")
        private BigDecimal expectedMonthlyTurnover;
    }
}
