package lv.slugboot.app.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lv.slugboot.app.models.Professor;

public interface IProfessorRepo extends JpaRepository<Professor, UUID>{

}
