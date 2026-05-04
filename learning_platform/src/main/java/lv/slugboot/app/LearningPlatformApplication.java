package lv.slugboot.app;

import java.util.Arrays;

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
				Professor testProf3 = new Professor("Jānis", "Cimze", "janis@emails.lv");
				Professor testProf4 = new Professor("Pēteris", "Agro", "Sviestnieks", "peteris@emails.lv");
				
				
				Student testStud1 = new Student("Jānis", "Pēteris", "Bērziņš", "janis_students@email.com");
				Student testStud2 = new Student("Annija", "Birzniece", "annija_studente@email.com");
				Student testStud3 = new Student("Kārlis", "Blaumanis", "karlis_students@email.com");
				Student testStud4 = new Student("Jana", "Liepiņa", "jana_studente@email.com");
				
				Course testCourse1 = new Course("1. uzdevums", "1. laboratorijas vide", testProf2);
				Course testCourse2 = new Course("2. uzdevums", "2. laboratorijas vide", testProf2);
				Course testCourse3 = new Course("3. uzdevums", testProf4);
				Course testCourse4 = new Course("4. uzdevums", "Cita laboratorijas vide", testProf2);
				
				
				studentRepo.saveAll(Arrays.asList(testStud1, testStud2, testStud3, testStud4));
				professorRepo.saveAll(Arrays.asList(testProf1, testProf2, testProf3, testProf4));
				courseRepo.saveAll(Arrays.asList(testCourse1, testCourse2, testCourse3, testCourse4));
				System.out.println("Command Line check");
			}
		};
	}
}
