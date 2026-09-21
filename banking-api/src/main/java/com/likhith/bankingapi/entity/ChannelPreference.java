package com.likhith.bankingapi.entity;

import com.likhith.bankingapi.entity.enums.NotificationEnums.NotificationChannelType;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelPreference {

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false)
    private NotificationChannelType channelType;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "contact_value", nullable = false)
    private String contactValue;
}
