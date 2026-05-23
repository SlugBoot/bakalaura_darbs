package lv.slugboot.app.models;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Collection;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "StudentTable")
public class Student extends Person {

	@ManyToMany
	@ToString.Exclude
	private Collection<Course> course;

	@OneToMany(mappedBy = "student")
	@ToString.Exclude
	private Collection<Grade> grades;

	@OneToMany(mappedBy = "student")
	@ToString.Exclude
	private Collection<LabInstance> labs;

	private String createUsername() {
		String surnameStr = (this.getSurname() != null) ? this.getSurname().trim().toLowerCase() : "";
	    String nameStr = (this.getName() != null) ? this.getName().trim().toLowerCase() : "";
		
	    String safeSurname = surnameStr.substring(0, Math.min(surnameStr.length(), 4));
	    String safeName = nameStr.substring(0, Math.min(nameStr.length(), 4));
	    
	    String yearTwoDigits = String.valueOf(LocalDate.now().getYear() % 100);
		// Lietotāja 4 uzvārda burti, 4 vārda burti, lietotāja izveidošanas gads
	    String usernameBase = safeSurname + safeName + yearTwoDigits;
	    
		String usernameDecomposed = Normalizer.normalize(usernameBase, Normalizer.Form.NFD);
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

		String cleanUsername =  pattern.matcher(usernameDecomposed).replaceAll("");
		
		return cleanUsername.replaceAll("[^a-z0-9]", "");
	}

	public Student(String name, String surname, String email) {
		super(name, surname, email);
		this.setUsername(createUsername());
	}

	public Student(String name, String middleName, String surname, String email) {
		super(name, middleName, surname, email);
		this.setUsername(createUsername());
	}
}
