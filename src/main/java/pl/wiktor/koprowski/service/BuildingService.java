package pl.wiktor.koprowski.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wiktor.koprowski.DTO.BuildingApartmentDto;
import pl.wiktor.koprowski.DTO.BuildingDTO;
import pl.wiktor.koprowski.DTO.BuildingManagerDto;
import pl.wiktor.koprowski.DTO.BuildingRowDTO;
import pl.wiktor.koprowski.domain.Apartment;
import pl.wiktor.koprowski.domain.Building;
import pl.wiktor.koprowski.domain.User;
import pl.wiktor.koprowski.repository.BuildingRepository;
import pl.wiktor.koprowski.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final ApartmentService apartmentService;

    public Building createBuilding(BuildingDTO buildingDTO) {
        if (buildingRepository.existsByAddress(buildingDTO.getAddress())) {
            throw new RuntimeException("error_building_address_already_exists");
        }

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


    public Building getBuildingById(Long id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("error_building_not_found"));
    }

    public List<Building> getAllBuildings() {
        return buildingRepository.findAll();
    }

    public List<BuildingRowDTO> getAllBuildingRows() {
        return buildingRepository.findAll().stream()
                .map(building -> {
                    BuildingRowDTO dto = new BuildingRowDTO();
                    dto.setId(building.getId());
                    dto.setAddress(building.getAddress());
                    dto.setNumberOfFloors(building.getNumberOfFloors());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public BuildingDTO getBuildingDetails(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("error_building_not_found"));

        BuildingDTO dto = new BuildingDTO();
        dto.setId(building.getId());
        dto.setAddress(building.getAddress());
        dto.setNumberOfFloors(building.getNumberOfFloors());
        dto.setElectricityRate(building.getElectricityRate());
        dto.setColdWaterRate(building.getColdWaterRate());
        dto.setHotWaterRate(building.getHotWaterRate());
        dto.setHeatingRate(building.getHeatingRate());
        dto.setRentRatePerM2(building.getRentRatePerM2());
        dto.setOtherChargesPerM2(building.getOtherChargesPerM2());
        dto.setFlatElectricityRate(building.getFlatElectricityRate());
        dto.setFlatColdWaterRate(building.getFlatColdWaterRate());
        dto.setFlatHotWaterRate(building.getFlatHotWaterRate());
        dto.setFlatHeatingRate(building.getFlatHeatingRate());

        List<BuildingApartmentDto> apartmentDtos = building.getApartments().stream()
                .map(apartment -> new BuildingApartmentDto(
                        apartment.getId(),
                        apartment.getFloor(),
                        apartment.getNumber()
                ))
                .collect(Collectors.toList());
        dto.setApartments(apartmentDtos);

        List<BuildingManagerDto> managerDtos = building.getManagers().stream()
                .map(manager -> new BuildingManagerDto(
                        manager.getId(),
                        manager.getFirstName(),
                        manager.getLastName()
                ))
                .collect(Collectors.toList());
        dto.setManagers(managerDtos);

        return dto;
    }

    public Building updateBuilding(Long id, BuildingDTO buildingDTO) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("error_building_not_found"));

         if (!building.getAddress().equals(buildingDTO.getAddress()) &&
                buildingRepository.existsByAddress(buildingDTO.getAddress())) {
            throw new RuntimeException("error_building_address_already_exists");
        }

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


    @Transactional
    public void deleteBuilding(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("error_building_not_found"));

        for (User user : building.getManagers()) {
            user.getManagedBuildings().remove(building);
            userRepository.save(user);
        }

        for (Apartment apartment : building.getApartments()) {
            apartmentService.deleteApartment(apartment.getId());
        }

        buildingRepository.delete(building);
    }
}
