package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentReposCompCount;

public interface ICounterRepository {

	<T> T getAgentCounter(Class<T> T, Integer idAgent);

	<T> T getAgentCounterByDate(Class<T> T, Integer idAgent, Date dateDebut, Date dateFin);

	<T> T getWeekHistoForAgentAndDate(Class<T> T, Integer idAgent, Date dateMonday);

	void persistEntity(Object entity);

	<T> T getEntity(Class<T> Tclass, Object Id);

	AgentReposCompCount getAgentReposCompCountByIdCounter(Integer IdCounter);

	List<Integer> getListAgentReposCompCountForResetAnneePrcd();

	List<Integer> getListAgentReposCompCountForResetAnneeEnCours();
}
