package nc.noumea.mairie.sirh.repository;

import nc.noumea.mairie.sirh.domain.Agent;

public interface IAgentRepository {

	Agent findAgent(Integer idAgent);
}
