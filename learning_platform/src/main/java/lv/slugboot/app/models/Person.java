package lv.slugboot.app.models;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name="PersonTable")
public class Person {
	// TODO: Pārbaudīt RegEx darbību, kad izveidots repo
	@Id
	@NotNull
	@GeneratedValue(strategy = GenerationType.UUID)
	@Setter(value=AccessLevel.NONE)
	private UUID person_id;
	
	@Column(name="FirstName")
	@Pattern(regexp="[[:upper:]]([[:alpha:]]1-44)")
	@NotNull
	private String name;
	
	@Column(name="MiddleName")
	@Pattern(regexp="[[:upper:]]([[:alpha:]]1-44)")
	private String middleName;
	
	@Column(name="LastName")
	@Pattern(regexp="[[:upper:]]([[:alpha:]]1-44)")
	@NotNull
	private String surname;
	
	public Person(String name, String surname) {
		setName(name);
		setSurname(surname);
	}
	public Person(String name, String middleName, String surname) {
		setName(name);
		setMiddleName(middleName);
		setSurname(surname);
	}
}
