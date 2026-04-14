package lv.slugboot.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.Professor;
import lv.slugboot.app.models.Student;
import lv.slugboot.app.repo.ICourseRepo;
import lv.slugboot.app.repo.IGradeRepo;
import lv.slugboot.app.repo.IProfessorRepo;
import lv.slugboot.app.repo.IStudentRepo;

@SpringBootApplication
public class LearningPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearningPlatformApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner testDB(IStudentRepo studentRepo, IProfessorRepo professorRepo, 
			ICourseRepo courseRepo, IGradeRepo gradeRepo) {
		return new CommandLineRunner() {
			@Override
			public void run(String... args) throws Exception {
				
				Professor testProf1 = new Professor("Anna", "Felicita", "Fabriciusa", "words@emails.lv");
				Professor testProf2 = new Professor("Andris", "Petrograds", "andris@emails.lv");
				
				Student testStud1 = new Student("Jānis", "Pēteris", "Bērziņš", "email@email.com");
				
				Course testCourse1 = new Course("1. uzdevums", "1. laboratorijas vide", testProf2);
				
				studentRepo.save(testStud1);
				professorRepo.save(testProf1);
				professorRepo.save(testProf2);
				courseRepo.save(testCourse1);
				System.out.println("Command Line check");
			}
		};
	}
}
