package com.likhith.bankingapi.service;

import java.time.Instant;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likhith.bankingapi.common.IdGenerator;
import com.likhith.bankingapi.dto.request.DomesticTransferRequest;
import com.likhith.bankingapi.dto.request.InternationalTransferRequest;
import com.likhith.bankingapi.dto.response.InternationalTransferResponse;
import com.likhith.bankingapi.dto.response.TransferResponse;
import com.likhith.bankingapi.entity.Account;
import com.likhith.bankingapi.entity.InternationalTransfer;
import com.likhith.bankingapi.entity.TransferTransaction;
import com.likhith.bankingapi.entity.enums.TransferEnums.TransferStatus;
import com.likhith.bankingapi.exception.BusinessRuleViolationException;
import com.likhith.bankingapi.repository.AccountRepository;
import com.likhith.bankingapi.repository.InternationalTransferRepository;
import com.likhith.bankingapi.repository.TransferTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountRepository accountRepository;
    private final TransferTransactionRepository transferTransactionRepository;
    private final InternationalTransferRepository internationalTransferRepository;
    private final IdempotencyService idempotencyService;

    @Transactional
    public TransferResponse createDomesticTransfer(DomesticTransferRequest request, String idempotencyKey) {
        idempotencyService.reserve("transfers.domestic", idempotencyKey);

        if (Objects.equals(request.getSourceAccountId(), request.getDestinationAccountId())) {
            throw new BusinessRuleViolationException("SAME_ACCOUNT_TRANSFER",
                    "sourceAccountId and destinationAccountId must be different");
        }

        Account source = accountRepository.findByAccountId(request.getSourceAccountId())
                .orElseThrow(() -> new BusinessRuleViolationException("SOURCE_ACCOUNT_NOT_FOUND",
                        "Source account '" + request.getSourceAccountId() + "' does not exist"));

        if (!accountRepository.existsByAccountId(request.getDestinationAccountId())) {
            throw new BusinessRuleViolationException("DESTINATION_ACCOUNT_NOT_FOUND",
                    "Destination account '" + request.getDestinationAccountId() + "' does not exist");
        }

        if (source.getBalance().compareTo(request.getAmount().getAmount()) < 0) {
            throw new BusinessRuleViolationException("INSUFFICIENT_FUNDS",
                    "Source account '" + request.getSourceAccountId() + "' has insufficient balance");
        }

        source.setBalance(source.getBalance().subtract(request.getAmount().getAmount()));
        accountRepository.save(source);

        TransferTransaction transaction = TransferTransaction.builder()
                .transferId(IdGenerator.generate("TRF"))
                .sourceAccountId(request.getSourceAccountId())
                .destinationAccountId(request.getDestinationAccountId())
                .amount(request.getAmount().getAmount())
                .currency(request.getAmount().getCurrency())
                .transferType(request.getTransferType())
                .purposeCode(request.getPurposeCode())
                .remarks(request.getRemarks())
                .scheduledDate(request.getScheduledDate())
                .status(TransferStatus.COMPLETED)
                .createdAt(Instant.now())
                .build();

        transaction = transferTransactionRepository.save(transaction);
        log.info("Processed domestic transfer {} from {} to {}", transaction.getTransferId(),
                request.getSourceAccountId(), request.getDestinationAccountId());

        return TransferResponse.builder()
                .transferId(transaction.getTransferId())
                .sourceAccountId(transaction.getSourceAccountId())
                .destinationAccountId(transaction.getDestinationAccountId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .transferType(transaction.getTransferType())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    @Transactional
    public InternationalTransferResponse createInternationalTransfer(InternationalTransferRequest request,
            String idempotencyKey) {
        idempotencyService.reserve("transfers.international", idempotencyKey);

        Account source = accountRepository.findByAccountId(request.getSourceAccountId())
                .orElseThrow(() -> new BusinessRuleViolationException("SOURCE_ACCOUNT_NOT_FOUND",
                        "Source account '" + request.getSourceAccountId() + "' does not exist"));

        if (source.getBalance().compareTo(request.getAmount().getAmount()) < 0) {
            throw new BusinessRuleViolationException("INSUFFICIENT_FUNDS",
                    "Source account '" + request.getSourceAccountId() + "' has insufficient balance");
        }

        source.setBalance(source.getBalance().subtract(request.getAmount().getAmount()));
        accountRepository.save(source);

        InternationalTransfer transfer = InternationalTransfer.builder()
                .transferId(IdGenerator.generate("ITX"))
                .sourceAccountId(request.getSourceAccountId())
                .beneficiaryName(request.getBeneficiary().getBeneficiaryName())
                .beneficiaryCountry(request.getBeneficiary().getBeneficiaryAddress().getCountry())
                .swiftBic(request.getBeneficiary().getBankDetails().getSwiftBic())
                .iban(request.getBeneficiary().getBankDetails().getIban())
                .amount(request.getAmount().getAmount())
                .currency(request.getAmount().getCurrency())
                .purposeOfTransfer(request.getPurposeOfTransfer())
                .chargeOption(request.getChargeOption())
                .status(TransferStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        transfer = internationalTransferRepository.save(transfer);
        log.info("Processed international transfer {} from {}", transfer.getTransferId(),
                request.getSourceAccountId());

        return InternationalTransferResponse.builder()
                .transferId(transfer.getTransferId())
                .sourceAccountId(transfer.getSourceAccountId())
                .beneficiaryName(transfer.getBeneficiaryName())
                .swiftBic(transfer.getSwiftBic())
                .amount(transfer.getAmount())
                .currency(transfer.getCurrency())
                .status(transfer.getStatus())
                .createdAt(transfer.getCreatedAt())
                .build();
    }
}
