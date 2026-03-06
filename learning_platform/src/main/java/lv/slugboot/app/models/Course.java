package lv.slugboot.app.models;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
	@Id
	@NotNull
	@Setter(value=AccessLevel.NONE)
	@GeneratedValue(strategy=GenerationType.UUID)
	@Column(name="CourseId")
	private UUID cId;
	
	@NotNull
	@Column(name="CourseName")
	private String courseName;
	
	@Column(name="CourseDescription")
	private String courseDesc;
	
	@NotNull
	@OneToOne
	@JoinColumn(name="ProfessorId")
	private Professor professor;
	
	public Course(String courseName, Professor professor) {
		setCourseName(courseName);
		setProfessor(professor);
	}
	
	public Course(String courseName,String courseDesc, Professor professor) {
		setCourseName(courseName);
		setCourseDesc(courseDesc);
		setProfessor(professor);
	}
	
}
