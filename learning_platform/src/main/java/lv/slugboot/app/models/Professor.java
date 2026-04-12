package lv.slugboot.app.models;

import java.text.Normalizer;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
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
  @OneToOne(mappedBy = "professor")
  private Course course;

  private String createUsername() {
    // Lietotāja vards.uzvards
    String usernameBase = this.getSurname().toLowerCase().concat(".").concat(
        this.getName().toLowerCase());
    if (this.getMiddleName() != null) {
      usernameBase = usernameBase.concat(".").concat(this.getMiddleName().toLowerCase());
    }

    String usernameDecomposed = Normalizer.normalize(usernameBase, Normalizer.Form.NFD);
    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    String username = pattern.matcher(usernameDecomposed).replaceAll("");
    return username;
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
