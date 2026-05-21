package lv.slugboot.app.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.Professor;
import lv.slugboot.app.service.IProfessorHomeService;

@Controller
@RequestMapping("/professor/home")
@RequiredArgsConstructor
public class ProfessorHomeController {

	private final IProfessorHomeService professorHomeService;

	@GetMapping
	public String getControllerProfessorHomePage(Authentication authentication, Model model) {
		try {
			String username = authentication.getName();
			Professor professor = professorHomeService.retrieveByUsername(username);
			model.addAttribute("professor", professor);

			List<Course> filteredCourses = professorHomeService
					.getAllCoursesWhereProfessorIdEquals(professor.getPersonId());
			model.addAttribute("filtered_courses", filteredCourses);

			return "professor-home-page";
		} catch (Exception e) {
			model.addAttribute("package", e.getMessage());
			return "show-error";
		}
	}

}
