package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.RefEtat;

public interface IDemandeRepository {

	void persistEntity(Object obj);

	<T> T getEntity(Class<T> Tclass, Object Id);

	List<Demande> listeDemandesAgent(Integer idAgentConnecte, Integer idAgentConcerne, Date fromDate, Date toDate, Integer idRefType);

	List<RefEtat> findRefEtatNonPris();

	List<RefEtat> findRefEtatEnCours();
	
	void removeEntity(Object obj);

}
