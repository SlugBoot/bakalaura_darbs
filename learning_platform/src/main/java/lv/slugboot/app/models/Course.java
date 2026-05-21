package lv.slugboot.app.models;

import java.text.Normalizer;
import java.util.Collection;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Table(name = "CourseTable")
public class Course {

	@Id
	@Setter(value = AccessLevel.NONE)
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "CourseId")
	private UUID cId;

	@NotNull
	@Column(name = "CourseName")
	private String courseName;

	@Column(name = "CourseDescription")
	private String courseDesc;

	@ManyToOne
	@JoinColumn(name = "ProfessorId")
	private Professor professor;

	@ManyToMany(mappedBy = "course")
	@ToString.Exclude
	private Collection<Student> students;

	@OneToMany(mappedBy = "course")
	@ToString.Exclude
	private Collection<LabInstance> labs;

	@Column(name = "slug", unique = true, nullable = false)
	private String slug;

	public Course(String courseName, Professor professor) {
		setCourseName(courseName);
		setProfessor(professor);
	}

	public Course(String courseName, String courseDesc, Professor professor) {
		setCourseName(courseName);
		setCourseDesc(courseDesc);
		setProfessor(professor);
	}

	@PrePersist
	@PreUpdate
	private void generateSlug() {
		if (this.courseName != null) {
			String normalized = Normalizer.normalize(this.courseName, Normalizer.Form.NFD);
			this.slug = normalized.toLowerCase().replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
					.replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-").replaceAll("-+", "-")
					.replaceAll("(?:^-)|(?:-$)", "");

		}
	}

}
