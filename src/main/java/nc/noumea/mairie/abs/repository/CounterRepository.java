package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

@Repository
public class CounterRepository implements ICounterRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
    private EntityManager absEntityManager;
	
	@Override
	public <T> T getAgentCounter(Class<T> T, Integer idAgent) {
		
		// Build query criteria
		CriteriaBuilder cb = absEntityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(T);
		Root<T> c = cq.from(T);
		cq.select(c);
		ParameterExpression<Integer> p = cb.parameter(Integer.class, "idAgent");
		cq.where(cb.equal(c.get("idAgent"), p));
		
		// Build query
		TypedQuery<T> q = absEntityManager.createQuery(cq);
		q.setParameter("idAgent", idAgent);
		q.setMaxResults(1);

		// Exec query
		List<T> r = q.getResultList();
		
		return (r.size() == 1 ? r.get(0) : null);
	}

	@Override
	public <T> T getWeekHistoForAgentAndDate(Class<T> T, Integer idAgent, Date dateMonday) {

		// Build query criteria
		CriteriaBuilder cb = absEntityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(T);
		Root<T> c = cq.from(T);
		cq.select(c);
		ParameterExpression<Integer> p = cb.parameter(Integer.class, "idAgent");
		ParameterExpression<Date> p2 = cb.parameter(Date.class, "dateMonday");
		cq.where(cb.and(cb.equal(c.get("idAgent"), p), cb.equal(c.get("dateMonday"), p2)));
		
		// Build query
		TypedQuery<T> q = absEntityManager.createQuery(cq);
		q.setParameter("idAgent", idAgent);
		q.setParameter("dateMonday", dateMonday);
		q.setMaxResults(1);
		
		// Exec query
		List<T> r = q.getResultList();
		
		return (r.size() == 1 ? r.get(0) : null);
	}
	
	@Override
	public void persistEntity(Object entity) {
		absEntityManager.persist(entity);
	}
}
