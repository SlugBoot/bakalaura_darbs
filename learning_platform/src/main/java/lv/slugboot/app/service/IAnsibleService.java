package lv.slugboot.app.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lv.slugboot.app.models.LabInstance;

public interface IAnsibleService {
	public abstract void createVarsFile(UUID courseId, Map<String, Object> variables) throws Exception;
	
	public abstract void createPlaybook(UUID courseId, String playbookYaml, String playbookName) throws Exception;
	
	public abstract String runPlaybook(UUID courseId, UUID studentId, String playbookName, String inventoryName) throws Exception;
	
	public abstract String runPlaybook(UUID courseId, String playbookName, String inventoryName) throws Exception;
	
	public abstract void createProxmoxVarsFile(UUID courseId, List<LabInstance> instances) throws Exception;

	public abstract void createInventoryFile(UUID courseId, String hostGroup, List<String> ipAddresses, String inventoryName) throws Exception;
	
}
