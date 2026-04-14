package lv.slugboot.app.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import lv.slugboot.app.models.Professor;
import lv.slugboot.app.service.IProfessorCRUDService;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/professor/crud")
public class ProfessorCRUDController {

  @Autowired
  private IProfessorCRUDService professorCRUDService;

  @GetMapping("/all")
  public String getControllerGetAllprofessors(Model model) {
    try {
      model.addAttribute("professor", professorCRUDService.retrieveAll());
      return "show-multiple-professors";
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "show-error";
    }
  }

  @GetMapping("/create")
  public String getControllerCreateProfessor(Model model) {
    model.addAttribute("professor", new Professor());
    return "create-professor";
  }

  @PostMapping("/create")
  public String postControllerCreateProfessor(@Valid Professor professor, BindingResult result, Model model) {
    if (result.hasErrors()) {
      return "create-professor";
    }

    try {
      professorCRUDService.createProfessor(professor.getName(), professor.getMiddleName(), professor.getSurname(),
          professor.getEmail());
      return "redirect:/professor/crud/all";
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "show-error";
    }
  }
  
  @GetMapping("/delete/{uuid}")
  public String getControllerDeleteProfessorById(@PathVariable(name="uuid") UUID professorId, Model model) {
	  try {
		professorCRUDService.deleteProfessorById(professorId);
		model.addAttribute("professor", professorCRUDService.retrieveAll());
		return "show-multiple-professors";
	} catch (Exception e) {
	      model.addAttribute("error", e.getMessage());
	      return "show-error";
	}
  }

}
