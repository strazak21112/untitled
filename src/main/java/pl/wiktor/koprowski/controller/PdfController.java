package pl.wiktor.koprowski.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import pl.wiktor.koprowski.domain.Invoice;
import pl.wiktor.koprowski.repository.InvoiceRepository;
import pl.wiktor.koprowski.service.PdfService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class PdfController {

    private final PdfService pdfService;
    private final InvoiceRepository invoiceRepository;

    @Autowired
    public PdfController(PdfService pdfService, InvoiceRepository invoiceRepository) {
        this.pdfService = pdfService;
        this.invoiceRepository = invoiceRepository;
    }

    // Endpoint generujący PDF z fakturą
    @GetMapping("/generateInvoicePdf/{invoiceId}")
    public ResponseEntity<?> generateInvoicePdf(@PathVariable Long invoiceId,
                                                @RequestParam(defaultValue = "pl") String lang,
                                                HttpServletResponse response) throws IOException {

        try {
            // Pobieranie faktury z bazy danych
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

            // Generowanie PDF na podstawie faktury i przekazanego języka
            pdfService.generateInvoicePdf(invoice, response, lang);
            return ResponseEntity.ok().build(); // Zwrócenie statusu OK, jeśli PDF został wygenerowany

        } catch (IllegalArgumentException e) {
            // Obsługa wyjątku w zależności od języka
            String errorMessage = switch (lang.toLowerCase()) {
                case "en" -> "Invoice not found"; // Angielska wersja komunikatu
                case "de" -> "Rechnung nicht gefunden"; // Niemiecka wersja komunikatu
                default -> "Faktura nie znaleziona"; // Polska wersja komunikatu
            };

            // Zwrócenie odpowiedzi z kodem błędu i odpowiednim komunikatem
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
    }
}
