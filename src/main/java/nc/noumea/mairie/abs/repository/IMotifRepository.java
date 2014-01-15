package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.MotifRefus;

public interface IMotifRepository {

	void persistEntity(Object obj);

	<T> T getEntity(Class<T> Tclass, Object Id);

	List<MotifRefus> getListeMotifRefus(Integer idType);

	List<MotifCompteur> getListeMotifCompteur(Integer idType);

}
