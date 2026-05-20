package lv.slugboot.app.service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface ISystemTaskService {
	public abstract void createFile(String filePath, String content) throws IOException;

	public abstract void deleteFile(String filePath) throws IOException;

	public abstract void deleteDirectory(String directoryPath);

	public abstract String executeCommand(String command) throws IOException, InterruptedException;


}
