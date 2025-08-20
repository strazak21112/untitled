package pl.wiktor.koprowski.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Table(name = "invoices", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "apartment_id", "billingStartDate", "billingEndDate"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    double rentAmount;

    @Column(nullable = false)
    double otherCharges;

    @Column(nullable = false)
    double totalMediaAmount;

    @Column(nullable = false)
    double totalAmount;

    @Column(nullable = false)
    LocalDate issueDate;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private User tenant;

    @OneToOne
    @JoinColumn(name = "reading_id", nullable = true)
    private Reading reading;

    @ManyToOne
    @JoinColumn(name = "apartment_id", nullable = true)
    private Apartment apartment;


    @Column(nullable = false)
    boolean paid = false;

    @Column(nullable = false)
    LocalDate billingStartDate;

    @Column(nullable = false)
    LocalDate billingEndDate;

     @Column(nullable = false)
    boolean confirmed = false;
    @Embedded
    InvoiceInfo info;
}
