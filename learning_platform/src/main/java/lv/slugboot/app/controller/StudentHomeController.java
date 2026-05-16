package lv.slugboot.app.controller;

import java.util.Collection;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.service.IStudentCRUDService;
import lv.slugboot.app.service.IStudentHomeService;

@Controller
@RequestMapping("/student/home")
@RequiredArgsConstructor
public class StudentHomeController {

	private final IStudentCRUDService studentCRUDService;
	private final IStudentHomeService studentHomeService;
	
	private static final String STUDENT_HOME_PAGE = "student-home-page";
	private static final String ERROR_PAGE = "show-error";
	
	private static final String STUDENT_ATTRIBUTE = "student";
	private static final String FILTERED_COURSE_ATTRIBUTE = "filtered_courses";
	private static final String ERROR_ATTRIBUTE = "error";
	
	@GetMapping("/{uuid}")
	public String getControllerStudentHomePage(@PathVariable(name="uuid") UUID studentId, Model model) {
		try {
			Student student = studentCRUDService.retrieveById(studentId);
			model.addAttribute(STUDENT_ATTRIBUTE, student);
			
			Collection<Course> filteredCourses = studentHomeService.getAllCourses(studentId);
			model.addAttribute(FILTERED_COURSE_ATTRIBUTE, filteredCourses);
			
			return STUDENT_HOME_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}
	
	@GetMapping("/{uuid}/remove/{courseId}")
	public String getControllerRemoveCourseFromStudent(@PathVariable(name="uuid") UUID studentId, 
			@PathVariable(name="courseId") UUID courseId, Model model) {
		try {
			Student student = studentCRUDService.retrieveById(studentId);
			model.addAttribute(STUDENT_ATTRIBUTE, student);
			
			studentHomeService.removeCourseFromStudent(studentId, courseId);
			
			Collection<Course> filteredCourses = studentHomeService.getAllCourses(studentId);
			model.addAttribute(FILTERED_COURSE_ATTRIBUTE, filteredCourses);
			
			return STUDENT_HOME_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}
}
