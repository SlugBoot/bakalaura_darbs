package lv.slugboot.app.models;

import java.util.Collection;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name="CourseTable")
public class Course {
	
	// TODO: Papildināt kursa klasi
	
	@OneToMany(mappedBy="course")
	private Collection<Student> student;
	
	@Id
	@NotNull
	@Setter(value=AccessLevel.NONE)
	@GeneratedValue(strategy=GenerationType.UUID)
	private UUID cId;
}
