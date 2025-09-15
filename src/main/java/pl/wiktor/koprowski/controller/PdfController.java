package pl.wiktor.koprowski.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.basic.InvoiceDTO;
import pl.wiktor.koprowski.service.PdfService;
import pl.wiktor.koprowski.service.basic.InvoiceService;

import java.io.IOException;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;
    private final InvoiceService invoiceService;

    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public void generateInvoicePdf(
            @PathVariable Long invoiceId,
            @RequestParam(defaultValue = "pl") String lang,
            HttpServletResponse response) throws IOException {

        InvoiceDTO invoice = invoiceService.getInvoiceDetails(invoiceId);
        if (invoice == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        pdfService.generateInvoicePdf(invoice, response, lang);
    }


}
