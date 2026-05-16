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
import lv.slugboot.app.models.Professor;
import lv.slugboot.app.service.IProfessorCRUDService;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/professor/crud")
@RequiredArgsConstructor
public class ProfessorCRUDController {

	private final IProfessorCRUDService professorCRUDService;

	private String multipleProfessorsPage = "show-multiple-professors";
	private String errorPage = "show-error";
	private String createProfessorPage = "create-professor";
	private String updateProfessorPage = "update-professor";

	private String professorAttribute = "professor";
	private String errorAttribute = "error";

	@GetMapping("/all")
	public String getControllerGetAllProfessors(Model model) {
		try {
			model.addAttribute(professorAttribute, professorCRUDService.retrieveAll());
			return multipleProfessorsPage;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}

	@GetMapping("/create")
	public String getControllerCreateProfessor(Model model) {
		model.addAttribute(professorAttribute, new Professor());
		return createProfessorPage;
	}

	@PostMapping("/create")
	public String postControllerCreateProfessor(@Valid Professor professor, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return createProfessorPage;
		}

		try {
			professorCRUDService.createProfessor(professor.getName(), professor.getMiddleName(), professor.getSurname(),
					professor.getEmail());
			return "redirect:/professor/crud/all";
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}

	@GetMapping("/update/{uuid}")
	public String getControllerUpdateStudentById(@PathVariable(name = "uuid") UUID professorId, Model model) {
		try {
			model.addAttribute(professorAttribute, professorCRUDService.retrieveById(professorId));
			return updateProfessorPage;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}

	@PostMapping("/update/{uuid}")
	public String postControllerUpdateStudentById(@PathVariable(name = "uuid") UUID professorId,
			@Valid Professor professor, BindingResult result, Model model) {
		if (result.hasErrors()) {
			try {
				return updateProfessorPage;
			} catch (Exception e) {
				model.addAttribute(errorAttribute, e.getMessage());
				return errorPage;
			}
		}

		try {
			professorCRUDService.updateProfessorById(professorId, professor.getName(), professor.getMiddleName(),
					professor.getSurname(), professor.getEmail());
			return "redirect:/professor/crud/all";
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}

	@GetMapping("/delete/{uuid}")
	public String getControllerDeleteProfessorById(@PathVariable(name = "uuid") UUID professorId, Model model) {
		try {
			professorCRUDService.deleteProfessorById(professorId);
			model.addAttribute(professorAttribute, professorCRUDService.retrieveAll());
			return multipleProfessorsPage;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}

}
