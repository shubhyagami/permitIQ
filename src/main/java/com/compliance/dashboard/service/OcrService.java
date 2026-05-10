package com.compliance.dashboard.service;

import java.nio.file.Path;

public interface OcrService {

    String extractText(Path filePath, String contentType);
}
