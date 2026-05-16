package lv.slugboot.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfessorDTO {

	@NotNull
    @Pattern(regexp = "([A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44}", message = "Invalid first name")
    private String name;

    @Pattern(regexp = "(([A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44})?", message = "Invalid middle name")
    private String middleName;

    @NotNull
    @Pattern(regexp = "([A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44}", message = "Invalid surname")
    private String surname;

    @NotNull
    @Email
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email")
    private String email;
}
