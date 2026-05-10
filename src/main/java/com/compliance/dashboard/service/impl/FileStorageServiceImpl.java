package com.compliance.dashboard.service.impl;

import com.compliance.dashboard.config.ApplicationProperties;
import com.compliance.dashboard.exception.FileStorageException;
import com.compliance.dashboard.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf", "image/jpeg", "image/png");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");

    private final Path uploadRoot;
    private final long maxFileSizeBytes;

    public FileStorageServiceImpl(ApplicationProperties properties) {
        this.uploadRoot = Path.of(properties.upload().dir()).toAbsolutePath().normalize();
        this.maxFileSizeBytes = properties.upload().maxFileSizeBytes();
    }

    @Override
    public StoredFile store(MultipartFile file) {
        validate(file);
        try {
            Files.createDirectories(uploadRoot);
            String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());
            String extension = extensionOf(originalName);
            String storedName = UUID.randomUUID() + "." + extension;
            Path target = uploadRoot.resolve(storedName).normalize();
            if (!target.startsWith(uploadRoot)) {
                throw new FileStorageException("Invalid upload path");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(originalName, storedName, target.toString(), file.getContentType());
        } catch (IOException ex) {
            throw new FileStorageException("Could not store uploaded file", ex);
        }
    }

    @Override
    public void delete(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return;
        }
        try {
            Path target = Path.of(filePath).toAbsolutePath().normalize();
            if (target.startsWith(uploadRoot)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete uploaded file", ex);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File is required");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new FileStorageException("File exceeds allowed size");
        }
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = extensionOf(originalName);
        String contentType = file.getContentType();
        if (!ALLOWED_EXTENSIONS.contains(extension) || contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new FileStorageException("Only PDF, JPG, and PNG files are allowed");
        }
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
