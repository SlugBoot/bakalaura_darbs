package lv.slugboot.app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lv.slugboot.app.models.LabInstance;
import lv.slugboot.app.models.enums.LabInstanceStatus;

public interface ILabInstanceCRUDService {

	public abstract void createLabInstance(UUID studentId, UUID courseID, String ipAddress) throws Exception;
	
	public abstract ArrayList<LabInstance> retrieveAll() throws Exception;
	
	public abstract List<LabInstance> retrieveByCourseId(UUID courseId) throws Exception;
	
	public abstract LabInstance retrieveById(UUID id) throws Exception;
		
	public abstract void deleteLabInstanceById(UUID id) throws Exception;
	
	public abstract void updateLabInstanceStatusById(UUID instanceId, LabInstanceStatus labInstanceStatus) throws Exception;
	
	public abstract void updateIPAddressById(UUID instanceId, String ipAddress) throws Exception;
	
}
