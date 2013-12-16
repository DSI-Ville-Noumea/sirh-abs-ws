package nc.noumea.mairie.abs.repository;

import java.util.Date;

import nc.noumea.mairie.domain.SpSold;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.sirh.domain.Agent;

public interface ISirhRepository {

	Agent getAgent(Integer idAgent);

	SpSold getSpsold(Integer idAgent);
	
	Spadmn getAgentCurrentPosition(Agent agent, Date asOfDate);
}
