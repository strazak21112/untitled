package pl.wiktor.koprowski.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.basic.ReadingDTO;
import pl.wiktor.koprowski.domain.Reading;
import pl.wiktor.koprowski.service.basic.ReadingService;

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
            Reading reading = readingService.createReading(readingDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(reading);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
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
