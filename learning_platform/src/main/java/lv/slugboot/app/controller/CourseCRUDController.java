package lv.slugboot.app.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lv.slugboot.app.models.Course;
import lv.slugboot.app.service.ICourseCRUDService;

@Controller
@RequestMapping("/course")
public class CourseCRUDController {

	@Autowired ICourseCRUDService courseCRUDService;
	
	@GetMapping("/{uuid}")
	public String getControllerCourseInfo(@PathVariable(name="uuid") UUID courseId, Model model) {
		try {
			Course course = courseCRUDService.retrieveById(courseId);
			model.addAttribute("course", course);
			return "course-info";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "show-error";
		}
	}
	
	@GetMapping("")
	public String getMappingAddStudentToCourse(Model model) {
		try {
			return "student-list";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "show-error";
		}
	}
}
