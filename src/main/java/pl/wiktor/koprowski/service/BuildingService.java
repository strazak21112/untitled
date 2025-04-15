package pl.wiktor.koprowski.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wiktor.koprowski.DTO.BuildingDTO;
import pl.wiktor.koprowski.domain.Apartment;
import pl.wiktor.koprowski.domain.Building;
import pl.wiktor.koprowski.domain.User;
import pl.wiktor.koprowski.repository.BuildingRepository;
import pl.wiktor.koprowski.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final ApartmentService apartmentService;

    public Building createBuilding(BuildingDTO buildingDTO) {
        Building building = new Building();
        building.setAddress(buildingDTO.getAddress());
        building.setNumberOfFloors(buildingDTO.getNumberOfFloors());
        building.updateRates(
                buildingDTO.getElectricityRate(),
                buildingDTO.getColdWaterRate(),
                buildingDTO.getHotWaterRate(),
                buildingDTO.getHeatingRate(),
                buildingDTO.getRentRatePerM2(),
                buildingDTO.getOtherChargesPerM2(),
                buildingDTO.getFlatElectricityRate(),
                buildingDTO.getFlatColdWaterRate(),
                buildingDTO.getFlatHotWaterRate(),
                buildingDTO.getFlatHeatingRate()
        );
        return buildingRepository.save(building);
    }

    // Pobieranie budynku po ID
    public Building getBuildingById(Long id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found"));
    }

     public List<Building> getAllBuildings() {
        return buildingRepository.findAll();
    }

    // Aktualizacja budynku
    public Building updateBuilding(Long id, BuildingDTO buildingDTO) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found"));

        building.setAddress(buildingDTO.getAddress());

        building.updateRates(
                buildingDTO.getElectricityRate(),
                buildingDTO.getColdWaterRate(),
                buildingDTO.getHotWaterRate(),
                buildingDTO.getHeatingRate(),
                buildingDTO.getRentRatePerM2(),
                buildingDTO.getOtherChargesPerM2(),
                buildingDTO.getFlatElectricityRate(),
                buildingDTO.getFlatColdWaterRate(),
                buildingDTO.getFlatHotWaterRate(),
                buildingDTO.getFlatHeatingRate()
        );

        return buildingRepository.save(building);
    }

    // Usuwanie budynku (w tym powiązanych użytkowników i mieszkań)
    @Transactional
    public void deleteBuilding(Long id, String lang) {
        lang = lang.toLowerCase();

        if (!lang.equals("pl") && !lang.equals("en") && !lang.equals("de")) {
            throw new IllegalArgumentException(
                    lang.equals("pl") ? "Nieprawidłowy język" :
                            lang.equals("de") ? "Ungültige Sprache" : "Invalid language"
            );
        }

        String finalLang = lang;
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        finalLang.equals("pl") ? "Budynek nie znaleziony" :
                                finalLang.equals("de") ? "Gebäude nicht gefunden" : "Building not found"));

        // Usuwanie menedżerów budynku
        for (User user : building.getManagers()) {
            user.getManagedBuildings().remove(building);
            userRepository.save(user);
        }

        // Usuwanie mieszkań powiązanych z budynkiem
        for (Apartment apartment : building.getApartments()) {
            apartmentService.deleteApartment(apartment.getId(), lang);
        }

        // Usuwanie budynku
        buildingRepository.delete(building);
    }
}
