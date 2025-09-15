package pl.wiktor.koprowski.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.basic.ReadingDTO;
import pl.wiktor.koprowski.DTO.inside.ReadingDetails;
import pl.wiktor.koprowski.DTO.row.ReadingRowDTO;
import pl.wiktor.koprowski.domain.Reading;
import pl.wiktor.koprowski.service.TranslationService;
import pl.wiktor.koprowski.service.basic.ReadingService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
public class ReadingController {

    private final ReadingService readingService;
    private final TranslationService translationService;


    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createReading(
            @Valid @RequestBody ReadingDTO readingDTO,
            @RequestParam(defaultValue = "pl") String lang) {

        Reading reading = readingService.createReading(readingDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("readingId", reading.getId());
        response.put("message", translationService.getTranslation("reading_created_success", lang));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateReading(
            @PathVariable Long id,
            @Valid @RequestBody ReadingDTO readingDTO,
            @RequestParam(defaultValue = "pl") String lang) {

        Reading updatedReading = readingService.updateReading(id, readingDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("readingId", updatedReading.getId());
        response.put("message", translationService.getTranslation("reading_updated_success", lang));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteReading(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pl") String lang) {

        readingService.deleteReading(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", translationService.getTranslation("reading_deleted_success", lang));

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/rows", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllReadingRows(
            @RequestParam(defaultValue = "pl") String lang) {

        List<ReadingRowDTO> readingRows = readingService.getAllReadingRows();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", readingRows);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')  or hasRole('USER')" )
    public ResponseEntity<Map<String, Object>> getReadingDetails(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pl") String lang) {

        ReadingDetails details = readingService.getReadingDetails(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", details);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/rows/by-tenant", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getReadingRowsByTenantEmail(
            @RequestParam String tenantEmail,
            @RequestParam(defaultValue = "pl") String lang) {

        List<ReadingRowDTO> readingRows = readingService.getReadingRowsByTenantEmail(tenantEmail);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", readingRows);
        response.put("message", translationService.getTranslation("reading_rows_fetched_success", lang));

        return ResponseEntity.ok(response);
    }



}
