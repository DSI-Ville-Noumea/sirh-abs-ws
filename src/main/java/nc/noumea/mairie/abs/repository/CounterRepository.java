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

import nc.noumea.mairie.abs.domain.AgentReposCompCount;

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

	@Override
	public <T> T getEntity(Class<T> Tclass, Object Id) {
		return absEntityManager.find(Tclass, Id);
	}

	@Override
	public AgentReposCompCount getAgentReposCompCountByIdCounter(Integer IdCounter) {
		return absEntityManager.find(AgentReposCompCount.class, IdCounter);
	}

	@Override
	public List<Integer> getListAgentReposCompCountForResetAnneePrcd() {

		StringBuilder sb = new StringBuilder();
		sb.append("select c.idAgentReposCompCount from AgentReposCompCount c ");
		sb.append("where c.totalMinutesAnneeN1 <> 0 ");

		TypedQuery<Integer> query = absEntityManager.createQuery(sb.toString(), Integer.class);

		return query.getResultList();
	}

	@Override
	public List<Integer> getListAgentReposCompCountForResetAnneeEnCours() {

		StringBuilder sb = new StringBuilder();
		sb.append("select c.idAgentReposCompCount from AgentReposCompCount c ");
		sb.append("where c.totalMinutes <> 0 ");

		TypedQuery<Integer> query = absEntityManager.createQuery(sb.toString(), Integer.class);

		return query.getResultList();
	}

	@Override
	public <T> T getAgentCounterByDate(Class<T> T, Integer idAgent, Date dateDebut, Date dateFin) {

		// Build query criteria
		CriteriaBuilder cb = absEntityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(T);
		Root<T> c = cq.from(T);
		cq.select(c);
		ParameterExpression<Integer> p = cb.parameter(Integer.class, "idAgent");
		ParameterExpression<Date> p2 = cb.parameter(Date.class, "dateDebut");
		ParameterExpression<Date> p3 = cb.parameter(Date.class, "dateFin");
		cq.where(cb.and(cb.equal(c.get("idAgent"), p), cb.equal(c.get("dateDebut"), p2), cb.equal(c.get("dateFin"), p3)));

		// Build query
		TypedQuery<T> q = absEntityManager.createQuery(cq);
		q.setParameter("idAgent", idAgent);
		q.setParameter("dateDebut", dateDebut);
		q.setParameter("dateFin", dateFin);
		q.setMaxResults(1);

		// Exec query
		List<T> r = q.getResultList();

		return (r.size() == 1 ? r.get(0) : null);
	}

}
