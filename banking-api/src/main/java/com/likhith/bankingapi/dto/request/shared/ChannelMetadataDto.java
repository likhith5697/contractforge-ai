package com.likhith.bankingapi.dto.request.shared;

import com.likhith.bankingapi.entity.enums.CommonEnums.Channel;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Device/channel context captured for fraud and audit purposes")
public class ChannelMetadataDto {

    @NotNull(message = "channel is required")
    @Schema(example = "MOBILE")
    private Channel channel;

    @NotBlank(message = "deviceId is required")
    @Schema(example = "DEV-9F1A2B3C")
    private String deviceId;

    @Pattern(regexp = "^(\\d{1,3}\\.){3}\\d{1,3}$", message = "ipAddress must be a valid IPv4 address")
    @Schema(example = "203.0.113.42")
    private String ipAddress;
}
