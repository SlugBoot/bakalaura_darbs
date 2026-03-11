package lv.slugboot.app.service;

import java.util.ArrayList;
import java.util.UUID;

import lv.slugboot.app.models.Student;

public interface IStudentCRUDService {
		
	public abstract void createStudent(String name, String middleName, String surname, String email) throws Exception;
	
	public abstract ArrayList<Student> retrieveAll() throws Exception;
	
	public abstract Student retrieveById(UUID id) throws Exception;
	
	public abstract void updateStudentById(UUID id, String name, String middleName, String surname, String email) throws Exception;

	public abstract void deleteById(UUID id) throws Exception;
}
