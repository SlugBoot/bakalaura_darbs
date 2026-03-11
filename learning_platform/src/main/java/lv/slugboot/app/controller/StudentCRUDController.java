package lv.slugboot.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lv.slugboot.app.service.IStudentCRUDService;

@Controller
@RequestMapping("/student/crud")
public class StudentCRUDController {
	
	@Autowired private IStudentCRUDService studentService;

	@GetMapping("/all")
	public String getControllerGetAllStudents(Model model) {
		try {
			model.addAttribute("student", studentService.retrieveAll());
			return "show-multiple-students";
		}
		catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "show-error";
		}
	}
}
