package nc.noumea.mairie.abs.repository;

import java.util.Date;

public interface ICounterRepository {

	<T> T getAgentCounter(Class<T> T, Integer idAgent);
	
	<T> T getWeekHistoForAgentAndDate(Class<T> T, Integer idAgent, Date dateMonday);
	
	void persistEntity(Object entity);
	
}
