package lv.slugboot.app.models;

import java.text.Normalizer;
import java.util.Collection;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "ProfessorTable")
public class Professor extends Person {

	@ToString.Exclude
	@OneToMany(mappedBy = "professor")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Collection<Course> course;

	private String createUsername() {
		String nameStr = (this.getName() != null) ? this.getName().trim().toLowerCase() : "";
	    String surnameStr = (this.getSurname() != null) ? this.getSurname().trim().toLowerCase() : "";
		
	    nameStr = nameStr.replaceAll("[^a-z\\p{L}]", "");
	    surnameStr = surnameStr.replaceAll("[^a-z\\p{L}]", "");
		
	    String usernameBase = surnameStr + "." + nameStr;
	    
	    if (this.getMiddleName() != null && !this.getMiddleName().trim().isEmpty()) {
	        String middleStr = this.getMiddleName().trim().toLowerCase().replaceAll("[^a-z\\p{L}]", "");
	        if (!middleStr.isEmpty()) {
	            usernameBase = usernameBase + "." + middleStr;
	        }
	    }

		String usernameDecomposed = Normalizer.normalize(usernameBase, Normalizer.Form.NFD);
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

		String cleanUsername = pattern.matcher(usernameDecomposed).replaceAll("");
		return cleanUsername.toLowerCase();
	}

	public Professor(String name, String surname, String email) {
		super(name, surname, email);
		this.setUsername(createUsername());
	}

	public Professor(String name, String middleName, String surname, String email) {
		super(name, middleName, surname, email);
		this.setUsername(createUsername());
	}
}
