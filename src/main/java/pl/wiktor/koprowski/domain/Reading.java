package pl.wiktor.koprowski.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Table(name = "readings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    double coldWaterValue;

    @Column(nullable = false)
    double hotWaterValue;

    @Column(nullable = false)
    double heatingValue;

    @Column(nullable = false)
    double electricityValue;

    @Column(nullable = false)
    LocalDate date;

    @Column(nullable = false)
    LocalDate billingStartDate;

    @Column(nullable = false)
    LocalDate billingEndDate;

    @ManyToOne
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @OneToOne(mappedBy = "reading")
    private Invoice invoice;
}
