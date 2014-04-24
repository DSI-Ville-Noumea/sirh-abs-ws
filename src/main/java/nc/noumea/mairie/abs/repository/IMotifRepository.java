package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.Motif;
import nc.noumea.mairie.abs.domain.MotifCompteur;

public interface IMotifRepository {

	void persistEntity(Object obj);

	<T> T getEntity(Class<T> Tclass, Object Id);

	List<Motif> getListeMotif();

	List<MotifCompteur> getListeMotifCompteur(Integer idType);

	void flush();
}
