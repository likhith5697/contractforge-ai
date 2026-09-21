package com.likhith.bankingapi.dto.request;

import java.util.List;

import com.likhith.bankingapi.entity.enums.NotificationEnums.NotificationChannelType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
@Schema(description = "Request payload to set a customer's notification preferences")
public class NotificationPreferencesRequest {

    @NotBlank(message = "customerId is required")
    @Schema(example = "CUS-8F3A1C2B9034")
    private String customerId;

    @NotEmpty(message = "at least one channel preference is required")
    @Valid
    private List<ChannelPreference> channels;

    @NotNull(message = "eventSubscriptions is required")
    @Valid
    private EventSubscriptions eventSubscriptions;

    @Valid
    private QuietHours quietHours;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Preference for a single notification channel")
    public static class ChannelPreference {

        @NotNull(message = "channelType is required")
        private NotificationChannelType channelType;

        @NotNull(message = "enabled is required")
        private Boolean enabled;

        @NotBlank(message = "contactValue is required")
        @Schema(example = "aarav.sharma@example-mail.com")
        private String contactValue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Which categories of events trigger notifications")
    public static class EventSubscriptions {

        @NotNull(message = "transactionAlerts is required")
        private Boolean transactionAlerts;

        @NotNull(message = "promotionalAlerts is required")
        private Boolean promotionalAlerts;

        @NotNull(message = "loanReminders is required")
        private Boolean loanReminders;

        @NotNull(message = "securityAlerts is required")
        private Boolean securityAlerts;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Time window during which non-critical notifications are suppressed")
    public static class QuietHours {

        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "startTime must be in HH:mm 24-hour format")
        @Schema(example = "22:00")
        private String startTime;

        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "endTime must be in HH:mm 24-hour format")
        @Schema(example = "07:00")
        private String endTime;
    }
}
