package lv.slugboot.app.service.impl;

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
public class CourseCRUDServiceImpl implements ICourseCRUDService{
	
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
	public void createCourse(String courseName, String courseDesc, UUID professorId) throws Exception {
		if (courseName == null) {
			throw new Exception("Course Name must not be null");
		}
		
		if (professorId == null) {
			throw new Exception("Professor must be set for course to exist");
		}
		
	    if (courseDesc != null && courseDesc.trim().isEmpty()) {
	    	courseDesc = null;
	    }
	    
	    Professor professor = professorRepo.findById(professorId).get();
	    
	    Course newCourse;
	    
	    if (courseDesc == null) {
	    	newCourse = new Course(courseName, professor);
	    }
	    else {
	    	newCourse = new Course(courseName, courseDesc, professor);
	    }
		
	    courseRepo.save(newCourse);
	}

	@Override
	public ArrayList<Course> retrieveAll() throws Exception {
	    if (courseRepo.count() == 0) {
	        throw new Exception("Course list is empty");
	      }
	      ArrayList<Course> result = (ArrayList<Course>) courseRepo.findAll();
	      return result;
	}

	@Override
	public Course retrieveById(UUID id) throws Exception {
		if (id == null) {
			throw new Exception("Course ID cannot be null");
		}
		
		
		if (!courseRepo.existsById(id)) {
			throw new Exception("Course with id " + id + "does not exist");
		}
		
		return courseRepo.findById(id).get();
	}

	@Override
	public void updateCourseById(UUID id, String courseName, String courseDesc, UUID professorId) throws Exception {		
		if (courseName == null) {
			throw new Exception("Course name cannot be empty");
		}
		
		Professor professor;
		
		if (!professorRepo.existsById(professorId)) {
			throw new Exception("This professor does not exist");
		}
		
		professor = professorRepo.findById(professorId).get();
		
		Course courseToUpdate = retrieveById(id);
		
		if(!courseToUpdate.getCourseName().equals(courseName)) {
			courseToUpdate.setCourseName(courseName);
		}
		
		if(!courseToUpdate.getCourseDesc().equals(courseDesc)) {
			courseToUpdate.setCourseDesc(courseDesc);
		}
		
		if(courseToUpdate.getProfessor() != professor) {
			courseToUpdate.setProfessor(professor);
		}
		
		courseRepo.save(courseToUpdate);
	}

	@Override
	public void deleteCourseById(UUID id) throws Exception {
		Course courseToDelete = retrieveById(id);
		
		cleanupLab(id);
		
		courseRepo.delete(courseToDelete);
	}

	@Override
	@Transactional
	public void addStudentToCourse(UUID courseId, UUID studentId) throws Exception {
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
	public void removeStudentFromCourse(UUID courseId, UUID studentId) throws Exception {
		Course course = retrieveById(courseId);
		Student student = studentRepo.findById(studentId).get();
		LabInstance instance = instanceRepo.findByCourseAndStudent(course,student);
		
		if (student.getCourse().contains(course)) {
			student.getCourse().remove(course);
			course.getStudents().remove(student);
			instanceRepo.delete(instance);
		}

		courseRepo.save(course);
	}

	@Override
	@Transactional
	public void deployLab(UUID courseId) throws Exception {
		Course course = retrieveById(courseId);
		List<LabInstance> instances = instanceRepo.findByCourse(course);
		
		if (instances.isEmpty()) {
			throw new Exception("No students enrolled. Nothing to deploy");
		}
		
		for (LabInstance inst : instances) {
		    if (inst.getStatus() == null || inst.getStatus() != LabInstanceStatus.INITIALIZED) {
		        throw new Exception("Cannot deploy lab: Student '" 
		            + inst.getStudent().getUsername() 
		            + "' has a lab container instance that is not Initialized. Please run preparation/provisioning first.");
		    }
		}
			
		String startPlaybook = "---\n"
	            + "- name: Power On Course Containers\n"
	            + "  hosts: proxmox\n"
	            + "  vars_files:\n"
	            + "    - multi-container.yml\n"
	            + "  tasks:\n"
	            + "    - name: Ensure containers are started\n"
	            + "      community.proxmox.proxmox:\n"
	            + "        node: \"prox-bak\"\n"
	            + "        api_host: \"192.168.0.112\"\n"
	            + "        api_token_id: \"ansible-token\"\n"
	            + "        api_token_secret: \"e7c7ea4a-8e10-4547-acd6-c145da35e1d3\"\n"
	            + "        api_user: \"root@pam\"\n"
	            + "        vmid: \"{{ item.vmid }}\"\n"
	            + "        state: started\n" 
	            + "      loop: \"{{ containers }}\"\n"
	            + "\n" 
	            + "    - name: Wait for container OS to initialize\n"
	            + "      pause:\n"
	            + "        seconds: 10\n"
	            + "\n"
	            + "    - name: Force PermitRootLogin via pct exec\n"
	            + "      shell: \"pct exec {{ item.vmid }} --"
	            + " sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config\"\n"
	            + "      loop: \"{{ containers }}\"\n"
	            + "\n"
	            + "    - name: Restart SSH service inside container\n"
	            + "      shell: \"pct exec {{ item.vmid }} -- systemctl restart ssh\"\n"
	            + "      loop: \"{{ containers }}\"";
		
		ansibleService.createPlaybook(courseId, startPlaybook, START_VMS_FILE);
	    ansibleService.runPlaybook(courseId, START_VMS_FILE, HOSTS_FILE);
		
		if (instances.isEmpty()) {
			throw new Exception("No students enrolled. Nothing to deploy");
		}
		
		String hostGroup = "target_vms";
		List<String> ips = new ArrayList<>();
		for (LabInstance inst : instances) {
			inst.setStatus(LabInstanceStatus.RUNNIING);
			instanceRepo.save(inst);
			if (inst.getIpAddress() != null) {
				ips.add(inst.getIpAddress());
			}
		}
		ansibleService.createInventoryFile(courseId, hostGroup, ips, STUDENT_HOSTS_FILE);
		
		String installPlaybook = "---\n"
				+ "- name: Install Necessary Packages\n"
				+ "  hosts: "+hostGroup+"\n"
				+ "  gather_facts: no\n"
				+ "  tasks:\n"
				+ "    - name: Wait for SSH\n"
				+ "      wait_for_connection:\n"
				+ "        timeout: 60\n"
				+ "    - name: Update apt cache\n"
				+ "      apt: update_cache=yes\n"
				+ "    - name: Install packages\n"
				+ "      package:\n"
				+ "        name: [git, curl, vim, build-essential, fastfetch]\n"
				+ "        state: present";
		
		ansibleService.createPlaybook(courseId, installPlaybook, PLAYBOOK_FILE);
		
		ansibleService.runPlaybook(courseId, PLAYBOOK_FILE, STUDENT_HOSTS_FILE);
	}

	@Override
	public void cleanupLab(UUID courseId) throws Exception {
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
	public void prepareProxmoxProvisioning(UUID courseId) throws Exception {
		Course course = retrieveById(courseId);
	    List<LabInstance> instances = instanceRepo.findByCourse(course);
	    
	    ansibleService.createProxmoxVarsFile(courseId, instances);
	    ansibleService.createInventoryFile(courseId, "proxmox", List.of("192.168.0.112"), HOSTS_FILE);
	    
	    String proxmoxPlaybook = "---\n"
	    	    + "- name: Create Course Containers\n"
	    	    + "  hosts: proxmox\n"
	    	    + "  vars_files:\n"
	    	    + "    - multi-container.yml\n"
	    	    + "  tasks:\n"
	    	    + "    - name: Create multiple containers\n"
	    	    + "      community.proxmox.proxmox:\n"
	    	    + "        node: \"prox-bak\"\n"
	    	    + "        api_host: \"192.168.0.112\"\n" 
	    	    + "        api_token_id: \"ansible-token\"\n"
	    	    + "        api_token_secret: \"e7c7ea4a-8e10-4547-acd6-c145da35e1d3\"\n"
	    	    + "        api_user: \"root@pam\"\n"
	    	    + "        hostname: \"{{ item.hostname }}\"\n"
	    	    + "        vmid: \"{{ item.vmid }}\"\n"
	    	    + "        netif:\n"
	    	    + "          net0: \"name=eth0,gw=192.168.0.1,ip={{ item.ip }}/24,bridge=vmbr0\"\n"
	    	    + "        password: \"securepassword\"\n"
	    	    + "        ostemplate: 'local:vztmpl/debian-13-standard_13.1-2_amd64.tar.zst'\n"
	    	    + "        disk: \"local-lvm:30\"\n" 
	    	    + "        cores: 1\n"
	    	    + "        memory: 512\n"
	    	    + "        features: \"nesting=1\"\n"
	    	    + "        unprivileged: yes\n"
	            + "        pubkey: \"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDAwqEBLSv83qVT6NFlKfR"
	            + "2JxjaaYgMVS5NdCFS5T1cUNXxtcTebfJg9Fn2cFuPF8VW2NUSdQMsprH4YLDPN8bAp+DF2SdC7w"
	            + "ahrnPlP0MM/g+4ztmyC4s+4VwN6qh2JMs9OX9mGXlzq+PTrm2KwwtlEUdxl5YqmzVBjjz9au7Lj"
	            + "qbTuyFqEuxuVyRvqw2V7KLkp/NM1GhvU+Rj8HUaTVY1zIB/twZAJ1UJ7JYAfsl4x/Q/Pn9WrGWYT"
	            + "C5L1HiLfKWxHFGBg5krK2beIT4ShhYhu0w+Tcj7ITMTvMoudNm15ROEJ4zno7XM4+SbJCwJYMkJM"
	            + "2zed5QO+zgdZPHt4yyCmQffyKBC5zTX+Zsp0M7FmsKH8D+AYFA4ZSobxHDMSN5pLZzResmtssJDl"
	            + "NVoqI/3NlSZ3rYE0NS948d1KPc+PzxjuiOVpYQJj1wGGDB0TL4OfkUy966NjoGyhI1R+UypJ7bwh1"
	            + "sLKZv78pmVi/NI3tb0DL8D5FfgxwBl9zDn69Ar2RwUHYiBl+y4sbXVMhEdFMPB3vaXxRoQ93AcZ3"
	            + "++xc4Aj8u55aaXn0nAd90Q2wV6DxPze/6hhR9IbpXM12KJvJV2ZoVYk7nLc0gRkW5Ywocw4w0legw"
	            + "sIa7bbK1Zb/FP7yIOdUyYMgEvleCUsj1pawD6sucb0GqL81NumDP72Q== root@test-cont\"\n" 
	    	    + "      loop: \"{{ containers }}\"";
	            
	        ansibleService.createPlaybook(courseId, proxmoxPlaybook, PROXMOX_FILE);
	        
	        String removePlaybook = "---\n"
	                + "- name: Remove Course Containers\n"
	                + "  hosts: proxmox\n"
	                + "  vars_files:\n"
	                + "    - multi-container.yml\n"
	                + "  tasks:\n"
	                + "    - name: Delete containers by VMID\n"
	                + "      community.proxmox.proxmox:\n"
	                + "        node: \"prox-bak\"\n"
	                + "        api_host: \"192.168.0.112\"\n" 
	                + "        api_token_id: \"ansible-token\"\n"
	                + "        api_token_secret: \"e7c7ea4a-8e10-4547-acd6-c145da35e1d3\"\n"
	                + "        api_user: \"root@pam\"\n"
	                + "        vmid: \"{{ item.vmid }}\"\n"
	                + "        state: absent\n"
	                + "        force: yes\n"
	                + "      loop: \"{{ containers }}\"";
	                
	        ansibleService.createPlaybook(courseId, removePlaybook, REMOVE_VMS_FILE);
	}

	


}
