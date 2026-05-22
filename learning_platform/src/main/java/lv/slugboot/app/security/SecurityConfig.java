package lv.slugboot.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final SuccessHandler successHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) {
		http.authorizeHttpRequests(auth -> auth

				.requestMatchers("/css/**", "/error", "/js/**", "/images/**", "/webjars/**").permitAll()

				.requestMatchers("/login").permitAll()

				.requestMatchers("/professor/**").hasRole("PROFESSOR")

				.requestMatchers("/student/crud/**").hasAnyRole("PROFESSOR", "STUDENT")

				.requestMatchers("/student/**").hasRole("STUDENT")

				.requestMatchers("/course/crud/**").authenticated()

				.anyRequest().authenticated())
				.formLogin(form -> form.loginPage("/login").successHandler(successHandler).permitAll())
				.logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll());

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
