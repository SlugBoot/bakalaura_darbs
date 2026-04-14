package lv.slugboot.app.models;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "PersonTable")
public class Person {
  // TODO: Pārbaudīt RegEx darbību, kad izveidots repo
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(value = AccessLevel.NONE)
  private UUID personId;

  @Column(name = "FirstName")
  @Pattern(regexp = "([A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44}")
  @NotNull
  private String name;

  @Column(name = "MiddleName")
  @Pattern(regexp = "(([A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44})?")
  private String middleName;

  @Column(name = "LastName")
  @Pattern(regexp = "([A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44}")
  @NotNull
  private String surname;

  @Column(name = "username")
  // @Pattern(regexp="")
  private String username;

  // NOTE: Iespējams var uzlabot ar vienu "kontaktinformācijas" klasi
  @Column(name = "email")
  @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
  // avots RegEx: https://colinhacks.com/essays/reasonable-email-regex
  private String email;

  public Person(String name, String surname, String email) {
    setName(name);
    setSurname(surname);
    setEmail(email);
  }

  public Person(String name, String middleName, String surname, String email) {
    setName(name);
    setMiddleName(middleName);
    setSurname(surname);
    setEmail(email);
  }
}
