package lv.slugboot.app.controller;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.service.IStudentCRUDService;

@Controller
@RequestMapping("/student/crud")
@RequiredArgsConstructor
public class StudentCRUDController {

	private final IStudentCRUDService studentCRUDService;

	private static final String MULTIPLE_STUDENTS_PAGE = "show-multiple-students";
	private static final String ERROR_PAGE = "show-error";
	private static final String CREATE_STUDENT_PAGE = "create-student";
	private static final String UPDATE_STUDENT_PAGE = "update-student";

	private static final String STUDENT_ATTRIBUTE = "student";
	private static final String ERROR_ATTRIBUTE = "error";

	@GetMapping("/all")
	public String getControllerGetAllStudents(Model model) {
		try {
			model.addAttribute(STUDENT_ATTRIBUTE, studentCRUDService.retrieveAll());
			return MULTIPLE_STUDENTS_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/create")
	public String getControllerCreateStudent(Model model) {
		model.addAttribute(STUDENT_ATTRIBUTE, new Student());
		return CREATE_STUDENT_PAGE;
	}

	@PostMapping("/create")
	public String postControllerCreateStudent(@Valid Student student, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return CREATE_STUDENT_PAGE;
		}

		try {
			studentCRUDService.createStudent(student.getName(), student.getMiddleName(), student.getSurname(),
					student.getEmail());
			return "redirect:/student/crud/all";
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/update/{uuid}")
	public String getControllerUpdateStudentById(@PathVariable(name = "uuid") UUID studentId, Model model) {
		try {
			model.addAttribute(STUDENT_ATTRIBUTE, studentCRUDService.retrieveById(studentId));
			return UPDATE_STUDENT_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/update/{uuid}")
	public String postControllerUpdateStudentById(@PathVariable(name = "uuid") UUID studentId, @Valid Student student,
			BindingResult result, Model model) {
		if (result.hasErrors()) {
			try {
				return UPDATE_STUDENT_PAGE;
			} catch (Exception e) {
				model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
				return ERROR_PAGE;
			}
		}

		try {
			studentCRUDService.updateStudentById(studentId, student.getName(), student.getMiddleName(),
					student.getSurname(), student.getEmail());
			return "redirect:/student/crud/all";
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/delete/{uuid}")
	public String getControllerDeleteStudent(@PathVariable(name = "uuid") UUID studentId, Model model) {
		try {
			studentCRUDService.deleteById(studentId);
			model.addAttribute(STUDENT_ATTRIBUTE, studentCRUDService.retrieveAll());
			return MULTIPLE_STUDENTS_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}
}
