package lv.slugboot.app.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		var authorities = authentication.getAuthorities();
		String redirectUrl = "/login?error";
		
		for (var authority : authorities) {
			if (authority.getAuthority().equals("PROFESSOR")) {
				redirectUrl = "/professor/home";
				break;
			} else if (authority.getAuthority().equals("STUDENT")) {
				redirectUrl = "/student/home";
				break;
			}
		}
		
		response.sendRedirect(redirectUrl);
	}

}
