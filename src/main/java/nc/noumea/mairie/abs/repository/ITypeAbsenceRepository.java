package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.RefTypeAbsence;

public interface ITypeAbsenceRepository {

	void persistEntity(Object obj);

	<T> T getEntity(Class<T> Tclass, Object Id);

	void removeEntity(Object obj);

	List<RefTypeAbsence> getListeTypAbsence(Integer idRefGroupeAbsence);
}
