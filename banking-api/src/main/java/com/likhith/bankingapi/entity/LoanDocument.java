package com.likhith.bankingapi.entity;

import java.time.Instant;

import com.likhith.bankingapi.entity.enums.LoanEnums.LoanDocumentType;
import com.likhith.bankingapi.entity.enums.LoanEnums.UploadedByType;

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
@Table(name = "loan_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false, unique = true)
    private String documentId;

    @Column(name = "loan_id", nullable = false)
    private String loanId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private LoanDocumentType documentType;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "checksum", nullable = false)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "uploaded_by", nullable = false)
    private UploadedByType uploadedBy;

    @Column(name = "upload_token", nullable = false)
    private String uploadToken;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
