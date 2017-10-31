package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.FlushModeType;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.dto.DemandeDto;

public interface IDemandeRepository {

	void persistEntity(Object obj);

	<T> T getEntity(Class<T> Tclass, Object Id);

	void clear();

	void flush();

	void setFlushMode(FlushModeType flushMode);

	List<Demande> listeDemandesAgent(Integer idAgentConnecte, Integer idAgentConcerne, Date fromDate, Date toDate,
			Integer idRefType, Integer idRefGroupeAbsence);

	void removeEntity(Object obj);

	List<Integer> getListViseursDemandesSaisiesJourDonne(List<Integer> listeTypesGroupe);
	
	List<Integer> getListApprobateursForAgent(Integer idAgent);

	List<Integer> getListApprobateursDemandesSaisiesViseesJourDonne(List<Integer> listeTypesGroupe);
	
	List<Integer> getAllMaladiesSaisiesVeille();
	
	List<Integer> getListApprobateursMaladiesSaisiesViseesVeille(List<Integer> listeTypesGroupe);

	List<Demande> listeDemandesSIRH(Date fromDate, Date toDate, Integer idRefEtat, Integer idRefType,
			List<Integer> listIdAgentRecherche, Integer idRefGroupeAbsence);

	Integer getNombreSamediOffertSurAnnee(Integer idAgent, Integer year, Integer idDemande);

	List<Demande> listeDemandesAgentVerification(Integer idAgentConcerne, Date fromDate, Date toDate,
			Integer idRefGroupeAbsence);

	List<Demande> listeDemandesASAAndCongesExcepAndMaladiesSIRHAValider(Date fromDate, Date toDate, List<Integer> listIdRefGroupe,Integer idRefTypeFamille,
			List<Integer> listIdAgentRecherche, Integer maxResult);

	List<Integer> listeIdDemandesASAAndCongesExcepAndMaladiesSIRHAValider(Date fromDate, Date toDate, List<Integer> listIdRefGroupe,Integer idRefTypeFamille,
			List<Integer> listIdAgentRecherche, Integer maxResult);

	List<Demande> listeDemandesCongesAnnuelsSIRHAValider(Date fromDate, Date toDate, List<Integer> listIdAgentRecherche, Integer idTypeCA, Integer maxResult);
	
	List<Integer> listeIdDemandesCongesAnnuelsSIRHAValider(Date fromDate, Date toDate, List<Integer> listIdAgentRecherche, Integer idTypeCA, Integer maxResult);

	List<Demande> listerDemandeCongeUnique(Integer idAgent, Integer annee);
	
	List<Demande> getListDemandeRejetDRHStatutVeille(List<Integer> listeTypes);

	boolean initialDemandeForProlongationExists(DemandeDto demande);

	List<Demande> getListeATReferenceForAgent(Integer idAgent);

	/**
	 * Compte le nombre de résultats pour la liste des demandes d un liste d agents
	 * Si trop de résultats, on litmitera les résultats a retourner au KiosqueRH
	 * #42015
	 * 
	 * @param idAgentConnecte
	 * @param idAgentConcerne
	 * @param fromDate
	 * @param toDate
	 * @param idRefType
	 * @param idRefGroupeAbsence
	 * @param listEtats
	 * @return le nombre de resultats
	 */
	int countListeDemandesForListAgent(Integer idAgentConnecte, List<Integer> idAgentConcerne, Date fromDate,
			Date toDate, Integer idRefType, Integer idRefGroupeAbsence, List<RefEtat> listEtats);

	/**
	 * Retourne une liste d ID de demande qui sera utilisée ensuite par la méthode ci-dessous listeDemandesByListIdsDemande()
	 * 
	 * Optimisation : #42015
	 * 
	 * @param idAgentConnecte
	 * @param idAgentConcerne
	 * @param fromDate
	 * @param toDate
	 * @param idRefType
	 * @param idRefGroupeAbsence
	 * @param listEtats
	 * @param limitResultMax
	 * @return Une liste d ID
	 */
	List<Integer> listeIdsDemandesForListAgent(Integer idAgentConnecte, List<Integer> idAgentConcerne, Date fromDate,
			Date toDate, Integer idRefType, Integer idRefGroupeAbsence, List<RefEtat> listEtats,
			Integer limitResultMax);

	/**
	 * Retourne une liste de demandes via une liste d ID
	 * 
	 * Optimisation : #42015
	 * 
	 * @param listIdsDemande
	 * @return Liste de demandes
	 */
	List<Demande> listeDemandesByListIdsDemande(List<Integer> listIdsDemande);

}
