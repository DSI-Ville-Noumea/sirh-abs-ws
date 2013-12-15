package nc.noumea.mairie.abs.repository;

import nc.noumea.mairie.abs.domain.EtatDemande;

public interface IDemandeRepository {

	void persisEntity(Object obj);
	
	<T> T getEntity(Class<T> Tclass, Object Id);
	
	EtatDemande getLastEtatDemandeByIdDemande(Integer idDemande);
	
}
