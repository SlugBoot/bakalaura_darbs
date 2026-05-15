package lv.slugboot.app.controller;

import java.util.Collection;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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
import lombok.extern.slf4j.Slf4j;
import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.service.IAnsibleService;
import lv.slugboot.app.service.ICourseCRUDService;
import lv.slugboot.app.service.IFilterService;
import lv.slugboot.app.service.ILabInstanceCRUDService;
import lv.slugboot.app.service.IProfessorCRUDService;
import lv.slugboot.app.service.IStudentCRUDService;

@Controller
@RequestMapping("/course/crud")
@Slf4j
public class CourseCRUDController {

	@Autowired ICourseCRUDService courseCRUDService;
	@Autowired IFilterService filterService;
	@Autowired IStudentCRUDService studentCRUDService;
	@Autowired IProfessorCRUDService professorCRUDService;
	@Autowired ILabInstanceCRUDService instanceCRUDService;
	@Autowired IAnsibleService ansibleService;
	
	private String courseList = "show-multiple-courses";
	private String courseInfoPage = "course-info";
	private String errorPage = "show-error";
	private String addStudentsPage = "course-add-student";
	private String createCoursePage = "create-course";
	private String updateCoursePage = "update-course";
	
	private String studentAttribute = "student";
	private String courseAttribute = "course";
	private String instanceAttribute = "instance";
	private String errorAttribute = "error";
	private String professorAttribute = "professor";
	private String previousURLAttribute = "previousUrl";
	
	private final String removeVMsFile = "remove_vms";
	private final String playbookFile = "playbook";
	private final String proxmoxFile = "provisioning";
	private final String hostsFile = "hosts";
	private final String studentHostsFile = "student_hosts";
	private final String defaultPlaybookFile = "default_playbook";
	
	@GetMapping("/all")
	public String getControllerAllCourses(Model model) {
		try {
			model.addAttribute(courseAttribute, courseCRUDService.retrieveAll());
			return courseList;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/{uuid}")
	public String getControllerCourseInfo(@PathVariable(name="uuid") UUID courseId,
			@RequestParam(value="referer", required=false) String manualReferer,
			HttpServletRequest request,
			Model model) {
		try {
			String referer = (manualReferer != null) ? manualReferer : request.getHeader("Referer");
			model.addAttribute(previousURLAttribute, referer);
			
			Course course = courseCRUDService.retrieveById(courseId);
			model.addAttribute(courseAttribute, course);
			model.addAttribute(instanceAttribute, instanceCRUDService.retrieveByCourseId(courseId));
			return courseInfoPage;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/delete/{uuid}")
	public String getControllerDeleteCourse(@PathVariable(name="uuid") UUID courseId, Model model) {
		try {
			courseCRUDService.deleteCourseById(courseId);
			ansibleService.runPlaybook(courseId, "remove_vms", hostsFile);
			model.addAttribute(courseAttribute, courseCRUDService.retrieveAll());
			return courseList;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/create")
	public String getControllerCreateCourse(HttpServletRequest request, Model model) {
		try {
			String referer = request.getHeader("Referer");
			model.addAttribute(previousURLAttribute, referer);
			model.addAttribute(courseAttribute, new Course());
			model.addAttribute(professorAttribute, professorCRUDService.retrieveAll());
			return createCoursePage;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@PostMapping("/create")
	public String postControllerCreateCourse(@Valid Course course,@RequestParam(value="referer", required=false)String referer, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return createCoursePage;
		}
		
		try {
			courseCRUDService.createCourse(course.getCourseName(),
					course.getCourseDesc(), 
					course.getProfessor().getPersonId());
			if (referer != null && referer.startsWith("/")) {
				return "redirect:" + referer;
			}
			return "redirect:/course/crud/all";
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/{uuid}/update")
	public String getControllerUpdateCourse(@PathVariable(name="uuid") UUID courseId, Model model) {
		try {
			Course course = courseCRUDService.retrieveById(courseId);
			model.addAttribute(courseAttribute, course);
			model.addAttribute(professorAttribute, professorCRUDService.retrieveAll());
			return updateCoursePage;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@PostMapping("/{uuid}/update")
	public String postControllerUpdateCourse(@PathVariable(name="uuid") UUID courseId,
			@Valid Course course, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return updateCoursePage;
		}
		
		try {
			courseCRUDService.updateCourseById(courseId, course.getCourseName(),
					course.getCourseDesc(), 
					course.getProfessor().getPersonId());
			return "redirect:/course/crud/all";
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/{uuid}/add")
	public String getControllerStudentsNotInCourse(@PathVariable(name="uuid") UUID courseId,
			HttpServletRequest request,Model model) {
		try {
			String referer = request.getHeader("Referer");
			model.addAttribute(previousURLAttribute, referer);
			
			Course course = courseCRUDService.retrieveById(courseId);
			Collection<Student> studentsNotInCourse = filterService.studentsNotInCourse(courseId);
			model.addAttribute(studentAttribute, studentsNotInCourse);
			model.addAttribute(courseAttribute, course);
			return addStudentsPage;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/{uuid}/add/{studentId}")
	public String getControllerAddStudentToCourse(@PathVariable(name="uuid") UUID courseId, 
			@PathVariable(name="studentId") UUID studentId,
			@RequestParam(value="referer", required=false) String referer, Model model) {
		try {
			courseCRUDService.addStudentToCourse(courseId, studentId);
			
			if (referer != null && referer.startsWith("/")) {
				return "redirect:" + referer;
			}
			
			return "redirect:/course/crud/" + courseId;
			
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/{uuid}/remove/{studentId}")
	public String getControllerRemoveStudentFromCourse(@PathVariable(name="uuid") UUID courseId,
			@PathVariable(name="studentId") UUID studentId,
			@RequestParam(name="referer", required=false) String referer,
			Model model) {
		try {
			courseCRUDService.removeStudentFromCourse(courseId, studentId);
			String redirectUrl = "/course/crud/" + courseId;
			if (referer != null && referer.startsWith("/")) {
				redirectUrl += "?referer=" + referer;
			}
			
			return "redirect:" + redirectUrl;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/{uuid}/deploy")
	public String getControllerDeployLab(@PathVariable(name="uuid") UUID courseId, 
			@RequestParam(value="referer", required=false) String referer, Model model) {
		try {
			courseCRUDService.deployLab(courseId);
			
            if (referer != null && referer.startsWith("/")) {
				return "redirect:" + referer;
			}
			else {
				return "redirect:/course/crud/" + courseId; 
			}
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}
	
	@GetMapping("/{uuid}/cleanup")
    public String getControllerCleanupLab(@PathVariable(name="uuid") UUID courseId, 
            @RequestParam(value="referer", required=false) String referer, Model model) {
        try {
            courseCRUDService.cleanupLab(courseId);
            if (referer != null && referer.startsWith("/")) {
				return "redirect:" + referer;
			}
			else {
				return "redirect:/course/crud/" + courseId; 
			}
        } catch (Exception e) {
            model.addAttribute(errorAttribute, e.getMessage());
            return errorPage;
        }
    }
	
	@GetMapping("/{uuid}/provision")
	public String getControllerProvisionCourseInfrastructure(@PathVariable(name="uuid") UUID courseId, Model model) {
		try {
			courseCRUDService.prepareProxmoxProvisioning(courseId);
			
			log.info("Files prepared. Starting playbook execution for course: {}", courseId);
			
			ansibleService.runPlaybook(courseId, proxmoxFile, hostsFile);
			
			return "redirect:/course/crud/" + courseId;
		} catch (Exception e) {
			model.addAttribute(errorAttribute, e.getMessage());
			return errorPage;
		}
	}

}
