package lv.slugboot.app.service.impl;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.Professor;
import lv.slugboot.app.repo.ICourseRepo;
import lv.slugboot.app.repo.IProfessorRepo;
import lv.slugboot.app.service.ICourseCRUDService;

@Service
public class CourseCRUDServiceImpl implements ICourseCRUDService{
	
	@Autowired private ICourseRepo courseRepo;
	@Autowired private IProfessorRepo professorRepo;

	@Override
	public void createCourse(String courseName, String courseDesc, UUID professorId) throws Exception {
		if (courseName == null) {
			throw new Exception("Course Name must not be null");
		}
		
	    if (courseDesc != null && courseDesc.trim().isEmpty()) {
	    	courseDesc = null;
	    }
	    
	    Professor professor = professorRepo.getReferenceById(professorId);
	    
	    Course newCourse;
	    
	    if (courseDesc == null) {
	    	newCourse = new Course(courseName, professor);
	    }
	    else {
	    	newCourse = new Course(courseName, courseDesc, professor);
	    }
		
	    courseRepo.save(newCourse);
	}

	@Override
	public ArrayList<Course> retrieveAll() throws Exception {
	    if (courseRepo.count() == 0) {
	        throw new Exception("Course list is empty");
	      }
	      ArrayList<Course> result = (ArrayList<Course>) courseRepo.findAll();
	      return result;
	}

	@Override
	public Course retrieveById(UUID id) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateCourseById(UUID id) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteCourseById(UUID id) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
