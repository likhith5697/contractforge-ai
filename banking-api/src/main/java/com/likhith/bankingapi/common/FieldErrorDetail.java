package com.likhith.bankingapi.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "A single field-level validation error")
public class FieldErrorDetail {

    @Schema(example = "contactInfo.email")
    private final String field;

    @Schema(example = "must be a well-formed email address")
    private final String message;
}
