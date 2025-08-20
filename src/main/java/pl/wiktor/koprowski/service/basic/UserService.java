package pl.wiktor.koprowski.service.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wiktor.koprowski.DTO.auth.RegisterRequest;
import pl.wiktor.koprowski.DTO.basic.ApartmentDTO;
import pl.wiktor.koprowski.DTO.basic.UserDTO;
import pl.wiktor.koprowski.DTO.inside.BuildingInfoDTO;
import pl.wiktor.koprowski.DTO.row.UserRowDTO;
import pl.wiktor.koprowski.domain.*;
import pl.wiktor.koprowski.repository.*;
import pl.wiktor.koprowski.service.EmailService;
import pl.wiktor.koprowski.service.TranslationService;

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
    public void updateUser(UserDTO userDTO) {
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("error_user_not_found"));

        if (userRepository.existsByTelephone(userDTO.getTelephone()) &&
                !user.getTelephone().equals(userDTO.getTelephone())) {
            throw new IllegalArgumentException("error_phone_in_use");
        }

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setTelephone(userDTO.getTelephone());

        if ("USER".equals(user.getRole())) {
            Long newApartmentId = userDTO.getApartment() != null ? userDTO.getApartment().getId() : null;
            Apartment currentApartment = user.getApartment();

            if (newApartmentId == null) {
                if (currentApartment != null) {
                    currentApartment.setTenant(null);
                    user.setApartment(null);
                    apartmentRepository.save(currentApartment);
                }
            } else {
                if (currentApartment == null) {

                    Apartment newApartment = apartmentRepository.findById(newApartmentId)
                            .orElseThrow(() -> new IllegalArgumentException("error_apartment_not_found"));

                    newApartment.setTenant(user);
                    user.setApartment(newApartment);
                    apartmentRepository.save(newApartment);
                }
            }
        }
        else if ("MANAGER".equals(user.getRole())) {
            List<Building> currentBuildings = new ArrayList<>(user.getManagedBuildings());

             List<Long> newBuildingIds = userDTO.getManagedBuilding() != null
                    ? userDTO.getManagedBuilding().stream()
                    .map(BuildingInfoDTO::getId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList())
                    : new ArrayList<>();

            List<Building> newBuildings = buildingRepository.findAllById(newBuildingIds);

             if (newBuildings.size() != newBuildingIds.size()) {
                throw new IllegalArgumentException("error_some_buildings_not_found");
            }

             for (Building oldBuilding : currentBuildings) {
                if (!newBuildings.contains(oldBuilding)) {
                    if (oldBuilding.getManagers().size() == 1 && oldBuilding.getManagers().contains(user)) {
                        throw new IllegalArgumentException("error_building_needs_at_least_one_manager");
                    }
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




    @Transactional(readOnly = true)
    public UserDTO getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("error_user_not_found"));

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setTelephone(user.getTelephone());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());

        if (user.getPesel() != null) {
            dto.setPesel(user.getPesel().getPesel());
        }

        if (user.getApartment() != null) {
            Apartment apartment = user.getApartment();
            ApartmentDTO apartmentDTO = new ApartmentDTO();
            apartmentDTO.setId(apartment.getId());
            apartmentDTO.setNumber(apartment.getNumber());
            apartmentDTO.setArea(apartment.getArea());
            apartmentDTO.setFloor(apartment.getFloor());

            Building building = apartment.getBuilding();

            BuildingInfoDTO buildingInfoDTO = new BuildingInfoDTO();
            buildingInfoDTO.setId(building.getId());
            buildingInfoDTO.setAddress(building.getAddress());
            apartmentDTO.setBuildingInfo(buildingInfoDTO);

            dto.setApartment(apartmentDTO);
        }

        if (user.getManagedBuildings() != null && !user.getManagedBuildings().isEmpty()) {
            List<BuildingInfoDTO> buildingDTOs = user.getManagedBuildings().stream()
                    .map(building -> {
                        BuildingInfoDTO bDTO = new BuildingInfoDTO();
                        bDTO.setId(building.getId());
                        bDTO.setAddress(building.getAddress());
                        return bDTO;
                    })
                    .collect(Collectors.toList());
            dto.setManagedBuilding(buildingDTOs);
        }


        return dto;
    }

    public List<UserRowDTO> getAllUserRows() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserRowDTO dto = new UserRowDTO();
                    dto.setId(user.getId());
                    dto.setEmail(user.getEmail());
                    dto.setRole(user.getRole());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("error_user_not_found"));

        for (Building building : user.getManagedBuildings()) {
            if (building.getManagers().size() == 1 && building.getManagers().contains(user)) {
                throw new IllegalArgumentException("error_building_needs_at_least_one_manager");
            }
        }

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
            invoice.setTenant(null);
            invoiceRepository.save(invoice);
        }

        if (user.getPesel() != null) {
            user.setPesel(null);
        }

        userRepository.delete(user);
    }


}
