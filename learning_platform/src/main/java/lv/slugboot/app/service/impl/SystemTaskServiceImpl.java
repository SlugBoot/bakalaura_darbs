package lv.slugboot.app.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import lombok.extern.slf4j.Slf4j;
import lv.slugboot.app.service.ISystemTaskService;

@Service
@Slf4j
public class SystemTaskServiceImpl implements ISystemTaskService{

	@Override
	public void createFile(String filePath, String content) throws Exception {
		Path path = Paths.get(filePath);
		if (path.getParent() != null) {
			Files.createDirectories(path.getParent());
		}
		Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		log.info("File created at: {}", filePath);
	}

	@Override
	public void deleteFile(String filePath) throws Exception {
		Path path = Paths.get(filePath);
		Files.deleteIfExists(path);
		log.info("File deleted at: {}", filePath);
	}

	@Override
	public String executeCommand(String command) throws Exception {
		ProcessBuilder processBuilder = new ProcessBuilder();
		
		processBuilder.command("sh", "-c", command);
		
		Process process = processBuilder.start();
		StringBuilder fullOutput = new StringBuilder();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println("[LIVE LOG]:" + line);
				fullOutput.append(line).append("\n");
			}
			String output = reader.lines().collect(Collectors.joining("\n"));
			int exitCode = process.waitFor();
			
			if (exitCode != 0) {
				throw new Exception(fullOutput.toString());
			}
			
			return output;
		}
	}

	@Override
	public void deleteDirectory(String directoryPath) throws Exception {
		File directory = new File(directoryPath);
		FileSystemUtils.deleteRecursively(directory);
	}

}
