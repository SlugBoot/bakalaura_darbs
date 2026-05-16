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
	public void createInventoryFile(UUID courseId, String hostGroup, List<String> ipAddresses, String inventoryName) throws Exception {
		String path = Paths.get(ANSIBLE_BASE_PATH, courseId.toString(), inventoryName).toString();
		StringBuilder sb = new StringBuilder("["+hostGroup+"]\n");
		for (String ip : ipAddresses) {
			sb.append(ip).append(" ansible_ssh_user=root\n");
		}
		systemTaskService.createFile(path, sb.toString());
	}

	@Override
	public void createPlaybook(UUID courseId, String playbookYaml, String playbookName) throws Exception {
		String path = Paths.get(ANSIBLE_BASE_PATH, courseId.toString(), playbookName+".yml").toString();
		systemTaskService.createFile(path, playbookYaml);
	}

	@Override
	public String runPlaybook(UUID courseId, String playbookName, String inventoryName) throws Exception {
		return runPlaybook(courseId, null, playbookName, inventoryName);
	}

	@Override
	public String runPlaybook(UUID courseId, UUID studentId, String playbookName, String inventoryName) throws Exception {
		String baseDir = Paths.get(ANSIBLE_BASE_PATH, courseId.toString()).toString();
		String playbookPath = Paths.get(baseDir, playbookName+".yml").toString();
		String inventoryPath = Paths.get(baseDir, inventoryName).toString();

		
		String command = String.format("ansible-playbook -i %s %s", inventoryPath, playbookPath);
		
		if (studentId != null) {
			Student student = studentRepo.findById(studentId).get();
			command += " --limit " + student.getUsername()+"-vm";
		}
		return systemTaskService.executeCommand(command);
	}

	@Override
	public void createProxmoxVarsFile(UUID courseId, List<LabInstance> instances) throws Exception {
		String path = Paths.get(ANSIBLE_BASE_PATH, courseId.toString(),"multi-container.yml").toString();
		
		StringBuilder sb = new StringBuilder("---\n"
				+ "containers:\n");
		
		String courseShortId = courseId.toString().substring(0, 8);
		
		
		// Max 80 kursi ar 20 konteineriem katrā
		int courseOffset = Math.abs(courseId.hashCode()) % 80;
		int vmidCounter = 1000 + (courseOffset * 20);
		int IPCounter = 10 + (courseOffset * 20);
		
		for (LabInstance inst : instances) {
			if (IPCounter > 254) {
				throw new Exception("Dynamic network allocation exceeded single subnet bounds (.254).");
			}
			
			String allocatedIp = "192.168.0."+IPCounter++;
			inst.setIpAddress(allocatedIp);
			inst.setStatus(LabInstanceStatus.Initialized);
			labInstanceRepo.save(inst);
			
			String uniqueHostname = inst.getStudent().getUsername() + "-" + courseShortId + "-vm";
			
			sb.append("  - vmid: ").append(vmidCounter++).append("\n");
	        sb.append("    hostname: ").append(uniqueHostname).append("\n");
	        sb.append("    ip: ").append(allocatedIp).append("\n");
		}
		
		systemTaskService.createFile(path, sb.toString());
	}
	
}
