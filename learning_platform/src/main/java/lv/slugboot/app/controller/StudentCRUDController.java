package lv.slugboot.app.controller;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lv.slugboot.app.dto.PasswordUpdateDTO;
import lv.slugboot.app.dto.PersonDTO;
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
	private static final String UPDATE_PASSWORD_PAGE = "update-password";
	private static final String STUDENT_REDIRECT_PAGE = "redirect:/student/crud/";

	private static final String STUDENT_ATTRIBUTE = "student";
	private static final String ERROR_ATTRIBUTE = "error";
	private static final String PASSWORD_ATTRIBUTE = "password";
	private static final String USER_TYPE_ATTRIBUTE = "userType";
	private static final String USER_ID_ATTRIBUTE = "userId";

	private static final String UUID_PARAMETER = "uuid";

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
		model.addAttribute(STUDENT_ATTRIBUTE, new PersonDTO());
		return CREATE_STUDENT_PAGE;
	}

	@PostMapping("/create")
	public String postControllerCreateStudent(@Valid PersonDTO student, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return CREATE_STUDENT_PAGE;
		}

		try {
			studentCRUDService.createStudent(student.getName(), student.getMiddleName(), student.getSurname(),
					student.getEmail());
			return STUDENT_REDIRECT_PAGE + "all";
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/update/{username}")
	public String getControllerUpdateStudentById(@PathVariable(name = "username") String username, Model model) {
		try {
			Student student = studentCRUDService.retrieveByUsername(username);
			model.addAttribute(STUDENT_ATTRIBUTE, studentCRUDService.retrieveById(student.getPersonId()));
			return UPDATE_STUDENT_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/update")
	public String postControllerUpdateStudentById(HttpServletRequest request, @Valid PersonDTO student,
			BindingResult result, Model model) {
		String studentIdStr = request.getParameter(UUID_PARAMETER);
		UUID studentId = UUID.fromString(studentIdStr);

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
			return STUDENT_REDIRECT_PAGE + "all";
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/update-password/{username}")
	public String getControllerUpdatePassword(@PathVariable(name = "username") String username, Model model) {
		try {
			Student student = studentCRUDService.retrieveByUsername(username);
			model.addAttribute(USER_ID_ATTRIBUTE, student.getPersonId());
			model.addAttribute(USER_TYPE_ATTRIBUTE, "student");
			model.addAttribute(PASSWORD_ATTRIBUTE, new PasswordUpdateDTO());
			return "update-password";
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/update-password")
	public String postControllerUpdatePassword(HttpServletRequest request,
			@Valid @ModelAttribute("password") PasswordUpdateDTO passwordDto, BindingResult result, Model model) {
		String studentIdStr = request.getParameter(UUID_PARAMETER);
		UUID studentId = UUID.fromString(studentIdStr);

		Runnable populateErrorModel = () -> {
			model.addAttribute(USER_ID_ATTRIBUTE, studentId);
			model.addAttribute(USER_TYPE_ATTRIBUTE, "student");
			model.addAttribute(PASSWORD_ATTRIBUTE, passwordDto);
		};

		if (result.hasErrors()) {
			populateErrorModel.run();
			return UPDATE_PASSWORD_PAGE;
		}

		if (!passwordDto.isNewPasswordMatching()) {
			result.rejectValue("confirmPassword", "error.passwordDto", "New Passwords do not match");
			populateErrorModel.run();
			return UPDATE_PASSWORD_PAGE;
		}

		try {
			studentCRUDService.updatePasswordById(studentId, passwordDto);
			return STUDENT_REDIRECT_PAGE + "all";
		} catch (IllegalArgumentException e) {
			result.rejectValue("currentPassword", "error.passwordDto", e.getMessage());
			populateErrorModel.run();
			return UPDATE_PASSWORD_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/delete")
	public String postControllerDeleteStudent(HttpServletRequest request, Model model) {
		try {
			String studentIdStr = request.getParameter(UUID_PARAMETER);
			UUID studentId = UUID.fromString(studentIdStr);

			studentCRUDService.deleteById(studentId);
			model.addAttribute(STUDENT_ATTRIBUTE, studentCRUDService.retrieveAll());
			return MULTIPLE_STUDENTS_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}
}
