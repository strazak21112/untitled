package pl.wiktor.koprowski.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wiktor.koprowski.DTO.auth.AdminCredentials;
import pl.wiktor.koprowski.DTO.auth.RegisterRequest;
import pl.wiktor.koprowski.DTO.auth.UserCredentials;
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
import java.util.*;

@Service
public class AuthService {

    private final JwtUtilities jwtUtilities;
    private final UserRepository userRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final EmailService emailService;
    private final PeselRepository peselRepository;
    private final TranslationService translationService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(JwtUtilities jwtUtilities,
                       UserRepository userRepository,
                       ActivationTokenRepository activationTokenRepository,
                       EmailService emailService,
                       PeselRepository peselRepository,
                       TranslationService translationService) {
        this.jwtUtilities = jwtUtilities;
        this.userRepository = userRepository;
        this.activationTokenRepository = activationTokenRepository;
        this.emailService = emailService;
        this.peselRepository = peselRepository;
        this.translationService = translationService;
    }

    @Transactional
    public void register(RegisterRequest request, String lang) {
        if (userRepository.existsByEmail(request.getEmail()) || isAdminEmail(request.getEmail())) {
            throw new IllegalArgumentException("error_email_in_use");
        }

        if (userRepository.existsByTelephone(request.getPhoneNumber())) {
            throw new IllegalArgumentException("error_phone_in_use");
        }

        if (peselRepository.existsByPesel(request.getPesel())) {
            throw new IllegalArgumentException("error_pesel_in_use");
        }

        if (!Pesel.isValidPesel(request.getPesel())) {
            throw new IllegalArgumentException("error_pesel_invalid");
        }

        User newUser = new User();
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setUsername(request.getEmail());
        newUser.setEmail(request.getEmail());
        newUser.setTelephone(request.getPhoneNumber());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setEnabled(false);
        newUser.setRole("USER");

        Pesel pesel = new Pesel();
        pesel.setPesel(request.getPesel());
        pesel.linkUser(newUser);

        User user = userRepository.save(newUser);
        peselRepository.save(pesel);

        String activationTokenValue = UUID.randomUUID().toString();
        while (activationTokenRepository.existsByToken(activationTokenValue)) {
            activationTokenValue = UUID.randomUUID().toString();
        }

        ActivationToken token = ActivationToken.builder()
                .token(activationTokenValue)
                .user(user)
                .used(false)
                .build();

        activationTokenRepository.save(token);

        String activationLink = "http://localhost:3000/activate?token=" + activationTokenValue;
        String subject = translationService.getTranslation("email_activation_subject", lang);
        String message = translationService.getTranslation("email_activation_body", lang) + "\n" + activationLink;

        emailService.sendMail(user.getEmail(), message, subject);
    }

    @Transactional
    public void activateAccount(String token, String lang) {
        ActivationToken activationToken = activationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("error_invalid_token"));

        if (activationToken.isUsed()) {
            throw new IllegalArgumentException("error_token_used");
        }

        User user = activationToken.getUser();
        if (user.isEnabled()) {
            throw new IllegalArgumentException("error_account_already_activated");
        }

        user.setEnabled(true);
        userRepository.save(user);

        activationToken.setUsed(true);
        activationTokenRepository.save(activationToken);
    }

    public Map<String, String> loginUser(UserCredentials credentials, String lang) {
        User user = userRepository.findByUsername(credentials.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("error_credentials_invalid"));

        if (!passwordEncoder.matches(credentials.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("error_credentials_invalid");
        }

        if (!user.getRole().equals(credentials.getRole())) {
            throw new IllegalArgumentException("error_role_invalid");
        }

        String token = jwtUtilities.generateToken(user.getUsername(), user.getRole());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", user.getRole());
        response.put("username", user.getUsername());

        return response;
    }

    public Map<String, String> loginAdmin(AdminCredentials credentials, String lang) {
        if (!validateAdminCredentials(credentials)) {
            throw new IllegalArgumentException("error_credentials_invalid");
        }

        String role = "ADMIN";
        String token = jwtUtilities.generateToken(credentials.getUsername(), role);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", role);
        response.put("username", credentials.getUsername());

        return response;
    }

    private boolean validateAdminCredentials(AdminCredentials credentials) {
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
                    if (username.equals(credentials.getUsername()) &&
                            password.equals(credentials.getPassword())) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading admin file", e);
        }
        return false;
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
                if (parts.length >= 1 && parts[0].trim().equalsIgnoreCase(email)) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading admin file", e);
        }
        return false;
    }
}
