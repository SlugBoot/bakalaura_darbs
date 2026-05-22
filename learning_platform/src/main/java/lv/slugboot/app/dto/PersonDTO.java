package lv.slugboot.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonDTO {

	public interface OnCreate extends jakarta.validation.groups.Default{}
	
	private static final String NAME_REGEX_PATTERN = "([A-ZĀĒĪŪŽŠČĶĢĻŅ])([a-zāēīūžščļķģņ]){1,44}";

	@NotNull
	@Pattern(regexp = NAME_REGEX_PATTERN, message = "Invalid first name")
	private String name;

	@Pattern(regexp = "(" + NAME_REGEX_PATTERN + ")?", message = "Invalid middle name")
	private String middleName;

	@NotNull
	@Pattern(regexp = NAME_REGEX_PATTERN, message = "Invalid surname")
	private String surname;

	@NotNull
	@Email
	@Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email")
	private String email;
	
	@NotBlank(message = "Password is required", groups = OnCreate.class)
	@Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
	private String password;
}
