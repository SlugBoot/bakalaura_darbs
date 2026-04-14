package lv.slugboot.app.service;

import java.util.ArrayList;
import java.util.UUID;

import lv.slugboot.app.models.Course;

public interface ICourseCRUDService {
	public abstract void createCourse(String courseName, String courseDesc, UUID professorId) throws Exception;
	
	public abstract ArrayList<Course> retrieveAll() throws Exception;
	
	public abstract Course retrieveById(UUID id) throws Exception;
	
	public abstract void updateCourseById(UUID id, String courseName, String courseDesc, UUID professorId) throws Exception;
	
	public abstract void deleteCourseById(UUID id) throws Exception;
}
