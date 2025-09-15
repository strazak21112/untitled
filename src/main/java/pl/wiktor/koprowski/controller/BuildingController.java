package pl.wiktor.koprowski.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.basic.BuildingDTO;
import pl.wiktor.koprowski.DTO.inside.BuildingInfoDTO;
import pl.wiktor.koprowski.DTO.row.BuildingRowDTO;
import pl.wiktor.koprowski.domain.Building;
import pl.wiktor.koprowski.service.basic.BuildingService;
import pl.wiktor.koprowski.service.TranslationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;
    private final TranslationService translationService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createBuilding(@Valid @RequestBody BuildingDTO buildingDTO,
                                                              @RequestParam("lang") String lang) {
        Building building = buildingService.createBuilding(buildingDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("buildingId", building.getId());
        response.put("message", translationService.getTranslation("building_created_success", lang));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = "/rows", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllBuildingRows(@RequestParam("lang") String lang) {
        List<BuildingRowDTO> buildingRows = buildingService.getAllBuildingRows();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", buildingRows);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBuildingDetails(@PathVariable Long id,
                                                                  @RequestParam("lang") String lang) {
        BuildingDTO details = buildingService.getBuildingDetails(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", details);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateBuilding(@PathVariable Long id,
                                                              @Valid @RequestBody BuildingDTO buildingDTO,
                                                              @RequestParam("lang") String lang) {
        Building updatedBuilding = buildingService.updateBuilding(id, buildingDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("buildingId", updatedBuilding.getId());
        response.put("message", translationService.getTranslation("building_updated_success", lang));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteBuilding(@PathVariable Long id,
                                                              @RequestParam("lang") String lang) {
        buildingService.deleteBuilding(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", translationService.getTranslation("building_deleted_success", lang));
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllBuildingInfo(@RequestParam("lang") String lang) {
        List<BuildingInfoDTO> buildingInfos = buildingService.getAllBuildingInfo();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", buildingInfos);
        return ResponseEntity.ok(response);
    }
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllBuildings(
            @RequestParam(defaultValue = "pl") String lang) {

        List<BuildingInfoDTO> buildings = buildingService.getAllBuildings();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", buildings);

        return ResponseEntity.ok(response);
    }
    @GetMapping(value = "/manager", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getBuildingsByManagerEmail(
            @RequestParam("email") String email,
            @RequestParam(defaultValue = "pl") String lang) {

        List<BuildingInfoDTO> buildings = buildingService.getBuildingsByManagerEmail(email);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", buildings);



        return ResponseEntity.ok(response);
    }



}
