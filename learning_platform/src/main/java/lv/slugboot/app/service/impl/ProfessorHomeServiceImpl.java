package lv.slugboot.app.service.impl;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lv.slugboot.app.models.Course;
import lv.slugboot.app.repo.ICourseRepo;
import lv.slugboot.app.repo.IProfessorRepo;
import lv.slugboot.app.service.IProfessorHomeService;

@Service
public class ProfessorHomeServiceImpl implements IProfessorHomeService {
	
	@Autowired ICourseRepo courseRepo;
	@Autowired IProfessorRepo professorRepo;
	
	
	@Override
	public ArrayList<Course> getAllCoursesWhereProfessorIdEquals(UUID professorId) throws Exception {
				
		if (professorId == null) {
			throw new Exception("UUID is null");
		}
		
		ArrayList<Course> result = courseRepo.findByProfessorPersonId(professorId);
				
		return result;
	}
	
	

}
