package com.likhith.bankingapi.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likhith.bankingapi.common.IdGenerator;
import com.likhith.bankingapi.dto.request.LoanApplicationRequest;
import com.likhith.bankingapi.dto.request.UploadLoanDocumentRequest;
import com.likhith.bankingapi.dto.response.LoanApplicationResponse;
import com.likhith.bankingapi.dto.response.LoanDocumentResponse;
import com.likhith.bankingapi.entity.LoanApplication;
import com.likhith.bankingapi.entity.LoanDocument;
import com.likhith.bankingapi.entity.enums.LoanEnums.LoanStatus;
import com.likhith.bankingapi.exception.BusinessRuleViolationException;
import com.likhith.bankingapi.exception.ResourceNotFoundException;
import com.likhith.bankingapi.repository.CustomerRepository;
import com.likhith.bankingapi.repository.LoanApplicationRepository;
import com.likhith.bankingapi.repository.LoanDocumentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanDocumentRepository loanDocumentRepository;
    private final CustomerRepository customerRepository;
    private final IdempotencyService idempotencyService;

    @Transactional
    public LoanApplicationResponse submitApplication(LoanApplicationRequest request, String idempotencyKey) {
        idempotencyService.reserve("loans.applications", idempotencyKey);

        if (!customerRepository.existsByCustomerId(request.getCustomerId())) {
            throw new BusinessRuleViolationException("CUSTOMER_NOT_FOUND",
                    "Customer '" + request.getCustomerId() + "' does not exist");
        }

        LoanApplication loanApplication = LoanApplication.builder()
                .loanId(IdGenerator.generate("LOAN"))
                .customerId(request.getCustomerId())
                .loanType(request.getLoanType())
                .requestedAmount(request.getRequestedAmount().getAmount())
                .currency(request.getRequestedAmount().getCurrency())
                .tenureMonths(request.getTenureMonths())
                .purpose(request.getPurpose())
                .status(LoanStatus.SUBMITTED)
                .createdAt(Instant.now())
                .build();

        loanApplication = loanApplicationRepository.save(loanApplication);
        log.info("Loan application {} submitted for customer {}", loanApplication.getLoanId(),
                request.getCustomerId());

        return LoanApplicationResponse.builder()
                .loanId(loanApplication.getLoanId())
                .customerId(loanApplication.getCustomerId())
                .loanType(loanApplication.getLoanType())
                .requestedAmount(loanApplication.getRequestedAmount())
                .currency(loanApplication.getCurrency())
                .status(loanApplication.getStatus())
                .createdAt(loanApplication.getCreatedAt())
                .build();
    }

    @Transactional
    public LoanDocumentResponse uploadDocumentMetadata(String loanId, UploadLoanDocumentRequest request) {
        LoanApplication loanApplication = loanApplicationRepository.findByLoanId(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("LOAN_NOT_FOUND",
                        "Loan application '" + loanId + "' does not exist"));

        String uploadToken = IdGenerator.token();
        Instant now = Instant.now();

        LoanDocument document = LoanDocument.builder()
                .documentId(IdGenerator.generate("DOC"))
                .loanId(loanApplication.getLoanId())
                .documentType(request.getDocumentType())
                .fileName(request.getFileName())
                .fileSizeBytes(request.getFileSizeBytes())
                .mimeType(request.getMimeType())
                .checksum(request.getChecksum())
                .uploadedBy(request.getUploadedBy())
                .uploadToken(uploadToken)
                .createdAt(now)
                .build();

        document = loanDocumentRepository.save(document);
        log.info("Registered document metadata {} for loan {}", document.getDocumentId(), loanId);

        String uploadUrl = "https://uploads.mock-bank.example/loans/" + loanId + "/" + document.getDocumentId();

        return LoanDocumentResponse.builder()
                .documentId(document.getDocumentId())
                .loanId(loanId)
                .documentType(document.getDocumentType())
                .uploadUrl(uploadUrl)
                .uploadToken(uploadToken)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .createdAt(now)
                .build();
    }
}
