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

	private static final String STUDENT_STR = "student";
	private static final String ERROR_STR = "error";
	private static final String PASSWORD_STR = "password";
	private static final String USER_TYPE_STR = "userType";
	private static final String USER_ID_STR = "userId";
	private static final String UUID_STR = "uuid";

	@GetMapping("/all")
	public String getControllerGetAllStudents(Model model) {
		try {
			model.addAttribute(STUDENT_STR, studentCRUDService.retrieveAll());
			return MULTIPLE_STUDENTS_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/create")
	public String getControllerCreateStudent(Model model) {
		model.addAttribute(STUDENT_STR, new PersonDTO());
		return CREATE_STUDENT_PAGE;
	}

	@PostMapping("/create")
	public String postControllerCreateStudent(@Valid PersonDTO student, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return CREATE_STUDENT_PAGE;
		}

		try {
			studentCRUDService.createStudent(student);
			return STUDENT_REDIRECT_PAGE + "all";
		} catch (Exception e) {
			model.addAttribute(ERROR_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/update/{username}")
	public String getControllerUpdateStudentById(@PathVariable(name = "username") String username, Model model) {
		try {
			Student student = studentCRUDService.retrieveByUsername(username);
			model.addAttribute(STUDENT_STR, student);
			model.addAttribute("studentId", student.getPersonId());

			PersonDTO studentDTO = new PersonDTO();
			studentDTO.setName(student.getName());
			studentDTO.setMiddleName(student.getMiddleName());
			studentDTO.setSurname(student.getSurname());
			studentDTO.setEmail(student.getEmail());
			model.addAttribute("studentDTO", studentDTO);

			return UPDATE_STUDENT_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/update")
	public String postControllerUpdateStudentById(HttpServletRequest request,
			@Valid @ModelAttribute("studentDTO") PersonDTO studentDTO, BindingResult result, Model model) {
		String studentIdStr = request.getParameter(UUID_STR);
		UUID studentId = UUID.fromString(studentIdStr);

		if (result.hasErrors()) {
			try {
				Student originalStudent = studentCRUDService.retrieveById(studentId);
				model.addAttribute("studentId", studentId);
				model.addAttribute("originalStudent", originalStudent);
				model.addAttribute("studentDTO", studentDTO);

				return UPDATE_STUDENT_PAGE;
			} catch (Exception e) {
				model.addAttribute(ERROR_STR, e.getMessage());
				return ERROR_PAGE;
			}
		}

		try {
			studentCRUDService.updateStudentById(studentId, studentDTO);
			return STUDENT_REDIRECT_PAGE + "all";
		} catch (Exception e) {
			model.addAttribute(ERROR_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/update-password/{username}")
	public String getControllerUpdatePassword(@PathVariable(name = "username") String username, Model model) {
		try {
			Student student = studentCRUDService.retrieveByUsername(username);
			model.addAttribute(USER_ID_STR, student.getPersonId());
			model.addAttribute(USER_TYPE_STR, STUDENT_STR);
			model.addAttribute(PASSWORD_STR, new PasswordUpdateDTO());
			return UPDATE_PASSWORD_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/update-password")
	public String postControllerUpdatePassword(HttpServletRequest request,
			@Valid @ModelAttribute("password") PasswordUpdateDTO passwordDto, BindingResult result, Model model) {
		String studentIdStr = request.getParameter(UUID_STR);
		UUID studentId = UUID.fromString(studentIdStr);

		Runnable populateErrorModel = () -> {
			model.addAttribute(USER_ID_STR, studentId);
			model.addAttribute(USER_TYPE_STR, STUDENT_STR);
			model.addAttribute(PASSWORD_STR, passwordDto);
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
			model.addAttribute(ERROR_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/delete")
	public String postControllerDeleteStudent(HttpServletRequest request, Model model) {
		try {
			String studentIdStr = request.getParameter(UUID_STR);
			UUID studentId = UUID.fromString(studentIdStr);

			studentCRUDService.deleteById(studentId);
			model.addAttribute(STUDENT_STR, studentCRUDService.retrieveAll());
			return MULTIPLE_STUDENTS_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}
}
