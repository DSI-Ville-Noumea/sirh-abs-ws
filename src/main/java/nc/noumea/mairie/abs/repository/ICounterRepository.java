package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA52Count;
import nc.noumea.mairie.abs.domain.AgentAsaA55Count;
import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.AgentCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;

public interface ICounterRepository {

	void persistEntity(Object entity);

	<T> T getEntity(Class<T> Tclass, Object Id);

	void removeEntity(Object obj);

	<T> T getAgentCounter(Class<T> T, Integer idAgent);

	<T> T getAgentCounterByDate(Class<T> T, Integer idAgent, Date date);
	
	<T> Integer countAllByYear(Class<T> T, Integer annee, Integer idAgentRecherche, Date dateMin, Date dateMax);

	<T> T getWeekHistoForAgentAndDate(Class<T> T, Integer idAgent, Date dateMonday);

	List<Integer> getListAgentReposCompCountForResetAnneePrcd();

	List<Integer> getListAgentReposCompCountForResetAnneeEnCours();

	<T> List<T> getListCounter(Class<T> T);
	
	<T> List<T> getListCounter(Class<T> T, Integer idAgentRecherche, Integer annee, Boolean actif);

	List<AgentHistoAlimManuelle> getListHisto(Integer idAgent, AgentCount compteurAgent);

	List<AgentHistoAlimManuelle> getListHistoOrganisationSyndicale(AgentCount compteurAgent);

	List<AgentAsaA55Count> getListAgentCounterA55ByDate(Integer idAgent, Date dateDebut, Date dateFin);

	<T> T getOSCounterByDate(Class<T> T, Integer idOrganisationSyndicale, Date date);

	List<Integer> getListAgentCongeAnnuelCountForReset();

	List<AgentAsaA52Count> getListOSCounterByDateAndOrganisation(Integer idOrganisationSyndicale, Date dateDebut, Date dateFin, Integer idCompteur);

	AgentWeekRecup getWeekHistoRecupCountByIdAgentAndIdPointage(Integer idAgent, Integer idPointage);

	<T> List<T> getListCounterByOrganisation(Class<T> T, Integer idOrganisation);

	<T> List<T> getListCounterByAnnee(Class<T> T, Integer annee);

	<T> List<T> getListCounterByAnneeAndAgent(Class<T> T, Integer annee, Integer pageSize, Integer pageNumber, Integer idAgentRecherche);
	
	<T> List<T> getListCounterByDate(Class<T> T, Integer pageSize, Integer pageNumber, Integer idAgentRecherche, Date dateMin, Date dateMax);

	List<AgentCongeAnnuelCount> getListAgentCongeAnnuelCountWithListAgents(List<Integer> listIdsAgent);

}
