package lv.slugboot.app.service.impl;

import java.util.Collection;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lv.slugboot.app.models.Course;
import lv.slugboot.app.repo.IStudentRepo;
import lv.slugboot.app.service.IStudentHomeService;

@Service
public class StudentHomeServiceImpl implements IStudentHomeService{
	
	@Autowired private IStudentRepo studentRepo;

	@Override
	public Collection<Course> getAllCourses(UUID studentId) throws Exception {
		
		if (studentId == null) {
			throw new Exception("UUID is null");
		}

		Collection<Course> result = studentRepo.findById(studentId).get().getCourse();
		
		return result;
	}

}
