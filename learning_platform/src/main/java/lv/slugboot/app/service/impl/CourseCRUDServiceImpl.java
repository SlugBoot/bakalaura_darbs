package lv.slugboot.app.service.impl;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.LabInstance;
import lv.slugboot.app.models.Professor;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.repo.ICourseRepo;
import lv.slugboot.app.repo.ILabInstanceRepo;
import lv.slugboot.app.repo.IProfessorRepo;
import lv.slugboot.app.repo.IStudentRepo;
import lv.slugboot.app.service.IAnsibleService;
import lv.slugboot.app.service.ICourseCRUDService;
import lv.slugboot.app.service.ISystemTaskService;

@Service
public class CourseCRUDServiceImpl implements ICourseCRUDService{
	
	@Autowired private ICourseRepo courseRepo;
	@Autowired private IProfessorRepo professorRepo;
	@Autowired private IStudentRepo studentRepo;
	@Autowired private ILabInstanceRepo instanceRepo;

	@Autowired private IAnsibleService ansibleService;
	@Autowired private ISystemTaskService systemTaskService;
	
	private final String ANSIBLE_BASE_PATH = "ansible_workspace";
	
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
		LabInstance instance = instanceRepo.findByStudentPersonIdAndCourseCId(studentId, courseId);
		
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
			throw new Exception("No Students enrolled. Nothing to deploy");
		}
		
		StringBuilder inventory = new StringBuilder("[target_vms]\n");
		for (LabInstance inst : instances) {
			if (inst.getIpAddress() != null) {
				inventory.append(inst.getIpAddress()).append(" ansible_ssh_user=root\n");
			}
			
			String path = Paths.get(ANSIBLE_BASE_PATH, courseId.toString(), "hosts").toString();
			systemTaskService.createFile(path, inventory.toString());
		}
		
		String defaultPlaybook = "---\n"
				+ "  tasks:\n"
				+ "    - name: Ensure lab tools\n"
				+ "      package: name=git state=present";
		ansibleService.createPlaybook(courseId, defaultPlaybook);
		
	}

	@Override
	public void cleanupLab(UUID courseId) throws Exception {
		systemTaskService.deleteFile(ANSIBLE_BASE_PATH + "/" + courseId + "/hosts");
		systemTaskService.deleteFile(ANSIBLE_BASE_PATH + "/" + courseId + "/vars.yml");
		systemTaskService.deleteFile(ANSIBLE_BASE_PATH + "/" + courseId + "/playbook.yml");
		systemTaskService.deleteDirectory(ANSIBLE_BASE_PATH + "/" + courseId);
	}

	@Override
	public void prepareProxmoxProvisioning(UUID courseId) throws Exception {
		Course course = retrieveById(courseId);
	    List<LabInstance> instances = instanceRepo.findByCourse(course);
	    
	    ansibleService.createProxmoxVarsFile(courseId, instances);
	    ansibleService.createInventoryFile(courseId, "proxmox", "192.168.0.112");
	    
	    String proxmoxPlaybook = "---\n" +
	            "- name: Create Course Containers\n" +
	            "  hosts: proxmox\n" +
	            "  vars_files:\n" +
	            "    - multi-container.yml\n" + // Points to the file we just created
	            "  tasks:\n" +
	            "  - name: Create multiple containers\n" +
	            "    community.proxmox.proxmox:\n" +
	            "      node: \"prox-bak\"\n" +
	            "      api_host: \"192.168.0.112\"\n" + 
	            "      api_token_id: \"ansible-token\"\n" +
	            "      api_token_secret: \"e7c7ea4a-8e10-4547-acd6-c145da35e1d3\"\n" +
	            "      api_user: \"root@pam\"\n" +
	            "      hostname: \"{{ item.hostname }}\"\n" +
	            "      vmid: \"{{ item.vmid }}\"\n" +
	            "      netif: \"name=eth0,ip={{ item.ip }}/24,bridge=vmbr0\"\n" +
	            "      password: \"securepassword\"\n" +
	            "      ostemplate: 'local:vztmpl/debian-13-standard_13.1-2_amd64.tar.zst'\n" +
	            "      disk_volume:\n"+ 
	            "        storage: local-lvm\n" +
	            "        size: 30\n" +
	            "      cores: 1\n" +
	            "      memory: 512\n" +
	            "      features: \"nesting=1\"\n" +
	            "      pubkey: ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQC/19QR+VepNQs1GLsuxyAW9jUv" +
	            "NQjfKIrL1kPIuVd7HBaDXZF7jvkNp46SMZ2lvWxDlA/W2F0QNj+U99ASCIUCicyw1exGbV0PtEVIdOFC" +
	            "yTyVrNerKt/J3OGqJgsfU/JVQE152WGLUtmeEDwvnZ5qvXQ7Cm06vsAFSj3j/O5pMRugcBxACI+b8op3HsD3wqvQkzH" +
	            "DvXKSMr9IeJNbIeI3DgpjASKSUs5eSiu+TRIh36WypbI+Q/h+x3HX8bkZe7q0dSTjEExAwS+ZP5Y1MVnKSyF+J9UB1A+" +
	            "ZuppeAkoQrUV4yH9UuxC9wEaNteNghCFTssZV0CHG0o9GE3st/VrhlqctPzKdKQGg27cGzmWmRVyGscobbg1r0UnguRFw" +
	            "2EbAY5+F9eJFbdZ3oYHC/9GOuuVnpQCGAx3+uiYZ7U/4AtebaAg7d7cL4lOeb+rleqmNMTn6NgR/qR3lhLh51n3mtA8MtP" +
	            "EVhrcFavgpgt2MrIW6cKpiOIlNkyaqQAdbcEi/ygyDZ4aNuOJAOuL+2HMtUMbI8GUZEfTOOufZ/3zfrrOezkr9FCeXpTVQ" +
	            "pkgiZd1qNYc7BzAS01a7DlD1nuC9oX1+rgloKdg1/R2tyeyCWLgwp3m6giCpXRvClHBctFLEoeKi/81Ceh49veHwdvtVmk" +
	            "xI6osMys4Xw3U03bR8pQ== root@test-cont\n" +
	            "    loop: \"{{ containers }}\"";
	            
	        ansibleService.createPlaybook(courseId, proxmoxPlaybook);
	}



}
