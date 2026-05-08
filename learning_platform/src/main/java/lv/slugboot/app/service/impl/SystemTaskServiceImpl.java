package lv.slugboot.app.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lv.slugboot.app.service.ISystemTaskService;

@Service
public class SystemTaskServiceImpl implements ISystemTaskService{

	@Override
	public void createFile(String filePath, String content) throws Exception {
		Path path = Paths.get(filePath);
		Files.write(path, content.getBytes());
	}

	@Override
	public boolean deleteFile(String filePath) throws Exception {
		Path path = Paths.get(filePath);
		return Files.deleteIfExists(path);
	}

	@Override
	public String executeCommand(String command) throws Exception {
		ProcessBuilder processBuilder = new ProcessBuilder();
		
		processBuilder.command("sh", "-c", command);
		
		Process process = processBuilder.start();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String output = reader.lines().collect(Collectors.joining("\n"));
			int exitCode = process.waitFor();
			
			if (exitCode != 0) {
				return "Error: Command failed with exit code " + exitCode;
			}
			
			return output;
		}
	}

}
