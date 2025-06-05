package pl.wiktor.koprowski.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class Address {

	@NotBlank(message = "City cannot be blank")
	private String city;

	@NotBlank(message = "Street cannot be blank")
	private String street;

	@NotBlank(message = "Number cannot be blank")
	@Pattern(regexp = "\\d+[A-Za-z]?", message = "Invalid address number format")
	private String number;

	@NotBlank(message = "Postal code cannot be blank")
	@Pattern(regexp = "\\d{2}-\\d{3}", message = "Invalid postal code format")
	private String postalCode;

	@Transient
	public String getFullAddress() {
		return street + " " + number + ", " + postalCode + " " + city;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Address)) return false;

		Address address = (Address) o;

		return city.equalsIgnoreCase(address.city)
				&& street.equalsIgnoreCase(address.street)
				&& number.equalsIgnoreCase(address.number)
				&& postalCode.equals(address.postalCode);
	}

	@Override
	public int hashCode() {
		int result = city.toLowerCase().hashCode();
		result = 31 * result + street.toLowerCase().hashCode();
		result = 31 * result + number.toLowerCase().hashCode();
		result = 31 * result + postalCode.hashCode();
		return result;
	}

}
