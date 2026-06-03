package lv.slugboot.app.service.impl;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lv.slugboot.app.dto.PasswordUpdateDTO;
import lv.slugboot.app.dto.PersonDTO;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.repo.IPersonRepo;
import lv.slugboot.app.repo.IStudentRepo;
import lv.slugboot.app.service.IStudentCRUDService;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentCRUDServiceImpl implements IStudentCRUDService {

	private final IStudentRepo studentRepo;
	private final IPersonRepo personRepo;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void createStudent(PersonDTO studentDTO) {
		String name = studentDTO.getName();
		String middleName = studentDTO.getMiddleName();
		String surname = studentDTO.getSurname();
		String email = studentDTO.getEmail();
		String rawPassword = studentDTO.getPassword();

		if (name == null || surname == null || email == null) {
			throw new NullPointerException("Student must have a name, surname and email");
		}

		if (studentRepo.existsByNameAndMiddleNameAndSurnameAndEmail(name, middleName, surname, email)) {
			throw new IllegalArgumentException("A student with that info already exists");
		}

		if (personRepo.existsByEmail(email)) {
			throw new IllegalArgumentException("The email has already been used for a different account");
		} else {
			Student newStudent = new Student(name, middleName, surname, email);
			
			if (rawPassword.length() < 8 || rawPassword.length() > 64) {
				throw new IllegalArgumentException("Password must be between 8 and 64 characters long");
			}
			
			newStudent.setPassword(passwordEncoder.encode(rawPassword));

			studentRepo.save(newStudent);
		}
	}

	@Override
	public ArrayList<Student> retrieveAll() throws NoSuchFieldException {
		return new ArrayList<>(studentRepo.findAll());
	}

	@Override
	public Student retrieveById(UUID id) throws NoSuchFieldException {
		if (id == null) {
			throw new NullPointerException("Student ID cannot be null");
		}
		if (!studentRepo.existsById(id)) {
			throw new NoSuchFieldException("Student with this ID does not exist");
		}

		return studentRepo.findById(id).get();
	}

	@Override
	public void updateStudentById(UUID id, PersonDTO studentDTO) throws NoSuchFieldException {
		Student studentToUpdate = retrieveById(id);
		String name = studentDTO.getName();
		String middleName = studentDTO.getMiddleName();
		String surname = studentDTO.getSurname();
		String email = studentDTO.getEmail();

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

		if (!studentToUpdate.getName().equals(name)) {
			studentToUpdate.setName(name);
		}
		if (middleName != null && !middleName.equals(studentToUpdate.getMiddleName())) {
			studentToUpdate.setMiddleName(middleName);
		} else if (middleName == null && studentToUpdate.getMiddleName() != null) {
			studentToUpdate.setMiddleName(null);
		}
		if (!studentToUpdate.getSurname().equals(surname)) {
			studentToUpdate.setSurname(surname);
		}
		if (!studentToUpdate.getEmail().equals(email)) {
			studentToUpdate.setEmail(email);
		}

		studentRepo.save(studentToUpdate);
	}

	@Override
	public void deleteById(UUID id) throws NoSuchFieldException {
		Student studentToDelete = retrieveById(id);

		studentRepo.delete(studentToDelete);
	}

	@Override
	public void updatePasswordById(UUID studentId, PasswordUpdateDTO passwordDTO) throws NoSuchFieldException {

		String plainPassword = passwordDTO.getNewPassword();

		if (plainPassword == null || plainPassword.trim().isEmpty()) {
			throw new IllegalArgumentException("Password cannot be empty");
		}

		Student student = retrieveById(studentId);

		student.setPassword(passwordEncoder.encode(plainPassword));

		studentRepo.save(student);
	}

	@Override
	public Student retrieveByUsername(String username) throws NoSuchFieldException {
		if (username == null) {
			throw new NullPointerException("Student ID cannot be null");
		}
		if (!studentRepo.existsByUsername(username)) {
			throw new NoSuchFieldException("Student with this ID does not exist");
		}

		return studentRepo.findByUsername(username);
	}
}
