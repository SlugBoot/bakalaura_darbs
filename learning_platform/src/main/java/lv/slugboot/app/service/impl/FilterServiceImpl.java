package lv.slugboot.app.service.impl;


import java.util.Collection;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lv.slugboot.app.models.Student;
import lv.slugboot.app.repo.ICourseRepo;
import lv.slugboot.app.repo.IProfessorRepo;
import lv.slugboot.app.repo.IStudentRepo;
import lv.slugboot.app.service.IFilterService;

@Service
public class FilterServiceImpl implements IFilterService{

	@Autowired private ICourseRepo courseRepo;
	@Autowired private IStudentRepo studentRepo;
	@Autowired private IProfessorRepo professorRepo;
	
	@Override
	public Collection<Student> studentsNotInCourse(UUID courseId) throws Exception {
		Collection<Student> studentsInCourse = courseRepo.findById(courseId).get().getStudents();
		Collection<Student> students = studentRepo.findAll();
		
		students.removeAll(studentsInCourse);
		
		return students;
	}

}
