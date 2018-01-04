package nc.noumea.mairie.abs.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import nc.noumea.mairie.abs.domain.AgentA48OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.AgentA54OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.AgentOrganisationSyndicale;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;

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
		return absEntityManager.createQuery("SELECT o FROM OrganisationSyndicale o order by o.sigle", OrganisationSyndicale.class).getResultList();
	}

	// #14737 tri par ordre alpha
	@Override
	public List<OrganisationSyndicale> findAllOrganisationActives() {
		return absEntityManager
				.createQuery("SELECT o FROM OrganisationSyndicale o where o.actif = true order by o.sigle", OrganisationSyndicale.class)
				.getResultList();
	}

	@Override
	public List<AgentOrganisationSyndicale> getListeAgentOrganisation(Integer idOrganisationSyndicale) {
		TypedQuery<AgentOrganisationSyndicale> query = absEntityManager.createQuery(
				"SELECT o FROM AgentOrganisationSyndicale o where o.organisationSyndicale.idOrganisationSyndicale = :idOrganisationSyndicale",
				AgentOrganisationSyndicale.class);
		query.setParameter("idOrganisationSyndicale", idOrganisationSyndicale);

		return query.getResultList();
	}

	@Override
	public List<AgentOrganisationSyndicale> getAgentOrganisation(Integer idAgent) {
		TypedQuery<AgentOrganisationSyndicale> query = absEntityManager
				.createQuery("SELECT o FROM AgentOrganisationSyndicale o where o.idAgent = :idAgent", AgentOrganisationSyndicale.class);
		query.setParameter("idAgent", idAgent);

		return query.getResultList();

	}

	@Override
	public AgentOrganisationSyndicale getAgentOrganisation(Integer idAgent, Integer idOrganisationSyndicale) {
		TypedQuery<AgentOrganisationSyndicale> query = absEntityManager.createQuery(
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
				"SELECT o FROM AgentOrganisationSyndicale o where o.idAgent = :idAgent and o.actif = true", AgentOrganisationSyndicale.class);
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

	@Override
	public List<AgentA54OrganisationSyndicale> getAgentA54Organisation(Integer idAgent) {
		TypedQuery<AgentA54OrganisationSyndicale> query = absEntityManager
				.createQuery("SELECT o FROM AgentA54OrganisationSyndicale o where o.idAgent = :idAgent", AgentA54OrganisationSyndicale.class);
		query.setParameter("idAgent", idAgent);

		return query.getResultList();
	}

	@Override
	public List<AgentA54OrganisationSyndicale> getAgentA54OrganisationByOS(Integer idOrganisationSyndicale, Integer pageSize, Integer pageNumber, Integer annee, Integer idAgentRecherche) {

		String sb = "SELECT os FROM AgentAsaA54Count asa, AgentA54OrganisationSyndicale os ";
		sb += "WHERE os.idAgent = asa.idAgent AND os.organisationSyndicale.idOrganisationSyndicale = :idOS ";
		if (annee != null) {
			sb += "AND extract(year from asa.dateDebut) = :annee ";
		}
		if (idAgentRecherche != null) {
			sb += "AND os.idAgent = :idAgentRecherche ";
		}
		sb += "order by asa.idAgent desc ";
		
		TypedQuery<AgentA54OrganisationSyndicale> query = absEntityManager.createQuery(sb, AgentA54OrganisationSyndicale.class);
		
		if (pageSize != null)
			query.setMaxResults(pageSize);
		
		if (pageNumber != null && pageSize != null) {
            query.setFirstResult(pageSize * (pageNumber - 1));
        }
		
		query.setParameter("idOS", idOrganisationSyndicale);
		
		if (annee != null) {
			query.setParameter("annee", annee);
		}		
		if (idAgentRecherche != null) {
			query.setParameter("idAgentRecherche", idAgentRecherche);
		}

		return query.getResultList();
	}

	@Override
	public List<AgentA48OrganisationSyndicale> getAgentA48Organisation(Integer idAgent) {
		TypedQuery<AgentA48OrganisationSyndicale> query = absEntityManager
				.createQuery("SELECT o FROM AgentA48OrganisationSyndicale o where o.idAgent = :idAgent", AgentA48OrganisationSyndicale.class);
		query.setParameter("idAgent", idAgent);

		return query.getResultList();
	}

	@Override
	public List<AgentA48OrganisationSyndicale> getAgentA48OrganisationByOS(Integer idOrganisationSyndicale, Integer pageSize, Integer pageNumber, Integer annee, Integer idAgentRecherche) {

		String sb = "SELECT os FROM AgentAsaA48Count asa, AgentA48OrganisationSyndicale os ";
		sb += "WHERE os.idAgent = asa.idAgent AND os.organisationSyndicale.idOrganisationSyndicale = :idOS ";
		if (annee != null) {
			sb += "AND extract(year from asa.dateDebut) = :annee ";
		}
		if (idAgentRecherche != null) {
			sb += "AND os.idAgent = :idAgentRecherche ";
		}
		sb += "order by asa.idAgent desc ";
		
		TypedQuery<AgentA48OrganisationSyndicale> query = absEntityManager.createQuery(sb, AgentA48OrganisationSyndicale.class);
		
		if (pageSize != null)
			query.setMaxResults(pageSize);
		
		if (pageNumber != null && pageSize != null) {
            query.setFirstResult(pageSize * (pageNumber - 1));
        }
		
		if (annee != null) {
			query.setParameter("annee", annee);
		}	
		if (idAgentRecherche != null) {
			query.setParameter("idAgentRecherche", idAgentRecherche);
		}
		
		query.setParameter("idOS", idOrganisationSyndicale);

		return query.getResultList();
	}

	@Override
	public <T, U> Integer countAllByidOSAndYear(Class<T> T, Class<U> U, Integer idOS, Integer annee,Integer idAgentRecherche) {

		String sb = "SELECT os FROM " + U.getSimpleName() + " asa, " + T.getSimpleName() + " os ";
		sb += "WHERE os.idAgent = asa.idAgent AND os.organisationSyndicale.idOrganisationSyndicale = :idOS ";
		if (annee != null) {
			sb += "AND extract(year from asa.dateDebut) = :annee ";
		}
		if (idAgentRecherche != null) {
			sb += "AND os.idAgent = :idAgentRecherche ";
		}
		sb += "order by asa.idAgent desc ";
		
		TypedQuery<T> query = absEntityManager.createQuery(sb, T);
		
		if (annee != null) {
			query.setParameter("annee", annee);
		}
		
		if (idAgentRecherche != null) {
			query.setParameter("idAgentRecherche", idAgentRecherche);
		}
		
		query.setParameter("idOS", idOS);

		return query.getResultList().size();
	}
}
