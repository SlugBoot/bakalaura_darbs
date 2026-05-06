package lv.slugboot.app.models;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name="GradeTable")
public class Grade {

	@Id
	@Setter(value=AccessLevel.NONE)
	@GeneratedValue(strategy=GenerationType.UUID)
	private UUID gradeId;
	
	@Column(name="GradeValue")
	private int gradeValue;
	
	@ManyToOne
	@JoinColumn(name="StudentPersonId")
	private Student student;
	
	@ManyToOne
	@JoinColumn(name="CourseId")
	private Course course;
	
	public Grade(int gradeValue, Student student, Course course) {
		setGradeValue(gradeValue);
		setStudent(student);
		setCourse(course);
	}
	
}
