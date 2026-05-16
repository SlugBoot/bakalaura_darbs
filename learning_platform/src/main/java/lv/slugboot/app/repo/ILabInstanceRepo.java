package lv.slugboot.app.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.LabInstance;
import lv.slugboot.app.models.Student;

public interface ILabInstanceRepo extends JpaRepository<LabInstance, UUID> {

	List<LabInstance> findByCourse(Course course);

	LabInstance findByCourseAndStudent(Course course, Student student);

}
