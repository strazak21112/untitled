package pl.wiktor.koprowski.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import pl.wiktor.koprowski.domain.Invoice;
import pl.wiktor.koprowski.domain.User;
import pl.wiktor.koprowski.repository.UserRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    private final UserRepository userRepository;

    public PdfService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void generateInvoicePdf(Invoice invoice, HttpServletResponse response, String lang) {
        lang = lang.toLowerCase();

        // Tłumaczenia etykiet
        String invoiceLabel = switch (lang) {
            case "en" -> "Invoice";
            case "de" -> "Rechnung";
            default -> "Faktura";
        };

        String userDataLabel = switch (lang) {
            case "en" -> "User Data";
            case "de" -> "Benutzerdaten";
            default -> "Dane użytkownika";
        };

        String firstNameLabel = switch (lang) {
            case "en" -> "First Name";
            case "de" -> "Vorname";
            default -> "Imię";
        };

        String lastNameLabel = switch (lang) {
            case "en" -> "Last Name";
            case "de" -> "Nachname";
            default -> "Nazwisko";
        };

        String emailLabel = switch (lang) {
            case "en" -> "Email";
            case "de" -> "E-Mail";
            default -> "Email";
        };

        String phoneLabel = switch (lang) {
            case "en" -> "Phone";
            case "de" -> "Telefon";
            default -> "Telefon";
        };

        String managerDataLabel = switch (lang) {
            case "en" -> "Manager Data";
            case "de" -> "Verwalterdaten";
            default -> "Dane zarządcy";
        };

        String buildingDataLabel = switch (lang) {
            case "en" -> "Building & Apartment Data";
            case "de" -> "Gebäude- und Wohnungsdaten";
            default -> "Dane budynku i apartamentu";
        };

        String addressLabel = switch (lang) {
            case "en" -> "Address";
            case "de" -> "Adresse";
            default -> "Adres";
        };

        String apartmentNumberLabel = switch (lang) {
            case "en" -> "Apartment Number";
            case "de" -> "Wohnungsnummer";
            default -> "Numer apartamentu";
        };

        String floorNumberLabel = switch (lang) {
            case "en" -> "Floor Number";
            case "de" -> "Stockwerk";
            default -> "Numer piętra";
        };

        String readingsLabel = switch (lang) {
            case "en" -> "Meter Readings";
            case "de" -> "Zählerstände";
            default -> "Pomiary";
        };

        String coldWaterLabel = switch (lang) {
            case "en" -> "Cold Water";
            case "de" -> "Kaltwasser";
            default -> "Zimna woda";
        };

        String hotWaterLabel = switch (lang) {
            case "en" -> "Hot Water";
            case "de" -> "Heißwasser";
            default -> "Ciepła woda";
        };

        String heatingLabel = switch (lang) {
            case "en" -> "Heating";
            case "de" -> "Heizung";
            default -> "Ogrzewanie";
        };

        String electricityLabel = switch (lang) {
            case "en" -> "Electricity";
            case "de" -> "Elektrizität";
            default -> "Elektryczność";
        };

        String measurementDateLabel = switch (lang) {
            case "en" -> "Measurement Date";
            case "de" -> "Messdatum";
            default -> "Data pomiaru";
        };

        String billingPeriodLabel = switch (lang) {
            case "en" -> "Billing Period";
            case "de" -> "Abrechnungszeitraum";
            default -> "Okres pomiaru";
        };

        String chargesLabel = switch (lang) {
            case "en" -> "Charges";
            case "de" -> "Gebühren";
            default -> "Opłaty";
        };

        String rentAmountLabel = switch (lang) {
            case "en" -> "Rent Amount";
            case "de" -> "Mietbetrag";
            default -> "Kwota czynszu";
        };

        String otherChargesLabel = switch (lang) {
            case "en" -> "Other Charges";
            case "de" -> "Sonstige Gebühren";
            default -> "Inne opłaty";
        };



        String totalAmountLabel = switch (lang) {
            case "en" -> "Total Amount";
            case "de" -> "Gesamtbetrag";
            default -> "Łączna kwota";
        };

        String issueDateLabel = switch (lang) {
            case "en" -> "Issue Date";
            case "de" -> "Ausgabedatum";
            default -> "Data wystawienia";
        };


        String paidLabel = switch (lang) {
            case "en" -> "Paid"; // Tłumaczenie "Paid" na angielski
            case "de" -> "Bezahlt"; // Tłumaczenie "Paid" na niemiecki
            default -> "Opłacona"; // Tłumaczenie "Paid" na polski
        };

// Tłumaczenie statusu płatności ("Yes"/"No")
        String paidStatus = switch (lang) {
            case "en" -> invoice.isPaid() ? "Yes" : "No"; // Tłumaczenie na angielski
            case "de" -> invoice.isPaid() ? "Ja" : "Nein"; // Tłumaczenie na niemiecki
            default -> invoice.isPaid() ? "Tak" : "Nie"; // Tłumaczenie na polski
        };


        try {
            OutputStream outputStream = response.getOutputStream();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=" + invoiceLabel + "_" + invoice.getId() + ".pdf");

            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Nagłówek faktury
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            document.add(new Paragraph(invoiceLabel + " #" + invoice.getId(), titleFont));
            document.add(new Paragraph("\n"));

            // Dane użytkownika
            document.add(new Paragraph(userDataLabel + ":", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            document.add(new Paragraph(firstNameLabel + ": " + invoice.getTenant().getFirstName()));
            document.add(new Paragraph(lastNameLabel + ": " + invoice.getTenant().getLastName()));
            document.add(new Paragraph(emailLabel + ": " + invoice.getTenant().getEmail()));
            document.add(new Paragraph(phoneLabel + ": " + invoice.getTenant().getTelephone()));
            document.add(new Paragraph("\n"));

             document.add(new Paragraph(buildingDataLabel + ":", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

            // Sprawdzenie, czy apartament istnieje
            if (invoice.getApartment() != null) {
                // Sprawdzenie, czy budynek istnieje
                if (invoice.getApartment().getBuilding() != null) {
                    document.add(new Paragraph(addressLabel + ": " + invoice.getApartment().getBuilding().getAddress()));
                    document.add(new Paragraph(apartmentNumberLabel + ": " + invoice.getApartment().getNumber()));
                    document.add(new Paragraph(floorNumberLabel + ": " + invoice.getApartment().getFloor()));

                } else {
                    document.add(new Paragraph(addressLabel + ": N/A"));
                    document.add(new Paragraph(apartmentNumberLabel + ": N/A"));
                    document.add(new Paragraph(floorNumberLabel + ": N/A"));
                }
            } else {
                document.add(new Paragraph(addressLabel + ": N/A"));
                document.add(new Paragraph(apartmentNumberLabel + ": N/A"));
                document.add(new Paragraph(floorNumberLabel + ": N/A"));
            }

            document.add(new Paragraph("\n"));

            document.add(new Paragraph(managerDataLabel + ":", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            if (invoice.getApartment() != null && invoice.getApartment().getBuilding() != null && !invoice.getApartment().getBuilding().getManagers().isEmpty()) {
                var manager = invoice.getApartment().getBuilding().getManagers().get(0);
                document.add(new Paragraph(firstNameLabel + ": " + manager.getFirstName()));
                document.add(new Paragraph(lastNameLabel + ": " + manager.getLastName()));
                document.add(new Paragraph(emailLabel + ": " + manager.getEmail()));
            } else {
                document.add(new Paragraph(firstNameLabel + ": N/A"));
                document.add(new Paragraph(lastNameLabel + ": N/A"));
                document.add(new Paragraph(emailLabel + ": N/A"));
            }

            document.add(new Paragraph("\n"));

             document.add(new Paragraph(readingsLabel + ":", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

             if (invoice.getReading() != null) {
                 document.add(new Paragraph(coldWaterLabel + ": " + invoice.getReading().getColdWaterValue() + " m³"));
                document.add(new Paragraph(hotWaterLabel + ": " + invoice.getReading().getHotWaterValue() + " m³"));
                document.add(new Paragraph(heatingLabel + ": " + invoice.getReading().getHeatingValue() + " GJ"));
                document.add(new Paragraph(electricityLabel + ": " + invoice.getReading().getElectricityValue() + " kWh"));

                 String measurementDate =invoice.getReading().getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

                document.add(new Paragraph(measurementDateLabel + ": " + measurementDate));

                 String billingPeriod =  invoice.getReading().getBillingStartDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "/"
                        + invoice.getReading().getBillingEndDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

                document.add(new Paragraph(billingPeriodLabel + ": " + billingPeriod));
            } else {
                document.add(new Paragraph(coldWaterLabel + ": N/A"));
                document.add(new Paragraph(hotWaterLabel + ": N/A"));
                document.add(new Paragraph(heatingLabel + ": N/A"));
                document.add(new Paragraph(electricityLabel + ": N/A"));
                document.add(new Paragraph(measurementDateLabel + ": N/A"));
                document.add(new Paragraph(billingPeriodLabel + ": N/A"));
            }

            document.add(new Paragraph("\n"));



           document.add(new Paragraph(chargesLabel + ":", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));


            document.add(new Paragraph(rentAmountLabel + ": " + invoice.getRentAmount() + " PLN"));
            document.add(new Paragraph(otherChargesLabel + ": " + invoice.getOtherCharges() + " PLN"));
            String mediaAmountLabel = invoice.getReading() != null ? switch (lang) {
                case "en" -> "Media Charges"; // Opłata za media w języku angielskim
                case "de" -> "Mediengebühren"; // Opłata za media w języku niemieckim
                default -> "Opłata za media";  // Opłata za media w języku polskim
            } : switch (lang) {
                case "en" -> "Media Lump Sum";  // Ryczałt za media w języku angielskim
                case "de" -> "Pauschalbetrag für Medien";  // Ryczałt za media w języku niemieckim
                default -> "Ryczałt za media";  // Ryczałt za media w języku polskim
            };
            document.add(new Paragraph(mediaAmountLabel + ": " + invoice.getTotalMediaAmount() + " PLN"));

            document.add(new Paragraph(totalAmountLabel + ": " + invoice.getTotalAmount() + " PLN"));
            document.add(new Paragraph(issueDateLabel + ": " + invoice.getIssueDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
            document.add(new Paragraph(billingPeriodLabel + ": " + invoice.getBillingStartDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    + " - " + invoice.getBillingEndDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
            document.add(new Paragraph(paidLabel + ": " + paidStatus));
            document.add(new Paragraph("\n"));


            document.close();
            outputStream.close();
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        }
    }
}
