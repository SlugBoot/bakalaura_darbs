package lv.slugboot.app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.Professor;
import lv.slugboot.app.service.IProfessorCRUDService;
import lv.slugboot.app.service.IProfessorHomeService;

@Controller
@RequestMapping("/professor/home")
@RequiredArgsConstructor
public class ProfessorHomeController {

	private final IProfessorHomeService professorHomeService;
	private final IProfessorCRUDService professorCRUDService;

	@GetMapping("/{uuid}")
	public String getControllerProfessorHomePage(@PathVariable(name = "uuid") UUID professorId, Model model) {
		try {
			Professor professor = professorCRUDService.retrieveById(professorId);
			model.addAttribute("professor", professor);

			List<Course> filteredCourses = professorHomeService.getAllCoursesWhereProfessorIdEquals(professorId);
			model.addAttribute("filtered_courses", filteredCourses);

			return "professor-home-page";
		} catch (Exception e) {
			model.addAttribute("package", e.getMessage());
			return "show-error";
		}
	}

}
