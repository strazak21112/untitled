package pl.wiktor.koprowski.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.BuildingDTO;
import pl.wiktor.koprowski.domain.Building;
import pl.wiktor.koprowski.service.BuildingService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    // Tworzenie nowego budynku (TYLKO ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createBuilding(@Valid @RequestBody BuildingDTO buildingDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            Building building = buildingService.createBuilding(buildingDTO);
            response.put("status", "success");
            response.put("message", "Building created successfully");
            response.put("buildingId", building.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create building: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Pobieranie budynku po ID (Dostęp dla KAŻDEGO zalogowanego użytkownika)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getBuildingById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Building building = buildingService.getBuildingById(id);
            response.put("status", "success");
            response.put("data", building);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            response.put("status", "error");
            response.put("message", "Building not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // Pobieranie wszystkich budynków (Dostęp dla KAŻDEGO zalogowanego użytkownika)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAllBuildings() {
        Map<String, Object> response = new HashMap<>();
        List<Building> buildings = buildingService.getAllBuildings();
        if (buildings.isEmpty()) {
            response.put("status", "error");
            response.put("message", "No buildings found");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        } else {
            response.put("status", "success");
            response.put("data", buildings);
            return ResponseEntity.ok(response);
        }
    }

    // Aktualizacja danych budynku (TYLKO ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateBuilding(@PathVariable Long id, @Valid @RequestBody BuildingDTO buildingDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            Building updatedBuilding = buildingService.updateBuilding(id, buildingDTO);
            response.put("status", "success");
            response.put("message", "Building updated successfully");
            response.put("buildingId", updatedBuilding.getId());
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            response.put("status", "error");
            response.put("message", "Building not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update building: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Usuwanie budynku (TYLKO ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteBuilding(@PathVariable Long id, @RequestParam("lang") String lang) {
        Map<String, Object> response = new HashMap<>();
        try {
            buildingService.deleteBuilding(id, lang);
            response.put("status", "success");
            response.put("message", "Building deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            response.put("status", "error");
            response.put("message", "Building not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", "Invalid language parameter");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete building: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
