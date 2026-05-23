package lv.slugboot.app.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lv.slugboot.app.dto.CourseDTO;
import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.LabInstance;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.models.enums.LabInstanceStatus;
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

	private static final String STUDENT_STR = "student";
	private static final String STUDENT_ID_STR = "studentId";
	private static final String COURSE_STR = "course";
	private static final String COURSE_DTO_STR = "courseDTO";
	private static final String INSTANCE_STR = "instance";
	private static final String INSTANCE_ID_STR = "instanceId";
	private static final String ERROR_STR = "error";
	private static final String ERROR_MESSAGE_STR = "errorMessage";
	private static final String ERROR_CODE_STR = "errorCode";
	private static final String ERROR_CODE_400_STR = "400 (Bad Request)";
	private static final String ERROR_CODE_500_STR = "500 (Internal Server Error)";
	private static final String PROFESSOR_STR = "professor";
	private static final String PREVIOUS_URL_STR = "previousUrl";
	private static final String REFERER_STR = "referer";
	private static final String USERNAME_STR = "username";
	private static final String UUID_STR = "uuid";

	private static final String HOSTS_FILE = "hosts";

	private static final String REDIRECT_STRING = "redirect:";
	private static final String REDIRECT_COURSE_CRUD = "redirect:/course/crud/";
	private static final String REDIRECT_COURSE_CRUD_NAME = REDIRECT_COURSE_CRUD + "name/";

	@GetMapping("/all")
	public String getControllerAllCourses(Model model) {
		try {
			model.addAttribute(COURSE_STR, courseCRUDService.retrieveAll());
			return COURSE_LIST;
		} catch (Exception e) {
			Thread.currentThread().interrupt();

			model.addAttribute(ERROR_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/name/{slug}")
	public String getControllerCourseInfo(@PathVariable(name = "slug") String slug, HttpServletRequest request,
			Model model) {
		try {
			String referer = request.getParameter(REFERER_STR);

			if (referer != null) {
				model.addAttribute(PREVIOUS_URL_STR, referer);
			}

			Course course = courseCRUDService.retrieveBySlug(slug);
			model.addAttribute(COURSE_STR, course);
			model.addAttribute(INSTANCE_STR, instanceCRUDService.retrieveByCourseId(course.getCId()));
			return COURSE_INFO_PAGE;
		} catch (IllegalArgumentException | NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_400_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_500_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/delete")
	@PreAuthorize("hasRole('PROFESSOR')")
	public String postControllerDeleteCourse(HttpServletRequest request, Model model) {
		try {
			String courseIdStr = request.getParameter(UUID_STR);
			UUID courseId;
			if (courseIdStr != null) {
				courseId = UUID.fromString(courseIdStr);
				boolean allInstancesUninitialized = true;
				List<LabInstance> instances = instanceCRUDService.retrieveByCourseId(courseId);
				for (LabInstance instance : instances) {
					if (instance.getStatus() != LabInstanceStatus.UNINITIALIZED) {
						allInstancesUninitialized = false;
					}
				}

				if (allInstancesUninitialized == false) {
					ansibleService.runPlaybook(courseId, "remove_vms", HOSTS_FILE);
				}

				courseCRUDService.deleteCourseById(courseId);

			}

			String referer = request.getParameter(REFERER_STR);
			if (referer != null) {
				model.addAttribute(PREVIOUS_URL_STR, referer);
			}

			model.addAttribute(COURSE_STR, courseCRUDService.retrieveAll());
			return COURSE_LIST;
		} catch (IllegalArgumentException | NoSuchFieldException | NullPointerException | IOException e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_400_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_500_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/create")
	public String getControllerCreateCourse(HttpServletRequest request, Model model) {
		try {
			String referer = request.getParameter(REFERER_STR);

			if (referer != null) {
				model.addAttribute(PREVIOUS_URL_STR, referer);
			}

			String username = request.getParameter(USERNAME_STR);
			if (username != null) {
				model.addAttribute(PROFESSOR_STR, professorCRUDService.retrieveByUsername(username));
			}

			model.addAttribute(COURSE_DTO_STR, new CourseDTO());

			return CREATE_COURSE_PAGE;
		} catch (IllegalArgumentException | NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_400_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_500_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/create")
	public String postControllerCreateCourse(@Valid CourseDTO course, HttpServletRequest request, BindingResult result,
			Model model) {
		String referer = request.getParameter(REFERER_STR);
		if (result.hasErrors()) {
			try {
				if (referer != null) {
					model.addAttribute(PREVIOUS_URL_STR, referer);
				}

				model.addAttribute(PROFESSOR_STR, professorCRUDService.retrieveById(course.getProfessorId()));

			} catch (Exception ignored) {
			}

			return CREATE_COURSE_PAGE;
		}

		try {
			courseCRUDService.createCourse(course);
			if (referer != null && referer.startsWith("/")) {
				return REDIRECT_STRING + referer;
			}

			return REDIRECT_COURSE_CRUD + "all";
		} catch (IllegalArgumentException | NullPointerException e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_400_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_500_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/{slug}/update")
	public String getControllerUpdateCourse(@PathVariable(name = "slug") String slug, HttpServletRequest request,
			Model model) {
		try {
			String username = request.getParameter(USERNAME_STR);
			if (username != null) {
				model.addAttribute(PROFESSOR_STR, professorCRUDService.retrieveByUsername(username));
			}

			Course course = courseCRUDService.retrieveBySlug(slug);

			CourseDTO courseDTO = new CourseDTO();
			courseDTO.setCourseName(course.getCourseName());
			courseDTO.setCourseDesc(course.getCourseDesc());
			courseDTO.setProfessorId(course.getProfessor().getPersonId());

			model.addAttribute(COURSE_STR, course);
			model.addAttribute(COURSE_DTO_STR, courseDTO);

			return UPDATE_COURSE_PAGE;
		} catch (IllegalArgumentException | NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_400_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_500_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/update")
	public String postControllerUpdateCourse(HttpServletRequest request,
			@Valid @ModelAttribute("courseDTO") CourseDTO course, BindingResult result, Model model) {
		String courseIdStr = request.getParameter(UUID_STR);
		UUID courseId = UUID.fromString(courseIdStr);

		if (result.hasErrors()) {
			try {
				Course courseObj = courseCRUDService.retrieveById(courseId);

				CourseDTO courseDTO = new CourseDTO();
				courseDTO.setCourseName(course.getCourseName());
				courseDTO.setCourseDesc(course.getCourseDesc());
				courseDTO.setProfessorId(courseObj.getProfessor().getPersonId());

				model.addAttribute(COURSE_DTO_STR, courseDTO);
				model.addAttribute(COURSE_STR, courseObj);
				return UPDATE_COURSE_PAGE;
			} catch (IllegalArgumentException | NoSuchFieldException | NullPointerException e) {
				Thread.currentThread().interrupt();
				model.addAttribute(ERROR_CODE_STR, ERROR_CODE_400_STR);
				model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
				return ERROR_PAGE;
			} catch (Exception e) {
				Thread.currentThread().interrupt();
				model.addAttribute(ERROR_CODE_STR, ERROR_CODE_500_STR);
				model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
				return ERROR_PAGE;
			}
		}

		try {
			Course courseObj = courseCRUDService.retrieveById(courseId);
			courseCRUDService.updateCourseById(courseId, course);
			return REDIRECT_COURSE_CRUD_NAME + courseObj.getSlug();
		} catch (IllegalArgumentException | NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_400_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_500_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/{slug}/add")
	public String getControllerStudentsNotInCourse(@PathVariable(name = "slug") String slug, HttpServletRequest request,
			Model model) {
		try {
			String referer = request.getParameter(REFERER_STR);
			if (referer != null) {
				model.addAttribute(PREVIOUS_URL_STR, referer);
			}
			Course course = courseCRUDService.retrieveBySlug(slug);
			Collection<Student> studentsNotInCourse = filterService.studentsNotInCourse(course.getCId());
			model.addAttribute(STUDENT_STR, studentsNotInCourse);
			model.addAttribute(COURSE_STR, course);
			return ADD_STUDENTS_PAGE;
		} catch (IllegalArgumentException | NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_400_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_500_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/add-student")
	public String postControllerAddStudentToCourse(HttpServletRequest request, Model model) {
		try {
			String courseIdStr = request.getParameter(UUID_STR);
			String studentIdStr = request.getParameter(STUDENT_ID_STR);

			UUID courseId = UUID.fromString(courseIdStr);
			UUID studentId = UUID.fromString(studentIdStr);

			String referer = request.getParameter(REFERER_STR);

			courseCRUDService.addStudentToCourse(courseId, studentId);

			Course course = courseCRUDService.retrieveById(courseId);

			if (referer != null && referer.startsWith("/")) {
				return REDIRECT_STRING + referer;
			}

			return REDIRECT_COURSE_CRUD_NAME + course.getSlug();

		} catch (IllegalArgumentException | NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_400_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_500_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/remove-student")
	public String postControllerRemoveStudentFromCourse(HttpServletRequest request, Model model) {
		try {
			String courseIdStr = request.getParameter(UUID_STR);
			String studentIdStr = request.getParameter(STUDENT_ID_STR);

			UUID courseId = UUID.fromString(courseIdStr);
			UUID studentId = UUID.fromString(studentIdStr);

			Course course = courseCRUDService.retrieveById(courseId);

			courseCRUDService.removeStudentFromCourse(courseId, studentId);

			return REDIRECT_COURSE_CRUD_NAME + course.getSlug();
		} catch (IllegalArgumentException | NoSuchFieldException | NullPointerException e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_400_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_500_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/deploy")
	public String postControllerDeployLab(HttpServletRequest request, Model model) {
		try {
			String courseIdStr = request.getParameter(UUID_STR);
			UUID courseId = UUID.fromString(courseIdStr);

			courseCRUDService.deployLab(courseId);
			Course course = courseCRUDService.retrieveById(courseId);

			return REDIRECT_COURSE_CRUD_NAME + course.getSlug();

		} catch (IllegalArgumentException | NoSuchFieldException | NullPointerException | IOException e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_400_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_500_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/cleanup")
	public String postControllerCleanupLab(HttpServletRequest request, Model model) {
		try {
			String courseIdStr = request.getParameter(UUID_STR);
			UUID courseId = UUID.fromString(courseIdStr);

			courseCRUDService.cleanupLab(courseId);
			Course course = courseCRUDService.retrieveById(courseId);

			return REDIRECT_COURSE_CRUD_NAME + course.getSlug();
		} catch (IllegalArgumentException | NoSuchFieldException | NullPointerException | IOException e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_400_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_500_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@PostMapping("/provision")
	public String postControllerProvisionCourseInfrastructure(HttpServletRequest request, Model model) {
		try {
			String courseIdStr = request.getParameter(UUID_STR);
			UUID courseId = UUID.fromString(courseIdStr);
			Course course = courseCRUDService.retrieveById(courseId);

			log.info("Starting provisioning for course: {}", courseId);
			courseCRUDService.provisionCourseInfrastructure(courseId);

			return REDIRECT_COURSE_CRUD_NAME + course.getSlug();
		} catch (IllegalArgumentException | NoSuchFieldException | NullPointerException | IOException e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_400_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_500_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}

	@GetMapping("/instance/terminal")
	public String getControllerDisplayContainerTerminal(HttpServletRequest request, Model model) {
		try {
			String instanceIdStr = request.getParameter(INSTANCE_ID_STR);
			UUID instanceId = UUID.fromString(instanceIdStr);

			model.addAttribute(INSTANCE_STR, instanceIdStr);
			request.getSession().setAttribute("instanceId", instanceId);
			return CONTAINER_TERMINAL_PAGE;
		} catch (IllegalArgumentException | NullPointerException e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_400_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			model.addAttribute(ERROR_CODE_STR, ERROR_CODE_500_STR);
			model.addAttribute(ERROR_MESSAGE_STR, e.getMessage());
			return ERROR_PAGE;
		}
	}
}
