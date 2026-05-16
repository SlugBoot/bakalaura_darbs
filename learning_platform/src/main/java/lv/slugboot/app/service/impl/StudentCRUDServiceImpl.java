package lv.slugboot.app.service.impl;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.repo.IPersonRepo;
import lv.slugboot.app.repo.IStudentRepo;
import lv.slugboot.app.service.IStudentCRUDService;

@Service
@RequiredArgsConstructor
public class StudentCRUDServiceImpl implements IStudentCRUDService {

	private final IStudentRepo studentRepo;

	private final IPersonRepo personRepo;

	@Override
	public void createStudent(String name, String middleName, String surname, String email) {
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
			studentRepo.save(newStudent);
		}
	}

	@Override
	public ArrayList<Student> retrieveAll() throws NoSuchFieldException {
		if (studentRepo.count() == 0) {
			throw new NoSuchFieldException("Student list is empty");
		}

		return (ArrayList<Student>) studentRepo.findAll();
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
	public void updateStudentById(UUID id, String name, String middleName, String surname, String email)
			throws NoSuchFieldException {
		Student studentToUpdate = retrieveById(id);

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
}
