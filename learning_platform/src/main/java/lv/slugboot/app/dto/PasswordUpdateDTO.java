package lv.slugboot.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordUpdateDTO {

	@NotBlank(message = "Current password is required")
	private String currentPassword;

	@NotBlank(message = "New password is required")
	@Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
	private String newPassword;

	@NotBlank(message = "Please confirm your new password")
	private String confirmPassword;

	public boolean isNewPasswordMatching() {
		return this.newPassword != null && this.newPassword.equals(this.confirmPassword);
	}
}
