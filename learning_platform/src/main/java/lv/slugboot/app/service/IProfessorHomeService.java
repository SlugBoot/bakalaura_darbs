package lv.slugboot.app.service;

import java.util.ArrayList;
import java.util.UUID;

import lv.slugboot.app.models.Course;

public interface IProfessorHomeService {
	
	public abstract ArrayList<Course> getAllCoursesWhereProfessorIdEquals(UUID professorId);
}
