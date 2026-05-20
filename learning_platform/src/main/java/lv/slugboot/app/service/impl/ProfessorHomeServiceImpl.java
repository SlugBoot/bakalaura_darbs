package lv.slugboot.app.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lv.slugboot.app.models.Course;
import lv.slugboot.app.models.Professor;
import lv.slugboot.app.repo.ICourseRepo;
import lv.slugboot.app.repo.IProfessorRepo;
import lv.slugboot.app.service.IProfessorHomeService;

@Service
@RequiredArgsConstructor
public class ProfessorHomeServiceImpl implements IProfessorHomeService {

	private final ICourseRepo courseRepo;
	private final IProfessorRepo professorRepo;

	@Override
	public List<Course> getAllCoursesWhereProfessorIdEquals(UUID professorId) {

		if (professorId == null) {
			throw new NullPointerException("UUID is null");
		}

		return courseRepo.findByProfessorPersonId(professorId);
	}

	@Override
	@Transactional(readOnly = true)
	public Professor retrieveByUsername(String username) {
		return professorRepo.findByUsername(username);
	}

}
