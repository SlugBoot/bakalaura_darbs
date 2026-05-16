package lv.slugboot.app.repo;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lv.slugboot.app.models.Course;

public interface ICourseRepo extends JpaRepository<Course, UUID> {

	ArrayList<Course> findByProfessorPersonId(UUID professorId);

}
