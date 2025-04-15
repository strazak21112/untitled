package pl.wiktor.koprowski.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "apartments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    @NotBlank(message = "Apartment number cannot be blank")
    String number;

    @Column(nullable = false)
    @Min(value = 1, message = "Area must be greater than 0")
    double area;

    @ManyToOne
    @JoinColumn(name = "building_id", nullable = false, updatable = false)
    Building building;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User tenant;


    @Column(nullable = false)
    @Min(value = 0, message = "Floor must be 0 or higher")
    int floor;

     @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Reading> readings = new ArrayList<>();


}
