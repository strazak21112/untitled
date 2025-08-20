package pl.wiktor.koprowski.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.auth.RegisterRequest;
import pl.wiktor.koprowski.DTO.basic.UserDTO;
import pl.wiktor.koprowski.DTO.row.UserRowDTO;
import pl.wiktor.koprowski.domain.User;
import pl.wiktor.koprowski.service.basic.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

   @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/managers")
    public ResponseEntity<String> createManager(@RequestBody RegisterRequest request, @RequestParam String lang) {
        userService.createManager(request,lang);
        return ResponseEntity.ok("Manager created successfully");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO, @RequestParam String lang) {
        userDTO.setId(id);
        userService.updateUser(userDTO);
        return ResponseEntity.ok("User updated successfully");
    }

   @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id, @RequestParam String lang) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

   @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.loadUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserDetails(@PathVariable Long id,
                                                              @RequestParam(defaultValue = "pl") String lang) {
        UserDTO details = userService.getUserDetails(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", details);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rows")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUserRows(@RequestParam(defaultValue = "pl") String lang) {
        List<UserRowDTO> userRows = userService.getAllUserRows();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", userRows);
        return ResponseEntity.ok(response);
    }


}
