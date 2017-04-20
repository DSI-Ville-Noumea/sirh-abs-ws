package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.FlushModeType;

import nc.noumea.mairie.abs.domain.Demande;

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

	List<Integer> getListApprobateursDemandesSaisiesViseesJourDonne(List<Integer> listeTypesGroupe);

	List<Demande> listeDemandesSIRH(Date fromDate, Date toDate, Integer idRefEtat, Integer idRefType,
			List<Integer> listIdAgentRecherche, Integer idRefGroupeAbsence);

	Integer getNombreSamediOffertSurAnnee(Integer idAgent, Integer year, Integer idDemande);

	List<Demande> listeDemandesAgentVerification(Integer idAgentConcerne, Date fromDate, Date toDate,
			Integer idRefGroupeAbsence);

	List<Demande> listeDemandesASAAndCongesExcepSIRHAValider(Date fromDate, Date toDate, List<Integer> listIdRefGroupe,Integer idRefTypeFamille,
			List<Integer> listIdAgentRecherche);

	List<Demande> listeDemandesCongesAnnuelsSIRHAValider(Date fromDate, Date toDate, List<Integer> listIdAgentRecherche);

	List<Demande> listerDemandeCongeUnique(Integer idAgent, Integer annee);

	List<Demande> listeDemandesForListAgent(Integer idAgentConnecte,
			List<Integer> idAgentConcerne, Date fromDate, Date toDate,
			Integer idRefType, Integer idRefGroupeAbsence);

	List<Demande> getListDemandeRejetDRHStatutVeille(List<Integer> listeTypes);

}
