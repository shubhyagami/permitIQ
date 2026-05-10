package com.compliance.dashboard.controller;

import com.compliance.dashboard.dto.DocumentResponse;
import com.compliance.dashboard.dto.DocumentUpdateRequest;
import com.compliance.dashboard.service.DocumentService;
import com.compliance.dashboard.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentRestController {

    private final SecurityUtil securityUtil;
    private final DocumentService documentService;

    @Operation(summary = "List documents for the current user")
    @GetMapping
    public Page<DocumentResponse> documents(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadTime"));
        return documentService.search(securityUtil.currentUser(), q, pageable);
    }

    @Operation(summary = "Upload a document")
    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.upload(securityUtil.currentUser(), file));
    }

    @Operation(summary = "Update document metadata")
    @PutMapping("/{id}")
    public DocumentResponse update(
            @PathVariable Long id,
            @Valid @RequestBody DocumentUpdateRequest request
    ) {
        return documentService.update(securityUtil.currentUser(), id, request);
    }

    @Operation(summary = "Delete a document and its stored file")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(securityUtil.currentUser(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Documents expiring within configured reminder window")
    @GetMapping("/expiring")
    public List<DocumentResponse> expiring() {
        return documentService.expiringSoon(securityUtil.currentUser());
    }
}
