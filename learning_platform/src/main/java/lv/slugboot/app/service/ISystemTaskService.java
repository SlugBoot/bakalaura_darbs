package lv.slugboot.app.service;

public interface ISystemTaskService {
	public abstract void createFile(String filePath, String content) throws Exception;
	
	public abstract void deleteFile(String filePath) throws Exception;
	
	public abstract void deleteDirectory(String directoryPath) throws Exception;
	
	public abstract String executeCommand(String command) throws Exception;
	
}
