package lv.slugboot.app.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import lv.slugboot.app.models.Course;

public interface ICourseCRUDService {
	public abstract void createCourse(String courseName, String courseDesc, UUID professorId);

	public abstract List<Course> retrieveAll() throws NoSuchFieldException;

	public abstract Course retrieveById(UUID id) throws NoSuchFieldException;

	public abstract void updateCourseById(UUID id, String courseName, String courseDesc, UUID professorId)
			throws NoSuchFieldException;

	public abstract void deleteCourseById(UUID id) throws NoSuchFieldException, IOException, InterruptedException;

	public abstract void addStudentToCourse(UUID courseId, UUID studentId) throws NoSuchFieldException;

	public abstract void removeStudentFromCourse(UUID courseId, UUID studentId) throws NoSuchFieldException;

	public abstract void deployLab(UUID courseId) throws NoSuchFieldException, IOException, InterruptedException;

	public abstract void cleanupLab(UUID courseId) throws IOException, InterruptedException, NoSuchFieldException;

	public abstract void prepareProxmoxProvisioning(UUID courseId)
			throws NoSuchFieldException, IOException, InterruptedException;

	public abstract Course retrieveBySlug(String slug) throws NoSuchFieldException;

	public abstract void provisionCourseInfrastructure(UUID courseId)
			throws NoSuchFieldException, IOException, InterruptedException;
}
