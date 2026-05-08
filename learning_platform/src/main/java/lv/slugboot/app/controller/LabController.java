package lv.slugboot.app.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lv.slugboot.app.service.IAnsibleService;
import lv.slugboot.app.service.ISystemTaskService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/lab")
public class LabController {
	
	@Autowired private IAnsibleService ansibleService;
	@Autowired private ISystemTaskService systemTaskService;
	
	private final String ANSIBLE_BASE_PATH = "ansible_workspace";
	
	@PostMapping("/{uuid}/create")
	public String postControllerDeployLab(@PathVariable(name="uuid") String courseId,
			@RequestBody Map<String, Object> config) {
		try {
			
			ansibleService.createInventoryFile(courseId, "target_vms", (String) config.get("ip"));
			ansibleService.createVarsFile(courseId, (Map<String, Object>) config.get("vars"));
			ansibleService.createPlaybook(courseId, (String) config.get("playbook_content"));
			
			return ansibleService.runPlaybook(courseId);
		}
		catch (Exception e) {
			return "Deployment error: " + e.getMessage();
		}
	}
	
	@DeleteMapping("/{uuid}")
	public String deleteLabFiles(@PathVariable(name="uuid") String courseId) {
		try {
			systemTaskService.deleteFile(ANSIBLE_BASE_PATH + "/" + courseId + "/hosts");
			systemTaskService.deleteFile(ANSIBLE_BASE_PATH + "/" + courseId + "/hosts");
			systemTaskService.deleteFile(ANSIBLE_BASE_PATH + "/" + courseId + "/hosts");
			return "Lab " + courseId + " files removed.";
		} catch (Exception e) {
			return "Cleanup error: " + e.getMessage();
		}
	}
	
}
