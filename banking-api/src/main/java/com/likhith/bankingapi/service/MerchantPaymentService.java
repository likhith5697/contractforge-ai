package com.likhith.bankingapi.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likhith.bankingapi.common.IdGenerator;
import com.likhith.bankingapi.dto.request.MerchantPaymentRequest;
import com.likhith.bankingapi.dto.response.MerchantPaymentResponse;
import com.likhith.bankingapi.entity.Account;
import com.likhith.bankingapi.entity.MerchantPayment;
import com.likhith.bankingapi.entity.enums.MerchantPaymentEnums.MerchantPaymentStatus;
import com.likhith.bankingapi.exception.BusinessRuleViolationException;
import com.likhith.bankingapi.repository.AccountRepository;
import com.likhith.bankingapi.repository.MerchantPaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MerchantPaymentService {

    private static final Logger log = LoggerFactory.getLogger(MerchantPaymentService.class);

    private final MerchantPaymentRepository merchantPaymentRepository;
    private final AccountRepository accountRepository;
    private final IdempotencyService idempotencyService;

    @Transactional
    public MerchantPaymentResponse pay(MerchantPaymentRequest request, String idempotencyKey) {
        idempotencyService.reserve("merchant-payments", idempotencyKey);

        Account account = accountRepository.findByAccountId(request.getAccountId())
                .orElseThrow(() -> new BusinessRuleViolationException("ACCOUNT_NOT_FOUND",
                        "Account '" + request.getAccountId() + "' does not exist"));

        if (account.getBalance().compareTo(request.getAmount().getAmount()) < 0) {
            throw new BusinessRuleViolationException("INSUFFICIENT_FUNDS",
                    "Account '" + request.getAccountId() + "' has insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount().getAmount()));
        accountRepository.save(account);

        MerchantPayment payment = MerchantPayment.builder()
                .merchantPaymentId(IdGenerator.generate("MPY"))
                .accountId(request.getAccountId())
                .merchantId(request.getMerchantId())
                .merchantName(request.getMerchantName())
                .merchantCategory(request.getMerchantCategory())
                .amount(request.getAmount().getAmount())
                .currency(request.getAmount().getCurrency())
                .paymentInstrument(request.getPaymentInstrument())
                .status(MerchantPaymentStatus.CAPTURED)
                .createdAt(Instant.now())
                .build();

        payment = merchantPaymentRepository.save(payment);
        log.info("Processed merchant payment {} to {}", payment.getMerchantPaymentId(), request.getMerchantName());

        return MerchantPaymentResponse.builder()
                .merchantPaymentId(payment.getMerchantPaymentId())
                .merchantName(payment.getMerchantName())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
