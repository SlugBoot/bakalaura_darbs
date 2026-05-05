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
import lv.slugboot.app.service.IStudentCRUDService;
import lv.slugboot.app.service.IStudentHomeService;

@Controller
@RequestMapping("/student/home")
public class StudentHomeController {

	@Autowired private IStudentCRUDService studentCRUDService;
	@Autowired private IStudentHomeService studentHomeService;
	
	private String studentHomePage = "student-home-page";
	private String errorPage = "show-error";
	
	private String studentAttribute = "student";
	private String filteredCourseAttribute = "filtered_courses";
	private String errorAttribute = "error";
	
	@GetMapping("/{uuid}")
	public String getControllerStudentHomePage(@PathVariable(name="uuid") UUID studentId, Model model) {
		try {
			Student student = studentCRUDService.retrieveById(studentId);
			model.addAttribute(studentAttribute, student);
			
			Collection<Course> filteredCourses = studentHomeService.getAllCourses(studentId);
			model.addAttribute(filteredCourseAttribute, filteredCourses);
			
			return studentHomePage;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
}
