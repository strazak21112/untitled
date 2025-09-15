package pl.wiktor.koprowski.service.basic;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wiktor.koprowski.DTO.inside.BuildingApartmentDto;
import pl.wiktor.koprowski.DTO.basic.BuildingDTO;
import pl.wiktor.koprowski.DTO.inside.BuildingInfoDTO;
import pl.wiktor.koprowski.DTO.inside.BuildingManagerDto;
import pl.wiktor.koprowski.DTO.row.BuildingRowDTO;
import pl.wiktor.koprowski.domain.*;
import pl.wiktor.koprowski.repository.BuildingRepository;
import pl.wiktor.koprowski.repository.InvoiceRepository;
import pl.wiktor.koprowski.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final ApartmentService apartmentService;
    private final InvoiceRepository invoiceRepository;

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

    @Transactional
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

        for (Apartment apartment : building.getApartments()) {
            List<Invoice> invoices = invoiceRepository.findByApartmentAndConfirmedFalse(apartment);
            for (Invoice invoice : invoices) {

                invoice.setRentAmount(round2(apartment.getArea() * building.getRentRatePerM2()));
                invoice.setOtherCharges(round2(apartment.getArea() * building.getOtherChargesPerM2()));

                Reading reading = invoice.getReading();
                double totalMediaAmount;
                if (reading != null) {
                    totalMediaAmount = (reading.getColdWaterValue() * building.getColdWaterRate()) +
                            (reading.getHotWaterValue() * building.getHotWaterRate()) +
                            (reading.getElectricityValue() * building.getElectricityRate()) +
                            (reading.getHeatingValue() * building.getHeatingRate());
                    invoice.setFlat(false);
                } else {
                    totalMediaAmount = building.getFlatColdWaterRate() +
                            building.getFlatHotWaterRate() +
                            building.getFlatElectricityRate() +
                            building.getFlatHeatingRate();
                    invoice.setFlat(true);
                }

                invoice.setTotalMediaAmount(round2(totalMediaAmount));
                invoice.setTotalAmount(round2(invoice.getRentAmount() + invoice.getOtherCharges() + totalMediaAmount));

                InvoiceInfo info = invoice.getInfo();
                info.setAddress(building.getAddress());
                info.setElectricityRate(building.getElectricityRate());
                info.setColdWaterRate(building.getColdWaterRate());
                info.setHotWaterRate(building.getHotWaterRate());
                info.setHeatingRate(building.getHeatingRate());
                info.setRentRatePerM2(building.getRentRatePerM2());
                info.setOtherChargesPerM2(building.getOtherChargesPerM2());
                info.setFlatElectricityRate(building.getFlatElectricityRate());
                info.setFlatColdWaterRate(building.getFlatColdWaterRate());
                info.setFlatHotWaterRate(building.getFlatHotWaterRate());
                info.setFlatHeatingRate(building.getFlatHeatingRate());

                invoice.setInfo(info);
            }
            invoiceRepository.saveAll(invoices);
        }

        return buildingRepository.save(building);
    }

     private double round2(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }


    @Transactional
    public void deleteBuilding(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("error_building_not_found"));

         for (User user : building.getManagers()) {
            user.getManagedBuilding().remove(building);
            userRepository.save(user);
        }

         List<Apartment> apartmentsToDelete = new ArrayList<>(building.getApartments());
        for (Apartment apartment : apartmentsToDelete) {
            apartmentService.deleteApartment(apartment.getId());
        }

         buildingRepository.delete(building);
    }


    public List<BuildingInfoDTO> getAllBuildingInfo() {
        return buildingRepository.findAll().stream()
                .filter(building -> building.getManagers() != null && !building.getManagers().isEmpty())
                .map(building -> new BuildingInfoDTO(building.getId(), building.getAddress()))
                .collect(Collectors.toList());
    }


    public List<BuildingInfoDTO> getAllBuildings() {
        return buildingRepository.findAll()
                .stream()
                .map(building -> new BuildingInfoDTO(building.getId(), building.getAddress()))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BuildingInfoDTO> getBuildingsByManagerEmail(String email) {
         User manager = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("error_user_not_found"));

         List<Building> buildings = manager.getManagedBuilding();

         return buildings.stream()
                .map(b -> new BuildingInfoDTO(b.getId(), b.getAddress()))
                .collect(Collectors.toList());
    }







}
