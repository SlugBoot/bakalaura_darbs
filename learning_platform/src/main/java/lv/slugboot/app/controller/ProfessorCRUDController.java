package lv.slugboot.app.controller;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lv.slugboot.app.dto.PasswordUpdateDTO;
import lv.slugboot.app.dto.ProfessorDTO;
import lv.slugboot.app.models.Professor;
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

	@GetMapping("/update/{username}")
	public String getControllerUpdateProfessorByUsername(@PathVariable(name = "username") String username, Model model) {
		try {
			model.addAttribute(PROFESSOR_ATTRIBUTE, professorCRUDService.retrieveByUsername(username));
			return UPDATE_PROFESSOR_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/update")
	public String postControllerUpdateProfessorById(@RequestParam(name = "professorId") UUID professorId,
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
	
	@GetMapping("/update-password/{username}")
	public String getControllerUpdatePassword(@PathVariable(name = "username") String username, Model model) {
	    try {
	    	Professor professor = professorCRUDService.retrieveByUsername(username);
	        model.addAttribute(USER_ID_ATTRIBUTE, professor.getPersonId());
			model.addAttribute(USER_TYPE_ATTRIBUTE, "professor");
			model.addAttribute(PASSWORD_ATTRIBUTE, new PasswordUpdateDTO());
	        return UPDATE_PASSWORD_PAGE;
	    } catch (Exception e) {
	        model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
	        return ERROR_PAGE;
	    }
	}

	@PostMapping("/update-password")
	public String postControllerUpdatePassword(@RequestParam(name = "uuid") UUID professorId,
			@Valid @ModelAttribute("password") PasswordUpdateDTO passwordDto, BindingResult result, Model model) {
		Runnable populateErrorModel = () -> {
	        model.addAttribute("userId", professorId);
	        model.addAttribute("userType", "professor");
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
	        professorCRUDService.updatePasswordById(professorId, passwordDto);
	        return PROFESSOR_REDIRECT_PAGE;
	    } catch (IllegalArgumentException e) {
	        result.rejectValue("currentPassword", "error.passwordDto", e.getMessage());
	        populateErrorModel.run();
	        model.addAttribute(USER_ID_ATTRIBUTE, professorId);
	        return UPDATE_PASSWORD_PAGE;
	    } catch (Exception e) {
	        model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
	        return ERROR_PAGE;
	    }
	}

	@PostMapping("/delete")
	public String getControllerDeleteProfessorById(@RequestParam(name = "uuid") UUID professorId, Model model) {
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
