package lv.slugboot.app.models;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="StudentTable")
public class Student extends Person {
	
	@Column(name="username")
	@Pattern(regexp="")
	private String username;
	
	// NOTE: Iespējams var uzlabot ar vienu "kontaktinformācijas" klasi
	@Column(name="email")
	@Pattern(regexp="/^(?!\\.)(?!.*\\.\\.)([a-z0-9_'+\\-\\.]*)[a-z0-9_'+\\-]@([a-z0-9][a-z0-9\\-]*\\.)+[a-z]{2,}$/i")
	// avots RegEx: https://colinhacks.com/essays/reasonable-email-regex
	private String email;
	
	@ManyToOne
	private Course course;
	
	
	public Student(String name, String surname, String email, String username) {
		super(name,surname);
		setEmail(email);
		setUsername(username);
	}
	
	public Student(String name, String middleName, String surname, String email, String username) {
		super(name,middleName,surname);
		setEmail(email);
		setUsername(username);
	}
}
