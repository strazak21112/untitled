package pl.wiktor.koprowski.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.basic.ApartmentDTO;
import pl.wiktor.koprowski.DTO.inside.AvailableApartment;
import pl.wiktor.koprowski.DTO.row.ApartmentRowDTO;
import pl.wiktor.koprowski.domain.Apartment;
import pl.wiktor.koprowski.service.TranslationService;
import pl.wiktor.koprowski.service.basic.ApartmentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/apartments")
@RequiredArgsConstructor
public class ApartmentController {

    private final ApartmentService apartmentService;
    private final TranslationService translationService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createApartment(
            @Valid @RequestBody ApartmentDTO apartmentDTO,
            @RequestParam(defaultValue = "pl") String lang) {

        Apartment apartment = apartmentService.createApartment(apartmentDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("apartmentId", apartment.getId());
        response.put("message", translationService.getTranslation("apartment_created_success", lang));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateApartment(
            @PathVariable Long id,
            @Valid @RequestBody ApartmentDTO apartmentDTO,
            @RequestParam(defaultValue = "pl") String lang) {

        Apartment updatedApartment = apartmentService.updateApartment(id, apartmentDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("apartmentId", updatedApartment.getId());
        response.put("message", translationService.getTranslation("apartment_updated_success", lang));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteApartment(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pl") String lang) {

        apartmentService.deleteApartment(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", translationService.getTranslation("apartment_deleted_success", lang));
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/rows", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllApartmentRows(
            @RequestParam(defaultValue = "pl") String lang) {

        List<ApartmentRowDTO> apartmentRows = apartmentService.getAllApartmentRows();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", apartmentRows);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getApartmentDetails(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pl") String lang) {

        ApartmentDTO details = apartmentService.getApartmentDetails(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", details);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/available", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAvailableApartments(
            @RequestParam(defaultValue = "pl") String lang) {

        List<AvailableApartment> availableApartments = apartmentService.getAvailableApartments();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", availableApartments);
        return ResponseEntity.ok(response);
    }


}
