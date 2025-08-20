package pl.wiktor.koprowski.service.basic;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wiktor.koprowski.DTO.basic.ApartmentDTO;
import pl.wiktor.koprowski.DTO.inside.AvailableApartment;
import pl.wiktor.koprowski.DTO.row.ApartmentRowDTO;
import pl.wiktor.koprowski.DTO.inside.ApartmentTenantDto;
import pl.wiktor.koprowski.DTO.inside.BuildingInfoDTO;
import pl.wiktor.koprowski.domain.*;
import pl.wiktor.koprowski.repository.*;
import pl.wiktor.koprowski.service.TranslationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReadingRepository readingRepository;
    private final TranslationService translationService;

    @Transactional
    public Apartment createApartment(ApartmentDTO apartmentDTO) {
        Building building = buildingRepository.findById(apartmentDTO.getBuildingInfo().getId())
                .orElseThrow(() -> new RuntimeException("error_building_not_found"));

        if (apartmentDTO.getFloor() > building.getNumberOfFloors() - 1) {
            throw new RuntimeException("error_apartment_invalid_floor");
        }

        boolean exists = building.getApartments().stream()
                .anyMatch(a -> a.getNumber().equalsIgnoreCase(apartmentDTO.getNumber()));
        if (exists) {
            throw new RuntimeException("error_apartment_number_exists");
        }

        Apartment apartment = new Apartment();
        apartment.setNumber(apartmentDTO.getNumber());
        apartment.setArea(apartmentDTO.getArea());
        apartment.setFloor(apartmentDTO.getFloor());
        apartment.setBuilding(building);
        apartment.setTenant(null);

        return apartmentRepository.save(apartment);
    }

    public ApartmentDTO getApartmentDetails(Long id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("error_apartment_not_found"));

        ApartmentDTO dto = new ApartmentDTO();
        dto.setId(apartment.getId());
        dto.setNumber(apartment.getNumber());
        dto.setArea(apartment.getArea());
        dto.setFloor(apartment.getFloor());

        Building building = apartment.getBuilding();
        BuildingInfoDTO buildingInfo = new BuildingInfoDTO();
        buildingInfo.setId(building.getId());
        buildingInfo.setAddress(building.getAddress());
        dto.setBuildingInfo(buildingInfo);

         User tenant = apartment.getTenant();
        if (tenant != null) {
            ApartmentTenantDto tenantDto = new ApartmentTenantDto();
            tenantDto.setId(tenant.getId());
            tenantDto.setFirstName(tenant.getFirstName());
            tenantDto.setLastName(tenant.getLastName());

            dto.setTenant(tenantDto);
        }

        return dto;
    }


    public List<ApartmentRowDTO> getAllApartmentRows() {
        return apartmentRepository.findAll().stream()
                .map(apartment -> new ApartmentRowDTO(
                        apartment.getId(),
                        apartment.getBuilding().getAddress(),
                        apartment.getNumber()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public Apartment updateApartment(Long id, ApartmentDTO apartmentDTO) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("error_apartment_not_found"));

        Building building = apartment.getBuilding();

         boolean apartmentExists = building.getApartments().stream()
                .anyMatch(a -> !a.getId().equals(id) && a.getNumber().equals(apartmentDTO.getNumber()));
        if (apartmentExists) {
            throw new RuntimeException("error_apartment_number_exists");
        }

         apartment.setNumber(apartmentDTO.getNumber());
        apartment.setArea(apartmentDTO.getArea());

        return apartmentRepository.save(apartment);
    }

    @Transactional
    public void deleteApartment(Long id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("error_apartment_not_found"));

         User tenant = apartment.getTenant();
        if (tenant != null) {
            tenant.setApartment(null);
            userRepository.save(tenant);
        }
         Building building = apartment.getBuilding();
        if (building != null) {
            building.getApartments().remove(apartment);
            buildingRepository.save(building);
        }

         List<Reading> readings = apartment.getReadings();
        if (readings != null && !readings.isEmpty()) {
            List<Invoice> invoicesToUpdate = new ArrayList<>();
            for (Reading reading : readings) {
                if (reading.getInvoice() != null) {
                    reading.getInvoice().setReading(null);
                    invoicesToUpdate.add(reading.getInvoice());
                }
            }
            //zmienic niezatwierdzonym fakturą na ryczałt przy zatwierdzonych zostają kwoty i  pomiary.
            //Przy zmianie rycząłtu bad stawek dla budynku to przy zatwierdzonym nic sie nie zmienia.
            if (!invoicesToUpdate.isEmpty()) {
                invoiceRepository.saveAll(invoicesToUpdate);
            }
            readingRepository.deleteAll(readings);
        }

        apartmentRepository.delete(apartment);
    }


    public List<AvailableApartment> getAvailableApartments() {
        return apartmentRepository.findByTenantIsNull()
                .stream()
                .map(apartment -> new AvailableApartment(
                        apartment.getId(),
                        apartment.getFloor(),
                        apartment.getNumber(),
                        apartment.getBuilding().getAddress()
                ))
                .collect(Collectors.toList());
    }

}
