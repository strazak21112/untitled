package pl.wiktor.koprowski.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Pesel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Pattern(regexp = "\\d{11}", message = "Invalid PESEL number")
	@Column(nullable = false, unique = true)
	private String pesel;

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;



	public Pesel(String pesel, User user) {
		setPesel(pesel);
		linkUser(user);
	}

	public void setPesel(String pesel) {
		if (pesel == null || !isValidPesel(pesel)) {
			throw new IllegalArgumentException("Invalid PESEL number");
		}
		this.pesel = pesel;
	}

	public void linkUser(User user) {
		if (user == null) {
			throw new IllegalArgumentException("User cannot be null");
		}
		this.user = user;
		user.setPesel(this);
	}

	public static boolean isValidPesel(String pesel) {
		if (pesel == null || !pesel.matches("\\d{11}")) return false;
		int[] weights = {1, 3, 7, 9, 1, 3, 7, 9, 1, 3};
		int sum = 0;
		for (int i = 0; i < 10; i++) {
			sum += (pesel.charAt(i) - '0') * weights[i];
		}
		int controlDigit = (10 - (sum % 10)) % 10;
		return controlDigit == (pesel.charAt(10) - '0');
	}

	@Override
	public String toString() {
		return "Pesel [id=" + id + ", pesel=" + pesel + "]";
	}

}
