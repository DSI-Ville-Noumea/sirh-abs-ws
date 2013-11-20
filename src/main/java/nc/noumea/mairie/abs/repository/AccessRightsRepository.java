package nc.noumea.mairie.abs.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.Droit;

import org.springframework.stereotype.Repository;

@Repository
public class AccessRightsRepository implements IAccessRightsRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Override
	public List<Droit> getAgentAccessRights(int idAgent) {

		TypedQuery<Droit> q = absEntityManager.createNamedQuery("getAgentAccessRights", Droit.class);
		q.setParameter("idAgent", idAgent);

		return q.getResultList();
	}

}
