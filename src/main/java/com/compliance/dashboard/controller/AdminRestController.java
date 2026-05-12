package com.compliance.dashboard.controller;

import com.compliance.dashboard.dto.DocumentResponse;
import com.compliance.dashboard.dto.DocumentUpdateRequest;
import com.compliance.dashboard.dto.UserResponse;
import com.compliance.dashboard.entity.Document;
import com.compliance.dashboard.entity.User;
import com.compliance.dashboard.exception.ResourceNotFoundException;
import com.compliance.dashboard.repository.DocumentRepository;
import com.compliance.dashboard.repository.UserRepository;
import com.compliance.dashboard.service.FileStorageService;
import com.compliance.dashboard.timer.CountdownService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminRestController {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final CountdownService countdownService;

    // ─── USERS ───────────────────────────────────────────────

    @GetMapping("/users")
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @GetMapping("/users/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return toUserResponse(findUser(id));
    }

    @PutMapping("/users/{id}")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateRequest request) {
        User user = findUser(id);
        user.setName(request.name().trim());
        if (request.phoneNumber() != null) user.setPhoneNumber(request.phoneNumber());
        if (request.company() != null) user.setCompany(request.company());
        if (request.age() != null) user.setAge(request.age());
        if (request.role() != null) user.setRole(request.role());
        return toUserResponse(userRepository.save(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = findUser(id);
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    // ─── DOCUMENTS ───────────────────────────────────────────

    @GetMapping("/documents")
    public List<DocumentResponse> listDocuments() {
        return documentRepository.findAll().stream()
                .map(this::toDocumentResponse)
                .toList();
    }

    @GetMapping("/documents/{id}")
    public DocumentResponse getDocument(@PathVariable Long id) {
        return toDocumentResponse(findDocument(id));
    }

    @GetMapping("/users/{userId}/documents")
    public List<DocumentResponse> listUserDocuments(@PathVariable Long userId) {
        User user = findUser(userId);
        return documentRepository.findByUserOrderByUploadTimeDesc(user).stream()
                .map(this::toDocumentResponse)
                .toList();
    }

    @PutMapping("/documents/{id}")
    public DocumentResponse updateDocument(@PathVariable Long id, @RequestBody @Valid DocumentUpdateRequest request) {
        Document doc = findDocument(id);
        doc.setDocumentName(request.getDocumentName().trim());
        doc.setDocumentType(blankToNull(request.getDocumentType()));
        doc.setPermitNumber(blankToNull(request.getPermitNumber()));
        doc.setIssueDate(request.getIssueDate());
        doc.setExpiryDate(request.getExpiryDate());
        doc.setAuthorityName(blankToNull(request.getAuthorityName()));
        doc.setStatus(countdownService.statusFor(request.getExpiryDate()));
        return toDocumentResponse(documentRepository.save(doc));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        Document doc = findDocument(id);
        fileStorageService.delete(doc.getFilePath());
        documentRepository.delete(doc);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentResponse uploadDocument(
            @RequestParam("userId") Long userId,
            @RequestParam("file") MultipartFile file) {
        User user = findUser(userId);
        var storedFile = fileStorageService.store(file);
        Document doc = Document.builder()
                .user(user)
                .documentName(stripExtension(storedFile.originalFileName()))
                .originalFileName(storedFile.originalFileName())
                .storedFileName(storedFile.storedFileName())
                .filePath(storedFile.filePath())
                .status(countdownService.statusFor(null))
                .build();
        return toDocumentResponse(documentRepository.save(doc));
    }

    // ─── HELPERS ─────────────────────────────────────────────

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private Document findDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .company(user.getCompany())
                .age(user.getAge())
                .gender(user.getGender())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private DocumentResponse toDocumentResponse(Document doc) {
        var countdown = countdownService.calculate(doc.getExpiryDate());
        return DocumentResponse.builder()
                .id(doc.getId())
                .documentName(doc.getDocumentName())
                .documentType(doc.getDocumentType())
                .permitNumber(doc.getPermitNumber())
                .issueDate(doc.getIssueDate())
                .expiryDate(doc.getExpiryDate())
                .authorityName(doc.getAuthorityName())
                .originalFileName(doc.getOriginalFileName())
                .uploadTime(doc.getUploadTime())
                .status(countdownService.statusFor(doc.getExpiryDate()))
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

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    // Inline record for user update
    public record UserUpdateRequest(
            String name,
            String phoneNumber,
            String company,
            Integer age,
            com.compliance.dashboard.entity.Role role
    ) {}
}
