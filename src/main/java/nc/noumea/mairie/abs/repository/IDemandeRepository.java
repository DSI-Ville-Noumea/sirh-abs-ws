package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.EtatDemande;

public interface IDemandeRepository {

	void persisEntity(Object obj);

	<T> T getEntity(Class<T> Tclass, Object Id);

	EtatDemande getLastEtatDemandeByIdDemande(Integer idDemande);

	List<Demande> listeDemandesAgentNonPrises(Integer idAgentConnecte, Date fromDate, Date toDate, Date dateDemande,
			Integer idRefType);
	
	
}
