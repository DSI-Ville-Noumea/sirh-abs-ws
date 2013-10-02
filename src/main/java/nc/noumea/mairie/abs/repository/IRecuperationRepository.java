package nc.noumea.mairie.abs.repository;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;

public interface IRecuperationRepository {

	AgentRecupCount getAgentRecupCount(Integer idAgent);
	
	AgentWeekRecup getWeekRecupForAgentAndDate(Integer idAgent, Date dateMonday);
	
}
