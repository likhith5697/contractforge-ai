package com.likhith.bankingapi.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "idempotency_records",
        uniqueConstraints = @UniqueConstraint(columnNames = {"endpoint_name", "idempotency_key"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "endpoint_name", nullable = false)
    private String endpointName;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
