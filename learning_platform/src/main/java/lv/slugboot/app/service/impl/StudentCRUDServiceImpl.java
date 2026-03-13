package lv.slugboot.app.service.impl;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lv.slugboot.app.models.Student;
import lv.slugboot.app.repo.IPersonRepo;
import lv.slugboot.app.repo.IStudentRepo;
import lv.slugboot.app.service.IStudentCRUDService;

@Service
public class StudentCRUDServiceImpl implements IStudentCRUDService{

	@Autowired private IStudentRepo studentRepo;
	@Autowired private IPersonRepo personRepo;

	@Override
	public void createStudent(String name, String middleName, String surname, String email) throws Exception {
		// TODO Auto-generated method stub
		if (name == null || surname == null || email == null) {
			throw new Exception("Student must have a name, surname and email");
		}
		
		if (studentRepo.existsByNameAndMiddleNameAndSurnameAndEmail(name,middleName,surname,email)) {
			throw new Exception("A student with that info already exists");
		}
		if (personRepo.existsByEmail(email)) {
			throw new Exception("The email has already been used for a different account");
		}
		else {
			Student newStudent = new Student(name, middleName, surname, email);
			studentRepo.save(newStudent);
		}
	}

	@Override
	public ArrayList<Student> retrieveAll() throws Exception {
		if (studentRepo.count() == 0) {
			throw new Exception("Student list is empty");
		}
		ArrayList<Student> result = (ArrayList<Student>)studentRepo.findAll();
		return result;
	}

	@Override
	public Student retrieveById(UUID id) throws Exception {
		if (id == null) {
			throw new Exception("Student ID cannot be null");
		}
		if (!studentRepo.existsById(id)) {
			throw new Exception("Student with this ID does not exist");
		}
		
		return studentRepo.findById(id).get();
	}

	@Override
	public void updateStudentById(UUID id, String name, String middleName, String surname, String email)
			throws Exception {
		// TODO Auto-generated method stub
		Student studentToUpdate = retrieveById(id);
		
		if (name == null || !name.matches("[A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44}")) {
			throw new Exception("First Name must be valid");
		}
		
		if (middleName != null && !middleName.matches("[A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44}")){
			throw new Exception("Middle name must be valid");
		}

		if (surname == null || !surname.matches("[A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44}")) {
			throw new Exception("Surname must be valid");
		}
		
		if (email == null || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
			throw new Exception("Email must be valid");
		}
		
		if (!studentToUpdate.getName().equals(name)) {
			studentToUpdate.setName(name);
		}
		if (!studentToUpdate.getMiddleName().equals(middleName)) {
			studentToUpdate.setMiddleName(middleName);
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
	public void deleteById(UUID id) throws Exception {
		// TODO Auto-generated method stub
		Student studentToDelete = retrieveById(id);
		
		studentRepo.delete(studentToDelete);
	}
}
