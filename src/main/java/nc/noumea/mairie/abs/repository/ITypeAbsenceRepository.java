package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;

public interface ITypeAbsenceRepository {

	void persistEntity(Object obj);

	<T> T getEntity(Class<T> Tclass, Object Id);

	List<RefTypeAbsence> getListeTypAbsence(Integer idRefGroupeAbsence);

	List<RefTypeSaisiCongeAnnuel> getListeTypeSaisiCongeAnnuel();
}
