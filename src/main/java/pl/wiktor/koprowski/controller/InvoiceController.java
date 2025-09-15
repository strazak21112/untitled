package pl.wiktor.koprowski.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.DTO.basic.InvoiceCreateDTO;
import pl.wiktor.koprowski.DTO.basic.InvoiceDTO;
import pl.wiktor.koprowski.DTO.row.InvoiceRowDTO;
import pl.wiktor.koprowski.domain.Invoice;
import pl.wiktor.koprowski.service.TranslationService;
import pl.wiktor.koprowski.service.basic.InvoiceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final TranslationService translationService;


    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> createInvoice(
            @Valid @RequestBody InvoiceCreateDTO invoiceDTO,
            @RequestParam String managerEmail,
            @RequestParam(defaultValue = "pl") String lang) {

        Invoice invoice = invoiceService.createInvoice(invoiceDTO, managerEmail);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("invoiceId", invoice.getId());
        response.put("message", translationService.getTranslation("invoice_created_success", lang));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> updateInvoice(
            @PathVariable Long id,
            @RequestParam boolean confirmed,
            @RequestParam  String managerEmail,
            @RequestParam(defaultValue = "pl") String lang) {

        Invoice updatedInvoice = invoiceService.updateInvoice(id, confirmed, managerEmail);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("invoiceId", updatedInvoice.getId());
        response.put("message", translationService.getTranslation("invoice_updated_success", lang));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> deleteInvoice(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pl") String lang) {

        invoiceService.deleteInvoice(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", translationService.getTranslation("invoice_deleted_success", lang));

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}/pay", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> payInvoice(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pl") String lang) {

        Invoice invoice = invoiceService.payInvoice(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("invoiceId", invoice.getId());
        response.put("message", translationService.getTranslation("invoice_paid_success", lang));

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')   or hasRole('USER') ")
    public ResponseEntity<Map<String, Object>> getInvoiceDetails(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pl") String lang) {

        InvoiceDTO details = invoiceService.getInvoiceDetails(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", details);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/rows", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getInvoiceRowsByBuilding(
            @RequestParam Long buildingId,
            @RequestParam(defaultValue = "pl") String lang) {

        List<InvoiceRowDTO> invoiceRows = invoiceService.getAllInvoiceRowsByBuilding(buildingId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", invoiceRows);

        return ResponseEntity.ok(response);
    }


    @GetMapping(value = "/my", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getMyInvoices(
            @RequestParam String email,
            @RequestParam(defaultValue = "pl") String lang) {

        List<InvoiceRowDTO> invoiceRows = invoiceService.getAllInvoiceRowsByTenantEmail(email);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", invoiceRows);

        return ResponseEntity.ok(response);
    }


}
