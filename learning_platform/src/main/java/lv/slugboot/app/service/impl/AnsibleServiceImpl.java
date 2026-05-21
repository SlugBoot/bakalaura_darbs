package lv.slugboot.app.service.impl;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lv.slugboot.app.models.LabInstance;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.models.enums.LabInstanceStatus;
import lv.slugboot.app.repo.ILabInstanceRepo;
import lv.slugboot.app.repo.IStudentRepo;
import lv.slugboot.app.service.IAnsibleService;
import lv.slugboot.app.service.ISystemTaskService;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnsibleServiceImpl implements IAnsibleService {

	private final ISystemTaskService systemTaskService;
	private final ILabInstanceRepo labInstanceRepo;
	private final IStudentRepo studentRepo;
	
	private final SimpMessagingTemplate messagingTemplate;

	private static final String ANSIBLE_BASE_PATH = "ansible_workspace";

	private void notifyStatusChange(UUID courseId) {
		String destination = "/topic/course/" + courseId;
		messagingTemplate.convertAndSend(destination, "refresh");
	}
	
	@Override
	public void createVarsFile(UUID courseId, Map<String, Object> variables) throws IOException {
		String path = Paths.get(ANSIBLE_BASE_PATH, courseId.toString(), "vars.yml").toString();
		StringBuilder sb = new StringBuilder("---\n");
		variables.forEach((key, value) -> sb.append(key).append(": ").append(value).append("\n"));
		systemTaskService.createFile(path, sb.toString());
		notifyStatusChange(courseId);
	}

	@Override
	public void createInventoryFile(UUID courseId, String hostGroup, List<String> ipAddresses, String inventoryName)
			throws IOException {
		String path = Paths.get(ANSIBLE_BASE_PATH, courseId.toString(), inventoryName).toString();
		StringBuilder sb = new StringBuilder("[" + hostGroup + "]\n");
		for (String ip : ipAddresses) {
			sb.append(ip).append(" ansible_ssh_user=root\n");
		}
		systemTaskService.createFile(path, sb.toString());
		notifyStatusChange(courseId);
	}

	@Override
	public void createPlaybook(UUID courseId, String playbookYaml, String playbookName) throws IOException {
		String path = Paths.get(ANSIBLE_BASE_PATH, courseId.toString(), playbookName + ".yml").toString();
		systemTaskService.createFile(path, playbookYaml);
	}

	@Override
	public void runPlaybook(UUID courseId, UUID studentId, String playbookName, String inventoryName)
			throws IOException, InterruptedException {
		log.info("Starting Ansible playbook in background for course: {}", (Object) courseId);
		
		String baseDir = Paths.get(ANSIBLE_BASE_PATH, courseId.toString()).toString();
		String playbookPath = Paths.get(baseDir, playbookName + ".yml").toString();
		String inventoryPath = Paths.get(baseDir, inventoryName).toString();

		String courseShortId = courseId.toString().substring(0, 8);

		String command = String.format("export ANSIBLE_HOST_KEY_CHECKING=False && ansible-playbook -i %s %s",
				inventoryPath, playbookPath);

		if (studentId != null) {
			Student student = studentRepo.findById(studentId).get();
			command += " --limit " + student.getUsername() + "-" + courseShortId + "-vm";
		}
		log.info("Running playbook: " + playbookName + " with inventory: " + inventoryName);
		 try {
			systemTaskService.executeCommand(command);
		} catch (Exception e) {
			log.error("Async Ansible task failed");
		}
	}

	@Override
	public void runPlaybook(UUID courseId, String playbookName, String inventoryName)
			throws IOException, InterruptedException {
		runPlaybook(courseId, null, playbookName, inventoryName);
	}

	@Override
	public void createProxmoxVarsFile(UUID courseId, List<LabInstance> instances)
			throws IOException, InterruptedException {
		String path = Paths.get(ANSIBLE_BASE_PATH, courseId.toString(), "multi-container.yml").toString();

		StringBuilder sb = new StringBuilder("---\n" + "containers:\n");

		String courseShortId = courseId.toString().substring(0, 8);

		// Max 9 kursi ar 10 konteineriem katrā
		int courseBlock = Math.floorMod(courseId.hashCode(), 9);
		int vmidCounter = 1000 + (courseBlock * 50);

		int startOctet = 20 + (courseBlock * 10);
		int currentOctet = startOctet;

		for (LabInstance inst : instances) {
			if (currentOctet >= (startOctet + 10) || currentOctet > 254) {
				throw new InterruptedException(
						"This course has exceeded its maximum allowance of 10 static dynamic IP slots.");
			}

			String allocatedIp = inst.getIpAddress();
			if (allocatedIp == null || allocatedIp.isEmpty() || allocatedIp.equalsIgnoreCase("null")) {
				allocatedIp = "192.168.15." + currentOctet;
				inst.setIpAddress(allocatedIp);
				inst.setStatus(LabInstanceStatus.INITIALIZED);
				labInstanceRepo.save(inst);
			}

			currentOctet++;

			String uniqueHostname = inst.getStudent().getUsername() + "-" + courseShortId + "-vm";

			sb.append("  - vmid: ").append(vmidCounter++).append("\n");
			sb.append("    hostname: ").append(uniqueHostname).append("\n");
			sb.append("    ip: ").append(allocatedIp).append("\n");
		}

		systemTaskService.createFile(path, sb.toString());
	}

}
