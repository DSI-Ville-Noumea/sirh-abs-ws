package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import nc.noumea.mairie.abs.asa.domain.AgentAsaA55Count;
import nc.noumea.mairie.abs.domain.AgentCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;

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
	public List<Integer> getListAgentReposCompCountForResetAnneePrcd() {

		StringBuilder sb = new StringBuilder();
		sb.append("select c.idAgentCount from AgentReposCompCount c ");
		sb.append("where c.totalMinutesAnneeN1 <> 0 ");

		TypedQuery<Integer> query = absEntityManager.createQuery(sb.toString(), Integer.class);

		return query.getResultList();
	}

	@Override
	public List<Integer> getListAgentReposCompCountForResetAnneeEnCours() {

		StringBuilder sb = new StringBuilder();
		sb.append("select c.idAgentCount from AgentReposCompCount c ");
		sb.append("where c.totalMinutes <> 0 ");

		TypedQuery<Integer> query = absEntityManager.createQuery(sb.toString(), Integer.class);

		return query.getResultList();
	}

	@Override
	public <T> T getAgentCounterByDate(Class<T> T, Integer idAgent, Date date) {

		// Build query criteria
		CriteriaBuilder cb = absEntityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(T);
		Root<T> c = cq.from(T);
		cq.select(c);
		ParameterExpression<Integer> p = cb.parameter(Integer.class, "idAgent");
		ParameterExpression<Date> p2 = cb.parameter(Date.class, "dateDebut");
		cq.where(cb.and(cb.equal(c.get("idAgent"), p),
				cb.between(p2, c.<Date> get("dateDebut"), c.<Date> get("dateFin"))));

		// Build query
		TypedQuery<T> q = absEntityManager.createQuery(cq);
		q.setParameter("idAgent", idAgent);
		q.setParameter("dateDebut", date);
		q.setMaxResults(1);

		// Exec query
		List<T> r = q.getResultList();

		return (r.size() == 1 ? r.get(0) : null);
	}
	
	@Override
	public <T> T getOSCounterByDate(Class<T> T, Integer idOrganisationSyndicale, Date date) {

		// Build query criteria
		CriteriaBuilder cb = absEntityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(T);
		Root<T> c = cq.from(T);
		cq.select(c);
		Join<T, OrganisationSyndicale> j = c.join("organisationSyndicale");
		ParameterExpression<Integer> p = cb.parameter(Integer.class, "idOrganisationSyndicale");
		ParameterExpression<Date> p2 = cb.parameter(Date.class, "dateDebut");
		cq.where(
		cb.and(cb.equal(j.get("idOrganisationSyndicale"), p),
				cb.between(p2, c.<Date> get("dateDebut"), c.<Date> get("dateFin")))
				)
				;

		// Build query
		TypedQuery<T> q = absEntityManager.createQuery(cq);
	    q.setParameter("idOrganisationSyndicale", idOrganisationSyndicale);
		q.setParameter("dateDebut", date);
		q.setMaxResults(1);

		// Exec query
		List<T> r = q.getResultList();

		return (r.size() == 1 ? r.get(0) : null);
	}

	@Override
	public <T> List<T> getListCounter(Class<T> T) {

		// Build query criteria
		CriteriaBuilder cb = absEntityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(T);
		Root<T> c = cq.from(T);
		cq.select(c);
		cq.orderBy(cb.asc(c.get("idAgent")), cb.desc(c.get("dateDebut")));

		// Build query
		TypedQuery<T> q = absEntityManager.createQuery(cq);

		// Exec query
		List<T> r = q.getResultList();

		return r;
	}

	@Override
	public List<AgentHistoAlimManuelle> getListHisto(Integer idAgent, AgentCount compteurAgent) {
		TypedQuery<AgentHistoAlimManuelle> q = absEntityManager
				.createQuery(
						"from AgentHistoAlimManuelle h where h.compteurAgent.idAgentCount = :idAgentCount and h.idAgentConcerne = :idAgent order by h.dateModification desc ",
						AgentHistoAlimManuelle.class);
		q.setParameter("idAgentCount", compteurAgent.getIdAgentCount());
		q.setParameter("idAgent", idAgent);

		return q.getResultList();
	}

	@Override
	public List<AgentAsaA55Count> getListAgentCounterByDate(Integer idAgent, Date dateDebut, Date dateFin) {

		TypedQuery<AgentAsaA55Count> q = absEntityManager
				.createQuery(
						"from AgentAsaA55Count h where h.idAgent = :idAgent and h.dateDebut BETWEEN :dateDebut and :dateFin order by h.dateDebut asc ",
						AgentAsaA55Count.class);
		q.setParameter("idAgent", idAgent);
		q.setParameter("dateDebut", dateDebut);
		q.setParameter("dateFin", dateFin);

		return q.getResultList();
	}

}
