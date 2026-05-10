package com.compliance.dashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 180)
    private String documentName;

    @Column(length = 80)
    private String documentType;

    @Column(length = 120)
    private String permitNumber;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    @Column(length = 180)
    private String authorityName;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false, unique = true)
    private String storedFileName;

    @Column(nullable = false)
    private String filePath;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String extractedText;

    @Column(nullable = false)
    private LocalDateTime uploadTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentStatus status;

    @PrePersist
    void onCreate() {
        uploadTime = LocalDateTime.now();
        if (status == null) {
            status = DocumentStatus.ACTIVE;
        }
    }
}
