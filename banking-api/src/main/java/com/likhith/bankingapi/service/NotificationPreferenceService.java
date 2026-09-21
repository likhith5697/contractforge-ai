package com.likhith.bankingapi.service;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likhith.bankingapi.common.IdGenerator;
import com.likhith.bankingapi.dto.request.NotificationPreferencesRequest;
import com.likhith.bankingapi.dto.response.NotificationPreferencesResponse;
import com.likhith.bankingapi.entity.ChannelPreference;
import com.likhith.bankingapi.entity.NotificationPreference;
import com.likhith.bankingapi.entity.enums.NotificationEnums.NotificationChannelType;
import com.likhith.bankingapi.exception.BusinessRuleViolationException;
import com.likhith.bankingapi.repository.CustomerRepository;
import com.likhith.bankingapi.repository.NotificationPreferenceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private static final Logger log = LoggerFactory.getLogger(NotificationPreferenceService.class);

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public NotificationPreferencesResponse savePreferences(NotificationPreferencesRequest request) {
        if (!customerRepository.existsByCustomerId(request.getCustomerId())) {
            throw new BusinessRuleViolationException("CUSTOMER_NOT_FOUND",
                    "Customer '" + request.getCustomerId() + "' does not exist");
        }
        if (!Boolean.TRUE.equals(request.getEventSubscriptions().getSecurityAlerts())) {
            throw new BusinessRuleViolationException("SECURITY_ALERTS_MANDATORY",
                    "securityAlerts cannot be disabled for compliance reasons");
        }

        List<ChannelPreference> channels = request.getChannels().stream()
                .map(c -> new ChannelPreference(c.getChannelType(), Boolean.TRUE.equals(c.getEnabled()), c.getContactValue()))
                .toList();

        NotificationPreference preference = NotificationPreference.builder()
                .preferenceId(IdGenerator.generate("NPF"))
                .customerId(request.getCustomerId())
                .channels(channels)
                .transactionAlerts(Boolean.TRUE.equals(request.getEventSubscriptions().getTransactionAlerts()))
                .promotionalAlerts(Boolean.TRUE.equals(request.getEventSubscriptions().getPromotionalAlerts()))
                .loanReminders(Boolean.TRUE.equals(request.getEventSubscriptions().getLoanReminders()))
                .securityAlerts(true)
                .quietHoursStart(request.getQuietHours() != null ? request.getQuietHours().getStartTime() : null)
                .quietHoursEnd(request.getQuietHours() != null ? request.getQuietHours().getEndTime() : null)
                .createdAt(Instant.now())
                .build();

        preference = notificationPreferenceRepository.save(preference);
        log.info("Saved notification preferences {} for customer {}", preference.getPreferenceId(),
                request.getCustomerId());

        List<NotificationChannelType> enabledChannels = preference.getChannels().stream()
                .filter(ChannelPreference::isEnabled)
                .map(ChannelPreference::getChannelType)
                .toList();

        return NotificationPreferencesResponse.builder()
                .preferenceId(preference.getPreferenceId())
                .customerId(preference.getCustomerId())
                .enabledChannels(enabledChannels)
                .securityAlerts(preference.isSecurityAlerts())
                .createdAt(preference.getCreatedAt())
                .build();
    }
}
