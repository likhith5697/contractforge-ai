package com.likhith.bankingapi.dto.request;

import com.likhith.bankingapi.entity.enums.LoanEnums.LoanDocumentType;
import com.likhith.bankingapi.entity.enums.LoanEnums.UploadedByType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Metadata describing a document to be uploaded against a loan application (no binary content)")
public class UploadLoanDocumentRequest {

    @NotNull(message = "documentType is required")
    private LoanDocumentType documentType;

    @NotBlank(message = "fileName is required")
    @Pattern(regexp = "^[\\w,\\s\\-]+\\.(pdf|png|jpg|jpeg)$", message = "fileName must be a valid pdf/png/jpg/jpeg file name")
    @Schema(example = "income-proof-march-2026.pdf")
    private String fileName;

    @NotNull(message = "fileSizeBytes is required")
    @Min(value = 1, message = "fileSizeBytes must be greater than 0")
    @Max(value = 10485760, message = "fileSizeBytes must not exceed 10MB")
    @Schema(example = "204800")
    private Long fileSizeBytes;

    @NotBlank(message = "mimeType is required")
    @Pattern(regexp = "^(application/pdf|image/png|image/jpeg)$", message = "mimeType must be one of application/pdf, image/png, image/jpeg")
    @Schema(example = "application/pdf")
    private String mimeType;

    @NotBlank(message = "checksum is required")
    @Pattern(regexp = "^[a-f0-9]{64}$", message = "checksum must be a 64-character lowercase SHA-256 hex digest")
    @Schema(example = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
    private String checksum;

    @NotNull(message = "uploadedBy is required")
    private UploadedByType uploadedBy;
}
