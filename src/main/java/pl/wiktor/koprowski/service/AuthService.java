package pl.wiktor.koprowski.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wiktor.koprowski.DTO.AdminCredentials;
import pl.wiktor.koprowski.DTO.RegisterRequest;
import pl.wiktor.koprowski.DTO.UserCredentials;
import pl.wiktor.koprowski.domain.ActivationToken;
import pl.wiktor.koprowski.domain.Pesel;
import pl.wiktor.koprowski.domain.User;
import pl.wiktor.koprowski.repository.ActivationTokenRepository;
import pl.wiktor.koprowski.repository.PeselRepository;
import pl.wiktor.koprowski.repository.UserRepository;
import pl.wiktor.koprowski.security.JwtUtilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final JwtUtilities jwtUtilities;
    private final UserRepository userRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final  EmailService emailService;
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PeselRepository peselRepository;

    @Transactional
    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail()) || isAdminEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (userRepository.existsByTelephone(registerRequest.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already in use");
        }

        if (peselRepository.existsByPesel(registerRequest.getPesel())) {
            throw new IllegalArgumentException("PESEL already in use");
        }

        if (!Pesel.isValidPesel(registerRequest.getPesel())) {
            throw new IllegalArgumentException("Invalid PESEL number");
        }

        User newUser = new User();
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setUsername(registerRequest.getEmail());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setTelephone(registerRequest.getPhoneNumber());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setEnabled(false);
        newUser.setRole("USER");


        Pesel pesel = new Pesel();
        pesel.setPesel(registerRequest.getPesel());
        pesel.linkUser(newUser);

         User user = userRepository.save(newUser);

         String activationTokenValue = UUID.randomUUID().toString();

        while (activationTokenRepository.existsByToken(activationTokenValue)) {
            activationTokenValue = UUID.randomUUID().toString();
        }

        ActivationToken tokens = ActivationToken.builder()
                .token(activationTokenValue)
                .user(user)
                .used(false)
                .build();
        activationTokenRepository.save(tokens);

        String activationLink = "http://localhost:3000/activate?token=" + activationTokenValue;

        emailService.sendMail(
                user.getEmail(),
                "Kliknij poniższy link, aby aktywować swoje konto:\n" + activationLink,
                "Aktywacja konta"
        );
    }

    private boolean isAdminEmail(String email) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("admin.txt")) {
            if (input == null) {
                throw new RuntimeException("admin.txt file not found in resources");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 1) {
                    String adminEmail = parts[0].trim();
                    if (adminEmail.equalsIgnoreCase(email)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading admin file", e);
        }
        return false;
    }

    @Transactional
    public void activateAccount(String token) {
        ActivationToken activationToken = activationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (activationToken.isUsed()) {
            throw new IllegalArgumentException("Token is already used");
        }

        User user = activationToken.getUser();
        if (user.isEnabled()) {
            throw new IllegalArgumentException("Account is already activated");
        }

        user.setEnabled(true);
        userRepository.save(user);

         activationToken.setUsed(true);
        activationTokenRepository.save(activationToken);
    }



    public Map<String, String> loginUser(UserCredentials userCredentials) {
        User user = userRepository.findByUsername(userCredentials.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(userCredentials.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Pobranie roli użytkownika
        String role = user.getRole();

        // Sprawdzenie, czy rola użytkownika zgadza się z podaną w `UserCredentials`
        if (!role.equals(userCredentials.getRole())) {
            throw new RuntimeException("Invalid role for this user");
        }

        // Generowanie tokena JWT
        String token = jwtUtilities.generateToken(user.getUsername(), role);

        // Tworzenie odpowiedzi JSON
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("token", token);
        response.put("role", role);
        response.put("username", user.getUsername());

        return response;
    }





    public AuthService(JwtUtilities jwtUtilities, UserRepository userRepository, ActivationTokenRepository activationTokenRepository, EmailService emailService, PeselRepository peselRepository) {
        this.jwtUtilities = jwtUtilities;
        this.userRepository = userRepository;
        this.activationTokenRepository = activationTokenRepository;
        this.emailService = emailService;
        this.peselRepository = peselRepository;
    }
    public Map<String, String> loginAdmin(AdminCredentials adminCredentials) {
        if (!validateAdminCredentials(adminCredentials)) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String role = "ADMIN";
        String token = jwtUtilities.generateToken(adminCredentials.getUsername(), role);


        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", role);
        response.put("username", adminCredentials.getUsername());

        return response;
    }

    private boolean validateAdminCredentials(AdminCredentials adminCredentials) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("admin.txt")) {
            if (input == null) {
                throw new RuntimeException("admin.txt file not found in resources");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    // Sprawdź, czy dane logowania pasują
                    if (username.equals(adminCredentials.getUsername()) && password.equals(adminCredentials.getPassword())) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading admin file", e);
        }
        return false;
    }
}
