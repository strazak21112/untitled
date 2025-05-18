package pl.wiktor.koprowski.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.RegisterRequest;
import pl.wiktor.koprowski.DTO.UserDTO;
import pl.wiktor.koprowski.domain.User;
import pl.wiktor.koprowski.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

   @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/managers")
    public ResponseEntity<String> createManager(@RequestBody RegisterRequest request, @RequestParam String lang) {
        userService.createManager(request, lang);
        return ResponseEntity.ok("Manager created successfully");
    }

  //  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO, @RequestParam String lang) {
        userDTO.setId(id);
        userService.updateUser(userDTO, lang);
        return ResponseEntity.ok("User updated successfully");
    }

 //   @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id, @RequestParam String lang) {
        userService.deleteUser(id, lang);
        return ResponseEntity.ok("User deleted successfully");
    }

 //   @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.loadUserByEmail(email);
        return ResponseEntity.ok(user);
    }


    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

     @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }
}
