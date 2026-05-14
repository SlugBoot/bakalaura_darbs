package lv.slugboot.app.service.impl;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lv.slugboot.app.models.LabInstance;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.models.enums.LabInstanceStatus;
import lv.slugboot.app.repo.ILabInstanceRepo;
import lv.slugboot.app.repo.IStudentRepo;
import lv.slugboot.app.service.IAnsibleService;
import lv.slugboot.app.service.ISystemTaskService;

@Service
public class AnsibleServiceImpl implements IAnsibleService{

	@Autowired private ISystemTaskService systemTaskService;
	@Autowired private ILabInstanceRepo labInstanceRepo;
	@Autowired private IStudentRepo studentRepo;
	
	private final String ANSIBLE_BASE_PATH = "ansible_workspace";

	@Override
	public void createVarsFile(UUID courseId, Map<String, Object> variables) throws Exception {
		String path = Paths.get(ANSIBLE_BASE_PATH, courseId.toString(), "vars.yml").toString();
		StringBuilder sb = new StringBuilder("---\n");
		variables.forEach((key, value) -> sb.append(key).append(": ").append(value).append("\n"));
		systemTaskService.createFile(path, sb.toString());
	}

	@Override
	public void createInventoryFile(UUID courseId, String hostGroup, String ipAddress) throws Exception {
		String path = Paths.get(ANSIBLE_BASE_PATH, courseId.toString(), "hosts").toString();
		String content = String.format("[%s]\n%s ansible_ssh_user=root", hostGroup, ipAddress);
		systemTaskService.createFile(path, content);
	}

	@Override
	public void createPlaybook(UUID courseId, String playbookYaml) throws Exception {
		String path = Paths.get(ANSIBLE_BASE_PATH, courseId.toString(), "playbook.yml").toString();
		systemTaskService.createFile(path, playbookYaml);
	}

	@Override
	public String runPlaybook(UUID courseId) throws Exception {
		return runPlaybook(courseId, null);
	}

	@Override
	public String runPlaybook(UUID courseId, UUID studentId) throws Exception {
		String baseDir = Paths.get(ANSIBLE_BASE_PATH, courseId.toString()).toString();
		String playbookPath = Paths.get(baseDir, "playbook.yml").toString();
		String inventoryPath = Paths.get(baseDir, "hosts").toString();
		Student student = studentRepo.findById(studentId).get();
		
		String command = String.format("ansible-playbook -i %s %s", inventoryPath, playbookPath);
		
		if (studentId != null) {
			command += " --limit " + student.getUsername()+"-vm";
		}
		return systemTaskService.executeCommand(command);
	}

	@Override
	public void createProxmoxVarsFile(UUID courseId, List<LabInstance> instances) throws Exception {
		String path = Paths.get(ANSIBLE_BASE_PATH, courseId.toString(),"multi-container.yml").toString();
		
		StringBuilder sb = new StringBuilder("---\n"
				+ "containers:\n");
		
		int vmidCounter = 100;
		int IPCounter = 10;
		
		for (LabInstance inst : instances) {
			inst.setIpAddress("192.168.0."+IPCounter++);
			inst.setStatus(LabInstanceStatus.Provisioning);
			labInstanceRepo.save(inst);
			sb.append("  - vmid: ").append(vmidCounter++).append("\n");
	        sb.append("    hostname: ").append(inst.getStudent().getUsername()).append("-vm\n");
	        sb.append("    ip: ").append(inst.getIpAddress()).append("\n");
		}
		
		systemTaskService.createFile(path, sb.toString());
	}
	
}
