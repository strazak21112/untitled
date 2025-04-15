package pl.wiktor.koprowski.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.ReadingDTO;
import pl.wiktor.koprowski.domain.Reading;
import pl.wiktor.koprowski.service.ReadingService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
public class ReadingController {

    private final ReadingService readingService;

    @PostMapping
    public ResponseEntity<?> createReading(@Valid @RequestBody ReadingDTO readingDTO,
                                           @RequestParam(defaultValue = "en") String lang) {
        try {
            Reading reading = readingService.createReading(readingDTO, lang);
            return ResponseEntity.status(HttpStatus.CREATED).body(reading);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReadingById(@PathVariable Long id,
                                            @RequestParam(defaultValue = "en") String lang) {
        Optional<Reading> reading = readingService.getReadingById(id);
        if (reading.isPresent()) {
            return ResponseEntity.ok(reading.get());
        } else {
            String message = lang.equals("pl") ? "Odczyt nie znaleziony" :
                    lang.equals("de") ? "Ablesung nicht gefunden" :
                            "Reading not found";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }


    @GetMapping
    public ResponseEntity<List<Reading>> getAllReadings() {
        List<Reading> readings = readingService.getAllReadings();
        return readings.isEmpty() ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                : ResponseEntity.ok(readings);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReading(@PathVariable Long id,
                                           @Valid @RequestBody ReadingDTO readingDTO,
                                           @RequestParam(defaultValue = "en") String lang) {
        try {
            Reading updatedReading = readingService.updateReading(id, readingDTO, lang);
            return ResponseEntity.ok(updatedReading);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReading(@PathVariable Long id,
                                           @RequestParam(defaultValue = "en") String lang) {
        try {
            readingService.deleteReading(id, lang);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
