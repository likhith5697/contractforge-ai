package com.likhith.bankingapi.dto.response;

import java.time.Instant;
import java.util.List;

import com.likhith.bankingapi.entity.enums.NotificationEnums.NotificationChannelType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Saved notification preferences")
public class NotificationPreferencesResponse {

    @Schema(example = "NPF-1A2B3C4D5E6F")
    private String preferenceId;

    private String customerId;

    private List<NotificationChannelType> enabledChannels;

    private boolean securityAlerts;

    private Instant createdAt;
}
