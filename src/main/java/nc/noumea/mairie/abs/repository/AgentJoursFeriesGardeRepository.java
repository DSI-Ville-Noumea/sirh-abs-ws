package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.AgentJoursFeriesGarde;

import org.springframework.stereotype.Repository;

@Repository
public class AgentJoursFeriesGardeRepository implements IAgentJoursFeriesGardeRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Override
	public void persistEntity(Object obj) {
		absEntityManager.persist(obj);
	}

	@Override
	public void removeEntity(Object obj) {
		absEntityManager.remove(obj);
	}

	@Override
	public AgentJoursFeriesGarde getAgentJoursFeriesGardeByIdAgentAndJourFerie(Integer idAgent, Date jourFerieChome) {

		TypedQuery<AgentJoursFeriesGarde> query = null;
		query = absEntityManager.createNamedQuery("findAgentJoursFeriesGardeByIdAgentAndJourFerie",
				AgentJoursFeriesGarde.class);
		query.setParameter("idAgent", idAgent);
		query.setParameter("jourFerieChome", jourFerieChome);
		query.setMaxResults(1);

		if (0 < query.getResultList().size()) {
			return query.getSingleResult();
		}

		return null;
	}

	@Override
	public List<AgentJoursFeriesGarde> getAgentJoursFeriesGardeByIdAgentAndPeriode(Integer idAgent, Date dateDebut,
			Date dateFin) {

		TypedQuery<AgentJoursFeriesGarde> query = null;
		query = absEntityManager.createNamedQuery("findAgentJoursFeriesGardeByIdAgentAndPeriode",
				AgentJoursFeriesGarde.class);
		query.setParameter("idAgent", idAgent);
		query.setParameter("dateDebut", dateDebut);
		query.setParameter("dateFin", dateFin);

		return query.getResultList();
	}

	@Override
	public List<AgentJoursFeriesGarde> getAgentJoursFeriesGardeByPeriode(Date dateDebut, Date dateFin) {

		TypedQuery<AgentJoursFeriesGarde> query = null;
		query = absEntityManager
				.createNamedQuery("findAllAgentsJoursFeriesGardeByPeriode", AgentJoursFeriesGarde.class);
		query.setParameter("dateDebut", dateDebut);
		query.setParameter("dateFin", dateFin);

		return query.getResultList();
	}

}
