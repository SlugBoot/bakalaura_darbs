package lv.slugboot.app.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import lv.slugboot.app.models.Person;

public interface IPersonRepo extends JpaRepository<Person, Long>{

}
