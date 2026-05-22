package lv.slugboot.app.service;

import java.util.Collection;
import java.util.UUID;

import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.LabInstance;
import lv.slugboot.app.models.Student;

public interface IStudentHomeService {

	public abstract Collection<Course> getAllCourses(UUID studentId);

	public abstract void removeCourseFromStudent(UUID studentId, UUID courseId);

	public abstract Student getStudentByUsername(String username);
	
	public abstract Collection<LabInstance> getLabInstancesForStudent(Student student);
}
