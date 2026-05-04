package lv.slugboot.app.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.service.IStudentCRUDService;

@Controller
@RequestMapping("/student/crud")
public class StudentCRUDController {
	
	@Autowired private IStudentCRUDService studentCRUDService;
	
	private String multipleStudentsPage = "show-multiple-students";
	private String errorPage = "show-error";
	private String createStudentPage = "create-student";
	private String updateStudentPage = "update-student";
	
	private String studentAttribute = "student";
	private String errorAttribute = "error";
	

	@GetMapping("/all")
	public String getControllerGetAllStudents(Model model) {
		try {
			model.addAttribute(studentAttribute, studentCRUDService.retrieveAll());
			return multipleStudentsPage;
		}
		catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/create")
	public String getControllerCreateStudent(Model model) {
		model.addAttribute(studentAttribute, new Student());
		return createStudentPage;
	}
	
	@PostMapping("/create")
	public String postControllerCreateStudent(@Valid Student student, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return createStudentPage;
		}
		
		try {
			studentCRUDService.createStudent(student.getName(), student.getMiddleName(), student.getSurname(), student.getEmail());
			return "redirect:/student/crud/all";
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/update/{uuid}")
	public String getControllerUpdateStudentById(@PathVariable(name="uuid") UUID studentId, Model model) {
		try {
			model.addAttribute(studentAttribute, studentCRUDService.retrieveById(studentId));
			return updateStudentPage;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@PostMapping("/update/{uuid}")
	public String postControllerUpdateStudentById(@PathVariable(name="uuid") UUID studentId, @Valid Student student, BindingResult result, Model model) {
		if (result.hasErrors()) {
			try {
				return updateStudentPage;
			} catch (Exception e) {
				model.addAttribute(errorAttribute, e.getMessage());
				return errorPage;
			}
		}
		
		try {
			studentCRUDService.updateStudentById(studentId, student.getName(), student.getMiddleName(), student.getSurname(), student.getEmail());
			return "redirect:/student/crud/all";
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/delete/{uuid}")
	public String getControllerDeleteStudent(@PathVariable(name="uuid") UUID studentId, Model model) {
		try {
			studentCRUDService.deleteById(studentId);
			model.addAttribute(studentAttribute, studentCRUDService.retrieveAll());
			return multipleStudentsPage;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
}
