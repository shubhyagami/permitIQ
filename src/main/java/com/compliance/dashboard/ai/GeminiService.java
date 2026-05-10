package com.compliance.dashboard.ai;

import com.compliance.dashboard.dto.DocumentExtractionResult;

import java.nio.file.Path;

public interface GeminiService {

    DocumentExtractionResult extractDocumentData(Path filePath, String contentType, String ocrText);
}
