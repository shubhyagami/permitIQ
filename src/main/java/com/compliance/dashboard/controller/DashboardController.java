package com.compliance.dashboard.controller;

import com.compliance.dashboard.dto.DocumentResponse;
import com.compliance.dashboard.dto.DocumentUpdateRequest;
import com.compliance.dashboard.entity.User;
import com.compliance.dashboard.exception.ResourceNotFoundException;
import com.compliance.dashboard.repository.DocumentRepository;
import com.compliance.dashboard.repository.UserRepository;
import com.compliance.dashboard.service.DocumentService;
import com.compliance.dashboard.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final SecurityUtil securityUtil;
    private final DocumentService documentService;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        var user = securityUtil.currentUser();
        model.addAttribute("user", user);
        model.addAttribute("documents", documentService.listForUser(user));
        return "dashboard";
    }

    @GetMapping("/upload")
    public String upload() {
        return "upload";
    }

    @GetMapping("/documents/{id}/edit")
    public String editDocument(@PathVariable Long id, Model model) {
        DocumentResponse document = documentService.getForUser(securityUtil.currentUser(), id);
        model.addAttribute("documentId", id);
        model.addAttribute("document", document);
        model.addAttribute("documentUpdateRequest", toUpdateRequest(document));
        return "edit-document";
    }

    @PostMapping("/documents/{id}/edit")
    public String updateDocument(
            @PathVariable Long id,
            @Valid @ModelAttribute DocumentUpdateRequest documentUpdateRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("documentId", id);
            model.addAttribute("document", documentService.getForUser(securityUtil.currentUser(), id));
            return "edit-document";
        }
        documentService.update(securityUtil.currentUser(), id, documentUpdateRequest);
        redirectAttributes.addFlashAttribute("success", "Document updated.");
        return "redirect:/dashboard";
    }

    @PostMapping("/documents/{id}/delete")
    public String deleteDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        documentService.delete(securityUtil.currentUser(), id);
        redirectAttributes.addFlashAttribute("success", "Document removed.");
        return "redirect:/dashboard";
    }

    @PostMapping("/upload")
    public String upload(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        try {
            documentService.upload(securityUtil.currentUser(), file);
            redirectAttributes.addFlashAttribute("success", "Document uploaded and queued for extraction.");
            return "redirect:/dashboard";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/upload";
        }
    }

@GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("user", securityUtil.currentUser());
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model) {
        model.addAttribute("user", securityUtil.currentUser());
        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(
            @RequestParam String name,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String company,
            @RequestParam int age,
            RedirectAttributes redirectAttributes
    ) {
        User current = securityUtil.currentUser();
        current.setName(name.trim());
        current.setPhoneNumber(phoneNumber);
        current.setCompany(company);
        current.setAge(age);
        userRepository.save(current);
        redirectAttributes.addFlashAttribute("success", "Profile updated.");
        return "redirect:/profile";
    }

    @GetMapping("/expiring")
    public String expiring(Model model) {
        var user = securityUtil.currentUser();
        model.addAttribute("user", user);
        model.addAttribute("documents", documentService.expiringSoon(user));
        return "expiring";
    }

    @GetMapping("/admin")
    public String adminStats(Model model) {
        model.addAttribute("usersCount", userRepository.count());
        model.addAttribute("documentsCount", documentRepository.count());
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin";
    }

    @GetMapping("/admin/users/{id}")
    public String adminViewUser(@PathVariable Long id, Model model) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<DocumentResponse> userDocs = documentService.listForUser(targetUser);
        model.addAttribute("targetUser", targetUser);
        model.addAttribute("documents", userDocs);
        return "admin-user-detail";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String adminDeleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(targetUser);
        redirectAttributes.addFlashAttribute("success", "User '" + targetUser.getEmail() + "' deleted.");
        return "redirect:/admin";
    }

    private DocumentUpdateRequest toUpdateRequest(DocumentResponse document) {
        DocumentUpdateRequest request = new DocumentUpdateRequest();
        request.setDocumentName(document.documentName());
        request.setDocumentType(document.documentType());
        request.setPermitNumber(document.permitNumber());
        request.setIssueDate(document.issueDate());
        request.setExpiryDate(document.expiryDate());
        request.setAuthorityName(document.authorityName());
        return request;
    }
}
