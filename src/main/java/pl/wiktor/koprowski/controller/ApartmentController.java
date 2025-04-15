package pl.wiktor.koprowski.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.ApartmentDTO;
import pl.wiktor.koprowski.domain.Apartment;
import pl.wiktor.koprowski.service.ApartmentService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/apartments")
@RequiredArgsConstructor
public class ApartmentController {

    private final ApartmentService apartmentService;

    @PostMapping
    public ResponseEntity<?> createApartment(@Valid @RequestBody ApartmentDTO apartmentDTO, @RequestParam(defaultValue = "en") String lang) {
        try {
            Apartment apartment = apartmentService.createApartment(apartmentDTO, lang);
            return ResponseEntity.status(HttpStatus.CREATED).body(apartment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getApartmentById(@PathVariable Long id, @RequestParam(defaultValue = "en") String lang) {
        try {
            Optional<Apartment> apartment = apartmentService.getApartmentById(id);
            return apartment.map(ResponseEntity::ok)
                    .orElseThrow(() -> new IllegalArgumentException(getErrorMessage("apartmentNotFound", lang)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Apartment>> getAllApartments() {
        List<Apartment> apartments = apartmentService.getAllApartments();
        return apartments.isEmpty() ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                : ResponseEntity.ok(apartments);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateApartment(@PathVariable Long id, @Valid @RequestBody ApartmentDTO apartmentDTO, @RequestParam(defaultValue = "en") String lang) {
        try {
            Apartment apartment = apartmentService.updateApartment(id, apartmentDTO, lang);
            return ResponseEntity.ok(apartment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApartment(@PathVariable Long id, @RequestParam(defaultValue = "en") String lang) {
        try {
            apartmentService.deleteApartment(id, lang);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    private String getErrorMessage(String key, String lang) {
        return switch (lang.toLowerCase()) {
            case "pl" -> switch (key) {
                case "apartmentNotFound" -> "Mieszkanie nie znalezione";
                default -> "Nieznany błąd";
            };
            case "de" -> switch (key) {
                case "apartmentNotFound" -> "Wohnung nicht gefunden";
                default -> "Unbekannter Fehler";
            };
            default -> switch (key) {
                case "apartmentNotFound" -> "Apartment not found";
                default -> "Unknown error";
            };
        };
    }
}
