package lv.slugboot.app.service;

import java.util.List;
import java.util.UUID;

import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.LabInstance;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.models.enums.LabInstanceStatus;

public interface ILabInstanceCRUDService {

	public abstract void createLabInstance(UUID studentId, UUID courseID, String ipAddress) throws NoSuchFieldException;

	public abstract List<LabInstance> retrieveAll() throws NoSuchFieldException;

	public abstract List<LabInstance> retrieveByCourseId(UUID courseId);

	public abstract LabInstance retrieveById(UUID id) throws NoSuchFieldException;

	public abstract void deleteLabInstanceById(UUID id) throws NoSuchFieldException;

	public abstract void updateLabInstanceStatusById(UUID instanceId, LabInstanceStatus labInstanceStatus)
			throws NoSuchFieldException;

	public abstract void updateIPAddressById(UUID instanceId, String ipAddress) throws NoSuchFieldException;

	public abstract LabInstance retrieveByCourseAndStudent(Course course, Student student);

}
