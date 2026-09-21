package com.likhith.bankingapi.entity;

import java.time.Instant;
import java.time.LocalDate;

import com.likhith.bankingapi.entity.enums.StatementEnums.StatementDeliveryMethod;
import com.likhith.bankingapi.entity.enums.StatementEnums.StatementFormat;
import com.likhith.bankingapi.entity.enums.StatementEnums.StatementPeriodType;
import com.likhith.bankingapi.entity.enums.StatementEnums.StatementRequestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "statement_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatementRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "statement_request_id", nullable = false, unique = true)
    private String statementRequestId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false)
    private StatementPeriodType periodType;

    @Column(name = "custom_start_date")
    private LocalDate customStartDate;

    @Column(name = "custom_end_date")
    private LocalDate customEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false)
    private StatementFormat format;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", nullable = false)
    private StatementDeliveryMethod deliveryMethod;

    @Column(name = "delivery_email")
    private String deliveryEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private StatementRequestStatus status = StatementRequestStatus.RECEIVED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
