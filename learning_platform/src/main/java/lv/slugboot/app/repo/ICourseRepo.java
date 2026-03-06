package lv.slugboot.app.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lv.slugboot.app.models.Course;

public interface ICourseRepo extends JpaRepository<Course, UUID>{

}
