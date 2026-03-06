package lv.slugboot.app.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lv.slugboot.app.models.Student;

public interface IStudentRepo extends JpaRepository<Student, UUID>{

}
