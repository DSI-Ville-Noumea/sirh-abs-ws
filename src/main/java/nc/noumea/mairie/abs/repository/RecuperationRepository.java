package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;

import org.springframework.stereotype.Repository;

@Repository
public class RecuperationRepository implements IRecuperationRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
    private EntityManager absEntityManager;

	@Override
	public AgentRecupCount getAgentRecupCount(Integer idAgent) {
		TypedQuery<AgentRecupCount> q = absEntityManager.createNamedQuery("findAgentRecupCountByIdAgent", AgentRecupCount.class);
		q.setParameter("idAgent", idAgent);
		q.setMaxResults(1);

		List<AgentRecupCount> r = q.getResultList();
		
		return (r.size() == 1 ? r.get(0) : null);
	}

	@Override
	public AgentWeekRecup getWeekRecupForAgentAndDate(Integer idAgent, Date dateMonday) {
		TypedQuery<AgentWeekRecup> q = absEntityManager.createNamedQuery("findAgentWeekRecupByIdAgentAndDateMonday", AgentWeekRecup.class);
		q.setParameter("idAgent", idAgent);
		q.setParameter("dateMonday", dateMonday);
		q.setMaxResults(1);
		List<AgentWeekRecup> r = q.getResultList();
		
		return (r.size() == 1 ? r.get(0) : null);
	}
	
	@Override
	public void persistEntity(Object entity) {
		absEntityManager.persist(entity);
	}
	
}
