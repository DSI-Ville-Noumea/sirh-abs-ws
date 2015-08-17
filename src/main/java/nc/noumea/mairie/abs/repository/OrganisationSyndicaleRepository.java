package nc.noumea.mairie.abs.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.AgentOrganisationSyndicale;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;

import org.springframework.stereotype.Repository;

@Repository
public class OrganisationSyndicaleRepository implements IOrganisationSyndicaleRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Override
	public void persistEntity(Object obj) {
		absEntityManager.persist(obj);
	}

	@Override
	public <T> T getEntity(Class<T> Tclass, Object Id) {
		return absEntityManager.find(Tclass, Id);
	}

	// #14737 tri par ordre alpha
	@Override
	public List<OrganisationSyndicale> findAllOrganisation() {
		return absEntityManager.createQuery("SELECT o FROM OrganisationSyndicale o order by o.sigle", OrganisationSyndicale.class)
				.getResultList();
	}

	// #14737 tri par ordre alpha
	@Override
	public List<OrganisationSyndicale> findAllOrganisationActives() {
		return absEntityManager.createQuery("SELECT o FROM OrganisationSyndicale o where o.actif = true order by o.sigle",
				OrganisationSyndicale.class).getResultList();
	}

	@Override
	public List<AgentOrganisationSyndicale> getListeAgentOrganisation(Integer idOrganisationSyndicale) {
		TypedQuery<AgentOrganisationSyndicale> query = absEntityManager
				.createQuery(
						"SELECT o FROM AgentOrganisationSyndicale o where o.organisationSyndicale.idOrganisationSyndicale = :idOrganisationSyndicale",
						AgentOrganisationSyndicale.class);
		query.setParameter("idOrganisationSyndicale", idOrganisationSyndicale);

		return query.getResultList();
	}

	@Override
	public List<AgentOrganisationSyndicale> getAgentOrganisation(Integer idAgent) {
		TypedQuery<AgentOrganisationSyndicale> query = absEntityManager.createQuery(
				"SELECT o FROM AgentOrganisationSyndicale o where o.idAgent = :idAgent",
				AgentOrganisationSyndicale.class);
		query.setParameter("idAgent", idAgent);

		return query.getResultList();

	}

	@Override
	public AgentOrganisationSyndicale getAgentOrganisation(Integer idAgent, Integer idOrganisationSyndicale) {
		TypedQuery<AgentOrganisationSyndicale> query = absEntityManager
				.createQuery(
						"SELECT o FROM AgentOrganisationSyndicale o where o.idAgent = :idAgent and o.organisationSyndicale.idOrganisationSyndicale = :idOrganisationSyndicale ",
						AgentOrganisationSyndicale.class);
		query.setParameter("idAgent", idAgent);
		query.setParameter("idOrganisationSyndicale", idOrganisationSyndicale);
		try {
			return query.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public List<AgentOrganisationSyndicale> getAgentOrganisationActif(Integer idAgent) {
		TypedQuery<AgentOrganisationSyndicale> query = absEntityManager.createQuery(
				"SELECT o FROM AgentOrganisationSyndicale o where o.idAgent = :idAgent and o.actif = true",
				AgentOrganisationSyndicale.class);
		query.setParameter("idAgent", idAgent);

		return query.getResultList();
	}

	@Override
	public List<OrganisationSyndicale> getListOSCounterForA52() {

		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(h.organisationSyndicale) from AgentAsaA52Count h ");

		TypedQuery<OrganisationSyndicale> q = absEntityManager.createQuery(sb.toString(), OrganisationSyndicale.class);
		

		return q.getResultList();
	}
}
