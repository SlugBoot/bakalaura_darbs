package lv.slugboot.app.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Data
@Table(name="PersonTable")
public class Person {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(value=AccessLevel.NONE)
	private long id;
	
    // TODO: Izveidot RegEx vārdam
	@Column(name="FirstName")
	@Pattern(regexp="")
	@NotNull
	private String name;
	
	// TODO: Izveidot RegEx uzvārdam
	@Column(name="LastName")
	@Pattern(regexp="")
	@NotNull
	private String surname;
	
	
}
