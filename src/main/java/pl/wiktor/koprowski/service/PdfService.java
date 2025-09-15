package pl.wiktor.koprowski.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import pl.wiktor.koprowski.DTO.basic.InvoiceDTO;

import java.io.IOException;
import java.io.OutputStream;

@Service
public class PdfService {

    private final TranslationService translationService;

    public PdfService(TranslationService translationService) {
        this.translationService = translationService;
    }

    public void generateInvoicePdf(InvoiceDTO invoice, HttpServletResponse response, String lang) {
        if (lang == null || lang.isBlank()) {
            lang = "pl";
        }

         var labels = translationService.getTranslations(lang);

        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=" +
                    labels.getOrDefault("invoice", "Faktura") + "_" + invoice.getId() + ".pdf");

            OutputStream outputStream = response.getOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            String fontPath = getClass().getResource("/fonts/arial.ttf").toExternalForm();
            BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font sectionFont = new Font(baseFont, 12, Font.BOLD);
            Font normalFont = new Font(baseFont, 12, Font.NORMAL);

             document.add(new Paragraph(labels.getOrDefault("invoice", "Faktura") + " #" + invoice.getId(), titleFont));
            document.add(new Paragraph("\n"));

             document.add(new Paragraph(labels.getOrDefault("billingStartDate", "Początek okresu rozliczeniowego") +
                    ": " + invoice.getBillingStartDate(), normalFont));
            document.add(new Paragraph(labels.getOrDefault("billingEndDate", "Koniec okresu rozliczeniowego") +
                    ": " + invoice.getBillingEndDate(), normalFont));
            document.add(new Paragraph(labels.getOrDefault("issueDate", "Data wystawienia") +
                    ": " + invoice.getIssueDate(), normalFont));

             document.add(new Paragraph("\n" + labels.getOrDefault("charges", "Opłaty") + ":", sectionFont));
            document.add(new Paragraph(labels.getOrDefault("rentAmount", "Wartość wynajmu") +
                    ": " + invoice.getRentAmount() + " PLN", normalFont));
            document.add(new Paragraph(labels.getOrDefault("otherCharges", "Inne koszty") +
                    ": " + invoice.getOtherCharges() + " PLN", normalFont));
            document.add(new Paragraph(labels.getOrDefault("totalMediaAmount", "Wartość mediów") +
                    ": " + invoice.getTotalMediaAmount() + " PLN", normalFont));
            document.add(new Paragraph(labels.getOrDefault("totalAmount", "Suma") +
                    ": " + invoice.getTotalAmount() + " PLN", normalFont));
            document.add(new Paragraph(labels.getOrDefault("paid", "Opłacone") +
                    ": " + (invoice.isPaid() ? labels.getOrDefault("yes", "Tak") : labels.getOrDefault("no", "Nie")), normalFont));
            document.add(new Paragraph(labels.getOrDefault("confirmed", "Potwierdzone") +
                    ": " + (invoice.isConfirmed() ? labels.getOrDefault("yes", "Tak") : labels.getOrDefault("no", "Nie")), normalFont));
            document.add(new Paragraph(labels.getOrDefault("flat", "Ryczałt") +
                    ": " + (invoice.isFlat() ? labels.getOrDefault("yes", "Tak") : labels.getOrDefault("no", "Nie")), normalFont));

             document.add(new Paragraph(labels.getOrDefault("rentRatePerM2", "Wynajem/m²") +
                    ": " + invoice.getInfo().getRentRatePerM2(), normalFont));
            document.add(new Paragraph(labels.getOrDefault("otherChargesPerM2", "Inne koszty/m²") +
                    ": " + invoice.getInfo().getOtherChargesPerM2(), normalFont));

             document.add(new Paragraph("\n" + (invoice.isFlat() ? labels.getOrDefault("flatRates", "Stawki ryczałtowe") : labels.getOrDefault("readings", "Odczyty")) + ":", sectionFont));
            if (!invoice.isFlat()) {
                document.add(new Paragraph(labels.getOrDefault("electricity", "Prąd") + ": " +
                        invoice.getInfo().getElectricityValue() + " kWh (" + invoice.getInfo().getElectricityRate() + " PLN/kWh)", normalFont));
                document.add(new Paragraph(labels.getOrDefault("coldWater", "Zimna woda") + ": " +
                        invoice.getInfo().getColdWaterValue() + " m³ (" + invoice.getInfo().getColdWaterRate() + " PLN/m³)", normalFont));
                document.add(new Paragraph(labels.getOrDefault("hotWater", "Ciepła woda") + ": " +
                        invoice.getInfo().getHotWaterValue() + " m³ (" + invoice.getInfo().getHotWaterRate() + " PLN/m³)", normalFont));
                document.add(new Paragraph(labels.getOrDefault("heating", "Ogrzewanie") + ": " +
                        invoice.getInfo().getHeatingValue() + " GJ (" + invoice.getInfo().getHeatingRate() + " PLN/GJ)", normalFont));
            } else {
                document.add(new Paragraph(labels.getOrDefault("electricity", "Prąd") + ": " + invoice.getInfo().getFlatElectricityRate(), normalFont));
                document.add(new Paragraph(labels.getOrDefault("coldWater", "Zimna woda") + ": " + invoice.getInfo().getFlatColdWaterRate(), normalFont));
                document.add(new Paragraph(labels.getOrDefault("hotWater", "Ciepła woda") + ": " + invoice.getInfo().getFlatHotWaterRate(), normalFont));
                document.add(new Paragraph(labels.getOrDefault("heating", "Ogrzewanie") + ": " + invoice.getInfo().getFlatHeatingRate(), normalFont));
            }

             document.add(new Paragraph("\n" + labels.getOrDefault("buildingData", "Dane budynku i apartamentu") + ":", sectionFont));
            document.add(new Paragraph(labels.getOrDefault("address", "Adres budynku") + ": " +
                    invoice.getInfo().getAddress().getStreet() + " " +
                    invoice.getInfo().getAddress().getNumber() + ", " +
                    invoice.getInfo().getAddress().getPostalCode() + " " +
                    invoice.getInfo().getAddress().getCity(), normalFont));
            document.add(new Paragraph(labels.getOrDefault("apartmentNumber", "Numer mieszkania") + ": " + invoice.getInfo().getApartmentNumber(), normalFont));
            document.add(new Paragraph(labels.getOrDefault("apartmentFloor", "Numer piętra") + ": " + invoice.getInfo().getApartmentFloor(), normalFont));
            document.add(new Paragraph(labels.getOrDefault("apartmentArea", "Powierzchnia mieszkania") + ": " + invoice.getInfo().getApartmentArea() + " m²", normalFont));

             document.add(new Paragraph("\n" + labels.getOrDefault("tenant", "Najemca") + ":", sectionFont));
            document.add(new Paragraph(labels.getOrDefault("firstName", "Imię") + ": " + invoice.getInfo().getTenantFirstName(), normalFont));
            document.add(new Paragraph(labels.getOrDefault("lastName", "Nazwisko") + ": " + invoice.getInfo().getTenantLastName(), normalFont));
            document.add(new Paragraph(labels.getOrDefault("pesel", "PESEL") + ": " + invoice.getInfo().getTenantPesel(), normalFont));
            document.add(new Paragraph(labels.getOrDefault("email", "E-mail") + ": " + invoice.getInfo().getTenantEmail(), normalFont));
            document.add(new Paragraph(labels.getOrDefault("telephone", "Telefon") + ": " + invoice.getInfo().getTenantTelephone(), normalFont));

             document.add(new Paragraph("\n" + labels.getOrDefault("manager", "Zarządca") + ":", sectionFont));
            document.add(new Paragraph(labels.getOrDefault("firstName", "Imię") + ": " + invoice.getInfo().getManagerFirstName(), normalFont));
            document.add(new Paragraph(labels.getOrDefault("lastName", "Nazwisko") + ": " + invoice.getInfo().getManagerLastName(), normalFont));
            document.add(new Paragraph(labels.getOrDefault("pesel", "PESEL") + ": " + invoice.getInfo().getManagerPesel(), normalFont));
            document.add(new Paragraph(labels.getOrDefault("email", "E-mail") + ": " + invoice.getInfo().getManagerEmail(), normalFont));
            document.add(new Paragraph(labels.getOrDefault("telephone", "Telefon") + ": " + invoice.getInfo().getManagerTelephone(), normalFont));

            document.close();

        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        }
    }
}
