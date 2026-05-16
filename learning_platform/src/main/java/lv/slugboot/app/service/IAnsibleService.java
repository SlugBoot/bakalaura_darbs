package lv.slugboot.app.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lv.slugboot.app.models.LabInstance;

public interface IAnsibleService {
	public abstract void createVarsFile(UUID courseId, Map<String, Object> variables) throws IOException;
	
	public abstract void createInventoryFile(UUID courseId, String hostGroup, List<String> ipAddresses, String inventoryName) throws IOException;
	
	public abstract void createPlaybook(UUID courseId, String playbookYaml, String playbookName) throws IOException;
	
	public abstract String runPlaybook(UUID courseId, UUID studentId, String playbookName, String inventoryName) throws IOException, InterruptedException;
	
	public abstract String runPlaybook(UUID courseId, String playbookName, String inventoryName) throws IOException, InterruptedException;
	
	public abstract void createProxmoxVarsFile(UUID courseId, List<LabInstance> instances) throws IOException, InterruptedException;

	
}
