package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;

public interface ICounterRepository {

	<T> T getAgentCounter(Class<T> T, Integer idAgent);

	<T> T getAgentCounterByDate(Class<T> T, Integer idAgent, Date dateDebutDemande);

	<T> T getWeekHistoForAgentAndDate(Class<T> T, Integer idAgent, Date dateMonday);

	void persistEntity(Object entity);

	<T> T getEntity(Class<T> Tclass, Object Id);

	List<Integer> getListAgentReposCompCountForResetAnneePrcd();

	List<Integer> getListAgentReposCompCountForResetAnneeEnCours();

	<T> List<T> getListCounter(Class<T> T);

	List<AgentHistoAlimManuelle> getListHistoByRefTypeAbsenceAndAgent(Integer idAgent, Integer codeRefTypeAbsence);
}
