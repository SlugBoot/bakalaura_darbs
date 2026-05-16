package lv.slugboot.app.service.impl;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lv.slugboot.app.models.Professor;
import lv.slugboot.app.repo.IPersonRepo;
import lv.slugboot.app.repo.IProfessorRepo;
import lv.slugboot.app.service.IProfessorCRUDService;

@Service
@RequiredArgsConstructor
public class ProfessorCRUDServiceImpl implements IProfessorCRUDService {

  private final IProfessorRepo professorRepo;

  private final IPersonRepo personRepo;

  @Override
  public void createProfessor(String name, String middleName, String surname, String email) {
    if (name == null || surname == null || email == null) {
      throw new NullPointerException("Professor must have a name, surname and email address");
    }
    if (professorRepo.existsByNameAndMiddleNameAndSurnameAndEmail(name, middleName, surname, email)) {
      throw new IllegalArgumentException("Professor with those details already exists");
    }
    
    if (middleName != null && middleName.trim().isEmpty()) {
    	middleName = null;
    }

    if (personRepo.existsByEmail(email)) {
      throw new IllegalArgumentException("The email has already been used for a different account");
    } else {
      Professor newProfessor = new Professor(name, middleName, surname, email);
      professorRepo.save(newProfessor);
    }
  }

  @Override
  public ArrayList<Professor> retrieveAll() throws NoSuchFieldException {
    if (professorRepo.count() == 0) {
      throw new NoSuchFieldException("Professor list is empty");
    }
    ArrayList<Professor> result = (ArrayList<Professor>) professorRepo.findAll();
    return result;
  }

  @Override
  public Professor retrieveById(UUID id) throws NoSuchFieldException {
    if (id == null) {
      throw new NullPointerException("Professor ID cannot be null");
    }
    if (!professorRepo.existsById(id)) {
      throw new NoSuchFieldException("Professor with this ID does not exist");
    }

    return professorRepo.findById(id).get();
  }

  @Override
  public void updateProfessorById(UUID id, String name, String middleName, String surname, String email)
      throws NoSuchFieldException {
    Professor professorToUpdate = retrieveById(id);

    if (name == null || !name.matches("([A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44}")) {
      throw new IllegalArgumentException("First Name must be valid");
    }

    if (middleName != null && !middleName.isEmpty() && !middleName.matches("([A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44}")) {
        throw new IllegalArgumentException("Middle name must be valid");
    }

    if (surname == null || !surname.matches("([A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44}")) {
      throw new IllegalArgumentException("Surname must be valid");
    }

    if (email == null || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
      throw new IllegalArgumentException("Email must be valid");
    }

    if (!professorToUpdate.getName().equals(name)) {
      professorToUpdate.setName(name);
    }
    if (middleName != null && !middleName.equals(professorToUpdate.getMiddleName())) {
        professorToUpdate.setMiddleName(middleName);
      } else if (middleName == null && professorToUpdate.getMiddleName() != null) {
      	professorToUpdate.setMiddleName(null);
      }
    if (!professorToUpdate.getSurname().equals(surname)) {
      professorToUpdate.setSurname(surname);
    }
    if (!professorToUpdate.getEmail().equals(email)) {
      professorToUpdate.setEmail(email);
    }

    professorRepo.save(professorToUpdate);
  }

  @Override
  public void deleteProfessorById(UUID id) throws NoSuchFieldException {
    Professor professorToDelete = retrieveById(id);

    professorRepo.delete(professorToDelete);
  }
}
