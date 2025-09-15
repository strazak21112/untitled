package pl.wiktor.koprowski.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.auth.ChangePasswordRequest;
import pl.wiktor.koprowski.DTO.auth.RegisterRequest;
import pl.wiktor.koprowski.DTO.auth.UpdateProfileRequest;
import pl.wiktor.koprowski.DTO.auth.UserProfileDTO;
import pl.wiktor.koprowski.DTO.basic.UserDTO;
import pl.wiktor.koprowski.DTO.row.UserRowDTO;
import pl.wiktor.koprowski.domain.User;
import pl.wiktor.koprowski.service.TranslationService;
import pl.wiktor.koprowski.service.basic.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TranslationService translationService;

    @PostMapping(value = "/managers", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createManager(
            @Valid @RequestBody RegisterRequest request,
            @RequestParam(defaultValue = "pl") String lang) {

        userService.createManager(request, lang);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", translationService.getTranslation("manager_created_success", lang));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO,
            @RequestParam(defaultValue = "pl") String lang) {

        userDTO.setId(id);
        userService.updateUser(userDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("userId", id);
        response.put("message", translationService.getTranslation("user_updated_success", lang));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pl") String lang) {

        userService.deleteUser(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", translationService.getTranslation("user_deleted_success", lang));
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/email/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getUserByEmail(
            @PathVariable String email,
            @RequestParam(defaultValue = "pl") String lang) {

        User user = userService.loadUserByEmail(email);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", user);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserDetails(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pl") String lang) {

        UserDTO details = userService.getUserDetails(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", details);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/rows", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUserRows(
            @RequestParam(defaultValue = "pl") String lang) {

        List<UserRowDTO> userRows = userService.getAllUserRows();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", userRows);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/change-password", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestParam(defaultValue = "pl") String lang,
            @RequestParam String email) {

        userService.changePassword(request, email);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", translationService.getTranslation("password_changed_success", lang));
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/update-profile", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @RequestParam(defaultValue = "pl") String lang,
            @RequestParam String email) {

        userService.updateProfile(request, email);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", translationService.getTranslation("profile_updated_success", lang));
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/profile-by-email", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserProfileByEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "pl") String lang) {

        UserProfileDTO profile = userService.getUserProfileByEmail(email);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
         response.put("data", profile);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/details-by-email", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserDetailsByEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "pl") String lang) {

        UserDTO details = userService.getUserDetailsByEmail(email);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
         response.put("data", details);
        return ResponseEntity.ok(response);
    }

}
