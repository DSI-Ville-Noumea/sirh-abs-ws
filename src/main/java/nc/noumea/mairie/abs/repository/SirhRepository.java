package nc.noumea.mairie.abs.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.domain.SpSold;
import nc.noumea.mairie.sirh.domain.Agent;

import org.springframework.stereotype.Repository;

@Repository
public class SirhRepository implements ISirhRepository {

	@PersistenceContext(unitName = "sirhPersistenceUnit")
	private EntityManager sirhEntityManager;

	@Override
	public Agent getAgent(Integer idAgent) {
		return sirhEntityManager.find(Agent.class, idAgent);
	}

	@Override
	public SpSold getSpsold(Integer idAgent) {
		Integer nomatr = Integer.valueOf(idAgent.toString().substring(3, idAgent.toString().length()));
		return sirhEntityManager.find(SpSold.class, nomatr);
	}
}
