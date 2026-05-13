package lv.slugboot.app.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lv.slugboot.app.models.LabInstance;

public interface ILabInstanceRepo extends JpaRepository<LabInstance, UUID> {

}
