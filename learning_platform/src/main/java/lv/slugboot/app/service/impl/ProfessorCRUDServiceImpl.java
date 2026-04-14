package lv.slugboot.app.service.impl;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lv.slugboot.app.models.Professor;
import lv.slugboot.app.repo.IPersonRepo;
import lv.slugboot.app.repo.IProfessorRepo;
import lv.slugboot.app.service.IProfessorCRUDService;

@Service
public class ProfessorCRUDServiceImpl implements IProfessorCRUDService {

  @Autowired
  private IProfessorRepo professorRepo;
  @Autowired
  private IPersonRepo personRepo;

  @Override
  public void createProfessor(String name, String middleName, String surname, String email) throws Exception {
    if (name == null || surname == null || email == null) {
      throw new Exception("Professor must have a name, surname and email address");
    }
    if (professorRepo.existsByNameAndMiddleNameAndSurnameAndEmail(name, middleName, surname, email)) {
      throw new Exception("Professor with those details already exists");
    }
    
    if (middleName != null && middleName.trim().isEmpty()) {
    	middleName = null;
    }

    if (personRepo.existsByEmail(email)) {
      throw new Exception("The email has already been used for a different account");
    } else {
      Professor newProfessor = new Professor(name, middleName, surname, email);
      professorRepo.save(newProfessor);
    }
  }

  @Override
  public ArrayList<Professor> retrieveAll() throws Exception {
    if (professorRepo.count() == 0) {
      throw new Exception("Professor list is empty");
    }
    ArrayList<Professor> result = (ArrayList<Professor>) professorRepo.findAll();
    return result;
  }

  @Override
  public Professor retrieveById(UUID id) throws Exception {
    if (id == null) {
      throw new Exception("Professor ID cannot be null");
    }
    if (!professorRepo.existsById(id)) {
      throw new Exception("Professor with this ID does not exist");
    }

    return professorRepo.findById(id).get();
  }

  @Override
  public void updateProfessorById(UUID id, String name, String middleName, String surname, String email)
      throws Exception {
    Professor professorToUpdate = retrieveById(id);

    if (name == null || !name.matches("([A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44}")) {
      throw new Exception("First Name must be valid");
    }

    if (middleName != null && !middleName.matches("([A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44}")) {
      throw new Exception("Middle name must be valid");
    }

    if (surname == null || !surname.matches("([A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44}")) {
      throw new Exception("Surname must be valid");
    }

    if (email == null || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
      throw new Exception("Email must be valid");
    }

    if (!professorToUpdate.getName().equals(name)) {
      professorToUpdate.setName(name);
    }
    if (!professorToUpdate.getMiddleName().equals(middleName)) {
      professorToUpdate.setMiddleName(middleName);
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
  public void deleteProfessorById(UUID id) throws Exception {
    Professor professorToDelete = retrieveById(id);

    professorRepo.delete(professorToDelete);
  }
}
