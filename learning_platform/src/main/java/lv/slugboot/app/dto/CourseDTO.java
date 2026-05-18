package lv.slugboot.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseDTO {

	@NotNull
	@Size(min = 2, max = 100, message = "Course name must be between 2 and 100 characters")
	private String courseName;

	private String courseDesc;

	@NotNull(message = "A professor must be assigned to the course")
	private UUID professorId;
}
