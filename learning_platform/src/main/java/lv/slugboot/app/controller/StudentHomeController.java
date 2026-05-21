package lv.slugboot.app.controller;

import java.util.Collection;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.service.IStudentHomeService;

@Controller
@RequestMapping("/student/home")
@RequiredArgsConstructor
public class StudentHomeController {

	private final IStudentHomeService studentHomeService;

	private static final String STUDENT_HOME_PAGE = "student-home-page";
	private static final String ERROR_PAGE = "show-error";

	private static final String STUDENT_ATTRIBUTE = "student";
	private static final String FILTERED_COURSE_ATTRIBUTE = "filtered_courses";
	private static final String ERROR_ATTRIBUTE = "error";

	private static final String COURSE_ID_PARAMETER = "courseId";

	@GetMapping
	public String getControllerStudentHomePage(Authentication authentication, Model model) {
		try {
			String username = authentication.getName();
			Student student = studentHomeService.getStudentByUsername(username);
			model.addAttribute(STUDENT_ATTRIBUTE, student);

			Collection<Course> filteredCourses = studentHomeService.getAllCourses(student.getPersonId());
			model.addAttribute(FILTERED_COURSE_ATTRIBUTE, filteredCourses);

			return STUDENT_HOME_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/remove")
	public String getControllerRemoveCourseFromStudent(HttpServletRequest request, Authentication authentication,
			Model model) {
		try {
			String courseIdStr = request.getParameter(COURSE_ID_PARAMETER);
			UUID courseId = UUID.fromString(courseIdStr);

			String username = authentication.getName();
			Student student = studentHomeService.getStudentByUsername(username);
			UUID studentId = student.getPersonId();
			model.addAttribute(STUDENT_ATTRIBUTE, student);

			studentHomeService.removeCourseFromStudent(studentId, courseId);

			Collection<Course> filteredCourses = studentHomeService.getAllCourses(studentId);
			model.addAttribute(FILTERED_COURSE_ATTRIBUTE, filteredCourses);

			return STUDENT_HOME_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}
}
