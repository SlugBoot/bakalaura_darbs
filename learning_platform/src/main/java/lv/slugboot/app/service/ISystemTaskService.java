package lv.slugboot.app.service;

public interface ISystemTaskService {
	public abstract void createFile(String filePath, String content) throws Exception;
	
	public abstract boolean deleteFile(String filePath) throws Exception;
	
	public abstract String executeCommand(String command) throws Exception;
}
