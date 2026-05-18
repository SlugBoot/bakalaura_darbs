package lv.slugboot.app.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

	private static final String REFERRER_HEADER = "Referer";
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

	@GetMapping("/{uuid}")
	public String getControllerCourseInfo(@PathVariable(name = "uuid") UUID courseId,
			@RequestParam(value = "referer", required = false) String manualReferer, HttpServletRequest request,
			Model model) {
		try {
			String referer = (manualReferer != null) ? manualReferer : request.getHeader(REFERRER_HEADER);
			model.addAttribute(PREVIOUS_URL_ATTRIBUTE, referer);

			Course course = courseCRUDService.retrieveById(courseId);
			model.addAttribute(COURSE_ATTRIBUTE, course);
			model.addAttribute(INSTANCE_ATTRIBUTE, instanceCRUDService.retrieveByCourseId(courseId));
			return COURSE_INFO_PAGE;
		} catch (NullPointerException | NoSuchFieldException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/delete/{uuid}")
	public String getControllerDeleteCourse(@PathVariable(name = "uuid") UUID courseId, Model model) {
		try {
			courseCRUDService.deleteCourseById(courseId);
			ansibleService.runPlaybook(courseId, "remove_vms", HOSTS_FILE);
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
			String referer = request.getHeader(REFERRER_HEADER);
			model.addAttribute(PREVIOUS_URL_ATTRIBUTE, referer);
			model.addAttribute(COURSE_ATTRIBUTE, new CourseDTO());
			model.addAttribute(PROFESSOR_ATTRIBUTE, professorCRUDService.retrieveAll());
			return CREATE_COURSE_PAGE;
		} catch (NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/create")
	public String postControllerCreateCourse(@Valid CourseDTO course,
			@RequestParam(value = "referer", required = false) String referer, BindingResult result, Model model) {
		if (result.hasErrors()) {
			try {
				model.addAttribute(PROFESSOR_ATTRIBUTE, professorCRUDService.retrieveAll());
			} catch (Exception ignored) {}
			
			return CREATE_COURSE_PAGE;
		}

		try {
			courseCRUDService.createCourse(course.getCourseName(), course.getCourseDesc(),
					course.getProfessorId());
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

	@GetMapping("/{uuid}/update")
	public String getControllerUpdateCourse(@PathVariable(name = "uuid") UUID courseId, Model model) {
		try {
			Course course = courseCRUDService.retrieveById(courseId);
			model.addAttribute(COURSE_ATTRIBUTE, course);
			model.addAttribute(PROFESSOR_ATTRIBUTE, professorCRUDService.retrieveAll());
			return UPDATE_COURSE_PAGE;
		} catch (NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/{uuid}/update")
	public String postControllerUpdateCourse(@PathVariable(name = "uuid") UUID courseId, @Valid Course course,
			BindingResult result, Model model) {
		if (result.hasErrors()) {
			return UPDATE_COURSE_PAGE;
		}

		try {
			courseCRUDService.updateCourseById(courseId, course.getCourseName(), course.getCourseDesc(),
					course.getProfessor().getPersonId());
			return REDIRECT_COURSE_CRUD + "all";
		} catch (NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/{uuid}/add")
	public String getControllerStudentsNotInCourse(@PathVariable(name = "uuid") UUID courseId,
			HttpServletRequest request, Model model) {
		try {
			String referer = request.getHeader(REFERRER_HEADER);
			model.addAttribute(PREVIOUS_URL_ATTRIBUTE, referer);

			Course course = courseCRUDService.retrieveById(courseId);
			Collection<Student> studentsNotInCourse = filterService.studentsNotInCourse(courseId);
			model.addAttribute(STUDENT_ATTRIBUTE, studentsNotInCourse);
			model.addAttribute(COURSE_ATTRIBUTE, course);
			return ADD_STUDENTS_PAGE;
		} catch (NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/{uuid}/add/{studentId}")
	public String getControllerAddStudentToCourse(@PathVariable(name = "uuid") UUID courseId,
			@PathVariable(name = "studentId") UUID studentId,
			@RequestParam(value = "referer", required = false) String referer, Model model) {
		try {
			courseCRUDService.addStudentToCourse(courseId, studentId);

			if (referer != null && referer.startsWith("/")) {
				return REDIRECT_STRING + referer;
			}

			return REDIRECT_COURSE_CRUD + courseId;

		} catch (NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/{uuid}/remove/{studentId}")
	public String getControllerRemoveStudentFromCourse(@PathVariable(name = "uuid") UUID courseId,
			@PathVariable(name = "studentId") UUID studentId,
			@RequestParam(name = "referer", required = false) String referer, Model model) {
		try {
			courseCRUDService.removeStudentFromCourse(courseId, studentId);
			String redirectUrl = "/course/crud/" + courseId;
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

	@GetMapping("/{uuid}/deploy")
	public String getControllerDeployLab(@PathVariable(name = "uuid") UUID courseId,
			@RequestParam(value = "referer", required = false) String referer, Model model) {
		try {
			courseCRUDService.deployLab(courseId);

			if (referer != null && referer.startsWith("/")) {
				return REDIRECT_STRING + referer;
			} else {
				return REDIRECT_COURSE_CRUD + courseId;
			}
		} catch (NoSuchFieldException | IOException | InterruptedException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/{uuid}/cleanup")
	public String getControllerCleanupLab(@PathVariable(name = "uuid") UUID courseId,
			@RequestParam(value = "referer", required = false) String referer, Model model) {
		try {
			courseCRUDService.cleanupLab(courseId);
			if (referer != null && referer.startsWith("/")) {
				return REDIRECT_STRING + referer;
			} else {
				return REDIRECT_COURSE_CRUD + courseId;
			}
		} catch (NoSuchFieldException | IOException | InterruptedException e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/{uuid}/provision")
	public String getControllerProvisionCourseInfrastructure(@PathVariable(name = "uuid") UUID courseId, Model model) {
		try {
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

	@GetMapping("/instance/{instanceId}/terminal")
	public String getControllerDisplayContainerTerminal(@PathVariable(name = "instanceId") UUID instanceId, Model model,
			HttpServletRequest request) {
		try {
			model.addAttribute(INSTANCE_ATTRIBUTE, instanceId.toString());
			request.getSession().setAttribute("instanceId", instanceId);
			return CONTAINER_TERMINAL_PAGE;
		} catch (Exception e) {
			model.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
			return ERROR_PAGE;
		}
	}
}
