package lv.slugboot.app.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lv.slugboot.app.dto.CourseDTO;
import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.service.IAnsibleService;
import lv.slugboot.app.service.ICourseCRUDService;
import lv.slugboot.app.service.IFilterService;
import lv.slugboot.app.service.ILabInstanceCRUDService;
import lv.slugboot.app.service.IProfessorCRUDService;

@Controller
@RequestMapping("/course/crud")
@Slf4j
@RequiredArgsConstructor
public class CourseCRUDController {

	private final ICourseCRUDService courseCRUDService;
	private final IFilterService filterService;
	private final IProfessorCRUDService professorCRUDService;
	private final ILabInstanceCRUDService instanceCRUDService;
	private final IAnsibleService ansibleService;

	private static final String COURSE_LIST = "show-multiple-courses";
	private static final String COURSE_INFO_PAGE = "course-info";
	private static final String ERROR_PAGE = "show-error";
	private static final String ADD_STUDENTS_PAGE = "course-add-student";
	private static final String CREATE_COURSE_PAGE = "create-course";
	private static final String UPDATE_COURSE_PAGE = "update-course";
	private static final String CONTAINER_TERMINAL_PAGE = "container-terminal";

	private static final String STUDENT_ATTRIBUTE = "student";
	private static final String COURSE_ATTRIBUTE = "course";
	private static final String INSTANCE_ATTRIBUTE = "instance";
	private static final String ERROR_ATTRIBUTE = "error";
	private static final String PROFESSOR_ATTRIBUTE = "professor";
	private static final String PREVIOUS_URL_ATTRIBUTE = "previousUrl";

	private static final String PROXMOX_FILE = "provisioning";
	private static final String HOSTS_FILE = "hosts";

	private static final String REFERER_PARAMETER = "referer";
	private static final String USERNAME_PARAMETER = "username";
	private static final String UUID_PARAMETER = "uuid";
	private static final String STUDENT_ID_PARAMETER = "studentId";
	private static final String INSTANCE_ID_PARAMETER = "instanceId";

	private static final String REDIRECT_STRING = "redirect:";
	private static final String REDIRECT_COURSE_CRUD = "redirect:/course/crud/";

	@GetMapping("/all")
	public String getControllerAllCourses(Model model) {
		try {
			model.addAttribute(COURSE_ATTRIBUTE, courseCRUDService.retrieveAll());
			return COURSE_LIST;
		} catch (Exception e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/name/{slug}")
	public String getControllerCourseInfo(@PathVariable(name = "slug") String slug, HttpServletRequest request,
			Model model) {
		try {
			String referer = request.getParameter(REFERER_PARAMETER);

			if (referer != null) {
				model.addAttribute(PREVIOUS_URL_ATTRIBUTE, referer);
			}

			Course course = courseCRUDService.retrieveBySlug(slug);
			model.addAttribute(COURSE_ATTRIBUTE, course);
			model.addAttribute(INSTANCE_ATTRIBUTE, instanceCRUDService.retrieveByCourseId(course.getCId()));
			return COURSE_INFO_PAGE;
		} catch (NullPointerException | NoSuchFieldException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/delete")
	@PreAuthorize("hasRole('PROFESSOR') and @courseCRUDService.retrieveById(#courseId).getProfessor().getUsername() == authentication.name")
	public String postControllerDeleteCourse(HttpServletRequest request, Model model) {
		try {
			String courseIdStr = request.getParameter(UUID_PARAMETER);
			UUID courseId;
			if (courseIdStr != null) {
				courseId = UUID.fromString(courseIdStr);
				courseCRUDService.deleteCourseById(courseId);
				ansibleService.runPlaybook(courseId, "remove_vms", HOSTS_FILE);
			}

			String referer = request.getParameter(REFERER_PARAMETER);
			if (referer != null) {
				model.addAttribute(PREVIOUS_URL_ATTRIBUTE, referer);
			}

			model.addAttribute(COURSE_ATTRIBUTE, courseCRUDService.retrieveAll());
			return COURSE_LIST;
		} catch (NoSuchFieldException | IOException | InterruptedException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/create")
	public String getControllerCreateCourse(HttpServletRequest request, Model model) {
		try {
			String referer = request.getParameter(REFERER_PARAMETER);

			if (referer != null) {
				model.addAttribute(PREVIOUS_URL_ATTRIBUTE, referer);
			}

			String username = request.getParameter(USERNAME_PARAMETER);
			if (username != null) {
				model.addAttribute(PROFESSOR_ATTRIBUTE, professorCRUDService.retrieveByUsername(username));
			}

			model.addAttribute(COURSE_ATTRIBUTE, new CourseDTO());

			return CREATE_COURSE_PAGE;
		} catch (NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/create")
	public String postControllerCreateCourse(@Valid CourseDTO course, HttpServletRequest request, BindingResult result,
			Model model) {
		String referer = request.getParameter(REFERER_PARAMETER);
		if (result.hasErrors()) {
			try {
				if (referer != null) {
					model.addAttribute(PREVIOUS_URL_ATTRIBUTE, referer);
				}

				model.addAttribute(PROFESSOR_ATTRIBUTE, professorCRUDService.retrieveById(course.getProfessorId()));

			} catch (Exception ignored) {
			}

			return CREATE_COURSE_PAGE;
		}

		try {
			courseCRUDService.createCourse(course.getCourseName(), course.getCourseDesc(), course.getProfessorId());
			if (referer != null && referer.startsWith("/")) {
				return REDIRECT_STRING + referer;
			}
			return REDIRECT_COURSE_CRUD + "all";
		} catch (NullPointerException | IllegalArgumentException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/{slug}/update")
	public String getControllerUpdateCourse(@PathVariable(name = "slug") String slug, HttpServletRequest request,
			Model model) {
		try {
			String username = request.getParameter(USERNAME_PARAMETER);
			if (username != null) {
				model.addAttribute(PROFESSOR_ATTRIBUTE, professorCRUDService.retrieveByUsername(username));
			}

			Course course = courseCRUDService.retrieveBySlug(slug);
			model.addAttribute(COURSE_ATTRIBUTE, course);

			return UPDATE_COURSE_PAGE;
		} catch (NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/update")
	public String postControllerUpdateCourse(HttpServletRequest request, @Valid Course course, BindingResult result,
			Model model) {
		String courseIdStr = request.getParameter(USERNAME_PARAMETER);
		UUID courseId = UUID.fromString(courseIdStr);

		if (result.hasErrors()) {
			return UPDATE_COURSE_PAGE;
		}

		try {
			courseCRUDService.updateCourseById(courseId, course.getCourseName(), course.getCourseDesc(),
					course.getProfessor().getPersonId());
			return REDIRECT_COURSE_CRUD + "name/" + course.getSlug();
		} catch (NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/{slug}/add")
	public String getControllerStudentsNotInCourse(@PathVariable(name = "slug") String slug, HttpServletRequest request,
			Model model) {
		try {
			String referer = request.getParameter(REFERER_PARAMETER);
			if (referer != null) {
				model.addAttribute(PREVIOUS_URL_ATTRIBUTE, referer);
			}
			Course course = courseCRUDService.retrieveBySlug(slug);
			Collection<Student> studentsNotInCourse = filterService.studentsNotInCourse(course.getCId());
			model.addAttribute(STUDENT_ATTRIBUTE, studentsNotInCourse);
			model.addAttribute(COURSE_ATTRIBUTE, course);
			return ADD_STUDENTS_PAGE;
		} catch (NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/add-student")
	public String postControllerAddStudentToCourse(HttpServletRequest request, Model model) {
		try {
			String courseIdStr = request.getParameter(UUID_PARAMETER);
			String studentIdStr = request.getParameter(STUDENT_ID_PARAMETER);

			UUID courseId = UUID.fromString(courseIdStr);
			UUID studentId = UUID.fromString(studentIdStr);

			String referer = request.getParameter(REFERER_PARAMETER);

			courseCRUDService.addStudentToCourse(courseId, studentId);

			Course course = courseCRUDService.retrieveById(courseId);

			if (referer != null && referer.startsWith("/")) {
				return REDIRECT_STRING + referer;
			}

			return REDIRECT_COURSE_CRUD + course.getSlug();

		} catch (NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/remove-student")
	public String postControllerRemoveStudentFromCourse(HttpServletRequest request, Model model) {
		try {
			String courseIdStr = request.getParameter(UUID_PARAMETER);
			String studentIdStr = request.getParameter(STUDENT_ID_PARAMETER);

			UUID courseId = UUID.fromString(courseIdStr);
			UUID studentId = UUID.fromString(studentIdStr);

			String referer = request.getParameter(REFERER_PARAMETER);

			courseCRUDService.removeStudentFromCourse(courseId, studentId);
			Course course = courseCRUDService.retrieveById(courseId);
			String redirectUrl = "/course/crud/" + course.getSlug();
			if (referer != null && referer.startsWith("/")) {
				redirectUrl += "?referer=" + referer;
			}

			return REDIRECT_STRING + redirectUrl;
		} catch (NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/deploy")
	public String postControllerDeployLab(HttpServletRequest request, Model model) {
		try {
			String courseIdStr = request.getParameter(UUID_PARAMETER);
			UUID courseId = UUID.fromString(courseIdStr);

			String referer = request.getParameter(REFERER_PARAMETER);

			courseCRUDService.deployLab(courseId);
			Course course = courseCRUDService.retrieveById(courseId);

			if (referer != null && referer.startsWith("/")) {
				return REDIRECT_STRING + referer;
			} else {
				return REDIRECT_COURSE_CRUD + course.getSlug();
			}
		} catch (NoSuchFieldException | IOException | InterruptedException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/cleanup")
	public String postControllerCleanupLab(HttpServletRequest request, Model model) {
		try {
			String courseIdStr = request.getParameter(UUID_PARAMETER);
			UUID courseId = UUID.fromString(courseIdStr);

			String referer = request.getParameter(REFERER_PARAMETER);

			courseCRUDService.cleanupLab(courseId);
			Course course = courseCRUDService.retrieveById(courseId);

			if (referer != null && referer.startsWith("/")) {
				return REDIRECT_STRING + referer;
			} else {
				return REDIRECT_COURSE_CRUD + course.getSlug();
			}
		} catch (NoSuchFieldException | IOException | InterruptedException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/provision")
	public String postControllerProvisionCourseInfrastructure(HttpServletRequest request, Model model) {
		try {
			String courseIdStr = request.getParameter(UUID_PARAMETER);
			UUID courseId = UUID.fromString(courseIdStr);

			courseCRUDService.prepareProxmoxProvisioning(courseId);

			log.info("Files prepared. Starting playbook execution for course: {}", courseId);

			ansibleService.runPlaybook(courseId, PROXMOX_FILE, HOSTS_FILE);

			return REDIRECT_COURSE_CRUD + courseId;
		} catch (NoSuchFieldException | IOException | InterruptedException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/instance/terminal")
	public String getControllerDisplayContainerTerminal(HttpServletRequest request, Model model) {
		try {
			String instanceId = request.getParameter(INSTANCE_ID_PARAMETER);
			
			model.addAttribute(INSTANCE_ATTRIBUTE, instanceId.toString());
			request.getSession().setAttribute("instanceId", instanceId);
			return CONTAINER_TERMINAL_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}
}
