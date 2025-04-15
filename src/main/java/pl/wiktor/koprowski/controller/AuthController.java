package pl.wiktor.koprowski.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.AdminCredentials;
import pl.wiktor.koprowski.DTO.RegisterRequest;
import pl.wiktor.koprowski.DTO.UserCredentials;
import pl.wiktor.koprowski.service.AuthService;
import pl.wiktor.koprowski.service.ReCaptchaService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final ReCaptchaService reCaptchaService;

    @Autowired
    public AuthController(AuthService authService, ReCaptchaService reCaptchaService) {
        this.authService = authService;
        this.reCaptchaService = reCaptchaService;
    }

    @PostMapping("/login/admin")
    public ResponseEntity<Map<String, String>> loginAdmin(@RequestBody AdminCredentials adminCredentials) {
        try {
            Map<String, String> response = authService.loginAdmin(adminCredentials);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid credentials");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest registerRequest) {
        Map<String, String> response = new HashMap<>();

        if (!reCaptchaService.verify(registerRequest.getRecaptchaToken())) {
            response.put("status", "error");
            response.put("message", "Invalid reCAPTCHA token");
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }

        try {
            authService.register(registerRequest);
            response.put("status", "success");
            response.put("message", "Registration successful! Please check your email to activate your account.");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }

    @PostMapping("/activate")
    public ResponseEntity<Map<String, String>> activateAccount(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        Map<String, String> response = new HashMap<>();

        try {
            authService.activateAccount(token);
            response.put("status", "success");
            response.put("message", "Account activated successfully");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody UserCredentials userCredentials) {
        try {
            Map<String, String> response = authService.loginUser(userCredentials);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Invalid credentials");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

}

