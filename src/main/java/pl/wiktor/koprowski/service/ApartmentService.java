package pl.wiktor.koprowski.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wiktor.koprowski.DTO.ApartmentDTO;
import pl.wiktor.koprowski.domain.*;
import pl.wiktor.koprowski.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReadingRepository readingRepository;

    @Transactional
    public Apartment createApartment(ApartmentDTO apartmentDTO, String lang) {
        lang = lang.toLowerCase();

        if (!lang.equals("pl") && !lang.equals("en") && !lang.equals("de")) {
            throw new IllegalArgumentException(
                    lang.equals("pl") ? "Nieprawidłowy język" :
                            lang.equals("de") ? "Ungültige Sprache" : "Invalid language"
            );
        }

        String finalLang = lang;
        Building building = buildingRepository.findById(apartmentDTO.getBuildingId())
                .orElseThrow(() -> new IllegalArgumentException(
                        finalLang.equals("pl") ? "Budynek nie znaleziony" :
                                finalLang.equals("de") ? "Gebäude nicht gefunden" : "Building not found"
                ));

        if (apartmentDTO.getFloor() > building.getNumberOfFloors() - 1) {
            throw new IllegalArgumentException(
                    lang.equals("pl") ? "Budynek ma tylko " + building.getNumberOfFloors() + " pięter, nie możesz przypisać mieszkania do tego piętra" :
                            lang.equals("de") ? "Das Gebäude hat nur " + building.getNumberOfFloors() + " Etagen, du kannst die Wohnung nicht auf dieses Etage zuweisen" :
                                    "The building has only " + building.getNumberOfFloors() + " floors, you cannot assign the apartment to this floor"
            );
        }

        boolean apartmentExists = building.getApartments().stream()
                .anyMatch(apartment -> apartment.getNumber().equals(apartmentDTO.getNumber()));
        if (apartmentExists) {
            throw new IllegalArgumentException(
                    lang.equals("pl") ? "Mieszkanie o tym numerze już istnieje w tym budynku" :
                            lang.equals("de") ? "Wohnung mit dieser Nummer existiert bereits im Gebäude" :
                                    "Apartment with this number already exists in the building"
            );
        }

         User tenant = null;

        Apartment apartment = new Apartment();
        apartment.setNumber(apartmentDTO.getNumber());
        apartment.setArea(apartmentDTO.getArea());
        apartment.setBuilding(building);
        apartment.setTenant(tenant);
        apartment.setFloor(apartmentDTO.getFloor());

        apartment = apartmentRepository.save(apartment);

         building.getApartments().add(apartment);
        buildingRepository.save(building);

        return apartment;
    }


    public Optional<Apartment> getApartmentById(Long id) {
        return apartmentRepository.findById(id);
    }

    public List<Apartment> getAllApartments() {
        return apartmentRepository.findAll();
    }

    @Transactional
    public Apartment updateApartment(Long id, ApartmentDTO apartmentDTO, String lang) {
        lang = lang.toLowerCase();
        if (!lang.equals("pl") && !lang.equals("en") && !lang.equals("de")) {
            throw new IllegalArgumentException(
                    "pl".equals(lang) ? "Nieprawidłowy język" :
                            "de".equals(lang) ? "Ungültige Sprache" :
                                    "Invalid language"
            );
        }

        String finalLang = lang;
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "pl".equals(finalLang) ? "Mieszkanie nie znalezione" :
                                "de".equals(finalLang) ? "Wohnung nicht gefunden" :
                                        "Apartment not found"
                ));

        String finalLang1 = lang;
        Building building = buildingRepository.findById(apartmentDTO.getBuildingId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "pl".equals(finalLang1) ? "Budynek nie znaleziony" :
                                "de".equals(finalLang1) ? "Gebäude nicht gefunden" :
                                        "Building not found"
                ));

        if (building.getNumberOfFloors() < apartmentDTO.getFloor()) {
            throw new IllegalArgumentException(
                    "pl".equals(lang) ? "Budynek nie ma wystarczającej liczby pięter" :
                            "de".equals(lang) ? "Das Gebäude hat nicht genügend Stockwerke" :
                                    "Building doesn't have enough floors"
            );
        }

        boolean apartmentExists = building.getApartments().stream()
                .anyMatch(a -> !a.getId().equals(id) && a.getNumber().equals(apartmentDTO.getNumber()));
        if (apartmentExists) {
            throw new IllegalArgumentException(
                    "pl".equals(lang) ? "Mieszkanie o tym numerze już istnieje w tym budynku" :
                            "de".equals(lang) ? "Diese Wohnung existiert bereits im Gebäude" :
                                    "An apartment with this number already exists in the building"
            );
        }

        User currentTenant = apartment.getTenant();

        // Jeśli tenantId jest null w DTO, to usuwamy relację
        if (apartmentDTO.getTenantId() == null) {
            // Zerwij relację z użytkownikiem, jeśli taki był przypisany
            if (currentTenant != null) {
                currentTenant.setApartment(null);
                userRepository.save(currentTenant);
            }

            // Zerwij relację z mieszkaniem
            apartment.setTenant(null);
        } else {
            // W przypadku, gdy tenantId jest obecny w DTO, przypisujemy nowego użytkownika
            String finalLang2 = lang;
            User newTenant = userRepository.findById(apartmentDTO.getTenantId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "pl".equals(finalLang2) ? "Użytkownik nie znaleziony" :
                                    "de".equals(finalLang2) ? "Benutzer nicht gefunden" :
                                            "User not found"
                    ));

            // Sprawdzenie, czy nowy użytkownik nie ma już przypisanego mieszkania
            if (newTenant.getApartment() != null) {
                throw new IllegalArgumentException(
                        "pl".equals(lang) ? "Ten użytkownik już ma przypisane mieszkanie" :
                                "de".equals(lang) ? "Dieser Benutzer hat bereits eine Wohnung" :
                                        "This user already has an assigned apartment"
                );
            }

            // Zerwij relację z obecnym najemcą, jeśli taki istnieje
            if (currentTenant != null) {
                currentTenant.setApartment(null);
                userRepository.save(currentTenant);
            }

            // Przypisz nowego najemcę do mieszkania
            apartment.setTenant(newTenant);
            newTenant.setApartment(apartment);
        }

        // Aktualizowanie szczegółów mieszkania
        apartment.setNumber(apartmentDTO.getNumber());
        apartment.setArea(apartmentDTO.getArea());
        apartment.setFloor(apartmentDTO.getFloor());

        return apartmentRepository.save(apartment);
    }


    @Transactional
    public void deleteApartment(Long id, String lang) {
        lang = lang.toLowerCase();
        if (!lang.equals("pl") && !lang.equals("en") && !lang.equals("de")) {
            throw new IllegalArgumentException(
                    "pl".equals(lang) ? "Nieprawidłowy język" :
                            "de".equals(lang) ? "Ungültige Sprache" :
                                    "Invalid language"
            );
        }

        String finalLang = lang;
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "pl".equals(finalLang) ? "Mieszkanie nie znalezione" :
                                "de".equals(finalLang) ? "Wohnung nicht gefunden" :
                                        "Apartment not found"
                ));

        // Zerwanie relacji z użytkownikiem
        User tenant = apartment.getTenant();
        if (tenant != null) {
            tenant.setApartment(null);
            userRepository.save(tenant);
        }

        // Zerwanie relacji z budynkiem
        Building building = apartment.getBuilding();
        if (building != null) {
            building.getApartments().remove(apartment);
            buildingRepository.save(building);
        }

        // Pobranie wszystkich odczytów i usunięcie powiązań z fakturami
        List<Reading> readings = apartment.getReadings();
        if (readings != null && !readings.isEmpty()) {
            List<Invoice> invoicesToUpdate = new ArrayList<>();
            for (Reading reading : readings) {
                if (reading.getInvoice() != null) {
                    reading.getInvoice().setReading(null);
                    invoicesToUpdate.add(reading.getInvoice());
                }
            }
            if (!invoicesToUpdate.isEmpty()) {
                invoiceRepository.saveAll(invoicesToUpdate);
            }
            // Usunięcie wszystkich odczytów
            readingRepository.deleteAll(readings);
        }

        apartmentRepository.delete(apartment);
    }



}
