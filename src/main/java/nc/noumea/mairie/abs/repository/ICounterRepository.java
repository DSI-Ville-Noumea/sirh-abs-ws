package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA55Count;
import nc.noumea.mairie.abs.domain.AgentCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;

public interface ICounterRepository {

	void persistEntity(Object entity);

	<T> T getEntity(Class<T> Tclass, Object Id);

	<T> T getAgentCounter(Class<T> T, Integer idAgent);

	<T> T getAgentCounterByDate(Class<T> T, Integer idAgent, Date date);

	<T> T getWeekHistoForAgentAndDate(Class<T> T, Integer idAgent, Date dateMonday);

	List<Integer> getListAgentReposCompCountForResetAnneePrcd();

	List<Integer> getListAgentReposCompCountForResetAnneeEnCours();

	<T> List<T> getListCounter(Class<T> T);

	List<AgentHistoAlimManuelle> getListHisto(Integer idAgent, AgentCount compteurAgent);

	List<AgentAsaA55Count> getListAgentCounterByDate(Integer idAgent, Date dateDebut, Date dateFin);

	<T> T getOSCounterByDate(Class<T> T, Integer idOrganisationSyndicale, Date date);

	List<Integer> getListAgentCongeAnnuelCountForReset();
}
