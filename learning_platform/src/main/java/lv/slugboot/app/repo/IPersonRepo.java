package lv.slugboot.app.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lv.slugboot.app.models.Person;

public interface IPersonRepo extends JpaRepository<Person, UUID> {

	boolean existsByEmail(String email);

	Optional<Person> findByUsername(String username);

}
