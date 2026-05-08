package lv.slugboot.app.service;

import java.util.Map;

public interface IAnsibleService {
	public abstract void createVarsFile(String courseId, Map<String, Object> variables) throws Exception;
	
	public abstract void createInventoryFile(String courseId, String hostGroup, String ipAddress) throws Exception;
	
	public abstract void createPlaybook(String courseId, String playbookYaml) throws Exception;
	
	public abstract String runPlaybook(String courseId) throws Exception;
}
