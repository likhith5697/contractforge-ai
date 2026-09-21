package com.likhith.bankingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likhith.bankingapi.entity.NotificationPreference;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
}
