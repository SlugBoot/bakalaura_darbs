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
import lv.slugboot.app.models.enums.LabInstanceStatus;

@Setter
@Getter
@NoArgsConstructor
@ToString
@Entity
@Table(name="LabInstanceTable")
public class LabInstance {

	@Id
	@Setter(value=AccessLevel.NONE)
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "InstanceId")
	private UUID instanceId;
	
	@ManyToOne
	@JoinColumn(name="StudentId")
	private Student student;
	
	@ManyToOne
	@JoinColumn(name="CourseId")
	private Course course;
	
	private String ipAddress;
	
	private LabInstanceStatus status;
	
	public LabInstance(Student student, Course course, String ipAddress) {
		setStudent(student);
		setCourse(course);
		setIpAddress(ipAddress);
		setStatus(LabInstanceStatus.Unprovisioned);
	}
}
