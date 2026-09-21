package com.likhith.bankingapi.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likhith.bankingapi.common.IdGenerator;
import com.likhith.bankingapi.dto.request.BillPaymentRequest;
import com.likhith.bankingapi.dto.request.ScheduledPaymentRequest;
import com.likhith.bankingapi.dto.response.PaymentResponse;
import com.likhith.bankingapi.dto.response.ScheduledPaymentResponse;
import com.likhith.bankingapi.entity.Payment;
import com.likhith.bankingapi.entity.ScheduledPayment;
import com.likhith.bankingapi.entity.enums.PaymentEnums.PaymentStatus;
import com.likhith.bankingapi.exception.BusinessRuleViolationException;
import com.likhith.bankingapi.repository.AccountRepository;
import com.likhith.bankingapi.repository.PaymentRepository;
import com.likhith.bankingapi.repository.ScheduledPaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;
    private final ScheduledPaymentRepository scheduledPaymentRepository;
    private final IdempotencyService idempotencyService;

    @Transactional
    public PaymentResponse payBill(BillPaymentRequest request, String idempotencyKey) {
        idempotencyService.reserve("payments.bill", idempotencyKey);

        if (!accountRepository.existsByAccountId(request.getAccountId())) {
            throw new BusinessRuleViolationException("ACCOUNT_NOT_FOUND",
                    "Account '" + request.getAccountId() + "' does not exist");
        }

        Payment payment = Payment.builder()
                .paymentId(IdGenerator.generate("PAY"))
                .accountId(request.getAccountId())
                .billerId(request.getBillerId())
                .billerCategory(request.getBillerCategory())
                .consumerNumber(request.getConsumerNumber())
                .amount(request.getAmount().getAmount())
                .currency(request.getAmount().getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.COMPLETED)
                .createdAt(Instant.now())
                .build();

        payment = paymentRepository.save(payment);
        log.info("Processed bill payment {} for account {}", payment.getPaymentId(), request.getAccountId());

        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .accountId(payment.getAccountId())
                .billerId(payment.getBillerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    @Transactional
    public ScheduledPaymentResponse schedulePayment(ScheduledPaymentRequest request, String idempotencyKey) {
        idempotencyService.reserve("payments.scheduled", idempotencyKey);

        if (!accountRepository.existsByAccountId(request.getAccountId())) {
            throw new BusinessRuleViolationException("ACCOUNT_NOT_FOUND",
                    "Account '" + request.getAccountId() + "' does not exist");
        }

        ScheduledPayment scheduledPayment = ScheduledPayment.builder()
                .scheduledPaymentId(IdGenerator.generate("SPM"))
                .accountId(request.getAccountId())
                .billerId(request.getBillerId())
                .billerCategory(request.getBillerCategory())
                .consumerNumber(request.getConsumerNumber())
                .amount(request.getAmount().getAmount())
                .currency(request.getAmount().getCurrency())
                .startDate(request.getScheduleDetails().getStartDate())
                .frequency(request.getScheduleDetails().getFrequency())
                .autoPayEnabled(Boolean.TRUE.equals(request.getAutoPayEnabled()))
                .status(PaymentStatus.SCHEDULED)
                .createdAt(Instant.now())
                .build();

        scheduledPayment = scheduledPaymentRepository.save(scheduledPayment);
        log.info("Scheduled payment {} for account {} starting {}", scheduledPayment.getScheduledPaymentId(),
                request.getAccountId(), scheduledPayment.getStartDate());

        return ScheduledPaymentResponse.builder()
                .scheduledPaymentId(scheduledPayment.getScheduledPaymentId())
                .accountId(scheduledPayment.getAccountId())
                .amount(scheduledPayment.getAmount())
                .currency(scheduledPayment.getCurrency())
                .startDate(scheduledPayment.getStartDate())
                .frequency(scheduledPayment.getFrequency())
                .status(scheduledPayment.getStatus())
                .createdAt(scheduledPayment.getCreatedAt())
                .build();
    }
}
