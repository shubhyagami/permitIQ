package com.compliance.dashboard.repository;

import com.compliance.dashboard.entity.Document;
import com.compliance.dashboard.entity.DocumentStatus;
import com.compliance.dashboard.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findByUserOrderByUploadTimeDesc(User user, Pageable pageable);

    List<Document> findByUserOrderByUploadTimeDesc(User user);

    Optional<Document> findByIdAndUser(Long id, User user);

    Page<Document> findByUserAndDocumentNameContainingIgnoreCaseOrderByUploadTimeDesc(
            User user,
            String documentName,
            Pageable pageable
    );

    List<Document> findByUserAndExpiryDateBetweenOrderByExpiryDateAsc(User user, LocalDate start, LocalDate end);

    List<Document> findByStatusAndExpiryDate(DocumentStatus status, LocalDate expiryDate);
}
