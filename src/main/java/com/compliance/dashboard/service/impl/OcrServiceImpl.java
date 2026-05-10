package com.compliance.dashboard.service.impl;

import com.compliance.dashboard.service.OcrService;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class OcrServiceImpl implements OcrService {

    @Override
    public String extractText(Path filePath, String contentType) {
        // Placeholder for a future Tesseract integration. Keep the service boundary stable so
        // Tess4J or a sidecar OCR process can be added without changing document upload logic.
        return "";
    }
}
