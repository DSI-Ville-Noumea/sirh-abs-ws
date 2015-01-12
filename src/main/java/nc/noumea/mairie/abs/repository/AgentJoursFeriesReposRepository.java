package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.AgentJoursFeriesRepos;

import org.springframework.stereotype.Repository;

@Repository
public class AgentJoursFeriesReposRepository implements
		IAgentJoursFeriesReposRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;
	
	@Override
	public AgentJoursFeriesRepos getAgentJoursFeriesReposByIdAgentAndJourFerie(
			Integer idAgent, Date jourFerie) {

		TypedQuery<AgentJoursFeriesRepos> query = null;
		query = absEntityManager.createNamedQuery("findAgentJoursFeriesReposByIdAgentAndJourFerie", AgentJoursFeriesRepos.class);
		query.setParameter("idAgent", idAgent);
		query.setParameter("jourFerie", jourFerie);
		query.setMaxResults(1);

		return query.getSingleResult();
	}

	@Override
	public List<AgentJoursFeriesRepos> getAgentJoursFeriesReposByIdAgentAndPeriode(
			Integer idAgent, Date dateDebut, Date dateFin) {
		
		TypedQuery<AgentJoursFeriesRepos> query = null;
		query = absEntityManager.createNamedQuery("findAgentJoursFeriesReposByIdAgentAndPeriode", AgentJoursFeriesRepos.class);
		query.setParameter("idAgent", idAgent);
		query.setParameter("dateDebut", dateDebut);
		query.setParameter("dateFin", dateFin);

		return query.getResultList();
	}

	@Override
	public List<AgentJoursFeriesRepos> getAgentJoursFeriesReposByPeriode(
			Date dateDebut, Date dateFin) {
		
		TypedQuery<AgentJoursFeriesRepos> query = null;
		query = absEntityManager.createNamedQuery("findAllAgentsJoursFeriesReposByPeriode", AgentJoursFeriesRepos.class);
		query.setParameter("dateDebut", dateDebut);
		query.setParameter("dateFin", dateFin);

		return query.getResultList();
	}

}
