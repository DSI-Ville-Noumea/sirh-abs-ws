package nc.noumea.mairie.sirh.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import nc.noumea.mairie.sirh.domain.Agent;

@Repository
public class AgentRepository implements IAgentRepository {

	@PersistenceContext(unitName = "sirhPersistenceUnit")
    private EntityManager sirhEntityManager;
	
	@Override
	public Agent findAgent(Integer idAgent) {
		if (idAgent == null) return null; 
		return sirhEntityManager.find(Agent.class, idAgent);
	}
}
