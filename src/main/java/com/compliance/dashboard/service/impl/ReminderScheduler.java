package com.compliance.dashboard.service.impl;

import com.compliance.dashboard.entity.DocumentStatus;
import com.compliance.dashboard.repository.DocumentRepository;
import com.compliance.dashboard.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final DocumentService documentService;
    private final DocumentRepository documentRepository;

    @Scheduled(cron = "${app.reminder.status-refresh-cron:0 0 * * * *}")
    public void refreshDocumentStatuses() {
        documentService.refreshStatuses();
    }

    @Scheduled(cron = "${app.reminder.daily-reminder-cron:0 0 9 * * *}")
    @Transactional(readOnly = true)
    public void logExpiryReminders() {
        LocalDate reminderDate = LocalDate.now().plusDays(7);
        documentRepository.findByStatusAndExpiryDate(DocumentStatus.EXPIRING_SOON, reminderDate)
                .forEach(document -> log.info(
                        "Expiry reminder: document '{}' for user '{}' expires on {}",
                        document.getDocumentName(),
                        document.getUser().getEmail(),
                        document.getExpiryDate()
                ));
    }
}
