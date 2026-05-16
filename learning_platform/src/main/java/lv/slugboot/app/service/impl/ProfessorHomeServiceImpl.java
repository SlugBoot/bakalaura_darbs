package lv.slugboot.app.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lv.slugboot.app.models.Course;
import lv.slugboot.app.repo.ICourseRepo;
import lv.slugboot.app.service.IProfessorHomeService;

@Service
@RequiredArgsConstructor
public class ProfessorHomeServiceImpl implements IProfessorHomeService {

	private final ICourseRepo courseRepo;

	@Override
	public List<Course> getAllCoursesWhereProfessorIdEquals(UUID professorId) {

		if (professorId == null) {
			throw new NullPointerException("UUID is null");
		}

		return courseRepo.findByProfessorPersonId(professorId);
	}

}
