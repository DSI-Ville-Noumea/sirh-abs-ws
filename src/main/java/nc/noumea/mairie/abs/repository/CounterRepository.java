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

import nc.noumea.mairie.abs.domain.AgentAsaA52Count;
import nc.noumea.mairie.abs.domain.AgentAsaA55Count;
import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.AgentCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
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
	public <T> Integer countAllByYear(Class<T> T, Integer annee, Integer idAgentRecherche, Date dateMin, Date dateMax) {
		// Build query criteria
		StringBuilder sb = new StringBuilder();
		sb.append("select c from " + T.getSimpleName() + " c ");
		sb.append("where 1=1 ");
		
		if(annee != null){
			sb.append("and year(dateDebut) = :annee ");
		}
		else if (dateMin != null && dateMax != null) {
			sb.append("and dateDebut between :dateMin and :dateMax ");
		} else if (dateMin != null) {
			sb.append("and dateDebut >= :dateMin ");
		} else if (dateMax != null) {
			sb.append("and dateDebut <= :dateMax ");
		}
		if(idAgentRecherche != null){
			sb.append("and idAgent = :idAgentRecherche ");
		}

		TypedQuery<T> query = absEntityManager.createQuery(sb.toString(), T);
		
		if(annee!=null){
			query.setParameter("annee", annee);
		}
		else if (dateMin != null && dateMax != null) {
			query.setParameter("dateMin", dateMin);
			query.setParameter("dateMax", dateMax);
		} else if (dateMin != null) {
			query.setParameter("dateMin", dateMin);
		} else if (dateMax != null) {
			query.setParameter("dateMax", dateMax);
		}
		if(idAgentRecherche != null){
			query.setParameter("idAgentRecherche", idAgentRecherche);
		}

		return query.getResultList().size();
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
		cq.where(cb.and(cb.equal(j.get("idOrganisationSyndicale"), p),
				cb.between(p2, c.<Date> get("dateDebut"), c.<Date> get("dateFin"))));

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
	public List<AgentAsaA55Count> getListAgentCounterA55ByDate(Integer idAgent, Date dateDebut, Date dateFin) {

		TypedQuery<AgentAsaA55Count> q = absEntityManager
				.createQuery(
						"from AgentAsaA55Count h where h.idAgent = :idAgent and h.dateDebut BETWEEN :dateDebut and :dateFin order by h.dateDebut asc ",
						AgentAsaA55Count.class);
		q.setParameter("idAgent", idAgent);
		q.setParameter("dateDebut", dateDebut);
		q.setParameter("dateFin", dateFin);

		return q.getResultList();
	}

	@Override
	public List<Integer> getListAgentCongeAnnuelCountForReset() {

		StringBuilder sb = new StringBuilder();
		sb.append("select c.idAgentCount from AgentCongeAnnuelCount c ");

		TypedQuery<Integer> query = absEntityManager.createQuery(sb.toString(), Integer.class);

		return query.getResultList();
	}

	@Override
	public List<AgentHistoAlimManuelle> getListHistoOrganisationSyndicale(AgentCount compteurAgent) {
		TypedQuery<AgentHistoAlimManuelle> q = absEntityManager
				.createQuery(
						"from AgentHistoAlimManuelle h where h.compteurAgent.idAgentCount = :idAgentCount order by h.dateModification desc ",
						AgentHistoAlimManuelle.class);
		q.setParameter("idAgentCount", compteurAgent.getIdAgentCount());

		return q.getResultList();
	}

	@Override
	public void removeEntity(Object obj) {
		absEntityManager.remove(obj);
	}

	@Override
	public List<AgentAsaA52Count> getListOSCounterByDateAndOrganisation(Integer idOrganisationSyndicale,
			Date dateDebut, Date dateFin, Integer idCompteur) {

		StringBuilder sb = new StringBuilder();
		sb.append("from AgentAsaA52Count h ");
		sb.append("where h.organisationSyndicale.idOrganisationSyndicale = :idOrganisationSyndicale ");
		sb.append("and((:dateDebut between  h.dateDebut and h.dateFin or :dateFin between h.dateDebut and h.dateFin) or (h.dateDebut between :dateDebut and :dateFin or h.dateFin between :dateDebut and :dateFin)) ");
		if (idCompteur != null)
			sb.append("and h.idAgentCount != :idCompteur ");
		sb.append("order by h.dateDebut asc ");

		TypedQuery<AgentAsaA52Count> q = absEntityManager.createQuery(sb.toString(), AgentAsaA52Count.class);
		q.setParameter("idOrganisationSyndicale", idOrganisationSyndicale);
		q.setParameter("dateDebut", dateDebut);
		q.setParameter("dateFin", dateFin);
		if (idCompteur != null)
			q.setParameter("idCompteur", idCompteur);

		return q.getResultList();
	}

	@Override
	public AgentWeekRecup getWeekHistoRecupCountByIdAgentAndIdPointage(Integer idAgent, Integer idPointage) {

		TypedQuery<AgentWeekRecup> q = absEntityManager.createNamedQuery(
				"getWeekHistoRecupCountByIdAgentAndIdPointage", AgentWeekRecup.class);
		q.setParameter("idAgent", idAgent);
		q.setParameter("idPointage", idPointage);

		List<AgentWeekRecup> list = q.getResultList();
		if (null == list || list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}

	@Override
	public <T> List<T> getListCounterByOrganisation(Class<T> T, Integer idOrganisation) {

		// Build query criteria
		CriteriaBuilder cb = absEntityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(T);
		Root<T> c = cq.from(T);
		cq.select(c);

		Join<T, OrganisationSyndicale> j = c.join("organisationSyndicale");
		ParameterExpression<Integer> p = cb.parameter(Integer.class, "idOrganisationSyndicale");
		cq.where(cb.equal(j.get("idOrganisationSyndicale"), p));
		cq.orderBy(cb.asc(c.get("idAgent")), cb.desc(c.get("dateDebut")));

		// Build query
		TypedQuery<T> q = absEntityManager.createQuery(cq);
		q.setParameter("idOrganisationSyndicale", idOrganisation);

		// Exec query
		List<T> r = q.getResultList();

		return r;
	}

	@Override
	public List<AgentCongeAnnuelCount> getListAgentCongeAnnuelCountWithListAgents(List<Integer> listIdsAgent) {

		StringBuilder sb = new StringBuilder();
		sb.append("select c from AgentCongeAnnuelCount c ");
		sb.append("where c.idAgent in :listIdsAgent ");

		TypedQuery<AgentCongeAnnuelCount> query = absEntityManager.createQuery(sb.toString(), AgentCongeAnnuelCount.class);
		query.setParameter("listIdsAgent", listIdsAgent);

		return query.getResultList();
	}

	@Override
	public <T> List<T> getListCounterByAnnee(Class<T> T, Integer annee, Integer pageSize, Integer pageNumber) {
		StringBuilder sb = new StringBuilder();
		sb.append("select c from "+T.getSimpleName()+" c ");
		if(annee!=null){
		sb.append("where year(dateDebut) = :annee ");
		}
		sb.append("order by c.idAgent asc, c.dateDebut desc");

		TypedQuery<T> query = absEntityManager.createQuery(sb.toString(), T);
		
		if (pageSize != null)
			query.setMaxResults(pageSize);
		
		if (pageNumber != null && pageSize != null) {
			query.setFirstResult(pageSize * (pageNumber - 1));
		}
		
		if(annee!=null){
			query.setParameter("annee", annee);
		}
		return query.getResultList();
	}

	@Override
	public <T> List<T> getListCounterByDate(Class<T> T, Integer pageSize, Integer pageNumber, Integer idAgentRecherche, Date dateMin, Date dateMax) {
		StringBuilder sb = new StringBuilder();
		sb.append("select c from "+T.getSimpleName()+" c ");
		sb.append("where 1=1 ");
		
		if(dateMin != null && dateMax != null){
			sb.append("and dateDebut between :dateMin and :dateMax ");
		} else if (dateMin != null) {
			sb.append("and dateDebut >= :dateMin ");
		}else if (dateMax != null) {
			sb.append("and dateDebut <= :dateMax ");
		}
		
		if (idAgentRecherche != null)
			sb.append("and c.idAgent = :idAgentRecherche ");
		
		sb.append("order by c.idAgent asc, c.dateDebut desc ");

		TypedQuery<T> query = absEntityManager.createQuery(sb.toString(), T);
		
		if (pageSize != null)
			query.setMaxResults(pageSize);
		
		if (pageNumber != null && pageSize != null) {
			query.setFirstResult(pageSize * (pageNumber - 1));
		}

		if(dateMin != null && dateMax != null){
			query.setParameter("dateMin", dateMin);
			query.setParameter("dateMax", dateMax);
		} else if (dateMin != null) {
			query.setParameter("dateMin", dateMin);
		}else if (dateMax != null) {
			query.setParameter("dateMax", dateMax);
		}
		
		if (idAgentRecherche != null)
			query.setParameter("idAgentRecherche", idAgentRecherche);
		
		return query.getResultList();
	}

	@Override
	public <T> List<T> getListCounterByAnnee(Class<T> T, Integer annee) {
		StringBuilder sb = new StringBuilder();
		sb.append("select c from "+T.getSimpleName()+" c ");
		if(annee!=null){
		sb.append("where year(dateDebut) = :annee ");
		}
		sb.append("order by c.idAgent asc, c.dateDebut desc");

		TypedQuery<T> query = absEntityManager.createQuery(sb.toString(), T);
		
		if(annee!=null){
			query.setParameter("annee", annee);
		}
		return query.getResultList();
	}

}
