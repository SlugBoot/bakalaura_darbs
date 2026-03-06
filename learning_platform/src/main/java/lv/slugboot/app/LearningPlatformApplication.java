package lv.slugboot.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import lv.slugboot.app.models.Professor;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.repo.IProfessorRepo;
import lv.slugboot.app.repo.IStudentRepo;

@SpringBootApplication
public class LearningPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearningPlatformApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner testDB(IStudentRepo studentRepo, IProfessorRepo professorRepo) {
		return new CommandLineRunner() {
			@Override
			public void run(String... args) throws Exception {
				
				studentRepo.save(new Student("Jānis", "Pēteris", "Bērziņš", "email@email.com"));
				professorRepo.save(new Professor("Anna", "Felicita", "Fabriciusa", "words@emails.lv"));
				professorRepo.save(new Professor("Andris", "Petrograds", "andris@emails.lv"));
				System.out.println("Command Line check");
			}
		};
	}
}
