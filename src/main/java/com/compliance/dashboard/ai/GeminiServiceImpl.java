package com.compliance.dashboard.ai;

import com.compliance.dashboard.config.ApplicationProperties;
import com.compliance.dashboard.dto.DocumentExtractionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Service
public class GeminiServiceImpl implements GeminiService {

    private static final String EXTRACTION_PROMPT = """
            Extract the following information from this permit/document and return JSON only:
            1. Document Name (e.g. "PUC Certificate", "Fire Safety Certificate", "Environmental Clearance", "Temporary Registration","Insurance papers","Road tax receipt","Form 21 / 22","FASTag","Owner manual")
            2. Permit Number
            3. Issue Date
            4. Expiry Date
            5. Authority Name
            6. Document Type

            Use this exact JSON structure:
            {
              "documentName": "",
              "permitNumber": "",
              "issueDate": "yyyy-MM-dd",
              "expiryDate": "yyyy-MM-dd",
              "authorityName": "",
              "documentType": ""
            }
            If a value is missing, use null.
            """;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApplicationProperties properties;

    public GeminiServiceImpl(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public DocumentExtractionResult extractDocumentData(Path filePath, String contentType, String ocrText) {
        if (!StringUtils.hasText(properties.gemini().apiKey())) {
            return DocumentExtractionResult.failed("Gemini API key is not configured");
        }
        try {
            String responseText = requestExtraction(filePath, contentType, ocrText);
            JsonNode json = objectMapper.readTree(cleanJson(responseText));
            return DocumentExtractionResult.builder()
                    .documentName(text(json, "documentName"))
                    .permitNumber(text(json, "permitNumber"))
                    .issueDate(date(json, "issueDate"))
                    .expiryDate(date(json, "expiryDate"))
                    .authorityName(text(json, "authorityName"))
                    .documentType(text(json, "documentType"))
                    .rawText(responseText)
                    .extractionSuccessful(true)
                    .build();
        } catch (Exception ex) {
            return DocumentExtractionResult.failed("Gemini extraction failed: " + ex.getMessage());
        }
    }

    private String requestExtraction(Path filePath, String contentType, String ocrText) throws IOException {
        byte[] bytes = Files.readAllBytes(filePath);
        Content content = Content.fromParts(
                Part.fromText(EXTRACTION_PROMPT + "\nOCR text if available:\n" + (ocrText == null ? "" : ocrText)),
                Part.fromBytes(bytes, contentType)
        );
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .candidateCount(1)
                .build();

        try (Client client = Client.builder().apiKey(properties.gemini().apiKey()).build()) {
            GenerateContentResponse response = client.models.generateContent(
                    properties.gemini().model(),
                    content,
                    config
            );
            String text = response.text();
            if (!StringUtils.hasText(text)) {
                throw new IllegalStateException("Gemini response did not contain extracted JSON");
            }
            return text;
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Gemini response did not contain extracted JSON");
        }
    }

    private String cleanJson(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
        }
        int first = cleaned.indexOf('{');
        int last = cleaned.lastIndexOf('}');
        if (first >= 0 && last > first) {
            return cleaned.substring(first, last + 1);
        }
        return cleaned;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || !StringUtils.hasText(value.asText())) {
            return null;
        }
        return value.asText().trim();
    }

    private LocalDate date(JsonNode node, String field) {
        String value = text(node, field);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next common permit date format.
            }
        }
        return null;
    }
}
