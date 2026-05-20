package lv.slugboot.app.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.LabInstance;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.models.enums.LabInstanceStatus;
import lv.slugboot.app.repo.ICourseRepo;
import lv.slugboot.app.repo.ILabInstanceRepo;
import lv.slugboot.app.repo.IStudentRepo;
import lv.slugboot.app.service.ILabInstanceCRUDService;

@Service
@RequiredArgsConstructor
public class LabInstanceCRUDServiceImpl implements ILabInstanceCRUDService {

	private final ILabInstanceRepo labInstanceRepo;
	private final IStudentRepo studentRepo;
	private final ICourseRepo courseRepo;

	@Override
	public void createLabInstance(UUID studentId, UUID courseId, String ipAddress) throws NoSuchFieldException {
		if (studentId == null) {
			throw new NullPointerException("Student Id cannot be null");
		}

		if (courseId == null) {
			throw new NullPointerException("Course ID cannot be null");
		}

		if (ipAddress == null) {
			throw new NullPointerException("IP Address cannot be null");
		}

		Student student = studentRepo.findById(studentId).get();
		Course course = courseRepo.findById(courseId).get();

		if (!student.getCourse().contains(course)) {
			throw new NoSuchFieldException("Student is not enrolled in course");
		}

		LabInstance labInstance = new LabInstance(student, course, ipAddress);

		labInstanceRepo.save(labInstance);
	}

	@Override
	public List<LabInstance> retrieveAll() throws NoSuchFieldException {
		if (labInstanceRepo.count() == 0) {
			throw new NoSuchFieldException("There are no lab instances made");
		}

		return labInstanceRepo.findAll();
	}

	@Override
	public LabInstance retrieveById(UUID instanceId) throws NoSuchFieldException {
		if (instanceId == null) {
			throw new NullPointerException("Instance ID cannot be null");
		}

		if (!labInstanceRepo.existsById(instanceId)) {
			throw new NoSuchFieldException("Instance with this ID does not exist");
		}

		return labInstanceRepo.findById(instanceId).get();
	}

	@Override
	public void deleteLabInstanceById(UUID instanceId) throws NoSuchFieldException {
		LabInstance labInstance = retrieveById(instanceId);

		labInstanceRepo.delete(labInstance);
	}

	@Override
	public void updateLabInstanceStatusById(UUID instanceId, LabInstanceStatus labInstanceStatus)
			throws NoSuchFieldException {
		LabInstance labInstance = retrieveById(instanceId);

		labInstance.setStatus(labInstanceStatus);
		labInstanceRepo.save(labInstance);
	}

	@Override
	public List<LabInstance> retrieveByCourseId(UUID courseId) {
		Course course = courseRepo.findById(courseId).get();

		return labInstanceRepo.findByCourse(course);
	}

	@Override
	public void updateIPAddressById(UUID instanceId, String ipAddress) throws NoSuchFieldException {
		LabInstance labInstance = retrieveById(instanceId);

		labInstance.setIpAddress(ipAddress);
		labInstanceRepo.save(labInstance);
	}

}
