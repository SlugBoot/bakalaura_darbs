package lv.slugboot.app.service;

import java.util.Collection;
import java.util.UUID;

import lv.slugboot.app.models.Student;

public interface IFilterService {
	
	public abstract Collection<Student> studentsNotInCourse(UUID courseId) throws Exception;
	
}
