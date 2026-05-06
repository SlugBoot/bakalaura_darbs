package lv.slugboot.app.service;

import java.util.Collection;
import java.util.UUID;

import lv.slugboot.app.models.Course;

public interface IStudentHomeService {
	
	public abstract Collection<Course> getAllCourses(UUID studentId) throws Exception;
	
	public abstract void removeCourseFromStudent(UUID studentId, UUID courseId) throws Exception;
}
