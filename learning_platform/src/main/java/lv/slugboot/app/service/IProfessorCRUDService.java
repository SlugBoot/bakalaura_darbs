package lv.slugboot.app.service;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import lv.slugboot.app.dto.PasswordUpdateDTO;
import lv.slugboot.app.models.Professor;

public interface IProfessorCRUDService {

	public abstract void createProfessor(String name, String middleName, String surname, String email);

	public abstract List<Professor> retrieveAll() throws NoSuchFieldException;

	public abstract Professor retrieveById(UUID id) throws NoSuchFieldException;

	public abstract void updateProfessorById(UUID id, String name, String middleName, String surname, String email)
			throws NoSuchFieldException;

	public abstract void deleteProfessorById(UUID id) throws NoSuchFieldException;
	
	public abstract void updatePasswordById(UUID professorId, PasswordUpdateDTO passwordDTO) throws NoSuchFieldException;

	public abstract Professor retrieveByUsername(String username) throws NoSuchFieldException;
}
