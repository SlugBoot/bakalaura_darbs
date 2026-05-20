package lv.slugboot.app.service;

import java.util.List;
import java.util.UUID;

import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.Professor;

public interface IProfessorHomeService {

	public abstract List<Course> getAllCoursesWhereProfessorIdEquals(UUID professorId);

	public abstract Professor retrieveByUsername(String username);
}
