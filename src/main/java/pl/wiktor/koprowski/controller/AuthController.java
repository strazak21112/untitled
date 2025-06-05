package pl.wiktor.koprowski.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.AdminCredentials;
import pl.wiktor.koprowski.DTO.RegisterRequest;
import pl.wiktor.koprowski.DTO.UserCredentials;
import pl.wiktor.koprowski.service.AuthService;
import pl.wiktor.koprowski.service.ReCaptchaService;
import pl.wiktor.koprowski.service.TranslationService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final ReCaptchaService reCaptchaService;
    private final TranslationService translationService;

    @Autowired
    public AuthController(AuthService authService,
                          ReCaptchaService reCaptchaService,
                          TranslationService translationService) {
        this.authService = authService;
        this.reCaptchaService = reCaptchaService;
        this.translationService = translationService;
    }

    @PostMapping("/login/admin")
    public ResponseEntity<Map<String, String>> loginAdmin(@RequestBody AdminCredentials adminCredentials,
                                                          @RequestParam(defaultValue = "pl") String lang) {
        Map<String, String> response = authService.loginAdmin(adminCredentials, lang);
        response.put("status", "success");


        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest registerRequest,
                                                        @RequestParam(defaultValue = "pl") String lang) {
        if (!reCaptchaService.verify(registerRequest.getRecaptchaToken())) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", translationService.getTranslation("error_recaptcha_invalid", lang));
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }

        authService.register(registerRequest, lang);

        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("status", "success");
         return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(successResponse);
    }

    @PostMapping("/activate")
    public ResponseEntity<Map<String, String>> activateAccount(@RequestBody Map<String, String> request,
                                                               @RequestParam(defaultValue = "pl") String lang) {
        String token = request.get("token");
        authService.activateAccount(token, lang);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
         return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody UserCredentials userCredentials,
                                                         @RequestParam(defaultValue = "pl") String lang) {
        Map<String, String> response = authService.loginUser(userCredentials, lang);
        response.put("status", "success");


        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}
