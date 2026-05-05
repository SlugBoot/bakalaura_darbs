package lv.slugboot.app.controller;

import java.util.Collection;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.repo.ICourseRepo;
import lv.slugboot.app.service.ICourseCRUDService;
import lv.slugboot.app.service.IFilterService;
import lv.slugboot.app.service.IStudentCRUDService;

@Controller
@RequestMapping("/course/crud")
public class CourseCRUDController {

	@Autowired ICourseCRUDService courseCRUDService;
	@Autowired IFilterService filterService;
	@Autowired IStudentCRUDService studentCRUDService;
	
	private String courseList = "show-multiple-courses";
	private String courseInfoPage = "course-info";
	private String errorPage = "show-error";
	private String addStudentsPage = "course-add-student";
	
	private String studentAttribute = "student";
	private String courseAttribute = "course";
	private String errorAttribute = "error";
	
	@GetMapping("/all")
	public String getControllerAllCourses(Model model) {
		try {
			model.addAttribute(courseAttribute, courseCRUDService.retrieveAll());
			return courseList;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/{uuid}")
	public String getControllerCourseInfo(@PathVariable(name="uuid") UUID courseId, Model model) {
		try {
			Course course = courseCRUDService.retrieveById(courseId);
			model.addAttribute(courseAttribute, course);
			return courseInfoPage;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/delete/{uuid}")
	public String getControllerDeleteCourse(@PathVariable(name="uuid") UUID courseId, Model model) {
		try {
			courseCRUDService.deleteCourseById(courseId);
			model.addAttribute(courseAttribute, courseCRUDService.retrieveAll());
			return courseList;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/{uuid}/add")
	public String getControllerStudentsNotInCourse(@PathVariable(name="uuid") UUID courseId, Model model) {
		try {
			Course course = courseCRUDService.retrieveById(courseId);
			Collection<Student> studentsNotInCourse = filterService.studentsNotInCourse(courseId);
			model.addAttribute(studentAttribute, studentsNotInCourse);
			model.addAttribute(courseAttribute, course);
			return addStudentsPage;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/{uuid}/add/{studentId}")
	public String getControllerAddStudentToCourse(@PathVariable(name="uuid") UUID courseId, 
			@PathVariable(name="studentId") UUID studentId, Model model) {
		try {
			courseCRUDService.addStudentToCourse(courseId, studentId);
			Course course = courseCRUDService.retrieveById(courseId);
			Collection<Student> studentsNotInCourse = filterService.studentsNotInCourse(courseId);
			model.addAttribute(studentAttribute, studentsNotInCourse);
			model.addAttribute(courseAttribute, course);
			return courseInfoPage;
			
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
}
