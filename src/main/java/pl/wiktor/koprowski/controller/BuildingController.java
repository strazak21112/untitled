package pl.wiktor.koprowski.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.BuildingDTO;
import pl.wiktor.koprowski.DTO.BuildingRowDTO;
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createBuilding(@Valid @RequestBody BuildingDTO buildingDTO,
                                                              @RequestParam("lang") String lang) {
        Building building = buildingService.createBuilding(buildingDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("buildingId", building.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBuildingById(@PathVariable Long id,
                                                               @RequestParam("lang") String lang) {
        Building building = buildingService.getBuildingById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", building);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllBuildings(@RequestParam("lang") String lang) {
        List<Building> buildings = buildingService.getAllBuildings();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", buildings);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rows")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllBuildingRows(@RequestParam("lang") String lang) {
        List<BuildingRowDTO> buildingRows = buildingService.getAllBuildingRows();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", buildingRows);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBuildingDetails(@PathVariable Long id,
                                                                  @RequestParam("lang") String lang) {
        BuildingDTO details = buildingService.getBuildingDetails(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", details);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateBuilding(@PathVariable Long id,
                                                              @Valid @RequestBody BuildingDTO buildingDTO,
                                                              @RequestParam("lang") String lang) {
        Building updatedBuilding = buildingService.updateBuilding(id, buildingDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("buildingId", updatedBuilding.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBuilding(@PathVariable Long id,
                                               @RequestParam("lang") String lang) {
        buildingService.deleteBuilding(id);
        return ResponseEntity.noContent().build();
    }
}
