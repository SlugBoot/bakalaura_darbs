package lv.slugboot.app.models;

import java.text.Normalizer;
import java.util.Collection;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "ProfessorTable")
public class Professor extends Person {

	@ToString.Exclude
	@OneToMany(mappedBy = "professor")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Collection<Course> course;

	private String createUsername() {
		// Lietotāja vards.uzvards
		String usernameBase = this.getSurname().toLowerCase().concat(".").concat(this.getName().toLowerCase());

		if (this.getMiddleName() != null) {
			usernameBase = usernameBase.concat(".").concat(this.getMiddleName().toLowerCase());
		}

		String usernameDecomposed = Normalizer.normalize(usernameBase, Normalizer.Form.NFD);
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

		return pattern.matcher(usernameDecomposed).replaceAll("");
	}

	public Professor(String name, String surname, String email) {
		super(name, surname, email);
		this.setUsername(createUsername());
	}

	public Professor(String name, String middleName, String surname, String email) {
		super(name, middleName, surname, email);
		this.setUsername(createUsername());
	}
}
