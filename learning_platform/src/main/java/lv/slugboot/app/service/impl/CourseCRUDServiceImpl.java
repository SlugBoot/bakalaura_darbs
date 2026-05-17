package lv.slugboot.app.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.LabInstance;
import lv.slugboot.app.models.Professor;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.models.enums.LabInstanceStatus;
import lv.slugboot.app.repo.ICourseRepo;
import lv.slugboot.app.repo.ILabInstanceRepo;
import lv.slugboot.app.repo.IProfessorRepo;
import lv.slugboot.app.repo.IStudentRepo;
import lv.slugboot.app.service.IAnsibleService;
import lv.slugboot.app.service.ICourseCRUDService;
import lv.slugboot.app.service.ISystemTaskService;

@Service
@RequiredArgsConstructor
public class CourseCRUDServiceImpl implements ICourseCRUDService {

	private final ICourseRepo courseRepo;
	private final IProfessorRepo professorRepo;
	private final IStudentRepo studentRepo;
	private final ILabInstanceRepo instanceRepo;

	private final IAnsibleService ansibleService;
	private final ISystemTaskService systemTaskService;

	private static final String ANSIBLE_BASE_PATH = "ansible_workspace";

	private static final String REMOVE_VMS_FILE = "remove_vms";
	private static final String PLAYBOOK_FILE = "playbook";
	private static final String PROXMOX_FILE = "provisioning";
	private static final String HOSTS_FILE = "hosts";
	private static final String STUDENT_HOSTS_FILE = "student_hosts";
	private static final String START_VMS_FILE = "start_vms";

	@Override
	public void createCourse(String courseName, String courseDesc, UUID professorId) {
		if (courseName == null) {
			throw new NullPointerException("Course Name must not be null");
		}

		if (professorId == null) {
			throw new NullPointerException("Professor must be set for course to exist");
		}

		if (courseDesc != null && courseDesc.trim().isEmpty()) {
			courseDesc = null;
		}

		Professor professor = professorRepo.findById(professorId).get();

		Course newCourse;

		if (courseDesc == null) {
			newCourse = new Course(courseName, professor);
		} else {
			newCourse = new Course(courseName, courseDesc, professor);
		}

		courseRepo.save(newCourse);
	}

	@Override
	public List<Course> retrieveAll() throws NoSuchFieldException {
		if (courseRepo.count() == 0) {
			throw new NoSuchFieldException("Course list is empty");
		}

		return courseRepo.findAll();
	}

	@Override
	public Course retrieveById(UUID id) throws NoSuchFieldException {
		if (id == null) {
			throw new NullPointerException("Course ID cannot be null");
		}

		if (!courseRepo.existsById(id)) {
			throw new NoSuchFieldException("Course with id " + id + "does not exist");
		}

		return courseRepo.findById(id).get();
	}

	@Override
	public void updateCourseById(UUID id, String courseName, String courseDesc, UUID professorId)
			throws NoSuchFieldException {
		if (courseName == null) {
			throw new NullPointerException("Course name cannot be empty");
		}

		Professor professor;

		if (!professorRepo.existsById(professorId)) {
			throw new NoSuchFieldException("This professor does not exist");
		}

		professor = professorRepo.findById(professorId).get();

		Course courseToUpdate = retrieveById(id);

		if (!courseToUpdate.getCourseName().equals(courseName)) {
			courseToUpdate.setCourseName(courseName);
		}

		if (!courseToUpdate.getCourseDesc().equals(courseDesc)) {
			courseToUpdate.setCourseDesc(courseDesc);
		}

		if (courseToUpdate.getProfessor() != professor) {
			courseToUpdate.setProfessor(professor);
		}

		courseRepo.save(courseToUpdate);
	}

	@Override
	public void deleteCourseById(UUID id) throws NoSuchFieldException, IOException, InterruptedException {
		Course courseToDelete = retrieveById(id);

		cleanupLab(id);

		courseRepo.delete(courseToDelete);
	}

	@Override
	@Transactional
	public void addStudentToCourse(UUID courseId, UUID studentId) throws NoSuchFieldException {
		Course course = retrieveById(courseId);
		Student student = studentRepo.findById(studentId).get();
		LabInstance instance;

		if (!student.getCourse().contains(course)) {
			student.getCourse().add(course);
			course.getStudents().add(student);
			instance = new LabInstance(student, course, null);
			instanceRepo.save(instance);
		}

		courseRepo.save(course);

	}

	@Override
	@Transactional
	public void removeStudentFromCourse(UUID courseId, UUID studentId) throws NoSuchFieldException {
		Course course = retrieveById(courseId);
		Student student = studentRepo.findById(studentId).get();
		LabInstance instance = instanceRepo.findByCourseAndStudent(course, student);

		if (student.getCourse().contains(course)) {
			student.getCourse().remove(course);
			course.getStudents().remove(student);
			instanceRepo.delete(instance);
		}

		courseRepo.save(course);
	}

	@Override
	@Transactional
	public void deployLab(UUID courseId) throws NoSuchFieldException, IOException, InterruptedException {
		Course course = retrieveById(courseId);
		List<LabInstance> instances = instanceRepo.findByCourse(course);

		if (instances.isEmpty()) {
			throw new NoSuchFieldException("No students enrolled. Nothing to deploy");
		}

		for (LabInstance inst : instances) {
			if (inst.getStatus() == null || inst.getStatus() != LabInstanceStatus.INITIALIZED) {
				throw new IllegalArgumentException("Cannot deploy lab: Student '" + inst.getStudent().getUsername()
						+ "' has a lab container instance that is not Initialized. Please run preparation/provisioning first.");
			}
		}

		String startPlaybook = """
				---
				- name: Power On Course Containers
				  hosts: proxmox
				  vars_files:
				    - multi-container.yml
				  tasks:
				    - name: Ensure containers are started
				      community.proxmox.proxmox:
				        node: "prox-bak"
				        api_host: "192.168.0.112"
				        api_token_id: "ansible-token"
				        api_token_secret: "e7c7ea4a-8e10-4547-acd6-c145da35e1d3"
				        api_user: "root@pam"
				        vmid: "{{ item.vmid }}"
				        state: started
				      loop: "{{ containers }}"

				    - name: Wait for container OS to initialize
				      pause:
				        seconds: 10

				    - name: Force PermitRootLogin via pct exec
				      shell: "pct exec {{ item.vmid }} -- sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config"
				      loop: "{{ containers }}"
				      
					- name: Force PermitRootLogin via pct exec
				      shell: "pct exec {{ item.vmid }} -- sed -i 's/#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config"
				      loop: "{{ containers }}"

				    - name: Restart SSH service inside container
				      shell: "pct exec {{ item.vmid }} -- systemctl restart ssh"
				      loop: "{{ containers }}"
				""";

		ansibleService.createPlaybook(courseId, startPlaybook, START_VMS_FILE);
		ansibleService.runPlaybook(courseId, START_VMS_FILE, HOSTS_FILE);

		String hostGroup = "target_vms";
		List<String> ips = new ArrayList<>();
		for (LabInstance inst : instances) {
			inst.setStatus(LabInstanceStatus.RUNNING);
			instanceRepo.save(inst);
			if (inst.getIpAddress() != null) {
				ips.add(inst.getIpAddress());
			}
		}
		ansibleService.createInventoryFile(courseId, hostGroup, ips, STUDENT_HOSTS_FILE);

		String installPlaybook = """
				---
				- name: Install Necessary Packages
				  hosts: %s
				  gather_facts: no
				  tasks:
				    - name: Wait for SSH
				      wait_for_connection:
				        timeout: 60
				    - name: Update apt cache
				      apt: update_cache=yes
				    - name: Install packages
				      package:
				        name: [git, curl, vim, build-essential, fastfetch]
				        state: present
				""".formatted(hostGroup);

		ansibleService.createPlaybook(courseId, installPlaybook, PLAYBOOK_FILE);

		ansibleService.runPlaybook(courseId, PLAYBOOK_FILE, STUDENT_HOSTS_FILE);
	}

	@Override
	public void cleanupLab(UUID courseId) throws IOException, InterruptedException, NoSuchFieldException {
		ansibleService.runPlaybook(courseId, REMOVE_VMS_FILE, HOSTS_FILE);
		Course course = retrieveById(courseId);
		List<LabInstance> instances = instanceRepo.findByCourse(course);

		for (LabInstance inst : instances) {
			inst.setIpAddress(null);
			inst.setStatus(LabInstanceStatus.UNINITIALIZED);
			instanceRepo.save(inst);
		}

		systemTaskService.deleteDirectory(ANSIBLE_BASE_PATH + "/" + courseId);
	}

	@Override
	public void prepareProxmoxProvisioning(UUID courseId)
			throws NoSuchFieldException, IOException, InterruptedException {
		Course course = retrieveById(courseId);
		List<LabInstance> instances = instanceRepo.findByCourse(course);

		ansibleService.createProxmoxVarsFile(courseId, instances);
		ansibleService.createInventoryFile(courseId, "proxmox", List.of("192.168.0.112"), HOSTS_FILE);

		String proxmoxPlaybook = """
				---
				- name: Create Course Containers
				  hosts: proxmox
				  vars_files:
				    - multi-container.yml
				  tasks:
				    - name: Create multiple containers
				      community.proxmox.proxmox:
				        node: "prox-bak"
				        api_host: "192.168.0.112"
				        api_token_id: "ansible-token"
				        api_token_secret: "e7c7ea4a-8e10-4547-acd6-c145da35e1d3"
				        api_user: "root@pam"
				        hostname: "{{ item.hostname }}"
				        vmid: "{{ item.vmid }}"
				        netif:
				          net0: "name=eth0,gw=192.168.0.1,ip={{ item.ip }}/24,bridge=vmbr0"
				        password: "securepassword"
				        ostemplate: 'local:vztmpl/debian-13-standard_13.1-2_amd64.tar.zst'
				        disk: "local-lvm:30"
				        cores: 1
				        memory: 512
				        features: "nesting=1"
				        unprivileged: yes
				        pubkey: "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDAwqEBLSv83qVT6NFlKfR2JxjaaYgMVS5NdCFS5T1cUNXxtcTebfJg9Fn2cFuPF8VW2NUSdQMsprH4YLDPN8bAp+DF2SdC7wahrnPlP0MM/g+4ztmyC4s+4VwN6qh2JMs9OX9mGXlzq+PTrm2KwwtlEUdxl5YqmzVBjjz9au7LjqbTuyFqEuxuVyRvqw2V7KLkp/NM1GhvU+Rj8HUaTVY1zIB/twZAJ1UJ7JYAfsl4x/Q/Pn9WrGWYTC5L1HiLfKWxHFGBg5krK2beIT4ShhYhu0w+Tcj7ITMTvMoudNm15ROEJ4zno7XM4+SbJCwJYMkJM2zed5QO+zgdZPHt4yyCmQffyKBC5zTX+Zsp0M7FmsKH8D+AYFA4ZSobxHDMSN5pLZzResmtssJDlNVoqI/3NlSZ3rYE0NS948d1KPc+PzxjuiOVpYQJj1wGGDB0TL4OfkUy966NjoGyhI1R+UypJ7bwh1sLKZv78pmVi/NI3tb0DL8D5FfgxwBl9zDn69Ar2RwUHYiBl+y4sbXVMhEdFMPB3vaXxRoQ93AcZ3++xc4Aj8u55aaXn0nAd90Q2wV6DxPze/6hhR9IbpXM12KJvJV2ZoVYk7nLc0gRkW5Ywocw4w0legwsIa7bbK1Zb/FP7yIOdUyYMgEvleCUsj1pawD6sucb0GqL81NumDP72Q== root@test-cont"
				      loop: "{{ containers }}"
				""";

		ansibleService.createPlaybook(courseId, proxmoxPlaybook, PROXMOX_FILE);

		String removePlaybook = """
				---
				- name: Remove Course Containers
				  hosts: proxmox
				  vars_files:
				    - multi-container.yml
				  tasks:
				    - name: Delete containers by VMID
				      community.proxmox.proxmox:
				        node: "prox-bak"
				        api_host: "192.168.0.112"
				        api_token_id: "ansible-token"
				        api_token_secret: "e7c7ea4a-8e10-4547-acd6-c145da35e1d3"
				        api_user: "root@pam"
				        vmid: "{{ item.vmid }}"
				        state: absent
				        force: yes
				      loop: "{{ containers }}"
				""";

		ansibleService.createPlaybook(courseId, removePlaybook, REMOVE_VMS_FILE);
	}

}
