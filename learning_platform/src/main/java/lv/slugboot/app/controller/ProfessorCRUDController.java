package lv.slugboot.app.controller;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lv.slugboot.app.dto.PasswordUpdateDTO;
import lv.slugboot.app.dto.ProfessorDTO;
import lv.slugboot.app.service.IProfessorCRUDService;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/professor/crud")
@RequiredArgsConstructor
public class ProfessorCRUDController {

	private final IProfessorCRUDService professorCRUDService;

	private static final String MULTIPLE_PROFESSORS_PAGE = "show-multiple-professors";
	private static final String ERROR_PAGE = "show-error";
	private static final String CREATE_PROFESSOR_PAGE = "create-professor";
	private static final String UPDATE_PROFESSOR_PAGE = "update-professor";
	private static final String UPDATE_PASSWORD_PAGE = "update-password";
	private static final String PROFESSOR_REDIRECT_PAGE = "redirect:/professor/crud/all";

	private static final String PROFESSOR_ATTRIBUTE = "professor";
	private static final String ERROR_ATTRIBUTE = "error";
	private static final String PASSWORD_ATTRIBUTE = "password";
	private static final String USER_TYPE_ATTRIBUTE = "userType";
	private static final String USER_ID_ATTRIBUTE = "userId";

	@GetMapping("/all")
	public String getControllerGetAllProfessors(Model model) {
		try {
			model.addAttribute(PROFESSOR_ATTRIBUTE, professorCRUDService.retrieveAll());
			return MULTIPLE_PROFESSORS_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/create")
	public String getControllerCreateProfessor(Model model) {
		model.addAttribute(PROFESSOR_ATTRIBUTE, new ProfessorDTO());
		return CREATE_PROFESSOR_PAGE;
	}

	@PostMapping("/create")
	public String postControllerCreateProfessor(@Valid ProfessorDTO professor, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return CREATE_PROFESSOR_PAGE;
		}

		try {
			professorCRUDService.createProfessor(professor.getName(), professor.getMiddleName(), professor.getSurname(),
					professor.getEmail());
			return PROFESSOR_REDIRECT_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/update/{uuid}")
	public String getControllerUpdateProfessorById(@PathVariable(name = "uuid") UUID professorId, Model model) {
		try {
			model.addAttribute(PROFESSOR_ATTRIBUTE, professorCRUDService.retrieveById(professorId));
			return UPDATE_PROFESSOR_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/update/{uuid}")
	public String postControllerUpdateProfessorById(@PathVariable(name = "uuid") UUID professorId,
			@Valid ProfessorDTO professor, BindingResult result, Model model) {
		if (result.hasErrors()) {
			try {
				return UPDATE_PROFESSOR_PAGE;
			} catch (Exception e) {
				model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
				return ERROR_PAGE;
			}
		}

		try {
			professorCRUDService.updateProfessorById(professorId, professor.getName(), professor.getMiddleName(),
					professor.getSurname(), professor.getEmail());
			return PROFESSOR_REDIRECT_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}
	
	@GetMapping("/update-password/{uuid}")
	public String getControllerUpdatePassword(@PathVariable(name = "uuid") UUID professorId, Model model) {
	    try {
	        model.addAttribute(USER_ID_ATTRIBUTE, professorId);
			model.addAttribute(USER_TYPE_ATTRIBUTE, "professor");
			model.addAttribute(PASSWORD_ATTRIBUTE, new PasswordUpdateDTO());
	        return UPDATE_PASSWORD_PAGE;
	    } catch (Exception e) {
	        model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
	        return ERROR_PAGE;
	    }
	}

	@PostMapping("/update-password/{uuid}")
	public String postControllerUpdatePassword(@PathVariable(name = "uuid") UUID professorId,
			@Valid PasswordUpdateDTO passwordDto, BindingResult result, Model model) {
		if (result.hasErrors()) {
			model.addAttribute(USER_ID_ATTRIBUTE, professorId);
			model.addAttribute(USER_TYPE_ATTRIBUTE, "professor");
			return UPDATE_PASSWORD_PAGE;
		}
		
		if (!passwordDto.isNewPasswordMatching()) {
			result.rejectValue("confirmPassword", "error.passwordDto", "New Passwords do not match");
			model.addAttribute(PROFESSOR_ATTRIBUTE, professorId);
		}
		
		try {
	        professorCRUDService.updatePasswordById(professorId, passwordDto);
	        return PROFESSOR_REDIRECT_PAGE;
	    } catch (IllegalArgumentException e) {
	        result.rejectValue("currentPassword", "error.passwordDto", e.getMessage());
	        model.addAttribute(USER_ID_ATTRIBUTE, professorId);
	        return UPDATE_PASSWORD_PAGE;
	    } catch (Exception e) {
	        model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
	        return ERROR_PAGE;
	    }
	}

	@GetMapping("/delete/{uuid}")
	public String getControllerDeleteProfessorById(@PathVariable(name = "uuid") UUID professorId, Model model) {
		try {
			professorCRUDService.deleteProfessorById(professorId);
			model.addAttribute(PROFESSOR_ATTRIBUTE, professorCRUDService.retrieveAll());
			return MULTIPLE_PROFESSORS_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

}
