package lv.slugboot.app.service.impl;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lv.slugboot.app.models.Person;
import lv.slugboot.app.models.Professor;
import lv.slugboot.app.repo.IPersonRepo;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final IPersonRepo personRepo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Person person = personRepo.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		String role = (person instanceof Professor) ? "PROFESSOR" : "STUDENT";

		return User.builder().username(person.getUsername()).password(person.getPassword()).roles(role).build();

	}

}
