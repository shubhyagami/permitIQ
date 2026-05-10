package com.compliance.dashboard.controller;

import com.compliance.dashboard.dto.SignupRequest;
import com.compliance.dashboard.entity.Gender;
import com.compliance.dashboard.exception.BadRequestException;
import com.compliance.dashboard.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthService authService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        if (!model.containsAttribute("signupRequest")) {
            model.addAttribute("signupRequest", new SignupRequest());
        }
        model.addAttribute("genders", Gender.values());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(
            @Valid @ModelAttribute SignupRequest signupRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("genders", Gender.values());
            return "signup";
        }
        try {
            authService.register(signupRequest);
            redirectAttributes.addFlashAttribute("success", "Account created. Please log in.");
            return "redirect:/login";
        } catch (BadRequestException ex) {
            model.addAttribute("genders", Gender.values());
            model.addAttribute("error", ex.getMessage());
            return "signup";
        }
    }
}
