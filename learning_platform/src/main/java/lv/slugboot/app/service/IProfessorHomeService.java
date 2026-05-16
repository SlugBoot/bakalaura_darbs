package lv.slugboot.app.service;

import java.util.List;
import java.util.UUID;

import lv.slugboot.app.models.Course;

public interface IProfessorHomeService {

	public abstract List<Course> getAllCoursesWhereProfessorIdEquals(UUID professorId);
}
