package nc.noumea.mairie.abs.repository;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;

import org.springframework.stereotype.Repository;

@Repository
public class RecuperationRepository implements IRecuperationRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
    private EntityManager absEntityManager;

	@Override
	public AgentRecupCount getAgentRecupCount(Integer idAgent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AgentWeekRecup getWeekRecupForAgentAndDate(Integer idAgent, Date dateMonday) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void persistEntity(Object entity) {
		absEntityManager.persist(entity);
	}
	
}
