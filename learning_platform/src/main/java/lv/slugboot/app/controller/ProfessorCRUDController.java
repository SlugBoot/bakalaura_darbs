package lv.slugboot.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lv.slugboot.app.service.IProfessorCRUDService;

@Controller
@RequestMapping("/professor/crud")
public class ProfessorCRUDController {
	
	@Autowired private IProfessorCRUDService professorService;

	@GetMapping("/all")
	public String getControllerGetAllprofessors(Model model) {
		try {
			model.addAttribute("professor", professorService.retrieveAll());
			return "show-multiple-professors";
		}
		catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "show-error";
		}
	}
}
