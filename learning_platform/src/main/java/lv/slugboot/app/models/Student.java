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
  private Collection<Grade> grades;
  
  @OneToMany(mappedBy = "student")
  private Collection<LabInstance> labs;

  private String createUsername() {
    String year = Integer.toString(LocalDate.now().getYear());
    // Lietotāja 4 uzvārda burti, 4 vārda burti, lietotāja izveidošanas gads
    String usernameBase = this.getSurname().toLowerCase().substring(0, 4).concat(
        this.getName().toLowerCase().substring(0, 4)).concat(
            year.substring(2, 4));

    String usernameDecomposed = Normalizer.normalize(usernameBase, Normalizer.Form.NFD);
    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    
    return pattern.matcher(usernameDecomposed).replaceAll("");
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
