package lv.slugboot.app.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lv.slugboot.app.models.LabInstance;

public interface IAnsibleService {
	public abstract void createVarsFile(UUID courseId, Map<String, Object> variables) throws Exception;
	
	public abstract void createInventoryFile(UUID courseId, String hostGroup, String ipAddress) throws Exception;
	
	public abstract void createPlaybook(UUID courseId, String playbookYaml) throws Exception;
	
	public abstract String runPlaybook(UUID courseId, UUID studentId) throws Exception;
	
	public abstract String runPlaybook(UUID courseId) throws Exception;
	
	public abstract void createProxmoxVarsFile(UUID courseId, List<LabInstance> instances) throws Exception;
	
}
