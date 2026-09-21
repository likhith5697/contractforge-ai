package com.likhith.bankingapi.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likhith.bankingapi.common.IdGenerator;
import com.likhith.bankingapi.dto.request.AddBeneficiaryRequest;
import com.likhith.bankingapi.dto.request.OpenAccountRequest;
import com.likhith.bankingapi.dto.response.AccountResponse;
import com.likhith.bankingapi.dto.response.BeneficiaryResponse;
import com.likhith.bankingapi.entity.Account;
import com.likhith.bankingapi.entity.Beneficiary;
import com.likhith.bankingapi.entity.Customer;
import com.likhith.bankingapi.entity.enums.AccountEnums.AccountStatus;
import com.likhith.bankingapi.exception.BusinessRuleViolationException;
import com.likhith.bankingapi.exception.ResourceNotFoundException;
import com.likhith.bankingapi.repository.AccountRepository;
import com.likhith.bankingapi.repository.BeneficiaryRepository;
import com.likhith.bankingapi.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final BeneficiaryRepository beneficiaryRepository;

    @Transactional
    public AccountResponse openAccount(OpenAccountRequest request) {
        Customer customer = customerRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> new BusinessRuleViolationException("CUSTOMER_NOT_FOUND",
                        "Account holder '" + request.getCustomerId() + "' does not exist"));

        Account account = Account.builder()
                .accountId(IdGenerator.generate("ACC"))
                .customer(customer)
                .accountType(request.getAccountType())
                .currency(request.getCurrency())
                .balance(request.getInitialDeposit())
                .branchCode(request.getBranchCode())
                .purpose(request.getPurpose())
                .status(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        account = accountRepository.save(account);
        log.info("Opened account {} for customer {}", account.getAccountId(), customer.getCustomerId());

        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .customerId(customer.getCustomerId())
                .accountType(account.getAccountType())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }

    @Transactional
    public BeneficiaryResponse addBeneficiary(String accountId, AddBeneficiaryRequest request) {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("ACCOUNT_NOT_FOUND",
                        "Account '" + accountId + "' does not exist"));

        Beneficiary beneficiary = Beneficiary.builder()
                .beneficiaryId(IdGenerator.generate("BEN"))
                .account(account)
                .beneficiaryName(request.getBeneficiaryName())
                .nickname(request.getNickname())
                .relationship(request.getRelationship())
                .bankName(request.getBankDetails().getBankName())
                .bankAccountNumber(request.getBankDetails().getAccountNumber())
                .routingCode(request.getBankDetails().getRoutingCode())
                .branchName(request.getBankDetails().getBranchName())
                .contactEmail(request.getContactInfo() != null ? request.getContactInfo().getEmail() : null)
                .contactPhone(request.getContactInfo() != null ? request.getContactInfo().getPhone() : null)
                .dailyTransferLimit(request.getDailyTransferLimit())
                .trusted(Boolean.TRUE.equals(request.getIsTrusted()))
                .createdAt(Instant.now())
                .build();

        beneficiary = beneficiaryRepository.save(beneficiary);
        log.info("Added beneficiary {} to account {}", beneficiary.getBeneficiaryId(), accountId);

        return BeneficiaryResponse.builder()
                .beneficiaryId(beneficiary.getBeneficiaryId())
                .accountId(accountId)
                .beneficiaryName(beneficiary.getBeneficiaryName())
                .bankName(beneficiary.getBankName())
                .dailyTransferLimit(beneficiary.getDailyTransferLimit())
                .trusted(beneficiary.isTrusted())
                .createdAt(beneficiary.getCreatedAt())
                .build();
    }
}
