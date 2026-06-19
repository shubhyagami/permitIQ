package com.compliance.dashboard.service.impl;

import com.compliance.dashboard.ai.GeminiService;
import com.compliance.dashboard.config.ApplicationProperties;
import com.compliance.dashboard.dto.CountdownResponse;
import com.compliance.dashboard.dto.DocumentExtractionResult;
import com.compliance.dashboard.dto.DocumentResponse;
import com.compliance.dashboard.dto.DocumentUpdateRequest;
import com.compliance.dashboard.entity.Document;
import com.compliance.dashboard.entity.User;
import com.compliance.dashboard.exception.ResourceNotFoundException;
import com.compliance.dashboard.repository.DocumentRepository;
import com.compliance.dashboard.repository.UserRepository;
import com.compliance.dashboard.service.DocumentService;
import com.compliance.dashboard.service.FileStorageService;
import com.compliance.dashboard.service.OcrService;
import com.compliance.dashboard.timer.CountdownService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final OcrService ocrService;
    private final GeminiService geminiService;
    private final CountdownService countdownService;
    private final ApplicationProperties properties;

    @Override
    @Transactional
    public DocumentResponse upload(User user, MultipartFile file) {
        FileStorageService.StoredFile storedFile = fileStorageService.store(file);
        Path path = Path.of(storedFile.filePath());
        String ocrText = ocrService.extractText(path, storedFile.contentType());
        DocumentExtractionResult extraction = geminiService.extractDocumentData(path, storedFile.contentType(), ocrText);

        String fallbackName = stripExtension(storedFile.originalFileName());
        Document document = Document.builder()
                .user(user)
                .documentName(firstNonBlank(extraction.documentName(), fallbackName))
                .documentType(extraction.documentType())
                .permitNumber(extraction.permitNumber())
                .issueDate(extraction.issueDate())
                .expiryDate(extraction.expiryDate())
                .authorityName(extraction.authorityName())
                .originalFileName(storedFile.originalFileName())
                .storedFileName(storedFile.storedFileName())
                .filePath(storedFile.filePath())
                .extractedText(extraction.extractionSuccessful() ? extraction.rawText() : extraction.failureReason())
                .status(countdownService.statusFor(extraction.expiryDate()))
                .build();
        return toResponse(documentRepository.save(document));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> listForUser(User user) {
        return documentRepository.findByUserOrderByUploadTimeDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getForUser(User user, Long documentId) {
        return toResponse(findOwnedDocument(user, documentId));
    }

    @Override
    @Transactional
    public DocumentResponse update(User user, Long documentId, DocumentUpdateRequest request) {
        Document document = findOwnedDocument(user, documentId);
        document.setDocumentName(request.getDocumentName().trim());
        document.setDocumentType(blankToNull(request.getDocumentType()));
        document.setPermitNumber(blankToNull(request.getPermitNumber()));
        document.setIssueDate(request.getIssueDate());
        document.setExpiryDate(request.getExpiryDate());
        document.setAuthorityName(blankToNull(request.getAuthorityName()));
        document.setStatus(countdownService.statusFor(request.getExpiryDate()));
        return toResponse(document);
    }

    @Override
    @Transactional
    public void delete(User user, Long documentId) {
        Document document = findOwnedDocument(user, documentId);
        fileStorageService.delete(document.getFilePath());
        documentRepository.delete(document);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> search(User user, String query, Pageable pageable) {
        Page<Document> page = StringUtils.hasText(query)
                ? documentRepository.findByUserAndDocumentNameContainingIgnoreCaseOrderByUploadTimeDesc(user, query, pageable)
                : documentRepository.findByUserOrderByUploadTimeDesc(user, pageable);
        return page.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> expiringSoon(User user) {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(properties.reminder().expiringSoonDays());
        return documentRepository.findByUserAndExpiryDateBetweenOrderByExpiryDateAsc(user, today, end).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void refreshStatuses() {
        documentRepository.findAll().forEach(document -> {
            var status = countdownService.statusFor(document.getExpiryDate());
            if (document.getStatus() != status) {
                document.setStatus(status);
            }
        });
    }

    private DocumentResponse toResponse(Document document) {
        CountdownResponse countdown = countdownService.calculate(document.getExpiryDate());
        return DocumentResponse.builder()
                .id(document.getId())
                .documentName(document.getDocumentName())
                .documentType(document.getDocumentType())
                .permitNumber(document.getPermitNumber())
                .issueDate(document.getIssueDate())
                .expiryDate(document.getExpiryDate())
                .authorityName(document.getAuthorityName())
                .originalFileName(document.getOriginalFileName())
                .uploadTime(document.getUploadTime())
                .status(countdownService.statusFor(document.getExpiryDate()))
                .remainingDays(countdown.remainingDays())
                .remainingHours(countdown.remainingHours())
                .remainingMinutes(countdown.remainingMinutes())
                .expired(countdown.expired())
                .build();
    }

    private String stripExtension(String filename) {
        int dot = filename == null ? -1 : filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private String firstNonBlank(String primary, String fallback) {
        return StringUtils.hasText(primary) ? primary : fallback;
    }

    private Document findOwnedDocument(User user, Long documentId) {
        return documentRepository.findByIdAndUser(documentId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

// sync @ 2026-06-20T01:48:36.104438
