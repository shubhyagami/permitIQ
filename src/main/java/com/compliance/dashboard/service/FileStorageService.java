package com.compliance.dashboard.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFile store(MultipartFile file);

    void delete(String filePath);

    record StoredFile(String originalFileName, String storedFileName, String filePath, String contentType) {
    }
}
