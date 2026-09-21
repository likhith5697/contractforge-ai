package com.likhith.bankingapi.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "preference_id", nullable = false, unique = true)
    private String preferenceId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "notification_preference_channels", joinColumns = @JoinColumn(name = "preference_id"))
    @Builder.Default
    private List<ChannelPreference> channels = new ArrayList<>();

    @Column(name = "transaction_alerts", nullable = false)
    private boolean transactionAlerts;

    @Column(name = "promotional_alerts", nullable = false)
    private boolean promotionalAlerts;

    @Column(name = "loan_reminders", nullable = false)
    private boolean loanReminders;

    @Column(name = "security_alerts", nullable = false)
    private boolean securityAlerts;

    @Column(name = "quiet_hours_start")
    private String quietHoursStart;

    @Column(name = "quiet_hours_end")
    private String quietHoursEnd;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
