package pl.wiktor.koprowski.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.InvoiceDTO;
import pl.wiktor.koprowski.domain.Invoice;
import pl.wiktor.koprowski.service.InvoiceService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    // Tworzenie nowej faktury
    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@Valid @RequestBody InvoiceDTO invoiceDTO,
                                                 @RequestParam(defaultValue = "en") String lang) {
        try {
            Invoice invoice = invoiceService.createInvoice(invoiceDTO, lang);
            return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Zwracamy 400 jeśli jest błąd
        }
    }

    // Pobieranie faktury po ID
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        Optional<Invoice> invoice = invoiceService.getInvoiceById(id);
        return invoice.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Pobieranie wszystkich faktur
    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        return invoices.isEmpty() ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                : ResponseEntity.ok(invoices);
    }

    // Aktualizacja faktury (tylko zmiana statusu confirmed)
    @PutMapping("/{id}")
    public ResponseEntity<Invoice> updateInvoice(@PathVariable Long id,
                                                 @RequestParam boolean confirmed,
                                                 @RequestParam(defaultValue = "en") String lang) {
        try {
            Invoice updatedInvoice = invoiceService.updateInvoice(id, confirmed, lang);
            return ResponseEntity.ok(updatedInvoice);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Usuwanie faktury
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id,
                                              @RequestParam(defaultValue = "en") String lang) {
        try {
            invoiceService.deleteInvoice(id, lang);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
