package com.compliance.dashboard.service;

import com.compliance.dashboard.dto.DocumentResponse;
import com.compliance.dashboard.dto.DocumentUpdateRequest;
import com.compliance.dashboard.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentResponse upload(User user, MultipartFile file);

    List<DocumentResponse> listForUser(User user);

    DocumentResponse getForUser(User user, Long documentId);

    DocumentResponse update(User user, Long documentId, DocumentUpdateRequest request);

    void delete(User user, Long documentId);

    Page<DocumentResponse> search(User user, String query, Pageable pageable);

    List<DocumentResponse> expiringSoon(User user);

    void refreshStatuses();
}
