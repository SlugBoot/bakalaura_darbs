package lv.slugboot.app.service.impl;

import java.util.Collection;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lv.slugboot.app.models.Student;
import lv.slugboot.app.models.Course;
import lv.slugboot.app.repo.ICourseRepo;
import lv.slugboot.app.repo.IStudentRepo;
import lv.slugboot.app.service.IStudentHomeService;

@Service
public class StudentHomeServiceImpl implements IStudentHomeService{
	
	@Autowired private IStudentRepo studentRepo;
	@Autowired private ICourseRepo courseRepo;

	@Override
	public Collection<Course> getAllCourses(UUID studentId) throws Exception {
		
		if (studentId == null) {
			throw new Exception("UUID is null");
		}

		Collection<Course> result = studentRepo.findById(studentId).get().getCourse();
		
		return result;
	}

	@Override
	@Transactional
	public void removeCourseFromStudent(UUID studentId, UUID courseId) throws Exception {
		Student student = studentRepo.findById(studentId).get();
		Course course = courseRepo.findById(courseId).get();
		
		if (student.getCourse().contains(course)) {
			student.getCourse().remove(course);
			course.getStudents().remove(student);	
		}
		
		studentRepo.save(student);
	}

}
