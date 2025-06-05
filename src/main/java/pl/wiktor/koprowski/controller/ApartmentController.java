package pl.wiktor.koprowski.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.ApartmentDTO;
import pl.wiktor.koprowski.DTO.ApartmentRowDTO;
import pl.wiktor.koprowski.domain.Apartment;
import pl.wiktor.koprowski.service.ApartmentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/apartments")
@RequiredArgsConstructor
public class ApartmentController {

    private final ApartmentService apartmentService;

    @PostMapping
    public ResponseEntity<Apartment> createApartment(
            @Valid @RequestBody ApartmentDTO apartmentDTO,
            @RequestParam(defaultValue = "pl") String lang) {
        Apartment apartment = apartmentService.createApartment(apartmentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(apartment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Apartment> getApartmentById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pl") String lang) {
        Optional<Apartment> apartment = apartmentService.getApartmentById(id);
        return apartment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    public ResponseEntity<List<Apartment>> getAllApartments() {
        List<Apartment> apartments = apartmentService.getAllApartments();
        return apartments.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(apartments);
    }
    @GetMapping("/{id}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getApartmentDetails(@PathVariable Long id,
                                                                   @RequestParam("lang") String lang) {
        ApartmentDTO details = apartmentService.getApartmentDetails(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", details);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/rows")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllApartmentRows(@RequestParam("lang") String lang) {
        List<ApartmentRowDTO> apartmentRows = apartmentService.getAllApartmentRows();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", apartmentRows);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Apartment> updateApartment(
            @PathVariable Long id,
            @Valid @RequestBody ApartmentDTO apartmentDTO,
            @RequestParam(defaultValue = "pl") String lang) {
        Apartment apartment = apartmentService.updateApartment(id, apartmentDTO);
        return ResponseEntity.ok(apartment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApartment(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pl") String lang) {
        apartmentService.deleteApartment(id);
        return ResponseEntity.noContent().build();
    }
}
