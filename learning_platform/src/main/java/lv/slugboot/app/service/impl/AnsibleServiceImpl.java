package lv.slugboot.app.service.impl;

import java.nio.file.Paths;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lv.slugboot.app.service.IAnsibleService;
import lv.slugboot.app.service.ISystemTaskService;

@Service
public class AnsibleServiceImpl implements IAnsibleService{

	@Autowired private ISystemTaskService systemTaskService;
	
	private final String ANSIBLE_BASE_PATH = "ansible_workspace";

	@Override
	public void createVarsFile(String courseId, Map<String, Object> variables) throws Exception {
		String path = Paths.get(ANSIBLE_BASE_PATH, courseId, "vars.yml").toString();
		StringBuilder sb = new StringBuilder("---\n");
		variables.forEach((key, value) -> sb.append(": ").append(value).append("\n"));
		systemTaskService.createFile(path, sb.toString());
	}

	@Override
	public void createInventoryFile(String courseId, String hostGroup, String ipAddress) throws Exception {
		String path = Paths.get(ANSIBLE_BASE_PATH, courseId, "hosts").toString();
		String content = String.format("[%s]\n%s ansible_ssh_user=root", hostGroup, ipAddress);
		systemTaskService.createFile(path, content);
	}

	@Override
	public void createPlaybook(String courseId, String playbookYaml) throws Exception {
		String path = Paths.get(ANSIBLE_BASE_PATH, courseId, "playbook.yml").toString();
		systemTaskService.createFile(path, playbookYaml);
	}

	@Override
	public String runPlaybook(String courseId) throws Exception {
		String playbookPath = Paths.get(ANSIBLE_BASE_PATH, courseId, "playbook.yml").toString();
		String inventoryPath = Paths.get(ANSIBLE_BASE_PATH, courseId, "hosts").toString();
		
		String command = String.format("ansible-playbook -i %s %s", inventoryPath, playbookPath);
		return systemTaskService.executeCommand(command);
		}

	
}
