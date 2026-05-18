package lv.slugboot.app.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lv.slugboot.app.dto.PasswordUpdateDTO;
import lv.slugboot.app.models.Professor;
import lv.slugboot.app.repo.IPersonRepo;
import lv.slugboot.app.repo.IProfessorRepo;
import lv.slugboot.app.service.IProfessorCRUDService;
import utils.PasswordGenerator;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfessorCRUDServiceImpl implements IProfessorCRUDService {

	private final IProfessorRepo professorRepo;
	private final IPersonRepo personRepo;
	private final PasswordEncoder passwordEncoder;

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
			
			String rawPassword = PasswordGenerator.generateRandomPassword(12);
			
			log.debug("[SECURITY DEBUG] Generated password for professor: ("+ newProfessor.getUsername() +"), " + rawPassword);
			
			newProfessor.setPassword(passwordEncoder.encode(rawPassword));
			
			professorRepo.save(newProfessor);
		}
	}

	@Override
	public List<Professor> retrieveAll() throws NoSuchFieldException {
		if (professorRepo.count() == 0) {
			throw new NoSuchFieldException("Professor list is empty");
		}

		return professorRepo.findAll();
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
		String regexPattern = "([A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44}";

		if (name == null || !name.matches(regexPattern)) {
			throw new IllegalArgumentException("First Name must be valid");
		}

		if (middleName != null && !middleName.isEmpty() && !middleName.matches(regexPattern)) {
			throw new IllegalArgumentException("Middle name must be valid");
		}

		if (surname == null || !surname.matches(regexPattern)) {
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

	@Override
	public void updatePasswordById(UUID professorId, PasswordUpdateDTO passwordDTO) throws NoSuchFieldException {
		String plainPassword = passwordDTO.getNewPassword();
		
		if (plainPassword == null || plainPassword.trim().isEmpty()) {
			throw new IllegalArgumentException("Password cannot be empty");
		}
		
		Professor professor = retrieveById(professorId);
		
		professor.setPassword(passwordEncoder.encode(plainPassword));
		
		professorRepo.save(professor);
	}
}
