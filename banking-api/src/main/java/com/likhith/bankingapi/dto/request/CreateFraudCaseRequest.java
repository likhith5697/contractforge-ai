package com.likhith.bankingapi.dto.request;

import java.util.List;

import com.likhith.bankingapi.dto.request.shared.MoneyAmountDto;
import com.likhith.bankingapi.entity.enums.CommonEnums.Channel;
import com.likhith.bankingapi.entity.enums.FraudEnums.EvidenceType;
import com.likhith.bankingapi.entity.enums.FraudEnums.FraudType;
import com.likhith.bankingapi.entity.enums.FraudEnums.Priority;
import com.likhith.bankingapi.entity.enums.FraudEnums.ReportedBy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
@Schema(description = "Request payload to open a new fraud investigation case")
public class CreateFraudCaseRequest {

    @NotNull(message = "reportedBy is required")
    private ReportedBy reportedBy;

    @Schema(example = "TRF-1234567890AB")
    private String relatedTransactionId;

    @NotBlank(message = "accountId is required")
    @Schema(example = "ACC-3D2E1F0A9B8C")
    private String accountId;

    @NotNull(message = "fraudType is required")
    private FraudType fraudType;

    @NotBlank(message = "description is required")
    @Size(max = 1000)
    @Schema(example = "Customer reports an unrecognized POS transaction while card was in their possession")
    private String description;

    @Valid
    private MoneyAmountDto estimatedLoss;

    @NotNull(message = "priority is required")
    private Priority priority;

    @Valid
    @Size(max = 10, message = "at most 10 evidence items are supported")
    private List<Evidence> evidenceList;

    @NotNull(message = "contactPreference is required")
    @Valid
    private ContactPreference contactPreference;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "A single piece of supporting evidence")
    public static class Evidence {

        @NotNull(message = "evidenceType is required")
        private EvidenceType evidenceType;

        @Size(max = 300)
        @Schema(example = "Screenshot of the disputed transaction in the mobile app")
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "How the customer prefers to be contacted about this case")
    public static class ContactPreference {

        @NotNull(message = "preferredChannel is required")
        private Channel preferredChannel;

        @NotBlank(message = "contactNumber is required")
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "contactNumber must be in E.164 format")
        @Schema(example = "+14155552671")
        private String contactNumber;
    }
}
