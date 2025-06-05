package pl.wiktor.koprowski.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wiktor.koprowski.DTO.RegisterRequest;
import pl.wiktor.koprowski.DTO.UserDTO;
import pl.wiktor.koprowski.domain.*;
import pl.wiktor.koprowski.repository.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final ApartmentRepository apartmentRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReadingRepository readingRepository;
    private final PeselRepository peselRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final TranslationService translationService;

    @Autowired
    public UserService(ApartmentRepository apartmentRepository,
                       BuildingRepository buildingRepository,
                       UserRepository userRepository,
                       InvoiceRepository invoiceRepository,
                       ReadingRepository readingRepository,
                       PeselRepository peselRepository,
                       ActivationTokenRepository activationTokenRepository,
                       EmailService emailService,
                       PasswordEncoder passwordEncoder, TranslationService translationService) {
        this.apartmentRepository = apartmentRepository;
        this.buildingRepository = buildingRepository;
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
        this.readingRepository = readingRepository;
        this.peselRepository = peselRepository;
        this.activationTokenRepository = activationTokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.translationService = translationService;
    }

    @Transactional
    public void createManager(RegisterRequest registerRequest, String lang) {
        if (userRepository.existsByEmail(registerRequest.getEmail()) || isAdminEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("error_email_in_use");
        }

        if (userRepository.existsByTelephone(registerRequest.getPhoneNumber())) {
            throw new IllegalArgumentException("error_phone_in_use");
        }

        if (peselRepository.existsByPesel(registerRequest.getPesel())) {
            throw new IllegalArgumentException("error_pesel_in_use");
        }

        if (!Pesel.isValidPesel(registerRequest.getPesel())) {
            throw new IllegalArgumentException("error_invalid_pesel");
        }

        User newManager = new User();
        newManager.setFirstName(registerRequest.getFirstName());
        newManager.setLastName(registerRequest.getLastName());
        newManager.setUsername(registerRequest.getEmail());
        newManager.setEmail(registerRequest.getEmail());
        newManager.setTelephone(registerRequest.getPhoneNumber());
        newManager.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newManager.setEnabled(false);
        newManager.setRole("MANAGER");

        newManager = userRepository.save(newManager);

        Pesel pesel = new Pesel(registerRequest.getPesel(), newManager);
        peselRepository.save(pesel);

        String activationTokenValue = UUID.randomUUID().toString();
        while (activationTokenRepository.existsByToken(activationTokenValue)) {
            activationTokenValue = UUID.randomUUID().toString();
        }

        ActivationToken token = ActivationToken.builder()
                .token(activationTokenValue)
                .user(newManager)
                .used(false)
                .build();
        activationTokenRepository.save(token);

        String activationLink = "http://localhost:3000/activate?token=" + activationTokenValue;
        String subject = translationService.getTranslation("email_activation_subject",lang);
        String message = translationService.getTranslation("email_activation_body",lang) + "\n" + activationLink;

        emailService.sendMail(newManager.getEmail(), message, subject);
    }

    public User loadUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
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
    public void updateUser(UserDTO userDTO, String lang) {
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        lang.equals("pl") ? "Użytkownik nie znaleziony" :
                                lang.equals("de") ? "Benutzer nicht gefunden" : "User not found"));

        if (userRepository.existsByTelephone(userDTO.getTelephone()) && !user.getTelephone().equals(userDTO.getTelephone())) {
            throw new IllegalArgumentException(
                    lang.equals("pl") ? "Numer telefonu jest już w użyciu" :
                            lang.equals("de") ? "Telefonnummer ist bereits in Gebrauch" : "Phone number is already in use");
        }

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setTelephone(userDTO.getTelephone());


        if (user.getRole().equals("USER")) {
            if (user.getApartment() != null && userDTO.getApartment().getId() == null) {
                 Apartment oldApartment = user.getApartment();
                oldApartment.setTenant(null);
                apartmentRepository.save(oldApartment);
                user.setApartment(null);
            } else if (userDTO.getApartment().getId() != null) {
                 Apartment apartment = apartmentRepository.findById(userDTO.getApartment().getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                lang.equals("pl") ? "Apartament nie znaleziony" :
                                        lang.equals("de") ? "Wohnung nicht gefunden" : "Apartment not found"));

                if (!apartment.equals(user.getApartment())) {
                     if (apartment.getTenant() != null) {
                        throw new IllegalArgumentException(
                                lang.equals("pl") ? "Ten apartament jest już zajęty" :
                                        lang.equals("de") ? "Diese Wohnung ist bereits belegt" : "This apartment is already occupied");
                    }

                     if (user.getApartment() != null) {
                        Apartment oldApartment = user.getApartment();
                        oldApartment.setTenant(null);
                        apartmentRepository.save(oldApartment);
                    }
                     apartment.setTenant(user);
                    user.setApartment(apartment);
                    apartmentRepository.save(apartment);
                }
            }
        } else {
            throw new IllegalArgumentException(
                    lang.equals("pl") ? "Tylko najemcy mogą być przypisani do apartamentu" :
                            lang.equals("de") ? "Nur Mieter können einer Wohnung zugewiesen werden" : "Only tenants can be assigned to an apartment");
        }


        if ("MANAGER".equals(user.getRole())) {
            List<Building> currentBuildings = new ArrayList<>(user.getManagedBuildings());
            List<Building> newBuildings = userDTO.getManagedBuildingIds() != null
                    ? buildingRepository.findAllById(userDTO.getManagedBuildingIds())
                    : new ArrayList<>();

             for (Building oldBuilding : currentBuildings) {
                if (!newBuildings.contains(oldBuilding)) {
                    oldBuilding.getManagers().remove(user);
                    buildingRepository.save(oldBuilding);
                }
            }

             for (Building newBuilding : newBuildings) {
                if (!currentBuildings.contains(newBuilding)) {
                    newBuilding.getManagers().add(user);
                    buildingRepository.save(newBuilding);
                }
            }

            user.setManagedBuildings(newBuildings);
        }


        userRepository.save(user);
    }



    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("error_user_not_found"));

        if (user.getApartment() != null) {
            user.getApartment().setTenant(null);
            apartmentRepository.save(user.getApartment());
        }

        for (Building building : user.getManagedBuildings()) {
            building.getManagers().remove(user);
            buildingRepository.save(building);
        }

        Set<Invoice> invoices = invoiceRepository.findByTenant(user);
        for (Invoice invoice : invoices) {
            Optional<Reading> readingOptional = readingRepository.findByInvoice(invoice);
            if (readingOptional.isPresent()) {
                Reading reading = readingOptional.get();
                reading.setInvoice(null);
                readingRepository.save(reading);
            }
            invoiceRepository.delete(invoice);
        }

        userRepository.delete(user);
    }



}
