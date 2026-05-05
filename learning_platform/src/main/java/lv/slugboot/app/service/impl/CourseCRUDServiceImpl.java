package lv.slugboot.app.service.impl;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.Professor;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.repo.ICourseRepo;
import lv.slugboot.app.repo.IProfessorRepo;
import lv.slugboot.app.repo.IStudentRepo;
import lv.slugboot.app.service.ICourseCRUDService;

@Service
public class CourseCRUDServiceImpl implements ICourseCRUDService{
	
	@Autowired private ICourseRepo courseRepo;
	@Autowired private IProfessorRepo professorRepo;
	@Autowired private IStudentRepo studentRepo;

	@Override
	public void createCourse(String courseName, String courseDesc, UUID professorId) throws Exception {
		if (courseName == null) {
			throw new Exception("Course Name must not be null");
		}
		
		if (professorId == null) {
			throw new Exception("Professor must be set for course to exist");
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
		if (id == null) {
			throw new Exception("Course ID cannot be null");
		}
		
		
		if (!courseRepo.existsById(id)) {
			throw new Exception("Course with id " + id + "does not exist");
		}
		
		return courseRepo.findById(id).get();
	}

	@Override
	public void updateCourseById(UUID id, String courseName, String courseDesc, UUID professorId) throws Exception {		
		if (courseName == null) {
			throw new Exception("Course name cannot be empty");
		}
		
		Professor professor;
		
		if (!professorRepo.existsById(professorId)) {
			throw new Exception("This professor does not exist");
		}
		
		professor = professorRepo.findById(professorId).get();
		
		Course courseToUpdate = retrieveById(id);
		
		if(!courseToUpdate.getCourseName().equals(courseName)) {
			courseToUpdate.setCourseName(courseName);
		}
		
		if(!courseToUpdate.getCourseDesc().equals(courseDesc)) {
			courseToUpdate.setCourseDesc(courseDesc);
		}
		
		if(courseToUpdate.getProfessor() != professor) {
			courseToUpdate.setProfessor(professor);
		}
		
		courseRepo.save(courseToUpdate);
	}

	@Override
	public void deleteCourseById(UUID id) throws Exception {
		Course courseToDelete = retrieveById(id);
		
		courseRepo.delete(courseToDelete);
	}

	@Override
	@Transactional
	public void addStudentToCourse(UUID courseId, UUID studentId) throws Exception {
		Course course = retrieveById(courseId);
		Student student = studentRepo.findById(studentId).get();
		
		student.getCourse().add(course);
		course.getStudents().add(student);
		
		courseRepo.save(course);
	}



}
