package lv.slugboot.app.service.impl;

import java.util.Collection;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.LabInstance;
import lv.slugboot.app.repo.ICourseRepo;
import lv.slugboot.app.repo.ILabInstanceRepo;
import lv.slugboot.app.repo.IStudentRepo;
import lv.slugboot.app.service.IStudentHomeService;

@Service
@RequiredArgsConstructor
public class StudentHomeServiceImpl implements IStudentHomeService {

	private final IStudentRepo studentRepo;
	private final ICourseRepo courseRepo;
	private final ILabInstanceRepo instanceRepo;
	
	@Override
	public Collection<Course> getAllCourses(UUID studentId) {

		if (studentId == null) {
			throw new NullPointerException("UUID is null");
		}

		return studentRepo.findById(studentId).get().getCourse();
	}

	@Override
	@Transactional
	public void removeCourseFromStudent(UUID studentId, UUID courseId) {
		Student student = studentRepo.findById(studentId).get();
		Course course = courseRepo.findById(courseId).get();

		if (student.getCourse().contains(course)) {
			student.getCourse().remove(course);
			course.getStudents().remove(student);
		}

		studentRepo.save(student);
	}

	@Override
	@Transactional(readOnly = true)
	public Student getStudentByUsername(String username) {
		return studentRepo.findByUsername(username);
	}

	@Override
	public Collection<LabInstance> getLabInstancesForStudent(Student student) {
		return instanceRepo.findByStudent(student);
	}

}
