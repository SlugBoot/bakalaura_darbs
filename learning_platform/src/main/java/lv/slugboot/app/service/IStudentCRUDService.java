package lv.slugboot.app.service;

import java.util.List;
import java.util.UUID;

import lv.slugboot.app.dto.PasswordUpdateDTO;
import lv.slugboot.app.models.Student;

public interface IStudentCRUDService {

	public abstract void createStudent(String name, String middleName, String surname, String email);

	public abstract List<Student> retrieveAll() throws NoSuchFieldException;

	public abstract Student retrieveById(UUID id) throws NoSuchFieldException;

	public abstract void updateStudentById(UUID id, String name, String middleName, String surname, String email)
			throws NoSuchFieldException;

	public abstract void deleteById(UUID id) throws NoSuchFieldException;
	
	public abstract void updatePasswordById(UUID studentId, PasswordUpdateDTO passwordDTO) throws NoSuchFieldException;

	public abstract Student retrieveByUsername(String username) throws NoSuchFieldException;
}
