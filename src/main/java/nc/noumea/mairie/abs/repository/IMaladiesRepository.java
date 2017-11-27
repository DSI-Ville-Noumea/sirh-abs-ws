package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.domain.RefDroitsMaladies;

public interface IMaladiesRepository {

	/**
	 * Retourne les demandes de maladies a l etat PRISE
	 * sur une annee glissante par rapport a la date de fin de la periode passee en parametre
	 * pour un agent.
	 * 
	 * Date de debut  = date de fin - 1 an  + 1 jour
	 * 
	 * @param idAgent Integer L agent
	 * @param dateFinAnneeGlissante Date de fin de l annee glissante
	 * @return List<DemandeMaladies> la liste des demandes de maladies
	 */
	List<DemandeMaladies> getListMaladiesAnneGlissanteByAgent(Integer idAgent, Date dateDebutAnneeGlissante, Date dateFinAnneeGlissante);
	

	List<DemandeMaladies> getListMaladiesAnneGlissanteRetroactiveByAgent(Integer idAgent, Date dateDebutAnneeGlissante, Date dateFinAnneeGlissante, Integer idDemande, boolean isCancel, boolean isRetroactif);

	/**
	 * Retourne la liste des demandes de maladies ayant une date de début supérieure à la demande en cours.
	 * 
	 * @param idAgent Integer L agent
	 * @param dateDebut Date de départ
	 * @return List<DemandeMaladies> la liste des demandes de maladies
	 */
	List<DemandeMaladies> getListMaladiesFuturesForDemande(Integer idAgent, Date dateDebut);

	/**
	 * Retourne les droits maladies qu un agent possède
	 * selon son statut et son anciennete.
	 *  
	 * @param isFonctionnaire boolean
	 * @param isContractuel boolean
	 * @param isConvColl boolean
	 * @param anneeAnciennete Integer Nombre annee anciennete arrondi a l annee inferieure
	 * @return RefDroitsMaladies
	 */
	RefDroitsMaladies getDroitsMaladies(boolean isFonctionnaire,
			boolean isContractuel, boolean isConvColl, Integer anneeAnciennete);

	List<DemandeMaladies> getListEnfantMaladeAnneeCivileByAgent(Integer idAgent, Date dateDebutAnneeForOneDate, Date dateFinAnneeForOneDate);

	boolean getInitialATByAgent(Integer idAgent, Date dateAT);
	
	List<DemandeMaladies> getAllATByDateATAndAgentId(Date dateAT, Integer idAgent);
}
