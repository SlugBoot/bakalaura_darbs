package lv.slugboot.app.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lv.slugboot.app.models.Person;
import lv.slugboot.app.models.Professor;
import lv.slugboot.app.repo.IPersonRepo;

@Component
@RequiredArgsConstructor
public class SuccessHandler implements AuthenticationSuccessHandler{

	private final IPersonRepo personRepo;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		String username = authentication.getName();
		Person person = personRepo.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found after login"));
		
		if (person instanceof Professor) {
			response.sendRedirect("/professor/home/" + person.getPersonId());
		} else {
			response.sendRedirect("/student/home/" + person.getPersonId());
		}
	}

}
