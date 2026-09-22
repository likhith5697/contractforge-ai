package com.likhith.bankingapi.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likhith.bankingapi.common.IdGenerator;
import com.likhith.bankingapi.dto.request.CreateStatementRequest;
import com.likhith.bankingapi.dto.response.StatementRequestResponse;
import com.likhith.bankingapi.entity.Account;
import com.likhith.bankingapi.entity.StatementRequest;
import com.likhith.bankingapi.entity.enums.StatementEnums.StatementDeliveryMethod;
import com.likhith.bankingapi.entity.enums.StatementEnums.StatementFormat;
import com.likhith.bankingapi.entity.enums.StatementEnums.StatementPeriodType;
import com.likhith.bankingapi.entity.enums.StatementEnums.StatementRequestStatus;
import com.likhith.bankingapi.exception.BusinessRuleViolationException;
import com.likhith.bankingapi.exception.ResourceNotFoundException;
import com.likhith.bankingapi.repository.AccountRepository;
import com.likhith.bankingapi.repository.StatementRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatementService {

    private static final Logger log = LoggerFactory.getLogger(StatementService.class);

    private final StatementRequestRepository statementRequestRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public StatementRequestResponse requestStatement(CreateStatementRequest request) {
        Account account = accountRepository.findByAccountId(request.getAccountId())
                .orElseThrow(() -> new BusinessRuleViolationException("ACCOUNT_NOT_FOUND",
                        "Account '" + request.getAccountId() + "' does not exist"));

        if (request.getPeriodType() == StatementPeriodType.CUSTOM_RANGE) {
            if (request.getCustomRange() == null || request.getCustomRange().getStartDate() == null
                    || request.getCustomRange().getEndDate() == null) {
                throw new BusinessRuleViolationException("CUSTOM_RANGE_REQUIRED",
                        "customRange with startDate and endDate is required when periodType is CUSTOM_RANGE");
            }
            if (!request.getCustomRange().getStartDate().isBefore(request.getCustomRange().getEndDate())) {
                throw new BusinessRuleViolationException("INVALID_CUSTOM_RANGE",
                        "customRange.startDate must be before customRange.endDate");
            }
        }

        if (request.getDeliveryMethod() == StatementDeliveryMethod.EMAIL
                && (request.getDeliveryEmail() == null || request.getDeliveryEmail().isBlank())) {
            throw new BusinessRuleViolationException("DELIVERY_EMAIL_REQUIRED",
                    "deliveryEmail is required when deliveryMethod is EMAIL");
        }

        StatementRequest statementRequest = StatementRequest.builder()
                .statementRequestId(IdGenerator.generate("STR"))
                .accountId(request.getAccountId())
                .periodType(request.getPeriodType())
                .customStartDate(request.getCustomRange() != null ? request.getCustomRange().getStartDate() : null)
                .customEndDate(request.getCustomRange() != null ? request.getCustomRange().getEndDate() : null)
                .format(request.getFormat())
                .deliveryMethod(request.getDeliveryMethod())
                .deliveryEmail(request.getDeliveryEmail())
                .status(StatementRequestStatus.RECEIVED)
                .createdAt(Instant.now())
                .build();

        // Neither depends on the other's output, so run the PDF build and the DB save
        // on separate threads at the same time, instead of one after the other.
        // Note: save() moves off this method's @Transactional thread when run this way,
        // so Spring Data JPA opens its own short-lived transaction for it on the worker
        // thread instead of joining the one this method started.
        StatementRequest statementRequestSnapshot = statementRequest;
        CompletableFuture<byte[]> pdfPreviewFuture = request.getFormat() == StatementFormat.PDF
                ? CompletableFuture.supplyAsync(() -> generateStatementPdf(statementRequestSnapshot, account))
                : null;
        CompletableFuture<StatementRequest> saveFuture =
                CompletableFuture.supplyAsync(() -> statementRequestRepository.save(statementRequestSnapshot));

        statementRequest = saveFuture.join();
        log.info("Statement request {} received for account {}", statementRequest.getStatementRequestId(),
                request.getAccountId());

        if (pdfPreviewFuture != null) {
            byte[] pdfPreview = pdfPreviewFuture.join();
            log.info("Pre-generated {}-byte PDF preview for statement request {} concurrently with the save",
                    pdfPreview.length, statementRequest.getStatementRequestId());
        }

        return StatementRequestResponse.builder()
                .statementRequestId(statementRequest.getStatementRequestId())
                .accountId(statementRequest.getAccountId())
                .format(statementRequest.getFormat())
                .status(statementRequest.getStatus())
                .createdAt(statementRequest.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] getStatementPdf(String statementRequestId) {
        StatementRequest statementRequest = statementRequestRepository.findByStatementRequestId(statementRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("STATEMENT_REQUEST_NOT_FOUND",
                        "Statement request '" + statementRequestId + "' does not exist"));

        if (statementRequest.getFormat() != StatementFormat.PDF) {
            throw new BusinessRuleViolationException("PDF_NOT_AVAILABLE",
                    "Statement request '" + statementRequestId + "' was not requested in PDF format");
        }

        Account account = accountRepository.findByAccountId(statementRequest.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("ACCOUNT_NOT_FOUND",
                        "Account '" + statementRequest.getAccountId() + "' does not exist"));

        return generateStatementPdf(statementRequest, account);
    }

    private byte[] generateStatementPdf(StatementRequest statementRequest, Account account) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float margin = 50;
            float leading = 18;
            float y = page.getMediaBox().getHeight() - margin;

            List<String> lines = new ArrayList<>(List.of(
                    "Statement Request ID: " + statementRequest.getStatementRequestId(),
                    "Account ID: " + account.getAccountId(),
                    "Account Type: " + account.getAccountType(),
                    "Branch Code: " + account.getBranchCode(),
                    "Currency: " + account.getCurrency(),
                    "Current Balance: " + account.getBalance(),
                    "Period Type: " + statementRequest.getPeriodType()));

            if (statementRequest.getPeriodType() == StatementPeriodType.CUSTOM_RANGE) {
                lines.add("Custom Range: " + statementRequest.getCustomStartDate() + " to "
                        + statementRequest.getCustomEndDate());
            }

            lines.add("Generated At: " + Instant.now());
            lines.add("");
            lines.add("This is a synthetic statement generated for API contract-testing purposes.");

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                content.beginText();
                content.setFont(bold, 16);
                content.newLineAtOffset(margin, y);
                content.showText("Account Statement");
                content.endText();
                y -= leading * 2;

                content.setFont(regular, 11);
                for (String line : lines) {
                    content.beginText();
                    content.newLineAtOffset(margin, y);
                    content.showText(line);
                    content.endText();
                    y -= leading;
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate statement PDF", e);
        }
    }
}
